package com.activels.als.diyappmanager.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.activels.als.diyappmanager.entity.DatasetInfo;
import com.activels.als.diyappmanager.utils.Utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
        db.close();
    }

    @Override
    public synchronized void updateDataset(DatasetInfo datasetInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("update dataset_info set finished=?, state=? where dataset_id=?", new Object[]{datasetInfo.getFinished(),
                datasetInfo.getOperateState(), datasetInfo.getId()});
        db.close();
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
                    cursor.getInt(cursor.getColumnIndex("finished"))
            );

            db.close();

            return info;
        }

        db.close();

        return null;
    }

    @Override
    public synchronized void deleteDatasetByDatasetId(int datasetId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("delete from dataset_info where dataset_id=?", new String[]{"" + datasetId});
        db.close();
    }

    @Override
    public List<DatasetInfo> getAllDownloadedDataset() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from dataset_info where state > ?", new String[]{Utils.STATE_DOWNLOAGING + ""});

        List<DatasetInfo> infoList = new CopyOnWriteArrayList<>();

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

        db.close();

        return infoList;
    }
}
