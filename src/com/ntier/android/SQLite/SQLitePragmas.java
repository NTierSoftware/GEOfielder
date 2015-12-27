package com.ntier.android.SQLite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import com.ntier.exception.GaelErrCode;
import com.ntier.exception.GaelException;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;


enum SQLitePragmas {
	//APPLICATION_ID( pragmaQueryOp.longForQuery), //does not work??
	AUTO_VACUUM( pragmaQueryOp.longForQuery, "=0"),
	AUTOMATIC_INDEX( pragmaQueryOp.stringForQuery, "=0"), //?
	//BUSY_TIMEOUT( pragmaQueryOp.longForQuery, "=2"), //1ms
	CACHE_SIZE( pragmaQueryOp.longForQuery, "=2000"), 
	//CACHE_SPILL( pragmaQueryOp.longForQuery, "=1"), 
	//CASE_SENSITIVE_LIKE( pragmaQueryOp.stringForQuery, "=0"), //does not work??
	CHECKPOINT_FULLFSYNC( pragmaQueryOp.stringForQuery, "=0"), 
	COLLATION_LIST( pragmaQueryOp.rawQuery), 
	COMPILE_OPTIONS( pragmaQueryOp.rawQuery), 
	DATABASE_LIST(pragmaQueryOp.rawQuery),
	ENCODING( pragmaQueryOp.stringForQuery), //"UTF-8"
	//TODO TEST!
	FOREIGN_KEY_CHECK( pragmaQueryOp.rawQuery), //"(table-name)"  
	//FOREIGN_KEY_LIST( pragmaQueryOp.rawQuery, "(" + com.ntier.android.GAEL1.SQLite.TASK_TABLE + "}" ), 
	FOREIGN_KEYS( pragmaQueryOp.longForQuery), 
	FREELIST_COUNT( pragmaQueryOp.longForQuery),
	FULLFSYNC( pragmaQueryOp.longForQuery, "=0"),
	IGNORE_CHECK_CONSTRAINTS( pragmaQueryOp.longForQuery, "=0"),
	//INCREMENTAL_VACUUM( pragmaQueryOp.longForQuery, "=0"),
	//INDEX_INFO( pragmaQueryOp.longForQuery, "=0"),
	//INDEX_LIST( pragmaQueryOp.longForQuery, "=0"),
	INTEGRITY_CHECK( pragmaQueryOp.rawQuery),
	JOURNAL_MODE (pragmaQueryOp.rawQuery, "=MEMORY" ),
	JOURNAL_SIZE_LIMIT( pragmaQueryOp.rawQuery, "=0"),
	LEGACY_FILE_FORMAT( pragmaQueryOp.longForQuery, "=0"),
	LOCKING_MODE( pragmaQueryOp.longForQuery, "=NORMAL"),
	MAX_PAGE_COUNT( pragmaQueryOp.longForQuery, "=0"),
	//MMAP_SIZE( pragmaQueryOp.longForQuery, "=-1"),//default
	PAGE_COUNT( pragmaQueryOp.longForQuery),
	PAGE_SIZE( pragmaQueryOp.longForQuery, "=-1"),//check value?
	//QUERY_ONLY( pragmaQueryOp.longForQuery, "=0"),
	QUICK_CHECK( pragmaQueryOp.rawQuery ),
	READ_UNCOMMITTED( pragmaQueryOp.longForQuery, "=0"),
	RECURSIVE_TRIGGERS( pragmaQueryOp.longForQuery, "=0"),
	REVERSE_UNORDERED_SELECTS( pragmaQueryOp.longForQuery, "=0"),
	SCHEMA_VERSION( pragmaQueryOp.longForQuery, "=-1"),	
	SECURE_DELETE( pragmaQueryOp.longForQuery, "=0"),
	//SHRINK_MEMORY( pragmaQueryOp.longForQuery ),	
	//SOFT_HEAP_LIMIT( pragmaQueryOp.longForQuery, "=-1"),
	SYNCHRONOUS( pragmaQueryOp.longForQuery, "=0"),
	//TABLE_INFO( pragmaQueryOp.longForQuery, "=0"),	
	TEMP_STORE( pragmaQueryOp.longForQuery, "=0"),
	USER_VERSION( pragmaQueryOp.longForQuery, "=-1"),
	WAL_AUTOCHECKPOINT(pragmaQueryOp.longForQuery, "=0"),
	WAL_CHECKPOINT( pragmaQueryOp.rawQuery, "=PASSIVE"),
	WRITABLE_SCHEMA( pragmaQueryOp.longForQuery, "=0");
	
	private static enum pragmaQueryOp {longForQuery, rawQuery, stringForQuery };
	private final static String NOOP = "";

	private final static Logger	log	= LoggerFactory.getLogger( SQLitePragmas.class );

	private final SQLitePragmas.pragmaQueryOp queryOp;    
    private final String pragmaVal; 
    private SQLitePragmas( final SQLitePragmas.pragmaQueryOp queryOp, final String pragmaVal) {
        this.queryOp = queryOp;
        this.pragmaVal = pragmaVal;
    }
    
    private static final boolean HEADER = true,
    							 NOHEADER = false; 
    private SQLitePragmas( final SQLitePragmas.pragmaQueryOp queryOp) {
        this.queryOp = queryOp;
        this.pragmaVal = NOOP;
    }
    
    
    private static StringBuilder cursorToString (Cursor c, boolean header){
    	StringBuilder retVal = new StringBuilder();

    	final int numCols = c.getColumnCount();
    	if (header) retVal.append("numrows: ").append(c.getCount()).
    						append("\tnumcols: ").append(numCols).
    						append("\n" );
        
        while (c.moveToNext()) {
    		for ( int colNum = 0; colNum < numCols; colNum++ ){
    			retVal.append( c.getColumnName( colNum ) ).
    					 append( ": " ).
    					 append( c.getString( colNum ) ).
    					 append( "\t" );
    				
    		}
    		retVal.append("\n"  );
        }
        
    return retVal;			
    }
    
    public static String statusDB( SQLiteDatabase db ){
    	Cursor c = null;
    	String qString,
    		   pragmaResult = null;
    	
    	StringBuilder retVal = new StringBuilder()  ;
    	for (SQLitePragmas p : SQLitePragmas.values()) {
    		qString = "pragma " + p ; 
    		retVal.append(qString + ":\t");
        	try{
    		    		
    		switch (p.queryOp) {
			case longForQuery:
				pragmaResult =  ((Long) DatabaseUtils.longForQuery(db, qString, null)).toString()  ;
				break;

			case rawQuery :
				c = db.rawQuery(qString, null);
				pragmaResult = cursorToString(c, HEADER).toString();
				//c.close();
				break;

			default: //stringForQuery
				pragmaResult =  DatabaseUtils.stringForQuery(db, qString, null) ;
				break;
			}//switch
        	}//try
        	catch (android.database.sqlite.SQLiteDoneException x){
        		GaelException BAD_SQLLITE_PRAGMA = new GaelException(GaelErrCode.BAD_SQLLITE_PRAGMA)
        							.set("PRAGMA", p.toString())
        							.set("QUERYOP", p.queryOp.toString())
        							.set("PRAGMAVAL", p.pragmaVal.toString() );

        		log.warn  (p.toString(), BAD_SQLLITE_PRAGMA);
        		pragmaResult = "ERROR!:\t " + pragmaResult;
        	}//catch
            finally {  if (c != null) c.close();  }
    		retVal.append(pragmaResult + "\n");
    	}//for
    	return retVal.toString();
    }//statusDB
    
    

}//enum Pragma