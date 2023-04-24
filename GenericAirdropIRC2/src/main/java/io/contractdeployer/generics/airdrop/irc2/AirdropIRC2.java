package io.contractdeployer.generics.airdrop.irc2;

import io.contractdeployer.generics.airdrop.irc2.exception.AirdropIRC2Exception;
import score.Address;
import score.ArrayDB;
import score.Context;
import score.DictDB;
import score.annotation.EventLog;
import score.annotation.External;
import scorex.util.ArrayList;

import java.math.BigInteger;
import java.util.List;

public class AirdropIRC2 {

    public static final String TAG = "Airdrop IRC2";

    public static final DictDB<Address,BigInteger> distributedTokens =
            Context.newDictDB("distributedTokens-irc2",BigInteger.class);

    public static final ArrayDB<UserDetails> userData = Context.newArrayDB("userData", UserDetails.class);


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
    public void airdropIRC2(Address _tokenAddress, Address _recipient, BigInteger _amount){
        Context.require(checkMinter(_tokenAddress), AirdropIRC2Exception.notMinter());

        call(_tokenAddress, "mint", _recipient, _amount);

        BigInteger amount = distributedTokens.getOrDefault(_recipient,BigInteger.ZERO);
        distributedTokens.set(_recipient,_amount.add(amount));

        IRC2Airdropped(_recipient, _amount);

    }

    @External
    public void airdropIRC2Batch(Address _tokenAddress, Address[] _recipients, BigInteger[] _amounts) {

        Context.require(_recipients.length == _amounts.length, AirdropIRC2Exception.lengthMismatch());
        Context.require(checkMinter(_tokenAddress), AirdropIRC2Exception.notMinter());
        int count = _recipients.length;

        for (int i = 0; i < count; i++) {
            call(_tokenAddress, "mint", _recipients[i], _amounts[i]);

            BigInteger amount = distributedTokens.getOrDefault(_recipients[i],BigInteger.ZERO);

            distributedTokens.set(_recipients[i], _amounts[i].add(amount));

            IRC2Airdropped(_recipients[i], _amounts[i]);
        }
    }

    @External
    public void addRecipients(Address[] _recipients, BigInteger[] _amounts, BigInteger[] _timestamp) {
        onlyOwner();
        Context.require(_recipients.length == _amounts.length, AirdropIRC2Exception.lengthMismatch());
        Context.require(_recipients.length == _timestamp.length, AirdropIRC2Exception.lengthMismatch());
        Context.require(checkValidTimeStamp(_timestamp), AirdropIRC2Exception.invalidTimestamp());
        int count = _recipients.length;

        for (int i = 0; i < count; i++) {
            UserDetails userDetails = new UserDetails(_recipients[i], _amounts[i], _timestamp[i]);
            userData.add(userDetails);
        }

    }

    @External
    public void airdropToListedUsers(Address _tokenAddress) {
        Context.require(checkMinter(_tokenAddress), AirdropIRC2Exception.notMinter());

        int count = userData.size();

        for (int i = 0; i < count; i++) {
             UserDetails userDetails = userData.get(i);

            BigInteger timestamp = userDetails.timestamp;
            BigInteger currentTime = getBlockTimestamp();
            Context.require(timestamp.compareTo(currentTime) <= 0,
                    AirdropIRC2Exception.unknown("Cannot call before timestamp."));


            call(_tokenAddress, "mint", userDetails.userAddress, userDetails.amount);

            BigInteger amount = distributedTokens.getOrDefault(userDetails.userAddress,BigInteger.ZERO);
            distributedTokens.set(userDetails.userAddress, userDetails.amount.add(amount));

            IRC2Airdropped(userDetails.userAddress, userDetails.amount);

        }
        emptyUserData(count);

    }

    public BigInteger getBlockTimestamp(){
        return  BigInteger.valueOf(Context.getBlockTimestamp());
    }

    private void emptyUserData(int count){
        for (int i = 0; i < count; i++) {
            userData.pop();
        }
    }

    public boolean checkMinter(Address tokenAddress) {
        Address minter = call(Address.class, tokenAddress, "getMinter");
        Address caller = Context.getAddress();
        if (!(minter == null)) {
            return minter.equals(caller);
        }
        return false;

    }

    protected void onlyOwner() {
        Context.require(Context.getCaller().equals(Context.getOwner()), AirdropIRC2Exception.notOwner());
    }

    protected boolean checkValidTimeStamp(BigInteger[] timestamp) {
        boolean isValid = false;
        for (BigInteger time : timestamp) {
            isValid = time.toString().length() == 16;
        }
        return isValid;
    }

    @EventLog(indexed = 2)
    public void IRC2Airdropped(Address to, BigInteger amount) {
    }

    public void call(Address contract, String method, Object... params) {
        Context.call(contract, method, params);
    }

    public <K> K call(Class<K> kClass, Address contract, String method, Object... params) {
        return Context.call(kClass, contract, method, params);
    }

}
