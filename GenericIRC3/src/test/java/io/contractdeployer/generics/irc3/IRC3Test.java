package io.contractdeployer.generics.irc3;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.math.BigInteger;

import static io.contractdeployer.generics.irc3.TestHelper.expectErrorMessage;
import static io.contractdeployer.generics.irc3.Vars.ZERO_ADDRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;

public class IRC3Test extends TestBase {

    private static final String name="IRC3";
    private static final String symbol="irc3";
    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account user = sm.createAccount();
    private static final Account admin = sm.createAccount();
    private static Score ircScore;
    private static IRC3 tokenSpy;

    @BeforeAll
    public static void setup() throws Exception {
        ircScore = sm.deploy(owner, IRC3.class,name,symbol);
        ircScore.invoke(owner,"setAdmin", admin.getAddress());
        // setup spy object against the ircScore object
        tokenSpy = (IRC3) spy(ircScore.getInstance());
        ircScore.setInstance(tokenSpy);
    }

    @Test
    void setAdmin(){
        Executable call = () -> ircScore.invoke(user,"setAdmin", user.getAddress());
        expectErrorMessage(call, Message.Not.owner());

        ircScore.invoke(owner,"setAdmin", user.getAddress());
        assertEquals(ircScore.call("getAdmin"),user.getAddress());
    }

    @Test
    void mint(){
        Executable call = () -> ircScore.invoke(user,"mint", user.getAddress(),BigInteger.valueOf(1));
        expectErrorMessage(call, Message.Not.admin());

        call = () -> ircScore.invoke(admin,"mint", ZERO_ADDRESS,BigInteger.valueOf(1));
        expectErrorMessage(call, Message.Found.zeroAddr("to"));

        ircScore.invoke(admin,"mint", user.getAddress(),BigInteger.valueOf(1));
        call = () -> ircScore.invoke(admin,"mint", user.getAddress(),BigInteger.valueOf(1));
        expectErrorMessage(call, Message.Found.token());

        ircScore.invoke(admin,"mint", user.getAddress(),BigInteger.valueOf(2));
        assertEquals(ircScore.call("balanceOf",user.getAddress()),2);
    }

    @Test
    void burn(){
        ircScore.invoke(admin,"mint", user.getAddress(),BigInteger.valueOf(1));
        ircScore.invoke(admin,"mint", user.getAddress(),BigInteger.valueOf(2));

        Executable call = () -> ircScore.invoke(admin,"burn", BigInteger.valueOf(1));
        expectErrorMessage(call, Message.Not.operatorApproved());

        call = () -> ircScore.invoke(user,"burn", BigInteger.valueOf(3));
        expectErrorMessage(call,"Reverted(0): "+Message.noToken());

        ircScore.invoke(user,"approve", admin.getAddress(),BigInteger.valueOf(1));
        ircScore.invoke(admin,"burn", BigInteger.valueOf(1));
        assertEquals(ircScore.call("balanceOf",user.getAddress()),1);

        ircScore.invoke(user,"burn", BigInteger.valueOf(2));
        assertEquals(ircScore.call("balanceOf",user.getAddress()),0);
    }

}
