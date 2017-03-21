package com.chronopost.vision.microservices.genereevt;

import javax.xml.ws.BindingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import fr.chronopost.sgesws.cxf.SGESServiceWS;
import fr.chronopost.sgesws.cxf.SGESServiceWSService;

/**
 * 
 * Commande Hystrix encapsulant l'initialisation du Webservice SGES
 * 
 * @author jcbontemps
 *
 */
public class CreateSGESCommand extends HystrixCommand<SGESServiceWS> {

	private final static Logger logger = LoggerFactory.getLogger(CreateSGESCommand.class);
	private final String endpoint;

	protected CreateSGESCommand(final String endpoint) {
		super(HystrixCommandGroupKey.Factory.asKey("CreateSGESCommand"));
		this.endpoint = endpoint;
	}

	@Override
	protected SGESServiceWS run() throws Exception {
		final SGESServiceWS service = new SGESServiceWSService().getSGESServiceWSPort();
		final BindingProvider bp = (BindingProvider) service;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.endpoint);
		return service;
	}

	/*
	 * Réponse en cas d'échec
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.netflix.hystrix.HystrixCommand#getFallback()
	 */
	@Override
	public SGESServiceWS getFallback() {
		logger.warn("Erreur CreateSGESCommand : " + getFailedExecutionException().getMessage(),
				getFailedExecutionException());
		throw new MSTechnicalException("Erreur CreateSGESCommand : " + getFailedExecutionException().getMessage(),
				getFailedExecutionException());
	}
}
