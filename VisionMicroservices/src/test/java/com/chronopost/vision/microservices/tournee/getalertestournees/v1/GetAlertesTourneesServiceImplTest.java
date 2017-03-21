package com.chronopost.vision.microservices.tournee.getalertestournees.v1;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.testng.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.joda.time.DateTime;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.sdk.GetDetailTourneeV1;
import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.microservices.sdk.utils.ServiceMockResponses;
import com.chronopost.vision.microservices.utils.TypeBorneCreneau;
import com.chronopost.vision.model.getAlertesTournees.v1.GetAlertesTourneesOutput;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.chronopost.vision.ut.RandomUts;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

public class GetAlertesTourneesServiceImplTest {

    GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
    private WireMockServer wireMockServer;
    private WireMock wireMock;

    private int httpPort = RandomUts.getRandomHttpPort2();
    String currentMonthDay = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    String currentMonthDay2 = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

    ITranscoderDao mockTranscoderDao = Mockito.mock(ITranscoderDao.class);
    IGetAlertesTourneesDao mockAlertesTourneesDao = Mockito.mock(IGetAlertesTourneesDao.class);

    private void resetMocks() throws Exception {
        Mockito.reset(mockDetailTourneeV1);
        Mockito.reset(mockAlertesTourneesDao);

    }

    @BeforeClass
    public void setUpBeforeClass() throws Exception {

        GetDetailTourneeV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
        GetLtV1.getInstance().setEndpoint("http://127.0.0.1:" + httpPort);
        wireMockServer = new WireMockServer(httpPort);
        wireMockServer.start();
        WireMock.configureFor("127.0.0.1", httpPort);
        wireMock = new WireMock("127.0.0.1", httpPort);

        wireMock.register(post(urlEqualTo("/GetLTs/true"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(ServiceMockResponses.readResponse("getltv1_getalertestournees_response.json"))));

    }

    @Test
    public void testZeroAlerte() throws Exception {

        resetMocks();
        wireMock.register(get(urlMatching("/GetDetailTournee/v1/.*")).withHeader("Accept", equalTo("application/json"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(
                                        ServiceMockResponses.readResponse("get_detail_tournee_alertes_1.json")
                                                .replace("2015-12-07", currentMonthDay)
                                                .replace("07/12/2015", currentMonthDay2))));

        GetAlertesTourneesOutput alertes = GetAlertesTourneesServiceImpl.getInstance().getAlertesTournees(
                Arrays.asList("VER78M33"), new DateTime().withHourOfDay(10).toDate());

        assertTrue(alertes.getAlertes().get("VER78M33") == 0);

    }

    @Test
    public void testUneAlerte() throws Exception {

        resetMocks();
        Mockito.when(
                mockAlertesTourneesDao.getNoLtAvecCreneauPourAgence(Mockito.matches("TST"), Mockito.any(Date.class),
                        Mockito.any(Date.class), Mockito.any(TypeBorneCreneau.class))).thenReturn(
                Arrays.asList("EERISQUE002FR"));

        GetAlertesTourneesOutput alertes = GetAlertesTourneesServiceImpl.getInstance().getAlertesTournees(
                Arrays.asList("TST00A01"),
                new DateTime(2015, 12, 7, 7, 0, 0).withHourOfDay(15).withMinuteOfHour(30).toDate());

        assertTrue(alertes.getAlertes().get("TST00A01") == 1);

    }

    @Test
    public void testPlusAlerteApresDepassement() throws Exception {

        resetMocks();
        wireMock.register(get(urlMatching("/GetDetailTournee/v1/.*")).withHeader("Accept", equalTo("application/json"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(
                                        ServiceMockResponses.readResponse("get_detail_tournee_alertes_1.json")
                                                .replace("2015-12-07", currentMonthDay)
                                                .replace("07/12/2015", currentMonthDay2))));

        GetAlertesTourneesOutput alertes = GetAlertesTourneesServiceImpl.getInstance().getAlertesTournees(
                Arrays.asList("VER78M33"), new DateTime().withHourOfDay(16).withMinuteOfHour(30).toDate());

        assertTrue(alertes.getAlertes().get("VER78M33") == 0);

    }

    @Test
    public void getAlertesNewMethodTest() throws Exception {

        resetMocks();
        // initFlip("true");

        Mockito.when(
                mockAlertesTourneesDao.getNoLtAvecCreneauPourAgence(Mockito.matches("TST"), Mockito.any(Date.class),
                        Mockito.any(Date.class), Mockito.any(TypeBorneCreneau.class))).thenReturn(
                Arrays.asList("EERISQUE002FR"));

        GetAlertesTourneesServiceImpl.getInstance().setDao(mockAlertesTourneesDao);
        GetAlertesTourneesOutput alertes = GetAlertesTourneesServiceImpl.getInstance().getAlertesTournees(
                Arrays.asList("TST00A01", "TST00A02"), new DateTime(2015, 12, 7, 7, 0, 0).toDate());

        assertTrue(alertes.getAlertes().get("TST00A01") == 1);
        assertTrue(alertes.getAlertes().get("TST00A02") == 0);

    }

}
