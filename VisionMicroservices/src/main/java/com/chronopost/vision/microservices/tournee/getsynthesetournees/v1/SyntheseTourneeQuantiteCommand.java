package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.model.getsynthesetournees.v1.SyntheseTourneeQuantite;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * Commande Hystrix appelant la méthode getOneSyntheseQuantiteTournee de la classe SyntheseTourneeServiceImpl
 * @author jcbontemps
 */
public class SyntheseTourneeQuantiteCommand extends HystrixCommand<SyntheseTourneeQuantite> {
	private static final Logger log = LoggerFactory.getLogger(SyntheseTourneeQuantiteCommand.class);
	
    private String idTournee;

    private SyntheseTourneeServiceImpl impl;

    /**
     * Constructeur
     * @param idTournee identifiant de la tournée
     * @param impl instance le la classe SyntheseTourneeServiceImpl appelée
     */
    public SyntheseTourneeQuantiteCommand(String idTournee, SyntheseTourneeServiceImpl impl) {
        super(HystrixCommandGroupKey.Factory.asKey("SyntheseTourneeQuantiteCommand"));
        this.idTournee = idTournee;
        this.impl = impl;
    }

    @Override
    @Timed
    /*
     * (non-Javadoc)
     * 
     * @see com.netflix.hystrix.HystrixCommand#run()
     */
    protected SyntheseTourneeQuantite run() throws Exception {
        return impl.getOneSyntheseQuantiteTournee(idTournee) ;
    }

    /*
     * Réponse en cas d'échec
     * 
     * (non-Javadoc)
     * 
     * @see com.netflix.hystrix.HystrixCommand#getFallback()
     */
    @Override
    public SyntheseTourneeQuantite getFallback() {
    	log.error("Erreur lors de la tentative de calcul de SyntheseTourneeQuantite pour la tournee '" + idTournee + "'", getFailedExecutionException());
    	return SyntheseTourneeQuantite.NONE;
//        throw new TechnicalException(getFailedExecutionException());
    }

}
