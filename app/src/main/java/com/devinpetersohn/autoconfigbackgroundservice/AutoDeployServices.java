package com.devinpetersohn.autoconfigbackgroundservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by DevinPetersohn on 4/7/15.
 */
public class AutoDeployServices extends Service {

    static boolean isRunning = false;
    static int person_id = 0;
    JSONArray jsonArray;
    static ArrayList<String> classes = new ArrayList<>();
    static ArrayList<String> frequencies = new ArrayList<>();
    static HashMap<String, ArrayList<Integer>> item_collection_ids = new HashMap<>();

    static ArrayList<String> surveys = new ArrayList<>();
    static ArrayList<String> surveyFrequencies = new ArrayList<>();
    static ArrayList<Integer> survey_ids = new ArrayList<>();

    public AutoDeployServices(){

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(isRunning) return 0;
        isRunning = true;

        ToBeDeployed tbd = new ToBeDeployed();

        tbd.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                for(int i=0; i<jsonArray.length();i++) {
                    try {
                        String temp = (jsonArray.getJSONObject(i).getString("class_name"));
                        if(temp.contains("com.devinpetersohn.autoconfigbackgroundservice")) {
                            classes.add(temp);
                            frequencies.add(jsonArray.getJSONObject(i).getString("frequency_class"));
                            if(item_collection_ids.containsKey(temp))
                                item_collection_ids.get(temp).add(jsonArray.getJSONObject(i).getInt("item_collection_id"));
                            else {
                                ArrayList<Integer> firstVal = new ArrayList<>();
                                firstVal.add(jsonArray.getJSONObject(i).getInt("item_collection_id"));
                                item_collection_ids.put(temp, firstVal);
                            }
                        } else {
                            surveys.add(temp);
                            surveyFrequencies.add(jsonArray.getJSONObject(i).getString("frequency_class"));
                            survey_ids.add(jsonArray.getJSONObject(i).getInt("item_collection_id"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if(!surveys.isEmpty())
                    startService(new Intent(getApplicationContext(), SurveyNotifier.class));
                if(!classes.isEmpty()) {
                    for (String c : classes) {
                        try {
                            startService(new Intent(getApplicationContext(), Class.forName(c)));
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        tbd.start();



        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class ToBeDeployed extends Thread {

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        @Override
        public void run() {

            final String aLink = "http://babbage.cs.missouri.edu/~cs4380sp15grp13/background_service_retrieve.php?" + person_id;

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
                jsonArray = new JSONArray(sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            pcs.firePropertyChange("message", "", "Process Complete");
        }
    }
}


