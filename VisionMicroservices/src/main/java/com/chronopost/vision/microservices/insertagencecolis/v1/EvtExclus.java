package com.chronopost.vision.microservices.insertagencecolis.v1;

import java.util.Date;

/**
 * Class DTO utilisée pour constituer une liste de colis à saisir dans une agence à une date données.
 * Cette liste est ensuite traitée par dao.addColisInASaisirAgence
 * @author lguay
 *
 */
public class EvtExclus {

	/** Identifiant du colis */
	private String noLt;
	/** Date de la reprise en compte */
	private Date dateRepriseEnCompte;
	/** agence (poste comptable) ou a été saisi l'evt */
	private String pcAgence;
	
	/**
	 * 
	 * @param noLt : identifiant du colis
	 * @param dateRepriseEnCompte : date de reprise en compte prévue
	 * @param pcAgence : l'agence où a été saisi l'événement
	 */
	public EvtExclus(String noLt, Date dateRepriseEnCompte, String pcAgence) {
		this.noLt = noLt;
		this.dateRepriseEnCompte = dateRepriseEnCompte;
		this.pcAgence = pcAgence;
	}

	/** @return Identifiant du colis */
	public String getNoLt() {
		return noLt;
	}
	
	/** @return Date de la reprise en compte */
	public Date getDateRepriseEnCompte() {
		return dateRepriseEnCompte;
	}
	
	/** @return L'agence où a été saisi l'événement */
	public String getPcAgence() {
		return pcAgence;
	}
	
}
