package com.pkjiao.friends.mm.share;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.tencent.connect.auth.QQAuth;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class PlatformQQ extends AbsSharePlatform {

    public PlatformQQ(Context appCtx, Map<String, String> params) {
        super(appCtx, params);
    }

    QQAuth mQQAuth;
    QQShare mQQShare;

    @Override
    public void shareMsg(Activity ctx, AbsShareMsg msg,
            CallbackListener listener) {
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, "要分享的标题");
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY,  "要分享的摘要");
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  "http://www.qq.com/news/1.html");
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL,"http://imgcache.qq.com/qzone/space_item/pre/0/66768.gif");
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME,  "测试应用222222");
        
        Tencent mTencent = Tencent.createInstance(Constants.QQ_APP_ID, ctx);
        mTencent.shareToQQ(ctx, params, new BaseUiListener(listener));
//        MsgText mt = null;
//        if (msg instanceof MsgText) {
//            mt = (MsgText) msg;
//
//            final Bundle params = new Bundle();
//            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE,
//                    QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
//            params.putString(QQShare.SHARE_TO_QQ_TITLE, mt.title);
//            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, mt.summary);
//            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, mt.targetUrl);
//            if (msg instanceof MsgImageText) {
//                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL,
//                        ((MsgImageText) mt).imageUrl);
//            }
//            params.putString(QQShare.SHARE_TO_QQ_APP_NAME, mt.appName);
//            mQQShare.shareToQQ(ctx, params, new BaseUiListener(listener));
//        } else if (msg instanceof MsgImage) {
//            shareImageMsg(ctx, (MsgImage) msg, listener);
//        }
    }

    public void shareImageMsg(Activity ctx, MsgImage msg,
            CallbackListener listener) {
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE,
                QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        if (msg.imagePath != null) {
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, msg.imagePath);
        } else if (msg.imageUrl != null) {
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, msg.imageUrl);
        }
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, msg.appName);
        mQQShare.shareToQQ(ctx, params, new BaseUiListener(listener));
    }

    @Override
    public void init(Context ctx) {
        String appid = Constants.QQ_APP_ID;
        if (appid != null) {
            mQQAuth = QQAuth.createInstance(appid, ctx); // 根据APPID 获取入口信息
            mQQShare = new QQShare(ctx, mQQAuth.getQQToken());
            return;
        }
        throw new IllegalArgumentException("PlatformQQ init error");
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
