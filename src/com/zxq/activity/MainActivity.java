package com.zxq.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.zxq.app.XmppBroadcastReceiver;
import com.zxq.app.XmppBroadcastReceiver.EventHandler;
import com.zxq.db.ChatProvider;
import com.zxq.db.RosterProvider;
import com.zxq.db.RosterProvider.RosterConstants;
import com.zxq.fragment.FriendChatFragment;
import com.zxq.fragment.GroupChatFragment;
import com.zxq.fragment.RecentChatFragment;
import com.zxq.fragment.SettingsFragment;
import com.zxq.service.IConnectionStatusCallback;
import com.zxq.service.XmppService;
import com.zxq.ui.quickaction.ActionItem;
import com.zxq.ui.quickaction.QuickAction;
import com.zxq.ui.quickaction.QuickAction.OnActionItemClickListener;
import com.zxq.ui.slidingmenu.BaseSlidingFragmentActivity;
import com.zxq.ui.slidingmenu.SlidingMenu;
import com.zxq.ui.view.GroupNameView;
import com.zxq.util.*;
import com.zxq.xmpp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseSlidingFragmentActivity implements OnClickListener, IConnectionStatusCallback, EventHandler, FragmentCallBack {
    private static final int ID_CHAT = 0;
    private static final int ID_AVAILABLE = 1;
    private static final int ID_AWAY = 2;
    private static final int ID_XA = 3;
    private static final int ID_DND = 4;

    public static HashMap<String, Integer> mStatusMap;
    private FragmentManager supportFragmentManager;
    private SlidingMenu mSlidingMenu;
    private View mNetErrorView;
    private TextView mTitleNameView;
    private ImageView mTitleStatusView;
    private ProgressBar mTitleProgressBar;

    public static Activity getMainContext() {
        return mainContext;
    }

    private static Activity mainContext;
    private XmppService mXmppService;

    private long firstTime;

    static {
        mStatusMap = new HashMap<String, Integer>();
        mStatusMap.put(PreferenceConstants.OFFLINE, -1);
        mStatusMap.put(PreferenceConstants.DND, R.drawable.status_shield);
        mStatusMap.put(PreferenceConstants.XA, R.drawable.status_invisible);
        mStatusMap.put(PreferenceConstants.AWAY, R.drawable.status_leave);
        mStatusMap.put(PreferenceConstants.AVAILABLE, R.drawable.status_online);
        mStatusMap.put(PreferenceConstants.CHAT, R.drawable.status_qme);
    }



    public void onClickTabButtun(View view) {
        int id = view.getId();
        FragmentTransaction mFragementTransaction = supportFragmentManager.beginTransaction();
        if (id == R.id.btn_friend_chat) {
            FriendChatFragment friendChatFragment = FriendChatFragment.getInstance();
            mFragementTransaction.replace(R.id.main_fragment_content, friendChatFragment);
        } else if (id == R.id.btn_group_chat) {
            GroupChatFragment groupChatFragment = GroupChatFragment.getInstance();
            mFragementTransaction.replace(R.id.main_fragment_content, groupChatFragment);
        }
        mFragementTransaction.commit();
    }

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mXmppService = ((XmppService.XXBinder) service).getService();
            mXmppService.registerConnectionStatusCallback(MainActivity.this);
            if (!mXmppService.isAuthenticated()) {    // 开始连接xmpp服务器
                String usr = PreferenceUtils.getPrefString(MainActivity.this, PreferenceConstants.ACCOUNT, "");
                String password = PreferenceUtils.getPrefString(MainActivity.this, PreferenceConstants.PASSWORD, "");
                mXmppService.login(usr, password);
            } else {
                mTitleNameView.setText(XMPPHelper.splitJidAndServer(PreferenceUtils.getPrefString(MainActivity.this, PreferenceConstants.ACCOUNT, "")));
                setStatusImage(true);
                mTitleProgressBar.setVisibility(View.GONE);
            }
            setupFragmentData();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mXmppService.unRegisterConnectionStatusCallback();
            mXmppService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(MainActivity.this, XmppService.class));

        supportFragmentManager = getSupportFragmentManager();
        setContentView(R.layout.main_center_layout);
        mainContext = this;
        initSlidingMenu();
        initViews();
        bindXMPPService();
    }


    @Override
    public void onBackPressed() {    //连续按两次返回键就退出
        if (System.currentTimeMillis() - firstTime < 3000) {
            finish();
        } else {
            firstTime = System.currentTimeMillis();
            ToastUtil.showShort(this, R.string.press_again_backrun);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatusImage(isConnected());
        XmppBroadcastReceiver.mListeners.add(this);
        if (NetUtil.getNetworkState(this) == NetUtil.NETWORN_NONE)
            mNetErrorView.setVisibility(View.VISIBLE);
        else
            mNetErrorView.setVisibility(View.GONE);


    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindXMPPService();
        XmppBroadcastReceiver.mListeners.remove(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    //所有数据都应该放到服务绑定之后，由于绑定是异步的会有所延迟
    private void setupFragmentData() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentTransaction mFragementTransaction = supportFragmentManager.beginTransaction();
                FriendChatFragment friendChatFragment = FriendChatFragment.getInstance();
                mFragementTransaction.replace(R.id.main_fragment_content, friendChatFragment);
                mFragementTransaction.commit();
                mFragementTransaction.show(friendChatFragment);
            }
        });


    }

    private void unbindXMPPService() {
        try {
            unbindService(mServiceConnection);
            LogUtil.i(LoginActivity.class, "[SERVICE] Unbind");
        } catch (IllegalArgumentException e) {
            LogUtil.e(LoginActivity.class, "Service wasn't bound!");
        }
    }

    private void bindXMPPService() {
        LogUtil.i(LoginActivity.class, "[SERVICE] Bind");
        bindService(new Intent(MainActivity.this, XmppService.class), mServiceConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
    }

    private void initViews() {
        mNetErrorView = findViewById(R.id.net_status_bar_top);
        mSlidingMenu.setSecondaryMenu(R.layout.main_right_layout);
        FragmentTransaction mFragementTransaction = supportFragmentManager.beginTransaction();
        Fragment mFrag = new SettingsFragment();
        mFragementTransaction.replace(R.id.main_right_fragment, mFrag);
        mFragementTransaction.commit();
        ImageButton mLeftBtn = ((ImageButton) findViewById(R.id.show_left_fragment_btn));
        mLeftBtn.setVisibility(View.VISIBLE);
        mLeftBtn.setOnClickListener(this);
        ImageButton mRightBtn = ((ImageButton) findViewById(R.id.show_right_fragment_btn));
        mRightBtn.setVisibility(View.VISIBLE);
        mRightBtn.setOnClickListener(this);
        mTitleNameView = (TextView) findViewById(R.id.ivTitleName);
        mTitleProgressBar = (ProgressBar) findViewById(R.id.ivTitleProgress);
        mTitleStatusView = (ImageView) findViewById(R.id.ivTitleStatus);
        mTitleNameView.setText(XMPPHelper.splitJidAndServer(PreferenceUtils.getPrefString(this, PreferenceConstants.ACCOUNT, "")));
        mTitleNameView.setOnClickListener(this);
    }

    //发起向好友会话
    private void startChatActivity(String userJid, String userName) {
        Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
        Uri userNameUri = Uri.parse(userJid);
        chatIntent.setData(userNameUri);
        chatIntent.putExtra(ChatActivity.INTENT_EXTRA_USERNAME, userName);
        startActivity(chatIntent);
    }

    private boolean isConnected() {
        return mXmppService != null && mXmppService.isAuthenticated();
    }


    private void initSlidingMenu() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int mScreenWidth = dm.widthPixels;// 获取屏幕分辨率宽度
        setBehindContentView(R.layout.main_left_layout);// 设置左菜单

        FragmentTransaction mFragementTransaction = supportFragmentManager.beginTransaction();
        Fragment mFrag = new RecentChatFragment();
        mFragementTransaction.replace(R.id.main_left_fragment, mFrag);
        mFragementTransaction.commit();

        // customize the SlidingMenu
        mSlidingMenu = getSlidingMenu();
        mSlidingMenu.setMode(SlidingMenu.LEFT_RIGHT);// 设置是左滑还是右滑，还是左右都可以滑，我这里左右都可以滑
        mSlidingMenu.setShadowWidth(mScreenWidth / 40);// 设置阴影宽度
        mSlidingMenu.setBehindOffset(mScreenWidth / 8);// 设置菜单宽度
        mSlidingMenu.setFadeDegree(0.35f);// 设置淡入淡出的比例
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        mSlidingMenu.setShadowDrawable(R.drawable.shadow_left);// 设置左菜单阴影图片
        mSlidingMenu.setSecondaryShadowDrawable(R.drawable.shadow_right);// 设置右菜单阴影图片
        mSlidingMenu.setFadeEnabled(true);// 设置滑动时菜单的是否淡入淡出
        mSlidingMenu.setBehindScrollScale(0.333f);// 设置滑动时拖拽效果
    }

    private static final String[] GROUPS_QUERY = new String[]{RosterConstants._ID, RosterConstants.GROUP,};
    private static final String[] ROSTER_QUERY = new String[]{RosterConstants._ID, RosterConstants.JID, RosterConstants.ALIAS, RosterConstants.STATUS_MODE, RosterConstants.STATUS_MESSAGE,};

    public List<String> getRosterGroups() {//获得所有分组名称
        List<String> list = new ArrayList<String>();
        Cursor cursor = getContentResolver().query(RosterProvider.GROUPS_URI, GROUPS_QUERY, null, null, RosterConstants.GROUP);
        int idx = cursor.getColumnIndex(RosterConstants.GROUP);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.add(cursor.getString(idx));
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public List<String[]> getRosterContacts() {//获得所有人员名称
        List<String[]> list = new ArrayList<String[]>();
        Cursor cursor = getContentResolver().query(RosterProvider.CONTENT_URI, ROSTER_QUERY, null, null, RosterConstants.ALIAS);
        int JIDIdx = cursor.getColumnIndex(RosterConstants.JID);
        int aliasIdx = cursor.getColumnIndex(RosterConstants.ALIAS);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String jid = cursor.getString(JIDIdx);
            String alias = cursor.getString(aliasIdx);
            if ((alias == null) || (alias.length() == 0))
                alias = jid;
            list.add(new String[]{jid, alias});
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    protected void setViewImage(ImageView v, String value) {
        int presenceMode = Integer.parseInt(value);
        int statusDrawable = getIconForPresenceMode(presenceMode);
        v.setImageResource(statusDrawable);
        if (statusDrawable == R.drawable.status_busy)
            v.setVisibility(View.INVISIBLE);
    }

    private int getIconForPresenceMode(int presenceMode) {
        return StatusMode.values()[presenceMode].getDrawableId();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.show_left_fragment_btn) {
            mSlidingMenu.showMenu(true);
        } else if (id == R.id.show_right_fragment_btn) {
            mSlidingMenu.showSecondaryMenu(true);
        } else if (id == R.id.ivTitleName) {
            if (isConnected())
                showStatusQuickAction(v);
        }
    }

    private void showStatusQuickAction(View v) {
        QuickAction quickAction = new QuickAction(this, QuickAction.VERTICAL);
        quickAction.addActionItem(new ActionItem(ID_CHAT, getString(R.string.status_chat), getResources().getDrawable(R.drawable.status_qme)));
        quickAction.addActionItem(new ActionItem(ID_AVAILABLE, getString(R.string.status_available), getResources().getDrawable(R.drawable.status_online)));
        quickAction.addActionItem(new ActionItem(ID_AWAY, getString(R.string.status_away), getResources().getDrawable(R.drawable.status_leave)));
        quickAction.addActionItem(new ActionItem(ID_XA, getString(R.string.status_xa), getResources().getDrawable(R.drawable.status_invisible)));
        quickAction.addActionItem(new ActionItem(ID_DND, getString(R.string.status_dnd), getResources().getDrawable(R.drawable.status_shield)));
        quickAction.setOnActionItemClickListener(new OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                if (!isConnected()) {
                    ToastUtil.showShort(MainActivity.this, R.string.conversation_net_error_label);
                    return;
                }
                switch (actionId) {
                    case ID_CHAT:
                        mTitleStatusView.setImageResource(R.drawable.status_qme);
                        PreferenceUtils.setPrefString(MainActivity.this, PreferenceConstants.STATUS_MODE, PreferenceConstants.CHAT);
                        PreferenceUtils.setPrefString(MainActivity.this, PreferenceConstants.STATUS_MESSAGE, getString(R.string.status_chat));
                        break;
                    case ID_AVAILABLE:
                        mTitleStatusView.setImageResource(R.drawable.status_online);
                        PreferenceUtils.setPrefString(MainActivity.this, PreferenceConstants.STATUS_MODE, PreferenceConstants.AVAILABLE);
                        PreferenceUtils.setPrefString(MainActivity.this, PreferenceConstants.STATUS_MESSAGE, getString(R.string.status_available));
                        break;
                    case ID_AWAY:
                        mTitleStatusView.setImageResource(R.drawable.status_leave);
                        PreferenceUtils.setPrefString(MainActivity.this, PreferenceConstants.STATUS_MODE, PreferenceConstants.AWAY);
                        PreferenceUtils.setPrefString(MainActivity.this, PreferenceConstants.STATUS_MESSAGE, getString(R.string.status_away));
                        break;
                    case ID_XA:
                        mTitleStatusView.setImageResource(R.drawable.status_invisible);
                        PreferenceUtils.setPrefString(MainActivity.this, PreferenceConstants.STATUS_MODE, PreferenceConstants.XA);
                        PreferenceUtils.setPrefString(MainActivity.this, PreferenceConstants.STATUS_MESSAGE, getString(R.string.status_xa));
                        break;
                    case ID_DND:
                        mTitleStatusView.setImageResource(R.drawable.status_shield);
                        PreferenceUtils.setPrefString(MainActivity.this, PreferenceConstants.STATUS_MODE, PreferenceConstants.DND);
                        PreferenceUtils.setPrefString(MainActivity.this, PreferenceConstants.STATUS_MESSAGE, getString(R.string.status_dnd));
                        break;
                    default:
                        break;
                }
                mXmppService.setStatusFromConfig();
                SettingsFragment fragment = (SettingsFragment) supportFragmentManager.findFragmentById(R.id.main_right_fragment);
                fragment.readData();
            }
        });
        quickAction.show(v);
    }


    void removeChatHistory(final String JID) {
        getContentResolver().delete(ChatProvider.CONTENT_URI, ChatProvider.ChatConstants.JID + " = ?", new String[]{JID});
    }

    void removeRosterItemDialog(final String JID, final String userName) {
        new AlertDialog.Builder(this).setTitle(R.string.deleteRosterItem_title).setMessage(getString(R.string.deleteRosterItem_text, userName, JID)).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mXmppService.removeRosterItem(JID);
            }
        }).setNegativeButton(android.R.string.no, null).create().show();
    }


    void moveRosterItemToGroupDialog(final String jabberID) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View group = inflater.inflate(R.layout.moverosterentrytogroupview, null);
        final GroupNameView gv = (GroupNameView) group.findViewById(R.id.moverosterentrytogroupview_gv);
        gv.setGroupList(getRosterGroups());
        new AlertDialog.Builder(this).setTitle(R.string.MoveRosterEntryToGroupDialog_title).setView(group).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                LogUtil.d("new group: " + gv.getGroupName());
                if (isConnected())
                    mXmppService.moveRosterItemToGroup(jabberID, gv.getGroupName());
            }
        }).setNegativeButton(android.R.string.cancel, null).create().show();
    }

    @Override
    public void onNetChange() {
        if (NetUtil.getNetworkState(this) == NetUtil.NETWORN_NONE) {
            ToastUtil.showShort(this, R.string.net_error_tip);
            mNetErrorView.setVisibility(View.VISIBLE);
        } else {
            mNetErrorView.setVisibility(View.GONE);
        }
    }

    private void setStatusImage(boolean isConnected) {
        if (!isConnected) {
            mTitleStatusView.setVisibility(View.GONE);
            return;
        }
        String statusMode = PreferenceUtils.getPrefString(this, PreferenceConstants.STATUS_MODE, PreferenceConstants.AVAILABLE);
        int statusId = mStatusMap.get(statusMode);
        if (statusId == -1) {
            mTitleStatusView.setVisibility(View.GONE);
        } else {
            mTitleStatusView.setVisibility(View.VISIBLE);
            mTitleStatusView.setImageResource(statusId);
        }
    }

    @Override
    public void connectionStatusChanged(int connectedState, String reason) {
        switch (connectedState) {
            case XmppService.CONNECTED:
                mTitleNameView.setText(XMPPHelper.splitJidAndServer(PreferenceUtils.getPrefString(MainActivity.this, PreferenceConstants.ACCOUNT, "")));
                mTitleProgressBar.setVisibility(View.GONE);
                // mTitleStatusView.setVisibility(View.GONE);
                setStatusImage(true);
                break;
            case XmppService.CONNECTING:
                mTitleNameView.setText(R.string.login_prompt_msg);
                mTitleProgressBar.setVisibility(View.VISIBLE);
                mTitleStatusView.setVisibility(View.GONE);
                break;
            case XmppService.DISCONNECTED:
                mTitleNameView.setText(R.string.login_prompt_no);
                mTitleProgressBar.setVisibility(View.GONE);
                mTitleStatusView.setVisibility(View.GONE);
                ToastUtil.showLong(this, reason);
                break;
            default:
                break;
        }
    }

    @Override
    public XmppService getService() {
        return mXmppService;
    }

    @Override
    public MainActivity getMainActivity() {
        return this;
    }


}
