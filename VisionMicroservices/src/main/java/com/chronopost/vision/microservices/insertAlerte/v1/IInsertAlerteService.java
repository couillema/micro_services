package com.chronopost.vision.microservices.insertAlerte.v1;

import java.util.List;

import com.chronopost.vision.model.insertAlerte.v1.Alerte;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;

public interface IInsertAlerteService {
	
	void setDao(IInsertAlerteDao insertAlerteDao);
	
	/**
	 * 
	 * @param alertes
	 * @return
	 */
	void insertAlertes(List<Alerte> alertes)throws NoHostAvailableException, QueryExecutionException, QueryValidationException;

}
