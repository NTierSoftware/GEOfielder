package com.ntier.android.REST;

import java.io.*;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import android.content.Context;


import com.google.gson.Gson;
import com.ntier.android.accounts.Contract;
import com.ntier.exception.GaelErrCode;
import com.ntier.exception.GaelException;
//TODO: enable gzip content encoding!!!
public class WebServicePost extends WebServiceGet{

	private static final Logger mLog	= LoggerFactory.getLogger( WebServicePost.class );

	public WebServicePost(Contract contract) { super(contract); }

	public String post(final JSONArray JSON) {

	   String result = "Nothing to post. JSONArray is null";
	   if (JSON == null) return result;
	   
       HttpsURLConnection urlConnection = null;
		
	   try {
		 URL url = mContract.toURL( Contract.URLPost.InsertuLocations );
		 //URL url = new URL(HTTPS_URL + "InsertuLocations" );
		 trustEveryone(true);

		 urlConnection = (HttpsURLConnection) url.openConnection();

	     urlConnection.setDoOutput(true);
		 urlConnection.setRequestMethod("POST");
	     urlConnection.setDoOutput(true);
	     urlConnection.setChunkedStreamingMode(0);
	     urlConnection.setRequestProperty("Content-Type","application/json");   

/*		     StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		     StrictMode.setThreadPolicy(policy);
*/
	     urlConnection.connect();
	     /* S E N D   the   J S O N !!! */
	     writeJsonStream(JSON, urlConnection );

	     InputStream in = new BufferedInputStream(urlConnection.getInputStream());
	     result = inputStreamToString(in);
		} 
	   catch (IOException e) { 
		   mLog.error(e.getLocalizedMessage()); 
		   @SuppressWarnings("null")
		   InputStream errIn = new BufferedInputStream(urlConnection.getErrorStream());
		   result = inputStreamToString(errIn);
	   } 
	   finally { if (urlConnection != null) urlConnection.disconnect(); }

	   return result;
	}

	
static public void writeJsonStream(final JSONArray JSON, HttpsURLConnection urlConnection ) throws IOException {
		OutputStream outputStream = null, 
					 bufferedOutputStream = null;
		
		OutputStreamWriter writer = null;
		Exception exception = null;
	try {
		outputStream = urlConnection.getOutputStream();
		bufferedOutputStream = new BufferedOutputStream(outputStream);
		writer = new OutputStreamWriter(bufferedOutputStream);

		mLog.debug("writeJsonStream.JSON:\t " + JSON.toString() );
                  /* !!! S E N D   the   J S O N !!! */
        new Gson().toJson(JSON, writer );
        
	} catch (Exception e) { 
		exception = e; 
		//mLog.error(e.getLocalizedMessage()); 
	}
	finally{
        if (writer != null) writer.close();
        if (bufferedOutputStream != null) bufferedOutputStream.close();
        if (outputStream != null) outputStream.close();
	}

	if (exception != null ) 
		throw new GaelException(GaelErrCode.WEBSERVICE_POST_FAILED)
					.set("exception", exception);
	
    }//writeJsonStream	
}//WebServicePost
