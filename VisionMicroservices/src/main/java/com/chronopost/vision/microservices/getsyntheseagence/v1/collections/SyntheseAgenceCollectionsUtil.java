package com.chronopost.vision.microservices.getsyntheseagence.v1.collections;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseColis;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseDispersionQuantite;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseDispersionQuantitePassee;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseDistributionQuantite;
import com.chronopost.vision.model.rules.SpecifsColisRules;
import com.google.common.collect.Sets;

public class SyntheseAgenceCollectionsUtil {

	/**
	 * Renseigne un objet SyntheseDispersionQuantite depuis un objet
	 * CollectionIndicateursDispersion<br>
	 */
	public static SyntheseDispersionQuantite calculSyntheseDispersion(
			final CollectionIndicateursDispersion collection) {
		final SyntheseDispersionQuantite synthese = new SyntheseDispersionQuantite();
		// Calcul synthese
		synthese.setNbColisDisperses(collection.getColisDisperses().size());
		synthese.setNbColisDispersesDeloc(collection.getColisDispersesDeloc().size());
		synthese.setNbColisRemisEnDistri(collection.getColisRemisEnDistri().size());
		synthese.setNbColisDispersesPoste(collection.getColisDispersesPoste().size());
		synthese.setNbColisPrisEnChargePoste(collection.getColisPrisEnChargePoste().size());
		synthese.setNbColisSDSeche(collection.getColisSDSeche().size());
		synthese.setNbColisSDSecheDeloc(collection.getColisSDSecheDeloc().size());
		synthese.setNbColisSDSechePoste(collection.getColisSDSechePoste().size());
		synthese.setNbColisAPreparer(collection.getColisAPreparer().size());
		synthese.setNbColisPrepares(collection.getColisPrepares().size());
		synthese.setNbColisPreparesDeloc(collection.getColisPreparesDeloc().size());
		synthese.setNbColisExclusLivraisonMatin(collection.getColisExclusLivraisonMatin().size());
		synthese.setNbColisExclusLivraisonUnJour(collection.getColisExclusLivraisonUnJour().size());
		synthese.setNbColisExclusLivraisonXJours(collection.getColisExclusLivraisonXJours().size());
		synthese.setNbColisNonRemisEnDistribution(collection.getColisNonRemisEnDistribution().size());
		synthese.setNbColisTASansSD(collection.getColisTASansSD().size());
		synthese.setNbColisTASansSDDeloc(collection.getColisTASansSDDeloc().size());
		synthese.setNbColisIncidentTG2Jour(collection.getColisIncidentTG2Jour().size());
		synthese.setNbColisPerdus(collection.getColisPerdus().size());
		synthese.setNbColisSDSecheTotal(collection.getColisSDSecheTotal().size());
		synthese.setNbColisEnAlerteActive(collection.getColisEnAlerteActive().size());
		synthese.setNbColisEnAlerteActiveDeloc(collection.getColisEnAlerteActiveDeloc().size());
		synthese.setNbColisEnCours(collection.getColisEnCours().size());
		return synthese;
	}

	/**
	 * Calcule la synthese dispersion des jours précédents
	 * 
	 * @param indicateursDispersionPassee
	 * @return un objet de type <code>SyntheseDispersionQuantitePassee</code>
	 * 
	 * @author bjbari
	 */
	public static SyntheseDispersionQuantitePassee calculSyntheseDispersionPassee(
			final CollectionIndicateursDispersionPassee indicateursDispersionPassee) {
		final SyntheseDispersionQuantitePassee synthese = new SyntheseDispersionQuantitePassee();
		synthese.setNbColisSDSechePassee(indicateursDispersionPassee.getColisSDSechePassee().size());
		synthese.setNbColisSDSechePostePassee(indicateursDispersionPassee.getColisSDSechePostePassee().size());
		synthese.setNbColisNonRemisEnDistributionPassee(
				indicateursDispersionPassee.getColisNonRemisEnDistributionPassee().size());
		synthese.setNbColisPerdusPassee(indicateursDispersionPassee.getColisPerdusPassee().size());
		synthese.setNbColisEnAlerteActivePassee(indicateursDispersionPassee.getColisEnAlerteActivePassee().size());
		synthese.setNbColisEnAlerteActiveDelocPassee(
				indicateursDispersionPassee.getColisEnAlerteActiveDelocPassee().size());
		synthese.setNbColisSDSecheDelocPassee(indicateursDispersionPassee.getColisSDSecheDelocPassee().size());
		return synthese;
	}

	/**
	 * Renseigne un objet SyntheseDistributionQuantite depuis un objet
	 * CollectionIndicateursDistribution<br>
	 */
	public static SyntheseDistributionQuantite calculSyntheseDistribution(
			final CollectionIndicateursDistribution collection) {
		final SyntheseDistributionQuantite synthese = new SyntheseDistributionQuantite();
		synthese.setNbColisDistribues(collection.getColisDistribues().size());
		synthese.setNbColisDistribuesDeloc(collection.getColisDistribuesDeloc().size());
		synthese.setNbColisAvecEchec(collection.getColisAvecEchec().size());
		synthese.setNbColisAvecEchecDeloc(collection.getColisAvecEchecDeloc().size());
		synthese.setNbColisInstance(collection.getColisInstance().size());
		synthese.setNbColisInstanceDeloc(collection.getColisInstanceDeloc().size());
		synthese.setNbColisTASeche(collection.getColisTASeche().size());
		synthese.setNbColisTASecheDeloc(collection.getColisTASecheDeloc().size());
		synthese.setNbColisInstanceNonAcquittes(collection.getColisInstanceNonAcquittes().size());
		synthese.setNbColisInstanceNonAcquittesDeloc(collection.getColisInstanceNonAcquittesDeloc().size());
		synthese.setNbColisEnEchec(collection.getColisEnEchec().size());
		synthese.setNbColisEnEchecDeloc(collection.getColisEnEchecDeloc().size());
		return synthese;
	}

	/**
	 * Renseigne un objet SyntheseDispersionQuantite depuis un objet
	 * SyntheseDispersionQuantite<br>
	 */
	public static CollectionIndicateursDistribution calculCollectionsIndicateurDistribution(
			final CollectionsDistribution collectionDistribution, final String dateAppel) {
		final CollectionIndicateursDistribution indicateursDistribution = new CollectionIndicateursDistribution();
		indicateursDistribution.setColisDistribues(computeColisDistribues(collectionDistribution));
		indicateursDistribution.setColisDistribuesDeloc(collectionDistribution.getLivreDomicilePADeloc());
		indicateursDistribution.setColisAvecEchec(computeColisAvecEchec(collectionDistribution));
		indicateursDistribution.setColisAvecEchecDeloc(computeColisAvecEchecDeloc(collectionDistribution));
		indicateursDistribution.setColisInstance(computeColisInstance(collectionDistribution));
		indicateursDistribution.setColisInstanceDeloc(collectionDistribution.getEnInstancePADeloc());
		indicateursDistribution.setColisTASeche(computeTASeche(collectionDistribution));
		indicateursDistribution.setColisTASecheDeloc(computeColisTASecheDeloc(collectionDistribution));
		indicateursDistribution.setColisInstanceNonAcquittes(computeColisInstanceNonAcquittes(collectionDistribution));
		indicateursDistribution.setColisInstanceNonAcquittesDeloc(computeColisInstanceNonAcquittesDeloc(collectionDistribution));
		indicateursDistribution.setColisEnEchec(computeColisEnEchec(collectionDistribution));
		indicateursDistribution.setColisEnEchecDeloc(computeColisEnEchecDeloc(collectionDistribution));
		return indicateursDistribution;
	}

	public static CollectionIndicateursDispersion calculCollectionsIndicateurDispersion(
			final CollectionsDispersion collection, final String dateAppel) {
		final CollectionIndicateursDispersion indicateursDispersion = new CollectionIndicateursDispersion();
		indicateursDispersion.setColisDisperses(collection.getDisperses());
		indicateursDispersion.setColisDispersesDeloc(collection.getDispersesDeloc());
		indicateursDispersion.setColisEnDispersion(collection.getEnDispersion());
		indicateursDispersion.setColisRemisEnDistri(collection.getARemettreEnDistribution());
		indicateursDispersion.setColisDispersesPoste(collection.getDispersesPoste());
		indicateursDispersion.setColisPrisEnChargePoste(collection.getAcquittementsPoste());
		indicateursDispersion.setColisSDSeche(computeColisSDSeche(collection, dateAppel));
		indicateursDispersion.setColisSDSecheDeloc(computeColisSDSecheDeloc(collection, dateAppel));
		indicateursDispersion.setColisSDSechePoste(collection.getEnDispersionPoste());
		indicateursDispersion.setColisAPreparer(computeColisAPreparer(collection));
		indicateursDispersion.setColisPrepares(computeColisPrepares(collection));
		indicateursDispersion.setColisPreparesDeloc(computeColisPreparesDeloc(collection));
		indicateursDispersion.setColisExclusLivraisonMatin(collection.getExclusAJ());
		indicateursDispersion.setColisExclusLivraisonUnJour(collection.getExclusAJ1());
		indicateursDispersion.setColisExclusLivraisonXJours(collection.getExclusAJX());
		indicateursDispersion.setColisNonRemisEnDistribution(collection.getNonRemisEnDistribution());
		indicateursDispersion.setColisTASansSD(computeColisTASansSD(collection));
		indicateursDispersion.setColisTASansSDDeloc(computeColisTASansSDDeloc(collection));
		indicateursDispersion.setColisIncidentTG2Jour(collection.getIncidentTG2Jour());
		indicateursDispersion.setColisPerdus(collection.getPerdus());
		indicateursDispersion.setColisSDSecheTotal(computeColisSDSecheTotal(collection, dateAppel));
		indicateursDispersion.setColisEnAlerteActive(collection.getEnAlerteActive());
		indicateursDispersion.setColisEnAlerteActiveDeloc(collection.getEnAlerteActiveDeloc());
		indicateursDispersion.setColisEnCours(computeColisEnCours(collection));
		return indicateursDispersion;
	}

	/**
	 * Calcule la collection des indicateurs de jours précédents
	 * 
	 * @param collectionDispersionPassee
	 * @return un objet de type
	 *         <code>CollectionIndicateursDispersionPassee</code>
	 * 
	 * 
	 * @author bjbari
	 */
	public static CollectionIndicateursDispersionPassee calculCollectionsIndicateurDispersionPassee(
			final CollectionsDispersionPassee collectionDispersionPassee) {
		final CollectionIndicateursDispersionPassee indicateursDispersionPassee = new CollectionIndicateursDispersionPassee();

		indicateursDispersionPassee.setColisSDSechePassee(computeColisSDSechePassee(collectionDispersionPassee));
		indicateursDispersionPassee.setColisSDSechePostePassee(collectionDispersionPassee.getEnDispersionPostePassee());
		indicateursDispersionPassee.setColisNonRemisEnDistributionPassee(
				computeColisNonRemisEnDistributionPassee(collectionDispersionPassee));
		indicateursDispersionPassee.setColisPerdusPassee(collectionDispersionPassee.getPerdusPassee());
		indicateursDispersionPassee.setColisEnAlerteActivePassee(collectionDispersionPassee.getEnAlerteActivePassee());
		indicateursDispersionPassee
				.setColisEnAlerteActiveDelocPassee(collectionDispersionPassee.getEnAlerteActiveDelocPassee());
		indicateursDispersionPassee.setColisSDSecheDelocPassee(collectionDispersionPassee.getEnDispersionDelocPassee());
		return indicateursDispersionPassee;
	}
	
	private static Set<String> computeColisInstance(final CollectionsDistribution collection){
		// si n'est pas activé on retire les delocs
		if (!FeatureFlips.INSTANCE.getBoolean("SADelocActif", Boolean.FALSE))
			return collection.getEnInstancePA();
		else 
			return Sets.difference(collection.getEnInstancePA(), collection.getEnInstancePADeloc());
	}

	private static Set<String> computeColisAvecEchec(final CollectionsDistribution collection) {
		final Set<String> colisFiltres = new HashSet<>();
		colisFiltres.addAll(collection.getEchecPADeloc());
		colisFiltres.addAll(collection.getLivreDomicileP());
		colisFiltres.addAll(collection.getEnInstanceP());
		colisFiltres.addAll(collection.getSortieReseau());
		return Sets.difference(collection.getEchecPA(), colisFiltres);
	}

	private static Set<String> computeColisAvecEchecDeloc(final CollectionsDistribution collection) {
		final Set<String> colisFiltres = new HashSet<>();
		colisFiltres.addAll(collection.getLivreDomicileP());
		colisFiltres.addAll(collection.getEnInstanceP());
		colisFiltres.addAll(collection.getSortieReseau());
		return Sets.difference(collection.getEchecPADeloc(), colisFiltres);
	}

	private static Set<String> computeTASeche(final CollectionsDistribution collection) {
		final Set<String> colisFiltres = new HashSet<>();
		// si n'est pas activé on retire les delocs
		if (FeatureFlips.INSTANCE.getBoolean("SADelocActif", Boolean.FALSE))
			colisFiltres.addAll(collection.getEnPrepaPADeloc());
		colisFiltres.addAll(collection.getSortieReseau());
		return Sets.difference(collection.getEnPrepaPA(), colisFiltres);
	}

	private static Set<String> computeColisInstanceNonAcquittes(final CollectionsDistribution collection) {
		final Set<String> colisFiltres = new HashSet<>();
		// si n'est pas activé on retire les delocs
		if (FeatureFlips.INSTANCE.getBoolean("SADelocActif", Boolean.FALSE))
			colisFiltres.addAll(collection.getEnInstancePADeloc());
		colisFiltres.addAll(collection.getSortieReseau());
		return Sets.difference(collection.getEnInstancePA(), colisFiltres);
	}

	private static Set<String> computeColisEnEchec(final CollectionsDistribution collection) {
		final Set<String> colisFiltres = new HashSet<>();
		// si n'est pas activé on retire les delocs
		if (FeatureFlips.INSTANCE.getBoolean("SADelocActif", Boolean.FALSE))
			colisFiltres.addAll(collection.getEchecPADeloc());
		colisFiltres.addAll(collection.getLivreDomicileP());
		colisFiltres.addAll(collection.getEnInstanceP());
		colisFiltres.addAll(collection.getSortieReseau());
		colisFiltres.addAll(collection.getVueEnRetourP());
		return Sets.difference(collection.getEchecPA(), colisFiltres);
	}

	private static Set<String> computeColisEnEchecDeloc(final CollectionsDistribution collection) {
		final Set<String> colisFiltres = new HashSet<>();
		colisFiltres.addAll(collection.getLivreDomicileP());
		colisFiltres.addAll(collection.getEnInstanceP());
		colisFiltres.addAll(collection.getSortieReseau());
		colisFiltres.addAll(collection.getVueEnRetourP());
		return Sets.difference(collection.getEchecPADeloc(), colisFiltres);
	}

	/**
	 * Calcule la liste des colis en SD sèche.<br>
	 * {Avant 14 => enDispersion - dispersésPoste} {Après 14h => enDispersion +
	 * 
	 * exclusAJ - dispersésPoste}
	 * 
	 * @param collection
	 * @return
	 */
	private static Set<String> computeColisSDSeche(final CollectionsDispersion collection,
			final String dateTime) {
		if (SpecifsColisRules.estAvantQuatorzeHeure(dateTime))
			return Sets.difference(collection.getEnDispersion(), collection.getDispersesPoste());
		else
			return Sets.difference(Sets.union(collection.getEnDispersion(), collection.getExclusAJ()),
					collection.getDispersesPoste());
	}

	/**
	 * Calcule la liste des colis délocalisés en SD sèche.<br>
	 * {Avant 14 => enDispersion - dispersésPoste} {Après 14h => enDispersion +
	 * 
	 * exclusAJ - dispersésPoste}
	 * 
	 * @param collection
	 * @return
	 */
	private static Set<String> computeColisSDSecheDeloc(final CollectionsDispersion collection,
			final String dateTime) {
		if (SpecifsColisRules.estAvantQuatorzeHeure(dateTime))
			return Sets.difference(collection.getEnDispersionDeloc(), collection.getDispersesPoste());
		else
			return Sets.difference(Sets.union(collection.getEnDispersionDeloc(), collection.getExclusAJ()),
					collection.getDispersesPoste());
	}

	/**
	 * Calcule de la liste des colis préparés.<br>
	 * {@code prepares - preparesPoste}
	 * 
	 * @param collection
	 * @return
	 */
	private static Set<String> computeColisPrepares(final CollectionsDispersion collection) {
		return Sets.difference(collection.getPrepares(), collection.getPreparesPoste());
	}

	/**
	 * Calcule de la liste des colis préparés délocalisés.<br>
	 * {@code preparesDeloc - preparesPoste}
	 * 
	 * @param collection
	 * @return
	 */
	private static Set<String> computeColisPreparesDeloc(final CollectionsDispersion collection) {
		return Sets.difference(collection.getPreparesDeloc(), collection.getPreparesPoste());
	}

	/**
	 * Calcule de la liste des colis à préparer.<br>
	 * {@code (disperses + aRemettreEnDistribution) - (dispersesPoste + exclusDuJour)}
	 * 
	 * @param collection
	 * @return
	 */
	private static Set<String> computeColisAPreparer(final CollectionsDispersion collection) {
		final Set<String> colisAPreparer1 = new HashSet<>();
		colisAPreparer1.addAll(collection.getDisperses());
		colisAPreparer1.addAll(collection.getARemettreEnDistribution());

		final Set<String> colisAPreparer2 = new HashSet<>();
		colisAPreparer2.addAll(collection.getDispersesPoste());
		colisAPreparer2.addAll(collection.getExclusDuJour());
		colisAPreparer2.addAll(collection.getIncidentTG2Jour());

		return Sets.difference(colisAPreparer1, colisAPreparer2);
	}

	/**
	 * Calcule de la liste des colis TA sans SD.<br>
	 * {@code prepares - disperses}
	 * 
	 * @param collection
	 * @return
	 */
	private static Set<String> computeColisTASansSD(final CollectionsDispersion collection) {
		return Sets.difference(collection.getPrepares(), collection.getDisperses());
	}

	/**
	 * Calcule de la liste des colis TA sans SD délocalisés.<br>
	 * {@code prepares - disperses}
	 * 
	 * @param collection
	 * @return
	 */
	private static Set<String> computeColisTASansSDDeloc(final CollectionsDispersion collection) {
		return Sets.difference(collection.getPreparesDeloc(), collection.getDispersesDeloc());
	}

	/**
	 * Calcule de la liste des ColisSDSechePassee.<br>
	 * {@code enDispersionPassee - enDispersionPostePassee}
	 * 
	 * @param collection
	 * @return
	 * 
	 * @author bjbari
	 */
	private static Set<String> computeColisSDSechePassee(final CollectionsDispersionPassee collection) {
		return Sets.difference(collection.getEnDispersionPassee(), collection.getEnDispersionPostePassee());
	}

	/**
	 * Calcule de la liste des ColisNonRemisEnDistributionPassee.<br>
	 * {@code aRemettreEnDistributionOubliesPassee  + exclusOubliesPassee}
	 * 
	 * @param collection
	 * @return
	 * 
	 * @author bjbari
	 */
	private static Set<String> computeColisNonRemisEnDistributionPassee(
			final CollectionsDispersionPassee collection) {
		final Set<String> colisNonRemisEnDistributionPassee = new HashSet<>();
		colisNonRemisEnDistributionPassee.addAll(collection.getARemettreEnDistributionOubliesPassee());
		colisNonRemisEnDistributionPassee.addAll(collection.getExclusOubliesPassee());
		return colisNonRemisEnDistributionPassee;
	}

	/**
	 * Calcule liste des colisSDSecheTotal</br>
	 * Avant 14h : {@code (enDispersion+enDispersionDeloc) - enDispersionPoste}
	 * </br>
	 * Après 14h:
	 * {@code (enDispersion+enDispersionDeloc) - enDispersionPoste + exclusAJ}
	 * 
	 * @param collection
	 * @param dateTime
	 * 
	 * @author bjbari
	 * @return
	 */
	private static Set<String> computeColisSDSecheTotal(final CollectionsDispersion collection,
			final String dateTime) {
		Set<String> colisSDSecheTemp = Sets.union(collection.getEnDispersion(), collection.getEnDispersionDeloc());
		if (SpecifsColisRules.estAvantQuatorzeHeure(dateTime))
			return Sets.difference(colisSDSecheTemp, collection.getDispersesPoste());
		else
			return Sets.difference(colisSDSecheTemp,
					Sets.union(collection.getExclusAJ(), collection.getDispersesPoste()));
	}

	/**
	 * Calcule de la liste des ColisEnCours.<br>
	 * {@code enDispersion + enDispersionDeloc + nonRemisEnDistribution + perdus}
	 * 
	 * @param collection
	 * @return
	 * 
	 * @author bjbari
	 */
	private static Set<String> computeColisEnCours(final CollectionsDispersion collectionDispersion) {
		final Set<String> colisEnCours = new HashSet<>();
		colisEnCours.addAll(collectionDispersion.getEnDispersion());
		colisEnCours.addAll(collectionDispersion.getEnDispersionDeloc());
		colisEnCours.addAll(collectionDispersion.getNonRemisEnDistribution());
		colisEnCours.addAll(collectionDispersion.getPerdus());
		return colisEnCours;
	}
	/**
	 * Calcule la liste des colis distribues 
	 * @param collection 
	 * @return
	 */
	private static Set<String> computeColisDistribues(final CollectionsDistribution collection) {
		return Sets.difference(collection.getLivreDomicilePA(), collection.getLivreDomicilePADeloc());
	}
	
	/**
	 * Calcule la liste des colis TA Sèche deloc
	 * @param collection
	 * @return
	 */
	private static Set<String> computeColisTASecheDeloc(final CollectionsDistribution collection) {
		return Sets.difference(collection.getEnPrepaPADeloc(), collection.getSortieReseau());
	}

	/**
	 * Calcule la liste des colis pour l'indicateur nbColisInstanceNonAcquittesDeloc
	 * @param collection
	 * @return
	 */
	private static Set<String> computeColisInstanceNonAcquittesDeloc(final CollectionsDistribution collection) {
		return Sets.difference(collection.getEnInstancePADeloc(), collection.getSortieReseau());
	}

	/**
	 * Extrait la liste de colis des collections à partir du code indicateur
	 * 
	 * @param collectionDispersion
	 * @param codeIndicateur
	 * @return
	 *
	 * 
	 * @author LGY
	 */
	public static Set<String> getCollectionFromIndicateurCode(final CollectionsDispersion collectionDispersion,
			final String codeIndicateur, final String dateAppel) {
		switch (codeIndicateur) {

		case "nbColisDisperses":
			return collectionDispersion.getDisperses();

		case "nbColisEnDispersion":
			return collectionDispersion.getEnDispersion();

		case "nbColisRemisEnDistri":
			return collectionDispersion.getARemettreEnDistribution();

		case "nbColisDispersesPoste":
			return collectionDispersion.getDispersesPoste();

		case "nbColisPrisEnChargePoste":
			return collectionDispersion.getAcquittementsPoste();

		case "nbColisSDSeche":
			return computeColisSDSeche(collectionDispersion, dateAppel);

		case "nbColisSDSechePoste":
			return collectionDispersion.getEnDispersionPoste();

		case "nbColisAPreparer":
			return computeColisAPreparer(collectionDispersion);

		case "nbColisPrepares":
			return computeColisPrepares(collectionDispersion);

		case "nbColisExclusLivraisonMatin":
			return collectionDispersion.getExclusAJ();

		case "nbColisExclusLivraisonUnJour":
			return collectionDispersion.getExclusAJ1();

		case "nbColisExclusLivraisonXJours":
			return collectionDispersion.getExclusAJX();

		case "nbColisNonRemisEnDistribution":
			return collectionDispersion.getNonRemisEnDistribution();

		case "nbColisTASansSD":
			return computeColisTASansSD(collectionDispersion);

		case "nbColisIncidentTG2Jour":
			return collectionDispersion.getIncidentTG2Jour();

		case "nbColisDispersesDeloc":
			return collectionDispersion.getDispersesDeloc();

		case "nbColisSDSecheDeloc":
			return computeColisSDSecheDeloc(collectionDispersion, dateAppel);

		case "nbColisPreparesDeloc":
			return computeColisPreparesDeloc(collectionDispersion);

		case "nbColisTASansSDDeloc":
			return computeColisTASansSDDeloc(collectionDispersion);
		case "nbColisPerdus":
			return collectionDispersion.getPerdus();

		case "nbColisSDSecheTotal":
			return computeColisSDSecheTotal(collectionDispersion, dateAppel);

		case "nbColisEnAlerteActive":
			return collectionDispersion.getEnAlerteActive();

		case "nbColisEnAlerteActiveDeloc":
			return collectionDispersion.getEnAlerteActiveDeloc();

		case "nbColisEnCours":
			return computeColisEnCours(collectionDispersion);

		default:
			return null;
		}
	}

	/**
	 * Extrait la liste de colis des collections à partir du code indicateur (
	 * Jours précédents )
	 * 
	 * @param collectionDispersionPassee
	 * @param codeIndicateur
	 *            : le code de l'indicateur
	 * @return
	 *
	 * @author bjbari
	 */
	public static Set<String> getCollectionFromIndicateurCodePassee(
			final CollectionsDispersionPassee collectionDispersionPassee, final String codeIndicateur) {
		switch (codeIndicateur) {

		case "nbColisSDSechePassee":
			return computeColisSDSechePassee(collectionDispersionPassee);
		case "nbColisSDSechePostePassee":
			return collectionDispersionPassee.getEnDispersionPostePassee();
		case "nbColisNonRemisEnDistributionPassee":
			return computeColisNonRemisEnDistributionPassee(collectionDispersionPassee);
		case "nbColisPerdusPassee":
			return collectionDispersionPassee.getPerdusPassee();
		case "nbColisEnAlerteActivePassee":
			return collectionDispersionPassee.getEnAlerteActivePassee();
		case "nbColisEnAlerteActiveDelocPassee":
			return collectionDispersionPassee.getEnAlerteActiveDelocPassee();
		case "nbColisSDSecheDelocPassee":
			return collectionDispersionPassee.getEnDispersionDelocPassee();
		default:
			return null;
		}
	}

	/**
	 * Renvoie la liste limitée des colis
	 * 
	 * @param colisSyntheseList
	 * @param limit
	 * @return une liste de colis de type <code>SyntheseDispersionColis</code>
	 * 
	 * @author bjbari
	 */
	public static List<SyntheseColis> getLimitedList(final List<SyntheseColis> colisSyntheseList,
			final Integer limit) {
		// Si une limite de colis est demandée on ne prend que les n premiers de
		// la liste
		if (null != limit && limit < colisSyntheseList.size()) {
			// on trie la liste par NoLt
			Collections.sort(colisSyntheseList, new Comparator<SyntheseColis>() {
				@Override
				public int compare(SyntheseColis tc1, SyntheseColis tc2) {
					return tc1.getNoLt().compareTo(tc2.getNoLt());
				}
			});
			// retourne la liste triée tronquée
			return colisSyntheseList.subList(0, limit);
		}
		return colisSyntheseList;
	}


	/**
	 * Extrait la liste de colis des collections à partir du code indicateur (
	 * de distribution)
	 * 
	 * @param collectionsDistribution
	 * @param codeIndicateur
	 *            : le code de l'indicateur
	 * @return
	 */
	public static Set<String> getCollectionDistriFromIndicateurCode(
			CollectionsDistribution collectionsDistribution, String codeIndicateur) {
		
		switch (codeIndicateur) {

		case "nbColisDistribues":
			return computeColisDistribues(collectionsDistribution);
		case "nbColisAvecEchec":
			return computeColisAvecEchec(collectionsDistribution);
		case "nbColisInstance":
			return computeColisInstance(collectionsDistribution);
		case "nbColisDistribuesDeloc":
			return collectionsDistribution.getLivreDomicilePADeloc();
		case "nbColisAvecEchecDeloc":
			return computeColisAvecEchecDeloc(collectionsDistribution);
		case "nbColisInstanceDeloc":
			return collectionsDistribution.getEnInstancePADeloc();
		case "nbColisTASeche":
			return computeTASeche(collectionsDistribution);
		case "nbColisInstanceNonAcquittes":
			return computeColisInstanceNonAcquittes(collectionsDistribution);
		case "nbColisEnEchec":
			return computeColisEnEchec(collectionsDistribution);
		case "nbColisTASecheDeloc":
			return computeColisTASecheDeloc(collectionsDistribution);
		case "nbColisInstanceNonAcquittesDeloc":
			return computeColisInstanceNonAcquittesDeloc(collectionsDistribution);
		case "nbColisEnEchecDeloc":
			return computeColisEnEchecDeloc(collectionsDistribution);
		default:
			return null;
		}
	}
}
