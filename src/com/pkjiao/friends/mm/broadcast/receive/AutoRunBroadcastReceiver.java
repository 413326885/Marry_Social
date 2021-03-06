package com.pkjiao.friends.mm.broadcast.receive;

import com.pkjiao.friends.mm.common.CommonDataStructure;
import com.pkjiao.friends.mm.services.DownloadChatMsgService;
import com.pkjiao.friends.mm.services.DownloadNoticesService;
import com.pkjiao.friends.mm.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class AutoRunBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "AutoRunBroadcastReceiver";

    private Context mContext;
    private SharedPreferences mPrefs;

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;
        mPrefs = mContext.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT,
                mContext.MODE_PRIVATE);

        int loginStatus = mPrefs.getInt(CommonDataStructure.LOGINSTATUS,
                CommonDataStructure.LOGIN_STATUS_NO_USER);
        if (loginStatus != CommonDataStructure.LOGIN_STATUS_LOGIN) {
            return;
        }

        if (!Utils.isActiveNetWorkAvailable(mContext)) {
            return;
        }

        startDownloadNoticesServices();
        startDownloadChatMsgServices();

    }

    private void startDownloadNoticesServices() {
        Intent service = new Intent(mContext, DownloadNoticesService.class);
        mContext.startService(service);
    }

    private void startDownloadChatMsgServices() {
        Intent service = new Intent(mContext, DownloadChatMsgService.class);
        mContext.startService(service);
    }
}
