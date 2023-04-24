package io.contractdeployer.generics.dao;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import com.iconloop.score.token.irc2.IRC2Basic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import score.Context;

import java.math.BigInteger;
import java.util.Map;

import static io.contractdeployer.generics.dao.GovImpl.TAG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GovImplTest extends TestBase {
    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account tom = sm.createAccount();
    private static final Account alice = sm.createAccount();
    private static GovImpl spyScore;
    private Score tokenScore;
    private Score genericDaoScore;

    @BeforeEach
    void setup() throws Exception {
        tokenScore = sm.deploy(owner, IRC2TestToken.class, ICX.multiply(BigInteger.valueOf(10000)));
        genericDaoScore = sm.deploy(owner, GovImpl.class,"Dao Score");
        spyScore = (GovImpl) spy(genericDaoScore.getInstance());
        genericDaoScore.setInstance(spyScore);
        // set governance token
        genericDaoScore.invoke(owner, "setGovernanceToken", tokenScore.getAddress(), "irc-2", BigInteger.ZERO);
        // transfer some token to Alice and Tom
        tokenScore.invoke(owner, "transfer", alice.getAddress(), ICX.multiply(BigInteger.valueOf(800)), "".getBytes());
        tokenScore.invoke(owner, "transfer", tom.getAddress(), ICX.multiply(BigInteger.valueOf(800)), "".getBytes());
    }

    @Test
    void name() {
        assertEquals(TAG +" Dao Score", genericDaoScore.call("name"));
    }

    @Test
    void setGovernanceToken() {
        assertThrows(AssertionError.class, () ->
                genericDaoScore.invoke(owner, "setGovernanceToken", tokenScore.getAddress(), "irc-2", BigInteger.ZERO)
        );
    }

    @Test
    void getVote() {
        doReturn(BigInteger.valueOf(800).multiply(ICX)).when(spyScore).getTokenBalance(any());
        // submit dummy proposal
        long endTime = sm.getBlock().getTimestamp() + 2 * GovImpl.DAY_IN_MICROSECONDS.longValue();
        doReturn(true).when(spyScore).checkValidTimeStamp(BigInteger.valueOf(endTime));
        genericDaoScore.invoke(owner, "submitProposal", BigInteger.valueOf(endTime), "testIpfsHash");

        var pid = (BigInteger) genericDaoScore.call("lastProposalId");
        genericDaoScore.invoke(tom, "vote", pid, "for");
        genericDaoScore.invoke(alice, "vote", pid, "against");

        for (Account voter : new Account[]{tom, alice}) {
            @SuppressWarnings("unchecked")
            var vote = (Map<String, Object>) genericDaoScore.call("getVote", voter.getAddress(), pid);
            System.out.println(vote);
            if (voter.equals(tom)) {
                assertEquals("for", vote.get("_vote"));
            } else {
                assertEquals("against", vote.get("_vote"));
            }
            var balance = tokenScore.call("balanceOf", voter.getAddress());
            assertEquals(balance, vote.get("_power"));
        }
    }

    public static class IRC2TestToken extends IRC2Basic {
        public IRC2TestToken(BigInteger initialSupply) {
            super("TestToken", "TST", 18);
            _mint(Context.getCaller(), initialSupply);
        }
    }
}
