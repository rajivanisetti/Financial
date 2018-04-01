package com.android.folio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.view.View;

import android.app.ProgressDialog;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnClickListener{

    //==============================================================================================
    // Declare Variables
    //==============================================================================================
    // Layout
    private EditText editTextEmail, editTextPassword;
    private Button buttonSignIn;
    private TextView textViewSignUp, textViewForgotPassword;
    private ProgressDialog progressDialog;
    private DatabaseReference db;

    // APIs
    private FirebaseAuth mAuth;

    private static final String EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Check if User is Authenticated
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currUser = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference();

        if(currUser != null) {
            db.child("users").child(currUser.getUid()).child("isVirgin").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final FirebaseUser user = mAuth.getCurrentUser();
                    db.child("users").child(user.getUid()).child("isVirgin").addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final int bool = Integer.parseInt(dataSnapshot.getValue().toString());

                            db.child("users").child(user.getUid()).child("stocks").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot ds) {
                                    final ArrayList<String> tickers = new ArrayList<>();
                                    final ArrayList<Integer> weights = new ArrayList<>();

                                    for(DataSnapshot stocks : ds.getChildren()) {
                                        tickers.add(stocks.getKey());
                                        weights.add(Integer.parseInt(stocks.getValue().toString()));
                                    }

                                    final Intent intent = new Intent(getBaseContext(), HomePageActivity.class);
                                    intent.putExtra("stockArray", tickers);
                                    intent.putExtra("weightArray", weights);
                                    updateUI(user, bool,intent);

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    updateUI(null, 1);
                }
            });
        }

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
                            final FirebaseUser user = mAuth.getCurrentUser();
                            db.child("users").child(user.getUid()).child("isVirgin").addValueEventListener(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final int bool = Integer.parseInt(dataSnapshot.getValue().toString());

                                    db.child("users").child(user.getUid()).child("stocks").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot ds) {
                                            final ArrayList<String> tickers = new ArrayList<>();
                                            final ArrayList<Integer> weights = new ArrayList<>();

                                            for(DataSnapshot stocks : ds.getChildren()) {
                                                tickers.add(stocks.getKey());
                                                weights.add(Integer.parseInt(stocks.getValue().toString()));
                                            }

                                            final Intent intent = new Intent(getBaseContext(), HomePageActivity.class);
                                            intent.putExtra("stockArray", tickers);
                                            intent.putExtra("weightArray", weights);
                                            updateUI(user, bool,intent);

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null, 1);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser currentUser, int isVirgin, Intent intent) {

        if (currentUser != null) {
            finish();
            if(isVirgin == 1) {
                startActivity(new Intent(this, StockActivity.class));
            } else {
                startActivity(intent);
            }
        }
    }
    private void updateUI(FirebaseUser currentUser, int isVirgin) {

        if (currentUser != null) {
            finish();
            if(isVirgin == 1) {
                startActivity(new Intent(this, StockActivity.class));
            }
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
