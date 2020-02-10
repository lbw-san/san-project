package com.openfire.imchat;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//需改  import com.yxr.imtalk.activity.FriendsActivity;
//需改  import com.yxr.imtalk.activity.RegisterActivity;
//需改  import com.yxr.imtalk.manager.ConnectionManager;

import com.openfire.imchat.activity.ChangePassword;
import com.openfire.imchat.activity.FriendsActivity;
import com.openfire.imchat.activity.RegisterActivity;
import com.openfire.imchat.beans.CurrentUser;
import com.openfire.imchat.manager.ConnectionManager;
import com.openfire.imchat.manager.MessageManager;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

public class MainActivity extends Activity implements View.OnClickListener {

    private EditText et_user, et_pwd;
    private TextView tv_login, tv_register,tv_changepsw;

    private String username;
    private String password;

    private CurrentUser cu ;

    public MessageManager msgmanager ;

    private XMPPTCPConnection connection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_user = (EditText) findViewById(R.id.login_editText_user);
        et_pwd = (EditText) findViewById(R.id.login_editText_password);
        tv_login =(TextView) findViewById(R.id.login_textview_enter);
        tv_register =(TextView)findViewById(R.id.login_textview_phone_regster);
        tv_changepsw = (TextView)findViewById(R.id.login_textview_forget_password) ;
       // System.out.println("mn"+et_user.getText().toString() + " " +password);
        initView();
    }

    private void initView() {

        tv_login.setOnClickListener(this);
        tv_register.setOnClickListener(this);
        tv_changepsw.setOnClickListener(this);
    }

    public void login() {
        // 登录
        new LoginTask().execute();
    }

    public void register() {
        // 注册
       Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void  changepsw(){
        // 改密码
        Intent intent = new Intent(this, ChangePassword.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_textview_enter: {
                login();
                break;
            }

            case R.id.login_textview_phone_regster: {
                register();
                break;
            }
            case R.id.login_textview_forget_password:{
                changepsw();
                System.out.println("为什么不让我改秘密");
                break;
            }
        }
    }

    class LoginTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            username = et_user.getText().toString().trim();
            password = et_pwd.getText().toString().trim();
            // 获取用户名密码
                System.out.println(username + " " +password);

            AbstractXMPPConnection connection = ConnectionManager.getConnection();
            try {
                // 登录
                connection.login(username,password);
                 // 登录成功,发送状态到服务器更新用户的在线状态
                Presence presence = new Presence(Presence.Type.available);
                connection.sendStanza(presence);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {

                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                // 跳转到用户界面(好友列表界面)
               //cu = new CurrentUser();
                msgmanager = new MessageManager();
                cu.getInstance().setCuser(username);
                msgmanager.messListener();
              Intent intent = new Intent(MainActivity.this, FriendsActivity.class);

                startActivity(intent);

            } else {
                Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                try {
                    ConnectionManager.release();
                    //finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
