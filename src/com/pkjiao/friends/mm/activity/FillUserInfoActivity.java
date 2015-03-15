package com.pkjiao.friends.mm.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dhn.marrysocial.R;
import com.pkjiao.friends.mm.adapter.SelectAstroGridViewAdapter;
import com.pkjiao.friends.mm.common.CommonDataStructure;
import com.pkjiao.friends.mm.database.MarrySocialDBHelper;
import com.pkjiao.friends.mm.dialog.ProgressLoadDialog;
import com.pkjiao.friends.mm.dialog.SelectAstroDialog;
import com.pkjiao.friends.mm.dialog.SelectHeaderPicDialog;
import com.pkjiao.friends.mm.dialog.SelectHeaderPicDialog.OnSmallItemClickListener;
import com.pkjiao.friends.mm.roundedimageview.RoundedImageView;
import com.pkjiao.friends.mm.utils.ImageUtils;
import com.pkjiao.friends.mm.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class FillUserInfoActivity extends Activity implements OnClickListener {

    private static final String TAG = "FillUserInfoActivity";

    private static final String[] HEAD_PICS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_HEAD_PIC_BITMAP,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_THUMB_PATH };

    private static final int TAKE_PICTURE_FROM_CAMERA = 0;
    private static final int CHOOSE_PICTURE_FROM_GALLERY = 1;
    private static final int NEED_CROP = 2;
    private static final int CROP_PICTURE = 3;

    private static final int POOL_SIZE = 10;
    private static final int REGISTER_SUCCESS = 100;
    private static final int START_TO_UPLOAD_USER_HEADER = 101;
    private static final int UPLOAD_USER_HEADER_FINISH = 102;
    private static final int UPLOAD_USER_HEADER_FAIL = 103;
    private static final int START_TO_UPLOAD_USER_INFO = 104;
    private static final int UPLOAD_USER_INFO_FINISH = 105;
    private static final int UPLOAD_USER_INFO_FAIL = 106;

    private static final int[] ASTRO_ICON = {
            R.drawable.ic_aries_baiyang_green,
            R.drawable.ic_taurus_jinniu_green,
            R.drawable.ic_gemini_shuangzhi_green,
            R.drawable.ic_cancer_juxie_green, R.drawable.ic_leo_shizhi_green,
            R.drawable.ic_virgo_chunv_green,
            R.drawable.ic_libra_tiancheng_green,
            R.drawable.ic_scorpio_tianxie_green,
            R.drawable.ic_sagittarius_sheshou_green,
            R.drawable.ic_capricprn_mejie_green,
            R.drawable.ic_aquarius_shuiping_green,
            R.drawable.ic_pisces_shuangyu_green };

    private RoundedImageView mHeaderImageView;
    private TextView mHeaderText;
    private EditText mUserInfoName;
    private EditText mUserinfoSignature;
    private RadioButton mGenderMaleBtn;
    private RadioButton mGenderFemaleBtn;
    private ImageView mAstroImageView;
    private RadioButton mHobbyMaleBtn;
    private RadioButton mHobbyFemaleBtn;
    private Button mInviteFriendsBtn;
    private EditText mUserInfoDepartment;

    private MarrySocialDBHelper mDBHelper;
    private ExecutorService mExecutorService;
    private SelectAstroDialog mAstroDialog;
    private SelectHeaderPicDialog mSelectHeaderPicDialog;

    private int mGender = 1;
    private int mAstro = 1;
    private int mHobby = 0;

    private String mUid;
    private Bitmap mUserHeadPic = null;
    private Bitmap mCropPhoto = null;
    private String mCropPhotoName;

    private Context mContext;

    // private ProgressDialog mUploadUserHeaderProgressDialog;
    private ProgressLoadDialog mUploadUserInfoProgressDialog;

    TextWatcher mNameTextChangeListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            if (s.length() > 10) {
                String name = mUserInfoName.getText().toString()
                        .substring(0, 10);
                mUserInfoName.setText(name);
                mUserInfoName.setSelection(name.length());
                Toast.makeText(FillUserInfoActivity.this, "姓名不能超过10个字符", 500)
                        .show();
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

    TextWatcher mSignatureTextChangeListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            if (s.length() > 20) {
                String name = mUserinfoSignature.getText().toString()
                        .substring(0, 20);
                mUserinfoSignature.setText(name);
                mUserInfoName.setSelection(name.length());
                Toast.makeText(FillUserInfoActivity.this, "签名不能超过20个字符", 500)
                        .show();
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

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case START_TO_UPLOAD_USER_HEADER: {
                // mUploadUserHeaderProgressDialog = ProgressDialog.show(
                // FillUserInfoActivity.this, "上传头像", "正在上传头像，请稍后...",
                // false, true);
                mExecutorService.execute(new UploadHeadPics(mUid));
                break;
            }
            case UPLOAD_USER_HEADER_FINISH: {
                mUploadUserInfoProgressDialog.dismiss();
                startToInviteFriends();
                // mUserHeadPic = mCropPhoto;
                // mHeaderImageView.setImageBitmap(mUserHeadPic);
                // mHeaderText.setVisibility(View.GONE);
                // mUploadUserHeaderProgressDialog.dismiss();
                break;
            }
            case START_TO_UPLOAD_USER_INFO: {

                mUploadUserInfoProgressDialog = new ProgressLoadDialog(mContext);
                mUploadUserInfoProgressDialog.setText("正在上传个人信息，请稍后...");
                mUploadUserInfoProgressDialog.show();

                mExecutorService.execute(new UploadUserInfo(mUid));
                break;
            }
            case UPLOAD_USER_INFO_FINISH: {
                SharedPreferences prefs = FillUserInfoActivity.this
                        .getSharedPreferences(
                                CommonDataStructure.PREFS_LAIQIAN_DEFAULT,
                                MODE_PRIVATE);
                Editor editor = prefs.edit();
                editor.putInt(CommonDataStructure.LOGINSTATUS,
                        CommonDataStructure.LONIN_STATUS_FILLED_INFO);
                editor.commit();

                mUploadUserInfoProgressDialog.dismiss();
                startToInviteFriends();
                break;
            }
            case UPLOAD_USER_INFO_FAIL: {
                mUploadUserInfoProgressDialog.dismiss();
                Toast.makeText(FillUserInfoActivity.this, "上传个人信息失败，请稍后重试", 500)
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
        setContentView(R.layout.fill_userinfo_layout);

        mContext = this;

        mHeaderImageView = (RoundedImageView) findViewById(R.id.userinfo_header);
        mHeaderText = (TextView) findViewById(R.id.userinfo_header_text);
        mUserInfoName = (EditText) findViewById(R.id.userinfo_name);
        mUserinfoSignature = (EditText) findViewById(R.id.userinfo_signature);
        mGenderMaleBtn = (RadioButton) findViewById(R.id.userinfo_gender_male);
        mGenderFemaleBtn = (RadioButton) findViewById(R.id.userinfo_gender_female);
        mAstroImageView = (ImageView) findViewById(R.id.userinfo_astro_icon);
        mHobbyMaleBtn = (RadioButton) findViewById(R.id.userinfo_hobby_male);
        mHobbyFemaleBtn = (RadioButton) findViewById(R.id.userinfo_hobby_female);
        mInviteFriendsBtn = (Button) findViewById(R.id.userinfo_invite_friends);
        mUserInfoDepartment = (EditText) findViewById(R.id.userinfo_department);

        SharedPreferences prefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        mUid = prefs.getString(CommonDataStructure.UID, "");
        mDBHelper = MarrySocialDBHelper.newInstance(this);
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);

        initOriginData();
    }

    private void initOriginData() {

        mHeaderImageView.setOnClickListener(this);
        mHeaderText.setOnClickListener(this);
        mGenderMaleBtn.setOnClickListener(this);
        mGenderFemaleBtn.setOnClickListener(this);
        mAstroImageView.setOnClickListener(this);
        mHobbyMaleBtn.setOnClickListener(this);
        mHobbyFemaleBtn.setOnClickListener(this);
        mInviteFriendsBtn.setOnClickListener(this);

        mUserInfoName.addTextChangedListener(mNameTextChangeListener);
        mUserinfoSignature.addTextChangedListener(mSignatureTextChangeListener);
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
        case R.id.userinfo_header: {
            showHeaderPicsPicker(this, true);
            if (mCropPhoto != null) {
                mCropPhoto = null;
            }
            break;
        }
        case R.id.userinfo_header_text: {
            showHeaderPicsPicker(this, true);
            if (mCropPhoto != null) {
                mCropPhoto = null;
            }
            break;
        }
        case R.id.userinfo_gender_male: {
            mGender = 1;
            mGenderFemaleBtn.setChecked(false);
            break;
        }
        case R.id.userinfo_gender_female: {
            mGender = 0;
            mGenderMaleBtn.setChecked(false);
            break;
        }
        case R.id.userinfo_astro_icon: {
            showAstroPicker();
            break;
        }
        case R.id.userinfo_hobby_male: {
            mHobby = 1;
            mHobbyFemaleBtn.setChecked(false);
            break;
        }
        case R.id.userinfo_hobby_female: {
            mHobby = 0;
            mHobbyMaleBtn.setChecked(false);
            break;
        }
        case R.id.userinfo_invite_friends: {
            // startToInviteFriends();
            if (inUserInfoValid()) {
                mHandler.sendEmptyMessage(START_TO_UPLOAD_USER_INFO);
            }
            break;
        }
        default:
            break;
        }
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

            mHandler.sendEmptyMessage(UPLOAD_USER_HEADER_FINISH);
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

    private void insertHeadPicToHeadPicsDB(
            CommonDataStructure.UploadHeadPicResultEntry headPic) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_UID, headPic.uid);
        insertValues.put(MarrySocialDBHelper.KEY_HEAD_PIC_BITMAP,
                Utils.Bitmap2Bytes(mCropPhoto));
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH,
                headPic.orgUrl);
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_LOCAL_PATH,
                mCropPhotoName);
        insertValues.put(MarrySocialDBHelper.KEY_PHOTO_REMOTE_THUMB_PATH,
                headPic.smallThumbUrl);
        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.UPLOAD_TO_CLOUD_SUCCESS);

        try {
            ContentResolver resolver = getContentResolver();
            resolver.insert(CommonDataStructure.HEADPICSURL, insertValues);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

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

        try {
            resolver.update(CommonDataStructure.HEADPICSURL, insertValues,
                    whereClause, null);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

    private void showHeaderPicsPicker(Context context, boolean isCrop) {

        final boolean needCrop = isCrop;

        mSelectHeaderPicDialog = new SelectHeaderPicDialog(context);
        mSelectHeaderPicDialog
                .setOnSmallItemClickListener(new OnSmallItemClickListener() {

                    // 类型码
                    int REQUEST_CODE;

                    @Override
                    public void onCameraBtnClick() {

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
                            ImageUtils.deletePhotoAtPathAndName(
                                    CommonDataStructure.HEAD_PICS_DIR_URL,
                                    sharedPreferences.getString(
                                            CommonDataStructure.HEAD_PIC_NAME,
                                            ""));

                            // 保存本次截图临时文件名字
                            fileName = "head_pic_" + mUid + ".jpg";
                            Editor editor = sharedPreferences.edit();
                            editor.putString(CommonDataStructure.HEAD_PIC_NAME,
                                    fileName);
                            editor.commit();
                        } else {
                            REQUEST_CODE = TAKE_PICTURE_FROM_CAMERA;
                            fileName = "image.jpg";
                        }
                        imageUri = Uri
                                .fromFile(new File(
                                        CommonDataStructure.HEAD_PICS_DIR_URL,
                                        fileName));
                        // 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
                        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                imageUri);
                        startActivityForResult(openCameraIntent, REQUEST_CODE);
                        mSelectHeaderPicDialog.dismiss();
                    }

                    @Override
                    public void onGalleryBtnClick() {

                        Intent openGalleryIntent = new Intent(
                                Intent.ACTION_PICK);
                        if (needCrop) {
                            REQUEST_CODE = NEED_CROP;
                        } else {
                            REQUEST_CODE = CHOOSE_PICTURE_FROM_GALLERY;
                        }
                        openGalleryIntent.setDataAndType(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                "image/*");
                        startActivityForResult(openGalleryIntent, REQUEST_CODE);
                        mSelectHeaderPicDialog.dismiss();
                    }
                });
        mSelectHeaderPicDialog.show();

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
                if (uri != null) {
                    cropImage(uri, Utils.mCropCenterThumbPhotoWidth,
                            Utils.mCropCenterThumbPhotoWidth, CROP_PICTURE);
                }

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
                mCropPhotoName = "head_pic_" + mUid + ".jpg";
                ImageUtils.savePhotoToSDCard(mCropPhoto,
                        CommonDataStructure.HEAD_PICS_DIR_URL, mCropPhotoName);

                mUserHeadPic = mCropPhoto;
                mHeaderImageView.setImageBitmap(mUserHeadPic);
                mHeaderText.setVisibility(View.GONE);

                // mHandler.sendEmptyMessage(START_TO_UPLOAD_USER_HEADER);
                // iv_image.setImageBitmap(mCropPhoto);//联网才能上传照片
                break;
            }

            default:
                break;
            }
        }
    }

    private void showAstroPicker() {
        mAstroDialog = new SelectAstroDialog(this, onAstroItemClick);
        mAstroDialog.show();
    }

    SelectAstroGridViewAdapter.OnAstroItemClickListener onAstroItemClick = new SelectAstroGridViewAdapter.OnAstroItemClickListener() {
        public void onItemClick(int position) {
            mAstroImageView.setImageResource(ASTRO_ICON[position]);
            mAstroDialog.dismiss();
            mAstro = position;
        };
    };

    class UploadUserInfo implements Runnable {

        private String uid;

        public UploadUserInfo(String uid) {
            this.uid = uid;
        }

        @Override
        public void run() {
            String nickname = mUserInfoName.getText().toString();
            String intro = mUserinfoSignature.getText().toString();
            String department = mUserInfoDepartment.getText().toString();
            String result = Utils.updateUserInfo(
                    CommonDataStructure.URL_UPDATE_USER_INFO, uid, nickname,
                    mGender, mAstro, mHobby, intro);

            if (result != null && result.length() != 0) {
                mHandler.sendEmptyMessage(UPLOAD_USER_INFO_FINISH);
                // mHandler.sendEmptyMessage(START_TO_UPLOAD_USER_HEADER);
            } else {
                mHandler.sendEmptyMessage(UPLOAD_USER_INFO_FAIL);
            }
        }
    }

    private boolean inUserInfoValid() {
        // if (mUserHeadPic == null) {
        // Toast.makeText(this, "头像不能为空", 500).show();
        // return false;
        // }
        if (mUserInfoName.getText().toString().length() == 0) {
            mUserInfoName.requestFocus();
            Toast.makeText(this, "昵称不能为空", 500).show();
            return false;
        }
        // if (mUserinfoSignature.getText().toString().length() == 0) {
        // mUserinfoSignature.requestFocus();
        // Toast.makeText(this, "个性签名不能为空", 500).show();
        // return false;
        // }
        return true;
    }

    private void startToInviteFriends() {
        Intent intent = new Intent(this, InviteFriendsActivity.class);
        startActivity(intent);
    }
}
