package com.android.folio;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;


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

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.services.language.v1beta2.CloudNaturalLanguage;
import com.google.api.services.language.v1beta2.CloudNaturalLanguageRequestInitializer;
import com.google.api.services.language.v1beta2.model.AnnotateTextRequest;
import com.google.api.services.language.v1beta2.model.AnnotateTextResponse;
import com.google.api.services.language.v1beta2.model.Document;
import com.google.api.services.language.v1beta2.model.Features;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.services.language.v1beta2.CloudNaturalLanguage;
import com.google.api.services.language.v1beta2.CloudNaturalLanguageRequestInitializer;
import com.google.api.services.language.v1beta2.model.AnnotateTextRequest;
import com.google.api.services.language.v1beta2.model.AnnotateTextResponse;
import com.google.api.services.language.v1beta2.model.Document;
import com.google.api.services.language.v1beta2.model.Features;

public class AnalysisActivity extends AppCompatActivity {

    float totalSentiment = 0;
    private String CLOUD_API_KEY = "AIzaSyBP_3jPRzVum-DnQqie68laZ3dWGgNaHow";
    ArrayList<String> myBodies = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();

       /* String body = myBodies.get(0);
        analyzeText(body); */
    }

    String ticker = "FB";

    private class AsyncTaskRunner extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String myUrl = "https://www.google.com/search?q=" + ticker + "&tbm=nws";
                org.jsoup.nodes.Document doc = Jsoup.connect(myUrl)
                        //.userAgent("Mozilla")
                        .ignoreHttpErrors(true)
                        .get();

                Elements links = doc.select("a.RTNUJf[href]");

                int n = 5;

                for (int i = 0; i < n; i++) {
                    //Log.e("I", "I: " + Integer.toString(i));
                    String newUrl = links.get(i).select("a[href]").attr("abs:href");
                    Log.e("link", newUrl);
                    try {
                        org.jsoup.nodes.Document newDoc = Jsoup.connect(newUrl).userAgent("Mozilla").ignoreHttpErrors(true).get();


                         Elements words = newDoc.select("h1, h2, h3, h4, h5, h6");
                         String s = "";



                         for (Element e : words) {
                             // myBodies.add(i, e.text());
                         s = s.concat(e.text());

                        }


                        Log.e("body", s);

                        totalSentiment += analyzeText(s);

                        myBodies.add(s);

                    }
                    catch (IOException e)
                    {
                        Log.e("Error", "Could not read");
                        n++;
                        continue;
                    }
                }

                // At this point, MyBodies array has the words of each if the first 5 files //

            }

            catch (IOException e)
            {
                Log.e("Main", e.toString());
            }

            float sentAve = totalSentiment / 5;
            Log.e("SentAve", Float.toString(sentAve));

            return null;
        }
    }

    public float analyzeText(String transcript) {
        final CloudNaturalLanguage naturalLanguageService =
                new CloudNaturalLanguage.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(),
                        null
                ).setCloudNaturalLanguageRequestInitializer(
                        new CloudNaturalLanguageRequestInitializer(CLOUD_API_KEY)
                ).build();

        com.google.api.services.language.v1beta2.model.Document document = new Document();
        document.setType("PLAIN_TEXT");
        document.setLanguage("en-US");
        document.setContent(transcript);

        Features features = new Features();
        features.setExtractDocumentSentiment(true);

        final AnnotateTextRequest request = new AnnotateTextRequest();
        request.setDocument(document);
        request.setFeatures(features);
        AnnotateTextResponse response =
                null;
        try {
            response = naturalLanguageService.documents()
                    .annotateText(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final float sentiment =response.getDocumentSentiment().getScore();
        Log.e("sentiment", Float.toString(sentiment));

        return sentiment;
    }
}
