package com.chronopost.vision.microservices.insertagencecolis.v1;

import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import com.chronopost.vision.model.Evt;

/**
 * Dao assurant l'interface avec la table colis_agence
 * @author lguay
 *
 */
public interface IInsertAgenceColisDao {

    /**
     * Déclare les saisis d'événements dans les agences et au moment indiqué
     * 
     * @param evenement : Les événements à considérer
     * @return : boolean true=ok
     */
    boolean addColisInSaisisAgence(@NotNull List<Evt> evenement);

    /**
     * Traitement des événements d'exclusion avec leur date de reprise en compte
     * 
     * @param evenement : La liste des evenements avec leur date de reapparition prévue
     * @return : boolean true=ok
     */

    boolean addColisInASaisirAgence(List<EvtExclus> evenement);
    
    
	/**
	 * Insertion des noLts dans la colonne colis_restant_tg2 de la table colis_agence pour une agence à 23h50
	 * @param agence
	 * @param jour
	 * @param noLts
	 * @return
	 */
	boolean updateColisRestantTG2(final String agence, final String jour, final Set<String> noLts);
	/**
	 * Comptabilisation de l'appel dans les compteur de microservice
	 * @param nbTrt
	 * @param nbFail
	 */
	public void updateCptTrtTrtFailMS(int nbTrt, int nbFail);

	public void updateCptHitMS();

	public void updateCptFailMS();
}
