package com.zxq.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.zxq.app.XmppBroadcastReceiver;
import com.zxq.service.XmppService;
import com.zxq.util.*;
import com.zxq.xmpp.R;

/**
 * Created by zxq on 2014/9/15.
 */
public class RegisterActivity extends Activity {
    private ImageView actionBarBack;
    private TextView acitonBarTitle;

    private EditText inputAccount;
    private EditText inputPassword;
    private EditText inputAgainPassword;
    private Button btnRegister;
    private View.OnClickListener clickListener;

    private XmppService mXmppService;

    private String account = "";
    private String password = "";
    private String passwordAgain = "";

    XmppService.IRegisterCallBack iRegisterCallBack;

    ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mXmppService = ((XmppService.XXBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mXmppService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
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


    private void initView() {
        actionBarBack = (ImageView) findViewById(R.id.actionbar_back);
        acitonBarTitle = (TextView) findViewById(R.id.actionbar_title);
        inputAccount = (EditText) findViewById(R.id.register_input_account);
        inputPassword = (EditText) findViewById(R.id.register_input_password);
        inputAgainPassword = (EditText) findViewById(R.id.register_input_password_again);
        btnRegister = (Button) findViewById(R.id.register_btn_register);
        acitonBarTitle.setText("用户注册");
        actionBarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterActivity.this.finish();
            }
        });
        iRegisterCallBack = new XmppService.IRegisterCallBack() {
            @Override
            public void onRegisterResult(int resultCode) {
//此函数返回值解释 1：注册成功 0：服务器没有返回结果 2：这个账号已经存在 3.注册失败
                switch (resultCode) {
                    case 0:
                        RegisterActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showShort(RegisterActivity.this.getBaseContext(), "服务器没有响应，请重试！");
                            }
                        });
                        break;
                    case 1:
                        RegisterActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showShort(RegisterActivity.this.getBaseContext(), "注册成功！");
                                Intent intent = new Intent();
                                intent.putExtra(LoginActivity.RESULT_REGISTER_KEY, account);
                                setResult(LoginActivity.RESULT_REGISTER_SUCESS, intent);
                                finish();
                            }
                        });
                        break;
                    case 2:
                        RegisterActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showShort(RegisterActivity.this.getBaseContext(), "这个账号已存在！");
                            }
                        });
                        break;
                    case 3:
                        RegisterActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showShort(RegisterActivity.this.getBaseContext(), "注册失败！");
                            }
                        });
                        break;
                    default:
                        RegisterActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showShort(RegisterActivity.this.getBaseContext(), "未知异常！");
                            }
                        });
                        break;
                }
            }
        };
        clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                account = inputAccount.getText().toString();
                password = inputPassword.getText().toString();
                passwordAgain = inputAgainPassword.getText().toString();
                int status = -1;
                if ("".equals(account.trim())) {
                    ToastUtil.showShort(RegisterActivity.this, "用户名不能为空！");
                    return;
                } else {
                    if ("".equals(password.trim())) {
                        ToastUtil.showShort(RegisterActivity.this, "密码不能为空！");
                        return;
                    } else {
                        if ("".equals(passwordAgain.trim())) {
                            ToastUtil.showShort(RegisterActivity.this, "确认密码不能为空！");
                            return;
                        } else {
                            if(password.length() < 6){
                                ToastUtil.showShort(RegisterActivity.this, "密码长度必须大于6位！");
                                return;
                            }else{
                                if (!password.equals(passwordAgain)) {
                                    ToastUtil.showShort(RegisterActivity.this, "密码与确认密码不一致！");
                                    return;
                                } else {
                                    mXmppService.registerAccount(account, password, iRegisterCallBack);
                                }
                            }
                        }
                    }
                }

            }
        };
        btnRegister.setOnClickListener(clickListener);
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
                        PreferenceUtils.setPrefString(RegisterActivity.this, PreferenceConstants.Server, inputEditText.getText().toString());
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

    @Override
    protected void onResume() {
        super.onResume();
        bindXMPPService();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindXMPPService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
