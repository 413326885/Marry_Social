package com.pkjiao.friends.mm.services;

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

import com.pkjiao.friends.mm.R;
import com.pkjiao.friends.mm.base.CommentsItem;
import com.pkjiao.friends.mm.base.ContactsInfo;
import com.pkjiao.friends.mm.base.ImagesItem;
import com.pkjiao.friends.mm.base.ReplysItem;
import com.pkjiao.friends.mm.common.CommonDataStructure;
import com.pkjiao.friends.mm.database.MarrySocialDBHelper;
import com.pkjiao.friends.mm.utils.Utils;

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

    private static final String[] IMAGES_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_COMMENT_ID,
            MarrySocialDBHelper.KEY_PHOTO_ID };

    private static final String[] CONTACTS_PROJECTION = { MarrySocialDBHelper.KEY_UID };

    private MarrySocialDBHelper mDBHelper;
    // private String mToken;
    private String mUid;
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

        Editor editor = mPrefs.edit();
        editor.putInt(CommonDataStructure.NOTIFICATION_COMMENTS_COUNT, 0);
        editor.commit();

        String tid = data.getStringExtra(CommonDataStructure.COMMENT_ID);

        Intent intent = new Intent(CommonDataStructure.KEY_BROADCAST_ACTION);
        intent.putExtra(CommonDataStructure.KEY_BROADCAST_CMDID,
                CommonDataStructure.KEY_NEW_COMMENTS);
        this.sendBroadcast(intent);

        mExecutorService.execute(new DownloadComments(tid));
    }

    class DownloadComments implements Runnable {

        private String tid;

        public DownloadComments(String tid) {
            this.tid = tid;
        }

        @Override
        public void run() {

            ArrayList<String> commentIds = new ArrayList<String> ();
            String indirectLists = loadIndirectsFromDB();
            Long addedTime = 0l;

            ArrayList<CommentsItem> commentItems = Utils.downloadCommentsList(
                    CommonDataStructure.URL_TOPIC_COMMENT_LIST, mUid,
                    indirectLists, tid, CommonDataStructure.DEFAULT_COUNT, "");
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

                ArrayList<ImagesItem> imageLists = comment.getImages();
                if (imageLists != null && imageLists.size() != 0) {
                    for (ImagesItem image : imageLists) {
                        if (!isPhotoIdExistInImagesDB(image.getPhotoId())) {
                            insertImagesToImageDB(image);
                        }
                    }
                }

                commentIds.add(comment.getCommentId());
            }

            Editor editor = mPrefs.edit();
            editor.putLong(MarrySocialDBHelper.KEY_ADDED_TIME, addedTime);
            editor.commit();

            startToDownloadBravos(commentIds);
            startToDownloadReply(commentIds, indirectLists);
        }
    }

    public void insertCommentsToDB(CommentsItem comment) {

        String addedTime = comment.getAddTime();

        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_UID, comment.getUid());
        values.put(MarrySocialDBHelper.KEY_COMMENT_ID, comment.getCommentId());
        values.put(MarrySocialDBHelper.KEY_BUCKET_ID, addedTime.hashCode());
        values.put(MarrySocialDBHelper.KEY_ADDED_TIME, addedTime);
        values.put(MarrySocialDBHelper.KEY_CONTENTS, comment.getContents());
        values.put(MarrySocialDBHelper.KEY_AUTHOR_NICKNAME,
                comment.getNickName());
        values.put(MarrySocialDBHelper.KEY_PHOTO_COUNT, comment.getPhotoCount());
        values.put(MarrySocialDBHelper.KEY_BRAVO_COUNT, 0);
        values.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.DOWNLOAD_FROM_CLOUD_SUCCESS);

        try {
            ContentResolver resolver = this.getContentResolver();
            resolver.insert(CommonDataStructure.COMMENTURL, values);
        } catch(Exception exp){
            exp.printStackTrace();
        }

    }

//    private void insertReplysToReplyDB(ReplysItem reply) {
//        ContentValues insertValues = new ContentValues();
//        insertValues.put(MarrySocialDBHelper.KEY_COMMENT_ID,
//                reply.getCommentId());
//        insertValues.put(MarrySocialDBHelper.KEY_UID, reply.getUid());
//        insertValues.put(MarrySocialDBHelper.KEY_AUTHOR_NICKNAME,
//                reply.getNickname());
//        insertValues.put(MarrySocialDBHelper.KEY_REPLY_CONTENTS,
//                reply.getReplyContents());
//        insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
//                reply.getReplyTime());
//        insertValues.put(MarrySocialDBHelper.KEY_REPLY_ID, reply.getReplyId());
//        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
//                MarrySocialDBHelper.DOWNLOAD_FROM_CLOUD_SUCCESS);
//
//        ContentResolver resolver = this.getContentResolver();
//        resolver.insert(CommonDataStructure.REPLYURL, insertValues);
//    }

    private void insertImagesToImageDB(ImagesItem image) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_COMMENT_ID,
                image.getCommentId());
        insertValues.put(MarrySocialDBHelper.KEY_UID, image.getUid());
        insertValues
                .put(MarrySocialDBHelper.KEY_BUCKET_ID, image.getBucketId());
        insertValues
                .put(MarrySocialDBHelper.KEY_ADDED_TIME, image.getAddTime());
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_ID, image.getPhotoId());
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_POS,
                image.getPhotoPosition());
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_NAME,
                image.getPhotoName());
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_TYPE,
                image.getPhotoType());
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH,
                image.getPhotoRemoteOrgPath());
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_REMOTE_SMALL_THUMB_PATH,
                image.getPhotoRemoteSmallThumbPath());
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_REMOTE_BIG_THUMB_PATH,
                image.getPhotoRemoteBigThumbPath());
        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.NEED_DOWNLOAD_FROM_CLOUD);

        try {
            mDBHelper.insert(MarrySocialDBHelper.DATABASE_IMAGES_TABLE,
                    insertValues);
        } catch(Exception exp){
            exp.printStackTrace();
        }

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

    public boolean isPhotoIdExistInImagesDB(String photoId) {
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_PHOTO_ID + " = "
                    + photoId;
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_IMAGES_TABLE,
                    IMAGES_PROJECTION, whereclause, null, null, null, null,
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
                result.append(uid).append(",");
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

    private void startToDownloadBravos(ArrayList<String> commentIds) {

        Intent serviceIntent = new Intent(getApplicationContext(),
                DownloadBravosIntentServices.class);
        serviceIntent.putStringArrayListExtra(CommonDataStructure.COMMENT_ID_LIST, commentIds);
        startService(serviceIntent);
    }

    private void startToDownloadReply(ArrayList<String> commentIds, String indirectIds) {

        Intent serviceIntent = new Intent(getApplicationContext(),
                DownloadReplysIntentServices.class);
        serviceIntent.putStringArrayListExtra(CommonDataStructure.COMMENT_ID_LIST, commentIds);
        serviceIntent.putExtra(CommonDataStructure.INDIRECT_ID_LIST, indirectIds);
        startService(serviceIntent);
    }
}
