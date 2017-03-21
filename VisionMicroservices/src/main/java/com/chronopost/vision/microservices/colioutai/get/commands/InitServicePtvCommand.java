package com.chronopost.vision.microservices.colioutai.get.commands;

import java.net.MalformedURLException;

import javax.xml.ws.BindingProvider;

import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.ptvgroup.chronopost.ws.xchrono.XChronoWS;
import com.ptvgroup.chronopost.ws.xchrono.XChronoWSService;

public class InitServicePtvCommand extends HystrixCommand<XChronoWS> {

	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(InitServicePtvCommand.class);

	private String endpoint;

	public InitServicePtvCommand(String endpoint) throws MalformedURLException {
		super(HystrixCommandGroupKey.Factory.asKey("InitServicePtvCommand"));
		this.endpoint = endpoint;
	}

	@Override
	@Timed
	protected XChronoWS run() throws Exception {
		XChronoWS servicePTV = new XChronoWSService().getXChronoWSPort();
		BindingProvider bpPOI = (BindingProvider) servicePTV;
		bpPOI.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.endpoint);

		return servicePTV;
	}

	/*
	 * Réponse en cas d'échec
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.netflix.hystrix.HystrixCommand#getFallback()
	 */
	@Override
	public XChronoWS getFallback() {
		try {
			logger.warn("Erreur InitPtvService " ,
					getFailedExecutionException());
		} catch (Exception e) {
			logger.error("Initialisation du service PTV impossible", e);
		}
		return null;
	}
}