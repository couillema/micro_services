package com.chronopost.vision.microservices.getsyntheseagence.v1.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * @author ftemplier
 *
 */
public class CollectionsDispersion {
	private final Set<String> colisSaisis = new HashSet<>();
	private final Set<String> colisASaisir = new HashSet<>();
	private final Set<String> disperses = new HashSet<>();
	private final Set<String> dispersesDeloc = new HashSet<>();
	private final Set<String> enDispersion = new HashSet<>();
	private final Set<String> enDispersionDeloc = new HashSet<>();
	private final Set<String> aRemettreEnDistribution = new HashSet<>();
	private final Set<String> nonRemisEnDistribution = new HashSet<>();
	private final Set<String> dispersesPoste = new HashSet<>();
	private final Set<String> acquittementsPoste = new HashSet<>();
	private final Set<String> prepares = new HashSet<>();
	private final Set<String> preparesDeloc = new HashSet<>();
	private final Set<String> preparesPoste = new HashSet<>();
	private final Set<String> distribues = new HashSet<>();
	private final Set<String> distribuesPoste = new HashSet<>();
	private final Set<String> exclusDuJour = new HashSet<>();
	private final Set<String> exclusAJ = new HashSet<>();
	private final Set<String> exclusAJ1 = new HashSet<>();
	private final Set<String> exclusAJX = new HashSet<>();
	private final Set<String> incidentTG2Jour = new HashSet<>();
	private final Set<String> perdus = new HashSet<>();
	private final Set<String> enAlerteActive = new HashSet<>();
	private final Set<String> enAlerteActiveDeloc = new HashSet<>();
	private final Set<String> enDispersionPoste = new HashSet<>();

	/**
	 * @return an immutable copy of colisSaisis
	 */
	public ImmutableSet<String> getColisSaisis() {
		return ImmutableSet.copyOf(colisSaisis);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisSaisis(Collection<String> c) {
		clearColisSaisis();
		if (c != null) {
			addAllToColisSaisis(c);
		}
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisSaisis(Collection<String> c) {
		colisSaisis.addAll(c);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisSaisis() {
		colisSaisis.clear();
	}

	/**
	 * @return an immutable copy of colisASaisir
	 */
	public ImmutableSet<String> getColisASaisir() {
		return ImmutableSet.copyOf(colisASaisir);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisASaisir(Collection<String> c) {
		clearColisASaisir();
		if (c != null) {
			addAllToColisASaisir(c);
		}
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisASaisir(Collection<String> c) {
		colisASaisir.addAll(c);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisASaisir() {
		colisASaisir.clear();
	}

	/**
	 * @return an immutable copy of disperses
	 */
	public ImmutableSet<String> getDisperses() {
		return ImmutableSet.copyOf(disperses);
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToDisperses(String e) {
		disperses.add(e);
	}
	
	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToDisperses(Collection<String> c) {
		disperses.addAll(c);
	}


	/**
	 * @return an immutable copy of enDispersion
	 */
	public ImmutableSet<String> getEnDispersion() {
		return ImmutableSet.copyOf(enDispersion);
	}

	/**
	 * @param noLt : colis number
	 * an element to add in the collection
	 */
	public void addToEnDispersion(String noLt) {
		enDispersion.add(noLt);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToEnDispersion(Collection<String> c) {
		enDispersion.addAll(c);
	}
	
	/**
	 * @return an immutable copy of enDispersion
	 */
	public ImmutableSet<String> getEnDispersionDeloc() {
		return ImmutableSet.copyOf(enDispersionDeloc);
	}

	/**
	 * @param noLt : colis number
	 * an element to add in the collection
	 */
	public void addToEnDispersionDeloc(String noLt) {
		enDispersionDeloc.add(noLt);
	}

	/**
	 * @return an immutable copy of aRemettreEnDistribution
	 */
	public ImmutableSet<String> getARemettreEnDistribution() {
		return ImmutableSet.copyOf(aRemettreEnDistribution);
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToARemettreEnDistribution(String e) {
		aRemettreEnDistribution.add(e);
	}

	/**
	 * @return an immutable copy of nonRemisEnDistribution
	 */
	public ImmutableSet<String> getNonRemisEnDistribution() {
		return ImmutableSet.copyOf(nonRemisEnDistribution);
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToNonRemisEnDistribution(String e) {
		nonRemisEnDistribution.add(e);
	}

	/**
	 * @return an immutable copy of dispersesPoste
	 */
	public ImmutableSet<String> getDispersesPoste() {
		return ImmutableSet.copyOf(dispersesPoste);
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToDispersesPoste(String e) {
		dispersesPoste.add(e);
	}

	/**
	 * @return an immutable copy of acquittementsPoste
	 */
	public ImmutableSet<String> getAcquittementsPoste() {
		return ImmutableSet.copyOf(acquittementsPoste);
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToAcquittementsPoste(String e) {
		acquittementsPoste.add(e);
	}

	/**
	 * @return an immutable copy of prepares
	 */
	public ImmutableSet<String> getPrepares() {
		return ImmutableSet.copyOf(prepares);
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToPrepares(String e) {
		prepares.add(e);
	}
	

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToPrepares(Collection<String> c) {
		prepares.addAll(c);
	}

	/**
	 * @return an immutable copy of preparesPoste
	 */
	public ImmutableSet<String> getPreparesPoste() {
		return ImmutableSet.copyOf(preparesPoste);
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToPreparesPoste(String e) {
		preparesPoste.add(e);
	}

	/**
	 * @return an immutable copy of distribues
	 */
	public ImmutableSet<String> getDistribues() {
		return ImmutableSet.copyOf(distribues);
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToDistribues(String e) {
		distribues.add(e);
	}

	/**
	 * @return an immutable copy of distribuesPoste
	 */
	public ImmutableSet<String> getDistribuesPoste() {
		return ImmutableSet.copyOf(distribuesPoste);
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToDistribuesPoste(String e) {
		distribuesPoste.add(e);
	}

	/**
	 * @return an immutable copy of exclusDuJour
	 */
	public ImmutableSet<String> getExclusDuJour() {
		return ImmutableSet.copyOf(exclusDuJour);
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToExclusDuJour(String e) {
		exclusDuJour.add(e);
	}

	/**
	 * @return an immutable copy of exclusAJ
	 */
	public ImmutableSet<String> getExclusAJ() {
		return ImmutableSet.copyOf(exclusAJ);
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToExclusAJ(String e) {
		exclusAJ.add(e);
	}

	/**
	 * @return an immutable copy of exclusAJ1
	 */
	public ImmutableSet<String> getExclusAJ1() {
		return ImmutableSet.copyOf(exclusAJ1);
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToExclusAJ1(String e) {
		exclusAJ1.add(e);
	}

	/**
	 * @return an immutable copy of exclusAJX
	 */
	public ImmutableSet<String> getExclusAJX() {
		return ImmutableSet.copyOf(exclusAJX);
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToExclusAJX(String e) {
		exclusAJX.add(e);
	}

	/**
	 * @return an immutable copy of exclusAJX
	 */
	public ImmutableSet<String> getIncidentTG2Jour() {
		return ImmutableSet.copyOf(incidentTG2Jour);
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToIncidentTG2Jour(String e) {
		incidentTG2Jour.add(e);
	}

	public ImmutableSet<String> getDispersesDeloc() {
		return ImmutableSet.copyOf(dispersesDeloc);
	}
	
	public void addToDispersesDeloc(String e) {
		dispersesDeloc.add(e);
	}

	public ImmutableSet<String> getPreparesDeloc() {
		return ImmutableSet.copyOf(preparesDeloc);
	}
	
	public void addToPreparesDeloc(String noLt) {
		preparesDeloc.add(noLt);
	}
	

	/**
	 * @return an immutable copy of perdus
	 */
	public ImmutableSet<String> getPerdus() {
		return ImmutableSet.copyOf(perdus);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addToPerdus(String c) {
		perdus.add(c);
	}

	/**
	 * Clear the collection
	 */
	public void clearPerdus() {
		perdus.clear();
	}
	
	/**
	 * @return an immutable copy of enAlerteActive
	 */
	public ImmutableSet<String> getEnAlerteActive() {
		return ImmutableSet.copyOf(enAlerteActive);
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToEnAlerteActive(String e) {
		enAlerteActive.add(e);
	}

	/**
	 * @return an immutable copy of enAlerteActiveDeloc
	 */
	public ImmutableSet<String> getEnAlerteActiveDeloc() {
		return ImmutableSet.copyOf(enAlerteActiveDeloc);
	}


	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToEnAlerteActiveDeloc(String e) {
		enAlerteActiveDeloc.add(e);
	}

	/**
	 * @return an immutable copy of enAlerteActiveDeloc
	 */
	public ImmutableSet<String> getEnDispersionPoste() {
		return ImmutableSet.copyOf(enDispersionPoste);
	}


	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToEnDispersionPoste(String e) {
		enDispersionPoste.add(e);
	}

}
