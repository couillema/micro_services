package com.chronopost.vision.microservices.getEvts;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.chronopost.vision.model.Evt;

public class GetEvtsResourceTest extends JerseyTestNg.ContainerPerClassTest {

	private GetEvtsResource getEvtsResource;
	private Client client;
	private IGetEvtsService serviceMock;

	@Override
	protected Application configure() {
		getEvtsResource = new GetEvtsResource();

		forceSet(TestProperties.CONTAINER_PORT, "0");

		final ResourceConfig config = new ResourceConfig();
		config.register(getEvtsResource);
		client = ClientBuilder.newClient();

		return config;
	}

	/**
	 * Initialise le service pour pouvoir donner un comportement différent à ses
	 * méthodes dans chaque test
	 */
	@BeforeMethod
	public void prepareTest() {
		serviceMock = mock(IGetEvtsService.class);
		getEvtsResource.setService(serviceMock);
	}

	/**
	 * Vérifie le status 204 quand aucun evt n'est trouvé
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_getEvts_noContent() throws Exception {
		// WHEN
		when(serviceMock.getEvts("noLt")).thenReturn(new ArrayList<Evt>());
		final Response response = client.target("http://localhost:" + getPort()).path("getEvts").path("noLt").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get();
		// THEN
		assertEquals(response.getStatus(), Status.NO_CONTENT.getStatusCode());
	}

	/**
	 * Vérifie le retour quand une exception est retournée par le service
	 */
	@Test
	public void test_getEvts_withException() throws Exception {
		// WHEN
		when(serviceMock.getEvts(Mockito.eq("noLt"))).thenThrow(new Exception("ERROR TEST"));
		final Response response = client.target("http://localhost:" + getPort()).path("getEvts").path("noLt").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get();
		// THEN
		assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
		assertEquals(response.readEntity(String.class), "ERROR TEST");
	}
}
