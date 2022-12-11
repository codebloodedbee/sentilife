package com.sapphirevirtual.sentilife;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;



import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;
public class Payment extends AppCompatActivity {

    public static String PAYMENTREFERENCE;

    private Card card;
    ProgressDialog progressDialog;
    private Charge charge;

    private EditText customerNameField;
    private EditText cardNumberField;
    private EditText expiryMonthField;
    private EditText expiryYearField;
    private EditText cvvField;

    private String customerName, cardNumber, cvv;
    private int expiryMonth, expiryYear;
    Spinner sMonth, sYear;
    AlertDialog alertDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PaystackSdk.initialize(getApplicationContext());
        setContentView(R.layout.activity_payment);

        Button payBtn = (Button) findViewById(R.id.pay_button);

        customerNameField = (EditText) findViewById(R.id.edit_customer_name);
        cardNumberField = (EditText) findViewById(R.id.edit_card_number);
        //expiryMonthField = (EditText) findViewById(R.id.edit_expiry_month);
        // expiryYearField = (EditText) findViewById(R.id.edit_expiry_year);
        cvvField = (EditText) findViewById(R.id.edit_cvv);

        sMonth = findViewById(R.id.expire_month);
        sYear = findViewById(R.id.expire_year);

//        cardNumberField.addTextChangedListener(new FourDigitCardFormatWatcher(cardNumberField));









//        String cardNumber = "4084084084084081";
//
//        int expiryMonth = 11; //any month in the future
//
//        int expiryYear = 2018; // any year in the future
//
//        String cvv = "408";

//        Card card = new Card(cardNumber, expiryMonth, expiryYear, cvv);
//
//        card.isValid();
    }

    public void progProc(){
        progressDialog = new ProgressDialog(Payment.this);
        progressDialog.setMessage("Loading..."); // Setting Message
        progressDialog.setTitle("Processing"); // Setting Title
        progressDialog.setIcon(R.drawable.sttudiom_icon);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);
    }

    public void pay(View v){

        expiryMonth = sMonth.getSelectedItemPosition()+1;
        expiryYear = Integer.parseInt(sYear.getSelectedItem().toString().trim());

        if (!validateForm()) {
            return;
        }
        try {
            customerName = customerNameField.getText().toString().trim();
            cardNumber = cardNumberField.getText().toString().trim().replace(" ","");
            //expiryMonth = sMonth.getSelectedItemPosition();


//            expiryMonth = Integer.parseInt(expiryMonthField.getText().toString().trim());
//            expiryYear = Integer.parseInt(sYear.getSelectedItem().toString().trim());


            cvv = cvvField.getText().toString().trim();
            progProc();

            //String cardNumber = "4084084084084081";
            //int expiryMonth = 11; //any month in the future
            //int expiryYear = 18; // any year in the future
            //String cvv = "408";
            Log.i("payyy", "cvv:"+cvv + "card_number"+cardNumber+"month"+expiryMonth+"year"+expiryYear);
            card = new Card(cardNumber, expiryMonth, expiryYear, cvv);
//            card = new Card()

            if (card.isValid()) {
                Toast.makeText(Payment.this, "Card is Valid", Toast.LENGTH_LONG).show();
                // compare card name with BVN.
                //if (compareName(LOAN_FIRST_NAME, LOAN_LAST_NAME, customerName)){
                if (true){
                    performCharge();
                }
                else {
                    Toast.makeText(Payment.this, "BVN Details and card details doesn't tally", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }

            } else {
                Toast.makeText(Payment.this, "Incorrect Card Details - Card not Valid", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performCharge() {
        //create a Charge object
        charge = new Charge();

        //set the card to charge
        charge.setCard(card);


        //Toast.makeText(LoanInitialPayment.this, "hi !" , Toast.LENGTH_LONG).show();

        charge.setEmail("dhizkeel@gmail.com"); //dummy email address

        //Log.i("payyy", "cvv:"+cvv + "card_number"+cardNumber+"month"+expiryMonth+"year"+expiryYear+"email"+LOANEMAIL);
        //charge.setEmail("dhizkeel@gmail.com");

        charge.setAmount(500); //test amount

        PaystackSdk.chargeCard(Payment.this, charge, new Paystack.TransactionCallback() {
            @Override
            public void onSuccess(Transaction transaction) {
                progressDialog.dismiss();
                // This is called only after transaction is deemed successful.
                // Retrieve the transaction, and send its reference to your server
                // for verification.
                PAYMENTREFERENCE = transaction.getReference();
                //Toast.makeText(LoanInitialPayment.this, "Transaction Successful! payment reference: "
                // + PAYMENTREFERENCE, Toast.LENGTH_LONG).show();
                Log.i("pays", "paystack-success"+PAYMENTREFERENCE);
//                Intent intent = new Intent(Payment.this, U.class);
//                startActivity(intent);
            }

            @Override
            public void beforeValidate(Transaction transaction) {
                // This is called only before requesting OTP.
                // Save reference so you may send to server. If
                // error occurs with OTP, you should still verify on server.
                Log.i("pays", "paystack-transaction1--"+transaction);
            }

            @Override
            public void onError(Throwable error, Transaction transaction) {
                progressDialog.dismiss();

                Toast.makeText(Payment.this, "Transaction Failed!" , Toast.LENGTH_LONG).show();
                Log.i("pays", "paystack-error"+error);
                Log.i("pays", "paystack-transaction--"+transaction.getReference());
                //handle error here
            }
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = customerNameField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            customerNameField.setError("Required.");
            valid = false;
        } else {
            customerNameField.setError(null);
        }

        String cardNumber = cardNumberField.getText().toString();
        if (TextUtils.isEmpty(cardNumber)) {
            cardNumberField.setError("Required.");
            valid = false;
        } else {
            cardNumberField.setError(null);
        }


//        String expiryMonth = expiryMonthField.getText().toString();
//        if (TextUtils.isEmpty(expiryMonth)) {
//            expiryMonthField.setError("Required.");
//            valid = false;
//        } else {
//            expiryMonthField.setError(null);
//        }
//
//
//        String expiryYear = sYear.toString();
//        if (TextUtils.isEmpty(expiryYear)) {
//            expiryYearField.setError("Required.");
//            valid = false;
//        } else {
//            expiryYearField.setError(null);
//        }

        String cvv = cvvField.getText().toString();
        if (TextUtils.isEmpty(cvv)) {
            cvvField.setError("Required.");
            valid = false;
        } else {
            cvvField.setError(null);
        }

        return valid;
    }

    public boolean compareName(String name1, String name2, String fullName){

        boolean xy;


        String fullName1 = fullName.split(" ")[0];
        String fullName2 = fullName.split(" ")[1];

        if ((name1.equalsIgnoreCase(fullName1)|| name1.equalsIgnoreCase(fullName2)) &&
                (name2.equalsIgnoreCase(fullName1)|| name2.equalsIgnoreCase(fullName2))){
            xy = true;
        } else {xy = false;}

        return xy;

    }




}
