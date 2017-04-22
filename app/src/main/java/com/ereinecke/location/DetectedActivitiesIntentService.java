package com.ereinecke.location;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;

import java.util.ArrayList;

/**
 * Created by ereinecke on 6/1/16.
 */
public class DetectedActivitiesIntentService extends IntentService {
    protected static final String TAG = "detection_is";
    private static final String LOG_TAG = DetectedActivitiesIntentService.class.getSimpleName();

    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     * @param intent The Intent is provided (inside a PendingIntent) when requestActivityUpdates()
     *               is called.
     * Google Play Services calls this once it has analyzed the sensor data
     *
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        ArrayList detectedActivityList = null;
        Log.d(LOG_TAG, "in onHandleIntent()");

        Intent returnIntent = new Intent(Constants.BROADCAST_ACTION);

        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            if(result != null){
                Log.d(LOG_TAG, "ActivityRecognitionResult: " + result.toString());
                detectedActivityList = (ArrayList) result.getProbableActivities();
            } else {
                Log.d(LOG_TAG, "ActivityRecognitionResult is null.");
            }
        } else {
            Log.d(LOG_TAG, "Intent has no result");
        }

        if (detectedActivityList != null) {
            returnIntent.putExtra(Constants.ACTIVITY_EXTRA, detectedActivityList);
            LocalBroadcastManager.getInstance(this).sendBroadcast(returnIntent);
        } else {
            Log.d(LOG_TAG, "detectedActivityList is null.");
        }
    }
}
