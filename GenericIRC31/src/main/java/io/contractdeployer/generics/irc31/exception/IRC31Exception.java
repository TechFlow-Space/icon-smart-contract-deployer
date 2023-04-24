package io.contractdeployer.generics.irc31.exception;

import static io.contractdeployer.generics.irc31.Constant.TAG;

public class IRC31Exception {
    public static String zeroAddr() {
        return TAG + " :: Address cannot be Zero Address.";

    }

    public static String insufficientBalance() {
        return TAG+" :: Not enough balance";
    }

    public static String notOwner() {
        return TAG+" :: Only owner can perform this action.";
    }

    public static String notAdmin() {
        return TAG+" :: Only admin/owner can perform this action.";
    }

    public static String pairMismatch() {
        return TAG + " :: Arrays do not have the same length.";
    }


    public static String notApproved() {
        return TAG + " :: Need operator approval for 3rd party transfers.";
    }

    public static String capExceeded() {
        return TAG+" :: Cap Exceeded.";
    }

    public static String nftCountPerTxRange() {
        return TAG+" :: NFT count per transaction exceeded.";
    }


    public static String lessThanZero() {
        return TAG+" :: Value must be greater than zero.";
    }
    public static String empty() {
        return TAG+" :: Can not set null or empty.";
    }

}
