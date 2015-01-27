package com.dhn.marrysocial.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dhn.marrysocial.R;

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
            break;
        }
        case R.id.logout: {
            break;
        }
        default:
            break;
        }
    }

    private void startToViewAboutUs() {
        Intent intent = new Intent(this, AboutUsActivity.class);
        startActivity(intent);
    }

    private void startToViewProductDesc() {
        Intent intent = new Intent(this, ProductDescActivity.class);
        startActivity(intent);
    }
}
