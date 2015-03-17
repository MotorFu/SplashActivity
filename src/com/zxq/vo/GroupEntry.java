package com.zxq.vo;

/**
 * Created by zxq on 2014/10/12.
 */
public class GroupEntry {
    private String jid;
    private String iconUrl;
    private String title;

    public boolean isLock() {
        return isLock;
    }

    public void setLock(boolean isLock) {
        this.isLock = isLock;
    }

    private boolean isLock;

    public int getOccupantsNumber() {
        return occupantsNumber;
    }

    public void setOccupantsNumber(int occupantsNumber) {
        this.occupantsNumber = occupantsNumber;
    }

    private int occupantsNumber;
    public GroupEntry() {
    }

    public GroupEntry(String jid, String iconUrl, String title,boolean isLock,int occupantsNumber) {
        this.jid = jid;
        this.iconUrl = iconUrl;
        this.title = title;
        this.isLock  = isLock;
        this.occupantsNumber =occupantsNumber;
    }

    public GroupEntry(String iconUrl, String title) {
        this.iconUrl = iconUrl;
        this.title = title;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIconUrl() {

        return iconUrl;
    }

    public String getTitle() {
        return title;
    }

}
