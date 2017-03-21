package com.chronopost.vision.microservices.suivibox;

import java.util.List;

import com.chronopost.vision.model.Evt;

/**
 * Interface d'insertion des événements GC
 * @author jcbontemps
 */
public interface SuiviboxService {

    /**
     * @param evts évenements à insérer en base
     * @return true si l'insertion (ou non insertion) en bas de l'événement est OK
     */
    public boolean insertEvtGCInDatabase(List<Evt> evts);

}
