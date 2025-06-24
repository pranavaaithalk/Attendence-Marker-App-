package com.example.attendencemarker;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminActivity extends AppCompatActivity {

    Toolbar tb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tb = findViewById(R.id.toolbar5);
        setSupportActionBar(tb);
        tb.setLogo(R.mipmap.logo_round);

        FirebaseDatabase.getInstance().getReference("attendance")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            addCard("No attendance records found.", "");
                            return;
                        }

                        for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                            String date = dateSnapshot.getKey();

                            for (DataSnapshot userSnapshot : dateSnapshot.getChildren()) {
                                String uid = userSnapshot.getKey();
                                String location = userSnapshot.child("Info/location").getValue(String.class);
                                String status = userSnapshot.child("Info/status").getValue(String.class);
                                String faceUrl = userSnapshot.child("Img/Face_IMG").getValue(String.class);
                                String workUrl = userSnapshot.child("Img/Work_Location_IMG").getValue(String.class);

                                FirebaseDatabase.getInstance().getReference("users").child(uid).child("name")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot nameSnap) {
                                                String name = nameSnap.getValue(String.class);
                                                String displayName = (name != null && !name.isEmpty()) ? name : uid;

                                                StringBuilder entryBuilder = new StringBuilder();
                                                entryBuilder.append("User: ").append(displayName).append("\n")
                                                        .append("Status: ").append(status).append("\n")
                                                        .append("Location: ").append(location).append("\n")
                                                        .append("[Face Image]").append("||").append(faceUrl).append("\n")
                                                        .append("[Work Location Image]").append("||").append(workUrl).append("\n\n");

                                                addCard(date, entryBuilder.toString().trim());
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                // fallback
                                                addCard(date, "User: " + uid + " (name unavailable)");
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        addCard("Error", error.getMessage());
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuinf = getMenuInflater();
        menuinf.inflate(R.menu.adminmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout_opt) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(AdminActivity.this, AuthActivity.class));
            finish();
        }
        else if(id==R.id.addadminopt)
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("Create New Admin");
            builder.setMessage("Enter UID:");
            EditText text=new EditText(this);
            text.setHint("UID");
            builder.setView(text);
            builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String uid=text.getText().toString().trim();
                    FirebaseDatabase.getInstance().getReference("admins").child("UID").setValue(uid);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    builder.setCancelable(true);
                }
            });
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void addCard(String date, String details) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.attendance_card, null);

        TextView dateText = cardView.findViewById(R.id.dateText);
        LinearLayout detailLayout = cardView.findViewById(R.id.detailContainer);

        dateText.setText("Date: " + date);

        String[] lines = details.split("\n");
        for (String line : lines) {
            TextView textView = new TextView(this);
            textView.setTextSize(17);

            if (line.contains("||")) {
                String[] parts = line.split("\\|\\|");
                String label = parts[0];
                String url = parts[1];
                textView.setText(label);
                textView.setTextColor(0xFF1E88E5); // blue
                textView.setOnClickListener(v -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                });
            } else {
                textView.setText(line);
            }

            detailLayout.addView(textView);
        }

        LinearLayout container = findViewById(R.id.cardContainer);
        container.addView(cardView);
    }
}
