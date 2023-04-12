package io.contractdeployer.generics.airdrop.irc31;

import io.contractdeployer.generics.airdrop.irc31.exception.AirdropIRC31Exception;
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

public class AirdropIRC31 {

    public static final String TAG = "Airdrop IRC31";

    public static final VarDB<Integer> airdropCount = Context.newVarDB("airdrop_count",Integer.class);
    public static final VarDB<Integer> processedCount = Context.newVarDB("processed_count",Integer.class);


    public static final ArrayDB<UserDetails> userData = Context.newArrayDB("userData", UserDetails.class);

    @External(readonly = true)
    public int getAirdroppedCount(){
        return airdropCount.getOrDefault(0);
    }

    @External(readonly = true)
    public int getProcessedCount(){
        return processedCount.getOrDefault(0);
    }
    @External(readonly = true)
    public String name() {
        return TAG;
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
    public void addRecipients(Address[] _tokenAddress,Address[] _owners ,
                              Address[] _recipients, BigInteger[] _tokenId, BigInteger[] _value,BigInteger[] _timestamp) {
        onlyOwner();
        Context.require(_recipients.length == _tokenId.length, AirdropIRC31Exception.lengthMismatch());
        Context.require(_recipients.length == _timestamp.length, AirdropIRC31Exception.lengthMismatch());
        Context.require(_recipients.length == _owners.length, AirdropIRC31Exception.lengthMismatch());
        Context.require(checkValidTimeStamp(_timestamp), AirdropIRC31Exception.invalidTimestamp());

        int count = _recipients.length;
        int currentCount = getAirdroppedCount();
        airdropCount.set(currentCount+count);

        for (int i = currentCount; i < count; i++) {
            UserDetails userDetails = new UserDetails(_tokenAddress[i],_owners[i],_recipients[i], _tokenId[i],_value[i], _timestamp[i]);
            userData.add(userDetails);
        }

    }

    @External
    public void airdropToListedUsers(int paymentToProcess){
        onlyOwner();// reentrancy
        int totalPayed = getAirdroppedCount();
        int countOfProcess = processedCount.getOrDefault(0);

        Context.require(paymentToProcess+countOfProcess <= totalPayed,AirdropIRC31Exception.invalidPayments());
        processedCount.set(countOfProcess+paymentToProcess);

        for (int i = countOfProcess; i < (countOfProcess+paymentToProcess); i++) {
            UserDetails userDetails = userData.get(i);

            BigInteger timestamp = userDetails.timestamp;
            BigInteger currentTime = now();
            Context.require(timestamp.compareTo(currentTime) <= 0,
                    AirdropIRC31Exception.unknown("Cannot call before timestamp."));

            airdropIRC31(userDetails.tokenAddress, userDetails.tokenOwner,
                    userDetails.userAddress, userDetails.tokenId,userDetails.value);
        }
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

    protected void onlyOwner() {
        Context.require(Context.getCaller().equals(Context.getOwner()), AirdropIRC31Exception.notOwner());
    }


//    @External
//    public void airdropIRC31Batch(Address _tokenAddress, Address[] _from, Address[] _recipients,
//                                  BigInteger[] _tokenId, BigInteger[] _value) {
//        Context.require(_recipients.length == _tokenId.length, AirdropIRC31Exception.lengthMismatch());
//        Context.require(_recipients.length == _value.length, AirdropIRC31Exception.lengthMismatch());
//        Context.require(_recipients.length == _tokenId.length, AirdropIRC31Exception.lengthMismatch());
//        int count = _recipients.length;
//
//        checkApprovalForAll(_tokenAddress, _from);
//
//        for (int i = 0; i < count; i++) {
//            Context.call(_tokenAddress, "transferFrom", _from[i], _recipients[i], _tokenId[i], _value[i]);
//
//            IRC31Airdrop(_from[i], _recipients[i], _tokenId[i],_value[i]);// check the transfer
//        }
//    }

    @External
    public void airdropIRC31(Address _tokenAddress, Address _from, Address _recipient,
                                  BigInteger _tokenId, BigInteger _value) {

        checkApproval(_tokenAddress, _from);


        Context.call(_tokenAddress, "transferFrom", _from, _recipient, _tokenId, _value);

        IRC31Airdrop(_from, _recipient, _tokenId,_value);

    }

    protected void checkApproval(Address address, Address owner) {

        boolean approved = Context.call(boolean.class, address, "isApprovedForAll", owner);
        Context.require(approved, AirdropIRC31Exception.approvalRequiredForAll(owner));

    }

//    protected void checkApprovalForAll(Address address, Address[] owner) {
//        for (Address addr : owner) {
//            checkApproval(address,addr);
//        }
//
//    }

    @EventLog(indexed = 3)
    public void IRC31Airdrop(Address from,Address to,BigInteger tokenId,BigInteger value) {
    }
}
