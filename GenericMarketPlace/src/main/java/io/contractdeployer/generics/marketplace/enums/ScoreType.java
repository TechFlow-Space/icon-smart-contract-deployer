package io.contractdeployer.generics.marketplace.enums;

import io.contractdeployer.generics.marketplace.MarketPlaceException;
import score.UserRevertedException;

public enum ScoreType {
    ID, COUNT, ID_COUNT;

    public static void validate(String type){
        switch (type){
            case "ID":
            case "COUNT":
            case "ID_COUNT":
                break;
            default:
                throw new UserRevertedException(MarketPlaceException.Invalid.currency());
        }
    }
}
