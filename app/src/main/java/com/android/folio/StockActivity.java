package com.android.folio;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class StockActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if User is Authenticated
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        db = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();

        setContentView(R.layout.activity_stock);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText stockName = findViewById(R.id.stock_name);
        final EditText stockWeight = findViewById(R.id.stock_weight);
        final ListView stockList = findViewById(R.id.stock_list);
        final Button addButton = findViewById(R.id.add_button);

        final ArrayList<String> tickers = new ArrayList<>();
        final ArrayList<Integer> weights = new ArrayList<>();

        final SeekBar risk = findViewById(R.id.risk_slider);

        Button doneButton = findViewById(R.id.done_button);

        final ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, tickers) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                text1.setText(tickers.get(position));
                text2.setText("Weight: " + weights.get(position).toString() + "%");
                return view;
            }
        };

        stockList.setAdapter(adapter);

        stockList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tickers.remove(position);
                weights.remove(position);

                adapter.notifyDataSetChanged();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tickers.add(stockName.getText().toString());
                weights.add(Integer.parseInt(stockWeight.getText().toString()));

                adapter.notifyDataSetChanged();

                stockName.setText("");
                stockWeight.setText("");
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int riskRating = risk.getProgress();

                int weightSum = 0;

                for (int i : weights) {
                    weightSum += i;
                }

                if (weightSum != 100) {
                    Snackbar.make(findViewById(R.id.stock_list), "Weights do not add up to 100.", Snackbar.LENGTH_LONG).show();
                }
                else {
                    String s = "";

                    for (int i = 0; i < tickers.size(); i++) {
                        String ticker = tickers.get(i);
                        String weight = weights.get(i).toString();

                        db.child("users").child(user.getUid()).child("stocks")
                                .child(tickers.get(i))
                                .setValue(weights.get(i));

                        db.child("users").child(user.getUid()).child("isVirgin").setValue(0);
                        s = s.concat(ticker);
                        s = s.concat("~");
                        s = s.concat(weight);
                        s = s.concat("|");
                    }

                    // remove trailing "|"
                    s = s.substring(0, s.length() - 1);

                    Intent intent = new Intent(getBaseContext(), HomePageActivity.class);
                    intent.putExtra("parameterString", s);
                    intent.putExtra("riskRating", riskRating);
                    intent.putExtra("stockArray", tickers);
                    intent.putExtra("weightArray", weights);
                    startActivity(intent);
                }
            }
        });
    }
}
