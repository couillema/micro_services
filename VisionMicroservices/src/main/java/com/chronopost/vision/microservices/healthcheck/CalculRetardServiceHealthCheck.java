package com.chronopost.vision.microservices.healthcheck;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.BindingProvider;

import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.codahale.metrics.health.HealthCheck;

import fr.chronopost.soap.calculretard.cxf.CalculRetardServiceWS;
import fr.chronopost.soap.calculretard.cxf.CalculRetardWS;
import fr.chronopost.soap.calculretard.cxf.ResultCalculerRetardPourNumeroLt;

public class CalculRetardServiceHealthCheck extends HealthCheck {

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	@Override
	protected Result check() throws Exception {
		final Callable<ResultCalculerRetardPourNumeroLt> calculRetardTask = new Callable<ResultCalculerRetardPourNumeroLt>() {
			@Override
			public ResultCalculerRetardPourNumeroLt call() throws Exception {
				final CalculRetardWS calculRetardService = new CalculRetardServiceWS().getCalculRetardWSPort();
				final BindingProvider bpCalculRetard = (BindingProvider) calculRetardService;
				bpCalculRetard.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
						VisionMicroserviceApplication.getConfig().getEndpoints().get("calculRetard"));
				return calculRetardService.calculerRetardPourNumeroLt("NUMERO_DE_LT_BIDON", "false", "1");
			}
		};

		final Future<ResultCalculerRetardPourNumeroLt> futureCalculRetardResult = executor.submit(calculRetardTask);

		if (futureCalculRetardResult.get(10, TimeUnit.SECONDS) != null) {
			return Result.healthy();
		}
		return Result.unhealthy("Le service calcul retard ne repond pas correctement");
	}
}
