package com.openfire.imchat.beans;

import android.app.Activity;

import com.openfire.imchat.activity.ChatActivity;

public class CurrentUser {
    private static CurrentUser cu;
    private Activity chatActivity;

    String cuser; //当前登录用户

    String cfriend;//当前对话朋友
    private CurrentUser() {

    }

    //单例模式
    public static CurrentUser getInstance() {
        if (cu == null) {
            cu = new CurrentUser();
        }
        return cu;
    }


    public String getCuser() { return cuser; }
    public  void  setCuser(String cuser){ this.cuser = cuser; }

    public  String getCfriend(){return  cfriend;}
    public void  setCfriend(String cfriend){ this.cfriend = cfriend;}

    public Activity getChatActivity() { return chatActivity; }

    public void setChatActivity(Activity chatActivity) { this.chatActivity = chatActivity; }
}
