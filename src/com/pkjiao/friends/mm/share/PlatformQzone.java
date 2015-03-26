package com.pkjiao.friends.mm.share;

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.tencent.connect.auth.QQAuth;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class PlatformQzone extends AbsSharePlatform {

    public PlatformQzone(Context appCtx, Map<String, String> params) {
        super(appCtx, params);
    }

    QQAuth mQQAuth;
    QzoneShare mQzoneShare;

    @Override
    public void shareMsg(Activity ctx, AbsShareMsg msg,
            CallbackListener listener) {
        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, "Test");
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, "content infro");
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL,
                "http://www.pkjiao.com");
        ArrayList imageUrls = new ArrayList();
        imageUrls
                .add("http://media-cdn.tripadvisor.com/media/photo-s/01/3e/05/40/the-sandbar-that-links.jpg");
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
        params.putInt(QzoneShare.SHARE_TO_QQ_EXT_INT,
                QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
        Tencent mTencent = Tencent.createInstance(Constants.QQ_APP_KEY, ctx);
        mTencent.shareToQzone(ctx, params, new BaseUiListener(listener));
        // MsgImageText mt = null;
        // if (msg instanceof MsgImageText) {
        // mt = (MsgImageText) msg;
        //
        // final Bundle params = new Bundle();
        // params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
        // QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        // params.putString(QzoneShare.SHARE_TO_QQ_TITLE, mt.title);
//         params.putString(QzoneShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, mt.summary);
        // params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, mt.targetUrl);
        // mQzoneShare.shareToQzone(ctx, params, new BaseUiListener(listener));
        // } else if (msg instanceof MsgImage) {
        // shareImageMsg(ctx, (MsgImage) msg, listener);
        // }
    }

    public void shareImageMsg(Activity ctx, MsgImage msg,
            CallbackListener listener) {
        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE);
        if (msg.imagePath != null) {
            params.putString(QzoneShare.SHARE_TO_QQ_IMAGE_LOCAL_URL,
                    msg.imagePath);
        } else if (msg.imageUrl != null) {
            params.putString(QzoneShare.SHARE_TO_QQ_IMAGE_URL, msg.imageUrl);
        }
        params.putString(QzoneShare.SHARE_TO_QQ_APP_NAME, msg.appName);
        params.putInt(QzoneShare.SHARE_TO_QQ_EXT_INT,
                QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
        mQzoneShare.shareToQzone(ctx, params, new BaseUiListener(listener));
    }

    @Override
    public void init(Context ctx) {
        String appid = Constants.QQ_APP_ID;
        if (appid != null) {
            // mQQAuth = QQAuth.createInstance(appid, ctx); // 根据APPID 获取入口信息
            // mQzoneShare = new QzoneShare(ctx, mQQAuth.getQQToken());
            Tencent mTencent = Tencent
                    .createInstance(Constants.QQ_APP_KEY, ctx);
            mQzoneShare = new QzoneShare(ctx, mTencent.getQQToken());
            return;
        }
        throw new IllegalArgumentException("PlatformQzone init error");
    }

    private class BaseUiListener implements IUiListener {
        CallbackListener listener;

        BaseUiListener(CallbackListener listener) {
            this.listener = listener;
        }

        @Override
        public void onComplete(Object arg0) {
            if (listener != null) {
                listener.onSuccess();
            }
        }

        @Override
        public void onError(UiError arg0) {
            if (listener != null) {
                listener.onFailure();
            }
        }

        @Override
        public void onCancel() {
            if (listener != null) {
                listener.onCancel();
            }
        }
    }

}
