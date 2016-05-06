package com.ereinecke.location.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private final long INTERVAL = 5000; // msec
    private final long MIN_TIME = 5000; // msec
    private final float MIN_DISTANCE = 20; // meters
    private TextView latitudeView;
    private TextView longitudeView;
    private TextView altitudeView;
    private TextView statusView;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build();

        latitudeView = (TextView) findViewById(R.id.latView);
        longitudeView = (TextView) findViewById(R.id.longView);
        altitudeView = (TextView) findViewById(R.id.altitudeView);
        statusView = (TextView) findViewById(R.id.warningView);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "Connected to Location Services");
        // Create a location request called mLocationRequest
        mLocationRequest = LocationRequest.create();
        // Set its priority to high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Update every second (1000 ms)
        mLocationRequest.setInterval(INTERVAL);
        // Call requestLocationUpdates in the API with this request

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        } else {
            // TODO: need to request permissions with API > 23
            Log.d(LOG_TAG, "failed permission check");
        }
        // Start with last known location
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            Log.d(LOG_TAG, "Last known location: " + location.toString());
            latitudeView.setText(Double.toString(location.getLatitude()));
            longitudeView.setText(Double.toString(location.getLongitude()));
            altitudeView.setText(Double.toString(location.getAltitude()));
            statusView.setText(getResources().getText(R.string.last_location_warning));
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "Connection to Location Services failed.");
    }

    @Override
    public void onConnectionSuspended(int result) {
        Log.d(LOG_TAG, "Connection to Location Services suspended.");

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, location.toString());
        latitudeView.setText(Double.toString(location.getLatitude()));
        longitudeView.setText(Double.toString(location.getLongitude()));
        altitudeView.setText(Double.toString(location.getAltitude()));
        // Show time since update
        statusView.setText("Live update");
    }
}
