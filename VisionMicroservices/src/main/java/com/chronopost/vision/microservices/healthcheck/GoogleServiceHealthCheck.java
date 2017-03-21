package com.chronopost.vision.microservices.healthcheck;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.chronopost.vision.microservices.VisionMicroserviceConfiguration;
import com.chronopost.vision.microservices.colioutai.get.services.GeoAdresse;
import com.chronopost.vision.microservices.colioutai.get.services.GoogleGeocoderHelper;
import com.chronopost.vision.model.Position;
import com.codahale.metrics.health.HealthCheck;

public class GoogleServiceHealthCheck extends HealthCheck {

	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	@Override
	protected Result check() throws Exception {
		
		Callable<Position> geocodeTask = new Callable<Position>(){			
			@Override
			public Position call() throws Exception {
				VisionMicroserviceConfiguration configuration = new VisionMicroserviceConfiguration();
				GoogleGeocoderHelper helper = GoogleGeocoderHelper.getInstance(configuration.getProxyURL(), configuration.getProxyPort(), 10);
				GeoAdresse geoAdresse = new GeoAdresse("vivien","de saint pern", "41 rue bernard palissy", null, "92500", "Rueil-Malmaison");
				return helper.geocodeFrom(geoAdresse);
			}
			
		};
		
		Future<Position> futurePosition = executor.submit(geocodeTask);
		
		if(futurePosition.get(10, TimeUnit.SECONDS) != null){
			return Result.healthy();
		}
		return Result.unhealthy("Le service google ne repond pas correctement");
	}

}
