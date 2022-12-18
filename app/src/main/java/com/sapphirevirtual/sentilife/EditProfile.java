package com.sapphirevirtual.sentilife;

import static android.widget.Toast.LENGTH_SHORT;

import static com.sapphirevirtual.sentilife.LoginActivity.FIRST_NAME;
import static com.sapphirevirtual.sentilife.LoginActivity.LAST_NAME;
import static com.sapphirevirtual.sentilife.LoginActivity.USER_ID;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfile extends AppCompatActivity  implements Validator.ValidationListener {


    private Validator validator;

    public static SharedPreferences sp;
    ProgressDialog progressDialog;


    @NotEmpty
    EditText etFn, etLn, etPhone, etAddress, etNk1, etNkPn1, etNk2, etNkPn2,etNk3, etNkPn3;

    UserData userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().hide();

        sp = getSharedPreferences("login",MODE_PRIVATE);

        etFn = findViewById(R.id.etFn);
        etLn = findViewById(R.id.etLn);
        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);
        etNk1 = findViewById(R.id.etNk1);
        etNkPn1 = findViewById(R.id.etNkPn1);
        etNk2 = findViewById(R.id.etNk2);
        etNkPn2 = findViewById(R.id.etNkPn2);
        etNk3 = findViewById(R.id.etNk3);
        etNkPn3 = findViewById(R.id.etNkPn3);




        userData = new UserData(this);

        fetchData();

    }

    @Override
    public void onValidationSucceeded() {
        //Toast.makeText(this, "Form validated!", Toast.LENGTH_SHORT).show();
        updateData();
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

    public void btnSubmit(View v){


        validator.validate();


    }




    private void fetchData(){

        progProc();


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

                                Toast.makeText(EditProfile.this, jsonObject.getString("message"), LENGTH_SHORT).show();

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
                                    String pictureFile = datao.getString("pictureFileName");
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

                                    etFn.setText(firstName);
                                    etLn.setText(lastName);
                                    etAddress.setText(contactAddress);
                                    etPhone.setText(phoneNumber);
                                    etNk1.setText(emergencyName1);
                                    etNkPn1.setText(emergencyNumber1);
                                    etNk2.setText(emergencyName2);
                                    etNkPn2.setText(emergencyNumber2);
                                    etNk3.setText(emergencyName3);
                                    etNkPn3.setText(emergencyNumber3);




                                    UserData userData = new UserData(getApplicationContext());
                                    userData.storeData(userId, firstName, lastName, email, contactAddress, pictureFile, phoneNumber, country, state );

//                                    progressOFF();
                                    progressDialog.dismiss();


                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }

//                                go();


                            } else {
//                                progressOFF();
                                progressDialog.dismiss();

                                Toast.makeText(EditProfile.this, jsonObject.getString("message"), LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        go();
                        //what to do if it encounter error
//                        progressOFF();
                        progressDialog.dismiss();
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
        RequestQueue requestQueue = Volley.newRequestQueue(EditProfile.this);
        requestQueue.add(stringRequest);




    }

    public void progProc(){
        progressDialog = new ProgressDialog(EditProfile.this);
        progressDialog.setMessage("Login..."); // Setting Message
//        progressDialog.setTitle("Processing"); // Setting Title
        //progressDialog.setIcon(R.drawable.app_logo);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);



    }


    private void updateData(){

        // set variable to values

        String fn = etFn.getText().toString();
        String ln = etLn.getText().toString();
        String phone = etPhone.getText().toString();
        String address = etAddress.getText().toString();
        String nk1 = etNk1.getText().toString();
        String nkPn1 = etNkPn1.getText().toString();
        String nk2 = etNk2.getText().toString();
        String nkPn2 = etNkPn2.getText().toString();
        String nk3 = etNk3.getText().toString();
        String nkPn3 = etNkPn3.getText().toString();


        //post to backend.
        progProc();
        String URL = "https://apis.sentinelock.com/sentinelife/v1/dev/post/profile/profile_update.php";
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("status").equals("true")) {
                                progressDialog.dismiss();
                                jsonObject.getString("message");
                                Toast.makeText(EditProfile.this, jsonObject.getString("message"), LENGTH_SHORT).show();
//                                go();


                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(EditProfile.this, jsonObject.getString("message"), LENGTH_SHORT).show();
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
                        progressDialog.dismiss();
                        Toast.makeText(EditProfile.this, "Seems your network is bad. Kindly restart app if this persist"+error, LENGTH_SHORT).show();
                    }
                }

        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("userId", userData.getUserId());
                params.put("firstName", fn);
                params.put("lastName", ln);
//                params.put("emailAddress",email);
                params.put("phoneNumber", phone);

                params.put("contactAddress", address);
                params.put("emergencyName1", nk1);
                params.put("emergencyNumber1", nkPn1);
                params.put("emergencyName2", nk2);
                params.put("emergencyNumber2", nkPn2);
                params.put("emergencyName3", nk3);
                params.put("emergencyNumber3", nkPn3);




                return params;
            }

            @Override
            public Map<String, String> getHeaders() {

                HashMap<String, String> headers = new HashMap<>();
                headers.put("requestApiKey", "zyJPW4avI3G1REZp26iFtB75HrnQ");

                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(EditProfile.this);
        requestQueue.add(stringRequest);




    }

}