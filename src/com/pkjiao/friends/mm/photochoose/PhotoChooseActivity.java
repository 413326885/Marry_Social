package com.pkjiao.friends.mm.photochoose;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dhn.marrysocial.R;
import com.pkjiao.friends.mm.dialog.ProgressLoadDialog;
import com.pkjiao.friends.mm.photochoose.PhotoChooseAdapter.OnPhotoClickListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.PopupWindow.OnDismissListener;

public class PhotoChooseActivity extends Activity {

    private static final String TAG = "EditCommentsActivity";

    private static final int POOL_SIZE = 10;
    private static final int LOADPHOTOFINISH = 11;
    private static final int STARTTOLOADPHOTOS = 12;

    private static final String[] PROJECTION_BUCKET = { ImageColumns.BUCKET_ID,
            FileColumns.MEDIA_TYPE, ImageColumns.BUCKET_DISPLAY_NAME,
            ImageColumns.MIME_TYPE, ImageColumns.DATA, ImageColumns.DATE_TAKEN };

    private static final String[] COUNT_PROJECTION = { "count(*)" };

    private static final int INDEX_BUCKET_ID = 0;
    private static final int INDEX_MEDIA_TYPE = 1;
    private static final int INDEX_BUCKET_NAME = 2;
    private static final int INDEX_MIME_TYPE = 3;
    private static final int INDEX_DATA_PATH = 4;

    private static final String BUCKET_GROUP_BY = " GROUP BY 1,(2) ";

    private static final String BUCKET_ORDER_BY = "MAX(datetaken) DESC";
    private static final String BUCKET_ORDER_BY_DATATAKEN = ImageColumns.DATE_TAKEN
            + " DESC ";

    private String mChoosedAlbumBucketId;
    private String mChoosedAlbumName;
    private int mChoosedAlbumPhotosCount;

    private RelativeLayout mReturnBackBtn;
    private RelativeLayout mChoosedAlbumBottomLayout;
    private TextView mChoosedAlbumNameBtn;
    private TextView mChoosedAlbumPhotosCountBtn;
    private Button mChoosePhotoFinishBtn;

    private int mScreenHeight;
    private PopupWindow mPopupWindow;

    private ProgressLoadDialog mProgressDialog;
    private GridView mPhotoGridView;
    private PhotoChooseAdapter mPhotoChooseAdapter;

    private ListView mAlbumListView;
    private AlbumChooseAdapter mAlbumChooseAdapter;

    private ArrayList<AlbumItem> mAlbums = new ArrayList<AlbumItem>();
    private ArrayList<String> mChoosedAlbumPhotos = new ArrayList<String>();

    private Activity mContext;
    private ExecutorService mExecutorService;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case LOADPHOTOFINISH: {
                mProgressDialog.dismiss();
                if (mAlbums != null && mAlbums.size() != 0) {
                    AlbumItem item = mAlbums.get(0);
                    mChoosedAlbumBucketId = item.getAlbumBucketId();
                    mChoosedAlbumName = item.getAlbumDisplayName();
                    mChoosedAlbumPhotosCount = item.getAlbumPhotoCount();
                    mAlbums.get(0).setIsSelected(true);
                }
                initPopupWindow();
                mHandler.sendEmptyMessage(STARTTOLOADPHOTOS);
                break;
            }
            case STARTTOLOADPHOTOS: {
                if (mChoosedAlbumBucketId != null) {
                    mChoosedAlbumPhotos.clear();
                    mChoosedAlbumPhotos
                            .addAll(loadChoosedAlbumPhoto(mChoosedAlbumBucketId));
                    mPhotoChooseAdapter.notifyDataSetChanged();
                }
                mChoosedAlbumNameBtn.setText(mChoosedAlbumName);
                mChoosedAlbumPhotosCountBtn.setText(String.format(
                        mContext.getString(R.string.album_photo_count),
                        mChoosedAlbumPhotosCount));
                mPopupWindow.dismiss();
                break;
            }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_photo_layout);

        mContext = this;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenHeight = displayMetrics.heightPixels;

        mReturnBackBtn = (RelativeLayout) findViewById(R.id.photo_choose_return);
        mChoosePhotoFinishBtn = (Button) findViewById(R.id.photo_choose_finish);
        mPhotoGridView = (GridView) findViewById(R.id.photo_choose_gridview);
        mChoosedAlbumNameBtn = (TextView) findViewById(R.id.choosed_album_name);
        mChoosedAlbumPhotosCountBtn = (TextView) findViewById(R.id.album_photo_count);
        mChoosedAlbumBottomLayout = (RelativeLayout) findViewById(R.id.album_choose);
        mPhotoChooseAdapter = new PhotoChooseAdapter(mContext);
        mPhotoChooseAdapter.setDataSource(mChoosedAlbumPhotos);
        mPhotoChooseAdapter.setOnPhotoClickListener(mPhotoClickListener);
        mPhotoGridView.setAdapter(mPhotoChooseAdapter);

        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);

        getAlbumsFromDB();

        mChoosedAlbumBottomLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.setAnimationStyle(R.style.album_popup_anim);
                mPopupWindow.showAsDropDown(mChoosedAlbumBottomLayout, 0, 0);

                // 设置背景颜色变暗
                WindowManager.LayoutParams lparams = getWindow()
                        .getAttributes();
                lparams.alpha = .3f;
                getWindow().setAttributes(lparams);
            }
        });
        mReturnBackBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                PhotoChooseActivity.this.finish();
            }
        });
        mChoosePhotoFinishBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
            }
        });
    }

    private void getAlbumsFromDB() {
        mProgressDialog = new ProgressLoadDialog(this);
        mProgressDialog.setText("正在努力的加载图片，请稍后...");
        mProgressDialog.show();
        mExecutorService.execute(new LoadAlbums());
    }

    class LoadAlbums implements Runnable {

        public LoadAlbums() {
        }

        @Override
        public void run() {

            Uri uri = Files.getContentUri("external");
            String whereClause = "1) AND " + FileColumns.MEDIA_TYPE + "=1"
                    + " AND ( " + MediaStore.Images.Media.MIME_TYPE + "=? or "
                    + MediaStore.Images.Media.MIME_TYPE + "=?" + " ) "
                    + " GROUP BY 1,(2";
            ContentResolver contentResolver = mContext.getContentResolver();
            Cursor cursor = contentResolver.query(uri, PROJECTION_BUCKET,
                    whereClause, new String[] { "image/jpeg", "image/png" },
                    BUCKET_ORDER_BY);

            if (cursor == null) {
                Log.w(TAG, "nannan query fail!");
                return;
            }

            try {
                while (cursor.moveToNext()) {
                    String albumName = cursor.getString(INDEX_BUCKET_NAME);
                    String albumBucketId = cursor.getString(INDEX_BUCKET_ID);
                    String firstPhotoPath = cursor.getString(INDEX_DATA_PATH);
                    AlbumItem albumItem = new AlbumItem();
                    albumItem.setAlbumBucketId(albumBucketId);
                    albumItem.setAlbumDisplayName(albumName);
                    albumItem.setAlbumFirstPhotoPath(firstPhotoPath);
                    albumItem.setIsSelected(false);
                    int photoCount = getPhotoCount(albumBucketId);
                    albumItem.setAlbumPhotoCount(photoCount);
                    mAlbums.add(albumItem);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                mHandler.sendEmptyMessage(LOADPHOTOFINISH);
            }
        }
    }

    private int getPhotoCount(String bucketId) {
        Uri uri = Files.getContentUri("external");
        String whereClause = ImageColumns.BUCKET_ID + " = ?" + " AND ( "
                + MediaStore.Images.Media.MIME_TYPE + " = ? or "
                + MediaStore.Images.Media.MIME_TYPE + " = ?" + " )";
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(uri, COUNT_PROJECTION,
                whereClause,
                new String[] { bucketId, "image/jpeg", "image/png" }, null);
        if (cursor == null) {
            Log.w(TAG, "query fail");
            return 0;
        }
        try {
            cursor.moveToNext();
            return cursor.getInt(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    private ArrayList<String> loadChoosedAlbumPhoto(String bucketId) {

        ArrayList<String> photos = new ArrayList<String>();

        Uri uri = Files.getContentUri("external");
        String whereClause = ImageColumns.BUCKET_ID + " = ?" + " AND ( "
                + MediaStore.Images.Media.MIME_TYPE + " = ? or "
                + MediaStore.Images.Media.MIME_TYPE + " = ?" + " )";
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(uri, PROJECTION_BUCKET,
                whereClause,
                new String[] { bucketId, "image/jpeg", "image/png" },
                BUCKET_ORDER_BY_DATATAKEN);

        if (cursor == null) {
            Log.w(TAG, "query fail");
            return photos;
        }

        try {
            while (cursor.moveToNext()) {
                String photoPath = cursor.getString(INDEX_DATA_PATH);
                photos.add(photoPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return photos;
    }

    private void initPopupWindow() {
        mAlbumListView = (ListView) (LayoutInflater.from(mContext).inflate(
                R.layout.choose_album_layout, null, false));
        mAlbumChooseAdapter = new AlbumChooseAdapter(mContext);
        mAlbumChooseAdapter.setDataSource(mAlbums);
        mAlbumListView.setAdapter(mAlbumChooseAdapter);
        mAlbumListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                AlbumItem item = mAlbums.get(position);
                mChoosedAlbumBucketId = item.getAlbumBucketId();
                mChoosedAlbumName = item.getAlbumDisplayName();
                mChoosedAlbumPhotosCount = item.getAlbumPhotoCount();
                mAlbums.get(position).setIsSelected(true);
                mHandler.sendEmptyMessage(STARTTOLOADPHOTOS);
            }
        });
        mAlbumChooseAdapter.notifyDataSetChanged();

        mPopupWindow = new PopupWindow(mAlbumListView,
                LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7), true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    mPopupWindow.dismiss();
                    return true;
                }
                return false;
            }
        });
        mPopupWindow.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1.0f;
                getWindow().setAttributes(params);
            }
        });
    }

    private OnPhotoClickListener mPhotoClickListener = new OnPhotoClickListener() {

        @Override
        public void onPhotoClicked(int count) {
            if (count == 0) {
                mChoosePhotoFinishBtn.setText("完成");
                mChoosePhotoFinishBtn.setEnabled(false);
            } else {
                String text = String.format(
                        mContext.getString(R.string.choose_photo_finish),
                        count, 9);
                mChoosePhotoFinishBtn.setText(text);
                mChoosePhotoFinishBtn.setEnabled(true);
            }

        }
    };
}
