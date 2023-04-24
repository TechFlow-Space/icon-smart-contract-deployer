package io.contractdeployer.generics.airdrop.irc31.exception;

import score.Address;

import static io.contractdeployer.generics.airdrop.irc31.AirdropIRC31.TAG;

public class AirdropIRC31Exception {
    public static String invalidPayments() {
        return TAG + " :: Invalid no of Payments";
    }

    public static String notOwner() {
        return TAG + " :: Only owner can perform this action";
    }

    public static String lengthMismatch() {
        return TAG + " :: Arrays do not have the same length.";
    }
    public static String invalidTimestamp() {
        return TAG + " :: Time stamp should be in microseconds";
    }

    public static String unknown(String msg) {
        return TAG + msg;
    }

    public static String approvalRequiredForAll(Address address) {
        return TAG + " :: Airdrop contract is not approved for transfer of " + address;
    }
}
