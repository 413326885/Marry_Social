package com.pkjiao.friends.mm.photochoose;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.dhn.marrysocial.R;
import com.pkjiao.friends.mm.base.AsyncImageViewBitmapLoader;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

public class PhotoChooseAdapter extends BaseAdapter {

    protected LayoutInflater mInflater;
    protected Context mContext;
    private AsyncImageViewBitmapLoader mAsyncBitmapLoader;
    private List<String> mPhotos = new ArrayList<String>();
    private List<String> mChoosedPhotos = new LinkedList<String>();
    private OnPhotoClickListener mPhotoClickListener;

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        mPhotoClickListener = listener;
    }

    public PhotoChooseAdapter(Context context) {
        mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        mAsyncBitmapLoader = new AsyncImageViewBitmapLoader(mContext);
    }

    public void setDataSource(ArrayList<String> photos) {
        mPhotos = photos;
    }

    @Override
    public int getCount() {
        return mPhotos.size();
    }

    @Override
    public Object getItem(int position) {
        return mPhotos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.choose_photo_item_layout,
                    parent, false);
            holder = new ViewHolder();
            holder.choosedPhoto = (ImageView) convertView
                    .findViewById(R.id.choosed_photo);
            holder.choosedPhotoIcon = (ImageView) convertView
                    .findViewById(R.id.choosed_photo_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final String photoPath = mPhotos.get(position);
        mAsyncBitmapLoader.loadImageBitmap(holder.choosedPhoto, photoPath,
                AsyncImageViewBitmapLoader.DECODE_LOCAL_DIRECT);

        final ViewHolder holderTmp = holder;
        holder.choosedPhoto.setColorFilter(null);
        holder.choosedPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mChoosedPhotos.contains(photoPath)) {
                    mChoosedPhotos.remove(photoPath);
                    holderTmp.choosedPhoto.setColorFilter(null);
                    mPhotoClickListener.onPhotoClicked(mChoosedPhotos.size());
                } else if (mChoosedPhotos.size() < 9) {
                    mChoosedPhotos.add(photoPath);
                    holderTmp.choosedPhoto.setColorFilter(Color
                            .parseColor("#77000000"));
                    mPhotoClickListener.onPhotoClicked(mChoosedPhotos.size());
                } else {
                    Toast.makeText(
                            mContext,
                            mContext.getResources().getString(
                                    R.string.add_pics_limit),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (mChoosedPhotos.contains(photoPath)) {
            holder.choosedPhoto.setColorFilter(Color.parseColor("#77000000"));
        }

        return convertView;
    }

    class ViewHolder {
        ImageView choosedPhoto;
        ImageView choosedPhotoIcon;
    }

    public interface OnPhotoClickListener {
        public void onPhotoClicked(int count);
    }
}
