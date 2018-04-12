package com.shaneaustrie.shooterscout.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.UUID;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by austrie on 4/11/18.
 */

public class DeviceInfo {

    @SuppressLint("MissingPermission")
    public String getUID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    @SuppressLint("MissingPermission")
    public Location getLocation(Context context) {
        try {
            Location location = null;
            double latitude, longitude = -1;
            LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isGPSEnabled) {
                if (location == null) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 80, new MyLocationListener());
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
//                            latitude = location.getLatitude();
//                            longitude = location.getLongitude();

                            return location;
                        }
                    }
                }
            } else if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 80, new MyLocationListener());
                Log.v("Network", "Network is enabled");
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        Log.v("LocationTracker", "Location : " + latitude + ", " + longitude);
                        return location;
                    } else {
                        Log.v("LocationTracker", "Location is null");
                    }
                } else {
                    Log.v("LocationTracker", "Location manager is null");
                }
            }
        } catch(Exception e) {
            System.out.println(e);
        }

        return null;
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {

//        /*------- To get city name from coordinates -------- */
//            String cityName = null;
//            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
//            List<Address> addresses;
//            try {
//                addresses = gcd.getFromLocation(loc.getLatitude(),
//                        loc.getLongitude(), 1);
//                if (addresses.size() > 0) {
//                    System.out.println(addresses.get(0).getLocality());
//                    cityName = addresses.get(0).getLocality();
//                }
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
//            String s = longitude + "\n" + latitude + "\n\nMy Current City is: "
//                    + cityName;
//            editLocation.setText(s);
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
}
