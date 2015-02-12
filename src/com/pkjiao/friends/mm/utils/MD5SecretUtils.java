package com.pkjiao.friends.mm.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5SecretUtils {

    /**
     * 生成MD5码
     * 
     * @param data
     *            要转换为md5的字符串
     * @return
     */
    public static String encrypt(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }

            return buf.toString();

        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        }
        return null;
    }

}
