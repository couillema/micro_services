package com.chronopost.vision.microservices.healthcheck;

import java.util.ArrayList;

import com.chronopost.vision.microservices.sdk.GenereEvtV1;
import com.chronopost.vision.model.Evt;
import com.codahale.metrics.health.HealthCheck;

public class GenereEvtV1HealthCheck extends HealthCheck {

	@Override
	protected Result check() throws Exception {
		boolean result = GenereEvtV1.getInstance().genereEvt(new ArrayList<Evt>(), false);
		if(result == true){
			return Result.healthy();
		}

		return Result.unhealthy("Le service GenereEvt ne repond pas correctement");
	}

}
