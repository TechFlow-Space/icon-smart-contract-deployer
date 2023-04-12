package io.contractdeployer.generics.airdrop.irc3.exception;

import java.math.BigInteger;

import static io.contractdeployer.generics.airdrop.irc3.AirdropIRC3.TAG;

public class AirdropIRC3Exception {

    public static String notOwner() {
        return TAG + " :: Only owner can perform this action.";
    }

    public static String invalidPayments() {
        return TAG + " :: Invalid no of Payments";
    }
    public static String lengthMismatch() {
        return TAG + " :: Arrays do not have the same length.";
    }

    public static String approvalRequired(BigInteger id) {
        return TAG + " :: Airdrop contract is not approved for transfer of tokenID " + id;
    }

    public static String invalidTimestamp() {
        return TAG + " :: Time stamp should be in microseconds.";
    }

    public static String unknown(String msg) {
        return TAG + " :: " + msg;
    }
}
