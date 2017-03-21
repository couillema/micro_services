package com.chronopost.vision.microservices.genereevt;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.sdk.InsertEvtV1;
import com.chronopost.vision.model.Evt;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * Command Hystrix encapsulant l'appel au microservice Vision insertEvt
 * 
 * @author jcbontemps
 *
 */
public class InsertEvtCommand extends HystrixCommand<Boolean> {

	private static final Logger logger = LoggerFactory.getLogger(InsertEvtCommand.class);
	private final List<Evt> evts;

	public InsertEvtCommand(List<Evt> evts) {
		super(HystrixCommandGroupKey.Factory.asKey("InsertEvtCommand"));
		this.evts = evts;
	}

	@Override
	protected Boolean run() throws Exception {
		return InsertEvtV1.getInstance().insertEvt(evts);
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
		final Throwable failedExecutionException = getFailedExecutionException();
		logger.error(failedExecutionException.getMessage(), failedExecutionException);
		throw new MSTechnicalException(failedExecutionException);
	}
}
