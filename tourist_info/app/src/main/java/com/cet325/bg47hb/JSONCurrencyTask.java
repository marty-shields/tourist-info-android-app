package com.cet325.bg47hb;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class JSONCurrencyTask extends AsyncTask<String, Void, CurrencyRate>{

    @Override
    protected CurrencyRate doInBackground(String... params) {
        Log.d("JSONDATA", params[0] + " " + params[1]);
        CurrencyRate currencyRate = new CurrencyRate();
        String data = ((new CurrencyConverterHttpClient()).getCurrencyData(params[1]));
        if (data != null) {
            try {
                currencyRate = JSONCurrencyParser.getRate(data, params[0], params[1]);
            } catch (JSONException e) {
                e.printStackTrace();
                currencyRate.setType(params[0]);
                currencyRate.setRate(0);
            }
            Log.d("NEWCURRENCYNAME", currencyRate.getType());
            Log.d("NEWCURRENCYRATE", String.valueOf(currencyRate.getRate()));
            return currencyRate;
        }

        currencyRate.setType(params[0]);
        currencyRate.setRate(0);
        return currencyRate;
    }

    @Override
    protected void onPostExecute(CurrencyRate currencyRate) {
        //do stuff
        myMethod(currencyRate);
    }

    private CurrencyRate myMethod(CurrencyRate myValue) {
        //handle value
        return myValue;
    }
    public List<CurrencyRate> requestCurrencyRates(String[] rates, String set){
        //create list object for the currency rates
        List<CurrencyRate> currencyRates = new ArrayList<CurrencyRate>();

        for(String rate : rates){
            //add each currency to the list and get the currency data
            JSONCurrencyTask task = new JSONCurrencyTask();
            task.execute(new String[]{rate, set});
            try {
                CurrencyRate currencyRate = task.get();
                currencyRates.add(currencyRate);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return currencyRates;
    }
}
