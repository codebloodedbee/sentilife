package com.sapphirevirtual.sentilife;

import static android.widget.Toast.LENGTH_SHORT;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorService2 extends Service implements SensorEventListener, GPSCallback {

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

    private int threshold = 3;
    UserData userData;

    // Notification Manager
    private NotificationManager mNotificationManager;


    private GPSManager gpsManager = null;
    private double speed = 0.0;
    Boolean isGPSEnabled=false;
    LocationManager locationManager;
    double currentSpeed,kmphSpeed;

    public SensorService2() {
    }

    public SensorService2(Context context) {

        mContext = context;
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
        public SensorService2 getService() {
            return SensorService2.this;
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

        getCurrentSpeed();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();


        Toast.makeText(getApplicationContext(), "Sensor destroyed", Toast.LENGTH_LONG).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            startForegroundService(new Intent(getApplicationContext(), GPSManager.class));

        }
        else
        {
            startService(new Intent(getApplicationContext(), GPSManager.class));
        }


    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


//        accelerationX = (Math.round(sensorEvent.values[0]*1000)/1000.0);
//        accelerationY = (Math.round(sensorEvent.values[1]*1000)/1000.0);
//        accelerationZ = (Math.round(sensorEvent.values[2]*1000)/1000.0);

        /*** Detect Accident ***/
//        if (accelerationX > threshold || accelerationY > threshold || accelerationZ > threshold) {



//            gpsHandler = new GPSHandler(getApplicationContext());

//        Toast.makeText(getApplicationContext(), "Accident Detected2", Toast.LENGTH_LONG).show();
            gpsHandler.getCurrentAddress();

            String xa = gpsHandler.getCurrentSpeed();

            if ( xa != null ) {

                if ( !xa.equals("0.0") ) {

//                    Toast.makeText(getApplicationContext(), " Speed:" + xa + "Latitude:" + gpsHandler.getCurrentLatitude(), Toast.LENGTH_LONG).show();

                    sendData();

                }
            }





//            Toast.makeText(getApplicationContext(), "Accident Detected, Your emergency contact will be contacted", Toast.LENGTH_LONG).show();



            String message = "Alert! It appears  that " + userData.getFirstname()
                    + " may have been in a car accident. " +  userData.getFirstname()
                    + " has chosen you as their emergency contact. " + userData.getFirstname()
                    + "'s current location is " + gpsHandler.getCurrentAddress();


//            sendSMS("09155560910", message);



//            sendSMSActivity.sendSMSMessage();

            addNotification();



//        }
    }

    private void addNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.sentilife_logo_icon)
                        .setContentTitle("Notifications Example")
                        .setContentText("This is a test notification");

        Intent notificationIntent = new Intent(this, SensorService2.class);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
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

                                Toast.makeText(SensorService2.this, jsonObject.getString("message"), LENGTH_SHORT).show();







                            } else {

                                Toast.makeText(SensorService2.this, jsonObject.getString("message"), LENGTH_SHORT).show();
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

                        Toast.makeText(SensorService2.this, "Seems your network is bad. Kindly restart app if this persist"+error, LENGTH_SHORT).show();
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
        RequestQueue requestQueue = Volley.newRequestQueue(SensorService2.this);
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

    public String getSpeed(Location location) {

        speed = location.getSpeed();
        int bearing  = (int)location.getBearing();
        int altitude = (int)location.getAltitude();
        currentSpeed = round(speed,3, BigDecimal.ROUND_HALF_UP);
        kmphSpeed = round((currentSpeed*3.6),3,BigDecimal.ROUND_HALF_UP);

        return String.valueOf(kmphSpeed);
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

    public void getCurrentSpeed(){

        Toast.makeText(this, "speed check ", Toast.LENGTH_SHORT).show();

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        gpsManager = new GPSManager(SensorService2.this);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(isGPSEnabled) {


            gpsManager.startListening(getApplicationContext());
            gpsManager.setGPSCallback(this);
        } else {
            gpsManager.showSettingsAlert();
        }


    }

//   updatte
    public void onGPSUpdate(Location location) {


        speed = location.getSpeed();
        int bearing  = (int)location.getBearing();
        int altitude = (int)location.getAltitude();
        currentSpeed = round(speed,3, BigDecimal.ROUND_HALF_UP);
        kmphSpeed = round((currentSpeed*3.6),3,BigDecimal.ROUND_HALF_UP);


//        double va1 = Math.random()*25;
//        int va = (int)va1;

        int ks = (int)kmphSpeed;

        Log.i("GPs", "servvice speeding: "+ks);

//            Toast.makeText(mContext, "servvice speeding: "+ks, Toast.LENGTH_SHORT).show();
//            Toast.makeText(this, "bearing: "+bearing, Toast.LENGTH_SHORT).show();

//        Speed.setSpeed(ks+1, 200, 50);





    }

    public static double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }




    public class GPSHandler {


        LocationManager locationManager;
        double currentSpeed,kmphSpeed;

        private static final long MIN_TIME = 0;
        private static final float MIN_DISTANCE = 0;
        private static final int MAX_RESULTS = 1;

        private Context mContext;
        private LocationManager mLocationManager;
        private Geocoder mGeocoder;
        private String currentAddress, latitude, longitude, speed;
        private List<String> hospitalAddresses;

        Location locc;

        private List<Point> mPoints;
        private List<String> items;

        public String getCurrentAddress() {
            return currentAddress;
        }

//    public String getS() {
//        return location;
//    }

        public String getCurrentLongitude() {
            return longitude;
        }

        public String getCurrentLatitude() {
            return latitude;
        }

        public String getCurrentSpeed() {
            return speed;
        }

        public List<String> getHospitalAddress() { return hospitalAddresses; }

        private LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {


                Log.d("debug", "location changed");
                findCurrentAddress(location);

                getSpeed(location);


                findHospitalAddress(location);


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) { }

            @Override
            public void onProviderEnabled(String s) { }

            @Override
            public void onProviderDisabled(String s) { }
        };

        public GPSHandler(Context context) {
            mContext = context;
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            final Criteria criteria = new Criteria();

            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setSpeedRequired(true);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_LOW);

            final String bestProvider = mLocationManager.getBestProvider(criteria, true);

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                Log.i("GPs", "Permision not granted  ");
            }
            if (bestProvider != null && bestProvider.length() > 0) {
                mLocationManager.requestLocationUpdates(bestProvider, MIN_TIME, MIN_DISTANCE, mLocationListener);
                Log.i("GPs", "Best provider ");
            }
            else {
                final List<String> providers = mLocationManager.getProviders(true);
                for (final String provider : providers)
                {
                    mLocationManager.requestLocationUpdates(provider, MIN_TIME, MIN_DISTANCE, mLocationListener);
                    Log.i("GPs", " provider ");
                }
            }


            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);

            mGeocoder = new Geocoder(mContext);    // Object to get address using coordinates

            mPoints = new ArrayList<>();
            hospitalAddresses = new ArrayList<>();
        }




        public String getSpeed(Location location) {


            double  speed = location.getSpeed();
            currentSpeed = round(speed,3, BigDecimal.ROUND_HALF_UP);
            kmphSpeed = round((currentSpeed*3.6),3,BigDecimal.ROUND_HALF_UP);



            return String.valueOf(kmphSpeed);
        }

        private void findCurrentAddress(Location location) {
            List<Address> addresses = null;                 // To hold the location and hospital addresses
            try {
                addresses = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), MAX_RESULTS);
            } catch (IOException e) {
            }

            currentAddress = "";
            latitude = String.valueOf(location.getLatitude());

            longitude = String.valueOf(location.getLongitude());

            double speed1 = location.getSpeed();

            currentSpeed = round(speed1,3, BigDecimal.ROUND_HALF_UP);
            kmphSpeed = round((currentSpeed*3.6),3,BigDecimal.ROUND_HALF_UP);

            speed = String.valueOf(kmphSpeed);


            if (addresses.size() > 0) {
                currentAddress = addresses.get(0).getAddressLine(0);
            }
        }


        public  double round(double unrounded, int precision, int roundingMode) {
            BigDecimal bd = new BigDecimal(unrounded);
            BigDecimal rounded = bd.setScale(precision, roundingMode);
            return rounded.doubleValue();
        }


        private void findHospitalAddress(Location location) {
            final String locationParm = location.getLatitude() + "," + location.getLongitude();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    InputStream inputStream = null;
                    HttpURLConnection urlConnection = null;
                    try {
                        // Connect to Google API Services
                        URL url = new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + locationParm + "&radius=5000&types=hospital&key=AIzaSyDoijibOb-tuDkGfJu6D_fMG10h8A5Epyk");
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.connect();

                        // Get the data from the API
                        inputStream = urlConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuffer stringBuffer = new StringBuffer();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) stringBuffer.append(line);

                        // Parse data and convert it to a JSON Object
                        JSONObject jsonObject = new JSONObject((stringBuffer.toString()));
                        JSONArray results = jsonObject.getJSONArray("results"); // Returns the hospitals found

                        hospitalAddresses.clear();
                        for (int ii = 0; ii < results.length(); ++ii) {
                            JSONObject jsonObjectEachResult = results.getJSONObject(ii);

                            String name = jsonObjectEachResult.optString("name");
                            String vicinity = jsonObjectEachResult.optString("vicinity");

                            hospitalAddresses.add(name + " at " + vicinity);
                        }
                    } catch (Exception ex) {}
                }
            }).start();
        }



    }

}
