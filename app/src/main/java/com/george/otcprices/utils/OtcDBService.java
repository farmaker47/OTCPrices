package com.george.otcprices.utils;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.george.otcprices.MainActivity;

/**
 * Created by farmaker1 on 04/05/2018.
 */

public class OtcDBService extends IntentService {

    public OtcDBService(){
        super("OtcDBService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String url="";
        if (intent.hasExtra(MainActivity.URL_DATABASE_STORAGE)) {
            url = intent.getStringExtra(MainActivity.URL_DATABASE_STORAGE);
            DownloadDBFunction.downloadFromInternet(this,url);

        }
    }
}
