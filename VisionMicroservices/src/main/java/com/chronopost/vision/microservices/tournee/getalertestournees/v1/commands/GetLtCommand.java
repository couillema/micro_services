package com.chronopost.vision.microservices.tournee.getalertestournees.v1.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.model.Lt;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class GetLtCommand extends HystrixCommand<Map<String, Lt>> {
	
	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(GetLtCommand.class);
	
	private List<String> numerosDeLt;

    public GetLtCommand(List<String> numerosDeLt) {
        super(HystrixCommandGroupKey.Factory.asKey("GetLtCommand"));
        this.numerosDeLt = numerosDeLt;
    }

    @Override
    @Timed
    protected Map<String, Lt> run() throws Exception {    	
		// Récupération des LTs en base pour en contrôler l'état et récupérer le synonyme maître s'il existe
    	if(numerosDeLt.size()>0){
    		Map<String, Lt> lts = GetLtV1.getInstance().getLt(numerosDeLt);
    		return lts;
    	}
    	
    	return new HashMap<String, Lt>();
    }

    /* 
     * Réponse en cas d'échec
     * 
     * (non-Javadoc)
     * @see com.netflix.hystrix.HystrixCommand#getFallback()
     */
    @Override
    public Map<String, Lt> getFallback() {
    	Throwable e = getFailedExecutionException();
    	logger.error("Erreur lors du GetLTs", e);
    	throw new MSTechnicalException(e);  
    }
}