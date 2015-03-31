package com.pkjiao.friends.mm.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.RemoteViews;

import com.pkjiao.friends.mm.MarrySocialMainActivity;
import com.pkjiao.friends.mm.R;
import com.pkjiao.friends.mm.common.CommonDataStructure;

public class UpdateAppServices extends Service {

    private static final int TIMEOUT = 10 * 1000;// 超时
    private static final String DOWNLOAD_URL = "http://www.pkjiao.com/apk";
    private static final int DOWNLOAD_OK = 1;
    private static final int DOWNLOAD_ERROR = 0;
    private static final int NOTIFICATION_ID = 0;

    private NotificationManager mNotificationManager;
    private Notification mNotification;

    private Intent mUpdateIntent;
    private PendingIntent mPendingIntent;

    private RemoteViews mDownloadView;
    private Handler mHandler;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mHandler = new Handler() {
            @SuppressWarnings("deprecation")
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case DOWNLOAD_OK:

                    File file = new File(CommonDataStructure.DOWNLOAD_APP_URL);
                    Uri uri = Uri.fromFile(file);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.setDataAndType(uri,
                            "application/vnd.android.package-archive");

                    mPendingIntent = PendingIntent.getActivity(
                            UpdateAppServices.this, 0, intent, 0);

                    mNotification = new Notification();
                    mNotification.icon = R.drawable.ic_notification;
                    mNotification.tickerText = "正在下载";
                    mNotification.when = System.currentTimeMillis();
                    mNotification.flags = Notification.FLAG_ONGOING_EVENT;
                    mNotification.flags = Notification.FLAG_AUTO_CANCEL;

                    mNotification.contentIntent = mPendingIntent;
                    mNotification.setLatestEventInfo(UpdateAppServices.this,
                            "friends+", "下载成功，点击安装", mPendingIntent);

                    mNotificationManager.notify(NOTIFICATION_ID, mNotification);

                    stopSelf();
                    break;
                case DOWNLOAD_ERROR:
                    mNotification.flags = Notification.FLAG_ONGOING_EVENT;
                    mNotification.flags = Notification.FLAG_AUTO_CANCEL;
                    mNotification.setLatestEventInfo(UpdateAppServices.this,
                            "friends+", "下载失败", mPendingIntent);
                    break;

                default:
                    stopSelf();
                    break;
                }

            }

        };

        createAppFile();

        createNotification();

        createThread();

        return super.onStartCommand(intent, flags, startId);

    }

    public void createThread() {

        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    long downloadSize = downloadUpdateFile(DOWNLOAD_URL,
                            CommonDataStructure.DOWNLOAD_APP_URL);
                    if (downloadSize > 0) {
                        mHandler.sendEmptyMessage(DOWNLOAD_OK);
                    } else {
                        mHandler.sendEmptyMessage(DOWNLOAD_ERROR);
                    }

                } catch (Exception e) {
                    mHandler.sendEmptyMessage(DOWNLOAD_ERROR);
                }

            }
        }).start();
    }

    public void createNotification() {

        mDownloadView = new RemoteViews(getPackageName(),
                R.layout.notification_download_app_layout);
        mDownloadView.setTextViewText(R.id.notificationTitle,
                "friends+ 正在下载...");
        mDownloadView.setTextViewText(R.id.notificationPercent, "0%");
        mDownloadView.setProgressBar(R.id.notificationProgress, 100, 0, false);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotification = new Notification();
        mNotification.icon = R.drawable.ic_notification;
        mNotification.tickerText = "正在下载";
        mNotification.when = System.currentTimeMillis();
        mNotification.contentView = mDownloadView;
        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        mNotification.flags = Notification.FLAG_AUTO_CANCEL;

        mUpdateIntent = new Intent(this, MarrySocialMainActivity.class);
        mUpdateIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mPendingIntent = PendingIntent.getActivity(this, 0, mUpdateIntent, 0);

        mNotification.contentIntent = mPendingIntent;
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);

    }

    public long downloadUpdateFile(String down_url, String file) {

        int down_step = 5;// 提示step
        int totalSize;// 文件总大小
        int downloadCount = 0;// 已经下载好的大小
        int updateCount = 0;// 已经上传的文件大小

        InputStream inputStream = null;
        OutputStream outputStream = null;
        URL url = null;
        HttpURLConnection httpURLConnection = null;

        try {
            url = new URL(down_url);
            if (url == null)
                return downloadCount;
            httpURLConnection = (HttpURLConnection) url.openConnection();
            if (httpURLConnection == null)
                return downloadCount;
            httpURLConnection.setConnectTimeout(TIMEOUT);
            httpURLConnection.setReadTimeout(TIMEOUT);

            totalSize = httpURLConnection.getContentLength();
            if (httpURLConnection.getResponseCode() != 200) {
                return downloadCount;
            }

            inputStream = httpURLConnection.getInputStream();
            outputStream = new FileOutputStream(file, false);// 文件存在则覆盖掉
            byte buffer[] = new byte[1024];
            int readsize = 0;

            while ((readsize = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readsize);
                downloadCount += readsize;// 时时获取下载到的大小

                if (updateCount == 0
                        || (downloadCount * 100 / totalSize - down_step) >= updateCount) {
                    updateCount += down_step;
                    mDownloadView.setTextViewText(R.id.notificationPercent,
                            updateCount + "%");
                    mDownloadView.setProgressBar(R.id.notificationProgress,
                            100, updateCount, false);

                    mNotificationManager.notify(NOTIFICATION_ID, mNotification);
                }
            }

            inputStream.close();
            outputStream.close();
        } catch (Exception exp) {
            exp.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        return downloadCount;

    }

    public void createAppFile() {
        if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
                .getExternalStorageState())) {
            File updateAppDir = new File(
                    CommonDataStructure.DOWNLOAD_APP_DIR_URL);
            File updateAppFile = new File(CommonDataStructure.DOWNLOAD_APP_URL);

            if (!updateAppDir.exists()) {
                updateAppDir.mkdirs();
            }

            if (!updateAppFile.exists()) {
                try {
                    updateAppFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
