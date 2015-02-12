package com.pkjiao.friends.mm.adapter;

import java.util.ArrayList;

import com.pkjiao.friends.mm.view.DragImageView;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PhotoPagerAdapter extends PagerAdapter {

    private Context mContext;
    private ArrayList<ImageView> mPhotoViews;
    private PhotoClickListener mPhotoClickListener;

    public interface PhotoClickListener {
        public void onPhotoClicked();
    }

    public void setPhotoClickListener(PhotoClickListener listener) {
        mPhotoClickListener = listener;
    }

    public PhotoPagerAdapter(Context context, ArrayList<ImageView> photos) {
        mContext = context;
        mPhotoViews = photos;
    }

    @Override
    public int getCount() {
        return mPhotoViews.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mPhotoViews.get(position);
        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mPhotoClickListener.onPhotoClicked();
            }
        });
        ((ViewPager) container).addView(view);
        return mPhotoViews.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }
}
