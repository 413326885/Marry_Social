package com.dhn.marrysocial.services;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dhn.marrysocial.base.CommentsItem;
import com.dhn.marrysocial.base.NoticesItem;
import com.dhn.marrysocial.base.ReplysItem;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.utils.Utils;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class DownloadNoticesService extends Service {

    private static final String TAG = "DownloadNoticesService";

    private static final int TIME_TO_DOWNLOAD_NOTICE = 100;
    private static final int POOL_SIZE = 10;

    private static final String[] COMMENTS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_BUCKET_ID,
            MarrySocialDBHelper.KEY_CONTENTS };

    private static final String[] REPLYS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_COMMENT_ID,
            MarrySocialDBHelper.KEY_REPLY_ID };

    private final Timer mTimer = new Timer();
    private TimerTask mTimerTask;

    private MarrySocialDBHelper mDBHelper;
    private ExecutorService mExecutorService;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == TIME_TO_DOWNLOAD_NOTICE) {
                Log.e(TAG, "nannan time to download notices");
                mExecutorService.execute(new DownloadNotices());
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(TIME_TO_DOWNLOAD_NOTICE);
            }
        };
        mTimer.schedule(mTimerTask, 2000, 2000);

        mDBHelper = MarrySocialDBHelper.newInstance(getApplicationContext());
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }

    class DownloadNotices implements Runnable {

        @Override
        public void run() {
            ArrayList<NoticesItem> noticeItems = Utils.downloadNoticesList(
                    CommonDataStructure.URL_TOPIC_COMMENT_WITH_REPLY_LIST,
                    null, null);
            if (noticeItems == null || noticeItems.size() == 0) {
                return;
            }
            for (NoticesItem notice : noticeItems) {

            }
        }

    }

    private void insertReplysToReplyDB(NoticesItem notice) {
//        ContentValues insertValues = new ContentValues();
//        insertValues.put(MarrySocialDBHelper.KEY_COMMENT_ID,
//                reply.getCommentId());
//        insertValues.put(MarrySocialDBHelper.KEY_UID, mUid);
//        insertValues.put(MarrySocialDBHelper.KEY_AUTHOR_FULLNAME, mAuthorName);
//        insertValues.put(MarrySocialDBHelper.KEY_REPLY_CONTENTS,
//                reply.getReplyContents());
//        insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
//                Long.toString(System.currentTimeMillis()));
//        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
//                MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD);
//
//        ContentResolver resolver = getApplicationContext().getContentResolver();
//        resolver.insert(CommonDataStructure.REPLYURL, insertValues);
    }

    private void insertBravoStatusToBravosDB(NoticesItem notice) {
//        ContentValues insertValues = new ContentValues();
//        insertValues.put(MarrySocialDBHelper.KEY_COMMENT_ID,
//                comment.getCommentId());
//        insertValues.put(MarrySocialDBHelper.KEY_UID, mUid);
//        insertValues.put(MarrySocialDBHelper.KEY_AUTHOR_FULLNAME, mAuthorName);
//        insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
//                Long.toString(System.currentTimeMillis()));
//        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
//                MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD);
//
//        ContentResolver resolver = getApplicationContext().getContentResolver();
//        resolver.insert(CommonDataStructure.BRAVOURL, insertValues);
    }

    public boolean isBravoIdExistInBravosDB(String bravoId) {
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_BRAVO_ID + " = "
                    + bravoId;
            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_COMMENTS_TABLE,
                    COMMENTS_PROJECTION, whereclause, null, null, null, null,
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

    public boolean isReplyIdExistInReplysDB(String replyId) {
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_REPLY_ID + " = "
                    + replyId;
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_REPLYS_TABLE,
                    REPLYS_PROJECTION, whereclause, null, null, null, null,
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
}
