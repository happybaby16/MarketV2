package com.example.masketv2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelperAuth extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Users";
    public static final String TABLE_NAME  = "auth";

    public static final String KEY_ID = "_id";
    public static final String KEY_USERNAME = "userName";
    public static final String KEY_PASSWORD = "passwordHash";
    public static final String KEY_ACCESS = "accessMode";


    public  DBHelperAuth(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL  ("create table "+TABLE_NAME +"("+
                    KEY_ID+" integer primary key, "+
                    KEY_USERNAME +" text, "+
                    KEY_PASSWORD + " text, "+
                    KEY_ACCESS +" text)"
                    );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NAME);
        onCreate(db);
    }
}