package com.cet325.bg47hb;

import android.*;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.LoaderManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.content.Loader;
import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PlacesActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    private CursorAdapter cursorAdapter = null;
    private String selectedItem;
    private ListView list;
    private int mode = 0;
    private int order = 0;
    private double price;
    private static int SELECTION;
    private static CurrencyRates localCurrencyRates;
    private static CurrencyRates favCurrencyRates;
    private static Context context;


    public static Context getAppContext() {
        return PlacesActivity.context;
    }

    public static CurrencyRates getFavCurrencyRates() {
        return favCurrencyRates;
    }

    public static CurrencyRates getLocalCurrencyRates() {
        return localCurrencyRates;
    }

    public static int getSELECTION(){
        return SELECTION;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        PlacesActivity.context = getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();

        localCurrencyRates = (CurrencyRates) i.getSerializableExtra("localCurrency");
        favCurrencyRates = (CurrencyRates) i.getSerializableExtra("favCurrency");


        list = (ListView)findViewById(R.id.listView_places);
        list.setEmptyView(findViewById(R.id.empty));

        //set on click listener for list view
        listViewClickListener();

        //floating action button on click
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //add a new location
                    createAddAction();
                }
            });

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //set up so it shows all when opening activity
        Menu menu = navigationView.getMenu();
        MenuItem all = menu.findItem(R.id.placeAll);
        onNavigationItemSelected(all);
    }

    public void listViewClickListener(){
        list = (ListView)findViewById(R.id.listView_places);
        //set onclick listener for list
        list.setClickable(true);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //get the title of the item selected
                //create a view containing the get track details activity
                final Cursor cursor = (Cursor)list.getItemAtPosition(position);
                selectedItem = cursor.getString(cursor.getColumnIndexOrThrow
                        (PlaceDBOpenHelper.KEY_ID));
                Intent intent = new Intent(view.getContext(), PlacesDetailView.class);
                intent.putExtra("index", selectedItem);
                intent.putExtra("localCurrency", localCurrencyRates);
                intent.putExtra("favCurrency", favCurrencyRates);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.places, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        //set spannable streams for each of the different options
        SpannableString s = new SpannableString("All");
        SpannableString sPlanned = new SpannableString("Planned");
        SpannableString sVisited = new SpannableString("Visited");
        SpannableString sFav = new SpannableString("Favourites");
        SpannableString sName = new SpannableString("Name");
        SpannableString sLocName = new SpannableString("Location Name");
        SpannableString sRank = new SpannableString("Rank");
        SpannableString sDPlanned = new SpannableString("Date Planned");
        SpannableString sDVisited = new SpannableString("Date Visited");
        StyleSpan ss = new StyleSpan(Typeface.BOLD);


        //get the navigation view
        NavigationView navView = (NavigationView)findViewById(R.id.nav_view);

        //get menu from nav view
        Menu menu = navView.getMenu();

        //go through each menu item and set text to colour black
        MenuItem all = menu.findItem(R.id.placeAll);
        MenuItem visited = menu.findItem(R.id.placeVisited);
        MenuItem favorites = menu.findItem(R.id.placeFav);
        MenuItem planned = menu.findItem(R.id.placePlanned);
        MenuItem name = menu.findItem(R.id.sortName);
        MenuItem locName = menu.findItem(R.id.sortLocationName);
        MenuItem rank = menu.findItem(R.id.sortRank);
        MenuItem mPlanned = menu.findItem(R.id.sortDatePlanned);
        MenuItem mVisited = menu.findItem(R.id.sortDateVisited);

        switch (id){
            case R.id.placeBack:
                //create the intent that loads up the main menu
                Intent i = new Intent(this, MainMenu.class);
                i.putExtra("localCurrency", localCurrencyRates);
                i.putExtra("favCurrency", favCurrencyRates);
                startActivity(i);
                break;
            case R.id.placeAll:
                SELECTION = 0;
                //reload all the places
                QueryDBMode(0);

                //auto set all colours to black
                setDefaultColor(s, all);
                setDefaultColor(sPlanned, planned);
                setDefaultColor(sVisited, visited);
                setDefaultColor(sFav, favorites);
                //auto set all colours to black
                setDefaultColor(sName, name);
                setDefaultColor(sLocName, locName);
                setDefaultColor(sRank, rank);
                setDefaultColor(sDPlanned, mPlanned);
                setDefaultColor(sDVisited, mVisited);

                //set up to change colour of used item
                s.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0, s.length(), 0);
                s.setSpan(ss, 0 ,s.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                item.setTitle(s);

                //set up colour for default rank
                sRank.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0, sRank.length(), 0);
                sRank.setSpan(ss, 0 ,sRank.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                rank.setTitle(sRank);
                break;
            case R.id.placePlanned:
                SELECTION = 1;
                //reload all the places
                QueryDBMode(1);

                //auto set all colours to black
                setDefaultColor(s, all);
                setDefaultColor(sPlanned, planned);
                setDefaultColor(sVisited, visited);
                setDefaultColor(sFav, favorites);
                //auto set all colours to black
                setDefaultColor(sName, name);
                setDefaultColor(sLocName, locName);
                setDefaultColor(sRank, rank);
                setDefaultColor(sDPlanned, mPlanned);
                setDefaultColor(sDVisited, mVisited);

                //set up to change colour of used item
                sPlanned.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0,
                        sPlanned.length(), 0);
                sPlanned.setSpan(ss, 0 ,sPlanned.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                item.setTitle(sPlanned);

                //set up to change colour of used order
                sDPlanned.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0,
                        sDPlanned.length(), 0);
                sDPlanned.setSpan(ss, 0 ,sDPlanned.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                mPlanned.setTitle(sDPlanned);
                break;
            case R.id.placeVisited:
                SELECTION = 1;
                //reload all the places
                QueryDBMode(2);

                //auto set all colours to black
                setDefaultColor(s, all);
                setDefaultColor(sPlanned, planned);
                setDefaultColor(sVisited, visited);
                setDefaultColor(sFav, favorites);
                //auto set all colours to black
                setDefaultColor(sName, name);
                setDefaultColor(sLocName, locName);
                setDefaultColor(sRank, rank);
                setDefaultColor(sDPlanned, mPlanned);
                setDefaultColor(sDVisited, mVisited);

                //set up to change colour of used item
                sVisited.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0,
                        sVisited.length(), 0);
                sVisited.setSpan(ss, 0 ,sVisited.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                item.setTitle(sVisited);

                //set up to change colour of used order
                sDVisited.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0,
                        sDVisited.length(), 0);
                sDVisited.setSpan(ss, 0 ,sDVisited.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                mVisited.setTitle(sDVisited);
                break;
            case R.id.placeFav:
                SELECTION = 1;
                //reload all the places
                QueryDBMode(3);

                //auto set all colours to black
                setDefaultColor(s, all);
                setDefaultColor(sPlanned, planned);
                setDefaultColor(sVisited, visited);
                setDefaultColor(sFav, favorites);
                //auto set all colours to black
                setDefaultColor(sName, name);
                setDefaultColor(sLocName, locName);
                setDefaultColor(sRank, rank);
                setDefaultColor(sDPlanned, mPlanned);
                setDefaultColor(sDVisited, mVisited);

                //set up to change colour of used item
                sFav.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0,
                        sFav.length(), 0);
                sFav.setSpan(ss, 0 ,sFav.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                item.setTitle(sFav);

                //set up colour for default rank
                sRank.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0, sRank.length(), 0);
                sRank.setSpan(ss, 0 ,sRank.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                rank.setTitle(sRank);

                break;
            case R.id.sortName:
                //reload all the places
                QueryDBMode(4);

                //auto set all colours to black
                setDefaultColor(sName, name);
                setDefaultColor(sLocName, locName);
                setDefaultColor(sRank, rank);
                setDefaultColor(sDPlanned, mPlanned);
                setDefaultColor(sDVisited, mVisited);

                //set up to change colour of used item
                sName.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0,
                        sName.length(), 0);
                sName.setSpan(ss, 0 ,sName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                item.setTitle(sName);
                break;
            case R.id.sortLocationName:
                //reload all the places
                QueryDBMode(5);

                //auto set all colours to black
                setDefaultColor(sName, name);
                setDefaultColor(sLocName, locName);
                setDefaultColor(sRank, rank);
                setDefaultColor(sDPlanned, mPlanned);
                setDefaultColor(sDVisited, mVisited);

                //set up to change colour of used item
                sLocName.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0,
                        sLocName.length(), 0);
                sLocName.setSpan(ss, 0 ,sLocName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                item.setTitle(sLocName);
                break;
            case R.id.sortRank:
                //reload all the places
                QueryDBMode(6);

                //auto set all colours to black
                setDefaultColor(sName, name);
                setDefaultColor(sLocName, locName);
                setDefaultColor(sRank, rank);
                setDefaultColor(sDPlanned, mPlanned);
                setDefaultColor(sDVisited, mVisited);

                //set up to change colour of used item
                sRank.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0,
                        sRank.length(), 0);
                sRank.setSpan(ss, 0 ,sRank.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                item.setTitle(sRank);
                break;
            case R.id.sortDatePlanned:
                //reload all the places
                QueryDBMode(7);

                //auto set all colours to black
                setDefaultColor(sName, name);
                setDefaultColor(sLocName, locName);
                setDefaultColor(sRank, rank);
                setDefaultColor(sDPlanned, mPlanned);
                setDefaultColor(sDVisited, mVisited);

                //set up to change colour of used item
                sDPlanned.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0,
                        sDPlanned.length(), 0);
                sDPlanned.setSpan(ss, 0 ,sDPlanned.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                item.setTitle(sDPlanned);
                break;
            case R.id.sortDateVisited:
                //reload all the places
                QueryDBMode(8);

                //auto set all colours to black
                setDefaultColor(sName, name);
                setDefaultColor(sLocName, locName);
                setDefaultColor(sRank, rank);
                setDefaultColor(sDPlanned, mPlanned);
                setDefaultColor(sDVisited, mVisited);

                //set up to change colour of used item
                sDVisited.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0,
                        sDVisited.length(), 0);
                sDVisited.setSpan(ss, 0 ,sDVisited.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                item.setTitle(sDVisited);
                break;
            default:
                //set default
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        listViewClickListener();
        return true;
    }

    public void setDefaultColor(SpannableString s, MenuItem m){
        //set up to change colour of item
        final StyleSpan ss = new StyleSpan(Typeface.NORMAL);
        s.setSpan(ss, 0 ,s.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        s.setSpan(new ForegroundColorSpan(Color.parseColor("#979797")), 0, s.length(), 0);
        m.setTitle(s);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, PlaceProvider.CONTENT_URI,null,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    //method to query for the different modes
    public void QueryDBMode(int index){
        list = (ListView)findViewById(R.id.listView_places);

        //create database helper
        PlaceDBOpenHelper helper = new PlaceDBOpenHelper(getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();

        //set values for queries seperate as makes it easier to read
        String table = PlaceDBOpenHelper.PLACES_TABLE_NAME;
        String[] colums = {"*"};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;
        String selection;
        String[] selctionArgs;

        switch (index){
            case 0: //case to show all
                selection = null;
                selctionArgs = null;
                mode = 0;
                orderBy = PlaceDBOpenHelper.KEY_RANK + " DESC";
                break;
            case 1: // case for to show date planned only
                selection = PlaceDBOpenHelper.KEY_DATEPLANNED + " != ? AND date_visited is null";
                selctionArgs = new String[]{"''"};
                mode = 1;
                orderBy = PlaceDBOpenHelper.KEY_DATEPLANNED + " ASC";
                break;
            case 2: //case to show date visited only
                selection = PlaceDBOpenHelper.KEY_DATEVISITED + " !=? ";
                selctionArgs = new String[]{"''"};
                mode = 2;
                orderBy = PlaceDBOpenHelper.KEY_DATEVISITED + " ASC";
                break;
            case 3: //case to show favourite only
                selection = PlaceDBOpenHelper.KEY_FAVORITE + " =? ";
                selctionArgs = new String[]{"1"};
                mode = 3;
                orderBy = PlaceDBOpenHelper.KEY_RANK + " DESC";
                break;
            case 4: //case to order by name
                //see what mode we are in first
                if (mode == 0){
                    selection = null;
                    selctionArgs = null;
                }else if (mode == 1){
                    selection = PlaceDBOpenHelper.KEY_DATEPLANNED + " !=? ";
                    selctionArgs = new String[]{"''"};
                }else if (mode == 2){
                    selection = PlaceDBOpenHelper.KEY_DATEVISITED + " !=? ";
                    selctionArgs = new String[]{"''"};
                }else{
                    selection = PlaceDBOpenHelper.KEY_FAVORITE + " =? ";
                    selctionArgs = new String[]{"1"};
                }

                orderBy = PlaceDBOpenHelper.KEY_NAME + " ASC";
                order = 0;
                break;
            case 5: //case to order by location name
                //see what mode we are in first
                if (mode == 0){
                    selection = null;
                    selctionArgs = null;
                }else if (mode == 1){
                    selection = PlaceDBOpenHelper.KEY_DATEPLANNED + " !=? ";
                    selctionArgs = new String[]{"''"};
                }else if (mode == 2){
                    selection = PlaceDBOpenHelper.KEY_DATEVISITED + " !=? ";
                    selctionArgs = new String[]{"''"};
                }else{
                    selection = PlaceDBOpenHelper.KEY_FAVORITE + " =? ";
                    selctionArgs = new String[]{"1"};
                }

                orderBy = PlaceDBOpenHelper.KEY_LOCATION + " ASC";
                order = 1;
                break;
            case 6: //case to order by rank
                //see what mode we are in first
                if (mode == 0){
                    selection = null;
                    selctionArgs = null;
                }else if (mode == 1){
                    selection = PlaceDBOpenHelper.KEY_DATEPLANNED + " !=? ";
                    selctionArgs = new String[]{"''"};
                }else if (mode == 2){
                    selection = PlaceDBOpenHelper.KEY_DATEVISITED + " !=? ";
                    selctionArgs = new String[]{"''"};
                }else{
                    selection = PlaceDBOpenHelper.KEY_FAVORITE + " =? ";
                    selctionArgs = new String[]{"1"};
                }

                orderBy = PlaceDBOpenHelper.KEY_RANK + " DESC";
                order = 2;
                break;
            case 7: //case to order by date planned
                //see what mode we are in first
                if (mode == 0){
                    selection = null;
                    selctionArgs = null;
                }else if (mode == 1){
                    selection = PlaceDBOpenHelper.KEY_DATEPLANNED + " !=? ";
                    selctionArgs = new String[]{"''"};
                }else if (mode == 2){
                    selection = PlaceDBOpenHelper.KEY_DATEVISITED + " !=? ";
                    selctionArgs = new String[]{"''"};
                }else{
                    selection = PlaceDBOpenHelper.KEY_FAVORITE + " =? ";
                    selctionArgs = new String[]{"1"};
                }

                orderBy = PlaceDBOpenHelper.KEY_DATEPLANNED + " ASC";
                order = 3;
                break;
            case 8: //case to order by date visited
                //see what mode we are in first
                if (mode == 0){
                    selection = null;
                    selctionArgs = null;
                }else if (mode == 1){
                    selection = PlaceDBOpenHelper.KEY_DATEPLANNED + " !=? ";
                    selctionArgs = new String[]{"''"};
                }else if (mode == 2){
                    selection = PlaceDBOpenHelper.KEY_DATEVISITED + " !=? ";
                    selctionArgs = new String[]{"''"};
                }else{
                    selection = PlaceDBOpenHelper.KEY_FAVORITE + " =? ";
                    selctionArgs = new String[]{"1"};
                }

                orderBy = PlaceDBOpenHelper.KEY_DATEVISITED + " ASC";
                order = 4;
                break;
            default:
                selection = null;
                selctionArgs = null;
                break;
        }

        //actual query to the database
        Cursor cursor = db.query(table, colums, selection, selctionArgs, groupBy, having,
                orderBy, limit);

        Log.d("queryCursorLength", String.valueOf(cursor.getCount()));

        //check to see if theres any records
        if(cursor.getCount() < 1){
            list.setAdapter(null);
        }else{
            cursorAdapter = new PlacesCursorAdapter(getApplicationContext(), cursor, 0);
            list.setAdapter(cursorAdapter);
        }

    }

    //action menu for add
    public void createAddAction(){
        //create a view containing the get track details activity
        LayoutInflater li = LayoutInflater.from(PlacesActivity.this);
        View getEmpIdView = li.inflate(R.layout.activity_add_place, null);

        //create alert dialog and link to view created
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PlacesActivity.this);
        alertDialogBuilder.setView(getEmpIdView);

        //link edit texts to variables
        final EditText title = (EditText)getEmpIdView.findViewById(R.id.editText_title);
        final EditText location = (EditText)getEmpIdView.findViewById(R.id.editText_location);
        final EditText priceInput = (EditText)getEmpIdView.findViewById(R.id.editText_price);
        final EditText longi = (EditText)getEmpIdView.findViewById(R.id.editText_long);
        final EditText lat = (EditText)getEmpIdView.findViewById(R.id.editText_lat);
        final EditText desc = (EditText)getEmpIdView.findViewById(R.id.editText_desc);

        //set dialog message
        alertDialogBuilder
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),
                                "Action Cancelled", Toast.LENGTH_LONG).show();
                    }
                })
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            //try to insert the contact
                            if(title.getText().toString() == " " || title.getText().length() == 0 ||
                                    location.getText().toString() == " " ||
                                    location.getText().length() == 0){ //title and location are mand
                                Toast.makeText(getApplicationContext(),
                                        "ERROR: Please insert values into title and location",
                                        Toast.LENGTH_LONG).show();
                                createAddAction();
                            }else {
                                price = Double.parseDouble(priceInput.getText().toString());
                                insertTrack(title.getText().toString(),
                                        location.getText().toString(), longi.getText().toString(),
                                        lat.getText().toString(), desc.getText().toString(), price);
                                Toast.makeText(getApplicationContext(),
                                        "Track Added", Toast.LENGTH_LONG).show();
                            }
                        }catch (NumberFormatException e){
                            e.printStackTrace();
                            //create alert dialog confirming we have failed on price
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder
                                    (PlacesActivity.this);
                            alertDialogBuilder
                                    .setTitle("Error")
                                    .setMessage("Price is invalid or has not been set would you " +
                                            "like it set to 0?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            price = 0;
                                            insertTrack(title.getText().toString(),
                                                    location.getText().toString(),
                                                    longi.getText().toString(),
                                                    lat.getText().toString(),
                                                    desc.getText().toString(), price);
                                            Toast.makeText(getApplicationContext(),
                                                    "Track Added", Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener(){
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Toast.makeText(getApplicationContext(),
                                                    "ERROR: Please insert a number into price",
                                                    Toast.LENGTH_LONG).show();
                                            createAddAction();
                                            return;
                                        }
                                    }).create()
                                    .show();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }).create().show();
    }

    //method for inserting the track into the database
    public void insertTrack(String name, String location, String longi, String lat, String desc,
                            Double price){
        if(desc.length() == 0 || desc == " "){
            desc = null;
        }
        if(price == null){
            price = 0.0;
        }
        if(longi.length() == 0 || longi == " "){
            longi = null;
        }
        if(lat.length() == 0 || lat == " "){
            lat = null;
        }

        //create content val and insert values
        ContentValues values = new ContentValues();
        values.put(PlaceDBOpenHelper.KEY_NAME, name);
        values.put(PlaceDBOpenHelper.KEY_LOCATION, location);
        values.put(PlaceDBOpenHelper.KEY_LATITUDE, lat);
        values.put(PlaceDBOpenHelper.KEY_LONGITUDE, longi);
        values.put(PlaceDBOpenHelper.KEY_DESCRIPTION, desc);
        values.put(PlaceDBOpenHelper.KEY_PRICE, price);
        values.put(PlaceDBOpenHelper.KEY_IMAGE, "IMG_default.jpg");
        values.put(PlaceDBOpenHelper.KEY_RANK, 0);
        values.put(PlaceDBOpenHelper.KEY_FAVORITE, 0);

        //get content resolver to insert the database info
        getContentResolver().insert(PlaceProvider.CONTENT_URI, values);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //set up so it shows all when opening activity
        Menu menu = navigationView.getMenu();
        MenuItem all = menu.findItem(R.id.placeAll);
        onNavigationItemSelected(all);
    }

}


