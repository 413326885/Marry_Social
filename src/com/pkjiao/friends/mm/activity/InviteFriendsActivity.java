package com.pkjiao.friends.mm.activity;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import com.dhn.marrysocial.R;
import com.pkjiao.friends.mm.MarrySocialMainActivity;
import com.pkjiao.friends.mm.adapter.InviteFriendsExpandableListAdapter;
import com.pkjiao.friends.mm.adapter.InviteFriendsListAdapter;
import com.pkjiao.friends.mm.base.ChatMsgItem;
import com.pkjiao.friends.mm.base.ContactsInfo;
import com.pkjiao.friends.mm.common.CommonDataStructure;
import com.pkjiao.friends.mm.database.MarrySocialDBHelper;
import com.pkjiao.friends.mm.dialog.ProgressLoadDialog;
import com.pkjiao.friends.mm.pingyin.AssortView;
import com.pkjiao.friends.mm.pingyin.AssortView.OnTouchAssortButtonListener;
import com.pkjiao.friends.mm.roundedimageview.RoundedImageView;
import com.pkjiao.friends.mm.utils.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class InviteFriendsActivity extends Activity implements OnClickListener {

    private static final String TAG = "InviteFriendsActivity";

    private static final String[] DIRECT_PROJECTION = {
            MarrySocialDBHelper.KEY_PHONE_NUM,
            MarrySocialDBHelper.KEY_REALNAME,
            MarrySocialDBHelper.KEY_DIRECT_ID,
            MarrySocialDBHelper.KEY_DIRECT_UID };

    private static final int POOL_SIZE = 10;
    private static final int START_TO_UPLOAD_CONTACTS = 100;
    private static final int UPLOAD_CONTACTS_SUCCESS = 101;
    private static final int READ_USER_CONTACTS_FAIL = 102;

    private String mUid;
    private MarrySocialDBHelper mDBHelper;
    private ExecutorService mExecutorService;
    // private ProgressLoadDialog mUploadProgressDialog;

    private RelativeLayout mReturnBtn;
    private RelativeLayout mShareBtn;
    private View mInviteFriendsHeader;
    private TextView mInviteDescriptionBtn;
    private RelativeLayout mShareToFriendsBtn;
    private RelativeLayout mContactFriendsBtn;
    private ExpandableListView mListView;
    private Button mInviteFinishBtn;
    private InviteFriendsExpandableListAdapter mListAdapter;

    private AssortView mAssortView;
    private PopupWindow mPopupWindow;

    private ArrayList<ContactsInfo> mContactList = new ArrayList<ContactsInfo>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case START_TO_UPLOAD_CONTACTS: {
                // mUploadProgressDialog = ProgressDialog.show(
                // InviteFriendsActivity.this, "获取好友",
                // "正在为你计算好友信息，请稍后...", false, true);
                mExecutorService.execute(new UploadUserContacts(mUid));
                break;
            }
            case UPLOAD_CONTACTS_SUCCESS: {
                // mUploadProgressDialog.dismiss();
                loadContactsFromDirectDB();
                mListAdapter.notifyDataSetChanged();
                break;
            }
            case READ_USER_CONTACTS_FAIL: {
                // mUploadProgressDialog.dismiss();
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
        
        final Activity context = this;
        
        mReturnBtn = (RelativeLayout) findViewById(R.id.invite_friends_return);
        mListView = (ExpandableListView) findViewById(R.id.invite_friends_listView);
        mInviteFinishBtn = (Button) findViewById(R.id.invite_friends_finish);

        mReturnBtn.setOnClickListener(this);
        mInviteFinishBtn.setOnClickListener(this);

        mInviteFriendsHeader = (LayoutInflater.from(this).inflate(
                R.layout.invite_friends_header_layout, null, false));
        mInviteDescriptionBtn = (TextView) mInviteFriendsHeader
                .findViewById(R.id.invite_friends_desc);
        mShareToFriendsBtn = (RelativeLayout) mInviteFriendsHeader
                .findViewById(R.id.share_group);
        mContactFriendsBtn = (RelativeLayout) mInviteFriendsHeader
                .findViewById(R.id.contacts_group);
        mInviteDescriptionBtn.setOnClickListener(this);
        mShareToFriendsBtn.setOnClickListener(this);
        mContactFriendsBtn.setOnClickListener(this);

        mContactList.clear();
        mContactList.addAll(getAllContactsInfo());

        mListView.addHeaderView(mInviteFriendsHeader);
        mListAdapter = new InviteFriendsExpandableListAdapter(this);
        mListAdapter.setDataSource(mContactList);
        mListView.setAdapter(mListAdapter);

        mAssortView = (AssortView) findViewById(R.id.invite_friends_assort_view);
        mAssortView.setOnTouchAssortListener(new OnTouchAssortButtonListener() {

            View layoutView = LayoutInflater.from(context).inflate(
                    R.layout.contacts_list_show_assort_char_layout, null);
            TextView text = (TextView) layoutView
                    .findViewById(R.id.assort_char);

            public void onTouchAssortButtonListener(String str) {
                int index = mListAdapter.getAssort().getHashList()
                        .indexOfKey(str);
                if (index != -1) {
                    mListView.setSelectedGroup(index);
                }
                if (mPopupWindow != null) {
                    text.setText(str);
                } else {
                    mPopupWindow = new PopupWindow(layoutView, 160, 160, false);
                    mPopupWindow.showAtLocation(context.getWindow()
                            .getDecorView(), Gravity.CENTER, 0, 0);
                }
                text.setText(str);
            }

            public void onTouchAssortButtonUP() {
                if (mPopupWindow != null)
                    mPopupWindow.dismiss();
                mPopupWindow = null;
            }
        });

        SharedPreferences prefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        mUid = prefs.getString(CommonDataStructure.UID, "");
        mDBHelper = MarrySocialDBHelper.newInstance(this);
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);

        mHandler.sendEmptyMessage(START_TO_UPLOAD_CONTACTS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mListAdapter.notifyDataSetChanged();
        for (int i = 0, length = mListAdapter.getGroupCount(); i < length; i++) {
            mListView.expandGroup(i);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.invite_friends_return: {
            this.finish();
            break;
        }
        case R.id.invite_friends_desc:
        case R.id.contacts_group:
        case R.id.share_group: {
            break;
        }
        case R.id.invite_friends_finish: {
            SharedPreferences prefs = InviteFriendsActivity.this
                    .getSharedPreferences(
                            CommonDataStructure.PREFS_LAIQIAN_DEFAULT,
                            MODE_PRIVATE);
            Editor editor = prefs.edit();
            editor.putInt(CommonDataStructure.LOGINSTATUS,
                    CommonDataStructure.LOGIN_STATUS_LOGIN);
            editor.commit();
            redirectToMainActivity();
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

            ArrayList<ContactsInfo> contacts = new ArrayList<ContactsInfo>();
            contacts.addAll(getAllContactsInfo());

            if (contacts == null || contacts.size() == 0) {
                mHandler.sendEmptyMessage(READ_USER_CONTACTS_FAIL);
                return;
            }
            // for (CommonDataStructure.ContactEntry entry : contacts) {
            // if (!isPhoneNumExistInDirectDB(entry.contact_phone_number)) {
            // insertContactsToDirectDB(entry);
            // }
            // }
            ArrayList<ContactsInfo> resultEntry = Utils.uploadUserContacts(
                    CommonDataStructure.URL_UPLOAD_CONTACTS, uid, contacts);
            if (resultEntry == null || resultEntry.size() == 0) {
                return;
            }
            for (ContactsInfo entry : resultEntry) {
                if (!isPhoneNumExistInDirectDB(entry.getPhoneNum())) {
                    insertContactsToDirectDB(entry);
                } else {
                    updateDirectIdToDirectDB(entry);
                }
            }

            mHandler.sendEmptyMessage(UPLOAD_CONTACTS_SUCCESS);
        }

    }

    private ArrayList<ContactsInfo> getAllContactsInfo() {
        ArrayList<ContactsInfo> contactMembers = new ArrayList<ContactsInfo>();
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
                ContactsInfo contact = new ContactsInfo();
                String name = cursor.getString(0);
                String contact_phone = cursor
                        .getString(cursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                contact.setNickName(name);
                contact.setPhoneNum(contact_phone);
                // if (name != null && isPhoneNumber(contact_phone)) {
                contactMembers.add(contact);
                // }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return contactMembers;
    }

    // private static String getSortKey(String sortKeyString) {
    // String key = sortKeyString.substring(0, 1).toUpperCase();
    // if (key.matches("[A-Z]")) {
    // return key;
    // }
    // return "#";
    // }
    //
    // private static boolean isPhoneNumber(String input) {
    //
    // if (input == null) {
    // return false;
    // }
    //
    // String regex =
    // "1([\\d]{10})|((\\+[0-9]{2,4})?\\(?[0-9]+\\)?-?)?[0-9]{7,8}";
    // Pattern p = Pattern.compile(regex);
    // return p.matcher(input).matches();
    // }

    private void insertContactsToDirectDB(ContactsInfo contact) {

        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_PHONE_NUM,
                contact.getPhoneNum());
        insertValues.put(MarrySocialDBHelper.KEY_REALNAME,
                contact.getNickName());
        insertValues.put(MarrySocialDBHelper.KEY_DIRECT_ID,
                contact.getDirectId());
        insertValues.put(MarrySocialDBHelper.KEY_DIRECT_UID, contact.getUid());

        try {
            mDBHelper.insert(MarrySocialDBHelper.DATABASE_DIRECT_TABLE,
                    insertValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDirectIdToDirectDB(ContactsInfo contact) {

        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_PHONE_NUM, contact.getPhoneNum());
        values.put(MarrySocialDBHelper.KEY_REALNAME, contact.getNickName());
        values.put(MarrySocialDBHelper.KEY_DIRECT_ID, contact.getDirectId());
        values.put(MarrySocialDBHelper.KEY_DIRECT_UID, contact.getUid());

        String whereClause = MarrySocialDBHelper.KEY_PHONE_NUM + " = " + '"'
                + contact.getPhoneNum() + '"';
        try {
            mDBHelper.update(MarrySocialDBHelper.DATABASE_DIRECT_TABLE, values,
                    whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                    DIRECT_PROJECTION, null, null, null, null, null, null);

            if (cursor == null) {
                Log.e(TAG,
                        "nannan loadContactsFromDirectDB()..  cursor == null");
                return;
            }

            while (cursor.moveToNext()) {
                ContactsInfo item = new ContactsInfo();
                item.setPhoneNum(cursor.getString(0));
                item.setNickName(cursor.getString(1));
                item.setDirectId(cursor.getString(2));
                item.setUid(cursor.getString(3));
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

    private void redirectToMainActivity() {
        Intent intent = new Intent(this, MarrySocialMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
