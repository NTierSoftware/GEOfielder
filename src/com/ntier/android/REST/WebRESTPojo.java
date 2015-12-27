package com.ntier.android.REST;

public interface WebRESTPojo {
	public void setPOJOnames (String POJOclientname, String POJOservername);
	//public void setPOJOnames (final WebRESTPojo exchangedPojo);
	public String getClientPOJOname ();
	public String getServerPOJOname ();

}


/*private static String ClientPojoName, ServerPojoName;

@Override
public  void setPOJOnames (String POJOclientname, String POJOservername){
	ClientPojoName = POJOclientname;
	ServerPojoName = POJOservername;
};

@Override public String getClientPOJOname(){return ClientPojoName;}
@Override public String getServerPOJOname(){return ServerPojoName;}
*/

/*public class WebRESTPojo {
private static String clientPOJOname = "",
				      serverPOJOname = "";
				      
				   //TODO CONSIDER KEY-VALUE PAIRS!!

public WebRESTPojo (final String POJOclientname, final String POJOservername){
	clientPOJOname = POJOclientname;
	serverPOJOname = POJOservername;
}

public WebRESTPojo ( final WebRESTPojo exchangedPojo) {
	if (clientPOJOname  == "") clientPOJOname  = exchangedPojo.getClientPOJOname();
	if (serverPOJOname  == "") serverPOJOname  = exchangedPojo.getServerPOJOname();
}

	public void   setPOJOnames (final WebRESTPojo exchangedPojo);
	if (clientPOJOname  == "") clientPOJOname  = exchangedPojo.getClientPOJOname();
	if (serverPOJOname  == "") serverPOJOname  = exchangedPojo.getServerPOJOname();
}


public static final String[] getPojoNames (){ return new String[]{clientPOJOname, serverPOJOname}; }
}//class WebRESTPojo 
*/

