package com.chronopost.vision.microservices.insertpointtournee.v1;

import java.util.List;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Evt;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * Commande appelant la méthode miseAJourTournee pour un evenement
 */
public class MiseAJourTourneeCommand extends HystrixCommand<Boolean> {

    private List<Evt> evenement;
    private IInsertPointTourneeDao dao;

    public MiseAJourTourneeCommand(final IInsertPointTourneeDao dao, final List<Evt> evtsTournee) {
        super(HystrixCommandGroupKey.Factory.asKey("ComputeIdC11Command"));
        this.evenement = evtsTournee;
        this.dao = dao;
        this.circuitBreaker.isOpen();
    }

    @Override
    @Timed
    protected Boolean run() throws Exception {

        return Boolean.valueOf(dao.miseAJourTournee(this.evenement));
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