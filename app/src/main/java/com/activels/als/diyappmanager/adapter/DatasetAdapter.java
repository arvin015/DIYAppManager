package com.activels.als.diyappmanager.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.activels.als.diyappmanager.R;
import com.activels.als.diyappmanager.db.DatasetDaoImpl;
import com.activels.als.diyappmanager.entity.DatasetInfo;
import com.activels.als.diyappmanager.service.DownloadService;
import com.activels.als.diyappmanager.utils.ToastUtil;
import com.activels.als.diyappmanager.utils.Utils;
import com.activels.als.diyappmanager.utils.ZipUtil;
import com.activels.als.diyappmanager.view.MyProgressBar;

import net.tsz.afinal.FinalBitmap;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by arvin.li on 2015/11/18.
 */
public class DatasetAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<DatasetInfo> datasetInfoList;

    private FinalBitmap fBitmap;

    public boolean isScrolling = false;//是否滑动中
    public boolean isDeleteAll = false;//是否执行了删除所有操作
    public int currentDeleteId = -1;//当前需要删除的dataset

    public DatasetAdapter(Context context, List<DatasetInfo> datasetInfoList,
                          FinalBitmap fBitmap) {
        this.context = context;
        this.datasetInfoList = datasetInfoList;
        this.fBitmap = fBitmap;

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
            viewHolder.iconImg = (ImageView) view.findViewById(R.id.iconImg);
            viewHolder.nameText = (TextView) view.findViewById(R.id.nameText);
            viewHolder.descText = (TextView) view.findViewById(R.id.descText);
            viewHolder.typeText = (TextView) view.findViewById(R.id.typeText);
            viewHolder.dateText = (TextView) view.findViewById(R.id.dateText);
            viewHolder.sizeText = (TextView) view.findViewById(R.id.sizeText);
            viewHolder.lockBtn = (ImageView) view.findViewById(R.id.lockBtn);
            viewHolder.operateBtn = (MyProgressBar) view.findViewById(R.id.operateBtn);
            viewHolder.checkBtn = (ToggleButton) view.findViewById(R.id.checkBtn);

            view.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.operateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int state = datasetInfo.getOperateState();

                if (Utils.STATE_STOP == state) {//当前是未下载，下载

                    viewHolder.lockBtn.setEnabled(true);
                    if (datasetInfo.isLocked()) {
                        viewHolder.lockBtn.setBackgroundResource(R.drawable.icon_locked);
                    } else {
                        viewHolder.lockBtn.setBackgroundResource(R.drawable.icon_unlocked);
                    }

                    downloadHandle(datasetInfo, view);

                } else if (Utils.STATE_DOWNLOAGING == state) {//当前下载中，停止

                    //停止下载
                    Intent intent = new Intent(context, DownloadService.class);
                    intent.setAction(Utils.ACTION_STOP);
                    intent.putExtra("dataset", datasetInfo);
                    context.startService(intent);

                    datasetInfo.setOperateState(Utils.STATE_STOP);

                    ((MyProgressBar) view).setText(context.getString(R.string.get_text));

                } else if (Utils.STATE_UNZIPED == state) {//当前解压完成，预览

                    intentHandle(datasetInfo.getDatasetName(), datasetInfo.getType());

                } else if (Utils.STATE_UPDATE == state) {//当前需更新，下载

                    //删除本地保存的旧记录
                    new DatasetDaoImpl(context).deleteDatasetByDatasetId(datasetInfo.getId());

                    downloadHandle(datasetInfo, view);

                    ((MyProgressBar) view).setProgress(0);
                }

            }
        });

        viewHolder.lockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (datasetInfo.isLocked()) {
                    datasetInfo.setIsLocked(false);
                    viewHolder.lockBtn.setBackgroundResource(R.drawable.icon_unlocked);
                } else {
                    datasetInfo.setIsLocked(true);
                    viewHolder.lockBtn.setBackgroundResource(R.drawable.icon_locked);
                }
            }
        });

        viewHolder.nameText.setText(datasetInfo.getName());
        viewHolder.descText.setText(datasetInfo.getInfo());
        viewHolder.sizeText.setText(datasetInfo.getSize() + "");
        viewHolder.typeText.setText(datasetInfo.getType());
        viewHolder.dateText.setText(datasetInfo.getCovertDate());
        viewHolder.operateBtn.setProgress(datasetInfo.getFinished());

        viewHolder.checkBtn.setVisibility(View.GONE);
        viewHolder.operateBtn.setVisibility(View.VISIBLE);

        if (datasetInfo.isCanDelete()) {
            viewHolder.lockBtn.setEnabled(true);
            if (datasetInfo.isLocked()) {
                viewHolder.lockBtn.setBackgroundResource(R.drawable.icon_locked);
            } else {
                viewHolder.lockBtn.setBackgroundResource(R.drawable.icon_unlocked);
            }
        } else {
            viewHolder.lockBtn.setEnabled(false);
            viewHolder.lockBtn.setBackgroundResource(R.drawable.icon_locked_d);
        }

        int state = datasetInfo.getOperateState();
        if (state == Utils.STATE_DOWNLOAGING) {//下载中-停止
            viewHolder.operateBtn.setText(context.getString(R.string.stop_text));
        } else if (Utils.STATE_UNZIPING == state) {//下载完成-解压中
            viewHolder.operateBtn.setText(context.getString(R.string.decompression_text));

            //解压操作
            new ZipThread(datasetInfo.getId(), datasetInfo.getLink()).start();

        } else if (Utils.STATE_UNZIPED == state) {//解压完成-预览
            viewHolder.operateBtn.setText(context.getString(R.string.preview_text));
        } else if (Utils.STATE_UPDATE == state) {//有更新-更新
            viewHolder.operateBtn.setText(context.getString(R.string.update_text));
        } else {//默认为未下载-下载
            viewHolder.operateBtn.setText(context.getString(R.string.get_text));
        }

        fBitmap.display(viewHolder.iconImg, Utils.ICON_DIR + datasetInfo.getIcon());

        return view;
    }

    static class ViewHolder {
        ImageView iconImg, lockBtn;
        TextView nameText, descText, dateText, typeText, sizeText;
        MyProgressBar operateBtn;
        ToggleButton checkBtn;
    }

    /**
     * 打开第三方应用
     *
     * @param dataset
     * @param type
     */
    private void intentHandle(String dataset, String type) {

        File file = new File(Utils.DOWNLOAD_PATH, dataset);

        if (!file.exists()) {
            ToastUtil.toastShort(context, String.format(context.getString(R.string.file_not_exist), dataset));
            return;
        }

        int t = Integer.parseInt(type);

        try {
            Intent intent = new Intent();
            intent.setClassName(Utils.PACKAGENAME[t - 1], Utils.CLASSNAME[t - 1]);
            Bundle bundle = new Bundle();
            bundle.putString("dataset", Utils.DOWNLOAD_PATH + "/" + dataset + "/");
            intent.putExtras(bundle);

            context.startActivity(intent);
        } catch (Exception e) {
            ToastUtil.toastShort(context, String.format(context.getString(R.string.install_app_first), Utils.TYPES[t]));
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
        }

        notifyDataSetChanged();
    }

    /**
     * 下载处理
     *
     * @param info
     */
    private void downloadHandle(DatasetInfo info, View view) {

        isDeleteAll = false;//更新可正常运行
        currentDeleteId = -1;

        //启动后台Service开始下载
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(Utils.ACTION_START);
        intent.putExtra("dataset", info);
        context.startService(intent);

        info.setOperateState(Utils.STATE_DOWNLOAGING);
        info.setCanDelete(true);//可以删除了

        ((MyProgressBar) view).setText(context.getString(R.string.stop_text));
    }

    /**
     * 更新下载进度
     *
     * @param datasetInfo
     */
    public void updateProgress(DatasetInfo datasetInfo) {

        //滑动中，禁止更新进度
        if (isScrolling) {
            return;
        }

        //正执行删除操作，则不再更新进度
        if (isDeleteAll ||
                datasetInfo.getId() == currentDeleteId) {
            return;
        }

        DatasetInfo info = getDatasetByDatasetId(datasetInfo.getId());
        if (info != null) {
            info.setFinished(datasetInfo.getFinished());

            notifyDataSetChanged();
        }
    }

    /**
     * 下载完成处理
     *
     * @param datasetInfo
     */
    public void downloadCompleted(DatasetInfo datasetInfo) {

        if (isDeleteAll ||
                datasetInfo.getId() == currentDeleteId) {
            return;
        }

        DatasetInfo info = getDatasetByDatasetId(datasetInfo.getId());
        if (info != null) {
            info.setFinished(datasetInfo.getFinished());
            info.setOperateState(datasetInfo.getOperateState());

            notifyDataSetChanged();
        }
    }

    /**
     * 下载完成批处理
     *
     * @param infoList
     */
    public void downloadCompletedBatch(Set<DatasetInfo> infoList) {
        if (isDeleteAll) {
            return;
        }

        if (infoList.size() > 0) {
            for (DatasetInfo datasetInfo : infoList) {
                DatasetInfo info = getDatasetByDatasetId(datasetInfo.getId());
                if (info != null) {
                    info.setFinished(datasetInfo.getFinished());
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

    /**
     * 重设列表
     */
    public void resetDatasetList() {

        isDeleteAll = true;

        for (DatasetInfo info : datasetInfoList) {

            if (!info.isLocked()) {
                info.setOperateState(Utils.STATE_STOP);
                info.setFinished(0);
                info.setCanDelete(false);
            }

        }

        notifyDataSetChanged();
    }

    //解压完成处理
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 222) {
                int datasetId = msg.arg1;

                DatasetInfo info = getDatasetByDatasetId(datasetId);
                if (info != null) {
                    info.setOperateState(Utils.STATE_UNZIPED);

                    notifyDataSetChanged();
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

                    Message msg = handler.obtainMessage();
                    msg.what = 222;
                    msg.arg1 = datasetId;
                    handler.sendMessage(msg);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}