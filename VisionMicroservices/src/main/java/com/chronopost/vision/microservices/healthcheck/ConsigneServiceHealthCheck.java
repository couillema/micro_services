package com.chronopost.vision.microservices.healthcheck;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.BindingProvider;

import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.codahale.metrics.health.HealthCheck;

import fr.chronopost.soap.consigne.cxf.ConsigneServiceWS;
import fr.chronopost.soap.consigne.cxf.ConsigneServiceWSService;
import fr.chronopost.soap.consigne.cxf.ResultInformationsConsigne;

public class ConsigneServiceHealthCheck extends HealthCheck {

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	@Override
	protected Result check() throws Exception {
		final Callable<Integer> consigneTask = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				final ConsigneServiceWS serviceConsigne = new ConsigneServiceWSService().getConsigneServiceWSPort();
				final BindingProvider bpConsigne = (BindingProvider) serviceConsigne;
				bpConsigne.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
						VisionMicroserviceApplication.getConfig().getEndpoints().get("consigne"));
				final ResultInformationsConsigne consigne = serviceConsigne
						.getAllInformationsColisConsigne("NUMERO_DE_LT_BIDON", false);
				return consigne.getCode();
			}
		};

		final Future<Integer> futureConsigne = executor.submit(consigneTask);

		if (futureConsigne.get(10, TimeUnit.SECONDS) == 0) {
			return Result.healthy();
		}

		return Result.unhealthy("Le service consigne ne repond pas correctement");
	}
}
