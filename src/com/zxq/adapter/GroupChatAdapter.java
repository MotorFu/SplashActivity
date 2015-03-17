package com.zxq.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.zxq.db.ChatProvider;
import com.zxq.db.GroupChatProvider.GroupChatConstants;
import com.zxq.util.LogUtil;
import com.zxq.util.PreferenceConstants;
import com.zxq.util.PreferenceUtils;
import com.zxq.util.TimeUtil;
import com.zxq.xmpp.R;

public class GroupChatAdapter extends SimpleCursorAdapter {

	private static final int DELAY_NEWMSG = 2000;
	private Context mContext;
	private LayoutInflater mInflater;

	public GroupChatAdapter(Context context, Cursor cursor, String[] from) {
		super(context, 0, cursor, from, null);
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Cursor cursor = this.getCursor();
		cursor.moveToPosition(position);

		long dateMilliseconds = cursor.getLong(cursor.getColumnIndex(GroupChatConstants.DATE));
		String date = TimeUtil.getChatTime(dateMilliseconds);
		String message = cursor.getString(cursor.getColumnIndex(GroupChatConstants.MESSAGE));
		int come = cursor.getInt(cursor.getColumnIndex(GroupChatConstants.DIRECTION));// 消息来自
		boolean from_me = (come == GroupChatConstants.OUTGOING);
		String jid = cursor.getString(cursor.getColumnIndex(GroupChatConstants.JID));
        String roomJid = cursor.getString(cursor.getColumnIndex(GroupChatConstants.RoomJID));
		ViewHolder viewHolder;
		if (convertView == null || convertView.getTag(R.drawable.qq_icon + come) == null) {
			if (come == GroupChatConstants.OUTGOING) {
				convertView = mInflater.inflate(R.layout.activity_chat_item_right, parent, false);
			} else {
				convertView = mInflater.inflate(R.layout.activity_chat_item_left, null);
			}
			viewHolder = buildHolder(convertView);
			convertView.setTag(R.drawable.qq_icon + come, viewHolder);
			convertView.setTag(R.string.app_name, R.drawable.qq_icon + come);
		} else {
			viewHolder = (ViewHolder) convertView.getTag(R.drawable.qq_icon + come);
		}

		bindViewData(viewHolder, date, from_me,jid, message);
		return convertView;
	}


	private void bindViewData(ViewHolder holder, String date, boolean from_me,String jid, String message) {
		holder.avatar.setBackgroundResource(R.drawable.login_default_avatar);
		if (from_me && !PreferenceUtils.getPrefBoolean(mContext, PreferenceConstants.SHOW_MY_HEAD, true)) {
			holder.avatar.setVisibility(View.GONE);
		}
		holder.content.setText(message);
		holder.time.setText(date);
	}

	private ViewHolder buildHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.content = (TextView) convertView.findViewById(R.id.textView2);
		holder.time = (TextView) convertView.findViewById(R.id.datetime);
		holder.avatar = (ImageView) convertView.findViewById(R.id.icon);
		return holder;
	}

	private static class ViewHolder {
		TextView content;
		TextView time;
		ImageView avatar;

	}

}
