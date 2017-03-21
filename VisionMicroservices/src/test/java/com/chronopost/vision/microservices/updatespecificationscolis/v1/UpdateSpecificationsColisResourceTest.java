package com.chronopost.vision.microservices.updatespecificationscolis.v1;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.model.updatespecificationscolis.v1.EvtEtModifs;

public class UpdateSpecificationsColisResourceTest extends JerseyTestNg.ContainerPerClassTest  {

    private static IUpdateSpecificationsColisService serviceMock = Mockito.mock(IUpdateSpecificationsColisService.class);
    private Client client;

    @Override
	protected Application configure() {
		/* Création de la resource et initialisation avec le service mocké */
		UpdateSpecificationsColisResource resource = new UpdateSpecificationsColisResource().setService(serviceMock);

		forceSet(TestProperties.CONTAINER_PORT, "0");

		ResourceConfig config = new ResourceConfig();
		config.register(resource);

		return config;
	}

    @SuppressWarnings("unchecked")
    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();
        client = ClientBuilder.newClient();

        Mockito.reset(serviceMock);
        Mockito.when(serviceMock.traitementSpecificationsColis(Mockito.anyList())).thenReturn(true);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void traitementSpecificationsColis() throws InterruptedException, ExecutionException {
        /* initialisation des variables à fournir au service */
        List<EvtEtModifs> evts = new ArrayList<>() ;

        /* Test de l'appel */
        Entity<List<EvtEtModifs>> inputEntity = Entity.entity(evts,MediaType.APPLICATION_JSON);

        Response e = client.target("http://localhost:" + getPort()).path("/UpdateSpecificationsColis/v1").request().accept(MediaType.APPLICATION_JSON_TYPE).post(inputEntity);
        assertEquals(e.getStatus(), 200);
        Mockito.verify(serviceMock, Mockito.times(1)).traitementSpecificationsColis(Mockito.anyList());
    }
}
