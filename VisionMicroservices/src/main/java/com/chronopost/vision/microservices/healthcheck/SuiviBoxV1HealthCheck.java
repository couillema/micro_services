package com.chronopost.vision.microservices.healthcheck;

import java.util.ArrayList;

import com.chronopost.vision.microservices.sdk.SuiviBoxV1;
import com.chronopost.vision.model.Evt;
import com.codahale.metrics.health.HealthCheck;

public class SuiviBoxV1HealthCheck extends HealthCheck {

    @Override
    protected Result check() throws Exception {
        boolean result = SuiviBoxV1.getInstance().insertGC(new ArrayList<Evt>());
        if (result == true) {
            return Result.healthy();
        }

        return Result.unhealthy("Le service SuiviBox ne repond pas correctement");
    }

}
