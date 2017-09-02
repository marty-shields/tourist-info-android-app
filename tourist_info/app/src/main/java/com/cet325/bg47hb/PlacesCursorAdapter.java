package com.cet325.bg47hb;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONException;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class PlacesCursorAdapter extends CursorAdapter{

    private int selection;
    private String local, fav;
    TextView geolocation;

    public PlacesCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.place_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        //choose what to show first (i.e ordered by favourites
        selection = PlacesActivity.getSELECTION();

        TextView textViewdatePlannedTitle = (TextView)view.findViewById(R.id.textView_title_placePlanned);
        TextView textViewdateVisitedTitle = (TextView)view.findViewById(R.id.textView_title_placeVisited);
        TextView textViewnotesTitle = (TextView)view.findViewById(R.id.textView_title_notes);
        TextView textViewdatePlanned = (TextView)view.findViewById(R.id.textView_placePlanned);
        TextView textViewdateVisited = (TextView)view.findViewById(R.id.textView_placeVisited);
        TextView textViewFavPriceTitle = (TextView)view.findViewById(R.id.textView_title_favPrice);
        TextView textViewFavPrice = (TextView)view.findViewById(R.id.textView_favPrice);
        TextView textViewnotes = (TextView)view.findViewById(R.id.textView_placeNotes);

        if (selection == 0){
            //make these items go away
            textViewdatePlanned.setVisibility(View.GONE);
            textViewdateVisited.setVisibility(View.GONE);
            textViewnotes.setVisibility(View.GONE);
            textViewdatePlannedTitle.setVisibility(View.GONE);
            textViewdateVisitedTitle.setVisibility(View.GONE);
            textViewnotesTitle.setVisibility(View.GONE);
            textViewFavPriceTitle.setVisibility(View.GONE);
            textViewFavPrice.setVisibility(View.GONE);
        } else{
            PlacesActivity pa = new PlacesActivity();
            String placePlanned = cursor.getString(cursor.getColumnIndex
                    (PlaceDBOpenHelper.KEY_DATEPLANNED));
            String placeVisited = cursor.getString(cursor.getColumnIndex
                    (PlaceDBOpenHelper.KEY_DATEVISITED));
            String placeNotes = cursor.getString(cursor.getColumnIndex
                    (PlaceDBOpenHelper.KEY_NOTES));

            if (placePlanned != null){

            }else{
                placePlanned = "Not Planned";
            }

            if (placeVisited != null){

            }else{
                placeVisited = "Not Visited";
            }

            if (placeNotes != null && !placeNotes.equals("") && placeNotes.length() != 0){
                if (placeNotes.length() > 84) {
                    placeNotes = placeNotes.substring(0, 83);
                    placeNotes += "...";
                }

            }else{
                placeNotes = "No Notes";
            }

            //get local and fav currency



            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences
                    (PlacesActivity.getAppContext());

            local = sharedPref.getString("localCurrency", "DEFAULT");
            fav = sharedPref.getString("favCurrency", "DEFAULT");

            //set up local price - get oringinal price first
            float placePrice = Float.parseFloat(cursor.getString(cursor.getColumnIndex
                    (PlaceDBOpenHelper.KEY_PRICE)));

            //set price
            if(placePrice == 0){
                textViewFavPrice.setText("(" + fav + ") " + "FREE");
            }else {
                //if the local price is GBP jsut set the same as these are places in the uk
                if (fav.equals(local)) {
                    //set price
                    DecimalFormat df = new DecimalFormat("00.00");
                    textViewFavPrice.setText("(" + fav + ") " + df.format(placePrice));
                } else {
                    //get the local currency rates
                    CurrencyRates currencyRates = PlacesActivity.getLocalCurrencyRates();

                    //loop through each currency to find GBP
                    for (CurrencyRate cr : currencyRates.getCurrencyRates()) {
                        if (cr.getType().equals(fav)) {
                            DecimalFormat df = new DecimalFormat("00.00");
                            if (cr.getRate() != 0) {
                                float newLocalCurrency = placePrice * cr.getRate();
                                textViewFavPrice.setText
                                        ("(" + fav + ") " + df.format(newLocalCurrency));
                            } else {
                                textViewFavPrice.setText("Unable to get currency data");
                            }
                        }
                    }
                }
            }
            //link text views to text
            textViewdatePlanned.setText(placePlanned);
            textViewdateVisited.setText(placeVisited);
            textViewnotes.setText(placeNotes);
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences
                (PlacesActivity.getAppContext());

        local = sharedPref.getString("localCurrency", "DEFAULT");
        fav = sharedPref.getString("favCurrency", "DEFAULT");

        //link strings to database items
        String placeName = cursor.getString(cursor.getColumnIndex(PlaceDBOpenHelper.KEY_NAME));
        String placeLocation = cursor.getString
                (cursor.getColumnIndex(PlaceDBOpenHelper.KEY_LOCATION));
        String placeLongi = cursor.getString
                (cursor.getColumnIndex(PlaceDBOpenHelper.KEY_LONGITUDE));
        String placeLati = cursor.getString
                (cursor.getColumnIndex(PlaceDBOpenHelper.KEY_LATITUDE));
        String placeDescription = cursor.getString
                (cursor.getColumnIndex(PlaceDBOpenHelper.KEY_DESCRIPTION));
        String imageDesc = cursor.getString
                (cursor.getColumnIndex(PlaceDBOpenHelper.KEY_IMAGE));
        Log.d("imageString", imageDesc);
        String placeRank = cursor.getString(cursor.getColumnIndex(PlaceDBOpenHelper.KEY_RANK));
        float placePrice = Float.parseFloat(cursor.getString(cursor.getColumnIndex
                (PlaceDBOpenHelper.KEY_PRICE)));

        //set up float for rank for half ranks and decimal format to get price shown correctly
        float rank = Float.parseFloat(placeRank);

        //cut off the desciption as long as its not null
        if(placeDescription != null) {
            if (placeDescription.length() > 84) {
                placeDescription = placeDescription.substring(0, 83);
                placeDescription += "...";
            }
        }else{
            placeDescription = "No Description Set";
        }


        //set text views to strings created
        ImageView img = (ImageView)view.findViewById(R.id.imageView_placeIcon);
        TextView name = (TextView)view.findViewById(R.id.textView_placeName);
        TextView location = (TextView)view.findViewById(R.id.textView_placeLocation);
        geolocation = (TextView)view.findViewById(R.id.textView_placeGeo);
        TextView description = (TextView)view.findViewById(R.id.textView_placeDesc);
        TextView price = (TextView)view.findViewById(R.id.textView_placePrice);
        RatingBar rating = (RatingBar)view.findViewById(R.id.ratingBar_rank);

        //see if image string is null
        if (cursor.getString(cursor.getColumnIndex(PlaceDBOpenHelper.KEY_IMAGE)) != null){
            String placeImage = cursor.getString(cursor.getColumnIndex(PlaceDBOpenHelper.KEY_IMAGE));
            final File file = new File(context.getExternalFilesDir
                    (Environment.DIRECTORY_PICTURES), placeImage);
            if(file.exists()) {
                Log.d("imageloaded", file.toString());
                Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
                image = Bitmap.createScaledBitmap(image, 300, 300, false);
                img.setImageBitmap(image);
            }else{
                img.setImageResource(R.drawable.image_default);
            }
        }else{
            img.setImageResource(R.drawable.image_default);
        }

        //set values to text boxes etc...
        name.setText(placeName);
        location.setText(placeLocation);
        description.setText(placeDescription);
        rating.setRating(rank);

        Log.d("PlaceLongi", placeName + placeLongi);


        if (placeLongi == null || placeLongi.equals("") || placeLongi.length() == 0) {

            geolocation.setText("Latitude: Loading...\nLongitude: Loading...");

            JSONWeatherTask task = new JSONWeatherTask();

            task.execute(new String[]{placeLocation});
        }else{
            //set geoloaction based on split
            geolocation.setText("Latitude: " + placeLati + "\nLongitude: " + placeLongi); //if geo is set right
        }

        //set price
        if(placePrice == 0){
            price.setText("(" + local + ") FREE");
        }else{
            DecimalFormat df = new DecimalFormat("00.00");
            price.setText("(GBP) " + df.format(placePrice));
        }
    }

    private class JSONWeatherTask extends AsyncTask<String, Void, Location> {

        @Override
        protected Location doInBackground(String... params) {
            Log.d("data", params[0]);
            Location loc = new Location();
            Log.d("urlString",params[0]);
            String data = ((new WeatherHTTPClient()).getWeatherData(params[0]));
            if (data != null) {
                try {
                    loc = JSONWeatherParser.getGeo(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return loc;
            }
            else return null;
        }

        @Override
        protected void onPostExecute(Location loc) {
            super.onPostExecute(loc);
            if (loc!=null) {
                DecimalFormat df = new DecimalFormat("00.00000");
                geolocation.setText("Latitude: " + String.valueOf(df.format(loc.getLatitude())) +
                        "\nLongitude: " + String.valueOf(df.format(loc.getLongitude())));
            }
            else
            {
                geolocation.setText("Latitude: Issues estimating\nLongitude: Issues estimating");
            }
        }
    }


}
