package com.zxq.activity;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zxq.service.XmppService;
import com.zxq.util.LogUtil;
import com.zxq.util.ToastUtil;
import com.zxq.util.VCardConstants;
import com.zxq.vo.PersonEntityInfo;
import com.zxq.xmpp.R;

/**
 * Created by zxq on 2014/9/16.
 */
public class EditPersonInfoActivity extends Activity {
    private ImageView actionBarBack;
    private TextView acitonBarTitle;

    private EditText inputName;
    private EditText inputSignature;
    private EditText inputQQ;
    private EditText inputPhone;
    private EditText inputEmail;
    private Button btnSaveInfo;

    private PersonEntityInfo personEntityInfo;
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
        setContentView(R.layout.activity_edit_person_info);
        initView();
    }

    private void setupData() {
        VCard myInfo = mXmppService.getMyInfo();
        personEntityInfo = new PersonEntityInfo();
        personEntityInfo.setName(myInfo.getField(VCardConstants.KEY_NIKENAME));
        personEntityInfo.setSignature(myInfo.getField(VCardConstants.KEY_SIGNATURE));
        personEntityInfo.setQq(myInfo.getField(VCardConstants.KEY_QQ));
        personEntityInfo.setPhone(myInfo.getField(VCardConstants.KEY_PHONE));
        personEntityInfo.setEmail(myInfo.getField(VCardConstants.KEY_EMAIL));
        inputName.setText(personEntityInfo.getName());
        inputSignature.setText(personEntityInfo.getSignature());
        inputQQ.setText(personEntityInfo.getQq());
        inputPhone.setText(personEntityInfo.getPhone());
        inputEmail.setText(personEntityInfo.getEmail());
        btnSaveInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ok = mXmppService.saveMyInfo(inputName.getText().toString(),inputSignature.getText().toString(),inputQQ.getText().toString(),inputPhone.getText().toString(),inputEmail.getText().toString());
                if (ok == true){
                    ToastUtil.showShort(EditPersonInfoActivity.this,"修改成功！");
                }else{
                    ToastUtil.showShort(EditPersonInfoActivity.this,"修改失败！");
                }
            }
        });
    }

    private void initView() {
        actionBarBack = (ImageView) findViewById(R.id.actionbar_back);
        acitonBarTitle = (TextView) findViewById(R.id.actionbar_title);
        inputName = (EditText) findViewById(R.id.edit_person_info_text_name);
        inputSignature = (EditText) findViewById(R.id.edit_person_info_text_signature);
        inputQQ = (EditText) findViewById(R.id.edit_person_info_text_qq);
        inputPhone = (EditText) findViewById(R.id.edit_person_info_text_phone);
        inputEmail = (EditText) findViewById(R.id.edit_person_info_text_email);
        btnSaveInfo = (Button) findViewById(R.id.edit_person_info_btn_save_info);
        acitonBarTitle.setText("修改用户信息");
        actionBarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPersonInfoActivity.this.finish();
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
