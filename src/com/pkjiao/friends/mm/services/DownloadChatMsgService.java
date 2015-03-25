package com.pkjiao.friends.mm.services;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.pkjiao.friends.mm.R;
import com.pkjiao.friends.mm.adapter.ChatMsgViewAdapter.IMsgViewType;
import com.pkjiao.friends.mm.base.ChatMsgItem;
import com.pkjiao.friends.mm.base.NoticesItem;
import com.pkjiao.friends.mm.base.NotificationManagerControl;
import com.pkjiao.friends.mm.common.CommonDataStructure;
import com.pkjiao.friends.mm.database.MarrySocialDBHelper;
import com.pkjiao.friends.mm.utils.Utils;

public class DownloadChatMsgService extends Service {

    private static final String TAG = "DownloadChatMsgService";

    private static final int POOL_SIZE = 10;
    private static final int TIME_SCHEDULE = 5000;

    private static final int TIME_TO_DOWNLOAD_CHAT_MSG = 100;

    private static final String[] COUNT_PROJECTION = { "count(*)" };
    private static final String[] BRIEF_CHAT_PROJECTION = {
            MarrySocialDBHelper.KEY_TO_UID, MarrySocialDBHelper.KEY_CHAT_ID };
    private static final String[] CONTACTS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_NICKNAME };

    private static final String[] HEAD_PICS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_HEAD_PIC_BITMAP,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_THUMB_PATH };

    private final Timer mTimer = new Timer();
    private TimerTask mTimerTask;

    private String mUid;
    private String mAuthorName;
    private SharedPreferences mPrefs;
    private Context mContext;
    private MarrySocialDBHelper mDBHelper;
    private ExecutorService mExecutorService;
    private NotificationManagerControl mNotificationManager;

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

                int loginStatus = mPrefs.getInt(
                        CommonDataStructure.LOGINSTATUS,
                        CommonDataStructure.LOGIN_STATUS_NO_USER);
                if (loginStatus != CommonDataStructure.LOGIN_STATUS_LOGIN) {
                    return;
                }

                mHandler.sendEmptyMessage(TIME_TO_DOWNLOAD_CHAT_MSG);
            }
        };
        mTimer.schedule(mTimerTask, TIME_SCHEDULE, TIME_SCHEDULE);

        mPrefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, this.MODE_PRIVATE);
        mUid = mPrefs.getString(CommonDataStructure.UID, "");
        mAuthorName = mPrefs.getString(CommonDataStructure.AUTHOR_NAME, "");

        mContext = getApplicationContext();
        mDBHelper = MarrySocialDBHelper.newInstance(mContext);
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);
        mNotificationManager = NotificationManagerControl.newInstance(mContext);
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

            while (true) {

                ChatMsgItem chatMsg = Utils.downloadChatMsg(
                        CommonDataStructure.URL_GET_CHAT_TEXT, mUid);
                if (chatMsg == null) {
                    return;
                }
                insertChatMsgToChatsDB(chatMsg);

                String chatUserUid;
                if (chatMsg.getMsgType() == IMsgViewType.IMVT_COM_MSG) {
                    chatUserUid = chatMsg.getFromUid();
                } else {
                    chatUserUid = chatMsg.getToUid();
                }
                String nikename = queryNikenameFromContactsDB(chatUserUid);
                if (nikename == null || nikename.length() == 0) {
                    nikename = "新年快乐";
                }
                if (!isChatIdExistInBriefChatDB(chatMsg.getChatId())) {
                    insertBriefChatMsgToBriefChatDB(chatMsg, nikename,
                            chatUserUid);
                } else {
                    updateBriefChatMsgToBriefChatDB(chatMsg, nikename,
                            chatUserUid);
                }

                if (!Utils.isAppRunningForeground(mContext)) {
                    int msgCount = getNewMsgCount(chatMsg.getChatId());
                    Bitmap header = loadUserHeadPicFromDB(chatUserUid);
                    Bitmap cropHeader;
                    if (header == null) {

                        Bitmap icon_launcher = BitmapFactory
                                .decodeResource(mContext.getResources(),
                                        R.drawable.ic_launcher);
                        cropHeader = Utils.resizeAndCropCenter(icon_launcher,
                                Utils.mTinyCropCenterThumbPhotoWidth, true);

                        mNotificationManager.showChatMsgNotification(
                                cropHeader, nikename, chatMsg.getChatContent(),
                                msgCount, chatMsg.getChatId());
                    } else {
                        mNotificationManager.showChatMsgNotification(header,
                                nikename, chatMsg.getChatContent(), msgCount,
                                chatMsg.getChatId());
                    }

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
        insertValues.put(MarrySocialDBHelper.KEY_READ_STATUS,
                MarrySocialDBHelper.MSG_NOT_READ);
        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                chat.getCurrentStatus());

        try {
            ContentResolver resolver = getApplicationContext()
                    .getContentResolver();
            resolver.insert(CommonDataStructure.CHATURL, insertValues);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

    public boolean isChatIdExistInBriefChatDB(String chatId) {
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_CHAT_ID + " = " + '"'
                    + chatId + '"';
            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_BRIEF_CHAT_TABLE,
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

    private void insertBriefChatMsgToBriefChatDB(ChatMsgItem chat,
            String nikename, String chatUserUid) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_TO_UID, chatUserUid);
        insertValues.put(MarrySocialDBHelper.KEY_CHAT_ID, chat.getChatId());
        insertValues.put(MarrySocialDBHelper.KEY_NICKNAME, nikename);
        insertValues.put(MarrySocialDBHelper.KEY_CHAT_CONTENT,
                chat.getChatContent());
        insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
                chat.getAddedTime());
        insertValues.put(MarrySocialDBHelper.KEY_HAS_NEW_MSG,
                MarrySocialDBHelper.HAS_NEW_MSG);

        try {
            ContentResolver resolver = getApplicationContext()
                    .getContentResolver();
            resolver.insert(CommonDataStructure.BRIEFCHATURL, insertValues);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

    private void updateBriefChatMsgToBriefChatDB(ChatMsgItem chat,
            String nikename, String chatUserUid) {
        ContentValues updateValues = new ContentValues();
        updateValues.put(MarrySocialDBHelper.KEY_TO_UID, chatUserUid);
        updateValues.put(MarrySocialDBHelper.KEY_CHAT_ID, chat.getChatId());
        updateValues.put(MarrySocialDBHelper.KEY_NICKNAME, nikename);
        updateValues.put(MarrySocialDBHelper.KEY_CHAT_CONTENT,
                chat.getChatContent());
        updateValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
                chat.getAddedTime());
        updateValues.put(MarrySocialDBHelper.KEY_HAS_NEW_MSG,
                MarrySocialDBHelper.HAS_NEW_MSG);

        String whereclause = MarrySocialDBHelper.KEY_CHAT_ID + " = " + '"'
                + chat.getChatId() + '"';

        try {
            ContentResolver resolver = getApplicationContext()
                    .getContentResolver();
            resolver.update(CommonDataStructure.BRIEFCHATURL, updateValues,
                    whereclause, null);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

    private String queryNikenameFromContactsDB(String uId) {
        String nikename = null;
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_UID + " = " + uId;
            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_CONTACTS_TABLE,
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

    private Bitmap loadUserHeadPicFromDB(String uid) {

        Bitmap headpic = null;

        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + uid;
        Cursor cursor = mDBHelper
                .query(MarrySocialDBHelper.DATABASE_HEAD_PICS_TABLE,
                        HEAD_PICS_PROJECTION, whereClause, null, null, null,
                        null, null);

        if (cursor == null) {
            Log.w(TAG, "nannan query fail!");
            return null;
        }

        try {
            cursor.moveToNext();
            byte[] in = cursor.getBlob(1);
            headpic = BitmapFactory.decodeByteArray(in, 0, in.length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return headpic;
    }

    private int getNewMsgCount(String chatId) {
        int count = 0;

        String whereClause = MarrySocialDBHelper.KEY_CHAT_ID + " = " + '"'
                + chatId + '"' + " AND " + MarrySocialDBHelper.KEY_READ_STATUS
                + " = " + MarrySocialDBHelper.MSG_NOT_READ;
        Cursor cursor = mDBHelper.query(
                MarrySocialDBHelper.DATABASE_CHATS_TABLE, COUNT_PROJECTION,
                whereClause, null, null, null, null, null);

        if (cursor == null) {
            Log.w(TAG, "nannan query fail!");
            return count;
        }

        try {
            cursor.moveToNext();
            count = cursor.getInt(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }
}
