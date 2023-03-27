package io.contractdeployer.generics.irc2;

import static io.contractdeployer.generics.irc2.Constant.*;

public class Message {

    public static class Found{

        public static String zeroAddr(String type) {
            return TAG + " :: "+type +" cannot be Zero Address.";

        }
    }

    public static class Not{

        public static String self() {
            return TAG+" :: "+" Cannot transfer to self.";
        }

        public static String enoughBalance() {
            return TAG+" :: Not enough balance";
        }

        public static String minter() {
            return TAG+" :: Minter required.";
        }

        public static String owner() {
            return TAG+" :: Only owner can perform this action.";
        }

    }

    public static String greaterThanZero(String type) {
        return TAG+" :: "+type+" must be greater than zero.";
    }
    public static String empty(String type) {
        return TAG+" :: "+type+" is null or empty.";
    }

}
