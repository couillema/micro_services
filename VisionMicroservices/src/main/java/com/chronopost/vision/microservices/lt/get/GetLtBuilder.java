package com.chronopost.vision.microservices.lt.get;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.chronopost.vision.microservices.enums.ESelectLT;
import com.chronopost.vision.microservices.utils.ValueForTypeAbleFactory;
import com.chronopost.vision.microservices.utils.ValueForTypeAbleFactory.ValueForTypeAble;
import com.chronopost.vision.model.Lt;
import com.datastax.driver.core.Row;

/**
 * Constructeur d'objets du model. Ce constructeur génère les objets Lt, Evt...
 * à partir d'une row conforme à l'enumSelect correspondant.
 * 
 * @author jcbontemps
 * JJC : all redone with one method only + POSITION and CodeService removed from PATCH    
 */
public class GetLtBuilder {
    /** Set in Caps for sonar  */
    private static final Logger LOGGER = LogManager.getLogger(GetLtBuilder.class);

	/** The only field in SGBD Cassandra and not in Lt.java. */	
	private static final EnumSet<ESelectLT> IGNORED = EnumSet.of(ESelectLT.DEPASSEMENT_PROACTIF);
	
	/** USE THIS because the static value is used to tell if we have all fields. 	 */
	private static final ESelectLT[] ALL_LT = ESelectLT.values() ; 
	
    /** @param row if null returned value is null.  
     *  @return null if row is null ELSE uses ALL FIELDS ESelectLT.values() with ordinal !!  */
	public Lt makeGenLtAll(final Row row ) {   	return  makeGenLt(row , ALL_LT);    }

    /** @param row if null returned value is null.
     *  @param fields : usually ESelectLT.FIELDS_FOR_SMALL (used mainly for synonym) or ESelectLT.values() for all fields.  
     *  @return null if row is null ELSE a reduced lt made from fields : ATTENTION works only if each field has a method for type Strings of method (for now)  */
    public Lt makeGenLt(final Row row , final ESelectLT[] fields ) {
    	if (row == null) // PRECONDITION   
    		return null ; 
   	
        Lt lt = new Lt();
        for ( int iCol = 0 ; iCol< fields.length ; iCol++ ) { 
        	final ESelectLT cur = fields[iCol] ; 
        	if ( IGNORED.contains(cur) ) { // only ESelectLT.DEPASSEMENT_PROACTIF : no corresponding field in LT   
        		continue;
        	}     
        	final ValueForTypeAble caster = ValueForTypeAbleFactory.getCasterByOrdinal(cur) ;
        	if ( caster == null ) { 
				LOGGER.info(" makeSmallLt field NOT IMPLEMENTED" + cur.getColName() + " getType=" + cur.getType());
				return null ; 
			}
			final Object valueToSet = caster.getValueForType(iCol,row) ;   // ORIG caster.getValueForType(cur,row) ;
    		final Method mth = cur.getSetMethod();    // System.out.println(" INVOKGEN objectVal='" + valueToSet + "' cur=" + cur + " typ=" + cur.getType() + ( ( mth== null) ? " NO METHOD " : (" method=" + mth.getName() + " mth=" + mth )) );
    		if ( mth==null ) { 
				LOGGER.info(" No method available for " + cur.getColName() + " getType=" + cur.getType());
    			return null ;
    		}
    		try {
       			mth.invoke(lt,valueToSet);
    		} catch (IllegalAccessException | IllegalArgumentException| InvocationTargetException e) {
    			final String msg = " PB builder ENUM " + cur.getDesc() + " while setting value " + valueToSet + " with method" + mth + " type=" + cur.getType() ;
    			LOGGER.info(msg , e);
    			e.printStackTrace();
    			return null ; // ???  
    		} // 
        }
        return lt;
    } // eom makelT


} // EOC LtBuilder 73 lines 


