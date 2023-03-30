package io.tokenfactory.score.enums;

import io.tokenfactory.score.Message;
import score.UserRevertedException;

public enum ContractName {

    MARKETPLACE,
    IRC2,
    IRC3,
    IRC31,
    DAO,
    NFT_AUCTION,
    AIRDROP;

    public static void contractNameValidate(String currency) {
        switch (currency) {
            case "MARKETPLACE":
            case "IRC2":
            case "IRC3":
            case "IRC31":
            case "DAO":
            case "NFT_AUCTION":
            case "AIRDROP":
                break;
            default:
                throw new UserRevertedException(Message.Not.validContract());
        }
    }
}
