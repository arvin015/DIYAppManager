package com.activels.als.diyappmanager.utils;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

/**
 * @author arvin.li
 */
@SuppressLint("ViewConstructor")
public class PopupWindowUtil {

    private PopupWindow pWindow;

    private static PopupWindowUtil popupUtil;

    private PopupWindowUtil() {
        pWindow = new PopupWindow();
    }

    public static PopupWindowUtil getInstance() {
        if (popupUtil == null) {
            popupUtil = new PopupWindowUtil();
        }

        return popupUtil;
    }

    /**
     * 设置PopupWindow大小
     *
     * @param popWidth
     * @param popHeight
     */
    public void setPopupWindowSize(int popWidth, int popHeight) {
        pWindow.setWidth(popWidth);
        pWindow.setHeight(popHeight);
    }

    public void showAsDropDown(View v, int xoff, int yoff) {
        pWindow.showAsDropDown(v, xoff, yoff);
    }

    public void showAtLocation(View parent, int gravity, int x, int y) {
        pWindow.showAtLocation(parent, gravity, x, y);
    }

    public void dismiss() {
        if (pWindow != null) {
            if (pWindow.isShowing()) {
                pWindow.dismiss();
            }
        }
    }

    /**
     * PopupWindow配置
     *
     * @param children
     * @param animationStyle
     * @param callback
     */
    public void setPopuWindow(View children, int animationStyle, final IPopupWindowCallback callback) {

        pWindow.setContentView(children);

        pWindow.setAnimationStyle(animationStyle);
        ColorDrawable dw = new ColorDrawable(Color.HSVToColor(0x11,
                new float[]{00, 00, 0xff}));
        pWindow.setBackgroundDrawable(dw);
        pWindow.setOutsideTouchable(true);
        pWindow.setFocusable(true);
        pWindow.setTouchable(true);

        pWindow.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                // TODO Auto-generated method stub
                if (callback != null)
                    callback.popWindowDismissed();
            }
        });

    }

    /**
     * @author arvin.li
     */
    public interface IPopupWindowCallback {
        public void popWindowDismissed();
    }
}
