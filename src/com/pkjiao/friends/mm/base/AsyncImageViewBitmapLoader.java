package com.pkjiao.friends.mm.base;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.pkjiao.friends.mm.R;
import com.pkjiao.friends.mm.common.CommonDataStructure;
import com.pkjiao.friends.mm.database.MarrySocialDBHelper;
import com.pkjiao.friends.mm.utils.Utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

public class AsyncImageViewBitmapLoader {

    @SuppressWarnings("unused")
    private static final String TAG = "AsyncImageViewBitmapLoader";

    private static final String[] IMAGES_PROJECTION = {
            MarrySocialDBHelper.KEY_PHOTO_NAME,
            MarrySocialDBHelper.KEY_PHOTO_LOCAL_PATH,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_SMALL_THUMB_PATH,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_BIG_THUMB_PATH,
            MarrySocialDBHelper.KEY_PHOTO_ID };

    public static final int NEED_DECODE_FROM_CLOUD = 1;
    public static final int DECODE_LOCAL_DIRECT = 2;

    private MemoryCache mMemoryCache = new MemoryCache();
    private FileCache mFileCache;
    private Context mContext;
    private MarrySocialDBHelper mDBHelper;

    private Map<ImageView, String> mImageViews = Collections
            .synchronizedMap(new WeakHashMap<ImageView, String>());

    private ExecutorService mExecutorService;

    public AsyncImageViewBitmapLoader(Context context) {
        mContext = context;
        mDBHelper = MarrySocialDBHelper.newInstance(context);
        mFileCache = new FileCache(context);
        mExecutorService = Executors.newFixedThreadPool(5);
    }

    // // 当进入listview时默认的图片，可换成你自己的默认图片
    // final int stub_id = R.drawable.stub;

    // 最主要的方法
    public void loadImageBitmap(ImageView imageView, String url, int from) {

        mImageViews.put(imageView, url);

        // 先从内存缓存中查找
        Bitmap bitmap = mMemoryCache.get(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            // 若没有的话则开启新线程加载图片
            queuePhoto(url, imageView, from);
            imageView.setBackgroundResource(R.color.gray_background_color);
        }
    }

    private void queuePhoto(String url, ImageView imageView, int from) {
        PhotoToLoad p = new PhotoToLoad(url, imageView, from);
        mExecutorService.submit(new PhotosLoader(p));
    }

    private Bitmap getLocalBitmap(String url) {
        File f = mFileCache.getFile(url);

        // 先从文件缓存中查找是否有
        Bitmap bitmap = decodeFile(f);
        if (bitmap != null)
            return bitmap;

        Bitmap thumbBitmap = null;
        Bitmap cropBitmap = null;
        if (url != null && url.length() != 0) {
            thumbBitmap = Utils.decodeThumbnail(url, null,
                    Utils.mThumbPhotoWidth);
            cropBitmap = Utils.resizeAndCropCenter(thumbBitmap,
                    Utils.mCropCenterThumbPhotoWidth, true);
        }
        return cropBitmap;
    }

    private Bitmap getBitmap(String url) {
        File f = mFileCache.getFile(url);

        // 先从文件缓存中查找是否有
        Bitmap bitmap = decodeFile(f);
        if (bitmap != null)
            return bitmap;

        String[] paths = url.split("_");
        String uId = paths[0];
        String bucketId = paths[1];
        String commentId = paths[2];
        String photoPos = paths[3];
        Cursor cursor = null;
        String whereClause = null;

        try {

            if (commentId != null && Integer.valueOf(commentId) > 0) {
                whereClause = MarrySocialDBHelper.KEY_UID + " = " + uId
                        + " AND " + MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                        + commentId + " AND "
                        + MarrySocialDBHelper.KEY_PHOTO_POS + " = " + photoPos;
            } else {
                whereClause = MarrySocialDBHelper.KEY_UID + " = " + uId
                        + " AND " + MarrySocialDBHelper.KEY_BUCKET_ID + " = "
                        + bucketId + " AND "
                        + MarrySocialDBHelper.KEY_PHOTO_POS + " = " + photoPos;
            }

            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_IMAGES_TABLE,
                    IMAGES_PROJECTION, whereClause, null, null, null, null,
                    null);
            if (cursor == null) {
                Log.e(TAG, "nannan getBitmap()..  cursor == null");
                return null;
            }

            String photoLocalPath = "";
            String photoRemoteOrgPath = "";
            String photoRemoteSmallThumbPath = "";
            String photoRemoteBigThumbPath = "";
            String photoId = "";

            cursor.moveToNext();
            photoLocalPath = cursor.getString(1);
            photoRemoteOrgPath = cursor.getString(2);
            photoRemoteSmallThumbPath = cursor.getString(3);
            photoRemoteBigThumbPath = cursor.getString(4);
            photoId = cursor.getString(5);

            Bitmap thumbBitmap = null;
            Bitmap cropBitmap = null;
            if (photoLocalPath != null && photoLocalPath.length() != 0) {
                thumbBitmap = Utils.decodeThumbnail(photoLocalPath, null,
                        Utils.mThumbPhotoWidth);
                cropBitmap = Utils.resizeAndCropCenter(thumbBitmap,
                        Utils.mCropCenterThumbPhotoWidth, true);
                return cropBitmap;
            }

            // 最后从指定的url中下载图片
            File imageFile = Utils.downloadImageAndCache(photoRemoteSmallThumbPath,
                    CommonDataStructure.DOWNLOAD_PICS_DIR_URL);
            thumbBitmap = Utils.decodeThumbnail(imageFile.getAbsolutePath(),
                    null, Utils.mThumbPhotoWidth);
            cropBitmap = Utils.resizeAndCropCenter(thumbBitmap,
                    Utils.mCropCenterThumbPhotoWidth, true);

            updateImageStatusOfImagesDB(uId, commentId, photoId,
                    imageFile.getAbsolutePath(),
                    MarrySocialDBHelper.DOWNLOAD_FROM_CLOUD_SUCCESS);

            return cropBitmap;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }

        }
        return null;
    }

    // decode这个图片并且按比例缩放以减少内存消耗，虚拟机对每张图片的缓存大小也是有限制的
    private Bitmap decodeFile(File f) {
        // try {
        // // decode image size
        // BitmapFactory.Options o = new BitmapFactory.Options();
        // o.inJustDecodeBounds = true;
        // BitmapFactory.decodeStream(new FileInputStream(f), null, o);
        //
        // // Find the correct scale value. It should be the power of 2.
        // final int REQUIRED_SIZE = 70;
        // int width_tmp = o.outWidth, height_tmp = o.outHeight;
        // int scale = 1;
        // while (true) {
        // if (width_tmp / 2 < REQUIRED_SIZE
        // || height_tmp / 2 < REQUIRED_SIZE)
        // break;
        // width_tmp /= 2;
        // height_tmp /= 2;
        // scale *= 2;
        // }
        //
        // // decode with inSampleSize
        // BitmapFactory.Options o2 = new BitmapFactory.Options();
        // o2.inSampleSize = scale;
        // return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        // } catch (FileNotFoundException e) {
        // }
        return null;
    }

    // Task for the queue
    private class PhotoToLoad {
        public String url;
        public ImageView imageView;
        public int from;

        public PhotoToLoad(String u, ImageView i, int f) {
            url = u;
            imageView = i;
            from = f;
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
            Bitmap bmp = null;
            switch (photoToLoad.from) {
            case NEED_DECODE_FROM_CLOUD: {
                bmp = getBitmap(photoToLoad.url);
                break;
            }
            case DECODE_LOCAL_DIRECT: {
                bmp = getLocalBitmap(photoToLoad.url);
                break;
            }
            default:
                break;
            }
            mMemoryCache.put(photoToLoad.url, bmp);
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
        if (tag == null || !tag.equals(photoToLoad.url))
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
        mFileCache.clear();
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (;;) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

    private void updateImageStatusOfImagesDB(String uid, String comment_id,
            String photo_id, String localPath, int updateStatus) {
        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + uid
                + " AND " + MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                + comment_id + " AND " + MarrySocialDBHelper.KEY_PHOTO_ID
                + " = " + photo_id;
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_CURRENT_STATUS, updateStatus);
        values.put(MarrySocialDBHelper.KEY_PHOTO_LOCAL_PATH, localPath);

        try {
            mDBHelper.update(MarrySocialDBHelper.DATABASE_IMAGES_TABLE, values,
                    whereClause, null);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }
}
