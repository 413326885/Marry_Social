package com.pkjiao.friends.mm.share;

import java.util.Map;

import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import android.app.Activity;
import android.content.Context;

public abstract class AbsSharePlatform {
    public static final String KEY_APPID = "appid";

    public abstract void shareMsg(Activity activity, ShareMsg msg,
            CallbackListener listener);

    class BaseUiListener implements IUiListener {
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
