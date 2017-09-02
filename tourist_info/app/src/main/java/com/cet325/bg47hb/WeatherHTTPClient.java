package com.cet325.bg47hb;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class WeatherHTTPClient {
    private static String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?q=";
    private static String IMG_URL = "http://openweathermap.org/img/w/";
    private static String API_KEY = "6a33c4da9acfd81fe8150f40fa1a657e";

    public String getWeatherData(String location) {
        HttpURLConnection con = null ;
        InputStream is = null;
        String urlString = "";

        try {
            // create URL for specified city and metric units (Celsius)
            urlString = BASE_URL + URLEncoder.encode(location, "UTF-8") + "&units=metric&APPID=" + API_KEY;
            Log.d("urlString",urlString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            con = (HttpURLConnection) (new URL(urlString)).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(true);
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
                return null;
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try { is.close(); } catch(Exception e) {}
            try { con.disconnect(); } catch(Exception e) {}
        }

        return null;
    }

    public Bitmap getImage(String code) {

        URL url = null;
        Bitmap bmp = null;
        try {
            url = new URL(IMG_URL + code + ".png");
            Log.d("urlImage",IMG_URL + code + ".png");
            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            return bmp;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
