package com.chronopost.vision.microservices.insertevt.v1.commands;

import java.net.MalformedURLException;

import javax.xml.ws.BindingProvider;

import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import fr.chronopost.soap.calculretard.cxf.CalculRetardServiceWS;
import fr.chronopost.soap.calculretard.cxf.CalculRetardWS;

public class InitCalculRetardCommand extends HystrixCommand<CalculRetardWS> {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(InitCalculRetardCommand.class);

    private String endpoint;

    public InitCalculRetardCommand(String endpoint) throws MalformedURLException {
        super(HystrixCommandGroupKey.Factory.asKey("InitCalculRetardCommand"));
        this.endpoint = endpoint;
    }

    @Override
    @Timed
    protected CalculRetardWS run() throws Exception {
        CalculRetardWS calculRetardService = new CalculRetardServiceWS().getCalculRetardWSPort();
        BindingProvider bpCalculRetard = (BindingProvider) calculRetardService;
        // (new URL(endpoint));
        bpCalculRetard.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.endpoint);
        // CalculRetardWS calculRetardClient =
        // calculRetardService.getCalculRetardWSPort();

        return calculRetardService;
    }

    /*
     * Réponse en cas d'échec
     * 
     * (non-Javadoc)
     * 
     * @see com.netflix.hystrix.HystrixCommand#getFallback()
     */
    @Override
    public CalculRetardWS getFallback() {
        try {
            logger.warn("Erreur initCalculRetard ",
                    getFailedExecutionException());
        } catch (Exception e) {
            logger.error("Initialisation du CalculRetard impossible");
        }
        return null;
    }
}