package com.chronopost.vision.microservices.updatespecificationscolis.v1.command;

import java.util.List;

import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisServiceImpl;
import com.chronopost.vision.model.updatespecificationscolis.v1.EvtEtModifs;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class TraitementCodeServiceCommand extends HystrixCommand<Boolean> {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(TraitementCodeServiceCommand.class);

    private List<EvtEtModifs> evenements;

    public TraitementCodeServiceCommand(List<EvtEtModifs> pEvenements) {
        super(HystrixCommandGroupKey.Factory.asKey("UpdateSpecificationsColisTraitementsCommand"));
        this.evenements = pEvenements;
    }

    @Override
    @Timed
    protected Boolean run() throws Exception {
        return UpdateSpecificationsColisServiceImpl.getInstance().traitementCodeService(evenements) ;
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
        Throwable e = getFailedExecutionException();
        logger.error("Erreur lors du Traitement Consigne", e);
        throw new MSTechnicalException(e);
    }
}