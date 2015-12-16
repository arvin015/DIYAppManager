package com.activels.als.diyappmanager.utils;

import android.os.Environment;

/**
 * Created by arvin.li on 2015/11/11.
 */
public class Utils {

    public static String httpip = "http://www.skycookbook.sinaapp.com/";

    public static String ICON_DIR = httpip + "Public/Icons/";

    //datasetinfo
    public static String url_listdataset = httpip + "index.php/Dataset/getpreviewappsinfo";

    public static void UPDATEPATH() {
        ICON_DIR = httpip + "Public/Icons/";
        url_listdataset = httpip + "index.php/Dataset/getpreviewappsinfo";
    }

    public static final String ACTION_START = "ACTION_START";            //开始下载
    public static final String ACTION_STOP = "ACTION_STOP";              //停止下载
    public static final String ACTION_DELETE_ONE = "ACTION_DELETE_ONE";  //删除下载
    public static final String ACTION_UPDATE = "ACTION_UPDATE";          //更新下载进度
    public static final String ACTION_QUIT = "ACTION_QUIT";              //退出应用
    public static final String ACTION_STOP_ALL_UNLOCKED = "ACTION_STOP_ALL_UNLOCKED";      //停止所有未上锁下载
    public static final String DOWNLOAD_COMPLETED = "DOWNLOAD_COMPLETED";//下载完成

    public static final int STATE_STOP = 0;//未下载，停止
    public static final int STATE_DOWNLOAGING = 1;//下载中
    public static final int STATE_UNZIPING = 2;//下载完成，解压中
    public static final int STATE_UNZIPED = 3;//解压完成，预览
    public static final int STATE_UPDATE = 4;//需更新，更新

    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/downloads";

//    public static final String[] TYPES = new String[]{"All App Types", "eBook",
//            "Draw Something", "Card Send", "Video Quiz"};

//    public static final String[] SORTS = new String[]{
//            "Last Modified Date", "App Name"
//    };

    public static final String LOGIN_USER_NAME = "userName";
    public static final String LOGIN_PSD = "psd";
    public static final String AUTO_LOGIN = "auto_login";
    public static final String SCHOOL_NAME = "school_name";
    public static final String PATH_NAME = "path_name";

    public static final String token = "2000000";
    public static final int count = 14;   //每页显示的数量

    public static final String[] PACKAGENAME = {"com.Accentrix.eTextBook_preview", "com.activels.als.drawcard_preview",
            "com.yzc.teacher.cardsend_preview", "org.astri.mclp.app.teacher.mclpvideoquestion_preview"};
    public static final String[] CLASSNAME = {"com.Accentrix.eTextBook_preview.ArticleActivity", "com.activels.als.drawcard_preview.DrawCardActivity",
            "com.yzc.teacher.cardsend_preview.CardSendEntryActivity", "org.astri.mclp.app.teacher.mclpvideoquestion_preview.EntryActivity"};

}
