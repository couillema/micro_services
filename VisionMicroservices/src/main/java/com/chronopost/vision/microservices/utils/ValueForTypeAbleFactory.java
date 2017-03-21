package com.chronopost.vision.microservices.utils;

import java.util.HashMap;
import java.util.Map;

import com.chronopost.vision.microservices.enums.ESelectLT;
import com.chronopost.vision.model.rules.DateRules;
import com.datastax.driver.core.Row;

/** @author JJC for all "cast" from row to value  2 version : one by ordinal one by name */
public class ValueForTypeAbleFactory {
	/** private empty CST: Avoids instantiation. */
	private ValueForTypeAbleFactory() { }

	/** @author JJC to generically transfer value from row using an enumeration ESelectLt .. ( or other in the future).**/
	public interface ValueForTypeAble {

		/**@return a type name acting as a key */
		public String getStrType() ;  

		/** @param idCol id column for small SELECT specific 
		 * @param row all fields values.
		 * @return the value to pass to the set method when cur is used on row	 */
		public Object getValueForType(final int idCol , final Row row ) ;

	}

    /** This version is with ordinal : will allow later to remove all get row methods.  */
    private static final Map<String,ValueForTypeAble> TYPE_BY_ORDINAL = new HashMap< >(); //
    static 
    {{
    	TYPE_BY_ORDINAL.put("String" , new  ValueForTypeAble  () { 
    		public String getStrType() { return "String" ;	}
    		public Object getValueForType(final int idCol		,final Row row)	{ return row.getString(idCol);} 
    	} );
    	TYPE_BY_ORDINAL.put("Int" , new  ValueForTypeAble  () { 
    		public String getStrType() { return "Int" ;	}
    		public Object getValueForType(final int idCol		,final Row row)	{ return row.getInt(idCol);  	} 
    	} );   	
    	TYPE_BY_ORDINAL.put("Timestamp" , new  ValueForTypeAble  () { 
    		public String getStrType() { return "Timestamp" ;	}
    		public Object getValueForType(final int idCol		,final Row row)	{ return DateRules.toTimestamp(row.getTimestamp(idCol));	} 
    	} );
    	TYPE_BY_ORDINAL.put("Date" , new  ValueForTypeAble  () { 
    		public String getStrType() { return "Date" ;	}
    		public Object getValueForType(final int idCol		,final Row row)	{ return DateRules.toTimestamp(row.getTimestamp(idCol));	} 
    	} );
    	TYPE_BY_ORDINAL.put("Set<String>" , new  ValueForTypeAble  () { 
    		public String getStrType() { return "Set<String>" ;	}
    		public Object getValueForType(final int idCol		,final Row row)	{ return row.getSet(idCol			,String.class);	} 
    	} );
    	TYPE_BY_ORDINAL.put("Set" , new  ValueForTypeAble  () { 
    		public String getStrType() { return "Set" ;	}
    		public Object getValueForType(final int idCol		,final Row row)	{ return row.getSet(idCol			,String.class);	} 
    	} );    	
    	TYPE_BY_ORDINAL.put("Map" , new  ValueForTypeAble  () { 
    		public String getStrType() { return "Map" ;	}
    		public Object getValueForType(final int idCol		,final Row row)	{ return row.getMap(idCol			,String.class, String.class);	} 
    	} );    		 
    	TYPE_BY_ORDINAL.put("Map<String,String>" , new  ValueForTypeAble  () { 
    		public String getStrType() { return "Map<String,String>" ;	}
    		public Object getValueForType(final int idCol		,final Row row)	{ return row.getMap(idCol			,String.class, String.class);	} 
    	} );    		 
    }}


	/** @param cur not null : it always has a type. 
	 * @return eventually null if no setMethod ... */
	public static ValueForTypeAble getCasterByOrdinal(final ESelectLT cur) {	return TYPE_BY_ORDINAL.get(cur.getType()); 	} 

} // EOC 71 lines 
