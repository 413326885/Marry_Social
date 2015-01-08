package com.dhn.marrysocial;

import java.util.ArrayList;

import com.dhn.marrysocial.adapter.ViewPagerFragmentAdapter;
import com.dhn.marrysocial.fragment.ChatMsgFragment;
import com.dhn.marrysocial.fragment.DynamicInfoFragment;
import com.dhn.marrysocial.fragment.ContactsListFragment;
import com.dhn.marrysocial.services.DownloadChatMsgService;
import com.dhn.marrysocial.services.DownloadNoticesService;
import com.dhn.marrysocial.viewpagerindicator.TabPageIndicator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.Toast;

public class MarrySocialMainActivity extends FragmentActivity {

    private static final String TAG = "MarrySocialMainActivity";

    private static final int DYNAMICINFOFRAGMENT = 0;
    private static final int CHATMSGFRAGMENT = 1;
    private static final int CONTACTSLISTFRAGMENT = 2;

    private ViewPager mViewPager;
    private ArrayList<Fragment> mViewFragmentSets;
    private ViewPagerFragmentAdapter mViewPagerFragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.marrysocial_main);
        initViewPager();
        startDownloadNoticesServices();
        startDownloadChatMsgServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        initViewPager();
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
            }
        });
    }

    private long mExitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            int fragmentIndex = mViewPager.getCurrentItem();
            Fragment currentFragment = mViewPagerFragmentAdapter.getItem(fragmentIndex);
            switch (fragmentIndex) {
            case DYNAMICINFOFRAGMENT:
                ((DynamicInfoFragment) currentFragment).onKeyDown(keyCode, event);
                break;
            case CHATMSGFRAGMENT:
                ((ChatMsgFragment) currentFragment).onKeyDown(keyCode, event);
                break;
            case CONTACTSLISTFRAGMENT:
                ((ContactsListFragment) currentFragment).onKeyDown(keyCode, event);
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
    }

    private void startDownloadNoticesServices() {
        Log.e(TAG, "nannan startDownloadNoticesServices");
        Intent service = new Intent(getApplicationContext(), DownloadNoticesService.class);
        startService(service);
    }

    private void stopDownloadNoticesServices() {
        Log.e(TAG, "nannan stopDownloadNoticesServices");
        Intent service = new Intent(getApplicationContext(), DownloadNoticesService.class);
        stopService(service);
    }

    private void startDownloadChatMsgServices() {
        Log.e(TAG, "nannan startDownloadNoticesServices");
        Intent service = new Intent(getApplicationContext(), DownloadChatMsgService.class);
        startService(service);
    }

    private void stopDownloadChatmsgServices() {
        Log.e(TAG, "nannan stopDownloadNoticesServices");
        Intent service = new Intent(getApplicationContext(), DownloadChatMsgService.class);
        stopService(service);
    }
}
