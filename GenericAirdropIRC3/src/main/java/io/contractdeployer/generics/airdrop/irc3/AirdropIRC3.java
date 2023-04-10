package io.contractdeployer.generics.airdrop.irc3;

import io.contractdeployer.generics.airdrop.irc3.exception.AirdropIRC3Exception;
import score.Address;
import score.ArrayDB;
import score.Context;
import score.DictDB;
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
        distributedTokens.set(_recipients, _tokenId); // sender ko info is not saved
        IRC3Airdrop(_from, _recipients, _tokenId);
    }


    @External
    public void airdropIRC3Batch(Address _tokenAddress, Address[] _from, Address[] _recipients, BigInteger[] _tokenId) {
        Context.require(_recipients.length == _tokenId.length, AirdropIRC3Exception.lengthMismatch());
        int count = _recipients.length;

        checkApprovalBatch(_tokenAddress, _tokenId);

        for (int i = 0; i < count; i++) {
            call(_tokenAddress, "transferFrom", _from[i], _recipients[i], _tokenId[i]); // check the transfer
            distributedTokens.set(_recipients[i], _tokenId[i]); // sender ko info is not saved
            IRC3Airdrop(_from[i], _recipients[i], _tokenId[i]);
        }

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
