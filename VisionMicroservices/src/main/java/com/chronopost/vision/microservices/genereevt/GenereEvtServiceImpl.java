/**
 * 
 */
package com.chronopost.vision.microservices.genereevt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Evt;

import fr.chronopost.sgesws.cxf.SGESServiceWS;

/**
 * Service de Génération des Evts dans le SI Vision Les evts vont être transmis
 * au webservice (SOAP) SGES si l'option injectionVision est positive, on insère
 * également directement dans la base Cassandra Vision en parallèle
 * 
 * @author jcbontemps
 */
public class GenereEvtServiceImpl implements IGenereEvtService {

    private static final Logger logger = LoggerFactory.getLogger(GenereEvtServiceImpl.class);
    private static final String GENERE_EVT_INJECTION_VISION = "Genere_Evt_Injection_Vision" ;
    private String endpoint;
    private Mapper mapper ;
    private SGESServiceWS sgesService;
    
    /**
     * Constructeur
     */
    private GenereEvtServiceImpl() {    	
    }

    /**
     * Singleton
     */
    static class InstanceHolder {
        public static GenereEvtServiceImpl service = new GenereEvtServiceImpl();
    }

    /**
     * Singleton
     * 
     * @return
     */
    public static GenereEvtServiceImpl getInstance() {
        return InstanceHolder.service;
    }

    public void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
        initSgesService();
    }

    public void setMapper(final Mapper mapper) {
        this.mapper = mapper;
    }

    /*
     * 
     * Insère les evts dans le SI en lançant en parallèle des appels au webservice SGES. 
     * Puis - éventuellement - les insère en base C* Vision au moyen du microservice insertEvt
     * Cette fonctionnalité peut être commandée de l'extérieur par un booléen
     * mais aussi par le featureflip Genere_Evt_Injection_Vision
     * (non-Javadoc)
     * 
     * @see
     * com.chronopost.vision.microservices.genereevt.IGenereEvtService#genereEvt
     * (java.util.List, java.lang.Boolean)
     */
    @Override
	public Map<Evt, Boolean> genereEvt(final List<Evt> evts, final Boolean injectionVision)
			throws MSTechnicalException, FunctionalException {
		final Map<Evt, Boolean> genereEvt = new HashMap<Evt, Boolean>();
		try {
			// Initialisation du webservice
			initSgesService();

			// appels parallélisés du WS SGES
			final Map<Evt, Future<Boolean>> futuresSGES = new HashMap<Evt, Future<Boolean>>();
			for (final Evt evt : evts) {
				final GenererEvtDTO dto = mapper.evtToDto(evt);
				final Future<Boolean> sgescommand = new SGESCommand(dto, sgesService).queue();
				futuresSGES.put(evt, sgescommand);
			}

			// enregistrement des résultats de l'insertion par SGES
			for (final Evt evt : futuresSGES.keySet()) {
				final Future<Boolean> futur = futuresSGES.get(evt);
				genereEvt.put(evt, futur.get());
			}

		} catch (final InterruptedException e) {
			throw new MSTechnicalException(
					"Une erreur de type InterruptedException est intervenue dans com.chronopost.vision.microservices.genereevt.GenereEvtServiceImpl.genereEvt",
					e);
		} catch (final ExecutionException e) {
			throw new MSTechnicalException(
					"Une erreur de type ExecutionException est intervenue dans com.chronopost.vision.microservices.genereevt.GenereEvtServiceImpl.genereEvt",
					e);
		}

		try {
			if (injectionVision && FeatureFlips.INSTANCE.getBoolean(GENERE_EVT_INJECTION_VISION, true))
				new InsertEvtCommand(evts).execute();
		} catch (final Exception e) {
			logger.info("une exception a été levée pour InsertEvtCommand. Celle-ci est ignorée : " + e.getMessage());
		}

		return genereEvt;
	}
    
    /**
     * Initialisation des appels au web service SGES. 
     */
	private void initSgesService() {
		if (sgesService == null) {
			final CreateSGESCommand createSGESCommand = new CreateSGESCommand(endpoint);
			sgesService = createSGESCommand.execute();
		}
	}
}
