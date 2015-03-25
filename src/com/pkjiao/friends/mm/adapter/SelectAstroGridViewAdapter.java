package com.pkjiao.friends.mm.adapter;

import com.pkjiao.friends.mm.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SelectAstroGridViewAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = "SelectAstroGridViewAdapter";

    private static final String[] ASTRO_DESCRIPTION = { "白羊座", "金牛座", "双子座",
            "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座", "水瓶座", "双鱼座" };

    private static final int[] ASTRO_ICON = {
            R.drawable.ic_aries_baiyang_gray,
            R.drawable.ic_taurus_jinniu_gray,
            R.drawable.ic_gemini_shuangzhi_gray,
            R.drawable.ic_cancer_juxie_gray, R.drawable.ic_leo_shizhi_gray,
            R.drawable.ic_virgo_chunv_gray,
            R.drawable.ic_libra_tiancheng_gray,
            R.drawable.ic_scorpio_tianxie_gray,
            R.drawable.ic_sagittarius_sheshou_gray,
            R.drawable.ic_capricprn_mejie_gray,
            R.drawable.ic_aquarius_shuiping_gray,
            R.drawable.ic_pisces_shuangyu_gray };

    public interface OnAstroItemClickListener {
        public void onItemClick(int position);
    }

    private Context mContext;
    private LayoutInflater mInflater;
    private OnAstroItemClickListener onItemClickListener = null;

    public SelectAstroGridViewAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    public void setOnItemClickListener(OnAstroItemClickListener listener) {
        onItemClickListener = listener;
    }

    @Override
    public int getCount() {
        return ASTRO_DESCRIPTION.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.astro_item_layout, parent,
                    false);
            holder = new ViewHolder();
            holder.astro_item = (LinearLayout) convertView
                    .findViewById(R.id.astro_item);
            holder.astro_icon = (ImageView) convertView
                    .findViewById(R.id.astro_icon);
            holder.astro_name = (TextView) convertView
                    .findViewById(R.id.astro_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final int pos = position;
        holder.astro_icon.setImageResource(ASTRO_ICON[pos]);
        holder.astro_name.setText(ASTRO_DESCRIPTION[pos]);
        holder.astro_item.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(pos);
                }
            }
        });

        return convertView;
    }

    class ViewHolder {
        LinearLayout astro_item;
        ImageView astro_icon;
        TextView astro_name;
    }

}
