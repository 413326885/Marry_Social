package com.dhn.marrysocial.activity;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.utils.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

    private static final String TAG = "RegisterActivity";

    private EditText mPhoneNumEditText;
    private EditText mPasswordEditText;
    private Button mLoginBtn;
    private TextView mForgetPassword;
    private TextView mReturnBack;

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
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.login_btn: {
            if (!isPhoneNumValid()) {
                mPhoneNumEditText.requestFocus();
                break;
            }
            if (!isPasswordValid()) {
                mPasswordEditText.requestFocus();
                break;
            }
            break;
        }
        case R.id.forget_password: {
            break;
        }
        case R.id.return_back: {
            this.finish();
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
}
