package com.chronopost.vision.microservices.tournee.getalertestournees.v1;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.joda.time.DateTime;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.microservices.sdk.utils.ServiceMockResponses;
import com.chronopost.vision.microservices.utils.TypeBorneCreneau;
import com.chronopost.vision.model.getAlertesTournees.v1.GetAlertesTourneesOutput;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.chronopost.vision.transco.transcoder.Transcoder;
import com.chronopost.vision.ut.RandomUts;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

/** @author unknown : JJC getSession . **/
public class GetAlertesTourneesAcceptanceTest {
	
    private boolean suiteLaunch = true;
    private WireMockServer wireMockServer;
    private WireMock wireMock;
	private PreparedStatement psCleanLtCreneauAgence;
    
    private int httpPort = RandomUts.getRandomHttpPort2();

    private IGetAlertesTourneesService service;

    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }
    
    @BeforeClass
    public void setUp() throws Exception {
        if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }
        CCMBridge.ipOfNode(1);

        service = GetAlertesTourneesServiceImpl.getInstance();
        service.setDao(GetAlertesTourneesDaoImpl.getInstance());
        getSession().execute(
                        "INSERT INTO lt_avec_creneau_par_agence (date_jour, code_agence, type_borne_livraison, borne_livraison, no_lt, code_tournee)"
                                + " values('2015-12-07', 'TST', '"
                                + TypeBorneCreneau.BORNE_SUP.getTypeBorne()
                                + "', '2015-12-07 16:00:00', 'EERISQUE003FR','TST00A01')");
        psCleanLtCreneauAgence = getSession().prepare("DELETE FROM lt_avec_creneau_par_agence where date_jour = ? AND code_agence = ?");

        wireMockServer = new WireMockServer(httpPort);
        wireMockServer.start();
        WireMock.configureFor("127.0.0.1", httpPort);
        wireMock = new WireMock("127.0.0.1", httpPort);
        GetLtV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
    }

    /**
     * Vérification qu'on récupère bien le colis à risque inséré dans la table
     * d'index.
     * 
     * @throws Exception
     * 
     */
    @Test(groups = { "database-needed", "slow", "acceptance" })
    public void cas1Test1() throws Exception {
        initFlip("true");
        wireMock.register(post(urlEqualTo("/GetLTs/true")).withHeader("Content-Type", equalTo("application/json"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(
                                        ServiceMockResponses
                                                .readResponse("getltv1_getalertestournees_acceptance_response.json"))));

        GetAlertesTourneesOutput alertes = service.getAlertesTournees(
                Arrays.asList("TST00A01", "TST00A02", "TTT00A00"), new DateTime(2015, 12, 7, 15, 30, 0).toDate());

        assertNotNull(alertes);
        assertNotNull(alertes.getAlertes());
        assertTrue(alertes.getAlertes().get("TST00A01") == 1);
        assertTrue(alertes.getAlertes().get("TST00A02") == 0);
        assertTrue(alertes.getAlertes().get("TTT00A00") == 0);
    }

    private void initFlip(String value) throws Exception {
        ITranscoderDao mockTranscoderDao = Mockito.mock(ITranscoderDao.class);
        // Préparation du mock des transcodifications
        // Transcos pour codes Evt
        ConcurrentMap<String, Transcoder> transcoders = new ConcurrentHashMap<>();
    	Map<String, Map<String, String>> map = new  HashMap<>();
    	Map<String, String> mapFlip = new HashMap<>();
        mapFlip.put("Alertes_Tournees_New_Method_Active", value);
        map.put("feature_flips", mapFlip);
        
        Map<String, String> mapProduit = new HashMap<>();
        mapProduit.put("Rendez-Vous",
                "939 940 941 942 943 944 945 946 947 948 949 950 951 952 959 960 961 962 963 964 969 970 971 972 973 974 975 976 977 978 979 982 983 985 988");
        map.put("type_produit", mapProduit);
        
        Transcoder transcoderVision = new Transcoder();
        transcoderVision.setTranscodifications(map);
        transcoders.put("Vision", transcoderVision);
		TranscoderService.INSTANCE.setTranscoders(transcoders);
    	
        Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("Vision")).thenReturn(map);

        TranscoderService.INSTANCE.setDao(mockTranscoderDao);
        TranscoderService.INSTANCE.addProjet("Vision");
    }

    @AfterClass
    public void tearDownAfterClass() throws Exception {
		getSession().execute(psCleanLtCreneauAgence.bind("2015-12-07", "TST"));
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }
}
