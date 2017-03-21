package com.chronopost.vision.microservices.getsyntheseagence.v1.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CollectionsDistribution {

	private final Set<String> colisSaisis = new HashSet<>();
	private final Set<String> instanceP = new HashSet<>();
	private final Set<String> enInstanceP = new HashSet<>();
	private final Set<String> vueEnRetourP = new HashSet<>();
	private final Set<String> sortieReseau = new HashSet<>();
	private final Set<String> enLivraisonPA = new HashSet<>();
	private final Set<String> livreDomicileP = new HashSet<>();
	private final Set<String> livreDomicilePA = new HashSet<>();
	private final Set<String> livreDomicilePADeloc = new HashSet<>();
	private final Set<String> enPrepaPA = new HashSet<>();
	private final Set<String> enPrepaPADeloc = new HashSet<>();
	private final Set<String> enInstancePA = new HashSet<>();
	private final Set<String> enInstancePADeloc = new HashSet<>();
	private final Set<String> echecPA = new HashSet<>();
	private final Set<String> echecPADeloc = new HashSet<>();

	public Set<String> getColisSaisis() {
		return colisSaisis;
	}

	public void setColisSaisis(Collection<String> c) {
		clearColisSaisis();
		if (c != null) {
			addAllToColisSaisis(c);
		}
	}

	public void addAllToColisSaisis(Collection<String> c) {
		colisSaisis.addAll(c);
	}

	public void clearColisSaisis() {
		colisSaisis.clear();
	}

	public Set<String> getInstanceP() {
		return instanceP;
	}

	public void setInstanceP(Collection<String> c) {
		clearInstanceP();
		if (c != null) {
			addAllToInstanceP(c);
		}
	}

	public void addAllToInstanceP(Collection<String> c) {
		instanceP.addAll(c);
	}

	public void addToInstanceP(String noLt) {
		instanceP.add(noLt);
	}

	public void clearInstanceP() {
		instanceP.clear();
	}

	public Set<String> getEnInstanceP() {
		return enInstanceP;
	}

	public void setEnInstanceP(Collection<String> c) {
		clearEnInstanceP();
		if (c != null) {
			addAllToEnInstanceP(c);
		}
	}

	public void addAllToEnInstanceP(Collection<String> c) {
		enInstanceP.addAll(c);
	}

	public void addToEnInstanceP(String noLt) {
		enInstanceP.add(noLt);
	}

	public void clearEnInstanceP() {
		enInstanceP.clear();
	}

	public Set<String> getVueEnRetourP() {
		return vueEnRetourP;
	}

	public void setVueEnRetourP(Collection<String> c) {
		clearVueEnRetourP();
		if (c != null) {
			addAllToVueEnRetourP(c);
		}
	}

	public void addAllToVueEnRetourP(Collection<String> c) {
		vueEnRetourP.addAll(c);
	}

	public void addToVueEnRetourP(String noLt) {
		vueEnRetourP.add(noLt);
	}

	public void clearVueEnRetourP() {
		vueEnRetourP.clear();
	}

	public Set<String> getSortieReseau() {
		return sortieReseau;
	}

	public void setSortieReseau(Collection<String> c) {
		clearSortieReseau();
		if (c != null) {
			addAllToSortieReseau(c);
		}
	}

	public void addAllToSortieReseau(Collection<String> c) {
		sortieReseau.addAll(c);
	}

	public void addToSortieReseau(String noLt) {
		sortieReseau.add(noLt);
	}

	public void clearSortieReseau() {
		sortieReseau.clear();
	}

	public Set<String> getEnLivraisonPA() {
		return enLivraisonPA;
	}

	public void setEnLivraisonPA(Collection<String> c) {
		clearEnLivraisonPA();
		if (c != null) {
			addAllToEnLivraisonPA(c);
		}
	}

	public void addAllToEnLivraisonPA(Collection<String> c) {
		enLivraisonPA.addAll(c);
	}

	public void addToEnLivraisonPA(String noLt) {
		enLivraisonPA.add(noLt);
	}

	public void clearEnLivraisonPA() {
		enLivraisonPA.clear();
	}

	public Set<String> getLivreDomicileP() {
		return livreDomicileP;
	}

	public void setLivreDomicileP(Collection<String> c) {
		clearLivreDomicileP();
		if (c != null) {
			addAllToLivreDomicileP(c);
		}
	}

	public void addAllToLivreDomicileP(Collection<String> c) {
		livreDomicileP.addAll(c);
	}

	public void addToLivreDomicileP(String noLt) {
		livreDomicileP.add(noLt);
	}

	public void clearLivreDomicileP() {
		livreDomicileP.clear();
	}

	public Set<String> getLivreDomicilePA() {
		return livreDomicilePA;
	}

	public void setLivreDomicilePA(Collection<String> c) {
		clearLivreDomicilePA();
		if (c != null) {
			addAllToLivreDomicilePA(c);
		}
	}

	public void addAllToLivreDomicilePA(Collection<String> c) {
		livreDomicilePA.addAll(c);
	}

	public void addToLivreDomicilePA(String noLt) {
		livreDomicilePA.add(noLt);
	}

	public void clearLivreDomicilePA() {
		livreDomicilePA.clear();
	}

	public Set<String> getLivreDomicilePADeloc() {
		return livreDomicilePADeloc;
	}

	public void setLivreDomicilePADeloc(Collection<String> c) {
		clearLivreDomicilePADeloc();
		if (c != null) {
			addAllToLivreDomicilePADeloc(c);
		}
	}

	public void addAllToLivreDomicilePADeloc(Collection<String> c) {
		livreDomicilePADeloc.addAll(c);
	}

	public void addToLivreDomicilePADeloc(String noLt) {
		livreDomicilePADeloc.add(noLt);
	}

	public void clearLivreDomicilePADeloc() {
		livreDomicilePADeloc.clear();
	}

	public Set<String> getEnPrepaPA() {
		return enPrepaPA;
	}

	public void setEnPrepaPA(Collection<String> c) {
		clearEnPrepaPA();
		if (c != null) {
			addAllToEnPrepaPA(c);
		}
	}

	public void addAllToEnPrepaPA(Collection<String> c) {
		enPrepaPA.addAll(c);
	}

	public void addToEnPrepaPA(String noLt) {
		enPrepaPA.add(noLt);
	}

	public void clearEnPrepaPA() {
		enPrepaPA.clear();
	}

	public Set<String> getEnPrepaPADeloc() {
		return enPrepaPADeloc;
	}

	public void setEnPrepaPADeloc(Collection<String> c) {
		clearEnPrepaPADeloc();
		if (c != null) {
			addAllToEnPrepaPADeloc(c);
		}
	}

	public void addAllToEnPrepaPADeloc(Collection<String> c) {
		enPrepaPADeloc.addAll(c);
	}

	public void addToEnPrepaPADeloc(String noLt) {
		enPrepaPADeloc.add(noLt);
	}

	public void clearEnPrepaPADeloc() {
		enPrepaPADeloc.clear();
	}

	public Set<String> getEnInstancePA() {
		return enInstancePA;
	}

	public void setEnInstancePA(Collection<String> c) {
		clearEnInstancePA();
		if (c != null) {
			addAllToEnInstancePA(c);
		}
	}

	public void addAllToEnInstancePA(Collection<String> c) {
		enInstancePA.addAll(c);
	}

	public void addToEnInstancePA(String noLt) {
		enInstancePA.add(noLt);
	}

	public void clearEnInstancePA() {
		enInstancePA.clear();
	}

	public Set<String> getEnInstancePADeloc() {
		return enInstancePADeloc;
	}

	public void setEnInstancePADeloc(Collection<String> c) {
		clearEnInstancePADeloc();
		if (c != null) {
			addAllToEnInstancePADeloc(c);
		}
	}

	public void addAllToEnInstancePADeloc(Collection<String> c) {
		enInstancePADeloc.addAll(c);
	}

	public void addToEnInstancePADeloc(String noLt) {
		enInstancePADeloc.add(noLt);
	}

	public void clearEnInstancePADeloc() {
		enInstancePADeloc.clear();
	}

	public Set<String> getEchecPA() {
		return echecPA;
	}

	public void setEchecPA(Collection<String> c) {
		clearEchecPA();
		if (c != null) {
			addAllToEchecPA(c);
		}
	}

	public void addAllToEchecPA(Collection<String> c) {
		echecPA.addAll(c);
	}

	public void addToEchecPA(String noLt) {
		echecPA.add(noLt);
	}

	public void clearEchecPA() {
		echecPA.clear();
	}

	public Set<String> getEchecPADeloc() {
		return echecPADeloc;
	}

	public void setEchecPADeloc(Collection<String> c) {
		clearEchecPADeloc();
		if (c != null) {
			addAllToEchecPADeloc(c);
		}
	}

	public void addAllToEchecPADeloc(Collection<String> c) {
		echecPADeloc.addAll(c);
	}

	public void addToEchecPADeloc(String noLt) {
		echecPADeloc.add(noLt);
	}

	public void clearEchecPADeloc() {
		echecPADeloc.clear();
	}
}
