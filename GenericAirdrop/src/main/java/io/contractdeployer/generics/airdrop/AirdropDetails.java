package io.contractdeployer.generics.airdrop;

import score.Address;

import java.math.BigInteger;

public class AirdropDetails {

    public Address userAddress;
    public BigInteger amount;
    public BigInteger timestamp;

    public AirdropDetails(Address userAddress, BigInteger amount, BigInteger timestamp) {
        this.userAddress = userAddress;
        this.amount = amount;
        this.timestamp = timestamp;
    }

}
