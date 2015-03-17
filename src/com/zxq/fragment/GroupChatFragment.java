package com.zxq.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.zxq.activity.FragmentCallBack;
import com.zxq.activity.GroupChatActivity;
import com.zxq.service.XmppService;
import com.zxq.util.DialogUtil;
import com.zxq.util.LogUtil;
import com.zxq.util.ToastUtil;
import com.zxq.vo.GroupEntry;
import com.zxq.xmpp.R;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.List;

/**
 * Created by zxq on 2014/9/13.
 */
public class GroupChatFragment extends Fragment {
    private ListView listView;
    private XmppService mXmppService;
    private static GroupChatFragment groupChatFragment;
    private GroupChatAdapter groupChatAdapter;
    private List<GroupEntry> groupEntryList;

    private FragmentCallBack mFragmentCallBack;

    public static String GROUP_CHAT_ROOM_JID = "GCRJ";
    public static String GROUP_CHAT_ROOM_PASD = "PASD";
    public static String GROUP_CHAT_ROOM_NAME = "NAME";

    public static GroupChatFragment getInstance() {
        if (groupChatFragment == null)
            groupChatFragment = new GroupChatFragment();
        return groupChatFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mFragmentCallBack = (FragmentCallBack) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View inflate = inflater.inflate(R.layout.fragment_group_chat_tree, container, false);
        initView(inflate);
        return inflate;
    }


    private void initView(View inflate) {
        listView = (ListView) inflate.findViewById(R.id.group_chat_tree_view);
    }

    private void setupData() {
        mXmppService = mFragmentCallBack.getService();
        if(mXmppService == null || !mXmppService.isAuthenticated()){
            ToastUtil.showShort(GroupChatFragment.this.getActivity(),"网络未连接,请连接后再执行该操作.");
            return;
        }
        groupEntryList = mXmppService.getGroupEntryList();
        groupChatAdapter = new GroupChatAdapter(groupEntryList, this.getActivity());
        listView.setAdapter(groupChatAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final GroupEntry groupEntry = groupEntryList.get(position);
                if (groupEntry.isLock()) {
                    displayPasswordDialog(groupEntry);
                } else {
                    String name = mXmppService.getXmppUserName();
                    name = name.substring(0, name.indexOf("@"));
                    MultiUserChat multiUserChat = mXmppService.getMultiUserChatByRoomJID(groupEntry.getJid());
                    try {

                        multiUserChat.join(name,null);
                        multiUserChat.leave();
                        Intent intent = new Intent();
                        intent.putExtra(GROUP_CHAT_ROOM_JID, groupEntry.getJid());
                        intent.putExtra(GROUP_CHAT_ROOM_NAME, groupEntry.getTitle());
                        intent.putExtra(GROUP_CHAT_ROOM_PASD, "");
                        intent.setClass(GroupChatFragment.this.getActivity(), GroupChatActivity.class);
                        GroupChatFragment.this.startActivity(intent);
                    } catch (XMPPException | SmackException e) {
                        e.printStackTrace();
                        if(e.getMessage().equals("not-authorized(401)")){
                           displayPasswordDialog(groupEntry);
                        }
                        if(e.getMessage().equals("not-acceptable(406)")){
                            ToastUtil.showShort(GroupChatFragment.this.getActivity(),"聊天室初始化未完成.");
                        }

                    }
                }
            }
        });
        groupChatAdapter.notifyDataSetChanged();
    }

    public void displayPasswordDialog(final GroupEntry groupEntry) {
        final Dialog groupPasswordInputDialog = DialogUtil.getGroupPasswordInputDialog(this.getActivity());
        final EditText passwordField = (EditText) groupPasswordInputDialog.findViewById(R.id.dialog_field_group_chat_password);
        Button okBtn = (Button) groupPasswordInputDialog.findViewById(R.id.dialog_btn_group_chat_ok);
        Button cancelBtn = (Button) groupPasswordInputDialog.findViewById(R.id.dialog_btn_group_chat_cancel);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todo:发送服务器一个验证，是否成功，成功则继续
                String name = mXmppService.getXmppUserName();
                name = name.substring(0, name.indexOf("@"));
                try {
                    MultiUserChat multiUserChat = mXmppService.getMultiUserChatByRoomJID(groupEntry.getJid());
                    String password = passwordField.getText().toString().trim();
                    multiUserChat.join(name, password);
                    multiUserChat.leave();
                    groupPasswordInputDialog.dismiss();
                    Intent intent = new Intent();
                    intent.putExtra(GROUP_CHAT_ROOM_JID, groupEntry.getJid());
                    intent.putExtra(GROUP_CHAT_ROOM_NAME, groupEntry.getTitle());
                    intent.putExtra(GROUP_CHAT_ROOM_PASD, password);

                    intent.setClass(GroupChatFragment.this.getActivity(), GroupChatActivity.class);
                    GroupChatFragment.this.startActivity(intent);
                } catch (XMPPException | SmackException e) {
                    e.printStackTrace();
                    LogUtil.e("=========多人聊天======", e.getMessage());
                    ToastUtil.showShort(GroupChatFragment.this.getActivity(), "密码错误");
                    groupPasswordInputDialog.dismiss();
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupPasswordInputDialog.dismiss();
            }
        });
        groupPasswordInputDialog.show();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.e("------------------------------------列表更新数据");
        setupData();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private class GroupChatAdapter extends BaseAdapter {
        private List<GroupEntry> groupList;
        private Context context;

        private GroupChatAdapter() {
        }

        public List<GroupEntry> getGroupList() {
            return groupList;
        }

        public void setGroupList(List<GroupEntry> groupList) {
            this.groupList = groupList;
            this.notifyDataSetChanged();
        }

        private GroupChatAdapter(List<GroupEntry> groupList, Context context) {
            this.groupList = groupList;
            this.context = context;
        }

        @Override
        public int getCount() {
            return groupList.size();
        }

        @Override
        public Object getItem(int position) {
            return groupList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GroupHolder groupHolder = new GroupHolder();
            GroupEntry groupEntry = groupList.get(position);
            convertView = LayoutInflater.from(context).inflate(R.layout.fragment_group_chat_tree_item, null);
            groupHolder.groupIcon = (ImageView) convertView.findViewById(R.id.group_chat_item_icon);
            groupHolder.groupTitle = (TextView) convertView.findViewById(R.id.group_chat_item_title);
            if (groupEntry.isLock()) {
                groupHolder.groupIcon.setBackgroundColor(Color.RED);
            }
            groupHolder.groupTitle.setText(groupEntry.getTitle());
            return convertView;
        }

        private class GroupHolder {
            public ImageView groupIcon;
            public TextView groupTitle;
        }
    }


}
