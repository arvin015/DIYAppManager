package com.activels.als.diyappmanager.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.activels.als.diyappmanager.db.DatasetDao;
import com.activels.als.diyappmanager.db.DatasetDaoImpl;
import com.activels.als.diyappmanager.entity.DatasetInfo;
import com.activels.als.diyappmanager.utils.Utils;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 下载Service
 * <p/>
 * Created by arvin.li on 2015/11/5.
 */
public class DownloadService extends Service {

    private static final int GET_SUCCESS = 10000;

    private DatasetDao mDatasetDao = new DatasetDaoImpl(this);

    private Map<Integer, DownloadTask> downloadTasks = new ConcurrentHashMap<>();//文件下载任务哈希集合

    //    public static ExecutorService executorService = Executors.newCachedThreadPool();//带缓存的线程池
    public static ExecutorService executorService = Executors.newFixedThreadPool(6);//最多留个线程并发，多余的排队等待

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == GET_SUCCESS) {
                //设置文件总长度
                final DatasetInfo datasetInfo = (DatasetInfo) msg.obj;

                //开始下载
                DownloadTask downloadTask = new DownloadTask(DownloadService.this, datasetInfo, 3,
                        new DownloadTask.IDownloadTaskListener() {
                            @Override
                            public void downloadCompleted(DatasetInfo info) {//下载完成
                                downloadTasks.remove(info.getId());

                                mDatasetDao.updateDataset(info);
                            }
                        });
                downloadTask.download();

                mDatasetDao.insertDataset(datasetInfo);//将dataset下载记录保存到本地数据库

                downloadTasks.put(datasetInfo.getId(), downloadTask);
            }
        }
    };

    /**
     * 接收Activity传递的参数
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            return -2;
        }

        String action = intent.getAction();

        if (Utils.ACTION_QUIT.equals(action)) {

            stopAllTask();

        } else if (Utils.ACTION_STOP_ALL_UNLOCKED.equals(action)) {

            ArrayList datasetList = (ArrayList) intent.getSerializableExtra("datasetList");

            stopUnLockedTask(datasetList);

        } else {
            DatasetInfo datasetInfo = (DatasetInfo) intent.getSerializableExtra("dataset");

            DownloadTask currentTask = null;

            if (downloadTasks.containsKey(datasetInfo.getId())) {
                currentTask = downloadTasks.get(datasetInfo.getId());
            }

            if (Utils.ACTION_START.equals(action)) {//开始下载

                if (currentTask == null) {
                    executorService.execute(new MyThread(datasetInfo));//启动初始化线程，获取文件长度，并在本地创建相应大小的保存文件
                } else {
                    currentTask.download();
                }

            } else if (Utils.ACTION_STOP.equals(action)) {//停止下载

                if (currentTask != null) {
                    currentTask.isPause = true;
                }
            } else if (Utils.ACTION_DELETE_ONE.equals(action)) {//删除某一下载
                if (currentTask != null) {
                    currentTask.isPause = true;

                    downloadTasks.remove(datasetInfo.getId());
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 停止所有下载任务并保存下载信息到本地
     */
    public void stopAllTask() {

        for (Integer key : downloadTasks.keySet()) {
            DownloadTask downloadTask = downloadTasks.get(key);

            mDatasetDao.updateDataset(downloadTask.datasetInfo);

            downloadTask.isPause = true;

        }

        downloadTasks.clear();
    }

    /**
     * 停止所有未上锁的下载任务
     *
     * @param infoList
     */
    public void stopUnLockedTask(ArrayList<DatasetInfo> infoList) {

        for (Integer key : downloadTasks.keySet()) {

            DownloadTask downloadTask = downloadTasks.get(key);

            DatasetInfo i = getDatasetById(infoList, downloadTask.datasetInfo.getId());

            if (!i.isLocked()) {
                downloadTask.isPause = true;
                downloadTasks.remove(key);
            }
        }

    }

    /**
     * 根据ID获取dataset
     *
     * @param datasetId
     * @return
     */
    public DatasetInfo getDatasetById(ArrayList<DatasetInfo> infoList, int datasetId) {
        for (DatasetInfo info : infoList) {
            if (info.getId() == datasetId) {
                return info;
            }
        }

        return null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 初始化线程---获取文件长度，并在本地创建相应大小的保存文件
     */
    class MyThread extends Thread {

        private DatasetInfo datasetInfo;

        public MyThread(DatasetInfo datasetInfo) {
            this.datasetInfo = datasetInfo;
        }

        @Override
        public void run() {

            HttpURLConnection conn = null;
            RandomAccessFile raf = null;

            try {

                //连接网络
                URL url = new URL(datasetInfo.getLink());

                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10 * 1000);
                conn.setRequestMethod("GET");

                int length = 0;

                //响应成功
                if (conn.getResponseCode() == HttpStatus.SC_OK) {

                    //获取文件总长度
                    length = conn.getContentLength();
                }

                if (length <= 0) {
                    return;
                }

                File dir = new File(Utils.DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File saveFile = new File(dir,
                        datasetInfo.getLink().substring(datasetInfo.getLink().lastIndexOf("/") + 1));

                //创建可任意写入的本地保存文件，并设置长度
                raf = new RandomAccessFile(saveFile, "rwd");
                raf.setLength(length);

                datasetInfo.setTotalLength(length);

                Message msg = Message.obtain();
                msg.what = GET_SUCCESS;
                msg.obj = datasetInfo;
                handler.sendMessage(msg);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    conn.disconnect();
                    if (raf != null) {
                        raf.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
