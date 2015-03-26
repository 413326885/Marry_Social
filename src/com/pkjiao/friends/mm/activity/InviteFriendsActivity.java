package com.pkjiao.friends.mm.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import com.pkjiao.friends.mm.R;
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
import com.pkjiao.friends.mm.share.CallbackListener;
import com.pkjiao.friends.mm.share.Constants;
import com.pkjiao.friends.mm.share.MsgImage;
import com.pkjiao.friends.mm.share.MsgImageText;
import com.pkjiao.friends.mm.share.PType;
import com.pkjiao.friends.mm.share.ShareManager;
import com.pkjiao.friends.mm.utils.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
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
            MarrySocialDBHelper.KEY_NICKNAME,
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

    private View mSharePopupView;
    private AssortView mAssortView;
    private PopupWindow mAssortPopupWindow;
    private PopupWindow mSharePopupWindow;

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
        SharedPreferences prefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        mUid = prefs.getString(CommonDataStructure.UID, "");
        mDBHelper = MarrySocialDBHelper.newInstance(this);
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);

        mReturnBtn = (RelativeLayout) findViewById(R.id.invite_friends_return);
        mListView = (ExpandableListView) findViewById(R.id.invite_friends_listView);
        mInviteFinishBtn = (Button) findViewById(R.id.invite_friends_finish);
        mSharePopupView = (View) findViewById(R.id.popup_masker);

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
        mContactList.addAll(getNeedInviteContacts());

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
                if (mAssortPopupWindow != null) {
                    text.setText(str);
                } else {
                    int width = Utils.dp2px(InviteFriendsActivity.this, 80);
                    int height = Utils.dp2px(InviteFriendsActivity.this, 80);
                    mAssortPopupWindow = new PopupWindow(layoutView, width, height,
                            false);
                    mAssortPopupWindow.showAtLocation(context.getWindow()
                            .getDecorView(), Gravity.CENTER, 0, 0);
                }
                text.setText(str);
            }

            public void onTouchAssortButtonUP() {
                if (mAssortPopupWindow != null)
                    mAssortPopupWindow.dismiss();
                mAssortPopupWindow = null;
            }
        });

        mHandler.sendEmptyMessage(START_TO_UPLOAD_CONTACTS);

        initShare();
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
        case R.id.contacts_group: {
            break;
        }
        case R.id.share_group: {
            showSharePopUpWindow();
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

    private ArrayList<ContactsInfo> getNeedInviteContacts() {
        ArrayList<ContactsInfo> contactMembers = new ArrayList<ContactsInfo>();
        Cursor cursor = null;

        try {
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_DIRECT_TABLE,
                    DIRECT_PROJECTION, null, null, null, null, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                contactMembers.addAll(getAllContactsInfo());
                return contactMembers;
            }

            while (cursor.moveToNext()) {
                ContactsInfo contact = new ContactsInfo();
                String phone_num = cursor.getString(0);
                String nickname = cursor.getString(1);
                String direct_id = cursor.getString(2);
                String direct_uid = cursor.getString(3);
                contact.setNickName(nickname);
                contact.setPhoneNum(phone_num);
                contact.setDirectId(direct_id);
                contact.setUid(direct_uid);
                contactMembers.add(contact);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return contactMembers;
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
        insertValues.put(MarrySocialDBHelper.KEY_NICKNAME,
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
        values.put(MarrySocialDBHelper.KEY_NICKNAME, contact.getNickName());
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

    private void showSharePopUpWindow() {
        View v = LayoutInflater.from(this).inflate(R.layout.share_popup, null);
        View ly1 = v.findViewById(R.id.share_ly_1);
        View ly2 = v.findViewById(R.id.share_ly_2);
        View ly3 = v.findViewById(R.id.share_ly_3);
        View ly4 = v.findViewById(R.id.share_ly_4);
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                case R.id.share_ly_1:// 分享微信好友
                    sendShare(PType.PLATFORM_WX);
                    break;
                case R.id.share_ly_2:// 分享微信朋友圈.
                    sendShare(PType.PLATFORM_WX_friends);
                    break;
                case R.id.share_ly_3:// 分享QQ空间
                    sendShare(PType.PLATFORM_QQzone);
                    break;
                case R.id.share_ly_4:// 分享QQ好友
                    sendShare(PType.PLATFORM_QQ);
                    break;
                }
                mSharePopupWindow.dismiss();
            }
        };
        ly1.setOnClickListener(listener);
        ly2.setOnClickListener(listener);
        ly3.setOnClickListener(listener);
        ly4.setOnClickListener(listener);
        mSharePopupWindow = new PopupWindow(v, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT, true);
        mSharePopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mSharePopupWindow.setOutsideTouchable(false); // 设置是否允许在外点击使其消失，到底有用没？
        mSharePopupWindow.setAnimationStyle(R.style.AnimationSharePopup); // 设置动画
        final Animation dismissA = AnimationUtils.loadAnimation(this,
                R.anim.popwindow_masker_dismiss);
        Animation showA = AnimationUtils.loadAnimation(this,
                R.anim.popwindow_masker_show);
        dismissA.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mSharePopupView.setVisibility(View.GONE);
            }
        });
        mSharePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        mSharePopupView.startAnimation(dismissA);
                    }
                });
            }
        });
        mSharePopupView.setVisibility(View.VISIBLE);
        mSharePopupView.startAnimation(showA);
        mSharePopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    private void initShare() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(ShareManager.KEY_APPID_QQ, Constants.QQ_APP_ID);
        map.put(ShareManager.KEY_APPID_WX, Constants.WX_APP_ID);
        ShareManager.init(this, map);
    }

    private void sendShare(int pType) {
        MsgImageText msg = new MsgImageText();
        msg.appName = getString(R.string.app_name);
        msg.pType = pType;
        msg.title = "nannan title";
        msg.summary = "nannan summary";
        msg.targetUrl = "http://www.baidu.com";
        // msg.imagePath = handleBitmap();
        msg.share(new CallbackListener() {
            @Override
            public void onSuccess() {
                showToast(InviteFriendsActivity.this, "分享成功");
            }

            @Override
            public void onFailure() {
                showToast(InviteFriendsActivity.this, "分享失败");
            }

            @Override
            public void onCancel() {
                // showToast(ImageEditActivity.this,"取消分享");
            }
        });
    }

    Toast mToast;

    public void showToast(Context context, String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, str, Toast.LENGTH_LONG);
        mToast.show();
    }
}
