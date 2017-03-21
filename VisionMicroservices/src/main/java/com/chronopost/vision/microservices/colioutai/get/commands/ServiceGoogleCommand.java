package com.chronopost.vision.microservices.colioutai.get.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.colioutai.get.services.GeoAdresse;
import com.chronopost.vision.microservices.colioutai.get.services.GoogleGeocoderHelper;
import com.chronopost.vision.model.Position;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class ServiceGoogleCommand extends HystrixCommand<Position> {

	private final static Logger logger = LoggerFactory
			.getLogger(ServiceGoogleCommand.class);

	private GoogleGeocoderHelper googleGeocoderHelper;
	private GeoAdresse adresse;

	public ServiceGoogleCommand(GoogleGeocoderHelper googleGeocoderHelper,GeoAdresse adresse) {
		super(HystrixCommandGroupKey.Factory.asKey("ServiceGoogleCommand"));
		this.googleGeocoderHelper = googleGeocoderHelper;
		this.adresse = adresse;
	}

	@Override
	@Timed
	protected Position run() throws Exception {
		logger.info("Service Google Command pour adresse " + adresse);		
		Position position = FeatureFlips.INSTANCE.getBoolean("Geocodage_Colioutai_Gmaps_Actif", true)?googleGeocoderHelper.geocodeFrom(adresse):null;
		logger.info("Service Google Command pour adresse " + adresse + " >>> OK >>> " );
		return position;
	}

	@Override
	public Position getFallback() {
		logger.warn("Erreur ServiceGoogleCommand pour adresse " + adresse + " : " + getFailedExecutionException().getMessage());
		return null;
	}
}
