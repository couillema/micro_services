package com.chronopost.vision.microservices.insertAlerte.v1;

import java.util.List;

import com.chronopost.vision.model.insertAlerte.v1.Alerte;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;

public interface IInsertAlerteDao {
	
	void attachesAlertes(List<Alerte> alertes)
			throws NoHostAvailableException, QueryExecutionException, QueryValidationException;

	void inserteAlertes(List<Alerte> alertes) 
			throws NoHostAvailableException, QueryExecutionException, QueryValidationException;
}
