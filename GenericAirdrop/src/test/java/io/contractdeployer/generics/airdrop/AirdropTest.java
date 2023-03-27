package io.contractdeployer.generics.airdrop;


import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;

public class AirdropTest extends TestBase {

    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account user = sm.createAccount();
    private static final Account minter = sm.createAccount();
    private static final Account dummyScore = sm.createAccount();

    private static Score airdropScore;
    private static Airdrop tokenSpy;

    @BeforeEach
    public void setup() throws Exception {
        airdropScore = sm.deploy(owner, Airdrop.class);
        // setup spy object against the airdropScore object
        tokenSpy = (Airdrop) spy(airdropScore.getInstance());
        airdropScore.setInstance(tokenSpy);
    }

    @Test
    void airdrop(){
        assertThrows(RuntimeException.class,()->airdropScore.invoke
                (owner,"airdrop","randomKey",dummyScore.getAddress(),user.getAddress(),owner.getAddress()),
                "Invalid Key");

    }

}
