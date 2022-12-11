package com.sapphirevirtual.sentilife;

import static android.widget.Toast.LENGTH_SHORT;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
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
import java.util.List;
import java.util.Map;

public class GPSManager2 extends Service implements android.location.GpsStatus.Listener
{


    //

    // TAG to identify notification
    private static final int NOTIFICATION = 007;

    // IBinder object to allow Activity to connect

    public String locLong, locLat;
    Context mContext;

    // Sensor Objects
    private Sensor accelerometer;
    public SendSMSActivity sendSMSActivity;
    private SensorManager mSensorManager;
    GPSHandler gpsHandler;

    private double accelerationX, accelerationY, accelerationZ;

    private int threshold = 30;
    UserData userData;

    // Notification Manager
    private NotificationManager mNotificationManager;



    //
    private static final int gpsMinTime = 500;
    private static final int gpsMinDistance = 0;
    private static LocationManager locationManager = null;
    private static LocationListener locationListener = null;
    private static GPSCallback gpsCallback = null;
    Context mcontext;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();


//        startForeground();

        userData = new UserData(this);
        gpsHandler = new GPSHandler(this);
        sendSMSActivity = new SendSMSActivity();

        Toast.makeText(getApplicationContext(), "Drive safe", Toast.LENGTH_LONG).show();


        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Channel human readable title", NotificationManager.IMPORTANCE_HIGH);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Hello").setContentText("Hey").build();

            startForeground(1, notification);
        }

//        showNotification();
    }


    public GPSManager2(Context context) {
        mcontext=context;
        GPSManager2.locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                if (GPSManager2.gpsCallback != null) {
                    GPSManager2.gpsCallback.onGPSUpdate(location);
                }

                Log.i("GPs", "GPSs  call back ");
            }

            @Override
            public void onProviderDisabled(final String provider) {
                Log.i("GPs", "Provider disablled ");
            }

            @Override
            public void onProviderEnabled(final String provider) {

                Log.i("GPs", "Provvider enabled ");
            }

            @Override
            public void onStatusChanged(final String provider, final int status, final Bundle extras) {

                Log.i("GPs", "Status changed");
            }
        };
    }
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mcontext);
// Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
// Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
// On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mcontext.startActivity(intent);
            }
        });
// on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
// Showing Alert Message
        alertDialog.show();
    }
    public GPSCallback getGPSCallback()
    {
        return GPSManager2.gpsCallback;
    }

    public void setGPSCallback(final GPSCallback gpsCallback) {
        GPSManager2.gpsCallback = gpsCallback;

        Log.i("GPs", "GPS1  call back ");
    }

    public void startListening(final Context context) {

        if (GPSManager2.locationManager == null) {
            GPSManager2.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        Log.i("GPs", "Line 2  ");

        final Criteria criteria = new Criteria();

        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(true);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        final String bestProvider = GPSManager2.locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
            Log.i("GPs", "Permision not granted  ");
        }
        if (bestProvider != null && bestProvider.length() > 0) {
            GPSManager2.locationManager.requestLocationUpdates(bestProvider, GPSManager2.gpsMinTime, GPSManager2.gpsMinDistance, GPSManager2.locationListener);
            Log.i("GPs", "Best provider ");
        }
        else {
            final List<String> providers = GPSManager2.locationManager.getProviders(true);
            for (final String provider : providers)
            {
                GPSManager2.locationManager.requestLocationUpdates(provider, GPSManager2.gpsMinTime, GPSManager2.gpsMinDistance, GPSManager2.locationListener);
                Log.i("GPs", " provider ");
            }
        }

        Log.i("GPs", "Line 3  ");
    }
    public void stopListening() {
        try
        {
            if (GPSManager2.locationManager != null && GPSManager2.locationListener != null) {
                GPSManager2.locationManager.removeUpdates(GPSManager2.locationListener);
            }
            GPSManager2.locationManager = null;
        }
        catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onGpsStatusChanged(int event) {

        Log.i("GPs", "GPS status changed ");


        int Satellites = 0;
        int SatellitesInFix = 0;
        if (ActivityCompat.checkSelfPermission(mcontext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mcontext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mcontext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        int timetofix = locationManager.getGpsStatus(null).getTimeToFirstFix();
        Log.i("GPs", "Time to first fix = "+String.valueOf(timetofix));
        for (GpsSatellite sat : locationManager.getGpsStatus(null).getSatellites()) {
            if(sat.usedInFix()) {
                SatellitesInFix++;
            }
            Satellites++;
        }
        Log.i("GPS", String.valueOf(Satellites) + " Used In Last Fix ("+SatellitesInFix+")");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendData(){


        String URL = "https://apis.sentinelock.com/sentinelife/v1/dev/post/emergency/index.php";
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("status").equals("true")) {

                                Toast.makeText(GPSManager2.this, jsonObject.getString("message"), LENGTH_SHORT).show();







                            } else {

                                Toast.makeText(GPSManager2.this, jsonObject.getString("message"), LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //what to do if it encounter error

                        Toast.makeText(GPSManager2.this, "Seems your network is bad. Kindly restart app if this persist"+error, LENGTH_SHORT).show();
                    }
                }

        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("userId", userData.getUserId() );
                params.put("emergencyType", "accident");
                params.put("accidentAlert", "1");
                params.put("locationLongitude", gpsHandler.getCurrentLongitude());
                params.put("locationLatitude", gpsHandler.getCurrentLatitude());
                params.put("hospitalName", "abc");
                params.put("hospitalAddress", "abc");
                params.put("hospitalLongitude", "abc");
                params.put("hospitalLatitude", "abc");



                return params;
            }

            @Override
            public Map<String, String> getHeaders() {

                HashMap<String, String> headers = new HashMap<>();
                headers.put("requestApiKey", "zyJPW4avI3G1REZp26iFtB75HrnQ");

                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(GPSManager2.this);
        requestQueue.add(stringRequest);




    }

    private void addNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.sentilife_logo_icon)
                        .setContentTitle("Notifications Example")
                        .setContentText("This is a test notification");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

}