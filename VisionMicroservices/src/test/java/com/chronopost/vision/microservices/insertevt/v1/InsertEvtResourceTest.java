package com.chronopost.vision.microservices.insertevt.v1;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

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

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Evt;

/** @author unknown : JJC port */
public class InsertEvtResourceTest extends JerseyTestNg.ContainerPerClassTest  {

	/**
	 * Mocking the service
	 */
	private static IInsertEvtService serviceMock = Mockito.mock(IInsertEvtService.class);
	
	private Client client;

	/**
	 * Binding the service to the instantiation of the resource
	 */
	@Override
	protected Application configure() {
		InsertEvtResource resourceInsertEvtResourceTest = new InsertEvtResource();
		resourceInsertEvtResourceTest.setService(serviceMock);

		forceSet(TestProperties.CONTAINER_PORT, "0");

		ResourceConfig config = new ResourceConfig();
		config.register(resourceInsertEvtResourceTest);
		return config;
	}
	
	@BeforeClass
	public void setUp() throws Exception {
		super.setUp();
		client = ClientBuilder.newClient();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testInsertEvtResource() throws Exception {
		Mockito.reset(serviceMock);

		Evt evt1 = new Evt().setNoLt("EE000000001FR").setCodeEvt("DC").setPrioriteEvt(150).setDateEvt(new Date());
		Evt evt2 = new Evt().setNoLt("EE000000002FR").setCodeEvt("D").setPrioriteEvt(10).setDateEvt(new Date());

		Mockito.when(serviceMock.insertEvts(Mockito.anyListOf(Evt.class))).thenReturn(true);

		Mockito.when(serviceMock.insertEvts(new ArrayList<Evt>())).thenThrow(MSTechnicalException.class);

		int status = client.target("http://localhost:" + getPort()).path("/InsertEvt/v1/").request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(Arrays.asList(evt1, evt2), MediaType.APPLICATION_JSON)).getStatus();

		assertEquals(status, 200);

		status = client.target("http://localhost:" + getPort()).path("/InsertEvt/v1/").request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(new ArrayList<Evt>(), MediaType.APPLICATION_JSON)).getStatus();

		assertEquals(status, 500);
	}
}
