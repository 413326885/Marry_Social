package com.dhn.marrysocial.fragment;

import java.util.ArrayList;

import com.dhn.marrysocial.adapter.ContactsListAdapter;
import com.dhn.marrysocial.base.ContactsInfo;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.utils.Utils;
import com.dhn.marrysocial.R;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class ContactsListFragment extends Fragment {

    @SuppressWarnings("unused")
    private static final String TAG = "ContactsListFragment";

    private ListView mListView;
    private ContactsListAdapter mListViewAdapter;
    private ArrayList<ContactsInfo> mContactMembers = new ArrayList<ContactsInfo>();

    private static final String[] PROJECTION = {
        MarrySocialDBHelper.KEY_UID,
        MarrySocialDBHelper.KEY_AVATAR,
        MarrySocialDBHelper.KEY_NAME,
        MarrySocialDBHelper.KEY_FRIENDS };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MarrySocialDBHelper dbHelper = MarrySocialDBHelper
                .newInstance(getActivity());
        Cursor cursor = dbHelper.query(MarrySocialDBHelper.DATABASE_CONTACTS_TABLE,
                PROJECTION, null, null, null, null, null, null);
        if (cursor == null) {
            Log.w(TAG, "nannan query fail!");
            return;
        }
        Log.e(TAG, "nannan count = " + cursor.getCount());

        try {
            while (cursor.moveToNext()) {
                String uid = cursor.getString(0);
                String avatar = cursor.getString(1);
                String name = cursor.getString(2);
                String friends = cursor.getString(3);
                ContactsInfo info = new ContactsInfo();
                info.setUid(uid);
                info.setAvatar(avatar);
                info.setNikeName(name);
                info.setFriends(friends);
                mContactMembers.add(info);
            }
        } finally {
            cursor.close();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.contacts_list_fragment_layout,
                container, false);
        mListView = (ListView) view.findViewById(R.id.contacts_listview);
        TextView emptyView = (TextView) view
                .findViewById(R.id.contacts_list_empty);
        mListViewAdapter = new ContactsListAdapter(getActivity());
        mListViewAdapter.setDataSource(mContactMembers);
        mListView.setAdapter(mListViewAdapter);
        mListView.setEmptyView(emptyView);
        return view;
    }

    private long mExitTime = 0;
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "nannan ContactsListFragment onKeyDown......");
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Utils.showMesage(getActivity(), R.string.exit_confirm);
            mExitTime = System.currentTimeMillis();
        } else {
            getActivity().finish();
        }
        return true;
    }
}