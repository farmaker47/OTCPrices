package com.george.otcprices;

import android.app.ActivityOptions;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.george.otcprices.data.OTCMainDBHelper;
import com.george.otcprices.data.OtcConract;
import com.george.otcprices.utils.DownloadDBFunction;
import com.george.otcprices.utils.MedicineJobScheduler;
import com.george.otcprices.utils.OtcDBService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
        MainRecyclerViewAdapter.MedicinesClickItemListener {

    private OTCMainDBHelper otcMainDBHelper;
    private SQLiteDatabase mDb;

    private static final int DATABASE_LOADER = 23;
    private ArrayList<MedicinesObject> medicineList;
    private LinearLayoutManager layoutManager;
    private MainRecyclerViewAdapter mainRecyclerViewAdapter;
    private SearchView searchView;
    private static final String SEARCH_KEY = "search_key";
    public static final String ID_TO_PASS = "id_to_pass";
    public static final String NAME_TO_PASS = "name_to_pass";
    public static final String INTERNET_TO_PASS = "internet_to_pass";
    private String mSearchString, mDownLoadString, mSearchDeletion;

    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mFilter;

    private Parcelable savedRecyclerLayoutState;
    private static final String BUNDLE_RECYCLER_LAYOUT = "recycler_layout";

    private static final int NOTIFICATION_ID = 4000;

    @BindView(R.id.recyclerMainMedicine)
    RecyclerView recyclerViewMain;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.adView)
    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mDownLoadString = getString(R.string.old_db);

        setSupportActionBar(toolbar);

        try {
            otcMainDBHelper = new OTCMainDBHelper(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mDb = otcMainDBHelper.getReadableDatabase();

        setTitle(getString(R.string.otc_prices_title));

        recyclerViewMain.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewMain.setLayoutManager(layoutManager);

        medicineList = new ArrayList<>();
        mainRecyclerViewAdapter = new MainRecyclerViewAdapter(this, medicineList, this);
        recyclerViewMain.setAdapter(mainRecyclerViewAdapter);

        //restore recycler view at same position
        if (savedInstanceState != null) {
            savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
        }

        //start loader to fetch medicines
        android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> internetLoader = loaderManager.getLoader(DATABASE_LOADER);
        if (internetLoader == null) {
            loaderManager.initLoader(DATABASE_LOADER, null, mLoaderDatabase);
        } else {
            loaderManager.restartLoader(DATABASE_LOADER, null, mLoaderDatabase);
        }

        mBroadcastReceiver = new OtcMedicineBroadcast();
        mFilter = new IntentFilter();
        mFilter.addAction(DownloadDBFunction.NUMBER_OF_FIREBASE_RECEIVER);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Retrieve the id of the medicine to delete
                int id = (int) viewHolder.itemView.getTag();

                // Build appropriate uri with String row id appended
                String stringId = Integer.toString(id);
                Uri uri = OtcConract.MainRecycler.CONTENT_URI_MAIN;
                uri = uri.buildUpon().appendPath(stringId).build();

                // Delete a single row of data using a ContentResolver
                getContentResolver().delete(uri, null, null);

                // Restart the loader to re-query for all medicines after a deletion
                mainRecyclerViewAdapter.setMedicineDataAfterDownload();

                mSearchDeletion = searchView.getQuery().toString();
                Log.e("deletionString", mSearchDeletion);

                getSupportLoaderManager().restartLoader(DATABASE_LOADER, null, mLoaderDatabase);

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        searchViewDeletion();
                    }
                }, 30);

            }
        }).attachToRecyclerView(recyclerViewMain);

        //Schedule download of fresh DB
        MedicineJobScheduler.scheduleFirebaseJobDispatcherSync(this);

        //Ads by Admob
        MobileAds.initialize(this,
                "ca-app-pub-3940256099942544~3347511713");

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //set up shared preferences
        setupSharedPreferences();
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String margin = sharedPreferences.getString(getString(R.string.pref_margin_key),
                getString(R.string.pref_margin_default));

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        mainRecyclerViewAdapter.setMedicineDataAfterDownload();
        getSupportLoaderManager().restartLoader(DATABASE_LOADER, null, mLoaderDatabase);

        /*if (key.equals("margin_key")) {
            String margin = sharedPreferences.getString("margin_key", getString(R.string.pref_margin_default));
            Log.e("marginAfter", margin);
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mSearchString = savedInstanceState.getString(SEARCH_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, layoutManager.onSaveInstanceState());

        //save the search query if present
        mSearchString = searchView.getQuery().toString();
        outState.putString(SEARCH_KEY, mSearchString);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver, mFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    private LoaderManager.LoaderCallbacks mLoaderDatabase = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            /*return new CursorLoader(MainActivity.this,OtcConract.MainRecycler.CONTENT_URI_MAIN,null,null,null,null);*/

            return new AsyncTaskLoader<Cursor>(MainActivity.this) {

                Cursor cursor;

                @Override
                protected void onStartLoading() {

                    if (cursor != null) {
                        deliverResult(cursor);
                    } else {
                        forceLoad();
                    }
                }

                @Override
                public Cursor loadInBackground() {

                    try {
                        Cursor mCursor = mDb.query(OtcConract.MainRecycler.TABLE_NAME,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null);
                        return mCursor;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                public void deliverResult(Cursor data) {
                    cursor = data;
                    super.deliverResult(data);
                }

            };
        }


        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            if (data != null) {

                data.moveToFirst();

                while (!data.isAfterLast()) {
                    medicineList.add(new MedicinesObject(data.getString(data.getColumnIndex(OtcConract.MainRecycler.MAIN_NAME)),
                            data.getString(data.getColumnIndex(OtcConract.MainRecycler.MAIN_PRICE)),
                            data.getBlob(data.getColumnIndex(OtcConract.MainRecycler.MAIN_IMAGE)),
                            data.getString(data.getColumnIndex(OtcConract.MainRecycler._ID)),
                            data.getString(data.getColumnIndex(OtcConract.MainRecycler.MAIN_INTERNET))));
                    data.moveToNext();
                }
                data.close();
            }

            mainRecyclerViewAdapter.setMedicineData(medicineList);

            //restore recycler view position
            if (savedRecyclerLayoutState != null) {
                layoutManager.onRestoreInstanceState(savedRecyclerLayoutState);
            }

            if (mDownLoadString.equals(getString(R.string.new_db))) {
                showNotification();
                //reset the name of string
                mDownLoadString = getString(R.string.old_db);
            }

        }


        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private void showNotification() {

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = getString(R.string.channel_notification);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.my_notifications), NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription(getString(R.string.channel_descript));
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_file_download)
                .setContentTitle(getString(R.string.notification))
                .setContentText(getString(R.string.newDBDownloadCompleted))
                .setAutoCancel(true);

        Intent detailIntentForToday = new Intent(this, MainActivity.class);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addNextIntentWithParentStack(detailIntentForToday);
        PendingIntent resultPendingIntent = taskStackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(resultPendingIntent);


        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mainRecyclerViewAdapter.getFilter().filter(query);
                Log.e("Submit", "String");
                mSearchDeletion = query;
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mainRecyclerViewAdapter.getFilter().filter(query);
                Log.e("Change", "String");
                return false;
            }
        });

        searchViewQuery();

        return true;
    }

    private void searchViewQuery() {
        //focus the SearchView
        if (mSearchString != null && !mSearchString.isEmpty()) {
            searchView.setIconified(true);
            searchView.onActionViewExpanded();
            searchView.setQuery(mSearchString, true);
            searchView.setFocusable(false);

            /*mainRecyclerViewAdapter.setMedicineData(medicineList);*/

           /* // Handler which will run after 2 seconds for finishing after delay of cursorloader
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    searchView.setQuery(mSearchString, true);
                }
            }, 50);*/
        }
    }

    private void searchViewDeletion() {
        if (mSearchDeletion != null && !mSearchDeletion.isEmpty()) {
            searchView.setIconified(true);
            searchView.onActionViewExpanded();
            searchView.setQuery(mSearchDeletion, true);
            searchView.setFocusable(false);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }

        if (id == R.id.action_search) {
            return true;
        }

        if (id == R.id.action_download_firebase_manually) {
            Intent intent = new Intent(this, OtcDBService.class);
            startService(intent);
            return true;
        }

        if (id == R.id.action_instructions) {
            Intent intent2 = new Intent(this, Instructions.class);
            startActivity(intent2);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onListItemClick(int position, String name, String internet, String price) {
        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        intent.putExtra(ID_TO_PASS, String.valueOf(position));
        intent.putExtra(NAME_TO_PASS, name);
        intent.putExtra(INTERNET_TO_PASS, internet);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        } else {
            startActivity(intent);
        }

        updateWidgetWithIngredients(name, price);
    }

    private void updateWidgetWithIngredients(String name, String price) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetId = appWidgetManager.getAppWidgetIds(new ComponentName(this, OtcWidgetProvider.class));

        OtcWidgetProvider.updateWidgetWithInfo(MainActivity.this, appWidgetManager, name, price, appWidgetId);

        Toast.makeText(MainActivity.this, name + " " + getString(R.string.isAdded), Toast.LENGTH_SHORT).show();

    }

    private class OtcMedicineBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DownloadDBFunction.NUMBER_OF_FIREBASE_RECEIVER)) {
                //start loader to fetch medicines
                android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
                Loader<String> internetLoader = loaderManager.getLoader(DATABASE_LOADER);
                if (internetLoader == null) {
                    loaderManager.initLoader(DATABASE_LOADER, null, mLoaderDatabase);
                    mDownLoadString = getString(R.string.new_db);
                } else {
                    //after downloading the fresh db we first clear the list and then we restart the loader
                    mainRecyclerViewAdapter.setMedicineDataAfterDownload();
                    loaderManager.restartLoader(DATABASE_LOADER, null, mLoaderDatabase);
                    //we give a new name of the string so in on load finish to throw a notification
                    mDownLoadString = getString(R.string.new_db);

                }
            }
        }
    }
}
