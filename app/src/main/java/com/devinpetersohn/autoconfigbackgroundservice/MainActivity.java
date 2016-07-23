package com.devinpetersohn.autoconfigbackgroundservice;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;


public class MainActivity extends Activity {

    private EditText username=null;
    private EditText  password=null;
    private TextView attempts;
    private Button login;
    int counter = 3;
    JSONObject json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        username = (EditText)findViewById(R.id.editText1);
        password = (EditText)findViewById(R.id.editText2);
        attempts = (TextView)findViewById(R.id.textView5);
        attempts.setText(Integer.toString(counter));
        login = (Button)findViewById(R.id.button1);



        //super.onCreate(savedInstanceState);


    }

    public void login(View view) {

        Login backgroundLogin = new Login();

        backgroundLogin.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent event) {

                if (AutoDeployServices.person_id != 0) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Your UI code here
                            Toast.makeText(getApplicationContext(), "Thank you for logging in.  Deploying Services...",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                    deploy();
                    moveTaskToBack(true);

                } else {

                    Handler handler = new Handler(Looper.getMainLooper());

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            //Your UI code here
                            Toast.makeText(getApplicationContext(), "Sorry, the login failed",
                                    Toast.LENGTH_SHORT).show();
                            attempts.setBackgroundColor(Color.RED);
                            counter--;
                            attempts.setText(Integer.toString(counter));
                            if (counter == 0) {
                                login.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
        backgroundLogin.start();


    }

    public void deploy() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (AutoDeployServices.isRunning == false)
                    startService(new Intent(getApplicationContext(), AutoDeployServices.class));
            }
        });
        thread.start();

    }

    public void status(String message) {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG);
    }

    private class Login extends Thread {

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        public void doNotify(String message) {
            pcs.firePropertyChange("message","",message);
        }

        @Override
        public void run() {
            String address = null;
            address = "http://babbage.cs.missouri.edu/~cs4380sp15grp13/api.php?action=login";
            address = address + "&email=" + username.getText().toString();
            address = address + "&pword=" + password.getText().toString();


            final String finalAddress = address;
            //Thread thread = new Thread(new Runnable() {
            //  @Override
            //  public void run() {
            URL url = null;
            try {
                url = new URL(finalAddress);
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
                json = new JSONObject(sb.toString());
                AutoDeployServices.person_id = json.getInt("person_id");
            } catch (Exception e) {
                e.printStackTrace();
            }

            doNotify("Complete");
        }

        }

    }

