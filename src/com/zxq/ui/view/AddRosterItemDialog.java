package com.zxq.ui.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.zxq.exception.XmppAdressMalformedException;
import com.zxq.util.PreferenceConstants;
import com.zxq.util.PreferenceUtils;
import com.zxq.util.ToastUtil;
import com.zxq.xmpp.R;
import com.zxq.activity.MainActivity;
import com.zxq.service.XmppService;
import com.zxq.util.XMPPHelper;

public class AddRosterItemDialog extends AlertDialog implements DialogInterface.OnClickListener {

	private MainActivity mMainActivity;
	private XmppService mXmppService;

	private Button okButton;
	private EditText userInputField;
	private EditText aliasInputField;
	private GroupNameView mGroupNameView;

	public AddRosterItemDialog(MainActivity mainActivity, XmppService service) {
		super(mainActivity);
		mMainActivity = mainActivity;
		mXmppService = service;

		setTitle(R.string.addFriend_Title);

		LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View group = inflater.inflate(R.layout.addrosteritemdialog, null, false);
		setView(group);

		userInputField = (EditText) group.findViewById(R.id.AddContact_EditTextField);
		aliasInputField = (EditText) group.findViewById(R.id.AddContactAlias_EditTextField);

		mGroupNameView = (GroupNameView) group.findViewById(R.id.AddRosterItem_GroupName);
		mGroupNameView.setGroupList(mMainActivity.getRosterGroups());

		setButton(BUTTON_POSITIVE, mainActivity.getString(android.R.string.ok), this);
		setButton(BUTTON_NEGATIVE, mainActivity.getString(android.R.string.cancel), (DialogInterface.OnClickListener) null);

	}

	public AddRosterItemDialog(MainActivity mainActivity, XmppService service, String jid) {
		this(mainActivity, service);
		userInputField.setText(jid);
	}

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		okButton = getButton(BUTTON_POSITIVE);
	}

	public void onClick(DialogInterface dialog, int which) {
		String xmppUserName = mXmppService.getXmppUserName();
		if(xmppUserName.substring(0,xmppUserName.indexOf("@")).equals(userInputField.getText().toString().trim())){
			ToastUtil.showShort(mMainActivity,"不能将自己添加为好友！");
			return;
		}
		String userJinName = userInputField.getText().toString().trim() +"@"+ PreferenceUtils.getPrefString(this.mMainActivity, PreferenceConstants.Server,PreferenceConstants.DEFAULT_SERVER);
		mXmppService.addRosterItem(userJinName, aliasInputField.getText().toString(), mGroupNameView.getGroupName());
	}



}
