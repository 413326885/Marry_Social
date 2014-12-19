package com.dhn.marrysocial.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dhn.marrysocial.activity.EditCommentsActivity;
import com.dhn.marrysocial.adapter.DynamicInfoListAdapter;
import com.dhn.marrysocial.adapter.DynamicInfoListAdapter.onReplyBtnClickedListener;
import com.dhn.marrysocial.base.CommentsItem;
import com.dhn.marrysocial.base.ReplysItem;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.provider.DataSetProvider;
import com.dhn.marrysocial.services.DownloadCommentsIntentService;
import com.dhn.marrysocial.services.ReadContactsIntentService;
import com.dhn.marrysocial.services.UploadCommentsAndBravosAndReplysIntentService;
import com.dhn.marrysocial.utils.Utils;
import com.dhn.marrysocial.view.RefreshListView;
import com.dhn.marrysocial.R;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.view.ViewGroup;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DynamicInfoFragment extends Fragment implements OnClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = "DynamicInfoFragment";

    private final String[] COMMENTS_PROJECTION = { MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_BUCKET_ID,
            MarrySocialDBHelper.KEY_CONTENTS,
            MarrySocialDBHelper.KEY_AUTHOR_FULLNAME,
            MarrySocialDBHelper.KEY_PHOTO_COUNT,
            MarrySocialDBHelper.KEY_BRAVO_STATUS,
            MarrySocialDBHelper.KEY_ADDED_TIME,
            MarrySocialDBHelper.KEY_COMMENT_ID };

    private final String[] BRAVOS_PROJECTION = { MarrySocialDBHelper.KEY_ID,
            MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_AUTHOR_FULLNAME };

    private final String[] REPLYS_PROJECTION = { MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_AUTHOR_FULLNAME,
            MarrySocialDBHelper.KEY_REPLY_CONTENTS,
            MarrySocialDBHelper.KEY_ADDED_TIME };

     private RefreshListView mListView;
//    private ListView mListView;
    private DynamicInfoListAdapter mListViewAdapter;
    private ImageView mEditComment;

    private String mUid;
    private String mAuthorName;
    private int mReplyCommentsPosition;

    private RelativeLayout mReplyFoot;
    private ImageView mReplySendBtn;
    private EditText mReplyContents;

    private float mTouchDownY = 0.0f;
    private float mTouchMoveY = 0.0f;
    // private int mCurrentScrollState = 0;
    // private boolean mIsTouchUp = true;
    private boolean mIsFingUp = false;

    // private static int SCROLL_STATE_TOUCH_SCROLL =
    // OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;
    // private static int SCROLL_STATE_IDLE =
    // OnScrollListener.SCROLL_STATE_IDLE;
    // private static int SCROLL_STATE_FLING =
    // OnScrollListener.SCROLL_STATE_FLING;

    private final static int TOUCH_FING_UP = 100;
    private final static int TOUCH_FING_DOWN = 101;

    private final static int UPLOAD_COMMENT = 102;
    private final static int UPLOAD_BRAVO = 103;
    private final static int UPLOAD_REPLY = 104;
    private final static int START_TO_LOAD_BRAVO_REPLY = 105;
    private final static int SHOW_SOFT_INPUT_METHOD = 106;
    private final static int SELECT_SPECIFIED_LIST_ITEM = 107;
    private final static int UPDATE_DYNAMIC_INFO = 108;
    private final static int SEND_REPLY_FINISH = 109;

    private AlphaAnimation mHideEditorAlphaAnimation;
    private AlphaAnimation mShowEditorAlphaAnimation;

    private TranslateAnimation mHideEditorTransAnimation;
    private TranslateAnimation mShowEditorTransAnimation;

    // public static final Uri mCommentUri = Uri.parse("content://"
    // + DataSetProvider.AUTHORITY + "/"
    // + MarrySocialDBHelper.DATABASE_COMMENTS_TABLE);

    private MarrySocialDBHelper mDBHelper;
    private ExecutorService mExecutorService;

    private ArrayList<CommentsItem> mCommentEntrys = new ArrayList<CommentsItem>();
    private HashMap<String, String> mBravoEntrys = new HashMap<String, String>();
    private HashMap<String, ArrayList<ReplysItem>> mReplyEntrys = new HashMap<String, ArrayList<ReplysItem>>();

    private DataSetChangeObserver mChangeObserver;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case TOUCH_FING_UP: {
                hideEditBar();
                hideActionBar();
                hideReplyFootBar();
                Utils.hideSoftInputMethod(mReplyFoot);
                break;
            }

            case TOUCH_FING_DOWN: {
                showEditBar();
                showActionBar();
                hideReplyFootBar();
                Utils.hideSoftInputMethod(mReplyFoot);
                break;
            }

            case UPLOAD_COMMENT: {
                uploadCommentsOrBravosOrReplys(CommonDataStructure.KEY_COMMENTS);
                mCommentEntrys.clear();
                mCommentEntrys.addAll(loadCommentsItemFromDB());
                mListViewAdapter.notifyDataSetChanged();
                Log.e(TAG, "nannan UPLOAD_COMMENT..");
                break;
            }

            case UPLOAD_BRAVO: {
                uploadCommentsOrBravosOrReplys(CommonDataStructure.KEY_BRAVOS);
                break;
            }

            case UPLOAD_REPLY: {
                uploadCommentsOrBravosOrReplys(CommonDataStructure.KEY_REPLYS);
                break;
            }

            case START_TO_LOAD_BRAVO_REPLY: {
                if (mCommentEntrys != null && mCommentEntrys.size() != 0) {
                    for (CommentsItem comment : mCommentEntrys) {
                        mExecutorService.execute(new LoadBravoAndReplyContents(
                                comment.getCommentId()));
                    }
                }
                break;
            }

            case SHOW_SOFT_INPUT_METHOD: {
                Utils.showSoftInputMethod(mReplyContents);
                break;
            }

            case SELECT_SPECIFIED_LIST_ITEM: {
                mListView.setSelection(msg.arg1);
                break;
            }

            case UPDATE_DYNAMIC_INFO: {
                // mCommentEntrys.clear();
                // mCommentEntrys.addAll(loadCommentsItemFromDB());
                mListViewAdapter.notifyDataSetChanged();
                break;
            }

            case SEND_REPLY_FINISH: {
                mReplyContents.setText(null);
                hideReplyFootBar();
                Utils.hideSoftInputMethod(mReplyFoot);
                uploadCommentsOrBravosOrReplys(CommonDataStructure.KEY_REPLYS);
                break;
            }

            default:
                break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // animation for Editor Bar
        mHideEditorAlphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        mHideEditorAlphaAnimation.setInterpolator(new LinearInterpolator());
        mHideEditorAlphaAnimation.setDuration(1000);
        mHideEditorAlphaAnimation.setFillAfter(true);
        mShowEditorAlphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        mShowEditorAlphaAnimation.setInterpolator(new LinearInterpolator());
        mShowEditorAlphaAnimation.setDuration(1000);
        mShowEditorAlphaAnimation.setFillAfter(true);

        mShowEditorTransAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 2.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        mShowEditorTransAnimation
                .setInterpolator(new AccelerateDecelerateInterpolator());
        mShowEditorTransAnimation.setDuration(500);
        mShowEditorTransAnimation.setFillAfter(true);

        mHideEditorTransAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 2.0f);
        mHideEditorTransAnimation
                .setInterpolator(new AccelerateDecelerateInterpolator());
        mHideEditorTransAnimation.setDuration(500);
        mHideEditorTransAnimation.setFillAfter(true);

        mDBHelper = MarrySocialDBHelper.newInstance(this.getActivity());
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * CommonDataStructure.THREAD_POOL_SIZE);

        mChangeObserver = new DataSetChangeObserver(mHandler);
        this.getActivity()
                .getContentResolver()
                .registerContentObserver(CommonDataStructure.COMMENTURL, true,
                        mChangeObserver);
        this.getActivity()
                .getContentResolver()
                .registerContentObserver(CommonDataStructure.BRAVOURL, true,
                        mChangeObserver);
        this.getActivity()
                .getContentResolver()
                .registerContentObserver(CommonDataStructure.REPLYURL, true,
                        mChangeObserver);
        Log.e(TAG, "nannan oncreate()..");

        SharedPreferences prefs = getActivity().getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT,
                getActivity().MODE_PRIVATE);
        mUid = prefs.getString(CommonDataStructure.UID, "");
        mAuthorName = prefs.getString(CommonDataStructure.AUTHOR_NAME, "");

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.dynamic_info_fragment_layout,
                container, false);
         mListView = (RefreshListView)
         view.findViewById(R.id.dynamic_info_listView);
//        mListView = (ListView) view.findViewById(R.id.dynamic_info_listView);
        TextView emptyView = (TextView) view
                .findViewById(R.id.dynamic_info_list_empty);
        mListViewAdapter = new DynamicInfoListAdapter(getActivity());
        mListViewAdapter.setCommentDataSource(mCommentEntrys);
        mListViewAdapter.setBravoDataSource(mBravoEntrys);
        mListViewAdapter.setReplyDataSource(mReplyEntrys);
        mListViewAdapter.setReplyBtnClickedListener(mReplyBtnClickedListener);
        mListView.setAdapter(mListViewAdapter);
        mListView.setEmptyView(emptyView);

        mListView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTouchDownY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mTouchMoveY = event.getY() - mTouchDownY;
                    if (Math.abs(mTouchMoveY) < 50) {
                        break;
                    }
                    if (mTouchMoveY < 0) {
                        if (!mIsFingUp) {
                            mIsFingUp = true;
                            mHandler.sendEmptyMessageDelayed(TOUCH_FING_UP, 50);
                        }
                    } else {
                        if (mIsFingUp) {
                            mIsFingUp = false;
                            mHandler.sendEmptyMessageDelayed(TOUCH_FING_DOWN,
                                    50);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    break;
                }
                return false;
            }
        });

        mEditComment = (ImageView) view
                .findViewById(R.id.dynamic_info_edit_comment);
        mEditComment.setOnClickListener(this);
        mEditComment.requestFocus();

        mReplyFoot = (RelativeLayout) view
                .findViewById(R.id.dynamic_info_reply_foot);
        mReplySendBtn = (ImageView) view
                .findViewById(R.id.dynamic_info_reply_send);
        mReplyContents = (EditText) view
                .findViewById(R.id.dynamic_info_reply_contents);

        mReplySendBtn.setOnClickListener(mReplySendBtnClickedListener);
        Log.e(TAG, "nannan oncreateView()..");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // isDirty();
        mCommentEntrys.clear();
        mCommentEntrys.addAll(loadCommentsItemFromDB());
        mListViewAdapter.setCommentDataSource(mCommentEntrys);
        mListViewAdapter.notifyDataSetChanged();
        mEditComment.requestFocus();
        downloadUserComments();
        Log.e(TAG, "nannan onResume()..");
    }

    private void hideEditBar() {
        mEditComment.clearAnimation();
        mEditComment.startAnimation(mHideEditorTransAnimation);
        mEditComment.setVisibility(View.GONE);
        mEditComment.setClickable(false);
    }

    private void showEditBar() {
        mEditComment.clearAnimation();
        mEditComment.startAnimation(mShowEditorTransAnimation);
        mEditComment.setVisibility(View.VISIBLE);
        mEditComment.setClickable(true);
        mEditComment.requestFocus();
    }

    private void hideActionBar() {
        // getActivity().getActionBar().hide();
    }

    private void showActionBar() {
        // getActivity().getActionBar().show();
    }

    private void hideReplyFootBar() {
        mReplyFoot.setVisibility(View.GONE);
    }

    private void showReplyFootBar() {
        mReplyFoot.setVisibility(View.VISIBLE);
        mReplyFoot.requestFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.getActivity().getContentResolver()
                .unregisterContentObserver(mChangeObserver);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), EditCommentsActivity.class);
        startActivity(intent);
    }

    // for change observer
    private AtomicBoolean mContentDirty = new AtomicBoolean(true);

    // Returns the dirty flag and clear it.
    public boolean isDirty() {
        return mContentDirty.compareAndSet(true, false);
    }

    private class DataSetChangeObserver extends ContentObserver {

        private Handler handler;

        public DataSetChangeObserver(Handler handler) {
            super(handler);
            this.handler = handler;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // if (mContentDirty.compareAndSet(false, true)) {
            handler.sendEmptyMessage(UPLOAD_COMMENT);
//            handler.sendEmptyMessage(UPLOAD_BRAVO);
//            handler.sendEmptyMessage(UPLOAD_REPLY);
            Log.e(TAG, "nannan onChange()..");
            // }
        }
    }

    private void uploadCommentsOrBravosOrReplys(int uploadType) {
        Log.e(TAG, "nannan uploadCommentsOrBravosOrReplys()..");
        Intent serviceIntent = new Intent(getActivity(),
                UploadCommentsAndBravosAndReplysIntentService.class);
        serviceIntent.putExtra(CommonDataStructure.KEY_UPLOAD_TYPE, uploadType);
        getActivity().startService(serviceIntent);
    }

    private void downloadUserComments() {
        Log.e(TAG,
                "nannan downloadUserComments()..  1111111111111111111111111111111111111");
        Intent serviceIntent = new Intent(getActivity(),
                DownloadCommentsIntentService.class);
        getActivity().startService(serviceIntent);
    }

    private ArrayList<CommentsItem> loadCommentsItemFromDB() {
        Log.e(TAG, "nannan loadCommentsItemFromDB()..");
        ArrayList<CommentsItem> commentEntrys = new ArrayList<CommentsItem>();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_CURRENT_STATUS
                    + " != " + MarrySocialDBHelper.NEED_DELETE_FROM_CLOUD;
            String orderBy = MarrySocialDBHelper.KEY_ADDED_TIME + " DESC";
            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_COMMENTS_TABLE,
                    COMMENTS_PROJECTION, null, null, null, null, orderBy, null);
            if (cursor == null) {
                Log.e(TAG, "nannan loadCommentsItemFromDB()..  cursor == null");
                return commentEntrys;
            }
            while (cursor.moveToNext()) {
                CommentsItem comment = new CommentsItem();
                String uId = cursor.getString(0);
                String bucketId = cursor.getString(1);
                String contents = cursor.getString(2);
                String full_name = cursor.getString(3);
                int photo_count = cursor.getInt(4);
                int bravo_status = cursor.getInt(5);
                String added_time = cursor.getString(6);
                String comment_id = cursor.getString(7);
                comment.setUid(uId);
                comment.setBucketId(bucketId);
                comment.setCommentId(comment_id);
                comment.setContents(contents);
                comment.setFullName(full_name);
                comment.setPhotoCount(photo_count);
                comment.setAddTime(Utils.getAddedTimeTitle(getActivity(),
                        added_time));
                comment.setIsBravo(bravo_status == MarrySocialDBHelper.BRAVO_CONFIRM);
                commentEntrys.add(comment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        mHandler.sendEmptyMessage(START_TO_LOAD_BRAVO_REPLY);

        return commentEntrys;
    }

    class LoadBravoAndReplyContents implements Runnable {

        private String comment_id;

        public LoadBravoAndReplyContents(String comment_id) {
            this.comment_id = comment_id;
        }

        @Override
        public void run() {
            loadBravosFromDB(comment_id);
            loadReplysFromDB(comment_id);
            mHandler.sendEmptyMessage(UPDATE_DYNAMIC_INFO);
        }
    }

    private void loadBravosFromDB(String comment_id) {
        StringBuffer author_names = new StringBuffer();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                    + comment_id;
            String orderBy = MarrySocialDBHelper.KEY_ID + " ASC";
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_BRAVOS_TABLE,
                    BRAVOS_PROJECTION, whereclause, null, null, null, orderBy,
                    null);
            if (cursor == null) {
                Log.e(TAG, "nannan loadBravosFromDB()..  cursor == null");
                return;
            }
            while (cursor.moveToNext()) {
                author_names.append(cursor.getString(2)).append("  ");
            }
            if (author_names.length() != 0) {
                mBravoEntrys.put(comment_id, author_names.toString());
            } else {
                mBravoEntrys.put(comment_id, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        // mHandler.sendEmptyMessage(UPDATE_DYNAMIC_INFO);
    }

    private void loadReplysFromDB(String comment_id) {
        ArrayList<ReplysItem> replys = new ArrayList<ReplysItem>();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                    + comment_id;
            String orderBy = MarrySocialDBHelper.KEY_ADDED_TIME + " ASC";
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
                replys.add(item);
            }
            if (replys.size() != 0) {
                mReplyEntrys.put(comment_id, replys);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        // mHandler.sendEmptyMessage(UPDATE_DYNAMIC_INFO);
    }

    private onReplyBtnClickedListener mReplyBtnClickedListener = new onReplyBtnClickedListener() {

        @Override
        public void onReplyBtnClicked(int position) {
            mReplyCommentsPosition = position;
            hideEditBar();
            showReplyFootBar();
            mHandler.sendEmptyMessageDelayed(SHOW_SOFT_INPUT_METHOD, 50);
            Message msg = mHandler.obtainMessage();
            msg.what = SELECT_SPECIFIED_LIST_ITEM;
            msg.arg1 = position;
            mHandler.sendMessage(msg);
        }
    };

    private long mExitTime = 0;

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "nannan DynamicInfoFragment onKeyDown......");
        if (mReplyFoot.getVisibility() == View.VISIBLE) {
            hideReplyFootBar();
            showEditBar();
            return true;
        }
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Utils.showMesage(getActivity(), R.string.exit_confirm);
            mExitTime = System.currentTimeMillis();
        } else {
            getActivity().finish();
        }
        return true;
    }

    private View.OnClickListener mReplySendBtnClickedListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            CommentsItem comment = (CommentsItem) (mListViewAdapter
                    .getItem(mReplyCommentsPosition));
            ReplysItem reply = new ReplysItem();
            reply.setCommentId(comment.getCommentId());
            reply.setReplyContents(mReplyContents.getText().toString());
            insertReplysToReplyDB(reply);
            mHandler.sendEmptyMessage(SEND_REPLY_FINISH);
            mHandler.sendEmptyMessage(UPDATE_DYNAMIC_INFO);
        }
    };

    private void insertReplysToReplyDB(ReplysItem reply) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_COMMENT_ID,
                reply.getCommentId());
        insertValues.put(MarrySocialDBHelper.KEY_UID, mUid);
        insertValues.put(MarrySocialDBHelper.KEY_AUTHOR_FULLNAME, mAuthorName);
        insertValues.put(MarrySocialDBHelper.KEY_REPLY_CONTENTS,
                reply.getReplyContents());
        insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
                Long.toString(System.currentTimeMillis()));
        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD);

        ContentResolver resolver = getActivity().getContentResolver();
        resolver.insert(CommonDataStructure.REPLYURL, insertValues);
    }
}
