package com.chronopost.vision.microservices.insertevt.v1.commands;

import java.util.List;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.sdk.UpdateTourneeV1;
import com.chronopost.vision.model.Evt;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class UpdateTourneeCommand extends HystrixCommand<Boolean> {
    
	private List<Evt> evts;

    public UpdateTourneeCommand(List<Evt> evts) {
        super(HystrixCommandGroupKey.Factory.asKey("UpdateTourneeCommand"));
        this.evts = evts;
    }

    @Override
    @Timed
    protected Boolean run() throws Exception {
        return UpdateTourneeV1.getInstance().updateTournee(this.evts);
    }

    /* 
     * Réponse en cas d'échec
     * 
     * (non-Javadoc)
     * @see com.netflix.hystrix.HystrixCommand#getFallback()
     */
    @Override
    public Boolean getFallback() {
    	throw new MSTechnicalException(getFailedExecutionException());
    }
}