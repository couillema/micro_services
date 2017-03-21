package com.chronopost.vision.microservices.insertevt.v1;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.microservices.sdk.InsertLtV1;
import com.chronopost.vision.microservices.sdk.InsertPointTourneeV1;
import com.chronopost.vision.microservices.sdk.MaintienIndexEvtV1;
import com.chronopost.vision.microservices.sdk.SuiviBoxV1;
import com.chronopost.vision.microservices.sdk.TraitementRetardV1;
import com.chronopost.vision.microservices.sdk.UpdateTourneeV1;
import com.chronopost.vision.microservices.sdk.utils.ServiceMockResponses;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.chronopost.vision.transco.transcoder.Transcoder;
import com.chronopost.vision.ut.RandomUts;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

public class InsertEvtAcceptanceTest {

    private boolean suiteLaunch = true;

    private WireMockServer wireMockServer;
    private WireMock wireMock;

    private ITranscoderDao mockTranscoderDao = Mockito.mock(ITranscoderDao.class);

    private int nbMaintienIndexEvt = 0;

	private PreparedStatement psCleanEvt;
	private PreparedStatement psCleanLt;

    /**
     * @return VisionMicroserviceApplication.cassandraSession (a
     *         com.datastax.driver.core )
     */
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

    /** port http d'écoute du serveur Jersey. */
    private int httpPort = RandomUts.getRandomHttpPort(); // ADD JER

    @BeforeClass(groups = { "init" })
    public void setUp() throws Exception {
        if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }
        CCMBridge.ipOfNode(1);
        wireMockServer = new WireMockServer(httpPort);
        wireMockServer.start();
        WireMock.configureFor("127.0.0.1", httpPort);
        
        psCleanEvt = getSession().prepare("DELETE FROM evt where no_lt = ?");
        psCleanLt = getSession().prepare("DELETE FROM lt where no_lt = ?");

        System.out.println("httpPort : " + httpPort);

        getSession().execute("INSERT INTO lt (no_lt,ref_expediteur,synonyme_maitre) values('EEINSEVT002FR','REFEXPED_EEINSEVT002FR','EEINSEVT003FR')");
        getSession().execute("INSERT INTO lt (no_lt,ref_expediteur,synonyme_maitre) values('EEINSEVT003FR','REFEXPED_EEINSEVT003FR','EEINSEVT003FR')");

        // Mock transco
        ConcurrentMap<String, Transcoder> transcoders = new ConcurrentHashMap<>();
    	Map<String, Map<String, String>> map = new  HashMap<>();
        
    	// nom : feature_flips - entree : Maintien_Index_Evt_Actif - sortie : true
        Map<String, String> mapFlips = new HashMap<>();
        mapFlips.put("Maintien_Index_Evt_Actif", "true");
        mapFlips.put("GestionCAB28", "true");
        mapFlips.put("Insert_Point_Tournee_Actif","true");
        map.put("feature_flips", mapFlips);
        Transcoder transcoderVision = new Transcoder();
        transcoderVision.setTranscodifications(map);
        transcoders.put("Vision", transcoderVision);
    	
        Transcoder transcoderDiffVision = new Transcoder();
    	// nom : parametre_microservices - entree : evt_calcul_retard - sortie : |RD...
        Map<String, String> mapParam = new HashMap<>();
        mapParam.put("evt_calcul_retard", "|RD|AC|AN|BL|CC|CP|CS|CT|D|DE|DC|DY|EA|ER|IX|NN|RO|SF|SM|SR|SV|TE|K|V|O|TO|TT|SJ|H|PH|IM|TR|GC|");
        map.put("parametre_microservices", mapParam);
    	// nom : code_pays - entree : 250 - sortie : FR|FRANCE
    	Map<String, String> mapPays = new HashMap<>();
    	mapPays.put("250", "FR|FRANCE");
    	map.put("code_pays", mapPays);
    	transcoderDiffVision.setTranscodifications(map);
        transcoders.put("DiffusionVision", transcoderDiffVision);

		TranscoderService.INSTANCE.setTranscoders(transcoders);
		
		Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("DiffusionVision")).thenReturn(map);
		Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("Vision")).thenReturn(map);

        TranscoderService.INSTANCE.setDao(mockTranscoderDao);
        TranscoderService.INSTANCE.addProjet("DiffusionVision");
        TranscoderService.INSTANCE.addProjet("Vision");
        FeatureFlips.INSTANCE.setFlipProjectName("Vision");
        
        GetLtV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
        MaintienIndexEvtV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
        InsertLtV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
        UpdateTourneeV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
        SuiviBoxV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
        TraitementRetardV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
        InsertPointTourneeV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
    }
    
    @BeforeMethod
    public void initWiremock() throws FileNotFoundException {
        wireMock = new WireMock("127.0.0.1", httpPort);
    	
        WireMock.reset();
        WireMock.resetAllRequests();
        WireMock.resetAllScenarios();
        InsertEvtServiceImpl.getInstance().resetCalculRetard();

        wireMock.register(get(urlMatching("/CalculRetardWS.*")).willReturn(
                aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(
                                ServiceMockResponses.readResponse("calculretardws.xml").replace(
                                        "http://10.37.92.170/calculretard-cxf/CalculRetardServiceWS", "http://127.0.0.1:" + httpPort + "/CalculRetardWS"))));

        wireMock.register(post(urlMatching("/CalculRetardWS"))
                .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
                                .withBody(ServiceMockResponses.readResponse("calculretard_response.xml"))));

        wireMock.register(post(urlEqualTo("/GetLTs/true")).withHeader("Content-Type", equalTo("application/json")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(ServiceMockResponses.readResponse("getltv1_insertevt_response.json"))));

        wireMock.register(post(urlEqualTo("/GetLTs/small/true")).withHeader("Content-Type", equalTo("application/json")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(ServiceMockResponses.readResponse("getltv1_insertevt_response.json"))));

        wireMock.register(post(urlEqualTo("/SuiviBox")).withHeader("Content-Type", equalTo("application/json")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(ServiceMockResponses.readResponse("insertevtv1_suivibox_response.json"))));

        wireMock.register(post(urlEqualTo("/InsertLT/")).withHeader("Content-Type", equalTo("application/json")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(ServiceMockResponses.readResponse("insertltv1_insertevt_response.json"))));

        wireMock.register(post(urlEqualTo("/UpdateTournee/v1")).withHeader("Content-Type", equalTo("application/json")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("true")));

        wireMock.register(post(urlEqualTo("/TraitementRetard")).withHeader("Content-Type", equalTo("application/json")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("true")));

        wireMock.register(post(urlMatching("/CalculRetardWS"))
                .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
                                .withBody(ServiceMockResponses.readResponse("calculretard_response.xml"))));

        wireMock.register(post(urlEqualTo("/MaintienIndexEvt/v1")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(ServiceMockResponses.readResponse("maintien_index_evt_ok_response.json"))));
        
        wireMock.register(post(urlEqualTo("/InsertPointTournee/v1")).withHeader("Content-Type", equalTo("application/json")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("true")));

    }

    private void initCalculRetardColi262() throws FileNotFoundException {
        wireMock.register(post(urlMatching("/CalculRetardWS")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
                        .withBody(ServiceMockResponses.readResponse("calculretard_response-coli262.xml"))));
    }

    @SuppressWarnings("boxing")
    @Test(groups = { "slow", "acceptance" })
    public void cas1Test1() throws IOException, InterruptedException, ExecutionException, TimeoutException, ParseException, JMSException, NamingException {
        nbMaintienIndexEvt++;

        InsertEvtServiceImpl.getInstance().resetCalculRetard();
        IInsertEvtService service = InsertEvtServiceImpl.getInstance().setDao(InsertEvtDaoImpl.getInstance())
                .setCalculRetardEndpoint("http://127.0.0.1:" + httpPort + "/CalculRetardWS");

        Map<String, String> infoscomp = new HashMap<>();
        infoscomp.put("190", "4.55555");
        infoscomp.put("191", "5.66666");
        infoscomp.put("240", "12:00");
        infoscomp.put("193", "AJA20A0100208092015065959");

        Evt evt = new Evt().setPrioriteEvt(146).setDateEvt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("EEINSEVT001FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TA").setCreateurEvt("TRI1")
                .setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
                .setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction")
                .setStatusEnvoi("status_envoi");

        Boolean result = service.insertEvts(Arrays.asList(evt));

        // Vérification du nombre d'appels au web service CalculRetard
        WireMock.verify(1, WireMock.postRequestedFor(urlMatching("/CalculRetardWS")));

        // Vérification de mise à jour conforme de la lt
		WireMock.verify(1, WireMock.postRequestedFor(urlEqualTo("/InsertLT/")));

        // Vérification du nombre d'appels à GetLt
        WireMock.verify(1, WireMock.postRequestedFor(urlEqualTo("/GetLTs/small/true")));

        // Vérification du nombre d'appels au WSDL du calculRetard
        // WireMock.verify(1,
        // WireMock.getRequestedFor(urlMatching("/CalculRetardWS.*")));

        // Vérification du nombre d'appels au web service CalculRetard
        WireMock.verify(1, WireMock.postRequestedFor(urlMatching("/CalculRetardWS")));

        // Vérification du nombre d'appels au microservice MaintienIndexEvtV1
        WireMock.verify(nbMaintienIndexEvt, WireMock.postRequestedFor(urlEqualTo("/MaintienIndexEvt/v1")));

        ResultSet resultSet = getSession().execute("SELECT * FROM evt where no_lt = 'EEINSEVT001FR'");
        Row row = resultSet.one();

        assertEquals(row.getInt("priorite_evt"), 146);
        assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(row.getTimestamp("date_evt")), "2015-03-18 22:26:00");
        assertEquals(row.getString("no_lt"), "EEINSEVT001FR");
        assertEquals(row.getString("cab_recu"), "%0020090NA146848396248899250");
        assertEquals(row.getString("code_evt"), "TA");
        assertEquals(row.getString("createur_evt"), "TRI1");
        assertEquals(row.getInt("id_acces_client"), 0);
        assertEquals(row.getString("id_extraction_evt"), "717493191");
        assertEquals(row.getInt("idbco_evt"), 88);
        assertEquals(row.getString("libelle_evt"), "Envoi en transit");
        assertEquals(row.getString("lieu_evt"), "93999");
        assertEquals(row.getInt("position_evt"), 0);
        assertEquals(row.getString("ref_id_abonnement"), "EVT_CHR");
        assertEquals(row.getString("ss_code_evt"), "AJA0");
        assertEquals(row.getString("status_evt"), "Acheminement en cours");
        assertEquals(row.getString("code_postal_evt"), "13999");
        assertEquals(row.getString("cab_evt_saisi"), "cab_evt_saisi");
        assertEquals(row.getString("code_evt_ext"), "toto");
        assertEquals(row.getString("code_raison_evt"), "code_raison_evt");
        assertEquals(row.getString("code_service"), "code_service");
        assertEquals(row.getInt("id_ss_code_evt"), 1);
        assertEquals(row.getString("libelle_lieu_evt"), "libelle_lieu_evt");
        assertEquals(row.getInt("prod_cab_evt_saisi"), 1);
        assertEquals(row.getInt("prod_no_lt"), 1);
        assertEquals(row.getString("ref_extraction"), "ref_extraction");
        assertEquals(row.getString("status_envoi"), "status_envoi");

        assertTrue(result);
    }

    @Test(groups = { "slow", "acceptance" })
    public void cas1Test1_CAB_28() throws IOException, InterruptedException, ExecutionException, TimeoutException, ParseException, JMSException, NamingException {
        nbMaintienIndexEvt++;

        InsertEvtServiceImpl.getInstance().resetCalculRetard();
        IInsertEvtService service = InsertEvtServiceImpl.getInstance().setDao(InsertEvtDaoImpl.getInstance())
                .setCalculRetardEndpoint("http://127.0.0.1:" + httpPort + "/CalculRetardWS");

        Map<String, String> infoscomp = new HashMap<>();
        infoscomp.put("190", "4.55555");
        infoscomp.put("191", "5.66666");
        infoscomp.put("240", "12:00");
        infoscomp.put("193", "AJA20A0100208092015065959");

        Evt evt = new Evt().setPrioriteEvt(146).setDateEvt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("%0075015MU594561948336925250").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TA").setCreateurEvt("TRI1")
                .setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
                .setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction")
                .setStatusEnvoi("status_envoi");

        Boolean result = service.insertEvts(Arrays.asList(evt));

        // Vérification du nombre d'appels au web service CalculRetard
        WireMock.verify(1, WireMock.postRequestedFor(urlMatching("/CalculRetardWS")));

        // Vérification de mise à jour conforme de la lt
        WireMock.verify(1, WireMock.postRequestedFor(urlEqualTo("/InsertLT/")));// "[{\"no_lt\":\"MU594561948336T\",\"adresse_1_destinataire\":null,\"adresse_1_expediteur\":null,\"adresse_2_destinataire\":null,\"adresse_2_expediteur\":null,\"article_1\":null,\"cab_evt_saisi\":null,\"cab_recu\":null,\"code_etat_destinataire\":null,\"code_etat_expediteur\":null,\"code_evt\":null,\"code_evt_ext\":null,\"code_pays_destinataire\":\"FR\",\"code_pays_expediteur\":null,\"code_pays_num_destinataire\":null,\"code_pays_num_expediteur\":null,\"code_point_relais\":null,\"code_postal_destinataire\":null,\"code_postal_evt\":null,\"code_postal_expediteur\":null,\"code_produit\":null,\"code_raison_evt\":null,\"code_service\":\"899\",\"codes_evt\":[\"TA\"],\"crbt_rep\":null,\"createur_evt\":null,\"date_creation_evt\":null,\"date_depot_lt\":null,\"date_depot_lt_intern\":null,\"date_entree_si\":null,\"date_evt\":null,\"heure_evt\":null,\"date_heure_saisie\":null,\"date_livraison_contractuelle\":\"2015-03-22T10:00:00.000+0100\",\"date_livraison_prevue\":\"2015-03-23T16:06:00.000+0100\",\"date_modification\":null,\"depot_expediteur\":null,\"description\":null,\"destination_id_fedex\":null,\"devise_assurance\":null,\"devise_rep\":null,\"devise_val_declaree\":null,\"doc_march\":null,\"email_1_destinataire\":null,\"email_1_expediteur\":null,\"email_2_destinataire\":null,\"email_2_expediteur\":null,\"esd\":null,\"hauteur\":null,\"id_abonnement\":null,\"id_acces_client\":null,\"id_appli\":null,\"id_colis_client\":null,\"id_extraction_evt\":null,\"id_ligne\":null,\"id_ss_code_evt\":null,\"idbco_evt\":null,\"infoscomp\":null,\"jour_livraison\":null,\"largeur\":null,\"libelle_evt\":null,\"libelle_lieu_evt\":null,\"lieu_evt\":null,\"longueur\":null,\"no_contrat\":null,\"no_ss_compte\":null,\"nom_1_destinataire\":null,\"nom_1_expediteur\":null,\"nom_2_destinataire\":null,\"nom_2_expediteur\":null,\"origine_saisie\":null,\"poids\":null,\"position_evt\":null,\"priorite_evt\":null,\"prod_cab_evt_saisi\":null,\"prod_no_lt\":null,\"ref_abonnement\":null,\"ref_destinataire\":null,\"ref_expediteur\":null,\"ref_extraction\":null,\"ref_id_abonnement\":null,\"ss_code_evt\":null,\"status_envoi\":null,\"status_evt\":null,\"telephone_destinataire\":null,\"telephone_expediteur\":null,\"val_declaree\":null,\"valeur_assuree\":null,\"valeur_rep\":null,\"ville_destinataire\":null,\"ville_expediteur\":null,\"date_evt_readable\":null,\"eta\":\"12:00\",\"etaMax\":null,\"position_c11\":\"002\",\"positionTournee\":0,\"creneauChargeur\":null,\"creneauTournee\":null,\"latitudePrevue\":\"4.55555\",\"longitudePrevue\":\"5.66666\",\"latitudeDistri\":null,\"longitudeDistri\":null,\"evts\":[\"000201|2015-03-18 22:26:00|EEINSEVT001FR|cab_evt_saisi|%0020090NA146848396248899250|TA|toto|13999|code_raison_evt|code_service|TRI1|2015-09-01T22:34:56|0|717493191|1|88|Envoi en transit|libelle_lieu_evt|93999|0|1|1|ref_extraction|EVT_CHR|AJA0|status_envoi|Acheminement en cours|{'190':'4.55555','191':'5.66666','193':'AJA20A0100208092015065959','240':'12:00'}\"],\"retardEta\":0,\"evenements\":null,\"synonymes\":null,\"heureMaxLivraison\":null,\"creneauTourneeRecopie\":false,\"synonyme_maitre\":null,\"idx_depassement\":\"2015-03-22__1\"}]")));

        // Vérification du nombre d'appels à GetLt
        WireMock.verify(1, WireMock.postRequestedFor(urlEqualTo("/GetLTs/small/true")));

        // Vérification du nombre d'appels au microservice MaintienIndexEvtV1
        WireMock.verify(1, WireMock.postRequestedFor(urlEqualTo("/MaintienIndexEvt/v1")));

        ResultSet resultSet = getSession().execute("SELECT * FROM evt where no_lt = 'MU594561948336T'");
        Row row = resultSet.one();

        assertEquals(row.getInt("priorite_evt"), 146);
        assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(row.getTimestamp("date_evt")), "2015-03-18 22:26:00");
        assertEquals(row.getString("no_lt"), "MU594561948336T");
        assertEquals(row.getString("cab_recu"), "%0020090NA146848396248899250");
        assertEquals(row.getString("code_evt"), "TA");
        assertEquals(row.getString("createur_evt"), "TRI1");
        assertEquals(row.getInt("id_acces_client"), 0);
        assertEquals(row.getString("id_extraction_evt"), "717493191");
        assertEquals(row.getInt("idbco_evt"), 88);
        assertEquals(row.getString("libelle_evt"), "Envoi en transit");
        assertEquals(row.getString("lieu_evt"), "93999");
        assertEquals(row.getInt("position_evt"), 0);
        assertEquals(row.getString("ref_id_abonnement"), "EVT_CHR");
        assertEquals(row.getString("ss_code_evt"), "AJA0");
        assertEquals(row.getString("status_evt"), "Acheminement en cours");
        assertEquals(row.getString("code_postal_evt"), "13999");
        assertEquals(row.getString("cab_evt_saisi"), "cab_evt_saisi");
        assertEquals(row.getString("code_evt_ext"), "toto");
        assertEquals(row.getString("code_raison_evt"), "code_raison_evt");
        assertEquals(row.getString("code_service"), "code_service");
        assertEquals(row.getInt("id_ss_code_evt"), 1);
        assertEquals(row.getString("libelle_lieu_evt"), "libelle_lieu_evt");
        assertEquals(row.getInt("prod_cab_evt_saisi"), 1);
        assertEquals(row.getInt("prod_no_lt"), 1);
        assertEquals(row.getString("ref_extraction"), "ref_extraction");
        assertEquals(row.getString("status_envoi"), "status_envoi");

        assertTrue(result);
    }
    
    
    @Test(groups = { "slow", "acceptance" }, expectedExceptions = MSTechnicalException.class)
    /**
     * Verifie que si une commande qui appel un sous evts (insertPointTournee) retourne un false (error 500),
     * alors une exception est bien remontée par insertEvt.
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws ParseException
     * @throws JMSException
     * @throws NamingException
     * @throws com.chronopost.vision.microservices.exception.TechnicalException
     * 
     * @author lguay
     */
	public void cas1Test5_error_insertPointTournee() throws IOException, InterruptedException, ExecutionException,
			TimeoutException, ParseException, JMSException, NamingException, MSTechnicalException {
        nbMaintienIndexEvt++;

        InsertEvtServiceImpl.getInstance().resetCalculRetard();
        IInsertEvtService service = InsertEvtServiceImpl.getInstance().setDao(InsertEvtDaoImpl.getInstance())
                .setCalculRetardEndpoint("http://127.0.0.1:" + httpPort + "/CalculRetardWS");

        wireMock.register(post(urlEqualTo("/InsertPointTournee/v1")).withHeader("Content-Type", equalTo("application/json")).willReturn(
                aResponse().withStatus(500).withHeader("Content-Type", "application/json").withBody("false")));

        Map<String, String> infoscomp = new HashMap<>();
        infoscomp.put("190", "4.55555");
        infoscomp.put("191", "5.66666");
        infoscomp.put("240", "12:00");
        infoscomp.put("193", "AJA20A0100208092015065959");

        Evt evt = new Evt().setPrioriteEvt(146).setDateEvt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("%0075015MU594561948336925250").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TA").setCreateurEvt("TRI1")
                .setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
                .setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction")
                .setStatusEnvoi("status_envoi");

        service.insertEvts(Arrays.asList(evt));

        /* On ne doit pas arrivé ici car une exception doit être remontée */
        assertTrue(false);
    }

    @Test(groups = { "slow", "acceptance" })
    public void cas2Test1() throws IOException, InterruptedException, ExecutionException, TimeoutException, ParseException, JMSException, NamingException {
        InsertEvtServiceImpl.getInstance().resetCalculRetard();
        IInsertEvtService service = InsertEvtServiceImpl.getInstance().setDao(InsertEvtDaoImpl.getInstance())
                .setCalculRetardEndpoint("http://127.0.0.1:" + httpPort + "/CalculRetardWS");

        Map<String, String> infoscomp = new HashMap<>();
        infoscomp.put("190", "4.55555");
        infoscomp.put("191", "5.66666");
        infoscomp.put("240", "12:00");
        infoscomp.put("193", "AJA20A0100208092015065959");

        Evt evt = new Evt().setPrioriteEvt(146).setDateEvt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("EEINSEVT002FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("D").setCreateurEvt("TRI1")
                .setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
                .setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction")
                .setStatusEnvoi("status_envoi");

        Boolean result = service.insertEvts(Arrays.asList(evt));

        WireMock.verify(1, WireMock.postRequestedFor(urlEqualTo("/InsertLT/"))); // "[{\"no_lt\":\"EEINSEVT003FR\",\"adresse_1_destinataire\":null,\"adresse_1_expediteur\":null,\"adresse_2_destinataire\":null,\"adresse_2_expediteur\":null,\"article_1\":null,\"cab_evt_saisi\":null,\"cab_recu\":null,\"code_etat_destinataire\":null,\"code_etat_expediteur\":null,\"code_evt\":null,\"code_evt_ext\":null,\"code_pays_destinataire\":\"FR\",\"code_pays_expediteur\":null,\"code_pays_num_destinataire\":null,\"code_pays_num_expediteur\":null,\"code_point_relais\":null,\"code_postal_destinataire\":null,\"code_postal_evt\":null,\"code_postal_expediteur\":null,\"code_produit\":null,\"code_raison_evt\":null,\"code_service\":\"899\",\"codes_evt\":[\"D\"],\"crbt_rep\":null,\"createur_evt\":null,\"date_creation_evt\":null,\"date_depot_lt\":null,\"date_depot_lt_intern\":null,\"date_entree_si\":null,\"date_evt\":null,\"heure_evt\":null,\"date_heure_saisie\":null,\"date_livraison_contractuelle\":null,\"date_livraison_prevue\":null,\"date_modification\":null,\"depot_expediteur\":null,\"description\":null,\"destination_id_fedex\":null,\"devise_assurance\":null,\"devise_rep\":null,\"devise_val_declaree\":null,\"doc_march\":null,\"email_1_destinataire\":null,\"email_1_expediteur\":null,\"email_2_destinataire\":null,\"email_2_expediteur\":null,\"esd\":null,\"hauteur\":null,\"id_abonnement\":null,\"id_acces_client\":null,\"id_appli\":null,\"id_colis_client\":null,\"id_extraction_evt\":null,\"id_ligne\":null,\"id_ss_code_evt\":null,\"idbco_evt\":null,\"infoscomp\":null,\"jour_livraison\":null,\"largeur\":null,\"libelle_evt\":null,\"libelle_lieu_evt\":null,\"lieu_evt\":null,\"longueur\":null,\"no_contrat\":null,\"no_ss_compte\":null,\"nom_1_destinataire\":null,\"nom_1_expediteur\":null,\"nom_2_destinataire\":null,\"nom_2_expediteur\":null,\"origine_saisie\":null,\"poids\":null,\"position_evt\":null,\"priorite_evt\":null,\"prod_cab_evt_saisi\":null,\"prod_no_lt\":null,\"ref_abonnement\":null,\"ref_destinataire\":null,\"ref_expediteur\":null,\"ref_extraction\":null,\"ref_id_abonnement\":null,\"ss_code_evt\":null,\"status_envoi\":null,\"status_evt\":null,\"telephone_destinataire\":null,\"telephone_expediteur\":null,\"val_declaree\":null,\"valeur_assuree\":null,\"valeur_rep\":null,\"ville_destinataire\":null,\"ville_expediteur\":null,\"date_evt_readable\":null,\"eta\":null,\"etaMax\":null,\"position_c11\":null,\"positionTournee\":0,\"creneauChargeur\":null,\"creneauTournee\":null,\"latitudePrevue\":null,\"longitudePrevue\":null,\"latitudeDistri\":\"4.55555\",\"longitudeDistri\":\"5.66666\",\"evts\":[\"000201|2015-03-18 22:26:00|EEINSEVT003FR|cab_evt_saisi|%0020090NA146848396248899250|D|toto|13999|code_raison_evt|226|TRI1|2015-09-01T22:34:56|0|717493191|1|88|Envoi en transit|libelle_lieu_evt|93999|0|1|1|ref_extraction|EVT_CHR|AJA0|status_envoi|Acheminement en cours|{'190':'4.55555','191':'5.66666','193':'AJA20A0100208092015065959','240':'12:00'}\"],\"retardEta\":0,\"evenements\":null,\"synonymes\":null,\"heureMaxLivraison\":null,\"creneauTourneeRecopie\":false,\"synonyme_maitre\":null,\"idx_depassement\":null}]")));
        WireMock.verify(1, WireMock.postRequestedFor(urlEqualTo("/GetLTs/small/true")));
        WireMock.verify(0, WireMock.postRequestedFor(urlMatching("/CalculRetardWS")));

        // Vérification du nombre d'appels au microservice MaintienIndexEvtV1
        WireMock.verify(1, WireMock.postRequestedFor(urlEqualTo("/MaintienIndexEvt/v1")));

        ResultSet resultSet = getSession().execute("SELECT * FROM evt where no_lt = 'EEINSEVT002FR'");
        assertNull(resultSet.one());

        resultSet = getSession().execute("SELECT * FROM evt where no_lt = 'EEINSEVT003FR'");
        Row row = resultSet.one();

        assertEquals(row.getInt("priorite_evt"), 146);
        assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(row.getTimestamp("date_evt")), "2015-03-18 22:26:00");
        assertEquals(row.getString("no_lt"), "EEINSEVT003FR");
        assertEquals(row.getString("cab_recu"), "%0020090NA146848396248899250");
        assertEquals(row.getString("code_evt"), "D");
        assertEquals(row.getString("createur_evt"), "TRI1");
        assertEquals(row.getInt("id_acces_client"), 0);
        assertEquals(row.getString("id_extraction_evt"), "717493191");
        assertEquals(row.getInt("idbco_evt"), 88);
        assertEquals(row.getString("libelle_evt"), "Envoi en transit");
        assertEquals(row.getString("lieu_evt"), "93999");
        assertEquals(row.getInt("position_evt"), 0);
        assertEquals(row.getString("ref_id_abonnement"), "EVT_CHR");
        assertEquals(row.getString("ss_code_evt"), "AJA0");
        assertEquals(row.getString("status_evt"), "Acheminement en cours");
        assertEquals(row.getString("code_postal_evt"), "13999");
        assertEquals(row.getString("cab_evt_saisi"), "cab_evt_saisi");
        assertEquals(row.getString("code_evt_ext"), "toto");
        assertEquals(row.getString("code_raison_evt"), "code_raison_evt");
        assertEquals(row.getString("code_service"), "226");
        assertEquals(row.getInt("id_ss_code_evt"), 1);
        assertEquals(row.getString("libelle_lieu_evt"), "libelle_lieu_evt");
        assertEquals(row.getInt("prod_cab_evt_saisi"), 1);
        assertEquals(row.getInt("prod_no_lt"), 1);
        assertEquals(row.getString("ref_extraction"), "ref_extraction");
        assertEquals(row.getString("status_envoi"), "status_envoi");
        for (String key : infoscomp.keySet()) {
            assertEquals(row.getMap("infoscomp", String.class, String.class).get(key), infoscomp.get(key));
        }

        assertTrue(result);
    }

    /**
     * Test du cas où 2 evt distincts pour un même colis ont la même date. On
     * vérifie que les 2 evts sont bien dans la table vision.evt en sortie.
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws ParseException
     * @throws NamingException
     * @throws JMSException
     */
    @Test(groups = { "slow", "acceptance" })
    public void cas3Test1() throws IOException, InterruptedException, ExecutionException, TimeoutException, ParseException, JMSException, NamingException {
        InsertEvtServiceImpl.getInstance().resetCalculRetard();
        IInsertEvtService service = InsertEvtServiceImpl.getInstance().setDao(InsertEvtDaoImpl.getInstance())
                .setCalculRetardEndpoint("http://127.0.0.1:" + httpPort + "/CalculRetardWS");

        Map<String, String> infoscomp = new HashMap<>();
        infoscomp.put("190", "4.55555");
        infoscomp.put("191", "5.66666");
        infoscomp.put("240", "12:00");
        infoscomp.put("193", "AJA20A0100208092015065959");
        Date dateEvt = new Date();

        Evt evt = new Evt().setPrioriteEvt(200).setDateEvt(dateEvt).setNoLt("EEINSEVT0C3FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("D")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
                .setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction")
                .setStatusEnvoi("status_envoi");

        Evt evt2 = new Evt().setPrioriteEvt(1000).setDateEvt(dateEvt).setNoLt("EEINSEVT0C3FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TA")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(80)
                .setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction")
                .setStatusEnvoi("status_envoi");

        service.insertEvts(Arrays.asList(evt, evt2));

        // Vérification du nombre d'appels au microservice MaintienIndexEvtV1
        WireMock.verify(1, WireMock.postRequestedFor(urlEqualTo("/MaintienIndexEvt/v1")));

        ResultSet resultSet = getSession().execute("SELECT * FROM evt where no_lt = 'EEINSEVT0C3FR'");
        int i = 0;
        for (Row row : resultSet) {
            // on vérifie que l'evt significatif est retourné en premier
            if (i == 0) {
                assertEquals(row.getString("code_evt"), "D");
            }
            if (i == 1) {
                assertEquals(row.getString("code_evt"), "TA");
            }
            ++i;
        }

        // On vérifie qu'on n'a 2 evt en sortie
        assertTrue(i == 2);

    }

    /**
     * Test du cas où 2 evt identiques mais avec des infoscomp différentes pour
     * un même colis ont la même date. On vérifie que les 2 evts sont bien dans
     * la table vision.evt en sortie.
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws ParseException
     * @throws NamingException
     * @throws JMSException
     */
    @Test(groups = { "slow", "acceptance" })
    public void cas3Test2() throws IOException, InterruptedException, ExecutionException, TimeoutException, ParseException, JMSException, NamingException {
        InsertEvtServiceImpl.getInstance().resetCalculRetard();
        IInsertEvtService service = InsertEvtServiceImpl.getInstance().setDao(InsertEvtDaoImpl.getInstance())
                .setCalculRetardEndpoint("http://127.0.0.1:" + httpPort + "/CalculRetardWS");

        Map<String, String> infoscomp = new HashMap<>();
        infoscomp.put("190", "4.55555");
        infoscomp.put("191", "5.66666");
        infoscomp.put("240", "12:00");
        infoscomp.put("193", "AJA20A0100208092015065959");

        Map<String, String> infoscomp2 = new HashMap<>();
        infoscomp.put("190", "4.55555");
        infoscomp.put("191", "5.66666");
        infoscomp.put("240", "13:00"); // différent du premier groupe
                                       // d'infoscomp
        infoscomp.put("193", "AJA20A0100208092015065959");
        Date dateEvt = new Date();

        Evt evt = new Evt().setPrioriteEvt(200).setDateEvt(dateEvt).setNoLt("EEINSEVT2C3FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("D")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
                .setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction")
                .setStatusEnvoi("status_envoi");

        Evt evt2 = new Evt().setPrioriteEvt(200).setDateEvt(dateEvt).setNoLt("EEINSEVT2C3FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("D")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
                .setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp2).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction")
                .setStatusEnvoi("status_envoi");

        service.insertEvts(Arrays.asList(evt, evt2));

        ResultSet resultSet = getSession().execute("SELECT * FROM evt where no_lt = 'EEINSEVT2C3FR'");
        int i = 0;
        for (Row row : resultSet) {
            // on vérifie que l'evt significatif est retourné en premier
            if (i == 0) {
                assertEquals(row.getString("code_evt"), "D");
            }
            if (i == 1) {
                assertEquals(row.getString("code_evt"), "D");
            }
            ++i;
        }

        // On vérifie qu'on n'a 2 evt en sortie
        assertTrue(i == 2);

    }

    /**
     * Test du cas où 2 evt strictement identiques sont chargés. On vérifie que
     * seul 1 evt est présent dans la table vision.evt en sortie.
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws ParseException
     * @throws NamingException
     * @throws JMSException
     */
    @Test(groups = { "slow", "acceptance" })
    public void cas3Test3() throws IOException, InterruptedException, ExecutionException, TimeoutException, ParseException, JMSException, NamingException {
        InsertEvtServiceImpl.getInstance().resetCalculRetard();
        IInsertEvtService service = InsertEvtServiceImpl.getInstance().setDao(InsertEvtDaoImpl.getInstance())
                .setCalculRetardEndpoint("http://127.0.0.1:" + httpPort + "/CalculRetardWS");

        Map<String, String> infoscomp = new HashMap<>();
        infoscomp.put("190", "4.55555");
        infoscomp.put("191", "5.66666");
        infoscomp.put("240", "12:00");
        infoscomp.put("193", "AJA20A0100208092015065959");

        Date dateEvt = new Date();

        Evt evt = new Evt().setPrioriteEvt(200).setDateEvt(dateEvt).setNoLt("EEINSEVT3C3FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("D")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
                .setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction")
                .setStatusEnvoi("status_envoi");

        Evt evt2 = new Evt().setPrioriteEvt(200).setDateEvt(dateEvt).setNoLt("EEINSEVT3C3FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("D")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
                .setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction")
                .setStatusEnvoi("status_envoi");

        service.insertEvts(Arrays.asList(evt, evt2));

        ResultSet resultSet = getSession().execute("SELECT * FROM evt where no_lt = 'EEINSEVT3C3FR'");
        int i = 0;
        for (Row row : resultSet) {
            // on vérifie que l'evt significatif est retourné en premier
            assertEquals(row.getString("code_evt"), "D");

            ++i;
        }

        // On vérifie qu'on n'a qu'un evt en sortie
        assertTrue(i == 1);
    }

    /**
     * Vérification de la prédictibilité du hash de dédoublonnage.
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws ParseException
     * @throws NamingException
     * @throws JMSException
     */
    @Test(groups = { "slow", "acceptance" })
    public void cas4Test1() throws IOException, InterruptedException, ExecutionException, TimeoutException, ParseException, JMSException, NamingException {
        InsertEvtServiceImpl.getInstance().resetCalculRetard();
        IInsertEvtService service = InsertEvtServiceImpl.getInstance().setDao(InsertEvtDaoImpl.getInstance())
                .setCalculRetardEndpoint("http://127.0.0.1:" + httpPort + "/CalculRetardWS");

        Map<String, String> infoscomp = new HashMap<>();
        infoscomp.put("190", "4.55555");
        infoscomp.put("191", "5.66666");
        infoscomp.put("240", "12:00");
        infoscomp.put("193", "AJA20A0100208092015065959");

        Date dateEvt = new Date(0);

        Evt evt = new Evt().setPrioriteEvt(200).setDateEvt(dateEvt).setNoLt("EEINSEVT1C4FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("D")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
                .setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction")
                .setStatusEnvoi("status_envoi");

        service.insertEvts(Arrays.asList(evt));

        ResultSet resultSet = getSession().execute("SELECT * FROM evt where no_lt = 'EEINSEVT1C4FR'");
        int i = 0;
        for (Row row : resultSet) {
            // on vérifie que l'evt significatif est retourné en premier
            assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(row.getTimestamp("date_evt")), "1970-01-01 01:00:00.565");

            ++i;
        }

        // On vérifie qu'on n'a qu'un evt en sortie
        assertTrue(i == 1);
    }

    /**
     * La date de livraison contractuelle et la date de livraison prévue ne sont
     * plus updatées dans la table lt.
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws ParseException
     * @throws NamingException
     * @throws JMSException
     */
    @Test(groups = { "slow", "acceptance" })
    public void coli262Test() throws IOException, InterruptedException, ExecutionException, TimeoutException, ParseException, JMSException, NamingException {
        final String noLtTest = "EEINSEVT262FR";

        initCalculRetardColi262();

        InsertEvtServiceImpl.getInstance().resetCalculRetard();
        IInsertEvtService service = InsertEvtServiceImpl.getInstance().setDao(InsertEvtDaoImpl.getInstance())
                .setCalculRetardEndpoint("http://127.0.0.1:" + httpPort + "/CalculRetardWS");

        Map<String, String> infoscomp = new HashMap<>();
        infoscomp.put("190", "4.55555");
        infoscomp.put("191", "5.66666");
        infoscomp.put("240", "12:00");
        infoscomp.put("193", "AJA20A0100208092015065959");

        Date dateEvt = new Date(0);

        Evt evt = new Evt().setPrioriteEvt(200).setDateEvt(dateEvt).setNoLt(noLtTest).setCabRecu("%0020090NA146848396248899250").setCodeEvt("TA")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
                .setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setIdSsCodeEvt(1).setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1)
                .setProdNoLt(1).setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

        service.insertEvts(Arrays.asList(evt));

        ResultSet resultSet = getSession().execute("SELECT * FROM evt where no_lt = '" + noLtTest + "'");
        int i = 0;
        for (Row row : resultSet) {
            // on vérifie que l'evt significatif est retourné en premier
            assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(row.getTimestamp("date_evt")), "1970-01-01 01:00:00.353");

            ++i;
        }

        // On vérifie qu'on n'a qu'un evt en sortie
        assertTrue(i == 1);

        WireMock.verify(1,
                WireMock.postRequestedFor(urlEqualTo("/InsertLT/"))); // "[{\"no_lt\":\"EEINSEVT003FR\",\"adresse_1_destinataire\":null,\"adresse_1_expediteur\":null,\"adresse_2_destinataire\":null,\"adresse_2_expediteur\":null,\"article_1\":null,\"cab_evt_saisi\":null,\"cab_recu\":null,\"code_etat_destinataire\":null,\"code_etat_expediteur\":null,\"code_evt\":null,\"code_evt_ext\":null,\"code_pays_destinataire\":\"FR\",\"code_pays_expediteur\":null,\"code_pays_num_destinataire\":null,\"code_pays_num_expediteur\":null,\"code_point_relais\":null,\"code_postal_destinataire\":null,\"code_postal_evt\":null,\"code_postal_expediteur\":null,\"code_produit\":null,\"code_raison_evt\":null,\"code_service\":\"899\",\"codes_evt\":[\"D\"],\"crbt_rep\":null,\"createur_evt\":null,\"date_creation_evt\":null,\"date_depot_lt\":null,\"date_depot_lt_intern\":null,\"date_entree_si\":null,\"date_evt\":null,\"heure_evt\":null,\"date_heure_saisie\":null,\"date_livraison_contractuelle\":null,\"date_livraison_prevue\":null,\"date_modification\":null,\"depot_expediteur\":null,\"description\":null,\"destination_id_fedex\":null,\"devise_assurance\":null,\"devise_rep\":null,\"devise_val_declaree\":null,\"doc_march\":null,\"email_1_destinataire\":null,\"email_1_expediteur\":null,\"email_2_destinataire\":null,\"email_2_expediteur\":null,\"esd\":null,\"hauteur\":null,\"id_abonnement\":null,\"id_acces_client\":null,\"id_appli\":null,\"id_colis_client\":null,\"id_extraction_evt\":null,\"id_ligne\":null,\"id_ss_code_evt\":null,\"idbco_evt\":null,\"infoscomp\":null,\"jour_livraison\":null,\"largeur\":null,\"libelle_evt\":null,\"libelle_lieu_evt\":null,\"lieu_evt\":null,\"longueur\":null,\"no_contrat\":null,\"no_ss_compte\":null,\"nom_1_destinataire\":null,\"nom_1_expediteur\":null,\"nom_2_destinataire\":null,\"nom_2_expediteur\":null,\"origine_saisie\":null,\"poids\":null,\"position_evt\":null,\"priorite_evt\":null,\"prod_cab_evt_saisi\":null,\"prod_no_lt\":null,\"ref_abonnement\":null,\"ref_destinataire\":null,\"ref_expediteur\":null,\"ref_extraction\":null,\"ref_id_abonnement\":null,\"ss_code_evt\":null,\"status_envoi\":null,\"status_evt\":null,\"telephone_destinataire\":null,\"telephone_expediteur\":null,\"val_declaree\":null,\"valeur_assuree\":null,\"valeur_rep\":null,\"ville_destinataire\":null,\"ville_expediteur\":null,\"date_evt_readable\":null,\"eta\":null,\"etaMax\":null,\"position_c11\":null,\"positionTournee\":0,\"creneauChargeur\":null,\"creneauTournee\":null,\"latitudePrevue\":null,\"longitudePrevue\":null,\"latitudeDistri\":\"4.55555\",\"longitudeDistri\":\"5.66666\",\"evts\":[\"000201|2015-03-18 22:26:00|EEINSEVT003FR|cab_evt_saisi|%0020090NA146848396248899250|D|toto|13999|code_raison_evt|226|TRI1|2015-09-01T22:34:56|0|717493191|1|88|Envoi en transit|libelle_lieu_evt|93999|0|1|1|ref_extraction|EVT_CHR|AJA0|status_envoi|Acheminement en cours|{'190':'4.55555','191':'5.66666','193':'AJA20A0100208092015065959','240':'12:00'}\"],\"retardEta\":0,\"evenements\":null,\"synonymes\":null,\"heureMaxLivraison\":null,\"creneauTourneeRecopie\":false,\"synonyme_maitre\":null,\"idx_depassement\":null}]")));
    }

    @AfterClass
    public void tearDownAfterClass() throws Exception {
		getSession().execute(psCleanEvt.bind("EEINSEVT001FR"));
		getSession().execute(psCleanEvt.bind("EEINSEVT003FR"));
		getSession().execute(psCleanEvt.bind("EEINSEVT0C3FR"));
		getSession().execute(psCleanEvt.bind("EEINSEVT1C4FR"));
		getSession().execute(psCleanEvt.bind("EEINSEVT262FR"));
		getSession().execute(psCleanEvt.bind("EEINSEVT2C3FR"));
		getSession().execute(psCleanEvt.bind("EEINSEVT3C3FR"));
		getSession().execute(psCleanEvt.bind("MU594561948336T"));
		getSession().execute(psCleanLt.bind("EEINSEVT003FR"));
		getSession().execute(psCleanLt.bind("EEINSEVT002FR"));
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
    }
}
