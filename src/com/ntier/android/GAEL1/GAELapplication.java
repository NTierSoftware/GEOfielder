package com.ntier.android.GAEL1;

import static org.acra.ReportField.*;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import com.ntier.android.accounts.*;
import com.ntier.android.accounts.Contract.*;
import com.ntier.android.util.GAELUtility;
import com.ntier.exception.GaelErrCode;
import com.ntier.exception.GaelException;

import android.app.Application;
import android.content.SharedPreferences;
//see https://github.com/ACRA/acra/wiki/AdvancedUsage#sending-reports-to-your-own-self-hosted-script
@ReportsCrashes(
	formUri =  "" //SET THIS IN onCreate() 
//			    formUri = "http://www.backendofyourchoice.com/reportpath"
//			    		formUriBasicAuthLogin = "yourlogin", // optional
//			    		formUriBasicAuthPassword = "y0uRpa$$w0rd", // optional
    , formKey = "" // This is required for backward compatibility but not used
   , customReportContent = {
			REPORT_ID, // Report Identifier
			/**
			* Application version code. This is the incremental integer version code
			* used to differentiate versions on the android market.
			* 
			* @see android.content.pm.PackageInfo#versionCode
			*/
			APP_VERSION_CODE,
			/**
			* Application version name.
			* 
			* @see android.content.pm.PackageInfo#versionName
			*/
			APP_VERSION_NAME,
			/**
			* Application package name.
			* 
			* @see android.content.Context#getPackageName()
			*/
			PACKAGE_NAME,
			/**
			* Base path of the application's private file folder.
			* 
			* @see android.content.Context#getFilesDir()
			*/
			FILE_PATH,
			/**
			* Device model name.
			* 
			* @see android.os.Build#MODEL
			*/
			PHONE_MODEL,
			/**
			* Device android version name.
			* 
			* @see android.os.Build.VERSION#RELEASE
			*/
			ANDROID_VERSION,
			/**
			* Android Build details.
			* 
			* @see android.os.Build
			*/
			BUILD ,
			/**
			* Device brand (manufacturer or carrier).
			* 
			* @see android.os.Build#BRAND
			*/
			BRAND,
			/**
			* Device overall product code.
			* 
			* @see android.os.Build#PRODUCT
			*/
			PRODUCT,
			/**
			* Estimation of the total device memory size based on filesystem stats.
			*/
			TOTAL_MEM_SIZE,
			/**
			* Estimation of the available device memory size based on filesystem stats.
			*/
			AVAILABLE_MEM_SIZE,
			/**
			* Contains key = value pairs defined by the application developer during
			* the application build.
			*/
			//BUILD_CONFIG ,
			/**
			* Contains key = value pairs defined by the application developer during
			* the application execution.
			*/
			CUSTOM_DATA ,
			/**
			* The Holy Stack Trace.
			*/
			STACK_TRACE,
			/**
			* A hash of the stack trace, taking only method names into account.<br>
			* Line numbers are stripped out before computing the hash. This can help you
			* uniquely identify stack traces.
			*/
			//STACK_TRACE_HASH,
			/**
			* {@link Configuration} fields state on the application start.
			* 
			* @see Configuration
			*/
			INITIAL_CONFIGURATION ,
			/**
			* {@link Configuration} fields state on the application crash.
			* 
			* @see Configuration
			*/
			CRASH_CONFIGURATION ,
			/**
			* Device display specifications.
			* 
			* @see android.view.WindowManager#getDefaultDisplay()
			*/
			DISPLAY ,
			/**
			* Comment added by the user in the CrashReportDialog displayed in
			* {@link ReportingInteractionMode#NOTIFICATION} mode.
			*/
			USER_COMMENT,
			USER_APP_START_DATE, //User date on application start.
			USER_CRASH_DATE, //User date immediately after the crash occurred.
			//DUMPSYS_MEMINFO,//* Memory state details for the application process.

			/**
			* Content of the android.os.DropBoxManager (introduced in API level 8).
			* Requires READ_LOGS permission.
			*/
			//DROPBOX,
			LOGCAT,//Logcat default extract. Requires READ_LOGS permission.
			EVENTSLOG,//Logcat eventslog extract. Requires READ_LOGS permission.
			RADIOLOG, //Logcat radio extract. Requires READ_LOGS permission.
			//IS_SILENT,//True if the report has been explicitly sent silently by the developer.
			/**
			* 
			*/
			DEVICE_ID, //Device unique ID (IMEI). Requires READ_PHONE_STATE permission.
			/**
			* Installation unique ID. This identifier allow you to track a specific
			* user application installation without using any personal data.
			*/
			INSTALLATION_ID,
			/**
			* User email address. Can be provided by the user in the
			* {@link ACRA#PREF_USER_EMAIL_ADDRESS} SharedPreference.
			*/
			USER_EMAIL,
			DEVICE_FEATURES, //Features declared as available on this device by the system. 
			ENVIRONMENT, 
			SETTINGS_SYSTEM, //External storage state and standard directories.

			SETTINGS_SECURE, /** Secure settings (applications can't modify them). */
			/**
			* Global settings, introduced in Android 4.2 (API level 17) to centralize settings for multiple users.
			*/
			SETTINGS_GLOBAL, 
			USER_IP,
			//SHARED_PREFERENCES,
			/**
			* Content of your own application log file. To be configured with
			* {@link ReportsCrashes#applicationLogFile()} to define the path/name of
			* the log file and {@link ReportsCrashes#applicationLogFileLines()} to set
			* the number of lines you want to be retrieved.
			*/
			//APPLICATION_LOG,
			/**
			* Since Android API Level 16 (Android 4.1 - Jelly Beans), retrieve the list
			* of supported Media codecs and their capabilities (color format, profile
			* and level).
			*/
			//MEDIA_CODEC_LIST,
			/**
			* Retrieves details of the failing thread (id, name, group name).
			*/
			THREAD_DETAILS
			
 }
	, disableSSLCertValidation = true
    , httpMethod = org.acra.sender.HttpSender.Method.POST
    
	, reportType = org.acra.sender.HttpSender.Type.JSON
	//, reportType = org.acra.sender.HttpSender.Type.FORM
    , mode = ReportingInteractionMode.TOAST
    , resToastText = R.string.crash_toast_text 
    )

public class GAELapplication extends Application {

@Override
public void onCreate() {
    super.onCreate();

    //http://www.jiahaoliuliu.com/2011/11/android-httpurlconnection-disconnect.html
    System.setProperty("http.keepAlive", "false");

    ACRA.init(this);

    //GAELUtility.getInstance(this);

    Contract mContract = new Contract(this);
    URL AcraException = mContract.toURL(URLPost.ACRAException);   
    ACRA.getConfig().setFormUri( AcraException.toString() );

    ACRA.getErrorReporter().setDefaultReportSenders();

    GAELUtility.getInstance(this).registerDevice(true);
}//onCreate

}//GAELapplication

