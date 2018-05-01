package com.george.otcprices.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by farmaker1 on 01/05/2018.
 */

public class OtcConract {

    //Creating the different types of Uri for use with the provider
    public static final String AUTHORITY = "com.george.otcprices";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_TABLE_MEDICINE = "medicine";

    public static final class MainRecycler implements BaseColumns{

        //Uri for the Main Recycler table
        public static final Uri CONTENT_URI_MAIN = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TABLE_MEDICINE).build();

        public static final String TABLE_NAME = "medicine";
        public static final String DB_PATH = "/data/data/com.george.otcprices/databases/";

        public static final String MAIN_NAME = "name";
        public static final String MAIN_INFORMATION = "information";
        public static final String MAIN_IMAGE = "image";
        public static final String MAIN_PRICE = "price";
        public static final String MAIN_INTERNET = "internet";

    }
}
