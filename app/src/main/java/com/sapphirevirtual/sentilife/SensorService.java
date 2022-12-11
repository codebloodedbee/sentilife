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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorService extends Service implements SensorEventListener, android.location.GpsStatus.Listener {

    // TAG to identify notification
    private static final int NOTIFICATION = 007;

    // IBinder object to allow Activity to connect
    private final IBinder mBinder = new LocalBinder();

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


    private GPSManager gpsManager = null;
    private double speed = 0.0;
    Boolean isGPSEnabled=false;

    double currentSpeed,kmphSpeed;



    //
    private static final int gpsMinTime = 500;
    private static final int gpsMinDistance = 0;
    private static LocationManager locationManager;
    private static LocationListener locationListener ;
    private static GPSCallback gpsCallback = null;
    Context mcontext;

    public SensorService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        public SensorService getService() {
            return SensorService.this;
        }
    }

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

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                startListening(getApplicationContext());

                Log.i("GPs", "GPSs  call back ");

                Toast.makeText(getApplicationContext(), "speeddddd:" +location.getSpeed(), LENGTH_SHORT ).show();


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


        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_02";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Channel human readable title", NotificationManager.IMPORTANCE_HIGH);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Hello").setContentText("Hey").build();

            startForeground(1, notification);
        }

//        getCurrentSpeed();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        mSensorManager.unregisterListener(this);                            // Unregister sensor when not in use

        mNotificationManager.cancel(NOTIFICATION);
        stopSelf();
    }

    public SensorService(Context context) {
        mcontext=context;
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                if (SensorService.gpsCallback != null) {
                    SensorService.gpsCallback.onGPSUpdate(location);
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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

//        Toast.makeText(getApplicationContext(), "Accident Detected2", Toast.LENGTH_LONG).show();

//        gpsHandler.getSpeed(locLong);

        accelerationX = (Math.round(sensorEvent.values[0]*1000)/1000.0);
        accelerationY = (Math.round(sensorEvent.values[1]*1000)/1000.0);
        accelerationZ = (Math.round(sensorEvent.values[2]*1000)/1000.0);

        /*** Detect Accident ***/
        if (accelerationX > threshold || accelerationY > threshold || accelerationZ > threshold) {



            Toast.makeText(getApplicationContext(), "Accident Detected, Your emergency contact will be contacted", Toast.LENGTH_LONG).show();

            sendData();

            String message = "Alert! It appears  that " + userData.getFirstname()
                    + " may have been in a car accident. " +  userData.getFirstname()
                    + " has chosen you as their emergency contact. " + userData.getFirstname()
                    + "'s current location is " + gpsHandler.getCurrentAddress();


//            sendSMS("09155560910", message);



//            sendSMSActivity.sendSMSMessage();

            addNotification();








//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startForegroundService(new Intent(getApplicationContext(), AccidentDetectorService.class));
//            } else {
//                startService(new Intent(getApplicationContext(), AccidentDetectorService.class));
//            }
//
//
//            // start the accident detector at the back ground
//            Intent serviceIntent = new Intent(this, AccidentDetectorService.class);
//            startService(serviceIntent);


        }
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void showNotification() {
        Log.d("SERVICE DEBUG", "Notification Shown");
        CharSequence text = "Started Data Collection";

        // PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        // PendingIntent deleteIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            Notification mNotification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.alarm)
                    .setTicker(text)
                    .setContentTitle("Hello there!")
                    .setContentText(text)
                    .setAutoCancel(false)
                    .build();

            mNotificationManager.notify(NOTIFICATION, mNotification);
        }
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

                                Toast.makeText(SensorService.this, jsonObject.getString("message"), LENGTH_SHORT).show();







                            } else {

                                Toast.makeText(SensorService.this, jsonObject.getString("message"), LENGTH_SHORT).show();
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

                        Toast.makeText(SensorService.this, "Seems your network is bad. Kindly restart app if this persist"+error, LENGTH_SHORT).show();
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
        RequestQueue requestQueue = Volley.newRequestQueue(SensorService.this);
        requestQueue.add(stringRequest);




    }



    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("debug", "location changed");
           locLat =  getLocationLongitude(location);
           locLong =  getLocationLatitude(location);



            /*
             * Code for calculation Speed

            Point mPoint = new Point(location.getLongitude(), location.getLatitude(), System.currentTimeMillis());
            mPoints.add(mPoint);
            calcSpeed();

             */
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) { }

        @Override
        public void onProviderEnabled(String s) { }

        @Override
        public void onProviderDisabled(String s) { }
    };

    public String getLocationLongitude(Location location) {



        return String.valueOf(location.getLongitude());
    }

    public String getLocationLatitude(Location location) {
        return String.valueOf(location.getLatitude());
    }

    private void sendSMS(String phoneNumber, String message) {
        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();
        PendingIntent sentPI = PendingIntent.getBroadcast(mContext, 0, new Intent(mContext, SmsSentReceiver.class), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(mContext, 0, new Intent(mContext, SmsDeliveredReceiver.class), 0);
        try {
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> mSMSMessage = sms.divideMessage(message);
            for (int i = 0; i < mSMSMessage.size(); i++) {
                sentPendingIntents.add(i, sentPI);
                deliveredPendingIntents.add(i, deliveredPI);
            }
            sms.sendMultipartTextMessage(phoneNumber, null, mSMSMessage,
                    sentPendingIntents, deliveredPendingIntents);

        } catch (Exception e) {

            e.printStackTrace();
            Toast.makeText(getBaseContext(), "SMS sending failed...",Toast.LENGTH_SHORT).show();
        }

    }

    public class SmsDeliveredReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent arg1) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS delivered", Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(context, "SMS not delivered", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public class SmsSentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent arg1) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS Sent", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "SMS generic failure", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "SMS no service", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "SMS null PDU", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "SMS radio off", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }




    public static double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }



    //

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


    public void startListening(final Context context) {

        if (SensorService.locationManager == null) {
            SensorService.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        Log.i("GPs", "Line 2  ");

        final Criteria criteria = new Criteria();

        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(true);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        final String bestProvider = SensorService.locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
            Log.i("GPs", "Permision not granted  ");
        }
        if (bestProvider != null && bestProvider.length() > 0) {
            SensorService.locationManager.requestLocationUpdates(bestProvider, SensorService.gpsMinTime, SensorService.gpsMinDistance, SensorService.locationListener);
            Log.i("GPs", "Best provider ");
        }
        else {
            final List<String> providers = SensorService.locationManager.getProviders(true);
            for (final String provider : providers)
            {
                SensorService.locationManager.requestLocationUpdates(provider, SensorService.gpsMinTime, SensorService.gpsMinDistance, SensorService.locationListener);
                Log.i("GPs", " provider ");
            }
        }

        Log.i("GPs", "Line 3  ");
    }
    public void stopListening() {
        try
        {
            if (SensorService.locationManager != null && SensorService.locationListener != null) {
                SensorService.locationManager.removeUpdates(SensorService.locationListener);
            }
            SensorService.locationManager = null;
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
            ActivityCompat.requestPermissions((Activity) mcontext, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
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






}
