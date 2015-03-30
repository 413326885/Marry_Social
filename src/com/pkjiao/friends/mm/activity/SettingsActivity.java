package com.pkjiao.friends.mm.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pkjiao.friends.mm.R;
import com.pkjiao.friends.mm.MarrySocialMainActivity;
import com.pkjiao.friends.mm.base.DataCleanManager;
import com.pkjiao.friends.mm.common.CommonDataStructure;
import com.pkjiao.friends.mm.dialog.ExitAppDialog;
import com.pkjiao.friends.mm.dialog.ExitAppDialog.OnConfirmBtnClickListener;

public class SettingsActivity extends Activity implements OnClickListener {

    private static final String TAG = "SettingsActivity";

    private RelativeLayout mReturnBtn;
    private RelativeLayout mVersionUpdate;
    private TextView mAboutUsBtn;
    private TextView mFuncDescBtn;
    private TextView mChangePassword;
    private TextView mLogoutBtn;
    private TextView mUserFeedback;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.settings_layout);

        mContext = this;

        mReturnBtn = (RelativeLayout) findViewById(R.id.settings_return);
        mVersionUpdate = (RelativeLayout) findViewById(R.id.version_update);
        mAboutUsBtn = (TextView) findViewById(R.id.about_us);
        mFuncDescBtn = (TextView) findViewById(R.id.func_desc);
        mUserFeedback = (TextView) findViewById(R.id.user_feedback);
        mChangePassword = (TextView) findViewById(R.id.change_password);
        mLogoutBtn = (TextView) findViewById(R.id.logout);

        mReturnBtn.setOnClickListener(this);
        mVersionUpdate.setOnClickListener(this);
        mAboutUsBtn.setOnClickListener(this);
        mFuncDescBtn.setOnClickListener(this);
        mUserFeedback.setOnClickListener(this);
        mChangePassword.setOnClickListener(this);
        mLogoutBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.settings_return: {
            this.finish();
            break;
        }
        case R.id.version_update: {
            break;
        }
        case R.id.about_us: {
            startToViewAboutUs();
            break;
        }
        case R.id.func_desc: {
            startToViewProductDesc();
            break;
        }
        case R.id.user_feedback: {
            startToFeedbck();
            break;
        }
        case R.id.change_password: {
            startToChangePassword();
            break;
        }
        case R.id.logout: {
            showLogoutDialog(this);
            break;
        }
        default:
            break;
        }
    }

    private void startToFeedbck() {
        Intent intent = new Intent(this, UserFeedbackActivity.class);
        startActivity(intent);
    }

    private void startToChangePassword() {
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        intent.putExtra(CommonDataStructure.PASSWORD, "修改密码");
        startActivity(intent);
    }

    private void startToViewAboutUs() {
        Intent intent = new Intent(this, AboutUsActivity.class);
        startActivity(intent);
    }

    private void startToViewProductDesc() {
        Intent intent = new Intent(this, ProductDescActivity.class);
        startActivity(intent);
    }

    private void showLogoutDialog(final Context context) {

        ExitAppDialog exitDialog = new ExitAppDialog(context);
        exitDialog.setOnConfirmBtnClickListener(mConfirmBtnClickListener);
        exitDialog.show();
    }

    private OnConfirmBtnClickListener mConfirmBtnClickListener = new OnConfirmBtnClickListener() {

        @Override
        public void onConfirmBtnClick() {
            SharedPreferences prefs = mContext.getSharedPreferences(
                    CommonDataStructure.PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
            Editor editor = prefs.edit();
            editor.putInt(CommonDataStructure.LOGINSTATUS,
                    CommonDataStructure.LOGIN_STATUS_NO_USER);
            editor.commit();
            DataCleanManager.cleanApplicationData(mContext,
                    CommonDataStructure.DOWNLOAD_PICS_DIR_URL,
                    CommonDataStructure.HEAD_PICS_DIR_URL,
                    CommonDataStructure.BACKGROUND_PICS_DIR_URL);
            redirectToRegisterActivity();
        }

    };

    private void redirectToRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
