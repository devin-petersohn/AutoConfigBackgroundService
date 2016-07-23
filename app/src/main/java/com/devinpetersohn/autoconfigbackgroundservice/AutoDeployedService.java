package com.devinpetersohn.autoconfigbackgroundservice;

import android.app.Service;

import org.apache.http.HttpResponse;

/**
 * Created by DevinPetersohn on 4/12/15.
 */
interface AutoDeployedService {

    long getFrequency();
    void setFrequency();
    HttpResponse sendData(String link);
    void deploySubclasses ();
}
