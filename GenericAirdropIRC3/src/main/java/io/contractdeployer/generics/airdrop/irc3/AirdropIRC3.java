package io.contractdeployer.generics.airdrop.irc3;

import io.contractdeployer.generics.airdrop.irc3.exception.AirdropIRC3Exception;
import score.Address;
import score.ArrayDB;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import scorex.util.ArrayList;

import java.math.BigInteger;
import java.util.List;

public class AirdropIRC3 {

    public static final String TAG = "Airdrop IRC3";

    public static final DictDB<Address,BigInteger> distributedTokens =
            Context.newDictDB("distributedTokens-irc3",BigInteger.class);

    public static final ArrayDB<UserDetails> userData = Context.newArrayDB("userData", UserDetails.class);

    public static final VarDB<Integer> airdropCount = Context.newVarDB("airdrop_count",Integer.class);
    public static final VarDB<Integer> processedCount = Context.newVarDB("airdrop_count",Integer.class);

    @External(readonly = true)
    public Integer getAirdropedCount(){
        return airdropCount.getOrDefault(0);
    }
    @External(readonly = true)
    public String name() {
        return TAG;
    }

    @External(readonly = true)
    public BigInteger getDistributedTokensOf(Address recipient){
        return distributedTokens.getOrDefault(recipient,BigInteger.ZERO);
    }

    @External(readonly = true)
    public List<UserDetails> getListedUserInfo() {
        List<UserDetails> details = new ArrayList<>();
        int count = userData.size();
        for (int i = 0; i < count; i++) {
            UserDetails userDetails = userData.get(i);
            details.add(userDetails);
        }
        return details;
    }

    @External
    public void airdropIRC3(Address _tokenAddress, Address _from, Address _recipients, BigInteger _tokenId) {

        checkApproval(_tokenAddress, _tokenId);

        call(_tokenAddress, "transferFrom", _from, _recipients, _tokenId); // check the transfer
        UserDetails userDetails = new UserDetails(_tokenAddress,_from,_recipients, _tokenId,BigInteger.ZERO);
        userData.set(getAirdropedCount(),userDetails);

        processedCount.set(processedCount.get()+1);
        IRC3Airdrop(_from, _recipients, _tokenId);
    }


    @External
    public void airdropIRC3Batch(Address _tokenAddress, Address[] _from, Address[] _recipients, BigInteger[] _tokenId) {
        Context.require(_recipients.length == _tokenId.length, AirdropIRC3Exception.lengthMismatch());
        int count = _recipients.length;

        checkApprovalBatch(_tokenAddress, _tokenId);

        for (int i = 0; i < count; i++) {
            call(_tokenAddress, "transferFrom", _from[i], _recipients[i], _tokenId[i]); // check the transfer
            UserDetails userDetails = new UserDetails(_tokenAddress,_from[i],_recipients[i], _tokenId[i],BigInteger.ZERO);
            userData.set(getAirdropedCount(),userDetails);

            processedCount.set(processedCount.get()+i);
            IRC3Airdrop(_from[i], _recipients[i], _tokenId[i]);
        }

    }

    @External
    public void addRecipients(Address[] _tokenAddress,Address[] _owners ,Address[] _recipients, BigInteger[] _tokenId, BigInteger[] _timestamp) {
        onlyOwner();
        Context.require(_recipients.length == _tokenId.length, AirdropIRC3Exception.lengthMismatch());
        Context.require(_recipients.length == _timestamp.length, AirdropIRC3Exception.lengthMismatch());
        Context.require(_recipients.length == _owners.length, AirdropIRC3Exception.lengthMismatch());
        Context.require(checkValidTimeStamp(_timestamp), AirdropIRC3Exception.invalidTimestamp());

        int count = _recipients.length;
        int currentCount = getAirdropedCount();
        airdropCount.set(currentCount+count);

        for (int i = currentCount; i < count; i++) {
            UserDetails userDetails = new UserDetails(_tokenAddress[i],_owners[i],_recipients[i], _tokenId[i], _timestamp[i]);
            userData.set(i,userDetails);
        }

    }

    @External
    public void airdropToListedUsers(int paymentToProcess){ // reentrancy
        int totalPayed = getAirdropedCount();
        int countOfProcess = processedCount.getOrDefault(0);

        Context.require(paymentToProcess+countOfProcess <= totalPayed,AirdropIRC3Exception.invalidPayments());
        processedCount.set(countOfProcess+paymentToProcess);

        for (int i = countOfProcess; i < (countOfProcess+paymentToProcess); i++) {
            UserDetails userDetails = userData.get(i);

            BigInteger timestamp = userDetails.timestamp;
            BigInteger currentTime = getBlockTimestamp();
            Context.require(timestamp.compareTo(currentTime) <= 0,
                    AirdropIRC3Exception.unknown("Cannot call before timestamp."));

            airdropIRC3(userDetails.tokenAddress, userDetails.tokenOwner, userDetails.userAddress, userDetails.tokenId);
        }
    }

    public BigInteger getBlockTimestamp(){
        return  BigInteger.valueOf(Context.getBlockTimestamp());
    }

    protected boolean checkValidTimeStamp(BigInteger[] timestamp) {
        boolean isValid = false;
        for (BigInteger time : timestamp) {
            isValid = time.toString().length() == 16;
        }
        return isValid;
    }


    protected void checkApprovalBatch(Address address, BigInteger[] tokenId) {
        for (BigInteger id : tokenId) {
            checkApproval(address,id);
        }
    }

    protected void checkApproval(Address address, BigInteger tokenId){
        Address approved = call(Address.class, address, "getApproved", tokenId);
        Address caller = Context.getAddress();
        Context.require(caller.equals(approved), AirdropIRC3Exception.approvalRequired(tokenId));
    }

    protected void onlyOwner() {
        Context.require(Context.getCaller().equals(Context.getOwner()), AirdropIRC3Exception.notOwner());
    }


    @EventLog(indexed = 3)
    public void IRC3Airdrop(Address from, Address to, BigInteger tokenId) {
    }

    public void call(Address contract, String method, Object... params) {
        Context.call(contract, method, params);
    }

    public <K> K call(Class<K> kClass, Address contract, String method, Object... params) {
        return Context.call(kClass, contract, method, params);
    }

}
