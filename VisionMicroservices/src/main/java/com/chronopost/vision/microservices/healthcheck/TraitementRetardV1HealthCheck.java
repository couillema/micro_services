package com.chronopost.vision.microservices.healthcheck;

import java.util.ArrayList;

import com.chronopost.vision.microservices.sdk.TraitementRetardV1;
import com.chronopost.vision.model.TraitementRetardInput;
import com.codahale.metrics.health.HealthCheck;

public class TraitementRetardV1HealthCheck extends HealthCheck {

    @Override
    protected Result check() throws Exception {
        boolean result = TraitementRetardV1.getInstance().traitementRetard(new ArrayList<TraitementRetardInput>());
        if (result == true) {
            return Result.healthy();
        }

        return Result.unhealthy("Le service TraitementRetard ne repond pas correctement");
    }

}
