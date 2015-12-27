/** Create a File for saving an image or video */
package com.ntier.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import android.net.Uri;


public class GEOfinderFile {
static public final String GEOfielderUploadsDir = "GEOfielderUploads"
					 ,prefix = "GEOfielder."
					 ,GFdateFormat = "yyyyMMddHHmmss"
					 ;

public File GFfile = null;//TODO handle null conditions!

public GEOfinderFile (GFfiletype afileType){
	if ( afileType.DirectoryExists() ) 
		
		//GFfile = new File(afileType.directory + File.separator + fileName(afileType) );
		GFfile = new File(afileType.getDirectory(), fileName(afileType));
		
}//cstr

	
private static String fileName(GFfiletype afileType){

String timeStamp = new SimpleDateFormat(GEOfinderFile.GFdateFormat, Locale.US)
					.format(new Date())
					+ ".";

return new StringBuilder(GEOfinderFile.prefix)
			.append(timeStamp)  
			.append(afileType.name())
			.toString();
}//fileName


public Uri toUri(){ return Uri.fromFile( this.GFfile ); }
}//GEOfinderFile
