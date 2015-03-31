package com.pkjiao.friends.mm.activity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.pkjiao.friends.mm.R;
import com.pkjiao.friends.mm.activity.EditCommentsActivity.UploadFiles;
import com.pkjiao.friends.mm.common.CommonDataStructure;
import com.pkjiao.friends.mm.dialog.ProgressLoadDialog;
import com.pkjiao.friends.mm.utils.Utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class UserFeedbackActivity extends Activity implements OnClickListener {

    private static final String TAG = "SettingsActivity";

    private static final int UPLOAD_FINISH = 100;
    private static final int UPLOAD_FAIL = 101;
    private static final int POOL_SIZE = 10;

    private EditText mFeedbackText;
    private ImageView mFeedbackSend;
    private String mFeedbackContent;
    private ProgressLoadDialog mUploadProgressDialog;
    private Handler mHandler;
    private String mUid;
    private ExecutorService mExecutorService;
    private RelativeLayout mReturnBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.feedback_layout);
        mFeedbackText = (EditText) findViewById(R.id.feedback_input);
        mFeedbackSend = (ImageView) findViewById(R.id.feedback_send);
        mReturnBtn = (RelativeLayout) findViewById(R.id.feedback_return);
        mFeedbackText.setOnClickListener(this);
        mFeedbackSend.setOnClickListener(this);
        mReturnBtn.setOnClickListener(this);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case UPLOAD_FINISH: {
                    if (mUploadProgressDialog != null) {
                        mUploadProgressDialog.dismiss();
                    }
                    if (mFeedbackText != null) {
                        mFeedbackText.setText("");
                    }
                    Toast.makeText(UserFeedbackActivity.this, "反馈成功，谢谢你的支持。",
                            Toast.LENGTH_SHORT).show();
                    break;
                }

                case UPLOAD_FAIL: {
                    if (mUploadProgressDialog != null) {
                        mUploadProgressDialog.dismiss();
                    }
                    Toast.makeText(UserFeedbackActivity.this, "反馈失败，请稍后重试。",
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
                }
            }
        };

        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);

        SharedPreferences prefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, this.MODE_PRIVATE);
        mUid = prefs.getString(CommonDataStructure.UID, "");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.feedback_send: {
            mFeedbackContent = mFeedbackText.getText().toString();
            if (mFeedbackContent != null && mFeedbackContent.length() > 0) {
                mUploadProgressDialog = new ProgressLoadDialog(this);
                mUploadProgressDialog.setText("正在上传动态，请稍后...");
                mUploadProgressDialog.show();

                mExecutorService.execute(new UploadFiles());
            }
            break;
        }
        case R.id.feedback_return: {
            finish();
            break;
        }
        default:
            break;
        }
    }

    class UploadFiles implements Runnable {

        public UploadFiles() {
        }

        @Override
        public void run() {
            boolean result = Utils.userFeedback(
                    CommonDataStructure.URL_USER_FEEDBACK, mUid,
                    mFeedbackContent);
            if (result) {
                mHandler.sendEmptyMessage(UPLOAD_FINISH);
            } else {
                mHandler.sendEmptyMessage(UPLOAD_FAIL);
            }
        }
    }
}
