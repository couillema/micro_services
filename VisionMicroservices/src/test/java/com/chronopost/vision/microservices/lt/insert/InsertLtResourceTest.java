package com.chronopost.vision.microservices.lt.insert;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

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
import com.chronopost.vision.model.Lt;

/** @author unknown : JJC port */
public class InsertLtResourceTest extends JerseyTestNg.ContainerPerClassTest {

	/**
	 * Mocking the service
	 */
	static IInsertLtService serviceMock = Mockito.mock(IInsertLtService.class);

	/**
	 * Binding the service to the instantiation of the resource
	 */
	@Override
	protected Application configure() {
		InsertLtResource resourceInsertLts = new InsertLtResource();
		resourceInsertLts.setService(serviceMock);

		forceSet(TestProperties.CONTAINER_PORT, "0");
		ResourceConfig config = new ResourceConfig();
		config.register(resourceInsertLts);

		return config;
	}

	@Test
	public void testInsertLt() throws MSTechnicalException, FunctionalException {
		Client client = ClientBuilder.newClient();
		Lt lt = new Lt();
		Mockito.reset(serviceMock);
		Mockito.when(serviceMock.insertLtsInDatabase(Mockito.anyListOf(Lt.class))).thenReturn(true);

		Response response = client.target("http://localhost:" + getPort()).path("InsertLT").request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(Arrays.asList(lt), MediaType.APPLICATION_JSON));

		assertEquals(response.getStatus(), 200);
		assertEquals((boolean) response.readEntity(Boolean.class), true);
	}

	@Test
	public void testException() throws MSTechnicalException, FunctionalException {
		Client client = ClientBuilder.newClient();
		Mockito.reset(serviceMock);
		Mockito.when(serviceMock.insertLtsInDatabase(Mockito.anyListOf(Lt.class))).thenThrow(new MSTechnicalException());

		Response response = client.target("http://localhost:" + getPort()).path("InsertLT").request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(Arrays.asList(new Lt()), MediaType.APPLICATION_JSON));

		MSTechnicalException exception = (MSTechnicalException) response.readEntity(MSTechnicalException.class);
		assertEquals(exception.getClass(), MSTechnicalException.class);
		assertEquals(response.getStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}
}
