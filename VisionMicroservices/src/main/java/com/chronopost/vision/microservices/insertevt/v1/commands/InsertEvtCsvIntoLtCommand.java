package com.chronopost.vision.microservices.insertevt.v1.commands;

import java.util.List;

import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.sdk.InsertLtV1;
import com.chronopost.vision.model.Lt;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class InsertEvtCsvIntoLtCommand extends HystrixCommand<Boolean> {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(InsertEvtCsvIntoLtCommand.class);
    private List<Lt> lts;

    public InsertEvtCsvIntoLtCommand(List<Lt> lts) {
        super(HystrixCommandGroupKey.Factory.asKey("InsertEvtCsvIntoLtCommand"));
        this.lts = lts;
    }

    @Override
    @Timed
    protected Boolean run() throws Exception {
        // Mise à jour des événements dans les LT correspondantes (champ evts)
        return InsertLtV1.getInstance().insertLt(lts);
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
		logger.error("Erreur InsertEvtCsvIntoLtCommand : " + getFailedExecutionException().getMessage(),
				getFailedExecutionException());
		throw new MSTechnicalException(getFailedExecutionException());
	}
}