package com.activels.als.diyappmanager.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.activels.als.diyappmanager.R;
import com.activels.als.diyappmanager.db.DatasetDao;
import com.activels.als.diyappmanager.db.DatasetDaoImpl;
import com.activels.als.diyappmanager.entity.DatasetInfo;
import com.activels.als.diyappmanager.service.DownloadService;
import com.activels.als.diyappmanager.uihelper.ConfirmDialogHelper;
import com.activels.als.diyappmanager.utils.FileSizeUtil;
import com.activels.als.diyappmanager.utils.FileUtils;
import com.activels.als.diyappmanager.utils.NetworkUtil;
import com.activels.als.diyappmanager.utils.SharedPreferencesUtils;
import com.activels.als.diyappmanager.utils.StringUtil;
import com.activels.als.diyappmanager.utils.ToastUtil;
import com.activels.als.diyappmanager.utils.Utils;
import com.activels.als.diyappmanager.utils.ZipUtil;
import com.activels.als.diyappmanager.view.MyProgressBar;

import net.tsz.afinal.FinalBitmap;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by arvin.li on 2015/11/18.
 */
public class DatasetAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<DatasetInfo> datasetInfoList;
    private GridView gridView;

    private List<String> zipingDatasetList;

    private FinalBitmap fBitmap;

    public boolean isScrolling = false;//是否滑动中
    public int currentDeleteId = -1;//当前需要删除的dataset

    private DatasetDao mDatasetDao;
    private ConfirmDialogHelper dialog;

    public DatasetAdapter(Context context, List<DatasetInfo> datasetInfoList,
                          FinalBitmap fBitmap, GridView gridView) {
        this.context = context;
        this.datasetInfoList = datasetInfoList;
        this.fBitmap = fBitmap;
        this.gridView = gridView;

        mDatasetDao = new DatasetDaoImpl(context);
        zipingDatasetList = new LinkedList<>();

        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return datasetInfoList.size();
    }

    @Override
    public Object getItem(int i) {
        return datasetInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        final DatasetInfo datasetInfo = datasetInfoList.get(i);

        final ViewHolder viewHolder;

        if (view == null) {

            viewHolder = new ViewHolder();

            view = inflater.inflate(R.layout.dataset_item, null);
            viewHolder.numText = (TextView) view.findViewById(R.id.numText);
            viewHolder.iconImg = (ImageView) view.findViewById(R.id.iconImg);
            viewHolder.nameText = (TextView) view.findViewById(R.id.nameText);
            viewHolder.descText = (TextView) view.findViewById(R.id.descText);
            viewHolder.typeText = (TextView) view.findViewById(R.id.typeText);
            viewHolder.dateText = (TextView) view.findViewById(R.id.dateText);
            viewHolder.sizeText = (TextView) view.findViewById(R.id.sizeText);
            viewHolder.lockBtn = (ImageView) view.findViewById(R.id.lockBtn);
            viewHolder.operateBtn = (MyProgressBar) view.findViewById(R.id.operateBtn);
            viewHolder.checkBtn = (ToggleButton) view.findViewById(R.id.checkBtn);
            viewHolder.handleBtn = (Button) view.findViewById(R.id.handleBtn);

            view.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.handleBtn.setOnClickListener(new View.OnClickListener() {
            //ver2        viewHolder.operateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int state = datasetInfo.getOperateState();

                if (Utils.STATE_STOP == state) {//当前是未下载，下载

//                    viewHolder.lockBtn.setEnabled(true);
//                    if (datasetInfo.isLocked()) {
//                        viewHolder.lockBtn.setBackgroundResource(R.drawable.icon_locked);
//                    } else {
//                        viewHolder.lockBtn.setBackgroundResource(R.drawable.icon_unlocked);
//                    }

                    downloadHandle(datasetInfo, view);

                } else if (Utils.STATE_DOWNLOAGING == state) {//当前下载中，取消下载

                    //停止下载
//ver 2               Intent intent = new Intent(context, DownloadService.class);
//                    intent.setAction(Utils.ACTION_STOP);
//                    intent.putExtra("dataset", datasetInfo);
//                    context.startService(intent);
//
//                    datasetInfo.setOperateState(Utils.STATE_STOP);
//
//                    ((MyProgressBar) view).setText(context.getString(R.string.get_text));

                    //ver 1 删除下载
                    if (dialog == null) {
                        dialog = new ConfirmDialogHelper(context);
                        dialog.setBtnPressListener(new ConfirmDialogHelper.BtnPressListener() {
                            @Override
                            public void onYesBtnPressed() {
                                currentDeleteId = datasetInfo.getId();

                                if (listener != null)
                                    listener.absortDownload(datasetInfo);
                            }
                        });
                        dialog.setDialogText(context.getString(R.string.msg_abortdlownload_text));
                    }
                    dialog.setSubjectText(datasetInfo.getName());
                    dialog.show();

                } else if (Utils.STATE_UNZIPED == state) {//当前解压完成，预览

                    intentHandle(datasetInfo.getDatasetName(), datasetInfo.getTypeIndex());

                } else if (Utils.STATE_UPDATE == state) {//当前需更新，下载

                    //删除本地保存的旧记录
                    mDatasetDao.deleteDatasetByDatasetId(datasetInfo.getId());

                    //删除本地该dataset已下载文件
                    File datasetFile = new File(Utils.DOWNLOAD_PATH, datasetInfo.getDatasetName());
                    FileUtils.deleteDir(datasetFile.getAbsolutePath());

                    downloadHandle(datasetInfo, view);

//ver2                    ((MyProgressBar) view).setProgress(0);
                }

            }
        });

//        viewHolder.lockBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (datasetInfo.isLocked()) {
//                    datasetInfo.setIsLocked(false);
//                    viewHolder.lockBtn.setBackgroundResource(R.drawable.icon_unlocked);
//                } else {
//                    datasetInfo.setIsLocked(true);
//                    viewHolder.lockBtn.setBackgroundResource(R.drawable.icon_locked);
//                }
//            }
//        });

        viewHolder.numText.setText("" + (i + 1));
        viewHolder.nameText.setText(datasetInfo.getName());
        viewHolder.descText.setText(datasetInfo.getInfo());
        viewHolder.sizeText.setText(datasetInfo.getSize() + "");
        viewHolder.typeText.setText(context.getResources().getStringArray(R.array.type_arr)[Integer.parseInt(datasetInfo.getType())]);
        viewHolder.dateText.setText(datasetInfo.getCovertDate());
//ver2        viewHolder.operateBtn.setProgress(datasetInfo.getFinished());

//ver2        viewHolder.operateBtn.setTag(datasetInfo.getId() + "_operateBtn");
        viewHolder.sizeText.setTag(datasetInfo.getId() + "_sizeText");
        viewHolder.handleBtn.setTag(datasetInfo.getId() + "_handleBtn");

        viewHolder.checkBtn.setVisibility(View.GONE);
//ver2    viewHolder.operateBtn.setVisibility(View.VISIBLE);

//        if (datasetInfo.isCanDelete()) {
//            viewHolder.lockBtn.setEnabled(true);
//            if (datasetInfo.isLocked()) {
//                viewHolder.lockBtn.setBackgroundResource(R.drawable.icon_locked);
//            } else {
//                viewHolder.lockBtn.setBackgroundResource(R.drawable.icon_unlocked);
//            }
//        } else {
//            viewHolder.lockBtn.setEnabled(false);
//            viewHolder.lockBtn.setBackgroundResource(R.drawable.icon_locked_d);
//        }

        int state = datasetInfo.getOperateState();
        if (state == Utils.STATE_DOWNLOAGING) {//下载中
//ver2            viewHolder.operateBtn.setText(context.getString(R.string.downloading_text));
            viewHolder.handleBtn.setBackgroundResource(R.drawable.b04_pa02_downloading_01_selector);
        } else if (Utils.STATE_UNZIPING == state) {//下载完成-解压中
//ver2            viewHolder.operateBtn.setText(context.getString(R.string.decompression_text));
            viewHolder.handleBtn.setBackgroundResource(R.drawable.b04_pa02_downloading_01_selector);
            if (!isZiping(datasetInfo.getId())) {
                //解压操作
                DownloadService.executorService.execute(new ZipThread(datasetInfo.getId(), datasetInfo.getLink()));
            }
        } else if (Utils.STATE_UNZIPED == state) {//解压完成-预览
//ver2            viewHolder.operateBtn.setText(context.getString(R.string.preview_text));
            viewHolder.handleBtn.setBackgroundResource(R.drawable.b04_pa02_preview_01_selector);
        } else if (Utils.STATE_UPDATE == state) {//有更新-更新
//ver2            viewHolder.operateBtn.setText(context.getString(R.string.update_text));
            viewHolder.handleBtn.setBackgroundResource(R.drawable.b04_pa02_update_01_selector);
        } else {//默认为未下载-下载
//ver2            viewHolder.operateBtn.setText(context.getString(R.string.get_text));
            viewHolder.handleBtn.setBackgroundResource(R.drawable.b04_pa02_download_01_selector);
        }

        fBitmap.display(viewHolder.iconImg, Utils.ICON_DIR + datasetInfo.getIcon());

        return view;
    }

    static class ViewHolder {
        ImageView iconImg, lockBtn;
        TextView numText, nameText, descText, dateText, typeText, sizeText;
        MyProgressBar operateBtn;
        ToggleButton checkBtn;
        Button handleBtn;
    }

    /**
     * 是否正在解压
     *
     * @param datasestId
     * @return
     */
    private boolean isZiping(int datasestId) {
        for (String i : zipingDatasetList) {
            if (i.equals(datasestId + "")) {
                return true;
            }
        }

        return false;
    }

    /**
     * 打开第三方应用
     *
     * @param dataset
     * @param type
     */
    private void intentHandle(String dataset, int type) {

        File file = new File(Utils.DOWNLOAD_PATH, dataset);

        if (!file.exists()) {
            ToastUtil.toastShort(context, String.format(context.getString(R.string.file_not_exist), dataset));
            return;
        }

        try {
            Intent intent = new Intent();
            intent.setClassName(Utils.PACKAGENAME[type - 1], Utils.CLASSNAME[type - 1]);
            Bundle bundle = new Bundle();
            bundle.putString("dataset", Utils.DOWNLOAD_PATH + "/" + dataset + "/");
            bundle.putString("teacherId", SharedPreferencesUtils.getInstance(context, "").loadStringSharedPreference(Utils.LOGIN_USER_NAME));
            intent.putExtras(bundle);

            context.startActivity(intent);
        } catch (Exception e) {
            ToastUtil.toastShort(context, String.format(context.getString(R.string.install_app_first),
                    context.getResources().getStringArray(R.array.type_arr)[type]));
        }
    }

    /**
     * 更新显示数据
     *
     * @param deletedList
     */
    public void updateData(List<DatasetInfo> deletedList) {

        for (DatasetInfo info : deletedList) {
            DatasetInfo info1 = getDatasetByDatasetId(info.getId());
            info1.setOperateState(0);
            info1.setFinished(0);
            info1.setIsChecked(false);
            info1.setCanDelete(false);
            info1.setSize(info1.getZipSize());
        }

        notifyDataSetChanged();
    }

    /**
     * 下载处理
     *
     * @param info
     */
    private void downloadHandle(DatasetInfo info, View view) {

        //判断网络是否正常
        if (!NetworkUtil.isNetworkConnected(context)) {
            new AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.msg_dlerror_text))
                    .setPositiveButton(context.getString(R.string.sure_text), null)
                    .show();
            return;
        }

        //判断SD空间是否足够
        float[] block = StringUtil.getBlockFromSD();
        if (info.getTotalLength() > block[0]) {
            new AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.msg_nospace_text))
                    .setPositiveButton(context.getString(R.string.sure_text), null)
                    .show();
            return;
        }

        currentDeleteId = -1;

        //启动后台Service开始下载
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(Utils.ACTION_START);
        intent.putExtra("dataset", info);
        context.startService(intent);

        info.setOperateState(Utils.STATE_DOWNLOAGING);
        info.setCanDelete(true);//可以删除了

//ver2        ((MyProgressBar) view).setText(context.getString(R.string.downloading_text));
        ((Button) view).setBackgroundResource(R.drawable.b04_pa02_downloading_01_selector);
    }

    /**
     * 更新下载进度---ver2
     *
     * @param datasetInfo
     */
    public void updateProgress(DatasetInfo datasetInfo) {

        //滑动中，禁止更新进度
        if (isScrolling) {
            return;
        }

        //正执行删除操作，则不再更新进度
        if (datasetInfo.getId() == currentDeleteId) {
            return;
        }

        DatasetInfo info = getDatasetByDatasetId(datasetInfo.getId());
        if (info != null) {
            info.setFinished(datasetInfo.getFinished());

            try {
                MyProgressBar bar = (MyProgressBar) gridView.findViewWithTag(info.getId() + "_operateBtn");
                if (bar != null) {
                    bar.setProgress(datasetInfo.getFinished());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 下载完成处理
     *
     * @param datasetInfo
     */
    public void downloadCompleted(DatasetInfo datasetInfo) {

        if (datasetInfo.getId() == currentDeleteId) {
            return;
        }

        DatasetInfo info = getDatasetByDatasetId(datasetInfo.getId());
        if (info != null) {
            info.setFinished(100);
            info.setOperateState(datasetInfo.getOperateState());

//ver2            try {
//                MyProgressBar bar = (MyProgressBar) gridView.findViewWithTag(info.getId() + "_operateBtn");
//                if (bar != null) {
//                    bar.setProgress(datasetInfo.getFinished());
//                    bar.setText(context.getString(R.string.decompression_text));
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            if (!isZiping(datasetInfo.getId())) {
                //解压操作
                DownloadService.executorService.execute(new ZipThread(info.getId(), info.getLink()));
            }
        }
    }

    /**
     * 下载完成批处理
     *
     * @param infoList
     */
    public void downloadCompletedBatch(Set<DatasetInfo> infoList) {

        if (infoList.size() > 0) {
            for (DatasetInfo datasetInfo : infoList) {
                DatasetInfo info = getDatasetByDatasetId(datasetInfo.getId());
                if (info != null) {
                    info.setFinished(100);
                    info.setOperateState(datasetInfo.getOperateState());
                }
            }

            notifyDataSetChanged();
        }
    }

    /**
     * 获取dataset
     *
     * @param datasetId
     * @return
     */
    private DatasetInfo getDatasetByDatasetId(int datasetId) {

        for (DatasetInfo info : datasetInfoList) {
            if (info.getId() == datasetId) {
                return info;
            }
        }

        return null;
    }

    //解压完成处理
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 222) {
                final int datasetId = msg.arg1;

                final DatasetInfo info = getDatasetByDatasetId(datasetId);
                if (info != null) {
                    info.setOperateState(Utils.STATE_UNZIPED);
                    info.setSize(msg.arg2 == 0 ?
                            info.getZipSize() : msg.arg2 + "kb");

                    zipingDatasetList.remove(datasetId + "");

                    try {
//ver2                        MyProgressBar bar = (MyProgressBar) gridView.findViewWithTag(info.getId() + "_operateBtn");
//                        if (bar != null) {
//                            bar.setProgress(100);
//                            bar.setText(context.getString(R.string.preview_text));
//                        }

                        Button btn = (Button) gridView.findViewWithTag(info.getId() + "_handleBtn");
                        if (btn != null) {
                            btn.setBackgroundResource(R.drawable.b04_pa02_preview_01_selector);
                        }

                        TextView sizeText = (TextView) gridView.findViewWithTag(info.getId() + "_sizeText");
                        if (sizeText != null) {
                            sizeText.setText(info.getSize());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mDatasetDao.updateDataset(info);
                    mDatasetDao.updateDatasetSize(datasetId, info.getSize());

//                    notifyDataSetChanged();
                }
            }
        }
    };

    /**
     * 解压线程
     */
    class ZipThread extends Thread {

        private int datasetId;
        private String src, dest;

        public ZipThread(int datasetId, String link) {

            this.datasetId = datasetId;

            zipingDatasetList.add(datasetId + "");

            String zipName = link.substring(link.lastIndexOf("/") + 1);

            this.src = Utils.DOWNLOAD_PATH + "/" + zipName;
            this.dest = Utils.DOWNLOAD_PATH + "/" + zipName.substring(0, zipName.lastIndexOf("."));

        }

        @Override
        public void run() {

            File srcFile = new File(src);

            if (srcFile.exists()) {
                try {
                    ZipUtil.unzip(src, dest);

                    srcFile.delete();//删除压缩文件

                    int size = 0;
                    if (new File(dest).exists()) {
                        size = (int) FileSizeUtil.getFileOrFilesSize(dest, 2);//获取文件大小
                    }

                    Message msg = handler.obtainMessage();
                    msg.what = 222;
                    msg.arg1 = datasetId;
                    msg.arg2 = size;
                    handler.sendMessage(msg);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public IDatasetAdapterListener listener;

    public interface IDatasetAdapterListener {
        public void absortDownload(DatasetInfo info);
    }
}
