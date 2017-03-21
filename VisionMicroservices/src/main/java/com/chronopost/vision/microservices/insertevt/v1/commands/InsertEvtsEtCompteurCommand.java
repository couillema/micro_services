package com.chronopost.vision.microservices.insertevt.v1.commands;

import java.util.ArrayList;
import java.util.List;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.insertevt.v1.IInsertEvtDao;
import com.chronopost.vision.model.Evt;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class InsertEvtsEtCompteurCommand extends HystrixCommand<Boolean> {

    private IInsertEvtDao dao;
    private final List<Evt> evts;
    private final long nbEvtsOrig;

    /**
     * Création d'une commande d'appel à la dao d'insertion des événement avec mise à jour du compteur 
     * @param dao : la dao à utiliser
     * @param evts : la liste des événements à insérer.
     * @param nbEvtOrig : nombre d'événéments reçus par insertEvt
     */
    public InsertEvtsEtCompteurCommand(IInsertEvtDao dao, List<Evt> evts, int nbEvtOrig) {
        super(HystrixCommandGroupKey.Factory.asKey("InsertEvtsCommand"));
        this.dao = dao;
        this.evts = ImmutableList.copyOf(new ArrayList<>(evts));
        this.nbEvtsOrig = nbEvtOrig;
    }

    @Override
    @Timed
    protected Boolean run() throws Exception {
        return dao.insertEvts(evts,nbEvtsOrig);
    }

    /*
     * Réponse en cas d'échec
     * 
     * (non-Javadoc)
     * 
     * @see com.netflix.hystrix.HystrixCommand#getFallback()
     */
    @Override
    public Boolean getFallback() {
        throw new MSTechnicalException(getFailedExecutionException());
    }
}