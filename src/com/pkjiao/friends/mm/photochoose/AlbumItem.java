package com.pkjiao.friends.mm.photochoose;

public class AlbumItem {

    private String mAlbumDisplayName;
    private String mAlbumBucketId;
    private int mAlbumPhotoCount;
    private String mAlbumFirstPhotoPath;
    private boolean mIsSelected;

    public String getAlbumDisplayName() {
        return mAlbumDisplayName;
    }

    public void setAlbumDisplayName(String mAlbumDisplayName) {
        this.mAlbumDisplayName = mAlbumDisplayName;
    }

    public String getAlbumBucketId() {
        return mAlbumBucketId;
    }

    public void setAlbumBucketId(String mAlbumBucketId) {
        this.mAlbumBucketId = mAlbumBucketId;
    }

    public int getAlbumPhotoCount() {
        return mAlbumPhotoCount;
    }

    public void setAlbumPhotoCount(int mAlbumPhotoCount) {
        this.mAlbumPhotoCount = mAlbumPhotoCount;
    }

    public String getAlbumFirstPhotoPath() {
        return mAlbumFirstPhotoPath;
    }

    public void setAlbumFirstPhotoPath(String mAlbumFirstPhotoPath) {
        this.mAlbumFirstPhotoPath = mAlbumFirstPhotoPath;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setIsSelected(boolean mIsSelected) {
        this.mIsSelected = mIsSelected;
    }

}
