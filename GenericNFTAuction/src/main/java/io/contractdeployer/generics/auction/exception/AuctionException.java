package io.contractdeployer.generics.auction.exception;

import java.math.BigInteger;

import static io.contractdeployer.generics.auction.Constant.TAG;

public class AuctionException {

    public static String onlyAuctionCreator() {
        return TAG + " :: Only auction creator can do this action";

    }

    public static String auctionEnded(BigInteger id) {
        return TAG + " :: Auction Ended for auction id " + id;

    }

    public static String invalidId() {
        return TAG + " :: Invalid auction id";

    }

    public static String invalidBid(String msg) {
        return TAG + " :: " + msg;

    }

    public static String creator() {
        return TAG + " :: Auction Creator Not Allowed To Bid";

    }

    public static String unavailable() {
        return TAG + " :: No Auction Available";

    }

    public static String invalidEndTime() {
        return TAG + " :: Invalid auction end time";
    }
}
