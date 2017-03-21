package com.chronopost.vision.microservices.healthcheck;

import java.util.Arrays;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.chronopost.vision.microservices.sdk.UpdateTourneeV1;
import com.chronopost.vision.model.Evt;
import com.codahale.metrics.health.HealthCheck;

public class UpdateTourneeV1HealthCheck extends HealthCheck {
	DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyyMMdd");

    @Override
    protected Result check() throws Exception {
		String jour = dtfOut.print(new DateTime());

        boolean result = UpdateTourneeV1.getInstance()
                .updateTournee(
                        Arrays.asList(new Evt().setNoLt("NOLTBIDON_"+jour).setPrioriteEvt(1).setCodeEvt("DC")
                                .setDateEvt(new Date())));
        if (result == true) {
            return Result.healthy();
        }

        return Result.unhealthy("Le service UpdateTournee ne repond pas correctement");
    }

}
