package io.contractdeployer.generics.marketplace;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import io.contractdeployer.generics.marketplace.db.SaleDB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import score.Address;
import scorex.util.ArrayList;

import java.math.BigInteger;
import java.util.List;

import static io.contractdeployer.generics.marketplace.TestHelper.expectErrorMessage;
import static io.contractdeployer.generics.marketplace.util.NumUtil.pow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GenericMarketplaceTest extends TestBase {

    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account admin = sm.createAccount();
    private static final Account nftOwner = sm.createAccount();
    private static Score score;
    private static GenericMarketPlace spyScore;
    private static Score gkScore;
    private static Score newScore;

    @BeforeEach
    void setup() throws Exception {
        gkScore = sm.deploy(owner, DummyScore.class);
        newScore = sm.deploy(owner, DummyScore.class);
        score = sm.deploy(owner, GenericMarketPlace.class);
        spyScore = (GenericMarketPlace) spy(score.getInstance());
        score.setInstance(spyScore);
        score.invoke(owner, "setAdmin", admin.getAddress());
        score.invoke(admin, "addScore", gkScore.getAddress());
    }

    @Test
    void testBuyingDisabled(){
        assertEquals(false, score.call("isBuyingEnabled", gkScore.getAddress()));
        Executable call = () -> score.invoke(sm.createAccount(), "toggleBuyingEnabled", gkScore.getAddress());
        expectErrorMessage(call, Message.Not.admin(admin.getAddress()));

        score.invoke(admin, "toggleBuyingEnabled", gkScore.getAddress());
        assertEquals(true, score.call("isBuyingEnabled", gkScore.getAddress()));
    }

    @Test
    void testSettingDisabled(){
        assertEquals(false, score.call("isSettingEnabled", gkScore.getAddress()));
        Executable call = () -> score.invoke(sm.createAccount(), "toggleSettingEnabled", gkScore.getAddress());
        expectErrorMessage(call, Message.Not.admin(admin.getAddress()));

        score.invoke(admin, "toggleSettingEnabled", gkScore.getAddress());
        assertEquals(true, score.call("isSettingEnabled", gkScore.getAddress()));
    }

    @Test
    void testAddScoreAddress(){
        ArrayList<Address> addresses = (ArrayList<Address>) score.call("getScores");
        assertEquals(addresses.size(), 1);
        Executable call = () -> score.invoke(sm.createAccount(), "addScore", newScore.getAddress());
        expectErrorMessage(call, Message.Not.admin(admin.getAddress()));

        score.invoke(admin, "addScore", newScore.getAddress());
        assertEquals(newScore.getAddress(), ((ArrayList<Address>)score.call("getScores")).get(1));
    }

    @Test
    void testMarketPlaceFee(){
        Executable call = () -> score.invoke(sm.createAccount(), "setMarketplaceFee", gkScore.getAddress(), BigInteger.valueOf(5));
        expectErrorMessage(call, Message.Not.admin(admin.getAddress()));

        call = () -> score.invoke(admin, "setMarketplaceFee", gkScore.getAddress(), BigInteger.valueOf(-1));
        expectErrorMessage(call, Message.Not.feeInRange());

        call = () -> score.invoke(admin, "setMarketplaceFee", gkScore.getAddress(), BigInteger.valueOf(101).multiply(ICX));
        expectErrorMessage(call, Message.Not.feeInRange());

        score.invoke(admin, "setMarketplaceFee", gkScore.getAddress(), BigInteger.TWO.multiply(ICX));
        assertEquals(BigInteger.TWO.multiply(ICX), score.call("getMarketplaceFee", gkScore.getAddress()));
    }

    @Test
    void testSetPrice(){
        BigInteger price = BigInteger.valueOf(300).multiply(ICX);
        Executable call = () -> score.invoke(owner, "setPrice", gkScore.getAddress(), BigInteger.ZERO, BigInteger.ZERO, 5);
        expectErrorMessage(call, Message.greaterThanZero("Price"));

        call = () -> score.invoke(nftOwner, "setPrice", gkScore.getAddress(), price, BigInteger.ONE, 5);
        expectErrorMessage(call, Message.Not.enabled("Selling"));

        score.invoke(admin, "toggleSettingEnabled", gkScore.getAddress());
        assertEquals(true, score.call("isSettingEnabled", gkScore.getAddress()));

        doReturn(nftOwner.getAddress()).when(spyScore).getNftOwner(any());
        doReturn(false).when(spyScore).operatorIsApprovedForAll(gkScore.getAddress(), nftOwner.getAddress(), score.getAddress());
        call = () ->score.invoke(nftOwner, "setPrice", gkScore.getAddress(), price, BigInteger.ONE, 5);
        expectErrorMessage(call, Message.Not.approved());

        doReturn(true).when(spyScore).operatorIsApprovedForAll(any(), any(), any());
        doReturn(BigInteger.valueOf(10)).when(spyScore).getBalanceOfOwner(any(), any(), any());
        call = () ->score.invoke(owner, "setPrice", gkScore.getAddress(), price, BigInteger.ONE, 11);
        expectErrorMessage(call, Message.Not.enough());

        doReturn(nftOwner.getAddress()).when(spyScore).getNftOwner(any());
        score.invoke(nftOwner, "setPrice", gkScore.getAddress(), price, BigInteger.ONE, 5);

        assertEquals(price, score.call("getRate", BigInteger.ONE));

        List<SaleDB> history = (List<SaleDB>) score.call("getPutOnSaleHistory", gkScore.getAddress(), BigInteger.ONE, 10, 0, "asc");
        assertEquals(nftOwner.getAddress(), history.get(0).getOwner());

        call = () ->score.invoke(nftOwner, "getRate", BigInteger.TWO);
        expectErrorMessage(call, Message.Not.found("Sale"));
    }

    @Test
    void testRemoveFromSale(){
        BigInteger saleId=BigInteger.ONE;
        BigInteger price = BigInteger.valueOf(300).multiply(ICX);
        score.invoke(admin, "toggleSettingEnabled", gkScore.getAddress());
        doReturn(nftOwner.getAddress()).when(spyScore).getNftOwner(saleId);
        doReturn(true).when(spyScore).operatorIsApprovedForAll(gkScore.getAddress(), nftOwner.getAddress(), score.getAddress());
        doReturn(BigInteger.valueOf(10)).when(spyScore).getBalanceOfOwner(any(), any(), any());
        score.invoke(nftOwner, "setPrice", gkScore.getAddress(), price, BigInteger.ZERO, 5);

        doReturn(sm.createAccount().getAddress()).when(spyScore).getNftOwner(gkScore.getAddress(), BigInteger.ONE);
        Executable call = () -> score.invoke(owner, "removeFromSale", saleId);
        expectErrorMessage(call, Message.Not.nftOwner());
        doReturn(nftOwner.getAddress()).when(spyScore).getNftOwner(gkScore.getAddress(), BigInteger.ONE);
        score.invoke(nftOwner, "removeFromSale", saleId);

        call = () -> score.invoke(nftOwner, "getRate", BigInteger.TWO);
        expectErrorMessage(call, Message.Not.found("Sale"));
    }

    @Test
    void testBuy(){
        final BigInteger saleId = BigInteger.valueOf(5);
        Account buyer = sm.createAccount();
        buyer.addBalance("ICX", BigInteger.valueOf(10000).multiply(ICX));
        Executable call = () -> score.invoke(buyer, "buy", saleId);
        expectErrorMessage(call,  Message.Not.found("Sale"));

        int count = 5;
        BigInteger price = BigInteger.valueOf(30).multiply(ICX);
        BigInteger totalPrice = price.multiply(BigInteger.valueOf(count));
        BigInteger value = BigInteger.TWO;
        doReturn(value).when(spyScore).getPaidAmount();
        doReturn(nftOwner.getAddress()).when(spyScore).getNftOwner(saleId);
        doReturn(true).when(spyScore).operatorIsApprovedForAll(gkScore.getAddress(), nftOwner.getAddress(), score.getAddress());
        doReturn(BigInteger.valueOf(10)).when(spyScore).getBalanceOfOwner(any(), any(), any());
        score.invoke(admin, "toggleSettingEnabled", gkScore.getAddress());
        score.invoke(nftOwner, "setPrice", gkScore.getAddress(), price, BigInteger.ZERO, count);
        final BigInteger correctSaleId = BigInteger.ONE;
        call = () -> score.invoke(buyer, "buy", correctSaleId);
        expectErrorMessage(call,  Message.Not.enabled("Buying"));

        score.invoke(admin, "toggleBuyingEnabled", gkScore.getAddress());
        doReturn(nftOwner.getAddress()).when(spyScore).getNftOwner(gkScore.getAddress(), BigInteger.ONE);
        call = () -> score.invoke(buyer, "buy", correctSaleId);
        expectErrorMessage(call,  Message.priceMisMatch(totalPrice, value));


        doReturn(totalPrice).when(spyScore).getPaidAmount();
        call = () -> score.invoke(nftOwner, "buy", correctSaleId);
        expectErrorMessage(call,  Message.Found.own());

        doReturn(false).when(spyScore).operatorIsApprovedForAll(any(), any(), any());
        call = () -> score.invoke(buyer, "buy", correctSaleId);
        expectErrorMessage(call,  Message.Not.approved());

        score.invoke(admin, "setMarketplaceFee", gkScore.getAddress(), BigInteger.valueOf(25).multiply(pow(BigInteger.TEN, 17)));
        doReturn(true).when(spyScore).operatorIsApprovedForAll(any(), any(), any());
        doNothing().when(spyScore).transferOwnershipFrom(any(), any(), any(), any());
        doNothing().when(spyScore).payToSeller(any(), any());
        score.invoke(buyer, "buy", correctSaleId);

        verify(spyScore).NFTSold(correctSaleId, nftOwner.getAddress(), buyer.getAddress(), totalPrice);
    }

    @Test
    void testSetPriceWithNftId(){
        BigInteger price = BigInteger.valueOf(300).multiply(ICX);
        score.invoke(admin, "toggleSettingEnabled", gkScore.getAddress());

        doReturn(true).when(spyScore).operatorIsApprovedForAll(any(), any(), any());
        doReturn(BigInteger.valueOf(10)).when(spyScore).getBalanceOfOwner(nftOwner.getAddress(), gkScore.getAddress(), BigInteger.valueOf(5));
        Account notOwner = sm.createAccount();
        doReturn(BigInteger.valueOf(2)).when(spyScore).getBalanceOfOwner(notOwner.getAddress(), gkScore.getAddress(), BigInteger.valueOf(5));
        Executable call = () -> score.invoke(notOwner, "setPrice", gkScore.getAddress(), price, BigInteger.valueOf(5), 5);
        expectErrorMessage(call, Message.Not.enough());

        score.invoke(nftOwner, "setPrice", gkScore.getAddress(), price, BigInteger.valueOf(5), 5);

        assertEquals(price, score.call("getRate", BigInteger.ONE));

        List<SaleDB> history = (List<SaleDB>) score.call("getPutOnSaleHistory", gkScore.getAddress(), BigInteger.valueOf(5), 10, 0, "asc");
        assertEquals(nftOwner.getAddress(), history.get(0).getOwner());

        call = () ->score.invoke(nftOwner, "getRate", BigInteger.TWO);
        expectErrorMessage(call, Message.Not.found("Sale"));
    }

    @Test
    void testRemoveFromSaleWithNftId(){
        BigInteger saleId=BigInteger.ONE;
        BigInteger price = BigInteger.valueOf(300).multiply(ICX);
        score.invoke(admin, "toggleSettingEnabled", gkScore.getAddress());

        doReturn(nftOwner.getAddress()).when(spyScore).getNftOwner(gkScore.getAddress(), BigInteger.valueOf(5));
        doReturn(true).when(spyScore).operatorIsApprovedForAll(any(), any(), any());
        doReturn(BigInteger.valueOf(10)).when(spyScore).getBalanceOfOwner(any(), any(), any());
        score.invoke(nftOwner, "setPrice", gkScore.getAddress(), price, BigInteger.valueOf(5), 0);

        doReturn(nftOwner.getAddress()).when(spyScore).getNftOwner(gkScore.getAddress(), BigInteger.ONE);
        doReturn(sm.createAccount().getAddress()).when(spyScore).getNftOwner(saleId);
        Executable call = () -> score.invoke(owner, "removeFromSale", saleId);
        expectErrorMessage(call, Message.Not.nftOwner());
        doReturn(nftOwner.getAddress()).when(spyScore).getNftOwner(saleId);
        score.invoke(nftOwner, "removeFromSale", saleId);

        call = () -> score.invoke(nftOwner, "getRate", BigInteger.TWO);
        expectErrorMessage(call, Message.Not.found("Sale"));
    }

    @Test
    void testBuyWithNftId(){
        final BigInteger saleId = BigInteger.valueOf(5);
        Account buyer = sm.createAccount();
        buyer.addBalance("ICX", BigInteger.valueOf(10000).multiply(ICX));

        int count = 1;
        BigInteger price = BigInteger.valueOf(30).multiply(ICX);
        BigInteger totalPrice = price.multiply(BigInteger.valueOf(count));
        BigInteger value = BigInteger.TWO;
        doReturn(value).when(spyScore).getPaidAmount();
        doReturn(nftOwner.getAddress()).when(spyScore).getNftOwner(saleId);
        doReturn(nftOwner.getAddress()).when(spyScore).getNftOwner(gkScore.getAddress(), BigInteger.valueOf(5));
        doReturn(true).when(spyScore).operatorIsApprovedForAll(gkScore.getAddress(), nftOwner.getAddress(), score.getAddress());
        doReturn(BigInteger.valueOf(10)).when(spyScore).getBalanceOfOwner(any(), any(), any());
        score.invoke(admin, "toggleSettingEnabled", gkScore.getAddress());
        score.invoke(nftOwner, "setPrice", gkScore.getAddress(), price, BigInteger.valueOf(5), count);
        final BigInteger correctSaleId = BigInteger.ONE;

        score.invoke(admin, "toggleBuyingEnabled", gkScore.getAddress());
        doReturn(totalPrice).when(spyScore).getPaidAmount();

        score.invoke(admin, "setMarketplaceFee", gkScore.getAddress(), BigInteger.valueOf(25).multiply(pow(BigInteger.TEN, 17)));
        doReturn(true).when(spyScore).operatorIsApprovedForAll(any(), any(), any());
        doNothing().when(spyScore).transferOwnershipFrom(any(), any(), any(), any());
        doNothing().when(spyScore).payToSeller(any(), any());
        score.invoke(buyer, "buy", correctSaleId);

        verify(spyScore).NFTSold(correctSaleId, nftOwner.getAddress(), buyer.getAddress(), totalPrice);
    }


}
