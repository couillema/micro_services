package com.chronopost.vision.microservices.insertagencecolis.v1;

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

public class InsertAgenceColisResourceTest extends JerseyTestNg.ContainerPerClassTest {

    /**
     * Mocking the service
     */
    private static InsertAgenceColisResource resourceInsertAgenceColisResourceTest;
    private static IInsertAgenceColisService serviceMock;
    private Client client;

    /**
     * Binding the service to the instantiation of the resource
     */
    @Override
    protected Application configure() {
        /* Création de la resource et initialisation avec le service mocké */
    	resourceInsertAgenceColisResourceTest = new InsertAgenceColisResource();

        forceSet(TestProperties.CONTAINER_PORT, "0");

        ResourceConfig config = new ResourceConfig();
        config.register(resourceInsertAgenceColisResourceTest);
        return config;
    }

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();
        client = ClientBuilder.newClient();
    }

    @Test
    public void insertAgenceColis_FALSE() {
        serviceMock = Mockito.mock(IInsertAgenceColisService.class);
        resourceInsertAgenceColisResourceTest.setService(serviceMock);

        /* initialisation des variables à fournir au service */
        /* Initialisation */
        List<Evt> evts = new ArrayList<>();

        /* Réglage du Mock pour qu'il retourne la réponse désirée */
        try {
            Mockito.when(serviceMock.traiteEvenement(Mockito.anyListOf(Evt.class))).thenReturn(Boolean.FALSE);
        } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
        }

        /* Test de l'appel */
        WebTarget a = client.target("http://localhost:" + getPort());
        WebTarget b = a.path("/InsertAgenceColis/v1");
        Builder c = b.request();
        Builder d = c.accept(MediaType.APPLICATION_JSON_TYPE);
        Entity<List<Evt>> f = Entity.entity(evts, MediaType.APPLICATION_JSON);
        Response e = d.post(f);
        assertEquals(e.getStatus(), 500);
    }
    
    @Test
    public void insertAgenceColis_EXCEPT() {
        serviceMock = Mockito.mock(IInsertAgenceColisService.class);
        resourceInsertAgenceColisResourceTest.setService(serviceMock);

        /* initialisation des variables à fournir au service */
        /* Initialisation */
        List<Evt> evts = new ArrayList<>();

        /* Réglage du Mock pour qu'il retourne la réponse désirée */
        try {
            Mockito.when(serviceMock.traiteEvenement(Mockito.anyListOf(Evt.class))).thenThrow(new RuntimeException("test de retour"));
        } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
        }

        /* Test de l'appel */
        WebTarget a = client.target("http://localhost:" + getPort());
        WebTarget b = a.path("/InsertAgenceColis/v1");
        Builder c = b.request();
        Builder d = c.accept(MediaType.APPLICATION_JSON_TYPE);
        Entity<List<Evt>> f = Entity.entity(evts, MediaType.APPLICATION_JSON);
        Response e = d.post(f);
        assertEquals(e.getStatus(), 500);
    }
}
