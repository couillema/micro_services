package com.chronopost.vision.microservices.getsyntheseagence.v1;

import java.util.Date;

import com.chronopost.vision.microservices.getsyntheseagence.v1.collections.ColisSpecByAgence;

public interface ISyntheseAgenceDao {
	/**
	 * Calcul des clés de la table colis_agence correspondant à la fenêtre de
	 * temps fournie Pour chaque clé Récupération de la liste des colis saisis
	 * et à saisir Récupérer la totalité des specifsColis de tous les colis des
	 * deux listes
	 * 
	 * Cumul des listes de colis saisis et à saisir dans 2 listes (ou set) et
	 * placement dans un objet de transit
	 * 
	 * @param posteComptable
	 *            Un poste comptable d’agence
	 * @param start
	 *            date de début de la fenêtre de temps
	 * @param end
	 *            date de fin de la fenêtre de temps
	 * @return l’objet de transit
	 */
	ColisSpecByAgence getDispersionByAgencePeriode(String posteComptable, Date startDate, Date endDate);

	/**
	 * Calcul des clés de la table colis_agence correspondant à la fenêtre de
	 * temps fournie. Pour chaque clé Récupération de la liste des colis saisis.
	 * Récupérer la totalité des specifsColis de tous les colis de la liste
	 * colis saisis.
	 * 
	 * @param posteComptable
	 *            Un poste comptable d’agence
	 * @param start
	 *            date de début de la fenêtre de temps
	 * @param end
	 *            date de fin de la fenêtre de temps
	 * @return l’objet de transit
	 */
	ColisSpecByAgence getDistributionByAgencePeriode(String posteComptable, Date startDate, Date endDate);

	ColisSpecByAgence getDispersionByAgencePeriodePassee(String posteComptable, Date startDate, Date endDate);

	/**
	 * Comptabilisation de l'appel dans les compteur de microservice
	 * 
	 * @param nbTrt
	 * @param nbFail
	 */
	void updateCptTrtTrtFailMS(int nbTrt, int nbFail);

	void updateCptHitMS();

	void updateCptFailMS();
}
