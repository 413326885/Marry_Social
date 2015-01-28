package com.dhn.marrysocial.services;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.common.CommonDataStructure.UploadReplysResultEntry;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.utils.Utils;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

public class UploadCommentsAndBravosAndReplysIntentService extends
        IntentService {

    private static final String TAG = "UploadCommentsAndBravosAndReplysIntentService";

    public static final String PREFS_LAIQIAN_DEFAULT = "marrysocial_default";

    private static final String[] COMMENTS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_BUCKET_ID,
            MarrySocialDBHelper.KEY_CONTENTS, MarrySocialDBHelper.KEY_ID };

    private static final String[] IMAGES_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_PHOTO_NAME,
            MarrySocialDBHelper.KEY_PHOTO_LOCAL_PATH };

    private static final String[] BRAVOS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_COMMENT_ID };

    private static final String[] REPLYS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_COMMENT_ID,
            MarrySocialDBHelper.KEY_REPLY_CONTENTS };

    private MarrySocialDBHelper mDBHelper;
    private String mToken;
    private String mUId;
    private SharedPreferences mPrefs;

    private ExecutorService mExecutorService;

    public UploadCommentsAndBravosAndReplysIntentService() {
        this(TAG);
        Log.e(TAG, "nannan UploadCommentsIntentService()");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "nannan onCreate()");
        mDBHelper = MarrySocialDBHelper.newInstance(this);
        mPrefs = this.getSharedPreferences(PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        mToken = mPrefs.getString(CommonDataStructure.TOKEN, null);
        mUId = mPrefs.getString(CommonDataStructure.UID, null);
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * CommonDataStructure.THREAD_POOL_SIZE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "nannan onStartCommand()");
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "nannan onDestroy()");
    }

    public UploadCommentsAndBravosAndReplysIntentService(String name) {
        super(name);
        Log.e(TAG, "nannan UploadCommentsIntentService(name)");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (!Utils.isActiveNetWorkAvailable(this)) {
            Toast.makeText(this, R.string.network_not_available, 1000);
            return;
        }

        int type = intent.getIntExtra(CommonDataStructure.KEY_UPLOAD_TYPE, -1);

        switch (type) {
        case CommonDataStructure.KEY_COMMENTS: {
            ArrayList<CommentsEntry> uploadComments = queryNeedUploadComments();
            if (uploadComments == null || uploadComments.size() == 0) {
                return;
            }
            for (CommentsEntry comment : uploadComments) {
                mExecutorService.execute(new UploadComments(comment, mToken));
            }
            break;
        }
        case CommonDataStructure.KEY_BRAVOS: {
            ArrayList<BravoEntry> uploadBravos = queryNeedUploadBravos();
            if (uploadBravos == null || uploadBravos.size() == 0) {
                return;
            }
            for (BravoEntry bravo : uploadBravos) {
                mExecutorService.execute(new UploadBravos(bravo, mToken));
            }
            break;
        }
        case CommonDataStructure.KEY_REPLYS: {
            ArrayList<ReplyEntry> uploadReplys = queryNeedUploadReplys();
            if (uploadReplys == null || uploadReplys.size() == 0) {
                return;
            }
            for (ReplyEntry reply : uploadReplys) {
                mExecutorService.execute(new UploadReplys(reply, mToken));
            }
            break;
        }
        default:
            break;
        }

        // if (mToken == null || mToken.length() == 0) {
        // mToken = Utils.getHttpToken(CommonDataStructure.URL_TOKEN_GET, mUId);
        // Editor editor = mPrefs.edit();
        // editor.putString(CommonDataStructure.TOKEN, mToken);
        // editor.commit();
        // }

        // } else if (!Utils.isHttpTokenValid(CHECK_TOKEN_URL, mUId, mToken)) {
        // mToken = Utils.getHttpToken(GET_TOKEN_URL, mUId);
        // Editor editor = mPrefs.edit();
        // editor.putString(CommonDataStructure.TOKEN, mToken);
        // editor.commit();
        // }

    }

    private ArrayList<CommentsEntry> queryNeedUploadComments() {
        ArrayList<CommentsEntry> commentEntrys = new ArrayList<CommentsEntry>();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_CURRENT_STATUS + " = "
                    + MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD;
            String orderBy = MarrySocialDBHelper.KEY_ID + " ASC ";
            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_COMMENTS_TABLE,
                    COMMENTS_PROJECTION, whereclause, null, null, null, orderBy,
                    null);
            if (cursor == null) {
                return commentEntrys;
            }
            while (cursor.moveToNext()) {
                CommentsEntry comment = new CommentsEntry();
                String uId = cursor.getString(0);
                String bucketId = cursor.getString(1);
                String contents = cursor.getString(2);
                comment.uId = uId;
                comment.bucketId = bucketId;
                comment.contents = contents;
                commentEntrys.add(comment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }

        }
        return commentEntrys;
    }

    private ArrayList<ImagesEntry> queryNeedUploadImages(String uid,
            String bucket_id) {
        ArrayList<ImagesEntry> imageEntrys = new ArrayList<ImagesEntry>();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_CURRENT_STATUS + " = "
                    + MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD + " AND "
                    + MarrySocialDBHelper.KEY_UID + " = " + uid + " AND "
                    + MarrySocialDBHelper.KEY_BUCKET_ID + " = " + bucket_id;
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_IMAGES_TABLE,
                    IMAGES_PROJECTION, whereclause, null, null, null, null,
                    null);
            if (cursor == null) {
                return imageEntrys;
            }
            while (cursor.moveToNext()) {
                ImagesEntry image = new ImagesEntry();
                String uId = cursor.getString(0);
                String photoName = cursor.getString(1);
                String photoPath = cursor.getString(2);
                image.uId = uId;
                image.photoName = photoName;
                image.photoPath = photoPath;
                imageEntrys.add(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }

        }
        return imageEntrys;
    }

    private void updateDBStatusOfComments(String uid, String bucket_id,
            String tid) {
        String whereClause = MarrySocialDBHelper.KEY_CURRENT_STATUS + " = "
                + MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD + " AND "
                + MarrySocialDBHelper.KEY_UID + " = " + uid + " AND "
                + MarrySocialDBHelper.KEY_BUCKET_ID + " = " + bucket_id;
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_COMMENT_ID, tid);
        values.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.UPLOAD_TO_CLOUD_SUCCESS);
        ContentResolver  resolver = this.getContentResolver();
        resolver.update(CommonDataStructure.COMMENTURL, values, whereClause, null);
//        mDBHelper.update(MarrySocialDBHelper.DATABASE_COMMENTS_TABLE, values,
//                whereClause, null);
    }

    private void updateCommentIdOfImages(String uid, String bucket_id,
            String tid) {
        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + uid
                + " AND " + MarrySocialDBHelper.KEY_BUCKET_ID + " = "
                + bucket_id;
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_COMMENT_ID, tid);
        mDBHelper.update(MarrySocialDBHelper.DATABASE_IMAGES_TABLE, values,
                whereClause, null);
    }

    private void updateDBStatusOfImages(String uid, String bucket_id,
            CommonDataStructure.UploadImageResultEntry uploadResult) {
        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + uid
                + " AND " + MarrySocialDBHelper.KEY_BUCKET_ID + " = "
                + bucket_id + " AND " + MarrySocialDBHelper.KEY_PHOTO_POS
                + " = " + uploadResult.pos;

        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH,
                uploadResult.orgUrl);
        values.put(MarrySocialDBHelper.KEY_PHOTO_REMOTE_THUMB_PATH,
                uploadResult.thumbUrl);
        values.put(MarrySocialDBHelper.KEY_PHOTO_ID,
                uploadResult.photoId);
        if (uploadResult.result) {
            values.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                    MarrySocialDBHelper.UPLOAD_TO_CLOUD_SUCCESS);
        } else {
            values.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                    MarrySocialDBHelper.UPLOAD_TO_CLOUD_FAIL);
        }

        mDBHelper.update(MarrySocialDBHelper.DATABASE_IMAGES_TABLE, values,
                whereClause, null);
    }

    class UploadComments implements Runnable {

        private CommentsEntry comments;
        private String tid;
        private String token;

        public UploadComments(CommentsEntry entry, String token) {
            comments = entry;
            this.token = token;
        }

        @Override
        public void run() {

            tid = Utils.uploadCommentContentFile(
                    CommonDataStructure.URL_TOPIC_POST, comments.uId,
                    comments.contents);
            if (tid == null) {
                return;
            }

            updateDBStatusOfComments(comments.uId, comments.bucketId, tid);

            ArrayList<ImagesEntry> uploadImages = queryNeedUploadImages(
                    comments.uId, comments.bucketId);
            if (uploadImages == null || uploadImages.size() == 0) {
                return;
            }

            updateCommentIdOfImages(comments.uId, comments.bucketId, tid);

            int position = 1;
            for (ImagesEntry image : uploadImages) {
                CommonDataStructure.UploadImageResultEntry uploadResult = Utils
                        .uploadImageFile(CommonDataStructure.URL_TOPIC_PIC,
                                image.photoPath, image.photoName, comments.uId,
                                tid, position++);
                updateDBStatusOfImages(comments.uId, comments.bucketId,
                        uploadResult);
            }
        }

    }

    class UploadBravos implements Runnable {

        private BravoEntry bravo;
        private boolean uploadSuccess;
        private String token;

        public UploadBravos(BravoEntry entry, String token) {
            bravo = entry;
            this.token = token;
        }

        @Override
        public void run() {

            uploadSuccess = Utils.uploadBravoFile(
                    CommonDataStructure.URL_TOPIC_PRAISE, bravo.uId,
                    bravo.commentId);
            if (!uploadSuccess) {
                return;
            }

            updateBravoStatusOfBravos(
                    bravo.uId,
                    bravo.commentId,
                    (uploadSuccess) ? MarrySocialDBHelper.UPLOAD_TO_CLOUD_SUCCESS
                            : MarrySocialDBHelper.UPLOAD_TO_CLOUD_FAIL);
        }
    }

    class UploadReplys implements Runnable {

        private ReplyEntry reply;
        private UploadReplysResultEntry result;
        private String token;

        public UploadReplys(ReplyEntry entry, String token) {
            reply = entry;
            this.token = token;
        }

        @Override
        public void run() {

            result = Utils.uploadReplyFile(
                    CommonDataStructure.URL_TOPIC_REPLY_POST, reply.uId,
                    reply.commentId, reply.replyContent);
            if (result == null) {
                return;
            }

            updateReplyStatusOfReplys(result.uId, result.commentId, result.replyId,
                    MarrySocialDBHelper.UPLOAD_TO_CLOUD_SUCCESS);
        }
    }

    private ArrayList<BravoEntry> queryNeedUploadBravos() {
        ArrayList<BravoEntry> bravoEntrys = new ArrayList<BravoEntry>();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_CURRENT_STATUS
                    + " = " + MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD;
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_BRAVOS_TABLE,
                    BRAVOS_PROJECTION, whereclause, null, null, null, null,
                    null);
            if (cursor == null) {
                return bravoEntrys;
            }
            while (cursor.moveToNext()) {
                BravoEntry bravo = new BravoEntry();
                String uId = cursor.getString(0);
                String commentId = cursor.getString(1);
                bravo.uId = uId;
                bravo.commentId = commentId;
                bravoEntrys.add(bravo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }

        }
        return bravoEntrys;
    }

    private ArrayList<ReplyEntry> queryNeedUploadReplys() {
        ArrayList<ReplyEntry> replyEntrys = new ArrayList<ReplyEntry>();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_CURRENT_STATUS
                    + " = " + MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD;
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_REPLYS_TABLE,
                    REPLYS_PROJECTION, whereclause, null, null, null, null,
                    null);
            if (cursor == null) {
                return replyEntrys;
            }
            while (cursor.moveToNext()) {
                ReplyEntry reply = new ReplyEntry();
                String uId = cursor.getString(0);
                String commentId = cursor.getString(1);
                String replyContent = cursor.getString(2);
                reply.uId = uId;
                reply.commentId = commentId;
                reply.replyContent = replyContent;
                replyEntrys.add(reply);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }

        }
        return replyEntrys;
    }

    private void updateBravoStatusOfBravos(String uid, String comment_id,
            int updateStatus) {
        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + uid
                + " AND " + MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                + comment_id;
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_CURRENT_STATUS, updateStatus);
        mDBHelper.update(MarrySocialDBHelper.DATABASE_BRAVOS_TABLE, values,
                whereClause, null);
    }

    private void updateReplyStatusOfReplys(String uid, String comment_id, String reply_id,
            int updataStatus) {
        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + uid
                + " AND " + MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                + comment_id;
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_CURRENT_STATUS, updataStatus);
        values.put(MarrySocialDBHelper.KEY_REPLY_ID, reply_id);
        mDBHelper.update(MarrySocialDBHelper.DATABASE_REPLYS_TABLE, values,
                whereClause, null);
    }

    static class CommentsEntry {
        public String uId;
        public String bucketId;
        public String contents;
    }

    static class ImagesEntry {
        public String uId;
        public String photoName;
        public String photoPath;
    }

    static class BravoEntry {
        public String uId;
        public String commentId;
    }

    static class ReplyEntry {
        public String uId;
        public String commentId;
        public String replyContent;
    }
}
