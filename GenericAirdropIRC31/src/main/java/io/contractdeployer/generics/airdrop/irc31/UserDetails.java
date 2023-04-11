package io.contractdeployer.generics.airdrop.irc31;

import score.Address;
import score.ObjectReader;
import score.ObjectWriter;
import scorex.util.HashMap;

import java.math.BigInteger;
import java.util.Map;

public class UserDetails {

    public Address userAddress;
    public BigInteger value;
    public BigInteger tokenId;
    public BigInteger timestamp;

    public void setUserAddress(Address userAddress) {
        this.userAddress = userAddress;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    public void setTokenId(BigInteger tokenId) {
        this.tokenId = tokenId;
    }

    public UserDetails(Address userAddress, BigInteger amount, BigInteger timestamp) {
        this.userAddress = userAddress;
        this.value = amount;
        this.timestamp = timestamp;
    }
    public UserDetails(){}

    public Address getUserAddress() {
        return userAddress;
    }

    public BigInteger getTokenId() {
        return tokenId;
    }

    public BigInteger getValue() {
        return value;
    }

    public BigInteger getTimestamp() {
        return timestamp;
    }

    public static void writeObject(ObjectWriter w, UserDetails obj) {
        w.beginList(3);
        w.write(obj.userAddress);
        w.write(obj.tokenId);
        w.write(obj.value);
        w.write(obj.timestamp);
        w.end();
    }

    public static UserDetails readObject(ObjectReader reader) {
        UserDetails obj = new UserDetails();
        reader.beginList();
        obj.setUserAddress(reader.readAddress());
        obj.setTokenId(reader.readBigInteger());
        obj.setValue(reader.readBigInteger());
        obj.setTimestamp(reader.readBigInteger());
        reader.end();
        return obj;
    }

    public Map<String, Object> toObject() {
        Map<String, Object> toObject = new HashMap<>();
        toObject.put("userAddress", getUserAddress());
        toObject.put("tokenId", getTokenId());
        toObject.put("value", getValue());
        toObject.put("timestamp", getTimestamp());
        return toObject;
    }

}
