package com.activels.als.diyappmanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.activels.als.diyappmanager.qrcode.CaptureActivity;
import com.activels.als.diyappmanager.uihelper.GuideHelper;
import com.activels.als.diyappmanager.utils.NetworkUtil;
import com.activels.als.diyappmanager.utils.SharedPreferencesUtils;
import com.activels.als.diyappmanager.utils.StringUtil;
import com.activels.als.diyappmanager.utils.SystemUtil;
import com.activels.als.diyappmanager.utils.ToastUtil;
import com.activels.als.diyappmanager.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by arvin.li on 2015/11/13.
 */
public class LoginActivity extends Activity {

    private Context context;

    //    private RadioGroup radioGroup;
    private LinearLayout qrContainer, manualContainer;
    private LinearLayout qrTag, manualTag;
    private TextView qrText, manualText;
    private ImageButton loginBtn, qrBtn;
    private EditText userEdit, psdEdit;
    private ToggleButton checkBoxBtn;
    private ImageView guideImg;
    private Button settingBtn;

    private boolean isCheckedManual = true;

    private SharedPreferencesUtils spfu;

    private ProgressDialog pDialog;

    public static final int RCode = 1000;

    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_login);

//        try {
//            getWindow().addFlags(WindowManager.LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY").getInt(null));
//        } catch (Exception e) {
//            Log.d("print", "Could not access FLAG_NEEDS_MENU_KEY in addLegacyOverflowButton()", e);
//        }

        context = this;

        Intent intent = getIntent();
        isFirst = intent.getBooleanExtra("isFirst", true);

        SystemUtil.updateLanguage(context, null);

//        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        qrContainer = (LinearLayout) findViewById(R.id.qrcodeContainer);
        manualContainer = (LinearLayout) findViewById(R.id.manualContainer);
        qrTag = (LinearLayout) findViewById(R.id.qrTab);
        manualTag = (LinearLayout) findViewById(R.id.manualTab);
        qrText = (TextView) findViewById(R.id.qrText);
        manualText = (TextView) findViewById(R.id.manualText);
        loginBtn = (ImageButton) findViewById(R.id.loginBtn);
        qrBtn = (ImageButton) findViewById(R.id.qrBtn);
        userEdit = (EditText) findViewById(R.id.userEdit);
        psdEdit = (EditText) findViewById(R.id.psdEdit);
        checkBoxBtn = (ToggleButton) findViewById(R.id.checkBoxBtn);
        guideImg = (ImageView) findViewById(R.id.guideImg);
        settingBtn = (Button) findViewById(R.id.settingBtn);

        settingBtn.setOnClickListener(new SettingClick());

        qrTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCheckedManual) {
                    return;
                }

                isCheckedManual = false;

                changeTabStyle();
            }
        });

        manualTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCheckedManual) {
                    return;
                }

                isCheckedManual = true;

                changeTabStyle();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginHandle();
            }
        });

        qrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanningHandle();
            }
        });

//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                if (checkedId == R.id.qrTab) {
//                    qrContainer.setVisibility(View.VISIBLE);
//                    manualContainer.setVisibility(View.GONE);
//                } else {
//                    qrContainer.setVisibility(View.GONE);
//                    manualContainer.setVisibility(View.VISIBLE);
//                }
//            }
//        });

        if (isFirst) {
            guideHandle();
        } else {
            guideImg.setVisibility(View.GONE);
        }

    }

    /**
     * 欢迎页处理
     */
    private void guideHandle() {
        GuideHelper guideHelper = new GuideHelper();
        guideHelper.setListener(new GuideHelper.IGuideListener() {
            @Override
            public void guideEnd() {
                guideImg.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 扫描处理
     */
    private void scanningHandle() {

        Intent intent = new Intent(context, CaptureActivity.class);
        startActivityForResult(intent, RCode);
    }

    /**
     * 登录处理
     */
    private void loginHandle() {

        final String userName = userEdit.getText().toString();
        final String psd = psdEdit.getText().toString();

        if (StringUtil.isEmpty(userName) || StringUtil.isEmpty(psd)) {
            ToastUtil.toastShort(context, getString(R.string.user_or_psd_is_empty));

            return;
        }

        if ("demot001".equals(userName) && "hktt001".equals(psd)) {

            pDialog = new ProgressDialog(context);
            pDialog.setMessage(getString(R.string.login_waiting));
            pDialog.show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    pDialog.dismiss();

                    spfu.saveSharedPreferences(Utils.LOGIN_USER_NAME, userName);
                    if (checkBoxBtn.isChecked()) {
                        spfu.saveSharedPreferences(Utils.LOGIN_PSD, psd);
                    } else {
                        spfu.saveSharedPreferences(Utils.LOGIN_PSD, "");
                    }
                    spfu.saveSharedPreferences(Utils.AUTO_LOGIN, checkBoxBtn.isChecked());//是否需自动登录

                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 1000);

        } else {
            ToastUtil.toastShort(context, getString(R.string.user_or_psd_is_incorrect));
        }
    }

    /**
     * 获取焦点
     *
     * @param v
     */
    private void getFocus(View v) {
        v.hasFocus();
        v.setFocusableInTouchMode(true);
        v.setFocusable(true);
    }

    /**
     * 改变Tab样式
     */
    private void changeTabStyle() {
        if (!isCheckedManual) {
            qrTag.setBackgroundResource(R.drawable.login_tab_selected);
            manualTag.setBackgroundResource(R.drawable.login_tab_unselected);

            qrText.setTextColor(Color.BLACK);
            manualText.setTextColor(Color.WHITE);

            qrContainer.setVisibility(View.VISIBLE);
            manualContainer.setVisibility(View.GONE);

        } else {
            manualTag.setBackgroundResource(R.drawable.login_tab_selected);
            qrTag.setBackgroundResource(R.drawable.login_tab_unselected);

            manualText.setTextColor(Color.BLACK);
            qrText.setTextColor(Color.WHITE);

            qrContainer.setVisibility(View.GONE);
            manualContainer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Activity回调函数
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //扫描回调
        if (requestCode == RCode && resultCode == RESULT_OK) {

            JSONObject resultJson = null;

            try {
                resultJson = new JSONObject(data.getStringExtra("result"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (resultJson != null) {

                isCheckedManual = true;
                changeTabStyle();

                String userName = resultJson.optString("username");
                String password = resultJson.optString("password");

                userEdit.setText(userName);
                psdEdit.setText(password);

                loginBtn.performClick();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!NetworkUtil.isNetworkConnected(context)) {
            ToastUtil.toastShort(context, getString(R.string.network_not_connected));
        }

        spfu = SharedPreferencesUtils.getInstance(this, "");

        String psdStr = spfu.loadStringSharedPreference(Utils.LOGIN_PSD);
        String userStr = spfu.loadStringSharedPreference(Utils.LOGIN_USER_NAME);

        userEdit.setText(userStr);

        if (StringUtil.isEmpty(psdStr)) {
            checkBoxBtn.setChecked(false);
        } else {
            checkBoxBtn.setChecked(true);
            psdEdit.setText(psdStr);
        }

        userEdit.setSelection(StringUtil.isEmpty(userStr) ? 0 : userStr.length());

        if (spfu.loadBooleanSharedPreference(Utils.AUTO_LOGIN))
            loginBtn.performClick();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent(this, ConfigActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private int settingClickCount = 0;

    class SettingClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            settingClickCount++;

            if (settingClickCount == 1) {
                startTimer();
            }

            if (settingClickCount == 5) {
                Intent intent = new Intent(LoginActivity.this, ConfigActivity.class);
                startActivity(intent);

                settingClickCount = 0;

                cancelTimer();
            }
        }

        private Timer mTimer;

        private void startTimer() {
            cancelTimer();

            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    settingClickCount = 0;
                }
            }, 2000);
        }

        private void cancelTimer() {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
        }

    }
}
