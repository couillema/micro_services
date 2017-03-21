package com.chronopost.vision.microservices.insertAlerte.v1;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

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

import com.chronopost.vision.model.insertAlerte.v1.Alerte;

public class InsertAlerteResourceTest  extends JerseyTestNg.ContainerPerClassTest {
	
    /**
     * Mocking the service
     */
	private InsertAlerteResource insertAlerteResourceTest;
    private static IInsertAlerteService serviceMock;

    private Client client;

    /**
     * Binding the service to the instantiation of the resource
     */
    @Override
    protected Application configure() {
        /* Création de la resource et initialisation avec le service mocké */
    	insertAlerteResourceTest = new InsertAlerteResource();

        forceSet(TestProperties.CONTAINER_PORT, "0");

        ResourceConfig config = new ResourceConfig();
        config.register(insertAlerteResourceTest);

        return config;
    }

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();
        client = ClientBuilder.newClient();
    }

    @Test
    public void insertAlerte_EXCEPT() throws Exception {
        serviceMock = Mockito.mock(IInsertAlerteService.class);
        insertAlerteResourceTest.setService(serviceMock);

        /* initialisation des variables à fournir au service */
        /* Initialisation */
        List<Alerte> alertes = new ArrayList<>();
        Mockito.doThrow(new RuntimeException("test de retour")).when(serviceMock).insertAlertes(Mockito.anyListOf(Alerte.class));

        /* Test de l'appel */
        WebTarget a = client.target("http://localhost:" + getPort());
		WebTarget b = a.path("/InsertAlertes");
		Builder c = b.request();
		Builder d = c.accept(MediaType.APPLICATION_JSON_TYPE);
		Entity<List<Alerte>> f = Entity.entity(alertes, MediaType.APPLICATION_JSON);
		Response e = d.post(f);
        assertEquals(e.getStatus(), 500);
    }
}
