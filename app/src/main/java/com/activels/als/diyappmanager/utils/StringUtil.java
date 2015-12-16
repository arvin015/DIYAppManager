package com.activels.als.diyappmanager.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;
import android.text.Html;
import android.text.Spanned;
import android.text.format.Formatter;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhouxin@easier.cn
 * @version V1.0
 * @Title: StringUtil.java
 * @Package com.fullteem.utils
 * @Description: 字符串的处理类
 * @date 2012-11-22 下午4:35
 */
public class StringUtil {
    private final static String[] CHINA_NUMBER = {"一", "二", "三", "四", "五",
            "六", "七", "八", "九", "十"};

    /**
     * 判断是否为null或空值
     *
     * @param str String
     * @return true or false
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().length() == 0 || str.endsWith("null");
    }

    /**
     * 判断str1和str2是否相同
     *
     * @param str1 str1
     * @param str2 str2
     * @return true or false
     */
    public static boolean equals(String str1, String str2) {
        return str1 == str2 || str1 != null && str1.equals(str2);
    }

    /**
     * 判断str1和str2是否相同(不区分大小写)
     *
     * @param str1 str1
     * @param str2 str2
     * @return true or false
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 != null && str1.equalsIgnoreCase(str2);
    }

    /**
     * 只允许字母、数字、下划线
     */
    public static boolean iscontainSpecialCharacter(String str) {
        return !str.matches("[A-Za-z0-9_]+");
    }

    /**
     * 判断字符串str1是否包含字符串str2
     *
     * @param str1 源字符串
     * @param str2 指定字符串
     * @return true源字符串包含指定字符串，false源字符串不包含指定字符串
     */
    public static boolean contains(String str1, String str2) {
        return str1 != null && str1.contains(str2);
    }

    /**
     * 判断字符串是否为空，为空则返回一个空值，不为空则返回原字符串
     *
     * @param str 待判断字符串
     * @return 判断后的字符串
     */
    public static String getString(String str) {
        return str == null ? "" : str;
    }

    private final static Pattern emailer = Pattern
            .compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
    public final static SimpleDateFormat dateFormater = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    private final static SimpleDateFormat dateFormater2 = new SimpleDateFormat(
            "yyyy-MM-dd");
    private final static SimpleDateFormat dateFormater3 = new SimpleDateFormat(
            "HH:mm");
    public final static SimpleDateFormat dateFormater4 = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyyMMddHHmmss", Locale.CHINA);

    /**
     * 将字符串转位日期类型
     *
     * @param sdate
     * @return
     */
    public static Date toDate(String sdate) {
        try {
            return dateFormater.parse(sdate);
        } catch (ParseException e) {
            try {
                return dateFormater.parse(new Date(toLong(sdate) * 1000L)
                        .toGMTString());
            } catch (ParseException e1) {
                return null;
            }
        }
    }

    // 中文转Unicode
    public static String string2unicode(String str) {
        String result = "";
        for (int i = 0; i < str.length(); i++) {
            int chr1 = (char) str.charAt(i);
            if (chr1 >= 19968 && chr1 <= 171941) {// 汉字范围 \u4e00-\u9fa5 (中文)
                result += "\\u" + Integer.toHexString(chr1);
            } else {
                result += str.charAt(i);
            }
        }
        return result;
    }

    // Unicode转中文
    public static String unicode2String(String unicodeStr) {
        StringBuffer sb = new StringBuffer();
        String str[] = unicodeStr.toUpperCase().split("U");
        for (int i = 0; i < str.length; i++) {
            if (str[i].equals(""))
                continue;
            char c = (char) Integer.parseInt(str[i].trim(), 16);
            sb.append(c);
        }
        return sb.toString();
    }

    // 判断字符串是否包含中文
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    // 去除字符串空格
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    public static String convertTimeStumpToDate(String time) {
        try {
            return dateFormater.format(new Date(toLong(time) * 1000L));
        } catch (Exception e) {
            return null;
        }
    }

    public static String convertTimeStumpToDate2(String time) {
        try {
            return dateFormater2.format(new Date(toLong(time) * 1000L));
        } catch (Exception e) {
            return "";
        }

    }

    public static String gettimenow() {
        //Date curDate  = new Date(System.currentTimeMillis());//获取当前时间
        String str = dateFormater.format(new Date(System.currentTimeMillis()));
        return str;
    }

    public static Date ConverToDate(String strDate) throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.parse(strDate);
    }

    /**
     * 以友好的方式显示时间
     *
     * @param sdate
     * @return
     */
    public static String friendly_time(String sdate) {
        //		Calendar post = Calendar.getInstance();
        //		post.setTimeInMillis(toLong(sdate) * 1000L);
        //		Date time = post.getTime();

        Date time = toDate(sdate);
        //		try
        //		{
        //			time = ConverToDate(sdate);
        //		}
        //		catch (Exception e) {
        //			// TODO: handle exception
        //		}

        String ftime = "";
        Calendar cal = Calendar.getInstance();

        // 判断是否是同一天
        String curDate = dateFormater2.format(cal.getTime());
        String paramDate = dateFormater2.format(time);
        if (curDate.equals(paramDate)) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0)
                ftime = Math.max(
                        (cal.getTimeInMillis() - time.getTime()) / 60000, 1)
                        + "分钟前";
            else
                ftime = hour + "小时前";
            return ftime;
        }

        long lt = time.getTime() / 86400000;
        long ct = cal.getTimeInMillis() / 86400000;
        int days = (int) (ct - lt);
        if (days == 0) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0)
                ftime = Math.max(
                        (cal.getTimeInMillis() - time.getTime()) / 60000, 1)
                        + "分钟前";
            else
                ftime = hour + "小时前";
        } else if (days == 1) {
            ftime = "昨天" + dateFormater3.format(time);
        } else if (days == 2) {
            ftime = "前天" + dateFormater3.format(time);
        } else if (days > 2 && days <= 10) {
            ftime = days + "天前";
        } else if (days > 10) {
            ftime = dateFormater2.format(time);
        }
        return ftime;
    }

    /**
     * 以友好的方式显示时间
     *
     * @param time
     * @return
     */
    public static String getChineseTime(long time) {
        if (time == 604800000) {
            return "一周";
        } else if (time == 86400000) {
            return "一天";
        }
        int yearCount = 0;
        int monthCount = 0;
        int dayCount = 0;
        int hourCount = 0;
        int minuteCount = 0;
        int secondCount = 0;
        yearCount = (int) time / (86400000 * 365);
        time = (int) time % (86400000 * 365);
        monthCount = (int) time / (86400000 * 30);
        time = (int) time % (86400000 * 30);
        dayCount = (int) time / (86400000);
        time = (int) time % (86400000);
        hourCount = (int) time / (3600000);
        time = (int) time % (3600000);
        minuteCount = (int) time / (60000);
        time = (int) time % (60000);
        secondCount = (int) time / (1000);
        String message = "";
        if (yearCount != 0) {
            message += yearCount + "年";
        }
        if (monthCount != 0) {
            message += monthCount + "月";
        }
        if (dayCount != 0) {
            message += dayCount + "天";
        }
        if (hourCount != 0) {
            message += hourCount + "小时";
        }
        if (minuteCount != 0) {
            message += minuteCount + "分钟";
        }
        if (secondCount != 0) {
            message += secondCount + "秒";
        }
        if (isNullOrEmpty(message)) {
            message = "即时";
        }
        return message;
    }

    public static String friendly_num(String str) {
        return friendly_num(toLong(str));
    }

    public static String friendly_num(long l) {
        if (l < 100)
            return l + "";
        else if (l < 1000)
            return l / 100 + "百";
        else if (l < 10000)
            return l / 1000 + "千";
        else if (l < 1000000)
            return l / 10000 + "万";
        else
            return "百万之上";
    }

    /**
     * 判断给定字符串时间是否为今日
     *
     * @param sdate
     * @return boolean
     */
    public static boolean isToday(String sdate) {
        boolean b = false;
        Date time = toDate(sdate);
        Date today = new Date();
        if (time != null) {
            String nowDate = dateFormater2.format(today);
            String timeDate = dateFormater2.format(time);
            if (nowDate.equals(timeDate)) {
                b = true;
            }
        }
        return b;
    }

    /**
     * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     *
     * @param input
     * @return boolean
     */
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是不是一个合法的电子邮件地址
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        if (email == null || email.trim().length() == 0)
            return false;
        return emailer.matcher(email).matches();
    }

    /**
     * 字符串转整数
     *
     * @param str
     * @param defValue
     * @return
     */
    public static int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str.replace("+", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static int toInt(Object obj) {
        if (obj == null)
            return 0;
        return toInt(obj.toString(), 0);
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static long toLong(String obj) {
        try {
            return Long.parseLong(obj);
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * 字符串转布尔值
     *
     * @param b
     * @return 转换异常返回 false
     */
    public static boolean toBool(String b) {
        try {
            return Boolean.parseBoolean(b);
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * HTML化字符串
     *
     * @param str
     * @return
     */
    public static Spanned fromHtml(String str) {
        if (!isEmpty(str)) {
            return Html.fromHtml(str);
        } else
            return Html.fromHtml("");
    }

    /**
     * 判断两个字符串是否相等
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean isEquals(String a, String b) {
        return a.compareTo(b) == 0;
    }

    /**
     * 判断是否手机号
     *
     * @param inputStr
     * @return
     */
    public static boolean isPhoneNumber(String inputStr) {
        // TODO Auto-generated method stub
        if (inputStr == null) {
            return false;
        }
        if (inputStr.startsWith("1") && inputStr.length() == 11) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否有效的手机号
     */
    public static boolean isMobile(String mobile) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(mobile);
        return m.matches();
    }

    /**
     * 判断今天
     */
    public static boolean dateIsToday(String date) {
        String today = dateFormater4.format(System.currentTimeMillis());
        String otherDay = null;
        if (date != null) {
            otherDay = dateFormater4.format(Long.parseLong(date));
        }
        return today.trim().equals(otherDay);
    }

    /**
     * 比较两个时间大小
     *
     * @param time1
     * @param time2
     * @return
     */
    public static boolean compareTime(String time1, String time2) {

        long t1, t2;

        try {
            t1 = toLong(time1);
        } catch (Exception e) {
            try {
                t1 = dateFormater.parse(time1).getTime();
            } catch (ParseException e1) {
                t1 = 0;
            }
        }

        try {
            t2 = toLong(time2);
        } catch (Exception e) {
            try {
                t2 = dateFormater.parse(time2).getTime();
            } catch (ParseException e1) {
                t2 = 0;
            }
        }

        return t1 > t2;
    }

    /**
     * 计算相隔时差
     *
     * @param totalTime 总共时间
     * @param lastTimeS 上一次时间
     * @return
     */
    public static float TimeLeft2(float totalTime, String lastTimeS) {

        long currentTime = System.currentTimeMillis();

        long lastTime = 0;

        try {
            lastTime = toLong(lastTimeS);
        } catch (Exception e) {
            try {
                lastTime = (dateFormater.parse(lastTimeS)).getTime();
            } catch (Exception e2) {
                lastTime = 0;
            }
        }

        float between = (totalTime * 60 * 1000) - (currentTime - lastTime);

        return between;
    }

    /**
     * 计算日期差
     */
    public static int DateCompare(long s1, long s2) throws Exception {
        Date d1 = new Date(s1 * 1000);
        Date d2 = new Date(s2 * 1000);
        return (int) (Math.abs(((d1.getTime() - d2.getTime()) / (24 * 3600 * 1000))));
    }

    /**
     * 计算相隔小时分-友好的方式(1小时20分)
     */
    public static String DateCompare2(String time) {
        String res = "";
        long currentTime = System.currentTimeMillis();
        long lastTime = 0;
        try {
            lastTime = toLong(time);
        } catch (Exception e) {
            try {
                lastTime = (dateFormater.parse(time)).getTime();
            } catch (Exception e2) {
                lastTime = 0;
            }
        }
        int hour = (int) ((currentTime - lastTime) / (1000 * 60 * 60));
        int minute = (int) (((currentTime - lastTime) % (1000 * 60 * 60)) / 60000);
        res = hour + "小时" + minute + "分";
        return res;
    }

    /**
     * 计算剩余小时分-友好的方式(1小时20分)
     */
    public static String TimeLeft(float total, String time) {
        String res = "";
        long currentTime = System.currentTimeMillis();
        long lastTime = 0;
        long totalTime = (long) (total * 60 * 60 * 1000);
        try {
            lastTime = toLong(time);
        } catch (Exception e) {
            try {
                lastTime = (dateFormater.parse(time)).getTime();
            } catch (Exception e2) {
                lastTime = 0;
            }
        }
        long betweentime = currentTime - lastTime;
        int hour = (int) ((totalTime - betweentime) / (1000 * 60 * 60));
        int minute = (int) (((totalTime - betweentime) % (1000 * 60 * 60)) / 60000);
        res = hour + "小时" + minute + "分";
        return res;
    }

    /**
     * 手机号码显示处理
     *
     * @param phonenumb
     * @return
     */
    public static String phoneNumb_show(String phonenumb) {
        String s = StringUtil.getString(phonenumb);
        if (!s.isEmpty() && s.length() == 11) {
            s = s.substring(0, 3) + "****" + s.substring(7);
        }
        return s;
    }

    /**
     * 返回软件包版本编号
     *
     * @return
     */
    public static String packVersion(Context c) {
        String version = "";
        try {
            // 获取packagemanager的实例
            PackageManager packageManager = c.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo;
            packInfo = packageManager.getPackageInfo(c.getPackageName(), 0);
            version = packInfo.versionName;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 截取String
     *
     * @param str
     * @param s
     * @return
     */
    public static String subString(String str, String s) {
        return str.substring(str.lastIndexOf(s) + 1);
    }

    public static int sdkVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * 显示隐藏软键盘
     *
     * @param context
     * @param edit
     * @param needOpen
     */
    public static void toggleKeyboard(Context context, EditText edit, boolean needOpen) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (needOpen) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
        } else {
            imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
        }
    }

    /**
     * byte(字节)根据长度转成kb(千字节)和mb(兆字节)
     *
     * @param bytes
     * @return
     */
    public static String bytes2kb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        if (returnValue > 1)
            return (returnValue + "MB");
        BigDecimal kilobyte = new BigDecimal(1024);
        returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        return (returnValue + "KB");
    }

    /**
     * byte(字节)根据长度转成mb(兆字节)
     *
     * @param bytes
     * @return
     */
    public static String bytes2mb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        return (returnValue + "MB");
    }

    /**
     * 比较两个String数组是否一致
     *
     * @param arr1
     * @param arr2
     * @return
     */
    public static boolean compareArrays(String[] arr1, String[] arr2) {
        if (arr1 != null && arr2 != null) {
            if (arr1.length != arr2.length) {
                return false;
            } else {
                Arrays.sort(arr1);
                Arrays.sort(arr2);

                return Arrays.equals(arr1, arr2);
            }
        }

        return false;
    }

    /**
     * 保留几位小数
     *
     * @param value
     * @param num
     * @return
     */
    public static String getDecimalFormat(float value, int num) {

        StringBuffer sb = new StringBuffer();
        sb.append("0");

        for (int i = 0; i < num - 1; i++) {
            sb.append("0");
        }

        DecimalFormat decimalFormat = new DecimalFormat("0." + sb.toString());

        return decimalFormat.format(value);
    }

    /**
     * 获取SD卡的总空间和可用空间
     *
     * @param context
     * @return
     */
    public static String[] getBlockFromSD(Context context) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        long availableBlocks = stat.getAvailableBlocks();

        long totalSize = totalBlocks * blockSize;
        long availSize = availableBlocks * blockSize;

        return new String[]{Formatter.formatFileSize(context, totalSize), Formatter.formatFileSize(context, availSize)};
    }

    /**
     * 获取SD卡的总空间和可用空间
     *
     * @return
     */
    public static float[] getBlockFromSD() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        long availableBlocks = stat.getAvailableBlocks();

        long totalSize = totalBlocks * blockSize;
        long availSize = availableBlocks * blockSize;

        return new float[]{totalSize, availSize};
    }

    /**
     * 获取手机的总空间和可用空间
     *
     * @return
     */
    public static float[] getBlockFromBody() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        long availableBlocks = stat.getAvailableBlocks();

        long totalSize = totalBlocks * blockSize;
        long availSize = availableBlocks * blockSize;

        return new float[]{totalSize, availSize};
    }

    /**
     * 获取手机的总空间和可用空间
     *
     * @param context
     * @return
     */
    public static String[] getBlockFromBody(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        long availableBlocks = stat.getAvailableBlocks();

        long totalSize = totalBlocks * blockSize;
        long availSize = availableBlocks * blockSize;

        return new String[]{Formatter.formatFileSize(context, totalSize), Formatter.formatFileSize(context, availSize)};
    }
}