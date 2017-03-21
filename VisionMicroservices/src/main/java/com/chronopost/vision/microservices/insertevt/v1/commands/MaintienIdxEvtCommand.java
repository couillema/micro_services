package com.chronopost.vision.microservices.insertevt.v1.commands;

import java.util.Collection;
import java.util.List;

import org.assertj.core.util.Lists;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.sdk.MaintienIndexEvtV1;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.Lt;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import fr.chronopost.soap.calculretard.cxf.ResultCalculerRetardPourNumeroLt;

public class MaintienIdxEvtCommand extends HystrixCommand<Boolean> {

    private List<Evt> evts;
    private Lt lt;
    private ResultCalculerRetardPourNumeroLt resultatCalculRetard;

    public MaintienIdxEvtCommand(Lt lt, Collection<Evt> evts, ResultCalculerRetardPourNumeroLt resultatCalculRetard) {
        super(HystrixCommandGroupKey.Factory.asKey("MaintienIdxEvtCommand"));
        this.evts = Lists.newArrayList();
        this.evts.addAll(evts);
        this.lt = lt;
        this.resultatCalculRetard = resultatCalculRetard;
    }

    @Override
    @Timed
    protected Boolean run() throws Exception {
        if (FeatureFlips.INSTANCE.getBoolean("Maintien_Index_Evt_Actif", false)) {
            return MaintienIndexEvtV1.getInstance().maintienIndexEvt(lt, evts, resultatCalculRetard).getSuccess();
        }

        return Boolean.TRUE;
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