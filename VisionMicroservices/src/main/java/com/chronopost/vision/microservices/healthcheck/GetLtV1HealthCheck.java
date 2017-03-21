package com.chronopost.vision.microservices.healthcheck;

import java.util.Arrays;
import java.util.Map;

import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.model.Lt;
import com.codahale.metrics.health.HealthCheck;

public class GetLtV1HealthCheck extends HealthCheck {

	@Override
	protected Result check() throws Exception {
		Map<String, Lt> result = GetLtV1.getInstance().getLt(Arrays.asList("NUMERO_DE_LT_BIDON"));
		if(result.isEmpty()){
			return Result.healthy();
		}
		
		
		return Result.unhealthy("Le service GetLt ne repond pas correctement");
	}

}
