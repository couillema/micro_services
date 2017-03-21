package com.chronopost.vision.microservices.colioutai.log.dao;

import java.util.Date;
import java.util.List;

import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.colioutai.ColioutaiInfoLT;
import com.chronopost.vision.model.colioutai.ColioutaiLog;

public interface ColioutaiLogDao {
	
	boolean insertLog(ColioutaiInfoLT colioutaiInfoLT) throws MSTechnicalException, FunctionalException;
	
	List<ColioutaiLog> getColioutaiLog(String typeLog,Date from, Date to) throws MSTechnicalException;

}
