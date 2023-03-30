package io.tokenfactory.score.dbs;

import score.Address;
import score.ObjectReader;
import score.ObjectWriter;

import java.math.BigInteger;
import java.util.Map;

public class ContentDB {

    private String content;

    private String description;
    private BigInteger lastUpdated;
    private Address updatedBy;
    private String type;

    public ContentDB() {
    }

    public ContentDB(String content, String description, BigInteger lastUpdated, Address updatedBy, String type) {
        this.content = content;
        this.description = description;
        this.lastUpdated = lastUpdated;
        this.updatedBy = updatedBy;
        this.type = type;
    }

    public static ContentDB readObject(ObjectReader reader) {
        ContentDB obj = new ContentDB();
        reader.beginList();
        obj.setContent(reader.readString());
        obj.setDescription(reader.readString());
        obj.setLastUpdated(reader.readBigInteger());
        obj.setUpdatedBy(reader.readAddress());
        obj.setType(reader.readString());
        reader.end();
        return obj;
    }

    public static void writeObject(ObjectWriter w, ContentDB obj) {
        w.beginList(4);
        w.write(obj.content);
        w.write(obj.description);
        w.write(obj.lastUpdated);
        w.write(obj.updatedBy);
        w.write(obj.type);
        w.end();
    }

    public Map<String, Object> toObject() {
        return Map.of(
                "type", getType(),
                "lastUpdated", getLastUpdated(),
                "updatedBy", getUpdatedBy()
        );
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
