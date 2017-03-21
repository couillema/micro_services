package com.chronopost.vision.microservices.insertagencecolis.v1;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.model.Agence;
import com.chronopost.vision.model.Evenement;
import com.chronopost.vision.model.Evt;

public interface IInsertAgenceColisService {


    /**
     * Traitement de tous les evenements reçus ayant une étape.
     * Pour chaque evenement ayant une étape non transverse le colis est déclaré saisi dans l'agence de l'événement à l'heure indiqué
     * Pour Chaque evenement d'exclusion, le colis est déclaré à saisir à la date de fin d'exclusion dans l'agence de l'évt. 
     * 
     * @param evenements : les evenements à traiter
     * @return true si tous les evenements ayant une étape ont été traités avec succes
     * @throws ExecutionException
     * @throws InterruptedException
     */
    boolean traiteEvenement(List<Evt> evenements) throws InterruptedException, ExecutionException;

    /**
     * Injection de la DAO
     * 
     * @param pDao
     */
	IInsertAgenceColisService setDao(IInsertAgenceColisDao pDao);

    /** Injection du cacheManager des événements
     * 
     * @param cacheEvenement : le cacheManager
     * @return le service (this) afin de permettre les initialisations en chaine. */
    IInsertAgenceColisService setRefentielEvenement(CacheManager<Evenement> cacheEvenement);
    /** Injection du cacheManager des agences
     * 
     * @param cacheAgence : le cacheManager
     * @return le service (this) afin de permettre les initialisations en chaine. */
    IInsertAgenceColisService setRefentielAgence(CacheManager<Agence> cacheAgence);
    
	/**
	 * Insertion des noLts dans la colonne colis_restant_tg2 de la table colis_agence pour une agence à 23h50
	 * @param agence
	 * @param jour
	 * @param noLts
	 * @return
	 */
	boolean setRestantTG2(final String agence, final String jour, final Set<String> noLts);
	
	

	public void declareAppelMS();
	public void declareFailMS();
}