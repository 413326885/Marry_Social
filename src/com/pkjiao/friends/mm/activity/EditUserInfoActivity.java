package com.pkjiao.friends.mm.activity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.pkjiao.friends.mm.R;
import com.pkjiao.friends.mm.adapter.SelectAstroGridViewAdapter;
import com.pkjiao.friends.mm.common.CommonDataStructure;
import com.pkjiao.friends.mm.database.MarrySocialDBHelper;
import com.pkjiao.friends.mm.dialog.ProgressLoadDialog;
import com.pkjiao.friends.mm.dialog.SelectAstroDialog;
import com.pkjiao.friends.mm.utils.Utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EditUserInfoActivity extends Activity implements OnClickListener {

    private static final String TAG = "EditUserInfoActivity";

    private static final int POOL_SIZE = 10;
    private static final int START_TO_UPLOAD_USER_INFO = 104;
    private static final int UPLOAD_USER_INFO_FINISH = 105;
    private static final int UPLOAD_USER_INFO_FAIL = 106;
    private static final int REQUEST_CODE_CHANGE_NAME = 2000;
    private static final int REQUEST_CODE_CHANGE_INTRODUCE = 2001;
    private static final int REQUEST_CODE_CHANGE_PROFESSION = 2002;

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

    private static final String[] CONTACTS_PROJECTION = {
            MarrySocialDBHelper.KEY_NICKNAME, MarrySocialDBHelper.KEY_GENDER,
            MarrySocialDBHelper.KEY_ASTRO, MarrySocialDBHelper.KEY_HOBBY,
            MarrySocialDBHelper.KEY_INTRODUCT,
            MarrySocialDBHelper.KEY_PROFESSION };

    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String EDITINFO = "editinfo";

    private String mUserInfoUid;
    private String mAuthorUid;
    private Context mContext;
    private MarrySocialDBHelper mDBHelper;
    private ExecutorService mExecutorService;
    private SelectAstroDialog mAstroDialog;
    private ProgressLoadDialog mUploadUserInfoProgressDialog;

    private RelativeLayout mReturnBtn;
    private RelativeLayout mEditUserNameGroup;
    private RelativeLayout mEditUserSignatureGroup;
    private RelativeLayout mEditUserProfessionGroup;
    private RelativeLayout mEditUserGenderGroup;
    private RelativeLayout mEditUserAstroGroup;
    private TextView mUserName;
    private TextView mUserSignature;
    private TextView mUserProfession;
    private RadioButton mUserGender;
    private ImageView mUserAstro;
    private Button mFinishBtn;

    private int mGender = 1;
    private int mAstro = 1;
    private int mHobby = 0;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case START_TO_UPLOAD_USER_INFO: {
                mUploadUserInfoProgressDialog = new ProgressLoadDialog(mContext);
                mUploadUserInfoProgressDialog.setText("正在修改个人信息，请稍后...");
                mUploadUserInfoProgressDialog.show();

                mExecutorService.execute(new UploadUserInfo());
                break;
            }
            case UPLOAD_USER_INFO_FINISH: {
                mUploadUserInfoProgressDialog.dismiss();
                Toast.makeText(EditUserInfoActivity.this, "修改个人信息成功", 500)
                        .show();
                EditUserInfoActivity.this.finish();
                break;
            }
            case UPLOAD_USER_INFO_FAIL: {
                mUploadUserInfoProgressDialog.dismiss();
                Toast.makeText(EditUserInfoActivity.this, "修改个人信息失败，请稍后重试", 500)
                        .show();
                break;
            }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_contacts_info_layout);

        mContext = this;
        Intent data = getIntent();
        mUserInfoUid = data.getStringExtra(MarrySocialDBHelper.KEY_UID);

        mReturnBtn = (RelativeLayout) findViewById(R.id.edit_info_return);
        mEditUserNameGroup = (RelativeLayout) findViewById(R.id.edit_info_name_group);
        mEditUserSignatureGroup = (RelativeLayout) findViewById(R.id.edit_info_signature_group);
        mEditUserProfessionGroup = (RelativeLayout) findViewById(R.id.edit_info_profession_group);
        mEditUserGenderGroup = (RelativeLayout) findViewById(R.id.edit_info_gender_group);
        mEditUserAstroGroup = (RelativeLayout) findViewById(R.id.edit_info_astro_group);
        mUserName = (TextView) findViewById(R.id.edit_info_name);
        mUserSignature = (TextView) findViewById(R.id.edit_info_signature);
        mUserProfession = (TextView) findViewById(R.id.edit_info_profession);
        mUserGender = (RadioButton) findViewById(R.id.edit_info_gender);
        mUserAstro = (ImageView) findViewById(R.id.edit_info_astro);
        mFinishBtn = (Button) findViewById(R.id.edit_info_finish);

        mReturnBtn.setOnClickListener(this);
        mEditUserNameGroup.setOnClickListener(this);
        mEditUserSignatureGroup.setOnClickListener(this);
        mEditUserProfessionGroup.setOnClickListener(this);
        mEditUserGenderGroup.setOnClickListener(this);
        mEditUserAstroGroup.setOnClickListener(this);
        mUserGender.setOnClickListener(this);
        mUserAstro.setOnClickListener(this);
        mFinishBtn.setOnClickListener(this);

        SharedPreferences prefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        mAuthorUid = prefs.getString(CommonDataStructure.UID, "");
        mDBHelper = MarrySocialDBHelper.newInstance(this);
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);

        loadUserInfoFromDB();

        if (mUserInfoUid.equalsIgnoreCase(mAuthorUid)) {
            mFinishBtn.setVisibility(View.VISIBLE);
            mEditUserNameGroup.setClickable(true);
            mEditUserSignatureGroup.setClickable(true);
            mEditUserProfessionGroup.setClickable(true);
            mEditUserGenderGroup.setClickable(true);
            mEditUserAstroGroup.setClickable(true);
        } else {
            mFinishBtn.setVisibility(View.GONE);
            mEditUserNameGroup.setClickable(false);
            mEditUserSignatureGroup.setClickable(false);
            mEditUserProfessionGroup.setClickable(false);
            mEditUserGenderGroup.setClickable(false);
            mEditUserAstroGroup.setClickable(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.edit_info_return: {
            this.finish();
            break;
        }
        case R.id.edit_info_name_group: {
            startToChangeNikename();
            break;
        }
        case R.id.edit_info_signature_group: {
            startToChangeIntroduce();
            break;
        }
        case R.id.edit_info_profession_group: {
            startToChangeProfession();
            break;
        }
        case R.id.edit_info_gender_group: {
        }
        case R.id.edit_info_gender: {
            if (mUserGender.isChecked()) {
                mUserGender.setChecked(false);
            } else {
                mUserGender.setChecked(true);
            }
            break;
        }
        case R.id.edit_info_astro_group: {
        }
        case R.id.edit_info_astro: {
            showAstroPicker();
            break;
        }
        case R.id.edit_info_finish: {
            mHandler.sendEmptyMessage(START_TO_UPLOAD_USER_INFO);
            break;
        }
        default:
            break;
        }
    }

    private void loadUserInfoFromDB() {

        MarrySocialDBHelper dbHelper = MarrySocialDBHelper.newInstance(this);
        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + mUserInfoUid;
        Cursor cursor = dbHelper.query(
                MarrySocialDBHelper.DATABASE_CONTACTS_TABLE,
                CONTACTS_PROJECTION, whereClause, null, null, null, null, null);
        if (cursor == null) {
            Log.w(TAG, "nannan query fail!");
            return;
        }

        try {
            while (cursor.moveToNext()) {
                String nickname = cursor.getString(0);
                int gender = Integer.valueOf(cursor.getInt(1));
                int astro = Integer.valueOf(cursor.getInt(2));
                int hobby = Integer.valueOf(cursor.getInt(3));
                String introduce = cursor.getString(4);
                String profession = cursor.getString(5);

                mUserName.setText(nickname);
                if (introduce == null || introduce.length() == 0) {
                    mUserSignature.setText("这家伙很懒，什么也没有留下...");
                } else {
                    mUserSignature.setText(introduce);
                }
                if (profession == null || profession.length() == 0) {
                    mUserProfession.setText("保密");
                } else {
                    mUserProfession.setText(profession);
                }
                mUserGender.setChecked(gender == 1);
                mUserAstro.setImageResource(ASTRO_ICON[astro]);
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

    class UploadUserInfo implements Runnable {

        @Override
        public void run() {
            String nickname = mUserName.getText().toString();
            String intro = mUserSignature.getText().toString();
            String profession = mUserProfession.getText().toString();
            mGender = (mUserGender.isChecked()) ? 1 : 0;
            String result = Utils.updateUserInfo(
                    CommonDataStructure.URL_UPDATE_USER_INFO, mAuthorUid, nickname,
                    mGender, mAstro, mHobby, intro, profession);

            if (result != null && result.length() != 0) {
                updateUserinfoToContactsDB(nickname, intro, profession,
                        mGender, mAstro);
                updateUserinfoToPreference(nickname, intro);
                updateAuthorNameToBravosDB(nickname);
                updateAuthorNameToCommentsDB(nickname);
                updateAuthorNameToReplysDB(nickname);
                mHandler.sendEmptyMessage(UPLOAD_USER_INFO_FINISH);
            } else {
                mHandler.sendEmptyMessage(UPLOAD_USER_INFO_FAIL);
            }
        }
    }

    private void showAstroPicker() {
        mAstroDialog = new SelectAstroDialog(this, onAstroItemClick);
        mAstroDialog.show();
    }

    SelectAstroGridViewAdapter.OnAstroItemClickListener onAstroItemClick = new SelectAstroGridViewAdapter.OnAstroItemClickListener() {
        public void onItemClick(int position) {
            mUserAstro.setImageResource(ASTRO_ICON[position]);
            mAstroDialog.dismiss();
            mAstro = position;
        };
    };

    private void startToChangeNikename() {
        Intent intent = new Intent(this, EditUserInfoItemActivity.class);
        intent.putExtra(TITLE, "更改姓名");
        intent.putExtra(DESCRIPTION, "好名字可以让你的朋友更容易记住你");
        intent.putExtra(EDITINFO, mUserName.getText().toString());
        startActivityForResult(intent, REQUEST_CODE_CHANGE_NAME);
    }

    private void startToChangeIntroduce() {
        Intent intent = new Intent(this, EditUserInfoItemActivity.class);
        intent.putExtra(TITLE, "更改签名");
        intent.putExtra(DESCRIPTION, "好的签名可以彰显你的个性");
        intent.putExtra(EDITINFO, mUserSignature.getText().toString());
        startActivityForResult(intent, REQUEST_CODE_CHANGE_INTRODUCE);
    }

    private void startToChangeProfession() {
        Intent intent = new Intent(this, EditUserInfoItemActivity.class);
        intent.putExtra(TITLE, "更改行业");
        intent.putExtra(DESCRIPTION, "填写行业可以帮你聚焦更多的相关好友");
        intent.putExtra(EDITINFO, mUserProfession.getText().toString());
        startActivityForResult(intent, REQUEST_CODE_CHANGE_PROFESSION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case REQUEST_CODE_CHANGE_NAME: {
                String name = data
                        .getStringExtra(CommonDataStructure.UPDATE_USER_INFO);
                mUserName.setText(name);
                break;
            }
            case REQUEST_CODE_CHANGE_INTRODUCE: {
                String intro = data
                        .getStringExtra(CommonDataStructure.UPDATE_USER_INFO);
                mUserSignature.setText(intro);
                break;
            }
            case REQUEST_CODE_CHANGE_PROFESSION: {
                String profession = data
                        .getStringExtra(CommonDataStructure.UPDATE_USER_INFO);
                mUserProfession.setText(profession);
                break;
            }
            default:
                break;
            }
        }
    }

    private void updateUserinfoToContactsDB(String nickname, String intro,
            String profession, int gender, int astro) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_NICKNAME, nickname);
        insertValues.put(MarrySocialDBHelper.KEY_INTRODUCT, intro);
        insertValues.put(MarrySocialDBHelper.KEY_PROFESSION, profession);
        insertValues.put(MarrySocialDBHelper.KEY_GENDER, gender);
        insertValues.put(MarrySocialDBHelper.KEY_ASTRO, astro);

        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + mAuthorUid;
        try {
            mDBHelper.update(MarrySocialDBHelper.DATABASE_CONTACTS_TABLE,
                    insertValues, whereClause, null);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    private void updateAuthorNameToBravosDB(String nickname) {
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_AUTHOR_NICKNAME, nickname);

        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + mAuthorUid;
        try {
            ContentResolver resolver = getContentResolver();
            resolver.update(CommonDataStructure.BRAVOURL, values, whereClause,
                    null);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    private void updateAuthorNameToCommentsDB(String nickname) {
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_AUTHOR_NICKNAME, nickname);

        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + mAuthorUid;
        try {
            ContentResolver resolver = getContentResolver();
            resolver.update(CommonDataStructure.COMMENTURL, values,
                    whereClause, null);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    private void updateAuthorNameToReplysDB(String nickname) {
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_AUTHOR_NICKNAME, nickname);

        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + mAuthorUid;
        try {
            ContentResolver resolver = getContentResolver();
            resolver.update(CommonDataStructure.REPLYURL, values,
                    whereClause, null);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    private void updateUserinfoToPreference(String nickname, String introduce) {
        SharedPreferences prefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString(CommonDataStructure.AUTHOR_NAME, nickname);
        editor.putString(CommonDataStructure.INTRODUCE, introduce);
        editor.commit();
    }
}
