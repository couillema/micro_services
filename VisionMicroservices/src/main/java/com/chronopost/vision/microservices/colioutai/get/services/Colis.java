package com.chronopost.vision.microservices.colioutai.get.services;

import java.util.Date;

import com.chronopost.vision.model.rules.DateRules;

/**
 * Classe toute simple ayant pour but de faciliter l'algo de calcul ETA / indice de confiance
 * @author vdesaintpern
 *
 */
public class Colis {

	int numero;
	
	Date etaOptim;
	
	Date etaMAJ; 
	
	Date dateRealisation;
	
	boolean realise;

	public Colis(int numero, Date dateRealisation) {
		super();
		this.numero = numero;
		this.dateRealisation = dateRealisation;
		this.realise = dateRealisation != null;
	}

	public String toString() {
// NO MORE Parse Exception		
//		try {
			return "[" + numero + " " + (this.dateRealisation == null ? "non realise" : DateRules.toDateHeureClient(dateRealisation)) + " ]";
//		} catch (ParseException e) {
//			return "Erreur Parsing Date real";
//		}
	}
}
