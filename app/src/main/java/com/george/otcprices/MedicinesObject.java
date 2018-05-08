package com.george.otcprices;

/**
 * Created by farmaker1 on 01/05/2018.
 */

class MedicinesObject {
    String nameMedicine, priceMedicine,numberPosition,internetText;
    byte[] blobMedicine;

    public MedicinesObject(String name, String price, byte[] blob, String number,String internet) {
        nameMedicine = name;
        priceMedicine = price;
        blobMedicine = blob;
        numberPosition = number;
        internetText = internet;
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
    public String getInternetText(){
        return internetText;
    }
}
