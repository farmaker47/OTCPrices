package com.george.otcprices;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.george.otcprices.data.OtcConract;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity {

    private String tagFromMain;
    private static final int ONE_MEDICINE_LOADER = 47;
    private static final String QUERY_BUNDLE = "query_bundle";
    @BindView(R.id.photoMedicine)
    ImageView medicineImage;
    @BindView(R.id.infoForMedicine)
    TextView medicineTextInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent.hasExtra(MainActivity.ID_TO_PASS)) {
            tagFromMain = intent.getStringExtra(MainActivity.ID_TO_PASS);
            Log.e("Details", tagFromMain);
        }

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

        //start loader to fetch medicine
        Bundle queryBundle = new Bundle();
        queryBundle.putString(QUERY_BUNDLE, tagFromMain);

        android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> internetLoader = loaderManager.getLoader(ONE_MEDICINE_LOADER);
        if (internetLoader == null) {
            loaderManager.initLoader(ONE_MEDICINE_LOADER, queryBundle, mLoaderMedicine);
        } else {
            loaderManager.restartLoader(ONE_MEDICINE_LOADER, queryBundle, mLoaderMedicine);
        }
    }

    private LoaderManager.LoaderCallbacks mLoaderMedicine = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            String query = args.getString(QUERY_BUNDLE);
            Log.e("queryBundle",query);

            return new CursorLoader(DetailsActivity.this, OtcConract.MainRecycler.CONTENT_URI_MAIN.buildUpon().appendPath(query).build(), null, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            data.moveToFirst();
            String text = data.getString(data.getColumnIndex(OtcConract.MainRecycler.MAIN_INFORMATION));
            medicineTextInfo.setText(text);

            byte[] image = data.getBlob(data.getColumnIndex(OtcConract.MainRecycler.MAIN_IMAGE));
            Bitmap bitmap = getImage(image);
            medicineImage.setImageBitmap(bitmap);
        }

        @Override
        public void onLoaderReset(Loader loader) {

        }
    };

    // convert from byte array to bitmap
    private static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
