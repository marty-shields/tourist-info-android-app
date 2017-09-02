package com.cet325.bg47hb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BudgetPlanner extends AppCompatActivity {

    private CurrencyRates localCurrencyRates, favCurrencyRates;
    String array[];
    String price[];
    private String local, fav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_planner);

        Intent i = getIntent();

        //get local and fav currency
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        local = sharedPref.getString("localCurrency", "DEFAULT");
        fav = sharedPref.getString("favCurrency", "DEFAULT");

        localCurrencyRates = (CurrencyRates) i.getSerializableExtra("localCurrency");
        favCurrencyRates = (CurrencyRates) i.getSerializableExtra("favCurrency");

        Log.d("localCurrency", localCurrencyRates.getDateModified());
        Log.d("localCurrency", local);
        Log.d("favCurrency", favCurrencyRates.getDateModified());
        Log.d("favCurrency", fav);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);



    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.budget_planner, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();
        final TextView textView_main = (TextView)findViewById(R.id.textView_budget_main);
        final TextView textView_calc = (TextView)findViewById(R.id.textView_budget_Calculations);
        int looper = 0;
        String msg = "";
        String priceLocal, priceNormal, priceFav;
        float priceAdditionNormal = 0;
        float priceAdditionFav = 0;

        //see if the edit button or the delete is clicked
        switch (id){
            case R.id.menu_goBack:
                //create the intent that loads up the main menu
                Intent i = new Intent(this, MainMenu.class);
                i.putExtra("localCurrency", localCurrencyRates);
                i.putExtra("favCurrency", favCurrencyRates);
                startActivity(i);
                break;
            case R.id.menu_planned:
                QueryDBMode(1);
                looper = 0;

                //loop through both strings as long as they are not empty
                if(array.length != 0 || price.length != 0){
                    msg += "<b><u>Planned</u></b>" + "<br><br>";
                    //get set local price first
                    for (String s: array){
                        //add the string to string object to p[ut in text view
                        priceNormal = "";
                        priceFav = "";

                        //set up local price - get oringinal price first
                        float placePrice = Float.parseFloat(price[looper]);

                        if(placePrice == 0){
                            priceNormal = "FREE ";
                            priceFav = "FREE ";
                            priceAdditionNormal += 0;
                        }else {
                            priceNormal = "(" + local + ")" + price[looper] + " ";
                            priceAdditionNormal += placePrice;
                        }

                        //set price
                        if(placePrice == 0){
                            priceNormal =  "(" + local + ")" + "FREE ";
                            priceFav = "(" + fav + ")" + "FREE ";
                            priceAdditionFav += 0;
                        }else {
                            //if the local price is GBP jsut set the same as these are places in the uk
                            if (fav.equals(local)) {
                                //set price
                                DecimalFormat df = new DecimalFormat("00.00");
                                priceAdditionFav += placePrice;
                                priceFav = (fav + df.format(placePrice));
                            } else {
                                //loop through each currency to find GBP
                                for (CurrencyRate cr : localCurrencyRates.getCurrencyRates()) {
                                    if (cr.getType().equals(fav)) {
                                        DecimalFormat df = new DecimalFormat("00.00");
                                        if (cr.getRate() != 0) {
                                            float newLocalCurrency = Calculations.CalculateNewPrice(placePrice, cr);
                                            priceAdditionFav += newLocalCurrency;
                                            priceFav = ("(" + fav + ")" + df.format(newLocalCurrency) + " ");
                                        } else {
                                            priceFav = ("Unable to get currency data ");
                                        }
                                    }
                                }
                            }
                        }

                        msg+= "<b>Name:</b> " + s + "<br>" + "<b>Local Price:</b> " + priceNormal +
                                "<br><b>Favourite Currency:</b> " + priceFav + "<br><br>";
                        looper++;
                    }
                    textView_main.setText(Html.fromHtml(msg));
                    textView_main.setTextSize(16);

                    //get details about currency rates and date it was added
                    float localRate = 0;
                    float favRate = 0;
                    localRate = Calculations.CalculateLocalRate(localCurrencyRates, fav);
                    favRate = Calculations.CalculateFavRate(favCurrencyRates, local);

                    //split string for date and time
                    String[] favSeperated = favCurrencyRates.getDateModified().split(" ");
                    String[] localSeperated = localCurrencyRates.getDateModified().split(" ");
                    String exchganged;

                    exchganged = ProcessExchangeRates(localRate, favRate, favSeperated);


                    DecimalFormat df = new DecimalFormat("00.00");
                    String msgCalc = Calculations.ProcessCostString(priceAdditionNormal, priceAdditionFav, df, local, fav);

                    textView_calc.setText(Html.fromHtml(msgCalc + exchganged));
                    textView_calc.setTextSize(16);

                }else{
                    textView_main.setText("No Records Found");
                }
                break;
            case R.id.menu_visited:
                QueryDBMode(2);
                looper = 0;

                //loop through both strings as long as they are not empty
                if(array.length != 0 || price.length != 0){
                    msg += "<b><u>Visited</u></b>" + "<br><br>";
                    //get set local price first
                    for (String s: array){
                        //add the string to string object to p[ut in text view
                        priceNormal = "";
                        priceFav = "";

                        //set up local price - get oringinal price first
                        float placePrice = Float.parseFloat(price[looper]);

                        if(placePrice == 0){
                            priceNormal = "FREE ";
                            priceFav = "FREE ";
                            priceAdditionNormal += 0;
                        }else {
                            priceNormal = "(" + local + ")" + price[looper] + " ";
                            priceAdditionNormal += placePrice;
                        }

                        //set price
                        if(placePrice == 0){
                            priceNormal =  "(" + local + ")" + "FREE ";
                            priceFav = "(" + fav + ")" + "FREE ";
                            priceAdditionFav += 0;
                        }else {
                            //if the local price is GBP jsut set the same as these are places in the uk
                            if (fav.equals(local)) {
                                //set price
                                DecimalFormat df = new DecimalFormat("00.00");
                                priceAdditionFav += placePrice;
                                priceFav = (fav + df.format(placePrice));
                            } else {
                                //loop through each currency to find GBP
                                for (CurrencyRate cr : localCurrencyRates.getCurrencyRates()) {
                                    if (cr.getType().equals(fav)) {
                                        DecimalFormat df = new DecimalFormat("00.00");
                                        if (cr.getRate() != 0) {
                                            float newLocalCurrency = Calculations.CalculateNewPrice(placePrice, cr);
                                            priceAdditionFav += newLocalCurrency;
                                            priceFav = ("(" + fav + ")" + df.format(newLocalCurrency) + " ");
                                        } else {
                                            priceFav = ("Unable to get currency data ");
                                        }
                                    }
                                }
                            }
                        }

                        msg+= "<b>Name:</b> " + s + "<br>" + "<b>Local Price:</b> " + priceNormal +
                                "<br><b>Favourite Currency:</b> " + priceFav + "<br><br>";
                        looper++;
                    }
                    textView_main.setText(Html.fromHtml(msg));
                    textView_main.setTextSize(16);

                    //get details about currency rates and date it was added
                    float localRate = 0;
                    float favRate = 0;

                    //get rate for local cuurrency rate
                    localRate = Calculations.CalculateLocalRate(localCurrencyRates, fav);
                    favRate = Calculations.CalculateFavRate(favCurrencyRates, local);

                    //split string for date and time
                    String[] favSeperated = favCurrencyRates.getDateModified().split(" ");
                    String[] localSeperated = localCurrencyRates.getDateModified().split(" ");
                    String exchganged;

                    exchganged = ProcessExchangeRates(localRate, favRate, favSeperated);


                    DecimalFormat df = new DecimalFormat("00.00");
                    String msgCalc = Calculations.ProcessCostString(priceAdditionNormal, priceAdditionFav, df, local, fav);

                    textView_calc.setText(Html.fromHtml(msgCalc + exchganged));
                    textView_calc.setTextSize(16);

                }else{
                    textView_main.setText("No Records Found");
                }
                break;
            case R.id.menu_fav:
                QueryDBMode(3);
                looper = 0;

                //loop through both strings as long as they are not empty
                if(array.length != 0 || price.length != 0){
                    msg += "<b><u>Favourites</u></b>" + "<br><br>";
                    //get set local price first
                    for (String s: array){
                        //add the string to string object to p[ut in text view
                        priceNormal = "";
                        priceFav = "";

                        //set up local price - get oringinal price first
                        float placePrice = Float.parseFloat(price[looper]);

                        if(placePrice == 0){
                            priceNormal = "FREE ";
                            priceFav = "FREE ";
                            priceAdditionNormal += 0;
                        }else {
                            priceNormal = "(" + local + ")" + price[looper] + " ";
                            priceAdditionNormal += placePrice;
                        }

                        //set price
                        if(placePrice == 0){
                            priceNormal =  "(" + local + ")" + "FREE ";
                            priceFav = "(" + fav + ")" + "FREE ";
                            priceAdditionFav += 0;
                        }else {
                            //if the local price is GBP jsut set the same as these are places in the uk
                            if (fav.equals(local)) {
                                //set price
                                DecimalFormat df = new DecimalFormat("00.00");
                                priceAdditionFav += placePrice;
                                priceFav = ("(" + fav + ")" + df.format(placePrice));
                            } else {
                                //loop through each currency to find GBP
                                for (CurrencyRate cr : localCurrencyRates.getCurrencyRates()) {
                                    if (cr.getType().equals(fav)) {
                                        DecimalFormat df = new DecimalFormat("00.00");
                                        if (cr.getRate() != 0) {
                                            float newLocalCurrency = Calculations.CalculateNewPrice(placePrice, cr);
                                            priceAdditionFav += newLocalCurrency;
                                            priceFav = ("(" + fav + ")" + df.format(newLocalCurrency) + " ");
                                        } else {
                                            priceFav = ("Unable to get currency data ");
                                        }
                                    }
                                }
                            }
                        }

                        msg+= "<b>Name:</b> " + s + "<br>" + "<b>Local Price:</b> " + priceNormal +
                                "<br><b>Favourite Currency:</b> " + priceFav + "<br><br>";
                        looper++;
                    }
                    textView_main.setText(Html.fromHtml(msg));
                    textView_main.setTextSize(16);

                    //get details about currency rates and date it was added
                    float localRate = 0;
                    float favRate = 0;

                    //get rate for local cuurrency rate
                    localRate = Calculations.CalculateLocalRate(localCurrencyRates, fav);
                    favRate = Calculations.CalculateFavRate(favCurrencyRates, local);

                    //split string for date and time
                    String[] favSeperated = favCurrencyRates.getDateModified().split(" ");
                    String[] localSeperated = localCurrencyRates.getDateModified().split(" ");
                    String exchganged;

                    exchganged = ProcessExchangeRates(localRate, favRate, favSeperated);


                    DecimalFormat df = new DecimalFormat("00.00");
                    String msgCalc = Calculations.ProcessCostString(priceAdditionNormal, priceAdditionFav, df, local, fav);

                    textView_calc.setText(Html.fromHtml(msgCalc + exchganged));
                    textView_calc.setTextSize(16);

                }else{
                    textView_main.setText("No Records Found");
                }
                break;
            default:
                break;
        }

        return true;
    }

    @NonNull
    public String ProcessExchangeRates(float localRate, float favRate, String[] favSeperated) {
        String exchganged;
        exchganged = "<br><br><b><u>Exchange Rates</u></b><br><br>";
        exchganged += ("(" + local + "): " + "1 - (" + fav + "): " + localRate + "<br>");
        exchganged += ("(" + fav + "): " + "1 - (" + local + "): " + favRate+ "<br>");
        exchganged += ("Time Updated: " + favSeperated[0]+ "<br>");
        exchganged += ("Date Updated: " + favSeperated[1]+ "<br>");
        return exchganged;
    }

    //method to query for the different modes
    public void QueryDBMode(int index) {

        //create database helper
        PlaceDBOpenHelper helper = new PlaceDBOpenHelper(getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();

        //set values for queries seperate as makes it easier to read
        String table = PlaceDBOpenHelper.PLACES_TABLE_NAME;
        String[] colums = {"*"};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;
        String selection;
        String[] selctionArgs;

        switch (index) {
            case 1: // case for to show date planned only
                selection = PlaceDBOpenHelper.KEY_DATEPLANNED + " != ? AND date_visited is null";
                selctionArgs = new String[]{"''"};
                orderBy = PlaceDBOpenHelper.KEY_DATEPLANNED + " ASC";
                break;
            case 2: //case to show date visited only
                selection = PlaceDBOpenHelper.KEY_DATEVISITED + " !=? ";
                selctionArgs = new String[]{"''"};
                orderBy = PlaceDBOpenHelper.KEY_DATEVISITED + " ASC";
                break;
            case 3: //case to show favourite only
                selection = PlaceDBOpenHelper.KEY_FAVORITE + " =? ";
                selctionArgs = new String[]{"1"};
                orderBy = PlaceDBOpenHelper.KEY_RANK + " DESC";
                break;
            default:
                selection = null;
                selctionArgs = null;
                break;
        }

        //actual query to the database
        Cursor cursor = db.query(table, colums, selection, selctionArgs, groupBy, having,
                orderBy, limit);

        Log.d("queryCursorLength", String.valueOf(cursor.getCount()));

        //check to see if theres any records
        if (cursor.getCount() < 1) {
            array = new String[cursor.getCount()];
            price = new String[cursor.getCount()];
            cursor.close();
        } else {
            array = new String[cursor.getCount()];
            price = new String[cursor.getCount()];
            int i = 0;


            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                array[i] = cursor.getString(1);
                price[i] = cursor.getString(6);
                Log.d("queryCursor", cursor.getString(1));
                Log.d("queryCursor", cursor.getString(6));
                i++;
                cursor.moveToNext();
            }
            cursor.close();
        }
        cursor.close();
    }
}
