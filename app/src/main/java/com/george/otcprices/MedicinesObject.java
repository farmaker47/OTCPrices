package com.george.otcprices;

/**
 * Created by farmaker1 on 01/05/2018.
 */

class MedicinesObject {
    String nameMedicine, priceMedicine,numberPosition;
    byte[] blobMedicine;

    public MedicinesObject(String name, String price, byte[] blob, String number) {
        nameMedicine = name;
        priceMedicine = price;
        blobMedicine = blob;
        numberPosition = number;
    }


    public String getName() {
        return nameMedicine;
    }

    public String getPrice() {
        return priceMedicine;
    }

    public byte[] getBlob() {
        return blobMedicine;
    }

    public String getNumberPosition(){
        return numberPosition;
    }
}
