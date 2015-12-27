package com.ntier.android.GAEL1;
//TODO PUSH down features/coords from svr to phone



//import info.androidhive.camerafileupload.*;
//import info.androidhive.camerafileupload.AndroidMultiPartEntity.ProgressListener;
//import info.androidhive.camerafileupload.UploadActivity.UploadFileToServer;

import java.io.*;
import java.text.*;
import java.util.*;

import com.belladati.httpclientandroidlib.*;
import com.belladati.httpclientandroidlib.client.*;
import com.belladati.httpclientandroidlib.client.methods.HttpPost;
//import com.belladati.httpclientandroidlib.
import com.belladati.httpclientandroidlib.entity.mime.MultipartEntityBuilder;
import com.belladati.httpclientandroidlib.entity.mime.content.FileBody;
import com.belladati.httpclientandroidlib.entity.*;
import com.belladati.httpclientandroidlib.impl.client.DefaultHttpClient;
import com.belladati.httpclientandroidlib.util.EntityUtils;

import org.slf4j.*;

import android.app.*;
import android.content.*;
import android.database.ContentObserver;
import android.graphics.Color;
import android.location.*;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.*;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.*;
import android.widget.*;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;

import com.ntier.util.*;
//import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.*;
import com.google.android.gms.location.*;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.ntier.android.REST.WebServiceGet;
import com.ntier.android.SQLite.*;
import com.ntier.android.accounts.*;
import com.ntier.android.accounts.Contract.TableNames;
import com.ntier.android.util.*;
import com.ntier.android.util.AndroidMultiPartEntity.*;
import com.ntier.exception.*;
import com.ntier.rest.model.Deployment;
import com.ntier.util.*;

public class MainActivity extends FragmentActivity implements 
	LocationListener
	,GooglePlayServicesClient.ConnectionCallbacks
	,GooglePlayServicesClient.OnConnectionFailedListener
	,OnMapLongClickListener
	,OnMarkerClickListener //for popupmenu
	,android.widget.PopupMenu.OnMenuItemClickListener
{

//begin static vars

//begin final
static private final LoggerContext		mLoggerContext	= (LoggerContext)LoggerFactory.getILoggerFactory();
static private final ContextInitializer	mContextInitializer		= new ContextInitializer( mLoggerContext );
static private final Logger				mLog	= LoggerFactory.getLogger( MainActivity.class );
//static private final String SHARED_PREFS_FNAME = "GAEL.sharedprefs";

//end final

static private ContentResolver mContentResolver;
static private GoogleMap mGoogleMap;

static private LocationRequest mLocationRequest;// A request to connect to Location Services.
static private LocationClient mLocationClient;// Stores the current instantiation of the location client in this object.
static private Contract mContract;

/*private static final AdRequest adRequest = new AdRequest.Builder()
															.addTestDevice( AdRequest.DEVICE_ID_EMULATOR )
															.addTestDevice( "5068799E1EDFDD0E9764876294535B80" )    //xt912
															//.addTestDevice( "6763727286E4279F947874142F5992EE" )  //XT907
															.build();*/

//end static vars 

//begin instance vars
Provider GAELdb; 

private final TableObserver mTableObserver = new TableObserver(new Handler());
GAELUtility mGAELUtility;
private static Uri mLocationsUri;

//*? private MyBroadcastReceiver mBroadcastReceiver = new MyBroadcastReceiver();

//private AdView adView ;


//end instance vars


@Override
protected void onCreate( final Bundle savedInstanceState ){
	super.onCreate( savedInstanceState );
	
	NoNetworkOnMainException();
	
	
	mGAELUtility = GAELUtility.getInstance(this);

	setContentView( R.layout.activity_main );
	setUpMapIfNeeded();
	
   // Create a new global location parameters object
   mLocationRequest = LocationRequest.create().  //Set the update interval; Use high accuracy; 
   					  setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS). 
   					  setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY). 
   					  setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

//Create a new location client, using the enclosing class to handle callbacks.
   mLocationClient = new LocationClient(this, this, this);
   mLog.trace( "OnCreate:\tmLocationRequest:\t" + mLocationRequest );
	
	//adView = (AdView)findViewById(R.id.adView); // Look up the AdView as a resource. 
	//adView.loadAd(adRequest);

	mContract = new Contract(this);
	GAELdb = new Provider();
	mContentResolver = getContentResolver();
	mLocationsUri = mContract.toUri(TableNames.uLocations);


	//mLog.trace( Provider.status() );
	//myPolyLine();
}// onCreate



private void NoNetworkOnMainException(){
//FOR TESTING ONLY!!!
//http://www.lucazanini.eu/2012/android/the-android-os-networkonmainthreadexception-exception/	
	StrictMode.ThreadPolicy policy = 
			new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();
			StrictMode.setThreadPolicy(policy);
}

@Override
protected void onRestart(){
	super.onRestart();
	// Reload Logback log: http://stackoverflow.com/questions/3803184/setting-logback-appender-path-programmatically/3810936#3810936
	mLoggerContext.reset();

	try{ mContextInitializer.autoConfig(); } //I prefer autoConfig() over JoranConfigurator.doConfigure() so I don't need to find the file myself.
	catch( ch.qos.logback.core.joran.spi.JoranException e ){ e.printStackTrace(); }	
}//onRestart()

//@SuppressWarnings( "static-access" )
@Override
protected void onStart(){
	super.onStart();

	hasGPS();
	mGAELUtility.setMobileDataEnabled( true );

	mLog.trace( "onStart: ConnectGAEL: "  );

}//onStart

public void hasGPS(){
   LocationManager locMgr = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

   if ( !locMgr.isProviderEnabled( LocationManager.GPS_PROVIDER ) ){
//	   final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	   new AlertDialog.Builder(this)
	   		  .setMessage("Please Enable High Accuracy GPS")
	          .setCancelable(false)
	          .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	              @Override
					public void onClick(final DialogInterface dialog, final int id) {
	            	//mActivity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	              }
	          })
	          .setNegativeButton("Cancel GAEL", new DialogInterface.OnClickListener() {
	              @Override
					public void onClick(final DialogInterface dialog, final int id) {
	                   dialog.cancel();
	                   finish();
	              }
	          })
	          .show();
//   	builder.show();
   }//if
}//hasGPS()


@Override
protected void onResume(){
	super.onResume();
	mLocationClient.connect();
//	startPeriodicUpdates();
	mLog.trace( "onResume:\t" + mLocationClient );

	//adView.resume();
//	Uri locationsUri = mContract.toUri(TableNames.uLocations);
	
	mContentResolver.registerContentObserver( mLocationsUri, true, mTableObserver );
	
	  //re-register BroadcastReceiver
	  //intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
	  //this.registerReceiver(mBroadcastReceiver, intentFilter);
	  
	// *? 	  LocalBroadcastManager.getInstance(this)
	// *? 	  					   .registerReceiver(  mBroadcastReceiver, mIntentFilter);

/*	if (!hasCamera()) {
		//Toast toast = 
				Toast.makeText(cameraContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG).show();;
		//finish();
	}
*/
}// onResume()

@Override
protected void onPause(){
	super.onPause();
	mLog.trace( "onPause()" );
	//when on Pause, release camera in order to be used from other applications
	//releaseCamera();

	mLocationClient.disconnect();
	//adView.pause(); 
//	stopPeriodicUpdates();
	// *? LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
	mContentResolver.unregisterContentObserver(mTableObserver);
}// onPause()

@Override
protected void onStop(){
	super.onStop();
	mLog.trace( "onStop():\t" );
	GAELdb.shutdown();

    mLocationClient.disconnect();// After disconnect() is called, the client is considered "dead".

	mLoggerContext.stop();//flush log
}// onStop()

@Override
protected void onDestroy(){
	//adView.destroy();
	super.onDestroy();
	mLog.trace( "onDestroy():\t" );
	GAELdb.shutdown();
	
	try {
		
		// *? LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
		mContentResolver.unregisterContentObserver(mTableObserver);
		
	} catch (Exception e){} //do nothing, we are destroy'ing

	mLoggerContext.stop();
}// onDestroy()


//Handle results returned to the FragmentActivity by Google Play services
@Override
protected void onActivityResult( final int requestCode, final int resultCode, final Intent data) {
	String toastText = "Error: Unknown OnActivityResult!";
	
    // Decide what to do based on the original request code
    switch (requestCode) {
        case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :
   		if ( resultCode == Activity.RESULT_CANCELED ) 
   			toastText = "Google Play Services must be installed.";

   		break;

        case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
        case CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE:
        	switch(resultCode) {
        	case RESULT_OK:
        		toastText = "Image saved to:\n" + data.getData();
        		new UploadFileToServer().execute(); //TODO refactor this out of this MainActivity!
        		break;
        	case RESULT_CANCELED:
        		toastText = "Image capture cancelled!";
        		break;
        	default: toastText = "Image capture failed!";
 
        	}//switch(resultCode)
   		break;
        
        default: 
    }
    
    Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
    
 }//onActivityResult


private boolean servicesConnected() {// Check that Google Play services is available
	final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	final boolean servicesConnected = resultCode == ConnectionResult.SUCCESS;
	
	if ( !servicesConnected ){// Display an error dialog
		Dialog dialog = GooglePlayServicesUtil.getErrorDialog( resultCode, this, 0 );
		if ( dialog != null ){
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();
			errorFragment.setDialog( dialog );
			errorFragment.show( getSupportFragmentManager(), "GAEL" );
		}
	}
	
	return servicesConnected;
}//servicesConnected()


private void setUpMapIfNeeded(){ // https://developers.google.com/maps/documentation/android/map
	// Do a null check to confirm that we have not already instantiated the map.
	if ( mGoogleMap == null && servicesConnected() ) {
		mGoogleMap = ((MapFragment)getFragmentManager().findFragmentById( R.id.map )).getMap();
		// Check if we were successful in obtaining the map.
		if ( mGoogleMap == null ) mLog.error( "Failed to get map fragment id: " + R.id.map );
		else {
			mGoogleMap.setMyLocationEnabled( true );
			mGoogleMap.setOnMapLongClickListener( this );
			mGoogleMap.setOnMarkerClickListener(this);
			//mLog.trace( "setUpMapIfNeeded: googleMap: " + mGoogleMap  );
		}
	}
}// setUpMapIfNeeded()


private static void myPolyLine(){
	final Location location = mLocationClient.getLastLocation();
	final float WIDTH = 12;
	PolylineOptions rectOptions;
	Polyline polyline ;
/*		PolylineOptions rectOptions = new PolylineOptions()
        .add(new LatLng(37.806714136729525, -122.18934662640095))
        .add(new LatLng(37.807589877811665, -122.1829753741622 ))  
        .add(new LatLng(37.80158795575109, -122.18241278082132))  
        .add(new LatLng(37.80432208683976, -122.189650721848))  
        .color(Color.BLUE)
        .width(WIDTH)
        ;
		Polyline polyline = mGoogleMap.addPolyline(rectOptions);
*/
		
		final double deltaLat = 37.8053038,
					 deltaLon = -122.1841851,
					 
					 myLat = location.getLatitude(),
					 myLon = location.getLongitude();

		
		rectOptions = new PolylineOptions()
		
        .add(new LatLng(myLat + 37.806714136729525 - deltaLat, myLon + -122.18934662640095 - deltaLon))
        .add(new LatLng(myLat + 37.807589877811665 - deltaLat, myLon + -122.1829753741622  - deltaLon))  
        .add(new LatLng(myLat + 37.80158795575109 -  deltaLat,  myLon + -122.18241278082132 - deltaLon))  
        .add(new LatLng(myLat + 37.80432208683976 -  deltaLat,  myLon + -122.189650721848 - deltaLon))  
        .color(Color.BLUE)
        .width(WIDTH)
        ;

			
		
		polyline = mGoogleMap.addPolyline(rectOptions);

	// Get back the mutable Polyline
}//myPolyLine



private static void myPolyLine2(){
	final Location location = mLocationClient.getLastLocation();
	final float WIDTH = 12;
	PolylineOptions rectOptions;
	Polyline polyline ;
		
		final double deltaLat = 37.8053038,
					 deltaLon = -122.1841851,
					 
					 myLat = location.getLatitude(),
					 myLon = location.getLongitude();

		
/*		rectOptions = new PolylineOptions()
        .add(new LatLng(myLat + 37.806714136729525 - deltaLat, myLon + -122.18934662640095 - deltaLon))
        .add(new LatLng(myLat + 37.807589877811665 - deltaLat, myLon + -122.1829753741622  - deltaLon))  
        .add(new LatLng(myLat + 37.80158795575109 -  deltaLat,  myLon + -122.18241278082132 - deltaLon))  
        .add(new LatLng(myLat + 37.80432208683976 -  deltaLat,  myLon + -122.189650721848 - deltaLon))  
        .color(Color.BLUE)
        .width(WIDTH)
        ;
*/
		final double deltaLat2 = 37.8053038 - .0003000,
				 deltaLon2 = -122.1841851 ;

		PolylineOptions rectOptions2 = new PolylineOptions()
        .add(new LatLng(myLat + 37.806714136729525 - deltaLat2, myLon + -122.18934662640095 - deltaLon2))
        .add(new LatLng(myLat + 37.807589877811665 - deltaLat2, myLon + -122.1829753741622  - deltaLon2))  
        .color(Color.RED)
        .width(WIDTH)
        ;

		
		
		//polyline = mGoogleMap.addPolyline(rectOptions);
		polyline = mGoogleMap.addPolyline(rectOptions2);

	// Get back the mutable Polyline
}//myPolyLine

@Override
public boolean onCreateOptionsMenu( final Menu menu ){
	// Inflate the menu items for use in the action bar
	getMenuInflater().inflate( R.menu.main_activity_actions, menu );
	return super.onCreateOptionsMenu( menu );
}

//@SuppressWarnings( "static-access" )
@Override
public boolean onOptionsItemSelected( final MenuItem item ){
	// Handle presses on the action bar items
	switch ( item.getItemId() ){
	case R.id.action_settings:
		DemoServerIP();
		break;
	case R.id.action_email:
		mGAELUtility.sendEmail();
		break;
	case R.id.action_test:
		Test();
		break;
	case R.id.action_finish:
		finish();
		this.onDestroy();
		break;
	default:
	}// switch
	return super.onOptionsItemSelected( item );
}// onOptionsItemSelected

void DemoServerIP(){
	// get demoipaddress.xml view
	View promptsView = LayoutInflater.from(this).inflate(R.layout.demoipaddress, null);
	
	//AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( this);
	
	//alertDialogBuilder.setView(promptsView);
	
	final EditText userInput = (EditText) promptsView
			.findViewById(R.id.editTextDialogUserInput);

    String ServerIPaddr = mGAELUtility.getServerIPaddr(); 
    
    userInput.setText(ServerIPaddr);
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	// set dialog message
	//alertDialogBuilder
//    AlertDialog setServerIPaddr = 
    		new AlertDialog.Builder( this)
    		.setView(promptsView)
			.setCancelable(false)
			.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) 
							{	dialog.cancel(); }
					})
			.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							String ServerIPaddr = userInput.getText().toString();

							prefs.edit()
								.putString("ServerIPaddr", ServerIPaddr)
								//.commit();
								.apply();
							MainActivity.mContract = new Contract(getBaseContext());
								 }
					})
			.create()
			.show();
}//DemoServerIP

void Test(){
	//View testResults = LayoutInflater.from(this).inflate(R.layout.testresults, null);
	//this.setContentView(R.layout.testresults);
	NoNetworkOnMainException();

	String result = new WebServiceGet(mContract).get(Contract.URLGet.Test);
	Deployment deploy = new Gson().fromJson(result, Deployment.class);
	
	mLog.trace( "WebServiceGet(mContract).get(Contract.URLGet.Test):\t" + result);
}

/*Called by Location Services when the request to connect the
 * client finishes successfully. At this point, you can
 * request the current location or start periodic updates */
private static boolean FirstTimeConnected = true;
@Override public void onConnected(final Bundle bundle){ 
	startPeriodicUpdates();
	mLog.trace( "onConnected(bundle):\t" + bundle);
	
	if ( FirstTimeConnected ) {
	   final Location location = mLocationClient.getLastLocation();
	   mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng( new LatLng(location.getLatitude(), location.getLongitude())));
	   FirstTimeConnected = false;
		myPolyLine2();
		myPolyLine();

	}
}//onConnected

//Called by Location Services if the connection to the location client drops because of an error.
@Override public void onDisconnected(){ 
	mLog.trace( "onDisconnected");
	stopPeriodicUpdates();
}

//Called by Location Services if the attempt to Location Services fails.
@Override
public void onConnectionFailed(final ConnectionResult connectionResult) {
	mLog.trace( "onConnectionFailed(ConnectionResult:\t" +  connectionResult );
    /*
     * Google Play services can resolve some errors it detects.
     * If the error has a resolution, try sending an Intent to
     * start a Google Play services activity that can resolve
     * error.
     */
    if (connectionResult.hasResolution()) {
    	
        try {

            // Start an Activity that tries to resolve the error
            connectionResult.startResolutionForResult(
                    this,
                    LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

            //Thrown if Google Play services canceled the original PendingIntent
        } catch (IntentSender.SendIntentException e) { 
        	mLog.error( "connectionResult.startResolutionForResult: ", e ); 
    	}//catch
        
    } else {// If no resolution is available, display a dialog to the user with the error.
        showErrorDialog(connectionResult.getErrorCode());
    }
}//onConnectionFailed

//static private int nullLocs = 0 ;
@Override public void onLocationChanged(final Location location){ 
//	http://www.andygup.net/handle-null-values-when-using-androids-locationmanager/
    if(location == null)  mLog.warn("tracking null Locations: " + ++nullLocs);
}//onLocationChanged


//In response to a request to start updates, send a request to Location Services
private void startPeriodicUpdates(){ mLocationClient.requestLocationUpdates(mLocationRequest, this); }

//In response to a request to stop updates, send a request to Location Services
private void stopPeriodicUpdates(){ mLocationClient.removeLocationUpdates(this); }



private void showErrorDialog(final int errorCode ){//Show a dialog returned by Google Play services for the connection error code.
    // Get the error dialog from Google Play services
    Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
        errorCode,
        this,
        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

    // If Google Play services can provide an error dialog
    if (errorDialog != null) {
        // Create a new DialogFragment in which to show the error dialog
        ErrorDialogFragment errorFragment = new ErrorDialogFragment();
        // Set the dialog in the DialogFragment
        errorFragment.setDialog(errorDialog);
        // Show the error dialog in the DialogFragment
        errorFragment.show(getSupportFragmentManager(), "GAEL");
    }
}//showErrorDialog

static private int nullLocs = 0,
				   numLeaks = 0;//TODO for DEMO only!
private Marker myMarker;

@SuppressWarnings("boxing")
@Override
public void onMapLongClick( final LatLng latLng ){
/*	mGoogleMap.addMarker(new MarkerOptions()
   							.position(latLng)
   							.title("leak# " + ++numLeaks )
   							//.draggable(true)
   							.snippet("Lat,Long:\t" + new Double(latLng.latitude).toString() + ", " + new Double(latLng.longitude).toString() 
   									(new Date()).toString())
   							  )
   							.showInfoWindow();
*/
	
	Location location = mLocationClient.getLastLocation();
	//TODO: VERIFY VALIDITY OF LOCATION HERE	
    if(location == null)  throw new GaelException(GaelErrCode.NULL_LOCATION)
    									.set("#null Locations", ++nullLocs); 

    myMarker = mGoogleMap.addMarker(new MarkerOptions()
		.position(latLng)
		//.draggable(true)
		.title("Gaspipe leak: Lat,Lon:" + new DecimalFormat("#.##").format(latLng.latitude) + "," + new DecimalFormat("#.##").format(latLng.longitude) )
		.snippet( "\nElev:" + new DecimalFormat("#.##").format(location.getAltitude())
		  )) ;
    
    myMarker.showInfoWindow();


    location.setLatitude (latLng.latitude);
	location.setLongitude(latLng.longitude);

	ContentValues values = new ContentValues();
	values.put("insertTable", Contract.TableNames.uLocations.name());
	values.put("upJSON", new Gson().toJson(location, Location.class) );

	
//	mContentResolver.insert(mLocationsUri, values);
	
//see http://stackoverflow.com/questions/11961857/implementing-asyncqueryhandler	
//	AsyncQueryHandler handler = 
	new AsyncQueryHandler(mContentResolver){}
			.startInsert(-1, null, mLocationsUri, values);
	//	handler.startInsert(-1, null, mLocationsUri, values);
}//onMapLongClick

/*//private static void insertLocation (final LatLng latLng){
private static void insertLocation( Location loc ){
	ContentValues values = new ContentValues();
	values.put("insertTable", Contract.TableNames.uLocations.name());
//	Gson gson = new Gson();
//	String upJSON = new Gson().toJson(loc, Location.class);
//	values.put("upJSON", upJSON);
	values.put("upJSON", new Gson().toJson(loc, Location.class) );

	mContentResolver.insert(mLocationsUri, values);
}//insertLocation
*/
/*@SuppressWarnings({ "rawtypes", "unchecked" })
private static ContentValues marshallValues(Uri uri, Object obj){
    //This technique puts a POJO into ContentValues
    Parcel myParcel = Parcel.obtain();    
    WeakHashMap hm = new WeakHashMap(); 
    hm.put(uri.toString(), obj);
    myParcel.writeMap(hm);

    myParcel.setDataPosition(0);  
    ContentValues retVal = ContentValues.CREATOR.createFromParcel(myParcel);
    myParcel.recycle();
    return retVal;
}//marshallValues
*/
/*
private static JSONArray cur2Json(Cursor cursor) {
	//http://stackoverflow.com/questions/13070791/android-cursor-to-jsonarray
    JSONArray resultSet = new JSONArray();
    cursor.moveToFirst();
    while (cursor.isAfterLast() == false) {
        int totalColumn = cursor.getColumnCount();
        JSONObject rowObject = new JSONObject();   
        for (int i = 0; i < totalColumn; i++) {
            if (cursor.getColumnName(i) != null) {
                try {
                    rowObject.put(cursor.getColumnName(i),
                            cursor.getString(i));
                } catch (JSONException e) {
                    mLog.error( e.getMessage());
                }
            }
        }
        resultSet.put(rowObject);
        cursor.moveToNext();
    }
    
    return resultSet;
}//cur2Json

*/
/*public class MyBroadcastReceiver extends BroadcastReceiver {

	  @Override
	  public void onReceive(Context context, Intent intent) {
	   final String result = intent.getStringExtra(GAELSyncSvc.RESPONSE);
	   handleResponse(result);
	  }

}//MyBroadcastReceiver */


public class TableObserver extends ContentObserver {
//see http://developer.android.com/training/sync-adapters/running-sync-adapter.html	
    public TableObserver(Handler handler) { super(handler); }
	/*
     * Define a method that's called when data in the
     * observed content provider changes.
     * This method signature is provided for compatibility with
     * older platforms.
     
    @Override
    public void onChange(boolean selfChange) {
// Invoke the method signature available as of Android platform version 4.1, with a null URI.
        onChange(selfChange, null);
    }
*/
    
    /*
     * Define a method that's called when data in the
     * observed content provider changes.
     */
    @SuppressWarnings("synthetic-access")
	@Override
    public void onChange(boolean selfChange, Uri changeUri) {
    	Bundle extras = new Bundle();
    	extras.putString("DEMOServerIPaddr", "");
//Ask the framework to run your sync adapter. 
//To maintain backward compatibility, assume that changeUri is null.
        ContentResolver.requestSync(mContract.mSyncAccount, mContract.getAuthority(), extras );
    }
    
}//subclass TableObserver


/*class GaelContentObserver extends ContentObserver {		
public GaelContentObserver(Handler handler) { super(handler); }

@Override
public void onChange(boolean selfChange, Uri uri) {
	   Intent i = new Intent(MainActivity.this, MainActivity.class);
	   i.putExtra("DATA_URI", uri);
	   startActivity(i);
}		
}//GaelContentObserver
*/

@Override
public boolean onMarkerClick( Marker marker) {
	//Toast.makeText(this, "onMarkerClick!!!\n", Toast.LENGTH_SHORT).show();
	View popupView = getLayoutInflater().inflate(R.layout.popup, null);
	PopupMenu popup = new PopupMenu(this, popupView);
    popup.setOnMenuItemClickListener(this);
    popup.inflate(R.menu.popupmenu);
	    
    popup.show();

    return true;
}//onMarkerClick

private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100
						, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
GEOfinderFile geoFinderFile;//TODO REFACTOR!!

@Override
public boolean onMenuItemClick(MenuItem item) {
//	GEOfinderFile geoFinderFile;
	Intent cameraIntent;
    switch (item.getItemId()) {
        case R.id.Photo:
            cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //Does this allow for either?: cameraIntent = new Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA);
            //fileUri = Uri.fromFile(getOutputMediaFileURI(GFfiletype.jpg));
            geoFinderFile = new GEOfinderFile(GFfiletype.jpg);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, geoFinderFile.toUri() ); // set the image file name
            startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            
            return true;
        case R.id.Video:
        	//Toast.makeText(this, "Video", Toast.LENGTH_LONG).show();
            cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            geoFinderFile  = new GEOfinderFile(GFfiletype.mp4);
        	final int maxVideoLengthSeconds = 15, //seconds
        			  maxVideoSize = maxVideoLengthSeconds*1024*1024; //megabytes
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, geoFinderFile.toUri() ) // set the image file name
            	.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1) // set the video image quality to high
            	.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize)
            	.putExtra(MediaStore.EXTRA_DURATION_LIMIT, maxVideoLengthSeconds);

            startActivityForResult(cameraIntent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
        	return true;
        default:
        	Toast.makeText(this, "Error: default???", Toast.LENGTH_LONG).show();
    }
    return false;
}//onMenuItemClick


//ProgressBar progressBar;
//String filePath = null;
//private TextView txtPercentage;
//private ImageView imgPreview;
//private VideoView vidPreview;
//private Button btnUpload;
//long totalSize = 0;

class UploadFileToServer extends AsyncTask<Void, Integer, String> {//TODO refactor this out of this MainActivity!
	@Override
	protected void onPreExecute() {
		// setting progress bar to zero
		//progressBar.setProgress(0);
		//super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		// Making progress bar visible
		//progressBar.setVisibility(View.VISIBLE);

		// updating progress bar value
		//progressBar.setProgress(progress[0]);

		// updating percentage value
		//txtPercentage.setText(String.valueOf(progress[0]) + "%");
	}

	@Override
	protected String doInBackground(Void... params) {
		return uploadFile();
	}

	
	@SuppressWarnings("deprecation")
	private String uploadFile() {//TODO this uses Apache Client. change to Android.
		String responseString = null;

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);

		
		HttpEntity entity2 = MultipartEntityBuilder.create()
				.addPart("file", new FileBody(geoFinderFile.GFfile))//TODO refactor!
				.build();

								
		
		try {
			//totalSize = entity.getContentLength();
			httppost.setEntity(entity2);

			// Making server call
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity r_entity = response.getEntity();

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				// Server response
				responseString = EntityUtils.toString(r_entity);
			} else {
				responseString = "Error occurred! Http Status Code: "
						+ statusCode;
			}
			
			geoFinderFile.GFfile.delete();
			
		} catch (ClientProtocolException e) {
			responseString = e.toString();
		} catch (IOException e) {
			responseString = e.toString();
		}

		return responseString;

	}

	
	@Override
	protected void onPostExecute(String result) {

		// showing the server response in an alert dialog
		showAlert(result);

		super.onPostExecute(result);
		mLog.debug("Response from server: " + result);

		 

	}


}//class UploadFileToServer

private void showAlert(String message) {
	new AlertDialog.Builder(this)
	.setMessage(message).setTitle("Response from Servers")
			.setCancelable(false)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() 
				{public void onClick(DialogInterface dialog, int id) {} }
			)
	.create().show();
}//showAlert


//Here we store the file url as it will be null after returning from camera app
@Override
protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

//save file url in bundle as it will be null on screen orientation changes
    outState.putParcelable("fileUri", geoFinderFile.toUri());
}

Uri fileUri;
@Override
protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);

    // get the file url
    fileUri = savedInstanceState.getParcelable("fileUri");
}



}//END class MainActivity


class ErrorDialogFragment extends DialogFragment {//Define a DialogFragment to display the error dialog generated in showErrorDialog.
  // Global field to contain the error dialog
  private Dialog mDialog = null; 

/*    public ErrorDialogFragment() {
      super();
      mDialog = null;
  }
*/
  public void setDialog(final Dialog dialog) { mDialog = dialog; }

//This method must return a Dialog to the DialogFragment.
  @Override public Dialog onCreateDialog(final Bundle savedInstanceState ){ return mDialog; }
}//class ErrorDialogFragment

