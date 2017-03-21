package com.chronopost.vision.microservices.updatespecificationscolis.v1;

import static com.chronopost.vision.model.EInfoComp.CODE_REGATE_EMMETEUR;
import static com.chronopost.vision.model.EInfoComp.CODE_TRANSPORT;
import static com.chronopost.vision.model.EInfoComp.ID_CONTENANT_PROPAG;
import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.ACQUITTEMENT_LIVRAISON;
import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.EXCLUSION;
import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.INCIDENT;
import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.TRANSVERSE;
import static com.chronopost.vision.model.updatespecificationscolis.v1.ESpecificiteColis.BOX;
import static com.chronopost.vision.model.updatespecificationscolis.v1.ESpecificiteColis.SAC;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.updatespecificationscolis.v1.command.TraitementCodeServiceCommand;
import com.chronopost.vision.microservices.updatespecificationscolis.v1.command.TraitementConsigneCommand;
import com.chronopost.vision.microservices.updatespecificationscolis.v1.command.TraitementEvenementCommand;
import com.chronopost.vision.model.CodeService;
import com.chronopost.vision.model.Evenement;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.model.rules.EvtRules;
import com.chronopost.vision.model.updatespecificationscolis.v1.ESpecificiteColis;
import com.chronopost.vision.model.updatespecificationscolis.v1.EvtEtModifs;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;

/**
 * implémentation du service UpdateSpecificationsColisService
 * 
 * @author LGY
 */
public class UpdateSpecificationsColisServiceImpl implements IUpdateSpecificationsColisService {

	private static final Logger logger = LoggerFactory.getLogger(UpdateSpecificationsColisServiceImpl.class);

	private final UpdateSpecificationsUtils utils = new UpdateSpecificationsUtils();

	private IUpdateSpecificationsTranscoder transcoderConsignes;

	/** Cache du referentiel codes services */
	private CacheManager<CodeService> cacheCodeService;

	/** Cache du référentiel evenements */
	private CacheManager<Evenement> cacheEvenement;

	private IUpdateSpecificationsColisDao dao;

    /**
     * Singleton
     */
    static class InstanceHolder {
        public static final UpdateSpecificationsColisServiceImpl service = new UpdateSpecificationsColisServiceImpl();
    }

    /**
     * Singleton
     * 
     * @return
     */
    public static UpdateSpecificationsColisServiceImpl getInstance() {
        return InstanceHolder.service;
    }

    /**
     * Constructeur
     */
    private UpdateSpecificationsColisServiceImpl() {
    }

    /**
     * @param dao IUpdateSpecificationsColisDao de ce service
     * @return cet objet UpdateSpecificationsColisServiceImpl
     */
    public UpdateSpecificationsColisServiceImpl setDao(final IUpdateSpecificationsColisDao dao) {
        this.dao = dao;
        return this;
    }

    /**
     * Met à jour le transcodeur des Consignes
     * 
     * @param transcoder IUpdateSpecificationsTranscoder de ce service
     * @return cet objet UpdateSpecificationsColisServiceImpl
     */
    public UpdateSpecificationsColisServiceImpl setTranscoderConsignes(final IUpdateSpecificationsTranscoder transcoder) {
        this.transcoderConsignes = transcoder;
        return this;
    }

    /**
     * Méthode d'injection du cache du referentiel Code Service
     * 
     * @param transcoder IUpdateSpecificationsTranscoder de ce service
     * @return cet objet UpdateSpecificationsColisServiceImpl
     */
    public UpdateSpecificationsColisServiceImpl setRefentielCodeService(final CacheManager<CodeService> cacheManager) {
        this.cacheCodeService = cacheManager;
        return this;
    }

    /**
     * Méthode d'injection du cache du referentiel Evenement
     * 
     * @param transcoder IUpdateSpecificationsTranscoder de ce service
     * @return cet objet UpdateSpecificationsColisServiceImpl
     */
    public UpdateSpecificationsColisServiceImpl setRefentielEvenement(final CacheManager<Evenement> cacheManager) {
        this.cacheEvenement = cacheManager;
        return this;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisService#traiteEvenement
     * (java.util.List)
     */
    @Override
    public boolean traitementSpecificationsColis(final List<EvtEtModifs> evts) {
    	final List<Future<Boolean>> updated = new ArrayList<>();
        boolean result = true;

        updated.add(new TraitementCodeServiceCommand(evts).queue());
        updated.add(new TraitementConsigneCommand(evts).queue());
        updated.add(new TraitementEvenementCommand(evts).queue());

		for (final Future<Boolean> waiter : updated) {
			try {
				if (waiter != null && waiter.get()!= null)
					result = result && waiter.get().booleanValue();
				else
					result = false;
					
			} catch (InterruptedException | ExecutionException e) {
				logger.error("Erreur traitementSpecificationsColis : " + e.getMessage());
				result = false;
			}
		}
        return result;
    }

    /**
     * Traitement des spécifications portées par le code Service
     * 
     * @param evts
     * @return un boolean a <true> si tout le traitement s’est bien déroulé
     */
    public boolean traitementCodeService(final @NotNull List<EvtEtModifs> evtEtModifs) {
        /**
         * Pour chaque événement de evts récupérer socode (et ascode) du colis/événement à partir socode+ascode
         * récupérer <spécifications> (via la Map des services) creer un objet colis de type SpecifsColis pour le colis
         * evt.no_lt ajouter dans colis.specifservice<evt.date> =<spécifications> ajouter ce colis à la liste LstSC
         */
    	final List<SpecifsColis> lstSC = new ArrayList<>();
    	MSTechnicalException lastError = null;
    	boolean result = false;
    	int nbTrtFail = 0;
    	
        for (final EvtEtModifs evtEtModif : evtEtModifs) {
        	try{
        	final Evt evt = evtEtModif.getEvt();
            if (evt.getDateEvt() != null && evt.getCodeService() != null
                    && cacheCodeService.getValue(evt.getCodeService()) != null
                    && cacheCodeService.getValue(evt.getCodeService()).getSpecifs() != null
                    && !cacheCodeService.getValue(evt.getCodeService()).getSpecifs().isEmpty()) {
            	final SpecifsColis colis = new SpecifsColis();
                colis.setNoLt(evt.getNoLt());
                colis.getSpecifsService().put(evt.getDateEvt(), cacheCodeService.getValue(evt.getCodeService()).getSpecifs());
                colis.addService(evt.getDateEvt(), evt.getCodeService());
                lstSC.add(colis);
            }
        	} catch (Exception e) {
        		logger.error("Erreur dans traitementCodeService : ",e);
    			dao.declareErreur(evtEtModif.getEvt(), "traitementCodeService", e);
        		if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
        			lastError = new MSTechnicalException(e);
        			nbTrtFail++;
        		}
        		else
        			throw e;
    		}
        }
        
        try{
        	 result = dao.updateSpecifsServices(lstSC);
        } catch (MSTechnicalException e) {
    		logger.error("Erreur dans traitementCodeService : ",e);
    		if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
    			lastError = e;
    		}
    		else
    			throw e;
		}
        
        if (lastError != null){
        	dao.updateCptTrtTrtFailMS(0, nbTrtFail);
        	throw lastError;
        }
        
        return  result;
    }

    /**
     * Traitement des événements pour en extraire les consignes portées par les infoscomp
     * 
     * @param evts la liste des événements fournis par insertEvt
     * @return un boolean a <true> si tout le traitement s’est bien déroulé
     */
    public boolean traitementConsigne(final List<EvtEtModifs> evtEtModifs) {
    	// Pour chaque événement de List<evt>
    	//      si code evt = ‘CL’
    	//          transcoder la consigne (infocomp 175) via le mapping_1 ⇒ valeurConsigne
    	//          si valeurConsigne définie si valeurConsigne !=ANNUL
    	//              creer un objet colis de type SpecifsColis pour le colis evt.no_lt
    	//              ajouter dans colis.consignesRecues<evt.date> = identifiantConsigne + “|” + valeurConsigne
    	//          sinon
    	//              creer un objet colis de type SpecifsColis pour le colis evt.no_lt
    	//              ajouter dans colis.consignesAnnulees<evt.date> = identifiantConsigne + “|” + valeurConsigne
    	//      si code evt = ‘CT’:
    	//          transcoder la consigne (infocomp 185) via le mapping_2 ⇒ valeurConsigne
    	//          si valeurConsigne définie
    	//              creer un objet colis de type SpecifsColis pour le colis evt.no_lt
    	//              ajouter dans colis.consignestraitees<evt.date> = identifiantConsigne + “|” + valeurConsigne
    	//      si code evt = ‘I’:
    	//          transcoder la consigne (infocomp 56) via le mapping_3 ⇒ valeurConsigne
    	//          si valeurConsigne définie
    	//              creer un objet colis de type SpecifsColis pour le colis evt.no_lt
    	//              ajouter dans colis.consignestraitees<evt.date> = “0|” + valeurConsigne
    	// persister les consignes en base : <dao.updateConsignes>

    	final List<SpecifsColis> lstSC = new ArrayList<>();
    	MSTechnicalException lastError = null;
    	boolean result = false;
    	int nbTrtFail = 0;
    	
    	for (final EvtEtModifs evtEtModif : evtEtModifs) {
    		try{
    			final Evt evt = evtEtModif.getEvt();
    			final SpecifsColis colis = new SpecifsColis();
    			colis.setNoLt(evt.getNoLt());
    			if (evt.getDateEvt() != null) {
    				if (utils.isConsignesRecues(evt) && transcoderConsignes.transcode(utils.extractConsigne(evt)) != null) {
    					colis.getConsignesRecues().put(evt.getDateEvt(), utils.extractIdConsigne(evt) + "|"
    							+ transcoderConsignes.transcode(utils.extractConsigne(evt)).getCode());
    					lstSC.add(colis);
    				}
    				if (utils.isConsignesAnnulees(evt)) {
    					colis.getConsignesAnnulees().put(evt.getDateEvt(), utils.extractIdConsigne(evt));
    					lstSC.add(colis);
    				}
    				if (utils.isConsignesTraiteesCT(evt)
    						&& transcoderConsignes.transcode(utils.extractConsigne(evt)) != null) {
    					colis.getConsignesTraitees().put(evt.getDateEvt(), utils.extractIdConsigne(evt) + "|"
    							+ transcoderConsignes.transcode(utils.extractConsigne(evt)).getCode());
    					lstSC.add(colis);
    				}
    				if (utils.isConsignesTraiteesI(evt)
    						&& transcoderConsignes.transcode(utils.extractConsigne(evt)) != null) {
    					colis.getConsignesTraitees().put(evt.getDateEvt(),
    							"0|" + transcoderConsignes.transcode(utils.extractConsigne(evt)).getCode());
    					lstSC.add(colis);
    				}
    			}
    		} catch (Exception e) {
    			logger.error("Erreur dans traitementConsigne : ",e);
				dao.declareErreur(evtEtModif.getEvt(),"traitementConsigne", e);
    			if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
    				lastError = new MSTechnicalException(e);
    				nbTrtFail++;
    			}
    			else
    				throw e;
    		}
    	}

    	try{
    		result = dao.updateConsignes(lstSC);
    	} catch (MSTechnicalException e) {
    		logger.error("Erreur dans traitementCodeService : ",e);
    		if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
    			lastError = e;
    		}
    		else
    			throw e;
    	}

    	if (lastError != null){
        	dao.updateCptTrtTrtFailMS(0, nbTrtFail);
    		throw lastError;
    	}

    	return  result;
    }

    /**
     * Traitement des événements pour en extraire les spécificités portées par le code événement lui-même ainsi que les
     * étapes et les dates contractuelles
     * 
     * @param evts la liste des événements fournis par insertEvt
     * @return un boolean a <true> si tout le traitement s’est bien déroulé
     * @throws ParseException
     */
    public boolean traitementEvenement(final List<EvtEtModifs> evtEtModifs) {
    	
    	// Liste des SpecificitesColis à fournir à la Dao pour sauvegarde en base
    	final List<SpecifsColis> lstSC = new ArrayList<>();
    	MSTechnicalException lastError = null;
    	boolean result = false;
    	int nbTrtFail=0;

        Evt evt = null;
    		// indicateur du nombre de modifs apportées à l'objet. Si 0 rien a sauver.
			for (EvtEtModifs evtEtModif : evtEtModifs) {
		    	try{
				evt = evtEtModif.getEvt();
				final Map<String, String> modifications = evtEtModif.getModifications();
				evtEtModif = null;
				// L'objet de transport des données à sauvegarder
				final SpecifsColis colis = new SpecifsColis();
				if (evt.getDateEvt() != null) {
					colis.setNoLt(evt.getNoLt());
					/* Extraction de la spécificité du code événement */
					final ESpecificiteColis extractSpecifsEvt = utils.extractSpecifsEvt(evt);
					if (extractSpecifsEvt != null) {
						if (extractSpecifsEvt.equals(SAC) || extractSpecifsEvt.equals(BOX)) {
							colis.getSpecifsEvt().put(new DateTime(2000, 1, 1, 0, 0, 0).toDate(),
									extractSpecifsEvt.getCode());
						} else {
							colis.getSpecifsEvt().put(evt.getDateEvt(), extractSpecifsEvt.getCode());
						}
					}

					/* Extraction de l'étape de l'événement */
					final Evenement evtRefRecord = cacheEvenement.getValue(evt.getCodeEvt());
					
					/* Définition du code transport */
					String ssCodeEvt = StringUtils.isBlank(evt.getSsCodeEvt()) ? "" : evt.getSsCodeEvt();
					// RG-MSUpdSpecColis-65
					// Si (l’événement contient l’infocomp 90 et qu’elle n’est
					// pas vide OU evt.createurEVT = PSFIO ou PSFIO) :
					// dans l’enregistrement de l’étape, remplacer le
					// code_transport par LAPOSTE
					if (evt.getInfoComp(CODE_REGATE_EMMETEUR) != null || (null != evt.getCreateurEvt()
							&& (evt.getCreateurEvt().equals("PSFIO") || evt.getCreateurEvt().equals("IFVSIO")))) {
						ssCodeEvt = "LAPOSTE";
						// Sinon si 4 premiers char de evt.lieuEvt sont
						// numériques et cinquiéme une lettre :
						// dans l’enregistrement de l’étape, remplacer le
						// code_transport par PICKUP
					} else if (null != evt.getLieuEvt() && evt.getLieuEvt().length() > 4
							&& StringUtils.isNumeric(evt.getLieuEvt().substring(0, 4))
							&& StringUtils.isAlpha(evt.getLieuEvt().substring(4, 5))) {
						ssCodeEvt = "PICKUP";
					}
					/* RG-MSUpdSpecColis-74 */
					if (evt.getCodeEvt().equals("SD") && StringUtils.isBlank(ssCodeEvt)
						&& StringUtils.isNotBlank(evt.getInfoComp(CODE_TRANSPORT))) {
						ssCodeEvt = evt.getInfoComp(CODE_TRANSPORT);
					}

					// indicateur de propagation de l'événement
					String propagated = "";

					// RG-MSUpdSpecColis-69
					// Si l'evt contient l'infocomp 104, alors propageted=P
					if (evt.getInfoComp(ID_CONTENANT_PROPAG) != null)
						propagated = "P";
					// RG-MSUpdSpecColis-70
					// récupération de l'outil de saisie (en supprimant le
					// préfixe PROPA_ s'il existe)
					String outilSaisie = null == evt.getCreateurEvt() ? "" : evt.getCreateurEvt();
					if (outilSaisie.startsWith("PROPA_")) {
						outilSaisie = outilSaisie.substring(6);
					}
					final String lieu = StringUtils.isBlank(evt.getLieuEvt()) ? "" : evt.getLieuEvt();

					if (evtRefRecord != null) {
						String etape = evtRefRecord.getEtape().getCode();
						if (etape != null && etape.length() > 0 && !TRANSVERSE.equals(etape)) {
							// RG-MSUpdSpecColis-72 Check si l'evt doit générer
							// l'étape associée
							if (EvtRules.doitGenererEtape(evt)) {
								colis.addEtape(evt.getDateEvt(), etape + "|" + evt.getCodeEvt() + "|" + ssCodeEvt + "|"
										+ propagated + "|" + outilSaisie + "|" + lieu);
							}
						}
					}

					// RG-MSUpdSpecColis-66
					// Si c'est un evt d'exclusion ajouter l'étape EXCLUSION en
					// plus, sauf pour la poste
					if (EvtRules.estUnEvtDExclusion(evt)) {
						final String nbJourExclusion = String.format("%02d", EvtRules.getNbreJoursDExclusion(evt));
						colis.addEtape(DateRules.nextMilliseconde(evt.getDateEvt()),
								EXCLUSION.getCode() + "|" + evt.getCodeEvt() + "|" + nbJourExclusion + "|" + propagated
										+ "|" + outilSaisie + "|" + lieu);
					}

					// RG-MSUpdSpecColis-67
					// Si c'est un événement H,SD ou TA de la poste
					// Ajouter l'étape ACKLIV|xx|LAPOSTE
					if (EvtRules.estUnEvtDAcquittementLivraisonLaposte(evt)) {
						colis.addEtape(DateRules.addMillisecondes(evt.getDateEvt(), 2),
								ACQUITTEMENT_LIVRAISON.getCode() + "|" + evt.getCodeEvt() + "|" + ssCodeEvt + "|"
										+ propagated + "|" + outilSaisie + "|" + lieu);
					}

					// RG-MSUpdSpecColis-71
					// Si evt ZA, ZC ou ZT, ou alors, SD avec code disp ERT,
					// ERA, ERC, xxERT, xxERA, xxER
					// Ajouter l'étape INCIDENT
					if (EvtRules.estUnIncidentDeDispersion(evt)) {
						colis.addEtape(DateRules.addMillisecondes(evt.getDateEvt(), 2),
								INCIDENT.getCode() + "|" + evt.getCodeEvt() + "|" + ssCodeEvt + "|" + propagated + "|"
										+ outilSaisie + "|" + lieu);
					}

					/* Extraction des infosupp de l'événement */
					final Map<String, String> infosupp = utils.extractInfoSupp(evt);
					if (!infosupp.isEmpty()) {
						colis.addAllInfoSupp(infosupp);
					}

					/* Extraction de la date contractuelle de l'événement */
					try {
						if (modifications != null && modifications.containsKey("DATE_CONTRACTUELLE")) {
							colis.getDatesContractuelles().put(evt.getDateEvt(),
									DateRules.toTimestampDateWsCalculRetard(modifications.get("DATE_CONTRACTUELLE")));
						}
					} catch (final ParseException e) {
						logger.error("Erreur lors de l'extraction de la date contractuelle de l'événement", e);
					}

					/* Si des valeurs ont été extraites de l'evt ==> ajouter le colis à la liste des colis à sauvegarder */
					if (colis.getEtapes().size() > 0 || colis.getSpecifsEvt().size() > 0
							|| colis.getDatesContractuelles().size() > 0 || colis.getInfoSupp().size() > 0) {
						lstSC.add(colis);
					}
				}
		    	} catch (Exception e) {
					logger.error("Erreur dans traitementEvenement : ", e);
					dao.declareErreur(evtEtModif==null?null:evtEtModif.getEvt(),"traitementEvenement", e);
					if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
	    				lastError = new MSTechnicalException(e);
	    				nbTrtFail++;
	    			}
	    			else
	    				throw e;
				}
			}
        /* Sauver les colis de la liste en les fournissant à la Dao */
        try{
    		result = dao.updateSpecifsEvenements(lstSC);
    	} catch (MSTechnicalException e) {
    		logger.error("Erreur dans traitementCodeService : ",e);
    		if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
    			lastError = e;
    		}
    		else
    			throw e;
    	}

    	if (lastError != null){
        	dao.updateCptTrtTrtFailMS(0, nbTrtFail);
    		throw lastError;
    	}

    	return  result;
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
