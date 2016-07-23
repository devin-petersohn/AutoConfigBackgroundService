package com.devinpetersohn.autoconfigbackgroundservice;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;


/**
 * Created by DevinPetersohn on 3/15/15.
 */


public class GPS extends Service implements LocationListener, AutoDeployedService {

    private final Context context = this;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    static boolean canGetLocation = false;

    JSONArray jsonArray;

    static boolean isRunning = false;

    static double frequency = 1000 * 10;

    String link = "http://babbage.cs.missouri.edu/~cs4380sp15grp13/api.php?action=store_data&person_id=" +
            AutoDeployServices.person_id + "&participant_data=";

    static Location location;

    private static long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    protected LocationManager locationManager;

    public GPS() {

    }

    public long getFrequency() {
        return (long) frequency;
    }

    @Override
    public void setFrequency() {

        String temp = AutoDeployServices.frequencies.get(AutoDeployServices.classes.indexOf("com.devinpetersohn.autoconfigbackgroundservice.GPS"));
        frequency = CollectionFrequency.getFrequency(temp).frequencyInMilli();
        //frequency = 1000 * 10;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isRunning) return 0;

        isRunning = true;
        setFrequency();
        final Handler h = new Handler();
        final long delay = getFrequency(); //milliseconds


        h.postDelayed(new Runnable() {
            public void run() {
                setLocation();
                deploySubclasses();
                h.postDelayed(this, delay);
            }
        }, delay);


        return START_STICKY;
    }


    private void setLocation() {
        try {

            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {

            } else {

                this.canGetLocation = true;

                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                }

                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }

                if (isGPSEnabled) {

                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                locationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    }

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        System.out.println(location);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopUsingGPS() {
        if (locationManager != null) locationManager.removeUpdates(GPS.this);
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle("GPS is settings");

        alertDialog.setMessage("GPS is not enabled.  Would you like to go to settings menu?");

        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location arg0) {

    }

    @Override
    public void onProviderDisabled(String arg0) {

    }

    @Override
    public void onProviderEnabled(String arg0) {

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void deploySubclasses() {
        for (int classname : AutoDeployServices.item_collection_ids.get("com.devinpetersohn.autoconfigbackgroundservice.GPS")) {
            final String aLink = "http://babbage.cs.missouri.edu/~cs4380sp15grp13/data_item_retrieve?" + classname;

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    URL url = null;
                    try {
                        url = new URL(aLink);
                        HttpClient client = new DefaultHttpClient();
                        HttpGet request = new HttpGet();
                        request.setURI(new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null));
                        HttpResponse response = client.execute(request);
                        BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                        StringBuffer sb = new StringBuffer("");
                        String line = "";
                        while ((line = in.readLine()) != null) {
                            sb.append(line);
                            break;
                        }
                        in.close();
                        if(sb.toString() == "") return;
                        jsonArray = new JSONArray(sb.toString());

                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                String temp = (jsonArray.getJSONObject(i).getString("class_name"));
                                int data_item_id = jsonArray.getJSONObject(i).getInt("data_item_id");
                                if (temp.contains("com.devinpetersohn.autoconfigbackgroundservice")) {
                                    Class c = null;
                                    try {
                                        c = Class.forName(temp).asSubclass(GPS.class);
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        c.getDeclaredConstructor(int.class).newInstance(data_item_id);
                                    } catch (InstantiationException e) {
                                        e.printStackTrace();
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();
        }
    }


    public HttpResponse sendData(String link) {
        final String aLink = link;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL(aLink);
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet();
                    request.setURI(new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null));
                    client.execute(request);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        return null;
    }
}

