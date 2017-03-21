package com.chronopost.vision.microservices.updatespecificationscolis.v1;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.chronopost.vision.model.updatespecificationscolis.v1.EvtEtModifs;

/**
 * Interface Service
 * @author jcbontemps
 */
public interface IUpdateSpecificationsColisService {

	/**
	 * Traitement de la liste d'événements pour en extraire les spécificités :
	 *     - service (code service)
	 *     - evenement
	 *     - les consignes
	 *     
	 * @param evenements : la liste des événements à considérer
	 * @return un boolean indiquant si le traitement s'est bien déroulé dans sa totalité.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public boolean traitementSpecificationsColis(final List<EvtEtModifs> evenements) throws InterruptedException, ExecutionException;

    public void declareAppelMS();
    public void declareFailMS();
}