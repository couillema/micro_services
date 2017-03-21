package com.chronopost.vision.microservices.tournee.getalertestournees.v1.commands;

import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.sdk.GetDetailTourneeV1;
import com.chronopost.vision.model.DetailTournee;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class GetDetailTourneeCommand extends HystrixCommand<DetailTournee> {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(GetDetailTourneeCommand.class);

    private String codeTournee;

    public GetDetailTourneeCommand(String codeTournee) {
        super(HystrixCommandGroupKey.Factory.asKey("GetDetailTourneeCommand"));
        this.codeTournee = codeTournee;
    }

    @Override
    @Timed
    protected DetailTournee run() throws Exception {
        // Récupération des LTs en base pour en contrôler l'état et récupérer le
        // synonyme maître s'il existe
        if (codeTournee.length() > 0) {
            DetailTournee tournee = GetDetailTourneeV1.getInstance().getDetailTournee(codeTournee);
            return tournee;
        }

        return new DetailTournee().setCodeAgence(codeTournee.substring(0, 3)).setCodeTournee(codeTournee.substring(3));
    }

    /*
     * Réponse en cas d'échec
     * 
     * (non-Javadoc)
     * 
     * @see com.netflix.hystrix.HystrixCommand#getFallback()
     */
    @Override
    public DetailTournee getFallback() {
        Throwable e = getFailedExecutionException();
        logger.error("Erreur lors du GetDetailTournee", e);
        return new DetailTournee().setCodeAgence(codeTournee.substring(0, 3)).setCodeTournee(codeTournee.substring(3));
    }
}