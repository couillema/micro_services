package com.chronopost.vision.microservices.colioutai.get.services;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.model.DetailTournee;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.colioutai.ColioutaiInfoLT;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class ColioutaiLtCommand extends HystrixCommand<ColioutaiInfoLT> {
	
	private final static Logger logger = LoggerFactory.getLogger(ColioutaiLtCommand.class);
	
	private final DetailTournee detailTournee;
	private final ColioutaiInfoLT infoLT;
	private final Lt lt;
	private ColioutaiServiceImpl service;
	
	protected ColioutaiLtCommand(Lt lt,DetailTournee detailTournee,ColioutaiInfoLT infoLT,ColioutaiServiceImpl service) {
		super(HystrixCommandGroupKey.Factory.asKey("ColioutaiLtCommand"));
		this.lt = lt;
		this.detailTournee = detailTournee;		
		this.infoLT = infoLT;
		this.service = service;
		
	}

	@Override
	@Timed
	protected ColioutaiInfoLT run() throws Exception {
		
		logger.info("Colioutai LT Command no lt : " + lt.getNoLt());
		
		ColioutaiInfoLT infoLTTournee = service.mapLTtoInfoLT(lt, new Date());

		infoLTTournee = service.computeLTPoint(infoLTTournee, detailTournee);
	
		if (infoLTTournee.getNoLt().equals(infoLT.getNoLt())) {
			infoLTTournee.setCamionPositionTourneeList(infoLT.getCamionPositionTourneeList());
		}

		// rappel de la tournee par consistency
		infoLTTournee.setCodeTournee(detailTournee.getCodeTournee());
		
		logger.info("Colioutai LT Command no lt : " + lt.getNoLt() + " >>> OK >>> ");
	
		return infoLTTournee;
	}
	
	 @Override
	    public ColioutaiInfoLT getFallback() {
	    	logger.info("Colioutai LT Command no point : " + lt.getNoLt() + " : " + getFailedExecutionException().getMessage(), getFailedExecutionException());
	        return null;
	    }

}
