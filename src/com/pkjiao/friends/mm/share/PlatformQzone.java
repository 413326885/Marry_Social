package com.pkjiao.friends.mm.share;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;

import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.Tencent;

public class PlatformQzone extends AbsSharePlatform {

    @Override
    public void shareMsg(Activity ctx, ShareMsg msg, CallbackListener listener) {
        ArrayList<String> imageUrl = new ArrayList<String>();
        imageUrl.add(msg.imageUrl);

        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, msg.title);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, msg.summary);
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, msg.targetUrl);
        params.putStringArrayList(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, msg.appName);
        params.putInt(QzoneShare.SHARE_TO_QQ_EXT_INT,
                QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);

        Tencent mTencent = Tencent.createInstance(Constants.QQ_APP_ID, ctx);
        mTencent.shareToQzone(ctx, params, new BaseUiListener(listener));
    }

}
