package com.chronopost.vision.microservices.traitementRetard;

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

import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.TraitementRetardInput;

import fr.chronopost.soap.calculretard.cxf.ResultCalculerRetardPourNumeroLt;

public class TraitementRetardResourceTest extends JerseyTestNg.ContainerPerClassTest  {

	/**
	 * Mocking the service
	 */
	private static ITraitementRetardService serviceMock = Mockito.mock(ITraitementRetardService.class);
	private Client client;

	/**
	 * Binding the service to the instantiation of the resource
	 */
	@Override
	protected Application configure() {
		/* Création de la resource et initialisation avec le service mocké */
		TraitementRetardResource resourceTraitementRetardResourceTest = new TraitementRetardResource();
		resourceTraitementRetardResourceTest.setService(serviceMock);

		forceSet(TestProperties.CONTAINER_PORT, "0");

		ResourceConfig config = new ResourceConfig();
		config.register(resourceTraitementRetardResourceTest);

		return config;
	}
	
	@BeforeClass
	public void setUp() throws Exception {
		super.setUp();
		client = ClientBuilder.newClient();
	}
	
	/**
	 * Test de réponse positive de la resource 
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "boxing" })
	@Test
    public void testTraitementRetardResource() throws Exception {
        Mockito.reset(serviceMock);
        
        /* initialisation des variables à fournir au service */
        TraitementRetardInput retard = new TraitementRetardInput();
        retard.setLt(new Lt().setNoLt("XX123456X"));
        retard.setResultCR(new ResultCalculerRetardPourNumeroLt());
        List<TraitementRetardInput> retards = new ArrayList<>();
        retards.add(retard);
        
        /* Réglage du Mock pour qu'il retourne la réponse désirée */
		Mockito.when(serviceMock.genereRD(Mockito.anyList())).thenReturn(true);

		/* Test de l'appel */
		WebTarget a = client.target("http://localhost:" + getPort());
		WebTarget b = a.path("/TraitementRetard");
		Builder c = b.request();
		Builder d = c.accept(MediaType.APPLICATION_JSON_TYPE);
		Entity<List<TraitementRetardInput>> f = Entity.entity(retards,MediaType.APPLICATION_JSON);
		Response e = d.post(f);
		assertEquals(e.getStatus(), 200);
	}
}
