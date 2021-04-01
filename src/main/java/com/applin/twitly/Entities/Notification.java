package com.applin.twitly.Entities;

public class Notification {

    private String sender;
    private String postid;
    private String receiver;
    private String action;
    private boolean pushed;

    public Notification(String sender, String postid, String receiver, String action, boolean pushed) {
        this.sender = sender;
        this.postid = postid;
        this.receiver = receiver;
        this.action = action;
        this.pushed = pushed;
    }

    public Notification() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isPushed() {
        return pushed;
    }

    public void setPushed(boolean pushed) {
        this.pushed = pushed;
    }
}
