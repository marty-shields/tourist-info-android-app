package com.cet325.bg47hb;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class CurrencyConverterHttpClient {
    private String url = "http://api.fixer.io/latest?base=";
    private String urlString;

    public String getCurrencyData(String rate){
        HttpURLConnection con = null ;
        InputStream is = null;

        //try to create the URL string
        try{
            String query = URLEncoder.encode(rate, "utf-8");
            urlString = "http://api.fixer.io/latest?base=" + query;
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            con = (HttpURLConnection)(new URL(urlString)).openConnection();
            con.setRequestMethod("GET");
            con.connect();
            int response = con.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                // Let's read the response
                StringBuilder buffer = new StringBuilder();
                is = con.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = br.readLine()) != null) {
                    Log.d("JSON-line",line);
                    buffer.append(line + "\r\n");
                }
                is.close();
                con.disconnect();
                Log.d("JSON",buffer.toString());
                return buffer.toString();
            }
            else {
                Log.d("HttpURLConnection","Unable to connect");
            }

        }catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try { is.close(); } catch(Exception e) {}
            try { con.disconnect(); } catch(Exception e) {}
        }
        return null;
    }
}
