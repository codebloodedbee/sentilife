package com.sapphirevirtual.sentilife;

import static android.widget.Toast.LENGTH_SHORT;

import static com.sapphirevirtual.sentilife.LoginActivity.FIRST_NAME;
import static com.sapphirevirtual.sentilife.LoginActivity.LAST_NAME;
import static com.sapphirevirtual.sentilife.LoginActivity.USER_ID;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateProfile extends AppCompatActivity implements Validator.ValidationListener{

    private Validator validator;

    @NotEmpty
    private EditText etAddress, etEcn1,  etEcp1 , etEcn2,  etEcp2 ;

    UserData userData;



    public static String SESSION_ID, userId;

//    @Checked
//    private CheckBox checkBox;


    String address, ecn1, ecp1, ecn2, ecp2;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        userData = new UserData(this);

        //initialization of elements
        etAddress = findViewById(R.id.etAddress);
        etEcn1 = findViewById(R.id.etEcn1);
        etEcp1 = findViewById(R.id.etEcp1);
        etEcn2 = findViewById(R.id.etEcn2);
        etEcp2 = findViewById(R.id.etEcp2);



        //validation of data.
        validator = new Validator(this);
        validator.setValidationListener(this);

        getSupportActionBar().hide();

//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        getSupportActionBar().setDisplayShowCustomEnabled(true);
//        getSupportActionBar().setCustomView(R.layout.custom_action_bar_layout);
//        View view =getSupportActionBar().getCustomView();


        ImageView imageButton= (ImageView) findViewById(R.id.action_bar_back);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
//                Toast.makeText(getApplicationContext(),"backward Button is clicked",Toast.LENGTH_LONG).show();

            }
        });





    }

    @Override
    public void onValidationSucceeded() {
        //Toast.makeText(this, "Form validated!", Toast.LENGTH_SHORT).show();
        createUser();
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

    public void progProc(){
        progressDialog = new ProgressDialog(UpdateProfile.this);
        progressDialog.setMessage("Updating User Info..."); // Setting Message
//        progressDialog.setTitle("Processing"); // Setting Title
        //progressDialog.setIcon(R.drawable.app_logo);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);



    }

    public void go(){


        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
    }

    public void gotoLogin(View v){

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

    }

    private void createUser(){

        // set variable to values

        address = etAddress.getText().toString();
        ecn1 = etEcn1.getText().toString();
        ecp1 = etEcp1.getText().toString();
        ecn2 = etEcn2.getText().toString();
        ecp2 = etEcp2.getText().toString();

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
                                Toast.makeText(UpdateProfile.this, jsonObject.getString("message"), LENGTH_SHORT).show();
                                go();


                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(UpdateProfile.this, jsonObject.getString("message"), LENGTH_SHORT).show();
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
                        Toast.makeText(UpdateProfile.this, "Seems your network is bad. Kindly restart app if this persist"+error, LENGTH_SHORT).show();
                    }
                }

        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("userId", USER_ID);
                params.put("firstName", FIRST_NAME);
                params.put("lastName", LAST_NAME);
                params.put("emailAddress", userData.getEmail());
                params.put("phoneNumber", userData.getPhone());

                //
                params.put("homePhoneNumber", "");
                params.put("contactAddress", address);
                params.put("emergencyName1", ecn1);
                params.put("emergencyNumber1", ecp1);
                params.put("emergencyName2", ecn2);
                params.put("emergencyNumber2", ecp2);




                return params;
            }

            @Override
            public Map<String, String> getHeaders() {

                HashMap<String, String> headers = new HashMap<>();
                headers.put("requestApiKey", "zyJPW4avI3G1REZp26iFtB75HrnQ");

                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(UpdateProfile.this);
        requestQueue.add(stringRequest);




    }

}