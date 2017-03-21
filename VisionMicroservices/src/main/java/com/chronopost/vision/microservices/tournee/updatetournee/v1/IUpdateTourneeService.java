package com.chronopost.vision.microservices.tournee.updatetournee.v1;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.chronopost.vision.model.Evt;

/**
 * Interface de Service pour la ressource UpdateTourneeResource
 */
public interface IUpdateTourneeService {

    /**
     * Injection du dao.
     * 
     * @param dao
     * @return l'objet lui-même (injection à la création)
     */
    public IUpdateTourneeService setDao(IUpdateTourneeDao dao);

    /**
     * Service de mise à jour des tournées à partir d'une liste d'événements.
     * 
     * @param evts
     * @return le résultat de l'opération
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public boolean updateTournee(List<Evt> evts) throws InterruptedException, ExecutionException;

}
