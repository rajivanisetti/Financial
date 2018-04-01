package com.android.folio;

import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            initChart();
            //Add ActionListeners
            findViewById(R.id.buttonSignOut).setOnClickListener(this);

            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.execute();
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

        public void initChart() {
            float rainfall[] = {98.8f, 123.8f, 162.6f, 24.3f};
            String monthName[] = {"Jan", "Feb", "Mar", "Apr"};

            List<PieEntry> pieEntries = new ArrayList<>();
            for (int i = 0; i < rainfall.length; i++) {
                pieEntries.add(new PieEntry(rainfall[i], monthName[i]));
            }

            PieDataSet dataSet = new PieDataSet(pieEntries, "Rainfall for Vancouver");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            PieData data = new PieData(dataSet);

            // Get the chart;
            PieChart chart = (PieChart) findViewById(R.id.pieChart);
            chart.setData(data);
            chart.animateY(3000, Easing.EasingOption.EaseOutBack);
            chart.invalidate();
        }

    static class AsyncTaskRunner extends AsyncTask <Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            APIReader apiReader = new APIReader();
            String output = "";
            try {
                output = apiReader.readData("BLK~25|AAPL~25|IXN~25|MALOX~25", "USD", true);
            }
            catch (Exception e) {
                Log.e("APIReader", e.toString());
            }
            return output;
        }

        @Override
        protected void onPostExecute(String result) {
            Pattern pattern = Pattern.compile("\"oneYear\":[0-9]*[.][0-9]*");
            Matcher matcher = pattern.matcher(result);
            String oneYear = "";

            if (matcher.find()) {
                oneYear = matcher.group(0);
            }

            Log.e("oneyear", oneYear);
        }
    }
}