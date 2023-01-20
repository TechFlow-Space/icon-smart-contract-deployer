package io.tokenfactory.score;

import io.tokenfactory.score.dbs.ContractDB;
import io.tokenfactory.score.utils.ArrayDBUtils;
import io.tokenfactory.score.utils.IterableDictDB;
import score.Address;
import score.ArrayDB;
import score.BranchDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static io.tokenfactory.score.Constant.*;
import static score.Context.*;

public class TokenFactory {

    private final VarDB<Address> admin = newVarDB(ADMIN, Address.class);
    private final VarDB<Address> treasury = newVarDB(TREASURY, Address.class);
    private final VarDB<BigInteger> deployFee = newVarDB(DEPLOY_FEE, BigInteger.class);
    private final IterableDictDB<String, byte[]> content = new IterableDictDB<>(SCORE_CONTENT, byte[].class, String.class, false);
    private final IterableDictDB<BigInteger, ContractDB> deploymentDetail = new IterableDictDB<>(DEPLOYMENT_DETAIL, ContractDB.class, BigInteger.class, false);
    private final BranchDB<Address, ArrayDB<ContractDB>> deployerDetail = newBranchDB(CONTRACT_DEPLOYER, ContractDB.class);

    public TokenFactory(Address admin, Address treasury, BigInteger deployFee){
        if(this.admin.get() == null){
            this.admin.set(admin);
            this.treasury.set(treasury);
            this.deployFee.set(deployFee);
        }
    }

    @External
    public void setAdmin(Address admin){
        ownerOnly();
        this.admin.set(admin);
    }

    @External(readonly = true)
    public Address getAdmin(){
        return this.admin.get();
    }

    @External
    public void setTreasury(Address treasury){
        ownerOnly();
        this.treasury.set(treasury);
    }

    @External(readonly = true)
    public Address getTreasury(){
        return this.treasury.get();
    }

    @External
    public void setContractContent(String name, byte[] content){
        adminOnly();
        require(availableContracts.contains(name), Message.Not.validContract());
        require(!this.content.keys().contains(name), Message.duplicateContract());
        this.content.set(name, content);
    }

    @External(readonly = true)
    public List<String> getAvailableContractContent(){
        return this.content.keys();
    }

    @External(readonly = true)
    public byte[] getContractContent(String key){
        return this.content.get(key);
    }

    @External(readonly = true)
    public BigInteger getDeployFee(){
        return this.deployFee.get();
    }

    @External
    @Payable
    public void deployContract(String key, Address deployer){
        require(!deployer.equals(ZERO_ADDRESS), Message.zeroAddress());
        require(getValue().equals(this.getDeployFee()), Message.paymentMismatch());
        require(this.content.keys().contains(key), Message.Not.deployed());

        var currentSize = BigInteger.valueOf(this.deploymentDetail.keys().size() + 1);
        var content = this.content.get(key);

        Address contract = deploy(content, this.getAdmin(), this.getTreasury(), this.getDeployFee());
        call(CHAIN_ADDRESS, "setScoreOwner",contract, deployer);

        ContractDB contractDB = new ContractDB(currentSize, key, deployer, BigInteger.valueOf(getBlockTimestamp()), contract);
        this.deploymentDetail.set(currentSize, contractDB);
        this.deployerDetail.at(deployer).add(contractDB);
        this.ContractDeployed(deployer, key);
    }

    @External(readonly = true)
    public ContractDB getDeployedContracts(Address deployer){
        return this.deployerDetail.at(deployer).get(0);
    }

    @External(readonly = true)
    public int deployCount(){
        return this.deploymentDetail.keys().size();
    }

    @External
    public void transferToTreasury(BigInteger amount){
        this.ownerOnly();
        transfer(getTreasury(), amount);
    }

    @EventLog(indexed = 2)
    public void ContractDeployed(Address deployer, String name){}

    void ownerOnly(){
        require(getCaller().equals(getOwner()), Message.Not.owner());
    }

    void adminOnly(){
        require(getCaller().equals(getAdmin()), Message.Not.admin());
    }
}
