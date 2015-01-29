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
    private String mUid;
    private SharedPreferences mPrefs;

    private ExecutorService mExecutorService;

    public UploadCommentsAndBravosAndReplysIntentService() {
        this(TAG);
    }

    public UploadCommentsAndBravosAndReplysIntentService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDBHelper = MarrySocialDBHelper.newInstance(this);
        mPrefs = this.getSharedPreferences(PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        mToken = mPrefs.getString(CommonDataStructure.TOKEN, null);
        mUid = mPrefs.getString(CommonDataStructure.UID, null);
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * CommonDataStructure.THREAD_POOL_SIZE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (!Utils.isActiveNetWorkAvailable(this)) {
            return;
        }

        int type = intent.getIntExtra(CommonDataStructure.KEY_UPLOAD_TYPE, -1);

        switch (type) {
        case CommonDataStructure.KEY_COMMENTS: {
            String bucket_id = intent
                    .getStringExtra(MarrySocialDBHelper.KEY_BUCKET_ID);
            CommentsEntry uploadComment = queryNeedUploadComments(bucket_id);
            if (uploadComment == null) {
                return;
            }
            mExecutorService.execute(new UploadComments(uploadComment, mToken));
            break;
        }
        case CommonDataStructure.KEY_BRAVOS: {
            String comment_id = intent
                    .getStringExtra(MarrySocialDBHelper.KEY_COMMENT_ID);
            BravoEntry uploadBravos = queryNeedUploadBravos(comment_id);
            if (uploadBravos == null) {
                return;
            }
            mExecutorService.execute(new UploadBravos(uploadBravos, mToken));
            break;
        }
        case CommonDataStructure.KEY_REPLYS: {
            String comment_id = intent
                    .getStringExtra(MarrySocialDBHelper.KEY_COMMENT_ID);
            ReplyEntry uploadReplys = queryNeedUploadReplys(comment_id);
            if (uploadReplys == null) {
                return;
            }
            mExecutorService.execute(new UploadReplys(uploadReplys, mToken));
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

    private CommentsEntry queryNeedUploadComments(String bucket_id) {
        CommentsEntry commentEntry = new CommentsEntry();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_BUCKET_ID + " = "
                    + bucket_id;
            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_COMMENTS_TABLE,
                    COMMENTS_PROJECTION, whereclause, null, null, null, null,
                    null);
            if (cursor == null) {
                return null;
            }
            while (cursor.moveToNext()) {
                String uId = cursor.getString(0);
                String bucketId = cursor.getString(1);
                String contents = cursor.getString(2);
                commentEntry.uId = uId;
                commentEntry.bucketId = bucketId;
                commentEntry.contents = contents;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }

        }
        return commentEntry;
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
        ContentResolver resolver = this.getContentResolver();
        resolver.update(CommonDataStructure.COMMENTURL, values, whereClause,
                null);
        // mDBHelper.update(MarrySocialDBHelper.DATABASE_COMMENTS_TABLE, values,
        // whereClause, null);
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
        values.put(MarrySocialDBHelper.KEY_PHOTO_ID, uploadResult.photoId);
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

            updateReplyStatusOfReplys(result.uId, result.commentId,
                    result.replyId, MarrySocialDBHelper.UPLOAD_TO_CLOUD_SUCCESS);
        }
    }

    private BravoEntry queryNeedUploadBravos(String comment_id) {
        BravoEntry bravoEntrys = new BravoEntry();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                    + comment_id + " AND " + MarrySocialDBHelper.KEY_UID + " ="
                    + mUid + " AND " + MarrySocialDBHelper.KEY_CURRENT_STATUS
                    + " =" + MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD;
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_BRAVOS_TABLE,
                    BRAVOS_PROJECTION, whereclause, null, null, null, null,
                    null);
            if (cursor == null) {
                return null;
            }
            while (cursor.moveToNext()) {
                String uId = cursor.getString(0);
                String commentId = cursor.getString(1);
                bravoEntrys.uId = uId;
                bravoEntrys.commentId = commentId;
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

    private ReplyEntry queryNeedUploadReplys(String comment_id) {
        ReplyEntry replyEntrys = new ReplyEntry();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                    + comment_id + " AND " + MarrySocialDBHelper.KEY_UID + " ="
                    + mUid + " AND " + MarrySocialDBHelper.KEY_CURRENT_STATUS
                    + " =" + MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD;
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_REPLYS_TABLE,
                    REPLYS_PROJECTION, whereclause, null, null, null, null,
                    null);
            if (cursor == null) {
                return null;
            }
            while (cursor.moveToNext()) {
                String uId = cursor.getString(0);
                String commentId = cursor.getString(1);
                String replyContent = cursor.getString(2);
                replyEntrys.uId = uId;
                replyEntrys.commentId = commentId;
                replyEntrys.replyContent = replyContent;
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

    private void updateReplyStatusOfReplys(String uid, String comment_id,
            String reply_id, int updataStatus) {
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
