package com.sapphirevirtual.sentilife;

import static android.widget.Toast.LENGTH_SHORT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity implements GPSCallback{
    private final int MY_PERMISSION_REQUEST_CODE = 1;
    private PermissionHandler mPermissionHandler;



    private GPSManager gpsManager = null;
    private double speed = 0.0;
    Boolean isGPSEnabled=false;
    LocationManager locationManager;
    double currentSpeed,kmphSpeed;
    TextView txtview;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private ServiceHandler mServiceHandler;
    private static boolean isTracking;
    private Button buttonToggleTracking;
    private DBEmergency mDatabase;

    TextView tvGreet, tvSpeed, tvElevation, tvBearing, tvDate;

    TextView tvStartDriving;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    ExpandableListAdapter listAdapter;
    ExpandableListView mDrawerexpList;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    private Typeface custom_font;

    SpeedometerView Speed;

    GPSHandler gpsHandler;
    UserData userData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        getSupportActionBar().hide();

        UserData  ud = new UserData(this);

        mPermissionHandler = new PermissionHandler(this);

        mServiceHandler = new ServiceHandler(this);
        isTracking = false;
        buttonToggleTracking = (Button) findViewById(R.id.buttonToggleTracking);

        tvStartDriving =  findViewById(R.id.tvStartDriving);

        CustomToastActivity.CustomToastActivity(this);

        userData = new UserData(this);

        gpsHandler = new GPSHandler(this);





        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);

        tvDate = (TextView)findViewById(R.id.tvDate);
        tvDate.setText(""+formattedDate);




        tvSpeed=(TextView)findViewById(R.id.tvSpeed);
        tvElevation=(TextView)findViewById(R.id.tvElevation);
        tvBearing=(TextView)findViewById(R.id.tvBearing);




        // set first name
        tvGreet = findViewById(R.id.tvGreet);
        tvGreet.setText("Hi "+ ud.getFirstname());

        Speed = (SpeedometerView)findViewById(R.id.speedometer);
        Speed.setLabelConverter(new SpeedometerView.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });

        // configure value range and ticks
        Speed.setMaxSpeed(200);
        Speed.setMajorTickStep(40);
        Speed.setMinorTicks(1);


        // Configure value range colors
        Speed.addColoredRange(0, 80, Color.GREEN);
        Speed.addColoredRange(80, 140, Color.YELLOW);
        Speed.addColoredRange(140, 200, Color.RED);


        //set a default speed
        Speed.setSpeed(2, 2000, 500);

        //get the current speed
        getCurrentSpeed();



        //start monitoring for accident. this start a background process that will run forever.

        // Initialize and assign variable
        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigation);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.home);

        // Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch(item.getItemId())
                {
                    case R.id.home:
                        return true;

                    case R.id.notification:
                        startActivity(new Intent(getApplicationContext(), com.sapphirevirtual.sentilife.Notification.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), com.sapphirevirtual.sentilife.Settings.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });





    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (isTracking) {
//            toggleTracking();
//        }

        getCurrentSpeed();
    }

    public void startTrackingAccident() {
        if (isTracking) {
            toggleTracking();
        } else {
            // Ask for Permissions
            // Add permissions to the permissionName List
            List<String> permissionName = new ArrayList<>();
            List<String> permissionTag = new ArrayList<>();
            permissionName.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionName.add(Manifest.permission.READ_PHONE_STATE);
            permissionName.add(Manifest.permission.SEND_SMS);
            permissionTag.add("Access Location");
            permissionTag.add("Read Phone State");
            permissionTag.add("Send SMS");

            if (!mPermissionHandler.requestPermissions(MY_PERMISSION_REQUEST_CODE, permissionName, permissionTag)
                    || !locationServicesStatusCheck() )
                return;

            toggleTracking();
        }
    }

    public void toggleTracking() {
        //Intent intent = new Intent(DashboardActivity.this, TrackingActivity.class);
        //startActivity(intent);
//        addNotification();
        createNotification();

        if (isTracking) {

//            tvStartDriving.setText("Start Tracking");
            isTracking = false;
        } else {
//            mServiceHandler.doBindService();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
//            startForegroundService(new Intent(getApplicationContext(), GPSManager2.class));
//            startForegroundService(new Intent(getApplicationContext(), SensorService2.class));
            startForegroundService(new Intent(getApplicationContext(), GPSTracker.class));
            isMyServiceRunning(GPSTracker.class);
//            startForegroundService(new Intent(getApplicationContext(), BackgroundService.class));
//            startService(new Intent(getApplicationContext(), SensorService.class));
        }
        else
        {
//            startService(new Intent(getApplicationContext(), GPSManager2.class));
//            startService(new Intent(getApplicationContext(), SensorService2.class));
            startService(new Intent(getApplicationContext(), GPSTracker.class));
            isMyServiceRunning(GPSTracker.class);
//            startService(new Intent(getApplicationContext(), BackgroundService.class));
        }

//            tvStartDriving.setText("Stop Tracking");
            isTracking = true;
        }
    }

    private String isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return "yes";
            }
        }
        return "no";
    }

        public void createNotification( ) {
            // Prepare intent which is triggered if the
            // notification is selected
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

            // Build notification
            // Actions are just fake
            Notification noti = new Notification.Builder(this)
                    .setContentTitle("New mail from " + "test@gmail.com")
                    .setContentText("Subject").setSmallIcon(R.drawable.sentilife_logo_icon)
                    .setContentIntent(pIntent)
                    .addAction(R.drawable.sttudiom_icon, "Call", pIntent)
                    .addAction(R.drawable.sttudiom_icon, "More", pIntent)
                    .addAction(R.drawable.sttudiom_icon, "And more", pIntent).build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // hide the notification after its selected
            noti.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(0, noti);

        }


        private void addNotification() {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.sttudiom_icon)
                            .setContentTitle("Notifications Example")
                            .setContentText("This is a test notification");

            Intent notificationIntent = new Intent(this, DashboardActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            // Add as notification
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());
        }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (mPermissionHandler.handleRequestResult(requestCode, permissions, grantResults)) {
                    toggleTracking();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean locationServicesStatusCheck() {
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return true;

        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
        builder.setTitle("Enable GPS")
                .setMessage("This function needs your GPS, do you want to enable it now?")
                .setIcon(android.R.drawable.ic_menu_mylocation)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();


        return false;
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
            //case R.id.search:
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
//        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
//        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void onBackPressed() {

        Log.d("Finish","Back pressed");
        finish();
    }

    public void emergContact(View v){

        Intent intent = new Intent(DashboardActivity.this, Payment.class);
        startActivity(intent);

    }

    public void capture(View v){

//        createNotification();

        Intent intent = new Intent(DashboardActivity.this, CaptureActivity.class);
        startActivity(intent);

    }

        public void record(View v){

            Intent intent = new Intent(DashboardActivity.this, RecordActivity.class);
            startActivity(intent);

        }

        public void pingEmergencyContact(View v){

            Toast.makeText(DashboardActivity.this, "Pinging Emergency Contact", LENGTH_SHORT).show();

            sendData("location");
        }

        public void upgrade(View v){

            Intent intent = new Intent(DashboardActivity.this, Payment.class);
            startActivity(intent);

        }

        public void sendSOS(View v){

            Toast.makeText(DashboardActivity.this, "SOS message is being sent", LENGTH_SHORT).show();


            sendData("sos");

        }

        public void accident(View v){

            Toast.makeText(DashboardActivity.this, "Accident message is being sent", LENGTH_SHORT).show();


            sendData("accident");

            String message = "Alert! It appears  that " + userData.getFirstname()
                    + " may have been in a car accident. " +  userData.getFirstname()
                    + " has chosen you as their emergency contact. " + userData.getFirstname()
                    + "'s current location is " + gpsHandler.getCurrentAddress();

            sendSMS("09155560910", message);

        }

        public void getCurrentSpeed(){
//            txtview.setText(getString(R.string.info));
            locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
            gpsManager = new GPSManager(DashboardActivity.this);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(isGPSEnabled) {

//                Toast.makeText(this, "test 1 ", Toast.LENGTH_SHORT).show();
                gpsManager.startListening(getApplicationContext());
                gpsManager.setGPSCallback(this);
            } else {
                gpsManager.showSettingsAlert();
            }



            startTrackingAccident();
        }

        @Override
        public void onGPSUpdate(Location location) {


            speed = location.getSpeed();
            int bearing  = (int)location.getBearing();
            int altitude = (int)location.getAltitude();
            currentSpeed = round(speed,3, BigDecimal.ROUND_HALF_UP);
            kmphSpeed = round((currentSpeed*3.6),3,BigDecimal.ROUND_HALF_UP);


            double va1 = Math.random()*25;
            int va = (int)va1;

            int ks = (int)kmphSpeed;

//            Toast.makeText(this, "altitude: "+altitude, Toast.LENGTH_SHORT).show();
//            Toast.makeText(this, "bearing: "+bearing, Toast.LENGTH_SHORT).show();

            Speed.setSpeed(ks+1, 200, 50);

            tvSpeed.setText(ks+"");
            tvElevation.setText(altitude+"");
            tvBearing.setText(bearing+"");



        }

        @Override
        protected void onDestroy() {
//            gpsManager.stopListening();
//            gpsManager.setGPSCallback(null);
//            gpsManager = null;
            super.onDestroy();
        }

        public static double round(double unrounded, int precision, int roundingMode) {
            BigDecimal bd = new BigDecimal(unrounded);
            BigDecimal rounded = bd.setScale(precision, roundingMode);
            return rounded.doubleValue();
        }


        private void sendData(String signal){




            String URL = "https://apis.sentinelock.com/sentinelife/v1/dev/post/emergency/index.php";
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("status").equals("true")) {

                                Toast.makeText(DashboardActivity.this, jsonObject.getString("message"), LENGTH_SHORT).show();







                            } else {

                                Toast.makeText(DashboardActivity.this, jsonObject.getString("message"), LENGTH_SHORT).show();
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

                        Toast.makeText(DashboardActivity.this, "Seems your network is bad. Kindly restart app if this persist"+error, LENGTH_SHORT).show();
                    }
                }

        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("userId", userData.getUserId() );
                params.put("emergencyType", signal);
                params.put("sosAlert", "1");
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
        RequestQueue requestQueue = Volley.newRequestQueue(DashboardActivity.this);
        requestQueue.add(stringRequest);




    }

    private void sendSMS(String phoneNumber, String message) {
        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();
        PendingIntent sentPI = PendingIntent.getBroadcast(DashboardActivity.this, 0, new Intent(DashboardActivity.this, SensorService2.SmsSentReceiver.class), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(DashboardActivity.this, 0, new Intent(DashboardActivity.this, SensorService2.SmsDeliveredReceiver.class), 0);
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

}