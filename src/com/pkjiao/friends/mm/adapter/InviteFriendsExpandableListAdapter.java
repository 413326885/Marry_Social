package com.pkjiao.friends.mm.adapter;

import java.util.ArrayList;
import java.util.Collections;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dhn.marrysocial.R;
import com.pkjiao.friends.mm.activity.ContactsInfoActivity;
import com.pkjiao.friends.mm.adapter.ContactsExpandableListAdapter.ChildViewHolder;
import com.pkjiao.friends.mm.adapter.ContactsExpandableListAdapter.GroupViewHolder;
import com.pkjiao.friends.mm.adapter.InviteFriendsListAdapter.ViewHolder;
import com.pkjiao.friends.mm.base.AsyncHeadPicBitmapLoader;
import com.pkjiao.friends.mm.base.ContactsInfo;
import com.pkjiao.friends.mm.database.MarrySocialDBHelper;
import com.pkjiao.friends.mm.pingyin.AssortPinyinList;
import com.pkjiao.friends.mm.pingyin.LanguageComparator;
import com.pkjiao.friends.mm.pingyin.LanguageComparator_CN;

public class InviteFriendsExpandableListAdapter extends BaseExpandableListAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = "InviteFriendsExpandableListAdapter";

    private AssortPinyinList assort = new AssortPinyinList();
    private LanguageComparator_CN cnSort = new LanguageComparator_CN();
    private LanguageComparator sort = new LanguageComparator();

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<ContactsInfo> mData;


    public InviteFriendsExpandableListAdapter(Context context) {
        super();
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    public void setDataSource(ArrayList<ContactsInfo> source) {
        mData = source;
        sortByFirstChar();
    }

    private void sortByFirstChar() {
        for (ContactsInfo contact : mData) {
            assort.getHashList().add(contact);
        }
        assort.getHashList().sortKeyComparator(cnSort);
        for (int i = 0, length = assort.getHashList().size(); i < length; i++) {
            Collections.sort((assort.getHashList().getValueListIndex(i)),
                    sort);
        }

    }

    public Object getChild(int group, int child) {
        return assort.getHashList().getValueIndex(group, child);
    }

    public long getChildId(int group, int child) {
        return child;
    }

    public View getChildView(int group, int child, boolean arg2,
            View convertView, ViewGroup parent) {

        ChildViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(
                    R.layout.invite_friends_item_layout, parent, false);
            holder = new ChildViewHolder();
            holder.invite_friend_name = (TextView) convertView
                    .findViewById(R.id.invite_friend_name);
            holder.invite_friend_registed = (TextView) convertView
                    .findViewById(R.id.invite_friend_registed);
            holder.invite_friends_btn = (Button) convertView
                    .findViewById(R.id.invite_friends_btn);
            convertView.setTag(holder);
        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }

        final ContactsInfo contactsItem = assort.getHashList().getValueIndex(
                group, child);
        holder.invite_friend_name.setText(contactsItem.getNickName());
        if (contactsItem.getUid() == null
                || "0".equalsIgnoreCase(contactsItem.getUid())) {
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
                                contactsItem.getPhoneNum());
                    }
                });

        return convertView;

    }

    public int getChildrenCount(int group) {
        return assort.getHashList().getValueListIndex(group).size();
    }

    public Object getGroup(int group) {
        return assort.getHashList().getValueListIndex(group);
    }

    public int getGroupCount() {
        return assort.getHashList().size();
    }

    public long getGroupId(int group) {
        return group;
    }

    public View getGroupView(int group, boolean arg1, View convertView,
            ViewGroup parent) {
        GroupViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.contacts_list_group_item_layout,
                    parent, false);
            holder = new GroupViewHolder();
            holder.group_name = (TextView) convertView
                    .findViewById(R.id.contacts_group_name);
            convertView.setClickable(true);
            convertView.setTag(holder);
        } else {
            holder = (GroupViewHolder) convertView.getTag();
        }
        holder.group_name.setText(assort.getFirstChar(assort.getHashList()
              .getValueIndex(group, 0).getNickName()));

        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int arg0, int arg1) {
        return true;
    }

    public AssortPinyinList getAssort() {
        return assort;
    }

    class ChildViewHolder {
        TextView invite_friend_name;
        TextView invite_friend_registed;
        Button invite_friends_btn;
    }

    class GroupViewHolder {
        TextView group_name;
    }

    private void startToInviteFriendViaSMS(Context context, String phone) {
        String body = " friends+： 体验不一样的社交，快来认识你好朋友的朋友，即刻点击下载 www.pkjiao.com（仅支持安卓用户）";
        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
        sendIntent.setData(Uri.parse("smsto:" + phone));
        sendIntent.putExtra("sms_body", body);
        context.startActivity(sendIntent);
    }
}
