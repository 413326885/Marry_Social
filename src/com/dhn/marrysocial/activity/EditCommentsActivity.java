package com.dhn.marrysocial.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.adapter.EditCommentsPhotoViewAdapter;
import com.dhn.marrysocial.base.ContactsInfo;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.provider.DBContentChangeProvider;
import com.dhn.marrysocial.roundedimageview.RoundedImageView;
import com.dhn.marrysocial.services.UploadCommentsAndBravosAndReplysIntentService;
import com.dhn.marrysocial.utils.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EditCommentsActivity extends Activity implements OnClickListener {

    private static final String TAG = "EditCommentsActivity";

    private static final int UPLOAD_FINISH = 100;
    private final int POOL_SIZE = 10;

    private static final int SELECTION_PHOTO_FROM_CAMERA = 1986;
    private static final int CROP_PHOTO_FROM_CAMERA = 1987;
    private static final int SELECTION_PHOTO_FROM_GALLERY = 1988;

    public static final String IMG_TYPE = "image/*";
    public static final String FILE_PREX = "marrysocial_";
    public static final String FILE_SUBFIX_FORMAT = "yyyy-MM-dd-HHmmss";
    public static final String DEFAULT_IMAGE_STORE_DIR = "/sdcard/DCIM/100MEDIA/";
    public static final String COMMENT_POST_URL = "http://www.pkjiao.com/topic/post";

    // public static final Uri mCommentUri = Uri.parse("content://"
    // + DataSetProvider.AUTHORITY + "/"
    // + MarrySocialDBHelper.DATABASE_COMMENTS_TABLE);

    private static final int INDEX_DATA = 3;

    static final String[] SELECT_PHOTO_PROJECTION = { ImageColumns._ID,
            ImageColumns.TITLE, ImageColumns.MIME_TYPE, ImageColumns.DATA,
            ImageColumns.ORIENTATION, ImageColumns.BUCKET_ID,
            ImageColumns.SIZE, };

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

    private static final String[] HEAD_PICS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_HEAD_PIC_BITMAP,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_THUMB_PATH };
    // static final String[] INSERT_COMMENT_PROJECTION = {
    // MarrySocialDBHelper.KEY_UID,
    // MarrySocialDBHelper.KEY_AVATAR, MarrySocialDBHelper.KEY_NAME,
    // MarrySocialDBHelper.KEY_FRIENDS };

    static {
        File temp = new File(DEFAULT_IMAGE_STORE_DIR);
        if (!temp.exists()) {
            temp.mkdirs();
        }
    }

    private RelativeLayout mCommentReturn;
    private RoundedImageView mCommentPersonPic;
    private TextView mCommentPersonName;
    private ImageView mCommentCamera;
    private ImageView mCommentGallery;
    private ImageView mCommentSend;
    private EditText mCommentDescription;
    private GridView mCommentAddPics;
    private String mCameraFilePath;
    private EditCommentsPhotoViewAdapter mPhotoViewAdapter;
    private String mCurrentEditTime;
    private String mBucketId;

    private ProgressDialog mUploadProgressDialog;
    private Handler mHandler;
    private String mTid;
    private String mUid;
    private String mAuthorName;

    private ContactsInfo mUserInfo;
    private Bitmap mUserHeadPic = null;
    private MarrySocialDBHelper mDBHelper;
    private ExecutorService mExecutorService;

    private ArrayList<Bitmap> mCropCenterThumbBitmaps = new ArrayList<Bitmap>();
    private ArrayList<Bitmap> mOriginalThumbBitmaps = new ArrayList<Bitmap>();
    private ArrayList<String> mOriginalThumbBitmapsPath = new ArrayList<String>();

    TextWatcher mTextChangeListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            if (s.length() == 0) {
                mCommentSend
                        .setImageResource(R.drawable.ic_send_button_unselected);
                mCommentSend.setEnabled(false);
            } else {
                mCommentSend
                        .setImageResource(R.drawable.ic_send_button_selected);
                mCommentSend.setEnabled(true);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_info_comments_layout);

        mCommentReturn = (RelativeLayout) findViewById(R.id.edit_info_comment_return);
        mCommentPersonPic = (RoundedImageView) findViewById(R.id.edit_info_comments_person_pic);
        mCommentPersonName = (TextView) findViewById(R.id.edit_info_comments_person_name);
        mCommentCamera = (ImageView) findViewById(R.id.edit_info_comments_camera);
        mCommentGallery = (ImageView) findViewById(R.id.edit_info_comments_gallery);
        mCommentSend = (ImageView) findViewById(R.id.edit_info_comments_send);
        mCommentDescription = (EditText) findViewById(R.id.edit_info_comments_description);
        mCommentAddPics = (GridView) findViewById(R.id.edit_info_comments_add_pics);

        mCommentReturn.setOnClickListener(this);
        mCommentCamera.setOnClickListener(this);
        mCommentGallery.setOnClickListener(this);
        mCommentSend.setOnClickListener(this);
        mCommentSend.setEnabled(false);

        mCommentDescription.setOnClickListener(this);
        mCommentDescription.addTextChangedListener(mTextChangeListener);

        mPhotoViewAdapter = new EditCommentsPhotoViewAdapter(this);
        mPhotoViewAdapter.setDataSource(mCropCenterThumbBitmaps);
        mPhotoViewAdapter.setPhotoOperationListener(mPhotoOperation);
        mCommentAddPics.setAdapter(mPhotoViewAdapter);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case UPLOAD_FINISH: {
                    mCommentDescription.setText("");
                    mOriginalThumbBitmaps.clear();
                    mCropCenterThumbBitmaps.clear();
                    mOriginalThumbBitmapsPath.clear();
                    mPhotoViewAdapter.notifyDataSetChanged();
                    mUploadProgressDialog.dismiss();
                    finishActivity();
                }
                default:
                    break;
                }
            }
        };

        mDBHelper = MarrySocialDBHelper.newInstance(this);
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);

        SharedPreferences prefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, this.MODE_PRIVATE);
        mUid = prefs.getString(CommonDataStructure.UID, "");
        mAuthorName = prefs.getString(CommonDataStructure.AUTHOR_NAME, "");

        mUserInfo = loadUserInfoFromDB(mUid);
        mUserHeadPic = loadUserHeadPicFromDB(mUid);
        mCommentPersonPic.setImageBitmap(mUserHeadPic);
        mCommentPersonName.setText(mUserInfo.getNickName());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
        case R.id.edit_info_comment_return: {
            Utils.hideSoftInputMethod(mCommentDescription);
            this.finish();
            break;
        }
        case R.id.edit_info_comments_camera: {
            pickPhotosFromCamera();
            break;
        }
        case R.id.edit_info_comments_gallery: {
            pickPhotosFromGallery();
            break;
        }
        case R.id.edit_info_comments_send: {
            mUploadProgressDialog = ProgressDialog.show(this, "上传动态",
                    "正在上传动态，请稍后...", false, true);
            mExecutorService.execute(new UploadFiles());
            break;
        }
        case R.id.edit_info_comments_description: {
            Utils.showSoftInputMethod(mCommentDescription);
            break;
        }
        case R.id.edit_info_comments_add_pics: {
            break;
        }
        default:
            break;
        }
    }

    private void pickPhotosFromGallery() {
        if (!checkAndResetSelectPhotoButtonStatus()) {
            Toast.makeText(this, R.string.add_pics_limit, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        try {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, SELECTION_PHOTO_FROM_GALLERY);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void pickPhotosFromCamera() {
        if (!checkAndResetSelectPhotoButtonStatus()) {
            Toast.makeText(this, R.string.add_pics_limit, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        SimpleDateFormat sDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        String date = sDateFormat.format(new java.util.Date());
        String name = DEFAULT_IMAGE_STORE_DIR + FILE_PREX + date + ".jpg";

        File out = new File(name);
        Uri uri = Uri.fromFile(out);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        mCameraFilePath = out.getAbsolutePath();

        try {
            startActivityForResult(intent, SELECTION_PHOTO_FROM_CAMERA);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void doCropPhotoWhenCamera(Bitmap data) {
        Intent intent = getCropImageIntent(data);
        startActivityForResult(intent, CROP_PHOTO_FROM_CAMERA);
    }

    private static Intent getCropImageIntent(Bitmap data) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        intent.putExtra("data", data);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 128);
        intent.putExtra("outputY", 128);
        intent.putExtra("return-data", true);
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case SELECTION_PHOTO_FROM_CAMERA: {
                Log.e(TAG, "nannan mCameraFilePath = " + mCameraFilePath);
                if (data == null) {
                    Log.v(TAG, "data intent is null!");
                }
                Bitmap thumbBitmap = null;
                Bitmap cropBitmap = null;
                thumbBitmap = Utils.decodeThumbnail(mCameraFilePath, null,
                        Utils.mThumbPhotoWidth);
                cropBitmap = Utils.resizeAndCropCenter(thumbBitmap,
                        Utils.mCropCenterThumbPhotoWidth, true);
                mOriginalThumbBitmapsPath.add(mCameraFilePath);
                mOriginalThumbBitmaps.add(thumbBitmap);
                mCropCenterThumbBitmaps.add(cropBitmap);
                break;
            }
            case SELECTION_PHOTO_FROM_GALLERY: {
                if (data == null) {
                    Log.v(TAG, "data intent is null!");
                }
                String filePath = null;
                Uri uri = data.getData();
                Log.e("uri", "nannan uri = " + uri.toString());
                ContentResolver cr = this.getContentResolver();
                Cursor cursor = cr.query(uri, SELECT_PHOTO_PROJECTION, null,
                        null, null);
                if (cursor == null) {
                    throw new RuntimeException("cannot get cursor for " + uri);
                }
                try {
                    if (cursor.moveToNext()) {
                        filePath = cursor.getString(INDEX_DATA);
                        Log.e("uri", "nannan filePath = " + filePath);
                    } else {
                        throw new RuntimeException("cannot find data for: "
                                + uri);
                    }
                } finally {
                    cursor.close();
                }
                Bitmap thumbBitmap = null;
                Bitmap cropBitmap = null;
                thumbBitmap = Utils.decodeThumbnail(filePath, null,
                        Utils.mThumbPhotoWidth);
                cropBitmap = Utils.resizeAndCropCenter(thumbBitmap,
                        Utils.mCropCenterThumbPhotoWidth, true);
                mOriginalThumbBitmapsPath.add(filePath);
                mOriginalThumbBitmaps.add(thumbBitmap);
                mCropCenterThumbBitmaps.add(cropBitmap);
                break;
            }
            default:
                break;
            }
            mPhotoViewAdapter.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.hideSoftInputMethod(mCommentDescription);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOriginalThumbBitmaps.clear();
        mCropCenterThumbBitmaps.clear();
        mOriginalThumbBitmapsPath.clear();
    }

    private EditCommentsPhotoViewAdapter.PhotoOperation mPhotoOperation = new EditCommentsPhotoViewAdapter.PhotoOperation() {

        @Override
        public void onPhotoClicked(int position) {
            mOriginalThumbBitmaps.remove(position);
            mCropCenterThumbBitmaps.remove(position);
            mOriginalThumbBitmapsPath.remove(position);
            mPhotoViewAdapter.notifyDataSetChanged();
            checkAndResetSelectPhotoButtonStatus();
        }
    };

    private boolean checkAndResetSelectPhotoButtonStatus() {
        if (mOriginalThumbBitmaps.size() >= 9) {
            mCommentGallery.setClickable(false);
            mCommentCamera.setClickable(false);
            return false;
        }
        mCommentGallery.setClickable(true);
        mCommentCamera.setClickable(true);
        return true;
    }

    public static class UploadCommentContentEntry {
        public String u_id;
        public String comment_content;
    }

    // public void sendCommentEntrys() {
    // insertCommentsToDB();
    // insertImagesToDB();
    // }

    class UploadFiles implements Runnable {

        public UploadFiles() {
        }

        @Override
        public void run() {
            insertCommentsToDB();
            insertImagesToDB();
            uploadCommentsToCloud(CommonDataStructure.KEY_COMMENTS, mBucketId);
            mHandler.sendEmptyMessage(UPLOAD_FINISH);
        }

    }

    public void insertCommentsToDB() {
        long time = System.currentTimeMillis() / 1000;
        mCurrentEditTime = Long.toString(time);
        mBucketId = String.valueOf(mCurrentEditTime.hashCode());

        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_UID, mUid);
        values.put(MarrySocialDBHelper.KEY_BUCKET_ID,
                mBucketId);
        values.put(MarrySocialDBHelper.KEY_COMMENT_ID,
                CommonDataStructure.INVALID_STR);
        values.put(MarrySocialDBHelper.KEY_ADDED_TIME, mCurrentEditTime);
        values.put(MarrySocialDBHelper.KEY_CONTENTS, mCommentDescription
                .getText().toString());
        values.put(MarrySocialDBHelper.KEY_AUTHOR_NICKNAME, mAuthorName);
        values.put(MarrySocialDBHelper.KEY_PHOTO_COUNT,
                mOriginalThumbBitmapsPath.size());
        values.put(MarrySocialDBHelper.KEY_BRAVO_COUNT, 0);
        values.put(MarrySocialDBHelper.KEY_BRAVO_STATUS,
                MarrySocialDBHelper.BRAVO_CANCEL);
        values.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD);

        ContentResolver resolver = this.getContentResolver();
        resolver.insert(CommonDataStructure.COMMENTURL, values);

        // MarrySocialDBHelper dbHelper = MarrySocialDBHelper.newInstance(this);
        // long result = dbHelper.insert(
        // MarrySocialDBHelper.DATABASE_COMMENTS_TABLE, values);

    }

    public void insertImagesToDB() {
        int position = 1;
        for (String path : mOriginalThumbBitmapsPath) {
            String[] paths = path.split(File.separator);
            int length = paths.length - 1;
            ContentValues values = new ContentValues();
            values.put(MarrySocialDBHelper.KEY_UID, mUid);
            values.put(MarrySocialDBHelper.KEY_BUCKET_ID,
                    mBucketId);
            values.put(MarrySocialDBHelper.KEY_COMMENT_ID,
                    CommonDataStructure.INVALID_STR);
            values.put(MarrySocialDBHelper.KEY_ADDED_TIME, mCurrentEditTime);
            values.put(MarrySocialDBHelper.KEY_PHOTO_NAME, paths[length]);
            values.put(MarrySocialDBHelper.KEY_PHOTO_LOCAL_PATH, path);
            values.put(MarrySocialDBHelper.KEY_PHOTO_POS, position++);
            values.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                    MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD);

            MarrySocialDBHelper dbHelper = MarrySocialDBHelper
                    .newInstance(this);
            long result = dbHelper.insert(
                    MarrySocialDBHelper.DATABASE_IMAGES_TABLE, values);
        }
    }

    private void finishActivity() {
        this.finish();
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

    private ContactsInfo loadUserInfoFromDB(String uid) {

        ContactsInfo userInfo = new ContactsInfo();

        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + uid;

        Cursor cursor = mDBHelper.query(
                MarrySocialDBHelper.DATABASE_CONTACTS_TABLE,
                CONTACTS_PROJECTION, whereClause, null, null, null, null, null);
        if (cursor == null) {
            Log.w(TAG, "nannan query fail!");
            return null;
        }

        try {
            cursor.moveToNext();

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

            userInfo.setUid(uid);
            userInfo.setPhoneNum(phoneNum);
            userInfo.setNickName(nickname);
            userInfo.setRealName(realname);
            userInfo.setHeadPic(avatar);
            userInfo.setGender(gender);
            userInfo.setAstro(astro);
            userInfo.setHobby(hobby);
            userInfo.setIntroduce(introduce);
            userInfo.setIndirectId(indirectId);
            userInfo.setFirstDirectFriend(firstDirectFriend);
            userInfo.setDirectFriends(directFriends);
            userInfo.setDirectFriendsCount(directFriendsCount);
            userInfo.setHeaderBkgIndex(headerBkg);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return userInfo;
    }

    private void uploadCommentsToCloud(int uploadType, String bucket_id) {
        Intent serviceIntent = new Intent(this,
                UploadCommentsAndBravosAndReplysIntentService.class);
        serviceIntent.putExtra(CommonDataStructure.KEY_UPLOAD_TYPE, uploadType);
        serviceIntent.putExtra(MarrySocialDBHelper.KEY_BUCKET_ID, bucket_id);
        startService(serviceIntent);
    }
}
