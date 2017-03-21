package com.chronopost.vision.microservices.insertpointtournee.v1;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.model.Evt;

public class InsertPointTourneeResourceTest extends JerseyTestNg.ContainerPerClassTest {

    /**
     * Mocking the service
     */
	private static InsertPointTourneeResource resourceInsertPointTourneeResourceTest;
	private static IInsertPointTourneeService serviceMock;
    private Client client;

    /**
     * Binding the service to the instantiation of the resource
     */
    @Override
    protected Application configure() {
        /* Création de la resource et initialisation avec le service mocké */
        resourceInsertPointTourneeResourceTest = new InsertPointTourneeResource();
        forceSet(TestProperties.CONTAINER_PORT, "0");
        ResourceConfig config = new ResourceConfig();
        config.register(resourceInsertPointTourneeResourceTest);
        return config;
    }

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();
        client = ClientBuilder.newClient();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void insertPointTournee() {
        serviceMock = Mockito.mock(IInsertPointTourneeService.class);
        resourceInsertPointTourneeResourceTest.setService(serviceMock);

        /* initialisation des variables à fournir au service */
        /* Initialisation */
        List<Evt> evts = new ArrayList<>();

        /* Réglage du Mock pour qu'il retourne la réponse désirée */
        try {
            Mockito.when(serviceMock.traiteEvenement(Mockito.anyList())).thenReturn(Boolean.TRUE);
        } catch (InterruptedException | ExecutionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        /* Test de l'appel */
        WebTarget a = client.target("http://localhost:" + getPort());
        WebTarget b = a.path("/InsertPointTournee/v1");
        Builder c = b.request();
        Builder d = c.accept(MediaType.APPLICATION_JSON_TYPE);
        Entity<List<Evt>> f = Entity.entity(evts, MediaType.APPLICATION_JSON);
        Response e = d.post(f);
        assertEquals(e.getStatus(), 200);
    }
}
