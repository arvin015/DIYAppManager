package com.activels.als.diyappmanager.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.activels.als.diyappmanager.R;
import com.activels.als.diyappmanager.utils.DisplayUtil;

/**
 * Created by arvin.li on 2015/11/18.
 */
public class MyProgressBar extends ProgressBar {

    private Paint paint;

    private String text;

    public MyProgressBar(Context context) {
        this(context, null);
    }

    public MyProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        text = context.getString(R.string.get_text);

        init();
    }

    private void init() {

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setTextSize(DisplayUtil.sp2px(18));

        setWillNotDraw(false);
    }

    /**
     * 设置显示文字
     *
     * @param text
     */
    public void setText(String text) {

        this.text = text;

        invalidate();
    }

    /**
     * 绘制文字
     *
     * @param canvas
     */
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (text != null && text.length() > 0) {
            Rect rect = new Rect();
            paint.getTextBounds(text, 0, text.length(), rect);

            int x = (getWidth() / 2) - rect.centerX();
            int y = (getHeight() / 2) - rect.centerY();

            canvas.drawText(text, x, y, paint);
        }
    }
}
