package com.cet325.bg47hb;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONCurrencyParser {
    public static CurrencyRate getRate(String data, String cRate, String cSelected) throws JSONException {
        CurrencyRate currencyRate = new CurrencyRate();

        try {
            if(cRate.equals(cSelected)){
                currencyRate.setType(cSelected);
                currencyRate.setRate(1);
            }else{
                JSONObject jObj = new JSONObject(data);
                JSONObject rateObj = jObj.getJSONObject("rates");
                String currencyR = rateObj.getString(cRate);
                Log.d("CURRENCYRATE",String.valueOf(currencyR));
                currencyRate.setType(cRate);
                currencyRate.setRate(Float.parseFloat(String.valueOf(currencyR)));
            }
        }catch (Exception e){e.printStackTrace();}

        return currencyRate;
    }
}
