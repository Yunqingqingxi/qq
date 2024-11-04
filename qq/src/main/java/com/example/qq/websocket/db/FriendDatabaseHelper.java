package com.example.qq.websocket.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class FriendDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "friends.db"; // 数据库名称
    private static final int DATABASE_VERSION = 1; // 数据库版本

    public static final String TABLE_FRIENDS = "friends"; // 好友表名称
    public static final String COLUMN_FRIEND_ID = "friend_id"; // 好友 ID 列
    public static final String COLUMN_USERNAME = "username"; // 用户账号列
    public static final String COLUMN_USERNAME1 = "username1"; // 另一个用户账号列

    // 创建 Friends 表的 SQL 语句
    private static final String CREATE_FRIENDS_TABLE =
            "CREATE TABLE " + TABLE_FRIENDS + " (" +
                    COLUMN_FRIEND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT NOT NULL, " +
                    COLUMN_USERNAME1 + " TEXT NOT NULL, " +
                    "UNIQUE (" + COLUMN_USERNAME + ", " + COLUMN_USERNAME1 + "));";

    public FriendDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FRIENDS_TABLE); // 创建好友表
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS); // 删除好友表
        onCreate(db); // 重新创建表
    }

    // 添加好友
    public void addFriend(String username, String username1) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_USERNAME1, username1);

        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.insertWithOnConflict(TABLE_FRIENDS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (result == -1) {
            // 处理添加好友失败的情况
        }
    }

    // 测试2
    // 测试3

    // 获取用户的好友列表
    public List<String> getFriendsForUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> friends = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT " + COLUMN_USERNAME1 + " FROM " + TABLE_FRIENDS + " WHERE " + COLUMN_USERNAME + " = ?", new String[]{username});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String friendId = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME1));
                friends.add(friendId);
            }
            cursor.close();
        }
        return friends;
    }

    // 检查好友关系是否存在
    public boolean isFriendExists(String username, String username1) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FRIENDS, new String[]{COLUMN_FRIEND_ID}, COLUMN_USERNAME + " = ? AND " + COLUMN_USERNAME1 + " = ?", new String[]{username, username1}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}