package com.pkjiao.friends.mm.photochoose;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkjiao.friends.mm.R;
import com.pkjiao.friends.mm.base.AsyncImageViewBitmapLoader;

public class AlbumChooseAdapter extends BaseAdapter {

    protected LayoutInflater mInflater;
    protected Context mContext;
    private AsyncImageViewBitmapLoader mAsyncBitmapLoader;
    private List<AlbumItem> mAlbums = new ArrayList<AlbumItem>();

    public AlbumChooseAdapter(Context context) {
        mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        mAsyncBitmapLoader = new AsyncImageViewBitmapLoader(mContext);
    }

    public void setDataSource(ArrayList<AlbumItem> albums) {
        mAlbums = albums;
    }

    @Override
    public int getCount() {
        return mAlbums.size();
    }

    @Override
    public Object getItem(int position) {
        return mAlbums.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.choose_album_item_layout,
                    parent, false);
            holder = new ViewHolder();
            holder.choosedAlbum = (ImageView) convertView
                    .findViewById(R.id.choosed_album);
            holder.choosedAlbumIcon = (ImageView) convertView
                    .findViewById(R.id.choosed_album_icon);
            holder.chooseAlbumName = (TextView) convertView
                    .findViewById(R.id.choosed_album_display_name);
            holder.chooseAlbumPhotoCount = (TextView) convertView
                    .findViewById(R.id.choosed_album_photo_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final AlbumItem album = mAlbums.get(position);
        mAsyncBitmapLoader.loadImageBitmap(holder.choosedAlbum,
                album.getAlbumFirstPhotoPath(),
                AsyncImageViewBitmapLoader.DECODE_LOCAL_DIRECT);
        holder.chooseAlbumName.setText(album.getAlbumDisplayName());
        holder.chooseAlbumPhotoCount.setText(String.format(
                mContext.getString(R.string.album_photo_count),
                album.getAlbumPhotoCount()));
        if (album.isSelected()) {
            holder.choosedAlbumIcon.setVisibility(View.VISIBLE);
        } else {
            holder.choosedAlbumIcon.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    class ViewHolder {
        ImageView choosedAlbum;
        ImageView choosedAlbumIcon;
        TextView chooseAlbumName;
        TextView chooseAlbumPhotoCount;
    }
}
