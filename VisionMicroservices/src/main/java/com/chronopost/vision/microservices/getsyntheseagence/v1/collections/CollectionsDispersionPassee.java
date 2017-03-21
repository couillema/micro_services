package com.chronopost.vision.microservices.getsyntheseagence.v1.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Représente un ensemble de collections de colis à partir desquels pourront se
 * faire le calcul des indicateurs de l’objet
 * <code>SyntheseDispersionQuantitePassee</code>
 * 
 * @author bjbari
 *
 */
public class CollectionsDispersionPassee {

	private final Set<String> colisEnCours = new HashSet<>();
	private final Set<String> enDispersionPassee = new HashSet<>();
	private final Set<String> enDispersionPostePassee = new HashSet<>();
	private final Set<String> exclusPassee = new HashSet<>();
	private final Set<String> exclusOubliesPassee = new HashSet<>();
	private final Set<String> aRemettreEnDistributionOubliesPassee = new HashSet<>();
	private final Set<String> perdusPassee = new HashSet<>();
	private final Set<String> enAlerteActivePassee = new HashSet<>();
	private final Set<String> enAlerteActiveDelocPassee = new HashSet<>();
	private final Set<String> enDispersionDelocPassee = new HashSet<>();
	
	/**
	 * @return an immutable copy of colisEnCours
	 */
	public ImmutableSet<String> getColisEnCours() {
		return ImmutableSet.copyOf(colisEnCours);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisEnCours(Collection<String> c) {
		clearColisEnCours();
		if (c != null) {
			addAllToColisEnCours(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisEnCours(String e) {
		colisEnCours.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisEnCours(Collection<String> c) {
		colisEnCours.addAll(c);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisEnCours() {
		colisEnCours.clear();
	}

	/**
	 * @return an immutable copy of enDispersionPassee
	 */
	public ImmutableSet<String> getEnDispersionPassee() {
		return ImmutableSet.copyOf(enDispersionPassee);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setEnDispersionPassee(Collection<String> c) {
		clearEnDispersionPassee();
		if (c != null) {
			addAllToEnDispersionPassee(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToEnDispersionPassee(String e) {
		enDispersionPassee.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToEnDispersionPassee(Collection<String> c) {
		enDispersionPassee.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromEnDispersionPassee(String e) {
		enDispersionPassee.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearEnDispersionPassee() {
		enDispersionPassee.clear();
	}

	/**
	 * @return an immutable copy of enDispersionPostePassee
	 */
	public ImmutableSet<String> getEnDispersionPostePassee() {
		return ImmutableSet.copyOf(enDispersionPostePassee);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setEnDispersionPostePassee(Collection<String> c) {
		clearEnDispersionPostePassee();
		if (c != null) {
			addAllToEnDispersionPostePassee(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToEnDispersionPostePassee(String e) {
		enDispersionPostePassee.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToEnDispersionPostePassee(Collection<String> c) {
		enDispersionPostePassee.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromEnDispersionPostePassee(String e) {
		enDispersionPostePassee.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearEnDispersionPostePassee() {
		enDispersionPostePassee.clear();
	}

	/**
	 * @return an immutable copy of exclusPassee
	 */
	public ImmutableSet<String> getExclusPassee() {
		return ImmutableSet.copyOf(exclusPassee);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setExclusPassee(Collection<String> c) {
		clearExclusPassee();
		if (c != null) {
			addAllToExclusPassee(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToExclusPassee(String e) {
		exclusPassee.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToExclusPassee(Collection<String> c) {
		exclusPassee.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromExclusPassee(String e) {
		exclusPassee.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearExclusPassee() {
		exclusPassee.clear();
	}

	/**
	 * @return an immutable copy of exclusOubliesPassee
	 */
	public ImmutableSet<String> getExclusOubliesPassee() {
		return ImmutableSet.copyOf(exclusOubliesPassee);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setExclusOubliesPassee(Collection<String> c) {
		clearExclusOubliesPassee();
		if (c != null) {
			addAllToExclusOubliesPassee(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToExclusOubliesPassee(String e) {
		exclusOubliesPassee.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToExclusOubliesPassee(Collection<String> c) {
		exclusOubliesPassee.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromExclusOubliesPassee(String e) {
		exclusOubliesPassee.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearExclusOubliesPassee() {
		exclusOubliesPassee.clear();
	}

	/**
	 * @return an immutable copy of aRemettreEnDistributionOubliesPassee
	 */
	public ImmutableSet<String> getARemettreEnDistributionOubliesPassee() {
		return ImmutableSet.copyOf(aRemettreEnDistributionOubliesPassee);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setARemettreEnDistributionOubliesPassee(Collection<String> c) {
		clearARemettreEnDistributionOubliesPassee();
		if (c != null) {
			addAllToARemettreEnDistributionOubliesPassee(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToARemettreEnDistributionOubliesPassee(String e) {
		aRemettreEnDistributionOubliesPassee.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToARemettreEnDistributionOubliesPassee(Collection<String> c) {
		aRemettreEnDistributionOubliesPassee.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromARemettreEnDistributionOubliesPassee(String e) {
		aRemettreEnDistributionOubliesPassee.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearARemettreEnDistributionOubliesPassee() {
		aRemettreEnDistributionOubliesPassee.clear();
	}

	/**
	 * @return an immutable copy of perdusPassee
	 */
	public ImmutableSet<String> getPerdusPassee() {
		return ImmutableSet.copyOf(perdusPassee);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setPerdusPassee(Collection<String> c) {
		clearPerdusPassee();
		if (c != null) {
			addAllToPerdusPassee(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToPerdusPassee(String e) {
		perdusPassee.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToPerdusPassee(Collection<String> c) {
		perdusPassee.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromPerdusPassee(String e) {
		perdusPassee.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearPerdusPassee() {
		perdusPassee.clear();
	}
	
	/**
	 * @return an immutable copy of enAlerteActivePassee
	 */
	public ImmutableSet<String> getEnAlerteActivePassee() {
		return ImmutableSet.copyOf(enAlerteActivePassee);
	}

	/**
	 * @param noLt
	 *           noLt to add in the collection
	 */
	public void addToEnAlerteActivePassee(String noLt) {
		enAlerteActivePassee.add(noLt);
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addAllToEnAlerteActivePassee(Collection<String> c) {
		colisEnCours.addAll(c);
	}
	
	/**
	 * @return an immutable copy of enAlerteActiveDelocPassee
	 */
	public ImmutableSet<String> getEnAlerteActiveDelocPassee() {
		return ImmutableSet.copyOf(enAlerteActiveDelocPassee);
	}
	
	/**
	 * @param noLt
	 *            noLt to add in the collection
	 */
	public void addToEnAlerteActiveDelocPassee(String noLt) {
		enAlerteActiveDelocPassee.add(noLt);
	}
	
	/**
	 * @return an immutable copy of enDispersionDelocPassee
	 */
	public ImmutableSet<String> getEnDispersionDelocPassee() {
		return ImmutableSet.copyOf(enDispersionDelocPassee);
	}
	
	/**
	 * @param noLt
	 *            noLt to add in the collection
	 */
	public void addToEnDispersionDelocPassee(String noLt) {
		enDispersionDelocPassee.add(noLt);
	}
}
