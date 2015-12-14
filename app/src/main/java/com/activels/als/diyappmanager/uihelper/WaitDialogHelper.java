package com.activels.als.diyappmanager.uihelper;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Html;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by arvin.li on 2015/12/7.
 */
public class WaitDialogHelper {

    private Context context;
    private ProgressDialog dialog;
    private Timer timer;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 1000)
                closeDialog();
        }
    };

    public WaitDialogHelper(Context context) {
        this.context = context;

        init();
    }

    private void init() {
        dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
    }

    public void updateMessage(String message) {
        dialog.setMessage(Html.fromHtml(message));
    }

    public void showDialog() {

        dialog.show();

        startTimer();
    }

    public void closeDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();

            if (listener != null)
                listener.close();
        }
    }

    private void startTimer() {
        cancelTimer();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(1000);
            }
        }, 60000);
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public IWaitDialogHelper listener;

    public void setListener(IWaitDialogHelper listener) {
        this.listener = listener;
    }

    public interface IWaitDialogHelper {
        public void close();
    }

}
