package com.chronopost.vision.microservices.insertevt.v1;

import java.text.ParseException;
import java.util.List;

import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.model.Evt;

public interface IInsertEvtDao {
	
	/**
	 * Insertion d'une liste d'événements dans la table evt.
	 * 
	 * @param evts
	 * @return
	 * @throws ParseException 
	 */
	public List<Integer> insertEvts(final List<Evt> evts) throws ParseException;
	public boolean insertEvts(final List<Evt> evts, final long nbEvtsOrig) throws ParseException;
	
	public Integer getPrioriteEvt(final Evt evt);
	public void insertDiffEvtCounter(final int size);
	IInsertEvtDao setRefentielParametre(CacheManager<Parametre> cacheAgence);
	
	/**
	 * Comptabilisation de l'appel dans les compteur de microservice
	 * @param nbTrt
	 * @param nbFail
	 */
	public void updateCptTrtTrtFailMS(int nbTrt, int nbFail);

	public void updateCptHitMS();

	public void updateCptFailMS();

}
