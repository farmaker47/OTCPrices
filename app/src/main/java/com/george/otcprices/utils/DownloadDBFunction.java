package com.george.otcprices.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.george.otcprices.R;
import com.george.otcprices.data.OtcConract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadDBFunction {

    public static final String NUMBER_OF_FIREBASE_RECEIVER = "updating";

    //Method for downloading a FRESH database from internet so it can be overwritten
    public static void downloadFromInternet(Context context) {
        String urlToUse = context.getString(R.string.db_firebase_path);

        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        //the path where DB already exists
        String path = OtcConract.MainRecycler.DB_PATH;

        File dir = new File(path);
        if (!dir.exists())
            dir.mkdirs();

        try {
            URL url = new URL(urlToUse);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e("Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage(), "server");
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();

            File fToPut = new File(dir, "otcData.db");

            /// set Append to false if you want to overwrite
            output = new FileOutputStream(fToPut, false);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }

        //When downloading is finished a broadcast is sent to the broadcast receiver in Main Actvity
        Intent intent = new Intent();
        intent.setAction(NUMBER_OF_FIREBASE_RECEIVER);
        context.sendBroadcast(intent);

    }


}
