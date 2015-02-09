package com.dhn.marrysocial.services;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dhn.marrysocial.base.NoticesItem;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.utils.Utils;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;

public class DownloadBravosIntentServices extends IntentService {

    private static final String TAG = "DownloadBravosIntentServices";

    private static final String[] CONTACTS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_NICKNAME };

    private static final String[] BRAVOS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_COMMENT_ID };

    private static final int POOL_SIZE = 10;

    public static final String PREFS_LAIQIAN_DEFAULT = "marrysocial_default";

    private MarrySocialDBHelper mDBHelper;
    // private String mToken;
    private String mUid;
    private SharedPreferences mPrefs;

    private ExecutorService mExecutorService;

    public DownloadBravosIntentServices() {
        this(TAG);
    }

    public DownloadBravosIntentServices(String name) {
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

        mExecutorService.execute(new DownloadBravos(commentIds));
    }

    class DownloadBravos implements Runnable {

        private ArrayList<String> commentIds;

        public DownloadBravos(ArrayList<String> commentIds) {
            this.commentIds = commentIds;
        }

        @Override
        public void run() {
            StringBuffer commentIdList = new StringBuffer();
            for (String commentId : commentIds) {
                commentIdList.append(commentId).append(",");
            }

            ArrayList<NoticesItem> noticeItems = Utils.downloadBravosList(
                    CommonDataStructure.URL_DOWNLOAD_BRAVOS, mUid,
                    commentIdList.toString());
            if (noticeItems == null || noticeItems.size() == 0) {
                return;
            }
            for (NoticesItem notice : noticeItems) {

                if (notice.getNoticeType() == CommonDataStructure.NOTICE_TYPE_BRAVO) {
                    String nikename = queryNikenameFromContactsDB(notice
                            .getFromUid());
                    if (nikename == null || nikename.length() == 0) {
                        nikename = "房东是傻逼";
                    }
                    if (!isBravoIdExistInBravosDB(notice.getFromUid(),
                            notice.getCommentId())) {
                        insertBravoToBravosDB(notice, nikename);
                        if ((notice.getUid()).equalsIgnoreCase(notice
                                .getFromUid())) {
                            updateCommentsBravoStatus(notice.getCommentId(),
                                    MarrySocialDBHelper.BRAVO_CONFIRM);
                        }
                    }
                } else {
                    if (isBravoIdExistInBravosDB(notice.getFromUid(),
                            notice.getCommentId())) {
                        deleteBravoFromBravosDB(notice.getFromUid(),
                                notice.getCommentId());
                    }
                }

            }
        }
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

        try {
            ContentResolver resolver = getApplicationContext()
                    .getContentResolver();
            resolver.insert(CommonDataStructure.BRAVOURL, insertValues);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

    private void deleteBravoFromBravosDB(String uId, String commentId) {

        String whereclause = MarrySocialDBHelper.KEY_UID + " = " + uId
                + " AND " + MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                + commentId;

        try {
            ContentResolver resolver = getApplicationContext()
                    .getContentResolver();
            resolver.delete(CommonDataStructure.BRAVOURL, whereclause, null);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

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

    private void updateCommentsBravoStatus(String comment_id, int bravoStatus) {

        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_BRAVO_STATUS, bravoStatus);

        String whereClause = MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                + comment_id;

        try {
            mDBHelper.update(MarrySocialDBHelper.DATABASE_COMMENTS_TABLE,
                    insertValues, whereClause, null);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }
}
