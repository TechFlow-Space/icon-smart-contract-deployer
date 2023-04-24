package io.contractdeployer.generics.airdrop.irc2.exception;

import static io.contractdeployer.generics.airdrop.irc2.AirdropIRC2.TAG;

public class AirdropIRC2Exception {

    public static String notOwner() {
        return TAG + " :: Only owner can perform this action.";
    }

    public static String notMinter() {
        return TAG + " :: IRC2 airdrop can only be initiated by the minter.";
    }

    public static String lengthMismatch() {
        return TAG + " :: Arrays do not have the same length.";
    }
    public static String invalidTimestamp() {
        return TAG + " :: Time stamp should be in microseconds.";
    }

    public static String unknown(String msg) {
        return TAG + " :: "+msg;
    }
}
