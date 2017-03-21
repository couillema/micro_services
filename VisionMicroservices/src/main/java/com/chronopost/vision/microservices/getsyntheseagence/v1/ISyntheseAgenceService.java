package com.chronopost.vision.microservices.getsyntheseagence.v1;

import java.util.Map;

import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseColisEtListeValeurs;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseListeValeurs;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseDispersionQuantite;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseDispersionQuantitePassee;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseDistributionQuantite;

public interface ISyntheseAgenceService {
	void setDao(ISyntheseAgenceDao dao);

	/**
	 * Récupérer les listes de colis saisis et de colis a saisir (sous forme
	 * colis / specifsColis) pour les N journées précédentes
	 * (getDispersionByAgencePeriode) <br>
	 * Générer les collections dispersion quantite Calculer la synthèse
	 * dispersion retourner la synthèse dispersion
	 * 
	 * @param posteComptable
	 *            : le poste comptable de l’agence
	 * @param nbJours
	 *            : le nombre de jour passés sur lesquels faire le calcul
	 * @return la synthèse dispersion
	 */
	SyntheseDispersionQuantitePassee getSyntheseDispersionQuantitePassee(String posteComptable, Integer nbJours);

	/**
	 * Récupérer les listes de colis saisis et de colis a saisir (sous forme
	 * colis / specifsColis) pour la Nieme journées précédentes
	 * (getDispersionByAgencePeriode) <br>
	 * Générer les collections dispersion quantite Calculer la synthèse
	 * dispersion retourner la synthèse dispersion
	 * 
	 * @param posteComptable
	 *            : le poste comptable de l’agence
	 * @param dateAppel
	 *            : La date et l'heure locale d'appel du MS au format ISO 8601
	 * @return
	 */
	SyntheseDispersionQuantite getSyntheseDispersionQuantite(String posteComptable, String dateAppel);

	/**
	 * Récupérer les listes de colis saisis pour la Nieme journées précédentes
	 * (getDispersionByAgencePeriode) <br>
	 * Générer les collections distribution quantite Calculer la synthèse
	 * distribution retourner la synthèse distribution
	 * 
	 * @param posteComptable
	 *            : le poste comptable de l’agence
	 * @param dateAppel
	 *            : La date et l'heure locale d'appel du MS au format ISO 8601
	 * @return
	 */
	SyntheseDistributionQuantite getSyntheseDistributionQuantite(String posteComptable, String dateAppel);

	/**
	 * Renvoie la liste raffinée et le nombre total des colis qui correspond aux
	 * critères ( les listes de valeurs sélectionnées par l'utilisateur )
	 * 
	 * @param SyntheseColisEtListeValeurs
	 *            qui correspond aux critères des filtres
	 * @param posteComptable
	 *            : le poste comptable de l’agence
	 * @param codeIndicateur
	 *            : le code de l'indicateur
	 * @param limit
	 *            : le nombre de colis attendu
	 * @param dateAppel
	 *            : La date et l'heure locale d'appel du MS au format ISO 8601
	 * @return un objet <code>SyntheseColisEtListeValeurs</code> qui
	 *         contient la liste raffinée et le nombre total des colis
	 * 
	 * @author bjbari
	 */
	SyntheseColisEtListeValeurs getSyntheseDetailIndicateurRaffine(
			SyntheseListeValeurs syntheseDispersionListeValeurs, String posteComptable, String codeIndicateur,
			Integer limit, String dateAppel, Integer nbJours);

	/**
	 * Renvoie la liste des colis, le nombre total des colis et la liste des
	 * valeurs pour un indicateur des jours précédents
	 * 
	 * @param posteComptable
	 *            : le poste comptable de l’agence
	 * @param codeIndicateur
	 *            : le code de l'indicateur
	 * @param limit
	 *            : le nombre de colis attendu
	 * @param dateAppel
	 *            : La date et l'heure locale d'appel du MS au format ISO 8601
	 * @param nbJours
	 *            : Nombre de jours
	 * @return Un objet de type
	 *         <code>SyntheseColisEtListeValeurs</code> qui contient
	 *         la liste des colis, les listes des valeurs et le nombre total des
	 *         colis
	 * 
	 */
	SyntheseColisEtListeValeurs getSyntheseDetailIndicateur(String posteComptable,
			String codeIndicateur, Integer limit, String dateAppel, Integer nbJours);

	/**
	 * Renvoie une map qui regroupe les colis par code dispersion et selon les
	 * précocité
	 * 
	 * @param posteComptable
	 *            : le poste comptable de l’agence
	 * @param codeIndicateur
	 *            : le code de l'indicateur
	 * @param dateAppel
	 *            : La date et l'heure locale d'appel du MS au format ISO 8601
	 * @param nbJours
	 * @return
	 * 
	 * @author bjbari
	 * 
	 */
	Map<String, Map<String, Integer>> getSyntheseDispersionGroupByCodeDispersion(String posteComptable,
			String codeIndicateur, String dateAppel, Integer nbJours);

	/**
	 * Renvoie la synthèse dispersion pour un interval de temps dans le passé
	 * 
	 * @param posteComptable
	 *            : le poste comptable de l’agence
	 * @param dateDebut
	 *            : La date et l'heure de début au format ISO 8601
	 * @param dateFin
	 *            : La date et l'heure de fin au format ISO 8601
	 * @return
	 */
	SyntheseDispersionQuantite getSyntheseDispersionQuantiteJoursPrecedents(String posteComptable, String dateDebut,
			String dateFin);

	/**
	 * Renvoie la liste des colis, le nombre total des colis et la liste des
	 * valeurs pour un indicateur ET un intervalle de temps [dateDebut, dateFin]
	 * 
	 * @param posteComptable
	 *            : le poste comptable de l’agence
	 * @param indicateur
	 *            : le code de l'indicateur
	 * @param dateDebut
	 *            : La date et l'heure de début au format ISO 8601
	 * @param dateFin
	 *            : La date et l'heure de fin au format ISO 8601
	 * @return Un objet de type
	 *         <code>SyntheseDispersionColisEtListeValeurs</code> qui contient
	 *         la liste des colis, les listes des valeurs et le nombre total des
	 *         colis
	 */
	SyntheseColisEtListeValeurs getSyntheseDispersionDetailIndicateurJoursPrecedents(String posteComptable,
			String indicateur, String dateDebut, String dateFin);

	/*
	 * Déclaration des appels et echecs du ms dans la table
	 * microservice_counters
	 */
	void declareAppelMS();

	void declareFailMS();
}
