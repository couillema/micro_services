package com.chronopost.vision.microservices.colioutai.get.v2.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.model.Lt;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import fr.chronopost.soap.consigne.cxf.ConsigneServiceWS;
import fr.chronopost.soap.consigne.cxf.ResultInformationsConsigne;

public class ServiceConsigneCommand extends HystrixCommand<ResultInformationsConsigne> {
	
	private final static Logger logger = LoggerFactory
			.getLogger(ServiceConsigneCommand.class);
	
	private ConsigneServiceWS serviceConsigne;
	private Lt lt;
	
	public ServiceConsigneCommand(ConsigneServiceWS serviceConsigne, Lt lt) {
		super(HystrixCommandGroupKey.Factory.asKey("ServiceConsigneCommand"));
		this.serviceConsigne = serviceConsigne;
		this.lt = lt;
	}

	@Override
	@Timed
	protected ResultInformationsConsigne run() throws Exception {
		logger.info("Service Consigne Command no lt : " + lt.getNoLt());
		ResultInformationsConsigne consigne = serviceConsigne.getInformationsColisConsigne(lt.getNoLt(), false);
		logger.info("Service Consigne Command no lt : " + lt.getNoLt() + " >>> OK >>> ");
		return consigne;
	}
	
	@Override
    public ResultInformationsConsigne getFallback() {
    	logger.warn("Erreur ServiceConsigneCommand pour lt " + lt.getNoLt() + " : ");
    	return null;
    }

}
