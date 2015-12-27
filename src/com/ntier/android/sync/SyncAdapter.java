//see http://developer.android.com/training/sync-adapters/creating-sync-adapter.html
package com.ntier.android.sync;

import org.json.*;
import org.slf4j.*;

import android.accounts.Account;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;

import com.ntier.android.REST.WebServicePost;
import com.ntier.android.accounts.Contract;
import com.ntier.android.accounts.Contract.TableNames;
import com.ntier.exception.*;

//Handle the transfer of data between a server and an app, using the Android sync adapter framework.
public class SyncAdapter extends AbstractThreadedSyncAdapter {
//{android.os.Debug.waitForDebugger();}
	static private final LoggerContext		mLoggerContext	= (LoggerContext)LoggerFactory.getILoggerFactory();
	static private final ContextInitializer	mContextInitializer		= new ContextInitializer( mLoggerContext );

	private static final Logger mLog	= LoggerFactory.getLogger( SyncAdapter.class );
    static private Contract mContract;
    
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContract = new Contract(context);
    }

/* Set up the sync adapter. This form of the constructor maintains 
 * compatibility with Android 3.0  and later platform versions */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs)
    { 
    	super(context, autoInitialize, allowParallelSyncs);
    	mContract = new Contract(context);
    }
    
/* The entire sync adapter runs in a background thread, so you don't 
 * have to set up your own background processing. */
    
    @SuppressWarnings("resource")
	@Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {

    	//String keepAlive = System.getProperty("http.keepAlive");

    	Cursor myCursor = null ;
    	String result = "Nothing to SYNC.";
    	mLog.debug("onPerformSync");
		try {
			
//	    	Uri uri = mContract.getTableURI(TableNames.uLocations);
	    	Uri uri = mContract.toUri(TableNames.uLocations);
	    	
			myCursor = provider.query(uri, null, null, null, null);

			if ( myCursor != null ) {
				JSONArray JSON = cur2Json(myCursor);
				myCursor.close();
				mLog.debug("JSON:\t " + JSON.toString() );
				result = new WebServicePost(mContract).post(JSON);
			}
	    	
		} catch (Exception ex) { 
			GaelException SYNC_FAILED = 
				 new GaelException(GaelErrCode.SYNC_FAILED)
						.set("remoteException", ex);
			mLog.error(SYNC_FAILED.toString());
			throw SYNC_FAILED;
		}//catch

  		mLog.debug("Sync Result:\t " + result);
    }//onPerformSync
    

    private static JSONArray cur2Json(Cursor cursor) {
    	//http://stackoverflow.com/questions/13070791/android-cursor-to-jsonarray
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            final int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject(); 
            int i;
            for (  i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                    	String getcol = cursor.getColumnName(i),
                    		   getstr = cursor.getString(i);
                    		   
                    	
                    	mLog.debug("ColumnName(i):\t " + getcol + "\t: " + getstr);
                        rowObject.put(
                        		    getcol,
                        			getstr 
                        			 );
                          
                    } catch (JSONException e) {
                        mLog.error( e.getMessage());
                    }
                }
            }//for
            mLog.debug("columns i:\t " + i + "\totalColumn:\t " + totalColumn); 
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
               
        return resultSet;
    }//cur2Json

}//class SyncAdapter 