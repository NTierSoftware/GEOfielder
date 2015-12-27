package com.ntier.android.util;

//Defines app-wide constants and utilities
public final class LocationUtils {
    // Debugging tag for the application
    public static final String APPTAG = "LocationSample",

    // Name of shared preferences repository that stores persistent state
		SHARED_PREFERENCES = "com.example.android.location.SHARED_PREFERENCES",

    // Key for storing the "updates requested" flag in shared preferences
		KEY_UPDATES_REQUESTED = "com.example.android.location.KEY_UPDATES_REQUESTED";

//Define a request code to send to Google Play services. This code is returned in Activity.onActivityResult
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000,

//Constants for location update parameters
    // Milliseconds per second
    						MILLISECONDS_PER_SECOND = 1000,

    // The update interval
    						UPDATE_INTERVAL_IN_SECONDS = 5,

    // A fast interval ceiling
    						FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS,

    // A fast ceiling of update intervals, used when the app is visible
            FAST_INTERVAL_CEILING_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;


}

