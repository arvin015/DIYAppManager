package com.activels.als.diyappmanager.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.activels.als.diyappmanager.entity.DatasetInfo;
import com.activels.als.diyappmanager.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件数据访问接口实现类
 * <p/>
 * Created by Administrator on 2015/11/8.
 */
public class DatasetDaoImpl implements DatasetDao {

    private DBHelper dbHelper;

    public DatasetDaoImpl(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }

    @Override
    public synchronized void insertDataset(DatasetInfo datasetInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.execSQL("insert into dataset_info(dataset_id, date, finished, state, icon, name, info, type, size, link)" +
                " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{datasetInfo.getId(), datasetInfo.getDate(), datasetInfo.getFinished(),
                datasetInfo.getOperateState(), datasetInfo.getIcon(), datasetInfo.getName(), datasetInfo.getInfo(),
                datasetInfo.getType(), datasetInfo.getSize(), datasetInfo.getLink()});
//        db.close();
    }

    @Override
    public synchronized void updateDataset(DatasetInfo datasetInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("update dataset_info set finished=?, state=?, size=? where dataset_id=?", new Object[]{datasetInfo.getFinished(),
                datasetInfo.getOperateState(), datasetInfo.getSize(), datasetInfo.getId()});
//        db.close();
    }

    @Override
    public synchronized void updateDatasetSize(int datasetId, String size) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("update dataset_info set size=? where dataset_id=?", new Object[]{size, datasetId});
//        db.close();
    }

    @Override
    public DatasetInfo selectDatasetByDatasetId(int datasetId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from dataset_info where dataset_id = ?", new String[]{datasetId + ""});

        if (cursor.moveToNext()) {

            DatasetInfo info = new DatasetInfo(
                    cursor.getInt(cursor.getColumnIndex("dataset_id")),
                    cursor.getString(cursor.getColumnIndex("date")),
                    cursor.getInt(cursor.getColumnIndex("state")),
                    cursor.getInt(cursor.getColumnIndex("finished")),
                    cursor.getString(cursor.getColumnIndex("size"))
            );

            return info;
        }

        cursor.close();
//        db.close();

        return null;
    }

    @Override
    public synchronized void deleteDatasetByDatasetId(int datasetId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("delete from dataset_info where dataset_id=?", new String[]{"" + datasetId});
//        db.close();
    }

    @Override
    public synchronized void deleteAllDownloadingDataset() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("delete from dataset_info where state < ?", new String[]{"" + Utils.STATE_UNZIPED});
//        db.close();
    }

    @Override
    public List<DatasetInfo> getAllDownloadedDataset(String type) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor;

        if ((0 + "").equals(type)) {
            cursor = db.rawQuery("select * from dataset_info where state > ?", new String[]{Utils.STATE_DOWNLOAGING + ""});
        } else {
            cursor = db.rawQuery("select * from dataset_info where state > ? and type = ?", new String[]{Utils.STATE_DOWNLOAGING + "", type});
        }

        List<DatasetInfo> infoList = new ArrayList<>();

        while (cursor.moveToNext()) {

            DatasetInfo info = new DatasetInfo(
                    cursor.getInt(cursor.getColumnIndex("dataset_id")),
                    cursor.getString(cursor.getColumnIndex("date")),
                    cursor.getInt(cursor.getColumnIndex("state")),
                    cursor.getInt(cursor.getColumnIndex("finished")),
                    cursor.getString(cursor.getColumnIndex("icon")),
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("info")),
                    cursor.getString(cursor.getColumnIndex("type")),
                    cursor.getString(cursor.getColumnIndex("size")),
                    cursor.getString(cursor.getColumnIndex("link"))
            );

            infoList.add(info);
        }

        cursor.close();
//        db.close();

        return infoList;
    }
}
