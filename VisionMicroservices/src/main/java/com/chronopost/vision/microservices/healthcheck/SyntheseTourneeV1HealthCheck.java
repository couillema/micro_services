package com.chronopost.vision.microservices.healthcheck;

import com.chronopost.vision.microservices.sdk.SyntheseTourneeV1;
import com.codahale.metrics.health.HealthCheck;

public class SyntheseTourneeV1HealthCheck extends HealthCheck {

    @Override
    protected Result check() throws Exception {
        Object result = SyntheseTourneeV1.getInstance().getSyntheseTourneeActivite("12");
        if (result != null) {
            return Result.healthy();
        }

        return Result.unhealthy("Le service SyntheseTournee ne repond pas correctement");
    }

}
