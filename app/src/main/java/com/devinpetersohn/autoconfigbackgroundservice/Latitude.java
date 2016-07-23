package com.devinpetersohn.autoconfigbackgroundservice;

public class Latitude extends GPS{

    private double latitude;

    public Latitude(int data_item_id){

        if(super.canGetLocation)
            if(super.location != null) {
                latitude = super.location.getLatitude();
                sendData(link+"[{\"data_item_id\":" + data_item_id + ",\"data\":\"" + String.valueOf(latitude) +"\"}]");
            }
        //else showSettingsAlert();
    }

}
