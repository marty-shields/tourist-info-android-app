package com.cet325.bg47hb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CurrencyRates implements Serializable{
    private List<CurrencyRate>currencyRates;
    private String dateModified;

    public void setCurrencyRates(List<CurrencyRate> currencyRates) {
        this.currencyRates = currencyRates;
    }

    public List<CurrencyRate> getCurrencyRates() {
        return currencyRates;
    }

    public String getDateModified() {
        return dateModified;
    }

    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }

    public CurrencyRates(){
        currencyRates = new ArrayList<CurrencyRate>();
    }
}
