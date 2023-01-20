package io.tokenfactory.score.dbs;

import score.Address;
import score.ObjectReader;
import score.ObjectWriter;

import java.math.BigInteger;

public class ContractDB {

    private BigInteger id;
    private String name;
    private Address deployer;
    private BigInteger timestamp;
    private Address contractAddress;

    public ContractDB(){}

    public ContractDB(BigInteger id, String name, Address deployer, BigInteger timestamp, Address contractAddress){
        this.id = id;
        this.name = name;
        this.deployer = deployer;
        this.timestamp = timestamp;
        this.contractAddress = contractAddress;
    }

    public static ContractDB readObject(ObjectReader reader) {
        ContractDB obj = new ContractDB();
        reader.beginList();
        obj.setId(reader.readBigInteger());
        obj.setName(reader.readString());
        obj.setDeployer(reader.readAddress());
        obj.setTimestamp(reader.readBigInteger());
        obj.setContractAddress(reader.readAddress());
        reader.end();
        return obj;
    }

    public static void writeObject(ObjectWriter w, ContractDB obj) {
        w.beginList(5);
        w.write(obj.id);
        w.write(obj.name);
        w.write(obj.deployer);
        w.write(obj.timestamp);
        w.write(obj.contractAddress);
        w.end();
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getDeployer() {
        return deployer;
    }

    public void setDeployer(Address deployer) {
        this.deployer = deployer;
    }

    public BigInteger getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    public Address getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(Address contractAddress) {
        this.contractAddress = contractAddress;
    }
}
