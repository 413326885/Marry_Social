package com.pkjiao.friends.mm.share;

public interface CallbackListener {
    public abstract void onSuccess();

    public abstract void onFailure();

    public abstract void onCancel();
}
