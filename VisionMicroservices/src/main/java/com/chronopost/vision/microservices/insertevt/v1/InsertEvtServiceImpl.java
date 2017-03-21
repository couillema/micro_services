package com.chronopost.vision.microservices.insertevt.v1;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.validation.constraints.NotNull;

import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.jms.ITibcoEmsSender;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.insertevt.v1.commands.CalculRetardCommand;
import com.chronopost.vision.microservices.insertevt.v1.commands.GetLtCommand;
import com.chronopost.vision.microservices.insertevt.v1.commands.InitCalculRetardCommand;
import com.chronopost.vision.microservices.insertevt.v1.commands.InsertAgenceColisCommand;
import com.chronopost.vision.microservices.insertevt.v1.commands.InsertEvtCsvIntoLtCommand;
import com.chronopost.vision.microservices.insertevt.v1.commands.InsertEvtsEtCompteurCommand;
import com.chronopost.vision.microservices.insertevt.v1.commands.InsertPointTourneeCommand;
import com.chronopost.vision.microservices.insertevt.v1.commands.MaintienIdxEvtCommand;
import com.chronopost.vision.microservices.insertevt.v1.commands.SuiviBoxCommand;
import com.chronopost.vision.microservices.insertevt.v1.commands.TraitementRetardCommand;
import com.chronopost.vision.microservices.insertevt.v1.commands.UpdateSpecificationsColisCommand;
import com.chronopost.vision.microservices.insertevt.v1.commands.UpdateTourneeCommand;
import com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.TraitementRetardInput;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.model.rules.EvtRules;
import com.chronopost.vision.model.updatespecificationscolis.v1.EvtEtModifs;
import com.chronopost.vision.transco.TranscoderService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import fr.chronopost.soap.calculretard.cxf.CalculRetardWS;
import fr.chronopost.soap.calculretard.cxf.ResultCalculerRetardPourNumeroLt;

/**
 * @author adejanovski
 *
 */
public class InsertEvtServiceImpl implements IInsertEvtService {

    private final static Logger logger = LoggerFactory.getLogger(InsertEvtServiceImpl.class);

    /* Format jour retournée par le WS Calcul Retard */
    private static final SimpleDateFormat JOUR_FMT = new SimpleDateFormat("dd/MM/yyyy");

    private IInsertEvtDao dao;
    // private String getLtEndpoint;
    // private String insertLtEndpoint;

    // private String updateTourneeEndpoint;
    private String calculRetardEndpoint;
    private CalculRetardWS calculRetardClient;

	/**
	 * Instance du Sender Tibco EMS
	 */
	private ITibcoEmsSender emsSender;

	/**
	 * Objet Destination pour la queue de diffusion des evt
	 */
	private Destination queueDestination;
	
	
	private ObjectMapper mapper;

    private InsertEvtServiceImpl() {
    	mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    }

    /**
     * Singleton
     */
    static class InstanceHolder {
        public static IInsertEvtService service = new InsertEvtServiceImpl();
    }

    /**
     * Singleton
     * 
     * @return
     */
    public static IInsertEvtService getInstance() {
        return InstanceHolder.service;
    }

    @Override
    public IInsertEvtService setDao(IInsertEvtDao dao) {
        this.dao = dao;
        return this;
    }

    @Override
    public IInsertEvtService setCalculRetardEndpoint(String calculRetardEndpoint) {
        this.calculRetardEndpoint = calculRetardEndpoint;
        return this;
    }
    
    public IInsertEvtService setEmsSender(ITibcoEmsSender emsSender){
    	this.emsSender = emsSender;
    	return this;
    }
    
    public IInsertEvtService setQueueDestination(String queueName) throws JMSException{
    	queueDestination = emsSender.getQueueDestination(queueName);
    	return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.chronopost.vision.microservices.insertevt.v1.IInsertEvtService#insertEvts
     * (java.util.List)
     */
    @Override
    public boolean insertEvts(final List<Evt> evts) throws IOException, InterruptedException, ExecutionException,
            TimeoutException, ParseException, JMSException, NamingException {

    	if (evts.size()>0)
    		logger.debug("InsertEvt (echantillon): "+evts.get(0).getNoLt());
    	
        // Liste des résultat d'appel au WS CalculRetard
    	final Map<String, ResultCalculerRetardPourNumeroLt> resultatsCalculRetard = Maps.newHashMap();

        // Liste des événements pour lesquels il est valable de relancer un calcul retard
        String listEvtThatDontNeedCallToCalculRetard = TranscoderService.INSTANCE.getTranscoder("DiffusionVision")
                .transcode("parametre_microservices", "evt_calcul_retard");

        /* Transforme les id CAB28 de la Poste en id 15 */
        List<Evt> evtsFiltres;  
        if (FeatureFlips.INSTANCE.getBoolean("GestionCAB28", false))
        	evtsFiltres = filtreEvtColisAEcarter(evts);
        else
        	evtsFiltres = evts;

        // Groupage des événements par numéro LT en retirant les colis fictifs
        final Multimap<String, Evt> evtsParNumeroLt = getEvtsParNoLt(filtreEvtColisAIgnorer(evtsFiltres));

        // Récupération de la liste des no lt présents dans les événements fournis en entrée
        final List<String> noLts = new ArrayList<>();
        noLts.addAll(evtsParNumeroLt.keySet());

        // Récupération des LTs en base pour en contrôler l'état et récupérer le synonyme maître s'il existe
        // Utilisation d'Hystrix comme circuit breaker
        final Map<String, Lt> lts = new GetLtCommand(noLts).execute();

        // liste des événements redressés (noLt devient le noLt maitre)
        final List<Evt> evtsPourInsertion = evtsSurSynonymeMaitre(evtsFiltres, lts);
        
        // Map de synonymie noLtOriginel/NoLtMaitre  
        final Map<String, String> synonymesMaitres = getLtsMaitreSynonyme(lts);
        
        // Section parallélisée
        final Future<Boolean> insertEvtStatusFuture = new InsertEvtsEtCompteurCommand(dao, evtsPourInsertion,evts.size()).queue();

        final Future<Boolean> updateTourneeFuture = new UpdateTourneeCommand(evtsFiltres).queue();

        final Map<String, Future<ResultCalculerRetardPourNumeroLt>> calculRetardFutures = new HashMap<>();

        // initialisation du client SOAP si nécessaire
        initCalculRetardClient(this.calculRetardEndpoint);

        /*
         * N'appeler le WS CalculRetard que si le flip feature idoine est actif
         * et si l'evt (ou 1 des evts) de la lt est d'un type qui modifie la DLE
         */

        if (FeatureFlips.INSTANCE.getBoolean("Calcul_Retard_Actif", true))
            for (String noLt : noLts) {
                Boolean containEvtModifyingDLE = false;
                for (Evt e : evtsParNumeroLt.get(noLt))
                    if (!listEvtThatDontNeedCallToCalculRetard.contains("|" + e.getCodeEvt() + "|"))
                        containEvtModifyingDLE = true;

                if (containEvtModifyingDLE)
                    calculRetardFutures.put(noLt, new CalculRetardCommand(noLt, calculRetardClient).queue());
            }

        // On attend le retour des appels au WS calculRetard pour lancer le maintien des index
        final List<Future<Boolean>> maintainIdxEvtFutures = new ArrayList<>();

        // Liste des traitements retard à effectuer
        final List<TraitementRetardInput> retards = new ArrayList<>();

        for (String noLt : calculRetardFutures.keySet()) {
            ResultCalculerRetardPourNumeroLt resultCalculRetard = calculRetardFutures.get(noLt).get();
            resultatsCalculRetard.put(noLt, resultCalculRetard);
            
            // Si le calcul retard a pu être calculé et que la date de livraison estimée a changé (chgt date ==> chgt de jour)
            // on l'ajoute à la liste des retards à traiter (par le MS traitementRetard)
            if (resultCalculRetard != null && resultCalculRetard.getCalculDateDeLivraisonEstimee() != null
                    && resultCalculRetard.getCalculDateDeLivraisonEstimee().isDateDeLivraisonEstimeeCalculee() == true) {

                Date oldDLE = (lts.get(noLt) == null) ? null : lts.get(noLt).getDateLivraisonPrevue();
                String oldDLES = (oldDLE == null) ? null : JOUR_FMT.format(lts.get(noLt).getDateLivraisonPrevue());
                String newDLES = resultCalculRetard.getCalculDateDeLivraisonEstimee().getDateDeLivraisonEstimee();

                if (newDLES != null && (oldDLES == null || !newDLES.equals(oldDLES))) {
                    TraitementRetardInput retard = new TraitementRetardInput();
                    retard.setLt(lts.get(noLt));
                    retard.setResultCR(resultCalculRetard);
                    retards.add(retard);
                }
            }
        }

        for (String noLt : noLts) {
            // Appels au service MaintienIndexEvt sur tous les colis chargés
            maintainIdxEvtFutures.add(new MaintienIdxEvtCommand(lts.get(noLt), evtsParNumeroLt.get(noLt),
                    calculRetardFutures.containsKey(noLt) ? calculRetardFutures.get(noLt).get() : null).queue());
        }

        /*
         * On traite les éventuels retard (asynchrone). S'il n'y a pas retard a
         * considérer, on appel pas le MS inutilement.
         */
        Future<Boolean> traitementRetardFuture = null;
        if (FeatureFlips.INSTANCE.getBoolean("Traitement_Retard_Actif", false))
            traitementRetardFuture = (!retards.isEmpty()) ? new TraitementRetardCommand(retards).queue() : null;

        /* On effectue la maj des lt (asynchrone) */
        final Future<Boolean> insertEvtCsvIntoLtStatusFuture = new InsertEvtCsvIntoLtCommand(getLtsPourMiseAJourDepuisEvt(
        		evtsFiltres, lts, resultatsCalculRetard)).queue();

        /* On prend en compte les positions de box (asynchrone) */
        final Future<Boolean> suiviBoxStatusFuture = new SuiviBoxCommand(evtsFiltres).queue();

        /* On maintient les spécificités colis */
        Future<Boolean> specifColisFuture = null;
         if (FeatureFlips.INSTANCE.getBoolean("Specificites_Colis_Actif",false))
        	 specifColisFuture = new UpdateSpecificationsColisCommand(getEvtsEtModifsPourSpecificationsColis(evtsFiltres, lts, resultatsCalculRetard,synonymesMaitres)).queue();
        
        /* On maintient les points de tournee visités */
		Future<Boolean> insertPointTourneeFuture = null;
		if (FeatureFlips.INSTANCE.getBoolean("Insert_Point_Tournee_Actif", false))
			insertPointTourneeFuture = new InsertPointTourneeCommand(evtsPourInsertion).queue();

		/* On maintient les colis par agence */
		Future<Boolean> insertAgenceColisFuture = null;
		if (FeatureFlips.INSTANCE.getBoolean("Insert_Agence_Colis_Actif", false))
			insertAgenceColisFuture = new InsertAgenceColisCommand(evtsPourInsertion).queue();

        // Vérification de l'insertion des evt en base
        Boolean insertEvtStatus = insertEvtStatusFuture.get();
        Boolean insertEvtCsvIntoLtStatus = insertEvtCsvIntoLtStatusFuture.get();
        Boolean updateTourneeStatus = updateTourneeFuture.get();
        Boolean suiviBoxStatus = suiviBoxStatusFuture.get();
        Boolean traitementRetardStatus = (traitementRetardFuture != null) ? traitementRetardFuture.get() : true;
        Boolean specifColisStatus = (specifColisFuture != null) ? specifColisFuture.get() : true;
        Boolean insertPointTourneeStatus = (insertPointTourneeFuture != null) ? insertPointTourneeFuture.get() : true;
        Boolean insertAgenceColisStatus = (insertAgenceColisFuture != null) ? insertAgenceColisFuture.get() : true;
        		
        // Vérification de la fin du maintien des index
        Boolean maintainIdxEvtStatus = true;
        for (Future<Boolean> maintainIdxEvtFuture : maintainIdxEvtFutures) {
            maintainIdxEvtStatus = maintainIdxEvtFuture.get();
            // Si l'un des appels échoue, on sort
            if (!maintainIdxEvtStatus) {
                break;
            }
        }

        if (!insertEvtStatus) {
            throw new MSTechnicalException("Insertion des événements impossible dans la table evt");
        }
        if (!updateTourneeStatus) {
            throw new MSTechnicalException("Mise à jour des tournées impossible");
        }
        if (!insertEvtCsvIntoLtStatus) {
            throw new MSTechnicalException("Insertion des événements csv impossible dans la table lt");
        }
        if (!maintainIdxEvtStatus) {
            throw new MSTechnicalException("Mise à jour des index evt impossible");
        }
        if (!traitementRetardStatus) {
            throw new MSTechnicalException("Traitement des retards impossible");
        }
        if (!suiviBoxStatus) {
            throw new MSTechnicalException("Traitement des suivis box impossible");
        }
        if (!specifColisStatus) {
            throw new MSTechnicalException("Update Spécifications Colis impossible");
        }
        if (!insertPointTourneeStatus) {
            throw new MSTechnicalException("Traitement des points tournées impossible");
        }
        if (!insertAgenceColisStatus) {
            throw new MSTechnicalException("Traitement des agence colis impossible");
        }
        
        if(FeatureFlips.INSTANCE.getBoolean("Diffusion_Evt_Active", Boolean.FALSE)){
        	diffusionEvt(evtsParNumeroLt, lts);
        }

        return insertEvtStatus;
    }

	/**
     * Retourne une map de correspondance : noLtMaitre ==> NoLtSynonyme
     * @param lts : map de lts indexées par noLt synonyme (par forcement le noLt maitre)
     * @return une map de correspondance : noLtMaitre ==> NoLtSynonyme
     */
	private Map<String, String> getLtsMaitreSynonyme(@NotNull final Map<String, Lt> lts) {
		final Map<String, String> synoLt = new HashMap<>();
		for (String noLtSyno : lts.keySet()) {
			synoLt.put(lts.get(noLtSyno).getNoLt(), noLtSyno);
		}
		return synoLt;
	}

    public List<EvtEtModifs> getEvtsEtModifsPourSpecificationsColis(final List<Evt> evts, final Map<String, Lt> ltsFromDb,
    		final Map<String, ResultCalculerRetardPourNumeroLt> resultatsCalculRetard, final Map<String, String> synonymesFromMaitre) {
    	final List<EvtEtModifs> evtsEtModifs = new ArrayList<>();

        for (final Evt evt : evts) {
            EvtEtModifs evtEtModifs = new EvtEtModifs() ; 
            evtEtModifs.setEvt(evt);
            
            String noLtSynonyme = synonymesFromMaitre.get(evt.getNoLt());
            if (resultatsCalculRetard.containsKey(noLtSynonyme) && resultatsCalculRetard.get(noLtSynonyme) != null) {
                // On a eu un retour du calcul retard pour cette lt
                // Date de livraison contractuelle LT = WS Date de livraison prevue
                try {
                    if (  resultatsCalculRetard.get(noLtSynonyme).getResultRetard() != null
                       && resultatsCalculRetard.get(noLtSynonyme).getResultRetard().getDateDeLivraisonPrevue() != null) {
                    	
                        Timestamp dateLivraisonContractuelle = DateRules.toTimestampDateWsCalculRetard(
                                resultatsCalculRetard.get(noLtSynonyme).getResultRetard().getDateDeLivraisonPrevue());
                        
//                        Timestamp previous = null ;
//                        if (ltsFromDb.get(noLtSynonyme) != null) previous = ltsFromDb.get(noLtSynonyme).getDateLivraisonContractuelle();
                        
                        //if (!dateLivraisonContractuelle.equals(previous))   {
                            evtEtModifs.setModifications(new HashMap<String, String>());
                            evtEtModifs.getModifications().put(UpdateSpecificationsColisConstants.VALUE_DATE_CONTRACTUELLE, DateRules.toStringTimestampDateWsCalculRetard(dateLivraisonContractuelle)) ;
                        //}
                    }
                } catch (Exception e) {
                    // Je sais que c'est mal, mais il y a certaines
                    // nullPointerException qui foute la grouille alors que
                    // c'est un processus facultatif
                    logger.warn("Erreur acceptable sur le traitement du retour du calcul retard : " + e.getMessage(), e);
                }
            }
            evtsEtModifs.add(evtEtModifs) ;
        }
        
        return evtsEtModifs ;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.chronopost.vision.microservices.insertevt.v1.IInsertEvtService#
     * getLtsPourInsertionEvtCsv(java.util.List, java.util.Map)
     */
    public List<Lt> getLtsPourMiseAJourDepuisEvt(final List<Evt> evts, final Map<String, Lt> ltsFromDb,
    		final Map<String, ResultCalculerRetardPourNumeroLt> resultatsCalculRetard) throws ParseException,
            JsonProcessingException {
    	final List<Lt> lts = new ArrayList<>();

        for (final Evt evt : evts) {
        	final Set<String> codeEvt = new LinkedHashSet<>();
        	final Set<String> evtCsv = new LinkedHashSet<>();
            codeEvt.add(evt.getCodeEvt());
            evtCsv.add(EvtRules.getEvtAsString(evt));

            final Lt lt = new Lt().setNoLt(evt.getNoLt());

            if (ltsFromDb.containsKey(evt.getNoLt())) {
                // l'evt peut ne pas etre sur le synonyme maitre donc on le met
                // sur le bon numero
                lt.setNoLt(ltsFromDb.get(evt.getNoLt()).getNoLt());
            }

            if (resultatsCalculRetard.containsKey(evt.getNoLt()) && resultatsCalculRetard.get(evt.getNoLt()) != null) {
                // On a eu un retour du calcul retard pour cette lt
                // Date de livraison contractuelle LT = WS Date de livraison prevue
                try {
                    Timestamp dateLivraisonContractuelle = null;
                    if (resultatsCalculRetard.containsKey(evt.getNoLt())
                            && resultatsCalculRetard.get(evt.getNoLt()).getResultRetard() != null
                            && resultatsCalculRetard.get(evt.getNoLt()).getResultRetard()
                                    .isDateDeLivraisonPrevueCalculee()) {
                        dateLivraisonContractuelle = DateRules.toTimestampDateWsCalculRetard(resultatsCalculRetard
                                .get(evt.getNoLt()).getResultRetard().getDateDeLivraisonPrevue());
                        lt.setDateLivraisonContractuelle(dateLivraisonContractuelle);
                    }

                    // Date de livraison prevue = WS date livraison estimée
                    if (resultatsCalculRetard.containsKey(evt.getNoLt())
                            && resultatsCalculRetard.get(evt.getNoLt()).getCalculDateDeLivraisonEstimee() != null
                            && resultatsCalculRetard.get(evt.getNoLt()).getCalculDateDeLivraisonEstimee()
                                    .isDateDeLivraisonEstimeeCalculee()
                            && resultatsCalculRetard.get(evt.getNoLt()).getCalculDateDeLivraisonEstimee()
                                    .getDateDeLivraisonEstimee() != "null") {
                        lt.setDateLivraisonPrevue(DateRules.toTimestampDateWsCalculRetard(resultatsCalculRetard
                                .get(evt.getNoLt()).getCalculDateDeLivraisonEstimee().getDateDeLivraisonEstimee()
                                + " "
                                + resultatsCalculRetard.get(evt.getNoLt()).getCalculDateDeLivraisonEstimee()
                                        .getHeureMaxDeLivraisonEstimee()));
                    }

                    // Flag pour contrôler que le colis est en retard
                    if (resultatsCalculRetard.containsKey(evt.getNoLt())
                            && resultatsCalculRetard.get(evt.getNoLt()).getAnalyse() != null
                            && resultatsCalculRetard.get(evt.getNoLt()).getAnalyse()
                                    .getEnRetardDateEstimeeSupDateContractuelle() == 1
                            && dateLivraisonContractuelle != null) {
                        lt.setIdxDepassement(DateRules.toDateSortable(dateLivraisonContractuelle) + "__1");
                    }

                    // S'il n'est pas en retard, on réinitialise le flag
                    if (resultatsCalculRetard.containsKey(evt.getNoLt())
                            && resultatsCalculRetard.get(evt.getNoLt()).getAnalyse() != null
                            && resultatsCalculRetard.get(evt.getNoLt()).getAnalyse()
                                    .getEnRetardDateEstimeeSupDateContractuelle() != 1
                            && dateLivraisonContractuelle != null) {
                        lt.setIdxDepassement("__0");
                    }
                } catch (Exception e) {
                    // Je sais que c'est mal, mais il y a certaines
                    // nullPointerException qui foute la grouille alors que
                    // c'est un processus facultatif
                    logger.warn("Erreur acceptable sur le traitement du retour du calcul retard : " + e.getMessage(), e);
                }
            }
            if (EvtRules.getLatitudePrevue(evt) != null) {
                lt.setLatitudePrevue(EvtRules.getLatitudePrevue(evt));
            }

            if (EvtRules.getLongitudePrevue(evt) != null) {
                lt.setLongitudePrevue(EvtRules.getLongitudePrevue(evt));
            }

            if (EvtRules.getLatitudeDistri(evt) != null) {
                lt.setLatitudeDistri(EvtRules.getLatitudeDistri(evt));
            }

            if (EvtRules.getLongitudeDistri(evt) != null) {
                lt.setLongitudeDistri(EvtRules.getLongitudeDistri(evt));
            }

            if (EvtRules.getCodeService(evt) != null) {
                lt.setCodeService(EvtRules.getCodeService(evt));
            }

            if (EvtRules.getCodePays(evt) != null) {
                lt.setCodePaysDestinataire(TranscoderService.INSTANCE.getTranscoder("DiffusionVision")
                        .transcode("code_pays", EvtRules.getCodePays(evt)).split("\\|")[0]);
            }

            if (EvtRules.getPositionC11(evt) != null) {
                lt.setPositionC11(EvtRules.getPositionC11(evt));
            }

            if (EvtRules.getEtaInitiale(evt) != null) {
                lt.setEta(EvtRules.getEtaInitiale(evt));
            }

            lt.setCodesEvt(codeEvt).setEvts(evtCsv);

            lt.setDateModification(new Timestamp(new DateTime().getMillis()));

            lts.add(lt);
        }

        return lts;
    }

    /**
     * Groupage des événements par numero LT
     * 
     * @param evts
     * @return
     */
    public Multimap<String, Evt> getEvtsParNoLt(final List<Evt> evts) {
        Function<? super Evt, String> groupByNoLtFunction = new Function<Evt, String>() {
            public String apply(final Evt evt) {
                return evt.getNoLt();
            }
        };

        return Multimaps.index(evts, groupByNoLtFunction);
    }

    /**
     * Initialisation de l'appel au WS Calcul Retard.
     * 
     * @param endpoint
     * @throws MalformedURLException
     */
    private void initCalculRetardClient(String endpoint) throws MalformedURLException {
        if (calculRetardClient == null) {
            calculRetardClient = new InitCalculRetardCommand(endpoint).execute();
        }
    }

    /**
     * Remplacement des numéros de LT par le synonyme maitre s'il existe
     * 
     * @param evts
     * @param lts
     * @return
     */
    private List<Evt> evtsSurSynonymeMaitre(List<Evt> evts, Map<String, Lt> lts) {
        List<Evt> evtsAvecNumeroLtRemplace = Lists.newArrayList();
        for (Evt evt : evts) {
            Evt evtModifie = evt;
            if (lts.containsKey(evt.getNoLt())) {
                String noLtMaitre = lts.get(evt.getNoLt()).getNoLt();
                String codeService = lts.get(evt.getNoLt()).getCodeService();
                if (!evt.getNoLt().equals(noLtMaitre)) {
                    evtModifie.setNoLt(noLtMaitre);
                }

                if (codeService != null) {
                    evtModifie.setCodeService(codeService);
                }

            }

            evtsAvecNumeroLtRemplace.add(modifieMillisecondesDateEvtAvecIdBcoEvt(evtModifie));
        }

        return ImmutableList.copyOf(new ArrayList<>(evtsAvecNumeroLtRemplace));
    }

    /**
     * Permet de remplacer les nanosecondes de la date evt par une valeur
     * discriminante pour éviter les écrasements. La nécessité de cette
     * manipulation est liée à un défaut du schéma de la table vision.evt qui ne
     * contient pas le codeEvt dans la PK. A terme il faudra migrer vers une
     * table ayant le bon schéma. Alexander est totalement responsable de cette
     * merde...
     * 
     * @param evt
     * @return
     */
    private Evt modifieMillisecondesDateEvtAvecIdBcoEvt(final Evt evt) {
        if (evt.getDateEvt() != null) {
            Timestamp newDateEvt = new Timestamp(evt.getDateEvt().getTime());
            newDateEvt.setNanos(EvtRules.getEvtHash(evt, 999999999));
            evt.setDateEvt(newDateEvt);
        }
        return evt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.chronopost.vision.microservices.insertevt.v1.IInsertEvtService#
     * filtreEvtColisAIgnorer(java.util.List)
     */
    public List<Evt> filtreEvtColisAIgnorer(final List<Evt> evts) {
        Predicate<Evt> nEstPasUnEvtColisFictif = new Predicate<Evt>() {
            public boolean apply(Evt evt) {
                return !EvtRules.estUnColisAIgnorer(evt);
            }
        };

        final Collection<Evt> evtsNonFictif = Collections2.filter(evts, nEstPasUnEvtColisFictif);
        return new ArrayList<>(evtsNonFictif);
    }

    public void resetCalculRetard() {
        calculRetardClient = null;
    }
    
    /**
     * RG-MSInsertEvt-007
     * Si un événement contient dans son numéro lt un CAB 28, alors le numéro lt est remplacé par l’id 15 immédiatement    
     */
    public List<Evt> filtreEvtColisAEcarter(final List<Evt> evts) {
    	final List<Evt> lts = new ArrayList<>();
		for (Evt evt : evts) {
			String lt = evt.getNoLt();
			// RG-MSInsertEvt-008
			if (lt.startsWith("%") && lt.length() == 28) {
				// RG-MSInsertEvt-009
				String validLt = lt.substring(8, 22);
				try {
					// RG-MSInsertEvt-010 récup du caractère de contrôle
					char controlChar = getChecksum(validLt);
					validLt = validLt + controlChar;
					evt.setNoLt(validLt);
					lts.add(evt);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				lts.add(evt);
			}
		}
		return lts;
    }
    
    // algo iso7064
	private char getChecksum(String LT) throws Exception {
		int len = LT.length();
		LT = LT.toUpperCase();
		int cs = 36;
		int mod = 36;
		for (int i = 0; i < len; ++i) {
			char aChar = LT.charAt(i);
			int val = getIso7064Value(aChar);
			cs += val;
			if (cs > mod)
				cs -= mod;
			cs *= 2;
			if (cs > mod)
				cs -= mod + 1;
		}

		cs = mod + 1 - cs;
		if (cs == mod)
			cs = 0;
		return getIso7064Char(cs);
	}

	private static int getIso7064Value(char aChar) throws Exception {
		if ((aChar >= '0') && (aChar <= '9'))
			return (aChar - '0');
		if ((aChar >= 'A') && (aChar <= 'Z'))
			return (aChar - 'A' + 10);
		throw new Exception();
	}

	private static char getIso7064Char(int value) throws Exception {
		if ((value >= 0) && (value <= 9))
			return (char) (48 + value);
		if ((value >= 10) && (value <= 36))
			return (char) (65 + value - 10);
		throw new Exception();
	}

    /**
     * Diffusion des événements dans une queue JMS
     * 
     * @param evtsParNumeroLt
     * @param lts
     * @throws NamingException 
     * @throws JMSException 
     * @throws JsonProcessingException 
     */
    public void diffusionEvt(final Multimap<String, Evt> evtsParNumeroLt, final Map<String, Lt> lts) throws JsonProcessingException, JMSException, NamingException{
    	final List<Lt> ltsADiffuser = Lists.newArrayList();
		for (final Entry<String, Collection<Evt>> evts : evtsParNumeroLt.asMap().entrySet()) {
			Lt lt = new Lt().setNoLt(evts.getKey());
			if (lts.containsKey(evts.getKey())) {
				lt = lts.get(evts.getKey());
			}
			lt.setEvenements(new ArrayList<Evt>(evts.getValue()));

			ltsADiffuser.add(lt);
		}
		dao.insertDiffEvtCounter(ltsADiffuser.size());
		diffusionEvtDansQueue(ltsADiffuser);
    }
    
    /**
     * Envoi effectif de la lt (avec son ou ses événements) dans la queue JMS
     * 
     * @param lt
     * @throws NamingException 
     * @throws JMSException 
     * @throws JsonProcessingException 
     */
    public void diffusionEvtDansQueue(List<Lt> lts) throws JsonProcessingException, JMSException, NamingException{
    	emsSender.sendMessage(mapper.writeValueAsString(lts), queueDestination);
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
