package com.chronopost.vision.microservices.maintienindexevt.v1;

import java.text.ParseException;

import com.chronopost.vision.model.maintienIndexEvt.v1.MaintienIndexEvtInput;
import com.chronopost.vision.model.maintienIndexEvt.v1.MaintienIndexEvtOutput;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface IMaintienIndexEvtService {
	
	/**
	 * Injection du dao.
	 * 
	 * @param dao
	 * @return
	 */
	IMaintienIndexEvtService setDao(final IMaintienIndexEvtDao dao);
	
	/**
	 * Mise à jour des tables d'index liées aux événements et au calcul retard.
	 * 
	 * @param input
	 * @return
	 * @throws Exception 
	 * @throws ParseException 
	 * @throws JsonProcessingException 
	 */
	MaintienIndexEvtOutput maintienIndexEvt(final MaintienIndexEvtInput input) throws JsonProcessingException, ParseException, Exception;
	
	/**
	 * Permet de coupler la date de livraison estimee à l'heure max de livraison estimée.
	 * 
	 * @param input
	 * @return
	 */
	String computeDateDeLivraisonEstimee(final MaintienIndexEvtInput input);
}
