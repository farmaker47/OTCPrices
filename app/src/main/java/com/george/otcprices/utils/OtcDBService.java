package com.george.otcprices.utils;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by farmaker1 on 04/05/2018.
 */

public class OtcDBService extends IntentService {

    public OtcDBService(){
        super("OtcDBService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        DownloadDBFunction.downloadFromInternet(this);
    }
}
