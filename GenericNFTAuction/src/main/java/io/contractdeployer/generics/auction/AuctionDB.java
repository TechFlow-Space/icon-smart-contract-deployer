package io.contractdeployer.generics.auction;

import score.Address;
import score.ObjectReader;
import score.ObjectWriter;
import scorex.util.HashMap;

import java.math.BigInteger;
import java.util.Map;

public class AuctionDB {

    private BigInteger id;
    private Address auctionCreator;
    private Address contractAddress;
    private BigInteger highestBid;
    private Address highestBidder;
    private BigInteger nftId;
    private BigInteger auctionEndTime;
    private Boolean isTransferred;
    private Boolean noParticipation;

    public AuctionDB() {
    }

    public AuctionDB(BigInteger id, Address auctionCreator, Address contractAddress,
                     BigInteger highestBid, Address highestBidder, BigInteger nftId, BigInteger auctionEndTime) {
        this.id = id;
        this.auctionCreator = auctionCreator;
        this.contractAddress = contractAddress;
        this.highestBid = highestBid;
        this.highestBidder = highestBidder;
        this.nftId = nftId;
        this.auctionEndTime = auctionEndTime;
        this.isTransferred = false;
        this.noParticipation = false;
    }

    public static AuctionDB readObject(ObjectReader reader) {
        AuctionDB obj = new AuctionDB();
        reader.beginList();
        obj.setId(reader.readBigInteger());
        obj.setAuctionCreator(reader.readAddress());
        obj.setContractAddress(reader.readAddress());
        obj.setHighestBid(reader.readBigInteger());
        obj.setHighestBidder(reader.readAddress());
        obj.setNftId(reader.readBigInteger());
        obj.setAuctionEndTime(reader.readBigInteger());
        obj.setTransferred(reader.readBoolean());
        obj.setNoParticipation(reader.readBoolean());
        reader.end();
        return obj;
    }

    public static void writeObject(ObjectWriter w, AuctionDB obj) {
        w.beginList(9);
        w.write(obj.id);
        w.write(obj.auctionCreator);
        w.write(obj.contractAddress);
        w.write(obj.highestBid);
        w.write(obj.highestBidder);
        w.write(obj.nftId);
        w.write(obj.auctionEndTime);
        w.write(obj.isTransferred);
        w.write(obj.noParticipation);
        w.end();
    }

    public Map<String, Object> toObject() {
        Map<String, Object> toObject = new HashMap<>();
        toObject.put("id", getId());
        toObject.put("auctionCreation", getAuctionCreator());
        toObject.put("contractAddress", getContractAddress());
        toObject.put("highestBidder", getHighestBidder());
        toObject.put("highestBid", getHighestBid());
        toObject.put("nftId", getNftId());
        toObject.put("auctionEndTime", getAuctionEndTime());
        toObject.put("isTransferred", getTransferred());
        toObject.put("noParticipation", getNoParticipation());
        return toObject;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public BigInteger getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(BigInteger highestBid) {
        this.highestBid = highestBid;
    }

    public Address getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBidder(Address highestBidder) {
        this.highestBidder = highestBidder;
    }

    public BigInteger getNftId() {
        return nftId;
    }

    public void setNftId(BigInteger nftId) {
        this.nftId = nftId;
    }

    public BigInteger getAuctionEndTime() {
        return auctionEndTime;
    }

    public void setAuctionEndTime(BigInteger auctionEndTime) {
        this.auctionEndTime = auctionEndTime;
    }

    public Address getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(Address contractAddress) {
        this.contractAddress = contractAddress;
    }

    public Address getAuctionCreator() {
        return auctionCreator;
    }

    public void setAuctionCreator(Address auctionCreator) {
        this.auctionCreator = auctionCreator;
    }

    public Boolean getTransferred() {
        return isTransferred;
    }

    public void setTransferred(Boolean transferred) {
        isTransferred = transferred;
    }

    public Boolean getNoParticipation() {
        return noParticipation;
    }

    public void setNoParticipation(Boolean noParticipation) {
        this.noParticipation = noParticipation;
    }
}

