package com.chronopost.vision.microservices.getsyntheseagence.v1.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.chronopost.vision.model.getsyntheseagence.v1.ECodeIndicateur;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class CollectionIndicateursDispersion {
	
	public static final Set<String> INDICATEURS_JOUR = Sets.newHashSet(
			 ECodeIndicateur.DISPERSES.getCode()
			,ECodeIndicateur.EN_DISPERSION.getCode()
			,ECodeIndicateur.REMIS_EN_DISTRI.getCode()
			,ECodeIndicateur.DISPERSES_POSTE.getCode()
			,ECodeIndicateur.PRIS_EN_CHARGE_POSTE.getCode()
			,ECodeIndicateur.SD_SECHE.getCode()
			,ECodeIndicateur.SD_SECHE_POSTE.getCode()
			,ECodeIndicateur.A_PREPARER.getCode()
			,ECodeIndicateur.PREPARES.getCode()
			,ECodeIndicateur.EXCLUS_LIVRAISON_MATIN.getCode()
			,ECodeIndicateur.EXCLUS_LIVRAISON_UN_JOUR.getCode()
			,ECodeIndicateur.EXCLUS_LIVRAISON_X_JOURS.getCode()
			,ECodeIndicateur.NON_REMIS_EN_DISTRIBUTION.getCode()
			,ECodeIndicateur.TA_SANS_SD.getCode()
			,ECodeIndicateur.INCIDENT_TG2_JOUR.getCode()
			,ECodeIndicateur.DISPERSES_DELOC.getCode()
			,ECodeIndicateur.PREPARES_DELOC.getCode()
			,ECodeIndicateur.TA_SANS_SD_DELOC.getCode()
			,ECodeIndicateur.SD_SECHE_DELOC.getCode()
			,ECodeIndicateur.PERDUS.getCode()
			,ECodeIndicateur.SD_SECHE_TOTAL.getCode()
			,ECodeIndicateur.EN_ALERTE_ACTIVE.getCode()
			,ECodeIndicateur.EN_ALERTE_ACTIVE_DELOC.getCode()
			,ECodeIndicateur.EN_COURS.getCode()
			);
	
	
	private final Set<String> colisDisperses = new HashSet<>();
	private final Set<String> colisDispersesDeloc = new HashSet<>();
	private final Set<String> colisEnDispersion = new HashSet<>();
	private final Set<String> colisRemisEnDistri = new HashSet<>();
	private final Set<String> colisDispersesPoste = new HashSet<>();
	private final Set<String> colisPrisEnChargePoste = new HashSet<>();
	private final Set<String> colisSDSeche = new HashSet<>();
	private final Set<String> colisSDSecheDeloc = new HashSet<>();
	private final Set<String> colisSDSechePoste = new HashSet<>();
	private final Set<String> colisAPreparer = new HashSet<>();
	private final Set<String> colisPrepares = new HashSet<>();
	private final Set<String> colisPreparesDeloc = new HashSet<>();
	private final Set<String> colisExclusLivraisonMatin = new HashSet<>();
	private final Set<String> colisExclusLivraisonUnJour = new HashSet<>();
	private final Set<String> colisExclusLivraisonXJours = new HashSet<>();
	private final Set<String> colisNonRemisEnDistribution = new HashSet<>();
	private final Set<String> colisIncidentTG2Jour = new HashSet<>();
	private final Set<String> colisTASansSD = new HashSet<>();
	private final Set<String> colisTASansSDDeloc = new HashSet<>();
	private final Set<String> colisPerdus = new HashSet<>();
	private final Set<String> colisSDSecheTotal = new HashSet<>();
	private final Set<String> colisEnAlerteActive = new HashSet<>();
	private final Set<String> colisEnAlerteActiveDeloc = new HashSet<>();
	private final Set<String> colisEnCours = new HashSet<>();

	/**
	 * @return an immutable copy of colisDisperses
	 */
	public ImmutableSet<String> getColisDisperses() {
		return ImmutableSet.copyOf(colisDisperses);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisDisperses(Collection<String> c) {
		clearColisDisperses();
		if (c != null) {
			addAllToColisDisperses(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisDisperses(String e) {
		colisDisperses.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisDisperses(Collection<String> c) {
		colisDisperses.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromColisDisperses(String e) {
		colisDisperses.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisDisperses() {
		colisDisperses.clear();
	}
	
	/**
	 * @return an immutable copy of colisDisperses
	 */
	public ImmutableSet<String> getColisDispersesDeloc() {
		return ImmutableSet.copyOf(colisDispersesDeloc);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisDispersesDeloc(Collection<String> c) {
		clearColisDispersesDeloc();
		if (c != null) {
			addAllToColisDispersesDeloc(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisDispersesDeloc(String e) {
		colisDispersesDeloc.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisDispersesDeloc(Collection<String> c) {
		colisDispersesDeloc.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromColisDispersesDeloc(String e) {
		colisDispersesDeloc.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisDispersesDeloc() {
		colisDispersesDeloc.clear();
	}

	/**
	 * @return an immutable copy of colisEnDispersion
	 */
	public ImmutableSet<String> getColisEnDispersion() {
		return ImmutableSet.copyOf(colisEnDispersion);
	}

	/**
	 * @param noLts
	 *            a collection to set in colisEnDispersion
	 */
	public void setColisEnDispersion(Collection<String> noLts) {
		colisEnDispersion.clear();
		if (noLts != null) {
			addAllToColisEnDispersion(noLts);
		}
	}

	/**
	 * @param noLts
	 *            a collection of elements to to set the collection
	 */
	public void addAllToColisEnDispersion(Collection<String> noLts) {
		colisEnDispersion.addAll(noLts);
	}

	/**
	 * @return an immutable copy of colisRemisEnDistri
	 */
	public ImmutableSet<String> getColisRemisEnDistri() {
		return ImmutableSet.copyOf(colisRemisEnDistri);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisRemisEnDistri(Collection<String> c) {
		clearColisRemisEnDistri();
		if (c != null) {
			addAllToColisRemisEnDistri(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisRemisEnDistri(String e) {
		colisRemisEnDistri.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisRemisEnDistri(Collection<String> c) {
		colisRemisEnDistri.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromColisRemisEnDistri(String e) {
		colisRemisEnDistri.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisRemisEnDistri() {
		colisRemisEnDistri.clear();
	}

	/**
	 * @return an immutable copy of colisDispersesPoste
	 */
	public ImmutableSet<String> getColisDispersesPoste() {
		return ImmutableSet.copyOf(colisDispersesPoste);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisDispersesPoste(Collection<String> c) {
		clearColisDispersesPoste();
		if (c != null) {
			addAllToColisDispersesPoste(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisDispersesPoste(String e) {
		colisDispersesPoste.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisDispersesPoste(Collection<String> c) {
		colisDispersesPoste.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromColisDispersesPoste(String e) {
		colisDispersesPoste.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisDispersesPoste() {
		colisDispersesPoste.clear();
	}

	/**
	 * @return an immutable copy of colisPrisEnChargePoste
	 */
	public ImmutableSet<String> getColisPrisEnChargePoste() {
		return ImmutableSet.copyOf(colisPrisEnChargePoste);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisPrisEnChargePoste(Collection<String> c) {
		clearColisPrisEnChargePoste();
		if (c != null) {
			addAllToColisPrisEnChargePoste(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisPrisEnChargePoste(String e) {
		colisPrisEnChargePoste.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisPrisEnChargePoste(Collection<String> c) {
		colisPrisEnChargePoste.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromColisPrisEnChargePoste(String e) {
		colisPrisEnChargePoste.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisPrisEnChargePoste() {
		colisPrisEnChargePoste.clear();
	}

	/**
	 * @return an immutable copy of colisSDSeche
	 */
	public ImmutableSet<String> getColisSDSeche() {
		return ImmutableSet.copyOf(colisSDSeche);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisSDSeche(Collection<String> c) {
		clearColisSDSeche();
		if (c != null) {
			addAllToColisSDSeche(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisSDSeche(String e) {
		colisSDSeche.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisSDSeche(Collection<String> c) {
		colisSDSeche.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromColisSDSeche(String e) {
		colisSDSeche.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisSDSeche() {
		colisSDSeche.clear();
	}

	/**
	 * @return an immutable copy of colisSDSeche
	 */
	public ImmutableSet<String> getColisSDSecheDeloc() {
		return ImmutableSet.copyOf(colisSDSecheDeloc);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisSDSecheDeloc(Collection<String> c) {
		clearColisSDSecheDeloc();
		if (c != null) {
			addAllToColisSDSecheDeloc(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisSDSecheDeloc(String e) {
		colisSDSecheDeloc.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisSDSecheDeloc(Collection<String> c) {
		colisSDSecheDeloc.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromColisSDSecheDeloc(String e) {
		colisSDSecheDeloc.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisSDSecheDeloc() {
		colisSDSecheDeloc.clear();
	}

	/**
	 * @return an immutable copy of colisSDSechePoste
	 */
	public ImmutableSet<String> getColisSDSechePoste() {
		return ImmutableSet.copyOf(colisSDSechePoste);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisSDSechePoste(Collection<String> c) {
		clearColisSDSechePoste();
		if (c != null) {
			addAllToColisSDSechePoste(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisSDSechePoste(String e) {
		colisSDSechePoste.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisSDSechePoste(Collection<String> c) {
		colisSDSechePoste.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromColisSDSechePoste(String e) {
		colisSDSechePoste.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisSDSechePoste() {
		colisSDSechePoste.clear();
	}

	/**
	 * @return an immutable copy of colisAPreparer
	 */
	public ImmutableSet<String> getColisAPreparer() {
		return ImmutableSet.copyOf(colisAPreparer);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisAPreparer(Collection<String> c) {
		clearColisAPreparer();
		if (c != null) {
			addAllToColisAPreparer(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisAPreparer(String e) {
		colisAPreparer.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisAPreparer(Collection<String> c) {
		colisAPreparer.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromColisAPreparer(String e) {
		colisAPreparer.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisAPreparer() {
		colisAPreparer.clear();
	}

	/**
	 * @return an immutable copy of colisPrepares
	 */
	public ImmutableSet<String> getColisPrepares() {
		return ImmutableSet.copyOf(colisPrepares);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisPrepares(Collection<String> c) {
		clearColisPrepares();
		if (c != null) {
			addAllToColisPrepares(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisPrepares(String e) {
		colisPrepares.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisPrepares(Collection<String> c) {
		colisPrepares.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromColisPrepares(String e) {
		colisPrepares.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisPrepares() {
		colisPrepares.clear();
	}

	/**
	 * @return an immutable copy of colisPrepares
	 */
	public ImmutableSet<String> getColisPreparesDeloc() {
		return ImmutableSet.copyOf(colisPreparesDeloc);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisPreparesDeloc(Collection<String> c) {
		clearColisPreparesDeloc();
		if (c != null) {
			addAllToColisPreparesDeloc(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisPreparesDeloc(String e) {
		colisPreparesDeloc.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisPreparesDeloc(Collection<String> c) {
		colisPreparesDeloc.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromColisPreparesDeloc(String e) {
		colisPreparesDeloc.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisPreparesDeloc() {
		colisPreparesDeloc.clear();
	}

	/**
	 * @return an immutable copy of colisExclusLivraisonMatin
	 */
	public ImmutableSet<String> getColisExclusLivraisonMatin() {
		return ImmutableSet.copyOf(colisExclusLivraisonMatin);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisExclusLivraisonMatin(Collection<String> c) {
		clearColisExclusLivraisonMatin();
		if (c != null) {
			addAllToColisExclusLivraisonMatin(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisExclusLivraisonMatin(String e) {
		colisExclusLivraisonMatin.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisExclusLivraisonMatin(Collection<String> c) {
		colisExclusLivraisonMatin.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromColisExclusLivraisonMatin(String e) {
		colisExclusLivraisonMatin.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisExclusLivraisonMatin() {
		colisExclusLivraisonMatin.clear();
	}

	/**
	 * @return an immutable copy of colisExclusLivraisonUnJour
	 */
	public ImmutableSet<String> getColisExclusLivraisonUnJour() {
		return ImmutableSet.copyOf(colisExclusLivraisonUnJour);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisExclusLivraisonUnJour(Collection<String> c) {
		clearColisExclusLivraisonUnJour();
		if (c != null) {
			addAllToColisExclusLivraisonUnJour(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisExclusLivraisonUnJour(String e) {
		colisExclusLivraisonUnJour.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisExclusLivraisonUnJour(Collection<String> c) {
		colisExclusLivraisonUnJour.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromColisExclusLivraisonUnJour(String e) {
		colisExclusLivraisonUnJour.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisExclusLivraisonUnJour() {
		colisExclusLivraisonUnJour.clear();
	}

	/**
	 * @return an immutable copy of colisExclusLivraisonXJours
	 */
	public ImmutableSet<String> getColisExclusLivraisonXJours() {
		return ImmutableSet.copyOf(colisExclusLivraisonXJours);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisExclusLivraisonXJours(Collection<String> c) {
		clearColisExclusLivraisonXJours();
		if (c != null) {
			addAllToColisExclusLivraisonXJours(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisExclusLivraisonXJours(String e) {
		colisExclusLivraisonXJours.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisExclusLivraisonXJours(Collection<String> c) {
		colisExclusLivraisonXJours.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromColisExclusLivraisonXJours(String e) {
		colisExclusLivraisonXJours.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisExclusLivraisonXJours() {
		colisExclusLivraisonXJours.clear();
	}

	/**
	 * @return an immutable copy of colisNonRemisEnDistribution
	 */
	public ImmutableSet<String> getColisNonRemisEnDistribution() {
		return ImmutableSet.copyOf(colisNonRemisEnDistribution);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisNonRemisEnDistribution(Collection<String> c) {
		clearColisNonRemisEnDistribution();
		if (c != null) {
			addAllToColisNonRemisEnDistribution(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisNonRemisEnDistribution(String e) {
		colisNonRemisEnDistribution.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisNonRemisEnDistribution(Collection<String> c) {
		colisNonRemisEnDistribution.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromColisNonRemisEnDistribution(String e) {
		colisNonRemisEnDistribution.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisNonRemisEnDistribution() {
		colisNonRemisEnDistribution.clear();
	}

	/**
	 * @return an immutable copy of colisTASansSD
	 */
	public ImmutableSet<String> getColisTASansSD() {
		return ImmutableSet.copyOf(colisTASansSD);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisTASansSD(Collection<String> c) {
		clearColisTASansSD();
		if (c != null) {
			addAllToColisTASansSD(c);
		}
	}

	/**
	 * @param e
	 *            an element to add in the collection
	 */
	public void addToColisTASansSD(String e) {
		colisTASansSD.add(e);
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisTASansSD(Collection<String> c) {
		colisTASansSD.addAll(c);
	}

	/**
	 * 
	 * @param e
	 *            an element to remove from the collection
	 */
	public void removeFromColisTASansSD(String e) {
		colisTASansSD.remove(e);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisTASansSD() {
		colisTASansSD.clear();
	}
	
	/**
	 * @return an immutable copy of colisTASansSD
	 */
	public ImmutableSet<String> getColisTASansSDDeloc() {
		return ImmutableSet.copyOf(colisTASansSDDeloc);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisTASansSDDeloc(Collection<String> c) {
		clearColisTASansSDDeloc();
		if (c != null) {
			addAllToColisTASansSDDeloc(c);
		}
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisTASansSDDeloc(Collection<String> c) {
		colisTASansSDDeloc.addAll(c);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisTASansSDDeloc() {
		colisTASansSDDeloc.clear();
	}

	public ImmutableSet<String> getColisIncidentTG2Jour() {
		return ImmutableSet.copyOf(colisIncidentTG2Jour);
	}

	public void setColisIncidentTG2Jour(Collection<String> c) {
		clearColisIncidentTG2Jour();
		if (c != null) {
			addAllToColisIncidentTG2Jour(c);
		}
	}

	public void addToColisIncidentTG2Jour(String e) {
		colisIncidentTG2Jour.add(e);
	}

	public void addAllToColisIncidentTG2Jour(Collection<String> c) {
		colisIncidentTG2Jour.addAll(c);
	}

	public void clearColisIncidentTG2Jour() {
		colisIncidentTG2Jour.clear();
	}
	
	public Set<String> getColisPerdus() {
		return ImmutableSet.copyOf(colisPerdus);
	}

	public void setColisPerdus(Collection<String> c) {
		colisPerdus.clear();
		if (c != null) {
			colisPerdus.addAll(c);
		}
	}
	
	/**
	 * @return an immutable copy of colisSDSecheTotal
	 */
	public ImmutableSet<String> getColisSDSecheTotal() {
		return ImmutableSet.copyOf(colisSDSecheTotal);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisSDSecheTotal(Collection<String> c) {
		clearColisSDSecheTotal();
		if (c != null) {
			addAllToColisSDSecheTotal(c);
		}
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisSDSecheTotal(Collection<String> c) {
		colisSDSecheTotal.addAll(c);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisSDSecheTotal() {
		colisSDSecheTotal.clear();
	}
	
	/**
	 * @return an immutable copy of colisEnAlerteActive
	 */
	public ImmutableSet<String> getColisEnAlerteActive() {
		return ImmutableSet.copyOf(colisEnAlerteActive);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisEnAlerteActive(Collection<String> c) {
		clearColisEnAlerteActive();
		if (c != null) {
			addAllToColisEnAlerteActive(c);
		}
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisEnAlerteActive(Collection<String> c) {
		colisEnAlerteActive.addAll(c);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisEnAlerteActive() {
		colisEnAlerteActive.clear();
	}
	
	/**
	 * @return an immutable copy of colisEnAlerteActiveDeloc
	 */
	public ImmutableSet<String> getColisEnAlerteActiveDeloc() {
		return ImmutableSet.copyOf(colisEnAlerteActiveDeloc);
	}

	/**
	 * @param c
	 *            a collection to set
	 */
	public void setColisEnAlerteActiveDeloc(Collection<String> c) {
		clearColisEnAlerteActiveDeloc();
		if (c != null) {
			addAllToColisEnAlerteActiveDeloc(c);
		}
	}

	/**
	 * @param c
	 *            a collection of elements to add in the collection
	 */
	public void addAllToColisEnAlerteActiveDeloc(Collection<String> c) {
		colisEnAlerteActiveDeloc.addAll(c);
	}

	/**
	 * Clear the collection
	 */
	public void clearColisEnAlerteActiveDeloc() {
		colisEnAlerteActiveDeloc.clear();
	}

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
}
