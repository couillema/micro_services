package com.chronopost.vision.microservices.genereevt;

import java.util.List;
import java.util.Map;

import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Evt;

/**
 * Interface service de génération d'Evt dans le SI 
 * @author jcbontemps
 */
public interface IGenereEvtService {

    /**
     * 
     * @param evts liste des évènements à insérer dans le SI
     * @param injectionVision Faut-il insérer les évènements dans la base Vision "manuellement" sans attendre le retour de l'EAI 
     * @return une map avec le résultat de l'insertion pour chaque evt
     * @throws MSTechnicalException
     * @throws FunctionalException
     */
    Map<Evt, Boolean> genereEvt(List<Evt> evts, Boolean injectionVision) throws MSTechnicalException, FunctionalException;

}
