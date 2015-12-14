package com.activels.als.diyappmanager.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * 
* @Title: ToastUtil.java 
* @Package com.geatgdrink.util 
* @Description: 弹出提示
* @author zgy 
* @date 2013-9-10 下午2:53:01 
* @version V1.0
 */
public class ToastUtil {
	private ToastUtil() {
	}
	
	public static final void toastShort(Context context,String msg){
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	
	public static final void toastShort(Context context,int msg){
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	
	public static final void toastLong(Context context,String msg){
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
	
	public static final void toastLong(Context context,int msg){
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
}
