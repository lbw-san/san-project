package com.openfire.imchat.manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyHelper extends SQLiteOpenHelper {
    public MyHelper(Context context) {
        super(context, "Chat.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sdb) {
            sdb.execSQL("create table Message (myname VARCHER(50),toname VARCHER(50),sendname VARCHER(50),meg VARCHER(200),time VARCHER(50),type integer)");
            sdb.execSQL("create table Account (username VARCHER(50),password VARCHER(50),level integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

}
