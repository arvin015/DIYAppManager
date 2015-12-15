package com.activels.als.diyappmanager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.activels.als.diyappmanager.entity.DatasetInfo;
import com.activels.als.diyappmanager.utils.SharedPreferencesUtils;
import com.activels.als.diyappmanager.utils.Utils;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.FinalHttp;

/**
 * Created by arvin.li on 2015/11/11.
 */
public class BaseActivity extends Activity {

    public FinalHttp fHttp;
    public FinalBitmap fBitmap;

    public SharedPreferencesUtils spfu;

    private MyBroadcast mBroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fHttp = new FinalHttp();
        fBitmap = FinalBitmap.create(this);
        fBitmap.configLoadingImage(R.drawable.photo_loading);
        fBitmap.configLoadfailImage(R.drawable.photo_loading);

        spfu = SharedPreferencesUtils.getInstance(this, null);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Utils.ACTION_UPDATE);
        filter.addAction(Utils.DOWNLOAD_COMPLETED);

        mBroadcast = new MyBroadcast();
        registerReceiver(mBroadcast, filter);

    }

    //toggle keyboard
    protected void toggleKeyboard(EditText edit, boolean needOpen) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (needOpen) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
        } else {
            imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBroadcast != null) {
            try {
                unregisterReceiver(mBroadcast);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新dataset下载进度
     *
     * @param datasetInfo
     */
    public void updateProgrss(DatasetInfo datasetInfo) {
    }

    /**
     * dataset下载完成
     *
     * @param datasetInfo
     */
    public void downloadCompleted(DatasetInfo datasetInfo) {
    }

    class MyBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            DatasetInfo info = (DatasetInfo) intent.getSerializableExtra("dataset");

            switch (intent.getAction()) {
                case Utils.ACTION_UPDATE:

                    updateProgrss(info);

                    break;
                case Utils.DOWNLOAD_COMPLETED:

                    downloadCompleted(info);

                    break;
            }
        }
    }
}
