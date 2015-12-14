package com.activels.als.diyappmanager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.activels.als.diyappmanager.utils.StringUtil;
import com.activels.als.diyappmanager.utils.ToastUtil;
import com.activels.als.diyappmanager.utils.Utils;

/**
 * Created by arvin.li on 2015/12/2.
 */
public class ConfigActivity extends Activity {

    private Context context;

    private EditText pathEdit, codeEdit;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_config);

        this.context = this;

        init();
    }

    private void init() {

        pathEdit = (EditText) findViewById(R.id.pathEdit);
        codeEdit = (EditText) findViewById(R.id.codeEdit);
        saveBtn = (Button) findViewById(R.id.saveBtn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ToastUtil.toastShort(context, context.getString(R.string.save_success_tip));

                String pathStr = pathEdit.getText().toString().trim();
                String codeStr = codeEdit.getText().toString().trim();

                if (!StringUtil.isEmpty(pathStr)) {
                    Utils.httpip = pathStr;
                    Utils.UPDATEPATH();
                }

                finish();
            }
        });
    }
}
