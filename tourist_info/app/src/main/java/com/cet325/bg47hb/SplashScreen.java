package com.cet325.bg47hb;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SplashScreen extends AppCompatActivity {

    //duration of the splash screen
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity

                storagePermission();

                if(ContextCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED){
                    //preload the places with 10 places -- we need to make sure we only pre load the first time
                    if (getContentResolver().query(PlaceProvider.CONTENT_URI, null, null, null, null).getCount() != 0) {
                        //get local and fav currency
                        SharedPreferences sharedPref =
                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                        String local = sharedPref.getString("localCurrency", "DEFAULT");
                        String fav = sharedPref.getString("favCurrency", "DEFAULT");

                        //get currency stuff
                        String[] rates = getResources().getStringArray(R.array.rates);

                        CurrencyRates localCurrencyRates = new CurrencyRates();

                        JSONCurrencyTask task = new JSONCurrencyTask();

                        localCurrencyRates.setCurrencyRates
                                (task.requestCurrencyRates(rates, local));

                        DateFormat df = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
                        String currentDateTimeSetting = df.format(new Date());
                        localCurrencyRates.setDateModified(currentDateTimeSetting);
                        Log.d("currentdate", localCurrencyRates.getDateModified());

                        for (CurrencyRate rate : localCurrencyRates.getCurrencyRates()) {
                            Log.d("OBJECTLOCAL", "THIS IS FROM THE OBJECT");
                            Log.d("Currency", rate.getType());
                            Log.d("Rate", String.valueOf(rate.getRate()));
                        }

                        CurrencyRates favCurrencyRates = new CurrencyRates();

                        task = new JSONCurrencyTask();

                        favCurrencyRates.setCurrencyRates
                                (task.requestCurrencyRates(rates, fav));

                        df = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
                        currentDateTimeSetting = df.format(new Date());
                        favCurrencyRates.setDateModified(currentDateTimeSetting);
                        Log.d("currentdate", favCurrencyRates.getDateModified());

                        for (CurrencyRate rate : favCurrencyRates.getCurrencyRates()) {
                            Log.d("OBJECTFAV", "THIS IS FROM THE OBJECT");
                            Log.d("Currency", rate.getType());
                            Log.d("Rate", String.valueOf(rate.getRate()));
                        }

                        Intent i = new Intent(SplashScreen.this, MainMenu.class);
                        i.putExtra("localCurrency", localCurrencyRates);
                        i.putExtra("favCurrency", favCurrencyRates);
                        startActivity(i);

                        // close this activity
                        finish();
                    } //if the database already has stuff in
                    else {

                    }

            }}
        }, SPLASH_TIME_OUT);
    }

    //method to inset a new place into the database
    public void insertPlace(String name, String location, double price, String description,
                            String lati, String longi, int img, double rating, boolean favorite,
                            String datePlanned, String dateVisited){
        try {
            Thread.sleep(1100); //place
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //create image onto external storage
        //create string name for bitmap
        Bitmap bm = BitmapFactory.decodeResource(getResources(), img);
        bm = Bitmap.createScaledBitmap(bm,600,400,false);


        //create filename
        //create string name for bitmap
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "IMG_" + timeStamp + ".jpg";

        //save file
        final File file = new File(getApplicationContext().getExternalFilesDir
                (Environment.DIRECTORY_PICTURES), filename);
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 20, outStream);
            outStream.flush();
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        //values to insert new record into database
        ContentValues values = new ContentValues();
        values.put(PlaceDBOpenHelper.KEY_NAME, name);
        values.put(PlaceDBOpenHelper.KEY_LOCATION, location);
        values.put(PlaceDBOpenHelper.KEY_PRICE, price);
        values.put(PlaceDBOpenHelper.KEY_DESCRIPTION, description);
        values.put(PlaceDBOpenHelper.KEY_LONGITUDE, longi);
        values.put(PlaceDBOpenHelper.KEY_LATITUDE, lati);
        values.put(PlaceDBOpenHelper.KEY_IMAGE, filename);
        values.put(PlaceDBOpenHelper.KEY_RANK, rating);
        values.put(PlaceDBOpenHelper.KEY_FAVORITE, favorite);
        values.put(PlaceDBOpenHelper.KEY_DATEPLANNED, datePlanned);
        values.put(PlaceDBOpenHelper.KEY_DATEVISITED, dateVisited);

        //get content resolver to insert the database info
        getContentResolver().insert(PlaceProvider.CONTENT_URI, values);

        Log.d("placeadded", file.toString());
    }

    public void preloadPlaces(){
        insertPlace("The Discovery Museum", "Newcastle, UK", 0.00,
                "Discovery Museum is housed in the old Co-operative Wholesale Society building, " +
                        "Blandford House. Opening in 1899, it quickly became an iconic building " +
                        "in Newcastle city centre. Blandford House became a museum in 1978 and " +
                        "was re-launched as Discovery Museum in 1993.", "54.9709441","-1.6236708",
                R.drawable.image1, 4.5, false, null, null);
        insertPlace("The Biscuit Factory", "Newcastle, UK", 0.00, "The Biscuit Factory is the UK’s " +
                "largest art, craft & design gallery set in the heart of Newcastle’s cultural " +
                "quarter. Housed in a former Victorian warehouse, our beautiful gallery spaces " +
                "are set over two floors and display a range of exciting contemporary fine art, " +
                "sculpture, original prints and jewellery, quality craftsmanship and design led " +
                "homewares. The Biscuit Factory hosts four major exhibitions a year, " +
                "representing the work of around 250 artists each season, so there is always " +
                "something new to see every time you visit.", "54.9765774","-1.5976521",
                R.drawable.image2, 4.5, true, "01/01/2017", null);
        insertPlace("Life Centre", "Newcastle, UK", 14.00, "At the heart of the science village is " +
                "the Life Science Centre, which attracts around 250,000 visitors annually. " +
                "Life's public engagement programme attracts a broad audience for exhibitions " +
                "and special events, and the education team deliver the biggest schools' science " +
                "workshop programme in any European museum or science centre.", "54.96849","-1.62136",
                R.drawable.image3, 2.5, true, "05/01/2017", "18/01/2017");
        insertPlace("Durham Cathedral", "Durham, UK", 7.50, "Durham Cathedral is a Christian Church " +
                "of the Anglican Communion, the shrine of St Cuthbert, the seat of the Bishop " +
                "of Durham and a focus of pilgrimage and spirituality in North East England. " +
                "We inhabit a treasured sacred space set in the natural and human landscape of " +
                "the World Heritage Site.", "54.77236","-1.57735", R.drawable.image4, 4.5, false, null, null);
        insertPlace("Beamish Museum", "Beamish, UK", 18.50, "Beamish, the North of England Open " +
                "Air Museum is an open-air museum located at Beamish, near the town of Stanley, " +
                "County Durham, England. The museum's guiding principle is to preserve an " +
                "example of everyday life in urban and rural North East England at the climax " +
                "of industrialisation in the early 20th century.\n\n" + "Much of the restoration " +
                "and interpretation is specific to the late Victorian and Edwardian eras, " +
                "together with portions of countryside under the influence of industrial " +
                "revolution in 1825. On its 350 acres (140 ha) estate it utilises a mixture " +
                "of translocated, original and replica buildings; a huge collection of " +
                "artifacts, working vehicles and equipment; as well as livestock and costumed " +
                "interpreters.", "54.87481","-1.66091", R.drawable.image5, 4.5, true, "21/01/2017", null);
        insertPlace("Tynemouth Park", "Tynemouth, UK", 0.00, "Welcome to Tynemouth Park… for a " +
                "great fun packed day out for the whole family in Tynemouth. Come along, there’s " +
                "loads of things to do!\n\n" + "Home to our friendly, family run, licensed, Clock " +
                "Tower Café, providing super home cooking, snacks, ice creams and refreshments; " +
                "Lost World Adventure Golf, for that fun round of mini-golf; Jungle Wipeout, " +
                "outdoor soft play area including Kamikazi slide; the all new Pirate Quest Maze " +
                "and traditional Boating Lake.\n\n" + "Situated adjacent to Tynemouth’s award " +
                "winning Longsands beach, near to the Blue Reef Aquarium, the park provides the " +
                "perfect stop-off point for visitors heading to the coast.",
                "55.0248973","-1.42990720197518", R.drawable.image6, 4.5, false, null, null);
        insertPlace("Bede's World", "Jarrow, UK", 0.00, "Jarrow Hall - Anglo-Saxon Farm, Village " +
                "and Bede Museum (formerly Bede's World) is a museum in Jarrow, South Tyneside, " +
                "England, re-opening in October 2016 which will celebrate of the life of the " +
                "Venerable Bede; a monk, author and scholar who lived in at the Abbey Church of " +
                "Saint Peter and Saint Paul, Wearmouth-Jarrow, a double monastery at Jarrow and " +
                "Monkwearmouth, (today part of Sunderland), England.\n\n" + "The site will feature " +
                "a museum and other educational services dedicated to the life and times of the " +
                "famous monk, with other features and attractions - including a reconstructed " +
                "Anglo-Saxon farm and the 18th Century Georgian building Jarrow Hall itself - " +
                "reflected in a calendar of activities, including special themed events, an " +
                "educational programme for schools and heritage skills workshops, alongside space " +
                "for businesses and events.[1]", "54.98151","-1.47457", R.drawable.image7, 5, false, null, null);
        insertPlace("Blue Reef Aquarium", "Tynemouth, UK", 10.50, "Blue Reef Aquarium in " +
                "Tynemouth is an aquarium located in Tynemouth, England. It is home to over 40 " +
                "living displays, from tropical sharks and lobsters to seahorses and tropical " +
                "fish.\n\n" + "At the Aquarium’s heart is a large ocean tank where an underwater " +
                "walkthrough tunnel offers close encounters with the tropical coral reef fish.\n\n" +
                "Other displays are home to giant Pacific octopus, poison dart frogs, nautilus, " +
                "toxic toads, turtles, terrapins, and otters.\n\n" + "In Easter 2007 the aquarium " +
                "opened \"Seal Cove\". The naturally-themed 500,000-litre pool includes rocky " +
                "haul out areas and underwater caves along with other environmental enrichment " +
                "features to ensure the seals are kept in near natural conditions. Large viewing " +
                "panels and a ramped walkway provide visitors with a unique opportunity to " +
                "admire their agility from both above and below the waterline.",
                "55.0286611","-1.429773", R.drawable.image8, 3, true, "18/01/2017", "23/01/2017");
        insertPlace("Heugh Battery Museum", "Hartlepool, UK", 6.00, "The Heugh Battery Museum is " +
                "situated on the Headland in Hartlepool. This area of the North East coast is " +
                "rich in history and the museum aims to educate its visitors on part of that " +
                "history. The First World War touched many lives but on 16th December 1914 " +
                "Hartlepool suffered a bombardment which would be remembered by many over 90 " +
                "years later.\n\n" + "The museum encourages people to understand war and conflict" +
                " and the impact it has both on those at the Front but also those who are left " +
                "behind at home. The venue is divided across three levels with the underground " +
                "magazines, the parade ground and main museum with the Observation Point tower " +
                "providing panoramic views of the North Sea and surrounding coast line.",
                "54.69647","-1.17756", R.drawable.image9, 1, false, null, null);
        insertPlace("Baltic Centre", "Gateshead, UK", 0.00, "Situated on the south bank of the " +
                "River Tyne in Gateshead, England, BALTIC Centre for Contemporary Art consists " +
                "of 2,600 square metres of art space, making it the UK’s largest dedicated " +
                "contemporary art institution.\n\n" + "BALTIC’s mission is to create exceptional " +
                "access to important and innovative contemporary art in a unique setting, that " +
                "encourages and enables learning and transformational thinking.\n" + "BALTIC " +
                "has gained an international reputation for its commissioning of cutting-edge " +
                "temporary exhibitions. It has presented the work of over 350 artists from over " +
                "50 countries in over 190 exhibitions to date. Since opening to the public in " +
                "July 2002, BALTIC has welcomed over 6 million visitors.", "54.96918","-1.59734",
                R.drawable.image10, 2, false, null, null);
        createDefaultImage();
        createSharePrefs();
        Log.d("database init", "complete");
    }
    public void createSharePrefs(){
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString
                ("localCurrency", "GBP").commit();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString
                ("favCurrency", "EUR").commit();
    }

    //check to see if we have permission to write to storage
    public void storagePermission(){
        //check to see if we have permission for the activity
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    //callback to see if we have permission granted or denied
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    // permission was granted, yay!
                    //preload the places with 10 places -- we need to make sure we only pre load the first time
                    if (getContentResolver().query(PlaceProvider.CONTENT_URI, null, null, null, null).getCount() != 0) {
                        //database is not empty
                    } //if the database already has stuff in
                    else {
                        //if first time loading database
                        preloadPlaces();
                        //get local and fav currency
                        SharedPreferences sharedPref =
                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                        String local = sharedPref.getString("localCurrency", "DEFAULT");
                        String fav = sharedPref.getString("favCurrency", "DEFAULT");

                        //get currency stuff
                        String[] rates = getResources().getStringArray(R.array.rates);

                        CurrencyRates localCurrencyRates = new CurrencyRates();

                        JSONCurrencyTask task = new JSONCurrencyTask();

                        localCurrencyRates.setCurrencyRates
                                (task.requestCurrencyRates(rates, local));

                        DateFormat df = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
                        String currentDateTimeSetting = df.format(new Date());
                        localCurrencyRates.setDateModified(currentDateTimeSetting);
                        Log.d("currentdate", localCurrencyRates.getDateModified());

                        for (CurrencyRate rate : localCurrencyRates.getCurrencyRates()) {
                            Log.d("OBJECTLOCAL", "THIS IS FROM THE OBJECT");
                            Log.d("Currency", rate.getType());
                            Log.d("Rate", String.valueOf(rate.getRate()));
                        }

                        CurrencyRates favCurrencyRates = new CurrencyRates();

                        task = new JSONCurrencyTask();

                        favCurrencyRates.setCurrencyRates
                                (task.requestCurrencyRates(rates, fav));

                        df = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
                        currentDateTimeSetting = df.format(new Date());
                        favCurrencyRates.setDateModified(currentDateTimeSetting);
                        Log.d("currentdate", localCurrencyRates.getDateModified());

                        for (CurrencyRate rate : favCurrencyRates.getCurrencyRates()) {
                            Log.d("OBJECTFAV", "THIS IS FROM THE OBJECT");
                            Log.d("Currency", rate.getType());
                            Log.d("Rate", String.valueOf(rate.getRate()));
                        }

                        CurrencyRates GBcurrencyRates = new CurrencyRates();

                        task = new JSONCurrencyTask();

                        GBcurrencyRates.setCurrencyRates
                                (task.requestCurrencyRates(rates, fav));

                        df = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
                        currentDateTimeSetting = df.format(new Date());
                        GBcurrencyRates.setDateModified(currentDateTimeSetting);
                        Log.d("currentdate", GBcurrencyRates.getDateModified());

                        for (CurrencyRate rate : GBcurrencyRates.getCurrencyRates()) {
                            Log.d("OBJECTFAV", "THIS IS FROM THE OBJECT");
                            Log.d("Currency", rate.getType());
                            Log.d("Rate", String.valueOf(rate.getRate()));
                        }

                        Intent i = new Intent(SplashScreen.this, MainMenu.class);
                        i.putExtra("localCurrency", localCurrencyRates);
                        i.putExtra("favCurrency", favCurrencyRates);
                        i.putExtra("GBCurrency", GBcurrencyRates);
                        startActivity(i);

                        // close this activity
                        finish();
                    }

                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    storagePermission();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public boolean createDefaultImage(){
        //create image onto external storage
        //create string name for bitmap
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.image_default);
        bm = Bitmap.createScaledBitmap(bm,500,500,false);
        Log.d("defaultImage", "started process");

        //create filename
        String filename = "IMG_default.jpg";

        //save file
        final File file = new File(getApplicationContext().getExternalFilesDir
                (Environment.DIRECTORY_PICTURES), filename);
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 20, outStream);
            outStream.flush();
            outStream.close();
            Log.d("defaultImage", "saved file");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
        Log.d("placeadded", file.toString());
        return true;
    }
}
