package com.chronopost.vision.microservices.colioutai.log.v2.dao;

import java.util.Date;
import java.util.List;

import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.colioutai.v2.ColioutaiInfoLT;
import com.chronopost.vision.model.colioutai.v2.ColioutaiLog;

public interface ColioutaiLogDao {
	
	boolean insertLog(ColioutaiInfoLT colioutaiInfoLT) throws MSTechnicalException, FunctionalException;
	
	List<ColioutaiLog> getColioutaiLog(String typeLog,Date from, Date to) throws MSTechnicalException;

}
