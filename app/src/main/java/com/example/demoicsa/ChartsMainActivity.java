package com.example.demoicsa;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demoicsa.charts.PSAccessLogChart;

public class ChartsMainActivity extends AppCompatActivity {

    private PSAccessLogChart accessLogChart;
    private static final String TAG = "ChartsMainActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: init");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        Log.d(TAG, "onCreate: ActionBar configured");
        LinearLayout chartsLayout1 = findViewById(R.id.collapsible_charts_1);
        Log.d(TAG, "onCreate: layout  1 found");

        /* -- Setup PSAccessLogChart -- */
        Log.d(TAG, "onCreate: Setting up PSAccessLogChart Helper Class");
        accessLogChart = new PSAccessLogChart("http://201.234.130.156:8000/", 60,
                getString(R.string.rest_login_user), getString(R.string.rest_login_pwd));
        Log.d(TAG, "onCreate: Inflating ProgressBar");
        ProgressBar progressBar = (ProgressBar) getLayoutInflater()
                .inflate(R.layout.customview_progressbar_large, chartsLayout1, false);
        Log.d(TAG, "onCreate: Setting ProgressBar LinearLayout.LayoutParams");
        progressBar.setLayoutParams(new ScrollView.LayoutParams(
                ScrollView.LayoutParams.WRAP_CONTENT,
                ScrollView.LayoutParams.WRAP_CONTENT));
        Log.d(TAG, "onCreate: Adding ProgressBar to parent view");
        chartsLayout1.addView(progressBar);
        progressBar.setVisibility(View.GONE);
        Log.d(TAG, "onCreate: Setting ProgressBar spinner to PSAccessLogChart helper class");
        accessLogChart.setSpinner(progressBar);

        /* -- OnClickListeners for each division -- */
        /* -- PSAccessLogChart OnClickListener -- */
        findViewById(R.id.collapsible_charts_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accessLogChart.toggle(view, 650, getApplicationContext());
                if (accessLogChart.isToggled){

                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
