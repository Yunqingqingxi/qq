package com.example.qq.websocket.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "chat.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "friendNickname TEXT, " +
                "content TEXT, " +
                "timestamp TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS messages");
        onCreate(db);
    }

    // 查询最新消息的方法
    public Cursor getLatestMessage(String friendNickname) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT content, timestamp FROM messages " +
                "WHERE sender = ? OR receiver = ? " +
                "ORDER BY id DESC LIMIT 1";
        return db.rawQuery(query, new String[]{friendNickname, friendNickname});
    }

}
