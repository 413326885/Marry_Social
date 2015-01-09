package com.dhn.marrysocial.services;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dhn.marrysocial.base.ChatMsgItem;
import com.dhn.marrysocial.base.NoticesItem;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.utils.Utils;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class DownloadChatMsgService extends Service {

    private static final String TAG = "DownloadChatMsgService";

    private static final int POOL_SIZE = 10;
    private static final int TIME_SCHEDULE = 2000;

    private static final int TIME_TO_DOWNLOAD_CHAT_MSG = 100;

    private static final String[] BRIEF_CHAT_PROJECTION = {
        MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_CHAT_ID };
    private static final String[] CONTACTS_PROJECTION = {
        MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_NIKENAME };

    private final Timer mTimer = new Timer();
    private TimerTask mTimerTask;

    private String mUid;
    private String mAuthorName;
    private MarrySocialDBHelper mDBHelper;
    private ExecutorService mExecutorService;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case TIME_TO_DOWNLOAD_CHAT_MSG: {
                mExecutorService.execute(new DownloadChatMsgs());
                break;
            }
            default:
                break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(TIME_TO_DOWNLOAD_CHAT_MSG);
            }
        };
        mTimer.schedule(mTimerTask, TIME_SCHEDULE, TIME_SCHEDULE);

        SharedPreferences prefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT,
                this.MODE_PRIVATE);
        mUid = prefs.getString(CommonDataStructure.UID, "");
        mAuthorName = prefs.getString(CommonDataStructure.AUTHOR_NAME, "");

        mDBHelper = MarrySocialDBHelper.newInstance(getApplicationContext());
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }

    class DownloadChatMsgs implements Runnable {

        @Override
        public void run() {
            Log.e(TAG, "nannan DownloadChatMsgs ");
            while(true) {

                ChatMsgItem chatMsg = Utils.downloadChatMsg(CommonDataStructure.URL_GET_CHAT_TEXT, mUid);
                if (chatMsg == null) {
                    return;
                }
                insertChatMsgToChatsDB(chatMsg);

                String nikename = queryNikenameFromContactsDB(chatMsg.getFromUid());
                if (nikename == null || nikename.length() == 0) {
                    nikename = "某神秘的傻逼";
                }
                Log.e(TAG, "nannan DownloadChatMsgs  insertChatMsgToChatsDB $$$$$$$$$$$$$$$$$");
                if (!isChatIdExistInBriefChatDB(chatMsg.getChatId())) {
                    insertBriefChatMsgToBriefChatDB(chatMsg, nikename);
                } else {
                    updateBriefChatMsgToBriefChatDB(chatMsg, nikename);
                }
                return;
            }
        }

    }

    private void insertChatMsgToChatsDB(ChatMsgItem chat) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_UID, chat.getUid());
        insertValues.put(MarrySocialDBHelper.KEY_CHAT_ID, chat.getChatId());
        insertValues.put(MarrySocialDBHelper.KEY_FROM_UID, chat.getFromUid());
        insertValues.put(MarrySocialDBHelper.KEY_TO_UID, chat.getToUid());
        insertValues.put(MarrySocialDBHelper.KEY_CHAT_CONTENT,
                chat.getChatContent());
        insertValues.put(MarrySocialDBHelper.KEY_MSG_TYPE, chat.getMsgType());
        insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
                chat.getAddedTime());
        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                chat.getCurrentStatus());

        ContentResolver resolver = getApplicationContext().getContentResolver();
        resolver.insert(CommonDataStructure.CHATURL, insertValues);
    }

    public boolean isChatIdExistInBriefChatDB(String chatId) {
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_CHAT_ID + " = "
                    + '"' + chatId + '"';
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_BRIEF_CHAT_TABLE,
                    BRIEF_CHAT_PROJECTION, whereclause, null, null, null, null,
                    null);
            if (cursor == null || cursor.getCount() == 0) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return true;
    }

    private void insertBriefChatMsgToBriefChatDB(ChatMsgItem chat, String nikename) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_UID, chat.getUid());
        insertValues.put(MarrySocialDBHelper.KEY_CHAT_ID, chat.getChatId());
        insertValues.put(MarrySocialDBHelper.KEY_NIKENAME, nikename);
        insertValues.put(MarrySocialDBHelper.KEY_CHAT_CONTENT,
                chat.getChatContent());
        insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
                chat.getAddedTime());

        ContentResolver resolver = getApplicationContext().getContentResolver();
        resolver.insert(CommonDataStructure.BRIEFCHATURL, insertValues);
    }

    private void updateBriefChatMsgToBriefChatDB(ChatMsgItem chat, String nikename) {
        ContentValues updateValues = new ContentValues();
        updateValues.put(MarrySocialDBHelper.KEY_UID, chat.getUid());
        updateValues.put(MarrySocialDBHelper.KEY_CHAT_ID, chat.getChatId());
        updateValues.put(MarrySocialDBHelper.KEY_NIKENAME, nikename);
        updateValues.put(MarrySocialDBHelper.KEY_CHAT_CONTENT,
                chat.getChatContent());
        updateValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
                chat.getAddedTime());

        String whereclause = MarrySocialDBHelper.KEY_CHAT_ID + " = "
                + '"' + chat.getChatId() + '"';
        ContentResolver resolver = getApplicationContext().getContentResolver();
        resolver.update(CommonDataStructure.BRIEFCHATURL, updateValues, whereclause, null);
    }

    private String queryNikenameFromContactsDB(String uId) {
        String nikename = null;
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_UID + " = "
                    + uId;
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_CONTACTS_TABLE,
                    CONTACTS_PROJECTION, whereclause, null, null, null, null,
                    null);
            if (cursor == null || cursor.getCount() == 0) {
                return nikename;
            }
            cursor.moveToNext();
            nikename = cursor.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return nikename;
    }
}