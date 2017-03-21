package com.chronopost.vision.microservices.insertevt.v1.commands;

import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import fr.chronopost.soap.calculretard.cxf.CalculRetardWS;
import fr.chronopost.soap.calculretard.cxf.ResultCalculerRetardPourNumeroLt;

public class CalculRetardCommand extends HystrixCommand<ResultCalculerRetardPourNumeroLt> {

    private final static Logger logger = LoggerFactory.getLogger(CalculRetardCommand.class);

    private String numeroLt;
    private CalculRetardWS calculRetardClient;

    public CalculRetardCommand(String numeroLt, CalculRetardWS calculRetardClient)
            throws MalformedURLException {
        super(HystrixCommandGroupKey.Factory.asKey("CalculRetardCommand"));
        this.numeroLt = numeroLt;
        this.calculRetardClient = calculRetardClient;
    }

    @Override
    @Timed
    protected ResultCalculerRetardPourNumeroLt run() throws Exception {
        logger.debug("Calcul retard no_lt : " + numeroLt);
        ResultCalculerRetardPourNumeroLt result = calculRetardClient.calculerRetardPourNumeroLt(numeroLt, "false",
                "INF-CPE-PRO");
        logger.debug("Calcul retard no_lt : " + numeroLt + " >>> OK >>> " + result);
        return result;
    }

    /*
     * Réponse en cas d'échec
     * 
     * (non-Javadoc)
     * 
     * @see com.netflix.hystrix.HystrixCommand#getFallback()
     */
    @Override
    public ResultCalculerRetardPourNumeroLt getFallback() {
        // logger.warn("Erreur calculRetard " + numeroLt + " : " +
        // getFailedExecutionException().getMessage(),
        // getFailedExecutionException());
        return null;
    }
}