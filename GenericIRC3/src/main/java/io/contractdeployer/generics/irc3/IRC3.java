package io.contractdeployer.generics.irc3;

import static io.contractdeployer.generics.irc3.Vars.*;
import com.iconloop.score.util.IntSet;
import score.Address;
import score.Context;

import score.annotation.EventLog;
import score.annotation.External;

import java.math.BigInteger;

public class IRC3 implements InterfaceIRC3 {

    public IRC3(String _name, String _symbol) {
        // initialize values only at first deployment
        if (name.get() == null) {
            name.set(_name);
            symbol.set(_symbol);
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
    public Address getAdmin() {
        return admin.get();
    }

    @External
    public void setAdmin(Address adminAddress) {
        this.ownerRequired();
        admin.set(adminAddress);
    }

    @External(readonly=true)
    public int balanceOf(Address _owner) {
        Context.require(!ZERO_ADDRESS.equals(_owner), Message.Found.zeroAddr("_owner"));
        var tokens = holderTokens.get(_owner);
        return (tokens != null) ? tokens.length() : 0;
    }

    @External(readonly=true)
    public Address ownerOf(BigInteger _tokenId) {
        return tokenOwners.getOrThrow(_tokenId, Message.noToken());
    }

    @External(readonly=true)
    public Address getApproved(BigInteger _tokenId) {
        return tokenApprovals.getOrDefault(_tokenId, ZERO_ADDRESS);
    }

    @External
    public void approve(Address _to, BigInteger _tokenId) {
        Address owner = ownerOf(_tokenId);
        Context.require(!owner.equals(_to), Message.ownerApproval());
        Context.require(owner.equals(Context.getCaller()), Message.Not.tokenOwner());
        _approve(_to, _tokenId);
    }

    private void _approve(Address to, BigInteger tokenId) {
        tokenApprovals.set(tokenId, to);
        Approval(ownerOf(tokenId), to, tokenId);
    }

    @External
    public void transfer(Address _to, BigInteger _tokenId) {
        Address owner = ownerOf(_tokenId);
        Context.require(owner.equals(Context.getCaller()), Message.Not.tokenOwner());
        _transfer(owner, _to, _tokenId);
    }

    @External
    public void transferFrom(Address _from, Address _to, BigInteger _tokenId) {
        Address owner = ownerOf(_tokenId);
        Address spender = Context.getCaller();
        Context.require(owner.equals(spender) || getApproved(_tokenId).equals(spender), Message.Not.operatorApproved());
        _transfer(_from, _to, _tokenId);
    }

    private void _transfer(Address from, Address to, BigInteger tokenId) {
        Context.require(ownerOf(tokenId).equals(from), Message.Not.tokenOwner());
        Context.require(!to.equals(ZERO_ADDRESS), Message.Found.zeroAddr("to"));
        // clear approvals from the previous owner
        _approve(ZERO_ADDRESS, tokenId);

        _removeTokenFrom(tokenId, from);
        _addTokenTo(tokenId, to);
        tokenOwners.set(tokenId, to);
        Transfer(from, to, tokenId);
    }

    /**
     * (Extension) Returns the total amount of tokens stored by the contract.
     */
    @External(readonly=true)
    public int totalSupply() {
        return tokenOwners.length();
    }

    /**
     * (Extension) Returns a token ID at a given index of all the tokens stored by the contract.
     * Use along with {@code _totalSupply} to enumerate all tokens.
     */
    @External(readonly=true)
    public BigInteger tokenByIndex(int _index) {
        return tokenOwners.getKey(_index);
    }

    /**
     * (Extension) Returns a token ID owned by owner at a given index of its token list.
     * Use along with {@code balanceOf} to enumerate all of owner's tokens.
     */
    @External(readonly=true)
    public BigInteger tokenOfOwnerByIndex(Address _owner, int _index) {
        var tokens = holderTokens.get(_owner);
        return (tokens != null) ? tokens.at(_index) : BigInteger.ZERO;
    }

    @External
    public void mint(Address _to,BigInteger _tokenId){
        this.adminRequired();
        _mint(_to,_tokenId);
    }

    @External
    public void burn(BigInteger _tokenId){
        Address owner=ownerOf(_tokenId);
        Address caller=Context.getCaller();
        Context.require(owner.equals(caller) || getApproved(_tokenId).equals(caller),Message.Not.operatorApproved());

        _burn(_tokenId);
    }

    /**
     * Mints `tokenId` and transfers it to `to`.
     */
    protected void _mint(Address to, BigInteger tokenId) {
        Context.require(!ZERO_ADDRESS.equals(to), Message.Found.zeroAddr("to"));
        Context.require(!_tokenExists(tokenId), Message.Found.token());

        _addTokenTo(tokenId, to);
        tokenOwners.set(tokenId, to);
        Transfer(ZERO_ADDRESS, to, tokenId);
    }

    /**
     * Destroys `tokenId`.
     */
    protected void _burn(BigInteger tokenId) {
        Address owner = ownerOf(tokenId);
        // clear approvals
        _approve(ZERO_ADDRESS, tokenId);

        _removeTokenFrom(tokenId, owner);
        tokenOwners.remove(tokenId);
        Transfer(owner, ZERO_ADDRESS, tokenId);
    }

    protected boolean _tokenExists(BigInteger tokenId) {
        return tokenOwners.contains(tokenId);
    }

    private void _addTokenTo(BigInteger tokenId, Address to) {
        var tokens = holderTokens.get(to);
        if (tokens == null) {
            tokens = new IntSet(to.toString());
            holderTokens.set(to, tokens);
        }
        tokens.add(tokenId);
    }

    private void _removeTokenFrom(BigInteger tokenId, Address from) {
        var tokens = holderTokens.get(from);
        Context.require(tokens != null, Message.noOwnerTokens());
        tokens.remove(tokenId);
        if (tokens.length() == 0) {
            holderTokens.set(from, null);
        }
    }

    private void ownerRequired() {
        Context.require(Context.getCaller().equals(Context.getOwner()), Message.Not.owner());
    }

    private void adminRequired() {
        Context.require(Context.getCaller().equals(this.getAdmin()) || Context.getCaller().equals(Context.getOwner()),
                Message.Not.admin());
    }

    @EventLog(indexed=3)
    public void Transfer(Address _from, Address _to, BigInteger _tokenId) {
    }

    @EventLog(indexed=3)
    public void Approval(Address _owner, Address _approved, BigInteger _tokenId) {
    }
}