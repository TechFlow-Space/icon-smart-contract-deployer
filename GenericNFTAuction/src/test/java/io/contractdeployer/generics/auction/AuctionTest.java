package io.contractdeployer.generics.auction;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import score.Context;

import java.math.BigInteger;

import static io.contractdeployer.generics.auction.Constant.TAG;
import static io.contractdeployer.generics.auction.Constant.ZERO_ADDRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class AuctionTest extends TestBase {

    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account tom = sm.createAccount(500);
    private static final Account eva = sm.createAccount(500);
    private static final Account john = sm.createAccount(500);
    private static final Account treasury = sm.createAccount();
    private static Score score;
    private static Auction spyScore;
    private final Account dummyScore = Account.newScoreAccount(0);

    private final static BigInteger HUNDRED = BigInteger.valueOf(100);
    private final BigInteger minBid = BigInteger.TEN.multiply(ICX);

    static MockedStatic<Context> contextMock;

    @BeforeEach
    void setup() throws Exception {
        contextMock.reset();
        score = sm.deploy(owner, Auction.class);

        spyScore = (Auction) spy(score.getInstance());
        score.setInstance(spyScore);

        doNothing().when(spyScore).validateOwner(any(), any(), any());
        doNothing().when(spyScore).transferToBidder(any(), any(), any());
        doNothing().when(spyScore).transferToContract(any(), any(), any());
        doNothing().when(spyScore).validateApproval(any(), any());
    }

    @BeforeAll
    protected static void init(){
        contextMock = Mockito.mockStatic(Context.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test
    public void invalid_bid_auction(){
        BigInteger endTime = BigInteger.valueOf(sm.getBlock().getTimestamp()+ 100_000_000);
        Executable call = () -> createAuction(tom,BigInteger.TWO,BigInteger.ZERO,endTime);
        expectErrorMessage(call,TAG+" :: Minimum Bid must be greater than 0");

    }

    @Test
    public void invalid_end_time_auction(){
        BigInteger endTime = BigInteger.valueOf(sm.getBlock().getTimestamp());
        Executable call = () -> createAuction(tom,BigInteger.TWO,minBid,endTime);
        expectErrorMessage(call,TAG + " :: Invalid auction end time");
    }

    @Test
    public void createAuctionTest() {

        BigInteger endTime = BigInteger.valueOf(sm.getBlock().getTimestamp()+ 1000_000_000);


        createAuction(tom,BigInteger.ONE,minBid,endTime);
        AuctionDB auctionDB = (AuctionDB) score.call("getCurrentAuction");

        assertEquals(BigInteger.ONE, auctionDB.getId());
        assertEquals(BigInteger.ONE, auctionDB.getNftId());
        assertEquals(minBid, auctionDB.getMinimumBid());
        assertEquals(ZERO_ADDRESS, auctionDB.getHighestBidder());
        assertEquals(BigInteger.ZERO, auctionDB.getHighestBid());
        assertEquals(dummyScore.getAddress(), auctionDB.getContractAddress());
        assertEquals(tom.getAddress(), auctionDB.getAuctionCreator());
        assertEquals(false, auctionDB.getTransferred());
        assertEquals(false, auctionDB.getNoParticipation());

        verify(spyScore).AuctionCreated(BigInteger.ONE,tom.getAddress(),dummyScore.getAddress(),BigInteger.ONE);
        verify(spyScore).validateApproval(dummyScore.getAddress(),BigInteger.ONE);
        verify(spyScore).transferToContract(dummyScore.getAddress(),tom.getAddress(),BigInteger.ONE);
        verify(spyScore).validateOwner(dummyScore.getAddress(),score.getAddress(),BigInteger.ONE);
    }

    @Test
    public void bid_on_not_available_auction(){
        BigInteger bidAmount = HUNDRED.multiply(ICX);
        contextMock.when(getValue()).thenReturn(bidAmount);

        Executable call= () ->score.invoke(eva,"bid");
        expectErrorMessage(call,TAG + " :: No Auction Available");
    }

    @Test
    public void creator_bid_on_their_own_auction(){
        BigInteger endTime = BigInteger.valueOf(sm.getBlock().getTimestamp()+ 100_000_000);

        createAuction(tom,BigInteger.TWO,minBid,endTime);

        BigInteger bidAmount = HUNDRED.multiply(ICX);
        contextMock.when(getValue()).thenReturn(bidAmount);

        Executable call= () ->score.invoke(tom,"bid");
        expectErrorMessage(call,TAG + " :: Auction Creator Not Allowed To Bid");

    }

    @Test
    public void creator_bids_on_auction(){
        BigInteger endTime = BigInteger.valueOf(sm.getBlock().getTimestamp()+ 100_000_000);

        createAuction(tom,BigInteger.TWO,minBid,endTime);

        BigInteger bidAmount = HUNDRED.multiply(ICX);
        contextMock.when(getValue()).thenReturn(bidAmount);

        Executable call= () ->score.invoke(tom,"bid");
        expectErrorMessage(call,TAG + " :: Auction Creator Not Allowed To Bid");

    }


    @Test
    public void bidOnAuction(){
        BigInteger endTime = BigInteger.valueOf(sm.getBlock().getTimestamp()+ 100_000_000);

        createAuction(tom,BigInteger.TWO,minBid,endTime);

        AuctionDB auctionDB = getCurrentAuction();
        assertEquals(ZERO_ADDRESS, auctionDB.getHighestBidder());
        assertEquals(BigInteger.ZERO, auctionDB.getHighestBid());

        BigInteger bidAmount = HUNDRED.multiply(ICX);
        contextMock.when(getValue()).thenReturn(bidAmount);


        score.invoke(eva,"bid");

        auctionDB = getCurrentAuction();
        assertEquals(eva.getAddress(), auctionDB.getHighestBidder());
        assertEquals(bidAmount, auctionDB.getHighestBid());

        verify(spyScore).HighestBidIncreased(BigInteger.ONE,bidAmount);
    }

    @Test
    public void bid_by_multiple_users(){

        BigInteger endTime = BigInteger.valueOf(sm.getBlock().getTimestamp()+ 100_000_000);

        createAuction(tom,BigInteger.TWO,minBid,endTime);

        AuctionDB auctionDB = getCurrentAuction();
        assertEquals(ZERO_ADDRESS, auctionDB.getHighestBidder());
        assertEquals(BigInteger.ZERO, auctionDB.getHighestBid());

        BigInteger bidAmount = HUNDRED.multiply(ICX);
        contextMock.when(getValue()).thenReturn(bidAmount);

        // bid ny eva
        score.invoke(eva,"bid");

        auctionDB = getCurrentAuction();
        assertEquals(eva.getAddress(), auctionDB.getHighestBidder());
        assertEquals(HUNDRED.multiply(ICX), auctionDB.getHighestBid());

        // bid by john
        Executable call= () ->score.invoke(john,"bid");
        expectErrorMessage(call,TAG + " :: Bid should be greater than zero/previous bidder");

        contextMock.when(getValue()).thenReturn(bidAmount.add(BigInteger.TWO));

        doNothing().when(spyScore).transferFromScore(eva.getAddress(),HUNDRED.multiply(ICX));
        score.invoke(john,"bid");

        auctionDB = getCurrentAuction();
        assertEquals(john.getAddress(), auctionDB.getHighestBidder());
        assertEquals(HUNDRED.multiply(ICX).add(BigInteger.TWO), auctionDB.getHighestBid());

        verify(spyScore).HighestBidIncreased(BigInteger.ONE,HUNDRED.multiply(ICX));
        verify(spyScore).transferFromScore(eva.getAddress(),HUNDRED.multiply(ICX));

    }

    @Test
    public void endAuction_with_invalid_id(){

        Executable call= () ->score.invoke(eva,"endAuction",BigInteger.ONE);
        expectErrorMessage(call,TAG + " :: Invalid auction id");
    }

    @Test
    public void endAuction_not_by_creator(){
        BigInteger endTime = BigInteger.valueOf(sm.getBlock().getTimestamp()+ 100_000_000);

        // create auction
        createAuction(tom,BigInteger.TWO,minBid,endTime);

        Executable call= () ->score.invoke(eva,"endAuction",BigInteger.ONE);
        expectErrorMessage(call,TAG + " :: Only auction creator can do this action");
    }

    @Test
    public void endAuction_already_transferred(){
        endAuction();

        Executable call= () ->score.invoke(eva,"endAuction",BigInteger.ONE);
        expectErrorMessage(call,TAG + " :: Auction Ended for auction id 1");
    }

    @Test
    public void endAuction(){
        BigInteger endTime = BigInteger.valueOf(sm.getBlock().getTimestamp()+ 100_000_000);

        // create auction
        createAuction(tom,BigInteger.TWO,minBid,endTime);

        AuctionDB auctionDB = getCurrentAuction();
        assertEquals(BigInteger.ONE,auctionDB.getId());
        assertEquals(false, auctionDB.getTransferred());
        assertEquals(endTime, auctionDB.getAuctionEndTime());

        BigInteger bidAmount = HUNDRED.multiply(ICX);
        contextMock.when(getValue()).thenReturn(bidAmount);

        // bid
        score.invoke(eva,"bid");

        score.invoke(tom,"endAuction",BigInteger.ONE);
        auctionDB = getCurrentAuction();
        assertEquals(BigInteger.valueOf(sm.getBlock().getTimestamp()), auctionDB.getAuctionEndTime());
        assertEquals(BigInteger.ONE,auctionDB.getId());
        assertEquals(true, auctionDB.getTransferred());
        assertEquals(false, auctionDB.getNoParticipation());

        verify(spyScore).transferToBidder(dummyScore.getAddress(),eva.getAddress(),BigInteger.TWO);
        verify(spyScore).AuctionCompleted(BigInteger.ONE,eva.getAddress(),dummyScore.getAddress(),BigInteger.TWO);

    }

//    @Test TODo
//    public void create_auction(){
//        BigInteger endTime = BigInteger.valueOf(sm.getBlock().getTimestamp()+ 100_000_000);
//
//        endAuction();
//        AuctionDB auctionDB = getCurrentAuction();
//
//        createAuction(tom,BigInteger.TWO,minBid,endTime);
//    }

    @Test
    public void auction_with_no_participation(){
        BigInteger endTime = BigInteger.valueOf(sm.getBlock().getTimestamp()+ 100_000_000);
        createAuction(tom,BigInteger.TEN,minBid,endTime);

        score.invoke(tom,"endAuction",BigInteger.ONE);

        AuctionDB auctionDB = getCurrentAuction();
        assertEquals(BigInteger.ZERO,auctionDB.getHighestBid());
        assertEquals(ZERO_ADDRESS,auctionDB.getHighestBidder());
        assertEquals(true,auctionDB.getNoParticipation());

        verify(spyScore).transferToBidder(dummyScore.getAddress(),tom.getAddress(),BigInteger.TEN);
        verify(spyScore).AuctionCreated(BigInteger.ONE,tom.getAddress(),dummyScore.getAddress(),BigInteger.TEN);

    }

    @Test
    public void getAuctions(){
        BigInteger endTime = BigInteger.valueOf(sm.getBlock().getTimestamp()+ 100_000_000);
        for (int i = 0; i < 4; i++) {
            createAuction(tom,BigInteger.valueOf(i),minBid,endTime);
        }

        assertEquals(BigInteger.valueOf(4),score.call("getCurrentIndex"));

        AuctionDB[] auctions = (AuctionDB[]) score.call("getAuctions",0,8,"aesc");
        for (int i = 0; i < 4; i++) {
            assertEquals(auctions[i].getId(),BigInteger.valueOf(i+1));
        }

    }
    @Test
    public void transferToTreasury(){
        endAuction();
        BigInteger scoreBalance = BigInteger.TEN.multiply(ICX);
        contextMock.when(caller()).thenReturn(owner.getAddress());
        contextMock.when(getBalance()).thenReturn(scoreBalance);

        doNothing().when(spyScore).transferFromScore(treasury.getAddress(),scoreBalance);

        score.invoke(owner,"transferToTreasury",treasury.getAddress());
        verify(spyScore).TransferToTreasury(treasury.getAddress(),scoreBalance);
    }

    private void createAuction(Account creator, BigInteger nftId, BigInteger minBid,BigInteger endTime){
        score.invoke(creator, "createAuction", dummyScore.getAddress(), nftId, minBid, endTime);
    }

    private AuctionDB getCurrentAuction(){
        return (AuctionDB) score.call("getCurrentAuction");
    }

    private MockedStatic.Verification caller() {
        return () -> Context.getCaller();
    }

    private MockedStatic.Verification getValue() {
        return () -> Context.getValue();
    }

    private MockedStatic.Verification getBalance() {
        return () -> Context.getBalance(score.getAddress());
    }

    private static void expectErrorMessage(Executable contractCall, String errorMessage) {
        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
        assertEquals(errorMessage, e.getMessage());
    }



}
