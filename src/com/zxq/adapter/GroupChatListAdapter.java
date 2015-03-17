package com.zxq.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.zxq.db.GroupChatProvider.GroupChatConstants;
import com.zxq.util.PreferenceConstants;
import com.zxq.util.PreferenceUtils;
import com.zxq.util.TimeUtil;
import com.zxq.vo.GroupChat;
import com.zxq.xmpp.R;

import java.util.ArrayList;
import java.util.List;

public class GroupChatListAdapter extends BaseAdapter {

	private static final int DELAY_NEWMSG = 2000;
	private Context mContext;
	private LayoutInflater mInflater;
    private List<GroupChat> arrayList;

	public GroupChatListAdapter(Context context, List<GroupChat> arrayList) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.arrayList = arrayList;
	}

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {

        GroupChat groupChat = arrayList.get(position);//new GroupChat();

		long dateMilliseconds = groupChat.dateMilliseconds;
		String date =  groupChat.date;
		String message =  groupChat.message;
		int come =  groupChat.come;// 消息来自
		boolean from_me = (come == GroupChatConstants.OUTGOING);
		String jid =  groupChat.jid;
        String roomJid =  groupChat.roomJid;
		ViewHolder viewHolder;
		if (convertView == null || convertView.getTag(R.drawable.qq_icon + come) == null) {
			if (come == GroupChatConstants.OUTGOING) {
				convertView = mInflater.inflate(R.layout.activity_group_chat_item_right, parent, false);
			} else {
				convertView = mInflater.inflate(R.layout.activity_group_chat_item_left, null);
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
        holder.sendName.setText(jid);
	}

	private ViewHolder buildHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.content = (TextView) convertView.findViewById(R.id.textView2);
		holder.time = (TextView) convertView.findViewById(R.id.datetime);
		holder.avatar = (ImageView) convertView.findViewById(R.id.icon);
        holder.sendName = (TextView) convertView.findViewById(R.id.user_name);
		return holder;
	}

	private static class ViewHolder {
		TextView content;
		TextView time;
		ImageView avatar;
        TextView sendName;
	}

    public void updateDataSource(List<GroupChat> arrayList){
        this.arrayList = arrayList;
        this.notifyDataSetChanged();
    }
}
