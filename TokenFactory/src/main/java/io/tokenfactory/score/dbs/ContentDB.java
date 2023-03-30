package io.tokenfactory.score.dbs;

import score.Address;
import score.ObjectReader;
import score.ObjectWriter;

import java.math.BigInteger;
import java.util.Map;

public class ContentDB {

    private String name;
    private String content;
    private BigInteger timestamp;
    private Address updatedBy;
    private String type;

    public ContentDB() {
    }

    public ContentDB(String name, String content, BigInteger timestamp, Address updatedBy, String type) {
        this.name = name;
        this.content = content;
        this.timestamp = timestamp;
        this.updatedBy = updatedBy;
        this.type = type;
    }

    public static ContentDB readObject(ObjectReader reader) {
        ContentDB obj = new ContentDB();
        reader.beginList();
        obj.setName(reader.readString());
        obj.setContent(reader.readString());
        obj.setTimestamp(reader.readBigInteger());
        obj.setUpdatedBy(reader.readAddress());
        obj.setType(reader.readString());
        reader.end();
        return obj;
    }

    public static void writeObject(ObjectWriter w, ContentDB obj) {
        w.beginList(5);
        w.write(obj.name);
        w.write(obj.content);
        w.write(obj.timestamp);
        w.write(obj.updatedBy);
        w.write(obj.type);
        w.end();
    }

    public Map<String, Object> toObject() {
        return Map.of("name", getName(),
                "content", getContent().getBytes(),
                "timestamp", getTimestamp(),
                "updatedBy", getUpdatedBy(),
                "type", getType());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public BigInteger getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
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
