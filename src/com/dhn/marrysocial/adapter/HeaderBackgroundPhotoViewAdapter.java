package com.dhn.marrysocial.adapter;

import java.util.ArrayList;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.common.CommonDataStructure.HeaderBackgroundEntry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class HeaderBackgroundPhotoViewAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<HeaderBackgroundEntry> mHeadBkgEntrys = new ArrayList<HeaderBackgroundEntry>();
    private PhotoOperation mPhotoOperation;

    public static interface PhotoOperation {
        public void onPhotoClicked(int position);
    }

    public HeaderBackgroundPhotoViewAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    public void setDataSource(ArrayList<HeaderBackgroundEntry> entrys) {
        mHeadBkgEntrys = entrys;
    }

    public void setPhotoOperationListener(PhotoOperation photoOper) {
        mPhotoOperation = photoOper;
    }

    @Override
    public int getCount() {
        return mHeadBkgEntrys.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mHeadBkgEntrys.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
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
        holder.added_pics
                .setImageBitmap(mHeadBkgEntrys.get(photoIndex).bkgBitmap);
        holder.added_pics.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPhotoOperation.onPhotoClicked(photoIndex);
            }
        });
        holder.delete_pics.setVisibility(View.GONE);
        return convertView;
    }

    class ViewHolder {
        ImageView added_pics;
        ImageView delete_pics;
    }
}
