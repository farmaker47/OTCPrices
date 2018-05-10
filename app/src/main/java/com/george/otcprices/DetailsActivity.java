package com.george.otcprices;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.george.otcprices.data.OtcConract;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailsActivity extends AppCompatActivity {

    private String tagFromMain, nameToSet, internetToSet;
    private static final int ONE_MEDICINE_LOADER = 47;
    private static final String QUERY_BUNDLE = "query_bundle";

    @BindView(R.id.photoMedicine)
    ImageView medicineImage;
    @BindView(R.id.infoForMedicine)
    TextView medicineTextInfo;
    @BindView(R.id.nestedScrollText)
    NestedScrollView mScrollView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.adViewDetails)
    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent.hasExtra(MainActivity.ID_TO_PASS)) {
            tagFromMain = intent.getStringExtra(MainActivity.ID_TO_PASS);
        }
        if (intent.hasExtra(MainActivity.NAME_TO_PASS)) {
            nameToSet = intent.getStringExtra(MainActivity.NAME_TO_PASS);
        }
        if (intent.hasExtra(MainActivity.INTERNET_TO_PASS)) {
            internetToSet = intent.getStringExtra(MainActivity.INTERNET_TO_PASS);
            Log.e("Details", internetToSet);
        }

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);

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

        //Ads by Admob
        MobileAds.initialize(this,
                "ca-app-pub-3940256099942544~3347511713");

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    private void setupWindowAnimations() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Gravity.END gives bad crash so I used right...sorry
            Slide slide = new Slide(Gravity.RIGHT);
            slide.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.interpolator.linear_out_slow_in));
            slide.setDuration(300);
            getWindow().setEnterTransition(slide);
        }
    }

    @OnClick(R.id.fab)
    public void clickFabToShare(View view) {
        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(DetailsActivity.this)
                .setType("text/plain")
                .setText(getString(R.string.read_info) + " " + internetToSet)
                .getIntent(), getString(R.string.action_share)));
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
            /*Glide.with(DetailsActivity.this).load(image).into(medicineImage);*/
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
