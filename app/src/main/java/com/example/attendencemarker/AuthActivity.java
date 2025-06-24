package com.example.attendencemarker;

import static android.content.ContentValues.TAG;
import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class AuthActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button signupButton, loginButton, googleLoginButton;
    private FirebaseAuth mAuth;
    Toolbar tb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        CheckPerm cp=new CheckPerm();
        cp.checkAndRequestPermissions(AuthActivity.this);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(AuthActivity.this, MainActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_auth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tb=findViewById(R.id.toolbar2);
        setSupportActionBar(tb);
        tb.setLogo(R.mipmap.logo_round);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signupButton = findViewById(R.id.signupButton);
        loginButton = findViewById(R.id.loginButton);
        googleLoginButton = findViewById(R.id.google_login_button);

        googleLoginButton.setOnClickListener(view -> {
            GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setAutoSelectEnabled(true)
                    .build();

            GetCredentialRequest request = new GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build();

            CredentialManager credentialManager = CredentialManager.create(AuthActivity.this);

            credentialManager.getCredentialAsync(
                    AuthActivity.this,
                    request,
                    null,
                    ContextCompat.getMainExecutor(AuthActivity.this),
                    new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                        @Override
                        public void onResult(GetCredentialResponse result) {
                            Credential credential = result.getCredential();
                            handleSignIn(credential);
                        }

                        @Override
                        public void onError(GetCredentialException e) {
                            Log.e("CredentialError", "Error getting credential", e);
                            Toast.makeText(AuthActivity.this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });


        signupButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if(email.isEmpty() || password.isEmpty())
            {
                Toast.makeText(AuthActivity.this, "Please Enter Email and Password", Toast.LENGTH_SHORT).show();
                return;
            }
            AlertDialog.Builder builder=new AlertDialog.Builder(AuthActivity.this);
            builder.setTitle("Notice!");
            EditText username=new EditText(AuthActivity.this);
            username.setHint("Enter User Name");
            builder.setView(username);
            builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    signUp(email, username.getText().toString().trim(),password);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    builder.setCancelable(true);
                }
            });
            AlertDialog dialog=builder.create();
            dialog.show();

        });

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if(email.isEmpty() || password.isEmpty())
            {
                Toast.makeText(AuthActivity.this, "Please Enter Email and Password", Toast.LENGTH_SHORT).show();
                return;
            }
            login(email, password);
        });
    }
    private void signUp(String email,String username, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseDatabase.getInstance().getReference("users").child(task.getResult().getUser().getUid()).child("name").setValue(username);
                        FirebaseDatabase.getInstance().getReference("admins").child(task.getResult().getUser().getUid()).setValue(false);
                        Toast.makeText(AuthActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                        Toast.makeText(AuthActivity.this, "Please Login Now.", Toast.LENGTH_SHORT).show();
                        signupButton.setVisibility(View.INVISIBLE);
                    } else {
                        Toast.makeText(AuthActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AuthActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        updateUI();
                    } else {
                        Toast.makeText(AuthActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleSignIn(Credential credential) {
        if (credential instanceof CustomCredential customCredential
                && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            Bundle credentialData = customCredential.getData();
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);

            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
        } else {
            Log.w(TAG, "Credential is not of type Google ID!");
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseDatabase.getInstance().getReference("users").child(mAuth.getUid()).child("name").setValue(mAuth.getCurrentUser().getDisplayName());
                        FirebaseDatabase.getInstance().getReference("admins").child(mAuth.getUid()).setValue(false);
                        Toast.makeText(AuthActivity.this, "Google Sign-In successful!", Toast.LENGTH_SHORT).show();
                        updateUI();
                    } else {
                        // If sign in fails, display a message to the user
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(AuthActivity.this, "Google Sign-In failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        String uid = mAuth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("admins").child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().getValue(Boolean.class)) {
                    startActivity(new Intent(AuthActivity.this, AdminActivity.class));
                    finish();
                    return;
            }else{
                startActivity(new Intent(AuthActivity.this, MainActivity.class));
                finish();
        }}});
    }
}