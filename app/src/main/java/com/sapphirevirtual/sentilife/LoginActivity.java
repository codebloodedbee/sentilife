package com.sapphirevirtual.sentilife;

import static android.widget.Toast.LENGTH_SHORT;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
//import android.service.autofill.UserData;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements Validator.ValidationListener{

    ProgressDialog progressDialog;

 BaseApplication ba;

    private Validator validator;


    @NotEmpty
    @Email
    EditText etEmail;

    @Password
    EditText etPassword;



    String  email, id, password ;

    public static String USER_ID, FIRST_NAME, LAST_NAME;

    public static SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //initialization of elements
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);



        //validation of data.
        validator = new Validator(this);
        validator.setValidationListener(this);

        sp = getSharedPreferences("login",MODE_PRIVATE);

        if(sp.getBoolean("logged",false) ){


            login2();
        }

        getSupportActionBar().hide();



//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        getSupportActionBar().setDisplayShowCustomEnabled(true);
//        getSupportActionBar().setCustomView(R.layout.login_action_bar_layout);
//        View view =getSupportActionBar().getCustomView();


        ImageView imageView= (ImageView)findViewById(R.id.action_bar_back);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
//                Toast.makeText(getApplicationContext(),"backward Button is clicked",Toast.LENGTH_LONG).show();

            }
        });


    }






    public boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }


    @Override
    public void onValidationSucceeded() {
        //Toast.makeText(this, "Form validated!", Toast.LENGTH_SHORT).show();
//        login();
        login1();
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {

        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);
            // Display error messages
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void btnLogin(View v){

        validator.validate();


    }

    public void progProc(){
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Login..."); // Setting Message
//        progressDialog.setTitle("Processing"); // Setting Title
        //progressDialog.setIcon(R.drawable.app_logo);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);



    }
    public void gotoRegister(View v){

        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);

    }

//    public void forgotPassword(View v){
//
//        Intent intent = new Intent(this, ForgotPasswordActivity.class);
//        startActivity(intent);
//
//    }



    public void go(){

        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
    }


    public void goUpdateProfile(){

        Intent intent = new Intent(this, UpdateProfile.class);
        startActivity(intent);
    }





    private void login2(){

        etEmail.setText(sp.getString("Semail",""));
        etPassword.setText(sp.getString("Spassword",""));
        password = etPassword.getText().toString();

        //post to backend.
//        progressON();
        progProc();
        String URL = "https://apis.sentinelock.com/sentinelife/v1/dev/post/login/index.php";
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("status").equals("true")) {

                                Toast.makeText(LoginActivity.this, jsonObject.getString("message"), LENGTH_SHORT).show();

                                sp.edit().putBoolean("logged", true).apply();


                                JSONObject datao = jsonObject.getJSONObject("data");
                                JSONObject emergencies = jsonObject.getJSONObject("data").getJSONObject("emergencies");
                                try {
                                    USER_ID = datao.getString("userId");

                                    FIRST_NAME = datao.getString("firstName");
                                    LAST_NAME = datao.getString("lastName");
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
                                    userData.storeData(USER_ID, FIRST_NAME, LAST_NAME, email, contactAddress, pictureFile, phoneNumber, country, state );

//                                    progressOFF();
                                    progressDialog.dismiss();

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
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, jsonObject.getString("message"), LENGTH_SHORT).show();
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
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Seems your network is bad. Kindly restart app if this persist"+error, LENGTH_SHORT).show();
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
        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
        requestQueue.add(stringRequest);




    }



    private void login1(){

        email = etEmail.getText().toString();
        password = etPassword.getText().toString();

        //post to backend.
//        progressON();
        progProc();
        String URL = "https://apis.sentinelock.com/sentinelife/v1/dev/post/login/index.php";
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("status").equals("true")) {

                                Toast.makeText(LoginActivity.this, jsonObject.getString("message"), LENGTH_SHORT).show();

                                sp.edit().putString("Semail", etEmail.getText().toString().trim()).apply();
                                sp.edit().putString("Spassword", etPassword.getText().toString().trim()).apply();



                                sp.edit().putBoolean("logged", true).apply();




                                JSONObject datao = jsonObject.getJSONObject("data");
                                JSONObject emergencies = jsonObject.getJSONObject("data").getJSONObject("emergencies");
                                try {
                                    USER_ID = datao.getString("userId");

                                    FIRST_NAME = datao.getString("firstName");
                                    LAST_NAME = datao.getString("lastName");
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
                                    userData.storeData(USER_ID, FIRST_NAME, LAST_NAME, email, contactAddress, pictureFile, phoneNumber, country, state );


                                    if(emergencyNumber1.equals("")){

                                        goUpdateProfile();

                                    }else {

                                        go();
                                    }



                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }




                            } else {
//                                progressOFF();
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, jsonObject.getString("message"), LENGTH_SHORT).show();
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
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Seems your network is bad. Kindly restart app if this persist"+error, LENGTH_SHORT).show();
                    }
                }

        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("emailAddress", email);
                params.put("password", password);



                return params;
            }

            @Override
            public Map<String, String> getHeaders() {

                HashMap<String, String> headers = new HashMap<>();
                headers.put("requestApiKey", "zyJPW4avI3G1REZp26iFtB75HrnQ");

                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
        requestQueue.add(stringRequest);




    }

    public static void showProgress(Context context) {
        Dialog progressDialog = new Dialog(context, R.style.AppTheme);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.custom_progress_layout);
        WindowManager.LayoutParams wmlp = progressDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER | Gravity.CENTER;
        wmlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wmlp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void progressON() {
//        BaseApplication.getInstance().progressON(Login.this, "rtjhghjnull");
        ba = new BaseApplication();
        ba.progressON(this, null);
    }

    public void progressON(String message) {
//        BaseApplication.getInstance().progressON(this, message);
        ba = new BaseApplication();
        ba.progressON(this, message);
    }

    public void progressOFF() {
//        ba.progressOFF();
    }



}