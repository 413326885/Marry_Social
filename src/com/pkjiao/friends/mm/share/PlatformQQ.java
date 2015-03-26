package com.pkjiao.friends.mm.share;

import android.app.Activity;
import android.os.Bundle;

import com.tencent.connect.share.QQShare;
import com.tencent.tauth.Tencent;

public class PlatformQQ extends AbsSharePlatform {

    @Override
    public void shareMsg(Activity ctx, ShareMsg msg, CallbackListener listener) {
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE,
                QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, msg.title);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, msg.summary);
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, msg.targetUrl);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, msg.imageUrl);
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, msg.appName);

        Tencent mTencent = Tencent.createInstance(Constants.QQ_APP_ID, ctx);
        mTencent.shareToQQ(ctx, params, new BaseUiListener(listener));
    }

}
