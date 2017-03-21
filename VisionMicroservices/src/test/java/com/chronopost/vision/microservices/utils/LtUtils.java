package com.chronopost.vision.microservices.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.chronopost.vision.microservices.enums.ESelectLT;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.stringuts.StrUtils;
import com.chronopost.vision.stringuts.StrUtils.FOUND_TYPE;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/** @author jjean-charles utilitaire for comparison. */
public class LtUtils {

	/** To avoid instatiation. 	 */
	private LtUtils() { }
	
	/**@param fields not null the ones to be compared using getAttr
	 * @param ltOne first to compare
	 * @param ltTwo first to compare
	 * @param nbDiffToPrint <= 0 ==> no print : to print the first nbDiffToPrint  dfferences.
	 * @return number of differences     */
	public static int getNbDiffLts(final ESelectLT[] fields , final Lt ltOne , final Lt ltTwo ,final int nbDiffToPrint) { 
		final String s1= makeJSonStrLt(ltOne); 
		final String s2= makeJSonStrLt(ltTwo);
		if ( s1 == null ) {    		System.out.println(" ltOne is null ???") ;    		return 1 ;     	}
		if ( s2 == null ) {    		System.out.println(" ltTwo is null ???") ;    		return 1 ;     	}
		if ( s1.compareTo(s2) == 0 ) return 0 ; // FULL EQUALS !!
		LtDataMakerTest.showDiffBigStr("getNbDiff" , s1,s2);
		int nbDiff = 0 ; // nb differences
		for  ( ESelectLT cur : fields)  { 
			final Method mth = getGetMethod(cur); 
			if ( mth == null ) continue ; 
			try {
				final Object objOne = mth.invoke(ltOne);
				final Object objTwo = mth.invoke(ltTwo);
				nbDiff += diffObjs(cur.getColName() , objOne , objTwo, (nbDiff < nbDiffToPrint) ) ; 
			} catch (IllegalAccessException | IllegalArgumentException	| InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		// System.out.println(" OK all GET CALLED : " + nbDiff); 
		return nbDiff; 
	}

	/** @param cur describe a field. 
	 * @return  a Descriptor of the getMethod associated to this field ( if it exists otherwise null).    */
	private static final Method getGetMethod( final ESelectLT cur ){ 
		Method ret = null ; 
		if ( cur.getSetMethod() == null ) return ret;  
		final String setMth = cur.getSetMethod().getName();
		if ( setMth == null ) return ret ;
		final String getMethodName = "get" + setMth.substring(3) ; // replace set by get
		final Class<?> clz = Lt.class ; 
		ret = StrUtils.getMethod(getMethodName, clz,cur.getField(), FOUND_TYPE.METHOD_IS_MISSING); // OK always sloopy
		// System.out.println(" FFFFFOUND : MTH=" + ret + " FOR NAME='" + getMethodName + "'") ; 
		return ret; 
	}

	/** @param in not null 
	 ** @return 	a json string  */
	public static  String makeJSonStrLt(final Lt in) {
		try {  return  "["  + new ObjectMapper().writeValueAsString(in) +  "]" ;
		} catch (JsonProcessingException e) { 		e.printStackTrace(); return  null; 	}
	} 

   /**
	 * @param caller for context  of trace.
	 * @param obj1 to compare.
	 * @param obj2 to compare.
	 * @param withTrace true means print if diff
	 * @return 0 or 1  with ventual print if diff. found	 */
	public static final int diffObjs(final String caller , final Object obj1 , final Object obj2 ,final boolean withTrace) {
	   	if ( obj1 == null ) { 
	   		if ( obj2 == null ) return 0 ; // 2 null is OK
	       	if (  withTrace )  System.out.println(" ERR1 NULL != NOT NULL c=" + caller + " a1/a2=" + obj1 + "//"+ obj2 ) ;
	       	return 1; 
	   	}
	   	if ( obj2 == null ) { 
	       	if (  withTrace )  System.out.println(" ERR2 NOT NULL != NULL c=" + caller + " a1/a2=" + obj1 + "//"+ obj2 ) ;
	   		return  1 ; 
	   	}
	   	final String str1= "" + obj1; 
	   	final String str2= "" + obj2;
	   	int ret = ( str1.compareTo(str2) == 0 ) ? 0 : 1 ;
	   	if (  ret != 0 && withTrace )  System.out.println(" ERR3 DIFF VALS c=" + caller + " a1/a2=" + obj1 + "//"+ obj2 ) ;
	   	return ret ;
	  }

} //EOC LtUtils 90 lines 
