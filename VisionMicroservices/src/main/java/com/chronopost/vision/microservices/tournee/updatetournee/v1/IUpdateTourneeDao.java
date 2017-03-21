package com.chronopost.vision.microservices.tournee.updatetournee.v1;

import java.text.ParseException;
import java.util.List;

import com.chronopost.vision.model.Evt;

/**
 * Interface DAO du microservice UpdateTournee
 * 
 */
public interface IUpdateTourneeDao {

    /**
     * Mise à jour de la table tournée. Un enreg est créé pour chaque code
     * tournée / date jour. les evt TA ajoutent un numéro de LT au champ ta. les
     * evt D+ ajoutent un numéro de LT au champ distri. les evt de prise en
     * charge (PC) ajoutent un numéro de LT au champ collecte.
     * 
     * @param evts
     * @return le résultat de l'insertion
     * @throws ParseException
     */
    public boolean updateTournee(List<Evt> evts) throws ParseException;

    /**
     * Mise à jour de la table tourneeParCodeService. Un enreg est créé pour
     * chaque code tournée / date jour / codeService.
     * 
     * @param evts
     * @return le résultat de l'insertion
     * @throws ParseException
     */
    public boolean updateTourneeCodeService(List<Evt> evts) throws ParseException;

    /**
     * Mise à jour de la table infoTournee. Cette table contient un
     * enregistrement par evt et remontée GPS.
     * 
     * @param evts
     * @return le résultat de l'insertion
     * @throws ParseException
     */
    public boolean insertInfoTournee(List<Evt> evts) throws ParseException;

    /**
     * Mise à jour de la table agenceTournee. Permet de lister les tournées des
     * agences sur un jour donné.
     * 
     * @param evts
     * @return le résultat de l'insertion
     * @throws ParseException
     */
    public boolean insertAgenceTournee(List<Evt> evts) throws ParseException;

    /**
     * Mise à jour de la table tourneeC11. Permet de retrouver le code tournée
     * (agence + tournée) à partir d'un idC11 sans codeAgence et sans position
     * dans la tournée (tel que sur les remontées GPS).
     * 
     * @param evts
     * @return le résultat de l'insertion
     * @throws ParseException
     */
    public boolean insertTourneeC11(List<Evt> evts) throws ParseException;

    /**
     * Mise à jour de la table colisTourneeAgence. Permet de rechercher la
     * tournée ayant en charge un colis à partir du numéro de LT.
     * 
     * @param evts
     * @return le résultat de l'insertion
     * @throws ParseException
     */
    public boolean insertColisTourneeAgence(List<Evt> evts) throws ParseException;

}
