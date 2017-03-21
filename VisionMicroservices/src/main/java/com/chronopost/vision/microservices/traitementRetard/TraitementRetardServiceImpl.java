package com.chronopost.vision.microservices.traitementRetard;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.TraitementRetardInput;
import com.chronopost.vision.model.TraitementRetardWork;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.transco.TranscoderService;

import fr.chronopost.soap.calculretard.cxf.CalculDateDeLivraisonEstimee;
import fr.chronopost.soap.calculretard.cxf.ResultCalculerRetardPourNumeroLt;

/**
 * implémentation du service ITraitementRetardServiceImpl
 * 
 * @author LGY
 */
public class TraitementRetardServiceImpl implements ITraitementRetardService {

    private static final Logger logger = LoggerFactory.getLogger(TraitementRetardServiceImpl.class);
    
    private static final String CODE_EVENEMENT = "RD";
    private static final Integer CODE_BCO_EVENEMENT = new Integer(27);
    private static final Integer PRIORITE_EVENEMENT_RD = new Integer(1000);
    private static final String CREATEUR_EVENEMENT = "AUTOMATIQUE";
    
    /** Nombre de millisecondes dans une heure */
    private static final long MILLISEC_BY_HOUR = 60 * 60 * 1000;
    
    /** Codes infoscomp bco des infocomps de l'evt RD */
    private static final String IDINFOCOMP_DLE_ID = "70";
    private static final String IDINFOCOMP_DLE_DATE = "152";
    private static final String IDINFOCOMP_HLE_MIN = "153";
    private static final String IDINFOCOMP_HLE_MAX = "154";
    private static final String IDINFOCOMP_DELAY_VALUE = "155";
    
    /** Format jour utilisé dans Webservice CalculRetard dd/MM/yyyy */
    private static final SimpleDateFormat JOUR_FMT = new SimpleDateFormat("dd/MM/yyyy");
    /** Format complet utilisé dans Webservice CalculRetard dd/MM/yyyy hh:mm */
    private static final SimpleDateFormat CALCUL_RETARD_DATETIME_FMT = new SimpleDateFormat("dd/MM/yyyy hh:mm");
    
    private ITraitementRetardDao dao;
    
    /**
	 * Singleton
	 */
	static class InstanceHolder {
		public static ITraitementRetardService service = new TraitementRetardServiceImpl();
	}

	/**
	 * Singleton
	 * 
	 * @return
	 */
	public static ITraitementRetardService getInstance() {
		return InstanceHolder.service;
	}
    
    private TraitementRetardServiceImpl()  {
    }
        
    public ITraitementRetardService setDao(final ITraitementRetardDao dao){
    	this.dao = dao;
    	return this;
    }
    
    /* (non-Javadoc)
     * @see com.chronopost.vision.microservices.traitementRetard.TraitementRetardService#genereRD(java.util.List<TraitementRetardInput>)
     */
    @Override
    public boolean genereRD(final List<TraitementRetardWork> retards) {
    	boolean resultat = true;
    	
    	/* On vérifie que le flag d'activation est bien sur ON  (RG-MSTraitRetard-004)
    	 * Si il ne l'est pas, alors on retourne OK car il n'y a rien à faire
    	 */
    	if (!FeatureFlips.INSTANCE.getBoolean("genere_evt_RD_actif", false)) {
    		return true;
    	}
    	

    	/* Récupération du poste comptable à appliquer aux événement RD générés (RG-MSTraitRetard-005) */
    	String posteComptableEvtRD;
    	try {
    		posteComptableEvtRD = TranscoderService.INSTANCE.getTranscoder("DiffusionVision").transcode("parametre_microservices", "poste_comptable_evt_RD");
    	if (posteComptableEvtRD == null)
    		posteComptableEvtRD = "";
    	} catch (final NullPointerException e){
    		posteComptableEvtRD = "";
    	}
    	
    	/* Mémoire des demandes de création d'événement par Lt */
    	final List<Evt> evtAGenerer = new ArrayList<>();
    	
    	/* Pour chaque couple Lt-DLE, estimer s'il faut ou non générer un RD */
		for (final TraitementRetardWork retard : retards) {
    		try {
    			final Lt lt = retard.getLt();
    			final ResultCalculerRetardPourNumeroLt dle = retard.getResultCR();
    			String generationRDRecommandee = "N";

    			/* Récupération de la recommandation si elle existe */
				if (dle != null && dle.getCalculDateDeLivraisonEstimee() != null
						&& dle.getCalculDateDeLivraisonEstimee().getGenerationRD() != null) {
					generationRDRecommandee = dle.getCalculDateDeLivraisonEstimee().getGenerationRD();
					//logger.info("Recommandation génération RD pour <" + lt.getNoLt() + "> = <" + generationRDRecommandee+ ">");
				}
    				
    	    			   
    			/* Si le WS de Calcul DLE indique qu'un evt de Retard devrait être généré (RG-MSTraitRetard-001) */
				if ("O".equals(generationRDRecommandee)) {

					/*
					 * Si le colis est en retard (livraison un autre jour que la
					 * date contractuelle: RG-MSTraitRetard-002)
					 */
					if (dle != null && dle.getAnalyse().getEnRetardDateEstimeeSupDateContractuelle() == 1) {
						/*
						 * Si la nouvel DLE > max(Anciennes DLE)
						 * (RG-MSTraitRetard-003)
						 */
						final Date maxDLE = retard.getMaxDLE() != null
								? JOUR_FMT.parse(JOUR_FMT.format(retard.getMaxDLE())) : null;

						Date newDLE = null; // DLE que l'on vient de calculer
						if (dle.getCalculDateDeLivraisonEstimee() != null
								&& dle.getCalculDateDeLivraisonEstimee().getDateDeLivraisonEstimee() != null) {
							newDLE = DateRules.toTimestampDateWsCalculRetard(
									dle.getCalculDateDeLivraisonEstimee().getDateDeLivraisonEstimee() + " 00:00");
							newDLE = JOUR_FMT.parse(JOUR_FMT.format(newDLE));
						}

						/*
						 * Si les conditions sont réunies, alors on génère un
						 * evt RD
						 */
						if (newDLE != null) {
							if (maxDLE == null || maxDLE.before(newDLE))
								evtAGenerer.add(makeNewEvtRD(dle, lt.getNoLt(), posteComptableEvtRD));
						}
					}
				}
    		} catch (final ParseException e) {
    			resultat = false;
    			logger.warn("Erreur de format sur une date de livraison estimée.");
    			continue;
    		}

    	} /* fin boucle*/
    	
    	/* Maintenant s'il y a des événements à générer, on appel le micro service genereEvt*/
		if (!evtAGenerer.isEmpty())
			resultat = resultat && new GenereEvtCommand(evtAGenerer).execute();
    	
    	return resultat;
    }

    /* (non-Javadoc)
     * @see com.chronopost.vision.microservices.traitementRetard.TraitementRetardService#memoriseDLE(List<TraitementRetardInput>)
     */
	@Override
	public boolean memoriseDLE(final List<TraitementRetardWork> retards) throws FunctionalException {
		return dao.insertDLE(retards);
	}

    /* (non-Javadoc)
     * @see com.chronopost.vision.microservices.traitementRetard.TraitementRetardService#extractMaxDLE(List<TraitementRetardInput>)
     */
	@Override
	public List<TraitementRetardWork> extractMaxDLE(final List<TraitementRetardInput> retards) {
		return dao.selectMaxDLE(retards);
	}
	
	/**
	 * Création d'un nouvel événement RD à la date actuelle.
	 * 
	 * @param dle : résultat du calcul retard pour cette lt 
	 * @param noLt : identifiant colis sur lequel doit porter l'événement.
	 * @param posteComptable : poste comptable de l'agence (logique) qui génère l'événement 
	 * @return : un objet Evt pré-rempli
	 */
	private Evt makeNewEvtRD(final ResultCalculerRetardPourNumeroLt dle, final String noLt, final String posteComptable ) {
		final Evt evt = new Evt();
		evt.setNoLt(noLt);
		evt.setDateEvt(new Date());
		evt.setPrioriteEvt(PRIORITE_EVENEMENT_RD);
		try {
			evt.setDateCreationEvt(DateRules.formatDateWS(new Date()));
		} catch (final ParseException e) {
			// Ceci ne peut pas arriver !!!
			logger.warn("Erreur de formattage sur la date actuelle");
		}
		evt.setIdbcoEvt(CODE_BCO_EVENEMENT);
		evt.setCodeEvt(CODE_EVENEMENT);
		evt.setCreateurEvt(CREATEUR_EVENEMENT);
		evt.setLieuEvt(posteComptable);

		/* infos comp de l'evt */
		final Map<String, String> infoscomp = new HashMap<>();

		if (dle != null) {
			final CalculDateDeLivraisonEstimee calculDateEstimee = dle.getCalculDateDeLivraisonEstimee();
			if (calculDateEstimee != null) {
				/* Ligne de parametrage utilisé pour le calcul par CalculRetard */
				if 		(  dle.getCalculDateDeLivraisonEstimee() != null 
						&& dle.getCalculDateDeLivraisonEstimee().getLigneParametragePourCalculDateDeLivraisonEstimee() != null) {
					final Pattern pattern = Pattern.compile("id=\\[([^\\])]*).*");
					final Matcher matcher = pattern.matcher(dle.getCalculDateDeLivraisonEstimee().getLigneParametragePourCalculDateDeLivraisonEstimee());
					if(matcher.matches()) {
						String numeroLigne = matcher.group(1);
						infoscomp.put(IDINFOCOMP_DLE_ID, numeroLigne);
					}
				}
				
				if(calculDateEstimee.getDateDeLivraisonEstimee()!=null){
					infoscomp.put(IDINFOCOMP_DLE_DATE, calculDateEstimee.getDateDeLivraisonEstimee());
				}
				
				if(calculDateEstimee.getHeureMinDeLivraisonEstimee()!=null){
					infoscomp.put(IDINFOCOMP_HLE_MIN, calculDateEstimee.getHeureMinDeLivraisonEstimee());
				}
				
				if(calculDateEstimee.getHeureMaxDeLivraisonEstimee()!=null){
					infoscomp.put(IDINFOCOMP_HLE_MAX, calculDateEstimee.getHeureMaxDeLivraisonEstimee());
				}

				/* calcul du nombre d'heure de retard */
				if (dle.getResultRetard() != null) {
					final String dateContractuelleS = dle.getResultRetard().getDateDeLivraisonPrevue();
					final String dateEstimeeS = calculDateEstimee.getDateDeLivraisonEstimee() + " "
							+ calculDateEstimee.getHeureMaxDeLivraisonEstimee();
					if (dateContractuelleS != null && dateEstimeeS.length() > 15) {
						try {
							final Date dateContractuelle = CALCUL_RETARD_DATETIME_FMT.parse(dateContractuelleS);
							final Date dateEstimee = CALCUL_RETARD_DATETIME_FMT.parse(dateEstimeeS);
							final Long retard = (dateEstimee.getTime() - dateContractuelle.getTime()) / MILLISEC_BY_HOUR;
							infoscomp.put(IDINFOCOMP_DELAY_VALUE, retard.toString() + "h");
						} catch (final ParseException e) {
							logger.warn("Erreur de formattage sur la date contractuelle");
						}
					}
				}
			}
		}
		evt.setInfoscomp(infoscomp);
		return evt;
	}
}
