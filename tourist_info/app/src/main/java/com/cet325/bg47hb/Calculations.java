package com.cet325.bg47hb;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.DecimalFormat;

/**
 * Created by Martin on 04/01/2017.
 */
public class Calculations {

    public static String ProcessCostString(float priceAdditionNormal, float priceAdditionFav,
                                           DecimalFormat df, String local, String fav) {
        return "<b><u>Total Costs</u></b>" + "<br><br>" + "<b>Local Currency:</b> (" + local + ")" +
                String.valueOf(df.format(priceAdditionNormal)) + "<br>" +
                "<b>Favourite Currency:</b> (" + fav + ")" + String.valueOf
                (df.format(priceAdditionFav));
    }

    public static float CalculateLocalRate(CurrencyRates cr, String fav) {
        float localRate = 0;
        //get rate for local cuurrency rate
        for (CurrencyRate currencyRate : cr.getCurrencyRates()) {
            if (currencyRate.getType().equals(fav)) {
                localRate = currencyRate.getRate();
            }
        }
        return localRate;
    }

    public static float CalculateFavRate(CurrencyRates cr, String local) {
        float favRate = 0;
        for (CurrencyRate currencyRate : cr.getCurrencyRates()) {
            if (currencyRate.getType().equals(local)) {
                favRate = currencyRate.getRate();
            }
        }
        return favRate;
    }

    public static float CalculateNewPrice(float placePrice, CurrencyRate cr) {
        return placePrice * cr.getRate();
    }

}