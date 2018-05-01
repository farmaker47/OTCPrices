package com.george.otcprices;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.george.otcprices.data.OTCMainDBHelper;
import com.george.otcprices.data.OtcConract;

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

    @BindView(R.id.recyclerMainMedicine)
    RecyclerView recyclerViewMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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
        mainRecyclerViewAdapter = new MainRecyclerViewAdapter(this,medicineList);
        recyclerViewMain.setAdapter(mainRecyclerViewAdapter);



        //start loader to fetch medicines
        android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> internetLoader = loaderManager.getLoader(DATABASE_LOADER);
        if (internetLoader == null) {
            loaderManager.initLoader(DATABASE_LOADER, null, mLoaderDatabase);
        } else {
            loaderManager.restartLoader(DATABASE_LOADER, null, mLoaderDatabase);
        }
    }

    private LoaderManager.LoaderCallbacks mLoaderDatabase = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
                            data.getString(data.getColumnIndex(OtcConract.MainRecycler.MAIN_PRICE)),data.getBlob(data.getColumnIndex(OtcConract.MainRecycler.MAIN_IMAGE))));
                    data.moveToNext();
                }

            }
            data.close();
            mainRecyclerViewAdapter.setMedicineData(medicineList);
        }


        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

        return super.onOptionsItemSelected(item);
    }
}
