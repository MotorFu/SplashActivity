package com.zxq.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.*;

import com.zxq.adapter.RosterAdapter;
import com.zxq.adapter.RosterChooseAdapter;
import com.zxq.db.RosterProvider;
import com.zxq.service.XmppService;
import com.zxq.util.LogUtil;
import com.zxq.util.ToastUtil;
import com.zxq.xmpp.R;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smackx.muc.InvitationRejectionListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by zxq on 2014/11/9.
 */
public class CurrentUserChooseActivity extends Activity {
    private ImageView actionBarBack;
    private TextView acitonBarTitle;
    private RosterChooseAdapter mRosterAdapter;
    private Button btnChoose;
    private HashMap<String, Boolean> checkedArray;

    private ExpandableListView mFriendChatTreeView;

    private XmppService mXmppService;
    private String mRoomJID;
    private MultiUserChat multiUserChat;

    private void bindXMPPService() {
        LogUtil.i(RegisterActivity.class, "[SERVICE] Unbind");
        Intent mServiceIntent = new Intent(this, XmppService.class);
        bindService(mServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
    }

    private void unbindXMPPService() {
        try {
            unbindService(mServiceConnection);
            LogUtil.i(RegisterActivity.class, "[SERVICE] Unbind");
        } catch (IllegalArgumentException e) {
            LogUtil.e(RegisterActivity.class, "Service wasn't bound!");
        }
    }

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mXmppService = ((XmppService.XXBinder) service).getService();
            setupDate();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mXmppService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_current_user_choose);
        actionBarBack = (ImageView) findViewById(R.id.actionbar_back);
        acitonBarTitle = (TextView) findViewById(R.id.actionbar_title);
        acitonBarTitle.setText("选择邀请的用户");
        actionBarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CurrentUserChooseActivity.this.finish();
            }
        });
        bindXMPPService();
        initView();
    }

    private void setupDate() {
        mRoomJID = getIntent().getStringExtra(GroupChatActivity.MULTI_USER_CHAT_ROOM_JID);
        multiUserChat = mXmppService.getMultiUserChatByRoomJID(mRoomJID);
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<Map.Entry<String, Boolean>> entries = checkedArray.entrySet();
                for(Map.Entry<String, Boolean> temp : entries){
                    Boolean value = temp.getValue();
                    if(value == true){
                        try {
							multiUserChat.invite(temp.getKey(), multiUserChat.getRoom()+":大家来聊聊！");
						} catch (NotConnectedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    }
                }
                CurrentUserChooseActivity.this.finish();
            }
        });
        mFriendChatTreeView = (ExpandableListView) findViewById(R.id.friend_chat_tree_view);
        mFriendChatTreeView.setEmptyView(findViewById(R.id.empty));
        mRosterAdapter = new RosterChooseAdapter(this,mXmppService);
        mRosterAdapter.setRosterChooselistener(new RosterChooseAdapter.RosterChooselistener() {
            @Override
            public void onCheckedPerson(HashMap<String, Boolean> checkedArray) {
                CurrentUserChooseActivity.this.checkedArray = checkedArray;
                if(checkedArray.containsValue(true)){
                    btnChoose.setBackgroundDrawable(getResources().getDrawable(R.drawable.common_btn_green));
                }else{
                    btnChoose.setBackgroundDrawable(getResources().getDrawable(R.drawable.common_btn_black_actionsheet));
                }
            }
        });
        mFriendChatTreeView.setAdapter(mRosterAdapter);
        mRosterAdapter.requery();
    }

    private void initView() {
        btnChoose = (Button) findViewById(R.id.person_info_btn_edit_info);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindXMPPService();
    }


}