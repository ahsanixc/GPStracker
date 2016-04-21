package com.example.maaz.gpsparse;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Maaz on 09-Jan-16.
 */
public class SmsListener extends BroadcastReceiver implements LocationListener {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    // flags for GPS, Network status
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;

    String msg = null;
    String url = null;
    String Number = null;
    String userID = null;
    Context mContext = null;

    private LocationManager locationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (intent.getAction().equals(SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }

                String s = messages[0].getMessageBody(); //saves the message recieved
                Number = messages[0].getOriginatingAddress(); //saves the number of sender

                //Checks for the Code Word and if yes do stuff
                if (s.charAt(0) == 'G' && s.charAt(1) == 'P' && s.charAt(2) == 'S') {

                    userID = s.substring(4,13);

                    Toast.makeText(context, "Sending Location!"+userID, Toast.LENGTH_LONG).show();

                    locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);

                    // getting GPS status
                    isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                    // getting network status
                    ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
                    NetworkInfo.State mobile = conMan.getNetworkInfo(0).getState();
                    NetworkInfo.State wifi = conMan.getNetworkInfo(1).getState();
                    if (mobile == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTED) {
                        isNetworkEnabled = true;
                    }

                    Log.d("GPS, Network = ", isGPSEnabled + ", " + isNetworkEnabled);

                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
//                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, this); //sec, meters
                            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, Looper.myLooper());
                        } else {
                            Log.d("2", "onCreate: GPS");
//                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this); //sec, meters
                            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, Looper.myLooper());
                        }
                    }
                }
                else if ((s.charAt(0) == 'S' || s.charAt(0) == 's') && s.charAt(1) == 't' && s.charAt(2) == 'a' && s.charAt(3) == 't' && s.charAt(4) == 'u' && s.charAt(5) == 's') {
                    Intent batteryIntent = context.getApplicationContext().registerReceiver(null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                    int level = batteryIntent.getIntExtra("level", -1);

                    locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
                    // getting GPS status
                    isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    // getting network status
                    ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
                    NetworkInfo.State mobile = conMan.getNetworkInfo(0).getState();
                    NetworkInfo.State wifi = conMan.getNetworkInfo(1).getState();
                    if (mobile == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTED) {
                        isNetworkEnabled = true;
                    }

                    if (level >= 0) {
                        if(isGPSEnabled == true && isNetworkEnabled == true) {
                            SendSMS("Battery: " + level + "%\nGPS: Enabled\nNetwork: Enabled");
                        }
                        else if(isGPSEnabled == true && isNetworkEnabled == false) {
                            SendSMS("Battery: " + level + "%\nGPS: Enabled\nNetwork: Disabled");
                        }
                        else if(isGPSEnabled == false && isNetworkEnabled == true) {
                            SendSMS("Battery: " + level + "%\nGPS: Disabled\nNetwork: Enabled");
                        }
                        else {
                            SendSMS("Battery: " + level + "%\nGPS: Disabled\nNetwork: Disabled");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        if (latitude != 0.0 && longitude != 0.0) {

            Toast.makeText(mContext, "Location Found!", Toast.LENGTH_LONG).show();
            findLoc(latitude, longitude);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        //GPS Not ENABLED
        SmsManager sms= SmsManager.getDefault();
        try {
            sms.sendTextMessage("+923452632931", null, "Provider Disabled!\nPlease enable Location and Data on the Device", null, null);
            sms.sendTextMessage(Number, null, "Provider Disabled!\nPlease enable Location and Data on the Device", null, null);
        } catch (Exception e) {
            sms.sendTextMessage("+923452632931", null, "SMS Sending Problem!", null, null);
            e.printStackTrace();
        }
    }

    public void findLoc(final double lat, final double log) {

        final Handler handler = new Handler();
        msg = null;
        url = null;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("1111", "Geocoder ");
                Geocoder gcd = new Geocoder(mContext, Locale.getDefault());
                List<Address> addresses;
                try {
                    addresses = gcd.getFromLocation(lat, log, 1);
                    if (addresses.size() > 0) {
                        String address = addresses.get(0).getAddressLine(0);
                        String area = addresses.get(0).getSubLocality();
                        String city = addresses.get(0).getLocality();
                        String country = addresses.get(0).getCountryName();

                        msg = "Address Line: "
                                + address + "\n"
                                + "Area: "
                                + area + "\n"
                                + "City: "
                                + city + "\n"
                                + "Country: "
                                + country;
//                                + "\nLatitude: " + lat +"\nLongitude: " +log
//                                + "\n\nURL: https://www.google.com/maps/search/" + lat + "," + log;

                        if (address == null && area == null && city == null && country == null) {
                            msg = null;
                        }
                    }
                    else {
                        msg = null;
                    }
                } catch (IOException e) {
                    msg = null;
                    e.printStackTrace();
                }

                url = "URL: https://www.google.com/maps/search/" + lat + "," + log;

                if(msg != null) {
                    SendSMS(msg);
                    SendSMS(url);
                    Log.d("Msg ", msg);
                    Log.d("URL ", url);
                }
                else {
                    SendSMS(url);
                    Log.d("URL ", url);
                }

                final ParseGeoPoint point = new ParseGeoPoint(lat, log);
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Coordinates");
                query.whereEqualTo("User", userID);
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
                                obj.put("User", userID);
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
    }

    public void SendSMS(String message) {
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(Number, null, message, null, null);
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

