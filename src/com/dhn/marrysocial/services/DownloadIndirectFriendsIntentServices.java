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
import android.widget.Toast;

public class DownloadIndirectFriendsIntentServices extends IntentService {

    private static final String TAG = "DownloadIndirectFriendsIntentServices";

    private static final String[] CONTACTS_PROJECTION = {
        MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_INDIRECT_ID };

    private static final int POOL_SIZE = 10;

    public static final String PREFS_LAIQIAN_DEFAULT = "marrysocial_default";

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
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!Utils.isActiveNetWorkAvailable(this)) {
            Toast.makeText(this, R.string.network_not_available, 1000);
            return;
        }
        mExecutorService.execute(new DownloadContacts());
    }

    class DownloadContacts implements Runnable {

        @Override
        public void run() {
            ArrayList<ContactsInfo> contactsList = Utils.downloadDirectFriendsList(
                    CommonDataStructure.URL_INDIRECT_LIST, null, null);
            if (contactsList == null || contactsList.size() == 0) {
                return;
            }
            for (ContactsInfo contact : contactsList) {
                if (!isContactExistInContactsDB(contact.getUid())) {
                    insertContactToContactsDB(contact);
                }
            }
        }

    }

    private void insertContactToContactsDB(ContactsInfo contact) {
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_UID, contact.getUid());
        values.put(MarrySocialDBHelper.KEY_NIKENAME, contact.getNikeName());
        values.put(MarrySocialDBHelper.KEY_REALNAME, contact.getRealName());
        values.put(MarrySocialDBHelper.KEY_HOBBY, contact.getHobby());
        values.put(MarrySocialDBHelper.KEY_GENDER, contact.getGender());
        values.put(MarrySocialDBHelper.KEY_ASTRO, contact.getAstro());
        values.put(MarrySocialDBHelper.KEY_DIRECT_FRIENDS_COUNT,
                contact.getDirectFriendsCount());
        values.put(MarrySocialDBHelper.KEY_FIRST_DIRECT_FRIEND,
                contact.getFirstDirectFriend());
        values.put(MarrySocialDBHelper.KEY_DIRECT_FRIENDS,
                contact.getDirectFriends());
        values.put(MarrySocialDBHelper.KEY_INDIRECT_ID, contact.getIndirectId());
        values.put(MarrySocialDBHelper.KEY_HEADPIC, contact.getHeadPic());

        mDBHelper.insert(MarrySocialDBHelper.DATABASE_CONTACTS_TABLE, values);
    }

    public boolean isContactExistInContactsDB(String uId) {
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_UID + " = "
                    + uId;
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
}
