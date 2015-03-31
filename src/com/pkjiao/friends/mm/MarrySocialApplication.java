package com.pkjiao.friends.mm;

import java.io.File;
import java.util.concurrent.ExecutorService;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.pkjiao.friends.mm.common.CommonDataStructure;
import com.pkjiao.friends.mm.database.MarrySocialDBHelper;

public class MarrySocialApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MarrySocialDBHelper.newInstance(getApplicationContext());
        // startDownloadNoticesServices();
        initDataDir();
        initUILImageLoader();
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

    private void initUILImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.empty_photo)
                .cacheInMemory(true).cacheOnDisc(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .discCacheSize(50 * 1024 * 1024)//
                .discCacheFileCount(100)// 缓存一百张图片
                .writeDebugLogs().build();
        ImageLoader.getInstance().init(config);
    }
}
