package com.pkjiao.friends.mm.base;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class CommentsItem {

    private static final String TAG = "CommentsItem";

    private String mUid;
    private String mAvatar;
    private String mRealName;
    private String mNickName;
    private String mAddTime;
    private String mContents;
    private String mBucketId;
    private String mCommentId;

    private int mPhotoCount;
    private int mCurrrentStatus;
    private boolean mIsBravo;

    private Bitmap mHeadPic;
    private ArrayList<ImagesItem> mImages;

    private ArrayList<ReplysItem> mReplyLists;

    public String getUid() {
        return mUid;
    }

    public void setUid(String mUid) {
        this.mUid = mUid;
    }

    public String getBucketId() {
        return mBucketId;
    }

    public void setBucketId(String mBucketId) {
        this.mBucketId = mBucketId;
    }

    public String getCommentId() {
        return mCommentId;
    }

    public void setCommentId(String mCommentId) {
        this.mCommentId = mCommentId;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public void setAvatar(String mAvatar) {
        this.mAvatar = mAvatar;
    }

    public String getRealName() {
        return mRealName;
    }

    public void setRealName(String mFullName) {
        this.mRealName = mFullName;
    }

    public String getNickName() {
        return mNickName;
    }

    public void setNickName(String mNikeName) {
        this.mNickName = mNikeName;
    }

    public String getAddTime() {
        return mAddTime;
    }

    public void setAddTime(String mAddTime) {
        this.mAddTime = mAddTime;
    }

    public String getContents() {
        return mContents;
    }

    public void setContents(String mContents) {
        this.mContents = mContents;
    }

    public int getPhotoCount() {
        return mPhotoCount;
    }

    public void setPhotoCount(int count) {
        this.mPhotoCount = count;
    }

    public int getCurrrentStatus() {
        return mCurrrentStatus;
    }

    public void setCurrrentStatus(int mCurrrentStatus) {
        this.mCurrrentStatus = mCurrrentStatus;
    }

    public boolean isBravo() {
        return mIsBravo;
    }

    public void setIsBravo(boolean mIsBravo) {
        this.mIsBravo = mIsBravo;
    }

    public Bitmap getHeadPic() {
        return mHeadPic;
    }

    public void setHeadPic(Bitmap mHeadPic) {
        this.mHeadPic = mHeadPic;
    }

    public ArrayList<ImagesItem> getImages() {
        return mImages;
    }

    public void setImages(ArrayList<ImagesItem> mImages) {
        this.mImages = mImages;
    }

    public void setReplyLists(ArrayList<ReplysItem> mReplyLists) {
        this.mReplyLists = mReplyLists;
    }

    public ArrayList<ReplysItem> getReplyList() {
        return mReplyLists;
    }
}
