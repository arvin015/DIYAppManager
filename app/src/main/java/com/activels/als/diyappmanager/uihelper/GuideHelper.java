package com.activels.als.diyappmanager.uihelper;

import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by arvin.li on 2015/11/16.
 */
public class GuideHelper {

    private final int duration = 3000;

    private Timer timer;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 1000) {
                listener.guideEnd();
            }
        }
    };

    public GuideHelper() {

        startTimer();
    }

    public void startTimer() {
        cancelTimer();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(1000);
            }
        }, duration);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private IGuideListener listener;

    public void setListener(IGuideListener listener) {
        this.listener = listener;
    }

    public interface IGuideListener {
        public void guideEnd();
    }

}
