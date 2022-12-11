package com.sapphirevirtual.sentilife;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BackgroundService extends Service {



    private static final int FIRST_RUN_TIMEOUT_MILISEC = 5 * 1000;
    private static final int SERVICE_STARTER_INTERVAL_MILISEC = 1 * 1000;
    private static final int SERVICE_TASK_TIMEOUT_SEC = 10;
    private final int REQUEST_CODE = 1;
    private AlarmManager serviceStarterAlarmManager = null;
    private MyTask asyncTask = null;

    private static final String OUR_SECURE_ADMIN_PASSWORD = "4530";
    //location
    Button btnShowLocation;
    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;

    private static final int MY_PERMISSIONS_REQUEST_PHONE = 1;
    double longitude, latitude;
    String _longitude, _latitude;

    // GPSTracker class
    GPSTracker gps;
    String _deviceImei, _deviceName, _storageSize, pn;


    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        Log.i("tag","boot completed for sentinel - service started");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");



    }



    @Override
    public void onCreate() {
        super.onCreate();
        // Start of timeout-autostarter for our service (watchdog)

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_02";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Channel human readable title", NotificationManager.IMPORTANCE_HIGH);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Sentilife Accident detector is active")
                    .setContentText("Sentilife Accident detector is active and accident will be reported")
                    .setSmallIcon(R.drawable.logo_icon).build();

            startForeground(1, notification);
        }


        Log.i("tag","boot completed for sentinel - service started");
        // startServiceStarter();

        // Start performing service task
        // serviceTask();
        //sta();
        //sentLoct();
        Toast.makeText(this, "Service Started!", Toast.LENGTH_LONG).show();
    }

    public void sentLoct(){

        _longitude = Double.toString(longitude);
        _latitude = Double.toString(latitude);




        String URL = "https://apis.sentinelock.com/v1/android/sentinelapi.php";
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,  URL ,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response){
//                  Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("status").equals("true")) {

                                try {
                                    pn = jsonObject.getString("message");

                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();


                        }
                        Toast.makeText(BackgroundService.this, pn, Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //what to do if it encounter error
                        //Toast.makeText(BackroundService.this, "failed", Toast.LENGTH_SHORT).show();
                    }
                }


        ){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("command", "send_location");

                params.put("device_name", _deviceName);
                params.put("device_imei", _deviceImei);
                params.put("address", null);
                params.put("latitude", _latitude);
                params.put("longitude", _longitude);




                return params;}

        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);


    }
    public void sta() {
        // create class object
        gps = new GPSTracker(BackgroundService.this);

        // check if GPS enabled
        if (gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();


            // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude +
            //  "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings

            latitude = 0;
            latitude = 0;
            gps.showSettingsAlert();
        }
    }

    private void StopPerformingServiceTask() {
        asyncTask.cancel(true);
    }

    private void GoToDesktop() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();


        Toast.makeText(getApplicationContext(), "Sensor destroyed", Toast.LENGTH_LONG).show();


//        mSensorManager.unregisterListener(this);                            // Unregister sensor when not in use

//        mNotificationManager.cancel(NOTIFICATION);
//        stopSelf();
    }



    private void serviceTask() {
        asyncTask = new MyTask();
        asyncTask.execute();
    }

    class MyTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (; ; ) {
                    TimeUnit.SECONDS.sleep(SERVICE_TASK_TIMEOUT_SEC);

                    // check if performing of the task is needed
                    if (isCancelled()) {
                        break;
                    }

                    // Initiating of onProgressUpdate callback that has access to UI
                    publishProgress();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... progress) {
            super.onProgressUpdate(progress);
            //Toast.makeText(getApplicationContext(), "Ooops!!! Try to kill me :)", Toast.LENGTH_SHORT).show();
        }
    }

    // To register our service in the AlarmManager service
    // for performing periodical starting of our service by the system
    private void startServiceStarter() {
        Intent intent = new Intent(this, SensorService2.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, this.REQUEST_CODE, intent, 0);
        if (pendingIntent == null) {
            Toast.makeText(this, "Some problems with creating of PendingIntent", Toast.LENGTH_LONG).show();
        } else {
            if (serviceStarterAlarmManager == null) {
                serviceStarterAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                serviceStarterAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + FIRST_RUN_TIMEOUT_MILISEC,
                        SERVICE_STARTER_INTERVAL_MILISEC, pendingIntent);
            }
        }

    }


}