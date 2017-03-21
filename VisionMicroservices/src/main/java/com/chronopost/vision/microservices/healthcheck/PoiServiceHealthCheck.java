package com.chronopost.vision.microservices.healthcheck;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.BindingProvider;

import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.colioutai.get.services.GeoAdresse;
import com.chronopost.vision.microservices.colioutai.get.services.PoiGeocoderHelper;
import com.chronopost.vision.model.Position;
import com.codahale.metrics.health.HealthCheck;

import fr.chronopost.poi.webservice.PoiService;
import fr.chronopost.poi.webservice.impl.PoiWebService;

public class PoiServiceHealthCheck extends HealthCheck {

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	@Override
	protected Result check() throws Exception {
		final Callable<Position> geocodeTask = new Callable<Position>() {
			@Override
			public Position call() throws Exception {
				final PoiService servicePOI = new PoiWebService().getPoiServiceImplPort();
				final BindingProvider bpPOI = (BindingProvider) servicePOI;
				bpPOI.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
						VisionMicroserviceApplication.getConfig().getEndpoints().get("poi"));

				final PoiGeocoderHelper poiGeocoderHelper = PoiGeocoderHelper.getInstance(servicePOI);
				final GeoAdresse geoAdresse = new GeoAdresse("vivien", "de saint pern", "41 rue bernard palissy", null,
						"92500", "Rueil-Malmaison");
				return poiGeocoderHelper.geocodeFrom(geoAdresse);
			}
		};

		final Future<Position> positionFuture = executor.submit(geocodeTask);
		if (positionFuture.get(10, TimeUnit.SECONDS) != null) {
			return Result.healthy();
		}
		return Result.unhealthy("Le service poi ne repond pas correctement");
	}
}
