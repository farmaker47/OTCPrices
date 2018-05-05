package com.george.otcprices;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.george.otcprices.data.OTCMainDBHelper;
import com.george.otcprices.data.OtcConract;
import com.george.otcprices.utils.DownloadDBFunction;
import com.george.otcprices.utils.MedicineJobScheduler;
import com.george.otcprices.utils.OtcDBService;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private OTCMainDBHelper otcMainDBHelper;
    private SQLiteDatabase mDb;

    private static final int DATABASE_LOADER = 23;
    private ArrayList<MedicinesObject> medicineList;
    private LinearLayoutManager layoutManager;
    private MainRecyclerViewAdapter mainRecyclerViewAdapter;
    private SearchView searchView;
    private static final String SEARCH_KEY = "search_key";
    private String mSearchString,mDownLoadString;

    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mFilter;

    private Parcelable savedRecyclerLayoutState;
    private static final String BUNDLE_RECYCLER_LAYOUT = "recycler_layout";

    private static final int NOTIFICATION_ID = 4000;

    @BindView(R.id.recyclerMainMedicine)
    RecyclerView recyclerViewMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mDownLoadString = getString(R.string.old_db);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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
        mainRecyclerViewAdapter = new MainRecyclerViewAdapter(this, medicineList);
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
                getSupportLoaderManager().restartLoader(DATABASE_LOADER, null, mLoaderDatabase);

            }
        }).attachToRecyclerView(recyclerViewMain);

        //Schedule download of fresh DB
        MedicineJobScheduler.scheduleFirebaseJobDispatcherSync(this);
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
                            data.getString(data.getColumnIndex(OtcConract.MainRecycler._ID))));
                    data.moveToNext();
                }
                data.close();
            }

            mainRecyclerViewAdapter.setMedicineData(medicineList);

            //restore recycler view position
            if (savedRecyclerLayoutState != null) {
                layoutManager.onRestoreInstanceState(savedRecyclerLayoutState);
            }

            if(mDownLoadString.equals(getString(R.string.new_db))){
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

        /*String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/2.jpeg";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); //This gets the image*/

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
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

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

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
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mainRecyclerViewAdapter.getFilter().filter(query);
                return false;
            }
        });

        searchViewQuery();

        return true;
    }

    private void searchViewQuery(){
        //focus the SearchView
        if (mSearchString != null && !mSearchString.isEmpty()) {
            searchView.setIconified(true);
            searchView.onActionViewExpanded();
            searchView.setQuery(mSearchString, true);
            searchView.setFocusable(true);

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



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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
