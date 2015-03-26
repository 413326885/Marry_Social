package com.pkjiao.friends.mm.share;

import android.app.Activity;

public class ShareManager {
    public static final String KEY_APPID_QQ = "WX_appid";
    public static final String KEY_APPID_WX = "QQ_appid";

    private static ShareManager mManager;
    private AbsSharePlatform mPlatform;
    private PlatformQQ mPlatformQQ;
    private PlatformQzone mPlatformQzone;
    private PlatformWX mPlatformWX;

    // private PlatformSina mPlatformSina;
    private ShareManager() {
        initPlatformQQ();
        initPlatformQzone();
        initPlatformWX();
    }

    public static ShareManager getInstance() {
        if (mManager == null) {
            mManager = new ShareManager();
        }
        return mManager;
    }

    public void sendMsg(Activity context, ShareMsg msg, CallbackListener listener) {
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
        }
        mPlatform.shareMsg(context, msg, listener);
    }

    public void initPlatformQQ() {
        mPlatformQQ = new PlatformQQ();
    }

    public void initPlatformQzone() {
        mPlatformQzone = new PlatformQzone();
    }

    public void initPlatformWX() {
        mPlatformWX = new PlatformWX();
    }
}
