//See https://northconcepts.com/blog/2013/01/18/6-tips-to-improve-your-exception-handling/
package com.ntier.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GaelException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private GaelErrCode mErrCode;

    private static final Logger	log	= LoggerFactory.getLogger( GaelException.class );

    public static GaelException wrap(Throwable exception, GaelErrCode errorCode) {
/*    	Long, redundant stack traces help no one. 
    	Even worse, they waste your time and resources.  
    	When rethrowing exceptions, 
    	call a static wrap method instead of the exception’s constructor . 
    	The wrap method will be responsible for deciding when 
    	to nest exceptions and when to just return the original instance.
*/    			
        if (exception instanceof GaelException) {
            GaelException ge = (GaelException)exception;
        	if (errorCode != null && errorCode != ge.getErrorCode()) {
                return new GaelException(exception.getMessage(), exception, errorCode);
			}
			return ge;
        }
		return new GaelException(exception.getMessage(), exception, errorCode);
    }
    
    public static GaelException wrap(Throwable exception) { return wrap(exception, null); }
    
    
    private final Map<String,Object> properties = new TreeMap<String,Object>();
    
    
    public GaelException(GaelErrCode errorCode) { mErrCode = errorCode; }

	public GaelException(String message, GaelErrCode errorCode) {
		super(message);
		this.mErrCode = errorCode;
	}

	public GaelException(Throwable cause, GaelErrCode errorCode) {
		super(cause);
		this.mErrCode = errorCode;
	}

	public GaelException(String message, Throwable cause, GaelErrCode errorCode) {
		super(message, cause);
		this.mErrCode = errorCode;
	}
	
	public GaelErrCode getErrorCode() { return mErrCode; }
	
	public GaelException setErrorCode(GaelErrCode errorCode) {
        this.mErrCode = errorCode;
        return this;
    }
	
	public Map<String, Object> getProperties() { return properties; }
	
    //@SuppressWarnings("unchecked")
	public <T> T get(String name) { return (T)properties.get(name); }
	
    public GaelException set(String name, Object value) {
        properties.put(name, value);
        return this;
    }
    
    @Override
	public void printStackTrace(PrintStream s) {
        synchronized (s) {
            printStackTrace(new PrintWriter(s));
        }
    }

    @Override
	public void printStackTrace(PrintWriter s) { 
        synchronized (s) {
            s.println("\t-------------------------------");
            if (mErrCode != null) {
	        	s.println("\t" + mErrCode + ":" + mErrCode.getClass().getName()); 
			}
            s.println( this );
            
            s.println("\t-------------------------------");
            StackTraceElement[] trace = getStackTrace();
            for (int i=0; i < trace.length; i++)
                s.println("\tat " + trace[i]);

            Throwable ourCause = getCause();
            if (ourCause != null) { ourCause.printStackTrace(s); }
            s.flush();
        }
    }

    @Override public String getMessage() { return toString();}
    @Override 
    public String toString()   {
    	StringBuilder retVal = new StringBuilder()
    									.append("\n")
    									.append(mErrCode.mErrMsg);
    	
        for (String key : properties.keySet()) {
        	retVal.append("\n:\t" + key + "=[" )
        			.append(properties.get(key) + "]");
        }
	return retVal.append("\n").toString() ;
    }
    
    //public void log(){ log.error(toString()); }
}//class GaelException

