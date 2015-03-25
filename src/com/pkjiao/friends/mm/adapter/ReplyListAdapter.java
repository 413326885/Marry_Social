package com.pkjiao.friends.mm.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.pkjiao.friends.mm.R;
import com.pkjiao.friends.mm.adapter.DynamicInfoListAdapter.ViewHolder;
import com.pkjiao.friends.mm.base.AsyncHeadPicBitmapLoader;
import com.pkjiao.friends.mm.base.ReplysItem;

public class ReplyListAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = "ReplyListAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<ReplysItem> mReplyItems = new ArrayList<ReplysItem>();
    private AsyncHeadPicBitmapLoader mHeadPicBitmapLoader;

    public ReplyListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mHeadPicBitmapLoader = new AsyncHeadPicBitmapLoader(mContext);
    }

    public void setReplyDataSource(ArrayList<ReplysItem> source) {
        mReplyItems = source;
    }

    @Override
    public int getCount() {
        return mReplyItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.reply_list_item_layout,
                    parent, false);
            holder = new ViewHolder();
            holder.reply_person_pic = (ImageView) convertView
                    .findViewById(R.id.reply_list_person_pic);
            holder.reply_person_name = (TextView) convertView
                    .findViewById(R.id.reply_list_person_name);
            holder.reply_content = (TextView) convertView
                    .findViewById(R.id.reply_list_content);
            holder.reply_time = (TextView) convertView
                    .findViewById(R.id.reply_list_msg_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        setViewHolderLoyout(holder, position);
        return convertView;
    }

    class ViewHolder {
        ImageView reply_person_pic;
        TextView reply_person_name;
        TextView reply_content;
        TextView reply_time;
    }

    private void setViewHolderLoyout(final ViewHolder holder, final int position) {
        holder.reply_person_name.setText(mReplyItems.get(position).getNickname());
        holder.reply_content.setText(mReplyItems.get(position).getReplyContents());
        holder.reply_time.setText(mReplyItems.get(position).getReplyTime());
        mHeadPicBitmapLoader.loadImageBitmap(holder.reply_person_pic, mReplyItems.get(position).getUid());
    }
}
