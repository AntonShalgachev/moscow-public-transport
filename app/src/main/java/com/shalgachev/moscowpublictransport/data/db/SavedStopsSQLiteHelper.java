package com.shalgachev.moscowpublictransport.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.shalgachev.moscowpublictransport.data.Direction;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.ScheduleType;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.TransportType;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by anton on 7/2/2017.
 */

public class SavedStopsSQLiteHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = "SavedStopsSQLiteHelper";
    private static final boolean SQL_DEBUG = true;

    private static final String TABLE_SAVED_STOPS = "saved_stops";
    private static final String TABLE_SCHEDULE_TYPES = "schedule_types";
    private static final String TABLE_TIMETABLES = "timetables";
    private static final String TABLE_STOPS_ON_MAIN_SCREEN = "stops_on_main_screen";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_PROVIDER_ID = "provider_id";
    private static final String COLUMN_TRANSPORT_TYPE = "transport_type";
    private static final String COLUMN_ROUTE = "route";
    private static final String COLUMN_DAYS_ROUTE = "days_mask";
    private static final String COLUMN_DIRECTION_ID = "direction_id";
    private static final String COLUMN_DIRECTION_FROM = "direction_from";
    private static final String COLUMN_DIRECTION_TO = "direction_to";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_STOP_ID = "stop_id";
    private static final String COLUMN_SAVED_STOP_ID = "saved_stop_id";
    private static final String COLUMN_SCHEDULE_TYPE = "schedule_type";
    private static final String COLUMN_TIMEPOINT = "timepoint";

    private static final String DATABASE_NAME = "saved_stops.db";
    private static final int DATABASE_VERSION = 7;

    public SavedStopsSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(LOG_TAG, "Creating new database");
        final String SAVED_STOPS_CREATE_QUERY = "create table " + TABLE_SAVED_STOPS
                + "( "
                + COLUMN_ID + " integer primary key autoincrement"
                + ", " + COLUMN_PROVIDER_ID + " text not null"
                + ", " + COLUMN_TRANSPORT_TYPE + " text not null"
                + ", " + COLUMN_ROUTE + " text not null"
                + ", " + COLUMN_DAYS_ROUTE + " text not null"
                + ", " + COLUMN_DIRECTION_ID + " text not null"
                + ", " + COLUMN_DIRECTION_FROM + " text not null"
                + ", " + COLUMN_DIRECTION_TO + " text not null"
                + ", " + COLUMN_NAME + " text not null"
                + ", " + COLUMN_STOP_ID + " integer not null"
                + ");";
        final String SCHEDULE_TYPES_CREATE_QUERY = "create table " + TABLE_SCHEDULE_TYPES
                + "( "
                + COLUMN_ID + " integer primary key autoincrement"
                + ", " + COLUMN_SAVED_STOP_ID + " integer not null"
                + ", " + COLUMN_SCHEDULE_TYPE + " text not null"
                + ");";
        final String TIMETABLES_CREATE_QUERY = "create table " + TABLE_TIMETABLES
                + "( "
                + COLUMN_ID + " integer primary key autoincrement"
                + ", " + COLUMN_SAVED_STOP_ID + " integer not null"
                + ", " + COLUMN_TIMEPOINT + " time not null"
                + ");";
        final String STOPS_ON_MAIN_SCREEN_CREATE_QUERY = "create table " + TABLE_STOPS_ON_MAIN_SCREEN
                + "( "
                + COLUMN_ID + " integer primary key autoincrement"
                + ", " + COLUMN_SAVED_STOP_ID + " integer not null"
                + ");";

        execSQLDebug(db, SAVED_STOPS_CREATE_QUERY, true);
        execSQLDebug(db, SCHEDULE_TYPES_CREATE_QUERY, true);
        execSQLDebug(db, TIMETABLES_CREATE_QUERY, true);
        execSQLDebug(db, STOPS_ON_MAIN_SCREEN_CREATE_QUERY, true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: 7/23/2017 Don't drop database on upgrade
        Log.w(LOG_TAG, "Upgrading database from version " + oldVersion
                + " to " + newVersion + ", which will destroy all old data");

        final String SAVED_STOPS_DROP_QUERY = "DROP TABLE IF EXISTS " + TABLE_SAVED_STOPS;
        final String SCHEDULE_TYPES_DROP_QUERY = "DROP TABLE IF EXISTS " + TABLE_SCHEDULE_TYPES;
        final String TIMETABLES_DROP_QUERY = "DROP TABLE IF EXISTS " + TABLE_TIMETABLES;
        final String STOPS_ON_MAIN_SCREEN_DROP_QUERY = "DROP TABLE IF EXISTS " + TABLE_STOPS_ON_MAIN_SCREEN;

        execSQLDebug(db, SAVED_STOPS_DROP_QUERY, true);
        execSQLDebug(db, SCHEDULE_TYPES_DROP_QUERY, true);
        execSQLDebug(db, TIMETABLES_DROP_QUERY, true);
        execSQLDebug(db, STOPS_ON_MAIN_SCREEN_DROP_QUERY, true);

        onCreate(db);
    }

    private void execSQLDebug(SQLiteDatabase db, String sql) {
        execSQLDebug(db, sql, false,"");
    }

    private void execSQLDebug(SQLiteDatabase db, String sql, boolean log) {
        execSQLDebug(db, sql, log,"");
    }

    private void execSQLDebug(SQLiteDatabase db, String sql, boolean log, String text) {
        if (text.isEmpty())
            text = "Executing";

        if (SQL_DEBUG && log)
            Log.v(LOG_TAG, String.format("%s:\n%s", text, sql));

        db.execSQL(sql);
    }

    public void cleanup() {
        // TODO: 1/12/2018
    }

    public void cleanUnused() {
        // TODO: 1/12/2018
    }

    private int getSavedStopId(Stop stop) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cur = db.query(TABLE_SAVED_STOPS, new String[]{COLUMN_ID}, getStopWhereClause(), getStopWhereArgs(stop), null, null, null);

        if (cur == null)
            return -1;

        if (!cur.moveToFirst()) {
            cur.close();
            return -1;
        }

        int stopId = cur.getInt(cur.getColumnIndex(COLUMN_ID));
        cur.close();

        return stopId;
    }

    private Stop getSavedStopById(int id) {
        String selectQuery = "SELECT * FROM " + TABLE_SAVED_STOPS + " WHERE " +
                COLUMN_ID + " = ?";
        String[] selectArgs = new String[]{String.valueOf(id)};

        SQLiteDatabase db = getReadableDatabase();
        Cursor cur = db.rawQuery(selectQuery, selectArgs);

        if (cur == null)
            return null;

        if (!cur.moveToFirst()) {
            cur.close();
            return null;
        }

        Stop stop = cursorToStop(cur);
        cur.close();

        return stop;
    }

    private boolean isStopSaved(Stop stop) {
        return getSavedStopId(stop) >= 0;
    }

    public Schedule getSchedule(Stop stop) {
        Log.d(LOG_TAG, String.format("getSchedule('%s')", stop.toString()));
        int stopId = getSavedStopId(stop);
        ScheduleType type = getScheduleType(stopId);
        if (type == null) {
            Log.i(LOG_TAG, "No saved schedule for a given stop");
            return null;
        }

        Log.d(LOG_TAG, String.format("Found saved schedule of type '%s'", type.toString()));

        switch (type) {
            case TIMEPOINTS:
                return getTimetableSchedule(stop, stopId);
            case INTERVALS:
                throw new UnsupportedOperationException("Intervals aren't yet supported");
        }

        throw new IllegalArgumentException(String.format("Invalid schedule type '%s'", type.name()));
    }

    private ScheduleType getScheduleType(int stopId) {
        SQLiteDatabase db = getReadableDatabase();

        String whereClause = COLUMN_SAVED_STOP_ID + " = ?";
        String[] whereArgs = new String[] {
                String.valueOf(stopId)
        };

        Cursor cur = db.query(TABLE_SCHEDULE_TYPES, new String[]{COLUMN_SCHEDULE_TYPE}, whereClause, whereArgs, "", "", "");
        try {
            if (cur != null && cur.moveToFirst()) {
                int rows = cur.getCount();
                if (rows != 1) {
                    Log.e(LOG_TAG, String.format("There are %d schedule types for stop id %d. Expected no more than 1", rows, stopId));
                }
                String val = cur.getString(cur.getColumnIndex(COLUMN_SCHEDULE_TYPE));
                return ScheduleType.valueOf(val);
            }
        } finally {
            if (cur != null)
                cur.close();
        }

        return null;
    }

    private Schedule getTimetableSchedule(Stop stop, int stopId) {
        Log.d(LOG_TAG, String.format("getTimetableSchedule('%s', %d)", stop.toString(), stopId));
        SQLiteDatabase db = getReadableDatabase();

        String whereClause = COLUMN_SAVED_STOP_ID + " = ?";
        String[] whereArgs = new String[] {
                String.valueOf(stopId)
        };

        Log.d(LOG_TAG, "Selecting timetable");
        Cursor cur = db.query(TABLE_TIMETABLES, new String[]{COLUMN_TIMEPOINT}, whereClause, whereArgs, "", "", "");
        if (cur != null)
            Log.d(LOG_TAG, String.format("There are %d rows in dataset", cur.getCount()));

        List<Schedule.Timepoint> timepoints = new ArrayList<>();

        try {
            if (cur != null) {
                while (cur.moveToNext()) {
                    String str = cur.getString(cur.getColumnIndex(COLUMN_TIMEPOINT));
                    timepoints.add(Schedule.Timepoint.valueOf(str));
                }
            }
        } finally {
            if (cur != null)
                cur.close();
        }

        Schedule schedule = new Schedule();
        schedule.setAsTimepoints(stop, timepoints);

        return schedule;
    }

    public void saveSchedule(Schedule schedule) {
        Log.d(LOG_TAG, String.format("saveSchedule('%s')", schedule.toString()));

        Stop stop = schedule.getStop();
        int stopId = getSavedStopId(stop);

        removeSchedule(stopId);

        ScheduleType type = schedule.getScheduleType();
        saveScheduleType(type, stopId);

        switch (type) {
            case TIMEPOINTS:
                saveTimetableSchedule(schedule, stopId);
                break;
            case INTERVALS:
                throw new UnsupportedOperationException("Intervals aren't yet supported");
        }
    }

    private void saveScheduleType(ScheduleType type, int stopId) {
        Log.d(LOG_TAG, String.format("saveScheduleType('%s', %d)", type.toString(), stopId));
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SAVED_STOP_ID, stopId);
        values.put(COLUMN_SCHEDULE_TYPE, type.name());

        db.insert(TABLE_SCHEDULE_TYPES, "", values);
    }

    private void saveTimetableSchedule(Schedule schedule, int stopId) {
        Log.d(LOG_TAG, String.format("saveTimetableSchedule('%s', %d)", schedule.toString(), stopId));
        SQLiteDatabase db = getWritableDatabase();

        List<Schedule.Timepoint> timepoints = schedule.getTimepoints();

        for (Schedule.Timepoint timepoint : timepoints) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_SAVED_STOP_ID, stopId);
            values.put(COLUMN_TIMEPOINT, timepoint.toString());

            db.insert(TABLE_TIMETABLES, "", values);
        }

        Log.d(LOG_TAG, String.format("Saved %d timepoints", timepoints.size()));
    }

    private void removeSchedule(int stopId) {
        Log.d(LOG_TAG, String.format("removeSchedule(%d)", stopId));

        ScheduleType type = getScheduleType(stopId);

        if (type == null) {
            Log.d(LOG_TAG, "There are no saved schedules for this stop");
            return;
        }

        switch(type) {
            case TIMEPOINTS:
                removeTimetableSchedule(stopId);
                break;
            case INTERVALS:
                throw new UnsupportedOperationException("Intervals aren't yet supported");
        }

        removeScheduleType(stopId);
    }

    private void removeScheduleType(int stopId) {
        SQLiteDatabase db = getWritableDatabase();

        String whereClause = COLUMN_SAVED_STOP_ID + " = ?";
        String[] whereArgs = new String[] {
                String.valueOf(stopId)
        };

        int affectedRows = db.delete(TABLE_SCHEDULE_TYPES, whereClause, whereArgs);

        Log.d(LOG_TAG, String.format("Affected rows after removing schedule type: %d", affectedRows));

        if (affectedRows != 1) {
            Log.w(LOG_TAG, "Expected only 1 row to be affected");
        }
    }

    private void removeTimetableSchedule(int stopId) {
        SQLiteDatabase db = getWritableDatabase();

        String whereClause = COLUMN_SAVED_STOP_ID + " = ?";
        String[] whereArgs = new String[] {
                String.valueOf(stopId)
        };

        int affectedRows = db.delete(TABLE_TIMETABLES, whereClause, whereArgs);

        Log.d(LOG_TAG, String.format("Affected rows after removing schedule: %d", affectedRows));
    }

    private boolean isAddedToMainMenu(Stop stop) {
        int stopId = getSavedStopId(stop);

        if (stopId < 0)
            return false;

        SQLiteDatabase db = getReadableDatabase();

        String whereClause = COLUMN_SAVED_STOP_ID + " = ?";
        String[] whereArgs = new String[] {
                String.valueOf(stopId)
        };

        long rows = DatabaseUtils.queryNumEntries(db, TABLE_STOPS_ON_MAIN_SCREEN, whereClause, whereArgs);

        if (rows < 0 || rows > 1)
            Log.w(LOG_TAG, String.format("queryNumEntries returned %d rows", rows));

        return rows > 0;
    }

    public void addToMainMenu(Stop stop) {
        Log.d(LOG_TAG, String.format("addToMainMenu('%s')", stop.toString()));

        if (!isStopSaved(stop))
            saveStop(stop);

        if (!isStopSaved(stop))
            throw new RuntimeException("Failed to save the stop before adding to the main menu");

        if (isAddedToMainMenu(stop)) {
            Log.w(LOG_TAG, "The stop is already added");
            return;
        }

        int stopId = getSavedStopId(stop);

        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_SAVED_STOP_ID, stopId);
        db.insert(TABLE_STOPS_ON_MAIN_SCREEN, "", contentValues);
    }

    public void removeFromMainMenu(Stop stop) {
        Log.d(LOG_TAG, String.format("removeFromMainMenu('%s')", stop.toString()));

        if (!isStopSaved(stop)) {
            Log.w(LOG_TAG, "The stop isn't saved");
            return;
        }

        if (!isAddedToMainMenu(stop)) {
            Log.w(LOG_TAG, "The stop isn't added to the main menu");
            return;
        }

        int stopId = getSavedStopId(stop);

        SQLiteDatabase db = getWritableDatabase();

        String whereClause = COLUMN_SAVED_STOP_ID + " = ?";
        String[] whereArgs = new String[] {
                String.valueOf(stopId)
        };

        int affectedRows = db.delete(TABLE_STOPS_ON_MAIN_SCREEN, whereClause, whereArgs);

        Log.d(LOG_TAG, String.format("Affected rows after removing from main menu: %d", affectedRows));

        if (affectedRows != 1) {
            Log.w(LOG_TAG, "Expected only 1 row to be affected");
        }

        cleanUnused();
    }

    private void saveStop(Stop stop) {
        Log.d(LOG_TAG, String.format("saveStop('%s')", stop.toString()));

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PROVIDER_ID, stop.providerId.toString());
        values.put(COLUMN_TRANSPORT_TYPE, transportTypeToString(stop.transportType));
        values.put(COLUMN_ROUTE, stop.route.toString());
        values.put(COLUMN_DAYS_ROUTE, stop.daysMask.toString());
        values.put(COLUMN_DIRECTION_ID, stop.direction.getId().toString());
        values.put(COLUMN_DIRECTION_FROM, stop.direction.getFrom().toString());
        values.put(COLUMN_DIRECTION_TO, stop.direction.getTo().toString());
        values.put(COLUMN_NAME, stop.name.toString());
        values.put(COLUMN_STOP_ID, stop.id);

        db.insert(TABLE_SAVED_STOPS, null, values);
    }

    private void deleteSavedStop(Stop stop) {
        Log.d(LOG_TAG, String.format("deleteSavedStop('%s')", stop.toString()));

        SQLiteDatabase db = getWritableDatabase();

        // TODO: 1/27/2018 Check for references to this stop in other tables

        int affectedRows = db.delete(TABLE_SAVED_STOPS, getStopWhereClause(), getStopWhereArgs(stop));

        Log.d(LOG_TAG, String.format("Affected rows after deleting stop: %d", affectedRows));

        if (affectedRows != 1) {
            Log.w(LOG_TAG, "Expected only 1 row to be affected");
        }
    }

    public List<Stop> getStopsOnMainMenu() {
        return getStopsOnMainMenu(null);
    }

    public List<Stop> getStopsOnMainMenu(TransportType transportType) {
        Log.d(LOG_TAG, String.format("getStopsOnMainMenu('%s')", transportTypeToString(transportType)));

        List<Stop> stops = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_SAVED_STOPS;
        String[] selectArgs = null;

        if (transportType != null) {
            selectQuery += " WHERE " + COLUMN_TRANSPORT_TYPE + " = ?";
            selectArgs = new String[]{
                    transportTypeToString(transportType)
            };
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cur = db.rawQuery(selectQuery, selectArgs);

        while (cur.moveToNext()) {
            Stop stop = cursorToStop(cur);
            if (isAddedToMainMenu(stop))
                stops.add(stop);
        }

        cur.close();

        return stops;
    }

    private String getStopWhereClause() {
        return COLUMN_PROVIDER_ID + " = ?"
            + " AND " + COLUMN_TRANSPORT_TYPE + " = ?"
            + " AND " + COLUMN_ROUTE + " = ?"
            + " AND " + COLUMN_DAYS_ROUTE + " = ?"
            + " AND " + COLUMN_DIRECTION_ID + " = ?"
            + " AND " + COLUMN_NAME + " = ?"
            + " AND " + COLUMN_STOP_ID + " = ?";
    }

    private String[] getStopWhereArgs(Stop stop) {
        return new String[]{
                stop.providerId.toString(),
                transportTypeToString(stop.transportType),
                stop.route.toString(),
                stop.daysMask.toString(),
                stop.direction.getId().toString(),
                stop.name.toString(),
                String.valueOf(stop.id),
        };
    }

    private Stop cursorToStop(@NonNull Cursor c) {
        String provider_id = c.getString(c.getColumnIndex(COLUMN_PROVIDER_ID));
        String transport_type = c.getString(c.getColumnIndex(COLUMN_TRANSPORT_TYPE));
        String route = c.getString(c.getColumnIndex(COLUMN_ROUTE));
        String days_mask = c.getString(c.getColumnIndex(COLUMN_DAYS_ROUTE));
        String direction_id = c.getString(c.getColumnIndex(COLUMN_DIRECTION_ID));
        String direction_from = c.getString(c.getColumnIndex(COLUMN_DIRECTION_FROM));
        String direction_to = c.getString(c.getColumnIndex(COLUMN_DIRECTION_TO));
        String name = c.getString(c.getColumnIndex(COLUMN_NAME));
        int stop_id = c.getInt(c.getColumnIndex(COLUMN_STOP_ID));

        Direction direction = new Direction(direction_id, direction_from, direction_to);
        return new Stop(provider_id, stringToTransportType(transport_type), route, days_mask, direction, name, stop_id);
    }

    private String transportTypeToString(TransportType transportType) {
        if (transportType != null)
            return transportType.name();

        return "null";
    }

    private TransportType stringToTransportType(String str) {
        return TransportType.valueOf(str);
    }
}
