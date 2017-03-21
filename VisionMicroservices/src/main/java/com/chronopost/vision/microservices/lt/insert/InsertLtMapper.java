package com.chronopost.vision.microservices.lt.insert;

import java.lang.reflect.Field;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.chronopost.vision.microservices.enums.EInsertLT;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Lt;

/** Classe de mapping entre les champs du DAO et ceux du model 
 * @author jjean-charles : use getFieldName() + final + DOCS done (factor Exception)  remove extractValue */
public class InsertLtMapper {
    /** Set in Caps for sonar  */
    private static final Logger LOGGER = LogManager.getLogger(EInsertLT.class);

    /** Empty CST     */
    private InsertLtMapper() {     }

    /**@param insertEnum the field to read
     * @param lt where to read 
     * @return the field of LT ( Handles the Exception of POSITION )
     * @throws MSTechnicalException   IF SecurityException | IllegalArgumentException | IllegalAccessException      **/
    public static final Object getLtValue(final EInsertLT insertEnum, final Lt lt) throws /* FunctionalException,*/   MSTechnicalException {
        Object object = null;
        Field field = null ; 
        try {
        	if ( insertEnum == EInsertLT.POSITION  )  // ONLY EXCEPTION  
        		return "" + lt.getPositionTournee() ; // WANT a String not an Integer   
        	 field = insertEnum.getField() ; // Rem: NoSuchFieldException statically handled by Enum.  this is never null
        	 synchronized (insertEnum) { // field is also possible. Solves the illegal access to attribute 
        		 field.setAccessible(true);
        		 object = field.get(lt);     // this is the attribute value and class : not the getter !! ( could or should use gettter ??)
        		 field.setAccessible(false);
        	 }
        }  catch (SecurityException  | IllegalAccessException | IllegalArgumentException e) {  // why is IllegalArgumentException removable ?? 
        	final String msg = "Erreur de récupération du champ (accès) '" + field + "' de la lt "  + String.valueOf(lt.getNoLt() + " Type Exp = " + e.getClass().getSimpleName()) ;  
        	LOGGER.info(msg , e);
            throw new MSTechnicalException(msg, e);
        } 
     
        return object;
    }

} // EOC InsertLtMapper
