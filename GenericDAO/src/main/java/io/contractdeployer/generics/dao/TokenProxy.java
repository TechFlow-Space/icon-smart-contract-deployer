package io.contractdeployer.generics.dao;

import score.Address;
import score.Context;

import java.math.BigInteger;

public class TokenProxy {

    public static final String IRC2 = "irc-2";
    public static final String IRC3 = "irc-3";
    public static final String IRC31 = "irc-31";

    private final Address address;
    private final String type;
    private final BigInteger id;

    public TokenProxy(Address address, String type, BigInteger id) {
        Context.require(address != null, "TokenAddressNotSet");
        this.address = address;
        this.type = type;
        this.id = id;
    }

    public BigInteger balanceOf(Address holder) {
        if (IRC2.equals(type)) {
            return Context.call(BigInteger.class, address, "balanceOf", holder);
        } else {
            return Context.call(BigInteger.class, address, "balanceOf", holder, id);
        }
    }
}
