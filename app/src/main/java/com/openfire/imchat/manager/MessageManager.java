package com.openfire.imchat.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.openfire.imchat.activity.ChatActivity;
import com.openfire.imchat.beans.CurrentUser;


import com.openfire.imchat.beans.IMessage;
import com.openfire.imchat.util.TimeUtils;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageManager {
    CurrentUser cfriend;
    MyHelper myHelper;


    AbstractXMPPConnection connection = ConnectionManager.getConnection();
    // 聊天管理器
    ChatManager chatManager = ChatManager.getInstanceFor(connection);

    ChatActivity chatActivity;
        Message  mm;
    /**
     * 信息监听
     * */
    public void messListener() {
        //cfriend = new CurrentUser();
        chatActivity = new ChatActivity();
        myHelper = new MyHelper(null);
        // XMPPConnection con = SE.getInstance().getCon();
        new Thread(new Runnable() {
            public void run() {
                                chatManager.addChatListener(new ChatManagerListener() {

                                        @Override
                                  public void chatCreated(Chat chat, boolean createdLocally) {
                                        if (!createdLocally) {
                                                        // 添加接受消息的监听
                                                    chat.addMessageListener(new ChatMessageListener() {
                                                        @Override
                                                    public void processMessage(Chat chat, Message message) {
                                                            mm = message;
                                                            System.out.println("收到的message："+mm);
                                                            System.out.println("收到的message.bady："+mm.getBody());
                                                    Object[] cm = new Object[] { chat, message };
                                                    android.os.Message msg = handler.obtainMessage();
                                                    msg.what = 1;//收到信息
                                                    msg.obj = cm;
                                                    msg.sendToTarget();
                                                     }
                                                    });
                                        }

                                 }
                      });
            }
        }).run();
    }

//消息处理
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
        //    UserActivity ua = (UserActivity) SE.getInstance().getUserActivity();
            switch (msg.what) {
                case 1://收到聊天信息

                    Object[] objs = (Object[]) msg.obj;
                    exclMessage(objs);
                    break;
            /*    case 2://收到文件请接收请求
                    FileTransferRequest fRequest = (FileTransferRequest) msg.obj;
                    exclFile(fRequest);
                    break;
                case 3://收到好友列表
                    @SuppressWarnings("unchecked")
                    ArrayList<HashMap<String, Object>> listItem = (ArrayList<HashMap<String, Object>>) msg.obj;
                    if (ua != null) {
                        ua.initUserList(listItem);
                    }
                    break;
                case 4://文件接收完成
                    String[] strs = (String[]) msg.obj;
                    exclFileSucces(strs);
                    break;
                    */
                default:
                    break;
            }

        };
    };

    /**
     * 处理接收到的本地信息
     * */
    private void exclMessage(Object[] objs) {
        //Message m = (Message) objs[1];
        Message m = mm;

        if(m.getBody() == null || m.getBody().equals("")){

        }else {
            String json = m.getBody();
            if (m.getType() == Message.Type.chat) {//聊天信息
                String cu = cfriend.getInstance().getCfriend();//当前聊天界面的会话
                String from = m.getFrom().split("/")[0];
                String chattoJid = from;
                System.out.println(cu+" "+chattoJid+" "+cfriend.getInstance().getCuser());
                if (cu != null && cu.equals(from)) {//找到会话
                    ChatActivity ca = (ChatActivity) cfriend.getInstance().getChatActivity();
                    if (ca != null) {
                        ca.printfmsg(mm);
                        //insert(cfriend.getInstance().getCuser() ,chattoJid ,chattoJid , json , TimeUtils.getNow() , IMessage.MESSAGE_TYPE_IN);
                        //显示json
                        //msgList.add(IMessage.fromJson(json));
                        System.out.println("可以监听到消息："+mm.getBody());
                    }
                 }/*else {//没有会话
                    UserActivity ua = (UserActivity) SE.getInstance().getUserActivity();
                    if (ua != null) {
                        ua.updateNewMess(from, 1);//修改未读数量
                    }
                    DB.getInstance().appMes(from, m.getBody());//保存在临时话会中
                }
                */
            }
        }
    }

    //将记录放在数据库里
    public void insert(String myname ,String toname ,String sendname , String mes ,String time , int type ){
        SQLiteDatabase sdb =  myHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("myname",myname);
        values.put("toname",toname);
        values.put("sendname",sendname);
        values.put("meg",mes);
        values.put("time",time);
        values.put("type",type);
        sdb.insert("Message",null,values);
        sdb.close();
    }
}
