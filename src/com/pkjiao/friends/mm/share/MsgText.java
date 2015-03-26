package com.pkjiao.friends.mm.share;

import android.os.Parcel;
import android.os.Parcelable;

public class MsgText extends AbsShareMsg {
    public String title;
    public String summary;
    public String targetUrl;

    public MsgText() {

    }

    public MsgText(Parcel source) {
        title = source.readString();
        summary = source.readString();
        targetUrl = source.readString();
        pType = source.readInt();
        appName = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(summary);
        dest.writeString(targetUrl);
        dest.writeInt(pType);
        dest.writeString(appName);
    }

    public static final Parcelable.Creator<MsgText> CREATOR = new Parcelable.Creator<MsgText>() {

        @Override
        public MsgText createFromParcel(Parcel source) {
            return new MsgText(source);
        }

        @Override
        public MsgText[] newArray(int size) {
            return new MsgText[size];
        }

    };
}
