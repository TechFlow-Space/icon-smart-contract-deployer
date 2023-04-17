package io.contractdeployer.generics.irc3;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import score.Address;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class IRC3Test extends TestBase {

    private static final String name="IRC3 token";
    private static final String symbol="IRC3";
    private static final BigInteger cap=BigInteger.valueOf(5);
    private static final BigInteger mintCost=BigInteger.valueOf(100).multiply(ICX);
    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account user = sm.createAccount();
    private static final Account admin = sm.createAccount();
    private static Score ircScore;
    private static IRC3 tokenSpy;

    @BeforeEach
    public void setup() throws Exception {
        ircScore = sm.deploy(owner, IRC3.class,name,symbol,cap,mintCost);

        // setup spy object against the ircScore object
        tokenSpy = (IRC3) spy(ircScore.getInstance());
        ircScore.setInstance(tokenSpy);
    }

    @Test
    void setAdmin(){
        Executable call = () -> ircScore.invoke(user,"setAdmin", user.getAddress());
        expectErrorMessage(call, "IRC3 :: Only owner can perform this action");

        ircScore.invoke(owner,"setAdmin", user.getAddress());
        assertEquals(ircScore.call("getAdmin"),user.getAddress());
        verify(tokenSpy).TransferAdmin(owner.getAddress(),user.getAddress());
    }

    @Test
    void setMintCost(){
        BigInteger amount = BigInteger.TWO.multiply(ICX);
        Executable call = () -> ircScore.invoke(user,"setMintCost", amount);
        expectErrorMessage(call, "IRC3 :: Only admin/owner can perform this action.");

        ircScore.invoke(owner,"setMintCost", amount);
        assertEquals(ircScore.call("getMintCost"),amount);

        // transfer admin rights
        ircScore.invoke(owner,"setAdmin", admin.getAddress());
        assertEquals(ircScore.call("getAdmin"),admin.getAddress());

        ircScore.invoke(owner,"setMintCost", amount.add(BigInteger.TEN));
        assertEquals(ircScore.call("getMintCost"),amount.add(BigInteger.TEN));
    }

    @Test
    void getTokenId(){
        assertEquals(ircScore.call("getTokenId"),BigInteger.valueOf(0));
    }

    @Test
    void mint_for_incorrect_amount(){
        BigInteger paidAmount = BigInteger.valueOf(99).multiply(ICX);
        doReturn(paidAmount).when(tokenSpy).getPaidValue();

        Executable call = () -> ircScore.invoke(user,"mint","uri");
        expectErrorMessage(call, "IRC3 :: Price Mismatch");


        paidAmount = BigInteger.valueOf(101).multiply(ICX);
        doReturn(paidAmount).when(tokenSpy).getPaidValue();

        call = () -> ircScore.invoke(user,"mint","uri");
        expectErrorMessage(call, "IRC3 :: Price Mismatch");
    }
    @Test
    void mint(){

        doReturn(mintCost).when(tokenSpy).getPaidValue();
        ircScore.invoke(user,"mint","minting IRC3");

        assertEquals(balanceOf(user.getAddress()),1);
        assertEquals(ircScore.call("getTokenUri",BigInteger.valueOf(1)),"minting IRC3");

        verify(tokenSpy).Mint(user.getAddress(),BigInteger.valueOf(1));


    }

    @Test
    void mint_more_than_cap(){
        doReturn(mintCost).when(tokenSpy).getPaidValue();
        for(int i=0;i<5;i++){
            ircScore.invoke(user,"mint","uri");
        }

        Executable call = () -> ircScore.invoke(user,"mint","uri");
        expectErrorMessage(call, "IRC3 :: Cap Exceeded.");

        assertEquals(balanceOf(user.getAddress()),5);
        assertEquals(ircScore.call("getTokenUri",BigInteger.valueOf(1)),"uri"); // check this

    }

    @Test
    void burn_without_user_approval(){
        doReturn(mintCost).when(tokenSpy).getPaidValue();

        // mint 2 tokens
        ircScore.invoke(user,"mint", "uri1");
        ircScore.invoke(user,"mint", "uri2");

        assertEquals(balanceOf(user.getAddress()),2);

        Executable call = () -> ircScore.invoke(admin,"burn", BigInteger.valueOf(1));
        expectErrorMessage(call, "IRC3 :: Need operator approval for 3rd party transfers.");

        assertEquals(balanceOf(user.getAddress()),2);
    }

    @Test
    void burn_non_existent_token(){
        doReturn(mintCost).when(tokenSpy).getPaidValue();

        Executable call = () -> ircScore.invoke(user,"burn", BigInteger.valueOf(1));
        expectErrorMessage(call,"Reverted(0): Non-existent token");

    }

    @Test
    void burn_by_user(){
        doReturn(mintCost).when(tokenSpy).getPaidValue();

        ircScore.invoke(user,"mint", "my_own_token");

        assertEquals(balanceOf(user.getAddress()),1);

        ircScore.invoke(user,"burn", BigInteger.ONE);

        assertEquals(balanceOf(user.getAddress()),0);

        verify(tokenSpy).Burn(user.getAddress(),BigInteger.ONE);
    }

    @Test
    void burn_by_third_party(){
        doReturn(mintCost).when(tokenSpy).getPaidValue();

        ircScore.invoke(user,"mint", "my_own_token");
        ircScore.invoke(user,"mint", "my_own_token_two");

        assertEquals(balanceOf(user.getAddress()),2);

        // user approves admin for token 1
        ircScore.invoke(user,"approve", admin.getAddress(),BigInteger.ONE);
        ircScore.invoke(admin,"burn", BigInteger.ONE);

        assertEquals(balanceOf(user.getAddress()),1);

        // approval for token 2 is not set
        Executable call = () -> ircScore.invoke(admin,"burn", BigInteger.TWO);
        expectErrorMessage(call, "IRC3 :: Need operator approval for 3rd party transfers.");


    }

    private void expectErrorMessage(Executable contractCall, String errorMessage) {
        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
        assertEquals(errorMessage, e.getMessage());
    }

    private int balanceOf(Address user){
        return (int) ircScore.call("balanceOf",user);
    }
}
