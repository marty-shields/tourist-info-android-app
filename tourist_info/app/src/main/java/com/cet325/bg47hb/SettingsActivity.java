package com.cet325.bg47hb;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private CurrencyRates localCurrencyRates, favCurrencyRates;
    private int looperFav = 0;
    private int selectedFav;
    private String local, fav;
    private boolean userIsInteracting;
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent i = getIntent();

        localCurrencyRates = (CurrencyRates) i.getSerializableExtra("localCurrency");
        favCurrencyRates = (CurrencyRates) i.getSerializableExtra("favCurrency");

        //get local and fav currency
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        local = sharedPref.getString("localCurrency", "DEFAULT");
        fav = sharedPref.getString("favCurrency", "DEFAULT");

        Log.d("SHAREDPREFS", local);
        Log.d("SHAREDPREFS", fav);
        Log.d("currentdate", localCurrencyRates.getDateModified());
        Log.d("currentdate", favCurrencyRates.getDateModified());
        setCurrencyTextViews();

        //create spinner array and resource string array
        final List<String> spinnerArray = new ArrayList<String>();
        final String[] rates = getResources().getStringArray(R.array.rates);

        for (String rate: rates) {
            spinnerArray.add(rate);

            //find the index of the local currency and favourite currency
            if(rate.equals(fav)){
                selectedFav = looperFav;
            }
            looperFav++;
        }

        //create array adapter for spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
        this, R.layout.spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner sFav = (Spinner) findViewById(R.id.spinner_settingsFav);
        sFav.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        sFav.setAdapter(adapter);


        //set the spinner selection to the actual local and favourite
        sFav.setSelection(selectedFav);

        //set on changed listener for the spinners
        sFav.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                if (userIsInteracting) {
                    looperFav = 0;
                    for (String rate: rates) {
                        //find the index of the local currency and favourite currency
                        if(rate.equals(fav)){
                            selectedFav = looperFav;
                        }

                        looperFav++;
                    }
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                            .putString("favCurrency", sFav.getSelectedItem().toString()).commit();
                    fav = sFav.getSelectedItem().toString();

                    //get current currency rate and add to views
                    getCurrencyRate();

                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        setCurrencyTextViews();
    }

    //this is for when clicking the button
    public void updateCurrency(View v){
        getCurrencyRate();
    }

    public void setCurrencyTextViews() {
        //set up the currency to show what the conversions are
        TextView textViewLocal = (TextView)findViewById(R.id.textView_localToFav);
        TextView textViewFav = (TextView)findViewById(R.id.textView_favToLocal);
        TextView textViewDateUpdated = (TextView)findViewById(R.id.textView_settings_dateUpdated);
        TextView textViewTimeUpdated = (TextView)findViewById(R.id.textView_settings_timeUpdated);
        TextView textViewLocalDateUpdated = (TextView)findViewById
                (R.id.textView_settings_localDateUpdated);
        TextView textViewLocalTimeUpdated = (TextView)findViewById
                (R.id.textView_settings_localTimeUpdated);

        float localRate = 0;
        float favRate = 0;

        //get rate for local cuurrency rate
        for (CurrencyRate currencyRate: localCurrencyRates.getCurrencyRates()) {
            if(currencyRate.getType().equals(fav)){
                localRate = currencyRate.getRate();
                Log.d("CurrencyRate", String.valueOf(currencyRate.getType().toString()));
                Log.d("CurrencyRate", String.valueOf(localRate));
            }
        }
        for (CurrencyRate currencyRate: favCurrencyRates.getCurrencyRates()) {
            if(currencyRate.getType().equals(local)){
                favRate = currencyRate.getRate();
                Log.d("CurrencyRate", String.valueOf(currencyRate.getType().toString()));
                Log.d("CurrencyRate", String.valueOf(favRate));
            }
        }

        //split string for date and time
        String[] favSeperated = favCurrencyRates.getDateModified().split(" ");
        String[] localSeperated = localCurrencyRates.getDateModified().split(" ");

        textViewLocal.setText("(" + local + "): " + "1 - (" + fav + "): " + localRate);
        textViewFav.setText("(" + fav + "): " + "1 - (" + local + "): " + favRate);
        textViewTimeUpdated.setText("Time Updated: " + favSeperated[0]);
        textViewDateUpdated.setText("Date Updated: " + favSeperated[1]);
        textViewLocalTimeUpdated.setText("Time Updated: " + localSeperated[0]);
        textViewLocalDateUpdated.setText("Date Updated: " + localSeperated[1]);
    }

    public void getCurrencyRate(){
        final View view = findViewById(android.R.id.content);
        final Spinner sFav = (Spinner) findViewById(R.id.spinner_settingsFav);
        Snackbar.make(view, "Loading...", Snackbar.LENGTH_INDEFINITE)
                .setAction("Action", null).show();
        //disable buttons until it is finished
        final Button btnUpdate = (Button)findViewById(R.id.button_settings_update);
        final Button btnExit = (Button)findViewById(R.id.button_settings_exit);
        TextView textViewLocal = (TextView)findViewById(R.id.textView_localToFav);
        TextView textViewFav = (TextView)findViewById(R.id.textView_favToLocal);
        textViewLocal.setText("Loading...");
        textViewFav.setText("Loading...");

        btnUpdate.setEnabled(false);
        btnExit.setEnabled(false);
        sFav.setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int isFinished = 0;
                do {
                    //get currency stuff
                    String[] rates = getResources().getStringArray(R.array.rates);

                    JSONCurrencyTask task = new JSONCurrencyTask();

                    localCurrencyRates.setCurrencyRates
                            (task.requestCurrencyRates(rates, local));

                    DateFormat df = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
                    String currentDateTimeSetting = df.format(new Date());
                    localCurrencyRates.setDateModified(currentDateTimeSetting);
                    Log.d("currentdate", localCurrencyRates.getDateModified());

                    for (CurrencyRate rate : localCurrencyRates.getCurrencyRates()) {
                        Log.d("OBJECTLOCAL", "THIS IS FROM THE OBJECT");
                        Log.d("Currency", rate.getType());
                        Log.d("Rate", String.valueOf(rate.getRate()));
                    }

                    task = new JSONCurrencyTask();

                    favCurrencyRates.setCurrencyRates
                            (task.requestCurrencyRates(rates, fav));

                    df = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
                    currentDateTimeSetting = df.format(new Date());
                    favCurrencyRates.setDateModified(currentDateTimeSetting);
                    Log.d("currentdate", localCurrencyRates.getDateModified());

                    for (CurrencyRate rate : favCurrencyRates.getCurrencyRates()) {
                        Log.d("OBJECTFAV", "THIS IS FROM THE OBJECT");
                        Log.d("Currency", rate.getType());
                        Log.d("Rate", String.valueOf(rate.getRate()));
                    }
                    isFinished = 1;
                }
                while(isFinished == 0);

                Snackbar.make(view, "Finished Loading", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                btnExit.setEnabled(true);
                btnUpdate.setEnabled(true);
                sFav.setEnabled(true);
                setCurrencyTextViews();

                }
        }, SPLASH_TIME_OUT);
    }

    public void goBack(View v){
        Intent i = new Intent(this, MainMenu.class);
        i.putExtra("localCurrency", localCurrencyRates);
        i.putExtra("favCurrency", favCurrencyRates);
        startActivity(i);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        userIsInteracting = true;
    }
}
