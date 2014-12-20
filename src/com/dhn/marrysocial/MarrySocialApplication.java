package com.dhn.marrysocial;

import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.services.DownloadNoticesService;

import android.app.Application;
import android.content.Intent;

public class MarrySocialApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        MarrySocialDBHelper.newInstance(getApplicationContext());
//        startDownloadNoticesServices();
    }

//    private void startDownloadNoticesServices() {
//        Intent service = new Intent(getApplicationContext(), DownloadNoticesService.class);
//        startService(service);
//    }

}
