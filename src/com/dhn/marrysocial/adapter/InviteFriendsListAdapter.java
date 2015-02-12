package com.dhn.marrysocial.adapter;

import java.util.ArrayList;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.common.CommonDataStructure;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class InviteFriendsListAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = "InviteFriendsListAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<CommonDataStructure.ContactEntry> mData;

    public InviteFriendsListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    public void setDataSource(ArrayList<CommonDataStructure.ContactEntry> source) {
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

        final CommonDataStructure.ContactEntry contact = mData.get(position);

        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(
                    R.layout.invite_friends_item_layout, parent, false);
            holder = new ViewHolder();
            holder.invite_friend_name = (TextView) convertView
                    .findViewById(R.id.invite_friend_name);
            holder.invite_friend_registed = (TextView) convertView
                    .findViewById(R.id.invite_friend_registed);
            holder.invite_friends_btn = (Button) convertView
                    .findViewById(R.id.invite_friends_btn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.invite_friend_name.setText(contact.contact_name);
        if (contact.direct_uid == null
                || "0".equalsIgnoreCase(contact.direct_uid)) {
            holder.invite_friend_registed.setVisibility(View.GONE);
        } else {
            holder.invite_friend_registed.setVisibility(View.VISIBLE);
        }
        // if ("0".equalsIgnoreCase(contact.direct_uid)) {
        // holder.invite_friend_registed.setVisibility(View.GONE);
        // } else {
        // holder.invite_friend_registed.setVisibility(View.VISIBLE);
        // }
        holder.invite_friends_btn
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        startToInviteFriendViaSMS(mContext,
                                contact.contact_phone_number);
                    }
                });

        return convertView;
    }

    class ViewHolder {
        TextView invite_friend_name;
        TextView invite_friend_registed;
        Button invite_friends_btn;
    }

    private void startToInviteFriendViaSMS(Context context, String phone) {
        String body = " friends+： 体验不一样的社交，快来认识你好朋友的朋友，即刻点击下载 www.pkjiao.com（仅支持安卓用户）";
        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
        sendIntent.setData(Uri.parse("smsto:" + phone));
        sendIntent.putExtra("sms_body", body);
        context.startActivity(sendIntent);
    }

}
