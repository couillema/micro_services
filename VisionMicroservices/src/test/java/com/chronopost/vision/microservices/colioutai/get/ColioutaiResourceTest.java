package com.chronopost.vision.microservices.colioutai.get;

import static org.testng.Assert.assertEquals;

import java.util.Date;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.colioutai.get.services.ColioutaiException;
import com.chronopost.vision.microservices.colioutai.get.services.ColioutaiService;
import com.chronopost.vision.model.colioutai.ColioutaiInfoLT;

public class ColioutaiResourceTest extends JerseyTestNg.ContainerPerClassTest {

    /**
     * Mocking the service
     */
    static ColioutaiService serviceMock = Mockito.mock(ColioutaiService.class);

    /**
     * Binding the service to the instantiation of the resource
     */
    @Override
    protected Application configure() {
    	ColioutaiResource resourceColioutai = new ColioutaiResource();
    	resourceColioutai.setService(serviceMock);
        
        forceSet(TestProperties.CONTAINER_PORT, "0");

        ResourceConfig config = new ResourceConfig();
        config.register(resourceColioutai);

        return config;
    }

    @Test
	public void testGetLTFoundNotFound() throws Exception {
		Client client = ClientBuilder.newClient();

		Mockito.reset(serviceMock);

		ColioutaiInfoLT colioutaiInfoLT5678 = new ColioutaiInfoLT();
		colioutaiInfoLT5678.setNoLt("5678");
		Mockito.when(serviceMock.findInfoLT(Mockito.eq("5678"), Mockito.any(Date.class), Mockito.isNull(String.class)))
				.thenReturn(colioutaiInfoLT5678);

		Mockito.when(serviceMock.findInfoLT(Mockito.eq("1234"), Mockito.any(Date.class), Mockito.isNull(String.class)))
				.thenThrow(new ColioutaiException(ColioutaiException.LT_NOT_FOUND));

		int status = client.target("http://localhost:" + getPort()).path("/colis/1234").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get().getStatus();

		assertEquals(status, 404);

		Response response = client.target("http://localhost:" + getPort()).path("/colis/5678").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get();

		assertEquals(response.getStatus(), 200);

		ColioutaiInfoLT ltInfo = response.readEntity(ColioutaiInfoLT.class);

		assertEquals(ltInfo.getNoLt(), "5678");
	}
}
