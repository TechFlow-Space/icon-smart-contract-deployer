package io.contractdeployer.generics.airdrop;

import io.contractdeployer.generics.airdrop.exception.AirdropException;
import score.Address;
import score.ArrayDB;
import score.BranchDB;
import score.Context;
import score.DictDB;
import score.annotation.EventLog;
import score.annotation.External;
import scorex.util.ArrayList;

import java.math.BigInteger;

import java.util.List;

public class Airdrop implements InterfaceAirdrop {

    public static final String TAG = "Airdrop";
    public static final ArrayDB<AirdropDetails> userData = Context.newArrayDB("userData", AirdropDetails.class);
    public static final BranchDB<Address, DictDB<String, BigInteger>> distributedTokens =
            Context.newBranchDB("distributedTokens", BigInteger.class);

    public static final String IRC2 = "irc-2";
    public static final String IRC3 = "irc-3";
    public static final String IRC31 = "irc-31";

    @External(readonly = true)
    public String name() {
        return TAG;
    }

    @External(readonly = true)
    public List<AirdropDetails> getDistributionOf() {
        List<AirdropDetails> details = new ArrayList<>();
        int count = userData.size();
        for (int i = 0; i < count; i++) {
            AirdropDetails airdropDetails = userData.get(i);
            details.add(airdropDetails);
        }
        return details;
    }


    @External
    public void addReceipients(Address[] _recipients, BigInteger[] _amounts, BigInteger[] _timestamp) {
        onlyOwner();
        Context.require(_recipients.length == _amounts.length, AirdropException.lengthMismatch());
        Context.require(_recipients.length == _timestamp.length, AirdropException.lengthMismatch());
        Context.require(checkValidTimeStamp(_timestamp), AirdropException.invalidTimestamp());
        int count = _recipients.length;

        for (int i = 0; i < count; i++) {
            AirdropDetails airdropDetails = new AirdropDetails(_recipients[i], _amounts[i], _timestamp[i]);
            userData.add(airdropDetails);
        }

    }

    @External
    public void airdropIRC2(Address _tokenAddress) {
        onlyOwner();
        Context.require(checkMinter(Context.getAddress()), AirdropException.notMinter());

        int count = userData.size();

        for (int i = 0; i < count; i++) {
            AirdropDetails airdropDetails = userData.get(i);

            // TODO: check this before call
            BigInteger timestamp = airdropDetails.timestamp;
            BigInteger currentTime = BigInteger.valueOf(Context.getBlockTimestamp());
            Context.require(timestamp.compareTo(currentTime) >= 0,
                    AirdropException.unknown("Cannot call before timestamp"));


            Context.call(_tokenAddress, "mint", airdropDetails.userAddress, airdropDetails.amount);

            distributedTokens.at(airdropDetails.userAddress).set(IRC2, airdropDetails.amount);

            AirdropToken(_tokenAddress, IRC2, airdropDetails.userAddress, airdropDetails.amount);

        }
    }

    @External
    public void airdropIRC2(Address _tokenAddress, Address[] _recipients, BigInteger[] _amounts) {

        Context.require(_recipients.length == _amounts.length, AirdropException.lengthMismatch());
        Context.require(checkMinter(Context.getAddress()), AirdropException.notMinter());
        int count = _recipients.length;

        for (int i = 0; i < count; i++) {
            Context.call(_tokenAddress, "mint", _recipients[i], _amounts[i]);

            distributedTokens.at(_recipients[i]).set(IRC2, _amounts[i]);

            AirdropToken(_tokenAddress, IRC2, _recipients[i], _amounts[i]);
        }
    }

    @External
    public void airdropIRC3(Address _tokenAddress, Address[] _from, Address[] _recipients, BigInteger[] _tokenId) {
        Context.require(_recipients.length == _tokenId.length, AirdropException.lengthMismatch());
        int count = _recipients.length;

        checkApproval(_tokenAddress, _tokenId);

        for (int i = 0; i < count; i++) {
            Context.call(_tokenAddress, "transferFrom", _from[i], _recipients[i], _tokenId[i]); // check the transfer
            distributedTokens.at(_recipients[i]).set(IRC3, _tokenId[i]); // sender ko info is not saved
            AirdropToken(_from[i], IRC3, _recipients[i], _tokenId[i]);
        }

    }

    @External
    public void airdropIRC31(Address _tokenAddress, Address[] _from, Address[] _recipients,
                             BigInteger[] _tokenId, BigInteger[] _amount) {
        Context.require(_recipients.length == _tokenId.length, AirdropException.lengthMismatch());
        Context.require(_recipients.length == _amount.length, AirdropException.lengthMismatch());
        int count = _recipients.length;

        checkApprovalForAll(_tokenAddress, _from);

        for (int i = 0; i < count; i++) {
            Context.call(_tokenAddress, "transferFrom", _from[i], _recipients[i], _tokenId[i], _amount[i]);
            distributedTokens.at(_recipients[i]).set(IRC31, _amount[i]); // check this
            AirdropToken(_from[i], IRC31, _recipients[i], _tokenId[i]);// check the transfer
        }
    }

    @EventLog(indexed = 4)
    public void AirdropToken(Address from, String key, Address to, BigInteger amount) {
    }

    protected boolean checkValidTimeStamp(BigInteger[] timestamp) {
        boolean isValid = false;
        for (BigInteger time : timestamp) {
            isValid = time.toString().length() == 16;
        }
        return isValid;
    }

    protected void checkApprovalForAll(Address address, Address[] owner) {
        for (Address addr : owner) {
            boolean approved = Context.call(boolean.class, address, "isApprovedForAll", addr);
            Context.require(approved, AirdropException.approvalRequiredForAll(addr));
        }

    }

    protected void checkApproval(Address address, BigInteger[] tokenId) {
        for (BigInteger id : tokenId) {
            Address approved = Context.call(Address.class, address, "getApproved", id);
            Address caller = Context.getAddress();
            Context.require(caller.equals(approved), AirdropException.approvalRequired(id));
        }
    }


    protected boolean checkMinter(Address tokenAddress) {
        Address minter = Context.call(Address.class, tokenAddress, "getMinter");
        Address caller = Context.getAddress();
        if (!(minter == null)) {
            return minter.equals(caller);
        }
        return false;

    }

    protected void onlyOwner() {
        Context.require(Context.getCaller().equals(Context.getOwner()), AirdropException.notOwner());
    }
}
