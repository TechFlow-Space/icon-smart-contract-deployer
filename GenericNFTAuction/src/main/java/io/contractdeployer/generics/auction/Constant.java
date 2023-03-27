package io.contractdeployer.generics.auction;

import score.Address;

public class Constant {

    static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);

    public static final String TAG = "NFT Auction";
    public static final String HIGHEST_BID = "highest_bid";
    public static final String HIGHEST_BIDDER = "highest_bidder";
    public static final String ENDED = "ended";
}
