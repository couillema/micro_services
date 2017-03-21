package com.chronopost.vision.microservices.tournee.updatetournee.v1.commands;

import java.util.List;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.tournee.updatetournee.v1.IUpdateTourneeDao;
import com.chronopost.vision.model.Evt;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * Commande appelant la méthode updateTournee d'un ITourneeDao
 */
public class UpdateTourneeDaoCommand extends HystrixCommand<Boolean> {

	private List<Evt> evts;
	private IUpdateTourneeDao dao;

	public UpdateTourneeDaoCommand(final IUpdateTourneeDao dao, final List<Evt> evts) {
		super(HystrixCommandGroupKey.Factory.asKey("UpdateTourneeDaoCommand"));
		this.evts = evts;
		this.dao = dao;
	}

	@Override
	@Timed
	protected Boolean run() throws Exception {
		
		return dao.updateTournee(this.evts);

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