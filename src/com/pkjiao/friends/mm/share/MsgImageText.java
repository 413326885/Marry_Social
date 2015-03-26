package com.pkjiao.friends.mm.share;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class MsgImageText extends MsgText {
    public String imageUrl;
    public Bitmap image;

    public MsgImageText() {
        super();
    }

    public MsgImageText(Parcel source) {
        super(source);
        imageUrl = source.readString();
        image = source.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(imageUrl);
        dest.writeParcelable(image, PARCELABLE_WRITE_RETURN_VALUE);
    }

    public static final Parcelable.Creator<MsgImageText> CREATOR = new Parcelable.Creator<MsgImageText>() {

        @Override
        public MsgImageText createFromParcel(Parcel source) {
            return new MsgImageText(source);
        }

        @Override
        public MsgImageText[] newArray(int size) {
            return new MsgImageText[size];
        }

    };
}
