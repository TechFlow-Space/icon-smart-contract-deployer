package io.contractdeployer.generics.airdrop.irc3;

import io.contractdeployer.generics.airdrop.irc3.exception.AirdropIRC3Exception;
import score.Address;
import score.ArrayDB;
import score.Context;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import scorex.util.ArrayList;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class AirdropIRC3 {

    public static final String TAG = "Airdrop IRC3";

    public static final ArrayDB<UserDetails> userData = Context.newArrayDB("userData", UserDetails.class);

    public static final VarDB<Integer> airdropCount = Context.newVarDB("airdrop_count",Integer.class);
    public static final VarDB<Integer> processedCount = Context.newVarDB("processed_count",Integer.class);

    @External(readonly = true)
    public String name() {
        return TAG;
    }

    @External(readonly = true)
    public int getAirdroppedCount(){
        return airdropCount.getOrDefault(0);
    }

    @External(readonly = true)
    public int getProcessedCount(){
        return processedCount.getOrDefault(0);
    }

    @External(readonly = true)
    public List<Map<String,Object>> getListedUserInfo() {
        List<Map<String,Object>> details = new ArrayList<>();
        int count = userData.size();
        for (int i = 0; i < count; i++) {
            UserDetails userDetails = userData.get(i);
            details.add(userDetails.toObject());
        }
        return details;
    }

    @External
    public void addRecipients(Address[] _tokenAddress,Address[] _owners ,Address[] _recipients, BigInteger[] _tokenId, BigInteger[] _timestamp) {
        onlyOwner();
        Context.require(_recipients.length == _tokenId.length, AirdropIRC3Exception.lengthMismatch());
        Context.require(_recipients.length == _timestamp.length, AirdropIRC3Exception.lengthMismatch());
        Context.require(_recipients.length == _owners.length, AirdropIRC3Exception.lengthMismatch());
        Context.require(checkValidTimeStamp(_timestamp), AirdropIRC3Exception.invalidTimestamp());

        int count = _recipients.length;
        int currentCount = getAirdroppedCount();
        airdropCount.set(currentCount+count);

        for (int i = currentCount; i < count; i++) {
            UserDetails userDetails = new UserDetails(_tokenAddress[i],_owners[i],_recipients[i], _tokenId[i], _timestamp[i]);
            userData.add(userDetails);
        }

    }

    @External
    public void airdropToListedUsers(int paymentToProcess){
        onlyOwner();// reentrancy
        int totalPayed = getAirdroppedCount();
        int countOfProcess = processedCount.getOrDefault(0);

        Context.require(paymentToProcess+countOfProcess <= totalPayed,AirdropIRC3Exception.invalidPayments());
        processedCount.set(countOfProcess+paymentToProcess);

        for (int i = countOfProcess; i < (countOfProcess+paymentToProcess); i++) {
            UserDetails userDetails = userData.get(i);

            BigInteger timestamp = userDetails.timestamp;
            BigInteger currentTime = now();
            Context.require(timestamp.compareTo(currentTime) <= 0,
                    AirdropIRC3Exception.unknown("Cannot call before timestamp."));

            airdropIRC3(userDetails.tokenAddress, userDetails.tokenOwner, userDetails.userAddress, userDetails.tokenId);
        }
    }

    @EventLog(indexed = 3)
    public void IRC3Airdropped(Address from, Address to, BigInteger tokenId) {
    }

    private void airdropIRC3(Address _tokenAddress, Address _from, Address _recipient, BigInteger _tokenId) {

        checkApproval(_tokenAddress, _tokenId);

        call(_tokenAddress, "transferFrom", _from, _recipient, _tokenId);

        IRC3Airdropped(_from, _recipient, _tokenId);
    }

    public BigInteger now(){
        return  BigInteger.valueOf(Context.getBlockTimestamp());
    }

    protected boolean checkValidTimeStamp(BigInteger[] timestamp) {
        boolean isValid = false;
        for (BigInteger time : timestamp) {
            isValid = time.toString().length() == 16;
        }
        return isValid;
    }

    protected void checkApproval(Address address, BigInteger tokenId){
        Address approved = call(Address.class, address, "getApproved", tokenId);
        Address caller = getAddress();
        Context.require(caller.equals(approved), AirdropIRC3Exception.approvalRequired(tokenId));
    }

    public Address getAddress(){
        return Context.getAddress();
    }

    protected void onlyOwner() {
        Context.require(Context.getCaller().equals(Context.getOwner()), AirdropIRC3Exception.notOwner());
    }

    public void call(Address contract, String method, Object... params) {
        Context.call(contract, method, params);
    }

    public <K> K call(Class<K> kClass, Address contract, String method, Object... params) {
        return Context.call(kClass, contract, method, params);
    }

}
