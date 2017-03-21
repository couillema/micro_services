package com.chronopost.vision.microservices.insertevt.v1.commands;

import java.util.List;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.sdk.SuiviBoxV1;
import com.chronopost.vision.model.Evt;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class SuiviBoxCommand extends HystrixCommand<Boolean> {

    private final List<Evt> evts;

    public SuiviBoxCommand(final List<Evt> evts) {
        super(HystrixCommandGroupKey.Factory.asKey("SuiviBoxCommand"));
        this.evts = evts;
    }

    @Override
    @Timed
	protected Boolean run() throws Exception {
		// mise en place d'un flip de désactivation du suiviBox
		return FeatureFlips.INSTANCE.getBoolean("SuiviBox_Actif", true) ? SuiviBoxV1.getInstance().insertGC(evts):Boolean.TRUE;
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
        throw new MSTechnicalException(getFailedExecutionException());
    }
}