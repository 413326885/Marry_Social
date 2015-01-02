package com.dhn.marrysocial.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.activity.ChatMsgActivity;
import com.dhn.marrysocial.base.ChatMsgItem;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.fragment.ChatMsgFragment;

public class ChatMsgListAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = "ChatMsgListAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<ChatMsgFragment.BriefChatItem> mData;

    public ChatMsgListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    public void setDataSource(ArrayList<ChatMsgFragment.BriefChatItem> source) {
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
    public View getView(int position, View convertView, ViewGroup parent) {

        final ChatMsgFragment.BriefChatItem chatItem = mData.get(position);

        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.chat_msg_item_layout,
                    parent, false);
            holder = new ViewHolder();
            holder.chat_item_entry = (RelativeLayout) convertView
                    .findViewById(R.id.chat_item_entry);
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

        holder.chat_person_name.setText(chatItem.nikename);
        holder.chat_description.setText(chatItem.chatContent);
        String time = chatItem.addTime;
        String chat_time = time.substring(0, time.length() - 4);
        holder.chat_time.setText(chat_time);

        holder.chat_item_entry.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startToChat(chatItem.chatId);
            }
        });

        return convertView;
    }

    class ViewHolder {
        RelativeLayout chat_item_entry;
        ImageView chat_person_pic;
        TextView chat_person_name;
        TextView chat_description;
        TextView chat_time;
    }

    private void startToChat(String chatId) {
        Intent intent = new Intent(mContext, ChatMsgActivity.class);
        intent.putExtra(MarrySocialDBHelper.KEY_CHAT_ID, chatId);
        mContext.startActivity(intent);
    }

}
