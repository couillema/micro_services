package com.chronopost.vision.microservices.insertC11;

import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.model.insertC11.TourneeC11;
import com.datastax.driver.core.exceptions.DriverException;

public interface IInsertC11Dao {

	IInsertC11Dao setRefentielParametre(final CacheManager<Parametre> cacheParametre);
	
	boolean miseAJourTournee(final TourneeC11 tourneeC11) throws DriverException;

	boolean miseAJourPoints(final TourneeC11 tourneeC11) throws DriverException;

	boolean miseAJourIdxTourneeJour(final TourneeC11 tourneeC11) throws DriverException;

}
