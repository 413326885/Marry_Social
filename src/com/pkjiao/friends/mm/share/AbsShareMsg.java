package com.pkjiao.friends.mm.share;

import android.app.Activity;
import android.os.Parcelable;

public abstract class AbsShareMsg implements Parcelable {
    /** 平台类型 */
    public int pType;
    public String appName;

    public final void share(CallbackListener listener) {
        ShareManager.getInstance().sendMsg(this, listener);
    }
}
