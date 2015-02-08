package com.dhn.marrysocial.activity;

import android.app.Activity;
import android.app.AlertDialog;
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

import com.dhn.marrysocial.MarrySocialMainActivity;
import com.dhn.marrysocial.R;
import com.dhn.marrysocial.base.DataCleanManager;
import com.dhn.marrysocial.common.CommonDataStructure;

public class SettingsActivity extends Activity implements OnClickListener {

    private static final String TAG = "SettingsActivity";

    private RelativeLayout mReturnBtn;
    private RelativeLayout mVersionUpdate;
    private TextView mAboutUsBtn;
    private TextView mFuncDescBtn;
    private TextView mChangePassword;
    private TextView mLogoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.settings_layout);
        mReturnBtn = (RelativeLayout) findViewById(R.id.settings_return);
        mVersionUpdate = (RelativeLayout) findViewById(R.id.version_update);
        mAboutUsBtn = (TextView) findViewById(R.id.about_us);
        mFuncDescBtn = (TextView) findViewById(R.id.func_desc);
        mChangePassword = (TextView) findViewById(R.id.change_password);
        mLogoutBtn = (TextView) findViewById(R.id.logout);

        mReturnBtn.setOnClickListener(this);
        mVersionUpdate.setOnClickListener(this);
        mAboutUsBtn.setOnClickListener(this);
        mFuncDescBtn.setOnClickListener(this);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("退出登录");
        builder.setMessage("退出当前账号？");
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定",
                new android.content.DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        SharedPreferences prefs = context.getSharedPreferences(
                                CommonDataStructure.PREFS_LAIQIAN_DEFAULT,
                                MODE_PRIVATE);
                        Editor editor = prefs.edit();
                        editor.putInt(CommonDataStructure.LOGINSTATUS,
                                CommonDataStructure.LOGIN_STATUS_NO_USER);
                        editor.commit();
                        DataCleanManager.cleanApplicationData(context,
                                CommonDataStructure.DOWNLOAD_PICS_DIR_URL,
                                CommonDataStructure.HEAD_PICS_DIR_URL,
                                CommonDataStructure.BACKGROUND_PICS_DIR_URL);
                        redirectToRegisterActivity();
                    }
                });
        builder.create().show();
    }

    private void redirectToRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
