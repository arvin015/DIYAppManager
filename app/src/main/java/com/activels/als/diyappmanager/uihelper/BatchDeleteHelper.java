package com.activels.als.diyappmanager.uihelper;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.activels.als.diyappmanager.R;
import com.activels.als.diyappmanager.utils.DisplayUtil;
import com.activels.als.diyappmanager.utils.PopupWindowUtil;
import com.activels.als.diyappmanager.utils.StringUtil;

/**
 * Created by Administrator on 2015/9/12.
 */
public class BatchDeleteHelper {

    private Context context;

    private View mainView;
    private TextView selectedText;
    private Button trashBtn;

    private TextView deleteText, deleteAllText;

    public int selectedNum = 0;//选中个数
    public int totalNum = 0;//总条数
    public int totalSize, selectedSize;

    public boolean deleteAll = true;
    public boolean delete = false;

    private PopupWindowUtil popup;
    private ConfirmDialogHelper dialog;

    public BatchDeleteHelper(Context context, View view) {
        this.context = context;
        this.mainView = view;

        init();
    }

    private void init() {

        selectedText = (TextView) mainView.findViewById(R.id.selectedText);
        trashBtn = (Button) mainView.findViewById(R.id.trashBtn);

        trashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popup = PopupWindowUtil.getInstance();
                popup.setPopupWindowSize(DisplayUtil.dip2px(200), DisplayUtil.dip2px(110));

                View v = LayoutInflater.from(context).inflate(R.layout.popup_select_delete, null);
                deleteText = (TextView) v.findViewById(R.id.deleteText);
                deleteAllText = (TextView) v.findViewById(R.id.deleteAllText);

                if (!deleteAll) {
                    deleteAllText.setEnabled(false);
                    deleteAllText.setTextColor(Color.parseColor("#E0E0E0"));
                } else {
                    deleteAllText.setEnabled(true);
                }

                if (!delete) {
                    deleteText.setEnabled(false);
                    deleteText.setTextColor(Color.parseColor("#E0E0E0"));
                } else {
                    deleteText.setEnabled(true);
                }

                deleteText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        popup.dismiss();

                        dialog = new ConfirmDialogHelper(context);
                        dialog.setBtnPressListener(new ConfirmDialogHelper.BtnPressListener() {
                            @Override
                            public void onYesBtnPressed() {
                                if (listener != null)
                                    listener.onDeleteClick();
                            }
                        });
                        dialog.setDialogText(context.getString(R.string.delete_all_the_selected_Apps));
                        dialog.show();
                    }
                });

                deleteAllText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        popup.dismiss();

                        dialog = new ConfirmDialogHelper(context);
                        dialog.setBtnPressListener(new ConfirmDialogHelper.BtnPressListener() {
                            @Override
                            public void onYesBtnPressed() {

                                if (listener != null)
                                    listener.onDeleteAllClick();
                            }
                        });
                        dialog.setDialogText(context.getString(R.string.delete_all_tip_text));
                        dialog.show();
                    }
                });

                popup.setPopuWindow(v, -1, null);
                popup.showAsDropDown(view, 0, 0);
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
    }

    /**
     * 设置已选中大小
     */
    public void setCountText() {
        selectedText.setText(context.getString(R.string.selected_size_text)
                + " " + selectedSize + " kb" +
                " (" + totalSize + " kb)");
    }

    /**
     * 加减选中大小
     *
     * @param currentSize
     */
    public void countSize(float currentSize) {

        if (currentSize > 0) {
            selectedNum++;
        } else {
            selectedNum--;
        }

        selectedSize += currentSize;

        if (selectedNum > 0)
            delete = true;
        else
            delete = false;

        setCountText();
    }

    private IDeleteHelperListener listener;

    public void setListener(IDeleteHelperListener listener) {
        this.listener = listener;
    }

    public static interface IDeleteHelperListener {

        void onDeleteClick();

        void onDeleteAllClick();
    }

}
