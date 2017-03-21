package com.chronopost.vision.microservices.tournee.getalertestournees.v1;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.chronopost.vision.microservices.sdk.exception.NotFoundException;
import com.chronopost.vision.model.getAlertesTournees.v1.GetAlertesTourneesOutput;

public interface IGetAlertesTourneesService {

    /**
     * Injecteur du dao.
     * 
     * @param dao
     * @return
     */
    public IGetAlertesTourneesService setDao(IGetAlertesTourneesDao dao);

    /**
     * Méthode qui retourne le nombre de colis qui risquent d'être livrés au
     * delà du délai imparti
     * 
     * @param codesTournee
     *            les codes des tournées à examiner
     * @param dateVerification
     *            la date à laquelle les colis doivent être livrés
     * @return Le nombre d'alertes sur chaque tournée
     * @throws NotFoundException
     * @throws ParseException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public GetAlertesTourneesOutput getAlertesTournees(List<String> codesTournee, Date dateVerification)
            throws NotFoundException, ParseException, InterruptedException, ExecutionException, TimeoutException;

}