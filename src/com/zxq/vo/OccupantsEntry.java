package com.zxq.vo;

/**
 * Created by zxq on 2014/10/12.
 */
public class OccupantsEntry {
    private String jid;
    private String iconUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;


    public OccupantsEntry() {
    }

    public OccupantsEntry(String jid, String iconUrl, String name) {
        this.jid = jid;
        this.iconUrl = iconUrl;
        this.name = name;
    }

    public OccupantsEntry(String iconUrl, String title) {
        this.iconUrl = iconUrl;
        this.name = title;
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

    public String getIconUrl() {

        return iconUrl;
    }


}
