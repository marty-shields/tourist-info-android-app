package com.cet325.bg47hb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PlaceDBOpenHelper extends SQLiteOpenHelper {

    //database information
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "touristInfo.db";
    public static final String PLACES_TABLE_NAME = "places";

    //database table information
    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_PRICE = "price";
    public static final String KEY_RANK = "rank";
    public static final String KEY_DATEPLANNED = "date_planned";
    public static final String KEY_DATEVISITED = "date_visited";
    public static final String KEY_FAVORITE = "favorite";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_NOTES = "notes";


    // all columns together in an array
    public static final String[] ALL_COLUMNS =
            {KEY_ID, KEY_NAME, KEY_LOCATION, KEY_LONGITUDE, KEY_LATITUDE, KEY_DESCRIPTION, KEY_PRICE,
                    KEY_RANK, KEY_DATEPLANNED, KEY_DATEVISITED, KEY_FAVORITE, KEY_IMAGE, KEY_NOTES};

    //SQL statement in order to create table
    private static final String CREATE_TABLE =
            "CREATE TABLE " + PLACES_TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_NAME + " TEXT, " +
                    KEY_LOCATION + " TEXT, " +
                    KEY_LATITUDE + " TEXT, " +
                    KEY_LONGITUDE + " TEXT, " +
                    KEY_DESCRIPTION + " TEXT, " +
                    KEY_PRICE + " INTEGER, " +
                    KEY_RANK + " INTEGER, " +
                    KEY_DATEPLANNED + " TEXT, " +
                    KEY_DATEVISITED + " TEXT, " +
                    KEY_FAVORITE + " BOOLEAN, " +
                    KEY_NOTES + " TEXT, " +
                    KEY_IMAGE +" TEXT);";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        Log.d("database created", db.getPath().toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older places table if existed
        db.execSQL("DROP TABLE IF EXISTS " + PLACES_TABLE_NAME);

        // create fresh books table
        this.onCreate(db);
    }

    public PlaceDBOpenHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
