package com.pkjiao.friends.mm.adapter;

import java.util.ArrayList;

import com.pkjiao.friends.mm.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class EditCommentsPhotoViewAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Bitmap> mBitmaps = new ArrayList<Bitmap>();
    private PhotoOperation mPhotoOperation;

    public static interface PhotoOperation {
        public void onPhotoClicked(int position);
    };

    public EditCommentsPhotoViewAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    public void setDataSource(ArrayList<Bitmap> bitmaps) {
        mBitmaps = bitmaps;
    }

    public void setPhotoOperationListener(PhotoOperation photoOper) {
        mPhotoOperation = photoOper;
    }

    @Override
    public int getCount() {
        return mBitmaps.size();
    }

    @Override
    public Object getItem(int position) {
        return mBitmaps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.added_pics_layout, parent,
                    false);
            holder = new ViewHolder();
            holder.added_pics = (ImageView) convertView
                    .findViewById(R.id.added_pics);
            holder.delete_pics = (ImageView) convertView
                    .findViewById(R.id.delete_pics);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final int photoIndex = position;
        holder.added_pics.setImageBitmap(mBitmaps.get(photoIndex));
        holder.delete_pics.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPhotoOperation.onPhotoClicked(photoIndex);
            }
        });
        return convertView;
    }

    class ViewHolder {
        ImageView added_pics;
        ImageView delete_pics;
    }
}
