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
        try {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {

                File downloadPicCacheDir = new File(
                        CommonDataStructure.DOWNLOAD_PICS_DIR_URL);
                if (!downloadPicCacheDir.exists()) {
                    downloadPicCacheDir.mkdirs();
                }
                File nomedia_download_pic = new File(downloadPicCacheDir,
                        ".nomedia");
                if (!nomedia_download_pic.exists()) {
                    nomedia_download_pic.createNewFile();
                }

                File headPicCacheDir = new File(
                        CommonDataStructure.HEAD_PICS_DIR_URL);
                if (!headPicCacheDir.exists()) {
                    headPicCacheDir.mkdirs();
                }
                File nomedia_head_pic = new File(headPicCacheDir, ".nomedia");
                if (!nomedia_head_pic.exists()) {
                    nomedia_head_pic.createNewFile();
                }

                File backgroudnPicCacheDir = new File(
                        CommonDataStructure.BACKGROUND_PICS_DIR_URL);
                if (!backgroudnPicCacheDir.exists()) {
                    backgroudnPicCacheDir.mkdirs();
                }
                File nomedia_background_pic = new File(backgroudnPicCacheDir,
                        ".nomedia");
                if (!nomedia_background_pic.exists()) {
                    nomedia_background_pic.createNewFile();
                }

                return;
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        Toast.makeText(this, "没有有效的可用内存空间", 1000).show();
    }
}
