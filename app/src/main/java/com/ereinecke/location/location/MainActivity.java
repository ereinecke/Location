package com.ereinecke.location.location;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        LocationListener, ResultCallback<Status> {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private final long INTERVAL = 5000; // msec
    private final long MIN_TIME = 5000; // msec
    private final float MIN_DISTANCE = 20; // meters
    private TextView latitudeView;
    private TextView longitudeView;
    private TextView altitudeView;
    private TextView statusView;
    private TextView activitiesView;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationManager mLocationManager;
    private Button requestUpdatesButton;
    private Button removeUpdatesButton;
    protected ActivityDetectionBroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeView = (TextView) findViewById(R.id.latView);
        longitudeView = (TextView) findViewById(R.id.longView);
        altitudeView = (TextView) findViewById(R.id.altitudeView);
        statusView = (TextView) findViewById(R.id.statusView);
        activitiesView = (TextView) findViewById(R.id.detectedActivities);

        requestUpdatesButton = (Button) findViewById(R.id.request_activity_updates_button);
        removeUpdatesButton  = (Button) findViewById(R.id.remove_activity_updates_button);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mBroadcastReceiver = new ActivityDetectionBroadcastReceiver();

        buildGoogleApiClient();
        }

    public void requestActivityUpdatesButtonHandler(View view) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent())
                .setResultCallback(this);
        requestUpdatesButton.setEnabled(false);
        removeUpdatesButton.setEnabled(true);

    }

    public void removeActivityUpdatesButtonHandler(View view) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        // Remove all activity updates for the PendingIntent that was used to request activity
        // updates.
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityDetectionPendingIntent())
                .setResultCallback(this);
        requestUpdatesButton.setEnabled(true);
        removeUpdatesButton.setEnabled(false);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register the broadcast receiver that informs this activity of the DetectedActivity
        // object broadcast sent by the intent service.
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(Constants.BROADCAST_ACTION));
    }

    @Override
    public void onPause() {
        // Unregister the broadcast receiver that was registered during onResume()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
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
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d(LOG_TAG, "Last known location: " + mLastLocation.toString());
            latitudeView.setText(getResources().getString(R.string.lat_long_result, mLastLocation.getLatitude()));
            longitudeView.setText(getResources().getString(R.string.lat_long_result, mLastLocation.getLongitude()));
            setAltitude(mLastLocation.getAltitude());
            statusView.setText(getResources().getText(R.string.last_location_warning));
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "Connection to Location Services failed, error: " +
                connectionResult.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int result) {
        Log.d(LOG_TAG, "Connection to Location Services suspended.");
        mGoogleApiClient.connect();
    }

    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.d(LOG_TAG, "Successfully added activity detection");
        } else {
            Log.d(LOG_TAG, "Error adding or removing activity detection: " +
                status.getStatusMessage());
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        // Log.d(LOG_TAG, location.toString());
        latitudeView.setText(getResources().getString(R.string.lat_long_result, location.getLatitude()));
        longitudeView.setText(getResources().getString(R.string.lat_long_result, location.getLongitude()));
        setAltitude(location.getAltitude());
        // Show time since update
        statusView.setText("Live update");
    }

    /**
     * Returns a human readable String corresponding to a detected activity type.
     */
    public String getActivityString(int detectedActivityType) {
        Resources resources = this.getResources();
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.UNKNOWN:
                return resources.getString(R.string.unknown);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            default:
                return resources.getString(R.string.unidentifiable_activity, detectedActivityType);
        }
    }

    private void setAltitude(double altitude) {
        if (altitude != 0) {
            String alt = getResources().getString(R.string.altitude_result,
                    altitude, metersToFeet(altitude));
            Log.d(LOG_TAG, "Altitude:" + alt);
            altitudeView.setText(alt);
        } else {
            altitudeView.setText(getString(R.string.not_available));
        }
    }

    private double metersToFeet(double meters) {
        return meters * 3.28084;
    }


    /* Receiver for intents sent by DetectedActivitiesIntentService via a sendBroadcast().
     * Receives a list of one or more DetectedActivity objects associated with with the
     * current state of the device.
     */
    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
        protected static final String TAG = "receiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<DetectedActivity> updatedActivities =
                    intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);

            String strStatus = "";
            for (DetectedActivity thisActivity: updatedActivities) {
                strStatus += getActivityString(thisActivity.getType()) + ": " +
                        thisActivity.getConfidence() + "%\n";
            }
            activitiesView.setText(strStatus);
        }
    }
}
