package com.zxq.activity;

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
import com.zxq.util.*;
import com.zxq.vo.PersonEntityInfo;
import com.zxq.xmpp.R;

/**
 * Created by zxq on 2014/9/16.
 */
public class EditPersonPasswordActivity extends Activity {
    private ImageView actionBarBack;
    private TextView acitonBarTitle;

    private EditText inputOld;
    private EditText inputNew;
    private EditText inputNewAgain;
    private Button btnAlterPassword;


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
        setContentView(R.layout.activity_edit_person_password);
        initView();
    }

    private void setupData() {
        btnAlterPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int resultCode = mXmppService.alterPersonPassword(inputOld.getText().toString(),inputNew.getText().toString(),inputNewAgain.getText().toString());
                switch (resultCode){
                    case -7:
                        ToastUtil.showShort(EditPersonPasswordActivity.this,"新密码长度必须大于6位！");
                        break;
                    case -6:
                        ToastUtil.showShort(EditPersonPasswordActivity.this,"确认密码不能为空！");
                        break;
                    case -5:
                        ToastUtil.showShort(EditPersonPasswordActivity.this,"新密码不能为空！");
                        break;
                    case -4:
                        ToastUtil.showShort(EditPersonPasswordActivity.this,"原密码不能为空！");
                        break;
                    case -3:
                        ToastUtil.showShort(EditPersonPasswordActivity.this,"未知异常！");
                        break;
                    case -2:
                        ToastUtil.showShort(EditPersonPasswordActivity.this,"新密码和确认密码不一致！");
                        break;
                    case -1:
                        ToastUtil.showShort(EditPersonPasswordActivity.this,"原密码不正确！");
                        break;
                    case 0:
                        ToastUtil.showShort(EditPersonPasswordActivity.this,"新密码和旧密码不能相同！");
                        break;
                    case 1:
                        ToastUtil.showShort(EditPersonPasswordActivity.this,"修改成功！");
                        if (mXmppService != null) {
                            mXmppService.logout();// 注销
                        }
                        PreferenceUtils.setPrefString(EditPersonPasswordActivity.this, PreferenceConstants.PASSWORD,"");
                        startActivity(new Intent(EditPersonPasswordActivity.this, LoginActivity.class));
                        EditPersonPasswordActivity.this.finish();
                        break;
                }
            }
        });
    }

    private void initView() {
        actionBarBack = (ImageView) findViewById(R.id.actionbar_back);
        acitonBarTitle = (TextView) findViewById(R.id.actionbar_title);
        inputOld = (EditText) findViewById(R.id.edit_person_password_old);
        inputNew = (EditText) findViewById(R.id.edit_person_password_new);
        inputNewAgain = (EditText) findViewById(R.id.edit_person_password_new_again);
        btnAlterPassword = (Button) findViewById(R.id.edit_person_password_btn);
        acitonBarTitle.setText("修改密码");
        actionBarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPersonPasswordActivity.this.finish();
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
