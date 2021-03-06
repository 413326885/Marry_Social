package com.pkjiao.friends.mm.dialog;

import com.pkjiao.friends.mm.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class UpdateAppDialog extends Dialog implements
        android.view.View.OnClickListener {

    private Context mContext;

    private Button mCancelBtn;
    private Button mConfirmBtn;
    private OnConfirmBtnClickListener mConfirmClickistener;

    public UpdateAppDialog(Context context) {
        this(context, R.style.TranslucentDialog);
    }

    private UpdateAppDialog(Context context, int style) {
        super(context, style);
        this.mContext = context;
        initView();
        setCanceledOnTouchOutside(false);
        setCancelable(false);
    }

    private void initView() {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_update_app_layout, null);
        setContentView(view);

        mCancelBtn = (Button) view.findViewById(R.id.update_app_confirm_btn);
        mConfirmBtn = (Button) view.findViewById(R.id.update_app_cancel_btn);
        mCancelBtn.setOnClickListener(this);
        mConfirmBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.update_app_cancel_btn:
            this.dismiss();
            break;
        case R.id.update_app_confirm_btn:
            if (mConfirmClickistener != null) {
                mConfirmClickistener.onConfirmBtnClick();
            }
            break;
        }
    }

    public void setOnConfirmBtnClickListener(OnConfirmBtnClickListener listener) {
        this.mConfirmClickistener = listener;
    }

    public interface OnConfirmBtnClickListener {
        public void onConfirmBtnClick();
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
