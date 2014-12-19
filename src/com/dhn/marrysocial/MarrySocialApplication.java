package com.dhn.marrysocial;

import com.dhn.marrysocial.database.MarrySocialDBHelper;

import android.app.Application;

public class MarrySocialApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        MarrySocialDBHelper.newInstance(getApplicationContext());
    }
}
