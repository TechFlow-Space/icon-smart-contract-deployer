package io.contractdeployer.generics.auction;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import score.Context;

import java.math.BigInteger;

import static io.contractdeployer.generics.auction.Constant.ZERO_ADDRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

public class AuctionTest extends TestBase {

    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account tom = sm.createAccount(500);
    private static final Account eva = sm.createAccount(500);
    private static Score score;
    private static Auction spyScore;
    private static Score dummyScore;

    @BeforeEach
    void setup() throws Exception {
        dummyScore = sm.deploy(owner, DummyScore.class);
        score = sm.deploy(owner, Auction.class);

        spyScore = (Auction) spy(score.getInstance());
        score.setInstance(spyScore);

        doNothing().when(spyScore).validateOwner(any(), any(), any());
        doNothing().when(spyScore).transferToBidder(any(), any(), any());
        doNothing().when(spyScore).transferToContract(any(), any(), any());
        doNothing().when(spyScore).validateApproval(any(), any());
    }

    @Test
    public void createAuctionTest() {
        BigInteger endTime = BigInteger.valueOf(Context.getBlockTimestamp() + 1000000000);
        score.invoke(tom, "createAuction", dummyScore.getAddress(), BigInteger.ONE, endTime);
        AuctionDB auctionDB = (AuctionDB) score.call("getCurrentAuction");
        assertEquals(BigInteger.ONE, auctionDB.getId());
        assertEquals(ZERO_ADDRESS, auctionDB.getHighestBidder());
        assertEquals(BigInteger.ZERO, auctionDB.getHighestBid());
        assertEquals(dummyScore.getAddress(), auctionDB.getContractAddress());
        assertEquals(tom.getAddress(), auctionDB.getAuctionCreator());
        assertEquals(false, auctionDB.getTransferred());
        assertEquals(false, auctionDB.getNoParticipation());
    }
}
