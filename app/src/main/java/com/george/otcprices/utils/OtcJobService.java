package com.george.otcprices.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by farmaker1 on 05/05/2018.
 */

public class OtcJobService extends JobService {
    private AsyncTask<Void,Void,Void> mDownloadFromInternet;

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {

        mDownloadFromInternet = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Context context = getApplicationContext();

                //Execute this method to download picture
                DownloadDBFunction.downloadFromInternet(context);

                jobFinished(jobParameters, false);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(jobParameters, false);
            }
        };

        mDownloadFromInternet.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (mDownloadFromInternet != null) {
            mDownloadFromInternet.cancel(true);
        }
        return true;
    }
}
