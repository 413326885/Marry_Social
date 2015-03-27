package com.pkjiao.friends.mm.activity;

import java.util.ArrayList;

import com.pkjiao.friends.mm.R;
import com.pkjiao.friends.mm.adapter.PhotoPagerAdapter;
import com.pkjiao.friends.mm.adapter.PhotoPagerAdapter.PhotoClickListener;
import com.pkjiao.friends.mm.base.CommentsItem;
import com.pkjiao.friends.mm.database.MarrySocialDBHelper;
import com.pkjiao.friends.mm.fragment.ImageDetailFragment;
import com.pkjiao.friends.mm.utils.Utils;
import com.pkjiao.friends.mm.view.DragImageView;
import com.pkjiao.friends.mm.view.HackyViewPager;
import com.pkjiao.friends.mm.viewpagerindicator.LinePageIndicator;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;

public class ViewPhotoActivity extends FragmentActivity {

    private static final String TAG = "MarrySocialMainActivity";

    private final String[] IMAGES_PROJECTION = {
            MarrySocialDBHelper.KEY_PHOTO_NAME,
            MarrySocialDBHelper.KEY_PHOTO_LOCAL_PATH,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_ORG_PATH,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_SMALL_THUMB_PATH,
            MarrySocialDBHelper.KEY_PHOTO_REMOTE_BIG_THUMB_PATH,
            MarrySocialDBHelper.KEY_PHOTO_POS };

    private HackyViewPager mViewPager;
    // private ArrayList<DragImageView> mPhotoViews = new
    // ArrayList<DragImageView>();
    private ArrayList<ImageView> mPhotoViews = new ArrayList<ImageView>();
    private ArrayList<String> mPhotoUrls = new ArrayList<String>();
    private ArrayList<SmallPhotoItem> mSmallPhotoItems = new ArrayList<SmallPhotoItem>();
    private MarrySocialDBHelper mDBHelper;
    private boolean mIsLocalPhotos;

    private String mUId;
    private String mBucketId;
    private String mCommentId;
    private int mOrgPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.photo_view_layout);

        Intent intent = getIntent();
        mUId = intent.getStringExtra(MarrySocialDBHelper.KEY_UID);
        mBucketId = intent.getStringExtra(MarrySocialDBHelper.KEY_BUCKET_ID);
        mCommentId = intent.getStringExtra(MarrySocialDBHelper.KEY_COMMENT_ID);
        mOrgPosition = intent.getIntExtra(MarrySocialDBHelper.KEY_PHOTO_POS, 0);
        mDBHelper = MarrySocialDBHelper.newInstance(this);

        loadPhotoItemsFromDB();
        setPhotoViewBitmaps();
        initPhotoViewUrls();
        initViewPager();

    }

    private void initViewPager() {
        mViewPager = (HackyViewPager) findViewById(R.id.photo_view_pager);
        if (mIsLocalPhotos) {
            PhotoPagerAdapter photoAdapter = new PhotoPagerAdapter(this,
                    mPhotoViews);
            photoAdapter.setPhotoClickListener(mPhotoClickListener);
            mViewPager.setAdapter(photoAdapter);
            mViewPager.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Log.e(TAG, "ViewPhotoActivity.this.finish()");
                    ViewPhotoActivity.this.finish();
                }
            });
        } else {
            ImagePagerAdapter photoAdapter = new ImagePagerAdapter(
                    getSupportFragmentManager(), mSmallPhotoItems);
            mViewPager.setAdapter(photoAdapter);
        }

        LinePageIndicator underLineIndicator = (LinePageIndicator) findViewById(R.id.photo_view_indicator);
        underLineIndicator.setViewPager(mViewPager, mOrgPosition);
    }

    private void loadPhotoItemsFromDB() {
        Cursor cursor = null;
        String whereClause = null;

        if (Integer.valueOf(mCommentId) == -1) {
            whereClause = MarrySocialDBHelper.KEY_UID + " = " + mUId + " AND "
                    + MarrySocialDBHelper.KEY_BUCKET_ID + " = " + mBucketId;
        } else {
            whereClause = MarrySocialDBHelper.KEY_UID + " = " + mUId + " AND "
                    + MarrySocialDBHelper.KEY_COMMENT_ID + " = " + mCommentId;
        }

        try {

            String orderBy = MarrySocialDBHelper.KEY_PHOTO_POS + " ASC";
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_IMAGES_TABLE,
                    IMAGES_PROJECTION, whereClause, null, null, null, orderBy,
                    null);
            if (cursor == null) {
                Log.e(TAG, "nannan loadCommentsItemFromDB()..  cursor == null");
                return;
            }
            while (cursor.moveToNext()) {
                SmallPhotoItem item = new SmallPhotoItem();
                item.photoName = cursor.getString(0);
                item.photoLocalPath = cursor.getString(1);
                item.photoRemoteOrgPath = cursor.getString(2);
                item.photoRemoteSmallThumbPath = cursor.getString(3);
                item.photoRemoteBigThumbPath = cursor.getString(4);
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
            // DragImageView dragImageView = new DragImageView(this);
            ImageView dragImageView = new ImageView(this);
            dragImageView.setImageBitmap(bmp);
            mPhotoViews.add(dragImageView);
        }
    }

    private void initPhotoViewUrls() {
        if (mSmallPhotoItems.get(0).photoRemoteOrgPath == null || mSmallPhotoItems.get(0).photoRemoteOrgPath.length() == 0) {
            mIsLocalPhotos = true;
        }
        for (SmallPhotoItem item : mSmallPhotoItems) {
            String photoPath = item.photoRemoteOrgPath;
            mPhotoUrls.add(photoPath);
        }
    }

    class SmallPhotoItem {
        String photoName;
        String photoLocalPath;
        String photoRemoteOrgPath;
        String photoRemoteSmallThumbPath;
        String photoRemoteBigThumbPath;
    }

    private PhotoClickListener mPhotoClickListener = new PhotoClickListener() {

        @Override
        public void onPhotoClicked() {
            ViewPhotoActivity.this.finish();
        }
    };

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {

        public ArrayList<SmallPhotoItem> photoUrlList;

        public ImagePagerAdapter(FragmentManager fm, ArrayList<SmallPhotoItem> fileList) {
            super(fm);
            this.photoUrlList = fileList;
        }

        @Override
        public int getCount() {
            return photoUrlList == null ? 0 : photoUrlList.size();
        }

        @Override
        public Fragment getItem(int position) {
            String url;
            url= photoUrlList.get(position).photoRemoteOrgPath;
            if (url == null || url.length() == 0) {
                url = photoUrlList.get(position).photoLocalPath;
            }
            return ImageDetailFragment.newInstance(url);
        }

    }
}
