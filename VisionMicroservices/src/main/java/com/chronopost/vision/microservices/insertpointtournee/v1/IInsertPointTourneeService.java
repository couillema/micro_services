package com.chronopost.vision.microservices.insertpointtournee.v1;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.model.Agence;
import com.chronopost.vision.model.Evt;

public interface IInsertPointTourneeService {

    /**
     * Positionnement de la DAO pour le service
     * 
     * @param pDao
     */
    public IInsertPointTourneeService setDao(IInsertPointTourneeDao pDao);

    /**
     * Traitement de tous les evenements reçus (filtrer par leur code evt) Seuls
     * les evts TA et D+ nous interessent dans ce microservice.
     * 
     * @param evenements : les evenements à traiter
     * @return true si tous les evenements ont été traités avec succes
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public boolean traiteEvenement(List<Evt> evenements) throws InterruptedException, ExecutionException;

    /**
     * Créer un idPointC11 pour une evt qui n'en a pas et l'ajoute aux infocomp
     * L'idPointC11 est intégré directement dans les infoscomp de l'evt fournit en parametre. 
     * @param evenement : l'événement pour lequel on désire créer l'idPointC11
     * 
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public boolean computeIdPointC11(Evt evenement);
    
    public IInsertPointTourneeService setRefentielAgence(CacheManager<Agence> cacheAgence);

	public void declareAppelMS();
	public void declareFailMS();
}