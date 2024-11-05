package com.example.qq.DBHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "user.db";
    public static final int DATABASE_VERSION = 2; // 更新数据库版本号
    public static final String TABLE_NAME = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ACCOUNT = "account"; // 新增的账号字段
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PASSWORD = "password";

    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ACCOUNT + " TEXT UNIQUE," // 假设账号是唯一的
            + COLUMN_NAME + " TEXT,"
            + COLUMN_PASSWORD + " TEXT" + ")";

    public UserDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 如果数据库版本发生变化，删除旧表并创建新表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}