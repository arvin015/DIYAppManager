package com.activels.als.diyappmanager.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * px   ： 是屏幕的像素点
 * dp   ： 一个基于density的抽象单位，如果一个160dpi的屏幕，1dp=1px
 * dip  ： 等同于dp
 * sp   ： 同dp相似，但还会根据用户的字体大小偏好来缩放(建议使用sp作为文本的单位，其它用dip)
 * dpi  : 屏幕密度，每英寸有多少个显示点
 *
 * drawable-ldpi  ：   屏幕密度为120的手机设备
 * drawable-mdpi  ：   屏幕密度为160的手机设备（此为baseline，其他均以此为基准，在此设备上，1dp = 1px）
 * drawable-hdpi  ：   屏幕密度为240的手机设备
 * drawable-xhdpi ：   屏幕密度为320的手机设备
 * drawable-xxhdpi：   屏幕密度为480的手机设备
 */

/**
 * dp、sp 转换为 px 的工具类
 * 
 * @author arvin.li
 *
 */
public class DisplayUtil { 

	private WindowManager wm;

	private static DisplayUtil util;

	private DisplayUtil(Context context){		
		if(wm == null){
			wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

			DisplayMetrics dm = new DisplayMetrics();
			wm.getDefaultDisplay().getMetrics(dm);

			screenWidth = dm.widthPixels;
			screenHeight = dm.heightPixels;

			density = dm.density;
		}
	}

	public static DisplayUtil getInstance(Context context){

		if(util == null){
			util = new DisplayUtil(context);
		}

		return util;
	}

	/**
	 * 屏幕宽
	 */
	public static int screenWidth;
	/**
	 * 屏幕高
	 */
	public static int screenHeight;

	/**
	 * 屏幕密度
	 */
	public static float density;

	/**
	 * 将px值转换为dip或dp值
	 * 
	 * @param pxValue
	 * @return
	 */ 
	public static int px2dip(float pxValue) { 
		return (int) (pxValue / density + 0.5f); 
	} 

	/**
	 * 将dip或dp值转换为px值
	 * 
	 * @param dipValue
	 * @return
	 */ 
	public static int dip2px(float dipValue) { 
		return (int) (dipValue * density + 0.5f); 
	} 

	/**
	 * 将px值转换为sp值
	 * 
	 * @param pxValue
	 * @return
	 */ 
	public static int px2sp(float pxValue) { 
		return (int) (pxValue / density + 0.5f); 
	} 

	/**
	 * 将sp值转换为px值
	 * 
	 * @param spValue
	 * @return
	 */ 
	public static int sp2px(float spValue) { 
		return (int) (spValue * density + 0.5f); 
	} 
}