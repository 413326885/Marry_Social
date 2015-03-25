package com.pkjiao.friends.mm.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.pkjiao.friends.mm.R;
import com.pkjiao.friends.mm.activity.ContactsInfoActivity;
import com.pkjiao.friends.mm.adapter.ContactsListAdapter.ViewHolder;
import com.pkjiao.friends.mm.base.AsyncHeadPicBitmapLoader;
import com.pkjiao.friends.mm.base.ContactsInfo;
import com.pkjiao.friends.mm.database.MarrySocialDBHelper;
import com.pkjiao.friends.mm.pingyin.AssortPinyinList;
import com.pkjiao.friends.mm.pingyin.LanguageComparator;
import com.pkjiao.friends.mm.pingyin.LanguageComparator_CN;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ContactsExpandableListAdapter extends BaseExpandableListAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = "ContactsExpandableListAdapter";

    private AssortPinyinList assort = new AssortPinyinList();
    private LanguageComparator_CN cnSort = new LanguageComparator_CN();
    private LanguageComparator sort = new LanguageComparator();

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<ContactsInfo> mData;
    private AsyncHeadPicBitmapLoader mHeadPicBitmapLoader;
    private MarrySocialDBHelper mDBHelper;

    public ContactsExpandableListAdapter(Context context) {
        super();
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mHeadPicBitmapLoader = new AsyncHeadPicBitmapLoader(mContext);
        mDBHelper = MarrySocialDBHelper.newInstance(mContext);
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
            convertView = mInflater.inflate(R.layout.contacts_list_item_layout,
                    parent, false);
            holder = new ChildViewHolder();
            holder.contacts_item_entry = (RelativeLayout) convertView
                    .findViewById(R.id.contacts_item_entry);
            holder.person_pic = (ImageView) convertView
                    .findViewById(R.id.contacts_person_pic);
            holder.person_name = (TextView) convertView
                    .findViewById(R.id.contacts_person_name);
            holder.person_description = (TextView) convertView
                    .findViewById(R.id.contacts_person_description);
            holder.person_description_more = (CheckBox) convertView
                    .findViewById(R.id.contacts_person_description_more);
            holder.new_contact_icon = (ImageView) convertView
                    .findViewById(R.id.contacts_new_icon);
            convertView.setTag(holder);
        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }

        final ContactsInfo contactsItem = assort.getHashList().getValueIndex(
                group, child);
        holder.contacts_item_entry
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        startToViewContactsInfo(contactsItem.getUid());
                    }
                });

        holder.person_name.setText(contactsItem.getNickName());
        mHeadPicBitmapLoader.loadImageBitmap(holder.person_pic,
                contactsItem.getUid());
        holder.person_description.setText(String.format(mContext.getResources()
                .getString(R.string.contacts_detail_more), contactsItem
                .getFirstDirectFriend()));
        holder.person_description_more.setChecked(false);

        final ChildViewHolder holder_temp = holder;
        final String description = (contactsItem.getFirstDirectFriend());
        final String description_more = contactsItem.getDirectFriends();

        holder.person_description_more
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (holder_temp.person_description_more.isChecked()) {
                            holder_temp.person_description.setText(String
                                    .format(mContext.getResources().getString(
                                            R.string.contacts_detail_more),
                                            description_more));
                        } else {
                            holder_temp.person_description.setText(String
                                    .format(mContext.getResources().getString(
                                            R.string.contacts_detail_more),
                                            description));
                        }

                    }
                });

        if (contactsItem.isNewContact()) {
            holder.new_contact_icon.setVisibility(View.VISIBLE);
        } else {
            holder.new_contact_icon.setVisibility(View.INVISIBLE);
        }

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
        RelativeLayout contacts_item_entry;
        ImageView person_pic;
        TextView person_name;
        TextView person_description;
        CheckBox person_description_more;
        ImageView new_contact_icon;
    }

    class GroupViewHolder {
        TextView group_name;
    }

    private void startToViewContactsInfo(String uid) {
        Intent intent = new Intent(mContext, ContactsInfoActivity.class);
        intent.putExtra(MarrySocialDBHelper.KEY_UID, uid);
        mContext.startActivity(intent);
        deleteNewContactsFlagFromContactsDB(uid);
    }

    private void deleteNewContactsFlagFromContactsDB(String uid) {

        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_IS_NEW,
                MarrySocialDBHelper.HAS_NO_MSG);

        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + uid;

        try {
            mDBHelper.update(MarrySocialDBHelper.DATABASE_CONTACTS_TABLE,
                    insertValues, whereClause, null);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }
}
