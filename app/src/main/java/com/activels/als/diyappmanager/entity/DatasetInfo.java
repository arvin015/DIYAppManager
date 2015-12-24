package com.activels.als.diyappmanager.entity;

import com.activels.als.diyappmanager.utils.StringUtil;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Dataset信息实体类
 * <p/>
 * Created by arvin.li on 2015/11/11.
 */
public class DatasetInfo implements Serializable {

    private int id;       //ID
    private String name;  //名字
    private String icon;  //图标名
    private String info;  //描述
    private String type;  //类型
    private String date;  //时间
    private String link;  //下载链接
    private String md5;   //MD5
    private String size;  //大小
    private String zipSize;//压缩的大小
    private String covertDate;//格式化时间

    private int typeIndex;//类型索引
    private int operateState; //当前状态 0：未下载；1：下载中；2：下载完成，解压中；3：解压完成；4：有更新
    private int finished;//下载完成百分比
    private int totalLength;//总长度
    private boolean canDelete = false;//是否可执行删除操作
    private boolean isLocked = false;//是否上锁了
    private boolean isChecked = false;//是否选中
    private String zipDatasetName; //dataset压缩名字
    private String datasetName;//dataset名字

    public DatasetInfo() {
    }

    public DatasetInfo(JSONObject json) {
        if (json == null) {
            return;
        }

        this.id = json.optInt("id");
        this.name = json.optString("name");
        this.icon = json.optString("icon");
        this.info = json.optString("info");
        this.date = json.optString("date");
        this.type = json.optInt("type") + "";
        this.link = json.optString("link");
        this.md5 = json.optString("md5");
        this.totalLength = json.optInt("size");
        this.size = StringUtil.bytes2kb(json.optInt("size"));
        this.zipSize = this.size;

        this.typeIndex = json.optInt("type");
        this.covertDate = StringUtil.convertTimeStumpToDate5(date);
        this.zipDatasetName = link.substring(link.lastIndexOf("/") + 1);
        this.datasetName = zipDatasetName.substring(0, zipDatasetName.lastIndexOf("."));
    }

    public DatasetInfo(int id, String date, int operateState, int finished, String icon,
                       String name, String info, String type, String size, String link) {
        this.id = id;
        this.date = date;
        this.operateState = operateState;
        this.finished = finished;
        this.icon = icon;
        this.name = name;
        this.info = info;
        this.type = type;
        this.size = size;
        this.link = link;

        this.covertDate = StringUtil.convertTimeStumpToDate5(date);
        this.zipDatasetName = link.substring(link.lastIndexOf("/") + 1);
        this.datasetName = zipDatasetName.substring(0, zipDatasetName.lastIndexOf("."));
    }

    public DatasetInfo(int id, String date, int operateState, int finished, String size) {
        this.id = id;
        this.date = date;
        this.operateState = operateState;
        this.finished = finished;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getCovertDate() {
        return covertDate;
    }

    public void setCovertDate(String covertDate) {
        this.covertDate = covertDate;
    }

    public int getOperateState() {
        return operateState;
    }

    public void setOperateState(int operateState) {
        this.operateState = operateState;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public String getZipDatasetName() {
        return zipDatasetName;
    }

    public void setZipDatasetName(String zipDatasetName) {
        this.zipDatasetName = zipDatasetName;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public void setTypeIndex(int typeIndex) {
        this.typeIndex = typeIndex;
    }

    public String getZipSize() {
        return zipSize;
    }

    public void setZipSize(String zipSize) {
        this.zipSize = zipSize;
    }

    /**
     * 排序
     *
     * @param datasetInfoList
     * @param type
     */
    public static void sortDatasetList(List<DatasetInfo> datasetInfoList, final int type) {
        Collections.sort(datasetInfoList, new Comparator<DatasetInfo>() {
            @Override
            public int compare(DatasetInfo datasetInfo, DatasetInfo t1) {

                int result;

                if (type == 1) {//根据App Name排序

                    Collator collator = Collator.getInstance(Locale.CHINA);//中文排序
                    result = collator.compare(datasetInfo.getName(), t1.getName());

                } else {//根据Last Modified Date排序

                    result = datasetInfo.getDate().compareTo(t1.getDate());
                }

                return result;
            }
        });
    }
}
