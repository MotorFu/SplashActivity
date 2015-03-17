package com.zxq.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zxq.activity.CreateGroupChatActivity;
import com.zxq.app.XmppApplication;
import com.zxq.util.DialogUtil;
import com.zxq.util.LogUtil;
import com.zxq.util.ToastUtil;
import com.zxq.xmpp.R;
import com.zxq.activity.ChatActivity;
import com.zxq.activity.FragmentCallBack;
import com.zxq.adapter.RecentChatAdapter;
import com.zxq.db.ChatProvider;
import com.zxq.db.ChatProvider.ChatConstants;
import com.zxq.service.XmppService;
import com.zxq.ui.swipelistview.BaseSwipeListViewListener;
import com.zxq.ui.swipelistview.SwipeListView;
import com.zxq.ui.view.AddRosterItemDialog;
import com.zxq.util.XMPPHelper;

public class RecentChatFragment extends Fragment implements OnClickListener {

	private Handler mainHandler = new Handler();
	private ContentObserver mChatObserver = new ChatObserver();
	private ContentResolver mContentResolver;
	private SwipeListView mSwipeListView;
	private RecentChatAdapter mRecentChatAdapter;
	private TextView mTitleView;
	private ImageView mTitleAddView;
	private FragmentCallBack mFragmentCallBack;
	private static RecentChatFragment recentChatFragment;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mFragmentCallBack = (FragmentCallBack) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
		}
	}

	public static RecentChatFragment getRecentChatFragment(){
		return recentChatFragment;
	}

	public void callbackWhenAddRoster(String name){
		final String user = name.substring(0,name.indexOf("@"));
		LogUtil.i("测试信息3(" + user + ")");
		RecentChatFragment.this.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ToastUtil.showShort(RecentChatFragment.this.getActivity(), "用户 " + user + " 已加入");
			}
		});
	}
	public void callbackWhenUpdateRoster(String name){
		final String user = name.substring(0,name.indexOf("@"));
		LogUtil.i("测试信息3(" + user + ")");
		RecentChatFragment.this.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ToastUtil.showShort(RecentChatFragment.this.getActivity(), "用户 " + user + " 信息更新");
			}
		});

	}
	public void callbackWhenDeleteRoster(String name){
		final String user = name.substring(0,name.indexOf("@"));
		LogUtil.i("测试信息3(" + user + ")");
		RecentChatFragment.this.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ToastUtil.showShort(RecentChatFragment.this.getActivity(), "用户 " + user + " 已删除");
			}
		});

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContentResolver = getActivity().getContentResolver();
		mRecentChatAdapter = new RecentChatAdapter(getActivity());
		recentChatFragment = this;
	}

	@Override
	public void onResume() {
		super.onResume();
		mRecentChatAdapter.requery();
		mContentResolver.registerContentObserver(ChatProvider.CONTENT_URI, true, mChatObserver);
	}

	@Override
	public void onPause() {
		super.onPause();
		mContentResolver.unregisterContentObserver(mChatObserver);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.recent_chat_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initView(view);
	}

	private void initView(View view) {
		mTitleView = (TextView) view.findViewById(R.id.ivTitleName);
		mTitleView.setText(R.string.recent_chat_fragment_title);
		mTitleAddView = (ImageView) view.findViewById(R.id.ivTitleBtnRightImage);
		mTitleAddView.setImageResource(R.drawable.setting_add_account_white);
		mTitleAddView.setVisibility(View.VISIBLE);
		mTitleAddView.setOnClickListener(this);
		mSwipeListView = (SwipeListView) view.findViewById(R.id.recent_listview);
		mSwipeListView.setEmptyView(view.findViewById(R.id.recent_empty));
		mSwipeListView.setAdapter(mRecentChatAdapter);
		mSwipeListView.setSwipeListViewListener(mSwipeListViewListener);

	}

	public void updateRoster() {
		mRecentChatAdapter.requery();
	}

	private class ChatObserver extends ContentObserver {
		public ChatObserver() {
			super(mainHandler);
		}

		public void onChange(boolean selfChange) {
			updateRoster();
			LogUtil.i("liweiping", "selfChange" + selfChange);
		}
	}

	BaseSwipeListViewListener mSwipeListViewListener = new BaseSwipeListViewListener() {
		@Override
		public void onClickFrontView(int position) {
			Cursor clickCursor = mRecentChatAdapter.getCursor();
			clickCursor.moveToPosition(position);
			String jid = clickCursor.getString(clickCursor.getColumnIndex(ChatConstants.JID));
			Uri userNameUri = Uri.parse(jid);
			Intent toChatIntent = new Intent(getActivity(), ChatActivity.class);
			toChatIntent.setData(userNameUri);
			toChatIntent.putExtra(ChatActivity.INTENT_EXTRA_USERNAME, XMPPHelper.splitJidAndServer(jid));
			startActivity(toChatIntent);
		}

		@Override
		public void onClickBackView(int position) {
			mSwipeListView.closeOpenedItems();// 关闭打开的项
		}
	};

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.ivTitleBtnRightImage) {
			final Dialog groupOrFriendChooseDialog = DialogUtil.getGroupOrFriendChooseDialog(RecentChatFragment.this.getActivity());
			Button btnCreateGroup = (Button) groupOrFriendChooseDialog.findViewById(R.id.dialog_group_list_btn_create_group);
			Button btnSearchFriend = (Button) groupOrFriendChooseDialog.findViewById(R.id.dialog_friend_list_btn_search_friend);
			btnCreateGroup.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					XmppService xmppService = mFragmentCallBack.getService();
					if (xmppService == null || !xmppService.isAuthenticated()) {
						ToastUtil.showShort(RecentChatFragment.this.getActivity(),"网络未连接,请连接后再执行该操作.");
						return;
					}
					Intent intent = new Intent();
					intent.setClass(RecentChatFragment.this.getActivity(), CreateGroupChatActivity.class);
					RecentChatFragment.this.getActivity().startActivity(intent);
					groupOrFriendChooseDialog.dismiss();
				}
			});
			btnSearchFriend.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					XmppService xmppService = mFragmentCallBack.getService();
					if (xmppService == null || !xmppService.isAuthenticated()) {
						ToastUtil.showShort(RecentChatFragment.this.getActivity(),"网络未连接,请连接后再执行该操作.");
						return;
					}
					new AddRosterItemDialog(mFragmentCallBack.getMainActivity(), xmppService).show();// 添加联系人
					groupOrFriendChooseDialog.dismiss();
				}
			});
			groupOrFriendChooseDialog.show();


		}
	}

}
