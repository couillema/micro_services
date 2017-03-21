package com.chronopost.vision.microservices.insertAlerte.v1;

import java.util.ArrayList;
import java.util.List;

import com.chronopost.vision.model.insertAlerte.v1.Alerte;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;

public enum InsertAlerteServiceImpl implements IInsertAlerteService {
	INSTANCE;

	private IInsertAlerteDao insertAlerteDao;

	@Override
	public void setDao(IInsertAlerteDao insertAlerteDao) {
		this.insertAlerteDao = insertAlerteDao;
	}

	private final static String TYPE_RPTSDTA = "RPTSDTA";

	@Override
	public void insertAlertes(final List<Alerte> alertes)throws NoHostAvailableException, QueryExecutionException, QueryValidationException {
		List<Alerte> alerteRPTSDTA = filtrerAlertes(alertes, TYPE_RPTSDTA);
		
		if (!alerteRPTSDTA.isEmpty()) {
			insertAlerteDao.inserteAlertes(alertes);
			insertAlerteDao.attachesAlertes(alertes);
		}
	}

	/**
	 * Renvoie la liste des alertes avec le type "type"
	 * @param alertes : Liste des alertes initiales
	 * @param type : Type de l'alerte
	 * @return
	 */
	private List<Alerte> filtrerAlertes(final List<Alerte> alertes, final String type) {
		List<Alerte> alerteRPTSDTA = new ArrayList<>();
		for (Alerte alerte : alertes) {
			if (TYPE_RPTSDTA.equals(alerte.getType()))
				alerteRPTSDTA.add(alerte);
		}
		return alerteRPTSDTA;
	}

}
