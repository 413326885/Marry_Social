package com.pkjiao.friends.mm.pingyin;

import java.util.Comparator;

import com.pkjiao.friends.mm.base.ContactsInfo;

import net.sourceforge.pinyin4j.PinyinHelper;

public class LanguageComparator implements Comparator<ContactsInfo> {

    public int compare(ContactsInfo ostr1, ContactsInfo ostr2) {

        for (int i = 0; i < ostr1.getNickName().length() && i < ostr2.getNickName().length(); i++) {

            int codePoint1 = ostr1.getNickName().charAt(i);
            int codePoint2 = ostr2.getNickName().charAt(i);
            if (Character.isSupplementaryCodePoint(codePoint1)
                    || Character.isSupplementaryCodePoint(codePoint2)) {
                i++;
            }
            if (codePoint1 != codePoint2) {
                if (Character.isSupplementaryCodePoint(codePoint1)
                        || Character.isSupplementaryCodePoint(codePoint2)) {
                    return codePoint1 - codePoint2;
                }
                String pinyin1 = pinyin((char) codePoint1);
                String pinyin2 = pinyin((char) codePoint2);

                if (pinyin1 != null && pinyin2 != null) {
                    if (!pinyin1.equals(pinyin2)) {
                        return pinyin1.compareTo(pinyin2);
                    }
                } else {
                    return codePoint1 - codePoint2;
                }
            }
        }
        return ostr1.getNickName().length() - ostr2.getNickName().length();
    }

    private String pinyin(char c) {
        String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c);
        if (pinyins == null) {
            return null;
        }
        return pinyins[0];
    }

}
