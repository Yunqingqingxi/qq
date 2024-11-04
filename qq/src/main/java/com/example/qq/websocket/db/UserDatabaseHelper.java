package com.example.qq.websocket.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.qq.R;

public class UserDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "user.db"; // 数据库名称
    private static final int DATABASE_VERSION = 1; // 数据库版本

    public static final String TABLE_USERS = "users"; // 用户表名称
    public static final String COLUMN_USER_ID = "user_id"; // 用户 ID 列
    public static final String COLUMN_USERNAME = "username"; // 用户名(账号)列
    public static final String COLUMN_NICKNAME = "nickname"; // 昵称列
    public static final String COLUMN_AVATAR = "avatar"; // 头像列

    // 创建 Users 表的 SQL 语句
    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                    COLUMN_NICKNAME + " TEXT, " +
                    COLUMN_AVATAR + " INTEGER DEFAULT NULL);";

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE); // 创建用户表
        // 添加默认用户
        ContentValues defaultUserValues = new ContentValues();
        defaultUserValues.put(COLUMN_USERNAME, "1233123");
        defaultUserValues.put(COLUMN_NICKNAME, "默认用户");
        defaultUserValues.put(COLUMN_AVATAR, R.drawable.p8);
        db.insert(TABLE_USERS, null, defaultUserValues); // 插入默认用户
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS); // 删除用户表
        onCreate(db); // 重新创建表
    }

    // 添加用户
    public void addUser(String username, String nickname, int avatar) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_NICKNAME, nickname);
        values.put(COLUMN_AVATAR, avatar);

        SQLiteDatabase db = this.getWritableDatabase(); // 获取可写数据库
        db.insert(TABLE_USERS, null, values); // 插入用户
    }

    // 检查用户是否存在
    public boolean isUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                null,
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // 获取用户ID
    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_USER_ID + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?", new String[]{username});
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int userId = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID));
            cursor.close();
            return userId;
        }
        return -1;
    }
}
