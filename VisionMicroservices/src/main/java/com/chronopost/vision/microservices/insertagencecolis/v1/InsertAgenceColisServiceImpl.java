package com.chronopost.vision.microservices.insertagencecolis.v1;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.model.Agence;
import com.chronopost.vision.model.Evenement;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.model.rules.EvtRules;
import com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis;

/**
 * implémentation du service TraitementRetardService
 * 
 * @author LGY
 */
public enum InsertAgenceColisServiceImpl implements IInsertAgenceColisService {
    INSTANCE;

    /** logger */
    private final Logger log = LoggerFactory.getLogger(InsertAgenceColisServiceImpl.class);
    /** objet interface avec la bdd */
    private IInsertAgenceColisDao dao;
    /** objet cache de la table ref_evenement */
    private CacheManager<Evenement> cacheEvenement;
	/** objet cache de la table agence */
    private CacheManager<Agence> cacheAgence;


    @Override
    public IInsertAgenceColisService setDao(IInsertAgenceColisDao dao) {
        this.dao = dao;
        return this;
    }

    @Override
    public IInsertAgenceColisService setRefentielEvenement(CacheManager<Evenement> cacheEvenement) {
        this.cacheEvenement = cacheEvenement;
        return this;
    }
    
    @Override
    public IInsertAgenceColisService setRefentielAgence(CacheManager<Agence> cacheAgence) {
        this.cacheAgence = cacheAgence;
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.chronopost.vision.microservices.traitementRetard.TraitementRetardService
     * #genereRD(java.util.List<TraitementRetardInput>)
     */
    @Override
    public boolean traiteEvenement(List<Evt> evenements) throws InterruptedException, ExecutionException {

    	/* Liste des événements ayant une étape */
    	List<Evt> evtsTransport = new ArrayList<>();
    	List<EvtExclus> evtsExclus = new ArrayList<>();
    	 
    	boolean result = true;
    	
    	Evenement refEvt;
    	
    	/* On ne traite que les evts des colis non fictifs qui ont une étape, dont l'étape n'est pas transverse, et dont le lieu (agence) existe dans la referentiel agence */
    	for (Evt evt: evenements) {
    		if (!EvtRules.estUnColisAIgnorer(evt).booleanValue() && evt.getCodeEvt() != null && evt.getLieuEvt() != null && evt.getLieuEvt().length()>4){
    			if ((refEvt = cacheEvenement.getValue(evt.getCodeEvt())) != null && cacheAgence.getValue(evt.getLieuEvt()) != null){
    				/* Si l'evt a une étape */
    				if (!EEtapesColis.NONE.equals(refEvt.getEtape()) && !EEtapesColis.TRANSVERSE.equals(refEvt.getEtape())){
    					/* On l'ajoute à la liste des evts a mémoriser saisie dans l'agence */
						evtsTransport.add(evt);
    					/* Et si c'est un evt d'exclusion, pas la poste, on l'ajoute a la liste des evts a mémoriser a reconsidérer */
    					if (EvtRules.estUnEvtDExclusionPlusJ(evt)){
    						int nbreJourDExclusion = EvtRules.getNbreJoursDExclusion(evt);
    						Date excluJusquA = DateRules.getJourOuvrableAPartirDe(evt.getDateEvt(), nbreJourDExclusion);
    						evtsExclus.add(new EvtExclus(evt.getNoLt(), excluJusquA, evt.getLieuEvt()));
    					}
    				}
    				else { //TODO Ce else est à supprimer à terme. Il n'est utile ici que pour détecter les événements non encore déclarés dans le référentiel
    					if (!EEtapesColis.TRANSVERSE.equals(refEvt.getEtape()))
    							log.warn("Evenement "+evt.getCodeEvt()+" non défini ou sans étape dans le référentiel des evts");
    				}
    			}
    		}
    	}

    	/* Appel sauvegarde des saisis en agence */
    	result = result && dao.addColisInSaisisAgence(evtsTransport);
    	/* Appel sauvegarde des a saisir en agence */
    	result = result && dao.addColisInASaisirAgence(evtsExclus);

    	return result;
    }

	@Override
	public boolean setRestantTG2(final String agence, final String jour,final Set<String> noLts) {
		return dao.updateColisRestantTG2(agence, jour, noLts);
	}
	
    /**
     * Declare un appel au MS
     * @param nbTrt
     * @param NbFail
     */
    public void declareAppelMS(){
    	if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
    		dao.updateCptHitMS();
    	}
    }

    public void declareFailMS(){
    	if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
    		dao.updateCptFailMS();
    	}
    }
}
