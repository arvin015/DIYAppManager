package com.activels.als.diyappmanager.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.activels.als.diyappmanager.utils.DisplayUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arvin.li on 2015/9/23.
 */
public class WaterfallView extends LinearLayout {

    private Context context;

    private int column = 2;

    private int viewWidth;

    private List<LinearLayout> layoutList;

    private int currentCol = 0;

    public WaterfallView(Context context) {
        this(context, null);
    }

    public WaterfallView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaterfallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.context = context;

        layoutList = new ArrayList<>();
    }

    /**
     * 初始化
     *
     * @param column
     */
    public void init(int column) {

        this.column = column;

        viewWidth = DisplayUtil.screenWidth / column - DisplayUtil.dip2px(20) / column;

        setOrientation(HORIZONTAL);

        for (int i = 0; i < column; i++) {
            LinearLayout layout = createColumnLayout();
            addView(layout);

            if (i != column - 1) {
                addView(createLineView());
            }

            layoutList.add(layout);
        }

    }

    /**
     * 创建LinearLayout
     *
     * @return
     */
    private LinearLayout createColumnLayout() {

        LinearLayout layout = new LinearLayout(context);

        LayoutParams params = new LayoutParams(viewWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT);
//        params.leftMargin = DisplayUtil.dip2px(10);

        layout.setLayoutParams(params);

        layout.setOrientation(VERTICAL);

        return layout;
    }

    private View createLineView() {
        View view = new View(context);
        view.setBackgroundColor(Color.parseColor("#B0B0B0"));
        LayoutParams params = new LayoutParams(2,
                ViewGroup.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);
        return view;
    }

    /**
     * 添加子View到LinearLayout中
     *
     * @param view
     */
    public void addItemToLayout(View view) {

        LinearLayout layout = layoutList.get(currentCol);

        layout.addView(view);

        currentCol++;

        if (currentCol >= column) {
            currentCol = 0;
        }
    }

    /**
     * 获取所有子View
     *
     * @return
     */
    public int getAllChildNum() {

        int count = 0;

        if (layoutList != null) {
            for (LinearLayout layout : layoutList) {
                count += layout.getChildCount();
            }
        }

        return count;
    }

    /**
     * 清空所有子View
     */
    public void removeAllChild() {
        if (layoutList != null) {
            for (LinearLayout layout : layoutList) {
                layout.removeAllViews();
            }
        }
    }

    /**
     * 重设当前列
     */
    public void resetCurrentColumn() {
        currentCol = 0;
    }
}
