package com.ntier.android.GAEL1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author JD
 * usage:  	TestLogging myTestLog = new TestLogging();
 */
public class TestLogging {
	//usage: 		TestLogging myTestLog = new TestLogging();

	private static final Logger log = LoggerFactory.getLogger(MainActivity.class);
	private static final int MinLogIteration = 14;

	TestLogging(){
		this( MinLogIteration );
	}//constructor
	
	TestLogging(int numIterations){
		String logstring;
		
		while (numIterations > 0) {
			logstring = "iteration: " + numIterations;
			log.trace( logstring );
			log.debug( logstring );
			log.info( logstring );
			log.warn( logstring );
			log.error( logstring );
			numIterations--;
		}//while	
		
    	//( (LoggerContext) LoggerFactory.getILoggerFactory() ).stop();
	}

}
