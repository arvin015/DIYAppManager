package com.activels.als.diyappmanager.uihelper;

import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.activels.als.diyappmanager.R;
import com.activels.als.diyappmanager.utils.DisplayUtil;

public class ConfirmDialogHelper {

    private Context context;
    private Dialog confirmDialog;

    public ConfirmDialogHelper(Context context) {
        this.context = context;

        confirmDialog = new Dialog(context, R.style.CustomDialogTheme);

        View view = LayoutInflater.from(context).inflate(R.layout.confirm_dialog, null);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(DisplayUtil.dip2px(400),
                DisplayUtil.dip2px(260));

        Button yesBtn = (Button) view.findViewById(R.id.yesBtn);
        Button cancelBtn = (Button) view.findViewById(R.id.cancelBtn);

        yesBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onYesBtnPressed();
                confirmDialog.dismiss();
            }
        });

        cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.cancel();
            }
        });

        confirmDialog.setContentView(view, params);
        confirmDialog.setCancelable(false);

    }

    public void setDialogText(String str) {
        TextView dialogText = (TextView) confirmDialog
                .findViewById(R.id.dialogText);

        dialogText.setText(Html.fromHtml(str));
    }

    public void setSubjectText(String str) {
        TextView subjectText = (TextView) confirmDialog
                .findViewById(R.id.subjectText);
        subjectText.setVisibility(View.VISIBLE);

        subjectText.setText(Html.fromHtml(str));
    }

    public void show() {
        confirmDialog.show();
    }

    public void dismiss() {
        if (confirmDialog.isShowing())
            confirmDialog.dismiss();
    }

    private BtnPressListener listener;

    public void setBtnPressListener(BtnPressListener listener) {
        this.listener = listener;
    }

    public static interface BtnPressListener {
        public void onYesBtnPressed();
    }

}
