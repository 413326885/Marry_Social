package com.pkjiao.friends.mm.base;

public class ReplysItem {

    private static final String TAG = "ReplysItem";

    private String mUid;
    private String mNickName;
    private String mReplyContents;
    private String mCommentId;
    private String mReplyTime;
    private String mReplyId;
    private String mBucketId;

    public String getUid() {
        return mUid;
    }

    public void setUid(String mUid) {
        this.mUid = mUid;
    }

    public String getNickname() {
        return mNickName;
    }

    public void setNickname(String mNickName) {
        this.mNickName = mNickName;
    }

    public String getReplyContents() {
        return mReplyContents;
    }

    public void setReplyContents(String mReplyContents) {
        this.mReplyContents = mReplyContents;
    }

    public String getCommentId() {
        return mCommentId;
    }

    public void setCommentId(String mCommentId) {
        this.mCommentId = mCommentId;
    }

    public String getReplyTime() {
        return mReplyTime;
    }

    public void setReplyTime(String mReplyTime) {
        this.mReplyTime = mReplyTime;
    }

    public String getReplyId() {
        return mReplyId;
    }

    public void setReplyId(String mReplyId) {
        this.mReplyId = mReplyId;
    }

    public String getBucketId() {
        return mBucketId;
    }

    public void setBucketId(String mBucketId) {
        this.mBucketId = mBucketId;
    }
}
