package com.example.qq.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.qq.DBHelper.UserDBHelper;

public class UserDAO {
    private UserDBHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new UserDBHelper(context);
    }

    // 添加账号作为参数
    public long addUser(String account, String name, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserDBHelper.COLUMN_ACCOUNT, account);
        values.put(UserDBHelper.COLUMN_NAME, name);
        values.put(UserDBHelper.COLUMN_PASSWORD, password);
        long result = db.insert(UserDBHelper.TABLE_NAME, null, values);
        db.close();
        return result;
    }

    public Cursor getAllUsers() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + UserDBHelper.TABLE_NAME, null);
    }

    // 更新updateUser方法以包含账号字段
    public int updateUser(long id, String account, String name, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserDBHelper.COLUMN_ACCOUNT, account);
        values.put(UserDBHelper.COLUMN_NAME, name);
        values.put(UserDBHelper.COLUMN_PASSWORD, password);
        return db.update(UserDBHelper.TABLE_NAME, values, UserDBHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public boolean validateUser(String account, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + UserDBHelper.TABLE_NAME + " WHERE " + UserDBHelper.COLUMN_ACCOUNT + "=? AND " + UserDBHelper.COLUMN_PASSWORD + "=?", new String[]{account, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public int deleteUser(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(UserDBHelper.TABLE_NAME, UserDBHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }
}