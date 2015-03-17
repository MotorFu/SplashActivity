package com.zxq.activity;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import android.widget.TextView;
import com.zxq.util.*;
import com.zxq.xmpp.R;
import com.zxq.service.IConnectionStatusCallback;
import com.zxq.service.XmppService;
import com.zxq.util.LogUtil;

public class LoginActivity extends FragmentActivity implements IConnectionStatusCallback {
    public static final String LOGIN_ACTION = "com.zxq.action.LOGIN";
    private static final int LOGIN_OUT_TIME = 0;
    public static final int RESULT_FOR_REGISTER = 0X110 ;
    public static final int RESULT_REGISTER_SUCESS = 0X120 ;
    public static final String RESULT_REGISTER_KEY = "result_register_key";
    private EditText mAccountEt;
    private EditText mPasswordEt;
    private TextView mRegister;
    private CheckBox mAutoSavePasswordCK;
    private CheckBox mHideLoginCK;
    private CheckBox mUseTlsCK;
    private CheckBox mSilenceLoginCK;
    private XmppService mXmppService;
    private Dialog mLoginDialog;
    private ConnectionOutTimeProcess mLoginOutTimeProcess;
    private String mAccount;
    private String mPassword;


    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOGIN_OUT_TIME:
                    if (mLoginOutTimeProcess != null && mLoginOutTimeProcess.running)
                        mLoginOutTimeProcess.stop();
                    if (mLoginDialog != null && mLoginDialog.isShowing())
                        mLoginDialog.dismiss();
                    ToastUtil.showShort(LoginActivity.this, R.string.timeout_try_again);
                    break;

                default:
                    break;
            }
        }

    };
    ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mXmppService = ((XmppService.XXBinder) service).getService();
            mXmppService.registerConnectionStatusCallback(LoginActivity.this);
            // 开始连接xmpp服务器
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mXmppService.unRegisterConnectionStatusCallback();
            mXmppService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(LoginActivity.this, XmppService.class));
        bindXMPPService();
        setContentView(R.layout.loginpage);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.equals(PreferenceUtils.getPrefString(this, PreferenceConstants.APP_VERSION, ""), getString(R.string.app_version)) && !TextUtils.isEmpty(PreferenceUtils.getPrefString(this, PreferenceConstants.ACCOUNT, ""))) {
            PreferenceUtils.setPrefString(this, PreferenceConstants.APP_VERSION, getString(R.string.app_version));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindXMPPService();
        if (mLoginOutTimeProcess != null) {
            mLoginOutTimeProcess.stop();
            mLoginOutTimeProcess = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_login_server:
                final Dialog inputDialog = DialogUtil.getInputDialog(this);
                final EditText inputEditText = (EditText) inputDialog.findViewById(R.id.edit_login_server_input);
                String server = PreferenceUtils.getPrefString(this, PreferenceConstants.Server, "");
                inputEditText.setText(server);
                Button inputOkBtn = (Button) inputDialog.findViewById(R.id.btn_login_server_ok);
                Button inputCancelBtn = (Button) inputDialog.findViewById(R.id.btn_login_server_cancel);
                inputOkBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PreferenceUtils.setPrefString(LoginActivity.this, PreferenceConstants.Server, inputEditText.getText().toString());
                        inputDialog.dismiss();
                    }
                });
                inputCancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        inputDialog.dismiss();
                    }
                });
                inputDialog.show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        mAutoSavePasswordCK = (CheckBox) findViewById(R.id.auto_save_password);
        mHideLoginCK = (CheckBox) findViewById(R.id.hide_login);
        mSilenceLoginCK = (CheckBox) findViewById(R.id.silence_login);
        mUseTlsCK = (CheckBox) findViewById(R.id.use_tls);
        mAccountEt = (EditText) findViewById(R.id.account_input);
        mPasswordEt = (EditText) findViewById(R.id.password);
        String account = PreferenceUtils.getPrefString(this, PreferenceConstants.ACCOUNT, "");
        String password = PreferenceUtils.getPrefString(this, PreferenceConstants.PASSWORD, "");
        if (!TextUtils.isEmpty(account))
            mAccountEt.setText(account);
        if (!TextUtils.isEmpty(password))
            mPasswordEt.setText(password);
        mRegister = (TextView) findViewById(R.id.login_btn_go_to_register);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGoToRegister();
            }
        });
        mLoginDialog = DialogUtil.getLoginDialog(this);
        mLoginOutTimeProcess = new ConnectionOutTimeProcess();
    }

    public void onLoginClick(View v) {
        mAccount = mAccountEt.getText().toString();
        mPassword = mPasswordEt.getText().toString();
        if (TextUtils.isEmpty(mAccount)) {
            ToastUtil.showShort(this, R.string.null_account_prompt);
            return;
        }
        if (TextUtils.isEmpty(mPassword)) {
            ToastUtil.showShort(this, R.string.password_input_prompt);
            return;
        }
        if (mLoginOutTimeProcess != null && !mLoginOutTimeProcess.running)
            mLoginOutTimeProcess.start();
        if (mLoginDialog != null && !mLoginDialog.isShowing())
            mLoginDialog.show();
        if (mXmppService != null) {
            mXmppService.login(mAccount, mPassword);
        }
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
        LogUtil.i(LoginActivity.class, "[SERVICE] Unbind");
        Intent mServiceIntent = new Intent(this, XmppService.class);
        mServiceIntent.setAction(LOGIN_ACTION);
        bindService(mServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
    }

    private void save2Preferences() {
        boolean isAutoSavePassword = mAutoSavePasswordCK.isChecked();
        boolean isUseTls = mUseTlsCK.isChecked();
        boolean isSilenceLogin = mSilenceLoginCK.isChecked();
        boolean isHideLogin = mHideLoginCK.isChecked();
        PreferenceUtils.setPrefString(this, PreferenceConstants.ACCOUNT, mAccount);// 帐号是一直保存的
        PreferenceUtils.setPrefString(this, PreferenceConstants.PASSWORD, mPassword);
//        if (isAutoSavePassword)
//            PreferenceUtils.setPrefString(this, PreferenceConstants.PASSWORD, mPassword);
//        else
//            PreferenceUtils.setPrefString(this, PreferenceConstants.PASSWORD, "");
        PreferenceUtils.setPrefBoolean(this, PreferenceConstants.REQUIRE_TLS, isUseTls);
        PreferenceUtils.setPrefBoolean(this, PreferenceConstants.SCLIENTNOTIFY, isSilenceLogin);
        if (isHideLogin)
            PreferenceUtils.setPrefString(this, PreferenceConstants.STATUS_MODE, PreferenceConstants.XA);
        else
            PreferenceUtils.setPrefString(this, PreferenceConstants.STATUS_MODE, PreferenceConstants.AVAILABLE);
    }

    // 登录超时处理线程
    class ConnectionOutTimeProcess implements Runnable {
        public boolean running = false;
        private long startTime = 0L;
        private Thread thread = null;

        ConnectionOutTimeProcess() {
        }

        public void run() {
            while (true) {
                if (!this.running)
                    return;
                if (System.currentTimeMillis() - this.startTime > 20 * 1000L) {
                    mHandler.sendEmptyMessage(LOGIN_OUT_TIME);
                }
                try {
                    Thread.sleep(10L);
                } catch (Exception localException) {
                }
            }
        }

        public void start() {
            try {
                this.thread = new Thread(this);
                this.running = true;
                this.startTime = System.currentTimeMillis();
                this.thread.start();
            } finally {
            }
        }

        public void stop() {
            try {
                this.running = false;
                this.thread = null;
                this.startTime = 0L;
            } finally {
            }
        }
    }

    @Override
    public void connectionStatusChanged(int connectedState, String reason) {
        if (mLoginDialog != null && mLoginDialog.isShowing())
            mLoginDialog.dismiss();
        if (mLoginOutTimeProcess != null && mLoginOutTimeProcess.running) {
            mLoginOutTimeProcess.stop();
            mLoginOutTimeProcess = null;
        }
        if (connectedState == XmppService.CONNECTED) {
            save2Preferences();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (connectedState == XmppService.DISCONNECTED)
            ToastUtil.showLong(LoginActivity.this, getString(R.string.request_failed) + reason);
    }

    public void onGoToRegister(){
        Intent intent = new Intent();
        intent.setClass(this,RegisterActivity.class);
        startActivityForResult(intent,RESULT_FOR_REGISTER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_FOR_REGISTER){
            if(resultCode == RESULT_REGISTER_SUCESS){
                String newAccount =data.getStringExtra(RESULT_REGISTER_KEY);
                mAccountEt.setText(newAccount);
            }
        }
    }
}
