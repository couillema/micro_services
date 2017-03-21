package com.chronopost.vision.microservices.insertpointtournee.v1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.tournee.getdetailtournee.v1.Tournee;
import com.chronopost.vision.microservices.utils.IntegerUtil;
import com.chronopost.vision.model.Agence;
import com.chronopost.vision.model.EInfoComp;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.rules.EvtRules;
import com.chronopost.vision.transco.TranscoderService;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * implémentation du service TraitementRetardService
 * 
 * @author LGY
 */
public enum InsertPointTourneeServiceImpl implements IInsertPointTourneeService {
    INSTANCE;

    /**
     * log
     */
    private final Logger log = LoggerFactory.getLogger(InsertPointTourneeServiceImpl.class);
    
    // Regex pour vérifier que la chaine testé est au format yyyyMMdd et créer 3 groupe (yyyy)(MM)(dd)
    /** REGEXP ^(\\d{4})(0\\d|1[0-2])([0-2]\\d|3[0-1])$ */
    private final String dateRegex = "^(\\d{4})(0\\d|1[0-2])([0-2]\\d|3[0-1])$";
    private final String idPointC11Regex = "^(\\w{8})(\\w{3})(\\w{14})$";
    private final String idC11Regex = "^(\\w{5})(\\w{14})$";
    private final String idC11RegexC11Plus = "^(\\w{8})(\\w{14})$";
    private IInsertPointTourneeDao dao;
    private CacheManager<Agence> cacheAgence;

    /**
     * Singleton
     * 
     * @return
     */
    @Deprecated
    public static IInsertPointTourneeService getInstance() {
        return INSTANCE;
    }

    @Override
    public IInsertPointTourneeService setDao(final IInsertPointTourneeDao dao) {
        this.dao = dao;
        return this;
    }

    @Override
    public IInsertPointTourneeService setRefentielAgence(final CacheManager<Agence> cacheAgence) {
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
    @SuppressWarnings("boxing")
	@Override
    public boolean traiteEvenement(final List<Evt> evenements) throws InterruptedException, ExecutionException {
        /* List des événements à reporter sur un point de la tournée */
    	final List<Evt> evtsPoint = new ArrayList<>();
        /* List des événements sur colis fictif (debut/fin de tournée) */
    	final List<Evt> evtsTournee = new ArrayList<>();

        boolean result = true;
        Future<Boolean> resultPoint = null;
        Future<Boolean> resultTournee = null;
        final List<Future<Boolean>> callToGetIdPointC11 = new ArrayList<>(); // Les appels asynchrone a getIdPointC11 (si necessaire)

        MSTechnicalException lastError = null;
        int nbTrtFail = 0;

        /* Pour chaque evenement de livraison (uniquement) */
        final List<Evt> deliveryEvts = getDeliveryEvts(evenements);
        for (final Evt evt : deliveryEvts) {
        	/* RG-MSInsPoint-018 Seuls les evts CHRONO sont considérés */
        	if (evt.getLieuEvt() != null && cacheAgence.getValue(evt.getLieuEvt()) != null ) {
        		if (EvtRules.estUnColisFictif(evt) == true) {
        			evtsTournee.add(evt);
        		} else {
        			/* si l'idPointC11 n'est pas défini on le définit (en asynchrone) */
        			final Map<String, String> infoComp = evt.getInfoscomp();
        			if (infoComp == null || !infoComp.containsKey(EInfoComp.ID_POINT_C11.getCode())) {
        				callToGetIdPointC11.add(new ComputeIdC11Command(this, evt).queue());
        				log.debug("Création de l'idPointC11 absent sur l'événement : {}", evt);
        			}

        			// Ajout temporaire pour afficher ce que contient
        			// heureDebutPoint (ce if est à supprimer)
        			if (FeatureFlips.INSTANCE.getBoolean("AffichageNouveauxAttributs", false)) {
        				if (evt.getHeureDebutPoint() != null && evt.getHeureDebutPoint().length() > 0) {
        					log.error("{} - Date Tournee        = {}", evt.getNoLt(), evt.getDateTournee());
        					log.error("{} - Heure debut tournee = {}", evt.getNoLt(), evt.getHeureDebutTournee());
        					log.error("{} - Heure fin tournee   = {}", evt.getNoLt(), evt.getHeureFinTournee());
        					log.error("{} - Numero point        = {}", evt.getNoLt(), evt.getNumeroPoint());
        					log.error("{} - Date  point         = {}", evt.getNoLt(), evt.getDatePoint());
        					log.error("{} - Heure debut point   = {}", evt.getNoLt(), evt.getHeureDebutPoint());
        					log.error("{} - Heure fin point     = {}", evt.getNoLt(), evt.getHeureFinPoint());
        					log.error("{} - Flag emission radio = {}", evt.getNoLt(), evt.getFlagEmissionRadio());
        				}
        			}
        			evtsPoint.add(evt);
        		}
        	}
        }

        /* On attend les événtuels thread (appels asynchrones à getIdPointC11) */
        for (final Future<Boolean> idPointC11Result : callToGetIdPointC11) {
            try {
                idPointC11Result.get();
            } catch (final InterruptedException | ExecutionException | CancellationException e) {
            	log.error("Catch exception au retour computeIdPointC11",e);
            	lastError = new MSTechnicalException(e);
                result = false;
                nbTrtFail++;
            }
        }
        
        /* On declare ces eventuelles erreur qui n'ont pas pu l'être par computeIdC11 car il s'agit d'interruptions */
        if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false) && nbTrtFail>0){
        	dao.updateCptTrtTrtFailMS(0, nbTrtFail);
        }

        /* on lance le traitement des Tournees et des points en asynchrone; */
        if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)){
        	try{
        		resultPoint = new AddEvtDansPointCommand(this.dao, evtsPoint).queue();
        	} catch (MSTechnicalException e){
        		lastError = e;
        	}
        	try{
        		resultTournee = new MiseAJourTourneeCommand(this.dao, evtsTournee).queue();
        	} catch (MSTechnicalException e){
        		lastError = e;
        	}
        	try{
        		if (resultPoint != null)
        			result = result && resultPoint.get();
        	} catch (MSTechnicalException e){
        		lastError = e;
        	}
        	try{
        		if (resultTournee != null)
        			result = result && resultTournee.get();
        	} catch (MSTechnicalException e){
        		lastError = e;
        	}

        	if (lastError != null)
        		throw lastError;
        }
        else {
        	resultPoint = new AddEvtDansPointCommand(this.dao, evtsPoint).queue();
        	resultTournee = new MiseAJourTourneeCommand(this.dao, evtsTournee).queue();

        	result = result && resultPoint.get();
        	result = result && resultTournee.get();
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.chronopost.vision.microservices.insertpointtournee.v1.
     * IInsertPointTourneeService #computeIdPointC11(Evt evenement)
     */
    public boolean computeIdPointC11(final Evt evenement) {
        boolean result = false;
        try {
        	String newIdPointC11 = null;
            final String idPoint = generateIdPoint(evenement);
            final Agence agence = getAgence(evenement);

            // Si le colis a eu un evt TA ce jour (le jour de l’evt), et si cet
            // evt TA indiquait un idPointC11 ou un idC11
            // et le meme code tournée que l'événement actuel
            final Evt dernierEvtTA = this.dao.trouverDernierEvtTA(evenement); // Cet appel est le plus couteux à ce jour
            
            /* Soit on a un evt TA antérieur possédant un idC11 ou idPointC11 */
            if (!evenement.getCodeEvt().equals("TA") && EvtRules.hasIdC11OuIdPointC11(dernierEvtTA) && dernierEvtTA.getSsCodeEvt().equals(evenement.getSsCodeEvt())) {
                // alors rattacher l’evt à un nouveau point de la tournée en
                // générant l’idPointC11 à partir de l’idC11 et l’heure de début
                // de point
                if (EvtRules.hasIdPointC11(dernierEvtTA)) {
                	final String idPointC11TA = dernierEvtTA.getInfoComp(EInfoComp.ID_POINT_C11);

                    // On prend le 1er groupe de 8 (code agence + code tournee) + idPoint + le dernier groupe de 14 (date + heure tournee)
                    newIdPointC11 = idPointC11TA.replaceAll(idPointC11Regex, "$1" + idPoint + "$3");
                    result = true;
                } else if (EvtRules.hasIdC11(dernierEvtTA)) {
                    String idC11TA = dernierEvtTA.getInfoComp(EInfoComp.ID_C11);

                    // On prend le code agence + 1er groupe de 5 (code tournee) + idPoint + le dernier groupe de 14 (date + heure tournee)
                	// 2 maniere de le faire selon que l'on a un idC11 ou idC11+
                	// TODO cette distinction C11 / C11+ doit disparaitre lorsque les idC11+ seront devenus la norme
                    if (idC11TA.length() == 19)
                    	newIdPointC11 = idC11TA.replaceAll(idC11Regex, agence.getTrigramme() + "$1" + idPoint + "$2");
                    else
                    	newIdPointC11 = idC11TA.replaceAll(idC11RegexC11Plus, "$1" + idPoint + "$2");
                    result = true;
                }
            } else {  /* Soit on a pas de TA.*/
            	final Tournee tournee = this.dao.trouverDerniereTournee(evenement);
                if (agence != null && tournee != null) {
                    // Sinon si il existe une tournée pour cette agence, ce code
                    // tournée, ce jour et précédent la date de l’evt
                    // Rattacher le point à cette tournée en générant
                    // l’idPointC11 à partir de l’idC11 et l’heure de début de
                    // point

                    // On prend le code agence + 1er groupe de 5 (code tournee) + idPoint + le dernier groupe de 14 (date + heure tournee)
                	// 2 maniere de le faire selon que l'on a un idC11 ou idC11+
                	// TODO cette distinction C11 / C11+ doit disparaitre lorsque les idC11+ seront devenus la norme
                	if (tournee.getIdC11().length() == 19)
                		newIdPointC11 = tournee.getIdC11().replaceAll(idC11Regex, agence.getTrigramme() + "$1" + idPoint + "$2");
                	else
                		newIdPointC11 = tournee.getIdC11().replaceAll(idC11RegexC11Plus, "$1" + idPoint + "$2");
                	
                    result = true;
                } else {
                    // Sinon créer une nouvelle tournée avec idC11 généré depuis
                    // code tournée et date_debut_tournée et heure=00:00:00
                    // Rattacher le point à cette tournée en générant
                    // l’idPointC11 à partir de l’idC11 et l’heure de début de
                    // point
                	final String jourTournee = getJourTournee(evenement);
                    
                    
                    // on prend le code agence + le code tournee + idPoint + date tournee + 000000 (heure de tournee à 00:00:00)
                    if (agence != null && jourTournee != null){
                    	newIdPointC11 = new StringBuilder()
                    					.append(agence.getTrigramme())
                    					.append(evenement.getSsCodeEvt())
                    					.append(idPoint).append(jourTournee)
                    					.append("000000").toString();
                    	result = true;
                    }
                }
            }

			if (result == true) {
				final Map<String, String> infosComp = new HashMap<>(evenement.getInfoscomp());
				infosComp.put(EInfoComp.ID_POINT_C11.getCode(), newIdPointC11);
				evenement.setInfoscomp(infosComp);
			}
            else
            	log.info("Impossible de générer un idPointC11 pour le colis "+evenement.getNoLt()+" et l'evt "+evenement.getCodeEvt()+" "+evenement.getDateEvt());
        } catch (final FunctionalException e) {
            log.error("FunctionalException  sur computeidPointC11 pour le colis "+evenement.getNoLt()+" et l'evt "+evenement.getCodeEvt()+" "+evenement.getDateEvt(),e);
        } catch (final MSTechnicalException e){
        	/* Ici on a a faire à une exception qui necessite de rejouer plus tard les evts. Ce sont des prob de timeout, base access etc... */
        	log.error("TechnicalException  sur computeidPointC11 pour le colis "+evenement.getNoLt()+" et l'evt "+evenement.getCodeEvt()+" "+evenement.getDateEvt(),e);
        	throw e;
        } catch (final Exception e) {
        	log.error("Exception inconnue sur computeidPointC11 pour le colis "+evenement.getNoLt()+" et l'evt "+evenement.getCodeEvt()+" "+evenement.getDateEvt(),e);		
       	}
        return result;
    }

    /**
     * @param evts
     *            la liste des événements reçus
     * @return Une liste issue de la liste d'événement fournie, mais ne
     *         contenant plus que les événements de tournée (TA et D+)
     */
    private List<Evt> getDeliveryEvts(final Collection<Evt> evts) {
        /*
         * Récupération de la liste des événements de tournée (les autre ne sont
         * pas à traiter) Il s'agit ici de TA, et des D+
         */
        final String paramEvtsAyantRapportAvecTournee = TranscoderService.INSTANCE.getTranscoder("DiffusionVision").transcode("parametre_microservices",
                "evt_point_tournee");

        /*
         * Si la liste d'evt à traiter n'est pas définie, on retourne une list
         * vide
         */
        if (StringUtils.isBlank(paramEvtsAyantRapportAvecTournee)) {
            log.error("La liste des événements à considérer par insertPointTournee n'est pas définie. (param. evt_point_tournee).");
            return new ArrayList<Evt>();
        }

        final Predicate<Evt> predicate = new Predicate<Evt>() {
            public boolean apply(final Evt evt) {
                return paramEvtsAyantRapportAvecTournee.contains("|" + evt.getCodeEvt() + "|");
            }
        };

        return Lists.newArrayList(Iterables.filter(evts, predicate));
    }

    /**
     * Renvoie la date de la tournée sous la forme ddMMyyyy.<br>
     * Utilise dateTournee si non-null, sinon utilise dateEvt.
     * 
     * @param evt
     *            un événement
     * @return la date de la tournée sous la forme ddMMyyyy
     */
    private String getJourTournee(final Evt evt) {
        String jourTournee = null;

        if (StringUtils.isNotBlank(evt.getDateTournee()) && evt.getDateTournee().length() == 8) {
            // Essaie de passer la date du format yyyyMMdd à ddMMyyyy
        	final String tmp = evt.getDateTournee().replaceAll(dateRegex, "$3$2$1");
            
            // Si la String transformé est identique, il y a un problème de format (A partir de 2013)
            if (tmp.equals(evt.getDateTournee())) {
                log.error("Format invalide. Attendu 'yyyyMMdd' Reçu : '{}'", evt.getDateTournee());
            } else {
                jourTournee = tmp;
            }
        }

        if (jourTournee == null) {
            jourTournee = new DateTime(evt.getDateEvt()).toString("ddMMyyyy");
        }

        return jourTournee;
    }

    /**
     * Génère un idPoint au format 'Shm'.<br>
     * On utilise heureDebutPoint s'il le format est 'HHmm', sinon on utilise
     * l'heure de l'evt. On convertit l'heure en base 24 (0-n), et les minutes
     * en base 60 (0-X)
     * 
     * @param evt
     * @return
     */
    private String generateIdPoint(final Evt evt) {
    	final StringBuilder newIdPoint = new StringBuilder();
        int hourOfDay = -1;
        int minuteOfHour = -1;
        if (StringUtils.isNotBlank(evt.getHeureDebutPoint())) {
            if (evt.getHeureDebutPoint().length() == 4) {
                try {
                    hourOfDay = Integer.parseInt(evt.getHeureDebutPoint().substring(0, 2));
                    minuteOfHour = Integer.parseInt(evt.getHeureDebutPoint().substring(2, 4));
                } catch (final NumberFormatException e) {
                    log.info("Format invalide. Attendu 'HHmm' Reçu : '{}'", evt.getHeureDebutPoint());
                }
            } else {
                log.info("Format invalide. Attendu 'HHmm' Reçu : '{}'", evt.getHeureDebutPoint());
            }
        }
        
        if (hourOfDay == -1 || minuteOfHour == -1) {
        	final DateTime dateTime = new DateTime(evt.getDateEvt());
            hourOfDay = dateTime.getHourOfDay();
            minuteOfHour = dateTime.getMinuteOfHour();
        }

        newIdPoint.append('S');
        newIdPoint.append(IntegerUtil.convertToBase(hourOfDay, 24));
        newIdPoint.append(IntegerUtil.convertToBase(minuteOfHour, 60));

        return newIdPoint.toString();
    }

    /**
     * Recherche l'agence qui a comme poste comptable le lieu de l'evt.<br>
     * Si lieuEvt est null (ou vide) ou si on ne trouve pas d'agence dans le
     * référentiel des agence, on retourne null.
     * exception.
     * 
     * @param evt
     *            un événement
     * @return l'agence ayant comme poste comptable le lieu de l'evt
     */
    private Agence getAgence(final Evt evt) throws FunctionalException {
        // On essaie de trouver l'agence par son poste comptable, si on ne
        // trouve rien, il y a un problème...
        return cacheAgence.getValue(evt.getLieuEvt());
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
