package com.activels.als.diyappmanager.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class CommonScrollView extends ScrollView {

    private OnBorderListener onBorderListener;
    private IScrollStopListener listener;
    private View contentView;

    private int lastY = 0;
    private int touchEventId = -9983761;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            View scroller = (View) msg.obj;
            if (msg.what == touchEventId) {
                if (lastY == scroller.getScrollY()) {
                    if (listener != null) {
                        listener.scrollStop();
                    }
                } else {
                    handler.sendMessageDelayed(handler.obtainMessage(touchEventId, scroller), 100);
                    lastY = scroller.getScrollY();
                }
            }
        }
    };

    public CommonScrollView(Context context) {
        super(context);
    }

    public CommonScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommonScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        doOnBorderListener();
    }

    private void doOnBorderListener() {

        if (onBorderListener != null)
            onBorderListener.scroll();

        if (contentView != null && contentView.getMeasuredHeight() <= getScrollY() + getHeight()) {
            if (onBorderListener != null) {
                onBorderListener.onBottom();
            }
        } else if (getScrollY() == 0) {
            if (onBorderListener != null) {
                onBorderListener.onTop();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            handler.sendMessageDelayed(handler.obtainMessage(touchEventId, this), 5);
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 设置滑动边界监听器
     *
     * @param onBorderListener
     */
    public void setOnBorderListener(final OnBorderListener onBorderListener) {
        this.onBorderListener = onBorderListener;
        if (onBorderListener == null) {
            return;
        }

        if (contentView == null) {
            contentView = getChildAt(0);
        }
    }

    /**
     * 设置滑动停止事件监听器
     *
     * @param listener
     */
    public void setScrollStopListener(IScrollStopListener listener) {
        this.listener = listener;
    }

    public static interface OnBorderListener {

        /**
         * Called when scroll to bottom
         */
        public void onBottom();

        /**
         * Called when scroll to top
         */
        public void onTop();

        public void scroll();
    }


    /**
     * 滑动停止事件接口
     */
    public static interface IScrollStopListener {
        /**
         * 滑动停止事件
         */
        void scrollStop();
    }
}