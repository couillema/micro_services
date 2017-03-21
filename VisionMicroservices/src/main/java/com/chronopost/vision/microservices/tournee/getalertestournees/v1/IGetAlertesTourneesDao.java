package com.chronopost.vision.microservices.tournee.getalertestournees.v1;

import java.util.Date;
import java.util.List;

import com.chronopost.vision.microservices.utils.TypeBorneCreneau;

public interface IGetAlertesTourneesDao {

    /**
     * Retourne la liste des lt ayant un créneau dont la borne demandée (min ou
     * max) est située entre dateInf et dateMax.
     * 
     * @param codeAgence
     * @param dateInf
     * @param dateMax
     * @param typeBorne
     * @return
     */
    public List<String> getNoLtAvecCreneauPourAgence(String codeAgence, Date dateInf, Date dateMax,
            TypeBorneCreneau typeBorne);

}
