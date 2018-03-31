package com.android.folio;

import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.services.language.v1beta2.CloudNaturalLanguage;
import com.google.api.services.language.v1beta2.CloudNaturalLanguageRequestInitializer;
import com.google.api.services.language.v1beta2.model.AnnotateTextRequest;
import com.google.api.services.language.v1beta2.model.AnnotateTextResponse;
import com.google.api.services.language.v1beta2.model.Document;
import com.google.api.services.language.v1beta2.model.Features;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener {

        //==============================================================================================
        // Declare Variables
        //==============================================================================================
        private FirebaseAuth mAuth;
        private String CLOUD_API_KEY = "AIzaSyBP_3jPRzVum-DnQqie68laZ3dWGgNaHow";

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
            findViewById(R.id.analyze_button).setOnClickListener(this);
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

                case R.id.analyze_button:
                    analyzeText("Hello World");

            }
        }

        public void analyzeText(String transcript) {
            final CloudNaturalLanguage naturalLanguageService =
                    new CloudNaturalLanguage.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new AndroidJsonFactory(),
                            null
                    ).setCloudNaturalLanguageRequestInitializer(
                            new CloudNaturalLanguageRequestInitializer(CLOUD_API_KEY)
                    ).build();

            Document document = new Document();
            document.setType("PLAIN_TEXT");
            document.setLanguage("en-US");
            document.setContent(transcript);

            Features features = new Features();
            features.setExtractDocumentSentiment(true);

            final AnnotateTextRequest request = new AnnotateTextRequest();
            request.setDocument(document);
            request.setFeatures(features);

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    AnnotateTextResponse response =
                            null;
                    try {
                        response = naturalLanguageService.documents()
                                .annotateText(request).execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    final float sentiment =((response.getDocumentSentiment().getScore())+1)*5;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            
                            AlertDialog dialog =
                                    new AlertDialog.Builder(HomePageActivity.this)
                                            .setTitle("Sentiment: " + sentiment)
                                            .setNeutralButton("Okay", null)
                                            .create();
                            dialog.show();
                        }
                    });
                }
            });
        }

    }