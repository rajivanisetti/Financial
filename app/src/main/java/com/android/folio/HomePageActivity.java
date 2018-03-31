package com.android.folio;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener {

        //==============================================================================================
        // Declare Variables
        //==============================================================================================
        private FirebaseAuth mAuth;
        private Button buttonSignOut;
        ImageView imageView;

        //==============================================================================================
        // On Create Setup
        //==============================================================================================
        @Override
        protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);

            // Check if User is Authenticated
            mAuth = FirebaseAuth.getInstance();
            if(mAuth.getCurrentUser() == null) {
                finish();
                startActivity(new Intent(this, MainActivity.class));
            }

            // Layout Setup
            setContentView(R.layout.activity_home_page);

            //Add ActionListeners

            findViewById(R.id.buttonSignOut).setOnClickListener(this);
        }

        //==============================================================================================
        // Action Listeners
        //==============================================================================================
        @Override
        public void onClick(View view) {
            switch(view.getId()) {

                case R.id.buttonSignOut:
                    finish();
                    mAuth.getInstance().signOut();
                    startActivity(new Intent(this, MainActivity.class));
                    break;

            }
        }

    }