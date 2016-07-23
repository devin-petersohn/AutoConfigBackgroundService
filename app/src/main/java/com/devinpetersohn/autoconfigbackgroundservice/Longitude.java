package com.devinpetersohn.autoconfigbackgroundservice;

public class Longitude extends GPS{

        private double longitude;

        public Longitude(int data_item_id) {
            if(super.canGetLocation)
                if(super.location != null) {
                    longitude = super.location.getLongitude();
                    sendData(link+"[{\"data_item_id\":" + data_item_id + ",\"data\":\"" + String.valueOf(longitude) +"\"}]");
                }
            //else showSettingsAlert();
        }

    }
