package io.contractdeployer.generics.airdrop;

import score.Address;
import score.Context;
import score.annotation.Optional;

import java.math.BigInteger;

public class TokenProxy {

    public static final String IRC2 = "irc-2";
    public static final String IRC3 = "irc-3";
    public static final String IRC31 = "irc-31";

    private final Address address;
    private final String type;

    public TokenProxy(Address address, String type) {
        Context.require(address != null, "TokenAddressNotSet");
        this.address = address;
        this.type = type;
    }

    public void transfer(Address from, Address to,@Optional BigInteger value, @Optional BigInteger tokenId) {

//        Address address=Context.getAddress();
//        Context.println("Token Proxy Address:"+address.toString());
        if (IRC2.equals(type)) {
            Address minter = Context.call(Address.class,address,"getMinter");
            Address caller=Context.getOrigin();
            Context.require(caller.equals(minter),"IRC2 airdrop can only be initiated by the minter.");

            Context.call(address, "mint", to,value);
        } else if(IRC3.equals(type)) {
            Address approved=Context.call(Address.class,address,"getApproved",tokenId);
            Context.require(approved.equals(Context.getAddress()),"Airdrop Contract not approved for transfers.");

            Context.call(address, "transferFrom", from, to, tokenId);
        }
          else if(IRC31.equals(type)){
            Boolean approved=Context.call(Boolean.class,address,"isApprovedForAll",from,Context.getAddress());
            Context.require(approved,"Airdrop Contract not approved for transfers.");

            Context.call(address, "transferFrom", from,to,tokenId,value);
        }
    }

    public void transferBatch(Address from, Address to,@Optional BigInteger[] values, @Optional BigInteger[] tokenIds) {
        if(IRC31.equals(type)){
            Boolean approved=Context.call(Boolean.class,address,"isApprovedForAll",from,Context.getAddress());
            Context.require(approved,"Airdrop Contract not approved for transfers.");

            Context.call(address, "transferFromBatch", from,to,tokenIds,values);
        }
    }


}
