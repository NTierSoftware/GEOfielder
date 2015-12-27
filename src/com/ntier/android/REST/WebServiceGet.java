package com.ntier.android.REST;

import java.io.*;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.*;

import javax.net.ssl.*;

import org.slf4j.*;

import com.google.gson.Gson;
import com.ntier.android.accounts.Contract;
import com.ntier.rest.model.Deployment;


public class WebServiceGet{ 
	protected static Contract mContract;
	private static final Logger mLog	= LoggerFactory.getLogger( WebServiceGet.class );
	  
	public WebServiceGet(Contract contract){ mContract = contract;  }//constructor
		
	protected void trustEveryone(boolean trust) {//TODO FOR TESTING/DEV PURPOSE ONLY!!! HIGHLY INSECURE!!!
		// see: http://stackoverflow.com/questions/1217141/self-signed-ssl-acceptance-android/1607997#1607997
		if (!trust) return;

		try {
			HttpsURLConnection
					.setDefaultHostnameVerifier(new HostnameVerifier() {
						@Override
						public boolean verify(String hostname,
								SSLSession session) {
							return true;
						}
					});
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null,
					new X509TrustManager[] { new X509TrustManager() {
						@Override
						public void checkClientTrusted(
								X509Certificate[] chain, String authType)
								throws CertificateException {
						}

						@Override
						public void checkServerTrusted(
								X509Certificate[] chain, String authType)
								throws CertificateException {
						}

						@Override
						public X509Certificate[] getAcceptedIssuers() {
							return new X509Certificate[0];
						}
					} }, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context
					.getSocketFactory());
		} catch (Exception e) { // should never happen
			e.printStackTrace();
		}
	}// trustEveryone

	
	public String get(Contract.URLGet urlget) {
//		public String get(URL url) {
		URL url = mContract.toURL(urlget);
		HttpsURLConnection urlConnection = null;
		InputStream in = null, ins = null;
		String result;
		try {
			trustEveryone(true);
			urlConnection = (HttpsURLConnection) url.openConnection();
			ins = urlConnection.getInputStream();
			in = new BufferedInputStream(ins );
			result = inputStreamToString(in);
			urlConnection.disconnect();
			in.close();
			ins.close();
		} 
		catch (IOException e) { 
			e.printStackTrace();
			return ("FAILED:\t" + url );
		}// TODO Auto-generated catch block 
	
		switch (urlget) {
		case Test  :
			Deployment deploy = new Gson().fromJson(result, Deployment.class);
			mLog.debug(deploy.toString());
			break;

		default:
			break;
		}
		return result;
	}//get

/*	public String get(String urlString) {
		String result = "";
		HttpsURLConnection urlConnection = null;
		InputStream in = null;
		try {
			URL url = new URL(urlString);
			//URL url = new URL(GET);
			trustEveryone(true);
			urlConnection = (HttpsURLConnection) url.openConnection();
			//debugging
			InputStream ins = urlConnection.getInputStream();
			in = new BufferedInputStream(ins );
			result = inputStreamToString(in);
			in.close();
		} // TODO Auto-generated catch block
		catch (MalformedURLException e) { e.printStackTrace(); } 
		catch (IOException   e) { e.printStackTrace(); } 
		finally { if (urlConnection != null) urlConnection.disconnect(); }
		
		return result;
	}//get

*/	
	protected static String inputStreamToString(InputStream is) {
		StringBuilder total = new StringBuilder();

		try {
			// Wrap a BufferedReader around the InputStream
			BufferedReader rd = new BufferedReader(
					new InputStreamReader(is));
			String line;
			// Read response until the end
			while ((line = rd.readLine()) != null) {
				total.append(line);
			}
			rd.close();
		} catch (IOException e) {
			mLog.error(e.getLocalizedMessage(), e);
		}

		return total.toString();
	}//inputStreamToString
}//class WebServiceGet
