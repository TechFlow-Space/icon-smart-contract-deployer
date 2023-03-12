package io.contractdeployer.generics.marketplace.db;

import score.Address;
import score.Context;
import score.ObjectReader;
import score.ObjectWriter;

import java.math.BigInteger;

public class SaleDB {

    private BigInteger id;
    private BigInteger nftId;
    private Address score;
    private Address owner;
    private BigInteger price;
    private Integer count;
    private String status;
    private BigInteger timestamp;

    private String hash;
    private Address newOwner;
    private BigInteger settleTimestamp;

    public SaleDB (){}

    public SaleDB (BigInteger id, BigInteger nftId, Address score, Address owner, BigInteger price, Integer count, String status, BigInteger timestamp, String hash, Address newOwner, BigInteger settleTimestamp){
        this.id=id;
        this.nftId=nftId;
        this.score=score;
        this.owner=owner;
        this.price=price;
        this.count=count;
        this.status=status;
        this.timestamp=timestamp;
        this.hash = hash;
        this.newOwner=newOwner!=null?newOwner:owner;
        this.settleTimestamp=settleTimestamp!=null?settleTimestamp:timestamp;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public BigInteger getNftId() {
        return nftId;
    }

    public void setNftId(BigInteger nftId) {
        this.nftId = nftId;
    }

    public Address getScore() {
        return score;
    }

    public void setScore(Address score) {
        this.score = score;
    }

    public Address getOwner() {
        return owner;
    }

    public void setOwner(Address owner) {
        this.owner = owner;
    }

    public BigInteger getPrice() {
        return price;
    }

    public void setPrice(BigInteger price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigInteger getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Address getNewOwner() {
        return newOwner;
    }

    public void setNewOwner(Address newOwner) {
        this.newOwner = newOwner;
    }

    public BigInteger getSettleTimestamp() {
        return settleTimestamp;
    }

    public void setSettleTimestamp(BigInteger settleTimestamp) {
        this.settleTimestamp = settleTimestamp;
    }

    public static SaleDB readObject(ObjectReader r){
        SaleDB obj = new SaleDB();
        r.beginList();
        obj.setId(r.readBigInteger());
        obj.setNftId(r.readBigInteger());
        obj.setScore(r.readAddress());
        obj.setOwner(r.readAddress());
        obj.setPrice(r.readBigInteger());
        obj.setCount(r.readInt());
        obj.setStatus(r.readString());
        obj.setTimestamp(r.readBigInteger());
        obj.setHash(r.readString());
        obj.setNewOwner(r.readAddress());
        obj.setSettleTimestamp(r.readBigInteger());
        r.end();
        return obj;
    }

    public static void writeObject(ObjectWriter w, SaleDB obj){
        w.beginList(10);
        w.write(obj.getId());
        w.write(obj.getNftId());
        w.write(obj.getScore());
        w.write(obj.getOwner());
        w.write(obj.getPrice());
        w.write(obj.getCount());
        w.write(obj.getStatus());
        w.write(obj.getTimestamp());
        w.write(obj.getHash());
        w.write(obj.getNewOwner());
        w.write(obj.getSettleTimestamp());
        w.end();
    }



}
