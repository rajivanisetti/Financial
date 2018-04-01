package com.android.folio;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class APIReader {
    public String readData(String positions, String currency, boolean extraCalculations) throws Exception {
        String urlString = "https://www.blackrock.com/tools/hackathon/portfolio-analysis";
        urlString += "?positions=" + positions + "&currency=" + currency + "&calculateRisk=" + extraCalculations +
                "&calculateExpectedReturns=" + extraCalculations + "&rcs=hackathon:pa-latest-perf";

        String output = "";

        URL url = new URL(urlString);
        URLConnection urlConnection = url.openConnection();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String inputLine;
        while ((inputLine = bufferedReader.readLine()) != null) {
            output = output.concat(inputLine);
            Log.d("API input", inputLine);
        }
        bufferedReader.close();

        return output;
    }
}