package io.contractdeployer.generics.irc3.exception;


import static io.contractdeployer.generics.irc3.Contsant.TAG;

public class IRC3Exception {

    public static String notOwner(){
        return TAG+" :: Only owner can perform this action";
    }

    public static String notAdminOrOwner() {
        return TAG+" :: Only admin/owner can perform this action.";
    }

    public static String notApproved() {
        return TAG + " :: Need operator approval for 3rd party transfers.";
    }

    public static String capExceeded() {
        return TAG+" :: Cap Exceeded.";
    }

    public static String negative() {
        return TAG+" :: Value can not be negative";
    }

    public static String zeroOrNegative() {
        return TAG+" :: Value can not be zero or negative";
    }

    public static String priceMismatch() {
        return TAG+" :: Price Mismatch";
    }


}
