package com.george.otcprices;

import java.sql.Blob;

/**
 * Created by farmaker1 on 01/05/2018.
 */

class MedicinesObject {
    String nameMedicine, priceMedicine;
    byte[] blobMedicine;

    public MedicinesObject(String name, String price, byte[] blob) {
        nameMedicine = name;
        priceMedicine = price;
        blobMedicine = blob;
    }


    public String getName() {
        return nameMedicine;
    }

    public String getPrice() {
        return priceMedicine;
    }

    public byte[] getBlob(){
        return blobMedicine;
    }
}
