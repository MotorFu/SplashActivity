package com.zxq.vo;

import java.io.Serializable;

/**
 * Created by zxq on 2014/12/1.
 */
public class SerializationOccupant implements Serializable {
    private String jid;
    private String nick;
    private String affiliation;
    private String role;

    public SerializationOccupant() {
    }

    public SerializationOccupant(String jid, String nick, String affiliation, String role) {
        this.jid = jid;
        this.nick = nick;
        this.affiliation = affiliation;
        this.role = role;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
