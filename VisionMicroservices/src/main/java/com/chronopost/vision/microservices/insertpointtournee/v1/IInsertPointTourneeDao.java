package com.chronopost.vision.microservices.insertpointtournee.v1;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.tournee.getdetailtournee.v1.Tournee;
import com.chronopost.vision.model.Agence;
import com.chronopost.vision.model.CodeService;
import com.chronopost.vision.model.Evt;

public interface IInsertPointTourneeDao {

    /**
     * Insert/Update un point à partir d'un événement fourni
     * 
     * On mémorise dans le point en base, les infos destinataire (nom + type) et
     * l'événements raccourci à son type (code_evt) sa date son no_lt son
     * diffETA son diffGPS ses anomalies
     * 
     * Ces informations ne sont pas forcément toutes disponibles pour tous les
     * événements Les anomalies (set<String>) correspondent aux anomalies de
     * l'événement et du colis
     * 
     * @param evenement
     *            : L'événement à considérer
     * @return : boolean true=ok
     * @throws FunctionalException
     *             : Si l'événement ne contient pas d'identifiant de point
     * 
     */
    boolean addEvtDansPoint(@NotNull List<Evt> evenement);

    /**
     * Traitement des événements sur colis fictif. Debut ou Fin de tournée, et
     * mise à jour de la tournée pour y indiquer le début ou la fin de la
     * tournée.
     * 
     * @param evenement
     *            : L'événement à considérer
     * @return : boolean true=ok
     */

    boolean miseAJourTournee(List<Evt> evenement);
    
    Evt trouverDernierEvtTA(Evt evenement);
    
    Tournee trouverDerniereTournee(Evt evenement);

    /**
     * Injecte le référentiel des codes service
     * @param transcoder
     * @return
     */
	IInsertPointTourneeDao setRefentielCodeService(CacheManager<CodeService> referentiel);

	IInsertPointTourneeDao setRefentielAgence(CacheManager<Agence> cacheManager);

	IInsertPointTourneeDao setRefentielParametre(CacheManager<Parametre> cacheManagerParametre);

	/**
	 * Comptabilisation de l'appel dans les compteur de microservice
	 * @param nbTrt
	 * @param nbFail
	 */
	void updateCptTrtTrtFailMS(int nbTrt, int nbFail);

	void updateCptHitMS();

	void updateCptFailMS();

	void declareErreur(final Evt evt, final String methode, final Exception except);
}
