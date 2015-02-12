package com.pkjiao.friends.mm.activity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dhn.marrysocial.R;
import com.pkjiao.friends.mm.broadcast.receive.AuthCodeBroadcastReceiver;
import com.pkjiao.friends.mm.broadcast.receive.NewTipsBroadcastReceiver;
import com.pkjiao.friends.mm.common.CommonDataStructure;
import com.pkjiao.friends.mm.dialog.ProgressLoadDialog;
import com.pkjiao.friends.mm.utils.AuthCodeUtils;
import com.pkjiao.friends.mm.utils.MD5SecretUtils;
import com.pkjiao.friends.mm.utils.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener {

    private static final String TAG = "RegisterActivity";

    private static final int POOL_SIZE = 10;
    private static final int REGISTER_SUCCESS = 100;
    private static final int NETWORK_INVALID = 101;
    private static final int START_TO_SEND_AUTH_CODE = 102;

    private EditText mPhoneNumEditText;
    private EditText mPasswordEditText;
    private EditText mVerifyCodeEditText;
    private Button mGetVerifyCodeBtn;
    private Button mRegisterBtn;
    private TextView mLoginBtn;

    private String mUid;
    private String mPhoneNum;
    private String mPassword;
    private CountTimer mCountTimer;
    private ExecutorService mExecutorService;
    private ProgressLoadDialog mUploadProgressDialog;
    private AuthCodeBroadcastReceiver mBroadcastReceiver;
    private SharedPreferences mPrefs;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case REGISTER_SUCCESS: {
                mUploadProgressDialog.dismiss();
                Editor editor = mPrefs.edit();
                editor.putString(CommonDataStructure.UID, mUid);
                editor.putString(CommonDataStructure.PHONE, mPhoneNum);
                editor.putInt(CommonDataStructure.LOGINSTATUS,
                        CommonDataStructure.LOGIN_STATUS_REGISTERED);
                editor.commit();
                startToFillUserinfo();
                break;
            }
            case NETWORK_INVALID: {
                Toast.makeText(RegisterActivity.this,
                        R.string.network_not_available, Toast.LENGTH_SHORT)
                        .show();
                break;
            }
            case START_TO_SEND_AUTH_CODE: {
                String macAddr = Utils.getMacAddress(RegisterActivity.this);
                String authCode = AuthCodeUtils.randAuthCode(6);
                Editor editor = mPrefs.edit();
                editor.putString(CommonDataStructure.AUTH_CODE, authCode);
                editor.commit();
                mPhoneNum = mPhoneNumEditText.getText().toString();
                mExecutorService.execute(new SendAuthCode(mPhoneNum, macAddr,
                        authCode));
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
        mLoginBtn = (TextView) findViewById(R.id.login_btn);

        mCountTimer = new CountTimer(60000, 1000);
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);
        mPrefs = getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);

        mGetVerifyCodeBtn.setOnClickListener(this);
        mRegisterBtn.setOnClickListener(this);
        mLoginBtn.setOnClickListener(this);

        mBroadcastReceiver = new AuthCodeBroadcastReceiver();
        mBroadcastReceiver.setOnReceivedMessageListener(mBroadcastListener);
        registerReceiver(mBroadcastReceiver, getIntentFilter());
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
        case R.id.register_btn: {
//            startToFillUserinfo();
//            return;
            if (!Utils.isActiveNetWorkAvailable(this)) {
                mHandler.sendEmptyMessage(NETWORK_INVALID);
                return;
            }

            if (!isPhoneNumValid()) {
                mPhoneNumEditText.requestFocus();
                return;
            }
            if (!isPasswordValid()) {
                mPasswordEditText.requestFocus();
                return;
            }
            if (!isAuthCodeValid()) {
                return;
            }

            mUploadProgressDialog = new ProgressLoadDialog(this);
            mUploadProgressDialog.setText("正在注册，请稍后...");
            mUploadProgressDialog.show();

            mPassword = MD5SecretUtils.encrypt(mPasswordEditText.getText()
                    .toString());
            String macAddr = Utils.getMacAddress(this);
            mPhoneNum = mPhoneNumEditText.getText().toString();
            mExecutorService.execute(new RegisterUserInfo(mPhoneNum, mPassword,
                    macAddr));
            break;
        }
        case R.id.login_btn: {
            startToLogin();
            break;
        }
        case R.id.get_verify_code: {

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

            mCountTimer.start();
            mHandler.sendEmptyMessage(START_TO_SEND_AUTH_CODE);
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

    private boolean isAuthCodeValid() {
        String authCode = mPrefs.getString(CommonDataStructure.AUTH_CODE, "");
        String confirmCode = mVerifyCodeEditText.getText().toString();
        if (authCode == null || authCode.length() == 0 || confirmCode == null || confirmCode.length() == 0) {
            Toast.makeText(this, "验证码不能为空", 500).show();
            return false;
        }
        if (!confirmCode.equalsIgnoreCase(authCode)) {
            Toast.makeText(this, "验证码已经失效", 500).show();
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
            mGetVerifyCodeBtn.setText("重获验证码 (" + millisUntilFinished / 1000
                    + "s" + ")");
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
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    class SendAuthCode implements Runnable {

        private String phoneNum;
        private String authCode;
        private String macAddr;

        public SendAuthCode(String phoneNum, String macAddr, String authCode) {
            this.phoneNum = phoneNum;
            this.macAddr = macAddr;
            this.authCode = authCode;
        }

        @Override
        public void run() {
            Boolean result = Utils.sendAuthCode(
                    CommonDataStructure.URL_SEND_AUTHCODE, phoneNum, macAddr,
                    authCode);
            // if (result) {
            // }
        }
    }

    private AuthCodeBroadcastReceiver.MessageListener mBroadcastListener = new AuthCodeBroadcastReceiver.MessageListener() {

        @Override
        public void onMsgReceived(String message) {
            mVerifyCodeEditText.setText(message);
            mCountTimer.cancel();
        }

    };

    private IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AuthCodeBroadcastReceiver.SMS_RECEIVED_ACTION);
        return intentFilter;
    }
}
