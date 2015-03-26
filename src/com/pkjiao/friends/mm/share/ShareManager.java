package com.pkjiao.friends.mm.share;

import java.util.Map;

import android.app.Activity;
import android.content.Context;

public class ShareManager {
    public static final String KEY_APPID_QQ = "WX_appid";
    public static final String KEY_APPID_WX = "QQ_appid";

    public static final String KEY_APPID_SINA = "SINA_appid";
    public static final String KEY_APPID_CALLBACK = "SINA_callback";
    public static final String KEY_APPID_SCOPE = "SINA_scope";

    private static ShareManager mManager;
    private Context ctx;
    private Activity mActivity;
    private AbsSharePlatform mPlatform;
    private PlatformQQ mPlatformQQ;
    private PlatformQzone mPlatformQzone;
    private PlatformWX mPlatformWX;

    // private PlatformSina mPlatformSina;
    private ShareManager(Activity activity, Map<String, String> params) {
        this.ctx = activity.getApplicationContext();
        this.mActivity = activity;
        if (params != null) {
            Constants.QQ_APP_ID = params.get(KEY_APPID_QQ);
            Constants.WX_APP_ID = params.get(KEY_APPID_WX);
            // Constants.APP_KEY = params.get(KEY_APPID_SINA);
            // Constants.REDIRECT_URL = params.get(KEY_APPID_CALLBACK);
            // Constants.SCOPE = params.get(KEY_APPID_SCOPE);
        }
        initPlatformQQ(params);
        initPlatformQzone(params);
        initPlatformWX(params);
        // initPlatformSina(params);
    }

    public static ShareManager getInstance() {
        if (mManager == null) {

        }
        return mManager;
    }

    public static ShareManager init(Activity ctx, Map<String, String> params) {
        mManager = new ShareManager(ctx, params);
        return mManager;
    }

    public void sendMsg(AbsShareMsg msg, CallbackListener listener) {
        switch (msg.pType) {
        case PType.PLATFORM_QQ:
            mPlatform = mPlatformQQ;
            break;
        case PType.PLATFORM_QQzone:
            mPlatform = mPlatformQzone;
            break;
        case PType.PLATFORM_WX:
            mPlatform = mPlatformWX;
            break;
        case PType.PLATFORM_WX_friends:
            mPlatform = mPlatformWX;
            break;
        // case PType.PLATFORM_SINA:
        // platform = mPlatformSina;
        // break;
        }
        mPlatform.shareMsg(mActivity, msg, listener);
    }

    public void initPlatformQQ(Map<String, String> params) {
        mPlatformQQ = new PlatformQQ(mActivity, params);
    }

    public void initPlatformQzone(Map<String, String> params) {
        mPlatformQzone = new PlatformQzone(mActivity, params);
    }

    public void initPlatformWX(Map<String, String> params) {
        mPlatformWX = new PlatformWX(mActivity, params);
    }
    // public void initPlatformSina(Map<String, String> params){
    // mPlatformSina = new PlatformSina(mActivity,params);
    // }
}
