package com.zxq.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.zxq.activity.*;
import com.zxq.util.*;
import com.zxq.xmpp.R;
import com.zxq.service.XmppService;
import com.zxq.ui.switcher.Switch;
import com.zxq.ui.view.CustomDialog;

import java.util.logging.Handler;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

public class SettingsFragment extends Fragment implements OnClickListener, OnCheckedChangeListener {
    private static final int PERSON_INFO_REQUEST = 0X122;
    private static final int PERSON_INFO_RESULT = 0X132;
    private static final String PERSON_INFO_KEY = "PERSON_INFO_KEY";
    private TextView mTitleNameView;
    private View mAccountSettingView;
    private ImageView mHeadIcon;
    private ImageView mStatusIcon;
    private TextView mStatusView;
    private TextView mNickView;
    private Switch mShowOfflineRosterSwitch;
    private Switch mNotifyRunBackgroundSwitch;
    private Switch mNewMsgSoundSwitch;
    private Switch mNewMsgVibratorSwitch;
    private Switch mNewMsgLedSwitch;
    private Switch mVisiableNewMsgSwitch;
    private Switch mShowHeadSwitch;
    private Switch mConnectionAutoSwitch;
    private Switch mPoweronReceiverMsgSwitch;
    private Switch mSendCrashSwitch;
    private View mFeedBackView;
    private View mAboutView;
    private Button mExitBtn;
    private Button mLogoutBtn;
    private View mExitMenuView;
    private Button mExitCancleBtn;
    private Button mExitConfirmBtn;
    private FragmentCallBack mFragmentCallBack;

    private Drawable userAvatar;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mFragmentCallBack = (FragmentCallBack) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_settings_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mExitMenuView = LayoutInflater.from(getActivity()).inflate(R.layout.common_menu_dialog_2btn_layout, null);
        mExitCancleBtn = (Button) mExitMenuView.findViewById(R.id.btnCancel);
        mExitConfirmBtn = (Button) mExitMenuView.findViewById(R.id.btn_exit_comfirm);
        mExitConfirmBtn.setText(R.string.exit);
        mExitCancleBtn.setOnClickListener(this);
        mExitConfirmBtn.setOnClickListener(this);
        mTitleNameView = (TextView) view.findViewById(R.id.ivTitleName);
        mTitleNameView.setText(R.string.settings_fragment_title);
        mAccountSettingView = view.findViewById(R.id.accountInfo);
        mAccountSettingView.setOnClickListener(this);
        mHeadIcon = (ImageView) view.findViewById(R.id.face);
        mStatusIcon = (ImageView) view.findViewById(R.id.statusIcon);
        mStatusView = (TextView) view.findViewById(R.id.status);
        mNickView = (TextView) view.findViewById(R.id.nick);
        mShowOfflineRosterSwitch = (Switch) view.findViewById(R.id.show_offline_roster_switch);
        mShowOfflineRosterSwitch.setOnCheckedChangeListener(this);
        mNotifyRunBackgroundSwitch = (Switch) view.findViewById(R.id.notify_run_background_switch);
        mNotifyRunBackgroundSwitch.setOnCheckedChangeListener(this);
        mNewMsgSoundSwitch = (Switch) view.findViewById(R.id.new_msg_sound_switch);
        mNewMsgSoundSwitch.setOnCheckedChangeListener(this);
        mNewMsgVibratorSwitch = (Switch) view.findViewById(R.id.new_msg_vibrator_switch);
        mNewMsgSoundSwitch.setOnCheckedChangeListener(this);
        mNewMsgLedSwitch = (Switch) view.findViewById(R.id.new_msg_led_switch);
        mNewMsgLedSwitch.setOnCheckedChangeListener(this);
        mVisiableNewMsgSwitch = (Switch) view.findViewById(R.id.visiable_new_msg_switch);
        mVisiableNewMsgSwitch.setOnCheckedChangeListener(this);
        mShowHeadSwitch = (Switch) view.findViewById(R.id.show_head_switch);
        mShowHeadSwitch.setOnCheckedChangeListener(this);
        mConnectionAutoSwitch = (Switch) view.findViewById(R.id.connection_auto_switch);
        mConnectionAutoSwitch.setOnCheckedChangeListener(this);
        mPoweronReceiverMsgSwitch = (Switch) view.findViewById(R.id.poweron_receiver_msg_switch);
        mPoweronReceiverMsgSwitch.setOnCheckedChangeListener(this);
        mSendCrashSwitch = (Switch) view.findViewById(R.id.send_crash_switch);
        mSendCrashSwitch.setOnCheckedChangeListener(this);
        mFeedBackView = view.findViewById(R.id.set_feedback);
        mAboutView = view.findViewById(R.id.set_about);
        mExitBtn = (Button) view.findViewById(R.id.exit_app);
        mLogoutBtn = (Button) view.findViewById(R.id.logout_app);

        mFeedBackView.setOnClickListener(this);
        mAboutView.setOnClickListener(this);
        mExitBtn.setOnClickListener(this);
        mLogoutBtn.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        readData();
    }

    private class AsynInitImageThread extends Thread {
        public AsynInitImageThread() {
        }

        @Override
        public void run() {
            super.run();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            XmppService xmppService = mFragmentCallBack.getService();
            if(xmppService == null || !xmppService.isAuthenticated()){
                return;
            }
            VCard vCard = xmppService.getMyInfo();
            if(vCard != null) {
                byte[] userAvatarByte = vCard.getAvatar();
                if (userAvatarByte == null) {
                    SettingsFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mHeadIcon.setImageResource(R.drawable.login_default_avatar);
                        }
                    });
                    return;
                }
                userAvatar = ImageTools.byteToDrawable(vCard.getAvatar());
                if (userAvatar != null) {
                    SettingsFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mHeadIcon.setImageDrawable(userAvatar);
                        }
                    });
                }
            }
        }
    }

    public void readData() {

        AsynInitImageThread asynInitImageThread = new AsynInitImageThread();
        asynInitImageThread.start();

        mStatusIcon.setImageResource(MainActivity.mStatusMap.get(PreferenceUtils.getPrefString(getActivity(), PreferenceConstants.STATUS_MODE, PreferenceConstants.AVAILABLE)));
        mStatusView.setText(PreferenceUtils.getPrefString(getActivity(), PreferenceConstants.STATUS_MESSAGE, getActivity().getString(R.string.status_available)));
        mNickView.setText(XMPPHelper.splitJidAndServer(PreferenceUtils.getPrefString(getActivity(), PreferenceConstants.ACCOUNT, "")));
        mShowOfflineRosterSwitch.setChecked(PreferenceUtils.getPrefBoolean(getActivity(), PreferenceConstants.SHOW_OFFLINE, true));

        mNotifyRunBackgroundSwitch.setChecked(PreferenceUtils.getPrefBoolean(getActivity(), PreferenceConstants.FOREGROUND, true));
        mNewMsgSoundSwitch.setChecked(PreferenceUtils.getPrefBoolean(getActivity(), PreferenceConstants.SCLIENTNOTIFY, false));
        mNewMsgVibratorSwitch.setChecked(PreferenceUtils.getPrefBoolean(getActivity(), PreferenceConstants.VIBRATIONNOTIFY, true));
        mNewMsgLedSwitch.setChecked(PreferenceUtils.getPrefBoolean(getActivity(), PreferenceConstants.LEDNOTIFY, true));
        mVisiableNewMsgSwitch.setChecked(PreferenceUtils.getPrefBoolean(getActivity(), PreferenceConstants.TICKER, true));
        mShowHeadSwitch.setChecked(PreferenceUtils.getPrefBoolean(getActivity(), PreferenceConstants.SHOW_MY_HEAD, true));
        mConnectionAutoSwitch.setChecked(PreferenceUtils.getPrefBoolean(getActivity(), PreferenceConstants.AUTO_RECONNECT, true));
        mPoweronReceiverMsgSwitch.setChecked(PreferenceUtils.getPrefBoolean(getActivity(), PreferenceConstants.AUTO_START, true));
        mSendCrashSwitch.setChecked(PreferenceUtils.getPrefBoolean(getActivity(), PreferenceConstants.REPORT_CRASH, true));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.show_offline_roster_switch) {
            PreferenceUtils.setPrefBoolean(getActivity(), PreferenceConstants.SHOW_OFFLINE, isChecked);
            //mFragmentCallBack.getMainActivity().updateRoster();
        } else if (id == R.id.notify_run_background_switch) {
            PreferenceUtils.setPrefBoolean(getActivity(), PreferenceConstants.FOREGROUND, isChecked);
        } else if (id == R.id.new_msg_sound_switch) {
            PreferenceUtils.setPrefBoolean(getActivity(), PreferenceConstants.SCLIENTNOTIFY, isChecked);
        } else if (id == R.id.new_msg_vibrator_switch) {
            PreferenceUtils.setPrefBoolean(getActivity(), PreferenceConstants.VIBRATIONNOTIFY, isChecked);
        } else if (id == R.id.new_msg_led_switch) {
            PreferenceUtils.setPrefBoolean(getActivity(), PreferenceConstants.LEDNOTIFY, isChecked);
        } else if (id == R.id.visiable_new_msg_switch) {
            PreferenceUtils.setPrefBoolean(getActivity(), PreferenceConstants.TICKER, isChecked);
        } else if (id == R.id.show_head_switch) {
            PreferenceUtils.setPrefBoolean(getActivity(), PreferenceConstants.SHOW_MY_HEAD, isChecked);
        } else if (id == R.id.connection_auto_switch) {
            PreferenceUtils.setPrefBoolean(getActivity(), PreferenceConstants.AUTO_RECONNECT, isChecked);
        } else if (id == R.id.poweron_receiver_msg_switch) {
            PreferenceUtils.setPrefBoolean(getActivity(), PreferenceConstants.AUTO_START, isChecked);
        } else if (id == R.id.send_crash_switch) {
            PreferenceUtils.setPrefBoolean(getActivity(), PreferenceConstants.REPORT_CRASH, isChecked);
        }
    }

    private Dialog mExitDialog;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.set_feedback) {
            startActivity(new Intent(getActivity(), FeedBackActivity.class));
        } else if (id == R.id.set_about) {
            startActivity(new Intent(getActivity(), AboutActivity.class));
        } else if (id == R.id.exit_app) {
            if (mExitDialog == null)
                mExitDialog = DialogUtil.getMenuDialog(getActivity(), mExitMenuView);
            mExitDialog.show();
        } else if (id == R.id.btnCancel) {
            if (mExitDialog != null && mExitDialog.isShowing())
                mExitDialog.dismiss();
        } else if (id == R.id.btn_exit_comfirm) {
            XmppService service = mFragmentCallBack.getService();
            if (service != null) {
                service.logout();// 注销
                service.stopSelf();// 停止服务
            }
            if (mExitDialog.isShowing()) {
                mExitDialog.cancel();
            }
            getActivity().finish();
        } else if (id == R.id.accountInfo) {
            onGoToInfoActivity();

        } else if (id == R.id.logout_app) {
            logoutDialog();

        }
    }

    private void onGoToInfoActivity() {
        Intent intent = new Intent();
        intent.setClass(this.getActivity(), PersonInfoActivity.class);
        startActivityForResult(intent, PERSON_INFO_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERSON_INFO_REQUEST) {
            if (requestCode == PERSON_INFO_RESULT) {
                //TODO:返回结果处理
                data.getStringExtra(PERSON_INFO_KEY);
            }
        }
    }

    public void logoutDialog() {
        new CustomDialog.Builder(getActivity()).setTitle(getActivity().getString(R.string.open_switch_account)).setMessage(getActivity().getString(R.string.open_switch_account_msg)).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                XmppService service = mFragmentCallBack.getService();
                if (service != null) {
                    service.logout();// 注销
                }
                dialog.dismiss();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }
}
