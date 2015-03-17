package com.zxq.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.zxq.app.XmppApplication;
import com.zxq.ui.emoji.EmojiKeyboard;
import com.zxq.ui.emoji.EmojiKeyboard.EventListener;
import com.zxq.util.*;
import com.zxq.xmpp.R;
import com.zxq.adapter.ChatAdapter;
import com.zxq.adapter.FaceAdapter;
import com.zxq.adapter.FacePageAdeapter;
import com.zxq.db.ChatProvider;
import com.zxq.db.RosterProvider;
import com.zxq.db.ChatProvider.ChatConstants;
import com.zxq.service.IConnectionStatusCallback;
import com.zxq.service.XmppService;
import com.zxq.ui.swipeback.SwipeBackActivity;
import com.zxq.ui.view.CirclePageIndicator;
import com.zxq.ui.xlistview.MsgListView;
import com.zxq.ui.xlistview.MsgListView.IXListViewListener;
import com.zxq.util.LogUtil;

public class ChatActivity extends SwipeBackActivity implements OnTouchListener, OnClickListener, IXListViewListener, IConnectionStatusCallback {
	public static final String INTENT_EXTRA_USERNAME = ChatActivity.class.getName() + ".username";// 昵称对应的key
	private MsgListView mMsgListView;// 对话ListView
	private ViewPager mFaceViewPager;// 表情选择ViewPager
	private int mCurrentPage = 0;// 当前表情页
	private boolean mIsFaceShow = false;// 是否显示表情
	private Button mSendMsgBtn;// 发送消息button
	private ImageButton mFaceSwitchBtn;// 切换键盘和表情的button
	private TextView mTitleNameView;// 标题栏
	private ImageView mTitleStatusView;
	private EditText mChatEditText;// 消息输入框
	private EmojiKeyboard mFaceRoot;// 表情父容器
	private WindowManager.LayoutParams mWindowNanagerParams;
	private InputMethodManager mInputMethodManager;
	private List<String> mFaceMapKeys;// 表情对应的字符串数组
	private String mWithJabberID = null;// 当前聊天用户的ID



	private static final String[] PROJECTION_FROM = new String[] { ChatProvider.ChatConstants._ID, ChatProvider.ChatConstants.DATE, ChatProvider.ChatConstants.DIRECTION, ChatProvider.ChatConstants.JID, ChatProvider.ChatConstants.MESSAGE, ChatProvider.ChatConstants.DELIVERY_STATUS };// 查询字段

	private ContentObserver mContactObserver = new ContactObserver();// 联系人数据监听，主要是监听对方在线状态
	private XmppService mXmppService;// Main服务
	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mXmppService = ((XmppService.XXBinder) service).getService();
			mXmppService.registerConnectionStatusCallback(ChatActivity.this);
			// 如果没有连接上，则重新连接xmpp服务器
			if (!mXmppService.isAuthenticated()) {
				String usr = PreferenceUtils.getPrefString(ChatActivity.this, PreferenceConstants.ACCOUNT, "");
				String password = PreferenceUtils.getPrefString(ChatActivity.this, PreferenceConstants.PASSWORD, "");
				mXmppService.login(usr, password);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXmppService.unRegisterConnectionStatusCallback();
			mXmppService = null;
		}

	};

	/**
	 * 解绑服务
	 */
	private void unbindXMPPService() {
		try {
			unbindService(mServiceConnection);
		} catch (IllegalArgumentException e) {
			LogUtil.e("Service wasn't bound!");
		}
	}

	/**
	 * 绑定服务
	 */
	private void bindXMPPService() {
		Intent mServiceIntent = new Intent(this, XmppService.class);
		Uri chatURI = Uri.parse(mWithJabberID);
		mServiceIntent.setData(chatURI);
		bindService(mServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		initData();// 初始化数据
		initView();// 初始化view
		setChatWindowAdapter();// 初始化对话数据
		getContentResolver().registerContentObserver(RosterProvider.CONTENT_URI, true, mContactObserver);// 开始监听联系人数据库
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateContactStatus();// 更新联系人状态
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	// 查询联系人数据库字段
	private static final String[] STATUS_QUERY = new String[] { RosterProvider.RosterConstants.STATUS_MODE, RosterProvider.RosterConstants.STATUS_MESSAGE, };

	private void updateContactStatus() {
		Cursor cursor = getContentResolver().query(RosterProvider.CONTENT_URI, STATUS_QUERY, RosterProvider.RosterConstants.JID + " = ?", new String[] { mWithJabberID }, null);
		int MODE_IDX = cursor.getColumnIndex(RosterProvider.RosterConstants.STATUS_MODE);
		int MSG_IDX = cursor.getColumnIndex(RosterProvider.RosterConstants.STATUS_MESSAGE);

		if (cursor.getCount() == 1) {
			cursor.moveToFirst();
			int status_mode = cursor.getInt(MODE_IDX);
			String status_message = cursor.getString(MSG_IDX);
			LogUtil.d("contact status changed: " + status_mode + " " + status_message);
			mTitleNameView.setText(XMPPHelper.splitJidAndServer(getIntent().getStringExtra(INTENT_EXTRA_USERNAME)));
			int statusId = StatusMode.values()[status_mode].getDrawableId();
			if (statusId != -1) {// 如果对应离线状态
				mTitleStatusView.setImageResource(statusId);
				mTitleStatusView.setVisibility(View.VISIBLE);
			} else {
				mTitleStatusView.setVisibility(View.GONE);
			}
		}
		cursor.close();
	}

	/**
	 * 联系人数据库变化监听
	 * 
	 */
	private class ContactObserver extends ContentObserver {
		public ContactObserver() {
			super(new Handler());
		}

		public void onChange(boolean selfChange) {
			LogUtil.d("ContactObserver.onChange: " + selfChange);
			updateContactStatus();// 联系人状态变化时，刷新界面
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (hasWindowFocus())
			unbindXMPPService();// 解绑服务
		getContentResolver().unregisterContentObserver(mContactObserver);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// 窗口获取到焦点时绑定服务，失去焦点将解绑
		if (hasFocus)
			bindXMPPService();
		else
			unbindXMPPService();
	}

	private void initData() {
		mWithJabberID = getIntent().getDataString().toLowerCase();// 获取聊天对象的id
		// 将表情map的key保存在数组中
		Set<String> keySet = XmppApplication.getInstance().getFaceMap().keySet();
		mFaceMapKeys = new ArrayList<String>();
		mFaceMapKeys.addAll(keySet);
	}

	/**
	 * 设置聊天的Adapter
	 */
	private void setChatWindowAdapter() {
		String selection = ChatConstants.JID + "='" + mWithJabberID + "'";
		// 异步查询数据库
		new AsyncQueryHandler(getContentResolver()) {

			@Override
			protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
				ListAdapter adapter = new ChatAdapter(ChatActivity.this, cursor, PROJECTION_FROM);
				mMsgListView.setAdapter(adapter);
				mMsgListView.setSelection(adapter.getCount() - 1);
			}

		}.startQuery(0, null, ChatProvider.CONTENT_URI, PROJECTION_FROM, selection, null, null);
	}

	private void initView() {
		mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		mWindowNanagerParams = getWindow().getAttributes();

		mMsgListView = (MsgListView) findViewById(R.id.msg_listView);
		// 触摸ListView隐藏表情和输入法
		mMsgListView.setOnTouchListener(this);
		mMsgListView.setPullLoadEnable(false);
		mMsgListView.setXListViewListener(this);
		mSendMsgBtn = (Button) findViewById(R.id.send);
		mFaceSwitchBtn = (ImageButton) findViewById(R.id.face_switch_btn);
		mChatEditText = (EditText) findViewById(R.id.chat_input);
		mFaceRoot = (EmojiKeyboard) findViewById(R.id.face_ll);
		mFaceRoot.setEventListener(new EventListener() {

			@Override
			public void onEmojiSelected(String res) {
				EmojiKeyboard.input(mChatEditText, res);
			}

			@Override
			public void onBackspace() {
				EmojiKeyboard.backspace(mChatEditText);
			}
		});
		mChatEditText.setOnTouchListener(this);
		mTitleNameView = (TextView) findViewById(R.id.ivTitleName);
		mTitleStatusView = (ImageView) findViewById(R.id.ivTitleStatus);
		mChatEditText.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					if (mWindowNanagerParams.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE || mIsFaceShow) {
						mFaceRoot.setVisibility(View.GONE);
						mIsFaceShow = false;
						// imm.showSoftInput(msgEt, 0);
						return true;
					}
				}
				return false;
			}
		});
		mChatEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > 0) {
					mSendMsgBtn.setEnabled(true);
				} else {
					mSendMsgBtn.setEnabled(false);
				}
			}
		});
		mFaceSwitchBtn.setOnClickListener(this);
		mSendMsgBtn.setOnClickListener(this);
	}

	@Override
	public void onRefresh() {
		mMsgListView.stopRefresh();
	}

    @Override
    public void onLoadMore() {

    }

    @Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.face_switch_btn) {
			if (!mIsFaceShow) {
				mInputMethodManager.hideSoftInputFromWindow(mChatEditText.getWindowToken(), 0);
				try {
					Thread.sleep(80);// 解决此时会黑一下屏幕的问题
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mFaceRoot.setVisibility(View.VISIBLE);
				mFaceSwitchBtn.setImageResource(R.drawable.aio_keyboard);
				mIsFaceShow = true;
			} else {
				mFaceRoot.setVisibility(View.GONE);
				mInputMethodManager.showSoftInput(mChatEditText, 0);
				mFaceSwitchBtn.setImageResource(R.drawable.qzone_edit_face_drawable);
				mIsFaceShow = false;
			}
		} else if (id == R.id.send) {// 发送消息
			sendMessageIfNotNull();
		}
	}

	private void sendMessageIfNotNull() {
		if (mChatEditText.getText().length() >= 1) {
			if (mXmppService != null) {
				mXmppService.sendMessage(mWithJabberID, mChatEditText.getText().toString());
				if (!mXmppService.isAuthenticated())
					ToastUtil.showShort(this, "消息已经保存随后发送");
			}
			mChatEditText.setText("");
			mSendMsgBtn.setEnabled(false);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int id = v.getId();
		if (id == R.id.msg_listView) {
			mInputMethodManager.hideSoftInputFromWindow(mChatEditText.getWindowToken(), 0);
			mFaceSwitchBtn.setImageResource(R.drawable.qzone_edit_face_drawable);
			mFaceRoot.setVisibility(View.GONE);
			mIsFaceShow = false;
		} else if (id == R.id.input) {
			mInputMethodManager.showSoftInput(mChatEditText, 0);
			mFaceSwitchBtn.setImageResource(R.drawable.qzone_edit_face_drawable);
			mFaceRoot.setVisibility(View.GONE);
			mIsFaceShow = false;
		}
		return false;
	}

	@Override
	public void connectionStatusChanged(int connectedState, String reason) {
	}

}
