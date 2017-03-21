package com.chronopost.vision.microservices.tournee.getdetailtournee.v1;

import java.text.ParseException;
import java.util.Date;

public interface IGetDetailTourneeDao {

    /**
     * Récupération depuis la base de la liste des colis pris en charge et/ou
     * distribués par la tournée recherchée.
     * 
     * @param codeTournee
     * @param dateTournee
     * @return
     * @throws ParseException
     */
    public Tournee getTournee(String codeTournee, Date dateTournee) throws ParseException;

}
