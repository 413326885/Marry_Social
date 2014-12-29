package com.dhn.marrysocial.services;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.widget.Toast;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.services.UploadCommentsAndBravosAndReplysIntentService.BravoEntry;
import com.dhn.marrysocial.services.UploadCommentsAndBravosAndReplysIntentService.UploadBravos;
import com.dhn.marrysocial.utils.Utils;

public class DeleteCommentsAndBravosAndReplysIntentServices extends
        IntentService {

    private static final String TAG = "DeleteCommentsAndBravosAndReplysIntentServices";

    public static final String PREFS_LAIQIAN_DEFAULT = "marrysocial_default";

    private static final String[] COMMENTS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_BUCKET_ID,
            MarrySocialDBHelper.KEY_CONTENTS };

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

    public DeleteCommentsAndBravosAndReplysIntentServices() {
        this(TAG);
    }

    public DeleteCommentsAndBravosAndReplysIntentServices(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDBHelper = MarrySocialDBHelper.newInstance(this);
        mPrefs = this.getSharedPreferences(PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        mToken = mPrefs.getString(CommonDataStructure.TOKEN, null);
        mUId = mPrefs.getString(CommonDataStructure.UID, null);
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * CommonDataStructure.THREAD_POOL_SIZE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (!Utils.isActiveNetWorkAvailable(this)) {
            Toast.makeText(this, R.string.network_not_available, 1000);
            return;
        }

        int type = intent.getIntExtra(CommonDataStructure.KEY_DELETE_TYPE, -1);

        switch (type) {
        case CommonDataStructure.KEY_COMMENTS: {
            break;
        }
        case CommonDataStructure.KEY_BRAVOS: {
            ArrayList<BravoEntry> deleteBravos = queryNeedDeleteBravos();
            if (deleteBravos == null || deleteBravos.size() == 0) {
                return;
            }
            for (BravoEntry bravo : deleteBravos) {
                mExecutorService.execute(new DeleteBravos(bravo, mToken));
            }
            break;
        }
        case CommonDataStructure.KEY_REPLYS: {
            break;
        }
        default:
            break;
        }
    }

    class DeleteBravos implements Runnable {

        private BravoEntry bravo;
        private boolean deleteSuccess;
        private String token;

        public DeleteBravos(BravoEntry entry, String token) {
            bravo = entry;
            this.token = token;
        }

        @Override
        public void run() {

            deleteSuccess = Utils.deleteBravoFileFromCloud(
                    CommonDataStructure.URL_TOPIC_DISPRAISE, bravo.uId,
                    bravo.commentId);
            if (!deleteSuccess) {
                return;
            }

            updateBravoStatusOfBravos(bravo.uId, bravo.commentId);
        }
    }

    private ArrayList<BravoEntry> queryNeedDeleteBravos() {
        ArrayList<BravoEntry> bravoEntrys = new ArrayList<BravoEntry>();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_CURRENT_STATUS + " = "
                    + MarrySocialDBHelper.NEED_DELETE_FROM_CLOUD;
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

    private void updateBravoStatusOfBravos(String uid, String comment_id) {
        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + uid
                + " AND " + MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                + comment_id;
        ContentResolver resolver = this.getContentResolver();
        resolver.delete(CommonDataStructure.BRAVOURL, whereClause, null);
    }
}
