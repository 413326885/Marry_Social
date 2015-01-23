package com.dhn.marrysocial.activity;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.adapter.InviteFriendsListAdapter;
import com.dhn.marrysocial.base.ChatMsgItem;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.utils.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class InviteFriendsActivity extends Activity implements OnClickListener {

    private static final String TAG = "InviteFriendsActivity";

    private static final String[] DIRECT_PROJECTION = {
        MarrySocialDBHelper.KEY_PHONE_NUM,
        MarrySocialDBHelper.KEY_REALNAME,
        MarrySocialDBHelper.KEY_DIRECT_ID,
        MarrySocialDBHelper.KEY_DIRECT_UID
        };

    private static final int POOL_SIZE = 10;
    private static final int START_TO_UPLOAD_CONTACTS = 100;
    private static final int UPLOAD_CONTACTS_SUCCESS = 101;
    private static final int READ_USER_CONTACTS_FAIL = 102;

    private String mUid;
    private MarrySocialDBHelper mDBHelper;
    private ExecutorService mExecutorService;
    private ProgressDialog mUploadProgressDialog;

    private RelativeLayout mReturnBtn;
    private RelativeLayout mShareBtn;
    private ListView mListView;
    private InviteFriendsListAdapter mListAdapter;
    
    private ArrayList<CommonDataStructure.ContactEntry> mContactList = new ArrayList<CommonDataStructure.ContactEntry>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case START_TO_UPLOAD_CONTACTS: {
                mUploadProgressDialog = ProgressDialog.show(
                        InviteFriendsActivity.this, "获取好友",
                        "正在为你计算好友信息，请稍后...", false, true);
                mExecutorService.execute(new UploadUserContacts(mUid));
                break;
            }
            case UPLOAD_CONTACTS_SUCCESS: {
                mUploadProgressDialog.dismiss();
                loadContactsFromDirectDB();
                mListAdapter.notifyDataSetChanged();
                break;
            }
            case READ_USER_CONTACTS_FAIL: {
                mUploadProgressDialog.dismiss();
                Toast.makeText(InviteFriendsActivity.this, "读取通讯录失败", 1000)
                        .show();
                break;
            }
            default:
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.invite_friends_layout);
        mReturnBtn = (RelativeLayout) findViewById(R.id.invite_friends_return);
        mShareBtn = (RelativeLayout) findViewById(R.id.share_group);
        mListView = (ListView) findViewById(R.id.invite_friends_listView);

        mReturnBtn.setOnClickListener(this);
        mShareBtn.setOnClickListener(this);

        mListAdapter = new InviteFriendsListAdapter(this);
        mListAdapter.setDataSource(mContactList);
        mListView.setAdapter(mListAdapter);

        SharedPreferences prefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        mUid = prefs.getString(CommonDataStructure.UID, "");
        mDBHelper = MarrySocialDBHelper.newInstance(this);
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);

        mContactList.clear();
        mContactList.addAll(getAllContactsInfo());
        mListAdapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.invite_friends_return: {
            this.finish();
            break;
        }
        case R.id.share_group: {
            break;
        }
        default:
            break;
        }
    }

    class UploadUserContacts implements Runnable {

        private String uid;

        public UploadUserContacts(String uid) {
            this.uid = uid;
        }

        @Override
        public void run() {
            ArrayList<CommonDataStructure.ContactEntry> contacts = getAllContactsInfo();
            if (contacts == null || contacts.size() == 0) {
                mHandler.sendEmptyMessage(READ_USER_CONTACTS_FAIL);
                return;
            }
//            for (CommonDataStructure.ContactEntry entry : contacts) {
//                if (!isPhoneNumExistInDirectDB(entry.contact_phone_number)) {
//                    insertContactsToDirectDB(entry);
//                }
//            }
            ArrayList<CommonDataStructure.ContactEntry> resultEntry = Utils
                    .uploadUserContacts(
                            CommonDataStructure.URL_UPLOAD_CONTACTS, uid,
                            contacts);
            if (resultEntry == null || resultEntry.size() == 0) {
                return;
            }
            for (CommonDataStructure.ContactEntry entry : resultEntry) {
                if (!isPhoneNumExistInDirectDB(entry.contact_phone_number)) {
                    insertContactsToDirectDB(entry);
                } else {
                    updateDirectIdToDirectDB(entry);
                }
            }

            mHandler.sendEmptyMessage(UPLOAD_CONTACTS_SUCCESS);
        }

    }

    private ArrayList<CommonDataStructure.ContactEntry> getAllContactsInfo() {
        ArrayList<CommonDataStructure.ContactEntry> contactMembers = new ArrayList<CommonDataStructure.ContactEntry>();
        Cursor cursor = null;

        try {
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            // 获取联系人表的电话里的信息 包括：名字，名字拼音，联系人id,电话号码；
            // 然后在根据"sort-key"排序
            cursor = this.getContentResolver().query(
                    uri,
                    new String[] { "display_name", "sort_key", "contact_id",
                            "data1" }, null, null, "sort_key");

            while (cursor.moveToNext()) {
                CommonDataStructure.ContactEntry contact = new CommonDataStructure.ContactEntry();
                String name = cursor.getString(0);
                String sortKey = getSortKey(cursor.getString(1));
                String contact_phone = cursor
                        .getString(cursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int contact_id = cursor
                        .getInt(cursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                contact.contact_name = name;
                contact.contact_sortKey = sortKey;
                contact.contact_phone_number = contact_phone;
                contact.contact_id = contact_id;
                if (name != null && isPhoneNumber(contact_phone)) {
                    contactMembers.add(contact);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return contactMembers;
    }

    private static String getSortKey(String sortKeyString) {
        String key = sortKeyString.substring(0, 1).toUpperCase();
        if (key.matches("[A-Z]")) {
            return key;
        }
        return "#";
    }

    private static boolean isPhoneNumber(String input) {

        if (input == null) {
            return false;
        }

        String regex = "1([\\d]{10})|((\\+[0-9]{2,4})?\\(?[0-9]+\\)?-?)?[0-9]{7,8}";
        Pattern p = Pattern.compile(regex);
        return p.matcher(input).matches();
    }

    private void insertContactsToDirectDB(
            CommonDataStructure.ContactEntry contact) {

        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_PHONE_NUM,
                contact.contact_phone_number);
        insertValues.put(MarrySocialDBHelper.KEY_REALNAME, contact.contact_name);
        insertValues.put(MarrySocialDBHelper.KEY_DIRECT_ID, contact.direct_id);
        insertValues.put(MarrySocialDBHelper.KEY_DIRECT_UID, contact.direct_uid);

        mDBHelper.insert(MarrySocialDBHelper.DATABASE_DIRECT_TABLE,
                insertValues);
    }

    private void updateDirectIdToDirectDB(
            CommonDataStructure.ContactEntry contact) {

        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_PHONE_NUM,
                contact.contact_phone_number);
        values.put(MarrySocialDBHelper.KEY_REALNAME, contact.contact_name);
        values.put(MarrySocialDBHelper.KEY_DIRECT_ID, contact.direct_id);
        values.put(MarrySocialDBHelper.KEY_DIRECT_UID, contact.direct_uid);

        String whereClause = MarrySocialDBHelper.KEY_PHONE_NUM + " = " + '"'
                + contact.contact_phone_number + '"';
        mDBHelper.update(MarrySocialDBHelper.DATABASE_DIRECT_TABLE, values,
                whereClause, null);
    }

    public boolean isPhoneNumExistInDirectDB(String phone) {
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_PHONE_NUM + " = "
                    + '"' + phone + '"';
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_DIRECT_TABLE,
                    DIRECT_PROJECTION, whereclause, null, null, null, null,
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

    private void loadContactsFromDirectDB() {

        mContactList.clear();
        Cursor cursor = null;

        try {
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_DIRECT_TABLE,
                    DIRECT_PROJECTION, null, null, null, null, null,
                    null);

            if (cursor == null) {
                Log.e(TAG, "nannan loadContactsFromDirectDB()..  cursor == null");
                return;
            }

            while (cursor.moveToNext()) {
                CommonDataStructure.ContactEntry item = new CommonDataStructure.ContactEntry();
                item.contact_phone_number = cursor.getString(0);
                item.contact_name = cursor.getString(1);
                item.direct_id = cursor.getString(2);
                item.direct_uid = cursor.getString(3);
                mContactList.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
