package com.chronopost.vision.microservices.tournee.updatetournee.v1.commands;

import java.util.List;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.tournee.updatetournee.v1.IUpdateTourneeDao;
import com.chronopost.vision.model.Evt;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * Commande appelant la méthode updateTourneeCodeService d'un ITourneeDao
 */
public class UpdateTourneeCodeServiceCommand extends HystrixCommand<Boolean> {

	private List<Evt> evts;
	private IUpdateTourneeDao dao;

	public UpdateTourneeCodeServiceCommand(final IUpdateTourneeDao dao, final List<Evt> evts) {
		super(HystrixCommandGroupKey.Factory.asKey("UpdateTourneeCodeServiceCommand"));
		this.evts = evts;
		this.dao = dao;
	}

	@Override
	@Timed
	protected Boolean run() throws Exception {
		
		return dao.updateTourneeCodeService(this.evts);

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