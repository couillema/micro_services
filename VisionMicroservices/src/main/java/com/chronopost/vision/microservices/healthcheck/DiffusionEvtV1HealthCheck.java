package com.chronopost.vision.microservices.healthcheck;

import java.util.Arrays;
import java.util.Date;

import com.chronopost.vision.microservices.sdk.DiffusionEvtV1;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.diffusionevt.v1.DiffusionEvtResponse;
import com.codahale.metrics.health.HealthCheck;

public class DiffusionEvtV1HealthCheck extends HealthCheck {

	@Override
	protected Result check() throws Exception {
		Lt lt = new Lt().setNoLt("YYY99999999FR");
		Evt evt = new Evt().setNoLt("YYY99999999FR").setDateEvt(new Date()).setCodeEvt("DC").setPrioriteEvt(5000);
		lt.setEvenements(Arrays.asList(evt));
		
		DiffusionEvtResponse result = DiffusionEvtV1.getInstance().diffusionEvt(Arrays.asList(lt));
		
		if(result.getSuccess()){
			return Result.healthy();
		} 
		
		
		return Result.unhealthy("Le service DiffusionEvt ne repond pas correctement");
	}

}
