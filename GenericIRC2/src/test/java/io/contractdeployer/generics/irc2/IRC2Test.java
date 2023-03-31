package io.contractdeployer.generics.irc2;

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

import static java.math.BigInteger.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class IRC2Test extends TestBase {

    protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    private static final String name = "IRC2Token";
    private static final String symbol = "IRC2";
    private static final BigInteger decimals = BigInteger.valueOf(18);
    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account alice = sm.createAccount();
    private static final Account bob = sm.createAccount();
    private static Score ircScore;
    private static IRC2 tokenSpy;

    @BeforeEach
    public void setup() throws Exception {
        ircScore = sm.deploy(owner, IRC2.class, name, symbol, decimals,owner.getAddress());

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
        assertEquals(ircScore.call("decimals"),decimals);
    }

    @Test
    void totalSupply(){
        assertEquals(ircScore.call("totalSupply"),BigInteger.ZERO);
    }

    @Test
    void setMinter_not_by_owner(){
        Executable call = () -> ircScore.invoke(alice,"setMinter", alice.getAddress());
        expectErrorMessage(call, "IRC2 :: Only owner can perform this action.");

        verify(tokenSpy,never()).TokenMinterUpdated(alice.getAddress());
    }

    @Test
    void setMinter(){

        ircScore.invoke(owner,"setMinter", alice.getAddress());
        assertEquals(ircScore.call("getMinter"), alice.getAddress());

        verify(tokenSpy).TokenMinterUpdated(alice.getAddress());
    }

    @Test
    void mint_negative_value(){
        BigInteger negativeAmount = BigInteger.valueOf(100).negate();

        Executable call = () -> ircScore.invoke(owner,"mint",alice.getAddress(),negativeAmount);
        expectErrorMessage(call, "amount needs to be positive");
    }

    @Test
    void mint_to_zero_address(){
        BigInteger amount = BigInteger.valueOf(100).multiply(ICX);

        Executable call = () -> ircScore.invoke(owner,"mint",ZERO_ADDRESS,amount);
        expectErrorMessage(call, "Owner address cannot be zero address");
    }

    @Test
    void mint_more_than_cap(){
        BigInteger amount = BigInteger.valueOf(1001).multiply(ICX);

        ircScore.invoke(owner,"mint",alice.getAddress(),amount);

    }

    @Test
    void mint(){
        BigInteger amount = BigInteger.valueOf(100).multiply(ICX);

        // mint to alice
        ircScore.invoke(owner,"mint", alice.getAddress(),amount);

        assertEquals(ircScore.call("totalSupply"),amount);
        assertEquals(ircScore.call("balanceOf",alice.getAddress()),amount);

        // mint to bob
        ircScore.invoke(owner,"mint", bob.getAddress(),amount);

        assertEquals(ircScore.call("totalSupply"),amount.add(amount));
        assertEquals(ircScore.call("balanceOf",bob.getAddress()),amount);

        // verification
        verify(tokenSpy).Mint(owner.getAddress(),alice.getAddress(),amount);
        verify(tokenSpy).Mint(owner.getAddress(),bob.getAddress(),amount);
        verify(tokenSpy).Transfer(ZERO_ADDRESS,alice.getAddress(),amount,"mint".getBytes());
        verify(tokenSpy).Transfer(ZERO_ADDRESS,bob.getAddress(),amount,"mint".getBytes());
    }

    @Test
    void mint_after_ownership_transfer(){
        BigInteger amount = BigInteger.valueOf(100).multiply(ICX);

        assertEquals(ircScore.call("getMinter"),owner.getAddress());

        // minter access to alice
        ircScore.invoke(owner,"setMinter",alice.getAddress());

        assertEquals(ircScore.call("getMinter"),alice.getAddress());

        //  mint allowed by owner and minter
        ircScore.invoke(alice,"mint",bob.getAddress(),amount);
        ircScore.invoke(owner,"mint",bob.getAddress(),amount);

        verify(tokenSpy).Mint(owner.getAddress(),bob.getAddress(),amount);
        verify(tokenSpy).Mint(alice.getAddress(),bob.getAddress(),amount);

    }

    @Test
    void burn_negative_amount(){
        BigInteger amount = BigInteger.valueOf(10).multiply(ICX);

        // mint to alice
        ircScore.invoke(owner,"mint", alice.getAddress(),amount);

        Executable call = () -> ircScore.invoke(alice,"burn", amount.negate());
        expectErrorMessage(call, "amount needs to be positive");

    }

    @Test
    void burn_more_than_balance(){
        BigInteger amount = BigInteger.valueOf(10).multiply(ICX);

        // mint to alice
        ircScore.invoke(owner,"mint", alice.getAddress(),amount.subtract(BigInteger.ONE));

        Executable call = () -> ircScore.invoke(alice,"burn", amount);
        expectErrorMessage(call, "Insufficient balance");

    }

    @Test
    void burn(){
        BigInteger amount = BigInteger.valueOf(100).multiply(ICX);

        ircScore.invoke(owner,"mint",bob.getAddress(),amount);

        assertEquals(ircScore.call("balanceOf",bob.getAddress()),amount);
        assertEquals(ircScore.call("totalSupply"),amount);

        BigInteger amountToBurn = TEN.multiply(ICX);
        ircScore.invoke(bob,"burn", amountToBurn);

        assertEquals(ircScore.call("balanceOf",bob.getAddress()),amount.subtract(amountToBurn));
        assertEquals(ircScore.call("totalSupply"),amount.subtract(amountToBurn));

        verify(tokenSpy).Burn(bob.getAddress(),amountToBurn);
        verify(tokenSpy).Transfer(bob.getAddress(),ZERO_ADDRESS,amountToBurn,"burn".getBytes());
    }

    public static void expectErrorMessage(Executable contractCall, String errorMessage) {
        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
        assertEquals(errorMessage, e.getMessage());
    }

}
