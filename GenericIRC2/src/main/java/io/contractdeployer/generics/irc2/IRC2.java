package io.contractdeployer.generics.irc2;

import com.iconloop.score.token.irc2.IRC2Basic;
import score.Address;
import score.Context;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;

import java.math.BigInteger;


public class IRC2 extends IRC2Basic {
    public final String TAG = "IRC2";
    public final String MINTER_ADDRESS="minter_address";

    public final VarDB<Address> minter = Context.newVarDB(MINTER_ADDRESS, Address.class);


    public IRC2(String _name, String _symbol, BigInteger _decimals, Address _minter) {
        super(_name,_symbol,_decimals.intValue());
        this.minter.set(_minter);

    }

    @External(readonly = true)
    public Address getMinter() {
        return minter.get();
    }

    @External
    public void setMinter(Address _minter) {
        onlyOwner();
        minter.set(_minter);
        TokenMinterUpdated(_minter);
    }

    @External
    public void mint(Address _to,BigInteger _amount) {
        Address caller = Context.getCaller();
        boolean mintCondition  = caller.equals(Context.getOwner())  || caller.equals(getMinter());
        Context.require(mintCondition,TAG+" :: Only owner/minter can perform this action.");

        _mint(_to,_amount);
        Mint(caller,_to,_amount);

    }

    @External
    public void burn(BigInteger _amount) {
        Address caller = Context.getCaller();
        _burn(caller, _amount);
        Burn(caller,_amount);
    }

    @EventLog(indexed=3)
    public void Transfer(Address _from, Address _to, BigInteger _value, byte[] _data) {}

    @EventLog(indexed = 1)
    public void TokenMinterUpdated(Address _newMinter){}

    @EventLog(indexed = 3)
    public void Mint(Address _caller, Address _to, BigInteger _amount){}

    @EventLog(indexed = 2)
    public void Burn(Address _from, BigInteger _amount){}

    protected void onlyOwner(){
        Address caller = Context.getCaller();
        Address owner = Context.getOwner();

        Context.require(caller.equals(owner), TAG+" :: Only owner can perform this action.");
    }

}
