package com.cet325.bg47hb;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PlacesDetailView extends AppCompatActivity {

    private String id, name, description, rank, image, planned, visited, price, notes;
    private String location, longi, lati, oldImage, prefFav, prefLocal;
    private int fav, index;
    private int PICK_PHOTO_FOR_AVATAR = 0;
    private int REQUEST_IMAGE_CAPTURE = 1;
    private float rate;
    Calendar myCalendar = Calendar.getInstance();;
    CheckBox editVisited, editPlanned, checkFav;
    TextView textviewVisited, textviewPlanned, textviewImage;
    RatingBar rateRank;
    Button imageButton;
    EditText editName, editlocation, editLong, editLat, editPrice, editDesc, editNotes;
    Bitmap bmp;
    private TextView cityText;
    private TextView condDesc;
    private TextView temp;
    private TextView press;
    private TextView windSpeed;
    private TextView windDeg;
    private TextView titleTemp;
    private TextView titlePress;
    private TextView titleWindSpeed;
    private TextView titleHum;
    private TextView hum;
    private ImageView imgView;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (index > 10) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.detail_page_menu, menu);
        } else{
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.detail_page_menu2, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        //see if the edit button or the delete is clicked
        switch (id){
            case R.id.menu_placeEdit:
                //create alert dialog confirming edit
                editAction();
                break;
            case R.id.menu_placeAdjust:
                //create alert dialog confirming edit
                adjustAction();
                break;
            case R.id.menu_placeDelete:
                    deleteAction();
                break;
            case R.id.menu_goBack:
                Intent i = new Intent(this, PlacesActivity.class);
                i.putExtra("localCurrency", PlacesActivity.getLocalCurrencyRates());
                i.putExtra("favCurrency", PlacesActivity.getFavCurrencyRates());
                startActivity(i);
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_detail_view);

        //getting the id of the database record selected
        Bundle extras = getIntent().getExtras();
        id = extras.getString("index");
        index = Integer.parseInt(id);

        //creating a cursor based on the query
        Cursor cursor = QueryDB();

        //setting the values of the different values on the activity
        Log.d("query", String.valueOf(cursor.getCount()));
        setValues(cursor);

        //this is setting the title of the page to the name of the place
        setTitle(name);

        //filling the text boxes etc with the values
        fillInfo();
    }

    //method to query the database to get the infomration
    public Cursor QueryDB(){
        //create database helper
        PlaceDBOpenHelper helper = new PlaceDBOpenHelper(getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();
        //set values for queries seperate as makes it easier to read
        String table = PlaceDBOpenHelper.PLACES_TABLE_NAME;
        String[] colums = PlaceDBOpenHelper.ALL_COLUMNS;
        String selection = PlaceDBOpenHelper.KEY_ID + " = ?";
        String[] selctionArgs = {id};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;

        //actual query to thge database
        Cursor cursor = db.query(table, colums, selection, selctionArgs, groupBy, having,
                orderBy, limit);

        return cursor;
    }

    //get the results and put them into variables
    public void setValues(Cursor cursor){
        if (cursor.moveToFirst()){ //move to the first row
            do{
                //set the different variables
                name = cursor.getString(cursor.getColumnIndex(PlaceDBOpenHelper.KEY_NAME));
                description = cursor.getString(cursor.getColumnIndex
                        (PlaceDBOpenHelper.KEY_DESCRIPTION));
                longi = cursor.getString(cursor.getColumnIndex
                        (PlaceDBOpenHelper.KEY_LONGITUDE));
                lati = cursor.getString(cursor.getColumnIndex
                        (PlaceDBOpenHelper.KEY_LATITUDE));
                image = cursor.getString(cursor.getColumnIndex
                        (PlaceDBOpenHelper.KEY_IMAGE));
                oldImage = cursor.getString(cursor.getColumnIndex
                        (PlaceDBOpenHelper.KEY_IMAGE));
                rank = cursor.getString(cursor.getColumnIndex
                        (PlaceDBOpenHelper.KEY_RANK));
                planned = cursor.getString(cursor.getColumnIndex
                        (PlaceDBOpenHelper.KEY_DATEPLANNED));
                visited = cursor.getString(cursor.getColumnIndex
                        (PlaceDBOpenHelper.KEY_DATEVISITED));
                fav = Integer.parseInt(cursor.getString(cursor.getColumnIndex
                        (PlaceDBOpenHelper.KEY_FAVORITE)));
                price = cursor.getString(cursor.getColumnIndex
                        (PlaceDBOpenHelper.KEY_PRICE));
                location = cursor.getString(cursor.getColumnIndex
                        (PlaceDBOpenHelper.KEY_LOCATION));
                notes = cursor.getString(cursor.getColumnIndex
                        (PlaceDBOpenHelper.KEY_NOTES));
            }while (cursor.moveToNext());
            if (cursor !=null && !cursor.isClosed()){
                cursor.close();
            }
        }
    }

    public void fillInfo(){
        ImageView imageView = (ImageView)findViewById(R.id.imageView_detailPicture);
        RatingBar ratingBar = (RatingBar)findViewById(R.id.ratingBar_detailRank);
        TextView textView_Description = (TextView)findViewById(R.id.textView_detailDesc);
        TextView textView_Geo = (TextView)findViewById(R.id.textView_detailGeo);
        TextView textView_detailLoc = (TextView)findViewById(R.id.textView_detailLoc);
        CheckBox checkFav = (CheckBox)findViewById(R.id.checkBox_detailFav);
        CheckBox checkPlanned = (CheckBox)findViewById(R.id.checkBox_detailPlanned);
        CheckBox checkVisited = (CheckBox)findViewById(R.id.checkBox_detailVisited);
        TextView textView_Price = (TextView)findViewById(R.id.textView_detailPrice);
        TextView textView_planned = (TextView)findViewById(R.id.textView_detailPlanned);
        TextView textView_visited = (TextView)findViewById(R.id.textView_detailVisited);
        TextView textView_title_notes = (TextView)findViewById(R.id.textView_Title_detailNotes);
        TextView textView_notes = (TextView)findViewById(R.id.textView_detailNotes);
        TextView textView_favPrice = (TextView)findViewById(R.id.textView_detailFavPrice);

        float cost;
        try {
            cost = Float.parseFloat(price);
        }catch(NumberFormatException e){
            e.printStackTrace();
            cost = 0;
        }catch(Exception e){
            e.printStackTrace();
            cost = 0;
        }

        //set local price
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences
                (getApplicationContext());

        prefLocal = sharedPref.getString("localCurrency", "DEFAULT");
        prefFav = sharedPref.getString("favCurrency", "DEFAULT");

        //set prices
        if(cost == 0){
            textView_Price.setText("FREE");
        }else{
            DecimalFormat df = new DecimalFormat("00.00");
            textView_Price.setText("(" + prefLocal + ") " + df.format(cost));
        }


        //set favourite price
        if(cost == 0){
            textView_favPrice.setText("FREE");
        }else {
            //if the local price is GBP jsut set the same as these are places in the uk
            if (prefFav.equals(prefLocal)) {
                //set price
                DecimalFormat df = new DecimalFormat("00.00");
                textView_favPrice.setText("(" + prefFav + ") " + df.format(cost));
            } else {
                //get the local currency rates
                CurrencyRates currencyRates = PlacesActivity.getLocalCurrencyRates();

                //loop through each currency to find GBP
                for (CurrencyRate cr : currencyRates.getCurrencyRates()) {
                    if (cr.getType().equals(prefFav)) {
                        DecimalFormat df = new DecimalFormat("00.00");
                        if (cr.getRate() != 0) {
                            float newLocalCurrency = cost * cr.getRate();
                            textView_favPrice.setText
                                    ("(" + prefFav + ") " + df.format(newLocalCurrency));
                        } else {
                            textView_favPrice.setText("Unable to get currency data");
                        }
                    }
                }
            }
        }

        //setup rank and fill rank bar
        float detailRank = Float.parseFloat(rank);
        ratingBar.setRating(detailRank);

        if (lati == null ||lati.equals("") || lati.length() == 0){
            textView_Geo.setText("Geolocation information not set");
        }else {
            textView_Geo.setText("Latitude: " + lati + "\nLongitude: " + longi);
        }


        //see if image string is null and fill the image
        if (image != null){
            final File file = new File(getApplicationContext().getExternalFilesDir
                    (Environment.DIRECTORY_PICTURES), image);
            if(file.exists()) {
                Log.d("imageloaded", file.toString());
                Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
                imageView.setImageBitmap(image);
            }else{
                imageView.setImageResource(R.drawable.image_default);
            }
        }else{
            imageView.setImageResource(R.drawable.image_default);
        }

        textView_Description.setText(description);
        textView_detailLoc.setText(location);

        //set up checkboxes
        if (fav == 1){
            checkFav.setChecked(true);
            textView_title_notes.setText("Notes");
            //set description
            if (notes == null || notes.equals(" ") || notes.length() == 0){
                textView_notes.setText("No Notes Set");
            }else{
                textView_notes.setText(notes);
            }
        } else{
            checkFav.setChecked(false);
            textView_title_notes.setVisibility(View.GONE);
            textView_notes.setVisibility(View.GONE);
        }

        //set planned
        if(planned == null || planned == "" || planned.length() == 0){
            textView_planned.setText("");
            checkPlanned.setChecked(false);
        }else{
            textView_planned.setText(planned);
            checkPlanned.setChecked(true);
        }

        //set visited
        if(visited == null || visited == "" || visited.length() == 0){
            textView_visited.setText("");
            checkVisited.setChecked(false);
        }else{
            textView_visited.setText(visited);
            checkVisited.setChecked(true);
        }


        //set description
        if (description == null || description.equals("") || description.length() == 0){
            textView_Description.setText("No Description Set");
        }else{
            textView_Description.setText(description);
        }
    }

    //method to delete
    public void deleteAction(){
        //create alert dialog confirming delete
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PlacesDetailView.this);
        alertDialogBuilder
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //delete action from the place provider
                        getContentResolver().delete(PlaceProvider.CONTENT_URI, "_id = " + index,
                                null);
                        Toast.makeText(getApplicationContext(), "Place Deleted",
                                Toast.LENGTH_LONG).show();
                        //create the intent that loads up the master detail for places
                        startActivity(new Intent(getApplicationContext(), PlacesActivity.class));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Action Cancelled",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                }).create()
                .show();
    }

    //method for to be able to edit certain things on the pre loaded
    public void adjustAction(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PlacesDetailView.this);
        //create a view containing the get track details activity
        LayoutInflater li = LayoutInflater.from(PlacesDetailView.this);
        View getEmpIdView = li.inflate(R.layout.activity_ajust_place, null);

        //set dialog message
        alertDialogBuilder
                .setTitle("Edit Place")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),
                                "Action Cancelled", Toast.LENGTH_LONG).show();
                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //link values in boxes to the different variables
                        planned = textviewPlanned.getText().toString();
                        visited = textviewVisited.getText().toString();
                        notes = editNotes.getText().toString();
                        if(checkFav.isChecked()){fav = 1;}else{fav = 0;}
                        editPlace();
                    }
                });

        //create alert dialog and link to view created
        alertDialogBuilder.setView(getEmpIdView);

        //create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();

        editNotes = (EditText)alertDialog.findViewById(R.id.editText_edit_notes);

        //section to create on click listener for the check box to set a date for visited
        editVisited = (CheckBox)alertDialog.findViewById(R.id.checkBox_edit_visited);
        textviewVisited = (TextView)alertDialog.findViewById(R.id.textView_edit_visited);
        editVisited.setOnClickListener(visitedClicked);

        //section to create on click listener for the check box to set a date for planned
        editPlanned = (CheckBox)alertDialog.findViewById(R.id.checkBox_edit_planned);
        textviewPlanned = (TextView)alertDialog.findViewById(R.id.textView_edit_planned);
        editPlanned.setOnClickListener(plannedClicked);

        //section to create listener for the fav checkbox
        checkFav = (CheckBox)alertDialog.findViewById(R.id.checkBox_edit_favorite);
        checkFav.setOnClickListener(favClicked);

        //set values
        textviewPlanned.setText(planned);
        textviewVisited.setText(visited);

        if (fav == 1){
            checkFav.setChecked(true);
            editNotes.setVisibility(View.VISIBLE);
            editNotes.setText(notes);
        } else{
            checkFav.setChecked(false);
            editNotes.setVisibility(View.GONE);
        }


        //set planned
        if(planned == null || planned == "" || planned.length() == 0){
            editPlanned.setChecked(false);
        }else{
            editPlanned.setChecked(true);
        }
        //set visited
        if(visited == null || visited == "" || visited.length() == 0){
            editVisited.setChecked(false);
        }else{
            editVisited.setChecked(true);
        }


    }

    //method to handle editing a place
    public void editAction(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PlacesDetailView.this);
        //create a view containing the get track details activity
        LayoutInflater li = LayoutInflater.from(PlacesDetailView.this);
        View getEmpIdView = li.inflate(R.layout.activity_edit_place, null);

        //set dialog message
        alertDialogBuilder
                .setTitle("Edit Place")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),
                                "Action Cancelled", Toast.LENGTH_LONG).show();
                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //link values in boxes to the different variables
                        name = editName.getText().toString();
                        location = editlocation.getText().toString();
                        longi = editLong.getText().toString();
                        lati = editLat.getText().toString();
                        if(editPrice.getText().toString() != null ||
                                !editPrice.getText().toString().equals("") ||
                                editPrice.getText().length() == 0) {
                            price = editPrice.getText().toString();
                        }else{
                            price = "0";
                        }
                        description = editDesc.getText().toString();
                        planned = textviewPlanned.getText().toString();
                        visited = textviewVisited.getText().toString();
                        notes = editNotes.getText().toString();
                        if(checkFav.isChecked()){fav = 1;}else{fav = 0;}
                        editPlace();
                    }
                });

        //create alert dialog and link to view created
        alertDialogBuilder.setView(getEmpIdView);

        //create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();

        //link edit texts to variables
        editName = (EditText)alertDialog.findViewById(R.id.editText_edit_Name);
        editlocation = (EditText)alertDialog.findViewById(R.id.editText_edit_location);
        editLong = (EditText)alertDialog.findViewById(R.id.editText_edit_long);
        editLat = (EditText)alertDialog.findViewById(R.id.editText_edit_lat);
        editPrice = (EditText)alertDialog.findViewById(R.id.editText_edit_price);
        editDesc = (EditText)alertDialog.findViewById(R.id.editText_edit_desc);
        editNotes = (EditText)alertDialog.findViewById(R.id.editText_edit_notes);

        //section to create on click listener for the check box to set a date for visited
        editVisited = (CheckBox)alertDialog.findViewById(R.id.checkBox_edit_visited);
        textviewVisited = (TextView)alertDialog.findViewById(R.id.textView_edit_visited);
        editVisited.setOnClickListener(visitedClicked);

        //section to create on click listener for the check box to set a date for planned
        editPlanned = (CheckBox)alertDialog.findViewById(R.id.checkBox_edit_planned);
        textviewPlanned = (TextView)alertDialog.findViewById(R.id.textView_edit_planned);
        editPlanned.setOnClickListener(plannedClicked);

        //section to create listener for the fav checkbox
        checkFav = (CheckBox)alertDialog.findViewById(R.id.checkBox_edit_favorite);
        checkFav.setOnClickListener(favClicked);

        //create listener for the rank stars if they change
        rateRank = (RatingBar)alertDialog.findViewById(R.id.ratingBar_edit_rank);
        rateRank.setOnRatingBarChangeListener(rankChanged);

        //create listener for the button to add your own image as default image
        imageButton = (Button)alertDialog.findViewById(R.id.button_edit_image);
        textviewImage = (TextView)alertDialog.findViewById(R.id.textView_edit_image);
        imageButton.setOnClickListener(buttonClicked);

        //link the values to the text boxes
        editName.setText(name);
        editlocation.setText(location);
        editLong.setText(longi);
        editLat.setText(lati);
        editPrice.setText(price);
        editDesc.setText(description);
        rateRank.setRating(Float.parseFloat(rank));
        textviewPlanned.setText(planned);
        textviewVisited.setText(visited);

        if (fav == 1){
            checkFav.setChecked(true);
            editNotes.setVisibility(View.VISIBLE);
            editNotes.setText(notes);

            //set planned
            if(planned == null || planned == "" || planned.length() == 0){
                editPlanned.setChecked(false);
            }else{
                editPlanned.setChecked(true);
            }
            //set visited
            if(visited == null || visited == "" || visited.length() == 0){
                editVisited.setChecked(false);
            }else{
                editVisited.setChecked(true);
            }
        } else{
            checkFav.setChecked(false);
            editNotes.setVisibility(View.GONE);
            editPlanned.setVisibility(View.GONE);
            editVisited.setVisibility(View.GONE);
        }

    }

    //region set listener for image button
    View.OnClickListener buttonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //check to see if they want to take a picture or pick one from the gallery

            //create action menu to ask if you are sure you want to save the changes
            AlertDialog.Builder adb = new AlertDialog.Builder(PlacesDetailView.this);

            adb
                    .setTitle("Choose Image")
                    .setMessage("Would you like to take a picture or use one from your gallery?")
                    .setNegativeButton("Use Gallery", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //using a picture from your gallery
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
                        }
                    })
                    .setPositiveButton("Use Camera", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            }else{
                                Toast.makeText(getApplicationContext(), "Camera not available",
                                        Toast.LENGTH_LONG).show();
                                imageButton.performClick();
                            }
                        }
                    }).create().show();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        bmp = null;
        //if they chose to pick photo from gallery
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            try {
                InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                Log.d("imageStuff", inputStream.toString());

                //create bitmap
                bmp = BitmapFactory.decodeStream(inputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else if(requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_CANCELED){
            imageButton.performClick();
            return;
        }

        //if they chose to take a piccy
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            // do something
            if (resultCode == RESULT_OK) {
                // The user took a picture
                Bundle extras = data.getExtras();
                Log.d("imageStuff", extras.toString());
                bmp = (Bitmap)extras.get("data");
            }else{
                imageButton.performClick();
                return;
            }
        }

        //create image onto external storage
        bmp = Bitmap.createScaledBitmap(bmp,600,400,false);

        //create filename
        //create string name for bitmap
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "IMG_" + timeStamp + ".jpg";

        image = filename; //filename is saved into the image string
        textviewImage.setText(image);

    }

    //process for taking the bitmap and saving it to storage and referencing
    public void saveImage(Bitmap bm){

        Log.d("gotintosaveImage", bm.toString());

        //save file
        final File file = new File(getApplicationContext().getExternalFilesDir
                (Environment.DIRECTORY_PICTURES), image);
        try {
            //sending file out
            FileOutputStream outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 20, outStream);
            outStream.flush();
            outStream.close();

            //remove old photo as long as its not teh default
            if(oldImage != "IMG_default.jpg"){
                //save file
                final File filez = new File(getApplicationContext().getExternalFilesDir
                        (Environment.DIRECTORY_PICTURES), oldImage);
                if (filez.exists()){
                    if(filez.delete()){
                        Log.d("fileDeletion", "is done mahn" + oldImage);
                    }else{
                        Log.d("fileDeletion", "NOPE" + oldImage);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    //endregion

    // region set listener for picking favvourite
    View.OnClickListener favClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(checkFav.isChecked()) {
                fav = 1;
                editNotes.setVisibility(View.VISIBLE);
            }else{
                fav = 0;
                editNotes.setVisibility(View.GONE);
                notes = null;
            }
            Log.d("changeFav", String.valueOf(fav));
        }};
    //endregion

    // region set listener for picking the date for visited
    View.OnClickListener visitedClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(editVisited.isChecked()){
                if(editPlanned.isChecked()) {
                   DatePickerDialog dialog = new DatePickerDialog(PlacesDetailView.this, dateVisited, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH));
                    //set cancel button for teh calender dialog
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editVisited.toggle();
                            visited = null;
                        }
                    });

                    //show the calender picker
                    dialog.show();
                }else{
                    Toast.makeText(getApplicationContext(), "Please Plan A Visit First",
                            Toast.LENGTH_LONG).show();
                    editVisited.toggle();
                }
            }else{
                textviewVisited.setText("");
                visited = null;
            }
        }};
    DatePickerDialog.OnDateSetListener dateVisited = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateVisited();
        }

    };
    private void updateVisited() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);
        textviewVisited.setText(sdf.format(myCalendar.getTime()));
        visited = sdf.format(myCalendar.getTime());


        Calendar c = Calendar.getInstance();
        myCalendar.set(Calendar.YEAR, c.get(Calendar.YEAR));
        myCalendar.set(Calendar.MONTH, c.get(Calendar.MONTH));
        myCalendar.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
    }
    //endregion

    //region set listener for picking the date for planned
    View.OnClickListener plannedClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(editPlanned.isChecked()){
                DatePickerDialog dialog = new DatePickerDialog(PlacesDetailView.this, datePlanned, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));

                //create an action for the cancel button
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editPlanned.toggle();
                        planned = null;
                    }
                });
                dialog.show();
            }else{
                if(editVisited.isChecked()){
                    Toast.makeText(getApplicationContext(), "Please Uncheck Visited Before Unchecking Planned",
                            Toast.LENGTH_LONG).show();
                    editPlanned.toggle();
                }else {
                    textviewPlanned.setText("");
                    planned = null;
                }
            }
        }};
    DatePickerDialog.OnDateSetListener datePlanned = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updatePlanned();
        }

    };
    private void updatePlanned() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);
        textviewPlanned.setText(sdf.format(myCalendar.getTime()));
        planned = sdf.format(myCalendar.getTime());

        Calendar c = Calendar.getInstance();
        myCalendar.set(Calendar.YEAR, c.get(Calendar.YEAR));
        myCalendar.set(Calendar.MONTH, c.get(Calendar.MONTH));
        myCalendar.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));

    }
    //endregion

    //region listener to see if rating bar changes
    RatingBar.OnRatingBarChangeListener rankChanged = new RatingBar.OnRatingBarChangeListener() {
        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            rate = rating;
            Log.d("rating", String.valueOf(rate));
        }
    };
    //endregion

    //method to edit place
    public void editPlace(){
        //create action menu to ask if you are sure you want to save the changes
        AlertDialog.Builder adb = new AlertDialog.Builder(PlacesDetailView.this);

        adb
                .setTitle("Confirm Edit")
                .setMessage("Are you sure you wish to edit this place?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //go back to edit action builder
                        editAction();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //set planned null if empty
                        if(planned == null || planned == "" || planned.length() == 0){
                            planned = null;
                        }
                        //set visited null if empty
                        if(visited == null || visited == "" || visited.length() == 0){
                            visited = null;
                        }

                        //set visited null if empty
                        if(price == null || price == "" || price.length() == 0){
                            price = "0";
                        }

                        //create content value
                        ContentValues values = new ContentValues();
                        if (index > 10) {
                            values.put(PlaceDBOpenHelper.KEY_NAME, name);
                            values.put(PlaceDBOpenHelper.KEY_LOCATION, location);
                            values.put(PlaceDBOpenHelper.KEY_PRICE, price);
                            values.put(PlaceDBOpenHelper.KEY_DESCRIPTION, description);
                            values.put(PlaceDBOpenHelper.KEY_LONGITUDE, longi);
                            values.put(PlaceDBOpenHelper.KEY_LATITUDE, lati);
                            values.put(PlaceDBOpenHelper.KEY_RANK, rate);
                            values.put(PlaceDBOpenHelper.KEY_FAVORITE, fav);
                            values.put(PlaceDBOpenHelper.KEY_DATEPLANNED, planned);
                            values.put(PlaceDBOpenHelper.KEY_DATEVISITED, visited);

                            if (bmp != null) {
                                values.put(PlaceDBOpenHelper.KEY_IMAGE, image);

                                saveImage(bmp);
                            }
                            if(fav == 1){
                                values.put(PlaceDBOpenHelper.KEY_NOTES, notes);
                            }
                        }else{
                            values.put(PlaceDBOpenHelper.KEY_FAVORITE, fav);
                            values.put(PlaceDBOpenHelper.KEY_DATEPLANNED, planned);
                            values.put(PlaceDBOpenHelper.KEY_DATEVISITED, visited);
                            if(fav == 1){
                                values.put(PlaceDBOpenHelper.KEY_NOTES, notes);
                            }
                        }

                        //set where for correct ID
                        String where = "_id = " + id;

                        //link content val and selection arg to database
                        getContentResolver().update(PlaceProvider.CONTENT_URI, values, where, null);

                        Intent intent = new Intent(getApplicationContext(), PlacesDetailView.class);
                        intent.putExtra("index", id);
                        startActivity(intent);

                        Toast.makeText(getApplicationContext(), "Place Edited",
                                Toast.LENGTH_LONG).show();
                    }
                }).create().show();
    }

    //get the weather report
    public void goWeather(View v){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PlacesDetailView.this);
        //create a view containing the get track details activity
        LayoutInflater li = LayoutInflater.from(PlacesDetailView.this);
        View getEmpIdView = li.inflate(R.layout.weather, null);

        alertDialogBuilder
                .setTitle("Weather Information")
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        //create alert dialog and link to view created
        alertDialogBuilder.setView(getEmpIdView);

        //create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();

        //link to textviews
        cityText = (TextView)alertDialog.findViewById(R.id.cityText);
        cityText.setText("Loading...");
        condDesc = (TextView)alertDialog.findViewById(R.id.condDesc);
        temp = (TextView)alertDialog.findViewById(R.id.temp);
        titleTemp = (TextView)alertDialog.findViewById(R.id.tempTitle);
        titleTemp.setVisibility(View.GONE);
        titleHum = (TextView)alertDialog.findViewById(R.id.humidTitle);
        titleHum.setVisibility(View.GONE);
        titlePress = (TextView)alertDialog.findViewById(R.id.pressureTitle);
        titlePress.setVisibility(View.GONE);
        titleWindSpeed = (TextView)alertDialog.findViewById(R.id.windTitle);
        titleWindSpeed.setVisibility(View.GONE);
        hum = (TextView)alertDialog.findViewById(R.id.humid);
        press = (TextView)alertDialog.findViewById(R.id.pressure);
        windSpeed = (TextView)alertDialog.findViewById(R.id.windSpeed);
        windDeg = (TextView)alertDialog.findViewById(R.id.windDeg);
        imgView = (ImageView)alertDialog.findViewById(R.id.condIcon);

        JSONWeatherTask task = new JSONWeatherTask();

        task.execute(new String[]{location});
    }

    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... params) {
            Log.d("data", params[0]);
            Weather weather = new Weather();
            Log.d("urlString",params[0]);
            String data = ((new WeatherHTTPClient()).getWeatherData(params[0]));
            if (data != null) {
                try {
                    weather = JSONWeatherParser.getWeather(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return weather;
            }
            else return null;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            if (weather!=null) {
                if (weather.bmp!=null) imgView.setImageBitmap(weather.bmp);
                cityText.setText(weather.location.getCity() + ", " + weather.location.getCountry());
                condDesc.setText(weather.currentCondition.getCondition() + " (" + weather.currentCondition.getDesc() + ")");
                temp.setText(" " + Math.round((weather.temperature.getTemp())) + "°C");
                hum.setText(" " + weather.currentCondition.getHumidity() + "%");
                press.setText(" " + weather.currentCondition.getPressure() + " hPa");
                windSpeed.setText(" " + weather.wind.getSpeed() + " km/h");
                windDeg.setText(" " + weather.wind.getDeg() + "°");
                titleTemp.setVisibility(View.VISIBLE);
                titleHum.setVisibility(View.VISIBLE);
                titlePress.setVisibility(View.VISIBLE);
                titleWindSpeed.setVisibility(View.VISIBLE);
            }
            else
            {
                cityText.setText("Unable to retrieve weather imformation please try again later");
            }
        }
    }
}
