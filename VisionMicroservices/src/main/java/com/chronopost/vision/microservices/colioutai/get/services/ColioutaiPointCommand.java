package com.chronopost.vision.microservices.colioutai.get.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.model.DetailTournee;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.Point;
import com.chronopost.vision.model.colioutai.ColioutaiInfoLT;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class ColioutaiPointCommand extends HystrixCommand<Point>{
	
	private final static Logger logger = LoggerFactory.getLogger(ColioutaiPointCommand.class);
	
	private Point p;
	private DetailTournee detailTournee;
	private ColioutaiInfoLT infoLT;
	private ColioutaiServiceImpl service;
	private Date dateCalcul;

	protected ColioutaiPointCommand(Point p,DetailTournee detailTournee,ColioutaiInfoLT infoLT,ColioutaiServiceImpl service, Date dateCalcul) {
		super(HystrixCommandGroupKey.Factory.asKey("ColioutaiPointCommand"));
		this.p = p;
		this.detailTournee = detailTournee;
		this.infoLT = infoLT;
		this.service = service;
		this.dateCalcul = dateCalcul;
	}

	
	
	@Override
	@Timed
	protected Point run() throws Exception {
		
		logger.info("Colioutai point command no point : " + p.getNumeroPoint());
		
		List<Future<ColioutaiInfoLT>> calculLtFutures = new ArrayList<Future<ColioutaiInfoLT>>();
		
		for (Lt lt : p.getLtsDuPoint()) {
			
			calculLtFutures.add(new ColioutaiLtCommand(
					lt, detailTournee, infoLT,service).queue());
		}
		
		for (Future<ColioutaiInfoLT> calculLtFuture : calculLtFutures) {			
			ColioutaiInfoLT resultlt = calculLtFuture.get();		
			resultlt = service.computeEtatPoint(resultlt, dateCalcul);
			
			if (resultlt != null && resultlt.getEtatPoint() != null) {
				infoLT.getTourneePositionsColis().add(resultlt);
			}

			if( resultlt != null &&  resultlt.isRealise()) //BUG null pointer !! 
				p.setRealise(true);		

		}
		
		logger.info("Colioutai point command no point : " + p.getNumeroPoint() + " >>> OK >>> ");
		
		return p;
	}
	
	 @Override
	    public Point getFallback() {
	    	logger.info("Colioutai point command no point : " + p.getNumeroPoint() + " : " + getFailedExecutionException().getMessage(), getFailedExecutionException());
	        return p;
	    }

}
	
