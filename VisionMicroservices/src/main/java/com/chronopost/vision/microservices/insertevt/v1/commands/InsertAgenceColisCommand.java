package com.chronopost.vision.microservices.insertevt.v1.commands;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.sdk.InsertAgenceColisV1;
import com.chronopost.vision.model.Evt;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * Encapsulation de l'appel au Micro Service InsertAgenceColis dans une commande
 * Histryx.
 * 
 * @author LGY
 *
 */
public class InsertAgenceColisCommand extends HystrixCommand<Boolean> {

    /**
     * Log
     */
	private static final Logger logger = LoggerFactory.getLogger(InsertAgenceColisCommand.class);

    /**
     * Liste des evenements à traiter pour maintenir les colis par agence
     */
    private List<Evt> evenements;

    /**
     * Instanciation de la commande avec les arguments nécessaire à son
     * execution.
     * 
     * @param pRetards
     *            : Liste des événements à traiter
     */
    public InsertAgenceColisCommand(List<Evt> pEvenements) {
        super(HystrixCommandGroupKey.Factory.asKey("InsertColisAgenceCommand"));
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
        Boolean status = InsertAgenceColisV1.getInstance().insertAgenceColis(evenements);
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
        logger.error("Erreur lors du InsertAgenceColis", e);
        return false;
    }
}