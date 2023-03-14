package io.contractdeployer.generics.irc2;

import score.Address;
import score.Context;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;

import java.math.BigInteger;

import static io.contractdeployer.generics.irc2.Vars.*;

public class IRC2 implements InterfaceIRC2 {

    public IRC2(String _name, String _symbol,BigInteger _decimals) {

        if (name.get() == null) {
            name.set(ensureNotEmpty(_name));
            symbol.set(ensureNotEmpty(_symbol));

            Context.require(_decimals.intValue() >= 0, Message.greaterThanZero("Decimals"));
            Context.require(_decimals.intValue() <= 21, "decimals needs to be equal or lower than 21");
            decimals.set(_decimals);

            Address minterAddress = minter.get();
            if (minterAddress == null) {
                minter.set(Context.getOwner());
            }
        }
    }

    private String ensureNotEmpty(String str) {
        Context.require(str != null && !str.trim().isEmpty(), Message.empty("String"));
        assert str != null;
        return str.trim();
    }

    @External(readonly=true)
    public String name() {
        return name.get();
    }

    @External(readonly=true)
    public String symbol() {
        return symbol.get();
    }

    @External(readonly=true)
    public BigInteger decimals() {
        return decimals.get();
    }

    @External(readonly=true)
    public BigInteger totalSupply() {
        return totalSupply.getOrDefault(BigInteger.ZERO);
    }

    @External(readonly=true)
    public BigInteger balanceOf(Address _owner) {
        return safeGetBalance(_owner);
    }

    @External
    public void transfer(Address _to, BigInteger _value, @Optional byte[] _data) {
        Address _from = Context.getCaller();

        Context.require(_to != _from, Message.Not.self());
        Context.require(_value.compareTo(BigInteger.ZERO) > 0, Message.greaterThanZero("_value"));
        Context.require(safeGetBalance(_from).compareTo(_value) >= 0, Message.Not.enoughBalance());

        safeSetBalance(_from, safeGetBalance(_from).subtract(_value));
        safeSetBalance(_to, safeGetBalance(_to).add(_value));

        byte[] dataBytes = (_data == null) ? new byte[0] : _data;
        if (_to.isContract()) {
            Context.call(_to, "tokenFallback", _from, _value, dataBytes);
        }

        Transfer(_from, _to, _value, dataBytes);
    }

    @External
    public void mint(BigInteger _amount, Address address) {
        Context.require(Context.getCaller().equals(minter.get()), Message.Not.minter());
        _mint(address, _amount);
    }

    @External
    public void setMinter(Address _minter) {
        ownerRequired();
        Address currentMinter = getMinter();
        minter.set(_minter);
    }

    @External(readonly = true)
    public Address getMinter() {
        return minter.get();
    }

    @External
    public void burn(BigInteger _amount) {
        Address caller = Context.getCaller();
        _burn(caller, _amount);
    }

    protected void _mint(Address owner, BigInteger amount) {
        Context.require(!ZERO_ADDRESS.equals(owner), Message.Found.zeroAddr("Owner"));
        Context.require(amount.compareTo(BigInteger.ZERO) > 0, Message.greaterThanZero("Amount"));

        totalSupply.set(totalSupply.getOrDefault(BigInteger.ZERO).add(amount));
        safeSetBalance(owner, safeGetBalance(owner).add(amount));
        Transfer(ZERO_ADDRESS, owner, amount, "mint".getBytes());
    }

    protected void _burn(Address owner, BigInteger amount) {
        Context.require(!ZERO_ADDRESS.equals(owner), Message.Found.zeroAddr("Owner"));
        Context.require(amount.compareTo(BigInteger.ZERO) > 0, Message.greaterThanZero("Amount"));
        Context.require(safeGetBalance(owner).compareTo(amount) >= 0, Message.Not.enoughBalance());

        safeSetBalance(owner, safeGetBalance(owner).subtract(amount));
        totalSupply.set(totalSupply.getOrDefault(BigInteger.ZERO).subtract(amount));
        Transfer(owner, ZERO_ADDRESS, amount, "burn".getBytes());
    }

    private BigInteger safeGetBalance(Address owner) {
        return balances.getOrDefault(owner, BigInteger.ZERO);
    }

    private void safeSetBalance(Address owner, BigInteger amount) {
        balances.set(owner, amount);
    }

    protected void ownerRequired(){
        Context.require(Context.getCaller().equals(Context.getOwner()), Message.Not.owner());
    }

    @EventLog(indexed=3)
    public void Transfer(Address _from, Address _to, BigInteger _value, byte[] _data) {}

}
