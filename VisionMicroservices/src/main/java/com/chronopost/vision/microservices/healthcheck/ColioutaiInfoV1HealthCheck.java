package com.chronopost.vision.microservices.healthcheck;

import com.chronopost.vision.microservices.sdk.ColioutaiInfoV1;
import com.chronopost.vision.microservices.sdk.exception.NotFoundException;
import com.codahale.metrics.health.HealthCheck;

public class ColioutaiInfoV1HealthCheck extends HealthCheck {

    @Override
    protected Result check() throws Exception {
        try {
            ColioutaiInfoV1.getInstance().colioutaiInfoLT("NUMERO_DE_LT_BIDON");
        } catch (NotFoundException e) {
            return Result.healthy();
        }

        return Result.unhealthy("Le service ColioutaiInfoV1 ne repond pas correctement");
    }

}
