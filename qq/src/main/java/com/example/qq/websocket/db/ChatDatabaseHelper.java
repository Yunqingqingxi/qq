package com.example.qq.websocket.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.qq.pojo.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "chat.db";
    private static final int DATABASE_VERSION = 1;

    // 消息表
    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_MESSAGE_ID = "_id";
    public static final String COLUMN_MESSAGE_CONTENT = "content";
    public static final String COLUMN_MESSAGE_TIMESTAMP = "timestamp";
    public static final String COLUMN_MESSAGE_SENDER = "sender";
    public static final String COLUMN_MESSAGE_RECEIVER = "receiver";

    private static final String TABLE_MESSAGES_CREATE =
            "CREATE TABLE " + TABLE_MESSAGES + " (" +
                    COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MESSAGE_CONTENT + " TEXT, " +
                    COLUMN_MESSAGE_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    COLUMN_MESSAGE_SENDER + " TEXT, " +
                    COLUMN_MESSAGE_RECEIVER + " TEXT" +
                    ");";

    public ChatDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建 messages 表
        db.execSQL(TABLE_MESSAGES_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    // 插入消息
    public void insertMessage(ChatMessage message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MESSAGE_CONTENT, message.getContent());
        values.put(COLUMN_MESSAGE_TIMESTAMP, message.getFormattedTime());
        values.put(COLUMN_MESSAGE_SENDER, message.getSender());
        values.put(COLUMN_MESSAGE_RECEIVER, message.getReceiver());

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
                long id = cursor.getLong(cursor.getColumnIndex(COLUMN_MESSAGE_ID));
                String content = cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_CONTENT));
                String timestamp = cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_TIMESTAMP));
                String sender = cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_SENDER));
                String receiver = cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_RECEIVER));

                ChatMessage message = new ChatMessage(id, content, timestamp, sender, receiver);
                messages.add(message);
            }
            cursor.close();
        }

        db.close();
        return messages;
    }
}
