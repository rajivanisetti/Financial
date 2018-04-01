package com.android.folio;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener {

    //==============================================================================================
    // Declare Variables
    //==============================================================================================
    private FirebaseAuth mAuth;
    private DatabaseReference db;

    ArrayList<String> tickers;
    ArrayList<Integer> weights;

    //==============================================================================================
    // On Create Setup
    //==============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Check if User is Authenticated
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();

        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        tickers = getIntent().getStringArrayListExtra("stockArray");
        weights = getIntent().getIntegerArrayListExtra("weightArray");

        // Layout Setup
        setContentView(R.layout.activity_home_page);
        initChart();

        //Add ActionListeners
        findViewById(R.id.buttonSignOut).setOnClickListener(this);

        ListView stockList = findViewById(R.id.stock_list);


        stockList.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        final ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, tickers) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                view.setBackgroundColor(Color.LTGRAY);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                text1.setText(tickers.get(position));
                text2.setText("Weight: " + weights.get(position).toString() + "%");
                return view;
            }
        };

        stockList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        stockList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ticker = adapter.getItem(position).toString();

                Intent intent = new Intent(getBaseContext(), AnalysisActivity.class);
                intent.putExtra("ticker", ticker);
                intent.putExtra("risk", getIntent().getIntExtra("riskRating", 0));
                startActivity(intent);
            }
        });

        String param = getIntent().getStringExtra("parameterString");

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(param);
    }

    //==============================================================================================
    // Action Listeners
    //==============================================================================================
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.buttonSignOut:
                finish();
                mAuth.getInstance().signOut();
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
    }

    public void initChart() {
        final FirebaseUser user = mAuth.getCurrentUser();

        db.child("users").child(user.getUid()).child("stocks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    List<PieEntry> pieEntries = new ArrayList<>();

                    for (DataSnapshot stocks : dataSnapshot.getChildren()) {
                        pieEntries.add(new PieEntry(Integer.parseInt(stocks.getValue().toString()), stocks.getKey().toString()));
                    }

                    PieDataSet dataSet = new PieDataSet(pieEntries, "Allocations");
                    dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                    PieData data = new PieData(dataSet);

                    // Get the chart;
                    PieChart chart = (PieChart) findViewById(R.id.pieChart);
                    chart.setData(data);
                    chart.setHoleRadius(0);
                    chart.setTransparentCircleRadius(0);
                    chart.animateY(3000, Easing.EasingOption.EaseOutBack);
                    chart.invalidate();
                } else {
                    System.out.println("USER HAS NO STOCKS");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("PULLING STOCKS FAILED");
            }
        });
    }

    class AsyncTaskRunner extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            APIReader apiReader = new APIReader();
            String output = "";

            String s = "";

            if (params[0] == null) {
                for (int i = 0; i < tickers.size(); i++) {
                    String ticker = tickers.get(i);
                    String weight = weights.get(i).toString();

                    s = s.concat(ticker);
                    s = s.concat("~");
                    s = s.concat(weight);
                    s = s.concat("|");
                }

                // remove trailing "|"
                s = s.substring(0, s.length() - 1);
            }
            else
                s = params[0];
            try {
                output = apiReader.readData(s, "USD", true);
                Log.e("output", output);
            } catch (Exception e) {
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
                oneYear = matcher.group(0).replaceAll("[^\\d.]", "");
            }

            pattern = Pattern.compile("\"threeYear\":[0-9]*[.][0-9]*");
            matcher = pattern.matcher(result);
            String threeYear = "";

            if (matcher.find()) {
                threeYear = matcher.group(0).replaceAll("[^\\d.]", "");
            }

            pattern = Pattern.compile("\"tenYear\":[0-9]*[.][0-9]*");
            matcher = pattern.matcher(result);
            String tenYear = "";

            if (matcher.find()) {
                tenYear = matcher.group(0).replaceAll("[^\\d.]", "");
            }

            pattern = Pattern.compile("\"oneYearRisk\":[0-9]*[.][0-9]*");
            matcher = pattern.matcher(result);
            String oneYearRisk = "";

            if (matcher.find()) {
                oneYearRisk = matcher.group(0).replaceAll("[^\\d.]", "");
            }

            pattern = Pattern.compile("\"threeYearRisk\":[0-9]*[.][0-9]*");
            matcher = pattern.matcher(result);
            String threeYearRisk = "";

            if (matcher.find()) {
                threeYearRisk = matcher.group(0).replaceAll("[^\\d.]", "");
            }

            pattern = Pattern.compile("\"tenYearRisk\":[0-9]*[.][0-9]*");
            matcher = pattern.matcher(result);
            String tenYearRisk = "";

            if (matcher.find()) {
                tenYearRisk = matcher.group(0).replaceAll("[^\\d.]", "");
            }

            pattern = Pattern.compile("\"sixMonth\":[0-9]*[.][0-9]*");
            matcher = pattern.matcher(result);
            String sixMonth = "";

            if (matcher.find()) {
                sixMonth = matcher.group(0).replaceAll("[^\\d.]", "");
            }

            pattern = Pattern.compile("\"sixMonthRisk\":[0-9]*[.][0-9]*");
            matcher = pattern.matcher(result);
            String sixMonthRisk = "";

            if (matcher.find()) {
                sixMonthRisk = matcher.group(0).replaceAll("[^\\d.]", "");
            }

            pattern = Pattern.compile("\"threeMonthRisk\":[0-9]*[.][0-9]*");
            matcher = pattern.matcher(result);
            String threeMonthRisk = "";

            if (matcher.find()) {
                threeMonthRisk = matcher.group(0).replaceAll("[^\\d.]", "");
            }

            pattern = Pattern.compile("\"threeMonth\":[0-9]*[.][0-9]*");
            matcher = pattern.matcher(result);
            String threeMonth = "";

            if (matcher.find()) {
                threeMonth = matcher.group(0).replaceAll("[^\\d.]", "");
            }

            threeMonth = (threeMonth.equals("")) ? "N/A" : String.format(Locale.US, "%.4g%n", Float.parseFloat(threeMonth));
            threeMonthRisk = (threeMonthRisk.equals("")) ? "N/A" : String.format(Locale.US, "%.4g%n", Float.parseFloat(threeMonthRisk));
            sixMonth = (sixMonth.equals("")) ? "N/A" : String.format(Locale.US, "%.4g%n", Float.parseFloat(sixMonth));
            sixMonthRisk = (sixMonthRisk.equals("")) ? "N/A" : String.format(Locale.US, "%.4g%n", Float.parseFloat(sixMonthRisk));
            oneYear = (oneYear.equals("")) ? "N/A" : String.format(Locale.US, "%.4g%n", Float.parseFloat(oneYear));
            threeYear = (threeYear.equals("")) ? "N/A" : String.format(Locale.US, "%.4g%n", Float.parseFloat(threeYear));
            tenYear = (tenYear.equals("")) ? "N/A" : String.format(Locale.US, "%.4g%n", Float.parseFloat(tenYear));
            oneYearRisk = (oneYearRisk.equals("")) ? "N/A" : String.format(Locale.US, "%.4g%n", Float.parseFloat(oneYearRisk));
            threeYearRisk = (threeYearRisk.equals("")) ? "N/A" : String.format(Locale.US, "%.4g%n", Float.parseFloat(threeYearRisk));
            tenYearRisk = (tenYearRisk.equals("")) ? "N/A" : String.format(Locale.US, "%.4g%n", Float.parseFloat(tenYearRisk));

            try {
                String[][] data = {
                        {"Three Month Performance", threeMonth},
                        {"Six Month Performance", sixMonth},
                        {"One Year Performance", oneYear},
                        {"Three Year Performance", threeYear},
                        {"Ten Year Performance", tenYear},
                        {"Three Month Risk", threeMonthRisk},
                        {"Six Month Risk", sixMonthRisk},
                        {"One Year Risk", oneYearRisk},
                        {"Three Year Risk", threeYearRisk},
                        {"Ten Year Risk", tenYearRisk}};

                TableView tv = findViewById(R.id.tableView);
                SimpleTableDataAdapter adapter = new SimpleTableDataAdapter(getBaseContext(), data);
                tv.setDataAdapter(adapter);
                adapter.notifyDataSetChanged();

                TableColumnWeightModel columnModel = new TableColumnWeightModel(2);
                columnModel.setColumnWeight(0, 2);
                columnModel.setColumnWeight(1, 1);
                tv.setColumnModel(columnModel);

                String[] headers = {"Metric", "Statistic"};
                tv.setHeaderAdapter(new SimpleTableHeaderAdapter(getBaseContext(), headers));
                tv.setHeaderBackgroundColor(Color.GRAY);
                tv.setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(Color.LTGRAY, Color.LTGRAY));
            }
            catch(Exception e){
                Log.e("number error", e.toString());
            }
        }
    }
}