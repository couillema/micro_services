package com.chronopost.vision.microservices.healthcheck;

import com.chronopost.vision.microservices.sdk.GetDetailTourneeV1;
import com.codahale.metrics.health.HealthCheck;

public class GetDetailTourneeV1HealthCheck extends HealthCheck {

    @Override
    protected Result check() throws Exception {
        try {
            GetDetailTourneeV1.getInstance().getDetailTournee("CODE_TOURNEE_BIDON");
            return Result.healthy();
        } catch (Exception e) {
            return Result.unhealthy("Le service GetDetailTournee ne repond pas correctement");
        }

    }

}
