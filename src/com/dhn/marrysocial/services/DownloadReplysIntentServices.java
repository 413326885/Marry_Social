package com.dhn.marrysocial.services;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dhn.marrysocial.base.ReplysItem;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.utils.Utils;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;

public class DownloadReplysIntentServices extends IntentService {

    private static final String TAG = "DownloadReplysIntentServices";

    private static final String[] REPLYS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_COMMENT_ID,
            MarrySocialDBHelper.KEY_REPLY_ID };

    private static final int POOL_SIZE = 10;

    public static final String PREFS_LAIQIAN_DEFAULT = "marrysocial_default";

    private MarrySocialDBHelper mDBHelper;
    // private String mToken;
    private String mUid;
    private SharedPreferences mPrefs;

    private ExecutorService mExecutorService;

    public DownloadReplysIntentServices() {
        this(TAG);
    }

    public DownloadReplysIntentServices(String name) {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDBHelper = MarrySocialDBHelper.newInstance(this);
        mPrefs = this.getSharedPreferences(PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        // mToken = mPrefs.getString(CommonDataStructure.TOKEN, null);
        mUid = mPrefs.getString(CommonDataStructure.UID, null);
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);
    }

    @Override
    protected void onHandleIntent(Intent data) {

        if (!Utils.isActiveNetWorkAvailable(this)) {
            return;
        }

        int loginStatus = mPrefs.getInt(CommonDataStructure.LOGINSTATUS,
                CommonDataStructure.LOGIN_STATUS_NO_USER);
        if (loginStatus != CommonDataStructure.LOGIN_STATUS_LOGIN) {
            return;
        }

        ArrayList<String> commentIds = data
                .getStringArrayListExtra(CommonDataStructure.COMMENT_ID_LIST);
        String indirectIds = data
                .getStringExtra(CommonDataStructure.INDIRECT_ID_LIST);

        mExecutorService.execute(new DownloadReplys(commentIds, indirectIds));
    }

    class DownloadReplys implements Runnable {

        private ArrayList<String> commentIds;
        private String indirectIds;

        public DownloadReplys(ArrayList<String> commentIds, String indirectIds) {
            this.commentIds = commentIds;
            this.indirectIds = indirectIds;
        }

        @Override
        public void run() {

            for (String commentId : commentIds) {
                ArrayList<ReplysItem> replyItems = Utils.downloadReplysList(
                        CommonDataStructure.URL_REPLY_LIST, mUid, commentId,
                        indirectIds, "");
                if (replyItems == null || replyItems.size() == 0) {
                    continue;
                }
                for (ReplysItem reply : replyItems) {
                    if (!isReplyIdExistInReplysDB(reply.getReplyId())) {
                        insertReplysToReplyDB(reply);
                    }
                }
            }
        }
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

        try {
            ContentResolver resolver = getApplicationContext()
                    .getContentResolver();
            resolver.insert(CommonDataStructure.REPLYURL, insertValues);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }
}
