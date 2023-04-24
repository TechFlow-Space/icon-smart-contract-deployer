package io.contractdeployer.generics.dao;

import score.Context;
import score.ObjectReader;
import score.ObjectWriter;

import java.math.BigInteger;
import java.util.List;

public class Votes {
    private static final String FOR = "for";
    private static final String AGAINST = "against";
    private static final String ABSTAIN = "abstain";
    private static final List<String> VALID_VOTES = List.of(Votes.FOR, Votes.AGAINST, Votes.ABSTAIN);

    private BigInteger _for;
    private BigInteger _against;
    private BigInteger _abstain;

    public Votes() {
        this(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
    }

    public Votes(BigInteger _for, BigInteger _against, BigInteger _abstain) {
        this._for = _for;
        this._against = _against;
        this._abstain = _abstain;
    }

    public static boolean isValid(String vote) {
        return VALID_VOTES.contains(vote);
    }

    public void increase(String vote, BigInteger balance) {
        switch (vote) {
            case FOR:
                _for = balance.add(_for);
                break;
            case AGAINST:
                _against = balance.add(_against);
                break;
            case ABSTAIN:
                _abstain = balance.add(_abstain);
                break;
            default:
                Context.revert("InvalidVoteType");
        }
    }

    public BigInteger getFor() {
        return _for;
    }

    public BigInteger getAgainst() {
        return _against;
    }

    public BigInteger getAbstain() {
        return _abstain;
    }

    public static void writeObject(ObjectWriter w, Votes v) {
        w.writeListOf(v._for, v._against, v._abstain);
    }

    public static Votes readObject(ObjectReader r) {
        r.beginList();
        Votes v = new Votes(
                r.readBigInteger(),
                r.readBigInteger(),
                r.readBigInteger()
        );
        r.end();
        return v;
    }
}
