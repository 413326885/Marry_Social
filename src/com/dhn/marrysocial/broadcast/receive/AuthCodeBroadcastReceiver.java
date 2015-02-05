package com.dhn.marrysocial.broadcast.receive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

public class AuthCodeBroadcastReceiver extends BroadcastReceiver {

    private String patternCoder = "(?<!\\d)\\d{6}(?!\\d)";

    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private MessageListener mMessageListener;

    @Override
    public void onReceive(Context context, Intent intent) {

        Object[] objs = (Object[]) intent.getExtras().get("pdus");
        for (Object obj : objs) {
            byte[] pdu = (byte[]) obj;
            SmsMessage sms = SmsMessage.createFromPdu(pdu);
            // 短信的内容
            String messageContext= sms.getMessageBody();
            // 短息的手机号。。+86开头？
            String senderPhoneNum = sms.getOriginatingAddress();

            if (!TextUtils.isEmpty(senderPhoneNum) /*&& "13466666666".equalsIgnoreCase(senderPhoneNum)*/) {
                String authCode = patternCode(messageContext);
                if (!TextUtils.isEmpty(authCode)) {
                    mMessageListener.onMsgReceived(authCode);
                }
            }
        }
    }

    // 回调接口
    public interface MessageListener {
        public void onMsgReceived(String message);
    }

    public void setOnReceivedMessageListener(MessageListener messageListener) {
        this.mMessageListener = messageListener;
    }

    /**
     * 匹配短信中间的6个数字（验证码等）
     * 
     * @param patternContent
     * @return
     */
    private String patternCode(String patternContent) {
        if (TextUtils.isEmpty(patternContent)) {
            return null;
        }
        Pattern p = Pattern.compile(patternCoder);
        Matcher matcher = p.matcher(patternContent);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

}
