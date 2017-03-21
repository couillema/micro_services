package com.chronopost.vision.microservices.tournee.getdetailtournee.v1;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Date;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.microservices.sdk.utils.ServiceMockResponses;
import com.chronopost.vision.model.DetailTournee;
import com.chronopost.vision.model.Position;
import com.chronopost.vision.model.PositionGps;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.ut.RandomUts;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

public class GetDetailTourneeServiceImplTest {
    private IGetDetailTourneeDao tourneeDao;
    private IGetDetailTourneeService service;
    private int servicePort = RandomUts.getRandomHttpPort();
    private WireMockServer wireMockServer;
    private WireMock wireMock;

    @BeforeClass
    public void setUpBeforeClass() throws FileNotFoundException {
        tourneeDao = Mockito.mock(IGetDetailTourneeDao.class);
        service = GetDetailTourneeServiceImpl.getInstance();
        wireMockServer = new WireMockServer(servicePort);
        wireMockServer.start();
        WireMock.configureFor("127.0.0.1", servicePort);
        wireMock = new WireMock("127.0.0.1", servicePort);
    }

    @Test
    public void getInstance() throws Exception {
        wireMock.register(post(urlEqualTo("/GetLTs/true")).withHeader("Accept", equalTo("application/json"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")

                                .withBody(
                                        ServiceMockResponses.readResponse("getltv1_response_detail_tournee1.json")
                                                .replace("2015-04-30", DateRules.toDateSortable(new Date())))));

        System.out.println(ServiceMockResponses.readResponse("getltv1_response_detail_tournee1.json").replace(
                "2015-04-30", DateRules.toDateSortable(new Date())));

        Mockito.when(tourneeDao.getTournee(Mockito.any(String.class), Mockito.any(Date.class))).thenAnswer(
                new Answer<Tournee>() {

                    @Override
                    public Tournee answer(InvocationOnMock invocation) throws Throwable {
                        Tournee tournee = new Tournee();
                        tournee.setCodeTournee("TST01A01");
                        tournee.setDateTournee(new Date());
                        tournee.setLtsDeLaTournee(Arrays.asList("EE000000001FR", "EE000000002FR"));
                        PositionGps position1 = new PositionGps().setCoordonnees(
                                new Position().setLati((double) 4).setLongi((double) 5)).setDateRelevePosition(
                                new Date());
                        PositionGps position2 = new PositionGps().setCoordonnees(
                                new Position().setLati((double) 5).setLongi((double) 6)).setDateRelevePosition(
                                new Date());
                        PositionGps position3 = new PositionGps().setCoordonnees(
                                new Position().setLati((double) 6).setLongi((double) 7)).setDateRelevePosition(
                                new Date());
                        tournee.setRelevesGps(Arrays.asList(position1, position2, position3));
                        return tournee;
                    }

                });
        service.setDao(tourneeDao);
        DetailTournee detailTournee = service.getDetailTournee(new Date(), "TST01A01", GetLtV1.getInstance()
                .setEndpoint("http://127.0.0.1:" + servicePort));

        assertNotNull(detailTournee);
        assertEquals(detailTournee.getPointsEnDistribution().size(), 1);
        assertEquals(detailTournee.getPointsRealises().size(), 1);
        assertEquals(detailTournee.getLtsCollecte().size(), 1);
        assertEquals(detailTournee.getRelevesGps().size(), 3);
    }
}
