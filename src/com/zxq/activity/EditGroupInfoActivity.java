package com.zxq.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import com.zxq.service.XmppService;
import com.zxq.ui.switcher.Switch;
import com.zxq.util.DialogUtil;
import com.zxq.util.LogUtil;
import com.zxq.util.ToastUtil;
import com.zxq.xmpp.R;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zxq on 2014/9/16.
 */
public class EditGroupInfoActivity extends Activity {
    public static final int EDIT_GROUP_CODE_KEY = 0X788;
    public static final int EDIT_GROUP_CODE_OK = 0X787;
    public static final int EDIT_GROUP_CODE_ERROR = 0X786;
    public static final int EDIT_GROUP_CODE_REMOVE_ROOM = 0X796;
    public static final String EDIT_GROUP_CODE_INTENT_VALUE = "EDIT_GROUP_CODE_INTENT_VALUE";
    public static final String EDIT_GROUP_CODE_INTENT_TITLE = "EDIT_GROUP_CODE_INTENT_TITLE";


    private ImageView actionBarBack;
    private TextView acitonBarTitle;

    private EditText groupName;
    private EditText groupDescript;

    private EditText groupPassword;
    private Spinner groupNumber;
    private Switch passwordProtect;
    private Button btnDeleteGroup;
    private Button btnSaveInfo;
    private Form form;
    private RelativeLayout layoutPassword;

    private XmppService mXmppService;
    private String mRoomJID;
    private MultiUserChat multiUserChat;


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
        setContentView(R.layout.activity_edit_group_info);
        initView();
    }

    private void setupData() {
        mRoomJID = getIntent().getStringExtra(GroupChatActivity.MULTI_USER_CHAT_ROOM_JID);
        multiUserChat = mXmppService.getMultiUserChatByRoomJID(mRoomJID);
        //获取聊天室的配置表单

        Iterator<String> values;
        try {
            form = multiUserChat.getConfigurationForm();
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (NoResponseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if(form == null){
            ToastUtil.showShort(this,"网络错误，无法显示信息");
            return;
        }

        FormField fieldRoomName = form.getField("muc#roomconfig_roomname");
        values = (Iterator<String>) fieldRoomName.getValues();
        while (values.hasNext()){
            String tempStr = values.next();
            groupName.setText(tempStr);
        }

        FormField fieldRoomDesc = form.getField("muc#roomconfig_roomdesc");
        values = (Iterator<String>) fieldRoomDesc.getValues();
        while (values.hasNext()){
            String tempStr = values.next();
            groupDescript.setText(tempStr);
        }

        FormField fieldMaxUsers = form.getField("muc#roomconfig_maxusers");
        values = (Iterator<String>) fieldMaxUsers.getValues();
        while (values.hasNext()){
            String tempStr = values.next();
            int tempNum = (Integer.parseInt(tempStr)/10) - 1;
            LogUtil.e("fieldMaxUsers："+tempNum);
            groupNumber.setSelection(tempNum);
        }
        FormField fieldPasswordProtectRoom = form.getField("muc#roomconfig_passwordprotectedroom");
        values = (Iterator<String>) fieldPasswordProtectRoom.getValues();
        while (values.hasNext()){
            String tempStr = values.next();
            int tempNum = Integer.parseInt(tempStr);
            LogUtil.e("fieldPasswordProtectRoom："+tempNum);
            if(tempNum == 1){
                passwordProtect.setChecked(true);
                layoutPassword.setVisibility(View.VISIBLE);
            }else{
                passwordProtect.setChecked(false);
                layoutPassword.setVisibility(View.GONE);
            }
        }


        btnSaveInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //根据原始表单创建一个要提交的新表单
                Form submitForm = form.createAnswerForm();
                Intent intent = new Intent();
                //向提交的表单添加默认答复
                for(Iterator<FormField> fields = (Iterator<FormField>) form.getFields(); fields.hasNext();) {
                    FormField field = (FormField) fields.next();
                    if(!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
                        submitForm.setDefaultAnswer(field.getVariable());
                    }
                }
                //重新设置聊天室名称（修改名称）
                submitForm.setAnswer("muc#roomconfig_roomname", groupName.getText().toString());
                //设置聊天室的新拥有者（群主转让）
//                List<String> owners = new ArrayList<String>();
//                owners.add("test@pc2010102716");
//                submitForm.setAnswer("muc#roomconfig_roomowners", owners);
                //设置密码 （设置密码保护，进入需要密码）
                //设置描述  （设置群描述）
                submitForm.setAnswer("muc#roomconfig_roomdesc", groupDescript.getText().toString());
                ToastUtil.showShort(EditGroupInfoActivity.this, "" + (groupNumber.getSelectedItemPosition()+1)*10);
                List<String> list = new ArrayList<String>();
                list.add((groupNumber.getSelectedItemPosition()+1)*10+"");
                submitForm.setAnswer("muc#roomconfig_maxusers", list);
                submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
                if(passwordProtect.isChecked()) {
                    // 进入是否需要密码
                    submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
                    // 设置进入密码
                    submitForm.setAnswer("muc#roomconfig_roomsecret", groupPassword.getText().toString().trim());
                }else{
                    submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", false);
                }
                //设置聊天室是持久聊天室，即将要被保存下来（是否持久此聊天室）该设置为默认设置
                submitForm.setAnswer("muc#roomconfig_persistentroom", true);

                //发送已完成的表单到服务器配置聊天室 （发送至服务器）
                try {

                    multiUserChat.sendConfigurationForm(submitForm);
                    ToastUtil.showShort(EditGroupInfoActivity.this, "修改成功。"+groupName.getText().toString());
                    intent.putExtra(EDIT_GROUP_CODE_INTENT_VALUE,EDIT_GROUP_CODE_OK);
                    intent.putExtra(EDIT_GROUP_CODE_INTENT_TITLE,groupName.getText().toString());
                } catch (XMPPException | NoResponseException | NotConnectedException e) {
                    e.printStackTrace();
                    ToastUtil.showShort(EditGroupInfoActivity.this, "修改错误，网络异常。");
                    intent.putExtra(EDIT_GROUP_CODE_INTENT_VALUE,EDIT_GROUP_CODE_ERROR);
                }

                EditGroupInfoActivity.this.setResult(EDIT_GROUP_CODE_KEY,intent);
                EditGroupInfoActivity.this.finish();
            }
        });
        btnDeleteGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog groupExitDialog = DialogUtil.getGroupExitDialog(EditGroupInfoActivity.this);
                Button btnOk = (Button) groupExitDialog.findViewById(R.id.group_exit_dialog_btn_ok);
                Button btnCancel = (Button) groupExitDialog.findViewById(R.id.group_exit_dialog_btn_cancel);
                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putExtra(EDIT_GROUP_CODE_INTENT_VALUE,EDIT_GROUP_CODE_REMOVE_ROOM);
                        EditGroupInfoActivity.this.setResult(EDIT_GROUP_CODE_KEY, intent);
                        groupExitDialog.dismiss();
                        EditGroupInfoActivity.this.finish();
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        groupExitDialog.dismiss();
                    }
                });

                groupExitDialog.show();
            }
        });


    }

    private void initView() {
        actionBarBack = (ImageView) findViewById(R.id.actionbar_back);
        acitonBarTitle = (TextView) findViewById(R.id.actionbar_title);
        groupName = (EditText) findViewById(R.id.edit_group_info_text_descript_room_name);
        groupDescript = (EditText) findViewById(R.id.edit_group_info_text_descript);
        groupNumber = (Spinner) findViewById(R.id.edit_group_info_spinner_room_person_number);
        passwordProtect = (Switch) findViewById(R.id.edit_group_info_switch_password_protect);

        groupPassword = (EditText) findViewById(R.id.edit_group_info_text_password);
        btnDeleteGroup = (Button) findViewById(R.id.edit_group_info_btn_delete_group);
        btnSaveInfo = (Button) findViewById(R.id.edit_group_info_btn_save_info);
        layoutPassword = (RelativeLayout) findViewById(R.id.edit_group_info_layout);
        String[] mNumberItems = getResources().getStringArray(R.array.group_info_number_array);
        ArrayAdapter<String> mNumberItemsAdapter=new ArrayAdapter<String>(this,R.layout.group_info_number_item, mNumberItems);
        groupNumber.setAdapter(mNumberItemsAdapter);
        acitonBarTitle.setText("修改聊天室信息");
        actionBarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditGroupInfoActivity.this.finish();
            }
        });
        btnDeleteGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog groupDeleteDialog = DialogUtil.getGroupDeleteDialog(EditGroupInfoActivity.this);
                Button ok = (Button) groupDeleteDialog.findViewById(R.id.dialog_group_list_btn_delete_group_ok);
                Button cancel = (Button) groupDeleteDialog.findViewById(R.id.dialog_group_list_btn_delete_group_cancel);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        groupDeleteDialog.dismiss();
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        groupDeleteDialog.dismiss();
                    }
                });
                groupDeleteDialog.show();

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
