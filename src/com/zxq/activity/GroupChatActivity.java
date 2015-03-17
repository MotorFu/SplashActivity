package com.zxq.activity;

import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.zxq.adapter.GroupChatAdapter;
import com.zxq.adapter.GroupChatListAdapter;
import com.zxq.adapter.RosterChooseAdapter;
import com.zxq.app.XmppApplication;
import com.zxq.db.GroupChatProvider;
import com.zxq.db.GroupChatProvider.GroupChatConstants;
import com.zxq.fragment.GroupChatFragment;
import com.zxq.service.IConnectionStatusCallback;
import com.zxq.service.XmppService;
import com.zxq.ui.emoji.EmojiKeyboard;
import com.zxq.ui.emoji.EmojiKeyboard.EventListener;
import com.zxq.ui.swipeback.SwipeBackActivity;
import com.zxq.ui.xlistview.MsgListView;
import com.zxq.ui.xlistview.MsgListView.IXListViewListener;
import com.zxq.util.*;
import com.zxq.vo.GroupChat;
import com.zxq.vo.SerializationOccupant;
import com.zxq.xmpp.R;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.*;

import java.util.*;

public class GroupChatActivity extends SwipeBackActivity implements OnTouchListener, OnClickListener, IXListViewListener, IConnectionStatusCallback {
    public static final String MULTI_USER_CHAT_ROOM_JID = "MULTI_USER_CHAT_JID";
    public static final String MULTI_USER_CHAT_ROOM_OCCUPANTS = "MULTI_USER_CHAT_ROOM_OCCUPANTS";

    private MsgListView mMsgListView;// 对话ListView
    private boolean mIsFaceShow = false;// 是否显示表情
    private Button mSendMsgBtn;// 发送消息button
    private ImageButton mFaceSwitchBtn;// 切换键盘和表情的button
    private TextView mTitleNameView;// 标题栏
    private EditText mChatEditText;// 消息输入框
    private EmojiKeyboard mFaceRoot;// 表情父容器
    private WindowManager.LayoutParams mWindowNanagerParams;
    private InputMethodManager mInputMethodManager;
    private List<String> mFaceMapKeys;// 表情对应的字符串数组
    private String mRoomName = null;
    private String mRoomJID = null;// 当前聊天用户的ID
    private MultiUserChat multiUserChat;
    private List<GroupChat> arrayList = new ArrayList<GroupChat>();
    private GroupChatListAdapter groupChatListAdapter;
    private String userName;
    private String passWord;
    private boolean isAdmin;

    private static final String[] PROJECTION_FROM = new String[]{GroupChatConstants._ID, GroupChatConstants.DATE, GroupChatConstants.DIRECTION, GroupChatConstants.JID, GroupChatConstants.RoomJID, GroupChatConstants.MESSAGE};// 查询字段


    private XmppService mXmppService;// Main服务
    ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mXmppService = ((XmppService.XXBinder) service).getService();
            mXmppService.registerConnectionStatusCallback(GroupChatActivity.this);
            // 如果没有连接上，则重新连接xmpp服务器
            if (!mXmppService.isAuthenticated()) {
                String usr = PreferenceUtils.getPrefString(GroupChatActivity.this, PreferenceConstants.ACCOUNT, "");
                String password = PreferenceUtils.getPrefString(GroupChatActivity.this, PreferenceConstants.PASSWORD, "");
                mXmppService.login(usr, password);
            }
            initData();// 初始化数据
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mXmppService.unRegisterConnectionStatusCallback();
            mXmppService = null;
        }

    };
    private ImageButton mGroupSettingBtn;
    private int REQUEST_CODE_INVITE = 0X1;
    private int REQUEST_CODE_INFO_CHANGE = 0X2;
    private int REQUEST_CODE_KILL_MENBER = 0X3;

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
        bindService(mServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        initView();// 初始化view
        bindXMPPService();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindXMPPService();// 解绑服务


    }


    private void initData() {
        mRoomJID = getIntent().getStringExtra(GroupChatFragment.GROUP_CHAT_ROOM_JID);
        mRoomName = getIntent().getStringExtra(GroupChatFragment.GROUP_CHAT_ROOM_NAME);
        final String name = mXmppService.getXmppUserName();
        userName = name.substring(0, name.indexOf("@"));
        passWord = getIntent().getStringExtra(GroupChatFragment.GROUP_CHAT_ROOM_PASD);
        multiUserChat = mXmppService.getMultiUserChatByRoomJID(mRoomJID);
        isAdmin = false;
        multiUserChat.addUserStatusListener(new UserStatusListener() {
            @Override
            public void kicked(final String s, String s1) {
                LogUtil.e("------------------>kicked");
                GroupChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showShort(GroupChatActivity.this,"你已被管理员"+s.substring(0,s.indexOf("@"))+"移除聊天室。");
                    }
                });
                GroupChatActivity.this.finish();

            }

            @Override
            public void voiceGranted() {
                LogUtil.e("------------------>voiceGranted");
            }

            @Override
            public void voiceRevoked() {
                LogUtil.e("------------------>voiceRevoked");
            }

            @Override
            public void banned(String s, String s1) {
                LogUtil.e("------------------>banned");
            }

            @Override
            public void membershipGranted() {
                LogUtil.e("------------------>membershipGranted");
            }

            @Override
            public void membershipRevoked() {
                LogUtil.e("------------------>membershipRevoked");
            }

            @Override
            public void moderatorGranted() {
                LogUtil.e("------------------>moderatorGranted");
            }

            @Override
            public void moderatorRevoked() {
                LogUtil.e("------------------>moderatorRevoked");
            }

            @Override
            public void ownershipGranted() {
                LogUtil.e("------------------>ownershipGranted");
            }

            @Override
            public void ownershipRevoked() {
                LogUtil.e("------------------>ownershipRevoked");
            }

            @Override
            public void adminGranted() {
                LogUtil.e("------------------>adminGranted");
            }

            @Override
            public void adminRevoked() {
                LogUtil.e("------------------>adminRevoked");
            }
        });
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(8);
        try {
            multiUserChat.join(userName, passWord, history, 3000);
        
            multiUserChat.getConfigurationForm();
            isAdmin = true;
        } catch (XMPPException e) {
            e.printStackTrace();
            isAdmin = false;
        } catch (NoResponseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        multiUserChat.addInvitationRejectionListener(new InvitationRejectionListener() {
            @Override
            public void invitationDeclined(String invitee, String reason) {
                LogUtil.e("==========================拒绝邀请"+invitee);
                ToastUtil.showShort(GroupChatActivity.this,invitee.substring(0,invitee.indexOf("@"))+"拒绝加入聊天。");
            }
        });


        mTitleNameView.setText(mRoomName);

        multiUserChat.addMessageListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                final Message message = (Message) packet;
                System.out.println(message.getFrom() + " : " + message.getBody());
                GroupChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GroupChat groupChat = new GroupChat();
                        groupChat.jid = message.getFrom().substring(message.getFrom().indexOf("/") + 1);
                        groupChat.come = groupChat.jid.equals(userName) ? 1 : 0;
                        groupChat.date = new Date().toLocaleString();
                        groupChat.dateMilliseconds = new Date().getTime();
                        groupChat.message = message.getBody();
                        groupChat.roomJid = message.getTo();
                        arrayList.add(groupChat);
                        groupChatListAdapter.notifyDataSetChanged();
                        mMsgListView.setSelection(groupChatListAdapter.getCount() - 1);
                    }
                });
            }
        });

        Set<String> keySet = XmppApplication.getInstance().getFaceMap().keySet();
        mFaceMapKeys = new ArrayList<String>();
        mFaceMapKeys.addAll(keySet);

        mGroupSettingBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog groupChatMenuDialog = DialogUtil.getGroupChatMenuDialog(GroupChatActivity.this);
                Button btnInvite = (Button) groupChatMenuDialog.findViewById(R.id.dialog_menu_btn_group_chat_invite);
                Button btnInfoChange = (Button) groupChatMenuDialog.findViewById(R.id.dialog_menu_btn_group_chat_info_change);
                Button btnKillMenber = (Button) groupChatMenuDialog.findViewById(R.id.dialog_menu_btn_group_chat_kill_menber);
                btnInvite.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(GroupChatActivity.this, CurrentUserChooseActivity.class);
                        intent.putExtra(MULTI_USER_CHAT_ROOM_JID, mRoomJID);
                        GroupChatActivity.this.startActivityForResult(intent, REQUEST_CODE_INVITE);
                        groupChatMenuDialog.dismiss();
                    }
                });

                btnInfoChange.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!isAdmin){
                            ToastUtil.showShort(GroupChatActivity.this,"你不是管理员，无法进行此操作！");
                            return;
                        }
                        Intent intent = new Intent();
                        intent.setClass(GroupChatActivity.this, EditGroupInfoActivity.class);
                        intent.putExtra(MULTI_USER_CHAT_ROOM_JID, mRoomJID);
                        GroupChatActivity.this.startActivityForResult(intent, REQUEST_CODE_INFO_CHANGE);
                        groupChatMenuDialog.dismiss();
                    }
                });

                btnKillMenber.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!isAdmin){
                            ToastUtil.showShort(GroupChatActivity.this,"你不是管理员，无法进行此操作！");
                            return;
                        }
                        Intent intent = new Intent();
                        intent.setClass(GroupChatActivity.this, GroupOccupantsActivity.class);
                        intent.putExtra(MULTI_USER_CHAT_ROOM_JID, mRoomJID);
                        StringBuffer sb = new StringBuffer();
                        ArrayList<SerializationOccupant> arrayList = new ArrayList<SerializationOccupant>();
                        Iterator<String> iterator = (Iterator<String>) multiUserChat.getOccupants();
                        while (iterator.hasNext()){
                            String next = iterator.next();
                            Occupant occupant = multiUserChat.getOccupant(next);
                            if("owner".equals(occupant.getAffiliation().toLowerCase())){
                                continue;
                            }else {
                                SerializationOccupant serializationOccupant = new SerializationOccupant();
                                serializationOccupant.setJid(occupant.getJid());
                                serializationOccupant.setNick(occupant.getNick());
                                serializationOccupant.setAffiliation(occupant.getAffiliation());
                                serializationOccupant.setRole(occupant.getRole());
                                arrayList.add(serializationOccupant);
                            }

                        }
                        intent.putExtra(MULTI_USER_CHAT_ROOM_OCCUPANTS,arrayList);
                        GroupChatActivity.this.startActivityForResult(intent, REQUEST_CODE_KILL_MENBER);
                        groupChatMenuDialog.dismiss();
                    }
                });
                groupChatMenuDialog.show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
			multiUserChat.leave();
		} catch (NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }


    /**
     * 设置聊天的Adapter
     */

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
        mGroupSettingBtn = (ImageButton) findViewById(R.id.btn_group_setting);
        mChatEditText = (EditText) findViewById(R.id.group_input);
        mFaceRoot = (EmojiKeyboard) findViewById(R.id.face_ll);
        groupChatListAdapter = new GroupChatListAdapter(this, arrayList);
        mMsgListView.setAdapter(groupChatListAdapter);
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
        mChatEditText.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mWindowNanagerParams.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE || mIsFaceShow) {
                        mFaceRoot.setVisibility(View.GONE);
                        mIsFaceShow = false;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_INFO_CHANGE) {
            if (resultCode == EditGroupInfoActivity.EDIT_GROUP_CODE_KEY) {
                int i = data.getIntExtra(EditGroupInfoActivity.EDIT_GROUP_CODE_INTENT_VALUE, EditGroupInfoActivity.EDIT_GROUP_CODE_ERROR);
                if (i == EditGroupInfoActivity.EDIT_GROUP_CODE_OK) {
                    String change = data.getStringExtra(EditGroupInfoActivity.EDIT_GROUP_CODE_INTENT_TITLE);
                    mTitleNameView.setText(change);
                } else if (i == EditGroupInfoActivity.EDIT_GROUP_CODE_ERROR) {

                } else if (i == EditGroupInfoActivity.EDIT_GROUP_CODE_REMOVE_ROOM){
                    try {
                        multiUserChat.destroy("no why!","NULL");
                    } catch (XMPPException | NoResponseException | NotConnectedException e) {
                        e.printStackTrace();
                    }
                    GroupChatActivity.this.finish();
                }
            }
        } else if (requestCode == REQUEST_CODE_INVITE) {

        } else if (requestCode == REQUEST_CODE_KILL_MENBER) {
            if (resultCode == GroupOccupantsActivity.CHOOSE_OCCUPANTS_RESULT_CODE) {
                String nikenames = data.getStringExtra(GroupOccupantsActivity.CHOOSE_OCCUPANTS_NIKENAMES);
                ToastUtil.showShort(this,nikenames);

                String[] split = nikenames.split(",");
                for(int i = 0 ; i < split.length;i++){
                    try {
                        multiUserChat.kickParticipant(split[i],"saad");
                        ToastUtil.showShort(this,"踢出成功");
                    } catch (XMPPException | NoResponseException | NotConnectedException e) {
                        ToastUtil.showShort(this,"踢出失败");
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    @Override
    public void onRefresh() {
        mMsgListView.stopRefresh();
    }

    @Override
    public void onLoadMore() {
        // do nothing
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
                mXmppService.sendGroupChat(multiUserChat, mChatEditText.getText().toString());
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
