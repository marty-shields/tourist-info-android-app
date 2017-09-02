package com.cet325.bg47hb;

import org.junit.Assert;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CalculationsClassTests {
    //set up values
    String local = "GBP";
    String fav = "EUR";
    List<CurrencyRate>crs = new ArrayList<CurrencyRate>();
    CurrencyRate cr;
    CurrencyRate cr1;
    CurrencyRate cr2;
    CurrencyRates currencyRates = new CurrencyRates();

    @Test
    public void testProcessCostString(){
        //set up values
        float priceAdditionNormal = 1;
        float priceAdditionFav = 2;
        DecimalFormat df = new DecimalFormat("00.00");
        String local = "testLocal";
        String fav = "testFav";

        //create actual val
        String actual = Calculations.ProcessCostString(priceAdditionNormal, priceAdditionFav, df, local, fav);

        //what is exprected
        String expected = "<b><u>Total Costs</u></b><br><br><b>Local Currency:</b> (testLocal)01.00<br>" +
                "<b>Favourite Currency:</b> (testFav)02.00";

        //assert they are the same
        assertEquals("String did not come together the same in the method", expected, actual);
    }

    @Test
    public void testCalculateFavRate(){
        //set up values
        cr = new CurrencyRate();
        cr.setRate(10);
        cr.setType("EUR");
        crs.add(cr);

        cr1 = new CurrencyRate();
        cr1.setRate(3);
        cr1.setType("GBP");
        crs.add(cr1);

        cr2 = new CurrencyRate();
        cr2.setRate(5);
        cr2.setType("USD");
        crs.add(cr2);

        currencyRates.setCurrencyRates(crs);

        //create actual val
        float actual = Calculations.CalculateFavRate(currencyRates, local);

        //what is exprected
        float expected = 3;

        //assert they are the same
        assertEquals("Did not bring back correct rate from the list of classes", expected, actual, 0.1);
    }

    @Test
    public void testCalculateLocalRate() {
        //set up values

        cr = new CurrencyRate();
        cr.setRate(2);
        cr.setType("EUR");
        crs.add(cr);

        cr1 = new CurrencyRate();
        cr1.setRate(3);
        cr1.setType("GBP");
        crs.add(cr1);

        cr2 = new CurrencyRate();
        cr2.setRate(1);
        cr2.setType("USD");
        crs.add(cr2);

        currencyRates.setCurrencyRates(crs);

        //create actual val
        float actual = Calculations.CalculateLocalRate(currencyRates, fav);

        //what is exprected
        float expected = 2;

        //assert they are the same
        assertEquals("Did not bring back correct rate from the list of classes", expected, actual, 0.1);
    }

    @Test
    public void testCurrencyRateClassIsSavingRate(){
        cr = new CurrencyRate();
        cr.setRate(2);

        //float actual
        float actual = cr.getRate();

        //geet expected
        float expected = 2;

        Assert.assertEquals("Rates are not the same and have not been saved", expected, actual, 0.1);
    }

    @Test
    public void testCurrencyRateClassIsSavingType(){
        cr = new CurrencyRate();
        cr.setType("TEST");

        //float actual
        String actual = cr.getType();

        //geet expected
        String expected = "TEST";

        Assert.assertEquals("Rates are not the same and have not been saved", expected, actual);
    }

    @Test
    public void testCalculateNewPrice(){
        cr = new CurrencyRate();
        cr.setType("EUR");
        cr.setRate(2);
        float placePrice = 10;


        //get the actual result
        float actual = Calculations.CalculateNewPrice(placePrice, cr);

        //set up the expected
        float expected = 20;

        //assert
        Assert.assertEquals("the calculation has not gone through correctly, it is supposed to * " +
                "each other", expected, actual, 0.1);
    }

    @Test
    public void test1Test(){
        assertEquals("both arnt the same", true, true);
    }
}
