package com.chronopost.vision.microservices.colioutai.get.commands;

import java.net.MalformedURLException;

import javax.xml.ws.BindingProvider;

import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import fr.chronopost.soap.consigne.cxf.ConsigneServiceWS;
import fr.chronopost.soap.consigne.cxf.ConsigneServiceWSService;

public class InitServiceConsigneCommand extends HystrixCommand<ConsigneServiceWS> {
    
	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(InitServiceConsigneCommand.class);
	
	private String endpoint;
	
    public InitServiceConsigneCommand(String endpoint) throws MalformedURLException {
        super(HystrixCommandGroupKey.Factory.asKey("InitServiceConsigneCommand"));
        this.endpoint = endpoint;        
    }

    @Override
    @Timed
    protected ConsigneServiceWS run() throws Exception {    	    	
    	ConsigneServiceWS serviceConsigne = new ConsigneServiceWSService().getConsigneServiceWSPort();
        BindingProvider bpConsigne = (BindingProvider)serviceConsigne;
        bpConsigne.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.endpoint);
        
        return serviceConsigne;
    }

    /* 
     * Réponse en cas d'échec
     * 
     * (non-Javadoc)
     * @see com.netflix.hystrix.HystrixCommand#getFallback()
     */
    @Override
    public ConsigneServiceWS getFallback() {
    	try{
    		logger.warn("Erreur initConsigneServiceWS " , getFailedExecutionException());
    	}catch(Exception e){
    		logger.error("Initialisation du ConsigneServiceWS impossible", e);
    	}
        return null;
    }
}