package com.cet325.bg47hb;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

public class PlaceProvider extends ContentProvider {
    //set paramenters
    private static final String AUTHORITY = "com.cet325.bg47hb";
    private static final String BASE_PATH = "places";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static final int PLACES = 1;
    private static final int PLACES_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY,BASE_PATH,PLACES);
        uriMatcher.addURI(AUTHORITY,BASE_PATH + "/#",PLACES_ID);
    }

    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        //create database
        PlaceDBOpenHelper helper = new PlaceDBOpenHelper(getContext());
        db = helper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (uriMatcher.match(uri)){
            case PLACES:
                cursor = db.query(PlaceDBOpenHelper.PLACES_TABLE_NAME,PlaceDBOpenHelper.ALL_COLUMNS,
                        selection,null,null,null,PlaceDBOpenHelper.KEY_ID + " ASC");
                break;
            default:
                throw  new IllegalArgumentException("Unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case PLACES:
                //return the mimi type of content provider
                return "vnd.android.cursor.dir/places";
            default:
                throw new IllegalArgumentException("uknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = db.insert(PlaceDBOpenHelper.PLACES_TABLE_NAME, null, values);

        if (id > 0){
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, id);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Insertion Failed for URI :" + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int delCount = 0;
        switch (uriMatcher.match(uri)){
            case PLACES:
                delCount = db.delete(PlaceDBOpenHelper.PLACES_TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return delCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updCount = 0;
        switch (uriMatcher.match(uri)){
            case PLACES:
                updCount = db.update(PlaceDBOpenHelper.PLACES_TABLE_NAME,values,selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return updCount;
    }
}
