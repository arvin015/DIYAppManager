package com.activels.als.diyappmanager.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 *
 * @author arvin.li
 *
 */
public class NetworkUtil {

	private static final int CMNET = 3;
	private static final int CMWAP = 2;
	private static final int WIFI = 1;

	/**
	 * 网络是否连接
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * WiFi是否连接
	 * @param context
	 * @return
	 */
	public static boolean isWifiConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null) {
				return mWiFiNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * @param context
	 * @return
	 */
	public static boolean isMobileConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mMobileNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mMobileNetworkInfo != null) {
				return mMobileNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 获取当前的网络状态 -1：没有网络 1：WIFI网络 2：wap网络 3：net网络
	 * @param context
	 * @return
	 */
	public static int getAPNType(Context context) {
		int netType = -1;
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}

		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			if (networkInfo.getExtraInfo().toLowerCase().equals("cmnet")) {
				netType = CMNET;
			}
			else {
				netType = CMWAP;
			}
		}
		else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = WIFI;
		}
		return netType;
	}

	/**
	 * 网络监听器
	 * @param context
	 * @param callback
	 * @return
	 */
	public static BroadcastReceiver registerNetworkCallback(Context context,
			NetworkChangedCallback callback) {
		BroadcastReceiver connectionReceiver = new ConnectionReceiver(callback);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		context.registerReceiver(connectionReceiver, intentFilter);
		return connectionReceiver;
	}

	public static class ConnectionReceiver extends BroadcastReceiver {
		private NetworkChangedCallback callback;

		public ConnectionReceiver(NetworkChangedCallback callback) {
			super();
			this.callback = callback;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager connectMgr = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mobNetInfo = connectMgr
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			NetworkInfo wifiNetInfo = connectMgr
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mobNetInfo.isConnected() || wifiNetInfo.isConnected()) {
				callback.onNetworkConnected(connectMgr);
			} else {
				callback.onNetworkUnconnected(connectMgr);
			}
		}
	}

	public interface NetworkChangedCallback {
		public void onNetworkConnected(ConnectivityManager cm);

		public void onNetworkUnconnected(ConnectivityManager cm);
	}
}
