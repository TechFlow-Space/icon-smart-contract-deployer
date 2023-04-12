package io.tokenfactory.score.dbs;

import score.Address;
import score.ObjectReader;
import score.ObjectWriter;

import java.math.BigInteger;
import java.util.Map;

public class ContentDB {

    private String name;
    private String description;
    private BigInteger lastUpdated;
    private Address updatedBy;
    private String type;

    public ContentDB() {
    }

    public ContentDB(String name, String description, BigInteger lastUpdated, Address updatedBy, String type) {
        this.name = name;
        this.description = description;
        this.lastUpdated = lastUpdated;
        this.updatedBy = updatedBy;
        this.type = type;
    }

    public static ContentDB readObject(ObjectReader reader) {
        ContentDB obj = new ContentDB();
        reader.beginList();
        obj.setName(reader.readString());
        obj.setDescription(reader.readString());
        obj.setLastUpdated(reader.readBigInteger());
        obj.setUpdatedBy(reader.readAddress());
        obj.setType(reader.readString());
        reader.end();
        return obj;
    }

    public static void writeObject(ObjectWriter w, ContentDB obj) {
        w.beginList(4);
        w.write(obj.name);
        w.write(obj.description);
        w.write(obj.lastUpdated);
        w.write(obj.updatedBy);
        w.write(obj.type);
        w.end();
    }

    public Map<String, Object> toObject() {
        return Map.of(
                "name", getName(),
                "type", getType(),
                "lastUpdated", getLastUpdated(),
                "updatedBy", getUpdatedBy(),
                "description", getDescription()
        );
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigInteger getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(BigInteger lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Address getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Address updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
