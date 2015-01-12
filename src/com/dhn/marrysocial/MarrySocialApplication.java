package com.dhn.marrysocial;

import java.io.File;

import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.services.DownloadNoticesService;

import android.app.Application;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

public class MarrySocialApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MarrySocialDBHelper.newInstance(getApplicationContext());
        // startDownloadNoticesServices();
        initDataDir();
    }

    // private void startDownloadNoticesServices() {
    // Intent service = new Intent(getApplicationContext(),
    // DownloadNoticesService.class);
    // startService(service);
    // }

    private void initDataDir() {
        try{
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                File sdCardDir = Environment.getExternalStorageDirectory();
                File downloadPicCacheDir = new File(sdCardDir.getAbsolutePath()
                        + File.separator + CommonDataStructure.IMAGE_CACHE_DIR
                        + File.separator
                        + CommonDataStructure.DOWNLOAD_PICS_DIR);
                if (!downloadPicCacheDir.exists()) {
                    downloadPicCacheDir.mkdirs();
                    File nomedia = new File(downloadPicCacheDir, ".nomedia");
                    nomedia.createNewFile();
                }
                File headPicCacheDir = new File(sdCardDir.getAbsolutePath()
                        + File.separator + CommonDataStructure.IMAGE_CACHE_DIR
                        + File.separator
                        + CommonDataStructure.HEAD_PICS_DIR);
                if (!headPicCacheDir.exists()) {
                    headPicCacheDir.mkdirs();
                    File nomedia = new File(headPicCacheDir, ".nomedia");
                    nomedia.createNewFile();
                }
                return;
            }
        } catch(Exception exp) {
            exp.printStackTrace();
        }
        Toast.makeText(this, "没有有效的可用内存空间", 1000);
    }
}
