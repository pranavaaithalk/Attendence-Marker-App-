package com.example.attendencemarker;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class markActivity extends AppCompatActivity {

    FirebaseStorage storage;
    StorageReference fileRef;
    private static final int REQUEST_FACE_IMAGE_CAPTURE = 77, REQUEST_BG_IMAGE_CAPTURE=22;
    private Uri imageUri;
    Button facecapturebutton,bgcapturebutton,markitbutton;
    private FusedLocationProviderClient fusedLocationClient;
    String locationString,faceurl=null,bgurl=null;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private DatabaseReference attendanceRef;
    private FirebaseAuth mAuth;

    private ImageView face,bg;
    private ProgressBar fb,bgb;
    private boolean isFaceUploaded = false;
    private boolean isBgUploaded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setContentView(R.layout.activity_mark);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth=FirebaseAuth.getInstance();
        attendanceRef = FirebaseDatabase.getInstance().getReference("attendance");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            fetchLocation();
        }
        storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        fileRef = storageRef.child("attendance_records").child(date).child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        Toolbar tb=findViewById(R.id.toolbar4);
        setSupportActionBar(tb);
        tb.setLogo(R.mipmap.logo_round);
        facecapturebutton = findViewById(R.id.facecapturebutton);
        bgcapturebutton = findViewById(R.id.bgcapturebutton);
        markitbutton = findViewById(R.id.markitbutton);
        face=findViewById(R.id.faceview);
        bg=findViewById(R.id.bgview);
        fb=findViewById(R.id.faceprogressBar);
        fb.setVisibility(View.INVISIBLE);
        bgb=findViewById(R.id.bgprogressBar);
        bgb.setVisibility(View.INVISIBLE);
        fb.setProgress(0);
        bgb.setProgress(0);

        facecapturebutton.setOnClickListener(view -> {
            openCamera(1);
            fb.setVisibility(View.VISIBLE);
        });
        bgcapturebutton.setOnClickListener(view -> {
            int prog=fb.getProgress();
            if(!isFaceUploaded)
            {
                Toast.makeText(markActivity.this,"Upload Face First !",Toast.LENGTH_SHORT).show();
            }
            else if(prog>0 && prog<100)
            {
                Toast.makeText(markActivity.this,"IMG Uploading, Wait !",Toast.LENGTH_SHORT).show();
            }
            else if(isFaceUploaded)
            {
                openCamera(0);
                bgb.setVisibility(View.VISIBLE);
            }
        });
        markitbutton.setOnClickListener(view -> {
            int prog2=bgb.getProgress();
            if(!isBgUploaded)
            {
                Toast.makeText(markActivity.this,"Upload Work Location IMG !",Toast.LENGTH_SHORT).show();
                return;
            }
            else if(prog2>0 && prog2<100)
            {
                Toast.makeText(markActivity.this,"IMG Uploading, Wait !",Toast.LENGTH_SHORT).show();
                return;
            }
            if(!isFaceUploaded && !isBgUploaded)
            {
                Toast.makeText(markActivity.this,"Upload Both Images!",Toast.LENGTH_SHORT).show();
            }else if(isFaceUploaded && isBgUploaded){
                markAttendance("Present",locationString);
                new Handler().postDelayed(() -> {
                    finish();
                }, 3000);
            }
        });

    }

    public void openCamera(int id) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            CheckPerm cp=new CheckPerm();
            cp.checkAndRequestPermissions(markActivity.this);
            return;
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        if (id == 1) {
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
            try {
                startActivityForResult(intent, REQUEST_FACE_IMAGE_CAPTURE);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Camera not supported", Toast.LENGTH_SHORT).show();
            }
        } else if(id==0){
            intent.putExtra("android.intent.extras.CAMERA_FACING", 0);
            intent.putExtra("android.intent.extras.LENS_FACING_BACK", 0);
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", false);
            try {
                startActivityForResult(intent, REQUEST_BG_IMAGE_CAPTURE);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Camera not supported", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FACE_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (imageUri != null) {
                face.setImageURI(imageUri);
                Log.d("ImageURI", "Captured Image URI: " + imageUri.toString());
                uploadfaceimg(imageUri);
            }
        }
        else if(requestCode == REQUEST_BG_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (imageUri != null) {
                bg.setImageURI(imageUri);
                Log.d("ImageURI", "Captured Image URI: " + imageUri.toString());
                uploadbgimg(imageUri);
            }
        }
    }

    private void uploadfaceimg(Uri imageUri) {
        UploadTask uploadTask = fileRef.child("Face").putFile(imageUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            fileRef.child("Face").getDownloadUrl().addOnSuccessListener(uri -> {
                faceurl = uri.toString();
                isFaceUploaded=true;
                Log.d("Img Upload","Upload successful! Download URL: " + faceurl);
                Toast.makeText(markActivity.this,"Face IMG Uploaded!",Toast.LENGTH_SHORT).show();
                fb.setVisibility(View.INVISIBLE);
            }).addOnFailureListener(exception -> {
                // Handle any errors getting the download URL
                Log.e("Failed to get download URL: ",exception.getMessage());
            });
        }).addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Log.e("Upload failed: ",exception.getMessage());
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            fb.setProgress((int)progress);
            Log.d("Uploading img","Upload is " + progress + "% done");
        });

    }

    private void uploadbgimg(Uri imageUri) {
        UploadTask uploadTask = fileRef.child("Work_Location").putFile(imageUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            fileRef.child("Work_Location").getDownloadUrl().addOnSuccessListener(uri -> {
                bgurl = uri.toString();
                isBgUploaded=true;
                Log.d("Img Upload","Upload successful! Download URL: " + bgurl);
                Toast.makeText(markActivity.this,"Work Location IMG Uploaded!",Toast.LENGTH_SHORT).show();
                bgb.setVisibility(View.INVISIBLE);
            }).addOnFailureListener(exception -> {
                // Handle any errors getting the download URL
                Log.e("Failed to get download URL: ",exception.getMessage());
            });
        }).addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Log.e("Upload failed: ",exception.getMessage());
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            bgb.setProgress((int)progress);
            Log.d("Uploading img","Upload is " + progress + "% done");
        });

    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(lat, lon, 1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                locationString= addresses.get(0).getLocality();

            } else {
                Toast.makeText(this, "Could not fetch location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void markAttendance(String statuss, String location) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(markActivity.this,AuthActivity.class));
            return;
        }

        String uid = user.getUid();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Map<String, Object> attendanceData = new HashMap<>();
        attendanceData.put("status", statuss);
        attendanceData.put("location", location);
        Map<String, String> imgData = new HashMap<>();
        imgData.put("Face_IMG",faceurl);
        imgData.put("Work_Location_IMG",bgurl);
        DatabaseReference newref=attendanceRef.child(date).child(uid);
        newref.child("Info").setValue(attendanceData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Attendance marked!", Toast.LENGTH_SHORT).show();

                    newref.child("Img").setValue(imgData)
                            .addOnSuccessListener(aVoid2 ->
                                    Toast.makeText(this, "IMG Link uploaded!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving attendance: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

}