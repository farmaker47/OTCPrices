package com.george.otcprices;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.george.otcprices.data.OtcConract;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity {

    private String tagFromMain, nameToSet;
    private static final int ONE_MEDICINE_LOADER = 47;
    private static final String QUERY_BUNDLE = "query_bundle";
    private ActionBar actionBar;
    private NestedScrollView mScrollView;
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
        if (intent.hasExtra(MainActivity.NAME_TO_PASS)) {
            nameToSet = intent.getStringExtra(MainActivity.NAME_TO_PASS);
        }
        mScrollView = findViewById(R.id.nestedScrollText);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
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

        setTitle(nameToSet);

        //transition
        setupWindowAnimations();

    }

    private void setupWindowAnimations() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slide = new Slide(Gravity.RIGHT);
            slide.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.interpolator.linear_out_slow_in));
            slide.setDuration(300);
            getWindow().setEnterTransition(slide);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putIntArray("ARTICLE_SCROLL_POSITION",
                new int[]{mScrollView.getScrollX(), mScrollView.getScrollY()});
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        final int[] position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION");
        if (position != null)
            mScrollView.post(new Runnable() {
                public void run() {
                    mScrollView.scrollTo(position[0], position[1]);
                }
            });
    }

    private LoaderManager.LoaderCallbacks mLoaderMedicine = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            String query = args.getString(QUERY_BUNDLE);
            Log.e("queryBundle", query);

            return new CursorLoader(DetailsActivity.this, OtcConract.MainRecycler.CONTENT_URI_MAIN.buildUpon().appendPath(query).build(), null, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            data.moveToFirst();
            String text = data.getString(data.getColumnIndex(OtcConract.MainRecycler.MAIN_INFORMATION));
            medicineTextInfo.setText(text);

            String name = data.getString(data.getColumnIndex(OtcConract.MainRecycler.MAIN_NAME));

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
