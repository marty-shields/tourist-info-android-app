package com.cet325.bg47hb;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainMenu extends AppCompatActivity {

    private CurrencyRates localCurrencyRates, favCurrencyRates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //getting currency rates to show up
        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        if(bundle != null) {
            localCurrencyRates = (CurrencyRates) i.getSerializableExtra("localCurrency");
            favCurrencyRates = (CurrencyRates) i.getSerializableExtra("favCurrency");
            Log.d("localCurrency", localCurrencyRates.getDateModified());
            Log.d("favCurrency", favCurrencyRates.getDateModified());
        }
    }

    public void GoToSettings(View v){
        //create the intent that loads up the settings activity
        Intent i = new Intent(MainMenu.this, SettingsActivity.class);
        i.putExtra("localCurrency", localCurrencyRates);
        i.putExtra("favCurrency", favCurrencyRates);
        startActivityForResult(i, 1);
    }

    public void GoToPlaces(View v){
        //create the intent that loads up the master detail for places
        Intent i = new Intent(MainMenu.this, PlacesActivity.class);
        i.putExtra("localCurrency", localCurrencyRates);
        i.putExtra("favCurrency", favCurrencyRates);
        startActivityForResult(i, 1);
    }

    public void GoToBudget(View v){
        //create the intent that loads up the master detail for places
        Intent i = new Intent(MainMenu.this, BudgetPlanner.class);
        i.putExtra("localCurrency", localCurrencyRates);
        i.putExtra("favCurrency", favCurrencyRates);
        startActivityForResult(i, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // your stuff
                localCurrencyRates = (CurrencyRates) data.getSerializableExtra("localCurrency");
                favCurrencyRates = (CurrencyRates) data.getSerializableExtra("favCurrency");
                Log.d("localCurrency", localCurrencyRates.getDateModified());
                Log.d("favCurrency", favCurrencyRates.getDateModified());
            }
        }
    }
}
