package com.chronopost.vision.microservices.updatereferentiel;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.updatereferentiel.DefinitionEvt;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * Commande mettant à jour les familles code_id_evt et evenements des transcodifications
 */
public class UpdateReferentielEvtCommand extends HystrixCommand<Boolean> {

	private final DefinitionEvt evt;
	private final ITranscoderDao dao;
    
    /**
     * Constructeur
     * @param evt un ensemble de champs utiles à la mise à jour
     * @param dao le transcodificateur utilisé
     */
	public UpdateReferentielEvtCommand(final DefinitionEvt evt, final ITranscoderDao dao) {
		super(HystrixCommandGroupKey.Factory.asKey("UpdateReferentielEvtCommand"));
		this.evt = evt;
		this.dao = dao;
	}

    @Override
    @Timed
    /*
     * (non-Javadoc)
     * @see com.netflix.hystrix.HystrixCommand#run()
     */
	protected Boolean run() throws Exception {
		dao.updateTransco("DiffusionVision", "code_id_evt", evt.getCodeProducerInput() + "|" + evt.getCodeEvtInput(),
				evt.getIdEvenement());
		dao.updateTransco("DiffusionVision", "evenements", evt.getIdEvenement(),
				evt.getCodeEvtInput() + "|" + evt.getPriorite() + "|" + evt.getLibVueCalculRetard() + "|"
						+ evt.getLivVueChronotrace() + "|" + evt.getLibEvt());
		return true;
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