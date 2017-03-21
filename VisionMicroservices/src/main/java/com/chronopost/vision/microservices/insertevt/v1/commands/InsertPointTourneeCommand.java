package com.chronopost.vision.microservices.insertevt.v1.commands;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.sdk.InsertPointTourneeV1;
import com.chronopost.vision.model.Evt;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * Encapsulation de l'appel au Micro Service TraitementRetard dans une commande
 * Histryx.
 * 
 * @author lguay
 *
 */
public class InsertPointTourneeCommand extends HystrixCommand<Boolean> {

    /**
     * Log 
     */
	private static final Logger logger = LoggerFactory.getLogger(InsertPointTourneeCommand.class);

    /**
     * Liste des evenements à traiter pour maintenir les points visités par les
     * tournées
     */
    private List<Evt> evenements;

    /**
     * Instanciation de la commande avec les arguments nécessaire à son
     * execution.
     * 
     * @param pRetards
     *            : Liste des événements à traiter
     */
    public InsertPointTourneeCommand(final List<Evt> pEvenements) {
        super(HystrixCommandGroupKey.Factory.asKey("InsertPointTourneeCommand"));
        this.evenements = pEvenements;
    }

    /**
     * L'execution ne prend pas de paramètres. Les paramètres sont donc
     * récupérés par le constructeur et transmis via les attribut de l'objet.
     */
    @Override
    @Timed
    protected Boolean run() throws Exception {
        // Traitement des retards
        return InsertPointTourneeV1.getInstance().insertPointTournee(evenements);
    }

    /*
     * Réponse en cas d'échec
     * 
     * (non-Javadoc)
     * 
     * @see com.netflix.hystrix.HystrixCommand#getFallback()
     */
    @Override
    public Boolean getFallback() {
        Throwable e = getFailedExecutionException();
        logger.error("Erreur lors du InsertPointTournee", e);
        return Boolean.FALSE;
    }
}