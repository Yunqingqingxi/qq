package com.example.qq.websocket.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.qq.pojo.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "chats.db";
    private static final int DATABASE_VERSION = 2;  // 更新数据库版本

    // 消息表
    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_MESSAGE_ID = "_id";
    public static final String COLUMN_MESSAGE_CONTENT = "content";
    public static final String COLUMN_MESSAGE_TIMESTAMP = "timestamp";
    public static final String COLUMN_MESSAGE_SENDER = "sender";
    public static final String COLUMN_MESSAGE_RECEIVER = "receiver";
    public static final String COLUMN_MESSAGE_AVATAR = "avatar";  // 新增头像字段

    private static final String TABLE_MESSAGES_CREATE =
            "CREATE TABLE " + TABLE_MESSAGES + " (" +
                    COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MESSAGE_CONTENT + " TEXT, " +
                    COLUMN_MESSAGE_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    COLUMN_MESSAGE_SENDER + " TEXT, " +
                    COLUMN_MESSAGE_RECEIVER + " TEXT, " +
                    COLUMN_MESSAGE_AVATAR + " INTEGER" +  // 头像字段，存储资源ID
                    ");";

    public ChatDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建消息表时加入头像字段
        db.execSQL(TABLE_MESSAGES_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // 升级时添加头像字段
            db.execSQL("ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COLUMN_MESSAGE_AVATAR + " INTEGER");
        }
    }

    // 插入消息
    public void insertMessage(ChatMessage message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MESSAGE_CONTENT, message.getContent());
        values.put(COLUMN_MESSAGE_TIMESTAMP, message.getFormattedTime());
        values.put(COLUMN_MESSAGE_SENDER, message.getSender());
        values.put(COLUMN_MESSAGE_RECEIVER, message.getReceiver());
        values.put(COLUMN_MESSAGE_AVATAR, message.getAvatarResId());  // 插入头像资源ID

        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    // 获取所有消息
    public List<ChatMessage> getAllMessages() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<ChatMessage> messages = new ArrayList<>();

        Cursor cursor = db.query(TABLE_MESSAGES,
                null, null, null, null, null, COLUMN_MESSAGE_TIMESTAMP + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(COLUMN_MESSAGE_ID));
                @SuppressLint("Range") String content = cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_CONTENT));
                @SuppressLint("Range") String timestamp = cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_TIMESTAMP));
                @SuppressLint("Range") String sender = cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_SENDER));
                @SuppressLint("Range") String receiver = cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_RECEIVER));
                @SuppressLint("Range") int avatarResId = cursor.getInt(cursor.getColumnIndex(COLUMN_MESSAGE_AVATAR)); // 获取头像资源ID

                ChatMessage message = new ChatMessage(id, content, timestamp, sender, receiver, avatarResId);  // 传递头像资源ID
                messages.add(message);
            }
            cursor.close();
        }

        db.close();
        return messages;
    }
}
