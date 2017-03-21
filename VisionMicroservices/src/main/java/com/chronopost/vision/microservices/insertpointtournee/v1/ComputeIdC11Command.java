package com.chronopost.vision.microservices.insertpointtournee.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Evt;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class ComputeIdC11Command extends HystrixCommand<Boolean> {
	private static final Logger log = LoggerFactory.getLogger(ComputeIdC11Command.class); 
    private final Evt evenement;
    private final IInsertPointTourneeService service;

    public ComputeIdC11Command(final IInsertPointTourneeService service, final Evt evenement) {
        super(HystrixCommandGroupKey.Factory.asKey("ComputeIdC11Command"));
        this.service = service;
        this.evenement = evenement;
    }

    @Override
    @Timed
    protected Boolean run() throws Exception {
        return service.computeIdPointC11(this.evenement);
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
		if (getFailedExecutionException().getClass() == MSTechnicalException.class) {
			log.error("Erreur lors du calcul de l'id point C11 pour l'événement (remonter d'une exception) : "
					+ evenement.toString());
			throw new MSTechnicalException(getFailedExecutionException());
		} else
			log.error("Erreur lors du calcul de l'id point C11 pour l'événement (sans exception) : "
					+ evenement.toString());
		return Boolean.FALSE;
	}
}
