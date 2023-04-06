package io.contractdeployer.generics.airdrop;

import score.Address;
import score.ObjectReader;
import score.ObjectWriter;
import scorex.util.HashMap;

import java.math.BigInteger;
import java.util.Map;

public class AirdropDetails {

    public Address userAddress;
    public BigInteger amount;
    public BigInteger timestamp;

    public void setUserAddress(Address userAddress) {
        this.userAddress = userAddress;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    public AirdropDetails(Address userAddress, BigInteger amount, BigInteger timestamp) {
        this.userAddress = userAddress;
        this.amount = amount;
        this.timestamp = timestamp;
    }
    public AirdropDetails(){}

    public Address getUserAddress() {
        return userAddress;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public BigInteger getTimestamp() {
        return timestamp;
    }

    public static void writeObject(ObjectWriter w, AirdropDetails obj) {
        w.beginList(3);
        w.write(obj.userAddress);
        w.write(obj.amount);
        w.write(obj.timestamp);
        w.end();
    }

    public static AirdropDetails readObject(ObjectReader reader) {
        AirdropDetails obj = new AirdropDetails();
        reader.beginList();
        obj.setUserAddress(reader.readAddress());
        obj.setAmount(reader.readBigInteger());
        obj.setTimestamp(reader.readBigInteger());
        reader.end();
        return obj;
    }

    public Map<String, Object> toObject() {
        Map<String, Object> toObject = new HashMap<>();
        toObject.put("userAddress", getUserAddress());
        toObject.put("amount", getAmount());
        toObject.put("timestamp", getTimestamp());
        return toObject;
    }

}
