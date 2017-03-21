package com.chronopost.vision.microservices.lt.getcodetourneefromlt;

import static org.testng.Assert.assertEquals;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.model.GetCodeTourneeFromLTResponse;
import com.chronopost.vision.model.rules.DateRules;

public class GetCodeTourneeFromLTResourceTest extends JerseyTestNg.ContainerPerClassTest {

    /**
     * Mocking the service
     */
    private static IGetCodeTourneeFromLTService serviceMock = Mockito.mock(IGetCodeTourneeFromLTService.class);

    private Client client;

    /**
     * Binding the service to the instantiation of the resource
     */
    @Override
    protected Application configure() {
        GetCodeTourneeFromLTResource resourceGetCodeTourneeResource = new GetCodeTourneeFromLTResource();
        resourceGetCodeTourneeResource.setService(serviceMock);

        forceSet(TestProperties.CONTAINER_PORT, "0");

        ResourceConfig config = new ResourceConfig();
        config.register(resourceGetCodeTourneeResource);

        return config;
    }

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();
        client = ClientBuilder.newClient();
    }

    @Test
    public void testSafeGuard() throws Exception {
        Mockito.reset(serviceMock);

        // not found
        Response response = client.target("http://localhost:" + getPort())
                .path("/GetCodeTourneeFromLT/1234/2015-12-07T10:10:00").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get();

        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void testGetLTFound() throws Exception {
        Mockito.reset(serviceMock);

        // found
        GetCodeTourneeFromLTResponse codeTourneeModel = new GetCodeTourneeFromLTResponse();
        codeTourneeModel.setCodeAgence("AGENCE");
        codeTourneeModel.setCodeTournee("TOURNEE");

        Mockito.when(serviceMock.findTourneeBy("ABCD", DateRules.toDateWS("2015-12-07T10:10:00"))).thenReturn(
                codeTourneeModel);

        Response response = client.target("http://localhost:" + getPort())
                .path("/GetCodeTourneeFromLT/ABCD/2015-12-07T10:10:00").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get();

        assertEquals(response.getStatus(), 200);

        GetCodeTourneeFromLTResponse model = response.readEntity(GetCodeTourneeFromLTResponse.class);

        assertEquals(model.getCodeAgence(), "AGENCE");
        assertEquals(model.getCodeTournee(), "TOURNEE");
    }

    @Test
    public void testGetLTNotFound() throws Exception {
        // LT Not Found
        Mockito.reset(serviceMock);

        Mockito.when(serviceMock.findTourneeBy("ABCD", DateRules.toDateWS("2015-12-07T10:10:00"))).thenThrow(
                new GetCodeTourneeFromLTException(GetCodeTourneeFromLTException.LT_NOT_FOUND));

        Response response = client.target("http://localhost:" + getPort())
                .path("/GetCodeTourneeFromLT/ABCD/2015-12-07T10:10:00").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get();

        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void testGetTourneeNotFound() throws Exception {
        // Tournee not found
        Mockito.reset(serviceMock);

        Mockito.when(serviceMock.findTourneeBy("ABCD", DateRules.toDateWS("2015-12-07T10:10:00"))).thenThrow(
                new GetCodeTourneeFromLTException(GetCodeTourneeFromLTException.TOURNEE_NOT_FOUND));

        Response response = client.target("http://localhost:" + getPort())
                .path("/GetCodeTourneeFromLT/ABCD/2015-12-07T10:10:00").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get();

        assertEquals(response.getStatus(), 404);
    }

    @Test
	public void testBadRequests() throws Exception {
		// too bad requests
		Mockito.reset(serviceMock);

		int status = client.target("http://localhost:" + getPort()).path("/GetCodeTourneeFromLT/ / ").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get().getStatus();

		assertEquals(status, 400);

		status = client.target("http://localhost:" + getPort()).path("/GetCodeTourneeFromLT/1234/ ").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get().getStatus();

		assertEquals(status, 400);

		status = client.target("http://localhost:" + getPort()).path("/GetCodeTourneeFromLT/2015-12-07T10:10:00/ ")
				.request().accept(MediaType.APPLICATION_JSON_TYPE).get().getStatus();

		assertEquals(status, 400);

		status = client.target("http://localhost:" + getPort()).path("/GetCodeTourneeFromLT/1234/xxxxy").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get().getStatus();

		assertEquals(status, 400);
	}
}
