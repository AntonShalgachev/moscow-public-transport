package com.shalgachev.moscowpublictransport.data.db.migrators;

import android.database.sqlite.SQLiteDatabase;

public class Migrator {
    protected int mOldVersion;
    protected int mNewVersion;

    Migrator(int from, int to)
    {
        mOldVersion = from;
        mNewVersion = to;
    }

    public void migrate(SQLiteDatabase db)
    {

    }

    public int getOldVersion()
    {
        return mOldVersion;
    }

    public int getNewVersion()
    {
        return mNewVersion;
    }
}
