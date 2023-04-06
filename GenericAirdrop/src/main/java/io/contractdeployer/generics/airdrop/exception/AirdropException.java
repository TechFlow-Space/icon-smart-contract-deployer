package io.contractdeployer.generics.airdrop.exception;

import score.Address;

import java.math.BigInteger;

import static io.contractdeployer.generics.airdrop.Airdrop.TAG;

public class AirdropException {

    public static String notOwner() {
        return TAG + " :: Only owner can perform this action";
    }

    public static String notMinter() {
        return TAG + " :: IRC2 airdrop can only be initiated by the minter.";
    }

    public static String lengthMismatch() {
        return TAG + " :: Arrays do not have the same length.";
    }

    public static String approvalRequired(BigInteger id) {
        return TAG + " :: Airdrop contract is not approved for transfer of " + id;
    }

    public static String approvalRequiredForAll(Address address) {
        return TAG + " :: Airdrop contract is not approved for transfer of " + address;
    }

    public static String invalidTimestamp() {
        return TAG + " :: Time stamp should be in microseconds";
    }

    public static String unknown(String msg) {
        return TAG + msg;
    }
}
