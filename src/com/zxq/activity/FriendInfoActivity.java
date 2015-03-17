package com.zxq.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zxq.service.XmppService;
import com.zxq.util.ImageTools;
import com.zxq.util.LogUtil;
import com.zxq.util.ToastUtil;
import com.zxq.util.VCardConstants;
import com.zxq.vo.PersonEntityInfo;
import com.zxq.xmpp.R;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

/**
 * Created by zxq on 2014/9/15.
 */
public class FriendInfoActivity extends Activity {

    public static final String FRIEND_JID_KEY = "FRIEND_JID_KEY";

    private ImageView actionBarBack;
    private TextView acitonBarTitle;
    private PersonEntityInfo personEntityInfo;
    private ImageView imageIcon;
    private TextView textAccount;
    private TextView textName;
    private TextView textSignature;
    private TextView textQQ;
    private TextView textPhone;
    private TextView textEmail;
    private XmppService mXmppService;
    private String account;

   private AsynLoadImageThread asynLoadImageThread;


    private static final int TAKE_PICTURE = 0;
    private static final int CHOOSE_PICTURE = 1;
    private static final int CROP = 2;
    private static final int CROP_PICTURE = 3;

    private interface IInitImageCallBack{
        void onSucessInitImage(Drawable userAvatar);
        void onFailInitImage();
    }

    private class AsynLoadImageThread extends Thread {
        String filePath;
        Bitmap photo;
        public AsynLoadImageThread(String filePath,Bitmap photo) {
            super();
            this.filePath = filePath;
            this.photo = photo;
        }

        @Override
        public void run() {
            super.run();
            //Runnable通知主线程更新UI，修改用户更改后头像
            mXmppService.changeImage(FriendInfoActivity.this,filePath,new Runnable(){
                @Override
                public void run() {
                   //这样子做是为了在UI里面更新控件，防止报在其他线程更新主UI控件的问题
                    FriendInfoActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageIcon.setImageBitmap(photo);
                        }
                    });
                }
            },new Runnable(){
                @Override
                public void run() {
                    ToastUtil.showShort(FriendInfoActivity.this,"图片修改不成功，请检查网络！");
                }
            });
        }
    }

    private class AsynInitImageThread extends Thread {
        VCard vCard;
        IInitImageCallBack iInitImageCallBack;
        public AsynInitImageThread(VCard vCard,IInitImageCallBack iInitImageCallBack){
            this.vCard = vCard;
            this.iInitImageCallBack = iInitImageCallBack;
        }
        @Override
        public void run() {
            super.run();
            byte[] userAvatarByte = vCard.getAvatar();
            if(userAvatarByte == null){
                return;
            }
            Drawable userAvatar =  ImageTools.byteToDrawable(vCard.getAvatar());
            if(userAvatar != null) {
                iInitImageCallBack.onSucessInitImage(userAvatar);
            } else{
                iInitImageCallBack.onFailInitImage();
            }
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

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
        setContentView(R.layout.activity_friend_info);
        initView();

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
        LogUtil.i(RegisterActivity.class, "[SERVICE] Bind");
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

    private void setupData() {
        String jid = getIntent().getStringExtra(FRIEND_JID_KEY);
        ToastUtil.showShort(this,jid);
        VCard myInfo = mXmppService.getUserAvatarByName(jid);
        personEntityInfo = new PersonEntityInfo();
        account = jid;
        personEntityInfo.setName(myInfo.getField(VCardConstants.KEY_NIKENAME));
        personEntityInfo.setSignature(myInfo.getField(VCardConstants.KEY_SIGNATURE));
        personEntityInfo.setQq(myInfo.getField(VCardConstants.KEY_QQ));
        personEntityInfo.setPhone(myInfo.getField(VCardConstants.KEY_PHONE));
        personEntityInfo.setEmail(myInfo.getField(VCardConstants.KEY_EMAIL));

        AsynInitImageThread asynInitImageThread = new AsynInitImageThread(myInfo,new IInitImageCallBack() {
            @Override
            public void onSucessInitImage(final Drawable userAvatar) {
                FriendInfoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageIcon.setImageDrawable(userAvatar);
                    }
                });
            }
            @Override
            public void onFailInitImage() {
            }
        });
        asynInitImageThread.start();
        textAccount.setText(account);
        textName.setText(personEntityInfo.getName() == null ? "(空)" : personEntityInfo.getName());
        textSignature.setText(personEntityInfo.getSignature() == null ? "(空)" : personEntityInfo.getSignature());
        textQQ.setText(personEntityInfo.getQq() == null ? "(空)" : personEntityInfo.getQq());
        textPhone.setText(personEntityInfo.getPhone() == null ? "(空)" : personEntityInfo.getPhone());
        textEmail.setText(personEntityInfo.getEmail() == null ? "(空)" : personEntityInfo.getEmail());
    }


    private void initView() {
        actionBarBack = (ImageView) findViewById(R.id.actionbar_back);
        acitonBarTitle = (TextView) findViewById(R.id.actionbar_title);
        imageIcon = (ImageView) findViewById(R.id.person_info_icon);
        textAccount = (TextView) findViewById(R.id.person_info_text_account);
        textName = (TextView) findViewById(R.id.person_info_text_name);
        textSignature = (TextView) findViewById(R.id.person_info_text_signature);
        textQQ = (TextView) findViewById(R.id.person_info_text_qq);
        textPhone = (TextView) findViewById(R.id.person_info_text_phone);
        textEmail = (TextView) findViewById(R.id.person_info_text_email);
        acitonBarTitle.setText("个人信息");
        actionBarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendInfoActivity.this.finish();
            }
        });
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
