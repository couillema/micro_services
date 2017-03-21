package com.chronopost.vision.microservices.traitementRetard;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.testng.AssertJUnit.assertTrue;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.sdk.GenereEvtV1;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.TraitementRetardInput;
import com.chronopost.vision.model.TraitementRetardWork;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.chronopost.vision.transco.transcoder.Transcoder;
import com.chronopost.vision.ut.RandomUts;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import fr.chronopost.soap.calculretard.cxf.Analyse;
import fr.chronopost.soap.calculretard.cxf.CalculDateDeLivraisonEstimee;
import fr.chronopost.soap.calculretard.cxf.ResultCalculerRetardPourNumeroLt;
import fr.chronopost.soap.calculretard.cxf.ResultRetard;

public class TraitementRetardServiceTest {

    /** Format retournée par le Calcul Retard : dd/MM/yyyy HH:mm */
    private static SimpleDateFormat CALCULRETARD_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    /** Format partiel retournée par le Calcul Retard : dd/MM/yyyy HH:mm */
    private static SimpleDateFormat CALCULRETARD_JOUR_FMT = new SimpleDateFormat("dd/MM/yyyy");

    private ITranscoderDao mockTranscoderDao = Mockito.mock(ITranscoderDao.class);

    /* Le mock de la base */
    private ITraitementRetardDao mockDao = Mockito.mock(ITraitementRetardDao.class);

    /** Le service de traitement des retard */
    private ITraitementRetardService service;
    private WireMockServer wireMockServer;
    private WireMock wireMock;

    private int httpPort = RandomUts.getRandomHttpPort2(); // ADDED JJC

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        setTranscos("true", "77998");

        service = TraitementRetardServiceImpl.getInstance().setDao(mockDao);
        /* On initialise le service sdk d'appel à genereEvt */
        GenereEvtV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);

        wireMockServer = new WireMockServer(httpPort);
        wireMockServer.start();
        WireMock.configureFor("127.0.0.1", httpPort);
        wireMock = new WireMock("127.0.0.1", httpPort);
    }

    @AfterClass
    private void tearDown() {
        wireMockServer.shutdownServer();
    }

    @SuppressWarnings("static-access")
    @Test
    /**
     * Une LT en retard d'un jour supplémentaire ==> Génération d'un evt RD
     * (cas passant)
     */
    public void genereRDTest1() throws Exception {
        /*
         * TEST 1: une LT en retard d'un jour supplémentaire (3 jours de
         * retards, et non plus 2)
         */
        boolean retourService = false;

        /* initialisation des variables à fournir au service */
        List<TraitementRetardWork> retards = new ArrayList<>();
        retards.add(newTraitementRetardWork("XX123456X", 1 // retard détecté
                , Calendar.DATE, 2 // DLE (= dans DCL + 2 jours)
                , Calendar.DATE, 1 // DCL (= dans maintenant + 1 jour)
                , Calendar.DATE, -1 // MaxDLE (= dans DLE - 1 jour)
                , "O" // WS CalculRetard propose génération d'un RD
        ));

        /*
         * La transcodification retournera ON pour le parametre
         * genereEvt_RD_actif
         */
        setTranscos("true", "77998");

        /*
         * Mock du MicroService genereEvt qui doit répondre true à toute
         * sollicitation
         */
        wireMock.register(post(urlMatching("/genereEvt.*")).withHeader("Content-Type", equalTo("application/json"))
                .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                                .withBody("{\"status\":true}")));
        wireMock.resetRequests();

        /* Appel du service à tester */
        retourService = service.genereRD(retards);

        /* Il doit y avoir eu un appel à genereEvt */
        LoggedRequest request = findAll(postRequestedFor(urlMatching("/genereEvt.*"))).get(0);
        String req = request.getBodyAsString();
        System.out.println("Evenement RD a générer : " + req);

        /*
         * Vérification de l'appel à genere evt une seul fois avec les valeurs
         * et infoscomp correctes
         */
        wireMock.verify(1, postRequestedFor(urlMatching("/genereEvt.*")));
        assertTrue("Le service doit retourner true", retourService == true);
        Calendar calDLE = Calendar.getInstance();
        calDLE.add(Calendar.DATE, 3);
        Date dateDLE = calDLE.getTime();
        String dt = "\"152\" : \"" + CALCULRETARD_JOUR_FMT.format(dateDLE);
        assert (req.contains(dt));
        assert (req.contains("\"153\" : \"13:00\""));
        assert (req.contains("\"154\" : \"14:30\""));
        assert (req.contains("\"155\" : \"" + String.valueOf((int) 48+ heureDEte(retards.get(0))) + "h\""));
        assert (req.contains("\"70\" : \"109\""));
    }

    @SuppressWarnings("static-access")
    @Test
    /**
     * Retard supplémentaire mais d'une heure seulement, sans changement de jour.
     * ==> Pas d'evt RD généré
     */
    public void genereRDTest2() throws Exception {
        /* TEST 2: une LT en retard d'une heure supplémentaire mais le meme jour */
        boolean retourService = false;

        /* initialisation des variables à fournir au service */
        List<TraitementRetardWork> retards = new ArrayList<>();
        retards.add(newTraitementRetardWork("XX123456X", 1 // retard détecté
                , Calendar.HOUR, 1 // DLE (= dans DCL + 2 jours)
                , Calendar.DATE, 1 // DCL (= dans maintenant + 1 jour)
                , Calendar.HOUR, -1 // MaxDLE (= dans DLE + 1 jour)
                , "O" // WS CalculRetard propose génération d'un RD
        ));

        /*
         * La transcodification retournera ON pour le parametre
         * genereEvt_RD_actif
         */
        setTranscos("true", "77998");

        /*
         * Mock du MicroService genereEvt qui doit répondre true à toute
         * sollicitation
         */
        wireMock.register(post(urlMatching("/genereEvt.*"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("true")));

        wireMock.resetRequests();

        /* Appel du service à tester */
        retourService = service.genereRD(retards);

        /* Il doit y avoir eu aucun appel à genereEvt */
        wireMock.verify(0, postRequestedFor(urlMatching("/genereEvt.*")));
        assertTrue("Le service doit retourner true", retourService == true);
    }

    @SuppressWarnings("static-access")
    @Test
    /**
     * Retard d'un jour par rapport à la derniere DLE calculée, mais il n'y a pas de 
     * dépassement de la date de livraison contractuelle (DCL)
     * ==> Pas d'evt RD
     */
    public void genereRDTest3() throws Exception {
        /*
         * TEST 3: une LT avec DLE d'un jour supplémentaire mais la DCL n'est
         * pas dépassée
         */
        boolean retourService = false;

        /* initialisation des variables à fournir au service */
        List<TraitementRetardWork> retards = new ArrayList<>();
        retards.add(newTraitementRetardWork("XX123456X", 0 // retard détecté
                , Calendar.DATE, -1 // DLE (= dans DCL - 1 jour)
                , Calendar.DATE, 3 // DCL (= dans maintenant + 1 jour)
                , Calendar.DATE, -1 // MaxDLE (= dans DLE - 1 jour)
                , "O" // WS CalculRetard propose génération d'un RD
        ));

        /*
         * La transcodification retournera ON pour le parametre
         * genereEvt_RD_actif
         */
        setTranscos("true", "77998");

        /*
         * Mock du MicroService genereEvt qui doit répondre true à toute
         * sollicitation
         */
        wireMock.register(post(urlMatching("/genereEvt.*"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("true")));

        wireMock.resetRequests();

        /* Appel du service à tester */
        retourService = service.genereRD(retards);

        /* Il doit y avoir eu aucun appel à genereEvt */
        wireMock.verify(0, postRequestedFor(urlMatching("/genereEvt.*")));
        assertTrue("Le service doit retourner true", retourService == true);
    }

    @SuppressWarnings("static-access")
    @Test
    /**
     * Retour en erreur du MS genereEvt 
     * ==> retourne FALSE.
     */
    public void genereRDTest4() throws Exception {
        /* TEST 4: Le MS genereEvt renvoie une erreur */
        boolean retourService = false;

        /* initialisation des variables à fournir au service */
        List<TraitementRetardWork> retards = new ArrayList<>();
        retards.add(newTraitementRetardWork("XX123456X", 1 // retard détecté
                , Calendar.DATE, 2 // DLE (= dans DCL + 2 jours)
                , Calendar.DATE, 1 // DCL (= dans maintenant + 1 jour)
                , Calendar.DATE, -1 // MaxDLE (= dans DLE - 1 jour)
                , "O" // WS CalculRetard propose génération d'un RD
        ));

        /*
         * La transcodification retournera ON pour le parametre
         * genereEvt_RD_actif
         */
        setTranscos("true", "77998");

        /*
         * Mock du MicroService genereEvt qui doit répondre false à toute
         * sollicitation
         */
        wireMock.register(post(urlMatching("/genereEvt.*")).withHeader("Content-Type", equalTo("application/json"))
                .willReturn(
                        aResponse().withStatus(500).withHeader("Content-Type", "application/json").withBody("false")));
        wireMock.resetRequests();

        /* Appel du service à tester */
        retourService = service.genereRD(retards);

        wireMock.verify(1, postRequestedFor(urlMatching("/genereEvt.*")));
        assertTrue("Le service doit retourner false", retourService == false);
    }

    @SuppressWarnings("static-access")
    @Test
    /**
     * Une LT en retard d'un jour supplémentaire 
     * Mais le parametre genereEvt_RD_actif est sur OFF ==> Pas de Génération d'un evt RD
     * (cas passant)
     */
    public void genereRDTest5() throws Exception {
        /* TEST 5: une LT en retard d'un jour supplémentaire */
        boolean retourService = false;

        /* initialisation des variables à fournir au service */
        List<TraitementRetardWork> retards = new ArrayList<>();
        retards.add(newTraitementRetardWork("XX123456X", 1 // retard détecté
                , Calendar.DATE, 2 // DLE (= dans DCL + 2 jours)
                , Calendar.DATE, 1 // DCL (= dans maintenant + 1 jour)
                , Calendar.DATE, -1 // MaxDLE (= dans DLE - 1 jour)
                , "O" // WS CalculRetard propose génération d'un RD
        ));

        /*
         * La transcodification retournera OFF pour le parametre
         * genereEvt_RD_actif
         */
        setTranscos("false", "77998");

        /*
         * Mock du MicroService genereEvt qui doit répondre true à toute
         * sollicitation
         */
        wireMock.register(post(urlMatching("/genereEvt.*"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("true")));
        wireMock.resetRequests();

        /* Appel du service à tester */
        retourService = service.genereRD(retards);

        /* Il doit y avoir eu un appel à genereEvt */
        wireMock.verify(0, postRequestedFor(urlMatching("/genereEvt.*")));
        assertTrue("Le service doit retourner true", retourService == true);
    }

    @SuppressWarnings("unchecked")
    @Test
    /**
     * On vérifie juste que le dao est bien appelé
     */
    public void extractMaxDLETest1() {

        TraitementRetardInput retard = new TraitementRetardInput();
        TraitementRetardWork retardRetour = new TraitementRetardWork();
        List<TraitementRetardInput> retards = new ArrayList<>();
        List<TraitementRetardWork> retardsRetours = new ArrayList<>();
        Lt lt = new Lt();
        lt.setNoLt("XX123456X");
        retard.setLt(lt);
        retardRetour = new TraitementRetardWork(new Date(), retard);
        retards.add(retard);
        retardsRetours.add(retardRetour);

        Mockito.when(mockDao.selectMaxDLE(Mockito.anyList())).thenReturn(retardsRetours);

        service.extractMaxDLE(retards);

        Mockito.verify(mockDao).selectMaxDLE(Mockito.eq(retards));
    }

    @SuppressWarnings("unchecked")
    @Test
    /**
     * On vérifie juste que le dao est bien appelée
     */
    public void memoriseDLETest1() throws FunctionalException {

        TraitementRetardWork retard = new TraitementRetardWork();
        List<TraitementRetardWork> retards = new ArrayList<>();
        Lt lt = new Lt();
        lt.setNoLt("XX123456X");
        retard.setLt(lt);
        retards.add(retard);

        Mockito.when(mockDao.insertDLE(Mockito.anyList())).thenReturn(true);

        service.memoriseDLE(retards);

        Mockito.verify(mockDao).insertDLE(Mockito.eq(retards));
    }

    /**
     * 
     * 
     * @param retardDetecte
     *            : indicateur de retard detecté (1 pour détecté, 0 pour non
     *            détecté)
     * @param dateActuelle
     *            : Date à partir de laquelle calculer la date de livraison
     *            prévue
     * @param uniteTempsDLE
     *            : (UTDLE) unité de temps à ajouter a la date contractuelle
     *            pour obtenir la DLE
     * @param nbJourFuturDLE
     *            : nombre d'unité de temps (UTDLE) a ajouter à la date
     *            contractuelle pour former la DLE (date livraison prévue)
     * @param uniteTempsDCL
     *            : (UTDCL) unité de temps à ajouter a la date actuelle pour
     *            obtenir la DCL
     * @param nbJourFuturDCL
     *            : nombre d'unité de temps (UTDCL) a ajouter à la date actuelle
     *            pour former la DCL (date contractuelle)
     * @param i
     * @param date
     * @param generationRDConseillee
     *            : indicateur de generation d'événement RD proposée.
     * @return : un objet ResultCalculerRetardPourNumeroLt tel qu'il serait
     *         retourné par le WebService CalculRetard avec les valeur indiquée
     *         en paramètre.
     */
    private ResultCalculerRetardPourNumeroLt newResultCalculerRetardPourNumeroLt(int retardDetecte, Date dateActuelle,
            int uniteTempsDLE, int nbJourFuturDLE, int uniteTempsDCL, int nbJourFuturDCL, String generationRDConseillee) {
        ResultCalculerRetardPourNumeroLt resultDLE = new ResultCalculerRetardPourNumeroLt();

        resultDLE.setAnalyse(new Analyse());
        resultDLE.setCalculDateDeLivraisonEstimee(new CalculDateDeLivraisonEstimee());
        resultDLE.setResultRetard(new ResultRetard());

        /* DCL = maintenant + NBJOURDCL */
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(dateActuelle);
        cal2.add(uniteTempsDCL, nbJourFuturDCL);
        resultDLE.getResultRetard().setDateDeLivraisonPrevue(CALCULRETARD_FMT.format(cal2.getTimeInMillis()));

        /* DLE = (DCL + NBJOURDLE) */
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(dateActuelle);
        cal1.add(uniteTempsDCL, nbJourFuturDCL);
        cal1.add(uniteTempsDLE, nbJourFuturDLE);
        resultDLE.getCalculDateDeLivraisonEstimee().setDateDeLivraisonEstimee(
                CALCULRETARD_FMT.format(cal1.getTimeInMillis())); // DLE
        resultDLE.getCalculDateDeLivraisonEstimee().setHeureMinDeLivraisonEstimee("13:00");
        resultDLE.getCalculDateDeLivraisonEstimee().setHeureMaxDeLivraisonEstimee("14:30");
        resultDLE
                .getCalculDateDeLivraisonEstimee()
                .setLigneParametragePourCalculDateDeLivraisonEstimee(
                        "id=[109] values: ,  , 00:00 , 23:59 , TA ,  , 0 ,  ,  , 0 ,  ,  , 0 ,  ,  ,  ,  ,  , O ,  ,  ,  ,  ,  ,  ,  ,  , 0 , 240 , 240 ,");

        resultDLE.getCalculDateDeLivraisonEstimee().setGenerationRD(generationRDConseillee);
        resultDLE.getAnalyse().setEnRetardDateEstimeeSupDateContractuelle(retardDetecte); // Retard
                                                                                          // détecté

        return resultDLE;
    }

    /**
     *
     * @param noLt
     *            : l'identifiant de la lt
     * @param retardDetecte
     *            : indicateur de retard detecté (1 pour détecté, 0 pour non
     *            détecté)
     * @param uniteTempsDLE
     *            : (UTDLE) unité de temps à ajouter a la date contractuelle
     *            pour obtenir la DLE
     * @param nbJourFuturDLE
     *            : nombre d'unité de temps (UTDLE) a ajouter à la date
     *            contractuelle pour former la DLE (date livraison prévue)
     * @param uniteTempsDCL
     *            : (UTDCL) unité de temps à ajouter a la date actuelle pour
     *            obtenir la DCL
     * @param nbJourFuturDCL
     *            : nombre d'unité de temps (UTDCL) a ajouter à la date actuelle
     *            pour former la DCL (date contractuelle)
     * @param uniteTempsMAXDLE
     *            : (UTMXDLE) unité de temps à ajouter a la DLE pour obtenir la
     *            Max(DLE)
     * @param nbJourFuturMaxDLE
     *            : nombre d'unité de temps (UTMXDLE) a ajouter à la date DLE
     *            pour former la MaxDLE (Plus grand DLE précédemment calculée)
     * @param generationRDConseillee
     *            : Est-ce que la génération d'un événement RD est conseillé par
     *            le WS CalculRetard? ("O" pour oui, "N" pour non)
     * 
     * @return Un objet d'échange de TraitementRetard initialisé pour le test
     */
    private TraitementRetardWork newTraitementRetardWork(String noLt, int retardDetecte, int uniteTempsDLE,
            int nbJourFuturDLE, int uniteTempsDCL, int nbJourFuturDCL, int uniteTempsMAXDLE, int nbJourFuturMaxDLE,
            String generationRDConseillee) {
        /* LT */
        TraitementRetardWork retard = new TraitementRetardWork();
        Lt lt = new Lt();
        lt.setNoLt(noLt);
        retard.setLt(lt);

        /* Max DLE */
        Calendar cal1 = Calendar.getInstance(); // Maintenant
        cal1.add(uniteTempsDCL, nbJourFuturDCL); // Maintenant --> DCL
        cal1.add(uniteTempsDLE, nbJourFuturDLE); // DCL --> DLE
        cal1.add(uniteTempsMAXDLE, nbJourFuturMaxDLE); // DLE --> MaxDLE
        retard.setMaxDLE(new Timestamp(cal1.getTimeInMillis()));

        /* ResultCR */
        Date maintenant = new Date();
        retard.setResultCR(newResultCalculerRetardPourNumeroLt(retardDetecte // retard
                                                                             // détecté
                , maintenant // date actuelle
                , uniteTempsDLE, nbJourFuturDLE // DLE (= dans DCL + 2 jours)
                , uniteTempsDCL, nbJourFuturDCL // DCL (= dans maintenant + 1
                                                // jour)
                , generationRDConseillee // WS CalculRetard propose génération
                                         // d'un RD
        ));

        return retard;
    }

    /**
     * Positionne les valeurs indiquées dans le mock de transco
     * 
     * @param genereEvt_RD_actif
     *            : paramètre d'activation de la génération d'evt RD
     * @param posteComptableEvt_RD
     *            : poste comptable à utilisé pour la génération du RD
     * @throws Exception
     */
    private void setTranscos(final String genereEvt_RD_actif, final String posteComptableEvt_RD) throws Exception {

        ConcurrentMap<String, Transcoder> transcoders = new ConcurrentHashMap<>();
    	Map<String, Map<String, String>> map = new  HashMap<>();
    	
    	Map<String, String> mapEvenements = new HashMap<>();
    	mapEvenements.put("27", "RD|9000|RETARD DETECTE|Acheminement en cours|Retard détecté");
    	map.put("evenements", mapEvenements);
        Map<String, String> transcoParams = new HashMap<>();
        transcoParams.put("poste_comptable_evt_RD", posteComptableEvt_RD);
        map.put("parametre_microservices", transcoParams);
        Transcoder transcoderDiffVision = new Transcoder();
        transcoderDiffVision.setTranscodifications(map);
        transcoders.put("DiffusionVision", transcoderDiffVision);
    	
        Map<String, String> transcosFlips = new HashMap<>();
        transcosFlips.put("genere_evt_RD_actif", genereEvt_RD_actif);
        map.put("feature_flips", transcosFlips);
        Transcoder transcoderVision = new Transcoder();
        transcoderVision.setTranscodifications(map);
        transcoders.put("Vision", transcoderVision);
    	

        Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("DiffusionVision")).thenReturn(map);
        Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("Vision")).thenReturn(map);

        TranscoderService.INSTANCE.setTranscoders(transcoders);
        TranscoderService.INSTANCE.setDao(mockTranscoderDao);
        TranscoderService.INSTANCE.addProjet("DiffusionVision");
        TranscoderService.INSTANCE.addProjet("Vision");
        FeatureFlips.INSTANCE.setFlipProjectName("Vision");
    }

    private int heureDEte(TraitementRetardWork retard) {
        SimpleDateFormat CALCUL_RETARD_DATETIME_FMT = new SimpleDateFormat("dd/MM/yyyy hh:mm");

        try {
            Date dateContractuelle = CALCUL_RETARD_DATETIME_FMT.parse(retard.getResultCR().getResultRetard().getDateDeLivraisonPrevue());
            Date dateEstimee = CALCUL_RETARD_DATETIME_FMT.parse(retard.getResultCR().getCalculDateDeLivraisonEstimee().getDateDeLivraisonEstimee());
            if (dateContractuelle.toString().contains("CET") && dateEstimee.toString().contains("CEST")) return -1 ;
            if (dateContractuelle.toString().contains("CEST") && dateEstimee.toString().contains("CET")) return 1 ;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0 ;
    }
}