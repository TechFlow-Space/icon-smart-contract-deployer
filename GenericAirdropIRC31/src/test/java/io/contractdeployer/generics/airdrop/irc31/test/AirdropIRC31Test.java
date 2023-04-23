package io.contractdeployer.generics.airdrop.irc31.test;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;

import io.contractdeployer.generics.airdrop.irc31.AirdropIRC31;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import score.Address;

import java.math.BigInteger;

import static io.contractdeployer.generics.airdrop.irc31.AirdropIRC31.TAG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


public class AirdropIRC31Test extends TestBase {

    private static final ServiceManager sm = getServiceManager();
    private static Score score;

    public static Account owner = sm.createAccount();
    public static Account alice = sm.createAccount();
    public static Account bob = sm.createAccount();
    public static Account catlin = sm.createAccount();
    public static Account dora = sm.createAccount();

    public static Account testScore = Account.newScoreAccount(0);
    public static Account dummyScore = Account.newScoreAccount(100);
    private static AirdropIRC31 spyScore;

    @BeforeEach
    public void setup() throws Exception {
        score = sm.deploy(owner, AirdropIRC31.class);
        // setup spy object against the score object
        spyScore = (AirdropIRC31) spy(score.getInstance());
        score.setInstance(spyScore);
    }

    @Test
    public void addRecipients_by_others(){
        Executable call =  () -> addRecipients(alice);
        expectErrorMessage(call,TAG + " :: Only owner can perform this action");
    }

    @Test
    public void addRecipients(){
        addRecipients(owner);

        assertEquals(score.call("getAirdroppedCount"),2);
        assertEquals(score.call("getProcessedCount"),0);
        System.out.println(score.call("getListedUserInfo"));
    }

    @Test
    public void airdrop_to_users_by_other(){
        addRecipients(owner);

        Executable call =  () -> score.invoke(alice,"airdropToListedUsers",2);
        expectErrorMessage(call,TAG + " :: Only owner can perform this action");


    }

    @Test
    public void airdrop_to_users(){
        addRecipients(owner);

        doReturn(true).when(spyScore).call(boolean.class, testScore.getAddress(),
                "isApprovedForAll", alice.getAddress(),score.getAddress());
        doReturn(true).when(spyScore).call(boolean.class, dummyScore.getAddress(),
                "isApprovedForAll", bob.getAddress(),score.getAddress());

        doReturn(BigInteger.valueOf(1681_721_966).multiply(BigInteger.valueOf(1000_000))).
                when(spyScore).now();

        doNothing().when(spyScore).call(testScore.getAddress(),
                "transferFrom",alice.getAddress(),catlin.getAddress(),BigInteger.TEN,BigInteger.TEN.multiply(ICX));
        doNothing().when(spyScore).call(dummyScore.getAddress(),
                "transferFrom",bob.getAddress(),dora.getAddress(),BigInteger.ONE,BigInteger.TWO.multiply(ICX));

        score.invoke(owner,"airdropToListedUsers",2);

        assertEquals(score.call("getAirdroppedCount"),2);
        assertEquals(score.call("getProcessedCount"),2);

    }

    @Test
    public void airdrop_again(){
        airdrop_to_users();

        Executable call = () -> score.invoke(owner,"airdropToListedUsers",2);
        expectErrorMessage(call,TAG + " :: Invalid no of Payments");

    }

    private void addRecipients(Account caller){
        Address[] tokenAddress = new Address[]{testScore.getAddress(),dummyScore.getAddress()};
        Address[] owners = new Address[]{alice.getAddress(),bob.getAddress()};
        Address[] recipients = new Address[]{catlin.getAddress(),dora.getAddress()};
        BigInteger[] tokenId = new BigInteger[]{BigInteger.TEN,BigInteger.ONE};
        BigInteger[] value = new BigInteger[]{BigInteger.TEN.multiply(ICX),BigInteger.TWO.multiply(ICX)};
        BigInteger[] timestamp = new BigInteger[]{BigInteger.valueOf(1681_721_965).multiply(BigInteger.valueOf(1000_000)),
                BigInteger.valueOf(1681_109_922).multiply(BigInteger.valueOf(1000_000))};

        score.invoke(caller,"addRecipients",
                tokenAddress,owners,recipients,tokenId,value,timestamp);
    }

    public static void expectErrorMessage(Executable contractCall, String errorMessage) {
        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
        assertEquals(errorMessage, e.getMessage());
    }

}
