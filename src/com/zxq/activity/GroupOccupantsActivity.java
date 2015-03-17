package com.zxq.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.zxq.adapter.GroupOccupantsAdapter;
import com.zxq.service.XmppService;
import com.zxq.util.LogUtil;
import com.zxq.vo.SerializationOccupant;
import com.zxq.xmpp.R;

import java.util.*;

/**
 * Created by zxq on 2014/11/9.
 */
public class GroupOccupantsActivity extends Activity {
    private ImageView actionBarBack;
    private TextView acitonBarTitle;
    private GroupOccupantsAdapter mAdapter;
    private HashMap<String,Boolean> checked;
    private Button btnCheck;

    public static final String CHOOSE_OCCUPANTS_NIKENAMES="CHOOSE_OCCUPANTS_NIKENAMES";
    public static final int CHOOSE_OCCUPANTS_RESULT_CODE= 0X1111;

    private ArrayList<SerializationOccupant> mRoomMenberList;

    private ListView mGroupOccupantsListView;






    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_group_occupants);
        actionBarBack = (ImageView) findViewById(R.id.actionbar_back);
        acitonBarTitle = (TextView) findViewById(R.id.actionbar_title);
        acitonBarTitle.setText("选择要踢出的用户");
        actionBarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupOccupantsActivity.this.finish();
            }
        });
        initView();
        setupDate();
    }

    private void setupDate() {
        mRoomMenberList = (ArrayList<SerializationOccupant>) getIntent().getSerializableExtra(GroupChatActivity.MULTI_USER_CHAT_ROOM_OCCUPANTS);

        mAdapter = new GroupOccupantsAdapter(this,mRoomMenberList);
        mAdapter.setOccupantsChooselistener(new GroupOccupantsAdapter.OccupantsChooselistener() {
            @Override
            public void onCheckedOccupants(HashMap<String, Boolean> checkedArray) {
                GroupOccupantsActivity.this.checked = checkedArray;
                if(checkedArray.containsValue(true)){
                    btnCheck.setBackgroundDrawable(getResources().getDrawable(R.drawable.common_btn_green));
                }else{
                    btnCheck.setBackgroundDrawable(getResources().getDrawable(R.drawable.common_btn_black_actionsheet));
                }
            }
        });
        mGroupOccupantsListView.setAdapter(mAdapter);
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuffer sb = new StringBuffer();
                Set<Map.Entry<String, Boolean>> entries = checked.entrySet();
                for(Map.Entry<String, Boolean> temp : entries){
                    Boolean value = temp.getValue();
                    if(value == true){
                        sb.append(temp.getKey()+",");
                    }
                }
                Intent intent = new Intent();
                intent.putExtra(CHOOSE_OCCUPANTS_NIKENAMES, sb.toString());
                setResult(CHOOSE_OCCUPANTS_RESULT_CODE,intent);
                GroupOccupantsActivity.this.finish();
            }
        });
        mAdapter.notifyDataSetChanged();
    }

    private void initView() {
        mGroupOccupantsListView = (ListView) findViewById(R.id.group_occupants_listview);
        btnCheck = (Button) findViewById(R.id.group_occupants_btn_check);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }




}