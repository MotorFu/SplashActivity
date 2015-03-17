package com.zxq.activity;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import com.zxq.service.XmppService;
import com.zxq.util.LogUtil;

public class BaseActivity extends FragmentActivity {
	public static ArrayList<BackPressHandler> mListeners = new ArrayList<BackPressHandler>();

	@Override
	protected void onResume() {
		super.onResume();
		if (mListeners.size() > 0)
			for (BackPressHandler handler : mListeners) {
				handler.activityOnResume();
			}

	}


//    private void bindXMPPService() {
//        LogUtil.i(LoginActivity.class, "[SERVICE] Unbind");
//        Intent mServiceIntent = new Intent(this, XmppService.class);
//        mServiceIntent.setAction(LOGIN_ACTION);
//        bindService(mServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
//    }

    @Override
	protected void onPause() {
		super.onPause();
		if (mListeners.size() > 0)
			for (BackPressHandler handler : mListeners) {
				handler.activityOnPause();
			}
	}

	public static abstract interface BackPressHandler {

		public abstract void activityOnResume();

		public abstract void activityOnPause();

	}
}
