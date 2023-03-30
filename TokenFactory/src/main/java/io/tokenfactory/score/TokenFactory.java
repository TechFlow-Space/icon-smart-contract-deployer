package io.tokenfactory.score;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import io.tokenfactory.score.dbs.ContentDB;
import io.tokenfactory.score.dbs.ContractDB;
import io.tokenfactory.score.utils.IterableDictDB;
import score.*;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;
import scorex.util.ArrayList;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static io.tokenfactory.score.Constant.*;
import static io.tokenfactory.score.enums.ContractName.contractNameValidate;
import static score.Context.*;

public class TokenFactory {

    private final VarDB<Address> admin = newVarDB(ADMIN, Address.class);
    private final VarDB<Address> treasury = newVarDB(TREASURY, Address.class);
    private final VarDB<BigInteger> deployFee = newVarDB(DEPLOY_FEE, BigInteger.class);
    private final IterableDictDB<String, ContentDB> content = new IterableDictDB<>(SCORE_CONTENT, ContentDB.class, String.class, false);
    private final IterableDictDB<BigInteger, ContractDB> deploymentDetail = new IterableDictDB<>(DEPLOYMENT_DETAIL, ContractDB.class, BigInteger.class, false);
    private final BranchDB<Address, ArrayDB<ContractDB>> deployerDetail = newBranchDB(CONTRACT_DEPLOYER, ContractDB.class);

    public TokenFactory(Address admin, Address treasury, BigInteger deployFee) {
        if (this.admin.get() == null) {
            this.admin.set(admin);
            this.treasury.set(treasury);
            this.deployFee.set(deployFee);
        }
    }

    @External(readonly = true)
    public Address getAdmin() {
        return this.admin.get();
    }

    @External
    public void setAdmin(Address admin) {
        ownerOnly();
        this.admin.set(admin);
    }

    @External(readonly = true)
    public Address getTreasury() {
        return this.treasury.get();
    }

    @External
    public void setTreasury(Address treasury) {
        ownerOnly();
        this.treasury.set(treasury);
    }

    @External
    public void setContractContent(String name, byte[] content, @Optional boolean isUpdate) {
        adminOnly();
        contractNameValidate(name);
        BigInteger timestamp = BigInteger.valueOf(getBlockTimestamp());
        Address caller = getCaller();

        if (!isUpdate) {
            require(!this.content.keys().contains(name), Message.duplicateContract());
        }

        ContentDB contentDB = new ContentDB(new String(content), timestamp, caller, name);
        this.content.set(name, contentDB);
        if (isUpdate) {
            this.ContentUpdated(name, caller);
        } else {
            this.ContentSet(name, caller);
        }
    }

    @External(readonly = true)
    public List<Map<String, Object>> getContracts() {
        List<Map<String, Object>> contracts = new ArrayList<>();
        int size = content.keys().size();
        for (int i = 0; i < size; i++) {
            String key = content.keys().get(i);
            contracts.add(content.get(key).toObject());
        }
        return contracts;
    }

    @External(readonly = true)
    public Map<String, Object> getContractContent(String key) {
        return this.content.get(key).toObject();
    }

    @External(readonly = true)
    public BigInteger getDeployFee() {
        return this.deployFee.get();
    }

    @External
    @Payable
    public void deployContract(String key, Address deployer, byte[] _data) {
        require(!deployer.equals(ZERO_ADDRESS), Message.zeroAddress());
        require(getPaidValue().equals(this.getDeployFee()), Message.paymentMismatch());
        require(this.content.keys().contains(key), Message.Not.deployed());

        BigInteger currentSize = BigInteger.valueOf(this.deploymentDetail.keys().size() + 1);
        byte[] content = (byte[]) this.content.get(key).getContent().getBytes();
        Address contract = deployContract(key, content, _data);
        setScoreOwner(contract, deployer);

        ContractDB contractDB = new ContractDB(currentSize, key, deployer, BigInteger.valueOf(getBlockTimestamp()), contract);
        this.deploymentDetail.set(currentSize, contractDB);
        this.deployerDetail.at(deployer).add(contractDB);
        this.ContractDeployed(contract, deployer, key);
    }

    Address deployContract(String key, byte[] content, byte[] _data) {
        JsonObject data = unpackAndFetchObject(_data);
        switch (key) {
            case "IRC2":
                return deploy(content, data.get("name").asString(), data.get("symbol").asString(), new BigInteger(data.get("decimal").asString()));
            case "IRC3":
                return deploy(content, data.get("name").asString(), data.get("symbol").asString(),
                        new BigInteger(data.get("cap").asString()), new BigInteger(data.get("mintCost").asString()));
            case "IRC31":
                return deploy(content, data.get("name").asString(), data.get("symbol").asString(),
                        new BigInteger(data.get("cap").asString()), new BigInteger(data.get("maxBatchMintCount").asString()));
            case "NFT_AUCTION":
            case "AIRDROP":
            case "MARKETPLACE":
            case "DAO":
                return deploy(content);
            default:
                throw new UserRevertedException(Message.Not.validContract());
        }
    }

    JsonObject unpackAndFetchObject(byte[] _data) {
        String unpackedData = new String(_data);
        return Json.parse(unpackedData).asObject();
    }


    @External(readonly = true)
    public ContractDB[] getDeployedContracts(Address deployer, int offset, int limit, String order) {
        int deployedCount = this.deployerDetail.at(deployer).size();
        ArrayDB<ContractDB> deployerDetail = this.deployerDetail.at(deployer);
        int maxCount = Math.min(offset + limit, deployedCount);
        ContractDB[] deployedContracts = new ContractDB[maxCount - offset];

        if (order.equals("desc")) {
            for (int i = maxCount - 1, j = 0; i >= offset; i--, j++) {
                deployedContracts[j] = deployerDetail.get(i);
            }
        } else {
            for (int i = offset, j = 0; i < maxCount; i++, j++) {
                deployedContracts[j] = deployerDetail.get(i);
            }
        }
        return deployedContracts;
    }

    @External(readonly = true)
    public int deployCount() {
        return this.deploymentDetail.keys().size();
    }

    @External
    public void transferToTreasury(BigInteger amount) {
        this.ownerOnly();
        transfer(getTreasury(), amount);
    }

    protected BigInteger getPaidValue() {
        return Context.getValue();
    }

    protected void setScoreOwner(Address contract, Address deployer) {
        call(CHAIN_ADDRESS, "setScoreOwner", contract, deployer);
    }

    @EventLog(indexed = 2)
    public void ContractDeployed(Address contract, Address deployer, String name) {
    }

    @EventLog(indexed = 2)
    public void ContentSet(String name, Address caller) {
    }

    @EventLog(indexed = 2)
    public void ContentUpdated(String name, Address caller) {
    }

    void ownerOnly() {
        require(getCaller().equals(getOwner()), Message.Not.owner());
    }

    void adminOnly() {
        require(getCaller().equals(getAdmin()), Message.Not.admin());
    }
}
