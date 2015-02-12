package com.pkjiao.friends.mm.base;

public class NoticesItem {

    private String mNoticeId;
    private String mUid;
    private String mFromUid;
    private String mTimeLine;
    private int mNoticeType;
    private String mCommentId;
    private int mIsReceived;

    public String getNoticeId() {
        return mNoticeId;
    }

    public void setNoticeId(String mNoticeId) {
        this.mNoticeId = mNoticeId;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String mUid) {
        this.mUid = mUid;
    }

    public String getFromUid() {
        return mFromUid;
    }

    public void setFromUid(String mFromUid) {
        this.mFromUid = mFromUid;
    }

    public String getTimeLine() {
        return mTimeLine;
    }

    public void setTimeLine(String mTimeLine) {
        this.mTimeLine = mTimeLine;
    }

    public int getNoticeType() {
        return mNoticeType;
    }

    public void setNoticeType(int mNoticeType) {
        this.mNoticeType = mNoticeType;
    }

    public String getCommentId() {
        return mCommentId;
    }

    public void setCommentId(String mCommentId) {
        this.mCommentId = mCommentId;
    }

    public int getIsReceived() {
        return mIsReceived;
    }

    public void setIsReceived(int mIsReceived) {
        this.mIsReceived = mIsReceived;
    }

}
