package io.contractdeployer.generics.marketplace;

import score.Address;

import java.math.BigInteger;

import static io.contractdeployer.generics.marketplace.Constant.TAG;

public class MarketPlaceException {
    public static String priceMisMatch(BigInteger expected, BigInteger provided) {
        return TAG+" :: Price of NFT:" + expected + ". Amount sent: " + provided;
    }

    public static String scoreAlreadyExist() {
        return  TAG + " :: score found";
    }
    public static String own() {
        return TAG+" :: Cannot buy own NFT.";
    }
   

    public static class Not{
        public static String found(String type) {
            return TAG+" :: "+type+" not found in the marketplace";
        }
        public static String forSale(){
            return TAG+" :: NFT not for sale.";
        }
        public static String enough() {
            return TAG+" :: Not enough nft found for sale";
        }
        public static String enoughBalance() {
            return TAG+" :: Not enough balance";
        }
        public static String historyAvailable(int offset) {
            return TAG+" :: History not available for offset: " + offset + ".";
        }
        public static String active() {
            return TAG+" :: Sale is not active";
        }
        public static String currentOwner(){
            return TAG+" :: Listing not made by current owner.";
        }
        public static String enabled(String type) {
            return TAG+" :: "+type+" must be enabled.";
        }
        public static String approved() {
            return TAG+" :: Owner has not approved marketplace.";
        }

        public static String nftOwner() {
            return TAG+" :: NFT owner required.";
        }
        public static String admin(Address admin){
            return TAG+ " :: Caller is not admin: "+admin;
        }
        public static String owner() {
            return TAG+" :: Only owner can perform this action.";
        }

        public static String feeInRange() {
            return TAG+" :: Fee rate should be between 0 and 100*10^18.";
        }

        public static String supportedScore(Address score){
            return TAG+" :: Not a supported score: "+score;
        }
    }

    public static String maxTenAllowed() {
        return TAG+" :: Max 10 items allowed in a single call.";
    }

    public static String greaterThanZero(String type) {
        return TAG+" :: "+type+" must be greater than zero.";
    }

    public static class Invalid {
        public static String score() {
            return TAG+" :: Invalid score.";
        }
        public static String currency(){
            return TAG+" :: Invalid currency";
        }
    }
}
