package com.dhn.marrysocial.base;

public class ChatMsgItem {

    private static final String TAG = "ChatMsgItem";


    private String mUid;
    private String mChatId;
    private String mFromUid;
    private String mToUid;
    private String mAddedTime;
    private String mChatContent;

    private int mDBId;
    private int mMsgType;
    private int mCurrentStatus;

    public int getDBId() {
        return mDBId;
    }

    public void setDBId(int mDBId) {
        this.mDBId = mDBId;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String mUid) {
        this.mUid = mUid;
    }

    public String getChatId() {
        return mChatId;
    }

    public void setChatId(String mChatId) {
        this.mChatId = mChatId;
    }

    public String getFromUid() {
        return mFromUid;
    }

    public void setFromUid(String mFromUid) {
        this.mFromUid = mFromUid;
    }

    public String getToUid() {
        return mToUid;
    }

    public void setToUid(String mToUid) {
        this.mToUid = mToUid;
    }

    public String getAddedTime() {
        return mAddedTime;
    }

    public void setAddedTime(String mAddedTime) {
        this.mAddedTime = mAddedTime;
    }

    public String getChatContent() {
        return mChatContent;
    }

    public void setChatContent(String mChatContent) {
        this.mChatContent = mChatContent;
    }

    public int getMsgType() {
        return mMsgType;
    }

    public void setMsgType(int mMsgType) {
        this.mMsgType = mMsgType;
    }

    public int getCurrentStatus() {
        return mCurrentStatus;
    }

    public void setCurrentStatus(int mCurrentStatus) {
        this.mCurrentStatus = mCurrentStatus;
    }

    @Override
    public String toString() {
        return "ChatMsgItem [mUid=" + mUid + ", mChatId=" + mChatId
                + ", mFromUid=" + mFromUid + ", mToUid=" + mToUid
                + ", mAddedTime=" + mAddedTime + ", mChatContent="
                + mChatContent + ", mMsgType=" + mMsgType + "]";
    }

}
