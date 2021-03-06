package com.activels.als.diyappmanager.utils;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtils {
	/** 
	 * 保存在手机里面的文件名
	 */  
	private static final String FILE_NAME = "sharetext"; 

	private final String MAK = "cookbook";
	@SuppressWarnings("deprecation")
	private final int MODE = Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE;
	private final SharedPreferences sharedpreferences;

	private static SharedPreferencesUtils spfu;

	/**
	 * @param context
	 * @param fileName:数据存储文件名称 默认为null,或自定义文件名称
	 */
	private SharedPreferencesUtils(Context context, String fileName) {
		if(fileName == null || fileName == ""){
			fileName = FILE_NAME;
		}
		sharedpreferences = context.getSharedPreferences(fileName, MODE);
	}

	/**
	 * 实例化SharedPreferencesUtils，单例模式
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static SharedPreferencesUtils getInstance(Context context, String fileName){
		if(spfu == null){
			spfu = new SharedPreferencesUtils(context, fileName);
		}

		return spfu;
	}

	public boolean saveSharedPreferences(String key, String value) {
		SharedPreferences.Editor editor = sharedpreferences.edit();
		try {
			editor.putString(key, AESEncryptor.encrypt(MAK, value));
		} catch (Exception e) {
			editor.putString(key, value);
			e.printStackTrace();
		}
		return editor.commit();
	}

	public String loadStringSharedPreference(String key) {
		String str = null;
		try {
			str = sharedpreferences.getString(key, null);
			if (str != null && !"".equals(str)){
				str = AESEncryptor.decrypt(MAK, str);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}

	public boolean saveSharedPreferences(String key, int value) {
		SharedPreferences.Editor editor = sharedpreferences.edit();
		editor.putInt(key, value);
		return editor.commit();
	}

	public int loadIntSharedPreference(String key) {
		return sharedpreferences.getInt(key, 0);
	}

	public boolean saveSharedPreferences(String key, float value) {
		SharedPreferences.Editor editor = sharedpreferences.edit();
		editor.putFloat(key, value);
		return editor.commit();
	}

	public float loadFloatSharedPreference(String key) {
		return sharedpreferences.getFloat(key, 0f);
	}

	public boolean saveSharedPreferences(String key, Long value) {
		SharedPreferences.Editor editor = sharedpreferences.edit();
		editor.putLong(key, value);
		return editor.commit();
	}

	public long loadLongSharedPreference(String key) {
		return sharedpreferences.getLong(key, 0l);
	}

	public boolean saveSharedPreferences(String key, Boolean value) {
		SharedPreferences.Editor editor = sharedpreferences.edit();
		editor.putBoolean(key, value);
		return editor.commit();
	}

	public boolean loadBooleanSharedPreference(String key) {
		return sharedpreferences.getBoolean(key, false);
	}

	public boolean saveAllSharePreference(String keyName, List<?> list) {
		int size = list.size();
		if (size < 1) {
			return false;
		}
		SharedPreferences.Editor editor = sharedpreferences.edit();
		if (list.get(0) instanceof String) {
			for (int i = 0; i < size; i++) {
				editor.putString(keyName + i, (String) list.get(i));
			}
		} else if (list.get(0) instanceof Long) {
			for (int i = 0; i < size; i++) {
				editor.putLong(keyName + i, (Long) list.get(i));
			}
		} else if (list.get(0) instanceof Float) {
			for (int i = 0; i < size; i++) {
				editor.putFloat(keyName + i, (Float) list.get(i));
			}
		} else if (list.get(0) instanceof Integer) {
			for (int i = 0; i < size; i++) {
				editor.putLong(keyName + i, (Integer) list.get(i));
			}
		} else if (list.get(0) instanceof Boolean) {
			for (int i = 0; i < size; i++) {
				editor.putBoolean(keyName + i, (Boolean) list.get(i));
			}
		}
		return editor.commit();
	}

	public Map<String, ?> loadAllSharePreference(String key) {
		return sharedpreferences.getAll();
	}

	public boolean removeKey(String key) {
		SharedPreferences.Editor editor = sharedpreferences.edit();
		editor.remove(key);
		return editor.commit();
	}

	public boolean removeAllKey() {
		SharedPreferences.Editor editor = sharedpreferences.edit();
		editor.clear();
		return editor.commit();
	}
}

