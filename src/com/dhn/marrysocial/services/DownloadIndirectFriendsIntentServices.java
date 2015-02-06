package com.dhn.marrysocial.services;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.base.ContactsInfo;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.utils.Utils;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.widget.Toast;

public class DownloadIndirectFriendsIntentServices extends IntentService {

    private static final String TAG = "DownloadIndirectFriendsIntentServices";

    private static final String[] CONTACTS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_INDIRECT_ID };

    private static final String[] HEAD_PICS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_HEAD_PIC_BITMAP,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_THUMB_PATH };

    private static final int POOL_SIZE = 10;

    public static final String PREFS_LAIQIAN_DEFAULT = "marrysocial_default";

    private String mUid;
    private MarrySocialDBHelper mDBHelper;
    private SharedPreferences mPrefs;

    private ExecutorService mExecutorService;

    public DownloadIndirectFriendsIntentServices() {
        this(TAG);
    }

    public DownloadIndirectFriendsIntentServices(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDBHelper = MarrySocialDBHelper.newInstance(this);
        mPrefs = this.getSharedPreferences(PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        mUid = mPrefs.getString(CommonDataStructure.UID, "");
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!Utils.isActiveNetWorkAvailable(this)) {
            Toast.makeText(this, R.string.network_not_available, 1000);
            return;
        }

        int loginStatus = mPrefs.getInt(CommonDataStructure.LOGINSTATUS,
                CommonDataStructure.LOGIN_STATUS_NO_USER);
        if (loginStatus != CommonDataStructure.LOGIN_STATUS_LOGIN) {
            return;
        }

        mExecutorService.execute(new DownloadContacts());
    }

    class DownloadContacts implements Runnable {

        @Override
        public void run() {

            if (!isContactExistInContactsDB(mUid)) {
                ContactsInfo authorInfo = Utils.downloadUserInfo(
                        CommonDataStructure.URL_GET_USER_PROFILE, mUid);
                if (authorInfo != null) {
                    insertContactToContactsDB(authorInfo);
                    String headPicOrgUrl = CommonDataStructure.HEAD_PICS_ORG_PATH
                            + authorInfo.getUid() + ".jpg";
                    String headPicThumbUrl = CommonDataStructure.HEAD_PICS_THUMB_PATH
                            + authorInfo.getUid() + ".jpg";
                    Bitmap headPicBitmap = Utils
                            .downloadHeadPicBitmap(headPicOrgUrl);
                    if (headPicBitmap != null) {
                        if (!isUidExistInHeadPicDB(authorInfo.getUid())) {
                            insertHeadPicToHeadPicsDB(headPicBitmap,
                                    authorInfo.getUid(), headPicOrgUrl,
                                    headPicThumbUrl);
                        } else {
                            updateHeadPicToHeadPicsDB(headPicBitmap,
                                    authorInfo.getUid(), headPicOrgUrl,
                                    headPicThumbUrl);
                        }
                    }
                }
            }

            boolean isServerUpdated = Utils.updateIndirectServer(
                    CommonDataStructure.URL_INDIRECT_SERVER_UPDATE, mUid);

            ArrayList<ContactsInfo> contactsList = Utils
                    .downloadInDirectFriendsList(
                            CommonDataStructure.URL_INDIRECT_LIST, mUid, "");
            if (contactsList == null || contactsList.size() == 0) {
                return;
            }
            for (ContactsInfo contact : contactsList) {
                if (!isContactExistInContactsDB(contact.getUid())) {
                    insertContactToContactsDB(contact);
                }
                String headPicOrgUrl = CommonDataStructure.HEAD_PICS_ORG_PATH
                        + contact.getUid() + ".jpg";
                String headPicThumbUrl = CommonDataStructure.HEAD_PICS_THUMB_PATH
                        + contact.getUid() + ".jpg";
                Bitmap headPicBitmap = Utils
                        .downloadHeadPicBitmap(headPicOrgUrl);
                if (headPicBitmap != null) {
                    if (!isUidExistInHeadPicDB(contact.getUid())) {
                        insertHeadPicToHeadPicsDB(headPicBitmap,
                                contact.getUid(), headPicOrgUrl,
                                headPicThumbUrl);
                    } else {
                        updateHeadPicToHeadPicsDB(headPicBitmap,
                                contact.getUid(), headPicOrgUrl,
                                headPicThumbUrl);
                    }
                }

            }
        }

    }

    private void insertContactToContactsDB(ContactsInfo contact) {
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_UID, contact.getUid());
        values.put(MarrySocialDBHelper.KEY_NICKNAME, contact.getNickName());
        values.put(MarrySocialDBHelper.KEY_REALNAME, contact.getRealName());
        values.put(MarrySocialDBHelper.KEY_HOBBY, contact.getHobby());
        values.put(MarrySocialDBHelper.KEY_GENDER, contact.getGender());
        values.put(MarrySocialDBHelper.KEY_ASTRO, contact.getAstro());
        values.put(MarrySocialDBHelper.KEY_INTRODUCT, contact.getIntroduce());
        values.put(MarrySocialDBHelper.KEY_DIRECT_FRIENDS_COUNT,
                contact.getDirectFriendsCount());
        values.put(MarrySocialDBHelper.KEY_FIRST_DIRECT_FRIEND,
                contact.getFirstDirectFriend());
        values.put(MarrySocialDBHelper.KEY_DIRECT_FRIENDS,
                contact.getDirectFriends());
        values.put(MarrySocialDBHelper.KEY_INDIRECT_ID, contact.getIndirectId());
        values.put(MarrySocialDBHelper.KEY_HEADPIC, contact.getHeadPic());
        values.put(MarrySocialDBHelper.KEY_HEADER_BACKGROUND_INDEX,
                contact.getHeaderBkgIndex());

        try {
            mDBHelper.insert(MarrySocialDBHelper.DATABASE_CONTACTS_TABLE,
                    values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isContactExistInContactsDB(String uId) {
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_UID + " = " + uId;
            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_CONTACTS_TABLE,
                    CONTACTS_PROJECTION, whereclause, null, null, null, null,
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

    private int insertHeadPicToHeadPicsDB(Bitmap headPicBitmap, String uid,
            String orgUrl, String thumbUrl) {

        String headPicName = "head_pic_" + uid + ".jpg";

        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_UID, uid);
        insertValues.put(MarrySocialDBHelper.KEY_HEAD_PIC_BITMAP,
                Utils.Bitmap2Bytes(headPicBitmap));
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH, orgUrl);
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_REMOTE_THUMB_PATH,
                thumbUrl);
        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.UPLOAD_TO_CLOUD_SUCCESS);
        long rowId = 0;
        try {
            rowId = mDBHelper.insert(
                    MarrySocialDBHelper.DATABASE_HEAD_PICS_TABLE, insertValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (int) (rowId);
    }

    private void updateHeadPicToHeadPicsDB(Bitmap headPicBitmap, String uid,
            String orgUrl, String thumbUrl) {

        String headPicName = "head_pic_" + uid + ".jpg";

        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_HEAD_PIC_BITMAP,
                Utils.Bitmap2Bytes(headPicBitmap));
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH, orgUrl);
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_REMOTE_THUMB_PATH,
                thumbUrl);
        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.UPLOAD_TO_CLOUD_SUCCESS);

        try {
            String whereClause = MarrySocialDBHelper.KEY_UID + " = " + uid;
            mDBHelper.update(MarrySocialDBHelper.DATABASE_HEAD_PICS_TABLE,
                    insertValues, whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isUidExistInHeadPicDB(String uid) {
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_UID + " = " + uid;
            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_HEAD_PICS_TABLE,
                    HEAD_PICS_PROJECTION, whereclause, null, null, null, null,
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
