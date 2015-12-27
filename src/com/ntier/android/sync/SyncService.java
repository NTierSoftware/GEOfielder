//See http://developer.android.com/training/sync-adapters/creating-sync-adapter.html#CreateSyncAdapterService
package com.ntier.android.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/* Define a Service that returns an IBinder for the sync adapter class, 
 * allowing the sync adapter framework to call onPerformSync(). */
public class SyncService extends Service {
	private static final Logger	mLog	= LoggerFactory.getLogger( SyncService.class );

//{android.os.Debug.waitForDebugger();}
    // Storage for an instance of the sync adapter
    private static SyncAdapter sSyncAdapter = null;
    // Object to use as a thread-safe lock
    private static final Object sSyncAdapterLock = new Object();
    

    // Instantiate the sync adapter object.
    @Override
    public void onCreate() {
    	mLog.debug("SyncService.onCreate()");
    	
    	
/* Create the sync adapter as a singleton. Set the sync adapter as syncable
 * Disallow parallel syncs */
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) 
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            
        }
    }

// Return an object that allows the system to invoke the sync adapter.
    @Override
    public IBinder onBind(Intent intent) {
    	
    	mLog.debug("SyncService.onBind().intent :\t" + intent) ;
    	

    	/*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
    	IBinder ibinder = sSyncAdapter.getSyncAdapterBinder();
    	mLog.debug("SyncService.onBind().ibinder :\t" + ibinder) ;

        return ibinder;
    }
}//end class SyncService
