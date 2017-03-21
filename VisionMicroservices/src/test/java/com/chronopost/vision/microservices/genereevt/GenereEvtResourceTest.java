package com.chronopost.vision.microservices.genereevt;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpStatus;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.genereevt.v1.GenereEvtV1Output;

public class GenereEvtResourceTest extends JerseyTestNg.ContainerPerClassTest {

    /**
     * Mocking the service
     */
    static IGenereEvtService serviceMock = Mockito.mock(IGenereEvtService.class);

    /**
     * Binding the service to the instantiation of the resource
     */
    @Override
    protected Application configure() {
        GenereEvtResource resource = new GenereEvtResource();
        resource.setService(serviceMock);

        forceSet(TestProperties.CONTAINER_PORT, "0");

        ResourceConfig config = new ResourceConfig();
        config.register(resource);

        return config;
    }

    @Test
    public void testGenereEvt() throws MSTechnicalException, FunctionalException {
        Client client = ClientBuilder.newClient();

        Evt evt = new Evt();
        Map<Evt, Boolean> result = new HashMap<Evt, Boolean>();
        result.put(evt, true);

        Mockito.reset(serviceMock);

        Mockito.when(serviceMock.genereEvt(Mockito.anyListOf(Evt.class), Mockito.anyBoolean())).thenReturn(result);

        Response response = client.target("http://localhost:" + getPort()).path("genereEvt/true").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(Arrays.asList(evt), MediaType.APPLICATION_JSON));

        System.out.println(response.getStatus());
        assertEquals(response.getStatus(), 200);
        GenereEvtV1Output output = response.readEntity(GenereEvtV1Output.class);
        assertEquals(output.getStatus().booleanValue(), true);
    }

    @Test
    public void testGenereEvtFalse() throws MSTechnicalException, FunctionalException {
        Client client = ClientBuilder.newClient();

        Evt evt = new Evt();
        Map<Evt, Boolean> result = new HashMap<Evt, Boolean>();
        result.put(evt, true);

        Mockito.reset(serviceMock);

        Mockito.when(serviceMock.genereEvt(Mockito.anyListOf(Evt.class), Mockito.anyBoolean())).thenReturn(result);

        Response response = client.target("http://localhost:" + getPort()).path("genereEvt/false").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(Arrays.asList(evt), MediaType.APPLICATION_JSON));

        System.out.println(response.getStatus());

        assertEquals(response.getStatus(), 200);
        GenereEvtV1Output output = response.readEntity(GenereEvtV1Output.class);
        assertEquals(output.getStatus().booleanValue(), true);
    }

    @Test
    public void testGenereEvtEmpty() throws MSTechnicalException, FunctionalException {
        Client client = ClientBuilder.newClient();

        Evt evt = new Evt();
        Map<Evt, Boolean> result = new HashMap<Evt, Boolean>();
        result.put(evt, true);

        Mockito.reset(serviceMock);

        Mockito.when(serviceMock.genereEvt(Mockito.anyListOf(Evt.class), Mockito.anyBoolean())).thenReturn(result);

        Response response = client.target("http://localhost:" + getPort()).path("genereEvt/").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(Arrays.asList(evt), MediaType.APPLICATION_JSON));

        System.out.println(response.getStatus());
        assertEquals(response.getStatus(), 404);

    }

    @Test
    public void testException() throws MSTechnicalException, FunctionalException {
        Client client = ClientBuilder.newClient();

        Mockito.reset(serviceMock);

        Mockito.when(serviceMock.genereEvt(Mockito.anyListOf(Evt.class), Mockito.anyBoolean())).thenThrow(
                new MSTechnicalException());

        Evt evt = new Evt();

        Response response = client.target("http://localhost:" + getPort()).path("genereEvt/true").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(Arrays.asList(evt), MediaType.APPLICATION_JSON));

        MSTechnicalException exception = (MSTechnicalException) response.readEntity(MSTechnicalException.class);
        assertEquals(exception.getClass(), MSTechnicalException.class);
        assertEquals(response.getStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
}
