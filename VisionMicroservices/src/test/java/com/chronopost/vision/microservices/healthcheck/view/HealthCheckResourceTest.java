package com.chronopost.vision.microservices.healthcheck.view;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.util.Map;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.ut.RandomUts;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

/** @author unknown : JJC port */
public class HealthCheckResourceTest {

	private WireMockServer wireMockServer;
	private WireMock wireMock;

    private int httpPort = RandomUts.getRandomHttpPort(); // ADD JER  
	
    private final static String HEALTHCHECK_RESPONSE = "{\"CalculRetard Service\":{\"healthy\":true},\"Cassandra\":{\"healthy\":true},\"ColioutaiInfoV1 Service\":{\"healthy\":true},\"Consigne Service\":{\"healthy\":true},\"GetCodeTourneeFromLt Service\":{\"healthy\":true},\"GetDetailTournee Service\":{\"healthy\":true},\"GetLt Service\":{\"healthy\":true},\"Google Service\":{\"healthy\":true},\"InsertEvtV1 Service\":{\"healthy\":true},\"Poi Service\":{\"healthy\":true},\"Ptv Service\":{\"healthy\":true},\"SuiviBoxV1 Service\":{\"healthy\":true},\"UpdateTourneeV1 Service\":{\"healthy\":true},\"cassandra.Fluks DEV Cluster\":{\"healthy\":true},\"cassandra.Vision DEV Cluster\":{\"healthy\":true},\"deadlocks\":{\"healthy\":true}}";

	@BeforeClass
	public void setUpBeforeClass() {
		wireMockServer = new WireMockServer(httpPort);
		wireMockServer.start();
		WireMock.configureFor("127.0.0.1", httpPort);
		wireMock = new WireMock("127.0.0.1", httpPort);
	}

	@Test
	public void serviceTest() {
		wireMock.register(
				get(urlEqualTo("/healthcheck")).withHeader("Accept", equalTo("application/json")).willReturn(aResponse()
						.withStatus(200).withHeader("Content-Type", "application/json").withBody(HEALTHCHECK_RESPONSE)));
		
		HealthCheckResource resourceHealthCheck = new HealthCheckResource().setPort(httpPort);
		
		SupervisionView supervision = resourceHealthCheck.check();
		Map<String,Healthy> map = supervision.getSupervision();      
		Map<String, Healthy> retMap = null;
        ObjectMapper mapper = new ObjectMapper();
        
		try {
			retMap = mapper.readValue(HEALTHCHECK_RESPONSE, new TypeReference<Map<String, Healthy>>(){});
		} catch (IOException e) {
			throw new RuntimeException("Failed parsing json: ", e);
		}
        
        assertNotNull(retMap);
        assertEquals(map, retMap);
	}

	@AfterClass
	public void tearDownAfterClass() {
		wireMockServer.shutdownServer();
		wireMockServer.shutdown();
	}
}
