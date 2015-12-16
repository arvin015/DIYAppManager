package com.activels.als.diyappmanager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.activels.als.diyappmanager.adapter.TypeAdapter;
import com.activels.als.diyappmanager.entity.DatasetInfo;
import com.activels.als.diyappmanager.utils.DisplayUtil;
import com.activels.als.diyappmanager.utils.PopupWindowUtil;
import com.activels.als.diyappmanager.utils.Utils;
import com.activels.als.diyappmanager.view.CommonScrollView;
import com.activels.als.diyappmanager.view.WaterfallView;


public class MainActivity_old extends BaseActivity {

    private Context context;

    private CommonScrollView scrollView;
    private WaterfallView waterfallView;
    private EditText searchEdit;
    private Button typeBtn, sortBtn;

    private View datasetView = null;
    private View loadMoreView = null;

    private boolean isLoading = false;

    private PopupWindowUtil popup;

    public int selectTypeIndex = 0; //选中的类型索引
    public int selectSortIndex = 0; //当前选中排序索引

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;

        DisplayUtil.getInstance(this);

        init();
    }

    /**
     * 初始化
     */
    private void init() {
        scrollView = (CommonScrollView) findViewById(R.id.scrollView);
        waterfallView = (WaterfallView) findViewById(R.id.waterfallView);
        searchEdit = (EditText) findViewById(R.id.searchEdit);
        typeBtn = (Button) findViewById(R.id.typeBtn);
        sortBtn = (Button) findViewById(R.id.sortBtn);

        waterfallView.init(2);

        for (int i = 0; i < 14; i++) {
            waterfallView.addItemToLayout(createDatasetView(new DatasetInfo(), i));
        }

        typeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                choiceHandle(view, 1, context.getResources().getStringArray(R.array.type_arr));
            }
        });

        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choiceHandle(view, 2, getResources().getStringArray(R.array.sort_arr));
            }
        });

        scrollView.setOnBorderListener(new CommonScrollView.OnBorderListener() {
            @Override
            public void onBottom() {

                if (!isLoading) {

                    isLoading = true;

                    if (loadMoreView == null) {
                        loadMoreView = createLoadMoreView();

                        ((LinearLayout) scrollView.getChildAt(0)).addView(loadMoreView);
                    }

                    loadMoreView.setVisibility(View.VISIBLE);

                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });

                    loadMore();
                }
            }

            @Override
            public void onTop() {

            }

            @Override
            public void scroll() {

            }
        });
    }

    /**
     * popupWindow处理
     */
    private void choiceHandle(View v, final int type, final String[] strs) {

        popup = PopupWindowUtil.getInstance();
        popup.setPopupWindowSize(DisplayUtil.dip2px(300),
                type == 1 ? DisplayUtil.dip2px(335) : DisplayUtil.dip2px(100));

        View view = LayoutInflater.from(context).inflate(R.layout.popup_type, null);
        final ListView listView = (ListView) view.findViewById(R.id.typeList);
        TextView popupTitle = (TextView) view.findViewById(R.id.popupTitle);

        final TypeAdapter typeAdapter = new TypeAdapter(context, strs);
        listView.setAdapter(typeAdapter);

        if (type == 2) {
            popupTitle.setVisibility(View.GONE);
            typeAdapter.setSelectedIndex(selectSortIndex);
        } else {
            typeAdapter.setSelectedIndex(selectTypeIndex);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                popup.dismiss();

                if (type == 1) {

                    if (i == selectTypeIndex) {
                        return;
                    }

                    typeBtn.setText(strs[i]);
                    selectTypeIndex = i;

                } else {

                    if (i == selectSortIndex) {
                        return;
                    }

                    sortBtn.setText(strs[i]);
                    selectSortIndex = i;
                }

                typeAdapter.setSelectedIndex(i);
            }
        });

        popup.setPopuWindow(view, -1, null);
        popup.showAsDropDown(v, 0, 0);
    }

    /**
     * 加载更多
     */
    private void loadMore() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < 15; i++) {
                    waterfallView.addItemToLayout(createDatasetView(new DatasetInfo(), i));
                }

                isLoading = false;

                loadMoreView.setVisibility(View.GONE);
            }
        }, 2000);
    }

    /**
     * 创建dataset布局
     *
     * @param info
     * @return
     */
    private View createDatasetView(DatasetInfo info, int index) {

        datasetView = LayoutInflater.from(context).inflate(R.layout.dataset_item, null);

        ImageView iconImg = (ImageView) datasetView.findViewById(R.id.iconImg);
        TextView nameText = (TextView) datasetView.findViewById(R.id.nameText);
        TextView descText = (TextView) datasetView.findViewById(R.id.descText);
        TextView typeText = (TextView) datasetView.findViewById(R.id.typeText);
        TextView sizeText = (TextView) datasetView.findViewById(R.id.sizeText);

        nameText.setText("分數比一比" + index);

        return datasetView;
    }

    private View createLoadMoreView() {

        if (loadMoreView == null) {
            loadMoreView = LayoutInflater.from(context).inflate(R.layout.load_more, null);
        }

        return loadMoreView;
    }

    @Override
    public void updateProgrss(DatasetInfo datasetInfo) {
        super.updateProgrss(datasetInfo);
    }

    @Override
    public void downloadCompleted(DatasetInfo datasetInfo) {
        super.downloadCompleted(datasetInfo);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
