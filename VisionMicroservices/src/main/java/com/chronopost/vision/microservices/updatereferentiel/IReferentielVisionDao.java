package com.chronopost.vision.microservices.updatereferentiel;

import java.util.List;

import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.model.updatereferentiel.contrat.ContratCarac;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.DriverException;

public interface IReferentielVisionDao {

	void setCassandraSession(Session session);

	void insertRefContrat(String version, List<ContratCarac> liste_contrats) throws DriverException;

	boolean checkInsertVersionComplete(String version, int nbContrats) throws DriverException;

	void updateParametreVersionContrat(String version) throws DriverException;

	void setRefentielParametre(CacheManager<Parametre> cacheManager);

}
