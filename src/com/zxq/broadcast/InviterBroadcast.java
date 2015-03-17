package com.zxq.broadcast;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.zxq.activity.GroupChatActivity;
import com.zxq.activity.MainActivity;
import com.zxq.fragment.GroupChatFragment;
import com.zxq.util.DialogUtil;
import com.zxq.util.LogUtil;
import com.zxq.util.ToastUtil;
import com.zxq.xmpp.R;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * Created by zxq on 2014/12/1.
 */
public class InviterBroadcast extends BroadcastReceiver {
    Activity activity;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        activity = MainActivity.getMainContext();
        final String room = intent.getStringExtra("room");
        final String inviter = intent.getStringExtra("inviter");
        final String password = intent.getStringExtra("password");
        final String reason = intent.getStringExtra("reason");
        final Dialog groupInviterDialog = DialogUtil.getGroupInviterDialog(activity);
        TextView roomName = (TextView) groupInviterDialog.findViewById(R.id.group_inviter_dialog_text_room_name);
        TextView roomInviter = (TextView) groupInviterDialog.findViewById(R.id.group_inviter_dialog_text_room_inviter);
        TextView roomReason = (TextView) groupInviterDialog.findViewById(R.id.group_inviter_dialog_text_room_reason);
        Button btnInviter = (Button) groupInviterDialog.findViewById(R.id.group_inviter_dialog_btn_inviter);
        Button btnReject = (Button) groupInviterDialog.findViewById(R.id.group_inviter_dialog_btn_reject);
        roomName.setText(roomName.getText() + room.substring(0,room.indexOf("@")));
        roomInviter.setText(roomInviter.getText()+inviter.substring(0,inviter.indexOf("@")));
        roomReason.setText(roomReason.getText()+reason.substring(reason.indexOf(":")+1));
        btnInviter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toward = new Intent();
                toward.putExtra(GroupChatFragment.GROUP_CHAT_ROOM_JID, room);
                toward.putExtra(GroupChatFragment.GROUP_CHAT_ROOM_NAME,room.substring(0,room.indexOf("@")));
                toward.putExtra(GroupChatFragment.GROUP_CHAT_ROOM_PASD, password);
                toward.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                toward.setClass(activity, GroupChatActivity.class);
                LogUtil.e(room+":"+inviter+":"+password+":"+inviter+":"+reason);
                activity.startActivity(toward);
                groupInviterDialog.dismiss();
              }
        });
        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupInviterDialog.dismiss();
            }
        });

        groupInviterDialog.show();

    }
}
