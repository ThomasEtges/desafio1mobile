package com.example.desafio1_mobile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.firebase.Timestamp;

public class TaskSqlite extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 4;

    public static final String TABLE_TASKS = "tasks";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";

    public static final String COLUMN_CONCLUSION_DATE = "conclusion_date";
    public static final String COLUMN_COMPLETED = "completed";

    public TaskSqlite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TASKS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_CONCLUSION_DATE + " INTEGER, " +
                COLUMN_COMPLETED + " INTEGER " +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    public void createtTaskSqlite(String title, String description, Timestamp conclusion_date, Boolean completed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_CONCLUSION_DATE, conclusion_date.toDate().getTime());
        values.put(COLUMN_COMPLETED, completed ? 1 : 0);
        db.insert(TABLE_TASKS, null, values);
        db.close();
    }

    public Cursor getAllTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        String orderBy = COLUMN_COMPLETED + " ASC, " + COLUMN_CONCLUSION_DATE + " ASC";
        return db.query(TABLE_TASKS, null, null, null, null, null, orderBy);
    }

    public int getTasksCount(){
        String countQuery = "SELECT * FROM " + TABLE_TASKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;

    }
    public void deleteAllTasks(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, null, null);
        db.close();
    }

    public void updateTaskStatus(String task_id, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPLETED, isCompleted ? 1 : 0);

        db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{task_id});
        db.close();
    }

}
