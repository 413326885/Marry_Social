package com.pkjiao.friends.mm.pingyin;

import com.pkjiao.friends.mm.base.ContactsInfo;

import net.sourceforge.pinyin4j.PinyinHelper;

public class AssortPinyinList {

    private HashList<String, ContactsInfo> hashList = new HashList<String, ContactsInfo>(
            new KeySort<String, ContactsInfo>() {
                public String getKey(ContactsInfo value) {
                    return getFirstChar(value.getNickName());
                }
            });

    public String getFirstChar(String value) {
        char firstChar = value.charAt(0);
        String first = null;
        String[] print = PinyinHelper.toHanyuPinyinStringArray(firstChar);

        if (print == null) {

            if ((firstChar >= 97 && firstChar <= 122)) {
                firstChar -= 32;
            }
            if (firstChar >= 65 && firstChar <= 90) {
                first = String.valueOf((char) firstChar);
            } else {
                first = "#";
            }
        } else {
            first = String.valueOf((char) (print[0].charAt(0) - 32));
        }
        if (first == null) {
            first = "?";
        }
        return first;
    }

    public HashList<String, ContactsInfo> getHashList() {
        return hashList;
    }

}
