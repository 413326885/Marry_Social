package com.pkjiao.friends.mm.base;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.pkjiao.friends.mm.R;
import com.pkjiao.friends.mm.database.MarrySocialDBHelper;

public class AsyncHeadPicBitmapLoader {

    @SuppressWarnings("unused")
    private static final String TAG = "AsyncHeadPicBitmapLoader";

    private static final String[] HEAD_PICS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_HEAD_PIC_BITMAP,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_THUMB_PATH };

    private MemoryCache mMemoryCache = new MemoryCache();
    private Context mContext;
    private MarrySocialDBHelper mDBHelper;

    private Map<ImageView, String> mImageViews = Collections
            .synchronizedMap(new WeakHashMap<ImageView, String>());

    private ExecutorService mExecutorService;

    public AsyncHeadPicBitmapLoader(Context context) {
        mContext = context;
        mDBHelper = MarrySocialDBHelper.newInstance(context);
        mExecutorService = Executors.newFixedThreadPool(5);
    }

    // // 当进入listview时默认的图片，可换成你自己的默认图片
    // final int stub_id = R.drawable.stub;

    // 最主要的方法
    public void loadImageBitmap(ImageView imageView, String uid) {

        mImageViews.put(imageView, uid);

        // 先从内存缓存中查找
        Bitmap bitmap = mMemoryCache.get(uid);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            // 若没有的话则开启新线程加载图片
            queuePhoto(uid, imageView);
            imageView.setImageResource(R.drawable.person_default_small_pic);
        }
    }

    private void queuePhoto(String uid, ImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(uid, imageView);
        mExecutorService.submit(new PhotosLoader(p));
    }

    private Bitmap getBitmap(String uid) {

        Bitmap headpic = null;

        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + uid;
        Cursor cursor = mDBHelper
                .query(MarrySocialDBHelper.DATABASE_HEAD_PICS_TABLE,
                        HEAD_PICS_PROJECTION, whereClause, null, null, null,
                        null, null);

        if (cursor == null || cursor.getCount() == 0) {
            Log.w(TAG, "nannan query fail! Uid = " + uid);
            return null;
        }

        try {
            cursor.moveToFirst();
            byte[] in = cursor.getBlob(1);
            headpic = BitmapFactory.decodeByteArray(in, 0, in.length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return headpic;
    }

    // Task for the queue
    private class PhotoToLoad {
        public String uid;
        public ImageView imageView;

        public PhotoToLoad(String u, ImageView i) {
            uid = u;
            imageView = i;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            if (imageViewReused(photoToLoad))
                return;
            Bitmap bmp = getBitmap(photoToLoad.uid);
            if (bmp == null)
                return;
            mMemoryCache.put(photoToLoad.uid, bmp);
            if (imageViewReused(photoToLoad))
                return;
            BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
            // 更新的操作放在UI线程中
            Activity a = (Activity) photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }

    /**
     * 防止图片错位
     * 
     * @param photoToLoad
     * @return
     */
    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = mImageViews.get(photoToLoad.imageView);
        if (tag == null || !tag.equals(photoToLoad.uid))
            return true;
        return false;
    }

    // 用于在UI线程中更新界面
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if (imageViewReused(photoToLoad))
                return;
            if (bitmap != null) {
                photoToLoad.imageView.setImageBitmap(bitmap);
            } else {
                photoToLoad.imageView
                        .setBackgroundResource(R.color.gray_background_color);
            }
        }
    }

    public void clearCache() {
        mMemoryCache.clear();
    }

}
