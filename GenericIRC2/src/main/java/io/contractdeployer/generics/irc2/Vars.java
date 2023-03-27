package io.contractdeployer.generics.irc2;

import score.Address;
import score.Context;
import score.DictDB;
import score.VarDB;

import java.math.BigInteger;

import static io.contractdeployer.generics.irc2.Constant.*;

public class Vars {

    static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    static final VarDB<String> name = Context.newVarDB(NAME, String.class);
    static final VarDB<String> symbol = Context.newVarDB(SYMBOL, String.class);
    static final VarDB<BigInteger> decimals = Context.newVarDB(DECIMALS, BigInteger.class);
    static final VarDB<BigInteger> totalSupply = Context.newVarDB(TOTAL_SUPPLY, BigInteger.class);
    static final DictDB<Address, BigInteger> balances = Context.newDictDB(BALANCES, BigInteger.class);
    static final VarDB<Address> minter = Context.newVarDB(MINTER_ADDRESS, Address.class);

}
