package com.dhn.marrysocial.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.activity.ContactsInfoActivity;
import com.dhn.marrysocial.base.AsyncHeadPicBitmapLoader;
import com.dhn.marrysocial.base.ChatMsgItem;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.utils.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatMsgViewAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = "ChatMsgViewAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<ChatMsgItem> mData = new ArrayList<ChatMsgItem>();
    private HashMap<String, Bitmap> mHeadPics = new HashMap<String, Bitmap>();
    private Long mLastChatTime = 0l;

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

    public void setHeadPisDataSource(HashMap<String, Bitmap> source) {
        mHeadPics = source;
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

        if (position <= 0) {
            mLastChatTime = 0l;
        } else {
            ChatMsgItem smallItem = mData.get(position - 1);
            if (smallItem == null) {
                mLastChatTime = 0l;
            } else {
                String lastTime = smallItem.getAddedTime();
                mLastChatTime = Long.valueOf(lastTime.substring(0,
                        lastTime.length() - 6));
            }
        }

        final ChatMsgItem msgItem = mData.get(position);

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
            holder.chat_person_pic = (ImageView) convertView
                    .findViewById(R.id.chat_msg_person_pic);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mHeadPics.get(msgItem.getFromUid()) == null) {
            holder.chat_person_pic
                    .setImageResource(R.drawable.person_default_small_pic);
        } else {
            holder.chat_person_pic.setImageBitmap(mHeadPics.get(msgItem
                    .getFromUid()));
        }
        holder.chat_person_pic.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                startToViewContactsInfo(msgItem.getFromUid());
            }
        });

        String time = msgItem.getAddedTime();
        String chat_time = time.substring(0, time.length() - 6);
        if ((Long.valueOf(chat_time) - mLastChatTime) > CommonDataStructure.TIME_FIVE_MINUTES_BEFORE) {
            holder.chat_msg_send_time.setVisibility(View.VISIBLE);
            holder.chat_msg_send_time.setText(Utils.getAddedTimeTitle(mContext,
                    chat_time));
        } else {
            holder.chat_msg_send_time.setVisibility(View.GONE);
        }

        holder.chat_msg_content.setText(msgItem.getChatContent());

        return convertView;
    }

    class ViewHolder {
        ImageView chat_person_pic;
        TextView chat_msg_send_time;
        TextView chat_msg_content;
    }

    private void startToViewContactsInfo(String uid) {
        Intent intent = new Intent(mContext, ContactsInfoActivity.class);
        intent.putExtra(MarrySocialDBHelper.KEY_UID, uid);
        mContext.startActivity(intent);
    }
}
