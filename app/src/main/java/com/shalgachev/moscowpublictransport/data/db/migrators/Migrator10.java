package com.shalgachev.moscowpublictransport.data.db.migrators;

import android.database.sqlite.SQLiteDatabase;

import com.shalgachev.moscowpublictransport.data.db.ScheduleCacheSQLiteHelper;

public class Migrator10 extends Migrator {
    public Migrator10() {
        super(10, 11);
    }

    @Override
    public void migrate(SQLiteDatabase db) {
        String colorQuery = "ALTER TABLE " + ScheduleCacheSQLiteHelper.TABLE_TIMETABLES
                + " ADD " + ScheduleCacheSQLiteHelper.COLUMN_COLOR + " TEXT";
        String noteQuery = "ALTER TABLE " + ScheduleCacheSQLiteHelper.TABLE_TIMETABLES
                + " ADD " + ScheduleCacheSQLiteHelper.COLUMN_NOTE + " TEXT";

        db.execSQL(colorQuery);
        db.execSQL(noteQuery);
    }
}
