package com.example.maaz.gpsparse;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

public class MapsActivitySignin extends FragmentActivity implements OnMapReadyCallback, android.location.LocationListener {

    private GoogleMap mMap;

    Button refresh, exit;

    // flags for GPS, Network status
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;

    double latitude = 0;
    double longitude = 0;

    String s = null;

    private LocationManager locationManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_activity_signin);

        exit = (Button) findViewById(R.id.btn);
        refresh = (Button) findViewById(R.id.refresh);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // getting GPS status
        isGPSEnabled = locationManager.isProviderEnabled(GPS_PROVIDER);

        // getting network status
        ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State mobile = conMan.getNetworkInfo(0).getState();
        NetworkInfo.State wifi = conMan.getNetworkInfo(1).getState();
        if (mobile == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTED) {
            isNetworkEnabled = true;
        }

        Log.d("GPS, Network = ", isGPSEnabled + ", " + isNetworkEnabled);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        } else {
            if (isNetworkEnabled == true) {
                Log.d("1", "onCreate: Network");
                locationManager.requestSingleUpdate(NETWORK_PROVIDER, this, Looper.myLooper());

            } else {
                Log.d("2", "onCreate: GPS");
                locationManager.requestSingleUpdate(GPS_PROVIDER, this, Looper.myLooper());
            }
        }

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //refresh
                Intent intent = new Intent(MapsActivitySignin.this, MapsActivitySignin.class);
                startActivity(intent);
                finish();
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logout from Parse and Goes to Login Screen
                ParseUser.logOut();
                ParseUser currentUser = ParseUser.getCurrentUser(); // should be null but isn't...
                invalidateOptionsMenu();
                Intent intent = new Intent(MapsActivitySignin.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

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
    public void onMapReady(final GoogleMap googleMap) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMap = googleMap;

                LatLng Coordinates = new LatLng(latitude, longitude);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(Coordinates)
                        .zoom(15)
                        .bearing(0)
                        .tilt(45)
                        .build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                mMap.addMarker(new MarkerOptions().position(Coordinates).title(s)).showInfoWindow();
            }
        },2000);
    }

    //For Location Listner
    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        if (latitude != 0.0 && longitude != 0.0) {

            Toast.makeText(getApplicationContext(), "Location Found!", Toast.LENGTH_LONG).show();

            findLoc(latitude, longitude);
        }

    }

    public void findLoc(final double lat, final double log) {

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
                List<Address> addresses;
                try {
                    addresses = gcd.getFromLocation(lat, log, 1);
                    if (addresses.size() > 0) {
                        String address = addresses.get(0).getAddressLine(0);
                        String area = addresses.get(0).getSubLocality();
                        String city = addresses.get(0).getLocality();
                        String country = addresses.get(0).getCountryName();

                        s = address +", "+ area +", "+ city +", "+ country;

                        if (address == null && area == null && city == null && country == null) {
                            s = "Latitude: " + lat + "\nLongitude: " + log + "\n\nURL: https://www.google.com/maps/search/" + lat + "," + log;
                        }
                    }
                    else {
                        s = "Latitude: " + lat + "\nLongitude: " + log + "\n\nURL: https://www.google.com/maps/search/" + lat + "," + log;
                    }
                } catch (IOException e) {
                    s = "Latitude: " + lat + "\nLongitude: " + log + "\n\nURL: https://www.google.com/maps/search/" + lat + "," + log;
                    e.printStackTrace();
                }

                //check if user already present in DB just keep updating that row with coordinates.
                //else create a new row with that userId + New Coordinates
                final ParseGeoPoint point = new ParseGeoPoint(lat, log);
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Coordinates");
                query.whereEqualTo("User", ParseUser.getCurrentUser().getObjectId());
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    public void done(ParseObject object, ParseException e) {
                        if (e == null) {
                            Log.d("Logging", "In IF");
                            object.put("Coordinates", point);
                            object.saveInBackground();;
                        } else {
                            if(e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                Log.d("Logging", "In ELSE");
                                ParseObject obj = new ParseObject("Coordinates");
                                obj.put("Coordinates", point);
                                obj.put("User", ParseUser.getCurrentUser().getObjectId());
                                try {
                                    obj.save();
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
        },2000);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting DialogHelp Title
        alertDialog.setTitle("GPS is not enabled");

        // Setting DialogHelp Message
        alertDialog
                .setMessage("Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }
}
