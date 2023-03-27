package io.contractdeployer.generics.marketplace.util;

import static score.Context.revert;

public class StringUtil {

    public static String[] stringSplit(String string, Character regex){
        if(string.length()==0){
            return new String[0];
        }
        String[] ret = new String[countArrEle(string, regex)];
        int arrCount = 0;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0; i<string.length(); i++){
            if(string.charAt(i)==regex){
                ret[arrCount] = stringBuilder.toString();
                stringBuilder = new StringBuilder();
                arrCount++;
            }else{
                stringBuilder.append(string.charAt(i));
            }

        }
        ret[arrCount] = stringBuilder.toString();
        return ret;
    }

    public static int countArrEle(String string, Character regex){
        int count = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == regex) {
                count++;
            }
        }
        return count+1;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            hexStringBuffer.append(byteToHex(bytes[i]));
        }
        return hexStringBuffer.toString();
    }

    public static String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

}
