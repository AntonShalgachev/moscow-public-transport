package com.shalgachev.moscowpublictransport.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.shalgachev.moscowpublictransport.BuildConfig;
import com.shalgachev.moscowpublictransport.data.Direction;
import com.shalgachev.moscowpublictransport.data.Route;
import com.shalgachev.moscowpublictransport.data.Schedule;
import com.shalgachev.moscowpublictransport.data.ScheduleDays;
import com.shalgachev.moscowpublictransport.data.ScheduleType;
import com.shalgachev.moscowpublictransport.data.Season;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.Timepoint;
import com.shalgachev.moscowpublictransport.data.TransportType;
import com.shalgachev.moscowpublictransport.data.db.migrators.Migrator;
import com.shalgachev.moscowpublictransport.data.db.migrators.Migrator10;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by anton on 7/2/2017.
 */

public class ScheduleCacheSQLiteHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = "ScheduleCacheSQLiteHlpr";
    private static final boolean SQL_DEBUG = true;

    // tables
    public static final String TABLE_SAVED_STOPS = "saved_stops";
    public static final String TABLE_STOPS_TRAITS = "stops_traits";
    public static final String TABLE_TIMETABLES = "timetables";
    public static final String TABLE_STOPS_ON_MAIN_SCREEN = "stops_on_main_screen";
    public static final String TABLE_STOPS_WIDGET_SIMPLE_STOP = "stops_widget_simple_stop";

    // columns
    // common
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SAVED_STOP_ID = "saved_stop_id";

    // TABLE_SAVED_STOPS
    public static final String COLUMN_PROVIDER_ID = "provider_id";
    public static final String COLUMN_TRANSPORT_TYPE = "transport_type";
    public static final String COLUMN_ROUTE_ID = "route_id";
    public static final String COLUMN_SEASON = "season";
    public static final String COLUMN_DAYS_ID = "days_id";
    public static final String COLUMN_DIRECTION_ID = "direction_id";
    public static final String COLUMN_STOP_ID = "stop_id";

    // TABLE_STOPS_TRAITS
    public static final String COLUMN_STOP_NAME = "stop_name";
    public static final String COLUMN_ROUTE_NAME = "route_name";
    public static final String COLUMN_DIRECTION_FROM = "direction_from";
    public static final String COLUMN_DIRECTION_TO = "direction_to";
    public static final String COLUMN_DAYS_MASK = "days_mask";
    public static final String COLUMN_FIRST_HOUR = "first_hour";
    public static final String COLUMN_SCHEDULE_TYPE = "schedule_type";

    // TABLE_TIMETABLES
    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_MINUTE = "minute";
    public static final String COLUMN_COLOR = "color";
    public static final String COLUMN_NOTE = "note";

    // TABLE_STOPS_WIDGET_SIMPLE_STOP
    public static final String COLUMN_WIDGET_ID = "widget_id";

    private static final String DATABASE_NAME = "moscow_public_transport.db";
    private static final int DATABASE_VERSION = 11;

    private static final List<Migrator> MIGRATORS;

    static
    {
        MIGRATORS = new ArrayList<>();
        MIGRATORS.add(new Migrator10());
    }

    public ScheduleCacheSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int version = oldVersion;

        for (Migrator migrator : MIGRATORS) {
            if (migrator.getOldVersion() != version)
                continue;

            migrator.migrate(db);
            version = migrator.getNewVersion();
        }

        if (version != newVersion) {
            String message = String.format(Locale.getDefault(), "Failed to migrate from %d to %d", oldVersion, newVersion);
            if (BuildConfig.DEBUG) {
                throw new IllegalStateException(message);
            } else {
                dropDatabase(db);
                createDatabase(db);
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG)
            super.onDowngrade(db, oldVersion, newVersion);
    }

    public void dropDatabase(SQLiteDatabase db) {
        // TODO: 8/19/2018 Backup database?
        Log.w(LOG_TAG, "Dropping whole database");

        final String SAVED_STOPS_DROP_QUERY = "DROP TABLE IF EXISTS " + TABLE_SAVED_STOPS;
        final String STOPS_TRAITS_DROP_QUERY = "DROP TABLE IF EXISTS " + TABLE_STOPS_TRAITS;
        final String TIMETABLES_DROP_QUERY = "DROP TABLE IF EXISTS " + TABLE_TIMETABLES;
        final String STOPS_ON_MAIN_SCREEN_DROP_QUERY = "DROP TABLE IF EXISTS " + TABLE_STOPS_ON_MAIN_SCREEN;
        final String TABLE_STOPS_WIDGET_SIMPLE_STOP_DROP_QUERY = "DROP TABLE IF EXISTS " + TABLE_STOPS_WIDGET_SIMPLE_STOP;

        execSQLDebug(db, SAVED_STOPS_DROP_QUERY, true);
        execSQLDebug(db, STOPS_TRAITS_DROP_QUERY, true);
        execSQLDebug(db, TIMETABLES_DROP_QUERY, true);
        execSQLDebug(db, STOPS_ON_MAIN_SCREEN_DROP_QUERY, true);
        execSQLDebug(db, TABLE_STOPS_WIDGET_SIMPLE_STOP_DROP_QUERY, true);
    }

    public void createDatabase(SQLiteDatabase db) {
        Log.i(LOG_TAG, "Creating new database");
        final String SAVED_STOPS_CREATE_QUERY = "create table " + TABLE_SAVED_STOPS
                + "( "
                + COLUMN_ID + " integer primary key autoincrement"
                + ", " + COLUMN_PROVIDER_ID + " text not null"
                + ", " + COLUMN_TRANSPORT_TYPE + " text not null"
                + ", " + COLUMN_ROUTE_ID + " text not null"
                + ", " + COLUMN_SEASON + " text not null"
                + ", " + COLUMN_DAYS_ID + " text not null"
                + ", " + COLUMN_DIRECTION_ID + " text not null"
                + ", " + COLUMN_STOP_ID + " integer not null"
                + ");";
        final String STOPS_TRAITS_CREATE_QUERY = "create table " + TABLE_STOPS_TRAITS
                + "( "
                + COLUMN_ID + " integer primary key autoincrement"
                + ", " + COLUMN_SAVED_STOP_ID + " integer not null"
                + ", " + COLUMN_STOP_NAME + " text not null"
                + ", " + COLUMN_ROUTE_NAME + " text not null"
                + ", " + COLUMN_DIRECTION_FROM + " text not null"
                + ", " + COLUMN_DIRECTION_TO + " text not null"
                + ", " + COLUMN_DAYS_MASK + " text not null"
                + ", " + COLUMN_FIRST_HOUR + " integer not null"
                + ", " + COLUMN_SCHEDULE_TYPE + " text not null"
                + ");";
        final String TIMETABLES_CREATE_QUERY = "create table " + TABLE_TIMETABLES
                + "( "
                + COLUMN_ID + " integer primary key autoincrement"
                + ", " + COLUMN_SAVED_STOP_ID + " integer not null"
                + ", " + COLUMN_HOUR + " integer not null"
                + ", " + COLUMN_MINUTE + " integer not null"
                + ", " + COLUMN_COLOR + " text"
                + ", " + COLUMN_NOTE + " text"
                + ");";
        final String STOPS_ON_MAIN_SCREEN_CREATE_QUERY = "create table " + TABLE_STOPS_ON_MAIN_SCREEN
                + "( "
                + COLUMN_ID + " integer primary key autoincrement"
                + ", " + COLUMN_SAVED_STOP_ID + " integer not null"
                + ");";
        final String TABLE_STOPS_WIDGET_SIMPLE_STOP_CREATE_QUERY = "create table " + TABLE_STOPS_WIDGET_SIMPLE_STOP
                + "( "
                + COLUMN_ID + " integer primary key autoincrement"
                + ", " + COLUMN_WIDGET_ID + " integer not null"
                + ", " + COLUMN_SAVED_STOP_ID + " integer not null"
                + ");";

        execSQLDebug(db, SAVED_STOPS_CREATE_QUERY, true);
        execSQLDebug(db, STOPS_TRAITS_CREATE_QUERY, true);
        execSQLDebug(db, TIMETABLES_CREATE_QUERY, true);
        execSQLDebug(db, STOPS_ON_MAIN_SCREEN_CREATE_QUERY, true);
        execSQLDebug(db, TABLE_STOPS_WIDGET_SIMPLE_STOP_CREATE_QUERY, true);
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

    private int getSavedStopId(SavedStop stop) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cur = db.query(TABLE_SAVED_STOPS, new String[]{COLUMN_ID}, getStopWhereClause(), getStopWhereArgs(stop), null, null, null);

        if (cur == null)
            return -1;

        if (!cur.moveToFirst()) {
            cur.close();
            return -1;
        }

        int stopId = cur.getInt(cur.getColumnIndexOrThrow(COLUMN_ID));
        cur.close();

        if (stopId < 0)
            Log.e(LOG_TAG, "Negative id " + stopId + " for saved stop " + stop.toString());

        return stopId;
    }

    public Schedule getSchedule(Stop stop) {
        Log.i(LOG_TAG, String.format("getSchedule('%s')", stop.toString()));
        SavedStop savedStop = convertToSavedStop(stop);
        StopTraits traits = convertToStopTraits(stop);
        int stopId = getSavedStopId(savedStop);

        ScheduleType type = traits.scheduleType;

        switch (type) {
            case TIMEPOINTS:
                return getTimetableSchedule(stop, stopId);
            case INTERVALS:
                throw new UnsupportedOperationException("Intervals aren't yet supported");
        }

        throw new IllegalArgumentException(String.format("Invalid schedule type '%s'", type.name()));
    }

    private StopTraits getStopTraits(int stopId) {
        SQLiteDatabase db = getReadableDatabase();

        String whereClause = COLUMN_SAVED_STOP_ID + " = ?";
        String[] whereArgs = {
                String.valueOf(stopId)
        };
        String[] traitCols = {
                COLUMN_STOP_NAME,
                COLUMN_ROUTE_NAME,
                COLUMN_DIRECTION_FROM,
                COLUMN_DIRECTION_TO,
                COLUMN_DAYS_MASK,
                COLUMN_FIRST_HOUR,
                COLUMN_SCHEDULE_TYPE,
        };

        Cursor cur = db.query(TABLE_STOPS_TRAITS, traitCols, whereClause, whereArgs, "", "", "");
        try {
            if (cur != null && cur.moveToFirst()) {
                int rows = cur.getCount();
                if (rows != 1) {
                    // TODO: 3/31/2018 throw error
                    Log.e(LOG_TAG, String.format("There are %d stop traits for stop id %d. Expected no more than 1", rows, stopId));
                }

                StopTraits traits = new StopTraits();
                traits.stopName = cur.getString(cur.getColumnIndexOrThrow(COLUMN_STOP_NAME));
                traits.routeName = cur.getString(cur.getColumnIndexOrThrow(COLUMN_ROUTE_NAME));
                traits.directionFrom = cur.getString(cur.getColumnIndexOrThrow(COLUMN_DIRECTION_FROM));
                traits.directionTo = cur.getString(cur.getColumnIndexOrThrow(COLUMN_DIRECTION_TO));
                traits.daysMask = cur.getString(cur.getColumnIndexOrThrow(COLUMN_DAYS_MASK));
                traits.firstHour = cur.getInt(cur.getColumnIndexOrThrow(COLUMN_FIRST_HOUR));
                traits.scheduleType = ScheduleType.valueOf(cur.getString(cur.getColumnIndexOrThrow(COLUMN_SCHEDULE_TYPE)));

                return traits;
            }
        } finally {
            if (cur != null)
                cur.close();
        }

        // TODO: 3/31/2018 throw error
        Log.e(LOG_TAG, String.format("There is no stop traits associated with stop id %d", stopId));
        return null;
    }

    private SavedStop getSavedStopFromCursor(Cursor cur) {
        SavedStop stop = new SavedStop();

        stop.providerId = cur.getString(cur.getColumnIndexOrThrow(COLUMN_PROVIDER_ID));
        stop.transportType = stringToEnum(TransportType.class, cur.getString(cur.getColumnIndexOrThrow(COLUMN_TRANSPORT_TYPE)));
        stop.routeId = cur.getString(cur.getColumnIndexOrThrow(COLUMN_ROUTE_ID));
        stop.season = stringToEnum(Season.class, cur.getString(cur.getColumnIndexOrThrow(COLUMN_SEASON)));
        stop.daysId = cur.getString(cur.getColumnIndexOrThrow(COLUMN_DAYS_ID));
        stop.directionId = cur.getString(cur.getColumnIndexOrThrow(COLUMN_DIRECTION_ID));
        stop.stopId = cur.getInt(cur.getColumnIndexOrThrow(COLUMN_STOP_ID));

        return stop;
    }

    private Schedule getTimetableSchedule(Stop stop, int stopId) {
        Log.d(LOG_TAG, String.format("getTimetableSchedule('%s', %d)", stop.toString(), stopId));
        SQLiteDatabase db = getReadableDatabase();

        String whereClause = COLUMN_SAVED_STOP_ID + " = ?";
        String[] whereArgs = {
                String.valueOf(stopId)
        };
        String[] timetableColumns = {
                COLUMN_HOUR,
                COLUMN_MINUTE,
                COLUMN_COLOR,
                COLUMN_NOTE,
        };

        Cursor cur = db.query(TABLE_TIMETABLES, timetableColumns, whereClause, whereArgs, "", "", "");
        if (cur == null) {
            Log.wtf(LOG_TAG, "Cursor is empty");
            // TODO: 3/31/2018 throw error
            return null;
        }

        int dataSize = cur.getCount();
        if (dataSize <= 0) {
            Log.i(LOG_TAG, "No timepoints found");
            return null;
        }

        Log.i(LOG_TAG, String.format("Found %d timepoints", dataSize));

        List<Timepoint> timepoints = new ArrayList<>();

        try {
            while (cur.moveToNext()) {
                int hour = cur.getInt(cur.getColumnIndexOrThrow(COLUMN_HOUR));
                int minute = cur.getInt(cur.getColumnIndexOrThrow(COLUMN_MINUTE));

                Timepoint.Color color = getEnum(Timepoint.Color.class, cur, COLUMN_COLOR);
                String note = getString(cur, COLUMN_NOTE);
                if (color != null && note != null)
                    timepoints.add(new Timepoint(hour, minute, color, note));
                else
                    timepoints.add(new Timepoint(hour, minute));
            }
        } finally {
            cur.close();
        }

        Schedule schedule = new Schedule();
        schedule.setAsTimepoints(stop, timepoints);

        return schedule;
    }

    public void saveSchedule(Schedule schedule) {
        Log.i(LOG_TAG, String.format("saveSchedule('%s')", schedule.toString()));

        Stop stop = schedule.getStop();
        SavedStop savedStop = convertToSavedStop(stop);
        StopTraits traits = convertToStopTraits(stop);
        int stopId = getSavedStopId(savedStop);

        removeSchedule(stopId, traits);

        ScheduleType type = schedule.getScheduleType();

        switch (type) {
            case TIMEPOINTS:
                saveTimetableSchedule(schedule, stopId);
                break;
            case INTERVALS:
                throw new UnsupportedOperationException("Intervals aren't yet supported");
        }
    }

    private void saveTimetableSchedule(Schedule schedule, int stopId) {
        Log.d(LOG_TAG, String.format("saveTimetableSchedule('%s', %d)", schedule.toString(), stopId));
        SQLiteDatabase db = getWritableDatabase();

        List<Timepoint> timepoints = schedule.getTimepoints().getTimepoints();

        for (Timepoint timepoint : timepoints) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_SAVED_STOP_ID, stopId);
            values.put(COLUMN_HOUR, timepoint.hour);
            values.put(COLUMN_MINUTE, timepoint.minute);

            if (timepoint.color != null)
                values.put(COLUMN_COLOR, enumToString(timepoint.color));
            if (timepoint.note != null)
                values.put(COLUMN_NOTE, timepoint.note);

            db.insert(TABLE_TIMETABLES, "", values);
        }

        Log.i(LOG_TAG, String.format("Saved %d timepoints", timepoints.size()));
    }

    private void removeSchedule(int stopId, StopTraits traits) {
        Log.d(LOG_TAG, String.format("removeSchedule(%d)", stopId));

        switch(traits.scheduleType) {
            case TIMEPOINTS:
                removeTimetableSchedule(stopId);
                break;
            case INTERVALS:
                throw new UnsupportedOperationException("Intervals aren't yet supported");
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

    private boolean isAddedToMainMenu(int stopId) {
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
        Log.i(LOG_TAG, String.format("addToMainMenu('%s')", stop.toString()));

        SavedStop savedStop = convertToSavedStop(stop);
        StopTraits traits = convertToStopTraits(stop);
        int stopId = getSavedStopId(savedStop);

        if (stopId < 0) {
            Log.d(LOG_TAG, "Saving stop first");
            stopId = saveStop(savedStop);
            saveStopTraits(stopId, traits);
        }

        if (isAddedToMainMenu(stopId)) {
            Log.w(LOG_TAG, "The stop is already added");
            return;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_SAVED_STOP_ID, stopId);
        long id = db.insert(TABLE_STOPS_ON_MAIN_SCREEN, "", contentValues);

        Log.i(LOG_TAG, String.format("Stop %d added to main menu with id %d", stopId, id));
    }

    public void removeFromMainMenu(Stop stop) {
        Log.i(LOG_TAG, String.format("removeFromMainMenu('%s')", stop.toString()));

        SavedStop savedStop = convertToSavedStop(stop);
        StopTraits traits = convertToStopTraits(stop);
        int stopId = getSavedStopId(savedStop);

        if (stopId < 0) {
            Log.w(LOG_TAG, "The stop isn't saved");
            return;
        }

//        if (!isAddedToMainMenu(stopId)) {
//            Log.w(LOG_TAG, "The stop isn't added to the main menu");
//            return;
//        }

        SQLiteDatabase db = getWritableDatabase();

        String whereClause = COLUMN_SAVED_STOP_ID + " = ?";
        String[] whereArgs = new String[] {
                String.valueOf(stopId)
        };

        int affectedRows = db.delete(TABLE_STOPS_ON_MAIN_SCREEN, whereClause, whereArgs);

        Log.i(LOG_TAG, String.format("%d entires were removed", affectedRows));
        if (affectedRows != 1) {
            Log.w(LOG_TAG, "Expected only 1 row to be affected");
        }

        cleanUnused();
    }

    private int saveStop(SavedStop stop) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PROVIDER_ID, stop.providerId);
        values.put(COLUMN_TRANSPORT_TYPE, enumToString(stop.transportType));
        values.put(COLUMN_ROUTE_ID, stop.routeId);
        values.put(COLUMN_SEASON, enumToString(stop.season));
        values.put(COLUMN_DAYS_ID, stop.daysId);
        values.put(COLUMN_DIRECTION_ID, stop.directionId);
        values.put(COLUMN_STOP_ID, stop.stopId);

        return (int)db.insert(TABLE_SAVED_STOPS, null, values);
    }

    private void saveStopTraits(int stopId, StopTraits traits) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_SAVED_STOP_ID, stopId);
        values.put(COLUMN_ROUTE_NAME, traits.routeName);
        values.put(COLUMN_STOP_NAME, traits.stopName);
        values.put(COLUMN_DIRECTION_FROM, traits.directionFrom);
        values.put(COLUMN_DIRECTION_TO, traits.directionTo);
        values.put(COLUMN_DAYS_MASK, traits.daysMask);
        values.put(COLUMN_FIRST_HOUR, traits.firstHour);
        values.put(COLUMN_SCHEDULE_TYPE, enumToString(traits.scheduleType));

        db.insert(TABLE_STOPS_TRAITS, null, values);
    }

    public List<Stop> getStopsOnMainMenu() {
        return getStopsOnMainMenu(null);
    }

    public List<Stop> getStopsOnMainMenu(TransportType transportType) {
        Log.i(LOG_TAG, String.format("getStopsOnMainMenu('%s')", enumToString(transportType)));

        List<Stop> stops = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_SAVED_STOPS;
        String[] selectArgs = null;

        if (transportType != null) {
            selectQuery += " WHERE " + COLUMN_TRANSPORT_TYPE + " = ?";
            selectArgs = new String[] {
                    enumToString(transportType),
            };
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cur = db.rawQuery(selectQuery, selectArgs);

        // TODO: 3/31/2018 optimize, make one query
        while (cur.moveToNext()) {
            int stopId = cur.getInt(cur.getColumnIndexOrThrow(COLUMN_ID));

            if (isAddedToMainMenu(stopId)) {
                StopTraits traits = getStopTraits(stopId);
                SavedStop savedStop = getSavedStopFromCursor(cur);
                stops.add(convertToStop(savedStop, traits));
            }
        }

        cur.close();

        Log.i(LOG_TAG, String.format("Found %d stops", stops.size()));

        return stops;
    }

    private boolean hasWidgetId(int widgetId) {
        if (widgetId < 0)
            return false;

        SQLiteDatabase db = getReadableDatabase();

        String whereClause = COLUMN_WIDGET_ID + " = ?";
        String[] whereArgs = new String[] {
                String.valueOf(widgetId)
        };

        long rows = DatabaseUtils.queryNumEntries(db, TABLE_STOPS_WIDGET_SIMPLE_STOP, whereClause, whereArgs);

        if (rows < 0 || rows > 1)
            Log.w(LOG_TAG, String.format("queryNumEntries returned %d rows", rows));

        return rows > 0;
    }

    public void addStopToWidgetSimpleStop(Stop stop, int widgetId) {
        Log.i(LOG_TAG, String.format("addStopToWidgetSimpleStop('%s', %d)", stop.toString(), widgetId));

        SavedStop savedStop = convertToSavedStop(stop);
        int stopId = getSavedStopId(savedStop);

        if (stopId < 0) {
            Log.e(LOG_TAG, "Stop isn't saved!");
            // TODO: 6/23/2018 throw error
            return;
        }

        if (hasWidgetId(widgetId)) {
            Log.w(LOG_TAG, "Found an existing widget!");
            removeStopToWidgetSimpleStop(widgetId);
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_WIDGET_ID, widgetId);
        contentValues.put(COLUMN_SAVED_STOP_ID, stopId);
        long id = db.insert(TABLE_STOPS_WIDGET_SIMPLE_STOP, "", contentValues);

        Log.i(LOG_TAG, String.format("Stop %d added to widget with id %d", stopId, id));
    }

    public void removeStopToWidgetSimpleStop(int widgetId) {
        Log.i(LOG_TAG, String.format("removeStopToWidgetSimpleStop(%d)", widgetId));

        SQLiteDatabase db = getWritableDatabase();

        String whereClause = COLUMN_WIDGET_ID + " = ?";
        String[] whereArgs = new String[] {
                String.valueOf(widgetId)
        };

        int affectedRows = db.delete(TABLE_STOPS_WIDGET_SIMPLE_STOP, whereClause, whereArgs);

        Log.i(LOG_TAG, String.format("%d entires were removed", affectedRows));
        if (affectedRows != 1) {
            Log.w(LOG_TAG, "Expected only 1 row to be affected");
        }
    }

    public Stop getStopForWidgetId(int widgetId) {
        Log.i(LOG_TAG, String.format("getStopForWidgetId(%d)", widgetId));

        SQLiteDatabase db = getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_SAVED_STOPS + " , " + TABLE_STOPS_WIDGET_SIMPLE_STOP +
                " WHERE " + TABLE_SAVED_STOPS + "." + COLUMN_ID + " = " + TABLE_STOPS_WIDGET_SIMPLE_STOP + "." + COLUMN_SAVED_STOP_ID +
                " AND " + COLUMN_WIDGET_ID + " = ?";
        String[] selectArgs = new String[] {
                String.valueOf(widgetId),
        };

        Log.d(LOG_TAG, selectQuery);
        Cursor cur = db.rawQuery(selectQuery, selectArgs);

        try {
            if (cur != null && cur.moveToFirst()) {
                int rows = cur.getCount();
                if (rows != 1) {
                    // TODO: 3/31/2018 throw error
                    Log.e(LOG_TAG, String.format("There are %d stops for widget id %d. Expected no more than 1", rows, widgetId));
                }

                SavedStop savedStop = getSavedStopFromCursor(cur);
                StopTraits traits = getStopTraits(cur.getInt(cur.getColumnIndexOrThrow(COLUMN_SAVED_STOP_ID)));

                if (traits == null) {
                    // TODO: 6/23/2018 throw error
                    Log.e(LOG_TAG, String.format("No traits for stop %d", savedStop.stopId));
                    return null;
                }

                return convertToStop(savedStop, traits);
            }
        } finally {
            if (cur != null)
                cur.close();
        }

        return null;
    }

    private SavedStop convertToSavedStop(Stop stop) {
        SavedStop savedStop = new SavedStop();

        savedStop.providerId = stop.route.providerId;
        savedStop.transportType = stop.route.transportType;
        savedStop.routeId = stop.route.id;
        savedStop.daysId = stop.days.daysId;
        savedStop.season = stop.days.season;
        savedStop.directionId = stop.direction.getId();
        savedStop.stopId = stop.id;

        return savedStop;
    }

    private StopTraits convertToStopTraits(Stop stop) {
        StopTraits traits = new StopTraits();

        traits.stopName = stop.name;
        traits.routeName = stop.route.name;
        traits.directionFrom = stop.direction.getFrom();
        traits.directionTo = stop.direction.getTo();
        traits.daysMask = stop.days.daysMask;
        traits.firstHour = stop.days.firstHour;
        traits.scheduleType = stop.scheduleType;

        return traits;
    }

    private Stop convertToStop(SavedStop savedStop, StopTraits traits) {
        Route route = new Route(savedStop.transportType, savedStop.routeId, traits.routeName, savedStop.providerId);
        ScheduleDays days = new ScheduleDays(savedStop.daysId, traits.daysMask, savedStop.season, traits.firstHour);

        Direction dir = new Direction(savedStop.directionId, traits.directionFrom, traits.directionTo);

        return new Stop(route, days, dir, traits.stopName, savedStop.stopId, traits.scheduleType);
    }

    private String getStopWhereClause() {
        return COLUMN_PROVIDER_ID + " = ?"
                + " AND " + COLUMN_TRANSPORT_TYPE + " = ?"
                + " AND " + COLUMN_ROUTE_ID + " = ?"
                + " AND " + COLUMN_DAYS_ID + " = ?"
                + " AND " + COLUMN_SEASON + " = ?"
                + " AND " + COLUMN_DIRECTION_ID + " = ?"
                + " AND " + COLUMN_STOP_ID + " = ?";
    }

    private String[] getStopWhereArgs(SavedStop stop) {
        return new String[]{
                stop.providerId,
                enumToString(stop.transportType),
                stop.routeId,
                stop.daysId,
                enumToString(stop.season),
                stop.directionId,
                String.valueOf(stop.stopId),
        };
    }

    private String getString(Cursor cur, String colName)
    {
        int colIndex = cur.getColumnIndexOrThrow(colName);

        if (cur.isNull(colIndex))
            return null;

        return cur.getString(colIndex);
    }

    private <T extends Enum<T>> T getEnum(Class<T> c, Cursor cur, String colName) {
        return stringToEnum(c, getString(cur, colName));
    }

    private String enumToString(Enum value) {
        if (value != null)
            return value.name();

        return "null";
    }

    private <T extends Enum<T>> T stringToEnum(Class<T> c, String str) {
        if (str == null || str.equals("null"))
            return null;

        return T.valueOf(c, str);
    }
}
