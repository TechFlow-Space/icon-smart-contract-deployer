package io.contractdeployer.generics.airdrop.irc31;

import io.contractdeployer.generics.airdrop.irc31.exception.AirdropIRC31Exception;
import score.Address;
import score.ArrayDB;
import score.BranchDB;
import score.Context;
import score.DictDB;
import score.annotation.EventLog;
import score.annotation.External;

import java.math.BigInteger;

public class AirdropIRC31 {

    public static final String TAG = "Airdrop IRC31";

    public static final BranchDB<Address,DictDB<BigInteger,BigInteger> >distributedTokens =
            Context.newBranchDB("distributedTokens-irc31",BigInteger.class);

    public static final ArrayDB<UserDetails> userData = Context.newArrayDB("userData", UserDetails.class);


    @External(readonly = true)
    public String name() {
        return TAG;
    }

    @External(readonly = true)
    public BigInteger getDistributedTokensOf(Address recipient,BigInteger tokenId){
        return distributedTokens.at(recipient).getOrDefault(tokenId,BigInteger.ZERO);
    }

//    @External(readonly = true)
//    public List<UserDetails> getListedUserInfo() {
//        List<UserDetails> details = new ArrayList<>();
//        int count = userData.size();
//        for (int i = 0; i < count; i++) {
//            UserDetails userDetails = userData.get(i);
//            details.add(userDetails);
//        }
//        return details;
//    }

    @External
    public void airdropIRC31Batch(Address _tokenAddress, Address[] _from, Address[] _recipients,
                                  BigInteger[] _tokenId, BigInteger[] _value) {
        Context.require(_recipients.length == _tokenId.length, AirdropIRC31Exception.lengthMismatch());
        Context.require(_recipients.length == _value.length, AirdropIRC31Exception.lengthMismatch());
        Context.require(_recipients.length == _tokenId.length, AirdropIRC31Exception.lengthMismatch());
        int count = _recipients.length;

        checkApprovalForAll(_tokenAddress, _from);

        for (int i = 0; i < count; i++) {
            Context.call(_tokenAddress, "transferFrom", _from[i], _recipients[i], _tokenId[i], _value[i]);
            distributedTokens.at(_recipients[i]).set(_tokenId[i], _value[i]); // check this
            IRC31Airdrop(_from[i], _recipients[i], _tokenId[i],_value[i]);// check the transfer
        }
    }

    @External
    public void airdropIRC31(Address _tokenAddress, Address _from, Address _recipient,
                                  BigInteger _tokenId, BigInteger _value) {

        checkApproval(_tokenAddress, _from);


        Context.call(_tokenAddress, "transferFrom", _from, _recipient, _tokenId, _value);
        distributedTokens.at(_recipient).set(_tokenId, _value); // check this
        IRC31Airdrop(_from, _recipient, _tokenId,_value);// check the transfer

    }

    protected void checkApproval(Address address, Address owner) {

        boolean approved = Context.call(boolean.class, address, "isApprovedForAll", owner);
        Context.require(approved, AirdropIRC31Exception.approvalRequiredForAll(owner));

    }

    protected void checkApprovalForAll(Address address, Address[] owner) {
        for (Address addr : owner) {
            checkApproval(address,addr);
        }

    }

    @EventLog(indexed = 3)
    public void IRC31Airdrop(Address from,Address to,BigInteger tokenId,BigInteger value) {
    }
}
