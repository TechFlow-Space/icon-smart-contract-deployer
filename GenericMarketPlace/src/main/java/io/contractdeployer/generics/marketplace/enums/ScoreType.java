package io.contractdeployer.generics.marketplace.enums;

import io.contractdeployer.generics.marketplace.Message;
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
                throw new UserRevertedException(Message.Invalid.currency());
        }
    }
}
