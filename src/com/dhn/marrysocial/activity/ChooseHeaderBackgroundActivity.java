package com.dhn.marrysocial.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.adapter.HeaderBackgroundPhotoViewAdapter;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.common.CommonDataStructure.HeaderBackgroundEntry;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.dialog.ProgressLoadDialog;
import com.dhn.marrysocial.utils.Utils;

public class ChooseHeaderBackgroundActivity extends Activity implements
        OnClickListener {

    private static final String TAG = "ChooseHeaderBackgroundActivity";

    private static final int POOL_SIZE = 10;
    private static final int DOWNLOAD_BKG_FINISH = 100;

    private static final String[] HEAD_BKG_PROJECTION = {
            MarrySocialDBHelper.KEY_PHOTO_NAME,
            MarrySocialDBHelper.KEY_PHOTO_LOCAL_PATH,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH,
            MarrySocialDBHelper.KEY_CURRENT_STATUS,
            MarrySocialDBHelper.KEY_HEADER_BACKGROUND_INDEX };

    static {
        File bkgDir = new File(CommonDataStructure.BACKGROUND_PICS_DIR_URL);
        if (!bkgDir.exists()) {
            try {
                bkgDir.mkdirs();
                File nomedia = new File(bkgDir, ".nomedia");
                nomedia.createNewFile();
            } catch (IOException e) {
            }
        }
    }

    private RelativeLayout mChooseBkgReturn;
    private GridView mChooseBkgPics;
    private ProgressLoadDialog mDownloadProgressDialog;
    private MarrySocialDBHelper mDBHelper;
    private ExecutorService mExecutorService;
    private HeaderBackgroundPhotoViewAdapter mPhotoViewAdapter;

    private ArrayList<HeaderBackgroundEntry> mHeadBkgEntrys = new ArrayList<HeaderBackgroundEntry>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case DOWNLOAD_BKG_FINISH: {
                mPhotoViewAdapter.notifyDataSetChanged();
                mDownloadProgressDialog.dismiss();
                break;
            }
            default:
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_header_bkg_layout);
        mChooseBkgReturn = (RelativeLayout) findViewById(R.id.choose_background_return);
        mChooseBkgPics = (GridView) findViewById(R.id.choose_background_add_pics);
        mChooseBkgReturn.setOnClickListener(this);

        mDBHelper = MarrySocialDBHelper.newInstance(this);
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);

        mPhotoViewAdapter = new HeaderBackgroundPhotoViewAdapter(this);
        mPhotoViewAdapter.setDataSource(mHeadBkgEntrys);
        mPhotoViewAdapter.setPhotoOperationListener(mPhotoOperation);
        mChooseBkgPics.setAdapter(mPhotoViewAdapter);

        mDownloadProgressDialog = new ProgressLoadDialog(this);
        mDownloadProgressDialog.setText("正在为你努力的下载封面图片，请稍后...");
        mDownloadProgressDialog.show();

        downloadBackgroundPics();
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
        case R.id.choose_background_return: {
            this.finish();
            break;
        }
        default:
            break;
        }
    }

    private void downloadBackgroundPics() {
        ArrayList<HeaderBackgroundEntry> headBkgEntrys = loadHeaderBackgroundEntrys();
        if (headBkgEntrys == null || headBkgEntrys.size() == 0) {
            return;
        }
        for (HeaderBackgroundEntry bkg : headBkgEntrys) {
            mExecutorService.execute(new DownloadBackgroundPics(bkg));
        }
    }

    class DownloadBackgroundPics implements Runnable {

        private HeaderBackgroundEntry bkgEntry;

        public DownloadBackgroundPics(HeaderBackgroundEntry entry) {
            bkgEntry = entry;
        }

        @Override
        public void run() {
            File imageFile;
            if (bkgEntry.photoLocalPath != null
                    && bkgEntry.photoLocalPath.length() != 0) {
                imageFile = new File(bkgEntry.photoLocalPath);
                if (!imageFile.exists()) {
                    imageFile = Utils.downloadImageAndCache(
                            bkgEntry.photoRemotePath,
                            CommonDataStructure.BACKGROUND_PICS_DIR_URL);
                    updateHeaderBkgPathToHeaderBkgDB(
                            imageFile.getAbsolutePath(),
                            bkgEntry.photoRemotePath);
                }
            } else {
                imageFile = Utils.downloadImageAndCache(
                        bkgEntry.photoRemotePath,
                        CommonDataStructure.BACKGROUND_PICS_DIR_URL);
                updateHeaderBkgPathToHeaderBkgDB(imageFile.getAbsolutePath(),
                        bkgEntry.photoRemotePath);
            }
            Bitmap thumbBitmap = Utils.decodeThumbnail(
                    imageFile.getAbsolutePath(), null, Utils.mThumbPhotoWidth);
            Bitmap cropBitmap = Utils.resizeAndCropCenter(thumbBitmap,
                    Utils.mCropCenterThumbPhotoWidth, true);
            bkgEntry.bkgBitmap = cropBitmap;
            bkgEntry.photoLocalPath = imageFile.getAbsolutePath();
            mHeadBkgEntrys.add(bkgEntry);
            mHandler.sendEmptyMessage(DOWNLOAD_BKG_FINISH);

        }

    }

    // private void generateDBData() {
    // int index = 1;
    // for (String remote : CommonDataStructure.HEADER_BKG_PATH) {
    // String name = index + ".jpg";
    // if (!isHeaderBkgPathExistInHeaderBkgDB(remote)) {
    // insertHeaderBkgPathToHeaderBkgDB(name, remote, index);
    // }
    // index++;
    // }
    // }

    private void insertHeaderBkgPathToHeaderBkgDB(String photoname,
            String remotepath, int picindex) {
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_PHOTO_NAME, photoname);
        values.put(MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH, remotepath);
        values.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.NEED_DOWNLOAD_FROM_CLOUD);
        values.put(MarrySocialDBHelper.KEY_HEADER_BACKGROUND_INDEX,
                String.valueOf(picindex));

        try {
            mDBHelper.insert(
                    MarrySocialDBHelper.DATABASE_HEAD_BACKGROUND_PICS_TABLE,
                    values);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

    private void updateHeaderBkgPathToHeaderBkgDB(String localpath,
            String remotepath) {
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_PHOTO_LOCAL_PATH, localpath);
        values.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.DOWNLOAD_FROM_CLOUD_SUCCESS);

        String whereClause = MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH
                + " = " + '"' + remotepath + '"';

        try {
            mDBHelper.update(
                    MarrySocialDBHelper.DATABASE_HEAD_BACKGROUND_PICS_TABLE,
                    values, whereClause, null);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

        // ContentResolver resolver = getContentResolver();
        // resolver.update(CommonDataStructure.HEADBACKGROUNDURL, values,
        // whereClause, null);
    }

    public boolean isHeaderBkgPathExistInHeaderBkgDB(String remotepath) {
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH
                    + " = " + '"' + remotepath + '"';
            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_HEAD_BACKGROUND_PICS_TABLE,
                    HEAD_BKG_PROJECTION, whereclause, null, null, null, null,
                    null);
            if (cursor == null || cursor.getCount() == 0) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return true;
    }

    private ArrayList<HeaderBackgroundEntry> loadHeaderBackgroundEntrys() {

        ArrayList<HeaderBackgroundEntry> headBkgList = new ArrayList<HeaderBackgroundEntry>();
        Cursor cursor = null;

        try {
            String orderBy = MarrySocialDBHelper.KEY_HEADER_BACKGROUND_INDEX
                    + " ASC ";
            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_HEAD_BACKGROUND_PICS_TABLE,
                    HEAD_BKG_PROJECTION, null, null, null, null, orderBy, null);
            if (cursor == null) {
                Log.e(TAG,
                        "nannan loadHeaderBackgroundEntrys()..  cursor == null");
                return headBkgList;
            }
            while (cursor.moveToNext()) {
                HeaderBackgroundEntry background = new HeaderBackgroundEntry();
                String photoname = cursor.getString(0);
                String localpath = cursor.getString(1);
                String remotepath = cursor.getString(2);
                String currentstatus = cursor.getString(3);
                String headerbkgindex = cursor.getString(4);
                background.photoName = photoname;
                background.photoLocalPath = localpath;
                background.photoRemotePath = remotepath;
                background.currentStatus = currentstatus;
                background.headerBkgIndex = headerbkgindex;
                headBkgList.add(background);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return headBkgList;
    }

    private HeaderBackgroundPhotoViewAdapter.PhotoOperation mPhotoOperation = new HeaderBackgroundPhotoViewAdapter.PhotoOperation() {

        @Override
        public void onPhotoClicked(int position) {

            String photoName = mHeadBkgEntrys.get(position).photoName;
            String photoLocalPath = mHeadBkgEntrys.get(position).photoLocalPath;
            String headerBkgIndex = mHeadBkgEntrys.get(position).headerBkgIndex;
            finishActivity(photoName, photoLocalPath, headerBkgIndex);
        }
    };

    private void finishActivity(String photoName, String photoLocalPath,
            String headerBkgIndex) {

        Intent data = new Intent();
        data.putExtra(MarrySocialDBHelper.KEY_PHOTO_LOCAL_PATH, photoLocalPath);
        data.putExtra(MarrySocialDBHelper.KEY_HEADER_BACKGROUND_INDEX,
                headerBkgIndex);
        setResult(RESULT_OK, data);
        this.finish();
    }
}
