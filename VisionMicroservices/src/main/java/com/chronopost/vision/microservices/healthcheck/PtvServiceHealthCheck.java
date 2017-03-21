package com.chronopost.vision.microservices.healthcheck;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.BindingProvider;

import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.codahale.metrics.health.HealthCheck;
import com.ptvgroup.chronopost.ws.xchrono.XChronoWS;
import com.ptvgroup.chronopost.ws.xchrono.XChronoWSService;

public class PtvServiceHealthCheck extends HealthCheck {

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	@Override
	protected Result check() throws Exception {
		final Callable<String> ptvTask = new Callable<String>() {
			@Override
			public String call() throws Exception {
				final XChronoWS servicePTV = new XChronoWSService().getXChronoWSPort();
				final BindingProvider bpPOI = (BindingProvider) servicePTV;
				bpPOI.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
						VisionMicroserviceApplication.getConfig().getEndpoints().get("ptv"));
				return servicePTV.etatModule();
			}
		};

		final Future<String> etatFuture = executor.submit(ptvTask);
		if (etatFuture.get(10, TimeUnit.SECONDS).equals("actif")) {
			return Result.healthy();
		}
		return Result.unhealthy("Le service PTV ne repond pas correctement");
	}
}
