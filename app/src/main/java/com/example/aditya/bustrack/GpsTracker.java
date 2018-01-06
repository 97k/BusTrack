package com.example.aditya.bustrack;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;


/**
 * Created by aditya on 12/27/17.
 */

public class GpsTracker extends Service implements LocationListener {

    public static final String LOG_TAG = GpsTracker.class.getSimpleName();
    public static final int MIN_UPDATE_DIST = 10;
    public static final int UPDATE_TIME_INTERVAL = 1000 * 60;

    private Context mContext;
    private Location mLocation;
    private LocationManager mLocationManager;

    // Bools
    private boolean canGetLocation = false;
    private boolean isGpsEnabled = false;
    private boolean networkConnectivity = false;

    double latitue;
    double longitude;


    public GpsTracker(Context context) {
        this.mContext = context;
        checkAndGetLocation();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public Location checkAndGetLocation() {
        try {
            mLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            isGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            networkConnectivity = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGpsEnabled && !networkConnectivity) {
                return null;
            } else {
                this.canGetLocation = true;
                if (networkConnectivity) {
                    if (!checkPermission())
                        return null;

                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_TIME_INTERVAL, MIN_UPDATE_DIST, this);
                    if (mLocationManager != null) {
                        mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (mLocation != null) {
                            latitue = mLocation.getLatitude();
                            longitude = mLocation.getLongitude();
                        }
                    }
                }
                if (isGpsEnabled) {
                    if (mLocation == null) {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_TIME_INTERVAL, MIN_UPDATE_DIST, this);
                        if (mLocationManager != null) {
                            mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (mLocation != null) {
                                latitue = mLocation.getLatitude();
                                longitude = mLocation.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mLocation;
    }

    public void stopGps(){
        if(checkPermission()){
            mLocationManager.removeUpdates(GpsTracker.this);
        }
    }
    public double getLatitue(){
        if (mLocation!=null)
            latitue = mLocation.getLatitude();
        return latitue;
    }
    public double getLongitude(){
        if (mLocation!=null){
            longitude = mLocation.getLongitude();
        }
        return longitude;
    }

    public boolean canGetLocation(){
        return this.canGetLocation;
    }

    public boolean checkPermission() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return false;
        else
            return true;
    }

    public void showAlertDialog(){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(getString(R.string.gps_dialog_title));
        dialog.setMessage(getString(R.string.gps_enable_message));
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        dialog.show();
    }
}
