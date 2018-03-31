package com.android.folio;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.ContentValues.TAG;

public class SignUpPageActivity extends AppCompatActivity implements View.OnClickListener {

    //==============================================================================================
    // Declare Variables
    //==============================================================================================
    private EditText editTextEmail, editTextPassword, editTextName;
    private ProgressBar progressBar;
    private TextView textViewSignIn;
    private FirebaseAuth mAuth;
    private Button buttonSignUp;

    private DatabaseReference db;

    private static final String NAME = "public_profile", EMAIL = "email";

    //==============================================================================================
    // On Create Setup
    //==============================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Check if User is Authenticated
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currUser = mAuth.getCurrentUser();
        updateUI(currUser);

        setContentView(R.layout.activity_sign_up_page);

        initListeners();

        db = FirebaseDatabase.getInstance().getReference();
    }

    //==============================================================================================
    // On Start setup
    //==============================================================================================
    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

    }

    //==============================================================================================
    // Action Listeners
    //==============================================================================================
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.buttonSignUp:
                registerUser();
                break;
            case R.id.textViewSignIn:
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
    }

    //==============================================================================================
    // Helper Functions
    //==============================================================================================
    private void updateUI(FirebaseUser currentUser) {

        if (currentUser != null) {
            finish();
            startActivity(new Intent(this, HomePageActivity.class));
        } else {
            Log.d(TAG, "current user is null");
        }

    }

    private void initListeners() {

        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextEmail = (EditText)findViewById(R.id.editTextEmail);
        editTextPassword = (EditText)findViewById(R.id.editTextPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        buttonSignUp = (Button) findViewById(R.id.buttonSignUp);
        textViewSignIn = (TextView) findViewById(R.id.textViewSignIn);

        buttonSignUp.setOnClickListener(this);
        textViewSignIn.setOnClickListener(this);
    }

    private void registerUser() {

        // Sanitize Inputs
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if(name.isEmpty()) {
            editTextName.setError("Name is required");
            editTextName.requestFocus();
            return;
        }

        if(email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if(password.length() < 6) {
            editTextPassword.setError("Minimum length of password should be 6");
            editTextPassword.requestFocus();
            return;
        }


        // Show Progress bar
        progressBar.setVisibility(View.VISIBLE);

        // If all fields pass all checks proceed to creation
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {

                            // Variable Set up
                            String name = editTextName.getText().toString().trim();
                            String email = editTextEmail.getText().toString().trim();
                            FirebaseUser currUser = mAuth.getCurrentUser();
                            String uid = currUser.getUid();


                            // Save User Data to DataBase
                            saveUserData(name, email, uid);

                            // Sign in success, update UI with the signed in User's Information
                            updateUI(currUser);

                        } else {

                            Toast.makeText(SignUpPageActivity.this, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void saveUserData(String name, String email, String uid) {
        User user = new User(name, email, uid);
        System.out.println("clicked: " + user.getName());
        db.child("users").child(uid).setValue(user);
    }
}
