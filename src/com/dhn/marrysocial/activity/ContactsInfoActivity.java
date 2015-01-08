package com.dhn.marrysocial.activity;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.database.MarrySocialDBHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class ContactsInfoActivity extends Activity {

    private static final String TAG = "ContactsInfoActivity";

    private String mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.contacts_info_layout);
        
        Intent data = getIntent();
        mUid = data.getStringExtra(MarrySocialDBHelper.KEY_UID);
    }
}
