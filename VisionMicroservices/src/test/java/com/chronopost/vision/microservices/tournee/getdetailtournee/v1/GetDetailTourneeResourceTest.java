package com.chronopost.vision.microservices.tournee.getdetailtournee.v1;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.model.DetailTournee;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.Point;
import com.chronopost.vision.model.Position;
import com.chronopost.vision.model.PositionGps;

public class GetDetailTourneeResourceTest extends JerseyTestNg.ContainerPerClassTest {

    /**
     * Mocking the service
     */
    private static IGetDetailTourneeService serviceMock = Mockito.mock(IGetDetailTourneeService.class);

    private Client client;

    /**
     * Binding the service to the instantiation of the resource
     */
    @Override
    protected Application configure() {
        GetDetailTourneeResource resourceGetDetailTournee = new GetDetailTourneeResource();
        resourceGetDetailTournee.setService(serviceMock);

        forceSet(TestProperties.CONTAINER_PORT, "0");

        ResourceConfig config = new ResourceConfig();
        config.register(resourceGetDetailTournee);

        resourceGetDetailTournee.setGetLt(Mockito.mock(GetLtV1.class));

        return config;
    }

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();
        client = ClientBuilder.newClient();
    }

    @Test
    public void testResource() throws Exception {
        Mockito.reset(serviceMock);
        DetailTournee detailTournee = new DetailTournee();
        PositionGps position1 = new PositionGps().setCoordonnees(
                new Position().setLati((double) 4).setLongi((double) 5)).setDateRelevePosition(new Date());
        PositionGps position2 = new PositionGps().setCoordonnees(
                new Position().setLati((double) 5).setLongi((double) 6)).setDateRelevePosition(new Date());
        PositionGps position3 = new PositionGps().setCoordonnees(
                new Position().setLati((double) 6).setLongi((double) 7)).setDateRelevePosition(new Date());

        detailTournee.setPointsEnDistribution(new ArrayList<Point>());
        detailTournee.setPointsRealises(new ArrayList<Point>());
        detailTournee.setLtsCollecte(new ArrayList<Lt>());
        detailTournee.setCodeAgence("AAA");
        detailTournee.setCodeTournee("11111");
        detailTournee.setRelevesGps(Arrays.asList(position1, position2, position3));

        Mockito.when(
                serviceMock.getDetailTournee(Mockito.any(Date.class), Mockito.matches("AAA11111"),
                        Mockito.any(GetLtV1.class))).thenReturn(detailTournee);

        int status = client.target("http://localhost:" + getPort())
                .path("/GetDetailTournee/v1/2015-07-30T11:00:00/AAA11111").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get().getStatus();

        assertEquals(status, 200);

        //
        status = client.target("http://localhost:" + getPort())
                .path("/GetDetailTournee/v1/2015 - 07 - 30 11:00:00/AAA11111").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get().getStatus();

        assertEquals(status, 500);
    }

    @Test
    public void testDateIncorrecte() throws Exception {
        Mockito.reset(serviceMock);
        DetailTournee detailTournee = new DetailTournee();
        PositionGps position1 = new PositionGps().setCoordonnees(
                new Position().setLati((double) 4).setLongi((double) 5)).setDateRelevePosition(new Date());
        PositionGps position2 = new PositionGps().setCoordonnees(
                new Position().setLati((double) 5).setLongi((double) 6)).setDateRelevePosition(new Date());
        PositionGps position3 = new PositionGps().setCoordonnees(
                new Position().setLati((double) 6).setLongi((double) 7)).setDateRelevePosition(new Date());

        detailTournee.setPointsEnDistribution(new ArrayList<Point>());
        detailTournee.setPointsRealises(new ArrayList<Point>());
        detailTournee.setLtsCollecte(new ArrayList<Lt>());
        detailTournee.setCodeAgence("AAA");
        detailTournee.setCodeTournee("11111");
        detailTournee.setRelevesGps(Arrays.asList(position1, position2, position3));

        Mockito.when(
                serviceMock.getDetailTournee(Mockito.any(Date.class), Mockito.matches("AAA11111"),
                        Mockito.any(GetLtV1.class))).thenReturn(detailTournee);

        int status = client.target("http://localhost:" + getPort())
                .path("/GetDetailTournee/v1/2015 - 07 - 30 11:00:00/AAA11111").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get().getStatus();

        assertEquals(status, 500);
    }

    @Test
    public void testTourneeAbsente() throws Exception {

        Mockito.reset(serviceMock);
        DetailTournee detailTournee = new DetailTournee();
        PositionGps position1 = new PositionGps().setCoordonnees(
                new Position().setLati((double) 4).setLongi((double) 5)).setDateRelevePosition(new Date());
        PositionGps position2 = new PositionGps().setCoordonnees(
                new Position().setLati((double) 5).setLongi((double) 6)).setDateRelevePosition(new Date());
        PositionGps position3 = new PositionGps().setCoordonnees(
                new Position().setLati((double) 6).setLongi((double) 7)).setDateRelevePosition(new Date());

        detailTournee.setPointsEnDistribution(new ArrayList<Point>());
        detailTournee.setPointsRealises(new ArrayList<Point>());
        detailTournee.setLtsCollecte(new ArrayList<Lt>());
        detailTournee.setCodeAgence("AAA");
        detailTournee.setCodeTournee("11111");
        detailTournee.setRelevesGps(Arrays.asList(position1, position2, position3));

        Mockito.when(
                serviceMock.getDetailTournee(Mockito.any(Date.class), Mockito.matches("AAA11111"),
                        Mockito.any(GetLtV1.class))).thenReturn(detailTournee);

        int status = client.target("http://localhost:" + getPort()).path("/GetDetailTournee/v1/2015-07-30T11:00:00/")
                .request().accept(MediaType.APPLICATION_JSON_TYPE).get().getStatus();

        assertEquals(status, 404);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTimeout() throws Exception {
        Mockito.reset(serviceMock);
        DetailTournee detailTournee = new DetailTournee();
        PositionGps position1 = new PositionGps().setCoordonnees(
                new Position().setLati((double) 4).setLongi((double) 5)).setDateRelevePosition(new Date());
        PositionGps position2 = new PositionGps().setCoordonnees(
                new Position().setLati((double) 5).setLongi((double) 6)).setDateRelevePosition(new Date());
        PositionGps position3 = new PositionGps().setCoordonnees(
                new Position().setLati((double) 6).setLongi((double) 7)).setDateRelevePosition(new Date());

        detailTournee.setPointsEnDistribution(new ArrayList<Point>());
        detailTournee.setPointsRealises(new ArrayList<Point>());
        detailTournee.setLtsCollecte(new ArrayList<Lt>());
        detailTournee.setCodeAgence("AAA");
        detailTournee.setCodeTournee("11111");
        detailTournee.setRelevesGps(Arrays.asList(position1, position2, position3));

        Mockito.when(
                serviceMock.getDetailTournee(Mockito.any(Date.class), Mockito.matches("AAA11111"),
                        Mockito.any(GetLtV1.class))).thenThrow(TimeoutException.class);

        int status = client.target("http://localhost:" + getPort())
                .path("/GetDetailTournee/v1/2015-07-30T11:00:00/AAA11111").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get().getStatus();

        assertEquals(status, 503);
    }
}
