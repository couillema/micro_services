package com.chronopost.vision.microservices.tournee.getalertestournees.v1;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.model.getAlertesTournees.v1.GetAlertesTourneesInput;
import com.chronopost.vision.model.getAlertesTournees.v1.GetAlertesTourneesOutput;
import com.google.common.collect.Maps;

public class GetAlertesTourneesResourceTest extends JerseyTestNg.ContainerPerClassTest {
    /**
     * Mocking the service
     */
    private static IGetAlertesTourneesService serviceMock = Mockito.mock(IGetAlertesTourneesService.class);

    private Client client;

    /**
     * Binding the service to the instantiation of the resource
     */
    protected Application configure() {
        GetAlertesTourneesResource resourceGetAlertesTourneesResource = new GetAlertesTourneesResource();
        resourceGetAlertesTourneesResource.setService(serviceMock);

        forceSet(TestProperties.CONTAINER_PORT, "0");

        ResourceConfig config = new ResourceConfig();
        config.register(resourceGetAlertesTourneesResource);

        return config;
    }

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();
        client = ClientBuilder.newClient();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testResource() throws Exception {

        Mockito.reset(serviceMock);
        Map<String, Integer> alertes = Maps.newHashMap();
        alertes.put("TST00A01", 1);
        GetAlertesTourneesOutput output = new GetAlertesTourneesOutput();
        output.setAlertes(alertes);
        Mockito.when(serviceMock.getAlertesTournees(Mockito.anyList(), Mockito.any(Date.class))).thenReturn(output);

        GetAlertesTourneesInput input = new GetAlertesTourneesInput();
        input.setCodesTournee(Arrays.asList("TST00A01"));
        input.setDateTournee(new Date());

        int status = client.target("http://localhost:" + getPort()).path("/GetAlertesTournees/v1/").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(input, MediaType.APPLICATION_JSON_TYPE))
                .getStatus();

        assertEquals(status, 200);

        // Vérification de l'appel à la méthode updateTournee du service, avec
        // les 2 événements
        Mockito.verify(serviceMock).getAlertesTournees(Mockito.anyList(), Mockito.any(Date.class));
    }
}
