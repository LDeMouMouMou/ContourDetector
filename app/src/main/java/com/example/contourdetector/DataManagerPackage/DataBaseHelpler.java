package com.example.contourdetector.DataManagerPackage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelpler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pamameter.db";
    private static final int DATABASE_VERSION = 1;

    public DataBaseHelpler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS parameter" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
