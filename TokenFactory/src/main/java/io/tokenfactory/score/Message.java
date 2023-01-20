package io.tokenfactory.score;

import static io.tokenfactory.score.Constant.TAG;

public class Message {

    public static final String duplicateContract(){
        return TAG + " :: " + "DuplicateContract";
    }

    public static final String zeroAddress(){
        return TAG + " :: " + "ZeroAddress";
    }

    public static final String paymentMismatch(){
        return TAG + " :: " + "PaymentMismatch";
    }

    static class Not {
        public static final String owner(){
            return TAG + " :: " + "NotOwner";
        }

        public static final String admin(){
            return TAG + " :: " + "NotOwner";
        }

        public static final String validContract(){
            return TAG + " :: " + "NotAValidContract";
        }

        public static final String deployed(){
            return TAG + " :: " + "ContractNotDeployed";
        }

    }
}
