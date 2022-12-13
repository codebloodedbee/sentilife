package com.sapphirevirtual.sentilife;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Settings extends AppCompatActivity {

    UserData userData;
    TextView tvEmail;
    TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().hide();

        userData = new UserData(this);
        tvEmail = findViewById(R.id.tvEmail);
        tvName = findViewById(R.id.tvName);

        tvName.setText(""+ userData.getFirstname()+ " "+ userData.getLastname());

        tvEmail.setText(""+ userData.getEmail());

        // Initialize and assign variable
        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigation);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.settings);

        // Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch(item.getItemId())
                {
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), com.sapphirevirtual.sentilife.DashboardActivity.class));
                        overridePendingTransition(0,0);
                        return true;


                    case R.id.notification:
                        startActivity(new Intent(getApplicationContext(), com.sapphirevirtual.sentilife.Notification.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.settings:

                        return true;
                }
                return false;
            }
        });

    }
}