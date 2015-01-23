package com.dhn.marrysocial.activity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.utils.AESecretUtils;
import com.dhn.marrysocial.utils.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener {

    private static final String TAG = "RegisterActivity";

    private static final int POOL_SIZE = 10;
    private static final int REGISTER_SUCCESS = 100;

    private EditText mPhoneNumEditText;
    private EditText mPasswordEditText;
    private EditText mVerifyCodeEditText;
    private Button mGetVerifyCodeBtn;
    private Button mRegisterBtn;
    private Button mLoginBtn;

    private String mUid;
    private String mPhoneNum;
    private String mPassword;
    private CountTimer mCountTimer;
    private ExecutorService mExecutorService;
    private ProgressDialog mUploadProgressDialog;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case REGISTER_SUCCESS: {
                mUploadProgressDialog.dismiss();
                SharedPreferences prefs = RegisterActivity.this
                        .getSharedPreferences(
                                CommonDataStructure.PREFS_LAIQIAN_DEFAULT,
                                MODE_PRIVATE);
                Editor editor = prefs.edit();
                editor.putString(CommonDataStructure.UID, mUid);
                editor.putString(CommonDataStructure.PHONE, mPhoneNum);
                editor.putInt(CommonDataStructure.LOGINSTATUS, 1);
                editor.commit();
                startToFillUserinfo();
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
        setContentView(R.layout.register_layout);

        mPhoneNumEditText = (EditText) findViewById(R.id.register_phone_num);
        mPasswordEditText = (EditText) findViewById(R.id.register_password);
        mVerifyCodeEditText = (EditText) findViewById(R.id.input_verify_code);
        mGetVerifyCodeBtn = (Button) findViewById(R.id.get_verify_code);
        mRegisterBtn = (Button) findViewById(R.id.register_btn);
        mLoginBtn = (Button) findViewById(R.id.login_btn);

        mCountTimer = new CountTimer(60000, 1000);
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);

        mGetVerifyCodeBtn.setOnClickListener(this);
        mRegisterBtn.setOnClickListener(this);
        mLoginBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
        case R.id.register_btn: {

            if (!isPhoneNumValid()) {
                mPhoneNumEditText.requestFocus();
                break;
            }
            if (!isPasswordValid()) {
                mPasswordEditText.requestFocus();
                break;
            }
            mUploadProgressDialog = ProgressDialog.show(this, "用户注册",
                    "正在注册，请稍后...", false, true);
            try {
                mPassword = AESecretUtils.encrypt(
                        CommonDataStructure.KEY_SECRET_CODE, mPasswordEditText
                                .getText().toString());
            } catch (Exception e) {
            }
//            String macAddr = Utils.getMacAddress(this);
            String macAddr = "10:ak:44:jj:55:u8";
            mPhoneNum = mPhoneNumEditText.getText().toString();
            mExecutorService.execute(new RegisterUserInfo(mPhoneNum, mPassword,
                    macAddr));
            break;
        }
        case R.id.login_btn: {
            if (!isPhoneNumValid()) {
                mPhoneNumEditText.requestFocus();
                break;
            }
            if (!isPasswordValid()) {
                mPasswordEditText.requestFocus();
                break;
            }
            startToLogin();
            break;
        }
        case R.id.get_verify_code: {
            if (!isPhoneNumValid()) {
                mPhoneNumEditText.requestFocus();
                break;
            }
            if (!isPasswordValid()) {
                mPasswordEditText.requestFocus();
                break;
            }
            mCountTimer.start();
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

    class CountTimer extends CountDownTimer {

        public CountTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            mGetVerifyCodeBtn.setText("重获验证码");
            mGetVerifyCodeBtn.setClickable(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mGetVerifyCodeBtn.setClickable(false);
            mGetVerifyCodeBtn.setText(millisUntilFinished / 1000 + "s");
        }
    }

    class RegisterUserInfo implements Runnable {

        private String phoneNum;
        private String password;
        private String macAddr;

        public RegisterUserInfo(String phoneNum, String password, String macAddr) {
            this.phoneNum = phoneNum;
            this.password = password;
            this.macAddr = macAddr;
        }

        @Override
        public void run() {
            mUid = Utils.registerUserInfo(
                    CommonDataStructure.URL_USER_REGISTER, phoneNum, password,
                    macAddr);
            if (mUid != null && mUid.length() != 0) {
                mHandler.sendEmptyMessage(REGISTER_SUCCESS);
            }
        }
    }

    private void startToFillUserinfo() {
        Intent intent = new Intent(this, FillUserInfoActivity.class);
        startActivity(intent);
    }

    private void startToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
