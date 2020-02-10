package com.openfire.imchat.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.openfire.imchat.MainActivity;
import com.openfire.imchat.R;
import com.openfire.imchat.adapter.ChatListAdapter;
import com.openfire.imchat.beans.CurrentUser;
import com.openfire.imchat.beans.IMessage;
import com.openfire.imchat.manager.ConnectionManager;
import com.openfire.imchat.manager.MyHelper;
import com.openfire.imchat.util.Constants;
import com.openfire.imchat.util.TimeUtils;
import com.openfire.imchat.views.RecordImageView;
//import com.yxr.imtalk.R;
//import com.yxr.imtalk.adapter.ChatListAdapter;
//import com.yxr.imtalk.beans.IMessage;
//import com.yxr.imtalk.manager.ConnectionManager;
//import com.yxr.imtalk.util.Constants;
//import com.yxr.imtalk.util.TimeUtils;
//import com.yxr.imtalk.views.RecordImageView;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jxmpp.util.XmppStringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener,RecordImageView.OnRecordFinishedListener, FileTransferListener {

    private AbstractXMPPConnection connection = ConnectionManager.getConnection();

    private EditText editEmojicon;

    private TextView btn_send;

    private TextView tv_title;

    private RecordImageView btn_record;

    private ImageView iv_record;

    private Chat chat;

    private ChatListAdapter adapter;

    private String loggedUser;

    private String chattoJid;

    private List<IMessage> msgList = new ArrayList<>();

    private FileTransferManager fileTransferManager;

    public static final int MESSAGE_REFRESEN_CHAT_LIST = 1;

    private LinearLayout get_pictuers;

    MyHelper myHelper;

    SQLiteDatabase sdb;

    CurrentUser cfriend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("聊天能来 不能跑");
        setContentView(R.layout.activity_chat);
        myHelper = new MyHelper(this);
        initView();

        Intent intent = getIntent();

        // 当前的聊天对象
        chattoJid = intent.getStringExtra("chattoJid");
        tv_title.setText(chattoJid);

        // 当前登录对象
        loggedUser = XmppStringUtils.parseBareJid(connection.getUser());

        ListView msg_listView = findViewById(R.id.msg_listView);

        adapter = new ChatListAdapter(this, msgList);

        msg_listView.setAdapter(adapter);

        String[] ld =new String[10];

        ld[0] = loggedUser;

        //提取本地数据库聊天记录
        getmMesHosity();

        // 聊天管理器
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        // 监听对话的创建(顺序提前)
        //chatManager.addChatListener(this);

        // 创建一个对话
        chat = chatManager.createChat(chattoJid, null);

        // 文件传输管理器
        fileTransferManager = FileTransferManager.getInstanceFor(connection);
        // 注册监听, 接收文件
        fileTransferManager.addFileTransferListener(this);
    }

    private void initView() {
        editEmojicon = findViewById(R.id.editEmojicon);
        tv_title = findViewById(R.id.tv_title);
        btn_send = findViewById(R.id.btn_send);
        btn_record = findViewById(R.id.iv_icon);
        iv_record = (ImageView)findViewById(R.id.iv_icon) ;
        get_pictuers = (LinearLayout)findViewById(R.id.get_pictuers);
        get_pictuers.setOnClickListener(this);
        btn_send.setOnClickListener(this);
        btn_record.setOnRecordFinishedListener(this);
        //cfriend = new CurrentUser();
        cfriend.getInstance().setChatActivity(this);//当前聊天界面
        //iv_record.setOnClickListener(this);
    }

    public void closeActiviy(View layout) {
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                mSend();
                break;
            case R.id.iv_icon:
                Toast.makeText(ChatActivity.this, "敬请期待该功能", Toast.LENGTH_SHORT).show();
                break;
            case R.id.get_pictuers:
                System.out.println("点对了 老哥！！");
                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_GET_CONTENT);
                intent1.addCategory(Intent.CATEGORY_OPENABLE);intent1.setType("image/*");
                startActivityForResult(intent1, 1);
                break;
            default:
                break;
        }
    }

    /**
     * 发送消息
     */
    public void mSend() {
        String msg = editEmojicon.getText().toString();
        // 用户可以马上看到自己发送的消息
        IMessage localMsg = new IMessage(loggedUser, msg, TimeUtils.getNow(), IMessage.MESSAGE_TYPE_OUT);
        // 添加到消息列表, 更新ListView
        msgList.add(localMsg);
        adapter.notifyDataSetChanged();

            //单纯存入文本
       // insert(loggedUser ,chattoJid ,loggedUser , msg , TimeUtils.getNow() , IMessage.MESSAGE_TYPE_OUT);



        //传json
        // 这条消息对于发送到的用户来说是接受的消息
        IMessage remoteMsg = new IMessage(loggedUser, msg, TimeUtils.getNow(), IMessage.MESSAGE_TYPE_IN);
        //存入localMagjson 对于本地用户是发出去的
        insert(loggedUser ,chattoJid ,loggedUser ,localMsg.toJson(), TimeUtils.getNow() , IMessage.MESSAGE_TYPE_OUT);

        try {
            //传出remoteMsgjson
            chat.sendMessage(remoteMsg.toJson());

        //单纯传文本
            //chat.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 发送完成后清空输入框
        editEmojicon.setText("");
    }

    // 录音完成,发送音频文件
    @Override
    public void onFinished(final File audioFile, int duration) {
        if (audioFile == null)
            return;

        // localMsg
        IMessage localMsg = new IMessage(loggedUser, TimeUtils.getNow(), IMessage.MESSAGE_TYPE_OUT, duration, audioFile.getName());

        msgList.add(localMsg);
            System.out.println("文件大小sssss："+duration);

        adapter.notifyDataSetChanged();

        // remoteMsg
        IMessage remoteMsg = new IMessage(loggedUser, TimeUtils.getNow(), IMessage.MESSAGE_TYPE_OUT, duration, audioFile.getName());
       // 单纯传文本
/*
                        String yuyinmeg = " " + (duration/1000) + "\' " + (duration%1000) + "\"  语音消息";
        insert(loggedUser ,chattoJid ,loggedUser , yuyinmeg , TimeUtils.getNow() , IMessage.MESSAGE_TYPE_OUT);
*/
        //存入json
        insert(loggedUser ,chattoJid ,loggedUser , remoteMsg.toJson() , TimeUtils.getNow() , IMessage.MESSAGE_TYPE_OUT);

        try {
            //传出json
            chat.sendMessage(remoteMsg.toJson());
            //传出文本
            //chat.sendMessage(yuyinmeg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread() {
            @Override
            public void run() {
        // 发送文件
        OutgoingFileTransfer transfer = fileTransferManager.createOutgoingFileTransfer(chattoJid+"/Smack");
        try {
            transfer.sendFile(audioFile, "send_audio");

                if (transfer.getStatus() == FileTransfer.Status.complete) {
                     Log.i("发送的状态是", transfer.getStatus().toString());
                    updateMsgStatus(audioFile);
                } else {
                    Log.i("发送的状态是", transfer.getStatus().toString());
                }
            updateMsgStatus(audioFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
            }
        }.start();

    }

    @Override
    public void fileTransferRequest(final FileTransferRequest request) {
        new Thread() {
            @Override
            public void run() {
        // 收到文件之后，存入本地
        IncomingFileTransfer transfer = request.accept();
        if (!Constants.AUDIO_DIR.exists()) {
            Constants.AUDIO_DIR.mkdirs();
        }

        File file = new File(Constants.AUDIO_DIR, transfer.getFileName());
          Log.i("该文件的大小是",  file.length()+ "B");
            Log.i("transfer传入的文件名字是", transfer.getFileName());

        try {
            // 收文件
            transfer.recieveFile(file);
            Log.i("收到的语音消息存放在", file.getAbsolutePath());
            // 传输成功
            if (transfer.getStatus() == FileTransfer.Status.complete) {
                Log.i("接收的状态是", transfer.getStatus().toString());
                updateMsgStatus(file);
            } else {
                Log.i("接收的状态是", transfer.getStatus().toString());
            }
            Log.i("收到本地", file.getAbsolutePath());
            Log.i("收到的文件大小是", file.length()+ "B");
            updateMsgStatus(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

            }
        }.start();
    }

    private void updateMsgStatus(File file) {

        // 更新消息的状态
        for (IMessage msg : msgList) {
            if (file.getName().equals(msg.getFileName())) {
                msg.setStatus(IMessage.MESSAGE_STATUS_SUCCESS);
                break;
            }
        }
    }
/*
    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        // 添加接受消息的监听
        chat.addMessageListener(this);
        System.out.println("在听呢");
    }
*/
    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESEN_CHAT_LIST:
                    adapter.notifyDataSetChanged();
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    //显示后台收到的消息
    public void printfmsg(Message message){

        // 当前聊天窗口要显示当前这个用户发送的消息
        String fromJid = XmppStringUtils.parseBareJid(message.getFrom());
        System.out.println(fromJid.trim()+"可以收到，没显示"+chattoJid);

        if(fromJid.equals(chattoJid.trim())) {
            String json = message.getBody().trim();
            //JSON son = (JSON) JSON.parse(json);

            if(json == null || json.equals(""));
            else {
                System.out.println(json + " json");

                //System.out.println("nb"+IMessage.fromJson(json));
                insert(loggedUser ,chattoJid ,chattoJid , json , TimeUtils.getNow() , IMessage.MESSAGE_TYPE_IN);
                //显示json
                msgList.add(IMessage.fromJson(json));

                //显示文本
                /*
                IMessage IntMsg = new IMessage(chattoJid, json, TimeUtils.getNow(), IMessage.MESSAGE_TYPE_IN);
                             msgList.add(IntMsg);
                 */

                //     adapter.notifyDataSetChanged();
                //刷新

                handler.sendEmptyMessage(MESSAGE_REFRESEN_CHAT_LIST);
            }
        }
    }
 /*
    // 收到好友发送的消息
    @Override
    public void processMessage(Chat chat, Message message) {
        // 当前聊天窗口要显示当前这个用户发送的消息
        String fromJid = XmppStringUtils.parseBareJid(message.getFrom());

            System.out.println(fromJid.trim()+"可以收到，没显示"+chattoJid.trim());

        if(fromJid.equals(chattoJid.trim())) {
            String json = message.getBody();
            //JSON son = (JSON) JSON.parse(json);

            if(json == null || json.equals(""));
            else {
                System.out.println(json + " json");

                //System.out.println("nb"+IMessage.fromJson(json));
                insert(loggedUser ,chattoJid ,chattoJid , json , TimeUtils.getNow() , IMessage.MESSAGE_TYPE_IN);
                        //显示json
                            msgList.add(IMessage.fromJson(json));


                    //显示文本
                /*
                IMessage IntMsg = new IMessage(chattoJid, json, TimeUtils.getNow(), IMessage.MESSAGE_TYPE_IN);
                             msgList.add(IntMsg);
                 */

            //     adapter.notifyDataSetChanged();
                //刷新
 /*
                handler.sendEmptyMessage(MESSAGE_REFRESEN_CHAT_LIST);
            }
        }

    }
    */
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

//提取数据库数据
   public void getmMesHosity(){
       SQLiteDatabase sdb =  myHelper.getWritableDatabase();
       Cursor cursor = sdb.query("Message",null,"toname=?",new String[]{chattoJid },null,null,null);
       while (cursor.moveToNext()){
           if (cursor.getString(0).equals(loggedUser)) {
               String sendname = cursor.getString(2);
               String msg = cursor.getString(3);
               String time = cursor.getString(4);
               int type = cursor.getInt(5);

              //打印json
                    IMessage mm = IMessage.fromJson(msg);
                 IMessage IntMsg = new IMessage(sendname, mm.toJson(), time, type);
                 msgList.add(IMessage.fromJson(mm.toJson()));




              //单纯显示文本
               /*
                      IMessage IntMsg = new IMessage(sendname, msg, time, type);
                      msgList.add(IntMsg);


                */

               //   adapter.notifyDataSetChanged();
           }
       }
       //刷新
       handler.sendEmptyMessage(MESSAGE_REFRESEN_CHAT_LIST);
       cursor.close();
       sdb.close();

   }
}
