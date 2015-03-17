package com.zxq.app;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.zxq.service.XmppService;
import com.zxq.util.LogUtil;
import com.zxq.util.PreferenceConstants;
import com.zxq.util.PreferenceUtils;

public class XmppBroadcastReceiver extends BroadcastReceiver {
	public static final String BOOT_COMPLETED_ACTION = "com.zxq.action.BOOT_COMPLETED";
	public static ArrayList<EventHandler> mListeners = new ArrayList<EventHandler>();

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		LogUtil.i("action = " + action);
		if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
			if (mListeners.size() > 0)// 通知接口完成加载
				for (EventHandler handler : mListeners) {
					handler.onNetChange();
				}
		} else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {//系统结束时停止XMPP服务
			LogUtil.d("System shutdown, stopping service.");
			Intent xmppServiceIntent = new Intent(context, XmppService.class);
			context.stopService(xmppServiceIntent);
		} else {//自动登录
			if (!TextUtils.isEmpty(PreferenceUtils.getPrefString(context, PreferenceConstants.PASSWORD, "")) && PreferenceUtils.getPrefBoolean(context, PreferenceConstants.AUTO_START, true)) {
				Intent xmppServiceIntent = new Intent(context, XmppService.class);
				xmppServiceIntent.setAction(BOOT_COMPLETED_ACTION);
				context.startService(xmppServiceIntent);
			}
		}
	}

	public static abstract interface EventHandler {

		public abstract void onNetChange();
	}
}
