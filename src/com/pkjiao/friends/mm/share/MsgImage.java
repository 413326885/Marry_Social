package com.pkjiao.friends.mm.share;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class MsgImage extends AbsShareMsg {
    public String imageUrl;
    public String imagePath;
    public Bitmap image;

    public MsgImage() {
        super();
    }

    public MsgImage(Parcel source) {
        pType = source.readInt();
        appName = source.readString();
        imageUrl = source.readString();
        imagePath = source.readString();
        image = source.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(pType);
        dest.writeString(appName);
        dest.writeString(imageUrl);
        dest.writeString(imagePath);
        dest.writeParcelable(image, PARCELABLE_WRITE_RETURN_VALUE);
    }

    public static final Parcelable.Creator<MsgImage> CREATOR = new Parcelable.Creator<MsgImage>() {

        @Override
        public MsgImage createFromParcel(Parcel source) {
            return new MsgImage(source);
        }

        @Override
        public MsgImage[] newArray(int size) {
            return new MsgImage[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }
}
