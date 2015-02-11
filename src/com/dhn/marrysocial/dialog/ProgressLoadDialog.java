package com.dhn.marrysocial.dialog;

import com.dhn.marrysocial.R;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class ProgressLoadDialog extends Dialog {

    private Context mContext;
    private TextView mContentText;

    
    public ProgressLoadDialog(Context context) {
        this(context, R.style.TranslucentDialog);
    }

    private ProgressLoadDialog(Context context, int style) {
        super(context, style);
        this.mContext = context;
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.progress_dialog_layout, null);
        mContentText = (TextView) v.findViewById(R.id.progress_dialog_content);
        setContentView(v);
        int w = getWinWidth(mContext);
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.width = w - (w >> 2);
        this.getWindow().setAttributes(params);
        this.setCanceledOnTouchOutside(false);
    }

    public int getWinWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    public void setText(String content) {
        mContentText.setText(content);
    }

    public void setText(int id) {
        mContentText.setText(mContext.getString(id));
    }

}
