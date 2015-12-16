package com.activels.als.diyappmanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.activels.als.diyappmanager.adapter.DatasetAdapter;
import com.activels.als.diyappmanager.adapter.DownloadedAdapter;
import com.activels.als.diyappmanager.adapter.TypeAdapter;
import com.activels.als.diyappmanager.db.DatasetDao;
import com.activels.als.diyappmanager.db.DatasetDaoImpl;
import com.activels.als.diyappmanager.db.ThreadDao;
import com.activels.als.diyappmanager.db.ThreadDaoImpl;
import com.activels.als.diyappmanager.entity.DatasetInfo;
import com.activels.als.diyappmanager.pulltorefresh.lib.ILoadingLayout;
import com.activels.als.diyappmanager.pulltorefresh.lib.PullToRefreshBase;
import com.activels.als.diyappmanager.pulltorefresh.view.PullToRefreshGridView;
import com.activels.als.diyappmanager.service.DownloadService;
import com.activels.als.diyappmanager.uihelper.BatchDeleteHelper;
import com.activels.als.diyappmanager.uihelper.WaitDialogHelper;
import com.activels.als.diyappmanager.utils.DisplayUtil;
import com.activels.als.diyappmanager.utils.FileUtils;
import com.activels.als.diyappmanager.utils.PopupWindowUtil;
import com.activels.als.diyappmanager.utils.StringUtil;
import com.activels.als.diyappmanager.utils.ToastUtil;
import com.activels.als.diyappmanager.utils.Utils;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends BaseActivity {

    private Context context;
    private View mainView;

    private PullToRefreshGridView pullToRefreshGridView;
    private EditText searchEdit;
    private Button typeBtn, sortBtn, refreshBtn, manageBtn, logoutBtn;
    private GridView downloadedGridView, gridView;
    private TextView emptyTipText;
    private RelativeLayout searchLayout;

    private ProgressDialog pDialog;

    private ILoadingLayout loadLayoutBottom;//加载更多View

    private PopupWindowUtil popup;

    private List<DatasetInfo> datasetInfoList;
    private List<DatasetInfo> downloadedDatasetList;//已经下载完的dataset
    private List<DatasetInfo> deletedDatasetList;//已经删掉的dataset

    private DatasetAdapter datasetAdapter;
    private DownloadedAdapter downloadedAdapter;

    //一般模式
    public int selectTypeIndex = 0; //选中的类型索引
    public int selectSortIndex = 0; //当前选中排序索引

    //删除模式
    public int dSelectTypeIndex = 0;//选中的类型索引
    public int dSelectSortIndex = 0;//当前选中排序索引

    private int totalCount = 0; //总条数
    private int currentPage = 1;//当前页数

    private boolean isLoading = false;
    private boolean isOk = false;
    private boolean isFirst = true;
    private boolean isFirstLoad = true;

    private long lastTime = 0;//上一次按系统返回键时间

    private DatasetDao mDatasetDao;
    private ThreadDao mThreadDao;

    private int mode = 0;//0:一般模式; 1:删除模式
    private BatchDeleteHelper batchDeleteHelper;
    private WaitDialogHelper waitDialog;

    private Set<DatasetInfo> infoList = new LinkedHashSet<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 200) {
                closeDialog();
                datasetAdapter.resetDatasetList();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        datasetAdapter.isDeleteAll = false;
                    }
                }, 2000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainView = LayoutInflater.from(this).inflate(R.layout.activity_main, null);

        setContentView(mainView);

        this.context = this;

        DisplayUtil.getInstance(this);

        init();
    }

    /**
     * 初始化
     */
    private void init() {

        showDialog(getString(R.string.login_waiting));

        mDatasetDao = new DatasetDaoImpl(context);

        pullToRefreshGridView = (PullToRefreshGridView) findViewById(R.id.refreshGridView);
        searchEdit = (EditText) findViewById(R.id.searchEdit);
        typeBtn = (Button) findViewById(R.id.typeBtn);
        sortBtn = (Button) findViewById(R.id.sortBtn);
        refreshBtn = (Button) findViewById(R.id.refreshBtn);
        manageBtn = (Button) findViewById(R.id.manageBtn);
        logoutBtn = (Button) findViewById(R.id.logoutBtn);
        emptyTipText = (TextView) findViewById(R.id.emptyTipText);
        downloadedGridView = (GridView) findViewById(R.id.downloadedGridView);
        searchLayout = (RelativeLayout) findViewById(R.id.searchLayout);

        initPullRefreshGridView();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutHandle();
            }
        });

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshHanlde();
            }
        });

        manageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                entryDeleteMode();
            }
        });

        typeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                choiceHandle(view, 1, getResources().getStringArray(R.array.type_arr));
            }
        });

        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choiceHandle(view, 2, getResources().getStringArray(R.array.sort_arr));
            }
        });

        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if (i == SCROLL_STATE_IDLE) {
                    if (datasetAdapter != null)
                        datasetAdapter.isScrolling = false;//滑动停止
                } else {
                    if (datasetAdapter != null)
                        datasetAdapter.isScrolling = true;//滑动中
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                if (isOk) {
                    if (i + i1 >= i2) {

                        if (datasetAdapter.getCount() >= totalCount) {

                            if (isFirst) {

                                ToastUtil.toastShort(context, getString(R.string.load_all_text));

                                isFirst = false;
                            }

                        } else {
                            if (!isLoading) {

                                pullToRefreshGridView.setRefreshing();

                                isLoading = true;
                            }
                        }
                    }
                }
            }
        });

        downloadedGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mode == 1) {//删除模式可用
                    if (downloadedAdapter != null) {

                        DatasetInfo info = downloadedDatasetList.get(i);

                        downloadedAdapter.setItemChecked(i, !info.isChecked());

                        float size = Float.parseFloat(info.getSize().substring(0, info.getSize().lastIndexOf("M")));
                        batchDeleteHelper.countSize(info.isChecked() ? size : -(size));
                    }
                }
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (true) //ver 1
                    return true;

                //ver 2
                if (datasetInfoList != null) {
                    final DatasetInfo info = datasetInfoList.get(i);
                    if (info.isCanDelete()) {

                        popup = PopupWindowUtil.getInstance();
                        popup.setPopupWindowSize(DisplayUtil.dip2px(60), DisplayUtil.dip2px(60));

                        View view1 = LayoutInflater.from(context).inflate(R.layout.pup_delete, null);
                        TextView deleteText = (TextView) view1.findViewById(R.id.deleteText);

                        deleteText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                deleteDatasetHandle(info);

                                popup.dismiss();
                            }
                        });

                        popup.setPopuWindow(view1, -1, null);
                        popup.showAsDropDown(view, view.getWidth() / 2 - DisplayUtil.dip2px(30),
                                -view.getHeight() / 2 - DisplayUtil.dip2px(60));
                    }
                }

                return true;
            }
        });

        datasetInfoList = new ArrayList<>();
        datasetAdapter = new DatasetAdapter(context, datasetInfoList, fBitmap);
        datasetAdapter.listener = new DatasetAdapter.IDatasetAdapterListener() {
            @Override
            public void absortDownload(DatasetInfo info) {
                deleteDatasetHandle(info);
            }
        };
        if (gridView != null) {
            gridView.setAdapter(datasetAdapter);
        }

        loadData();
    }

    /**
     * 进入删除模式
     */
    private void entryDeleteMode() {

        mode = 1;

        setBtnVisibility(View.GONE);

        downloadedGridView.setVisibility(View.VISIBLE);
        pullToRefreshGridView.setVisibility(View.GONE);

        if (batchDeleteHelper == null) {
            batchDeleteHelper = new BatchDeleteHelper(context, mainView);
            batchDeleteHelper.setListener(new BatchDeleteHelper.IDeleteHelperListener() {
                @Override
                public void onBackClick() {
                    exitDeleteMode();
                }

                @Override
                public void onSelectAllClick(boolean isChecked) {
                    for (DatasetInfo info : downloadedDatasetList) {
                        info.setIsChecked(isChecked);
                    }

                    downloadedAdapter.notifyDataSetChanged();
                }

                @Override
                public void onDeleteClick() {
                    deleteDownloadedDataset();
                }
            });
        }
        batchDeleteHelper.setDeleteBarVisibility(View.VISIBLE);

        if (downloadedDatasetList == null) {
            downloadedDatasetList = new ArrayList<>();
        }
        downloadedDatasetList.clear();

        if (deletedDatasetList == null) {
            deletedDatasetList = new ArrayList<>();
        }
        deletedDatasetList.clear();

        infoList.clear();

        loadDataFromLocal(0);//从本地数据库获取下载完成数据

        if (downloadedAdapter == null) {
            downloadedAdapter = new DownloadedAdapter(context, downloadedDatasetList, fBitmap);
            downloadedGridView.setAdapter(downloadedAdapter);
        } else {
            downloadedAdapter.updateData(downloadedDatasetList);
        }

        updateTypeAndSort();
    }


    /**
     * 退出删除模式
     */
    private void exitDeleteMode() {

        mode = 0;

        emptyTipText.setVisibility(View.GONE);
        setBtnVisibility(View.VISIBLE);

        downloadedGridView.setVisibility(View.GONE);
        pullToRefreshGridView.setVisibility(View.VISIBLE);

        if (deletedDatasetList != null) {
            datasetAdapter.updateData(deletedDatasetList);
        }

        datasetAdapter.downloadCompletedBatch(infoList);//批处理已经下载完成的

        batchDeleteHelper.setDeleteBarVisibility(View.GONE);

        updateTypeAndSort();
    }

    /**
     * 按钮显示隐藏
     *
     * @param visibility
     */
    private void setBtnVisibility(int visibility) {
        searchLayout.setVisibility(visibility);
        logoutBtn.setVisibility(visibility);
        manageBtn.setVisibility(visibility);
        refreshBtn.setVisibility(visibility);
    }

    /**
     * 更新TypeAndSort选项信息
     */
    private void updateTypeAndSort() {
        if (mode == 0) {//一般模式
            typeBtn.setText(getResources().getStringArray(R.array.type_arr)[selectTypeIndex]);
            sortBtn.setText(getResources().getStringArray(R.array.sort_arr)[selectSortIndex]);
        } else {//删除模式
            dSelectSortIndex = 0;
            dSelectTypeIndex = 0;

            typeBtn.setText(getResources().getStringArray(R.array.type_arr)[0]);
            sortBtn.setText(getResources().getStringArray(R.array.sort_arr)[0]);
        }
    }

    /**
     * 登出
     */
    private void logoutHandle() {

        new AlertDialog.Builder(context).setMessage(getString(R.string.logout_tip_text))
                .setPositiveButton(getString(R.string.sure_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showDialog(getString(R.string.login_waiting));

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                closeDialog();

                                Intent intent = new Intent(context, LoginActivity.class);
                                intent.putExtra("isFirst", false);
                                startActivity(intent);

                                spfu.saveSharedPreferences(Utils.AUTO_LOGIN, false);

                                finish();
                            }
                        }, 1000);

                    }
                }).setNegativeButton(getString(R.string.cancel_text), null).show();
    }

    /**
     * 初始化刷新数据
     */
    private void initPullRefreshGridView() {
        // TODO Auto-generated method stub
        pullToRefreshGridView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);//只可加载更多

        gridView = pullToRefreshGridView.getRefreshableView();

        gridView.setNumColumns(2);

        loadLayoutBottom = pullToRefreshGridView.getLoadingLayoutProxy(false, true);//获取底部加载更多View
        loadLayoutBottom.setPullLabel(getString(R.string.loadmore_text));
        loadLayoutBottom.setRefreshingLabel(getString(R.string.loadmore_text));
        loadLayoutBottom.setReleaseLabel(getString(R.string.loadmore_text));
        loadLayoutBottom.setLoadingDrawable(null);

        pullToRefreshGridView.setScrollingWhileRefreshingEnabled(true);//刷新时是否可滑动
        pullToRefreshGridView.setPullToRefreshOverScrollEnabled(false);//设置滑动是否引起刷新

        //该加载更多了回调接口
        pullToRefreshGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<GridView>() {
            @Override
            public void onRefresh(PullToRefreshBase<GridView> refreshView) {
                loadData();
            }
        });
    }

    /**
     * 加载数据
     */
    private void loadData() {

        AjaxParams params = new AjaxParams();
        params.put("type", selectTypeIndex + "");
        params.put("token", Utils.token);
        params.put("page", currentPage + "");
        params.put("count", Utils.count + "");
        params.put("keyword", searchEdit.getText().toString().trim());

        fHttp.post(Utils.url_listdataset, params, new MyAjaxCallback());
    }

    /**
     * 删除dataset操作
     *
     * @param info
     */
    private void deleteDatasetHandle(DatasetInfo info) {

        //通知后台service停止该dataset下载
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(Utils.ACTION_DELETE_ONE);
        intent.putExtra("dataset", info);
        startService(intent);

        //删除本地该下载文件及本地数据库下载任务信息
        if (mThreadDao == null) {
            mThreadDao = new ThreadDaoImpl(context);
        }
        mThreadDao.deleteThread(info.getLink());//删除数据库下载任务信息
        mDatasetDao.deleteDatasetByDatasetId(info.getId());//删除数据库文件信息

        //删除本地该dataset已下载文件
        File zipFile = new File(Utils.DOWNLOAD_PATH, info.getZipDatasetName());
        if (zipFile.exists())
            FileUtils.deleteFile(zipFile.getAbsolutePath());

        File datasetFile = new File(Utils.DOWNLOAD_PATH, info.getDatasetName());
        FileUtils.deleteDir(datasetFile.getAbsolutePath());

        //设置当前删除dataset，防止其再更新进度
        datasetAdapter.currentDeleteId = info.getId();

        //重设该dataset数据
        info.setCanDelete(false);
        info.setFinished(0);
        info.setOperateState(0);

        datasetAdapter.notifyDataSetChanged();
    }

    /**
     * 删除所有dataset操作
     */
    private void deleteAllHandle() {

        showDialog(getString(R.string.login_waiting));

        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(context, DownloadService.class);
                intent.setAction(Utils.ACTION_STOP_ALL_UNLOCKED);
                intent.putExtra("datasetList", (Serializable) datasetInfoList);
                context.startService(intent);

                //删除未上锁并且已下载的dataset的本地保存及文件
                for (DatasetInfo info : datasetInfoList) {
                    if (info.isCanDelete() && !info.isLocked()) {

                        mDatasetDao.deleteDatasetByDatasetId(info.getId());

                        File zipFile = new File(Utils.DOWNLOAD_PATH, info.getZipDatasetName());
                        if (zipFile.exists())
                            FileUtils.deleteFile(zipFile.getAbsolutePath());

                        File datasetFile = new File(Utils.DOWNLOAD_PATH, info.getDatasetName());
                        FileUtils.deleteDir(datasetFile.getAbsolutePath());
                    }
                }

                handler.sendEmptyMessage(200);
            }
        }).start();
    }

    /**
     * 删除选中的已下载的dataset
     */
    private void deleteDownloadedDataset() {

        //删除显示框
        if (waitDialog == null) {
            waitDialog = new WaitDialogHelper(context);
        }

        waitDialog.updateMessage(getString(R.string.deleted_text) +
                "(" + batchDeleteHelper.selectedNum + "/" + downloadedDatasetList.size() + ")");
        waitDialog.setListener(new WaitDialogHelper.IWaitDialogHelper() {
            @Override
            public void close() {

                batchDeleteHelper.totalSize -= batchDeleteHelper.selectedSize;
                batchDeleteHelper.reset();

                downloadedDatasetList.removeAll(deletedDatasetList);

                //全部删除了，则提示数据为空
                if (downloadedDatasetList.size() < 1) {
                    emptyTipText.setVisibility(View.VISIBLE);
                }

                downloadedAdapter.notifyDataSetChanged();
            }
        });
        waitDialog.showDialog();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (downloadedDatasetList != null) {
                    for (DatasetInfo info : downloadedDatasetList) {

                        if (info.isChecked()) {

                            mDatasetDao.deleteDatasetByDatasetId(info.getId());//删除数据库文件信息

                            //删除本地该dataset已下载文件
                            File zipFile = new File(Utils.DOWNLOAD_PATH, info.getZipDatasetName());
                            if (zipFile.exists())
                                FileUtils.deleteFile(zipFile.getAbsolutePath());

                            File datasetFile = new File(Utils.DOWNLOAD_PATH, info.getDatasetName());
                            FileUtils.deleteDir(datasetFile.getAbsolutePath());

                            //重设该dataset数据
                            info.setCanDelete(false);
                            info.setFinished(0);
                            info.setOperateState(0);
                            info.setIsChecked(false);

                            batchDeleteHelper.selectedNum--;

                            deletedDatasetList.add(info);

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    waitDialog.updateMessage(getString(R.string.deleted_text) +
                                            "(" + batchDeleteHelper.selectedNum + "/" + batchDeleteHelper.totalNum + ")");
                                }
                            });
                        }
                    }

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            waitDialog.closeDialog();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 刷新处理
     */
    private void refreshHanlde() {

        showDialog(getString(R.string.login_waiting));

        if (mode == 0) {
            datasetInfoList.clear();
            datasetAdapter.notifyDataSetChanged();

            currentPage = 1;

            isFirst = true;
            isOk = false;
            isFirstLoad = true;

            loadData();
        }

    }

    /**
     * popupWindow处理
     */
    private void choiceHandle(View v, final int type, final String[] strs) {

        popup = PopupWindowUtil.getInstance();
        popup.setPopupWindowSize(DisplayUtil.dip2px(300),
                type == 1 ? DisplayUtil.dip2px(280) : DisplayUtil.dip2px(100));

        View view = LayoutInflater.from(context).inflate(R.layout.popup_type, null);
        final ListView listView = (ListView) view.findViewById(R.id.typeList);
        TextView popupTitle = (TextView) view.findViewById(R.id.popupTitle);

        final TypeAdapter typeAdapter = new TypeAdapter(context, strs);
        listView.setAdapter(typeAdapter);

        if (type == 2) {
            popupTitle.setVisibility(View.GONE);
            typeAdapter.setSelectedIndex(mode == 0 ? selectSortIndex : dSelectSortIndex);
        } else {
            typeAdapter.setSelectedIndex(mode == 0 ? selectTypeIndex : dSelectTypeIndex);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                popup.dismiss();

                if (type == 1) {//分类

                    typeBtn.setText(strs[i]);
                    if (mode == 0) {//一般模式
                        selectTypeIndex = i;

                        refreshHanlde();

                    } else {

                        if (dSelectTypeIndex == i)
                            return;

                        dSelectTypeIndex = i;

                        loadDataFromLocal(dSelectTypeIndex);

                        downloadedAdapter.notifyDataSetChanged();
                    }

                } else {//排序

                    if (mode == 0 && i == selectSortIndex ||
                            mode == 1 && i == dSelectSortIndex) {
                        return;
                    }

                    sortBtn.setText(strs[i]);
                    if (mode == 0) {
                        selectSortIndex = i;

                        DatasetInfo.sortDatasetList(datasetInfoList, selectSortIndex);
                        datasetAdapter.notifyDataSetChanged();

                    } else {
                        dSelectSortIndex = i;

                        DatasetInfo.sortDatasetList(downloadedDatasetList, dSelectSortIndex);
                        downloadedAdapter.notifyDataSetChanged();
                    }
                }

                typeAdapter.setSelectedIndex(i);
            }
        });

        popup.setPopuWindow(view, -1, null);
        popup.showAsDropDown(v, 0, 0);
    }

    /**
     * 从本地获取下载完成数据
     *
     * @param index
     */
    private void loadDataFromLocal(int index) {
        //获取相应的分类dataset集合
        downloadedDatasetList.clear();
        downloadedDatasetList.addAll(mDatasetDao.getAllDownloadedDataset(index + ""));

        //对应的分类没有则为空提示
        if (downloadedDatasetList.size() < 1) {
            emptyTipText.setVisibility(View.VISIBLE);
        } else {
            emptyTipText.setVisibility(View.GONE);
        }

        float totalSize = 0;

        for (DatasetInfo info : downloadedDatasetList) {
            info.setIsChecked(false);
            totalSize += Float.parseFloat(info.getSize().substring(0, info.getSize().lastIndexOf("M")));
        }

        batchDeleteHelper.totalNum = downloadedDatasetList.size();
        batchDeleteHelper.totalSize = totalSize;
        batchDeleteHelper.reset();

        DatasetInfo.sortDatasetList(downloadedDatasetList, dSelectSortIndex);//排序
    }

    /**
     * 显示加载框
     */
    private void showDialog(String message) {
        if (pDialog == null) {
            pDialog = new ProgressDialog(context);
            pDialog.setMessage(message);
            pDialog.show();
        }

        pDialog.show();
    }

    /**
     * 关闭加载框
     */
    private void closeDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    class MyAjaxCallback extends AjaxCallBack {

        public MyAjaxCallback() {
        }

        @Override
        public void onSuccess(final Object o) {
            super.onSuccess(o);

            int time = 1000;

            if (isFirstLoad) {
                time = 0;

                isFirstLoad = false;
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    closeDialog();

                    isLoading = false;

                    pullToRefreshGridView.onRefreshComplete();

                    isOk = true;

                    JSONObject json = null;

                    if (o != null) {

                        try {
                            json = new JSONObject(o.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (json != null) {
                            totalCount = json.optInt("count");

                            List<DatasetInfo> datasetInfos;

                            JSONArray array = json.optJSONArray("datasets");

                            //数据为空
                            if (array == null || (array != null && array.length() < 1)) {

                                emptyTipText.setVisibility(View.VISIBLE);

                            } else {

                                emptyTipText.setVisibility(View.GONE);

                                datasetInfos = new ArrayList<>();

                                for (int i = 0; i < array.length(); i++) {

                                    DatasetInfo info = new DatasetInfo(array.optJSONObject(i));

                                    //从本地数据库中获取dataset下载记录
                                    DatasetInfo d = mDatasetDao.selectDatasetByDatasetId(info.getId());
                                    if (d != null) {

                                        int state = d.getOperateState();
                                        if (state < Utils.STATE_UNZIPING) {
                                            state = Utils.STATE_STOP;
                                        }

                                        if (Utils.STATE_UNZIPED == state) {

                                            //服务器上的时间比已经下载好本地时间要新，则需更新
                                            if (StringUtil.compareTime(info.getDate(), d.getDate())) {
                                                state = Utils.STATE_UPDATE;
                                            } else { //下载完了，则显示解压后的大小
                                                info.setSize(d.getSize());
                                            }
                                        }

                                        info.setOperateState(state);
                                        info.setFinished(d.getFinished());
                                        info.setCanDelete(true);//可以删除
                                    }

                                    datasetInfos.add(info);
                                }

                                datasetInfoList.addAll(datasetInfos);
                                DatasetInfo.sortDatasetList(datasetInfoList, selectSortIndex);//排序

                                datasetAdapter.notifyDataSetChanged();

                                currentPage++;
                            }
                        }
                    }
                }
            }, time);

        }

        @Override
        public void onFailure(Throwable t, int errorNo, String strMsg) {
            super.onFailure(t, errorNo, strMsg);

            closeDialog();
            pullToRefreshGridView.onRefreshComplete();

            isLoading = false;

            ToastUtil.toastShort(context, strMsg);
        }
    }

    @Override
    public void updateProgrss(DatasetInfo datasetInfo) {
        if (mode == 0)
            datasetAdapter.updateProgress(datasetInfo);
    }

    @Override
    public void downloadCompleted(DatasetInfo datasetInfo) {
        if (mode == 0)
            datasetAdapter.downloadCompleted(datasetInfo);
        else //删除模式时，先保存下载完成的dataset的通知
            infoList.add(datasetInfo);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == keyCode && event.getRepeatCount() == 0) {

            if (mode == 0) {//一般模式退出
                if (System.currentTimeMillis() - lastTime > 2000) {
                    ToastUtil.toastShort(context, getString(R.string.press_again_to_quit));
                    lastTime = System.currentTimeMillis();
                } else {
                    finish();
                }
            } else {//删除模式返回
                exitDeleteMode();
            }

        } else if (KeyEvent.KEYCODE_ENTER == keyCode) {//软键盘回车事件
            toggleKeyboard(searchEdit, false);
            refreshHanlde();//刷新
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        clear();
    }

    /**
     * 清除下载信息
     */
    private void clear() {
        //退出时保存dataset下载记录
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(Utils.ACTION_QUIT);
        startService(intent);

        //ver 1
        if (mDatasetDao != null)
            mDatasetDao.deleteAllDownloadingDataset();

        //ver 1 删除未下载完未解压完的文件
        FileUtils.deleteDir2(Utils.DOWNLOAD_PATH);
    }
}
