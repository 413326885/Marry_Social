package com.dhn.marrysocial.adapter;

import java.util.ArrayList;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.base.ChatMsgItem;
import com.dhn.marrysocial.utils.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatMsgViewAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = "ChatMsgViewAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<ChatMsgItem> mData = new ArrayList<ChatMsgItem>();

    public static interface IMsgViewType {
        int IMVT_COM_MSG = 0;
        int IMVT_TO_MSG = 1;
    }

    public ChatMsgViewAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    public void setChatDataSource(ArrayList<ChatMsgItem> source) {
        mData = source;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMsgItem item = mData.get(position);
        return item.getMsgType();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMsgItem msgItem = mData.get(position);

        if (msgItem == null) {
            return convertView;
        }
        ViewHolder holder = null;
        if (convertView == null) {
            if (msgItem.getMsgType() == IMsgViewType.IMVT_COM_MSG) {
                convertView = mInflater.inflate(
                        R.layout.chat_msg_item_from_left_layout, parent, false);
            } else {
                convertView = mInflater.inflate(
                        R.layout.chat_msg_item_to_right_layout, parent, false);
            }

            holder = new ViewHolder();
            holder.chat_msg_send_time = (TextView) convertView
                    .findViewById(R.id.chat_msg_sendtime);
            holder.chat_msg_content = (TextView) convertView
                    .findViewById(R.id.chat_msg_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String time = msgItem.getAddedTime();
        String chat_time = time.substring(0, time.length() - 6);
        holder.chat_msg_send_time.setText(Utils.getAddedTimeTitle(mContext, chat_time));
        holder.chat_msg_content.setText(msgItem.getChatContent());

        return convertView;
    }

    class ViewHolder {
        TextView chat_msg_send_time;
        TextView chat_msg_content;
    }
}
