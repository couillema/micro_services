package com.chronopost.vision.microservices.healthcheck;

import java.util.Date;

import com.chronopost.vision.microservices.sdk.GetCodeTourneeFromLtV1;
import com.chronopost.vision.microservices.sdk.exception.NotFoundException;
import com.chronopost.vision.model.GetCodeTourneeFromLTResponse;
import com.codahale.metrics.health.HealthCheck;

public class GetCodeTourneeFromLtV1HealthCheck extends HealthCheck {

    @Override
    protected Result check() throws Exception {
        try {
            GetCodeTourneeFromLTResponse result = GetCodeTourneeFromLtV1.getInstance().getCodeTourneeFromLt(
                    "NUMERO_DE_LT_BIDON", new Date());
            System.out.println("GetCodeTourneeFromLTResponse : " + result);
        } catch (NotFoundException e) {
            return Result.healthy();
        }

        return Result.unhealthy("Le service GetCodeTourneeFromLt ne repond pas correctement");
    }

}
