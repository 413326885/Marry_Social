package com.pkjiao.friends.mm.activity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dhn.marrysocial.R;
import com.pkjiao.friends.mm.MarrySocialMainActivity;
import com.pkjiao.friends.mm.base.ContactsInfo;
import com.pkjiao.friends.mm.common.CommonDataStructure;
import com.pkjiao.friends.mm.dialog.ProgressLoadDialog;
import com.pkjiao.friends.mm.utils.MD5SecretUtils;
import com.pkjiao.friends.mm.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

    private static final String TAG = "RegisterActivity";

    private static final int POOL_SIZE = 2;
    private static final int LOGIN_SUCCESS = 100;
    private static final int LOGIN_FAIL = 101;
    private static final int NEEDFILLUSERINFO = 102;
    private static final int NOTNEEDFILLUSERINFO = 103;
    private static final int NETWORK_INVALID = 104;

    private EditText mPhoneNumEditText;
    private EditText mPasswordEditText;
    private Button mLoginBtn;
    private TextView mForgetPassword;
    private TextView mReturnBack;

    private String mUid;
    private String mPhoneNum;
    private String mPassword;
    private String mAuthorName;
    private ExecutorService mExecutorService;
    private ProgressLoadDialog mUploadProgressDialog;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case LOGIN_SUCCESS: {
                mUploadProgressDialog.dismiss();
                SharedPreferences prefs = LoginActivity.this
                        .getSharedPreferences(
                                CommonDataStructure.PREFS_LAIQIAN_DEFAULT,
                                MODE_PRIVATE);
                Editor editor = prefs.edit();
                editor.putString(CommonDataStructure.UID, mUid);
                editor.putString(CommonDataStructure.PHONE, mPhoneNum);
                editor.putString(CommonDataStructure.AUTHOR_NAME, mAuthorName);
                editor.putInt(CommonDataStructure.LOGINSTATUS,
                        CommonDataStructure.LOGIN_STATUS_LOGIN);
                editor.commit();
                if (msg.arg1 == NEEDFILLUSERINFO) {
                    redirectToFillUserInfoActivity();
                } else {
                    startToMainActivity();
                }
                break;
            }
            case LOGIN_FAIL: {
                Toast.makeText(LoginActivity.this, "手机号与密码不匹配", 500).show();
                mUploadProgressDialog.dismiss();
                break;
            }
            case NETWORK_INVALID: {
                Toast.makeText(LoginActivity.this,
                        R.string.network_not_available, Toast.LENGTH_SHORT)
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
        setContentView(R.layout.login_layout);
        mPhoneNumEditText = (EditText) findViewById(R.id.login_phone_num);
        mPasswordEditText = (EditText) findViewById(R.id.login_password);
        mLoginBtn = (Button) findViewById(R.id.login_btn);
        mForgetPassword = (TextView) findViewById(R.id.forget_password);
        mReturnBack = (TextView) findViewById(R.id.return_back);

        mLoginBtn.setOnClickListener(this);
        mForgetPassword.setOnClickListener(this);
        mReturnBack.setOnClickListener(this);

        SharedPreferences prefs = LoginActivity.this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        mUid = prefs.getString(CommonDataStructure.UID, "");
        mPhoneNum = prefs.getString(CommonDataStructure.PHONE, "");
        if (mPhoneNum.length() != 0) {
            mPhoneNumEditText.setText(mPhoneNum);
            mPasswordEditText.requestFocus();
        }
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.login_btn: {

            if (!Utils.isActiveNetWorkAvailable(this)) {
                mHandler.sendEmptyMessage(NETWORK_INVALID);
                return;
            }

            if (!isPhoneNumValid()) {
                mPhoneNumEditText.requestFocus();
                break;
            }
            if (!isPasswordValid()) {
                mPasswordEditText.requestFocus();
                break;
            }

            mUploadProgressDialog = new ProgressLoadDialog(this);
            mUploadProgressDialog.setText("正在登录系统，请稍后...");
            mUploadProgressDialog.show();

            mPassword = MD5SecretUtils.encrypt(mPasswordEditText.getText()
                    .toString());
            String macAddr = Utils.getMacAddress(this);
            mPhoneNum = mPhoneNumEditText.getText().toString();
            mExecutorService.execute(new LoginSystem(mPhoneNum, mPassword,
                    macAddr));
            break;
        }
        case R.id.forget_password: {
            redirectToForgetPasswordActivity();
            break;
        }
        case R.id.return_back: {
            redirectToRegisterActivity();
            break;
        }
        default:
            break;
        }
    }

    private boolean isPhoneNumValid() {
        if (!Utils.isMobilePhoneNum(mPhoneNumEditText.getText().toString())) {
            Toast.makeText(this, "不是有效手机号码", 500).show();
            return false;
        }
        return true;
    }

    private boolean isPasswordValid() {
        if (!Utils.isPassworkValid(mPasswordEditText.getText().toString())) {
            Toast.makeText(this, "密码长度必须大于6位", 500).show();
            return false;
        }
        return true;
    }

    class LoginSystem implements Runnable {

        private String phoneNum;
        private String password;
        private String macAddr;

        public LoginSystem(String phoneNum, String password, String macAddr) {
            this.phoneNum = phoneNum;
            this.password = password;
            this.macAddr = macAddr;
        }

        @Override
        public void run() {
            mUid = Utils.loginSystem(CommonDataStructure.URL_USER_LOGIN,
                    phoneNum, password, macAddr);
            if (mUid != null && mUid.length() != 0) {
                ContactsInfo authorInfo = Utils.downloadUserInfo(
                        CommonDataStructure.URL_GET_USER_PROFILE, mUid);
                mAuthorName = authorInfo.getNickName();
                Message msg = mHandler.obtainMessage(LOGIN_SUCCESS);
                if (authorInfo == null) {
                    msg.arg1 = NEEDFILLUSERINFO;
                } else {
                    msg.arg1 = NOTNEEDFILLUSERINFO;
                }
                mHandler.sendMessage(msg);
            } else {
                mHandler.sendEmptyMessage(LOGIN_FAIL);
            }
        }
    }

    private void startToMainActivity() {
        Intent intent = new Intent(this, MarrySocialMainActivity.class);
        startActivity(intent);
        finish();
    }

    private void redirectToFillUserInfoActivity() {
        Intent intent = new Intent(this, FillUserInfoActivity.class);
        startActivity(intent);
    }

    private void redirectToForgetPasswordActivity() {
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        intent.putExtra(CommonDataStructure.PASSWORD, "忘记密码");
        startActivity(intent);
    }

    private void redirectToRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        this.finish();
    }
}
