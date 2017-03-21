package com.chronopost.vision.microservices.suivibox;

import java.util.Date;

/**
 * DTO intermédiaire entre l'objet Evt et le DAO  
 * Utilisé par la DAO pour mettre à jour les données et alimentée par le Service
 *  
 * @author adejanovski
 */
public class SuiviBoxAgence {
	
	/**
	 * identifiant de Box.
	 */
	private String idBox;
	
	
	/**
	 * Date du dernier evt de la box.
	 */
	private Date dateDernierEvt;
	
	/**
	 * Poste comptable de l'agence ayant saisi l'evt sur la box.
	 */
	private String pcAgence;
	
	
	/** Code de l'action qui a eu lieu sur la box */
	private String action;
	/** Code etape de l'action qui a eu lieu sur la box */
	private String etape;
	/** Code de la tournee s'il est présent  */
	private String codeTournee;
	/** Code de la ligne routière s'il est présent */
	private String codeLR;
	
	/**
	 * Classe permettant de modéliser les mises à jour des positions des Box. 
	 */
	public SuiviBoxAgence(){	
	}
	
	/** trivial  */
	public String getIdBox() {
		return idBox;
	}
	
	
	/** trivial  */
	public SuiviBoxAgence setIdBox(String idBox) {
		this.idBox = idBox;
		return this;
	}
	
	/** trivial  */
	public Date getDateDernierEvt() {
		return dateDernierEvt;
	}
	
	
	/** trivial  */
	public SuiviBoxAgence setDateDernierEvt(Date dateDernierEvt) {
		this.dateDernierEvt = dateDernierEvt;
		return this;
	}
	
	/** trivial  */
	public String getPcAgence() {
		return pcAgence;
	}
	
	/** trivial  */
	public SuiviBoxAgence setPcAgence(String pcAgence) {
		this.pcAgence = pcAgence;
		return this;
	}

	/** trivial  */
	public String getAction() {
		return action;
	}

	/** trivial  */
	public SuiviBoxAgence setAction(String action) {
		this.action = action;
		return this;
	}

	/** trivial  */
	public String getEtape() {
		return etape;
	}

	/** trivial  */
	public SuiviBoxAgence setEtape(String etape) {
		this.etape = etape;
		return this;
	}

	/** trivial  */
	public String getCodeTournee() {
		return codeTournee;
	}

	/** trivial */
	public SuiviBoxAgence setCodeTournee(String codeTournee) {
		this.codeTournee = codeTournee;
		return this;
	}

	/** trivial  */
	public String getCodeLR() {
		return codeLR;
	}

	/** trivial  */
	public SuiviBoxAgence setCodeLR(String codeLR) {
		this.codeLR = codeLR;
		return this;
	}
}
