package com.ntier.android.SQLite;

import java.util.*;

import org.slf4j.*;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.net.Uri;

import com.ntier.android.accounts.*;
import com.ntier.android.util.GAELUtility;
import com.ntier.exception.*;

// @author Heavily modified from Erik Hellman, "Android Programming Pushing the Limits"
@SuppressWarnings("resource") //warnings from db.close
public class Provider extends ContentProvider{
//{android.os.Debug.waitForDebugger();}

public static GaelDbHelper	GAELdatabase;

private static final Logger	mLog	= LoggerFactory.getLogger( Provider.class );
private ContentResolver mContentResolver;
static private Contract mContract;
@Override
public boolean onCreate(){
	final Context context = getContext();
	mContentResolver = context.getContentResolver();
	GAELdatabase = new GaelDbHelper( context );
	mContract = new Contract(context);
    return true;

}//onCreate


@Override
public Cursor query( final Uri uri, String[] projection,
							String selection, String[] selectionArgs,
							String sortOrder ){
//{android.os.Debug.waitForDebugger();}	

//	final String queryTable = mContract.getTableName(uri),
	final String queryTable = mContract.toTableNames(uri).name(),
			     groupBy = null
				 ,having = null;

	//SQLiteDatabase db = GAELdatabase.getReadableDatabase();
	
	mLog.debug("Provider.queryTable:\t" + queryTable);
	
	Cursor cursor = GAELdatabase.getReadableDatabase()
					.query( queryTable, 
							 projection,
							 selection, 
							 selectionArgs,
							 groupBy, 
							 having, 
							 sortOrder );
//see http://stackoverflow.com/questions/14002022/android-sq-lite-closed-exception
//see http://stackoverflow.com/questions/23293572/android-cannot-perform-this-operation-because-the-connection-pool-has-been-clos
	//db.close();
	return cursor;
}//query

@Override public String getType( final Uri uri ){ return new String(); } //stub

//@SuppressWarnings("unused")
//private static Uri doInsert( final Uri uri, final ContentValues values, final SQLiteDatabase database ){ return uri;}


@Override
public Uri insert( final Uri uri, final ContentValues values ){
	String insertTable = values.getAsString("insertTable");
	//need to remove in order to re-use values in db.insert();
	values.remove("insertTable");

//	SQLiteDatabase db = GAELdatabase.getWritableDatabase();
    long rowID = GAELdatabase
    				.getWritableDatabase()
    				.insert( insertTable, "", values);
  //see http://stackoverflow.com/questions/14002022/android-sq-lite-closed-exception
  //see http://stackoverflow.com/questions/23293572/android-cannot-perform-this-operation-because-the-connection-pool-has-been-clos
	//db.close();

    if (rowID <= 0){
		GaelException INSERT_FAILED = new GaelException(GaelErrCode.INSERT_FAILED)
		.set("URI", uri)
		.set("insertTable", insertTable)
		.set("upJSON", values.getAsString("upJSON"));
    	throw INSERT_FAILED;
    }
    
    //startservice sync here
    //TODO: fix this URI!!
    final Uri _uri = Uri.withAppendedPath(uri, Long.valueOf(rowID).toString() );
    mContentResolver.notifyChange( _uri, null );
    return _uri;
   
}//insert

/*public Uri insert( final Uri uri, final ContentValues values ){
	TableNames tableName = mContract.toTableNames(uri);
	//final String insertTable ;
//	final String insertTable = mContract.getTableName(uri);
	//final String insertTable = mContract.getTableName(uri);
	String upJSON;
    ContentValues newValues = new ContentValues();
//    switch(mContract.toTableNames(uri)) { 
    switch(tableName) { 
	case uLocations :
		Location loc = (Location)values.get(uri.toString());
		Gson gson = new Gson();
		upJSON = gson.toJson(loc, Location.class);
		newValues.put("upJSON", upJSON);
		newValues.
		break;
	default: throw new GaelException(GaelErrCode.BAD_GAEL_TABLE_URI).set("uri", uri);
	}//switch


	SQLiteDatabase db = GAELdatabase.getWritableDatabase();
    long rowID = db.insert(	tableName.name(), "", newValues);
  //see http://stackoverflow.com/questions/14002022/android-sq-lite-closed-exception
  //see http://stackoverflow.com/questions/23293572/android-cannot-perform-this-operation-because-the-connection-pool-has-been-clos
	//db.close();

//    if (rowID <= 0)  throw new SQLException("Failed to insert a record into " + uri);
    if (rowID <= 0){
		GaelException INSERT_FAILED = new GaelException(GaelErrCode.INSERT_FAILED)
		.set("URI", uri)
		.set("upJSON", upJSON);
    	throw INSERT_FAILED;
    }
    //startservice sync here
    
    //Uri _uri = ContentUris.withAppendedId(uri, rowID);
    final Uri _uri = Uri.withAppendedPath(uri, Long.valueOf(rowID).toString() );
    mContentResolver.notifyChange( _uri, null );
    return _uri;
   
}//insert
*/
@Override
public int bulkInsert( final Uri uri, final ContentValues[] contentValueses ){
	SQLiteDatabase db = GAELdatabase.getWritableDatabase();
	int count = 0;
	try{
		db.beginTransaction();
		for ( ContentValues values: contentValueses ){
			//Uri resultUri = doInsert( uri, values, db );
			Uri resultUri = insert( uri, values );
			if ( resultUri != null ) count++ ; 
			else{
				count = 0;
				throw new RuntimeException( "Error in bulk insert" );
			}
		}
		db.setTransactionSuccessful();
	}
	finally{
		db.endTransaction();
		//see http://stackoverflow.com/questions/14002022/android-sq-lite-closed-exception
		//see http://stackoverflow.com/questions/23293572/android-cannot-perform-this-operation-because-the-connection-pool-has-been-clos
		//database.close();
	}
	return count;
}



@Override // TODO
public int delete( final Uri uri
				   , final String s
				   , final String[] strings )
{ return 0; }

@Override // TODO
public int update( final Uri uri
				   , final ContentValues contentValues
				   , final String s
				   , final String[] strings )
{ return 0; }


@Override
public ContentProviderResult[] applyBatch( final ArrayList< ContentProviderOperation > operations ) throws OperationApplicationException{
	SQLiteDatabase db = GAELdatabase.getWritableDatabase();
	ContentProviderResult[] result = new ContentProviderResult[operations.size()];
	try{
		db.beginTransaction();
		for ( int i = 0; i < operations.size(); i++ ){
			ContentProviderOperation operation = operations.get( i );
			result[i] = operation.apply( this, result, i );
		}
		db.setTransactionSuccessful();
	}
	finally{
		db.endTransaction();
		//see http://stackoverflow.com/questions/14002022/android-sq-lite-closed-exception
		//see http://stackoverflow.com/questions/23293572/android-cannot-perform-this-operation-because-the-connection-pool-has-been-clos
		//database.close();

	}
	return result;
}

public static String status(){ return GAELdatabase.status(); }

@Override public void shutdown() { GAELdatabase.shutdown(); }
}//public class Provider


class GaelDbHelper extends SQLiteOpenHelper{
private static final String DATABASE_NAME		= "GaelProvider.db"; 
						   //TASK_TABLE			= "task" ; 

private static final int  DATABASE_VERSION = 1;

//private static final String PROPERTIESFILENAME = "com.ntier.android.GAEL1.SQLite.gaelDDL.properties";



//								CREATE_SQL			= 	
//"CREATE TABLE `uLocations` ( id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, updated datetime DEFAULT current_timestamp, `upJSON` TEXT );" ;								
																	
/*
CreateTableOrders = "CREATE TABLE orders ( id INTEGER PRIMARY KEY, customer_id INTEGER, salesperson_id INTEGER, FOREIGN KEY(PRIORITY) REFERENCES task(PRIORITY), FOREIGN KEY(STATUS) REFERENCES task(STATUS) );",

								CREATED_INDEX_SQL	= "CREATE INDEX "
																	+ TaskColumns.CREATED + "_idx ON " + TASK_TABLE + " ("
																	+ TaskColumns.CREATED + " ASC);" , 
								OWNER_INDEX_SQL	= "CREATE INDEX "
																	+ TaskColumns.OWNER + "_idx ON " + TASK_TABLE + " ("
																	+ TaskColumns.CREATED + " ASC);";
*/

private final Context mContext; 
private final Logger	log	= LoggerFactory.getLogger( GaelDbHelper.class );
//private static final boolean HEADER = true, NOHEADER = false; 

//See http://www.sqlite.org/pragma.html


public GaelDbHelper( final Context context ){
	super( context, DATABASE_NAME, null, DATABASE_VERSION );
	mContext = context;
	log.trace( "GaelDbHelper.constructor():" + DATABASE_NAME );
}


@Override
public void onConfigure( final SQLiteDatabase db  ){
	//log.trace( "onConfigure():\t" + DATABASE_NAME  );
	super.onConfigure( db );

	db.disableWriteAheadLogging();
}

@Override
public void onCreate( final SQLiteDatabase db ){
	Properties properties = GAELUtility.getInstance(mContext).getPropertiesFromAssets("SQLliteDDL.properties");
	String CREATE_uLocations = properties.getProperty("CREATE_uLocations");

	//Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data. 
	db.execSQL( CREATE_uLocations );
	
}//onCreate

/*@Override
public void onOpen( final SQLiteDatabase db  ){
	log.trace( "onConfigure():\t" + DATABASE_NAME  );
	super.onConfigure( db );
}
*/

@Override
public void onUpgrade( final SQLiteDatabase db, final int oldVersion, final int newVersion ){
	log.trace( "onUpgrade()\tdb: " + db + "\toldVersion: " + oldVersion + "\tnewVersion: " + newVersion );

	/*	if ( oldVersion < 2 ){
			db.execSQL( "ALTER TABLE " + TASK_TABLE
							+ " ADD COLUMN " + TaskColumns.OWNER + " TEXT" );
			db.execSQL( "ALTER TABLE " + TASK_TABLE + " ADD COLUMN " + TaskColumns.DATA + "TEXT" );
			db.execSQL( OWNER_INDEX_SQL );
		}
*/
}//onUpgrade


public String status() { 
	StringBuilder retVal = new StringBuilder("\nDB Status for:\t").
			append(this.getDatabaseName() ).append("\n").
			append("DATABASE_VERSION: " + GaelDbHelper.DATABASE_VERSION ).append("\n").
			append(this.getDatabaseName() ).append("\n").
								//append(SQLitePragmas.statusDB( this.getWritableDatabase() ));
								append(SQLitePragmas.statusDB( getReadableDatabase() ));
	return retVal.toString();
}


public void shutdown() {
	this.close();
	log.info("Shut down DB: " + this.getDatabaseName() );
}

}//class GaelDbHelper
