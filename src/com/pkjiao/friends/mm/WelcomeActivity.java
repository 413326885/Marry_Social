package com.pkjiao.friends.mm;

import com.dhn.marrysocial.R;
import com.pkjiao.friends.mm.activity.FillUserInfoActivity;
import com.pkjiao.friends.mm.activity.InviteFriendsActivity;
import com.pkjiao.friends.mm.activity.LoginActivity;
import com.pkjiao.friends.mm.activity.RegisterActivity;
import com.pkjiao.friends.mm.common.CommonDataStructure;
import com.pkjiao.friends.mm.services.DownloadIndirectFriendsIntentServices;
import com.pkjiao.friends.mm.test.MainActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class WelcomeActivity extends Activity {

    @SuppressWarnings("unused")
    private static final String TAG = "WelcomeActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        SharedPreferences prefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        boolean isFirstStartUp = prefs.getBoolean(
                CommonDataStructure.IS_FIRST_STARTUP, true);
//        if (!isFirstStartUp) {
//            redirectToCorrespondActivity();
//            return;
//        }
        // uploadUserContacts();
        // downloadUserContacts();

        Editor editor = prefs.edit();
        editor.putBoolean(CommonDataStructure.IS_FIRST_STARTUP, false);
        // editor.putString(CommonDataStructure.UID, "3");
        // editor.putString(CommonDataStructure.AUTHOR_NAME, "nannan");
        editor.commit();

        final View view = View.inflate(this, R.layout.welcome_layout, null);
        setContentView(view);

        AlphaAnimation animation = new AlphaAnimation(0.3f, 1.0f);
        animation.setDuration(100);
        view.startAnimation(animation);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                redirectToCorrespondActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }

        });

    }

    private void redirectToCorrespondActivity() {
        SharedPreferences prefs = getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        int login_status = prefs.getInt(CommonDataStructure.LOGINSTATUS, 0);
        switch (login_status) {
        case CommonDataStructure.LOGIN_STATUS_REGISTERED: {
            redirectToFillUserInfoActivity();
            break;
        }
        case CommonDataStructure.LONIN_STATUS_FILLED_INFO: {
            redirectToInviteFriendsActivity();
            break;
        }
        case CommonDataStructure.LOGIN_STATUS_LOGIN: {
            redirectToMainActivity();
            break;
        }
        case CommonDataStructure.LONIN_STATUS_LOGOUT: {
            redirectToLoginActivity();
            break;
        }
        default:
            redirectToRegisterActivity();
            break;
        }
    }

    private void redirectToFillUserInfoActivity() {
        Intent intent = new Intent(this, FillUserInfoActivity.class);
        startActivity(intent);
        finish();
    }

    private void redirectToInviteFriendsActivity() {
        Intent intent = new Intent(this, InviteFriendsActivity.class);
        startActivity(intent);
        finish();
    }

    private void redirectToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(this, MarrySocialMainActivity.class);
        startActivity(intent);
        finish();
    }

    private void redirectToRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    private void uploadUserContacts() {
        // Intent serviceIntent = new Intent(this,
        // ReadContactsIntentService.class);
        // startService(serviceIntent);
    }

    private void downloadUserContacts() {
        Intent serviceIntent = new Intent(this,
                DownloadIndirectFriendsIntentServices.class);
        startService(serviceIntent);
    }
}
