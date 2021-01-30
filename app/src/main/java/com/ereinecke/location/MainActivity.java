/*
      Main Activity for Location app
      Substantial code from https://github.com/android/location-samples
      <p>
      Licensed under the Apache License, Version 2.0 (the "License");
      you may not use this file except in compliance with the License.
      You may obtain a copy of the License at
      <p>
      http://www.apache.org/licenses/LICENSE-2.0
      <p>
      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.
 */

package com.ereinecke.location;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.icu.text.DateFormat;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.Math.floor;

public class MainActivity extends AppCompatActivity implements
        LocationListener, ResultCallback<Status> {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34; // ??
    private final long INTERVAL = 5000; // msec
    private final long MIN_TIME = 5000; // msec
    private final float MIN_DISTANCE = 20; // meters
    private TextView latitudeView;
    private TextView longitudeView;
    private TextView altitudeView;
    private TextView bearingView;
    private TextView statusView;
    private TextView activitiesView;
    private FusedLocationProviderClient mFusedLocationClient;
    private ActivityRecognitionClient mActivityRecognitionClient;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager mLocationManager;
    protected Location mLastLocation;
    private PendingIntent pendingIntent;
    private Button requestUpdatesButton;
    private Button removeUpdatesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeView = (TextView) findViewById(R.id.latView);
        longitudeView = (TextView) findViewById(R.id.longView);
        altitudeView = (TextView) findViewById(R.id.altitudeView);
        bearingView = (TextView) findViewById(R.id.bearingView);
        statusView = (TextView) findViewById(R.id.statusView);
        activitiesView = (TextView) findViewById(R.id.detectedActivities);

        requestUpdatesButton = (Button) findViewById(R.id.request_activity_updates_button);
        removeUpdatesButton  = (Button) findViewById(R.id.remove_activity_updates_button);

        /* not used?
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         */
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        /*
        requestActivityTransitionUpdates(this);
        */

        }

    public void requestActivityUpdatesButtonHandler(View view) {
        /*
        if (!mGoogleApiClient.isConnected()) {
            // TODO: Convert Toasts to Snackbars
            Toast.makeText(this, getString(R.string.not_connected),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        requestActivityUpdates(this);
        */
        requestUpdatesButton.setEnabled(false);
        removeUpdatesButton.setEnabled(true);

    }

    public void removeActivityUpdatesButtonHandler(View view) {
        /*
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
        */
        requestUpdatesButton.setEnabled(true);
        removeUpdatesButton.setEnabled(false);
    }

    void requestActivityTransitionUpdates(final Context context) {
        ActivityTransitionRequest request = buildTransitionRequest();

        ArrayList detectedActivityList = null;

        Task task = ActivityRecognition.getClient(context)
                .requestActivityUpdates(INTERVAL, pendingIntent);

        task.addOnSuccessListener(
                new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                         /* Update UI with latest info
                       pendingIntent.putExtra(Constants.ACTIVITY_EXTRA, detectedActivityList);// Handle success...
                         */
                    }
                });
        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Handle failure...
                        Log.d(LOG_TAG, "Exception in requestAtivityUpdates" + e);
                    }
                });
    }

    ActivityTransitionRequest buildTransitionRequest() {
        List transitions = new ArrayList<>();
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.ON_FOOT)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.ON_FOOT)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.ON_BICYCLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.ON_BICYCLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
        Log.d(LOG_TAG, transitions.toString());
        return new ActivityTransitionRequest(transitions);
    }


    protected synchronized void buildGoogleApiClient() {
        /*
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    */
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }



    /**
     * Provides a simple way of getting a device's location and is well suited for
     * applications that do not require a fine-grained location and that do not need location
     * updates. Gets the best and most recent location currently available, which may be null
     * in rare cases when a location is not available.
     *
     * Note: this method should be called after location permission has been granted.
     */
    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Get location and time stamp
                            mLastLocation = task.getResult();
                            Log.d(LOG_TAG, "Location object: " + mLastLocation.toString());
                            Date dateStamp = new Date(mLastLocation.getTime());
                            String dateStr = DateFormat.getDateTimeInstance().format(dateStamp);

                            // Set location data in textviews
                            Log.d(LOG_TAG, "Last known location: " + mLastLocation.toString());
                            latitudeView.setText(getResources().getString(R.string.lat_long_result,
                                    mLastLocation.getLatitude()));
                            longitudeView.setText(getResources().getString(R.string.lat_long_result,
                                    mLastLocation.getLongitude()));
                            altitudeView.setText(formatAltitude(mLastLocation.getAltitude()));
                            bearingView.setText(formatBearing(mLastLocation.getBearing()));
                            dateStr = getResources().getText(R.string.live_update) + dateStr;
                            statusView.setText(dateStr);
                        } else {
                            Log.w(LOG_TAG, "getLastLocation:exception", task.getException());
                            showSnackbar(getString(R.string.no_location_detected));
                        }
                    }
                });
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(LOG_TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i(LOG_TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(LOG_TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
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

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLastLocation();
        }
        // mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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
    public void onLocationChanged(@NonNull Location location) {
        // TODO: factor this out to displayLocation(Location location)
        // Here and in getLastLocation?
        Date dateStamp = new Date(mLastLocation.getTime());
        String dateStr = DateFormat.getDateTimeInstance().format(dateStamp);

        Log.d(LOG_TAG, "Last known location: " + mLastLocation.toString());
        latitudeView.setText(getResources().getString(R.string.lat_long_result,
                mLastLocation.getLatitude()));
        longitudeView.setText(getResources().getString(R.string.lat_long_result,
                mLastLocation.getLongitude()));
        altitudeView.setText(formatAltitude(mLastLocation.getAltitude()));
        bearingView.setText(formatBearing(mLastLocation.getBearing()));
        dateStr = getResources().getText(R.string.live_update) + dateStr;
        statusView.setText(dateStr);
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

    /**
     * Take altitude in meters and convert to a display string with meters and feet
     *
     * @param altitude
     * @return string with altitude in meters and feet
     */
    private String formatAltitude(double altitude) {
        String alt = "";
        if (altitude != 0) {
            alt = getResources().getString(R.string.altitude_result,
                    altitude, metersToFeet(altitude));
        } else {
            alt = getString(R.string.not_available);
        }
        Log.d(LOG_TAG, "Altitude: " + alt);
        return alt;
    }

    /**
     * Take bearing in degrees and convert to a display string with degrees and cardinal point
     *
     * @param bearing
     * @return string with altitude in meters and feet
     */
    private String formatBearing(double bearing) {
        String bear = getResources().getString(R.string.bearing_result,
                bearing, bearingToCardinal(bearing));

        Log.d(LOG_TAG, "Bearing: " + bear);
        return bear;
    }

    /**
     * Returns the string with the cardinal points to the third level
     * (e.g. ENE).  The return sting will always have three char.
     *
     * @param bearing
     * @return String with the alpha compass bearing to 16ths.
     */
    private String bearingToCardinal (double bearing) {
        String cardinal;

        switch ((int) floor(bearing/16)) {
            case  0: cardinal = " N "; break;
            case  1: cardinal = "NNE"; break;
            case  2: cardinal = " NE"; break;
            case  3: cardinal = "ENE"; break;
            case  4: cardinal = " E "; break;
            case  5: cardinal = "ESE"; break;
            case  6: cardinal = " SE"; break;
            case  7: cardinal = "SSE"; break;
            case  8: cardinal = " S "; break;
            case  9: cardinal = "SSW"; break;
            case 10: cardinal = " SW"; break;
            case 11: cardinal = "WSW"; break;
            case 12: cardinal = " W "; break;
            case 13: cardinal = "WNW"; break;
            case 14: cardinal = " NW"; break;
            case 15: cardinal = "NNW"; break;
            default: cardinal = "";
        }
        return cardinal;
    }

    private double metersToFeet(double meters) {
        return meters * 3.28084;
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(R.id.main_activity_container);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }
}
