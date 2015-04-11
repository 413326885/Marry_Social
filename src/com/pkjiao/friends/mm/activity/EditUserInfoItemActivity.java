package com.pkjiao.friends.mm.activity;

import com.pkjiao.friends.mm.R;
import com.pkjiao.friends.mm.common.CommonDataStructure;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EditUserInfoItemActivity extends Activity implements
        OnClickListener {

    private static final String TAG = "EditUserInfoItemActivity";

    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String EDITINFO = "editinfo";

    private RelativeLayout mReturnBtn;
    private Button mFinishBtn;
    private TextView mTitle;
    private TextView mDescription;
    private EditText mEditInfoItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_contacts_info_item_layout);

        mTitle = (TextView) findViewById(R.id.edit_info_item_title);
        mDescription = (TextView) findViewById(R.id.edit_info_item_description);
        mEditInfoItem = (EditText) findViewById(R.id.edit_info_item_input);

        Intent data = getIntent();
        mTitle.setText(data.getStringExtra(TITLE));
        mDescription.setText(data.getStringExtra(DESCRIPTION));
        String editinfo = data.getStringExtra(EDITINFO);
        mEditInfoItem.setText(editinfo);
        mEditInfoItem.setSelection(editinfo.length());

        mReturnBtn = (RelativeLayout) findViewById(R.id.edit_info_item_return);
        mFinishBtn = (Button) findViewById(R.id.edit_info_item_finish);
        mReturnBtn.setOnClickListener(this);
        mFinishBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.edit_info_item_return: {
            String title = mEditInfoItem.getText().toString();
            if (title == null || title.length() == 0) {
                Toast.makeText(this, "信息不能为空", Toast.LENGTH_SHORT).show();
                break;
            }
            finishActivity(title);
            break;
        }
        case R.id.edit_info_item_finish: {
            String title = mEditInfoItem.getText().toString();
            if (title == null || title.length() == 0) {
                Toast.makeText(this, "信息不能为空", Toast.LENGTH_SHORT).show();
                break;
            }
            finishActivity(title);
            break;
        }
        default:
            break;
        }
    }

    private void finishActivity(String title) {

        Intent data = new Intent();
        data.putExtra(CommonDataStructure.UPDATE_USER_INFO, title);
        setResult(RESULT_OK, data);
        this.finish();
    }
}
