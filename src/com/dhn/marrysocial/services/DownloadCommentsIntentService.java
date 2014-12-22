package com.dhn.marrysocial.services;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.utils.Utils;
import com.dhn.marrysocial.base.CommentsItem;
import com.dhn.marrysocial.base.ReplysItem;

public class DownloadCommentsIntentService extends IntentService {

    private static final String TAG = "DownloadCommentsIntentService";

    private static final int POOL_SIZE = 10;

    public static final String PREFS_LAIQIAN_DEFAULT = "marrysocial_default";

    private static final String[] COMMENTS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_BUCKET_ID,
            MarrySocialDBHelper.KEY_CONTENTS };

    private static final String[] REPLYS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_COMMENT_ID,
            MarrySocialDBHelper.KEY_REPLY_ID };

    private MarrySocialDBHelper mDBHelper;
    // private String mToken;
    // private String mUId;
    private SharedPreferences mPrefs;

    private ExecutorService mExecutorService;

    public DownloadCommentsIntentService() {
        this(TAG);
    }

    public DownloadCommentsIntentService(String name) {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,
                "nannan DownloadCommentsIntentService onCreate()  2222222222222");
        mDBHelper = MarrySocialDBHelper.newInstance(this);
        mPrefs = this.getSharedPreferences(PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        // mToken = mPrefs.getString(CommonDataStructure.TOKEN, null);
        // mUId = mPrefs.getString(CommonDataStructure.UID, null);
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(TAG,
                "nannan DownloadCommentsIntentService onHandleIntent()  333333333333");
        if (!Utils.isActiveNetWorkAvailable(this)) {
            Toast.makeText(this, R.string.network_not_available, 1000);
            return;
        }

        mExecutorService.execute(new DownloadFiles());
    }

    class DownloadFiles implements Runnable {

        @Override
        public void run() {
            Log.e(TAG, "nannan DownloadFiles ()  1234567890");
            Long addedTime = 0l;
            ArrayList<CommentsItem> commentItems = Utils
                    .downloadCommentsList(
                            CommonDataStructure.URL_TOPIC_COMMENT_WITH_REPLY_LIST,
                            null);
            if (commentItems == null || commentItems.size() == 0) {
                return;
            }
            for (CommentsItem comment : commentItems) {
                if (Long.valueOf(comment.getAddTime()) > addedTime) {
                    addedTime = Long.valueOf(comment.getAddTime());
                }
                if (!isCommentIdExistInCommentsDB(comment.getCommentId())) {
                    insertCommentsToDB(comment);
                }
                ArrayList<ReplysItem> replyLists = comment.getReplyList();
                if (replyLists != null && replyLists.size() != 0) {
                    for (ReplysItem reply : replyLists) {
                        if (!isReplyIdExistInReplysDB(reply.getReplyId())) {
                            insertReplysToReplyDB(reply);
                        }
                    }
                }
            }
            Editor editor = mPrefs.edit();
            editor.putLong(MarrySocialDBHelper.KEY_ADDED_TIME, addedTime);
            editor.commit();
        }
    }

    public void insertCommentsToDB(CommentsItem comment) {

        Log.e(TAG, "nannan insertCommentsToDB ()  000000000000000");
        String addedTime = comment.getAddTime();

        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_UID, comment.getUid());
        values.put(MarrySocialDBHelper.KEY_COMMENT_ID, comment.getCommentId());
        values.put(MarrySocialDBHelper.KEY_BUCKET_ID, addedTime.hashCode());
        values.put(MarrySocialDBHelper.KEY_ADDED_TIME, addedTime);
        values.put(MarrySocialDBHelper.KEY_CONTENTS, comment.getContents());
        values.put(MarrySocialDBHelper.KEY_AUTHOR_FULLNAME,
                comment.getFulName());
        values.put(MarrySocialDBHelper.KEY_PHOTO_COUNT, comment.getPhotoCount());
        values.put(MarrySocialDBHelper.KEY_BRAVO_COUNT, 0);
        values.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.DOWNLOAD_FROM_CLOUD_SUCCESS);

        ContentResolver resolver = this.getContentResolver();
        resolver.insert(CommonDataStructure.COMMENTURL, values);
    }

    private void insertReplysToReplyDB(ReplysItem reply) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_COMMENT_ID,
                reply.getCommentId());
        insertValues.put(MarrySocialDBHelper.KEY_UID, reply.getUid());
        insertValues.put(MarrySocialDBHelper.KEY_AUTHOR_FULLNAME,
                reply.getFullName());
        insertValues.put(MarrySocialDBHelper.KEY_REPLY_CONTENTS,
                reply.getReplyContents());
        insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
                reply.getReplyTime());
        insertValues.put(MarrySocialDBHelper.KEY_REPLY_ID, reply.getReplyId());
        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.DOWNLOAD_FROM_CLOUD_SUCCESS);

        ContentResolver resolver = this.getContentResolver();
        resolver.insert(CommonDataStructure.REPLYURL, insertValues);
    }

    public boolean isCommentIdExistInCommentsDB(String commentId) {
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                    + commentId;
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
