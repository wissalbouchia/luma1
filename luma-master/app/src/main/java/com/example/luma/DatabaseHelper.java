package com.example.luma;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Notes.db";
    private static final int DATABASE_VERSION =7;
    public static final String TABLE_NAME = "notes";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    //login
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
//login





    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_CONTENT + " TEXT, "+
                "table_data TEXT, " + // لتخزين بيانات الجدول
                "list_items TEXT, " + // لتخزين عناصر القائمة
                "audio_path TEXT,"+//// إضافة عمود جديد لمسار الصوت
                "image_path TEXT," +   // عمود للصور
                "is_deleted INTEGER DEFAULT 0,"+ // حالة الحذف (سلة المهملات
                "alarm_time INTEGER DEFAULT 0,"+
                "is_archived INTEGER DEFAULT 0)";
        db.execSQL(createTable);
        //login
        // إنشاء جدول المستخدمين
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT)";
        db.execSQL(createUsersTable);
//login





    }

   /* @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }*/



//login
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS notes");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
    //login




    public boolean addNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_CONTENT, note.getContent());
        values.put("audio_path", note.getAudioPath()); // إضافة عمود جديد لمسار الصوت
        values.put("image_path", note.getImagePath()); // إضافة عمود جديد لمسار الصورة


        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        /*Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);*/
        Cursor cursor = db.rawQuery("SELECT * FROM notes WHERE is_deleted=0", null);


        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
                String audioPath = cursor.getString(cursor.getColumnIndexOrThrow("audio_path")); // استرجاع مسار الصوت
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path")); // استرجاع مسار الصورة
                Note note = new Note(id, title, content);
                note.setAudioPath(audioPath); // تعيين مسار الصوت
                notes.add(note);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return notes;
    }

    public boolean updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_CONTENT, note.getContent());

        int result = db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(note.getId())});
        return result > 0;
    }

    //archive notes
    public boolean archiveNote(int id, boolean isArchived) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_archived", isArchived ? 1 : 0);

        int result = db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }
    //archive notes
    public List<Note> getArchivedNotes() {
        List<Note> archivedNotes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE is_archived=1", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
                String audioPath = cursor.getString(cursor.getColumnIndexOrThrow("audio_path")); // استرجاع مسار الصوت
                Note note = new Note(id, title, content,audioPath);
                archivedNotes.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return archivedNotes;
    }
    //delete notes
    /*public boolean deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }*/
    //delete
    //trash

    // في DatabaseHelper
    public boolean moveNoteToTrash(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_deleted", 1); // تحريك الملاحظة إلى سلة المهملات

        int result = db.update("notes", values, "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }
    // الحصول على الملاحظات المحذوفة
    public List<Note> getDeletedNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM notes WHERE is_deleted=1", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));
                String audioPath = cursor.getString(cursor.getColumnIndexOrThrow("audio_path"));
                Note note = new Note(id, title, content, imagePath, audioPath);
                notes.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notes;
    }

    // استعادة ملاحظة
    public boolean restoreNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_deleted", 0); // إعادة تعيين الحالة إلى غير محذوفة

        int result = db.update("notes", values, "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    // حذف ملاحظة نهائيًا
    public boolean deleteNotePermanently(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("notes", "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

//trash





    //login
    // إضافة مستخدم جديد
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1; // إرجاع true إذا تم الإضافة بنجاح
    }

    // التحقق من بيانات تسجيل الدخول
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}


















