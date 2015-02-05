package com.dhn.marrysocial.broadcast.receive;

import com.dhn.marrysocial.common.CommonDataStructure;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NewTipsBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "NewTipsBroadcastReceiver";

    private BroadcastListener mBroadcastListener;

    public void setBroadcastListener(BroadcastListener listener) {
        mBroadcastListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        int cmdID = intent.getIntExtra(CommonDataStructure.KEY_BROADCAST_CMDID,
                -1);

        if (mBroadcastListener == null) {
            return;
        }

        switch (cmdID) {
        case CommonDataStructure.KEY_NEW_CHAT_MSG: {
            mBroadcastListener.onReceivedNewChatMsgs();
            break;
        }
        case CommonDataStructure.KEY_NEW_COMMENTS: {
            mBroadcastListener.onReceivedNewComments();
            break;
        }
        case CommonDataStructure.KEY_NEW_CONTACTS: {
            mBroadcastListener.onReceivedNewContacts();
            break;
        }
        }
    }

    public interface BroadcastListener {
        public void onReceivedNewComments();

        public void onReceivedNewChatMsgs();

        public void onReceivedNewContacts();
    }
}
