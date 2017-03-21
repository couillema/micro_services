package com.chronopost.vision.microservices.healthcheck;

import java.util.Arrays;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.chronopost.vision.microservices.sdk.InsertPointTourneeV1;
import com.chronopost.vision.model.Evt;
import com.codahale.metrics.health.HealthCheck;

public class InsertPointTourneeV1HealthCheck extends HealthCheck {
	DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyyMMdd");

	@Override
	protected Result check() throws Exception {
		String jour = dtfOut.print(new DateTime());
		Boolean result = InsertPointTourneeV1.getInstance().insertPointTournee(Arrays.asList(new Evt().setNoLt("NOLTBIDON_"+jour).setPrioriteEvt(new Integer(1)).setCodeEvt("DC").setDateEvt(new Date())));
		if(result.booleanValue() == true){
			return Result.healthy();
		}

		return Result.unhealthy("Le service InsertPointTournee ne repond pas correctement");
	}

}
