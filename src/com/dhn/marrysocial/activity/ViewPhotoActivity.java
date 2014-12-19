package com.dhn.marrysocial.activity;

import java.util.ArrayList;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.adapter.PhotoPagerAdapter;
import com.dhn.marrysocial.base.CommentsItem;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.utils.Utils;
import com.dhn.marrysocial.view.DragImageView;
import com.dhn.marrysocial.viewpagerindicator.LinePageIndicator;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;

public class ViewPhotoActivity extends Activity {

    private static final String TAG = "MarrySocialMainActivity";

    private final String[] IMAGES_PROJECTION = {
            MarrySocialDBHelper.KEY_PHOTO_NAME,
            MarrySocialDBHelper.KEY_PHOTO_LOCAL_PATH,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_THUMB_PATH,
            MarrySocialDBHelper.KEY_PHOTO_POS };

    private ViewPager mViewPager;
//    private ArrayList<DragImageView> mPhotoViews = new ArrayList<DragImageView>();
    private ArrayList<ImageView> mPhotoViews = new ArrayList<ImageView>();
    private ArrayList<SmallPhotoItem> mSmallPhotoItems = new ArrayList<SmallPhotoItem>();
    private MarrySocialDBHelper mDBHelper;

    private String mUId;
    private String mBucketId;
    private int mOrgPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.photo_view_layout);

        Intent intent = getIntent();
        mUId = intent.getStringExtra(MarrySocialDBHelper.KEY_UID);
        mBucketId = intent.getStringExtra(MarrySocialDBHelper.KEY_BUCKET_ID);
        mOrgPosition = intent.getIntExtra(MarrySocialDBHelper.KEY_PHOTO_POS, 0);
        mDBHelper = MarrySocialDBHelper.newInstance(this);

        loadPhotoItemsFromDB();
        setPhotoViewBitmaps();
        initViewPager();

    }

    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.photo_view_pager);
        mViewPager.setAdapter(new PhotoPagerAdapter(this, mPhotoViews));

        LinePageIndicator underLineIndicator = (LinePageIndicator) 
                findViewById(R.id.photo_view_indicator);
        underLineIndicator.setViewPager(mViewPager, mOrgPosition);
    }

    private void loadPhotoItemsFromDB() {
        ArrayList<CommentsItem> commentEntrys = new ArrayList<CommentsItem>();
        Cursor cursor = null;

        try {
            String whereClause = MarrySocialDBHelper.KEY_UID + " = " + mUId
                    + " AND " + MarrySocialDBHelper.KEY_BUCKET_ID + " = " + mBucketId;
            String orderBy = MarrySocialDBHelper.KEY_PHOTO_POS + " ASC";
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_IMAGES_TABLE,
                    IMAGES_PROJECTION, whereClause, null, null, null, orderBy, null);
            if (cursor == null) {
                Log.e(TAG, "nannan loadCommentsItemFromDB()..  cursor == null");
                return;
            }
            while (cursor.moveToNext()) {
                SmallPhotoItem item = new SmallPhotoItem();
                item.photoName = cursor.getString(0);
                item.photoLocalPath = cursor.getString(1);
                item.photoRemoteOrgPath = cursor.getString(2);
                item.photoRemoteThumbPath = cursor.getString(3);
                mSmallPhotoItems.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }

        }
    }

    private void setPhotoViewBitmaps() {
        for (SmallPhotoItem item : mSmallPhotoItems) {
            Bitmap bmp = Utils.decodeThumbnail(item.photoLocalPath, null,
                    Utils.mThumbPhotoWidth);
//            DragImageView dragImageView = new DragImageView(this);
            ImageView dragImageView = new ImageView(this);
            dragImageView.setImageBitmap(bmp);
            mPhotoViews.add(dragImageView);
        }
    }

    class SmallPhotoItem {
        String photoName;
        String photoLocalPath;
        String photoRemoteOrgPath;
        String photoRemoteThumbPath;
    }
}
