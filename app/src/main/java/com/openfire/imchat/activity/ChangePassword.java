package com.openfire.imchat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.openfire.imchat.R;

public class ChangePassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("说好的  改密码");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

    }
}
