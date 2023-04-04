package io.contractdeployer.generics.irc3;

import com.iconloop.score.token.irc3.IRC3Basic;
import io.contractdeployer.generics.irc3.exception.IRC3Exception;
import score.Address;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

import java.math.BigInteger;


public class IRC3 extends IRC3Basic {
    public static final String TAG = "IRC3";
    public final String ADMIN="admin";
    public final String MINT_COST="mint_cost";
    public final String CAP="cap";
    public final String TOKEN_URI="token_uri";
    public final String TOKEN_ID="token_id";
    public final VarDB<BigInteger> mintCost = Context.newVarDB(MINT_COST, BigInteger.class);
    public final VarDB<BigInteger> tokenId = Context.newVarDB(TOKEN_ID, BigInteger.class);
    public final VarDB<BigInteger> cap = Context.newVarDB(CAP, BigInteger.class);
    public final VarDB<Address> admin = Context.newVarDB(ADMIN, Address.class);
    public final DictDB<BigInteger, String> tokenURIs = Context.newDictDB(TOKEN_URI, String.class);

    public IRC3(String _name, String _symbol, BigInteger _cap, BigInteger _mintCost) {
        super(_name,_symbol);

        if (cap.get() == null) {

            Context.require(_mintCost.compareTo(BigInteger.ZERO) >= 0, IRC3Exception.negative());
            Context.require(_cap.compareTo(BigInteger.ZERO) > 0, IRC3Exception.zeroOrNegative());

            tokenId.set(BigInteger.ZERO);
            cap.set(_cap);
            mintCost.set(_mintCost);
            admin.set(Context.getCaller());
        }

    }

    @External(readonly = true)
    public BigInteger getMintCost() {
        return mintCost.get();
    }

    @External
    public void setMintCost(BigInteger _mintCost) {
        onlyAdmin();
        mintCost.set(_mintCost);
    }

    @External(readonly = true)
    public BigInteger getTokenId() {
        return tokenId.get();
    }

    @External(readonly = true)
    public String getTokenUri(BigInteger _tokenId) {
        return tokenURIs.get(_tokenId);
    }

    @External(readonly = true)
    public BigInteger getCap() {
        return cap.get();
    }

    @External(readonly = true)
    public Address getAdmin() {
        return admin.get();
    }

    @External
    public void setAdmin(Address adminAddress) {
        onlyOwner();
        admin.set(adminAddress);
        TransferAdmin(Context.getOwner(),adminAddress);
    }

    @EventLog
    public void TransferAdmin(Address _oldAmin, Address _newAdmin){}
    @External
    public BigInteger totalSupplyOf(){
        return BigInteger.valueOf(super.totalSupply());
    }


    @External
    @Payable
    public void mint(String _uri) {
        Context.require(getPaidValue().equals(getMintCost()), IRC3Exception.priceMismatch());
        Context.require(BigInteger.valueOf(totalSupply() + 1).compareTo(getCap()) <= 0, IRC3Exception.capExceeded());

        Address caller = Context.getCaller();
        tokenId.set(getTokenId().add(BigInteger.ONE));
        tokenURIs.set(getTokenId(), _uri);
        _mint(caller, getTokenId());
        Mint(caller,getTokenId());
    }

    @External
    public void burn(BigInteger _tokenId) {
        Address owner = ownerOf(_tokenId);
        Address caller = Context.getCaller();
        Context.require(owner.equals(caller) || getApproved(_tokenId).equals(caller), IRC3Exception.notApproved());

        _burn(_tokenId);
        Burn(caller,_tokenId);

    }

    protected BigInteger getPaidValue() {
        return Context.getValue();
    }

    private void onlyOwner() {
        Context.require(Context.getCaller().equals(Context.getOwner()), IRC3Exception.notOwner());
    }

    private void onlyAdmin() {
        Context.require(Context.getCaller().equals(this.getAdmin())
                        || Context.getCaller().equals(Context.getOwner()), IRC3Exception.notAdmin());
    }

    @EventLog(indexed = 3)
    public void Transfer(Address _from, Address _to, BigInteger _tokenId) {
    }

    @EventLog(indexed = 2)
    public void Burn(Address _from, BigInteger _tokenId){}

    @EventLog(indexed = 2)
    public void Mint(Address _to, BigInteger _tokenId){}

}