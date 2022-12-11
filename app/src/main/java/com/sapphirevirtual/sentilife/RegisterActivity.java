package com.sapphirevirtual.sentilife;

import static android.widget.Toast.LENGTH_SHORT;

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
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements Validator.ValidationListener{

    private Validator validator;

    @NotEmpty
    private EditText etFirstname, etLastname,  etPhone ;

    @NotEmpty @Email
    EditText etEmail;

    @Password
    EditText etPassword;


    public static String SESSION_ID, userId;

//    @Checked
//    private CheckBox checkBox;


    String firstName, lastName, email, password, phone, confirmPassword;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        //initialization of elements
        etFirstname = findViewById(R.id.etFirstname);
        etLastname = findViewById(R.id.etLastname);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);


        etPassword = findViewById(R.id.etPassword);




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
        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setMessage("Creating User..."); // Setting Message
//        progressDialog.setTitle("Processing"); // Setting Title
        //progressDialog.setIcon(R.drawable.app_logo);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);



    }

    public void go(){


        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void gotoLogin(View v){

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

    }

    private void createUser(){

        // set variable to values

        firstName = etFirstname.getText().toString();
        lastName = etLastname.getText().toString();
        email = etEmail.getText().toString();
        phone = etPhone.getText().toString();

        password = etPassword.getText().toString();

        //post to backend.
        progProc();
        String URL = "https://apis.sentinelock.com/sentinelife/v1/dev/post/signup/index.php";
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
                                Toast.makeText(RegisterActivity.this, jsonObject.getString("message"), LENGTH_SHORT).show();
                                go();


                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, jsonObject.getString("message"), LENGTH_SHORT).show();
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
                        Toast.makeText(RegisterActivity.this, "Seems your network is bad. Kindly restart app if this persist"+error, LENGTH_SHORT).show();
                    }
                }

        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("firstName", firstName);
                params.put("lastName", lastName);
                params.put("emailAddress", email);
                params.put("phoneNumber", phone);
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
        RequestQueue requestQueue = Volley.newRequestQueue(RegisterActivity.this);
        requestQueue.add(stringRequest);




    }

}