package io.contractdeployer.generics.irc31;

import com.iconloop.score.token.irc31.IRC31Basic;
import io.contractdeployer.generics.irc31.exception.IRC31Exception;
import score.Address;
import score.Context;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;

import java.math.BigInteger;

import static io.contractdeployer.generics.irc31.Constant.ADMIN;
import static io.contractdeployer.generics.irc31.Constant.CAP;
import static io.contractdeployer.generics.irc31.Constant.MAX_BATCH_MINT;
import static io.contractdeployer.generics.irc31.Constant.NAME;
import static io.contractdeployer.generics.irc31.Constant.SYMBOL;
import static io.contractdeployer.generics.irc31.Constant.TOTAL_SUPPLY;


public class IRC31 extends IRC31Basic implements InterfaceIRC31{

    public static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    public final VarDB<String> name = Context.newVarDB(NAME, String.class);
    public final VarDB<String> symbol = Context.newVarDB(SYMBOL, String.class);
    public final VarDB<BigInteger> cap = Context.newVarDB(CAP, BigInteger.class);
    public final VarDB<BigInteger> maxBatchMintCount = Context.newVarDB(MAX_BATCH_MINT, BigInteger.class);
    public final VarDB<BigInteger> totalSupply = Context.newVarDB(TOTAL_SUPPLY, BigInteger.class);
    public final VarDB<Address> admin = Context.newVarDB(ADMIN, Address.class);

    public IRC31(String _name, String _symbol,BigInteger _cap,BigInteger _maxBatchMintCount) {

        if (name.get() == null) {
            name.set(ensureNotEmpty(_name));
            symbol.set(ensureNotEmpty(_symbol));

            Context.require(_cap.compareTo(BigInteger.ZERO)>0, IRC31Exception.lessThanZero());
            Context.require(_maxBatchMintCount.compareTo(BigInteger.ZERO)>0, IRC31Exception.lessThanZero());

            name.set(_name);
            symbol.set(_symbol);
            admin.set(Context.getCaller());
            cap.set(_cap);
            maxBatchMintCount.set(_maxBatchMintCount);
            totalSupply.set(BigInteger.ZERO);
        }
    }



    @External(readonly=true)
    public String name() {
        return name.get();
    }

    @External(readonly=true)
    public String symbol() {
        return symbol.get();
    }

    @External(readonly = true)
    public BigInteger getMaxBatchMintCount() {
        return maxBatchMintCount.get();
    }

    @External(readonly = true)
    public BigInteger getTotalSupply() {
        return totalSupply.getOrDefault(BigInteger.ZERO);
    }

    @External(readonly = true)
    public BigInteger getCap(){
        return cap.get();
    }

    @External(readonly = true)
    public Address getAdmin() {
        return admin.get();
    }

    @External
    public void setAdmin(Address _address) {
        this.onlyOwner();
        admin.set(_address);
        TransferAdmin(Context.getOwner(),_address);

    }

    @EventLog(indexed = 2)
    public void TransferAdmin(Address _oldAmin, Address _newAdmin){}

    @External
    public void mint(Address _to, BigInteger _id, BigInteger _amount,String _uri) {
        this.preMintConditions(_to,_amount);
        _mintInternal(_to, _id, _amount,_uri);

        TransferSingle(Context.getCaller(), ZERO_ADDRESS, _to, _id, _amount);
    }

    @External
    public void mintBatch(Address _owner, BigInteger[] _ids, BigInteger[] _amounts,String[] _uris) {
        Context.require(_ids.length == _amounts.length, IRC31Exception.pairMismatch());
        Context.require(_ids.length == _uris.length, IRC31Exception.pairMismatch());

        for (int i = 0; i < _ids.length; i++) {
            BigInteger id = _ids[i];
            BigInteger amount = _amounts[i];
            this.preMintConditions(_owner,amount);
            _mintInternal(_owner, id, amount,_uris[i]);
        }

        // emit transfer event for Mint semantic
        TransferBatch(_owner, ZERO_ADDRESS, _owner, rlpEncode(_ids), rlpEncode(_amounts));
    }

    @External
    public void burn(Address _owner, BigInteger _id, BigInteger _amount) {
        this.preBurnConditions(_owner,_id,_amount);
        _burnInternal(_owner, _id, _amount);

        TransferSingle(_owner, _owner, ZERO_ADDRESS, _id, _amount);
    }

    @External
    public void burnBatch(Address _owner, BigInteger[] _ids, BigInteger[] _amounts) {
        Context.require(_ids.length == _amounts.length, IRC31Exception.pairMismatch());

        for (int i = 0; i < _ids.length; i++) {
            BigInteger id = _ids[i];
            BigInteger amount = _amounts[i];
            this.preBurnConditions(_owner,id,amount);
            _burnInternal(_owner, id, amount);
        }

        TransferBatch(_owner, _owner, ZERO_ADDRESS, rlpEncode(_ids), rlpEncode(_amounts));
    }

    // ================================================
    // Internal methods
    // ================================================

    private void _mintInternal(Address owner, BigInteger id, BigInteger amount,String uri) {

        totalSupply.set(getTotalSupply().add(amount));
        _setTokenURI(id,uri);

        super._mint(owner, id, amount);
    }

    private void _burnInternal(Address owner, BigInteger id, BigInteger amount) {

        totalSupply.set(getTotalSupply().subtract(amount));

        super._burn(owner,id,amount);
    }

    private void onlyOwner() {
        Context.require(Context.getCaller().equals(Context.getOwner()), IRC31Exception.notOwner());
    }

    private void onlyAdminOrOwner() {
        Context.require(Context.getCaller().equals(this.getAdmin()) || Context.getCaller().equals(Context.getOwner()),
                IRC31Exception.notAdmin());
    }

    private void preMintConditions(Address address, BigInteger amount) {
        onlyAdminOrOwner();
        Context.require(!address.equals(ZERO_ADDRESS), IRC31Exception.zeroAddr());
        Context.require(amount.compareTo(BigInteger.ZERO) > 0,IRC31Exception.lessThanZero());
        Context.require(amount.compareTo(getMaxBatchMintCount())<=0, IRC31Exception.nftCountPerTxRange());
        Context.require(getTotalSupply().add(amount).compareTo(getCap())<=0, IRC31Exception.capExceeded());
    }

    private void preBurnConditions(Address address, BigInteger id, BigInteger amount) {
        final Address caller = Context.getCaller();
        Context.require(!address.equals(ZERO_ADDRESS), IRC31Exception.zeroAddr());
        Context.require(amount.compareTo(BigInteger.ZERO) > 0, IRC31Exception.lessThanZero());
        Context.require(address.equals(caller) || this.isApprovedForAll(address, caller),
                IRC31Exception.notApproved());
        Context.require(balanceOf(address,id).compareTo(amount)>=0, IRC31Exception.insufficientBalance());
    }

    private String ensureNotEmpty(String str) {
        Context.require(str != null && !str.trim().isEmpty(), IRC31Exception.empty());
        assert str != null;
        return str.trim();
    }

}
