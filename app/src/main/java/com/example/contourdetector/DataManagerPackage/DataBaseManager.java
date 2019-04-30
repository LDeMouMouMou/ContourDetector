package com.example.contourdetector.DataManagerPackage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DataBaseManager {

    private DataBaseHelpler helpler;
    private SQLiteDatabase db;

    public DataBaseManager(Context context) {
        helpler = new DataBaseHelpler(context);
        db = helpler.getWritableDatabase();
    }

    public void add() {
        db.beginTransaction();
        db.endTransaction();
    }

}
