package com.dhn.marrysocial.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.activity.EditCommentsActivity;
import com.dhn.marrysocial.activity.ReplyListsActivity;
import com.dhn.marrysocial.activity.ViewPhotoActivity;
import com.dhn.marrysocial.base.AsyncImageViewBitmapLoader;
import com.dhn.marrysocial.base.CommentsItem;
import com.dhn.marrysocial.base.ReplysItem;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.roundedimageview.RoundedImageView;
import com.dhn.marrysocial.services.DownloadCommentsIntentService;
import com.dhn.marrysocial.services.UploadCommentsAndBravosAndReplysIntentService;

public class DynamicInfoListAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = "DynamicInfoListAdapter";

    private static final String[] COMMENTS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_COMMENT_ID };

    private Context mContext;
    private LayoutInflater mInflater;
    private ScaleAnimation mBravoScaleAnimation;
    private ScaleAnimation mReplyScaleAnimation;

    private String mUid;
    private String mAuthorName;

    private MarrySocialDBHelper mDBHelper;
    private ExecutorService mExecutorService;
    private AsyncImageViewBitmapLoader mAsyncBitmapLoader;

    private ArrayList<CommentsItem> mCommentsData = new ArrayList<CommentsItem>();
    private HashMap<String, String> mBravoEntrys = new HashMap<String, String>();
    private HashMap<String, ArrayList<ReplysItem>> mReplyEntrys = new HashMap<String, ArrayList<ReplysItem>>();

    private onReplyBtnClickedListener mReplyBtnClickedListener;

    public static interface onReplyBtnClickedListener {
        public void onReplyBtnClicked(int position);
    }

    public void setReplyBtnClickedListener(onReplyBtnClickedListener listener) {
        mReplyBtnClickedListener = listener;
    }

    public DynamicInfoListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mBravoScaleAnimation = new ScaleAnimation(0.1f, 1.0f, 0.1f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        mBravoScaleAnimation.setInterpolator(new BounceInterpolator());
        // mBravoScaleAnimation.setInterpolator(new
        // AnticipateOvershootInterpolator());
        // mBravoScaleAnimation.setInterpolator(new
        // AccelerateDecelerateInterpolator());
        mBravoScaleAnimation.setDuration(100);
        mBravoScaleAnimation.setFillAfter(true);
        mBravoScaleAnimation.setFillBefore(true);

        mReplyScaleAnimation = new ScaleAnimation(0.1f, 1.0f, 0.1f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        mReplyScaleAnimation.setInterpolator(new BounceInterpolator());
        mReplyScaleAnimation.setDuration(100);
        mReplyScaleAnimation.setFillAfter(true);
        mReplyScaleAnimation.setFillBefore(true);

        SharedPreferences prefs = mContext.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT,
                mContext.MODE_PRIVATE);
        mUid = prefs.getString(CommonDataStructure.UID, "");
        mAuthorName = prefs.getString(CommonDataStructure.AUTHOR_NAME, "");

        mAsyncBitmapLoader = new AsyncImageViewBitmapLoader(mContext);
        mDBHelper = MarrySocialDBHelper.newInstance(mContext);
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * CommonDataStructure.THREAD_POOL_SIZE);
    }

    public void setCommentDataSource(ArrayList<CommentsItem> source) {
        mCommentsData = source;
    }

    public void setBravoDataSource(HashMap<String, String> source) {
        mBravoEntrys = source;
    }

    public void setReplyDataSource(HashMap<String, ArrayList<ReplysItem>> source) {
        mReplyEntrys = source;
    }

    @Override
    public int getCount() {
        return mCommentsData.size();
    }

    @Override
    public Object getItem(int position) {
        return mCommentsData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.dynamic_info_item_layout,
                    parent, false);
            holder = initViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        setViewHolderLoyout(holder, position);
        return convertView;
    }

    private void setViewHolderLoyout(final ViewHolder holder, final int position) {

        // final ViewHolder holderTemp = holder;
        if (mAuthorName != null
                && mAuthorName.equalsIgnoreCase(mCommentsData.get(position)
                        .getFulName())) {
            holder.mHeadPic.setImageResource(R.drawable.head);
        } else {
            holder.mHeadPic
                    .setImageResource(R.drawable.person_default_small_pic);
        }

        holder.mFullName.setText(mCommentsData.get(position).getFulName());
        holder.mDynamicBravo.setChecked(mCommentsData.get(position).isBravo());
        holder.mDynamicBravo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                holder.mDynamicBravo.startAnimation(mBravoScaleAnimation);
                mExecutorService.execute(new UpdateBravoStatus(mCommentsData
                        .get(position), holder.mDynamicBravo.isChecked()));
            }
        });
        holder.mReplyBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                holder.mReplyBtn.startAnimation(mReplyScaleAnimation);
                mReplyBtnClickedListener.onReplyBtnClicked(position);
            }
        });
        holder.mReplyTipsTitle.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                showAllReplys(position);
            }
        });
        holder.mAddedTime.setText(mCommentsData.get(position).getAddTime());
        holder.mContents.setText(mCommentsData.get(position).getContents());

        setCommentPicsVisibleIfNeed(holder, position);
        setCommentPicsOnClickListener(holder, position);

        setBravosVisibleIfNeed(holder, position);
        setReplysVisibleIfNeed(holder, position);

        holder.mDynamicBravo.clearAnimation();
        holder.mReplyBtn.clearAnimation();
    }

    class ViewHolder {
        RoundedImageView mHeadPic;
        CheckBox mDynamicBravo;
        ImageView mReplyBtn;
        TextView mFullName;
        TextView mAddedTime;
        TextView mContents;
        TextView mBravosAuthorNames;
        ImageView mCommentPics01;
        ImageView mCommentPics02;
        ImageView mCommentPics03;
        ImageView mCommentPics04;
        ImageView mCommentPics05;
        ImageView mCommentPics06;
        ImageView mCommentPics07;
        ImageView mCommentPics08;
        ImageView mCommentPics09;
        LinearLayout mReply01;
        TextView mReplyAuthor01;
        TextView mReplyContent01;
        LinearLayout mReply02;
        TextView mReplyAuthor02;
        TextView mReplyContent02;
        LinearLayout mReply03;
        TextView mReplyAuthor03;
        TextView mReplyContent03;
        LinearLayout mReply04;
        TextView mReplyAuthor04;
        TextView mReplyContent04;
        LinearLayout mReply05;
        TextView mReplyAuthor05;
        TextView mReplyContent05;
        LinearLayout mBravoTips;
        TextView mBravoTipsTitle;
        LinearLayout mReplyTips;
        TextView mReplyTipsTitle;
    }

    private ViewHolder initViewHolder(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.mHeadPic = (RoundedImageView) convertView
                .findViewById(R.id.dynamic_info_person_pic);
        holder.mFullName = (TextView) convertView
                .findViewById(R.id.dynamic_info_name);
        holder.mDynamicBravo = (CheckBox) convertView
                .findViewById(R.id.dynamic_info_bravo);
        holder.mReplyBtn = (ImageView) convertView
                .findViewById(R.id.dynamic_info_pinglun);
        holder.mAddedTime = (TextView) convertView
                .findViewById(R.id.dynamic_info_time);
        holder.mContents = (TextView) convertView
                .findViewById(R.id.dynamic_info_contents);
        holder.mBravosAuthorNames = (TextView) convertView
                .findViewById(R.id.dynamic_info_bravo_authors);
        holder.mCommentPics01 = (ImageView) convertView
                .findViewById(R.id.comments_pics_01);
        holder.mCommentPics02 = (ImageView) convertView
                .findViewById(R.id.comments_pics_02);
        holder.mCommentPics03 = (ImageView) convertView
                .findViewById(R.id.comments_pics_03);
        holder.mCommentPics04 = (ImageView) convertView
                .findViewById(R.id.comments_pics_04);
        holder.mCommentPics05 = (ImageView) convertView
                .findViewById(R.id.comments_pics_05);
        holder.mCommentPics06 = (ImageView) convertView
                .findViewById(R.id.comments_pics_06);
        holder.mCommentPics07 = (ImageView) convertView
                .findViewById(R.id.comments_pics_07);
        holder.mCommentPics08 = (ImageView) convertView
                .findViewById(R.id.comments_pics_08);
        holder.mCommentPics09 = (ImageView) convertView
                .findViewById(R.id.comments_pics_09);
        holder.mReply01 = (LinearLayout) convertView
                .findViewById(R.id.dynamic_info_reply_01);
        holder.mReplyAuthor01 = (TextView) convertView
                .findViewById(R.id.dynamic_info_reply_author_01);
        holder.mReplyContent01 = (TextView) convertView
                .findViewById(R.id.dynamic_info_reply_content_01);
        holder.mReply02 = (LinearLayout) convertView
                .findViewById(R.id.dynamic_info_reply_02);
        holder.mReplyAuthor02 = (TextView) convertView
                .findViewById(R.id.dynamic_info_reply_author_02);
        holder.mReplyContent02 = (TextView) convertView
                .findViewById(R.id.dynamic_info_reply_content_02);
        holder.mReply03 = (LinearLayout) convertView
                .findViewById(R.id.dynamic_info_reply_03);
        holder.mReplyAuthor03 = (TextView) convertView
                .findViewById(R.id.dynamic_info_reply_author_03);
        holder.mReplyContent03 = (TextView) convertView
                .findViewById(R.id.dynamic_info_reply_content_03);
        holder.mReply04 = (LinearLayout) convertView
                .findViewById(R.id.dynamic_info_reply_04);
        holder.mReplyAuthor04 = (TextView) convertView
                .findViewById(R.id.dynamic_info_reply_author_04);
        holder.mReplyContent04 = (TextView) convertView
                .findViewById(R.id.dynamic_info_reply_content_04);
        holder.mReply05 = (LinearLayout) convertView
                .findViewById(R.id.dynamic_info_reply_05);
        holder.mReplyAuthor05 = (TextView) convertView
                .findViewById(R.id.dynamic_info_reply_author_05);
        holder.mReplyContent05 = (TextView) convertView
                .findViewById(R.id.dynamic_info_reply_content_05);
        holder.mBravoTips = (LinearLayout) convertView
                .findViewById(R.id.dynamic_info_bravo_tips);
        holder.mBravoTipsTitle = (TextView) convertView
                .findViewById(R.id.dynamic_info_bravo_tips_title);
        holder.mReplyTips = (LinearLayout) convertView
                .findViewById(R.id.dynamic_info_reply_tips);
        holder.mReplyTipsTitle = (TextView) convertView
                .findViewById(R.id.dynamic_info_reply_tips_title);
        return holder;
    }

    private void setCommentPicsVisibleIfNeed(ViewHolder holder, int position) {

        holder.mCommentPics01
                .setBackgroundResource(R.color.gray_background_color);
        holder.mCommentPics01.setImageBitmap(null);
        holder.mCommentPics01.setVisibility(View.GONE);

        holder.mCommentPics02
                .setBackgroundResource(R.color.gray_background_color);
        holder.mCommentPics02.setImageBitmap(null);
        holder.mCommentPics02.setVisibility(View.GONE);

        holder.mCommentPics03
                .setBackgroundResource(R.color.gray_background_color);
        holder.mCommentPics03.setImageBitmap(null);
        holder.mCommentPics03.setVisibility(View.GONE);

        holder.mCommentPics04
                .setBackgroundResource(R.color.gray_background_color);
        holder.mCommentPics04.setImageBitmap(null);
        holder.mCommentPics04.setVisibility(View.GONE);

        holder.mCommentPics05
                .setBackgroundResource(R.color.gray_background_color);
        holder.mCommentPics05.setImageBitmap(null);
        holder.mCommentPics05.setVisibility(View.GONE);

        holder.mCommentPics06
                .setBackgroundResource(R.color.gray_background_color);
        holder.mCommentPics06.setImageBitmap(null);
        holder.mCommentPics06.setVisibility(View.GONE);

        holder.mCommentPics07
                .setBackgroundResource(R.color.gray_background_color);
        holder.mCommentPics07.setImageBitmap(null);
        holder.mCommentPics07.setVisibility(View.GONE);

        holder.mCommentPics08
                .setBackgroundResource(R.color.gray_background_color);
        holder.mCommentPics08.setImageBitmap(null);
        holder.mCommentPics08.setVisibility(View.GONE);

        holder.mCommentPics09
                .setBackgroundResource(R.color.gray_background_color);
        holder.mCommentPics09.setImageBitmap(null);
        holder.mCommentPics09.setVisibility(View.GONE);

        ArrayList<ImageView> commentPics = new ArrayList<ImageView>();
        commentPics.add(holder.mCommentPics01);
        commentPics.add(holder.mCommentPics02);
        commentPics.add(holder.mCommentPics03);
        commentPics.add(holder.mCommentPics04);
        commentPics.add(holder.mCommentPics05);
        commentPics.add(holder.mCommentPics06);
        commentPics.add(holder.mCommentPics07);
        commentPics.add(holder.mCommentPics08);
        commentPics.add(holder.mCommentPics09);

        String uId = mCommentsData.get(position).getUid();
        String bucketId = mCommentsData.get(position).getBucketId();
        String commentId = mCommentsData.get(position).getCommentId();
        int count = mCommentsData.get(position).getPhotoCount();
        for (int index = 0; index < count; index++) {
            String url = uId + "_" + bucketId + "_" + commentId + "_" + (index + 1);
            commentPics.get(index).setVisibility(View.VISIBLE);
            mAsyncBitmapLoader.loadImageBitmap(commentPics.get(index), url);
        }

    }

    private void setCommentPicsOnClickListener(ViewHolder holder,
            final int position) {
        holder.mCommentPics01.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startToViewPhoto(0, position);
            }
        });
        holder.mCommentPics02.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startToViewPhoto(1, position);
            }
        });
        holder.mCommentPics03.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startToViewPhoto(2, position);
            }
        });
        holder.mCommentPics04.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startToViewPhoto(3, position);
            }
        });
        holder.mCommentPics05.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startToViewPhoto(4, position);
            }
        });
        holder.mCommentPics06.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startToViewPhoto(5, position);
            }
        });
        holder.mCommentPics07.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startToViewPhoto(6, position);
            }
        });
        holder.mCommentPics08.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startToViewPhoto(7, position);
            }
        });
        holder.mCommentPics09.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startToViewPhoto(8, position);
            }
        });
    }

    private void startToViewPhoto(int photoIndex, int position) {
        Intent intent = new Intent(mContext, ViewPhotoActivity.class);

        intent.putExtra(MarrySocialDBHelper.KEY_UID, mCommentsData
                .get(position).getUid());
        intent.putExtra(MarrySocialDBHelper.KEY_BUCKET_ID,
                mCommentsData.get(position).getBucketId());
        intent.putExtra(MarrySocialDBHelper.KEY_PHOTO_POS, photoIndex);

        mContext.startActivity(intent);
    }

    private void setBravosVisibleIfNeed(ViewHolder holder, int position) {
        holder.mBravoTips.setVisibility(View.GONE);
        holder.mBravosAuthorNames.setVisibility(View.GONE);
        String bravoAuthorsName = mBravoEntrys.get(mCommentsData.get(
                position).getCommentId());
        if (bravoAuthorsName != null && bravoAuthorsName.length() != 0) {
            holder.mBravosAuthorNames.setVisibility(View.VISIBLE);
            holder.mBravosAuthorNames.setText(bravoAuthorsName);
            String[] names = bravoAuthorsName.trim().split(" ");
            if (names != null && names.length >= 0) {
                holder.mBravoTips.setVisibility(View.VISIBLE);
                String title = String.format(
                        mContext.getString(R.string.dynamic_bravo_tips_title),
                        names.length);
                holder.mBravoTipsTitle.setText(title);
            }
        }
    }

    private void setReplysVisibleIfNeed(ViewHolder holder, int position) {

        ArrayList<LinearLayout> replysFather = new ArrayList<LinearLayout>();
        ArrayList<TextView> authors = new ArrayList<TextView>();
        ArrayList<TextView> contents = new ArrayList<TextView>();
        replysFather.add(holder.mReply01);
        replysFather.add(holder.mReply02);
        replysFather.add(holder.mReply03);
        replysFather.add(holder.mReply04);
        replysFather.add(holder.mReply05);
        authors.add(holder.mReplyAuthor01);
        authors.add(holder.mReplyAuthor02);
        authors.add(holder.mReplyAuthor03);
        authors.add(holder.mReplyAuthor04);
        authors.add(holder.mReplyAuthor05);
        contents.add(holder.mReplyContent01);
        contents.add(holder.mReplyContent02);
        contents.add(holder.mReplyContent03);
        contents.add(holder.mReplyContent04);
        contents.add(holder.mReplyContent05);

        for (int index = 0; index < 5; index++) {
            replysFather.get(index).setVisibility(View.GONE);
            authors.get(index).setText(null);
            contents.get(index).setText(null);
        }

        holder.mReplyTips.setVisibility(View.GONE);
        ArrayList<ReplysItem> replys = mReplyEntrys.get(mCommentsData.get(
                position).getCommentId());
        if (replys == null || replys.size() <= 0) {
            return;
        }
        int replysCount = replys.size();
        if (replysCount > 0) {
            holder.mReplyTips.setVisibility(View.VISIBLE);
            String title = String.format(
                    mContext.getString(R.string.dynamic_reply_tips_title),
                    replysCount);
            holder.mReplyTipsTitle.setText(title);
        }
        if (replysCount <= 5) {
            for (int index = 0; index < replysCount; index++) {
                replysFather.get(index).setVisibility(View.VISIBLE);
                authors.get(index).setText(replys.get(index).getFullName());
                contents.get(index).setText(replys.get(index).getReplyContents());
            }
        } else {
            ArrayList<ReplysItem> top5Replys = new ArrayList<ReplysItem> ();
            for (int index = 5; index > 0; index--) {
                top5Replys.add(replys.get(replysCount - index));
            }
            for (int index = 0; index < top5Replys.size(); index++) {
                replysFather.get(index).setVisibility(View.VISIBLE);
                authors.get(index).setText(top5Replys.get(index).getFullName());
                contents.get(index).setText(top5Replys.get(index).getReplyContents());
            }
        }

    }

    class UpdateBravoStatus implements Runnable {

        private CommentsItem comment;
        private boolean isChecked;

        public UpdateBravoStatus(CommentsItem comment, boolean isChecked) {
            this.comment = comment;
            this.isChecked = isChecked;
        }

        @Override
        public void run() {
            insertBravoStatusToBravosDB(comment, isChecked);
            updateBravoStatusOfCommentsDB(comment, isChecked);
            uploadCommentsOrBravosOrReplys(CommonDataStructure.KEY_BRAVOS);
        }
    }

    private void updateBravoStatusOfCommentsDB(CommentsItem comment,
            boolean isChecked) {
        String whereClause = MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                + comment.getCommentId();
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_BRAVO_STATUS,
                isChecked ? MarrySocialDBHelper.BRAVO_CONFIRM
                        : MarrySocialDBHelper.BRAVO_CANCEL);
        mDBHelper.update(MarrySocialDBHelper.DATABASE_COMMENTS_TABLE, values,
                whereClause, null);
    }

    private void insertBravoStatusToBravosDB(CommentsItem comment,
            boolean isChecked) {
        if (isChecked && !isCommentIdExist(comment.getCommentId())) {
            ContentValues insertValues = new ContentValues();
            insertValues.put(MarrySocialDBHelper.KEY_COMMENT_ID,
                    comment.getCommentId());
            insertValues.put(MarrySocialDBHelper.KEY_UID, mUid);
            insertValues.put(MarrySocialDBHelper.KEY_AUTHOR_FULLNAME,
                    mAuthorName);
            insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
                    Long.toString(System.currentTimeMillis()));
            insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS, MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD);

            ContentResolver resolver = mContext.getContentResolver();
            resolver.insert(CommonDataStructure.BRAVOURL, insertValues);
        } else {
            String whereClause = MarrySocialDBHelper.KEY_COMMENT_ID + " = " + comment.getCommentId()
                    + " AND " + MarrySocialDBHelper.KEY_UID + " = " + mUid;
            ContentResolver resolver = mContext.getContentResolver();
            resolver.delete(CommonDataStructure.BRAVOURL, whereClause, null);
        }

    }

    public boolean isCommentIdExist(String commentId) {
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_COMMENT_ID + " = "
                    + commentId;
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_BRAVOS_TABLE,
                    COMMENTS_PROJECTION, whereclause, null, null, null, null,
                    null);
            if (cursor == null || cursor.getCount() == 0) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return true;
    }

    private void showAllReplys(int position) {
        String commentId = mCommentsData.get(position).getCommentId();
        Intent intent = new Intent(mContext, ReplyListsActivity.class);
        intent.putExtra(MarrySocialDBHelper.KEY_COMMENT_ID, commentId);
        mContext.startActivity(intent);
    }

    private void uploadCommentsOrBravosOrReplys(int uploadType) {
        Log.e(TAG, "nannan uploadCommentsOrBravosOrReplys()..");
        Intent serviceIntent = new Intent(mContext,
                UploadCommentsAndBravosAndReplysIntentService.class);
        serviceIntent.putExtra(CommonDataStructure.KEY_UPLOAD_TYPE, uploadType);
        mContext.startService(serviceIntent);
    }
}
