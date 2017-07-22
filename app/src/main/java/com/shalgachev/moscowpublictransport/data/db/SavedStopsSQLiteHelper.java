package com.shalgachev.moscowpublictransport.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.shalgachev.moscowpublictransport.data.Direction;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.TransportType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anton on 7/2/2017.
 */

    public class SavedStopsSQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_SAVED_STOPS = "saved_stops";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PROVIDER_ID = "provider_id";
    public static final String COLUMN_TRANSPORT_TYPE = "transport_type";
    public static final String COLUMN_ROUTE = "route";
    public static final String COLUMN_DAYS_ROUTE = "days_mask";
    public static final String COLUMN_DIRECTION_ID = "direction_id";
    public static final String COLUMN_DIRECTION_FROM = "direction_from";
    public static final String COLUMN_DIRECTION_TO = "direction_to";
    public static final String COLUMN_NAME = "name";

    private static final String DATABASE_NAME = "saved_stops.db";
    private static final int DATABASE_VERSION = 3;

    public SavedStopsSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String DATABASE_CREATE_QUERY = "create table " + TABLE_SAVED_STOPS
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
                + ");";

        Log.i("SavedStopsSQLiteHelper", String.format("Creating new database with query:\n%s", DATABASE_CREATE_QUERY));

        db.execSQL(DATABASE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("SavedStopsSQLiteHelper", "Upgrading database from version " + oldVersion
                + " to " + newVersion + ", which will destroy all old data");
        // TODO: 7/23/2017 Don't drop database on upgrade
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED_STOPS);
        onCreate(db);
    }

    public void addStop(Stop stop) {
        Log.d("SavedStopsSQLiteHelper", String.format("addStop('%s')", stop.toString()));

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

        db.insert(TABLE_SAVED_STOPS, null, values);
    }

    public Stop getStop(int id) {
        Log.d("SavedStopsSQLiteHelper", String.format("getStop('%d')", id));

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_SAVED_STOPS, new String[] {
                        COLUMN_ID,
                        COLUMN_PROVIDER_ID,
                        COLUMN_TRANSPORT_TYPE,
                        COLUMN_ROUTE,
                        COLUMN_DAYS_ROUTE,
                        COLUMN_DIRECTION_ID,
                        COLUMN_DIRECTION_FROM,
                        COLUMN_DIRECTION_TO,
                        COLUMN_NAME,
                },
                COLUMN_ID + " = ?", new String[] {String.valueOf(id)}, null, null, null, null);

        if (cursor == null || !cursor.moveToFirst())
            return null;

        return cursorToStop(cursor);
    }

    public List<Stop> getStops() {
        Log.d("SavedStopsSQLiteHelper", "getStops()");

        List<Stop> stops = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_SAVED_STOPS;

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        while(cursor.moveToNext()) {
            stops.add(cursorToStop(cursor));
        }

        return stops;
    }

    public List<Stop> getStops(TransportType transportType) {
        Log.d("SavedStopsSQLiteHelper", String.format("getStops('%s')", transportTypeToString(transportType)));

        List<Stop> stops = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_SAVED_STOPS + "WHERE "
                + COLUMN_TRANSPORT_TYPE + " = ?";

        String[] selectArgs = new String[] {
                transportTypeToString(transportType)
        };

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, selectArgs);

        while(cursor.moveToNext()) {
            stops.add(cursorToStop(cursor));
        }

        return stops;
    }

    public void deleteStop(Stop stop) {
        Log.d("SavedStopsSQLiteHelper", String.format("deleteStop('%s')", stop.toString()));

        SQLiteDatabase db = getWritableDatabase();

        final String where = COLUMN_PROVIDER_ID + " = ?"
                + " AND " + COLUMN_TRANSPORT_TYPE + " = ?"
                + " AND " + COLUMN_ROUTE + " = ?"
                + " AND " + COLUMN_DAYS_ROUTE + " = ?"
                + " AND " + COLUMN_DIRECTION_ID + " = ?"
                + " AND " + COLUMN_NAME + " = ?";

        String[] args = new String[] {
                stop.providerId.toString(), transportTypeToString(stop.transportType), stop.route.toString(), stop.daysMask.toString(),
                stop.direction.getId().toString(),
                stop.name.toString()
        };

        int affectedRows = db.delete(TABLE_SAVED_STOPS, where, args);

        Log.d("SavedStopsSQLiteHelper", String.format("Affected rows after deleting stop: %d", affectedRows));

        if (affectedRows != 1) {
            Log.w("SavedStopsSQLiteHelper", "Expected only 1 row to be affected");
        }
    }

    private Stop cursorToStop(Cursor c) {
        String provider_id = "", transport_type = "", route = "", days_mask = "", direction_id = "", direction_from = "", direction_to = "", name = "";

        for (int i = 0; i < c.getColumnCount(); i++) {
            String cname = c.getColumnName(i);
            String cvalue = c.getString(i);
            switch (cname) {
                case COLUMN_PROVIDER_ID:
                    provider_id = cvalue;
                    break;
                case COLUMN_TRANSPORT_TYPE:
                    transport_type = cvalue;
                    break;
                case COLUMN_ROUTE:
                    route = cvalue;
                    break;
                case COLUMN_DAYS_ROUTE:
                    days_mask = cvalue;
                    break;
                case COLUMN_DIRECTION_ID:
                    direction_id = cvalue;
                    break;
                case COLUMN_DIRECTION_FROM:
                    direction_from = cvalue;
                    break;
                case COLUMN_DIRECTION_TO:
                    direction_to = cvalue;
                    break;
                case COLUMN_NAME:
                    name = cvalue;
                    break;
            }
        }
        
        Direction direction = new Direction(direction_id, direction_from, direction_to);
        return new Stop(provider_id, stringToTransportType(transport_type), route, days_mask, direction, name);
    }

    private String transportTypeToString(TransportType transportType) {
        return transportType.name();
    }

    private TransportType stringToTransportType(String str) {
        return TransportType.valueOf(str);
    }
}
