package io.tokenfactory.score.exception;

import static io.tokenfactory.score.Constant.TAG;

public class TokenFactoryException {

    public static String duplicateContract(){
        return TAG + " :: " + "DuplicateContract";
    }

    public static String zeroAddress(){
        return TAG + " :: " + "ZeroAddress";
    }

    public static String notOwner(){
        return TAG + " :: " + "NotOwner";
    }

    public static String notAdmin(){
        return TAG + " :: " + "NotAdmin";
    }

    public static  String notValidContract(){
        return TAG + " :: " + "NotAValidContract";
    }
    public static String notValidContractType(){
        return TAG + " :: " + "NotAValidContractType";
    }

    public static String notDeployed(){
        return TAG + " :: " + "ContractNotDeployed";
    }

    public static String paymentMismatch(){
        return TAG + " :: " + "PaymentMismatch";
    }

}
