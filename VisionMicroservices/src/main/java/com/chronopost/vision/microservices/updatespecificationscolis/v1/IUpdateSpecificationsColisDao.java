package com.chronopost.vision.microservices.updatespecificationscolis.v1;

import java.util.List;

import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;

/**
 * Interface DAO
 * @author jcbontemps
 */
public interface IUpdateSpecificationsColisDao {

    /**
     * @param specs la liste des spécificités service à ajouter au colis
     * @return un boolean à &lt;true&gt; si tout le traitement s’est bien déroulé
     */
    boolean updateSpecifsServices(final List<SpecifsColis> specifsColis);

    /**
     * @param specs Liste des colis avec les ajouts de consignes a faire
     * @return un boolean &lt;true&gt; si tout le traitement s’est bien déroulé
     */
    boolean updateConsignes(final List<SpecifsColis> specs);

    /**
     * @param specs la liste des spécificités service à ajouter au colis
     * @return un boolean &lt;true&gt; si tout le traitement s’est bien déroulé
     */
    boolean updateSpecifsEvenements(final List<SpecifsColis> specifsColis);

	/**
	 * Comptabilisation de l'appel dans les compteur de microservice
	 * @param nbTrt
	 * @param nbFail
	 */
	public void updateCptTrtTrtFailMS(int nbTrt, int nbFail);

	public void updateCptHitMS();

	public void updateCptFailMS();

	void declareErreur(final Evt evt, final String methode, final Exception except);
}