package com.chronopost.vision.microservices.colioutai.get.v2.services;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;

import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.colioutai.v2.ColioutaiInfoLT;
import com.chronopost.vision.model.colioutai.v2.ColioutaiLog;

/**
 * Interface du service COILOUTAI
 * @author vdesaintpern
 *
 */
public interface ColioutaiService {

	/**
	 * Recherche des infos sur le colis pour colioutai
	 * @param noLT
	 * @return
	 * @throws ColioutaiException
	 * @throws MalformedURLException 
	 */	
	ColioutaiInfoLT findInfoLT(String noLT, Date dateCalcul, String mockTempsTrajets,String client) throws ColioutaiException, MalformedURLException;
	
	ColioutaiInfoLT findInfoLT(String noLT, Date dateCalcul, String mockTempsTrajets) throws ColioutaiException, MalformedURLException;
	
	boolean insertLog(ColioutaiInfoLT colioutaiInfoLT) throws MSTechnicalException, FunctionalException;
	
	List<ColioutaiLog> getColioutaiLog(String typeLog, Date from, Date to) throws MSTechnicalException;
	
	String calculLtHash(String infoLt);
}
