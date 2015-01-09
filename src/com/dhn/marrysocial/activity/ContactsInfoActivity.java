package com.dhn.marrysocial.activity;

import java.util.ArrayList;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.base.ContactsInfo;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.roundedimageview.RoundedImageView;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

public class ContactsInfoActivity extends Activity implements OnClickListener {

    private static final String TAG = "ContactsInfoActivity";

    private static final String[] PROJECTION = { MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_PHONE_NUM,
            MarrySocialDBHelper.KEY_NIKENAME, MarrySocialDBHelper.KEY_REALNAME,
            MarrySocialDBHelper.KEY_FIRST_DIRECT_FRIEND,
            MarrySocialDBHelper.KEY_DIRECT_FRIENDS,
            MarrySocialDBHelper.KEY_INDIRECT_ID,
            MarrySocialDBHelper.KEY_DIRECT_FRIENDS_COUNT,
            MarrySocialDBHelper.KEY_HEADPIC, MarrySocialDBHelper.KEY_GENDER,
            MarrySocialDBHelper.KEY_ASTRO, MarrySocialDBHelper.KEY_HOBBY };

    public enum GENDER{
        FAMALE, //女孩
        MALE,   //男孩
    }

    private String mUid;
    private ContactsInfo mUserInfo;
    private ListView mListView;

    private RelativeLayout mReturnBtn;
    private TextView mUserName;
    private TextView mFriendName;
    private RoundedImageView mUserPic;
    private TextView mFriendsDesc;
    private ImageView mUserGender;
    private ImageView mUserAstro;
    private ImageView mUserHobby;
    private Button mChatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.contacts_info_layout);

        Intent data = getIntent();
        mUid = data.getStringExtra(MarrySocialDBHelper.KEY_UID);
        mUserInfo = loadUserInfoFromDB(mUid);

        mReturnBtn = (RelativeLayout) findViewById(R.id.contacts_info_return);
        mUserName = (TextView) findViewById(R.id.contacts_info_person_name);
        mFriendName = (TextView) findViewById(R.id.contacts_info_friend_name);
        mUserPic = (RoundedImageView) findViewById(R.id.chat_msg_person_pic);
        mFriendsDesc = (TextView) findViewById(R.id.contacts_info_friends_description);
        mUserGender = (ImageView) findViewById(R.id.contacts_info_gender_pic);
        mUserAstro = (ImageView) findViewById(R.id.contacts_info_astro_pic);
        mUserHobby = (ImageView) findViewById(R.id.contacts_info_hobby_pic);
        mChatButton = (Button) findViewById(R.id.contacts_info_chat_btn);

        mReturnBtn.setOnClickListener(this);
        mUserPic.setOnClickListener(this);
        mChatButton.setOnClickListener(this);

        mUserName.setText(mUserInfo.getNikeName());
        mFriendName.setText(mUserInfo.getNikeName());
        String friendsDesc = String.format(
                this.getString(R.string.chat_msg_friends_more),
                mUserInfo.getFirstDirectFriend(),
                mUserInfo.getDirectFriendsCount());
        mFriendsDesc.setText(friendsDesc);

//        if (mUserInfo.getGender() == GENDER.FAMALE.) {
//            
//        }
    }

    private ContactsInfo loadUserInfoFromDB(String uid) {

        ContactsInfo userInfo = new ContactsInfo();

        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + uid;
        MarrySocialDBHelper dbHelper = MarrySocialDBHelper.newInstance(this);
        Cursor cursor = dbHelper.query(
                MarrySocialDBHelper.DATABASE_CONTACTS_TABLE, PROJECTION,
                whereClause, null, null, null, null, null);
        if (cursor == null) {
            Log.w(TAG, "nannan query fail!");
            return null;
        }

        try {
            cursor.moveToFirst();

            String phoneNum = cursor.getString(1);
            String nickname = cursor.getString(2);
            String realname = cursor.getString(3);
            String firstDirectFriend = cursor.getString(4);
            String directFriends = cursor.getString(5);
            String indirectId = cursor.getString(6);
            int directFriendsCount = cursor.getInt(7);
            int avatar = Integer.valueOf(cursor.getInt(8));
            int gender = Integer.valueOf(cursor.getInt(9));
            int astro = Integer.valueOf(cursor.getInt(10));
            int hobby = Integer.valueOf(cursor.getInt(11));

            userInfo.setUid(uid);
            userInfo.setPhoneNum(phoneNum);
            userInfo.setNikeName(nickname);
            userInfo.setRealName(realname);
            userInfo.setHeadPic(avatar);
            userInfo.setGender(gender);
            userInfo.setAstro(astro);
            userInfo.setHobby(hobby);
            userInfo.setIndirectId(indirectId);
            userInfo.setFirstDirectFriend(firstDirectFriend);
            userInfo.setDirectFriends(directFriends);
            userInfo.setDirectFriendsCount(directFriendsCount);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return userInfo;
    }

    @Override
    public void onClick(View arg0) {
        switch(arg0.getId()) {
        case R.id.contacts_info_return: {
            this.finish();
            break;
        }
        case R.id.chat_msg_person_pic: {
            break;
        }
        case R.id.contacts_info_chat_btn: {
            break;
        }
        default:
            break;
        }
    }
}
