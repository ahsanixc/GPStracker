package com.example.maaz.gpsparse;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.os.Process;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity implements LocationListener {

    TextView t1;
    ProgressBar spinner;
    Button exit;

    // flags for GPS, Network status
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;

    String s = null;

    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "0Zvm6VwwKgQkwTqHKpA7xFYWihvMOsTchLLrzWlN", "9fxWGu1hWsChwdndIAhua6yLm0uYmv9Sl6g0iSsn");

        t1 = (TextView) findViewById(R.id.text);
        spinner = (ProgressBar) findViewById(R.id.progressBar);
        exit = (Button) findViewById(R.id.btn);
        spinner.setVisibility(View.VISIBLE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // getting GPS status
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

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
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 2, this); //sec, meters

            } else {
                Log.d("2", "onCreate: GPS");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 2, this); //sec, meters
            }
        }

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                android.os.Process.killProcess(Process.myPid());
            }
        });

    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        if (latitude != 0.0 && longitude != 0.0) {

            Toast.makeText(getApplicationContext(), "Location Found!", Toast.LENGTH_LONG).show();

            spinner.setVisibility(View.GONE);
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

                        s = "Address Line: "
                                + address + "\n"
                                + "Area: "
                                + area + "\n"
                                + "City: "
                                + city + "\n"
                                + "Country: "
                                + country + "\n"
                                + "\nLatitude: " + lat +"\nLongitude: " +log
                                + "\n\nhttps://www.google.com/maps/search/" + lat + "," + log;

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
                Log.d("Msg ", s);
                t1.setText(s);

                try {
                    ParseGeoPoint point = new ParseGeoPoint(lat, log);
                    ParseObject object = new ParseObject("Coordinates");
                    object.put("Coordinates", point);
                    object.save();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        },2000);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
//        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//        startActivity(intent);
//        Toast.makeText(getBaseContext(), "Gps is turned off!! ",
//                Toast.LENGTH_SHORT).show();

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
