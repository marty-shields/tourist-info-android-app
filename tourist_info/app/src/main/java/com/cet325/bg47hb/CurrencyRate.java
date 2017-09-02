package com.cet325.bg47hb;

import java.io.Serializable;

/**
 * Created by Martin on 02/01/2017.
 */
public class CurrencyRate implements Serializable{

    private String type;
    private float rate;

    public String getType(){
        return type;
    }
    public void setType(String type){
        this.type = type;
    }
    public float getRate(){
        return rate;
    }
    public void setRate(float rate){
        this.rate = rate;
    }

}
