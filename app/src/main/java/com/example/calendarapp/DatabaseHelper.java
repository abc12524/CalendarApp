package com.example.calendarapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 日历笔记数据库
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "calendar.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NOTES = "notes";

    private static final String COL_ID = "id";
    private static final String COL_DATE = "date";
    private static final String COL_TITLE = "title";
    private static final String COL_CONTENT = "content";
    private static final String COL_UPDATED_AT = "updated_at";

    private static final SimpleDateFormat ISO_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NOTES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_DATE + " TEXT UNIQUE NOT NULL, "
                + COL_TITLE + " TEXT DEFAULT '', "
                + COL_CONTENT + " TEXT DEFAULT '', "
                + COL_UPDATED_AT + " TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    // ========== CRUD ==========

    /** 获取某天的笔记，没有则返回 null */
    public Note getNote(String date) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES, null,
                COL_DATE + "=?", new String[]{date},
                null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursorToNote(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    /** 保存笔记（插入或更新） */
    public void saveNote(String date, String title, String content) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, date);
        values.put(COL_TITLE, title != null ? title.trim() : "");
        values.put(COL_CONTENT, content != null ? content.trim() : "");
        values.put(COL_UPDATED_AT, ISO_FORMAT.format(new Date()));

        // 如果标题和内容都为空，删除这条记录
        String t = values.getAsString(COL_TITLE);
        String c = values.getAsString(COL_CONTENT);
        if ((t == null || t.isEmpty()) && (c == null || c.isEmpty())) {
            db.delete(TABLE_NOTES, COL_DATE + "=?", new String[]{date});
            return;
        }

        // INSERT OR REPLACE
        db.insertWithOnConflict(TABLE_NOTES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /** 删除某天的笔记 */
    public void deleteNote(String date) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NOTES, COL_DATE + "=?", new String[]{date});
    }

    /** 加载所有笔记到内存 Map (date → "title|content") */
    public Map<String, String> loadAllNotes() {
        Map<String, String> map = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES, new String[]{COL_DATE, COL_TITLE, COL_CONTENT},
                null, null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT));
                String noteData = (title != null ? title : "") + "|" + (content != null ? content : "");
                map.put(date, noteData);
            }
        } finally {
            cursor.close();
        }
        return map;
    }

    /** 判断某天是否有笔记内容 */
    public boolean hasNote(String date) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES, new String[]{COL_ID},
                COL_DATE + "=?", new String[]{date},
                null, null, null);
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    // ========== 内部方法 ==========

    private Note cursorToNote(Cursor cursor) {
        Note note = new Note();
        note.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)));
        note.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)));
        note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)));
        note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)));
        note.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_UPDATED_AT)));
        return note;
    }
}
