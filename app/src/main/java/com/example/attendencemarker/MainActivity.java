package com.example.attendencemarker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    TextView date,status;
    Button markatt,history,logout;

    private FirebaseAuth mAuth;
    Toolbar tb;
    private DatabaseReference attendanceRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        CheckPerm cp=new CheckPerm();
        cp.checkAndRequestPermissions(MainActivity.this);
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        if(uid.equals(getString(R.string.admin_uid)))
        {
            startActivity(new Intent(MainActivity.this, AdminActivity.class));
            finish();
        }
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, AuthActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        attendanceRef = FirebaseDatabase.getInstance().getReference("attendance");
        tb=findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setLogo(R.mipmap.logo_round);
        date=findViewById(R.id.date);
        status=findViewById(R.id.status);
        fetchStatus();
        markatt=findViewById(R.id.markattbut);
        markatt.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, markActivity.class));
        });
        history=findViewById(R.id.historybut);
        history.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
        });
        logout=findViewById(R.id.logoutbut);
        logout.setOnClickListener(view -> {
                    mAuth.signOut();
                    startActivity(new Intent(MainActivity.this, AuthActivity.class));
                    finish();
        });
        String currdt=new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        date.setText(currdt);
    }

    @Override
    protected void onResume() {
        fetchStatus();
        if(status.getText().toString().equals("User not logged in"))
        {
            startActivity(new Intent(MainActivity.this, AuthActivity.class));
            finish();
        }
        super.onResume();
    }

    private void fetchStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            status.setText("User not logged in");
            return;
        }

        String uid = user.getUid();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        DatabaseReference statusRef = attendanceRef.child(date).child(uid).child("Info").child("status");

        statusRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String statuss = task.getResult().getValue(String.class);
                if (statuss != null) {
                    status.setText("Status: Attendance Marked");
                } else {
                    status.setText("Attendance not marked today");
                }
            } else {
                status.setText("Error fetching status");
            }
        });
    }


}