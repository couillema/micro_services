package com.chronopost.vision.microservices.getsyntheseagence.v1.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.chronopost.vision.model.getsyntheseagence.v1.ECodeIndicateur;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;


public class CollectionIndicateursDispersionPassee {

	public static final Set<String> INDICATEURS_JOURS_PRECEDENTS = Sets.newHashSet(
			 ECodeIndicateur.SD_SECHE_PASSEE.getCode()
			,ECodeIndicateur.SD_SECHE_POSTE_PASSEE.getCode()
			,ECodeIndicateur.NON_REMIS_EN_DISTRIBUTION_PASSEE.getCode()
			,ECodeIndicateur.PERDUS_PASSEE.getCode()
			,ECodeIndicateur.EN_ALERTE_ACTIVE_PASSEE.getCode()
			,ECodeIndicateur.EN_ALERTE_ACTIVE_DELOC_PASSEE.getCode()
			,ECodeIndicateur.SD_SECHE_DELOC_PASSEE.getCode()
			);
	
	private final Set<String> colisSDSechePassee = new HashSet<>();
	private final Set<String> colisSDSechePostePassee = new HashSet<>();
	private final Set<String> colisNonRemisEnDistributionPassee = new HashSet<>();
	private final Set<String> colisPerdusPassee = new HashSet<>();
	private final Set<String> colisEnAlerteActivePassee = new HashSet<>();
	private final Set<String> colisEnAlerteActiveDelocPassee = new HashSet<>();
	private final Set<String> colisSDSecheDelocPassee = new HashSet<>();
	/**
	 * @return an immutable copy of colisSDSechePassee
	 */
	public ImmutableSet<String> getColisSDSechePassee() {
		return ImmutableSet.copyOf(colisSDSechePassee);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisSDSechePassee(final Collection<String> c) {
		clearColisSDSechePassee();
		if (c != null) {
			addAllToColisSDSechePassee(c);
		}
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisSDSechePassee(Collection<String> c) {
		colisSDSechePassee.addAll(c);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisSDSechePassee() {
		colisSDSechePassee.clear();
	}

	/**
	 * @return an immutable copy of colisSDSechePostePassee
	 */
	public ImmutableSet<String> getColisSDSechePostePassee() {
		return ImmutableSet.copyOf(colisSDSechePostePassee);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisSDSechePostePassee(Collection<String> c) {
		clearColisSDSechePostePassee();
		if (c != null) {
			addAllToColisSDSechePostePassee(c);
		}
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisSDSechePostePassee(Collection<String> c) {
		colisSDSechePostePassee.addAll(c);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisSDSechePostePassee() {
		colisSDSechePostePassee.clear();
	}

	/**
	 * @return an immutable copy of colisNonRemisEnDistributionPassee
	 */
	public ImmutableSet<String> getColisNonRemisEnDistributionPassee() {
		return ImmutableSet.copyOf(colisNonRemisEnDistributionPassee);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisNonRemisEnDistributionPassee(Collection<String> c) {
		clearColisNonRemisEnDistributionPassee();
		if (c != null) {
			addAllToColisNonRemisEnDistributionPassee(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisNonRemisEnDistributionPassee(String e) {
		colisNonRemisEnDistributionPassee.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisNonRemisEnDistributionPassee(Collection<String> c) {
		colisNonRemisEnDistributionPassee.addAll(c);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisNonRemisEnDistributionPassee() {
		colisNonRemisEnDistributionPassee.clear();
	}

	/**
	 * @return an immutable copy of colisPerdusPassee
	 */
	public ImmutableSet<String> getColisPerdusPassee() {
		return ImmutableSet.copyOf(colisPerdusPassee);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisPerdusPassee(Collection<String> c) {
		clearColisPerdusPassee();
		if (c != null) {
			addAllToColisPerdusPassee(c);
		}
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisPerdusPassee(Collection<String> c) {
		colisPerdusPassee.addAll(c);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisPerdusPassee() {
		colisPerdusPassee.clear();
	}
	
	/**
	 * @return an immutable copy of colisEnAlerteActivePassee
	 */
	public ImmutableSet<String> getColisEnAlerteActivePassee() {
		return ImmutableSet.copyOf(colisEnAlerteActivePassee);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisEnAlerteActivePassee(Collection<String> c) {
		clearColisEnAlerteActivePassee();
		if (c != null) {
			addAllToColisEnAlerteActivePassee(c);
		}
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisEnAlerteActivePassee(Collection<String> c) {
		colisEnAlerteActivePassee.addAll(c);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisEnAlerteActivePassee() {
		colisEnAlerteActivePassee.clear();
	}

	/**
	 * @return an immutable copy of colisEnAlerteActiveDelocPassee
	 */
	public ImmutableSet<String> getColisEnAlerteActiveDelocPassee() {
		return ImmutableSet.copyOf(colisEnAlerteActiveDelocPassee);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisEnAlerteActiveDelocPassee(Collection<String> c) {
		clearColisEnAlerteActiveDelocPassee();
		if (c != null) {
			addAllToColisEnAlerteActiveDelocPassee(c);
		}
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisEnAlerteActiveDelocPassee(Collection<String> c) {
		colisEnAlerteActiveDelocPassee.addAll(c);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisEnAlerteActiveDelocPassee() {
		colisEnAlerteActiveDelocPassee.clear();
	}
	
	/**
	 * @return an immutable copy of colisSDSecheDelocPassee
	 */
	public ImmutableSet<String> getColisSDSecheDelocPassee() {
		return ImmutableSet.copyOf(colisSDSecheDelocPassee);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisSDSecheDelocPassee(Collection<String> c) {
		clearColisSDSecheDelocPassee();
		if (c != null) {
			addAllToColisSDSecheDelocPassee(c);
		}
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisSDSecheDelocPassee(Collection<String> c) {
		colisSDSecheDelocPassee.addAll(c);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisSDSecheDelocPassee() {
		colisSDSecheDelocPassee.clear();
	}
}
