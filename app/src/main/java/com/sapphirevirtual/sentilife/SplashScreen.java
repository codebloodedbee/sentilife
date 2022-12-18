package com.sapphirevirtual.sentilife;

import static android.widget.Toast.LENGTH_SHORT;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

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

public class SplashScreen extends AppCompatActivity {

    Handler handler = new Handler();
    Runnable runnable;


    public static SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


                getSupportActionBar().hide();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        sp = getSharedPreferences("login",MODE_PRIVATE);

        if(sp.getBoolean("logged",false) ){


            login2();


        } else {

            timer(3000);

        }

    }



    private void login2(){

        String email = sp.getString("Semail","");
        String pass= sp.getString("Spassword","");



        String URL = "https://apis.sentinelock.com/sentinelife/v1/dev/post/login/index.php";
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("status").equals("true")) {

                                Toast.makeText(SplashScreen.this, jsonObject.getString("message"), LENGTH_SHORT).show();

                                sp.edit().putBoolean("logged", true).apply();


                                JSONObject datao = jsonObject.getJSONObject("data");
                                JSONObject emergencies = jsonObject.getJSONObject("data").getJSONObject("emergencies");
                                try {
                                    String userId = datao.getString("userId");

                                    String firstName = datao.getString("firstName");
                                    String lastName = datao.getString("lastName");
                                    String email = datao.getString("emailAddress");
                                    String phoneNumber = datao.getString("phoneNumber");
                                    String homePhoneNumber = datao.getString("homePhoneNumber");
                                    String contactAddress = datao.getString("contactAddress");
                                    String pictureFile = datao.getString("pictureFile");
                                    String state = datao.getString("state");
                                    String country = datao.getString("country");
                                    String emailVerifyStatus = datao.getString("emailVerifyStatus");
                                    String accountStatus = datao.getString("accountStatus");
                                    String emergencyName1 = emergencies.getString("emergencyName1");
                                    String emergencyNumber1 = emergencies.getString("emergencyNumber1");
                                    String emergencyName2 = emergencies.getString("emergencyName2");
                                    String emergencyNumber2 = emergencies.getString("emergencyNumber2");
                                    String emergencyName3 = emergencies.getString("emergencyName3");
                                    String emergencyNumber3 = emergencies.getString("emergencyNumber3");


                                    UserData userData = new UserData(getApplicationContext());
                                    userData.storeData(userId, firstName, lastName, email, contactAddress, pictureFile, phoneNumber, country, state );

//                                    progressOFF();
//                                    progressDialog.dismiss();

                                    if(emergencyNumber1.equals("")){

                                        goUpdateProfile();

                                    }else {

                                        go();
                                    }





                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }

//                                go();


                            } else {
//                                progressOFF();
//                                progressDialog.dismiss();
                                Toast.makeText(SplashScreen.this, jsonObject.getString("message"), LENGTH_SHORT).show();
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
//                        progressOFF();
//                        progressDialog.dismiss();
//                        Toast.makeText(LoginActivity.this, "Seems your network is bad. Kindly restart app if this persist"+error, LENGTH_SHORT).show();
                    }
                }

        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("emailAddress", sp.getString("Semail","") );
                params.put("password", sp.getString("Spassword",""));



                return params;
            }

            @Override
            public Map<String, String> getHeaders() {

                HashMap<String, String> headers = new HashMap<>();
                headers.put("requestApiKey", "zyJPW4avI3G1REZp26iFtB75HrnQ");

                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(SplashScreen.this);
        requestQueue.add(stringRequest);




    }


    private void timer(long timeInMilli) {
        runnable = new Runnable() {
            @Override
            public void run() {
                go();

            }

        };

        handler.postDelayed(runnable, timeInMilli);
    }

    public void goUpdateProfile(){

        Intent intent = new Intent(this, UpdateProfile.class);
        startActivity(intent);
    }





    public void go(){



        Intent intent = new Intent(SplashScreen.this, Launch.class);
        startActivity(intent);
    }
}