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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class DownloadNoticesService extends Service {

    private static final String TAG = "DownloadNoticesService";

    private static final int TIME_TO_DOWNLOAD_INDIRECT_COMMENTS = 101;
    private static final int TIME_TO_DOWNLOAD_INDIRECT_BRAVOS = 102;
    private static final int TIME_TO_DOWNLOAD_INDIRECT_REPLYS = 103;
    private static final int TIME_TO_DOWNLOAD_MYSELF_COMMENTS = 104;
    private static final int TIME_TO_DOWNLOAD_MYSELF_BRAVOS = 105;
    private static final int TIME_TO_DOWNLOAD_MYSELF_REPLYS = 106;

    private static final int POOL_SIZE = 10;
    private static final int TIME_SCHEDULE = 120000;

    private static final String[] COMMENTS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_BUCKET_ID,
            MarrySocialDBHelper.KEY_CONTENTS };

    private static final String[] BRAVOS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_COMMENT_ID };

    private static final String[] REPLYS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_COMMENT_ID,
            MarrySocialDBHelper.KEY_REPLY_ID };

    private static final String[] CONTACTS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_NICKNAME };

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
            case TIME_TO_DOWNLOAD_INDIRECT_COMMENTS: {
                mExecutorService.execute(new DownloadIndirectCommentNotices());
                break;
            }
            case TIME_TO_DOWNLOAD_INDIRECT_BRAVOS: {
                mExecutorService.execute(new DownloadBravoNotices());
                break;
            }
            case TIME_TO_DOWNLOAD_INDIRECT_REPLYS: {
                mExecutorService.execute(new DownloadReplyNotices());
                break;
            }
//            case TIME_TO_DOWNLOAD_MYSELF_COMMENTS: {
//                mExecutorService.execute(new DownloadMyselfCommentNotices());
//                break;
//            }
//            case TIME_TO_DOWNLOAD_MYSELF_BRAVOS: {
//                mExecutorService.execute(new DownloadMyselfBravoNotices());
//                break;
//            }
//            case TIME_TO_DOWNLOAD_MYSELF_REPLYS: {
//                mExecutorService.execute(new DownloadMyselfReplyNotices());
//                break;
//            }
            default:
                break;
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
                mHandler.sendEmptyMessage(TIME_TO_DOWNLOAD_INDIRECT_BRAVOS);
//                mHandler.sendEmptyMessage(TIME_TO_DOWNLOAD_MYSELF_BRAVOS);
                mHandler.sendEmptyMessage(
                        TIME_TO_DOWNLOAD_INDIRECT_REPLYS);
//                mHandler.sendEmptyMessageDelayed(
//                        TIME_TO_DOWNLOAD_MYSELF_REPLYS, TIME_SCHEDULE);
            }
        };
        mTimer.schedule(mTimerTask, TIME_SCHEDULE, TIME_SCHEDULE);

        SharedPreferences prefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, this.MODE_PRIVATE);
        mUid = prefs.getString(CommonDataStructure.UID, "");
        mAuthorName = prefs.getString(CommonDataStructure.AUTHOR_NAME, "");

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

    class DownloadBravoNotices implements Runnable {

        @Override
        public void run() {
            ArrayList<NoticesItem> noticeItems = Utils
                    .downloadNoticesList(
                            CommonDataStructure.URL_INDIRECT_NOTICE_LIST, mUid,
                            "", CommonDataStructure.NOTICE_BRAVO);
            if (noticeItems == null || noticeItems.size() == 0) {
                return;
            }
            for (NoticesItem notice : noticeItems) {
                String nikename = queryNikenameFromContactsDB(notice
                        .getFromUid());
                if (nikename == null || nikename.length() == 0) {
                    nikename = "房东是傻逼";
                }
                if (!isBravoIdExistInBravosDB(notice.getFromUid(),
                        notice.getCommentId())) {
                    insertBravoToBravosDB(notice, nikename);
                    if((notice.getUid()).equalsIgnoreCase(notice.getFromUid())) {
                        updateCommentsBravoStatus(notice.getCommentId(),
                                MarrySocialDBHelper.BRAVO_CONFIRM);
                    }
                }
            }
        }

    }

//    class DownloadMyselfBravoNotices implements Runnable {
//
//        @Override
//        public void run() {
//            Log.e(TAG, "nannan DownloadMyselfBravoNotices");
//            ArrayList<NoticesItem> noticeItems = Utils
//                    .downloadMyselfNoticesList(
//                            CommonDataStructure.URL_MYSELF_NOTICE_LIST, mUid,
//                            "", CommonDataStructure.NOTICE_BRAVO);
//            if (noticeItems == null || noticeItems.size() == 0) {
//                return;
//            }
//            Log.e(TAG, "nannan DownloadMyselfBravoNotices 1111");
//            for (NoticesItem notice : noticeItems) {
//                if (!isBravoIdExistInBravosDB(mUid, notice.getCommentId())) {
//                    insertBravoToBravosDB(notice, mAuthorName);
//                    updateCommentsBravoStatus(notice.getCommentId(),
//                            MarrySocialDBHelper.BRAVO_CONFIRM);
//                }
//            }
//        }
//
//    }

    class DownloadReplyNotices implements Runnable {

        @Override
        public void run() {
            String indirectLists = loadIndirectsFromDB();
            ArrayList<NoticesItem> noticeItems = Utils
                    .downloadNoticesList(
                            CommonDataStructure.URL_INDIRECT_NOTICE_LIST, mUid,
                            "", CommonDataStructure.NOTICE_REPLY);
            if (noticeItems == null || noticeItems.size() == 0) {
                return;
            }
            for (NoticesItem notice : noticeItems) {
                ArrayList<ReplysItem> replyItems = Utils.downloadReplysList(
                        CommonDataStructure.URL_REPLY_LIST, notice.getUid(),
                        notice.getCommentId(), indirectLists,
                        "");
                if (replyItems == null || replyItems.size() == 0) {
                    continue;
                }
                for (ReplysItem reply : replyItems) {
                    if (!isReplyIdExistInReplysDB(reply.getReplyId())) {
                        insertReplysToReplyDB(reply);
                    }
                }
            }
            // mHandler.sendEmptyMessage(TIME_TO_DOWNLOAD_INDIRECT_COMMENTS);
        }

    }

//    class DownloadMyselfReplyNotices implements Runnable {
//
//        @Override
//        public void run() {
//            Log.e(TAG, "nannan DownloadMyselfReplyNotices");
//            ArrayList<NoticesItem> noticeItems = Utils
//                    .downloadMyselfNoticesList(
//                            CommonDataStructure.URL_MYSELF_NOTICE_LIST, mUid,
//                            "", CommonDataStructure.NOTICE_REPLY);
//            if (noticeItems == null || noticeItems.size() == 0) {
//                return;
//            }
//            Log.e(TAG, "nannan DownloadMyselfReplyNotices 1111");
//            for (NoticesItem notice : noticeItems) {
//                ArrayList<ReplysItem> replyItems = Utils.downloadReplysList(
//                        CommonDataStructure.URL_REPLY_LIST, notice.getUid(),
//                        notice.getCommentId(), CommonDataStructure.INDIRECTIDS,
//                        "");
//                if (replyItems == null || replyItems.size() == 0) {
//                    continue;
//                }
//                for (ReplysItem reply : replyItems) {
//                    if (!isReplyIdExistInReplysDB(reply.getReplyId())) {
//                        insertReplysToReplyDB(reply);
//                    }
//                }
//            }
//            // mHandler.sendEmptyMessage(TIME_TO_DOWNLOAD_INDIRECT_COMMENTS);
//        }
//
//    }

    class DownloadIndirectCommentNotices implements Runnable {

        @Override
        public void run() {
            // ArrayList<NoticesItem> noticeItems = Utils.downloadNoticesList(
            // CommonDataStructure.URL_TOPIC_COMMENT_WITH_REPLY_LIST,
            // null, null);
            // if (noticeItems == null || noticeItems.size() == 0) {
            // return;
            // }
            // for (NoticesItem notice : noticeItems) {
            // String nikename =
            // queryNikenameFromContactsDB(notice.getFromUid());
            // }
        }

    }

    class DownloadMyselfCommentNotices implements Runnable {

        @Override
        public void run() {
            // ArrayList<NoticesItem> noticeItems = Utils.downloadNoticesList(
            // CommonDataStructure.URL_TOPIC_COMMENT_WITH_REPLY_LIST,
            // null, null);
            // if (noticeItems == null || noticeItems.size() == 0) {
            // return;
            // }
            // for (NoticesItem notice : noticeItems) {
            // String nikename =
            // queryNikenameFromContactsDB(notice.getFromUid());
            // }
        }

    }

    private void insertReplysToReplyDB(ReplysItem reply) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_REPLY_ID, reply.getReplyId());
        insertValues.put(MarrySocialDBHelper.KEY_COMMENT_ID,
                reply.getCommentId());
        insertValues.put(MarrySocialDBHelper.KEY_UID, reply.getUid());
        insertValues.put(MarrySocialDBHelper.KEY_AUTHOR_NICKNAME,
                reply.getNickname());
        insertValues.put(MarrySocialDBHelper.KEY_REPLY_CONTENTS,
                reply.getReplyContents());
        insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
                reply.getReplyTime());
        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.DOWNLOAD_FROM_CLOUD_SUCCESS);

        ContentResolver resolver = getApplicationContext().getContentResolver();
        resolver.insert(CommonDataStructure.REPLYURL, insertValues);
    }

    private void insertBravoToBravosDB(NoticesItem notice, String nikename) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_COMMENT_ID,
                notice.getCommentId());
        insertValues.put(MarrySocialDBHelper.KEY_UID, notice.getFromUid());
        insertValues.put(MarrySocialDBHelper.KEY_AUTHOR_NICKNAME, nikename);
        insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
                notice.getTimeLine());
        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.DOWNLOAD_FROM_CLOUD_SUCCESS);

        ContentResolver resolver = getApplicationContext().getContentResolver();
        resolver.insert(CommonDataStructure.BRAVOURL, insertValues);
    }

    public boolean isBravoIdExistInBravosDB(String uId, String commentId) {
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_UID + " = " + uId
                    + " AND " + MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                    + commentId;
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_BRAVOS_TABLE,
                    BRAVOS_PROJECTION, whereclause, null, null, null, null,
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

    public String queryNikenameFromContactsDB(String uId) {
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

    private void updateCommentsBravoStatus(String comment_id, int bravoStatus) {

        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_BRAVO_STATUS, bravoStatus);

        String whereClause = MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                + comment_id;
        mDBHelper.update(MarrySocialDBHelper.DATABASE_COMMENTS_TABLE,
                insertValues, whereClause, null);
    }

    private String loadIndirectsFromDB() {

        StringBuffer result = new StringBuffer();
        ArrayList<String> indirects = new ArrayList<String>();

        Cursor cursor = mDBHelper.query(
                MarrySocialDBHelper.DATABASE_CONTACTS_TABLE,
                CONTACTS_PROJECTION, null, null, null, null, null, null);
        if (cursor == null) {
            Log.w(TAG, "nannan query fail!");
            return null;
        }

        try {
            while (cursor.moveToNext()) {
                String uid = cursor.getString(0);
                indirects.add(uid);
            }

            for (String uid : indirects) {
                result.append(uid);
            }

            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result.toString();
    }
}
