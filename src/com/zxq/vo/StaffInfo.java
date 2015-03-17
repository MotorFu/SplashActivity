package com.zxq.vo;

import java.util.List;

/**
 * Created by zxq on 2014/9/14.
 */
public class StaffInfo extends OrgInfo {
    String jid;
    String alias;
    int statusMode;
    String statusMessage;
    String rosterGroup;

    public StaffInfo() {
    }

    public StaffInfo(String jid, String alias, int statusMode, String statusMessage, String rosterGroup) {
        this.jid = jid;
        this.alias = alias;
        this.statusMode = statusMode;
        this.statusMessage = statusMessage;
        this.rosterGroup = rosterGroup;
    }

    @Override
    public boolean addChildInfo(OrgInfo orgInfo) {
        return false;
    }

    @Override
    public boolean removeChildInfo(OrgInfo orgInfo) {
        return false;
    }

    @Override
    public boolean isDepartmentInfo() {
        return false;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setStatusMode(int statusMode) {
        this.statusMode = statusMode;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public void setRosterGroup(String rosterGroup) {
        this.rosterGroup = rosterGroup;
    }

    public String getJid() {

        return jid;
    }

    public String getAlias() {
        return alias;
    }

    public int getStatusMode() {
        return statusMode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getRosterGroup() {
        return rosterGroup;
    }
}

