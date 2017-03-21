package com.chronopost.vision.microservices.colioutai.get.v2.commands;

import java.net.MalformedURLException;

import javax.xml.ws.BindingProvider;

import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import fr.chronopost.poi.webservice.PoiService;
import fr.chronopost.poi.webservice.impl.PoiWebService;

public class InitServicePoiCommand extends HystrixCommand<PoiService> {
    
	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(InitServicePoiCommand.class);
	
	private String endpoint;
	
    public InitServicePoiCommand(String endpoint) throws MalformedURLException {
        super(HystrixCommandGroupKey.Factory.asKey("InitServicePoiCommand"));
        this.endpoint = endpoint;        
    }

    @Override
    @Timed
    protected PoiService run() throws Exception {
    	logger.info("Init du WS POI sur le endpoint : " + this.endpoint);
    	PoiService servicePOI = new PoiWebService().getPoiServiceImplPort();
        BindingProvider bpPOI = (BindingProvider)servicePOI;
        bpPOI.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.endpoint);
        
        return servicePOI;
    }

    /* 
     * Réponse en cas d'échec
     * 
     * (non-Javadoc)
     * @see com.netflix.hystrix.HystrixCommand#getFallback()
     */
    @Override
    public PoiService getFallback() {
    	try{
    		logger.warn("Erreur InitPoiService", getFailedExecutionException());
    	}catch(Exception e){
    		logger.error("Initialisation du service POI impossible", e);
    	}
        return null;
    }
}