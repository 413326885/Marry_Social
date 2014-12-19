package com.dhn.marrysocial.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dhn.marrysocial.R;

public class ChatMsgListAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = "ChatMsgListAdapter";

    private Context mContext;
    private LayoutInflater mInflater;

    public ChatMsgListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

//    public void setDataSource(ArrayList<ContactsInfo> source) {
//        mData = source;
//    }

    @Override
    public int getCount() {
        return 5;
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
            convertView = mInflater.inflate(R.layout.chat_msg_item_layout,
                    parent, false);
            holder = new ViewHolder();
            holder.chat_person_pic = (ImageView) convertView
                    .findViewById(R.id.chat_person_pic);
            holder.chat_person_name = (TextView) convertView
                    .findViewById(R.id.chat_person_name);
            holder.chat_description = (TextView) convertView
                    .findViewById(R.id.chat_msg_description);
            holder.chat_time = (TextView) convertView
                    .findViewById(R.id.chat_msg_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }

    class ViewHolder {
        ImageView chat_person_pic;
        TextView chat_person_name;
        TextView chat_description;
        TextView chat_time;
    }

}
