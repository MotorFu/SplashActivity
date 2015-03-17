package com.zxq.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.*;
import com.zxq.service.XmppService;
import com.zxq.ui.switcher.Switch;
import com.zxq.util.LogUtil;
import com.zxq.util.ToastUtil;
import com.zxq.xmpp.R;

/**
 * Created by zxq on 2014/9/16.
 */
public class CreateGroupChatActivity extends Activity {
    private ImageView actionBarBack;
    private TextView acitonBarTitle;

    private EditText groupName;
    private EditText groupDescript;
    private EditText groupPassword;
    private Spinner groupNumber;
    private Switch passwordProtect;
    private Button btnSaveInfo;

    private RelativeLayout layoutPassword;

    private XmppService mXmppService;



    ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mXmppService = ((XmppService.XXBinder) service).getService();
            setupData();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mXmppService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_info);
        initView();
    }

    private void setupData() {
        btnSaveInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomName = groupName.getText().toString();
                String roomDesc = groupDescript.getText().toString();
                String roomPassword = groupPassword.getText().toString();
                String roomNumber = (groupNumber.getSelectedItemPosition()+1)*10+"";
                boolean checked = passwordProtect.isChecked();
                if(roomName.trim().length() < 6){
                    ToastUtil.showShort(CreateGroupChatActivity.this,"房间名不能少于6个字！");
                    return;
                }
                if(checked){
                    if(roomPassword.trim().length() < 6){
                        ToastUtil.showShort(CreateGroupChatActivity.this,"房间名不能少于6个字！");
                        return;
                    }
                }
                boolean isOk = mXmppService.createGroupChatRoom(roomName, roomDesc, checked, roomPassword, roomNumber);
                if(isOk){
                    ToastUtil.showShort(CreateGroupChatActivity.this,"创建成功！");
                    CreateGroupChatActivity.this.finish();
                }else{
                    ToastUtil.showShort(CreateGroupChatActivity.this,"创建失败,房间已存在或者网络异常！");
                }

            }
        });
    }

    private void initView() {
        actionBarBack = (ImageView) findViewById(R.id.actionbar_back);
        acitonBarTitle = (TextView) findViewById(R.id.actionbar_title);
        groupName = (EditText) findViewById(R.id.create_group_info_text_descript_room_name);
        groupDescript = (EditText) findViewById(R.id.create_group_info_text_descript);
        groupNumber = (Spinner) findViewById(R.id.create_group_info_spinner_room_person_number);
        groupPassword = (EditText) findViewById(R.id.create_group_info_text_password);
        passwordProtect = (Switch) findViewById(R.id.create_group_info_switch_password_protect);
        layoutPassword = (RelativeLayout) findViewById(R.id.create_group_info_layout);
        btnSaveInfo = (Button) findViewById(R.id.create_group_info_btn_save_info);
        String[] mNumberItems = getResources().getStringArray(R.array.group_info_number_array);
        ArrayAdapter<String> mNumberItemsAdapter=new ArrayAdapter<String>(this,R.layout.group_info_number_item, mNumberItems);
        groupNumber.setAdapter(mNumberItemsAdapter);
        layoutPassword.setVisibility(View.GONE);
        acitonBarTitle.setText("创建聊天室");
        actionBarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateGroupChatActivity.this.finish();
            }
        });
        passwordProtect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(passwordProtect.isChecked()){
                    layoutPassword.setVisibility(View.VISIBLE);
                }else{
                    layoutPassword.setVisibility(View.GONE);
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        bindXMPPService();

    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindXMPPService();
    }

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

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
