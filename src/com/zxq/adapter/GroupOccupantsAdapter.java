package com.zxq.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.zxq.vo.SerializationOccupant;
import com.zxq.xmpp.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by zxq on 2014/12/1.
 */
public class GroupOccupantsAdapter extends BaseAdapter {
    private List<SerializationOccupant> occupantsList;
    private HashMap<String,Boolean> checked = new HashMap<String, Boolean>();
    private Context context;

    public void setOccupantsChooselistener(OccupantsChooselistener occupantsChooselistener) {
        this.occupantsChooselistener = occupantsChooselistener;
    }

    public OccupantsChooselistener occupantsChooselistener;

    public static interface OccupantsChooselistener{
        void onCheckedOccupants(HashMap<String,Boolean> checkedArray);
    }


    public List<SerializationOccupant> getGroupList() {
        return occupantsList;
    }

    public GroupOccupantsAdapter(Context context, List<SerializationOccupant> groupList) {
        this.occupantsList = groupList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return occupantsList.size();
    }

    @Override
    public Object getItem(int position) {
        return occupantsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final GroupHolder groupHolder;
        final SerializationOccupant occupantsEntry = occupantsList.get(position);
        if (convertView == null) {
            groupHolder = new GroupHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_group_occupants_listview_item, null);
            groupHolder.occupantsIcon = (ImageView) convertView.findViewById(R.id.group_occupants_item_icon);
            groupHolder.occupantsName = (TextView) convertView.findViewById(R.id.group_occupants_item_title);
            groupHolder.occupantCheck = (CheckBox) convertView.findViewById(R.id.group_occupants_item_checked);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }

        Boolean aBoolean = checked.get(occupantsEntry.getJid());
        if(aBoolean == null){
            checked.put(occupantsEntry.getNick(),false);
            groupHolder.occupantCheck.setChecked(false);
        }else{
            checked.put(occupantsEntry.getNick(),aBoolean);
            groupHolder.occupantCheck.setChecked(aBoolean);
        }

        groupHolder.occupantCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean aBoolean = checked.get(occupantsEntry.getNick());
                checked.put(occupantsEntry.getNick(),!aBoolean);

                if(occupantsChooselistener != null){
                    occupantsChooselistener.onCheckedOccupants(checked);
                }
            }
        });
        groupHolder.occupantsName.setText(occupantsEntry.getNick());
        return convertView;
    }

    private class GroupHolder {
        public ImageView occupantsIcon;
        public TextView occupantsName;
        public CheckBox occupantCheck;
    }
}