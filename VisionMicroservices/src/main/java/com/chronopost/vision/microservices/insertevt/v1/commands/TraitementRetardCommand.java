package com.chronopost.vision.microservices.insertevt.v1.commands;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.sdk.TraitementRetardV1;
import com.chronopost.vision.model.TraitementRetardInput;
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
public class TraitementRetardCommand extends HystrixCommand<Boolean> {

    /**
     * Log
     */
    private static final Logger logger = LoggerFactory.getLogger(TraitementRetardCommand.class);

    /** Liste des couples lt/calcul DLE à transmettre à TraitementRetard */
    private List<TraitementRetardInput> retards;

    /**
     * Instanciation de la commande avec les arguments nécessaire à son
     * execution.
     * 
     * @param endpoint
     *            : URL du micro service TraitementRetard
     * @param pRetards
     *            : Liste des couples lt/calcul DLE à transmettre à
     *            TraitementRetard
     */
    public TraitementRetardCommand(List<TraitementRetardInput> pRetards) {
        super(HystrixCommandGroupKey.Factory.asKey("TraitementRetardCommand"));
        this.retards = pRetards;
    }

    /**
     * L'execution ne prend pas de paramètres. Les paramètres sont donc
     * récupérés par le constructeur et transmis via les attribut de l'objet.
     */
    @Override
    @Timed
    protected Boolean run() throws Exception {
        // Traitement des retards
        Boolean status = TraitementRetardV1.getInstance().traitementRetard(retards);
        return status;
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
        logger.error("Erreur lors du TraitementRetard", e);
        return false;
        // throw new TechnicalException(getFailedExecutionException());
    }
}