package com.activels.als.diyappmanager.db;


import com.activels.als.diyappmanager.entity.DatasetInfo;

import java.util.List;

/**
 * 文件数据访问接口
 * <p/>
 * Created by Administrator on 2015/11/8.
 */
public interface DatasetDao {

    /**
     * 插入文件信息
     *
     * @param datasetInfo
     */
    public void insertDataset(DatasetInfo datasetInfo);

    /**
     * 更新下载完成进度，及是否完成状态
     *
     * @param datasetInfo
     */
    public void updateDataset(DatasetInfo datasetInfo);

    /**
     * 更新大小
     *
     * @param datasetId
     * @param size
     */
    public void updateDatasetSize(int datasetId, String size);

    /**
     * 查询执行文件ID的文件信息
     *
     * @param datasetId
     * @return
     */
    public DatasetInfo selectDatasetByDatasetId(int datasetId);

    /**
     * 删除文件下载记录
     *
     * @param datasetId
     */
    public void deleteDatasetByDatasetId(int datasetId);

    /**
     * 删除所有下载中的dataset记录 ver 1
     */
    public void deleteAllDownloadingDataset();

    /**
     * 获取所有已下载完成的dataset
     *
     * @param type
     * @return
     */
    public List<DatasetInfo> getAllDownloadedDataset(String type);
}
