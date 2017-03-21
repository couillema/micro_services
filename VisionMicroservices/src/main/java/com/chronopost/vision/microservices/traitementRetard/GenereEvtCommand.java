package com.chronopost.vision.microservices.traitementRetard;

import java.util.List;

import javax.ws.rs.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.sdk.GenereEvtV1;
import com.chronopost.vision.microservices.sdk.exception.ServiceUnavailableException;
import com.chronopost.vision.microservices.sdk.exception.TechnicalException;
import com.chronopost.vision.model.Evt;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class GenereEvtCommand extends HystrixCommand<Boolean> {

	private final static Logger logger = LoggerFactory.getLogger(GenereEvtCommand.class);

	private final List<Evt> evenements;

	public GenereEvtCommand(final List<Evt> pEvenements) {
		super(HystrixCommandGroupKey.Factory.asKey("GenereEvtCommand"));
		this.evenements = pEvenements;
	}

	@Override
	@Timed
	protected Boolean run() throws Exception {
		// Récupération des LTs en base pour en contrôler l'état et récupérer le
		// synonyme maître s'il existe
		if (evenements.size() > 0) {
			try {
				return GenereEvtV1.getInstance().genereEvt(evenements, true);
			} catch (final NotFoundException | ServiceUnavailableException | MSTechnicalException
					| TechnicalException e) {
				logger.error("Erreur lors du GenereEvt", e);
			}
		}
		return Boolean.FALSE;
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
		final Throwable e = getFailedExecutionException();
		logger.error("Erreur lors du GenereEvt", e);
		throw new MSTechnicalException(e);
	}
}