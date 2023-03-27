package io.contractdeployer.generics.irc2;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import score.Address;

import java.math.BigInteger;

import static io.contractdeployer.generics.irc2.TestHelper.expectErrorMessage;
import static java.math.BigInteger.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;

public class IRC2Test extends TestBase {

    protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    private static final String name = "IRC2Token";
    private static final String symbol = "IRC2";
    private static final int decimals = 18;
    private static final BigInteger initialSupply = BigInteger.valueOf(1000);

    private static BigInteger totalSupply = initialSupply.multiply(TEN.pow(decimals));
    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account user = sm.createAccount();
    private static final Account minter = sm.createAccount();
    private static Score ircScore;
    private static IRC2 tokenSpy;

    @BeforeEach
    public void setup() throws Exception {
        ircScore = sm.deploy(owner, IRC2.class,
                name, symbol, BigInteger.valueOf(decimals),minter.getAddress());
        ircScore.invoke(minter,"mint",owner.getAddress(),totalSupply);

        // setup spy object against the ircScore object
        tokenSpy = (IRC2) spy(ircScore.getInstance());
        ircScore.setInstance(tokenSpy);
    }

    @Test
    void name(){
        assertEquals(ircScore.call("name"),name);
    }

    @Test
    void symbol(){
        assertEquals(ircScore.call("symbol"),symbol);
    }

    @Test
    void decimals(){
        assertEquals(ircScore.call("decimals"),BigInteger.valueOf(decimals));
    }

    @Test
    void totalSupply(){
        assertEquals(ircScore.call("totalSupply"),totalSupply);
    }

    @Test
    void balanceOf(){
        assertEquals(ircScore.call("balanceOf",owner.getAddress()),totalSupply);
        assertEquals(ircScore.call("balanceOf",user.getAddress()),BigInteger.ZERO);
    }

    @Test
    void transfer(){
        byte[] data=new byte[0];

        Executable call = () -> ircScore.invoke(owner,"transfer", owner.getAddress(),BigInteger.ZERO,data);
        expectErrorMessage(call, Message.Not.self());

        call = () -> ircScore.invoke(owner,"transfer", user.getAddress(),BigInteger.ZERO,data);
        expectErrorMessage(call, Message.greaterThanZero("_value"));

        call = () -> ircScore.invoke(user,"transfer", owner.getAddress(), BigInteger.TEN,data);
        expectErrorMessage(call, Message.Not.enoughBalance());

        ircScore.invoke(owner,"transfer", user.getAddress(), BigInteger.TEN,data);
        assertEquals(ircScore.call("balanceOf",user.getAddress()),BigInteger.TEN);
        assertEquals(ircScore.call("balanceOf",owner.getAddress()),totalSupply.subtract(BigInteger.TEN));
    }

    @Test
    void setMinter(){
        Executable call = () -> ircScore.invoke(user,"setMinter", user.getAddress());
        expectErrorMessage(call, Message.Not.owner());

        ircScore.invoke(owner,"setMinter", user.getAddress());
        assertEquals(ircScore.call("getMinter"),user.getAddress());
    }

    @Test
    void mint(){
        int val=10000;
        Account admin=sm.createAccount();
        ircScore.invoke(owner,"setMinter", admin.getAddress());

        Executable call = () -> ircScore.invoke(owner,"mint", owner.getAddress(),BigInteger.valueOf(val));
        expectErrorMessage(call, Message.Not.minter());

        call = () -> ircScore.invoke(admin,"mint", ZERO_ADDRESS,BigInteger.valueOf(val));
        expectErrorMessage(call, Message.Found.zeroAddr("Owner"));

        call = () -> ircScore.invoke(admin,"mint", user.getAddress(),BigInteger.ZERO);
        expectErrorMessage(call, Message.greaterThanZero("Amount"));

        ircScore.invoke(admin,"mint", owner.getAddress(),BigInteger.valueOf(val));
        assertEquals(ircScore.call("totalSupply"),totalSupply.add(BigInteger.valueOf(val)));
        assertEquals(ircScore.call("balanceOf",owner.getAddress()),totalSupply.add(BigInteger.valueOf(val)));
    }

    @Test
    void burn(){
        int val=10000;

        Executable call = () -> ircScore.invoke(owner,"burn", BigInteger.ZERO);
        expectErrorMessage(call, Message.greaterThanZero("Amount"));

        ircScore.invoke(owner,"burn", BigInteger.valueOf(val));
        assertEquals(ircScore.call("totalSupply"),totalSupply.subtract(BigInteger.valueOf(val)));
        assertEquals(ircScore.call("balanceOf",owner.getAddress()),totalSupply.subtract(BigInteger.valueOf(val)));
    }

}
