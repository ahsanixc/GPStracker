package com.example.maaz.gpsparse;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

public class MapsActivityFind extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    Button refresh, exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_activity_find);

        exit = (Button) findViewById(R.id.btn);
        refresh = (Button) findViewById(R.id.refresh);

//        Query();

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //refresh
                Query();
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //deletes the row from DB
//                ParseQuery<ParseObject> query = ParseQuery.getQuery("Coordinates");
//                query.whereEqualTo("User", ParseUser.getCurrentUser().getObjectId());
//                query.findInBackground(new FindCallback<ParseObject>() {
//                    public void done(List<ParseObject> invites, ParseException e) {
//                        if (e == null) {
//                            // iterate over all messages and delete them
//                            for(ParseObject invite : invites)
//                            {
//                                invite.deleteInBackground();
//                            }
//                        }
//                    }
//                });

                // Logout from Parse and Goes to Login Screen
                ParseUser.logOut();
                ParseUser currentUser = ParseUser.getCurrentUser(); // should be null but isn't...
                invalidateOptionsMenu();
                Intent intent = new Intent(MapsActivityFind.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    public void Query() {
        Toast.makeText(getApplicationContext(), "Locating Please wait!!", Toast.LENGTH_LONG).show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("objectId",ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    // row of Object Id "Current USer"
                    try {
                        //Sms send to phone number. Read SMS through SmsListener update coordinates. check time and update on
                        String Number = "0" + String.valueOf(objects.get(0).fetch().get("PhoneNum"));
                        Log.d("Numb", Number);

                        SmsManager sms = SmsManager.getDefault();
                        try {
                            sms.sendTextMessage(Number, null, "GPS" + ParseUser.getCurrentUser().getObjectId(), null, null);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    Query();
                }
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Coordinates");
        query.whereEqualTo("User", ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    // row of Object Id "Current USer"
                    try {
                        //gets the coordinates and last updated time from the table
                        ParseGeoPoint Coordinates = objects.get(0).fetch().getParseGeoPoint("Coordinates");
                        Date update = objects.get(0).fetch().getUpdatedAt();

                        LatLng latlng = new LatLng(Coordinates.getLatitude(), Coordinates.getLongitude());
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(latlng)
                                .zoom(15)
                                .bearing(0)
                                .tilt(45)
                                .build();

                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        mMap.addMarker(new MarkerOptions().position(latlng).title("Last Location")).showInfoWindow();

                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }
}
