package com.george.otcprices.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.george.otcprices.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by farmaker1 on 01/05/2018.
 */

public class OTCMainDBHelper extends SQLiteOpenHelper {

    private Context mContext;
    public static final String DB_NAME = "otcData.db";
    private static final int DB_VERSION = 1;
    private SQLiteDatabase mDb;

    public OTCMainDBHelper(Context context) throws IOException {
        super(context, DB_NAME, null, DB_VERSION);

        this.mContext = context;

        //Checking if database exists in folder and either creates it or opens it
        boolean dbexist = checkdatabase();
        if (dbexist) {
            opendatabase();
        } else {
            createdatabase();
        }
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String DATABASE_CREATE_MAIN =
                "CREATE TABLE IF NOT EXISTS " + OtcConract.MainRecycler.TABLE_NAME + "(" +
                        OtcConract.MainRecycler._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        OtcConract.MainRecycler.MAIN_NAME + " TEXT NOT NULL, " +
                        OtcConract.MainRecycler.MAIN_INFORMATION + " TEXT NOT NULL, " +
                        OtcConract.MainRecycler.MAIN_PRICE + " TEXT NOT NULL, " +
                        OtcConract.MainRecycler.MAIN_INTERNET + " TEXT NOT NULL, " +
                        OtcConract.MainRecycler.MAIN_IMAGE + " BLOB " +
                        ");";

        sqLiteDatabase.execSQL(DATABASE_CREATE_MAIN);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + OtcConract.MainRecycler.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    private boolean checkdatabase() {

        boolean checkdb = false;
        try {
            String myPath = OtcConract.MainRecycler.DB_PATH + DB_NAME;
            File dbfile = new File(myPath);
            checkdb = dbfile.exists();
        } catch (SQLiteException e) {
            System.out.println(mContext.getString(R.string.db_Not_exist));
        }
        return checkdb;
    }

    private void opendatabase() throws SQLException {
        //Open the database
        String mypath = OtcConract.MainRecycler.DB_PATH + DB_NAME;
        mDb = SQLiteDatabase.openDatabase(mypath, null, SQLiteDatabase.OPEN_READONLY);

    }

    private void createdatabase() throws IOException {
        boolean dbexist = checkdatabase();
        if (dbexist) {
            System.out.println(mContext.getString(R.string.db_exist));
        } else {
            this.getWritableDatabase();
            try {
                copydatabase();
            } catch (IOException e) {
                throw new Error(mContext.getString(R.string.error_copying_db));
            }
        }
    }


    private void copydatabase() throws IOException {
        //Open your local db as the input stream
        InputStream myinput = mContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outfilename = OtcConract.MainRecycler.DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myoutput = new FileOutputStream(outfilename);

        // transfer byte to inputfile to outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myinput.read(buffer)) > 0) {
            myoutput.write(buffer, 0, length);
        }

        //Close the streams
        myoutput.flush();
        myoutput.close();
        myinput.close();
    }
}
