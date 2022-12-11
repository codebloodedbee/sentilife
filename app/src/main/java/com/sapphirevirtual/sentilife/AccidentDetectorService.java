package com.sapphirevirtual.sentilife;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AccidentDetectorService extends IntentService {

   ServiceHandler mServiceHandler;
    public AccidentDetectorService() {
        super("AccidentDetectorService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Context context = getApplicationContext();

        mServiceHandler = new ServiceHandler(this);

        mServiceHandler.doBindService();

        Toast.makeText(context, "hello", Toast.LENGTH_LONG).show();
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

//        Log.i("jjkl");



    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        mServiceHandler = new ServiceHandler(this);

        mServiceHandler.doBindService();

        Context context = getApplicationContext();
        CharSequence text = "Hello toast!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        reportAcccidentBackground();

        System.out.println("hell23");

//        DashboardActivity da = new DashboardActivity();
//
//        da.toggleTracking();







    }

    public void reportAcccidentBackground(){


        Toast.makeText(AccidentDetectorService.this, "Accident Detected", Toast.LENGTH_LONG).show();



        String URL = "https://apis.sentinelock.com/v2/android/";
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, URL,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("status").equals("true")) {


                                PendingIntent intent3;

//                                showNotification("ALERT !!", "Congrats, Your Sentinel Activation has been completed");

//                                Toast.makeText(AccidentDetectorService.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                            } else if( jsonObject.getString("status").equals("false"))
                            {

//

                                Toast.makeText(AccidentDetectorService.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();


                        }

                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //what to do if it encounter error

//                        Toast.makeText(AccidentDetectorService.this, "Not Successfull", Toast.LENGTH_SHORT).show();


                    }
                }


        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("command", "register");




                return params;
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(AccidentDetectorService.this);
        requestQueue.add(stringRequest);








        //dcapacity = getTotalInternalMemorySize().toString();


    }

}