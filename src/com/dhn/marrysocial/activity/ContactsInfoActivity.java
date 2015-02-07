package com.dhn.marrysocial.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.adapter.DynamicInfoListAdapter;
import com.dhn.marrysocial.adapter.DynamicInfoListAdapter.onReplyBtnClickedListener;
import com.dhn.marrysocial.base.CommentsItem;
import com.dhn.marrysocial.base.ContactsInfo;
import com.dhn.marrysocial.base.ReplysItem;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.common.CommonDataStructure.HeaderBackgroundEntry;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.roundedimageview.RoundedImageView;
import com.dhn.marrysocial.services.UploadCommentsAndBravosAndReplysIntentService;
import com.dhn.marrysocial.utils.ImageUtils;
import com.dhn.marrysocial.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.FeatureInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

public class ContactsInfoActivity extends Activity implements OnClickListener {

    private static final String TAG = "ContactsInfoActivity";

    private static final int TAKE_PICTURE_FROM_CAMERA = 0;
    private static final int CHOOSE_PICTURE_FROM_GALLERY = 1;
    private static final int NEED_CROP = 2;
    private static final int CROP_PICTURE = 3;
    private static final int CHOOSE_BACKGROUND_PICTURE = 4;

    private static final int CHANGE_HEAD_BACKGROUND = 0;

    private static final int POOL_SIZE = 10;
    private static final int START_TO_UPLOAD = 100;
    private static final int UPLOAD_FINISH = 101;
    private static final int RELOAD_DATA_SOURCE = 102;
    private static final int DOWNLOAD_HEADER_BKG_FINISH = 103;
    private final static int START_TO_LOAD_BRAVO_REPLY = 104;
    private final static int UPDATE_DYNAMIC_INFO = 105;
    private final static int NETWORK_INVALID = 106;
    private final static int SEND_REPLY_FINISH = 107;
    private final static int SHOW_SOFT_INPUT_METHOD = 108;
    private final static int SELECT_SPECIFIED_LIST_ITEM = 109;
    private final static int TOUCH_FING_UP = 110;
    private final static int TOUCH_FING_DOWN = 111;
    private final static int REFRESH_HEADER_PIC = 112;
    private final static int UPLOAD_COMMENT = 113;

    private float mTouchDownY = 0.0f;
    private float mTouchMoveY = 0.0f;
    private boolean mIsFingUp = false;

    private static final String[] CONTACTS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_PHONE_NUM,
            MarrySocialDBHelper.KEY_NICKNAME, MarrySocialDBHelper.KEY_REALNAME,
            MarrySocialDBHelper.KEY_FIRST_DIRECT_FRIEND,
            MarrySocialDBHelper.KEY_DIRECT_FRIENDS,
            MarrySocialDBHelper.KEY_INDIRECT_ID,
            MarrySocialDBHelper.KEY_DIRECT_FRIENDS_COUNT,
            MarrySocialDBHelper.KEY_HEADPIC, MarrySocialDBHelper.KEY_GENDER,
            MarrySocialDBHelper.KEY_ASTRO, MarrySocialDBHelper.KEY_HOBBY,
            MarrySocialDBHelper.KEY_HEADER_BACKGROUND_INDEX,
            MarrySocialDBHelper.KEY_INTRODUCT };

    private final String[] COMMENTS_PROJECTION = { MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_BUCKET_ID,
            MarrySocialDBHelper.KEY_CONTENTS,
            MarrySocialDBHelper.KEY_AUTHOR_NICKNAME,
            MarrySocialDBHelper.KEY_PHOTO_COUNT,
            MarrySocialDBHelper.KEY_BRAVO_STATUS,
            MarrySocialDBHelper.KEY_ADDED_TIME,
            MarrySocialDBHelper.KEY_COMMENT_ID };

    private static final String[] HEAD_PICS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_HEAD_PIC_BITMAP,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_THUMB_PATH };

    private static final String[] HEAD_BACKGROUND_PROJECTION = {
            MarrySocialDBHelper.KEY_PHOTO_NAME,
            MarrySocialDBHelper.KEY_PHOTO_LOCAL_PATH,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH,
            MarrySocialDBHelper.KEY_HEADER_BACKGROUND_INDEX };

    private static final String[] HEAD_BKG_PROJECTION = {
            MarrySocialDBHelper.KEY_PHOTO_NAME,
            MarrySocialDBHelper.KEY_PHOTO_LOCAL_PATH,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH,
            MarrySocialDBHelper.KEY_CURRENT_STATUS };

    private final String[] BRAVOS_PROJECTION = { MarrySocialDBHelper.KEY_ID,
            MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_AUTHOR_NICKNAME };

    private final String[] REPLYS_PROJECTION = { MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_AUTHOR_NICKNAME,
            MarrySocialDBHelper.KEY_REPLY_CONTENTS,
            MarrySocialDBHelper.KEY_ADDED_TIME };
    private String mUserInfoUid;
    private ContactsInfo mUserInfo;

    private String mAuthorUid;
    private String mAuthorName;

    private ListView mListView;
    private DynamicInfoListAdapter mListViewAdapter;
    private ArrayList<CommentsItem> mCommentEntrys = new ArrayList<CommentsItem>();
    private HashMap<String, String> mBravoEntrys = new HashMap<String, String>();
    private HashMap<String, ArrayList<ReplysItem>> mReplyEntrys = new HashMap<String, ArrayList<ReplysItem>>();
    private HashMap<String, ContactsInfo> mUserInfoEntrys = new HashMap<String, ContactsInfo>();

    private RelativeLayout mReturnBtn;
    private RelativeLayout mHeaderLayout;
    private LinearLayout mHeaderLayout01;
    private TextView mUserName;
    private TextView mFriendName;
    private RoundedImageView mUserPic;
    private TextView mFriendsDesc;
    private ImageView mUserGender;
    private ImageView mUserAstro;
    private ImageView mUserHobby;
    private Button mChatButton;

    private RelativeLayout mReplyFoot;
    private ImageView mReplySendBtn;
    private EditText mReplyContents;
    private int mReplyCommentsPosition;

    private TranslateAnimation mHideChatBtnTransAnimation;
    private TranslateAnimation mShowChatBtnTransAnimation;

    private MarrySocialDBHelper mDBHelper;
    private ExecutorService mExecutorService;

    private Bitmap mUserHeadPic = null;
    private Bitmap mCropPhoto = null;
    private String mCropPhotoName;

    private DataSetChangeObserver mChangeObserver;
    private DataSetChangeObserver mHeaderPicChangeObserver;
    private ProgressDialog mUploadProgressDialog;

    private View mContactsInfoHeader;
    private int mContactsInfoHeaderWidth;
    private int mContactsInfoHeaderHeight;
    private String mHeadBkgPath;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case TOUCH_FING_UP: {
                hideChatBtn();
                hideReplyFootBar();
                Utils.hideSoftInputMethod(mReplyFoot);
                break;
            }

            case TOUCH_FING_DOWN: {
                showChatBtn();
                hideReplyFootBar();
                Utils.hideSoftInputMethod(mReplyFoot);
                break;
            }
            case START_TO_UPLOAD: {
                mUploadProgressDialog = ProgressDialog.show(
                        ContactsInfoActivity.this, "更改头像", "正在上传头像，请稍后...",
                        false, true);
                mExecutorService.execute(new UploadHeadPics(mUserInfoUid));
                break;
            }
            case UPLOAD_FINISH: {
                mUserHeadPic = mCropPhoto;
                mUserPic.setImageBitmap(mUserHeadPic);
                mUploadProgressDialog.dismiss();
                break;
            }
            case REFRESH_HEADER_PIC: {
                mListViewAdapter.clearHeadPicsCache();
                mListViewAdapter.notifyDataSetChanged();
                break;
            }
            case DOWNLOAD_HEADER_BKG_FINISH: {
                Bitmap thumbHeader = Utils.decodeThumbnail(mHeadBkgPath, null,
                        Utils.mThumbPhotoWidth);
                Bitmap cropHeader = Utils.cropImages(thumbHeader,
                        mContactsInfoHeaderWidth, mContactsInfoHeaderHeight,
                        true);
                mHeaderLayout.setBackground(ImageUtils
                        .bitmapToDrawable(cropHeader));
            }
            case START_TO_LOAD_BRAVO_REPLY: {
                if (mCommentEntrys != null && mCommentEntrys.size() != 0) {
                    for (CommentsItem comment : mCommentEntrys) {
                        mExecutorService.execute(new LoadBravoAndReplyContents(
                                comment.getCommentId()));
                    }
                }
                break;
            }
            case UPDATE_DYNAMIC_INFO: {
                mListViewAdapter.notifyDataSetChanged();
                break;
            }
            case NETWORK_INVALID: {
                Toast.makeText(ContactsInfoActivity.this,
                        R.string.network_not_available, Toast.LENGTH_SHORT)
                        .show();
                break;
            }
            case SEND_REPLY_FINISH: {
                mReplyContents.setText(null);
                showChatBtn();
                hideReplyFootBar();
                Utils.hideSoftInputMethod(mReplyFoot);
                break;
            }
            case SHOW_SOFT_INPUT_METHOD: {
                Utils.showSoftInputMethod(mReplyContents);
                break;
            }
            case SELECT_SPECIFIED_LIST_ITEM: {
                mListView.setSelection(msg.arg1);
                break;
            }
            case UPLOAD_COMMENT: {
                // uploadCommentsOrBravosOrReplys(CommonDataStructure.KEY_COMMENTS);
                mCommentEntrys.clear();
                mCommentEntrys.addAll(loadUserCommentsFromDB(mUserInfoUid));
                mListViewAdapter.notifyDataSetChanged();
                Log.e(TAG, "nannan UPLOAD_COMMENT..");
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
        setContentView(R.layout.contacts_info_layout);

        mShowChatBtnTransAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 2.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        mShowChatBtnTransAnimation
                .setInterpolator(new AccelerateDecelerateInterpolator());
        mShowChatBtnTransAnimation.setDuration(500);
        mShowChatBtnTransAnimation.setFillAfter(true);

        mHideChatBtnTransAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 2.0f);
        mHideChatBtnTransAnimation
                .setInterpolator(new AccelerateDecelerateInterpolator());
        mHideChatBtnTransAnimation.setDuration(500);
        mHideChatBtnTransAnimation.setFillAfter(true);

        mDBHelper = MarrySocialDBHelper.newInstance(this);

        generateDBData();
        loadContactsFromDB();

        Intent data = getIntent();
        mUserInfoUid = data.getStringExtra(MarrySocialDBHelper.KEY_UID);
        mUserInfo = mUserInfoEntrys.get(mUserInfoUid);
        mUserHeadPic = loadUserHeadPicFromDB(mUserInfoUid);

        mReturnBtn = (RelativeLayout) findViewById(R.id.contacts_info_return);
        mUserName = (TextView) findViewById(R.id.contacts_info_person_name);
        mChatButton = (Button) findViewById(R.id.contacts_info_chat_btn);

        mReplyFoot = (RelativeLayout) findViewById(R.id.contacts_info_reply_foot);
        mReplySendBtn = (ImageView) findViewById(R.id.contacts_info_reply_send);
        mReplyContents = (EditText) findViewById(R.id.contacts_info_reply_contents);
        mReplySendBtn.setOnClickListener(mReplySendBtnClickedListener);

        mContactsInfoHeader = (LayoutInflater.from(this).inflate(
                R.layout.contacts_info_header_layout, null, false));
        mHeaderLayout = (RelativeLayout) mContactsInfoHeader
                .findViewById(R.id.contacts_info_head);
        mHeaderLayout01 = (LinearLayout) mContactsInfoHeader
                .findViewById(R.id.contacts_info_head_01);
        mUserPic = (RoundedImageView) mContactsInfoHeader
                .findViewById(R.id.chat_msg_person_pic);
        mFriendName = (TextView) mContactsInfoHeader
                .findViewById(R.id.contacts_info_friend_name);
        mFriendsDesc = (TextView) mContactsInfoHeader
                .findViewById(R.id.contacts_info_friends_description);
        mUserGender = (ImageView) mContactsInfoHeader
                .findViewById(R.id.contacts_info_gender_pic);
        mUserAstro = (ImageView) mContactsInfoHeader
                .findViewById(R.id.contacts_info_astro_pic);
        mUserHobby = (ImageView) mContactsInfoHeader
                .findViewById(R.id.contacts_info_hobby_pic);

        mListView = (ListView) findViewById(R.id.contacts_info_listview);
        mListView.addHeaderView(mContactsInfoHeader);
        mListViewAdapter = new DynamicInfoListAdapter(this);
        mListViewAdapter.setCommentDataSource(mCommentEntrys);
        mListViewAdapter.setBravoDataSource(mBravoEntrys);
        mListViewAdapter.setReplyDataSource(mReplyEntrys);
        mListViewAdapter.setUserInfoDataSource(mUserInfoEntrys);
        mListViewAdapter.setReplyBtnClickedListener(mReplyBtnClickedListener);
        mListViewAdapter.setEnterInContactsInfoActivity(true);
        mListView.setAdapter(mListViewAdapter);

        mListView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTouchDownY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mTouchMoveY = event.getY() - mTouchDownY;
                    if (Math.abs(mTouchMoveY) < 50) {
                        break;
                    }
                    if (mTouchMoveY < 0) {
                        if (!mIsFingUp) {
                            mIsFingUp = true;
                            mHandler.sendEmptyMessageDelayed(TOUCH_FING_UP, 50);
                        }
                    } else {
                        if (mIsFingUp) {
                            mIsFingUp = false;
                            mHandler.sendEmptyMessageDelayed(TOUCH_FING_DOWN,
                                    50);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    break;
                }
                return false;
            }
        });

        SharedPreferences prefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        mAuthorUid = prefs.getString(CommonDataStructure.UID, "");
        mAuthorName = prefs.getString(CommonDataStructure.AUTHOR_NAME, "");
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);

        initOriginData();

        mChangeObserver = new DataSetChangeObserver(mHandler, UPLOAD_COMMENT);
        mHeaderPicChangeObserver = new DataSetChangeObserver(mHandler,
                REFRESH_HEADER_PIC);
        getContentResolver()
                .registerContentObserver(CommonDataStructure.HEADPICSURL, true,
                        mHeaderPicChangeObserver);
        getContentResolver().registerContentObserver(
                CommonDataStructure.BRAVOURL, true, mChangeObserver);
        getContentResolver().registerContentObserver(
                CommonDataStructure.REPLYURL, true, mChangeObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCommentEntrys.clear();
        mCommentEntrys.addAll(loadUserCommentsFromDB(mUserInfoUid));
        mListViewAdapter.setCommentDataSource(mCommentEntrys);
        mListViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        mUserInfo = mUserInfoEntrys.get(mUserInfoUid);

        mContactsInfoHeaderHeight = mContactsInfoHeader.getMeasuredHeight();
        mContactsInfoHeaderWidth = mContactsInfoHeader.getMeasuredWidth();

        if ("0".equalsIgnoreCase(mUserInfo.getHeaderBkgIndex())) {
            Bitmap thumbHeader = BitmapFactory.decodeResource(getResources(),
                    R.drawable.person_default_bkg);
            Bitmap cropHeader = Utils.cropImages(thumbHeader,
                    mContactsInfoHeaderWidth, mContactsInfoHeaderHeight, true);
            mHeaderLayout
                    .setBackground(ImageUtils.bitmapToDrawable(cropHeader));
        } else {
            mExecutorService.execute(new DownloadHeadBackground(mUserInfo
                    .getUid(), mUserInfo.getHeaderBkgIndex()));
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mChangeObserver);
        getContentResolver()
                .unregisterContentObserver(mHeaderPicChangeObserver);
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
        case R.id.contacts_info_return: {
            this.finish();
            break;
        }
        case R.id.contacts_info_head_01: {
            if (mUserInfoUid.equalsIgnoreCase(mAuthorUid)) {
                showBackgroundPicsPicker(this);
            }
            break;
        }
        case R.id.chat_msg_person_pic: {
            if (mUserInfoUid.equalsIgnoreCase(mAuthorUid)) {
                showHeaderPicsPicker(this, true);
            }
            if (mCropPhoto != null) {
                mCropPhoto = null;
            }
            break;
        }
        case R.id.contacts_info_chat_btn: {
            String chatId = mAuthorUid + "_" + mUserInfoUid;
            startToChat(chatId);
            break;
        }
        default:
            break;
        }
    }

    private void initOriginData() {
        mHeaderLayout01.setOnClickListener(this);
        mReturnBtn.setOnClickListener(this);
        mUserPic.setOnClickListener(this);
        if (mUserHeadPic != null) {
            mUserPic.setImageBitmap(mUserHeadPic);
        }

        mUserName.setText(mUserInfo.getNickName());
        mFriendName.setText(mUserInfo.getNickName());
        mFriendsDesc.setText(mUserInfo.getIntroduce());

        if (mUserInfoUid.equalsIgnoreCase(mAuthorUid)) {
            hideChatBtn();
        } else {
            // String friendsDesc = String.format(
            // this.getString(R.string.chat_msg_friends_more),
            // mUserInfo.getFirstDirectFriend(),
            // mUserInfo.getDirectFriendsCount());
            // mFriendsDesc.setText(friendsDesc);
            showChatBtn();
        }

        if (mUserInfo.getGender() == ContactsInfo.GENDER.FEMALE.ordinal()) {
            mUserGender.setImageResource(R.drawable.ic_female_selected);
        } else {
            mUserGender.setImageResource(R.drawable.ic_male_selected);
        }

        if (mUserInfo.getAstro() == ContactsInfo.ASTRO.ARIES.ordinal()) {
            mUserAstro.setImageResource(R.drawable.ic_aries_baiyang_green);
        } else if (mUserInfo.getAstro() == ContactsInfo.ASTRO.TAURUS.ordinal()) {
            mUserAstro.setImageResource(R.drawable.ic_taurus_jinniu_green);
        } else if (mUserInfo.getAstro() == ContactsInfo.ASTRO.GEMINI.ordinal()) {
            mUserAstro.setImageResource(R.drawable.ic_gemini_shuangzhi_green);
        } else if (mUserInfo.getAstro() == ContactsInfo.ASTRO.CANCER.ordinal()) {
            mUserAstro.setImageResource(R.drawable.ic_cancer_juxie_green);
        } else if (mUserInfo.getAstro() == ContactsInfo.ASTRO.LEO.ordinal()) {
            mUserAstro.setImageResource(R.drawable.ic_leo_shizhi_green);
        } else if (mUserInfo.getAstro() == ContactsInfo.ASTRO.VIRGO.ordinal()) {
            mUserAstro.setImageResource(R.drawable.ic_virgo_chunv_green);
        } else if (mUserInfo.getAstro() == ContactsInfo.ASTRO.LIBRA.ordinal()) {
            mUserAstro.setImageResource(R.drawable.ic_libra_tiancheng_green);
        } else if (mUserInfo.getAstro() == ContactsInfo.ASTRO.SCORPIO.ordinal()) {
            mUserAstro.setImageResource(R.drawable.ic_scorpio_tianxie_green);
        } else if (mUserInfo.getAstro() == ContactsInfo.ASTRO.SAGITTARIUS
                .ordinal()) {
            mUserAstro
                    .setImageResource(R.drawable.ic_sagittarius_sheshou_green);
        } else if (mUserInfo.getAstro() == ContactsInfo.ASTRO.CAPRICPRN
                .ordinal()) {
            mUserAstro.setImageResource(R.drawable.ic_capricprn_mejie_green);
        } else if (mUserInfo.getAstro() == ContactsInfo.ASTRO.AQUARIUS
                .ordinal()) {
            mUserAstro.setImageResource(R.drawable.ic_aquarius_shuiping_green);
        } else if (mUserInfo.getAstro() == ContactsInfo.ASTRO.PISCES.ordinal()) {
            mUserAstro.setImageResource(R.drawable.ic_pisces_shuangyu_green);
        }

        if (mUserInfo.getHobby() == ContactsInfo.GENDER.FEMALE.ordinal()) {
            mUserHobby.setImageResource(R.drawable.ic_female_selected);
        } else {
            mUserHobby.setImageResource(R.drawable.ic_male_selected);
        }

    }

    private Bitmap loadUserHeadPicFromDB(String uid) {

        Bitmap headpic = null;

        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + uid;
        Cursor cursor = mDBHelper
                .query(MarrySocialDBHelper.DATABASE_HEAD_PICS_TABLE,
                        HEAD_PICS_PROJECTION, whereClause, null, null, null,
                        null, null);

        if (cursor == null) {
            Log.w(TAG, "nannan query fail!");
            return null;
        }

        try {
            cursor.moveToNext();
            byte[] in = cursor.getBlob(1);
            headpic = BitmapFactory.decodeByteArray(in, 0, in.length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return headpic;
    }

    private void loadContactsFromDB() {

        MarrySocialDBHelper dbHelper = MarrySocialDBHelper.newInstance(this);
        Cursor cursor = dbHelper.query(
                MarrySocialDBHelper.DATABASE_CONTACTS_TABLE,
                CONTACTS_PROJECTION, null, null, null, null, null, null);
        if (cursor == null) {
            Log.w(TAG, "nannan query fail!");
            return;
        }

        try {
            while (cursor.moveToNext()) {
                String uid = cursor.getString(0);
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
                String headerBkg = cursor.getString(12);
                String introduce = cursor.getString(13);

                ContactsInfo contactItem = new ContactsInfo();
                contactItem.setUid(uid);
                contactItem.setPhoneNum(phoneNum);
                contactItem.setNickName(nickname);
                contactItem.setRealName(realname);
                contactItem.setHeadPic(avatar);
                contactItem.setGender(gender);
                contactItem.setAstro(astro);
                contactItem.setHobby(hobby);
                contactItem.setIndirectId(indirectId);
                contactItem.setFirstDirectFriend(firstDirectFriend);
                contactItem.setDirectFriends(directFriends);
                contactItem.setDirectFriendsCount(directFriendsCount);
                contactItem.setHeaderBkgIndex(headerBkg);
                contactItem.setIntroduce(introduce);

                mUserInfoEntrys.put(uid, contactItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return;
    }

    private ArrayList<CommentsItem> loadUserCommentsFromDB(String uid) {

        ArrayList<CommentsItem> commentEntrys = new ArrayList<CommentsItem>();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_UID + " = " + uid;
            String orderBy = MarrySocialDBHelper.KEY_ADDED_TIME + " DESC";
            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_COMMENTS_TABLE,
                    COMMENTS_PROJECTION, whereclause, null, null, null,
                    orderBy, null);
            if (cursor == null) {
                Log.e(TAG, "nannan loadUserCommentsFromDB()..  cursor == null");
                return commentEntrys;
            }
            while (cursor.moveToNext()) {
                CommentsItem comment = new CommentsItem();
                String uId = cursor.getString(0);
                String bucketId = cursor.getString(1);
                String contents = cursor.getString(2);
                String nick_name = cursor.getString(3);
                int photo_count = cursor.getInt(4);
                int bravo_status = cursor.getInt(5);
                String added_time = cursor.getString(6);
                String comment_id = cursor.getString(7);
                comment.setUid(uId);
                comment.setBucketId(bucketId);
                comment.setCommentId(comment_id);
                comment.setContents(contents);
                comment.setRealName(nick_name);
                comment.setNickName(nick_name);
                comment.setPhotoCount(photo_count);
                comment.setAddTime(Utils.getAddedTimeTitle(this, added_time));
                comment.setIsBravo(bravo_status == MarrySocialDBHelper.BRAVO_CONFIRM);
                commentEntrys.add(comment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        mHandler.sendEmptyMessage(START_TO_LOAD_BRAVO_REPLY);

        return commentEntrys;
    }

    private void startToChat(String chatId) {
        Intent intent = new Intent(this, ChatMsgActivity.class);
        intent.putExtra(MarrySocialDBHelper.KEY_CHAT_ID, chatId);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

            case NEED_CROP: {
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();
                } else {
                    String fileName = getSharedPreferences(
                            CommonDataStructure.PREFS_LAIQIAN_DEFAULT,
                            MODE_PRIVATE).getString(
                            CommonDataStructure.HEAD_PIC_NAME, "");
                    uri = Uri.fromFile(new File(
                            CommonDataStructure.HEAD_PICS_DIR_URL, fileName));
                }
                cropImage(uri, Utils.mCropCenterThumbPhotoWidth,
                        Utils.mCropCenterThumbPhotoWidth, CROP_PICTURE);
                break;
            }

            case CROP_PICTURE: {
                Uri photoUri = data.getData();
                if (photoUri != null) {
                    mCropPhoto = BitmapFactory.decodeFile(photoUri.getPath());
                }
                if (mCropPhoto == null) {
                    Bundle extra = data.getExtras();
                    if (extra != null) {
                        mCropPhoto = (Bitmap) extra.get("data");
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        mCropPhoto.compress(Bitmap.CompressFormat.JPEG, 100,
                                stream);
                    }
                }
                mCropPhotoName = "head_pic_" + mUserInfoUid + ".jpg";
                ImageUtils.savePhotoToSDCard(mCropPhoto,
                        CommonDataStructure.HEAD_PICS_DIR_URL, mCropPhotoName);

                mHandler.sendEmptyMessage(START_TO_UPLOAD);
                // iv_image.setImageBitmap(mCropPhoto);//联网才能上传照片
                break;
            }

            case CHOOSE_BACKGROUND_PICTURE: {
                String localPath = data.getExtras().getString(
                        MarrySocialDBHelper.KEY_PHOTO_LOCAL_PATH);
                String headerBkgIndex = data.getExtras().getString(
                        MarrySocialDBHelper.KEY_HEADER_BACKGROUND_INDEX);
                updateHeaderBkgIndexToContactsDB(mAuthorUid, headerBkgIndex);
                Bitmap thumbHeader = Utils.decodeThumbnail(localPath, null,
                        Utils.mThumbPhotoWidth);
                Bitmap cropHeader = Utils.cropImages(thumbHeader,
                        mContactsInfoHeaderWidth, mContactsInfoHeaderHeight,
                        true);
                mHeaderLayout.setBackground(ImageUtils
                        .bitmapToDrawable(cropHeader));
                mExecutorService.execute(new UploadHeadBackground(mAuthorUid,
                        headerBkgIndex));
                break;
            }

            default:
                break;
            }
        }
    }

    private void updateHeaderBkgIndexToContactsDB(String uid,
            String headerebkgindex) {
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_HEADER_BACKGROUND_INDEX,
                headerebkgindex);

        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + uid;
        mDBHelper.update(MarrySocialDBHelper.DATABASE_CONTACTS_TABLE, values,
                whereClause, null);
    }

    private void showBackgroundPicsPicker(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(new String[] { "更换相册封面" },
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                        case CHANGE_HEAD_BACKGROUND: {
                            startToChooseBackgroundPic();
                            break;
                        }
                        default:
                            break;
                        }
                    }
                });
        builder.create().show();
    }

    private void showHeaderPicsPicker(Context context, boolean isCrop) {
        final boolean needCrop = isCrop;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("图片来源");
        builder.setNegativeButton("取消", null);
        builder.setItems(new String[] { "拍照", "相册" },
                new DialogInterface.OnClickListener() {
                    // 类型码
                    int REQUEST_CODE;

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                        case TAKE_PICTURE_FROM_CAMERA:
                            Uri imageUri = null;
                            String fileName = null;
                            Intent openCameraIntent = new Intent(
                                    MediaStore.ACTION_IMAGE_CAPTURE);
                            if (needCrop) {
                                REQUEST_CODE = NEED_CROP;
                                // 删除上一次截图的临时文件
                                SharedPreferences sharedPreferences = getSharedPreferences(
                                        CommonDataStructure.PREFS_LAIQIAN_DEFAULT,
                                        MODE_PRIVATE);
                                ImageUtils
                                        .deletePhotoAtPathAndName(
                                                CommonDataStructure.HEAD_PICS_DIR_URL,
                                                sharedPreferences
                                                        .getString(
                                                                CommonDataStructure.HEAD_PIC_NAME,
                                                                ""));

                                // 保存本次截图临时文件名字
                                fileName = "head_pic_" + mUserInfoUid + ".jpg";
                                Editor editor = sharedPreferences.edit();
                                editor.putString(
                                        CommonDataStructure.HEAD_PIC_NAME,
                                        fileName);
                                editor.commit();
                            } else {
                                REQUEST_CODE = TAKE_PICTURE_FROM_CAMERA;
                                fileName = "image.jpg";
                            }
                            imageUri = Uri.fromFile(new File(
                                    CommonDataStructure.HEAD_PICS_DIR_URL,
                                    fileName));
                            // 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
                            openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    imageUri);
                            startActivityForResult(openCameraIntent,
                                    REQUEST_CODE);
                            break;

                        case CHOOSE_PICTURE_FROM_GALLERY:
                            Intent openGalleryIntent = new Intent(
                                    Intent.ACTION_GET_CONTENT);
                            if (needCrop) {
                                REQUEST_CODE = NEED_CROP;
                            } else {
                                REQUEST_CODE = CHOOSE_PICTURE_FROM_GALLERY;
                            }
                            openGalleryIntent
                                    .setDataAndType(
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            "image/*");
                            startActivityForResult(openGalleryIntent,
                                    REQUEST_CODE);
                            break;

                        default:
                            break;
                        }
                    }
                });
        builder.create().show();
    }

    // 截取图片
    public void cropImage(Uri uri, int outputX, int outputY, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("outputFormat", "JPEG");
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, requestCode);
    }

    class UploadHeadPics implements Runnable {

        private String uid;

        public UploadHeadPics(String uid) {
            this.uid = uid;
        }

        @Override
        public void run() {
            CommonDataStructure.UploadHeadPicResultEntry resultEntry = Utils
                    .uploadHeadPicBitmap(
                            CommonDataStructure.URL_UPLOAD_HEAD_PIC, uid,
                            mCropPhoto, mCropPhotoName);
            if (!isUidExistInHeadPicDB(uid)) {
                insertHeadPicToHeadPicsDB(resultEntry);
            } else {
                updateHeadPicToHeadPicsDB(resultEntry);
            }

            mHandler.sendEmptyMessage(UPLOAD_FINISH);
        }

    }

    class UploadHeadBackground implements Runnable {

        private String uid;
        private String photonum;

        public UploadHeadBackground(String uid, String photonum) {
            this.uid = uid;
            this.photonum = photonum;
        }

        @Override
        public void run() {
            Utils.uploadHeaderBackground(
                    CommonDataStructure.URL_PROFILE_BACKGROUND, uid, photonum);
        }

    }

    class DownloadHeadBackground implements Runnable {

        private String uid;
        private String photonum;

        public DownloadHeadBackground(String uid, String photonum) {
            this.uid = uid;
            this.photonum = photonum;
        }

        @Override
        public void run() {
            HeaderBackgroundEntry headerBkg = queryHeaderBkgEntryFromDB(photonum);
            if (headerBkg.photoLocalPath != null
                    && headerBkg.photoLocalPath.length() != 0) {
                mHeadBkgPath = headerBkg.photoLocalPath;
            } else {
                File imageFile = Utils.downloadImageAndCache(
                        headerBkg.photoRemotePath,
                        CommonDataStructure.BACKGROUND_PICS_DIR_URL);
                updateHeaderBkgPathToHeaderBkgDB(imageFile.getAbsolutePath(),
                        headerBkg.photoRemotePath);
                mHeadBkgPath = imageFile.getAbsolutePath();
            }
            mHandler.sendEmptyMessage(DOWNLOAD_HEADER_BKG_FINISH);
        }

    }

    private void updateHeaderBkgPathToHeaderBkgDB(String localpath,
            String remotepath) {
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_PHOTO_LOCAL_PATH, localpath);
        values.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.DOWNLOAD_FROM_CLOUD_SUCCESS);

        String whereClause = MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH
                + " = " + '"' + remotepath + '"';
        ContentResolver resolver = getContentResolver();
        resolver.update(CommonDataStructure.HEADBACKGROUNDURL, values,
                whereClause, null);
    }

    private void insertHeadPicToHeadPicsDB(
            CommonDataStructure.UploadHeadPicResultEntry headPic) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_UID, headPic.uid);
        insertValues.put(MarrySocialDBHelper.KEY_HEAD_PIC_BITMAP,
                Utils.Bitmap2Bytes(mCropPhoto));
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH,
                headPic.orgUrl);
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_REMOTE_THUMB_PATH,
                headPic.smallThumbUrl);
        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.UPLOAD_TO_CLOUD_SUCCESS);

        ContentResolver resolver = getContentResolver();
        resolver.insert(CommonDataStructure.HEADPICSURL, insertValues);
    }

    private void updateHeadPicToHeadPicsDB(
            CommonDataStructure.UploadHeadPicResultEntry headPic) {

        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_HEAD_PIC_BITMAP,
                Utils.Bitmap2Bytes(mCropPhoto));
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH,
                headPic.orgUrl);
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_REMOTE_THUMB_PATH,
                headPic.smallThumbUrl);
        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.UPLOAD_TO_CLOUD_SUCCESS);

        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + headPic.uid;
        ContentResolver resolver = getContentResolver();
        resolver.update(CommonDataStructure.HEADPICSURL, insertValues,
                whereClause, null);
    }

    public HeaderBackgroundEntry queryHeaderBkgEntryFromDB(String headerBkgIndex) {

        HeaderBackgroundEntry headerBkgEntry = new HeaderBackgroundEntry();
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_HEADER_BACKGROUND_INDEX
                    + " = " + headerBkgIndex;
            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_HEAD_BACKGROUND_PICS_TABLE,
                    HEAD_BACKGROUND_PROJECTION, whereclause, null, null, null,
                    null, null);
            if (cursor == null || cursor.getCount() == 0) {
                return headerBkgEntry;
            }
            cursor.moveToNext();
            headerBkgEntry.photoName = cursor.getString(0);
            headerBkgEntry.photoLocalPath = cursor.getString(1);
            headerBkgEntry.photoRemotePath = cursor.getString(2);
            headerBkgEntry.headerBkgIndex = cursor.getString(3);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return headerBkgEntry;
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

    private class DataSetChangeObserver extends ContentObserver {

        private Handler handler;
        private int status;

        public DataSetChangeObserver(Handler handler, int status) {
            super(handler);
            this.handler = handler;
            this.status = status;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            switch (status) {
            case REFRESH_HEADER_PIC: {
                handler.sendEmptyMessage(REFRESH_HEADER_PIC);
                break;
            }
            case UPLOAD_COMMENT: {
                handler.sendEmptyMessage(UPLOAD_COMMENT);
                break;
            }
            default:
                break;
            }
            Log.e(TAG, "nannan onChange()..");
        }
    }

    private void startToChooseBackgroundPic() {
        Intent intent = new Intent(this, ChooseHeaderBackgroundActivity.class);
        startActivityForResult(intent, CHOOSE_BACKGROUND_PICTURE);
    }

    private void generateDBData() {
        int index = 1;
        for (String remote : CommonDataStructure.HEADER_BKG_PATH) {
            String name = index + ".jpg";
            if (!isHeaderBkgPathExistInHeaderBkgDB(remote)) {
                insertHeaderBkgPathToHeaderBkgDB(name, remote, index);
            }
            index++;
        }
    }

    public boolean isHeaderBkgPathExistInHeaderBkgDB(String remotepath) {
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH
                    + " = " + '"' + remotepath + '"';
            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_HEAD_BACKGROUND_PICS_TABLE,
                    HEAD_BKG_PROJECTION, whereclause, null, null, null, null,
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

    private void insertHeaderBkgPathToHeaderBkgDB(String photoname,
            String remotepath, int picindex) {
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_PHOTO_NAME, photoname);
        values.put(MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH, remotepath);
        values.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.NEED_DOWNLOAD_FROM_CLOUD);
        values.put(MarrySocialDBHelper.KEY_HEADER_BACKGROUND_INDEX,
                String.valueOf(picindex));
        mDBHelper
                .insert(MarrySocialDBHelper.DATABASE_HEAD_BACKGROUND_PICS_TABLE,
                        values);
    }

    class LoadBravoAndReplyContents implements Runnable {

        private String comment_id;

        public LoadBravoAndReplyContents(String comment_id) {
            this.comment_id = comment_id;
        }

        @Override
        public void run() {
            loadContactsFromDB();
            loadBravosFromDB(comment_id);
            loadReplysFromDB(comment_id);
            mHandler.sendEmptyMessage(UPDATE_DYNAMIC_INFO);
        }
    }

    private void loadBravosFromDB(String comment_id) {

        StringBuffer author_names = new StringBuffer();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                    + comment_id + " AND "
                    + MarrySocialDBHelper.KEY_CURRENT_STATUS + " != "
                    + MarrySocialDBHelper.NEED_DELETE_FROM_CLOUD;
            String orderBy = MarrySocialDBHelper.KEY_ID + " ASC";
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_BRAVOS_TABLE,
                    BRAVOS_PROJECTION, whereclause, null, null, null, orderBy,
                    null);
            if (cursor == null) {
                Log.e(TAG, "nannan loadBravosFromDB()..  cursor == null");
                return;
            }
            while (cursor.moveToNext()) {
                author_names.append(cursor.getString(2)).append("  ");
            }
            if (author_names.length() != 0) {
                mBravoEntrys.put(comment_id, author_names.toString());
            } else {
                mBravoEntrys.put(comment_id, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void loadReplysFromDB(String comment_id) {

        ArrayList<ReplysItem> replys = new ArrayList<ReplysItem>();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                    + comment_id;
            String orderBy = MarrySocialDBHelper.KEY_ADDED_TIME + " ASC";
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_REPLYS_TABLE,
                    REPLYS_PROJECTION, whereclause, null, null, null, orderBy,
                    null);
            if (cursor == null) {
                Log.e(TAG, "nannan loadReplysFromDB()..  cursor == null");
                return;
            }
            while (cursor.moveToNext()) {
                ReplysItem item = new ReplysItem();
                item.setCommentId(comment_id);
                item.setNickname(cursor.getString(1));
                item.setReplyContents(cursor.getString(2));
                item.setUid(cursor.getString(0));
                replys.add(item);
            }
            if (replys.size() != 0) {
                mReplyEntrys.put(comment_id, replys);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private onReplyBtnClickedListener mReplyBtnClickedListener = new onReplyBtnClickedListener() {

        @Override
        public void onReplyBtnClicked(int position) {
            mReplyCommentsPosition = position;
            hideChatBtn();
            showReplyFootBar();
            mHandler.sendEmptyMessageDelayed(SHOW_SOFT_INPUT_METHOD, 50);
            Message msg = mHandler.obtainMessage();
            msg.what = SELECT_SPECIFIED_LIST_ITEM;
            msg.arg1 = position + 1;
            mHandler.sendMessage(msg);
        }
    };

    private View.OnClickListener mReplySendBtnClickedListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (!Utils.isActiveNetWorkAvailable(ContactsInfoActivity.this)) {
                mHandler.sendEmptyMessage(NETWORK_INVALID);
                return;
            }
            CommentsItem comment = (CommentsItem) (mListViewAdapter
                    .getItem(mReplyCommentsPosition));
            long time = System.currentTimeMillis() / 1000;
            String replyTime = Long.toString(time);
            String bucketId = String.valueOf(replyTime.hashCode());
            ReplysItem reply = new ReplysItem();
            reply.setCommentId(comment.getCommentId());
            reply.setReplyContents(mReplyContents.getText().toString());
            reply.setReplyTime(replyTime);
            reply.setBucketId(bucketId);
            insertReplysToReplyDB(reply);
            uploadReplysToCloud(CommonDataStructure.KEY_REPLYS,
                    comment.getCommentId(), bucketId);
            mHandler.sendEmptyMessage(SEND_REPLY_FINISH);
            mHandler.sendEmptyMessage(UPDATE_DYNAMIC_INFO);
        }
    };

    private void insertReplysToReplyDB(ReplysItem reply) {

        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_COMMENT_ID,
                reply.getCommentId());
        insertValues.put(MarrySocialDBHelper.KEY_UID, mAuthorUid);
        insertValues
                .put(MarrySocialDBHelper.KEY_BUCKET_ID, reply.getBucketId());
        insertValues.put(MarrySocialDBHelper.KEY_AUTHOR_NICKNAME, mAuthorName);
        insertValues.put(MarrySocialDBHelper.KEY_REPLY_CONTENTS,
                reply.getReplyContents());
        insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
                reply.getReplyTime());
        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD);

        ContentResolver resolver = this.getContentResolver();
        resolver.insert(CommonDataStructure.REPLYURL, insertValues);
    }

    private void uploadReplysToCloud(int uploadType, String comment_id,
            String bucket_id) {
        Intent serviceIntent = new Intent(this,
                UploadCommentsAndBravosAndReplysIntentService.class);
        serviceIntent.putExtra(CommonDataStructure.KEY_UPLOAD_TYPE, uploadType);
        serviceIntent.putExtra(MarrySocialDBHelper.KEY_COMMENT_ID, comment_id);
        serviceIntent.putExtra(MarrySocialDBHelper.KEY_BUCKET_ID, bucket_id);
        startService(serviceIntent);
    }

    private void hideReplyFootBar() {
        mReplyFoot.setVisibility(View.GONE);
    }

    private void showReplyFootBar() {
        mReplyFoot.setVisibility(View.VISIBLE);
        mReplyFoot.requestFocus();
    }

    private void hideChatBtn() {
        mChatButton.clearAnimation();
        mChatButton.startAnimation(mHideChatBtnTransAnimation);
        mChatButton.setVisibility(View.INVISIBLE);
        mChatButton.setClickable(false);
    }

    private void showChatBtn() {

        if (!mUserInfoUid.equalsIgnoreCase(mAuthorUid)) {
            mChatButton.clearAnimation();
            mChatButton.startAnimation(mShowChatBtnTransAnimation);
            mChatButton.setVisibility(View.VISIBLE);
            mChatButton.setClickable(true);
            mChatButton.requestFocus();
        }

    }
}
