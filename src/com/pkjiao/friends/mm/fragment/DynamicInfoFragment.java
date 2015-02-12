package com.pkjiao.friends.mm.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dhn.marrysocial.R;
import com.pkjiao.friends.mm.activity.EditCommentsActivity;
import com.pkjiao.friends.mm.adapter.DynamicInfoListAdapter;
import com.pkjiao.friends.mm.adapter.DynamicInfoListAdapter.onReplyBtnClickedListener;
import com.pkjiao.friends.mm.base.CommentsItem;
import com.pkjiao.friends.mm.base.ContactsInfo;
import com.pkjiao.friends.mm.base.ReplysItem;
import com.pkjiao.friends.mm.common.CommonDataStructure;
import com.pkjiao.friends.mm.database.MarrySocialDBHelper;
import com.pkjiao.friends.mm.services.DownloadCommentsIntentService;
import com.pkjiao.friends.mm.services.UploadCommentsAndBravosAndReplysIntentService;
import com.pkjiao.friends.mm.utils.Utils;
import com.pkjiao.friends.mm.view.RefreshListView;
import com.pkjiao.friends.mm.view.RefreshListView.PullDownRefreshListener;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.view.animation.TranslateAnimation;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DynamicInfoFragment extends Fragment implements OnClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    @SuppressWarnings("unused")
    private static final String TAG = "DynamicInfoFragment";

    private final String[] COMMENTS_PROJECTION = { MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_BUCKET_ID,
            MarrySocialDBHelper.KEY_CONTENTS,
            MarrySocialDBHelper.KEY_AUTHOR_NICKNAME,
            MarrySocialDBHelper.KEY_PHOTO_COUNT,
            MarrySocialDBHelper.KEY_BRAVO_STATUS,
            MarrySocialDBHelper.KEY_ADDED_TIME,
            MarrySocialDBHelper.KEY_COMMENT_ID,
            MarrySocialDBHelper.KEY_CURRENT_STATUS };

    private final String[] BRAVOS_PROJECTION = { MarrySocialDBHelper.KEY_ID,
            MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_AUTHOR_NICKNAME };

    private final String[] REPLYS_PROJECTION = { MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_AUTHOR_NICKNAME,
            MarrySocialDBHelper.KEY_REPLY_CONTENTS,
            MarrySocialDBHelper.KEY_ADDED_TIME };

    private static final String[] CONTACTS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_PHONE_NUM,
            MarrySocialDBHelper.KEY_NICKNAME, MarrySocialDBHelper.KEY_REALNAME,
            MarrySocialDBHelper.KEY_FIRST_DIRECT_FRIEND,
            MarrySocialDBHelper.KEY_DIRECT_FRIENDS,
            MarrySocialDBHelper.KEY_INDIRECT_ID,
            MarrySocialDBHelper.KEY_DIRECT_FRIENDS_COUNT,
            MarrySocialDBHelper.KEY_HEADPIC, MarrySocialDBHelper.KEY_GENDER,
            MarrySocialDBHelper.KEY_ASTRO, MarrySocialDBHelper.KEY_HOBBY };

    // private RefreshListView mListView;
    private SwipeRefreshLayout mDynamicInfoSwipeRefresh;
    private ListView mListView;
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
    private boolean mIsFingUp = false;

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
    private final static int REFRESH_HEADER_PIC = 110;
    private final static int NETWORK_INVALID = 111;

    private final static int REFRESH_COMPLETE = 112;

    private AlphaAnimation mHideEditorAlphaAnimation;
    private AlphaAnimation mShowEditorAlphaAnimation;

    private TranslateAnimation mHideEditorTransAnimation;
    private TranslateAnimation mShowEditorTransAnimation;

    private MarrySocialDBHelper mDBHelper;
    private ExecutorService mExecutorService;

    private ArrayList<CommentsItem> mCommentEntrys = new ArrayList<CommentsItem>();
    private HashMap<String, String> mBravoEntrys = new HashMap<String, String>();
    private HashMap<String, ArrayList<ReplysItem>> mReplyEntrys = new HashMap<String, ArrayList<ReplysItem>>();
    private HashMap<String, ContactsInfo> mUserInfoEntrys = new HashMap<String, ContactsInfo>();

    private DataSetChangeObserver mChangeObserver;
    private DataSetChangeObserver mHeaderPicChangeObserver;
    private SharedPreferences mPrefs;

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
                // uploadCommentsOrBravosOrReplys(CommonDataStructure.KEY_COMMENTS);
                mCommentEntrys.clear();
                mCommentEntrys.addAll(loadCommentsItemFromDB());
                mListViewAdapter.notifyDataSetChanged();
                Log.e(TAG, "nannan UPLOAD_COMMENT..");
                break;
            }

            case REFRESH_HEADER_PIC: {
                mListViewAdapter.clearHeadPicsCache();
                mListViewAdapter.notifyDataSetChanged();
                break;
            }

            // case UPLOAD_BRAVO: {
            // uploadCommentsOrBravosOrReplys(CommonDataStructure.KEY_BRAVOS);
            // break;
            // }
            //
            // case UPLOAD_REPLY: {
            // uploadCommentsOrBravosOrReplys(CommonDataStructure.KEY_REPLYS);
            // break;
            // }

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
                showEditBar();
                hideReplyFootBar();
                Utils.hideSoftInputMethod(mReplyFoot);
                break;
            }

            case NETWORK_INVALID: {
                Toast.makeText(getActivity(), R.string.network_not_available,
                        Toast.LENGTH_SHORT).show();
                break;
            }

            case REFRESH_COMPLETE: {
                mDynamicInfoSwipeRefresh.setRefreshing(false);
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

        mChangeObserver = new DataSetChangeObserver(mHandler, UPLOAD_COMMENT);
        mHeaderPicChangeObserver = new DataSetChangeObserver(mHandler,
                REFRESH_HEADER_PIC);
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
        this.getActivity()
                .getContentResolver()
                .registerContentObserver(CommonDataStructure.HEADPICSURL, true,
                        mHeaderPicChangeObserver);
        Log.e(TAG, "nannan oncreate()..");

        mPrefs = getActivity().getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT,
                getActivity().MODE_PRIVATE);
        mUid = mPrefs.getString(CommonDataStructure.UID, "");
        mAuthorName = mPrefs.getString(CommonDataStructure.AUTHOR_NAME, "");
        loadContactsFromDB();

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.dynamic_info_fragment_layout,
                container, false);

        mDynamicInfoSwipeRefresh = (SwipeRefreshLayout) view
                .findViewById(R.id.dynamic_info_swipe_refresh);
        mDynamicInfoSwipeRefresh.setOnRefreshListener(this);
        mDynamicInfoSwipeRefresh.setColorScheme(R.color.swipe_refresh_color_01,
                R.color.swipe_refresh_color_02, R.color.swipe_refresh_color_03,
                R.color.swipe_refresh_color_04);

        mListView = (ListView) view.findViewById(R.id.dynamic_info_listView);
        // mListView = (ListView) view.findViewById(R.id.dynamic_info_listView);
        TextView emptyView = (TextView) view
                .findViewById(R.id.dynamic_info_list_empty);
        mListViewAdapter = new DynamicInfoListAdapter(getActivity());
        mListViewAdapter.setCommentDataSource(mCommentEntrys);
        mListViewAdapter.setBravoDataSource(mBravoEntrys);
        mListViewAdapter.setReplyDataSource(mReplyEntrys);
        mListViewAdapter.setUserInfoDataSource(mUserInfoEntrys);
        mListViewAdapter.setReplyBtnClickedListener(mReplyBtnClickedListener);
        mListViewAdapter.setEnterInContactsInfoActivity(false);
        mListView.setAdapter(mListViewAdapter);
        mListView.setEmptyView(emptyView);
        // mListView.setOnPullDownRefreshListener(mPullDownRefreshListener);

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
        startToDownloadUserComments();
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
        this.getActivity().getContentResolver()
                .unregisterContentObserver(mHeaderPicChangeObserver);
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
        private int status;

        public DataSetChangeObserver(Handler handler, int status) {
            super(handler);
            this.handler = handler;
            this.status = status;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // if (mContentDirty.compareAndSet(false, true)) {
            switch (status) {
            case REFRESH_HEADER_PIC: {
                handler.sendEmptyMessage(REFRESH_HEADER_PIC);
                break;
            }
            case UPLOAD_COMMENT: {
                handler.sendEmptyMessage(UPLOAD_COMMENT);
                break;
            }
            default:
                break;
            }
            // }
        }
    }

    private void uploadReplysToCloud(int uploadType, String comment_id,
            String bucket_id) {
        Intent serviceIntent = new Intent(getActivity(),
                UploadCommentsAndBravosAndReplysIntentService.class);
        serviceIntent.putExtra(CommonDataStructure.KEY_UPLOAD_TYPE, uploadType);
        serviceIntent.putExtra(MarrySocialDBHelper.KEY_COMMENT_ID, comment_id);
        serviceIntent.putExtra(MarrySocialDBHelper.KEY_BUCKET_ID, bucket_id);
        getActivity().startService(serviceIntent);
    }

    private void startToDownloadUserComments() {
        Intent serviceIntent = new Intent(getActivity(),
                DownloadCommentsIntentService.class);
        serviceIntent.putExtra(CommonDataStructure.COMMENT_ID, "-1");
        getActivity().startService(serviceIntent);
    }

    private ArrayList<CommentsItem> loadCommentsItemFromDB() {
        ArrayList<CommentsItem> commentEntrys = new ArrayList<CommentsItem>();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_CURRENT_STATUS
                    + " != " + MarrySocialDBHelper.NEED_DELETE_FROM_CLOUD;
            String orderBy = MarrySocialDBHelper.KEY_ADDED_TIME + " DESC";
            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_COMMENTS_TABLE,
                    COMMENTS_PROJECTION, whereclause, null, null, null,
                    orderBy, null);
            if (cursor == null) {
                Log.e(TAG, "nannan loadCommentsItemFromDB()..  cursor == null");
                return commentEntrys;
            }
            while (cursor.moveToNext()) {
                CommentsItem comment = new CommentsItem();
                String uId = cursor.getString(0);
                String bucketId = cursor.getString(1);
                String contents = cursor.getString(2);
                String nick_name = cursor.getString(3);
                int photo_count = cursor.getInt(4);
                int bravo_status = cursor.getInt(5);
                String added_time = cursor.getString(6);
                String comment_id = cursor.getString(7);
                int current_status = cursor.getInt(8);
                comment.setUid(uId);
                comment.setBucketId(bucketId);
                comment.setCommentId(comment_id);
                comment.setContents(contents);
                comment.setRealName(nick_name);
                comment.setNickName(nick_name);
                comment.setPhotoCount(photo_count);
                comment.setCurrrentStatus(current_status);
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
            loadContactsFromDB();
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
                    + comment_id + " AND "
                    + MarrySocialDBHelper.KEY_CURRENT_STATUS + " != "
                    + MarrySocialDBHelper.NEED_DELETE_FROM_CLOUD;
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
                item.setNickname(cursor.getString(1));
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
            if (!Utils.isActiveNetWorkAvailable(getActivity())) {
                mHandler.sendEmptyMessage(NETWORK_INVALID);
                return;
            }
            CommentsItem comment = (CommentsItem) (mListViewAdapter
                    .getItem(mReplyCommentsPosition));
            long time = System.currentTimeMillis() / 1000;
            String replyTime = Long.toString(time);
            String bucketId = String.valueOf(replyTime.hashCode());
            ReplysItem reply = new ReplysItem();
            reply.setCommentId(comment.getCommentId());
            reply.setReplyContents(mReplyContents.getText().toString());
            reply.setReplyTime(replyTime);
            reply.setBucketId(bucketId);
            insertReplysToReplyDB(reply);
            uploadReplysToCloud(CommonDataStructure.KEY_REPLYS,
                    comment.getCommentId(), bucketId);
            mHandler.sendEmptyMessage(SEND_REPLY_FINISH);
            mHandler.sendEmptyMessage(UPDATE_DYNAMIC_INFO);
        }
    };

    private void insertReplysToReplyDB(ReplysItem reply) {

        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_COMMENT_ID,
                reply.getCommentId());
        insertValues.put(MarrySocialDBHelper.KEY_UID, mUid);
        insertValues
                .put(MarrySocialDBHelper.KEY_BUCKET_ID, reply.getBucketId());
        insertValues.put(MarrySocialDBHelper.KEY_AUTHOR_NICKNAME, mAuthorName);
        insertValues.put(MarrySocialDBHelper.KEY_REPLY_CONTENTS,
                reply.getReplyContents());
        insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
                reply.getReplyTime());
        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD);

        try {
            ContentResolver resolver = getActivity().getContentResolver();
            resolver.insert(CommonDataStructure.REPLYURL, insertValues);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

    PullDownRefreshListener mPullDownRefreshListener = new PullDownRefreshListener() {

        @Override
        public Object pullToRefresh() {
            startToDownloadUserComments();
            return null;
        }

        @Override
        public void refreshDone(Object obj) {
        }

        @Override
        public void loadMore() {
        }

    };

    private void loadContactsFromDB() {

        MarrySocialDBHelper dbHelper = MarrySocialDBHelper
                .newInstance(getActivity());
        Cursor cursor = dbHelper.query(
                MarrySocialDBHelper.DATABASE_CONTACTS_TABLE,
                CONTACTS_PROJECTION, null, null, null, null, null, null);
        if (cursor == null) {
            Log.w(TAG, "nannan query fail!");
            return;
        }

        try {
            while (cursor.moveToNext()) {
                String uid = cursor.getString(0);
                String phoneNum = cursor.getString(1);
                String nickname = cursor.getString(2);
                String realname = cursor.getString(3);
                String firstDirectFriend = cursor.getString(4);
                String directFriends = cursor.getString(5);
                String indirectId = cursor.getString(6);
                int directFriendsCount = cursor.getInt(7);
                int avatar = Integer.valueOf(cursor.getInt(8));
                int gender = Integer.valueOf(cursor.getInt(9));
                int astro = Integer.valueOf(cursor.getInt(10));
                int hobby = Integer.valueOf(cursor.getInt(11));

                ContactsInfo contactItem = new ContactsInfo();
                contactItem.setUid(uid);
                contactItem.setPhoneNum(phoneNum);
                contactItem.setNickName(nickname);
                contactItem.setRealName(realname);
                contactItem.setHeadPic(avatar);
                contactItem.setGender(gender);
                contactItem.setAstro(astro);
                contactItem.setHobby(hobby);
                contactItem.setIndirectId(indirectId);
                contactItem.setFirstDirectFriend(firstDirectFriend);
                contactItem.setDirectFriends(directFriends);
                contactItem.setDirectFriendsCount(directFriendsCount);

                mUserInfoEntrys.put(uid, contactItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return;
    }

    @Override
    public void onRefresh() {
        startToDownloadUserComments();
        mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 1200);
    }
}
