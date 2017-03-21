package com.chronopost.vision.microservices.updatereferentiel;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * Commande mettant à jour la famille id_infoscomp des transcodifications
 */
public class UpdateReferentielInfoscompCommand extends HystrixCommand<Boolean> {

    private String cle;
    private String valeur;
    private ITranscoderDao dao;

    /**
     * Constructeur
     * @param cle clé de l'entrée à mettre à jour
     * @param valeur nouvelle valeur associée à l'entrée
     * @param dao le transcodificateur utilisé
     */
    public UpdateReferentielInfoscompCommand(String cle, String valeur, ITranscoderDao dao) {
        super(HystrixCommandGroupKey.Factory.asKey("UpdateReferentielInfoscompCommand"));
        this.cle = cle ;
        this.valeur = valeur ;
        this.dao = dao ;
    }

    @Override
    @Timed
/*
 * (non-Javadoc)
 * @see com.netflix.hystrix.HystrixCommand#run()
 */
    protected Boolean run() throws Exception {
        this.dao.updateTransco("DiffusionVision","id_infocomp", this.cle, this.valeur);
        return true ;
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
        throw new MSTechnicalException(getFailedExecutionException());
    }
}