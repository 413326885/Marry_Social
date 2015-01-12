package com.dhn.marrysocial;

import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.services.DownloadIndirectFriendsIntentServices;
import com.dhn.marrysocial.services.ReadContactsIntentService;
import com.dhn.marrysocial.test.MainActivity;

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
        boolean isFirstStartUp = prefs.getBoolean(CommonDataStructure.IS_FIRST_STARTUP, true);
        if (!isFirstStartUp) {
            redirectToMainActivity();
            return;
        }
        uploadUserContacts();
        downloadUserContacts();

        Editor editor = prefs.edit();
        editor.putBoolean(CommonDataStructure.IS_FIRST_STARTUP, false);
        editor.putString(CommonDataStructure.UID, "3");
        editor.putString(CommonDataStructure.AUTHOR_NAME, "nannan");
        editor.commit();

        final View view = View.inflate(this, R.layout.welcome_layout, null);
        setContentView(view);

        AlphaAnimation animation = new AlphaAnimation(0.3f, 1.0f);
        animation.setDuration(2000);
        view.startAnimation(animation);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                redirectToMainActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }

        });

    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(this, MarrySocialMainActivity.class);
//        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void uploadUserContacts() {
//        Intent serviceIntent = new Intent(this, ReadContactsIntentService.class);
//        startService(serviceIntent);
    }

    private void downloadUserContacts() {
      Intent serviceIntent = new Intent(this, DownloadIndirectFriendsIntentServices.class);
      startService(serviceIntent);
    }
}
