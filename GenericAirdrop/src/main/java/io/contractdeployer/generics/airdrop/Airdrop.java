package io.contractdeployer.generics.airdrop;

import score.Address;
import score.Context;
import score.UserRevertedException;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;

import java.math.BigInteger;

public class Airdrop {

    @External
    public void airdrop(String key, Address contractAddress,Address from,Address to,
                        @Optional BigInteger value, @Optional BigInteger tokenId) {

        var tokenProxy = new TokenProxy(contractAddress, key);
        switch(key){
            case TokenProxy.IRC2:
            case TokenProxy.IRC3:
            case TokenProxy.IRC31:
                tokenProxy.transfer(from,to,value,tokenId);
                AirdropToken(Context.getAddress(),to,key,value,tokenId);
                break;
            default:
                throw new UserRevertedException("Invalid Key");
        }
    }

    @External
    public void airdropBatch(String key, Address contractAddress,Address from,Address to,
                        @Optional BigInteger[] values, @Optional BigInteger[] tokenIds){

        var tokenProxy = new TokenProxy(contractAddress, key);

        if (TokenProxy.IRC31.equals(key)) {
            tokenProxy.transferBatch(from, to, values, tokenIds);
        } else {
            throw new UserRevertedException("Invalid Key");
        }
    }

    @External
    public void approve(String key,Address contractAddress,@Optional BigInteger tokenId){

        switch(key){
            case TokenProxy.IRC3:
                Context.call(contractAddress,"approve",Context.getAddress(),tokenId);
                break;
            case TokenProxy.IRC31:
                Context.call(contractAddress,"setApprovalForAll",Context.getAddress(),true);
                break;
            default:
                throw new UserRevertedException("Invalid Key");
        }
    }

    @EventLog(indexed = 3)
    public void AirdropToken(Address from, Address to,String type,@Optional BigInteger value,@Optional BigInteger tokenId) {}

}
