package io.tokenfactory.score.enums;

import io.tokenfactory.score.exception.TokenFactoryException;
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
                throw new UserRevertedException(TokenFactoryException.notValidContractType());
        }
    }
}
