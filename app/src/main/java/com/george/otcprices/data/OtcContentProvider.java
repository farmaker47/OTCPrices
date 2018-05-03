package com.george.otcprices.data;

/*
* Copyright (C) 2018 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.george.otcprices.R;

import java.io.IOException;

public class OtcContentProvider extends ContentProvider {

    //to use with Urimatcher when tries to match the Uri.
    public static final int MAIN_GRID = 100;
    public static final int MAIN_GRID_ID = 101;

    private OTCMainDBHelper dbHelper;
    private static final UriMatcher sUriMatcher = buildUriMacher();
    private Context context;

    public static UriMatcher buildUriMacher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(OtcConract.AUTHORITY, OtcConract.PATH_TABLE_MEDICINE, MAIN_GRID);
        uriMatcher.addURI(OtcConract.AUTHORITY, OtcConract.PATH_TABLE_MEDICINE + "/#", MAIN_GRID_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        context = getContext();
        try {
            dbHelper = new OTCMainDBHelper(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        final SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);

        Cursor retCursor;

        switch (match) {

            case MAIN_GRID:
                retCursor = sqLiteDatabase.query(OtcConract.MainRecycler.TABLE_NAME, strings, s, strings1, null, null, s1);
                break;
            case MAIN_GRID_ID:
                String id = uri.getPathSegments().get(1);
                retCursor = sqLiteDatabase.query(OtcConract.MainRecycler.TABLE_NAME, strings, "_id=?", new String[]{id}, null, null, s1);
                break;
            default:
                throw new UnsupportedOperationException(context.getString(R.string.unknown) + uri);

        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        // Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        // Keep track of the number of deleted medicine
        int medicineDeleted;
        switch (match) {
            // Handle the single item case, recognized by the ID included in the URI path
            case MAIN_GRID_ID:
                // Get the medicine ID from the URI path
                String id = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
               medicineDeleted = db.delete(OtcConract.MainRecycler.TABLE_NAME, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException(context.getString(R.string.unknown) + uri);
        }
        // Notify the resolver of a change and return the number of items deleted
        if (medicineDeleted != 0) {
            // A medicine (or more) was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of medicine deleted
        return medicineDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
