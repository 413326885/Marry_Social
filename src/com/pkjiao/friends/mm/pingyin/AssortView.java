package com.pkjiao.friends.mm.pingyin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class AssortView extends Button {

    public interface OnTouchAssortButtonListener {
        public void onTouchAssortButtonListener(String str);

        public void onTouchAssortButtonUP();
    }

    public AssortView(Context context) {
        super(context);
    }

    public AssortView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AssortView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private String[] mAssortChars = { "?", "#", "A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z" };

    private Paint mPaint = new Paint();
    private int mSelectedIndex = -1;
    private OnTouchAssortButtonListener onTouchListener;

    public void setOnTouchAssortListener(OnTouchAssortButtonListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();
        int interval = height / mAssortChars.length;

        for (int i = 0, length = mAssortChars.length; i < length; i++) {
            mPaint.setAntiAlias(true);
            mPaint.setTypeface(Typeface.DEFAULT);
            mPaint.setColor(Color.GRAY);
            mPaint.setTextSize(30);
            if (i == mSelectedIndex) {
                mPaint.setColor(Color.parseColor("#1abc9a"));
                mPaint.setFakeBoldText(true);
                mPaint.setTextSize(60);
            }
            float xPos = width / 2 - mPaint.measureText(mAssortChars[i]) / 2;
            float yPos = interval * i + interval;
            canvas.drawText(mAssortChars[i], xPos, yPos, mPaint);
            mPaint.reset();
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float y = event.getY();
        int index = (int) (y / getHeight() * mAssortChars.length);
        if (index >= 0 && index < mAssortChars.length) {

            switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (mSelectedIndex != index) {
                    mSelectedIndex = index;
                    if (onTouchListener != null) {
                        onTouchListener.onTouchAssortButtonListener(mAssortChars[mSelectedIndex]);
                    }

                }
                break;
            case MotionEvent.ACTION_DOWN:
                mSelectedIndex = index;
                if (onTouchListener != null) {
                    onTouchListener.onTouchAssortButtonListener(mAssortChars[mSelectedIndex]);
                }

                break;
            case MotionEvent.ACTION_UP:
                if (onTouchListener != null) {
                    onTouchListener.onTouchAssortButtonUP();
                }
                mSelectedIndex = -1;
                break;
            }
        } else {
            mSelectedIndex = -1;
            if (onTouchListener != null) {
                onTouchListener.onTouchAssortButtonUP();
            }
        }
        invalidate();

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void clear() {
        mSelectedIndex = -1;
        invalidate();
    }
}
