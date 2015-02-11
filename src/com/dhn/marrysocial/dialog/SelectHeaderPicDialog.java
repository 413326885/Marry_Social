package com.dhn.marrysocial.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.dhn.marrysocial.R;

public class SelectHeaderPicDialog extends Dialog implements
        android.view.View.OnClickListener {

    private Context mContext;

    private TextView mSelectFromCameraBtn;
    private TextView mSelectFromGalleryBtn;

    private OnSmallItemClickListener mSmallItemClickistener;

    public SelectHeaderPicDialog(Context context) {
        this(context, R.style.TranslucentDialog);
    }

    private SelectHeaderPicDialog(Context context, int style) {
        super(context, style);
        this.mContext = context;
        initView();
    }

    private void initView() {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.select_header_pic_dialog,
                null);
        setContentView(view);

        mSelectFromCameraBtn = (TextView) view
                .findViewById(R.id.select_from_camera);
        mSelectFromGalleryBtn = (TextView) view
                .findViewById(R.id.select_from_gallery);
        mSelectFromCameraBtn.setOnClickListener(this);
        mSelectFromGalleryBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.select_from_camera: {
            if (mSmallItemClickistener != null) {
                mSmallItemClickistener.onCameraBtnClick();
            }
            break;
        }
        case R.id.select_from_gallery: {
            if (mSmallItemClickistener != null) {
                mSmallItemClickistener.onGalleryBtnClick();
            }
            break;
        }
        }
    }

    public void setOnSmallItemClickListener(OnSmallItemClickListener listener) {
        this.mSmallItemClickistener = listener;
    }

    public interface OnSmallItemClickListener {
        public void onCameraBtnClick();
        public void onGalleryBtnClick();
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
