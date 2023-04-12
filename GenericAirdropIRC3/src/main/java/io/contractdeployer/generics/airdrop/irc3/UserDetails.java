package io.contractdeployer.generics.airdrop.irc3;

import score.Address;
import score.ObjectReader;
import score.ObjectWriter;
import scorex.util.HashMap;

import java.math.BigInteger;
import java.util.Map;

public class UserDetails {

    public Address userAddress;
    public Address tokenAddress;
    public Address tokenOwner;
    public BigInteger tokenId;
    public BigInteger timestamp;


    public void setUserAddress(Address userAddress) {
        this.userAddress = userAddress;
    }
    public void setTokenOwner(Address tokenOwner) {
        this.tokenOwner = tokenOwner;
    }

    public void setTokenId(BigInteger tokenId) {
        this.tokenId = tokenId;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    public UserDetails(Address tokenAddress,Address tokenOwner,Address userAddress, BigInteger tokenId,
                      BigInteger timestamp) {
        this.tokenAddress = tokenAddress;
        this.tokenOwner = tokenOwner;
        this.userAddress = userAddress;
        this.tokenId = tokenId;
        this.timestamp = timestamp;
    }

    public UserDetails(){}

    public void setTokenAddress(Address tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    public Address getUserAddress() {
        return userAddress;
    }

    public Address getTokenOwner() {
        return tokenOwner;
    }

    public BigInteger getTokenId() {
        return tokenId;
    }

    public BigInteger getTimestamp() {
        return timestamp;
    }

    public Address getTokenAddress() {
        return tokenAddress;
    }


    public static void writeObject(ObjectWriter w, UserDetails obj) {
        w.beginList(5);
        w.write(obj.tokenAddress);
        w.write(obj.tokenOwner);
        w.write(obj.userAddress);
        w.write(obj.tokenId);
        w.write(obj.timestamp);
        w.end();
    }

    public static UserDetails readObject(ObjectReader reader) {
        UserDetails obj = new UserDetails();
        reader.beginList();
        obj.setTokenOwner(reader.readAddress());
        obj.setTokenAddress(reader.readAddress());
        obj.setUserAddress(reader.readAddress());
        obj.setTokenId(reader.readBigInteger());
        obj.setTimestamp(reader.readBigInteger());
        reader.end();
        return obj;
    }

    public Map<String, Object> toObject() {
        Map<String, Object> toObject = new HashMap<>();
        toObject.put("tokenAddress", getTokenAddress());
        toObject.put("tokenOwner", getTokenOwner());
        toObject.put("userAddress", getUserAddress());
        toObject.put("tokenId", getTokenId());
        toObject.put("timestamp", getTimestamp());
        return toObject;
    }

}
