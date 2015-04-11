package com.pkjiao.friends.mm;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.pkjiao.friends.mm.activity.ContactsInfoActivity;
import com.pkjiao.friends.mm.activity.InviteFriendsActivity;
import com.pkjiao.friends.mm.activity.RegisterActivity;
import com.pkjiao.friends.mm.activity.SettingsActivity;
import com.pkjiao.friends.mm.adapter.ViewPagerFragmentAdapter;
import com.pkjiao.friends.mm.base.NotificationManagerControl;
import com.pkjiao.friends.mm.broadcast.receive.NewTipsBroadcastReceiver;
import com.pkjiao.friends.mm.common.CommonDataStructure;
import com.pkjiao.friends.mm.database.MarrySocialDBHelper;
import com.pkjiao.friends.mm.dialog.UpdateAppDialog;
import com.pkjiao.friends.mm.dialog.UpdateAppDialog.OnConfirmBtnClickListener;
import com.pkjiao.friends.mm.fragment.ChatMsgFragment;
import com.pkjiao.friends.mm.fragment.ContactsListFragment;
import com.pkjiao.friends.mm.fragment.DynamicInfoFragment;
import com.pkjiao.friends.mm.services.DownloadChatMsgService;
import com.pkjiao.friends.mm.services.DownloadIndirectFriendsIntentServices;
import com.pkjiao.friends.mm.services.DownloadNoticesService;
import com.pkjiao.friends.mm.services.UpdateAppServices;
import com.pkjiao.friends.mm.utils.Utils;
import com.pkjiao.friends.mm.viewpagerindicator.TabPageIndicator;

public class MarrySocialMainActivity extends FragmentActivity implements
        OnClickListener {

    private static final String TAG = "MarrySocialMainActivity";

    private static final int DYNAMICINFOFRAGMENT = 0;
    private static final int CHATMSGFRAGMENT = 1;
    private static final int CONTACTSLISTFRAGMENT = 2;
    private static final int APP_NEED_UPDATE = 3;

    private ViewPager mViewPager;
    private ArrayList<Fragment> mViewFragmentSets;
    private ViewPagerFragmentAdapter mViewPagerFragmentAdapter;

    private ImageButton mSettings;
    private ImageButton mUserCenter;
    private ImageButton mInviteFriends;
    private ImageView mNewCommentsTips;

    private String mUid;

    private NotificationManagerControl mNotificationManager;
    private NewTipsBroadcastReceiver mBroadcastReceiver;
    private SharedPreferences mPrefs;
    private ExecutorService mExecutorService;

    private UpdateAppDialog mUpdateDialog;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case APP_NEED_UPDATE: {
                showUpdateAppDialogIfNeeded(MarrySocialMainActivity.this);
                break;
            }
            default:
                break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.marrysocial_main);

        mPrefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        mUid = mPrefs.getString(CommonDataStructure.UID, "");
        mNotificationManager = NotificationManagerControl.newInstance(this);

        mSettings = (ImageButton) findViewById(R.id.actionbar_setting);
        mUserCenter = (ImageButton) findViewById(R.id.actionbar_user_center);
        mInviteFriends = (ImageButton) findViewById(R.id.actionbar_invite_friends);
        mNewCommentsTips = (ImageView) findViewById(R.id.community_headbar_comments_new_tips);
        mSettings.setOnClickListener(this);
        mUserCenter.setOnClickListener(this);
        mInviteFriends.setOnClickListener(this);

        mBroadcastReceiver = new NewTipsBroadcastReceiver();
        mBroadcastReceiver.setBroadcastListener(mBroadcastListener);

        if (mPrefs.getInt(CommonDataStructure.NOTIFICATION_COMMENTS_COUNT, 0) == 0) {
            mNewCommentsTips.setVisibility(View.INVISIBLE);
        } else {
            mNewCommentsTips.setVisibility(View.VISIBLE);
        }

        initViewPager();
        downloadUserContacts();
        startDownloadNoticesServices();
        startDownloadChatMsgServices();
        checkAppVersion();
        registerReceiver(mBroadcastReceiver, getIntentFilter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        redirectToCorrespondActivityIfNeeded();
        mNotificationManager.cancelAll();
        // initViewPager();
    }

    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        mViewFragmentSets = new ArrayList<Fragment>();
        mViewFragmentSets.add(new DynamicInfoFragment());
        mViewFragmentSets.add(new ChatMsgFragment());
        mViewFragmentSets.add(new ContactsListFragment());

        mViewPagerFragmentAdapter = new ViewPagerFragmentAdapter(
                getSupportFragmentManager(), mViewFragmentSets);
        mViewPager.setAdapter(mViewPagerFragmentAdapter);

        TabPageIndicator headBarIndicator = (TabPageIndicator) findViewById(R.id.community_headbar_indicator);
        headBarIndicator.setViewPager(mViewPager);
        headBarIndicator.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                int fragmentIndex = mViewPager.getCurrentItem();
                if (fragmentIndex == CONTACTSLISTFRAGMENT) {
                    Fragment currentFragment = mViewPagerFragmentAdapter
                            .getItem(fragmentIndex);
                    ((ContactsListFragment) currentFragment)
                            .dismissPopupWindow();
                }
            }
        });
    }

    private long mExitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            int fragmentIndex = mViewPager.getCurrentItem();
            Fragment currentFragment = mViewPagerFragmentAdapter
                    .getItem(fragmentIndex);
            switch (fragmentIndex) {
            case DYNAMICINFOFRAGMENT:
                ((DynamicInfoFragment) currentFragment).onKeyDown(keyCode,
                        event);
                break;
            case CHATMSGFRAGMENT:
                ((ChatMsgFragment) currentFragment).onKeyDown(keyCode, event);
                break;
            case CONTACTSLISTFRAGMENT:
                ((ContactsListFragment) currentFragment).onKeyDown(keyCode,
                        event);
                break;
            default:
                break;
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDownloadNoticesServices();
        stopDownloadChatmsgServices();
        unregisterReceiver(mBroadcastReceiver);
    }

    private void downloadUserContacts() {
        Intent serviceIntent = new Intent(this,
                DownloadIndirectFriendsIntentServices.class);
        startService(serviceIntent);
    }

    private void startDownloadNoticesServices() {
        Intent service = new Intent(getApplicationContext(),
                DownloadNoticesService.class);
        startService(service);
    }

    private void stopDownloadNoticesServices() {
        Intent service = new Intent(getApplicationContext(),
                DownloadNoticesService.class);
        stopService(service);
    }

    private void startDownloadChatMsgServices() {
        Intent service = new Intent(getApplicationContext(),
                DownloadChatMsgService.class);
        startService(service);
    }

    private void stopDownloadChatmsgServices() {
        Intent service = new Intent(getApplicationContext(),
                DownloadChatMsgService.class);
        stopService(service);
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
        case R.id.actionbar_setting: {
            startToViewSettings();
            break;
        }
        case R.id.actionbar_user_center: {
            startToViewContactsInfo(mUid);
            break;
        }
        case R.id.actionbar_invite_friends: {
            startToInviteFriends();
            break;
        }
        default:
            break;
        }
    }

    private void startToViewContactsInfo(String uid) {
        Intent intent = new Intent(this, ContactsInfoActivity.class);
        intent.putExtra(MarrySocialDBHelper.KEY_UID, uid);
        startActivity(intent);
    }

    private void startToViewSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void startToInviteFriends() {
        Intent intent = new Intent(this, InviteFriendsActivity.class);
        intent.putExtra("invite_friends", true);
        startActivity(intent);
    }

    private void redirectToCorrespondActivityIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        int login_status = prefs.getInt(CommonDataStructure.LOGINSTATUS, 0);
        switch (login_status) {
        case CommonDataStructure.LONIN_STATUS_LOGOUT: {
            redirectToRegisterActivity();
            break;
        }
        }
    }

    private void redirectToRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    private NewTipsBroadcastReceiver.BroadcastListener mBroadcastListener = new NewTipsBroadcastReceiver.BroadcastListener() {

        @Override
        public void onReceivedNewComments() {
            if (mPrefs.getInt(CommonDataStructure.NOTIFICATION_COMMENTS_COUNT,
                    0) == 0) {
                mNewCommentsTips.setVisibility(View.INVISIBLE);
            } else {
                mNewCommentsTips.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onReceivedNewChatMsgs() {
        }

        @Override
        public void onReceivedNewContacts() {
        }

    };

    protected IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CommonDataStructure.KEY_BROADCAST_ACTION);
        return intentFilter;
    }

    private void showUpdateAppDialogIfNeeded(final Context context) {

        mUpdateDialog = new UpdateAppDialog(context);
        mUpdateDialog.setOnConfirmBtnClickListener(mConfirmBtnClickListener);
        mUpdateDialog.show();
    }

    private OnConfirmBtnClickListener mConfirmBtnClickListener = new OnConfirmBtnClickListener() {

        @Override
        public void onConfirmBtnClick() {
            startToUpdateApp();
            mUpdateDialog.dismiss();
        }

    };

    private void startToUpdateApp() {
        Intent intent = new Intent(this, UpdateAppServices.class);
        startService(intent);
    }

    private void checkAppVersion() {
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * CommonDataStructure.THREAD_POOL_SIZE);
        mExecutorService.execute(new GetAppVersion());
    }

    class GetAppVersion implements Runnable {

        @Override
        public void run() {
            int localVersionCode = 0;
            int remoteVersionCode = Integer.valueOf(Utils
                    .getLatestAppVersion(CommonDataStructure.URL_CHECK_VERSION));
            try {
                PackageInfo packageInfo = getApplicationContext()
                        .getPackageManager()
                        .getPackageInfo(getPackageName(), 0);
                localVersionCode = packageInfo.versionCode;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            boolean isNeedUpdate = remoteVersionCode > localVersionCode;
            if (isNeedUpdate) {
                mHandler.sendEmptyMessage(APP_NEED_UPDATE);
            }
        }
    }
}
