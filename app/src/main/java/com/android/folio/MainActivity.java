package com.android.folio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.view.View;

import android.app.ProgressDialog;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity implements View.OnClickListener{

    //==============================================================================================
    // Declare Variables
    //==============================================================================================
    // Layout
    private EditText editTextEmail, editTextPassword;
    private Button buttonSignIn;
    private TextView textViewSignUp, textViewForgotPassword;
    private ProgressDialog progressDialog;

    // APIs
    private FirebaseAuth mAuth;

    private static final String EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Check if User is Authenticated
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currUser = mAuth.getCurrentUser();
        updateUI(currUser);

        // Layout Setup
        setContentView(R.layout.activity_main);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonSignIn = (Button) findViewById(R.id.buttonSignIn);
        textViewSignUp = (TextView) findViewById(R.id.textViewSignUp);
        progressDialog = new ProgressDialog(this);
        textViewForgotPassword = (TextView) findViewById(R.id.textViewForgotPassword);

        //Listeners
        buttonSignIn.setOnClickListener(this);
        textViewSignUp.setOnClickListener(this);
        textViewForgotPassword.setOnClickListener(this);

    }


    //==============================================================================================
    // Helper Functions
    //==============================================================================================
    private void userLogin() {

        // Sanitize Inputs
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if(email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        progressDialog.setMessage("Logging In please wait...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser currentUser) {

        if (currentUser != null) {
            finish();
            startActivity(new Intent(this, StockActivity.class));
        }
    }


    //==============================================================================================
    // Action Listeners
    //==============================================================================================
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.buttonSignIn:
                userLogin();
                break;
            case R.id.textViewSignUp:
                finish();
                startActivity(new Intent(this, SignUpPageActivity.class));
                break;
            case R.id.textViewForgotPassword:
                finish();
                startActivity(new Intent(this, ForgotPasswordActivity.class));
                break;
        }
    }
}
