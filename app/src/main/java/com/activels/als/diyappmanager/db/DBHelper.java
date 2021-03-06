package com.activels.als.diyappmanager.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类---单例模式
 * <p/>
 * Created by arvin.li on 2015/11/5.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static String name = "download.db";

    private static int version = 1;

    private static DBHelper dbHelper;

    private static final String SQL_CREATE = "create table thread_info(_id integer primary key autoincrement," +
            "thread_id integer, url text, start integer, end integer, finished integer)";
    private static final String SQL_DROP = "drop table if exists thread_info";

    private static final String SQL_CREATE1 = "create table dataset_info(_id integer primary key autoincrement, " +
            "dataset_id integer, date text, finished integer, state integer, icon text, name text, info text, type text, size text, link text)";
    private static final String SQL_DROP1 = "drop table if exists dataset_info";

    private DBHelper(Context context) {
        super(context, name, null, version);
    }

    public static DBHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new DBHelper(context);
        }

        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(SQL_CREATE);

        sqLiteDatabase.execSQL(SQL_CREATE1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DROP);
        sqLiteDatabase.execSQL(SQL_CREATE);

        sqLiteDatabase.execSQL(SQL_DROP1);
        sqLiteDatabase.execSQL(SQL_CREATE1);
    }
}
