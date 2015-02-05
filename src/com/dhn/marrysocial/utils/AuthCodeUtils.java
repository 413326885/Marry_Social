package com.dhn.marrysocial.utils;

import java.util.Random;

public class AuthCodeUtils {

    private static Random random = new Random();
    private static String ssource = "0123456789";
    private static char[] src = ssource.toCharArray();

    public static String randAuthCode(int length) {
        char[] buf = new char[length];
        int rnd;
        for (int i = 0; i < length; i++) {
            rnd = Math.abs(random.nextInt()) % src.length;

            buf[i] = src[rnd];
        }
        return new String(buf);
    }

}
