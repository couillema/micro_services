package com.chronopost.vision.microservices.tournee.updatetournee.v1.commands;

import java.util.List;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.tournee.updatetournee.v1.IUpdateTourneeDao;
import com.chronopost.vision.model.Evt;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * Commande appelant la méthode insertAgenceTournee d'un ITourneeDao
 */
public class InsertAgenceTourneeCommand extends HystrixCommand<Boolean> {

    private List<Evt> evts;
    private IUpdateTourneeDao dao;

    public InsertAgenceTourneeCommand(final IUpdateTourneeDao dao, final List<Evt> evts) {
        super(HystrixCommandGroupKey.Factory.asKey("InsertAgenceTourneeCommand"));
        this.evts = evts;
        this.dao = dao;
        this.circuitBreaker.isOpen();
    }

    @Override
    @Timed
    protected Boolean run() throws Exception {

        return dao.insertAgenceTournee(this.evts);

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