package io.contractdeployer.generics.airdrop.irc2.test;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import io.contractdeployer.generics.airdrop.irc2.AirdropIRC2;
import io.contractdeployer.generics.airdrop.irc2.UserDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import score.Address;

import java.math.BigInteger;
import java.util.List;

import static io.contractdeployer.generics.airdrop.irc2.AirdropIRC2.TAG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AirdropIrc2Test extends TestBase {
    private static final ServiceManager sm = getServiceManager();
    private static Score score;

    public static Account owner = sm.createAccount();
    public static Account alice = sm.createAccount();
    public static Account bob = sm.createAccount();

    public static Account IRC2Score = Account.newScoreAccount(0);

    public static BigInteger HUNDRED = BigInteger.valueOf(100);
    public static BigInteger TEN = BigInteger.valueOf(10);

    private static AirdropIRC2 spyScore;
    @BeforeEach
    public void setup() throws Exception {
        score = sm.deploy(owner,AirdropIRC2.class);
        // setup spy object against the score object
        spyScore = (AirdropIRC2) spy(score.getInstance());
        score.setInstance(spyScore);
    }

    @Test
    public void testName(){
        String expected = (String) score.call("name");
        assertEquals(expected,"Airdrop IRC2");
    }

    @Test
    public void single_user_airdrop_by_non_minter(){

        doReturn(false).when(spyScore).checkMinter(IRC2Score.getAddress());

        Executable call = () -> score.invoke(alice,"airdropIRC2",IRC2Score.getAddress(),
                owner.getAddress(),HUNDRED.multiply(ICX));
        expectErrorMessage(call, TAG+" :: IRC2 airdrop can only be initiated by the minter.");


    }

    @Test
    public void single_user_airdrop(){

        BigInteger amount = HUNDRED.multiply(ICX);

        doReturn(true).when(spyScore).checkMinter(IRC2Score.getAddress());

        doNothing().when(spyScore).call(IRC2Score.getAddress(),"mint",alice.getAddress(),amount);
        score.invoke(owner,"airdropIRC2",IRC2Score.getAddress(),
                alice.getAddress(),amount);

         assertEquals(HUNDRED.multiply(ICX),score.call("getDistributedTokensOf",alice.getAddress()));

        verify(spyScore).IRC2Airdropped(alice.getAddress(),amount);
        verify(spyScore).call(IRC2Score.getAddress(),"mint",alice.getAddress(),amount);

    }

    @Test
    public void multiple_user_invalid_length(){
        Address[] recipients = new Address[]{alice.getAddress(),bob.getAddress()};
        BigInteger[] amount = new BigInteger[]{HUNDRED.multiply(ICX)};


        Executable call = () -> score.invoke(owner,"airdropIRC2Batch",IRC2Score.getAddress(),recipients,amount);

        expectErrorMessage(call, TAG+" :: Arrays do not have the same length.");


    }

    @Test
    public void multiple_user_airdrop(){
        Address[] recipients = new Address[]{alice.getAddress(),bob.getAddress()};
        BigInteger[] amount = new BigInteger[]{HUNDRED.multiply(ICX),TEN.multiply(ICX)};

        doReturn(true).when(spyScore).checkMinter(IRC2Score.getAddress());

        doNothing().when(spyScore).call(IRC2Score.getAddress(),"mint",recipients[0],amount[0]);
        doNothing().when(spyScore).call(IRC2Score.getAddress(),"mint",recipients[1],amount[1]);

        score.invoke(owner,"airdropIRC2Batch",IRC2Score.getAddress(),recipients,amount);

        verify(spyScore).IRC2Airdropped(recipients[0],amount[0]);
        verify(spyScore).IRC2Airdropped(recipients[1],amount[1]);

    }

    @Test
    public void multiple_instance_multiple_user_airdrop(){
        Address[] recipients = new Address[]{alice.getAddress(),bob.getAddress()};
        BigInteger[] amount = new BigInteger[]{HUNDRED.multiply(ICX),TEN.multiply(ICX)};

        doReturn(true).when(spyScore).checkMinter(IRC2Score.getAddress());

        doNothing().when(spyScore).call(IRC2Score.getAddress(),"mint",recipients[0],amount[0]);
        doNothing().when(spyScore).call(IRC2Score.getAddress(),"mint",recipients[1],amount[1]);

        score.invoke(owner,"airdropIRC2Batch",IRC2Score.getAddress(),recipients,amount);


        assertEquals(HUNDRED.multiply(ICX),score.call("getDistributedTokensOf",recipients[0]));
        assertEquals(TEN.multiply(ICX),score.call("getDistributedTokensOf",recipients[1]));


        // airdropping again
        score.invoke(owner,"airdropIRC2Batch",IRC2Score.getAddress(),recipients,amount);

        assertEquals(HUNDRED.multiply(ICX).multiply(BigInteger.TWO),score.call("getDistributedTokensOf",recipients[0]));
        assertEquals(TEN.multiply(ICX).multiply(BigInteger.TWO),score.call("getDistributedTokensOf",recipients[1]));

        verify(spyScore,times(2)).IRC2Airdropped(recipients[0],amount[0]);
        verify(spyScore,times(2)).IRC2Airdropped(recipients[1],amount[1]);

    }

    @Test
    public void add_users_to_airdrop_invalid_timestamp(){
        Address[] recipients = new Address[]{alice.getAddress(),bob.getAddress()};
        BigInteger[] amount = new BigInteger[]{HUNDRED.multiply(ICX),TEN.multiply(ICX)};
        BigInteger[] timestamp = new BigInteger[]{BigInteger.valueOf(1681109922),BigInteger.valueOf(1681109922)};

        Executable call =  () -> score.invoke(owner,"addRecipients",recipients,amount,timestamp);
        expectErrorMessage(call,TAG+" :: Time stamp should be in microseconds.");

    }

    @Test
    public void add_users_to_airdrop(){
        Address[] recipients = new Address[]{alice.getAddress(),bob.getAddress()};
        BigInteger[] amount = new BigInteger[]{HUNDRED.multiply(ICX),TEN.multiply(ICX)};
        BigInteger[] timestamp = new BigInteger[]{BigInteger.valueOf(1681_721_965).multiply(BigInteger.valueOf(1000_000)),
                BigInteger.valueOf(1681_109_922).multiply(BigInteger.valueOf(1000_000))};

        score.invoke(owner,"addRecipients",recipients,amount,timestamp);

        List<UserDetails> details = (List<UserDetails>) score.call("getListedUserInfo");
        for (int i = 0; i < 2; i++) {
            assertEquals(details.get(i).userAddress, recipients[i]);
            assertEquals(details.get(i).amount, amount[i]);
            assertEquals(details.get(i).timestamp, timestamp[i]);
        }

    }

    @Test
    public void airdrop_to_listed_user_by_anyone(){
        Executable call = () -> score.invoke(alice,"airdropToListedUsers",IRC2Score.getAddress());
        expectErrorMessage(call,TAG+" :: IRC2 airdrop can only be initiated by the minter.");
    }

    @Test
    public void airdrop_to_listed_user_before_timestamp(){
        add_users_to_airdrop();

        doReturn(true).when(spyScore).checkMinter(IRC2Score.getAddress());
        doReturn(BigInteger.valueOf(1681_109_921).multiply(BigInteger.valueOf(1000_000))).
                when(spyScore).getBlockTimestamp();
        Executable call = () -> score.invoke(alice,"airdropToListedUsers",IRC2Score.getAddress());
        expectErrorMessage(call,TAG+" :: Cannot call before timestamp.");
    }

    @Test
    public void airdrop_to_listed_user(){

        Address[] recipients = new Address[]{alice.getAddress(),bob.getAddress()};
        BigInteger[] amount = new BigInteger[]{HUNDRED.multiply(ICX),TEN.multiply(ICX)};

        add_users_to_airdrop();

        doReturn(true).when(spyScore).checkMinter(IRC2Score.getAddress());
        doReturn(BigInteger.valueOf(1681_721_965).multiply(BigInteger.valueOf(1000_000))).
                when(spyScore).getBlockTimestamp();
        doNothing().when(spyScore).call(IRC2Score.getAddress(),"mint",recipients[0],amount[0]);
        doNothing().when(spyScore).call(IRC2Score.getAddress(),"mint",recipients[1],amount[1]);

        score.invoke(alice,"airdropToListedUsers",IRC2Score.getAddress());

        assertEquals(HUNDRED.multiply(ICX),score.call("getDistributedTokensOf",recipients[0]));
        assertEquals(TEN.multiply(ICX),score.call("getDistributedTokensOf",recipients[1]));

        verify(spyScore).IRC2Airdropped(recipients[0],amount[0]);
        verify(spyScore).IRC2Airdropped(recipients[1],amount[1]);

    }

    public static void expectErrorMessage(Executable contractCall, String errorMessage) {
        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
        assertEquals(errorMessage, e.getMessage());
    }
}
