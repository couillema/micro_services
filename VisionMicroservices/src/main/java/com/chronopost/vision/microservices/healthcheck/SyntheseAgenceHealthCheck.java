package com.chronopost.vision.microservices.healthcheck;

import com.codahale.metrics.health.HealthCheck;

public class SyntheseAgenceHealthCheck extends HealthCheck {
    @Override
    protected Result check() throws Exception {
        // TODO 
        return Result.healthy();
    }
}
