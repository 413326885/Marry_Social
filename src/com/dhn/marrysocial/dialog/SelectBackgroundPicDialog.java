package com.dhn.marrysocial.dialog;

import com.dhn.marrysocial.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class SelectBackgroundPicDialog extends Dialog implements
        android.view.View.OnClickListener {

    private Context mContext;

    private TextView mSelectBackgroundPicBtn;
    private OnSelectPicBtnClickListener mSelectPicBtnClickistener;

    public SelectBackgroundPicDialog(Context context) {
        this(context, R.style.TranslucentDialog);
    }

    private SelectBackgroundPicDialog(Context context, int style) {
        super(context, style);
        this.mContext = context;
        initView();
    }

    private void initView() {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.select_background_pic_dialog, null);
        setContentView(view);

        mSelectBackgroundPicBtn = (TextView) view.findViewById(R.id.select_background_pic_desc);
        mSelectBackgroundPicBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.select_background_pic_desc:
            if (mSelectPicBtnClickistener != null) {
                mSelectPicBtnClickistener.onSelectPicBtnClick();
            }
            break;
        }
    }

    public void setOnSelectPicBtnClickListener(OnSelectPicBtnClickListener listener) {
        this.mSelectPicBtnClickistener = listener;
    }

    public interface OnSelectPicBtnClickListener {
        public void onSelectPicBtnClick();
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
