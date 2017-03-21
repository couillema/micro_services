package com.chronopost.vision.microservices.insertevt.v1;

import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_DATE_CONTRACTUELLE;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.NamingException;

import org.joda.time.DateTime;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.jms.ITibcoEmsSender;
import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.microservices.sdk.InsertLtV1;
import com.chronopost.vision.microservices.sdk.MaintienIndexEvtV1;
import com.chronopost.vision.microservices.sdk.SuiviBoxV1;
import com.chronopost.vision.microservices.sdk.TraitementRetardV1;
import com.chronopost.vision.microservices.sdk.UpdateTourneeV1;
import com.chronopost.vision.microservices.sdk.utils.ServiceMockResponses;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.model.updatespecificationscolis.v1.EvtEtModifs;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.chronopost.vision.transco.transcoder.Transcoder;
import com.chronopost.vision.ut.RandomUts;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.collect.Multimap;

import fr.chronopost.soap.calculretard.cxf.ResultCalculerRetardPourNumeroLt;
import fr.chronopost.soap.calculretard.cxf.ResultRetard;

/** @author unknown : JJC port */
public class InsertEvtServiceTest {

    private IInsertEvtService service;
    private IInsertEvtDao mockDao = Mockito.mock(IInsertEvtDao.class);
    private ITranscoderDao mockTranscoderDao = Mockito.mock(ITranscoderDao.class);
    private ITibcoEmsSender mockEms = Mockito.mock(ITibcoEmsSender.class);

    private WireMockServer wireMockServer;
    private WireMock wireMock;

    private int httpPort = RandomUts.getRandomHttpPort2(); // ADD JER

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        service = InsertEvtServiceImpl.getInstance().setDao(mockDao)
                .setCalculRetardEndpoint("http://127.0.0.1:" + httpPort + "/CalculRetardWS");

        GetLtV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
        MaintienIndexEvtV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
        InsertLtV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
        UpdateTourneeV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
        SuiviBoxV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
        TraitementRetardV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
        TraitementRetardV1.getInstance().setTimeout(100000);
		
		InsertEvtServiceImpl.getInstance().setEmsSender(mockEms);
		InsertEvtServiceImpl.getInstance().setQueueDestination("sample");

        wireMockServer = new WireMockServer(httpPort);
        wireMockServer.start();
        WireMock.configureFor("127.0.0.1", httpPort);
        wireMock = new WireMock("127.0.0.1", httpPort);

        ConcurrentMap<String, Transcoder> transcoders = new ConcurrentHashMap<>();
    	Map<String, Map<String, String>> map = new  HashMap<>();
        
    	// Mock flux vision
        Map<String, String> mapFlips = new HashMap<>();
        mapFlips.put("Maintien_Index_Evt_Actif", "true");
        mapFlips.put("Diffusion_Evt_Active", "true");
        map.put("feature_flips", mapFlips);
        Transcoder transcosVision = new Transcoder();
        transcosVision.setTranscodifications(map);
        transcoders.put("Vision", transcosVision);
    	
        // Mock flux diffusion vision
        Map<String, String> mapCodePays = new HashMap<>();
        mapCodePays.put("250", "FR|FRANCE");
        map.put("code_pays", mapCodePays);
        Map<String, String> mapParam = new HashMap<>();
        mapParam.put("evt_calcul_retard", "|PC|EC|SD|TA|");
        map.put("parametre_microservices", mapParam);
        Transcoder transcosDiffVision = new Transcoder();
        transcosDiffVision.setTranscodifications(map);
        transcoders.put("DiffusionVision", transcosDiffVision);

        Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("DiffusionVision")).thenReturn(map);
        Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("Vision")).thenReturn(map);

        TranscoderService.INSTANCE.setTranscoders(transcoders);
        TranscoderService.INSTANCE.setDao(mockTranscoderDao);
        TranscoderService.INSTANCE.addProjet("DiffusionVision");
        TranscoderService.INSTANCE.addProjet("Vision");
        FeatureFlips.INSTANCE.setFlipProjectName("Vision");
    }

    /**
     * Test d'insertion d'un evt via le dao.
     * 
     * @throws TimeoutException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     * @throws ParseException
     * @throws NamingException 
     * @throws JMSException 
     * 
     */
    @Test
    public void insertEvtsInDatabaseTest() throws IOException, InterruptedException, ExecutionException,
            TimeoutException, ParseException, JMSException, NamingException {
        wireMock.register(post(urlEqualTo("/GetLTs/true")).withHeader("Content-Type", equalTo("application/json"))
                .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                                .withBody(ServiceMockResponses.readResponse("getltv1_insertevt_response.json"))));

        wireMock.register(post(urlEqualTo("/InsertLT/")).withHeader("Content-Type", equalTo("application/json"))
                .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                                .withBody(ServiceMockResponses.readResponse("insertltv1_insertevt_response.json"))));

        wireMock.register(post(urlEqualTo("/UpdateTournee/v1"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("true")));

        wireMock.register(get(urlMatching("/CalculRetardWS.*")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
                        .withBody(ServiceMockResponses.readResponse("calculretardws.xml"))));

        wireMock.register(post(urlEqualTo("/SuiviBox")).withHeader("Content-Type", equalTo("application/json"))
                .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                                .withBody(ServiceMockResponses.readResponse("insertevtv1_suivibox_response.json"))));

        wireMock.register(post(urlEqualTo("/MaintienIndexEvt/v1")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(ServiceMockResponses.readResponse("maintien_index_evt_ok_response.json"))));

        wireMock.register(post(urlEqualTo("/TraitementRetard"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("true")));

        Mockito.reset(mockDao);
        Mockito.when(mockDao.insertEvts(Arrays.asList(Mockito.any(Evt.class)),Mockito.anyLong())).thenReturn(true);

        List<Evt> evts = Arrays.asList(new Evt().setNoLt("test").setCodeEvt("DC").setDateEvt(new Date()));
        boolean success = service.insertEvts(evts);

        assertTrue(success);
        Mockito.verify(mockDao, Mockito.times(1)).insertEvts(evts,1);
        Mockito.verify(mockEms, Mockito.times(1)).sendMessage(Mockito.anyString(), Mockito.any(Destination.class));
        

    }

    @Test
    public void getNoLtsFromEvtsTest() {
        List<Evt> evts = Arrays.asList(new Evt().setNoLt("EE000000001FR"), new Evt().setNoLt("EE000000002FR"),
                new Evt().setNoLt("EE000000003FR"), new Evt().setNoLt("EE000000002FR"));

        Multimap<String, Evt> evtsParNumeroLt = service.getEvtsParNoLt(evts);
        List<String> numerosDeLt = new ArrayList<>();
        numerosDeLt.addAll(evtsParNumeroLt.keySet());

        assertEquals(numerosDeLt.size(), 3);
        Collections.sort(numerosDeLt);

        assertEquals(numerosDeLt.get(0), "EE000000001FR");
        assertEquals(numerosDeLt.get(1), "EE000000002FR");
        assertEquals(numerosDeLt.get(2), "EE000000003FR");
    }

    @Test
    public void getEvtsEtModifsPourSpecificationsColisTest() throws ParseException, JsonProcessingException {

        HashMap<String, ResultCalculerRetardPourNumeroLt> resultatCalculRetard = new HashMap<>() ;

        List<Evt> evts = new ArrayList<>() ;

        HashMap<String, Lt> lts = new HashMap<>() ;
        Map<String, String> synonymesEtMaitre = new HashMap<>() ;

        {
            // pas de lt ni de calcul retard

            String noLt = "NUMEROLT1" ;

            Evt evt = new Evt()
                    .setDateEvt(DateRules.toDateAndTimeSortable("2015-03-18 22:26:01"))
                    .setNoLt(noLt)
                    .setCodeEvt("D") ;

            evts.add(evt) ;

        }

        {
            // uniquement une lt

            String noLt = "NUMEROLT2" ;

            Evt evt = new Evt()
            .setDateEvt(DateRules.toDateAndTimeSortable("2015-03-18 22:26:02"))
            .setNoLt(noLt)
            .setCodeEvt("D") ;

            Lt lt = new Lt().setNoLt(evt.getNoLt()).setDateLivraisonContractuelle(null) ;

            evts.add(evt) ;
            lts.put(evt.getNoLt(), lt) ;
            synonymesEtMaitre.put(noLt,noLt);

        }

        {
            // uniquement une lt

            String noLt = "NUMEROLT3" ;

            Evt evt = new Evt()
            .setDateEvt(DateRules.toDateAndTimeSortable("2015-03-18 22:26:03"))
            .setNoLt(noLt)
            .setCodeEvt("D") ;

            Lt lt = new Lt().setNoLt(evt.getNoLt()).setDateLivraisonContractuelle(DateRules.toTimestampDateWsCalculRetard("29/02/2016 16:00")) ;

            evts.add(evt) ;
            lts.put(evt.getNoLt(), lt) ;
            synonymesEtMaitre.put(noLt,noLt);

        }

        {

            // Uniquement un calcul retard

            String noLt = "NUMEROLT4" ;

            Evt evt = new Evt()
            .setDateEvt(DateRules.toDateAndTimeSortable("2015-03-18 22:26:04"))
            .setNoLt(noLt)
            .setCodeEvt("D") ;

            ResultCalculerRetardPourNumeroLt calc = new ResultCalculerRetardPourNumeroLt() ;
            calc.setResultRetard(new ResultRetard());
            calc.getResultRetard().setDateDeLivraisonPrevue(null);  

            evts.add(evt) ;
            resultatCalculRetard.put(noLt, calc) ;
            synonymesEtMaitre.put(noLt,noLt);
            
        }

        {

            // Uniquement un calcul retard

            String noLt = "NUMEROLT5" ;

            Evt evt = new Evt()
            .setDateEvt(DateRules.toDateAndTimeSortable("2015-03-18 22:26:05"))
            .setNoLt(noLt)
            .setCodeEvt("D") ;

            ResultCalculerRetardPourNumeroLt calc = new ResultCalculerRetardPourNumeroLt() ;
            calc.setResultRetard(new ResultRetard());
            calc.getResultRetard().setDateDeLivraisonPrevue("04/03/2016 13:57"); 

            evts.add(evt) ;
            resultatCalculRetard.put(noLt, calc) ;
            synonymesEtMaitre.put(noLt,noLt);

        }

        {
            // lt et calcul retard à null

            String noLt = "NUMEROLT6" ;

            Evt evt = new Evt()
            .setDateEvt(DateRules.toDateAndTimeSortable("2015-03-18 22:26:06"))
            .setNoLt(noLt)
            .setCodeEvt("D") ;

            Lt lt = new Lt().setNoLt(noLt).setDateLivraisonContractuelle(null) ;

            ResultCalculerRetardPourNumeroLt calc = new ResultCalculerRetardPourNumeroLt() ;
            calc.setResultRetard(new ResultRetard());
            calc.getResultRetard().setDateDeLivraisonPrevue(null); 

            evts.add(evt) ;
            lts.put(noLt, lt) ;
            resultatCalculRetard.put(noLt, calc) ;
            synonymesEtMaitre.put(noLt,noLt);
        }

        {
            // lt renseigné et calcul retard à null

            String noLt = "NUMEROLT7" ;

            Evt evt = new Evt()
            .setDateEvt(DateRules.toDateAndTimeSortable("2015-03-18 22:26:07"))
            .setNoLt(noLt)
            .setCodeEvt("D") ;

            Lt lt = new Lt().setNoLt(noLt).setDateLivraisonContractuelle(DateRules.toTimestampDateWsCalculRetard("29/02/2016 13:07")) ;

            ResultCalculerRetardPourNumeroLt calc = new ResultCalculerRetardPourNumeroLt() ;
            calc.setResultRetard(new ResultRetard());
            calc.getResultRetard().setDateDeLivraisonPrevue(null); 

            evts.add(evt) ;
            lts.put(noLt, lt) ;
            resultatCalculRetard.put(noLt, calc) ;
            synonymesEtMaitre.put(noLt,noLt);
        }

        {
            // lt null et calcul retard renseigné

            String noLt = "NUMEROLT8" ;

            Evt evt = new Evt()
            .setDateEvt(DateRules.toDateAndTimeSortable("2015-03-18 22:26:08"))
            .setNoLt(noLt)
            .setCodeEvt("D") ;

            Lt lt = new Lt().setNoLt(noLt).setDateLivraisonContractuelle(null) ;

            ResultCalculerRetardPourNumeroLt calc = new ResultCalculerRetardPourNumeroLt() ;
            calc.setResultRetard(new ResultRetard());
            calc.getResultRetard().setDateDeLivraisonPrevue("29/02/2016 13:08"); 

            evts.add(evt) ;
            lts.put(noLt, lt) ;
            resultatCalculRetard.put(noLt, calc) ;
            synonymesEtMaitre.put(noLt,noLt);
        }

        {
            // lt renseigné et calcul retard renseigné identiques

            String noLt = "NUMEROLT9" ;

            Evt evt = new Evt()
            .setDateEvt(DateRules.toDateAndTimeSortable("2015-03-18 22:26:09"))
            .setNoLt(noLt)
            .setCodeEvt("D") ;

            Lt lt = new Lt().setNoLt(noLt).setDateLivraisonContractuelle(DateRules.toTimestampDateWsCalculRetard("29/02/2016 13:09")) ;

            ResultCalculerRetardPourNumeroLt calc = new ResultCalculerRetardPourNumeroLt() ;
            calc.setResultRetard(new ResultRetard());
            calc.getResultRetard().setDateDeLivraisonPrevue("29/02/2016 13:09"); 

            evts.add(evt) ;
            lts.put(noLt, lt) ;
            resultatCalculRetard.put(noLt, calc) ;
            synonymesEtMaitre.put(noLt,noLt);
        }

        {
            // lt renseigné et calcul retard renseigné différents

            String noLt = "NUMEROLT10" ;

            Evt evt = new Evt()
            .setDateEvt(DateRules.toDateAndTimeSortable("2015-03-18 22:26:10"))
            .setNoLt(noLt)
            .setCodeEvt("D") ;

            Lt lt = new Lt().setNoLt(noLt).setDateLivraisonContractuelle(DateRules.toTimestampDateWsCalculRetard("29/02/2016 13:10")) ;

            ResultCalculerRetardPourNumeroLt calc = new ResultCalculerRetardPourNumeroLt() ;
            calc.setResultRetard(new ResultRetard());
            calc.getResultRetard().setDateDeLivraisonPrevue("01/04/2016 13:10"); 

            evts.add(evt) ;
            lts.put(noLt, lt) ;
            resultatCalculRetard.put(noLt, calc) ;
            synonymesEtMaitre.put(noLt,noLt);
        }

        {
            // lt renseigné et calcul retard renseigné différents

            String noLt = "NUMEROLT10" ;

            Evt evt = new Evt()
            .setDateEvt(DateRules.toDateAndTimeSortable("2015-03-18 22:26:10"))
            .setNoLt(noLt)
            .setCodeEvt("D") ;

            Lt lt = new Lt().setNoLt(noLt).setDateLivraisonContractuelle(DateRules.toTimestampDateWsCalculRetard("29/02/2016 13:10")) ;

            ResultCalculerRetardPourNumeroLt calc = new ResultCalculerRetardPourNumeroLt() ;
            calc.setResultRetard(new ResultRetard());
            calc.getResultRetard().setDateDeLivraisonPrevue("01/04/2016 13:10"); 

            evts.add(evt) ;
            lts.put(noLt, lt) ;
            resultatCalculRetard.put(noLt, calc) ;
            synonymesEtMaitre.put(noLt,noLt);
        }

        List<EvtEtModifs> evtsEtModifsPourSpecificationsColis = service.getEvtsEtModifsPourSpecificationsColis(evts, lts, resultatCalculRetard,synonymesEtMaitre);

        assertEquals(evtsEtModifsPourSpecificationsColis.size(), evts.size());
        
        for (EvtEtModifs evtEtmodif : evtsEtModifsPourSpecificationsColis)  {
            Evt evt = evtEtmodif.getEvt() ;
            Map<String, String> modifications = evtEtmodif.getModifications() ;

            if ("NUMEROLT1".equals(evt.getNoLt()))   {
                assertEquals(new DateTime(2015,3,18,22,26,1).toDate(), evt.getDateEvt()) ;
                assertNull(modifications);
            }

            if ("NUMEROLT2".equals(evt.getNoLt()))   {
                assertEquals(new DateTime(2015,3,18,22,26,2).toDate(), evt.getDateEvt()) ;
                assertNull(modifications);
            }

            if ("NUMEROLT3".equals(evt.getNoLt()))   {
                assertEquals(new DateTime(2015,3,18,22,26,3).toDate(), evt.getDateEvt()) ;
                assertNull(modifications);
            }

            if ("NUMEROLT4".equals(evt.getNoLt()))   {
                assertEquals(new DateTime(2015,3,18,22,26,4).toDate(), evt.getDateEvt()) ;
                assertNull(modifications);
            }

            if ("NUMEROLT5".equals(evt.getNoLt()))   {
                assertEquals(new DateTime(2015,3,18,22,26,5).toDate(), evt.getDateEvt()) ;
                assertEquals("04/03/2016 13:57", modifications.get(VALUE_DATE_CONTRACTUELLE)) ;

            }

            if ("NUMEROLT6".equals(evt.getNoLt()))   {
                assertEquals(new DateTime(2015,3,18,22,26,6).toDate(), evt.getDateEvt()) ;
                assertNull(modifications);
            }

            if ("NUMEROLT7".equals(evt.getNoLt()))   {
                assertEquals(new DateTime(2015,3,18,22,26,7).toDate(), evt.getDateEvt()) ;
                assertNull(modifications);
            }

            if ("NUMEROLT8".equals(evt.getNoLt()))   {
                assertEquals(new DateTime(2015,3,18,22,26,8).toDate(), evt.getDateEvt()) ;
                assertEquals("29/02/2016 13:08", modifications.get(VALUE_DATE_CONTRACTUELLE)) ;
            }

            if ("NUMEROLT9".equals(evt.getNoLt()))   {
                assertEquals(new DateTime(2015,3,18,22,26,9).toDate(), evt.getDateEvt()) ;
                assertNotNull(modifications);
            }

            if ("NUMEROLT10".equals(evt.getNoLt()))   {
                assertEquals(new DateTime(2015,3,18,22,26,10).toDate(), evt.getDateEvt()) ;
                assertEquals("01/04/2016 13:10", modifications.get(VALUE_DATE_CONTRACTUELLE)) ;
            }
        
        }

    }
    
    @Test
    public void getLtsPourMiseAJourDepuisEvtTest() throws ParseException, JsonProcessingException {
        Map<String, String> infoscomp = new HashMap<>();
        infoscomp.put("190", "4.55555");
        infoscomp.put("191", "5.66666");
        infoscomp.put("240", "12:00");
        infoscomp.put("193", "AJA20A0100208092015065959");

        Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("NUMEROLT1").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TA")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("13999")
                .setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt")
                .setCodeService("code_service").setIdSsCodeEvt(1).setLibelleLieuEvt("libelle_lieu_evt")
                .setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

        ObjectMapper mapper = new ObjectMapper();

        Evt evt2 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("NUMEROLT2").setCabRecu("%0020090NA146848396248899250").setCodeEvt("D").setCreateurEvt("TRI1")
                .setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191")
                .setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0)
                .setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0").setStatusEvt("Acheminement en cours")
                .setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service")
                .setIdSsCodeEvt(1).setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

        List<Evt> evts = Arrays.asList(evt1, evt2);

        List<Lt> lts = service.getLtsPourMiseAJourDepuisEvt(evts, new HashMap<String, Lt>(),
                new HashMap<String, ResultCalculerRetardPourNumeroLt>());

        assertEquals(lts.size(), 2);
        assertNotNull(lts.get(0).getNoLt());
        assertNotNull(lts.get(0).getCodesEvt());
        assertNotNull(lts.get(0).getEvts());
        assertTrue(lts.get(0).getLatitudePrevue().equals("4.55555"));
        assertTrue(lts.get(0).getLongitudePrevue().equals("5.66666"));
        assertTrue(lts.get(0).getCodeService().equals("899"));
        assertTrue(lts.get(0).getPositionC11().equals("002"));
        assertTrue(lts.get(0).getEta().equals("12:00"));
        assertTrue(lts.get(0).getCodePaysDestinataire().equals("FR"));

        assertNotNull(lts.get(1).getNoLt());
        assertNotNull(lts.get(1).getCodesEvt());
        assertNotNull(lts.get(1).getEvts());
        assertTrue(lts.get(1).getLatitudeDistri().equals("4.55555"));
        assertTrue(lts.get(1).getLongitudeDistri().equals("5.66666"));

        assertTrue(lts.get(1).getCodeService().equals("899"));
        assertTrue(lts.get(1).getCodePaysDestinataire().equals("FR"));

        System.out.println(mapper.writeValueAsString(evt1));

    }

    @Test
    private void filtreEvtColisAIgnorer() {
        // Aucun evt ne doit être filtré dans ce cas, et la liste en sortie doit
        // contenir 2 entrées
        List<Evt> evtsSansFiltrage = InsertEvtServiceImpl.getInstance().filtreEvtColisAIgnorer(
                Arrays.asList(new Evt().setNoLt("YY123456789FR"), new Evt().setNoLt("XX123456789FR")));
        assertEquals(evtsSansFiltrage.size(), 2);

        // 2 evt doivent être filtrés dans ce cas, et la liste en sortie doit
        // contenir 3 entrées
        List<Evt> evtsAvecFiltrage = InsertEvtServiceImpl.getInstance().filtreEvtColisAIgnorer(
                Arrays.asList(new Evt().setNoLt("YY123456789FR"), new Evt().setNoLt("YYY23456789FR"),
                        new Evt().setNoLt("XX123456789FR"), new Evt().setNoLt("YYYFDF456789FR"),
                        new Evt().setNoLt("XX12345DFDFFR"),
                        new Evt().setNoLt("FDFFR")));
        assertEquals(evtsAvecFiltrage.size(), 3);
    }
    
    @Test
    /**
     * Entree: 4 des evts dont 1 qui est a ecarter
     * 
     * Sortie: l'evt a ecarter n'est plus dans la liste retournée
     */
	public void filtreEvtColisAEcarter() {
		List<Evt> evtsSansFiltrage = Arrays.asList(
				new Evt().setNoLt("%0075015MU594561948336925250"),
				new Evt().setNoLt("%000121005258814875518327250"),
				new Evt().setNoLt("%0093150XW583720925248226250"),
				new Evt().setNoLt("%0035135MX980735881248887250"),
				new Evt().setNoLt("%0053440XW584507230248226250"),
				new Evt().setNoLt("%0035033XW584264317248226250"));
		List<Evt> evtFiltres = InsertEvtServiceImpl.getInstance().filtreEvtColisAEcarter(evtsSansFiltrage);
		List<String> lts = new ArrayList<>();
		for (Evt evt : evtFiltres) {
			lts.add(evt.getNoLt());
			System.out.println(evt.getNoLt());
		}
		assertTrue(lts.contains("MU594561948336T"));
		assertTrue(lts.contains("05258814875518N"));
		assertTrue(lts.contains("XW583720925248K"));
		assertTrue(lts.contains("MX980735881248I"));
		assertTrue(lts.contains("XW584507230248A"));
		assertTrue(lts.contains("XW5842643172489"));
	}
}
