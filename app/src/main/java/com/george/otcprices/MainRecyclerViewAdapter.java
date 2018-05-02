package com.george.otcprices;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by farmaker1 on 01/05/2018.
 */

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.MainViewHolder> {

    private Context mContext;
    private ArrayList<MedicinesObject> medicinesArray;

    public MainRecyclerViewAdapter(Context context,ArrayList<MedicinesObject> list) {
        mContext = context;
        medicinesArray = list;
    }


    @Override
    public MainRecyclerViewAdapter.MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_main_recycler, parent, false);

        MainViewHolder vh = new MainViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MainRecyclerViewAdapter.MainViewHolder holder, int position) {

        MedicinesObject medicinesObject = medicinesArray.get(position);

        holder.textTitle.setText(medicinesObject.getName());
        holder.textPrice.setText(medicinesObject.getPrice());


        //Get image from database
        byte[] image = medicinesObject.getBlob();
        Bitmap bitmap = getImage(image);
        holder.imageMain.setImageBitmap(bitmap);

    }

    @Override
    public int getItemCount() {
        return medicinesArray.size();
    }

    public class MainViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imageMainRecycler)
        ImageView imageMain;
        @BindView(R.id.textMainTitle)
        TextView textTitle;
        @BindView(R.id.textMainPrice)
        TextView textPrice;

        public MainViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

        }
    }

    public void setMedicineData(ArrayList<MedicinesObject> list) {
        medicinesArray = list;
        notifyDataSetChanged();
    }

    // convert from byte array to bitmap
    private static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
