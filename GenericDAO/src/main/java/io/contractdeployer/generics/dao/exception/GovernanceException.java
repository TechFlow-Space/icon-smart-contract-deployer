package io.contractdeployer.generics.dao.exception;

import java.math.BigInteger;

import static io.contractdeployer.generics.dao.GovImpl.TAG;

public class GovernanceException {

    public static String invalidToken(){
        return TAG + " :: Invalid token type";
    }

    public static String greaterThanZero(){
        return TAG + " :: Minimum threshold must be positive";
    }


    public static String invalidEndTime(){
        return TAG + " :: Invalid end time";
    }
    public static String invalidTime(){
        return TAG + " :: Time should be in microseconds";
    }
    public static String onlyEOA(){
        return TAG + " :: Only EOA can submit proposal";
    }

    public static String thresholdNotMet(BigInteger threshold){
        return TAG + " :: Insufficient token balance. Should meet minimum threshold " + threshold;
    }
    public static String onlyCreator(){
        return TAG + " :: Only proposal creator can perform this";
    }public static String invalidId(BigInteger id){
        return TAG + " :: Invalid proposal id " + id;
    }
    public static String notActive(){
        return TAG + " :: Proposal not active";
    }

    public static String onlyOwner(){
        return TAG + " :: Only owner perform this action";
    }public static String notTokenHolder(){
        return TAG + " :: Only token holder can vote";
    }public static String tokenSet(){
        return TAG + " :: Governance token has already been set";
    }
    public static String invalidVote(String msg){
        return TAG + " :: " + msg;
    }
    public static String unknown(String msg){
        return TAG + " :: " + msg;
    }





}
