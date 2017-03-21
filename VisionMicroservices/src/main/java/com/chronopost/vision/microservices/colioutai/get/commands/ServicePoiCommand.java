package com.chronopost.vision.microservices.colioutai.get.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.colioutai.get.services.GeoAdresse;
import com.chronopost.vision.microservices.colioutai.get.services.PoiGeocoderHelper;
import com.chronopost.vision.model.Position;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class ServicePoiCommand extends HystrixCommand<Position> {
	
	private final static Logger logger = LoggerFactory.getLogger(ServicePoiCommand.class);
	
	private PoiGeocoderHelper poiGeocoderHelper;
	private GeoAdresse adresse;

	public ServicePoiCommand(PoiGeocoderHelper poiGeocoderHelper,GeoAdresse adresse) {
		super(HystrixCommandGroupKey.Factory.asKey("ServicePoiCommand"));
		this.poiGeocoderHelper = poiGeocoderHelper;
		this.adresse = adresse;
	}

	@Override
	@Timed
	protected Position run() throws Exception {
		logger.info("Service Poi Command pour adresse " + adresse);
		Position position = FeatureFlips.INSTANCE.getBoolean("Geocodage_Colioutai_POI_Actif", true)?poiGeocoderHelper.geocodeFrom(adresse):null;
		logger.info("Service Poi Command pour adresse " + adresse + " >>> OK >>> " );
		return position;
	}
	
	@Override
	public Position getFallback() {
		logger.warn("Erreur ServicePoiCommand pour adresse " + adresse + " : ");
		return null;
	}
}
