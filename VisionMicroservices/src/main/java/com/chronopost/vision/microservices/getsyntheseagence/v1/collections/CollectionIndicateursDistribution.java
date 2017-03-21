package com.chronopost.vision.microservices.getsyntheseagence.v1.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.chronopost.vision.model.getsyntheseagence.v1.ECodeIndicateur;
import com.google.common.collect.Sets;

public class CollectionIndicateursDistribution {

	
	public static final Set<String> INDICATEURS_JOUR = Sets.newHashSet(
			 ECodeIndicateur.DISTRIBUES.getCode()
			,ECodeIndicateur.AVEC_ECHEC.getCode()
			,ECodeIndicateur.INSTANCE.getCode()
			,ECodeIndicateur.DISTRIBUES_DELOC.getCode()
			,ECodeIndicateur.AVEC_ECHEC_DELOC.getCode()
			,ECodeIndicateur.INSTANCE_DELOC.getCode()
			,ECodeIndicateur.TA_SECHE.getCode()
			,ECodeIndicateur.INSTANCE_NON_ACQUITTES.getCode()
			,ECodeIndicateur.EN_ECHEC.getCode()
			,ECodeIndicateur.TA_SECHE_DELOC.getCode()
			,ECodeIndicateur.INSTANCE_NON_ACQUITTES_DELOC.getCode()
			,ECodeIndicateur.EN_ECHEC_DELOC.getCode()
			);
	
	
	private final Set<String> colisDistribues = new HashSet<>();
	private final Set<String> colisDistribuesDeloc = new HashSet<>();
	private final Set<String> colisAvecEchec = new HashSet<>();
	private final Set<String> colisAvecEchecDeloc = new HashSet<>();
	private final Set<String> colisInstance = new HashSet<>();
	private final Set<String> colisInstanceDeloc = new HashSet<>();
	private final Set<String> colisTASeche = new HashSet<>();
	private final Set<String> colisTASecheDeloc = new HashSet<>();
	private final Set<String> colisInstanceNonAcquittes = new HashSet<>();
	private final Set<String> colisInstanceNonAcquittesDeloc = new HashSet<>();
	private final Set<String> colisEnEchec = new HashSet<>();
	private final Set<String> colisEnEchecDeloc = new HashSet<>();

	public Set<String> getColisDistribues() {
		return colisDistribues;
	}

	public void setColisDistribues(Collection<String> c) {
		clearColisDistribues();
		if (c != null) {
			addAllToColisDistribues(c);
		}
	}

	public void addAllToColisDistribues(Collection<String> c) {
		colisDistribues.addAll(c);
	}

	public void clearColisDistribues() {
		colisDistribues.clear();
	}

	public Set<String> getColisDistribuesDeloc() {
		return colisDistribuesDeloc;
	}

	public void setColisDistribuesDeloc(Collection<String> c) {
		clearColisDistribuesDeloc();
		if (c != null) {
			addAllToColisDistribuesDeloc(c);
		}
	}

	public void addAllToColisDistribuesDeloc(Collection<String> c) {
		colisDistribuesDeloc.addAll(c);
	}

	public void clearColisDistribuesDeloc() {
		colisDistribuesDeloc.clear();
	}

	public Set<String> getColisAvecEchec() {
		return colisAvecEchec;
	}

	public void setColisAvecEchec(Collection<String> c) {
		clearColisAvecEchec();
		if (c != null) {
			addAllToColisAvecEchec(c);
		}
	}

	public void addAllToColisAvecEchec(Collection<String> c) {
		colisAvecEchec.addAll(c);
	}

	public void clearColisAvecEchec() {
		colisAvecEchec.clear();
	}

	public Set<String> getColisAvecEchecDeloc() {
		return colisAvecEchecDeloc;
	}

	public void setColisAvecEchecDeloc(Collection<String> c) {
		clearColisAvecEchecDeloc();
		if (c != null) {
			addAllToColisAvecEchecDeloc(c);
		}
	}

	public void addAllToColisAvecEchecDeloc(Collection<String> c) {
		colisAvecEchecDeloc.addAll(c);
	}

	public void clearColisAvecEchecDeloc() {
		colisAvecEchecDeloc.clear();
	}

	public Set<String> getColisInstance() {
		return colisInstance;
	}

	public void setColisInstance(Collection<String> c) {
		clearColisInstance();
		if (c != null) {
			addAllToColisInstance(c);
		}
	}

	public void addAllToColisInstance(Collection<String> c) {
		colisInstance.addAll(c);
	}

	public void clearColisInstance() {
		colisInstance.clear();
	}

	public Set<String> getColisInstanceDeloc() {
		return colisInstanceDeloc;
	}

	public void setColisInstanceDeloc(Collection<String> c) {
		clearColisInstanceDeloc();
		if (c != null) {
			addAllToColisInstanceDeloc(c);
		}
	}

	public void addAllToColisInstanceDeloc(Collection<String> c) {
		colisInstanceDeloc.addAll(c);
	}

	public void clearColisInstanceDeloc() {
		colisInstanceDeloc.clear();
	}

	public Set<String> getColisTASeche() {
		return colisTASeche;
	}

	public void setColisTASeche(Collection<String> c) {
		clearColisTASeche();
		if (c != null) {
			addAllToColisTASeche(c);
		}
	}

	public void addAllToColisTASeche(Collection<String> c) {
		colisTASeche.addAll(c);
	}

	public void clearColisTASeche() {
		colisTASeche.clear();
	}

	public Set<String> getColisTASecheDeloc() {
		return colisTASecheDeloc;
	}

	public void setColisTASecheDeloc(Collection<String> c) {
		clearColisTASecheDeloc();
		if (c != null) {
			addAllToColisTASecheDeloc(c);
		}
	}

	public void addAllToColisTASecheDeloc(Collection<String> c) {
		colisTASecheDeloc.addAll(c);
	}

	public void clearColisTASecheDeloc() {
		colisTASecheDeloc.clear();
	}

	public Set<String> getColisInstanceNonAcquittes() {
		return colisInstanceNonAcquittes;
	}

	public void setColisInstanceNonAcquittes(Collection<String> c) {
		clearColisInstanceNonAcquittes();
		if (c != null) {
			addAllToColisInstanceNonAcquittes(c);
		}
	}

	public void addAllToColisInstanceNonAcquittes(Collection<String> c) {
		colisInstanceNonAcquittes.addAll(c);
	}

	public void clearColisInstanceNonAcquittes() {
		colisInstanceNonAcquittes.clear();
	}

	public Set<String> getColisInstanceNonAcquittesDeloc() {
		return colisInstanceNonAcquittesDeloc;
	}

	public void setColisInstanceNonAcquittesDeloc(Collection<String> c) {
		clearColisInstanceNonAcquittesDeloc();
		if (c != null) {
			addAllToColisInstanceNonAcquittesDeloc(c);
		}
	}

	public void addAllToColisInstanceNonAcquittesDeloc(Collection<String> c) {
		colisInstanceNonAcquittesDeloc.addAll(c);
	}

	public void clearColisInstanceNonAcquittesDeloc() {
		colisInstanceNonAcquittesDeloc.clear();
	}

	public Set<String> getColisEnEchec() {
		return colisEnEchec;
	}

	public void setColisEnEchec(Collection<String> c) {
		clearColisEnEchec();
		if (c != null) {
			addAllToColisEnEchec(c);
		}
	}

	public void addAllToColisEnEchec(Collection<String> c) {
		colisEnEchec.addAll(c);
	}

	public void clearColisEnEchec() {
		colisEnEchec.clear();
	}

	public Set<String> getColisEnEchecDeloc() {
		return colisEnEchecDeloc;
	}

	public void setColisEnEchecDeloc(Collection<String> c) {
		clearColisEnEchecDeloc();
		if (c != null) {
			addAllToColisEnEchecDeloc(c);
		}
	}

	public void addAllToColisEnEchecDeloc(Collection<String> c) {
		colisEnEchecDeloc.addAll(c);
	}

	public void clearColisEnEchecDeloc() {
		colisEnEchecDeloc.clear();
	}
}
