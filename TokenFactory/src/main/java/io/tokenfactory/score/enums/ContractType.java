package io.tokenfactory.score.enums;

import io.tokenfactory.score.Message;
import score.UserRevertedException;

public enum ContractType {

    UTILITY,
    FUNGIBLE_TOKEN,
    NON_FUNGIBLE_TOKEN;

    public static void contractTypeValidate(String currency) {
        switch (currency) {
            case "UTILITY":
            case "FUNGIBLE_TOKEN":
            case "NON_FUNGIBLE_TOKEN":
                break;
            default:
                throw new UserRevertedException(Message.Not.validContractType());
        }
    }
}
