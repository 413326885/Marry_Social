package com.dhn.marrysocial.dialog;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.adapter.SelectAstroGridViewAdapter;
import com.dhn.marrysocial.adapter.SelectAstroGridViewAdapter.OnAstroItemClickListener;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.GridView;

public class SelectAstroDialog extends Dialog {

    private Context mContext;
    private OnAstroItemClickListener mAstroClickistener;

    public SelectAstroDialog(Context context, OnAstroItemClickListener listener) {
        this(context, R.style.TranslucentDialog, listener);
    }

    private SelectAstroDialog(Context context, int style, OnAstroItemClickListener listener) {
        super(context, style);
        this.mContext = context;
        this.mAstroClickistener = listener;
        initView();
    }

    private void initView() {
        SelectAstroGridViewAdapter adapter = new SelectAstroGridViewAdapter(
                mContext);
        adapter.setOnItemClickListener(mAstroClickistener);
        GridView astroList = (GridView) LayoutInflater.from(mContext).inflate(
                R.layout.astro_layout, null, false);
        astroList.setAdapter(adapter);
        setContentView(astroList);

    }

//    public void setOnAstroClickListener(OnAstroItemClickListener listener) {
//        this.mAstroClickistener = listener;
//    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
