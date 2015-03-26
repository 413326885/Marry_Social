package com.pkjiao.friends.mm.share;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ShareMsg implements Parcelable {

    public int pType;
    public String appName;
    public String title;
    public String summary;
    public String targetUrl;
    public String imageUrl;
    public Bitmap image;

    public ShareMsg() {
    }

    public ShareMsg(Parcel source) {
        pType = source.readInt();
        appName = source.readString();
        title = source.readString();
        summary = source.readString();
        targetUrl = source.readString();
        imageUrl = source.readString();
        image = source.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flag) {
        dest.writeInt(pType);
        dest.writeString(appName);
        dest.writeString(title);
        dest.writeString(summary);
        dest.writeString(targetUrl);
        dest.writeString(imageUrl);
        dest.writeParcelable(image, PARCELABLE_WRITE_RETURN_VALUE);
    }

    public static final Parcelable.Creator<ShareMsg> CREATOR = new Parcelable.Creator<ShareMsg>() {

        @Override
        public ShareMsg createFromParcel(Parcel source) {
            return new ShareMsg(source);
        }

        @Override
        public ShareMsg[] newArray(int size) {
            return new ShareMsg[size];
        }

    };
}
