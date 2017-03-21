package com.chronopost.vision.microservices.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.chronopost.vision.stringuts.StrUtils;
import com.chronopost.vision.stringuts.StrUtils.FOUND_TYPE;


/** @author JJC : structural utility to handle field and method name through enums : 
 * factors out and lightens code of EInsertLT and ESelectLT **/
public class AttrDesc {
	/** like Lt or Evt */
	final Class<?> clz ; 

	/** Usually setMethodName  etField... */ 	 
	private String fieldName ;
	/** Usually setMethodName  etField... */ 	 
	private String setMethodName ;

	/** Usually got from setMethodName... */ 	 
	private Field field ;
	/** Usually got from setMethodName... */ 	 
	private Method setMethod ;


	/** @param aClz not null 
	 *  @param aFieldName not null  method will be "setFieldName" */
	public AttrDesc(final Class<?> aClz , final String aFieldName ,final FOUND_TYPE fndForField ) {
		this(aClz ,aFieldName,getSetMethodFromFieldName(StrUtils.toUpperUnderscoreStr(aFieldName)),fndForField) ; 
	} 

	/** @param aClz not null 
	 *  @param aFieldName not null 
	 *  @param aSetMethodName not null  
	 *  @param fnd should be one of FIELD_IS_MISSING or FIELD_IS_REQUIRED */
	public AttrDesc(final Class<?> aClz , final String aFieldName , final String aSetMethodName ,final FOUND_TYPE fndForField) {
		this.clz = aClz ; 
		this.fieldName =  aFieldName ;
		this.setMethodName = aSetMethodName ; 

		final String renormalizedName = StrUtils.toUpperUnderscoreStr(fieldName) ;
		this.field = StrUtils.getField(renormalizedName,clz,fndForField) ; 
		
		this.setMethod = StrUtils.getMethod(setMethodName, clz,field, FOUND_TYPE.METHOD_IS_MISSING); // OK always sloopy
	}

	/** @return the field	 */
	public Field getField() { return field ; }

	/** @return the method if any. */
	public Method getSetMethod() { return setMethod ; }

	/** @return fieldName of class LT  ( = normalized column name given at construction  )   */
	public final String getFieldName() {  return fieldName;    }

	/**@param fieldName like "myAttribute". 
	 * @return a standard set method name like : "setMyAttribute". */
	private static final String getSetMethodFromFieldName(final String fieldName) { 
		return "set" +  fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);  
	}

} // EOC AttrDesc 58 lines 

// this.fieldName = StrUtils.toUpperUnderscoreStr(nomCol) ; 
// this.ltField = StrUtils.getFieldFromUnderscored(fieldName,Lt.class); // ADDJER   
// this.ltDesc = new AttrDesc(Lt.class,StrUtils.toUpperUnderscoreStr(nomCol)); 
