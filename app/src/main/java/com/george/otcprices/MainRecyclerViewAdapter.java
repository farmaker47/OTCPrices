package com.george.otcprices;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.MainViewHolder> implements Filterable {

    private Context mContext;
    private ArrayList<MedicinesObject> medicinesArray;
    private ArrayList<MedicinesObject> medicinesArrayFiltered;

    public MainRecyclerViewAdapter(Context context, ArrayList<MedicinesObject> list) {
        mContext = context;
        medicinesArray = list;
        medicinesArrayFiltered = list;
    }


    @Override
    public MainRecyclerViewAdapter.MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_main_recycler, parent, false);

        MainViewHolder vh = new MainViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MainRecyclerViewAdapter.MainViewHolder holder, int position) {

        MedicinesObject medicinesObject = medicinesArrayFiltered.get(position);

        //to be used with swipe to delete method
        String idOfMedicine = medicinesObject.getNumberPosition();

        holder.textTitle.setText(medicinesObject.getName());
        holder.textPrice.setText(medicinesObject.getPrice());


        //Get image from database
        byte[] image = medicinesObject.getBlob();
        Bitmap bitmap = getImage(image);
        holder.imageMain.setImageBitmap(bitmap);
        //setting tag
        int id = Integer.parseInt(idOfMedicine);
        holder.itemView.setTag(id);

    }

    @Override
    public int getItemCount() {
        if (medicinesArrayFiltered != null) {
            Log.e("number", String.valueOf(medicinesArrayFiltered.size()));
            return medicinesArrayFiltered.size();
        } else {
            return 0;
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    medicinesArrayFiltered = medicinesArray;
                    Log.e("Empty", "String");
                } else {
                    ArrayList<MedicinesObject> filteredList = new ArrayList<>();
                    for (MedicinesObject row : medicinesArray) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getPrice().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    medicinesArrayFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = medicinesArrayFiltered;
                Log.e("outside", "String");
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                medicinesArrayFiltered = (ArrayList<MedicinesObject>) filterResults.values;
                Log.e("publishResults", "String");
                notifyDataSetChanged();
            }
        };
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

        medicinesArrayFiltered = new ArrayList<>();
        medicinesArrayFiltered = list;
        notifyDataSetChanged();

    }

    public void setMedicineDataAfterDownload() {
        medicinesArrayFiltered.clear();
    }

    // convert from byte array to bitmap
    private static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
