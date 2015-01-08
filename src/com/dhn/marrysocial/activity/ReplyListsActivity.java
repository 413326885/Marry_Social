package com.dhn.marrysocial.activity;

import java.util.ArrayList;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.adapter.ReplyListAdapter;
import com.dhn.marrysocial.base.ReplysItem;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.services.UploadCommentsAndBravosAndReplysIntentService;
import com.dhn.marrysocial.utils.Utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class ReplyListsActivity extends Activity implements OnClickListener {

    private static final String TAG = "ReplyListsActivity";

    private final String[] REPLYS_PROJECTION = { MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_AUTHOR_FULLNAME,
            MarrySocialDBHelper.KEY_REPLY_CONTENTS,
            MarrySocialDBHelper.KEY_ADDED_TIME };

    private final static int UPLOAD_REPLY = 100;

    private ListView mListView;
    private ReplyListAdapter mListViewAdapter;
    private RelativeLayout mReplyReturnBtn;
    private ImageView mReplySendBtn;
    private EditText mReplyContent;

    private String mUid;
    private String mAuthorName;

    private MarrySocialDBHelper mDBHelper;
    private String mCommentId;
    private ArrayList<ReplysItem> mReplyItems = new ArrayList<ReplysItem>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch(msg.what) {
            case UPLOAD_REPLY: {
                loadReplysFromDB(mCommentId);
                mListViewAdapter.notifyDataSetChanged();
                Utils.hideSoftInputMethod(mReplyContent);
                mReplyContent.setText(null);
                uploadCommentsOrBravosOrReplys(CommonDataStructure.KEY_REPLYS);
                break;
            }
            default:
                break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.reply_list_layout);

        Intent data = getIntent();
        mCommentId = data.getStringExtra(MarrySocialDBHelper.KEY_COMMENT_ID);

        mListView = (ListView) this.findViewById(R.id.reply_listview);
        mListViewAdapter = new ReplyListAdapter(this);
        mListViewAdapter.setReplyDataSource(mReplyItems);
        mListView.setAdapter(mListViewAdapter);

        mReplyReturnBtn = (RelativeLayout) this.findViewById(R.id.reply_list_return);
        mReplySendBtn = (ImageView) this.findViewById(R.id.reply_list_reply_send);
        mReplyContent = (EditText) this.findViewById(R.id.reply_list_reply_contents);
        mReplyReturnBtn.setOnClickListener(this);
        mReplySendBtn.setOnClickListener(this);

        mDBHelper = MarrySocialDBHelper.newInstance(this);
        SharedPreferences prefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT,
                this.MODE_PRIVATE);
        mUid = prefs.getString(CommonDataStructure.UID, "");
        mAuthorName = prefs.getString(CommonDataStructure.AUTHOR_NAME, "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReplysFromDB(mCommentId);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
        case R.id.reply_list_return: {
            Utils.hideSoftInputMethod(mReplyContent);
            this.finish();
            break;
        }
        case R.id.reply_list_reply_send: {
            String replyContents = mReplyContent.getText().toString();
            if (replyContents != null && replyContents.length() != 0) {
                ReplysItem reply = new ReplysItem();
                reply.setCommentId(mCommentId);
                reply.setReplyContents(replyContents);
                reply.setFullName(mAuthorName);
                reply.setUid(mUid);
                insertReplysToReplyDB(reply);
                mHandler.sendEmptyMessage(UPLOAD_REPLY);
            }
            break;
        }
        default:
            break;
        }
    }

    private void loadReplysFromDB(String comment_id) {
        mReplyItems.clear();
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                    + comment_id;
            String orderBy = MarrySocialDBHelper.KEY_ADDED_TIME + " DESC";
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_REPLYS_TABLE,
                    REPLYS_PROJECTION, whereclause, null, null, null, orderBy,
                    null);
            if (cursor == null) {
                Log.e(TAG, "nannan loadReplysFromDB()..  cursor == null");
                return;
            }
            while (cursor.moveToNext()) {
                ReplysItem item = new ReplysItem();
                item.setCommentId(comment_id);
                item.setFullName(cursor.getString(1));
                item.setReplyContents(cursor.getString(2));
                item.setUid(cursor.getString(0));
                String reply_time = cursor.getString(3);
                item.setReplyTime(Utils.getAddedTimeTitle(this,
                        reply_time));
                mReplyItems.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void insertReplysToReplyDB(ReplysItem reply) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_COMMENT_ID,
                reply.getCommentId());
        insertValues.put(MarrySocialDBHelper.KEY_UID, mUid);
        insertValues.put(MarrySocialDBHelper.KEY_AUTHOR_FULLNAME, mAuthorName);
        insertValues.put(MarrySocialDBHelper.KEY_REPLY_CONTENTS,
                reply.getReplyContents());
        insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
                Long.toString(System.currentTimeMillis() / 1000));
        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS, MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD);

        ContentResolver resolver = this.getContentResolver();
        resolver.insert(CommonDataStructure.REPLYURL, insertValues);
    }

    private void uploadCommentsOrBravosOrReplys(int uploadType) {
        Log.e(TAG, "nannan uploadCommentsOrBravosOrReplys()..");
        Intent serviceIntent = new Intent(this,
                UploadCommentsAndBravosAndReplysIntentService.class);
        serviceIntent.putExtra(CommonDataStructure.KEY_UPLOAD_TYPE, uploadType);
        this.startService(serviceIntent);
    }
}
