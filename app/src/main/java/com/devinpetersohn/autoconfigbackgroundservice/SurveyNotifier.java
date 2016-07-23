package com.devinpetersohn.autoconfigbackgroundservice;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by DevinPetersohn on 5/5/15.
 */
public class SurveyNotifier extends Service implements AutoDeployedService{

    static boolean isRunning = false;

    ArrayList<Thread> threads = new ArrayList<>();
    HashMap<Integer,String> surveyNames = new HashMap<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (isRunning) return 0;

        isRunning = true;
        //setFrequency();


        for (int i = 0; i < AutoDeployServices.surveys.size(); i++) {
            final int tempI = i;

            Thread t = new Thread(new Runnable() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void run() {
                    final int finalI = tempI;
                    while (true) {
                        getSurveyName(finalI);
                        long sleepTime = (long) CollectionFrequency.getFrequency(AutoDeployServices.surveyFrequencies.get(finalI)).frequencyInMilli();
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Intent deleteIntent = new Intent(getApplicationContext(), DismissNotification.class);
                        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(getApplicationContext(), 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        String subject = "Survey Time! Survey:";
                        String body = surveyNames.get(finalI);
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(getApplicationContext())
                                        .setSmallIcon(R.drawable.icon)
                                        .setContentTitle(subject)
                                        .setContentText(body)
                                        .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss Notification", pendingIntentCancel);
                        NotificationManager mNotificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(finalI, mBuilder.build());

                    }
                }
            });
            t.start();
            threads.add(t);
        }
        return START_STICKY;
    }

    private void getSurveyName(final int item_collection_id) {

        final String aLink = "http://babbage.cs.missouri.edu/~cs4380sp15grp13/getSurveyName.php?" + item_collection_id;

        Thread thread = new Thread(new Runnable(){
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
                    while((line = in.readLine()) != null){
                        sb.append(line);
                        break;
                    }
                    in.close();
                    JSONObject jsonObject = new JSONObject(sb.toString());
                    surveyNames.put(item_collection_id,jsonObject.getString("name"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public long getFrequency() {
        return 0;
    }

    @Override
    public void setFrequency() {

    }

    @Override
    public HttpResponse sendData(String link) {
        return null;
    }

    @Override
    public void deploySubclasses() {

    }
}
