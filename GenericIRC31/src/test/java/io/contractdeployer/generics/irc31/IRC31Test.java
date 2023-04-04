package io.contractdeployer.generics.irc31;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import score.Address;

import java.math.BigInteger;

import static io.contractdeployer.generics.irc31.IRC31.ZERO_ADDRESS;
import static io.contractdeployer.generics.irc31.TestHelper.expectErrorMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;

public class IRC31Test extends TestBase {

    private static final String name="IRC31";
    private static final String symbol="irc31";
    private static final BigInteger cap=BigInteger.valueOf(10);
    private static final BigInteger maxBatchMintCount=BigInteger.valueOf(5);
    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account user = sm.createAccount();
    private static final Account admin = sm.createAccount();
    private static Score ircScore;
    private static IRC31 tokenSpy;

    @BeforeEach
    public void setup() throws Exception {
        ircScore = sm.deploy(owner, IRC31.class,name,symbol,cap,maxBatchMintCount);
//        ircScore.invoke(owner,"setAdmin", admin.getAddress());
        // setup spy object against the ircScore object
        tokenSpy = (IRC31) spy(ircScore.getInstance());
        ircScore.setInstance(tokenSpy);
    }

    @Test
    void setAdmin(){
        Executable call = () -> ircScore.invoke(user,"setAdmin", user.getAddress());
        expectErrorMessage(call, "IRC31 :: Only owner can perform this action.");

        ircScore.invoke(owner,"setAdmin", user.getAddress());
        assertEquals(ircScore.call("getAdmin"),user.getAddress());
    }

    @Test
    void mint(){
        Address to = user.getAddress();
        BigInteger id = BigInteger.ONE;
        BigInteger amount = BigInteger.TWO;
        String tokenUri = "uri";
        Executable call = () -> ircScore.invoke(user,"mint", to,id,amount,tokenUri);
        expectErrorMessage(call,"IRC31 :: Only admin/owner can perform this action.");

        call = () -> ircScore.invoke(owner,"mint", ZERO_ADDRESS,id,amount,tokenUri);
        expectErrorMessage(call, "IRC31 :: Address cannot be Zero Address.");

        call = () -> ircScore.invoke(owner,"mint", to,id,BigInteger.ZERO,tokenUri);
        expectErrorMessage(call, "IRC31 :: Value must be greater than zero.");

        call = () -> ircScore.invoke(owner,"mint", to,id,BigInteger.valueOf(6),tokenUri);
        expectErrorMessage(call, "IRC31 :: NFT count per transaction exceeded.");

        ircScore.invoke(owner,"mint", to,id,amount,tokenUri);
        assertEquals(ircScore.call("balanceOf",to,id),BigInteger.valueOf(2));

        ircScore.invoke(owner,"mint", to,id,BigInteger.valueOf(5),tokenUri);


        assertEquals(ircScore.call("getTotalSupply"),BigInteger.valueOf(7));
        assertEquals(ircScore.call("tokenURI",BigInteger.valueOf(1)),tokenUri);

    }

    @Test
    void burn(){
        Address of = user.getAddress();
        BigInteger id = BigInteger.ONE;
        BigInteger amount = BigInteger.TWO;

        Executable call = () -> ircScore.invoke(admin,"burn", ZERO_ADDRESS,id,amount);
        expectErrorMessage(call, "IRC31 :: Address cannot be Zero Address.");

        call = () -> ircScore.invoke(user,"burn", of,id,BigInteger.ZERO);
        expectErrorMessage(call, "IRC31 :: Value must be greater than zero.");

        call=()->ircScore.invoke(owner,"burn", of,id,amount);
        expectErrorMessage(call, "IRC31 :: Need operator approval for 3rd party transfers.");

        // approval for all
        ircScore.invoke(user,"setApprovalForAll",owner.getAddress(),true);
        call=()->ircScore.invoke(owner,"burn", of,id,amount);
        expectErrorMessage(call, "IRC31 :: Not enough balance");

        ircScore.invoke(owner,"mint", user.getAddress(),BigInteger.valueOf(1),BigInteger.valueOf(3),"uri");
        ircScore.invoke(owner,"burn", user.getAddress(),BigInteger.valueOf(1),BigInteger.valueOf(2));
        assertEquals(ircScore.call("balanceOf",user.getAddress(),BigInteger.valueOf(1)),BigInteger.valueOf(1));

        ircScore.invoke(user,"burn", user.getAddress(),BigInteger.valueOf(1),BigInteger.valueOf(1));
        assertEquals(ircScore.call("balanceOf",user.getAddress(),BigInteger.valueOf(1)),BigInteger.valueOf(0));
    }

    @Test
    void mintBatch(){
        BigInteger[] _ids={BigInteger.valueOf(1),BigInteger.valueOf(2),BigInteger.valueOf(3)};
        String[] _uris={"uri","uri","uri"};
        BigInteger[] wrong_ids={BigInteger.valueOf(1),BigInteger.valueOf(2),BigInteger.valueOf(3),BigInteger.valueOf(4)};
        BigInteger[] _amounts={BigInteger.valueOf(1),BigInteger.valueOf(2),BigInteger.valueOf(3)};

        Executable call = () -> ircScore.invoke(admin,"mintBatch", user.getAddress(),wrong_ids,_amounts,_uris);
        expectErrorMessage(call, "IRC31 :: Arrays do not have the same length.");

        ircScore.invoke(owner,"mintBatch", user.getAddress(),_ids,_amounts,_uris);

        for(int i=0;i< _ids.length;i++){
            assertEquals(ircScore.call("balanceOf",user.getAddress(),_ids[i]),_amounts[i]);
        }
    }

    @Test
    void burnBatch(){
        BigInteger[] _ids={BigInteger.valueOf(1),BigInteger.valueOf(2),BigInteger.valueOf(3)};
        String[] _uris={"uri","uri","uri"};
        BigInteger[] wrong_ids={BigInteger.valueOf(1),BigInteger.valueOf(2),BigInteger.valueOf(3),BigInteger.valueOf(4)};
        BigInteger[] _amounts={BigInteger.valueOf(1),BigInteger.valueOf(2),BigInteger.valueOf(3)};
        BigInteger[] burn_amounts={BigInteger.valueOf(1),BigInteger.valueOf(1),BigInteger.valueOf(1)};

        Executable call = () -> ircScore.invoke(user,"burnBatch", user.getAddress(),wrong_ids,_amounts);
        expectErrorMessage(call, "IRC31 :: Arrays do not have the same length.");

        ircScore.invoke(owner,"mintBatch", user.getAddress(),_ids,_amounts,_uris);
        ircScore.invoke(user,"burnBatch", user.getAddress(),_ids,burn_amounts);
        for(int i=0;i< _ids.length;i++){
            assertEquals(ircScore.call("balanceOf",user.getAddress(),_ids[i]),_amounts[i].subtract(burn_amounts[i]));
        }
    }

}
