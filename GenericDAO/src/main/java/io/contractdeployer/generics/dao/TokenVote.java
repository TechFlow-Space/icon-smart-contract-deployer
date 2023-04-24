package io.contractdeployer.generics.dao;

import score.ObjectReader;
import score.ObjectWriter;

import java.math.BigInteger;

public class TokenVote {
    private final String vote;
    private final BigInteger amount;

    public TokenVote(String vote, BigInteger amount) {
        this.vote = vote;
        this.amount = amount;
    }

    public String getVote() {
        return vote;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public static void writeObject(ObjectWriter w, TokenVote v) {
        w.writeListOf(v.vote, v.amount);
    }

    public static TokenVote readObject(ObjectReader r) {
        r.beginList();
        TokenVote v = new TokenVote(
                r.readString(),
                r.readBigInteger()
        );
        r.end();
        return v;
    }
}
