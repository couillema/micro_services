package com.chronopost.vision.microservices.tournee.getdetailtournee.v1;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.microservices.sdk.utils.ServiceMockResponses;
import com.chronopost.vision.model.DetailTournee;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.ut.RandomUts;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

/** @author unknown : JJC getSession + LOGGER import min. **/
public class GetDetailTourneeAcceptanceTest {

    private boolean suiteLaunch = true;

    private WireMockServer wireMockServer;
    private WireMock wireMock;
	private PreparedStatement psCleanInfoTournee;
	private PreparedStatement psCleanTournees;

    /**
     * @return VisionMicroserviceApplication.cassandraSession (a
     *         com.datastax.driver.core )
     */
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

    /**
     * port http d'écoute du serveur Wiremock.
     */
    private int httpPort;

    @BeforeClass
    public void setUp() throws Exception {

        if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }
        CCMBridge.ipOfNode(1);

        getSession()
                .execute(
                        "INSERT INTO tournees(date_jour,code_tournee,collecte,distri,ta, informations) VALUES ('2015-07-30','AJA20M71',{'AA000000006FR','AA000000007FR'},{'AA000000003FR','AA000000004FR','AA000000005FR'},{ 'AA000000001FR','AA000000002FR'}, {'debut':'2015-07-30 07:00:00','fin':'2015-07-30 15:00:00'});");

        // insertion des positions GPS
        getSession()
                .execute(
                        "INSERT INTO info_tournee(code_tournee,type_information,date_heure_transmission,id_information,date_jour,informations) VALUES ('AJA20M71','position','2015-07-30 11:00:00','f8084a41-8ab2-4658-93d2-fe941303b28b','2015-07-30',{'idC11':'20M7130072015073833','latitude':'41.9128766','longitude':'8.72515166','numero_dernier_point':'036'});");
        getSession()
                .execute(
                        "INSERT INTO info_tournee(code_tournee,type_information,date_heure_transmission,id_information,date_jour,informations) VALUES ('AJA20M71','position','2015-07-30 11:05:00','eace2488-c39d-4c57-bf85-a217b483a063','2015-07-30',{'idC11':'20M7130072015073833','latitude':'41.9082083','longitude':'8.63431','numero_dernier_point':'032'});");
        getSession()
                .execute(
                        "INSERT INTO info_tournee(code_tournee,type_information,date_heure_transmission,id_information,date_jour,informations) VALUES ('AJA20M71','position','2015-07-30 11:10:00','ef930a21-2277-4737-be66-f5ac791e30f1','2015-07-30',{'idC11':'20M7130072015073833','latitude':'41.910295','longitude':'8.63820833','numero_dernier_point':'031'});");
        getSession()
                .execute(
                        "INSERT INTO info_tournee(code_tournee,type_information,date_heure_transmission,id_information,date_jour,informations) VALUES ('AJA20M71','position','2015-07-30 11:15:00','415dc7b8-582e-427f-9e81-f5e3a3358c3b','2015-07-30',{'idC11':'20M7130072015073833','latitude':'41.9080533','longitude':'8.66312166','numero_dernier_point':'021'});");
        getSession()
                .execute(
                        "INSERT INTO info_tournee(code_tournee,type_information,date_heure_transmission,id_information,date_jour,informations) VALUES ('AJA20M71','position','2015-07-30 11:20:00','b022e4fe-8d39-4480-93ac-70bd01c964a5','2015-07-30',{'idC11':'20M7130072015073833','latitude':'41.906135','longitude':'8.66983','numero_dernier_point':'021'});");

        psCleanInfoTournee  = getSession().prepare("DELETE FROM info_tournee where code_tournee = ?");
        psCleanTournees = getSession().prepare("DELETE FROM tournees where date_jour = ? and code_tournee = ?");
        
        httpPort = RandomUts.getRandomHttpPort();
        wireMockServer = new WireMockServer(httpPort);
        wireMockServer.start();
        WireMock.configureFor("127.0.0.1", httpPort);
        wireMock = new WireMock("127.0.0.1", httpPort);
    }

    @Test(groups = { "slow" })
    public void cas1Test1() throws Exception {
        wireMock.register(post(urlEqualTo("/GetLTs/true"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(
                                        ServiceMockResponses.readResponse("getltv1_get_detail_tournee_response1.json"))));

        IGetDetailTourneeService service = GetDetailTourneeServiceImpl.getInstance().setDao(
                GetDetailTourneeDaoImpl.getInstance());

        DetailTournee detailTournee = service.getDetailTournee(DateRules.toDateWS("2015-07-30T01:00:00"), "AJA20M71",
                GetLtV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort));

        assertNotNull(detailTournee);
        assertEquals(detailTournee.getPointsEnDistribution().size(), 2);
        assertEquals(detailTournee.getPointsRealises().size(), 3);
        assertEquals(detailTournee.getRelevesGps().size(), 5);
        assertEquals(detailTournee.getLtsCollecte().size(), 2);

        assertEquals(detailTournee.getInformations().get("debut"), "2015-07-30 07:00:00");
        assertEquals(detailTournee.getInformations().get("fin"), "2015-07-30 15:00:00");
    }

    @AfterClass
    public void tearDownAfterClass() throws Exception {
		getSession().execute(psCleanInfoTournee.bind("AJA20M71"));
		getSession().execute(psCleanTournees.bind("2015-07-30", "AJA20M71"));
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }

}
