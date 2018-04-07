package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;

/**
 * Created by Steven on 7/04/2018.
 */

public class Geofencing implements ResultCallback<Status> {

    private static final long GEOFENCE_TIMEOUT = 24*60*60*1000;
    private static final float GOEFENCE_RADIUS = 50;
    private static final String LOG_TAG = Geofencing.class.getSimpleName();
    private Context mContext;
    private GoogleApiClient mClient;
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mPendingIntent;

    // DONE (1) Create a Geofencing class with a Context and GoogleApiClient constructor that
    // initializes a private member ArrayList of Geofences called mGeofenceList
    public Geofencing(Context context, GoogleApiClient client){
        mContext = context;
        mClient = client;
        mGeofenceList = new ArrayList<>();
        mPendingIntent = null;
    }

    // DONE (2) Inside Geofencing, implement a public method called updateGeofencesList that
    // given a PlaceBuffer will create a Geofence object for each Place using Geofence.Builder
    // and add that Geofence to mGeofenceList
    public void updateGeofencesList(PlaceBuffer placeBuffer){
        if (placeBuffer == null || placeBuffer.getCount() == 0) return;
        for (Place place : placeBuffer){
            String id = place.getId();
            double longitude = place.getLatLng().longitude;
            double latitude = place.getLatLng().latitude;
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(id)
                    .setExpirationDuration(GEOFENCE_TIMEOUT)
                    .setCircularRegion(latitude, longitude, GOEFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
            mGeofenceList.add(geofence);
        }
    }

    // DONE (3) Inside Geofencing, implement a private helper method called getGeofencingRequest that
    // uses GeofencingRequest.Builder to return a GeofencingRequest object from the Geofence list
    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest.Builder geofencingRequest = new GeofencingRequest.Builder();
        // defines what happens if the device was already inside one of the geofences
        geofencingRequest.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        geofencingRequest.addGeofences(mGeofenceList);
        return geofencingRequest.build();
    }

    // DONE (5) Inside Geofencing, implement a private helper method called getGeofencePendingIntent that
    // returns a PendingIntent for the GeofenceBroadcastReceiver class
    private PendingIntent getGeofencePendingIntent(){
        if (mPendingIntent != null){
            return mPendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return mPendingIntent;
    }

    // DONE (6) Inside Geofencing, implement a public method called registerAllGeofences that
    // registers the GeofencingRequest by calling LocationServices.GeofencingApi.addGeofences
    // using the helper functions getGeofencingRequest() and getGeofencePendingIntent()
    public void registerAllGeofences(){
        if (mClient == null || !mClient.isConnected() || mGeofenceList == null
                || mGeofenceList.size() == 0) {
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(mClient, getGeofencingRequest(),
                    getGeofencePendingIntent())
                    .setResultCallback(this); // need to implement resultcallback
        } catch (SecurityException e){
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.e(LOG_TAG, "Error: " + status.getStatus().toString());
    }

    // DONE (7) Inside Geofencing, implement a public method called unRegisterAllGeofences that
    // unregisters all geofences by calling LocationServices.GeofencingApi.removeGeofences
    // using the helper function getGeofencePendingIntent()
    public void unregisterAllGeofences(){
        if (mClient == null || !mClient.isConnected()) {
            return;
        }
        try {
            LocationServices.GeofencingApi.removeGeofences(mClient, getGeofencePendingIntent())
                    .setResultCallback(this);
        } catch (SecurityException e){
            Log.e(LOG_TAG, e.getMessage().toString());
        }
    }

}
