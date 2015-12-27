package com.ntier.exception;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ntier.android.GAEL1.MainActivity;
import com.ntier.android.util.GAELUtility;

//See file GaelErrCodes.properties
public enum GaelErrCode 
//To preserve errcode numbers, you should add to the bottom and NOT from the top!
{   NULL_LOCATION
	, SYNC_FAILED
	, WEBSERVICE_POST_FAILED
	, BAD_GAEL_TABLE_URI
	, INSERT_FAILED
	, TOO_MANY_ACCOUNTS
	, ACCOUNT_CREATION_ERROR
	, ACCOUNT_VERIFICATION_ERROR
	, MISSING_DDL
	, ASSETFILE_NOT_FOUND
	, BAD_SQLLITE_PRAGMA
	//, TEST_EXCEPTION
	, INITIALIZATION_NEEDED //The programmer failed to initialize GAELUtility()with GAELUtility.getInstance(context);
	;


	private final String name = name();
	//public final String mKey = "GaelErrCode." + name;
	public final String mErrMsg;

	private static final int errNumIncrement = 9000;
	public final int errNum = errNumIncrement + ordinal();

	private final Logger mLog = LoggerFactory.getLogger( GaelErrCode.class );
	private final static String BADEXCEPTION = ": This GAEL exception could not be defined! (check assetfile gaelErrCodes.properties)(or confirm programmer GAELUtility.getInstance(this)"; ;
	
	private GaelErrCode() {
		if (name == "INITIALIZATION_NEEDED") {
			mErrMsg = "The programmer did not init GAELUtility with GAELUtility.getInstance(context)";
			return;
		}
		
		
		boolean badException = false;
		
		String tmpMsg = BADEXCEPTION ;
		try {
			Properties properties = GAELUtility.getInstance().getPropertiesFromAssets("GaelErrCodes.properties");
			tmpMsg = properties.getProperty( name );			
		} 
		catch (Exception e) {
			badException = true;
			tmpMsg += name ; 
		}
		finally {mErrMsg = (tmpMsg == null)? name : tmpMsg;}		
		
		if (badException) mLog.error( new GaelException(this).toString() ); 
	}//cstr private GaelErrCode()

	
	private String toString;
	@Override
	public String toString() {
		if (toString == null) 
			toString = new StringBuilder()
					.append( " errNum: " )
					.append( errNum )
					.append( ": " )
					.append( name )
					.append( ": " )
					.append( mErrMsg )
					.toString();

		return toString; 
	}
}//enum GaelErrCode

