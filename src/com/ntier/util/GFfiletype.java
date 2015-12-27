package com.ntier.util;

import java.io.File;

import org.slf4j.LoggerFactory;

import android.os.Environment;

public enum GFfiletype { jpg (Environment.DIRECTORY_PICTURES) , mp4(Environment.DIRECTORY_MOVIES) , unknownFileType(Environment.DIRECTORY_PICTURES);
private final String relativeDirectory;
private static String absoluteDirectory; 
private GFfiletype( final String adirectory) {relativeDirectory = adirectory + File.separator; }

private boolean dirExists = false;

/*protected String fileName(){

	String timeStamp = new SimpleDateFormat(GEOfinderFile.GFdateFormat, Locale.US)
					.format(new Date())
					+ ".";

return new StringBuilder(GEOfinderFile.prefix)
			.append(timeStamp)  
			.append(name())
			.toString();
}//fileName
*/

protected boolean DirectoryExists() {
// This location works best if you want the created images to be shared
// between applications and persist after your app has been uninstalled.
if (!dirExists){
	
	File gfDir = new File(Environment.getExternalStoragePublicDirectory
		( relativeDirectory )
	, GEOfinderFile.GEOfielderUploadsDir);

	LoggerFactory.getLogger( GFfiletype.class ).debug("DirectoryExists():\t" + this.relativeDirectory + " \t:\t " + gfDir.getAbsolutePath());

	// TODO To be safe, you should check that the SDCard is mounted
	// using Environment.getExternalStorageState() before doing this.
	dirExists = gfDir.exists() || gfDir.mkdirs();

	if (dirExists) absoluteDirectory = gfDir.getAbsolutePath(); //TODO what about if !dirExists ????
	else LoggerFactory.getLogger( GFfiletype.class )
			.error("DirectoryExists(): failed to create directory:\t" + gfDir.getPath());
}
return dirExists;
}//DirectoryExists

protected String getDirectory(){
	return absoluteDirectory; //TODO what about if !dirExists ????
	
}
}//enum GFfiletype
