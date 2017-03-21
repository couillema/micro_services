package com.chronopost.vision.microservices.lt.insert;

import java.util.List;

import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Lt;


/**
 * Service de traitement et d'insertion des LTs
 * @author jcbontemps
 *
 */
public interface IInsertLtService {

	/**
	 * méthode appelée pour traiter les LTs fournis. Notamment pour les insérer
	 * en base au moyen d'un DAO
	 * 
	 * @param lts
	 * @return true si l'insertion a eu lieu
	 * @throws MSTechnicalException
	 * @throws FunctionalException
	 */
	boolean insertLtsInDatabase(List<Lt> lts) throws MSTechnicalException, FunctionalException;

	/**
	 * Ajoute un DAO au service pour lui permettre d'effectuer l'insertion en
	 * base
	 * 
	 * @param insertLtDAO
	 *            DAO utilisé pour l'insertion en base
	 */
	void setDao(InsertLtDAO insertLtDAO);

}

