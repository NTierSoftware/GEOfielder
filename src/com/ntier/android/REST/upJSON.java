//see http://avilyne.com/?p=105
package com.ntier.android.REST;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//import com.ntier.rest.model.Location;

//import java.util.Date;

//import com.ntier.rest.model.Location;
 
//import javax.xml.bind.annotation.XmlRootElement;

//import com.ntier.rest.resource.LocationResource;


//@XmlRootElement
public class upJSON 
//implements WebRESTPojo
{
	private final static long NoLocation = -1, NO_ARG_CONSTRUCTOR = -999;
//	private final static String datetimeformat = "yyyyMMddHHmmssSSS" ;
    public long id; 
    public String updated, upJSON; 
    
 
    //instance initializer
    { this.id = NoLocation;  
      Date created = Calendar.getInstance().getTime();        
      DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
      this.updated = df.format(created);
    }//instance initializer
 
    //no-arg constructor required for webservice marshal/unmarshal serialization
    // TODO test private no-arg cstr private Location(){} AND  private members too.
    public upJSON(){
    	this.id = NO_ARG_CONSTRUCTOR;
    	this.upJSON = "{\"mProvider\":\"NO ARG CONSTRUCTOR!\"}" ; 
    }//end NO_ARG_CONSTRUCTOR
 
    //INSERT new location
    public upJSON(long aid, 
    			  String aupdated,
    			  String aupJSON ){
		  this.id = aid;
		  this.updated = aupdated;
		  this.upJSON = aupJSON;

    }//cstr Location()


    @Override  
    public String toString() {
    	return new StringBuilder().append("id: ").append(id)
								  .append(", updated: ").append(updated)
								  .append(", upJSON: ").append(upJSON)
								  .toString();    			
    }//toString()  
}//class upJSON
 
