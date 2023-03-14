package io.tokenfactory.score;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import io.tokenfactory.score.dbs.ContractDB;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import score.Address;

import java.math.BigInteger;

import static io.tokenfactory.score.TestHelper.expectErrorMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class TokenFactoryTest extends TestBase{

    protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    private static final BigInteger deployFee = BigInteger.valueOf(10);
    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account admin = sm.createAccount();
    private static final Account user = sm.createAccount();
    private static final Account contract = sm.createAccount();
    private static final Account treasury = sm.createAccount();
    private static Score score;
    private static TokenFactory spyScore;

    @BeforeEach
    public void setup() throws Exception {
        score = sm.deploy(owner, TokenFactory.class,
                admin.getAddress(), treasury.getAddress(), deployFee);
        // setup spy object against the score object
        spyScore = (TokenFactory) spy(score.getInstance());
        score.setInstance(spyScore);
    }

    @Test
    void setAdmin(){
        Account user = sm.createAccount();

        Executable call = () -> score.invoke(admin,"setAdmin", admin.getAddress());
        expectErrorMessage(call, Message.Not.owner());

        score.invoke(owner,"setAdmin", user.getAddress());

        assertEquals(score.call("getAdmin"),user.getAddress());
    }

    @Test
    void setTreasury(){
        Account user = sm.createAccount();

        Executable call = () -> score.invoke(admin,"setTreasury", user.getAddress());
        expectErrorMessage(call, Message.Not.owner());

        score.invoke(owner,"setTreasury", user.getAddress());

        assertEquals(score.call("getTreasury"),user.getAddress());
    }

    @Test
    void setContractContent(){

        Executable call = () -> score.invoke(user,"setContractContent", "IRC2","content".getBytes());
        expectErrorMessage(call, Message.Not.admin());

        call = () -> score.invoke(admin,"setContractContent", "IRC21","content".getBytes());
        expectErrorMessage(call, Message.Not.validContract());

        score.invoke(admin,"setContractContent", "IRC2","content".getBytes());

        call = () -> score.invoke(admin,"setContractContent", "IRC2","content".getBytes());
        expectErrorMessage(call, Message.duplicateContract());

    }

    @Test
    void getDeployFee(){
        assertEquals(score.call("getDeployFee"),deployFee);
    }

    @Test
    void deployContract(){

        JsonObject data = Json.object().add("name", "MyIRC2").add(
                "symbol", "IRC2").add("decimal","18");
        byte[] irc2_data=data.toString().getBytes();

        doReturn(deployFee).when(spyScore).getPaidValue();

        Executable call = () -> score.invoke(user,"deployContract", "IRC2",ZERO_ADDRESS,irc2_data);
        expectErrorMessage(call, Message.zeroAddress());

        doReturn(BigInteger.valueOf(5)).when(spyScore).getPaidValue();

        call = () -> score.invoke(user,"deployContract", "IRC2",user.getAddress(),irc2_data);
        expectErrorMessage(call, Message.paymentMismatch());

        doReturn(deployFee).when(spyScore).getPaidValue();

        call = () -> score.invoke(user,"deployContract", "IRC2",user.getAddress(),irc2_data);
        expectErrorMessage(call, Message.Not.deployed());

        score.invoke(admin,"setContractContent", "IRC2","content".getBytes());

        doReturn(contract.getAddress()).when(spyScore).deployContract("IRC2","content".getBytes(),irc2_data);

        doNothing().when(spyScore).setScoreOwner(contract.getAddress(),user.getAddress());

        score.invoke(user,"deployContract", "IRC2",user.getAddress(),irc2_data);

        assertEquals(score.call("deployCount"),1);

        ContractDB[] deployedContracts = (ContractDB[]) score.call("getDeployedContracts", user.getAddress(), 0,2, "asc");
        assertEquals(1, deployedContracts.length);
        assertEquals(user.getAddress(), deployedContracts[0].getDeployer());
        assertEquals("IRC2", deployedContracts[0].getName());


    }

}
