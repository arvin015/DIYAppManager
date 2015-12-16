package com.activels.als.diyappmanager.uihelper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.activels.als.diyappmanager.R;
import com.activels.als.diyappmanager.utils.StringUtil;
import com.activels.als.diyappmanager.utils.ToastUtil;

/**
 * Created by Administrator on 2015/9/12.
 */
public class BatchDeleteHelper {

    private Context context;

    private View mainView;
    private FrameLayout topContainer;
    private Button cancelBtn;
    private TextView countText;
    public ToggleButton selectAllBtn;
    public Button deleteBtn;

    private Animation topInAnim;
    private Animation topOutAnim;

    public int selectedNum = 0;//选中个数
    public int totalNum = 0;//总条数
    public float totalSize, selectedSize;

    public BatchDeleteHelper(Context context, View view) {
        this.context = context;
        this.mainView = view;

        init();
    }

    private void init() {
        topContainer = (FrameLayout) mainView.findViewById(R.id.topContainer);
        cancelBtn = (Button) mainView.findViewById(R.id.cancelBtn);
        deleteBtn = (Button) mainView.findViewById(R.id.deleteBtn);
        countText = (TextView) mainView.findViewById(R.id.countText);
        selectAllBtn = (ToggleButton) mainView.findViewById(R.id.selectAllBtn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onBackClick();
            }
        });

        selectAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectAllBtn.isChecked()) {
                    selectedNum = totalNum;
                    selectedSize = totalSize;
                } else {
                    selectedNum = 0;
                    selectedSize = 0;
                }

                setCountText();

                if (listener != null)
                    listener.onSelectAllClick(selectAllBtn.isChecked());
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (totalSize == 0 || selectedSize == 0) {
                    ToastUtil.toastShort(context, context.getString(R.string.no_subject_selected_text));
                    return;
                }

                new AlertDialog.Builder(context).setMessage(context.getString(R.string.delete_all_the_selected_Apps))
                        .setNegativeButton(context.getString(R.string.cancel_text), null)
                        .setPositiveButton(context.getString(R.string.sure_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (listener != null)
                                    listener.onDeleteClick();
                            }
                        }).show();
            }
        });
    }

    /**
     * 数据重新设置
     */
    public void reset() {
        selectedNum = 0;
        selectedSize = 0;
        setCountText();
        selectAllBtn.setChecked(false);
    }

    /**
     * 设置已选中大小
     */
    public void setCountText() {
        countText.setText(context.getString(R.string.selected_size_text)
                + "(" + StringUtil.getDecimalFormat(Math.abs(selectedSize), 2) + "MB / " +
                "" + StringUtil.getDecimalFormat(totalSize, 2) + "MB)");
    }

    /**
     * 加减选中大小
     *
     * @param currentSize
     */
    public void countSize(float currentSize) {

        if (currentSize > 0) {
            selectedNum++;

            if (selectedNum >= totalNum)
                selectAllBtn.setChecked(true);

        } else {
            selectedNum--;

            selectAllBtn.setChecked(false);
        }

        selectedSize += currentSize;

        setCountText();
    }

    /**
     * 按钮是否可操作
     *
     * @param enabled
     */
    public void setBtnEnabled(boolean enabled) {
        selectAllBtn.setEnabled(enabled);
        deleteBtn.setEnabled(enabled);
    }

    /**
     * 显示隐藏删除bar
     *
     * @param visibility
     */
    public void setDeleteBarVisibility(int visibility) {

        if (visibility == View.VISIBLE) {
            if (topInAnim == null) {
                topInAnim = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_top);
            }

            topContainer.setAnimation(topInAnim);
            topInAnim.start();

        } else {
            if (topOutAnim == null) {
                topOutAnim = AnimationUtils.loadAnimation(context, R.anim.slide_out_to_top);
            }

            topContainer.setAnimation(topOutAnim);
            topOutAnim.start();
        }

        topContainer.setVisibility(visibility);
    }

    private IDeleteHelperListener listener;

    public void setListener(IDeleteHelperListener listener) {
        this.listener = listener;
    }

    public static interface IDeleteHelperListener {
        void onBackClick();

        void onSelectAllClick(boolean isChecked);

        void onDeleteClick();
    }

}
