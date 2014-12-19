package com.dhn.marrysocial.fragment;

import com.dhn.marrysocial.adapter.ChatMsgListAdapter;
import com.dhn.marrysocial.utils.Utils;
import com.dhn.marrysocial.R;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class ChatMsgFragment extends Fragment {

    @SuppressWarnings("unused")
    private static final String TAG = "ChatMsgFragment";

    private ListView mListView;
    private ChatMsgListAdapter mListViewAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.chat_msg_fragment_layout,
                container, false);
        mListView = (ListView) view.findViewById(R.id.chat_msg_listView);
        TextView emptyView = (TextView) view
                .findViewById(R.id.chat_list_empty);
        mListViewAdapter = new ChatMsgListAdapter(getActivity());
        mListView.setAdapter(mListViewAdapter);
        mListView.setEmptyView(emptyView);
        return view;
    }

//    @Override
//    public void onClick(View v) {
//        MarrySocialDBHelper mDBHelper = MarrySocialDBHelper.newInstance(getActivity());
//        ContentValues values01 = new ContentValues();
//        values01.put(MarrySocialDBHelper.KEY_UID, 1);
//        values01.put(MarrySocialDBHelper.KEY_AVATAR, 0);
//        values01.put(MarrySocialDBHelper.KEY_NAME, "楠楠");
//        values01.put(MarrySocialDBHelper.KEY_FRIENDS, "立东,久辉");
//        ContentValues values02 = new ContentValues();
//        values02.put(MarrySocialDBHelper.KEY_UID, 1);
//        values02.put(MarrySocialDBHelper.KEY_AVATAR, 0);
//        values02.put(MarrySocialDBHelper.KEY_NAME, "立东");
//        values02.put(MarrySocialDBHelper.KEY_FRIENDS, "楠楠,久辉");
//        ContentValues values03 = new ContentValues();
//        values03.put(MarrySocialDBHelper.KEY_UID, 1);
//        values03.put(MarrySocialDBHelper.KEY_AVATAR, 0);
//        values03.put(MarrySocialDBHelper.KEY_NAME, "久辉");
//        values03.put(MarrySocialDBHelper.KEY_FRIENDS, "楠楠,立东");
//        mDBHelper.insert(MarrySocialDBHelper.DATABASE_TABLE, values01);
//        mDBHelper.insert(MarrySocialDBHelper.DATABASE_TABLE, values02);
//        mDBHelper.insert(MarrySocialDBHelper.DATABASE_TABLE, values03);
//    }

    private long mExitTime = 0;
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "nannan ChatMsgFragment onKeyDown......");
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Utils.showMesage(getActivity(), R.string.exit_confirm);
            mExitTime = System.currentTimeMillis();
        } else {
            getActivity().finish();
        }
        return true;
    }
}
