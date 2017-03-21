package com.chronopost.vision.microservices.colioutai.get.services;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;

import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.colioutai.ColioutaiInfoLT;
import com.chronopost.vision.model.colioutai.ColioutaiLog;

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
	ColioutaiInfoLT findInfoLT(String noLT, Date dateCalcul, String mockTempsTrajets) throws ColioutaiException, MalformedURLException;
	
	public boolean insertLog(ColioutaiInfoLT colioutaiInfoLT) throws MSTechnicalException, FunctionalException;
	
	public List<ColioutaiLog> getColioutaiLog(String typeLog, Date from, Date to) throws MSTechnicalException;
}
