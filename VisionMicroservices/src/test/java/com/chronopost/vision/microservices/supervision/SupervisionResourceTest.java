package com.chronopost.vision.microservices.supervision;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.chronopost.vision.exceptions.InvalidParameterException;
import com.chronopost.vision.model.supervision.SnapShotVision;

public class SupervisionResourceTest extends JerseyTestNg.ContainerPerClassTest {

	private SupervisionResource resource;
	private Client client;
	private ISupervisionService serviceMock;

	@Override
	protected Application configure() {
		resource = new SupervisionResource();
		
		forceSet(TestProperties.CONTAINER_PORT, "0");

		ResourceConfig config = new ResourceConfig();
		config.register(resource);
		client = ClientBuilder.newClient();

		return config;
	}

	/**
	 * Initialise le service pour pouvoir donner un comportement différent à ses
	 * méthodes dans chaque test
	 */
	@BeforeMethod
	public void prepareTest() {
		serviceMock = Mockito.mock(ISupervisionService.class);
		resource.setService(serviceMock);
	}

	/**
	 * Appelle en GET la méthode getSnapShotVisionForLast10Minutes
	 */
	@Test
	public void test_getRecentSnapShotVision() throws Exception {
		// GIVEN
		SnapShotVision snapShotVision = new SnapShotVision();
		snapShotVision.setAskEvt(15L);
		when(serviceMock.getSnapShotVisionForLast10Minutes()).thenReturn(snapShotVision);
		// WHEN
		Response response = client.target("http://localhost:" + getPort()).path("supervision").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get();
		SnapShotVision entity = response.readEntity(SnapShotVision.class);
		assertEquals(entity.getAskEvt().longValue(), 15L);
	}

	/**
	 * Appelle en GET la méthode getSnapShotsVisionForADay
	 */
	@Test
	public void test_getSnapShotsVisionForDay() throws Exception {
		// GIVEN
		List<SnapShotVision> snapShots = new ArrayList<>();
		SnapShotVision snapShotVision = new SnapShotVision();
		snapShotVision.setAskEvt(15L);
		snapShots.add(snapShotVision);
		when(serviceMock.getSnapShotsVisionForADay("20161028")).thenReturn(snapShots);
		// WHEN
		Response response = client.target("http://localhost:" + getPort()).path("supervision").path("releve")
				.queryParam("jour", "20161028").request().accept(MediaType.APPLICATION_JSON_TYPE).get();
		ArrayList<SnapShotVision> entity = response.readEntity(new GenericType<ArrayList<SnapShotVision>>() { });
		assertEquals(entity.get(0).getAskEvt().longValue(), 15L);
	}

	/**
	 * Appelle en GET la méthode getSnapShotAverage
	 */
	@Test
	public void test_getSnapShotAverage() throws Exception {
		// GIVEN
		SnapShotVision snapShotVision = new SnapShotVision();
		snapShotVision.setAskEvt(4L);
		when(serviceMock.getSnapShotsAverageForADay("20161028")).thenReturn(snapShotVision);
		// WHEN
		Response response = client.target("http://localhost:" + getPort()).path("supervision").path("average")
				.queryParam("jour", "20161028").request().accept(MediaType.APPLICATION_JSON_TYPE).get();
		SnapShotVision entity = response.readEntity(SnapShotVision.class);
		assertEquals(entity.getAskEvt().longValue(), 4L);
	}

	/**
	 * Appelle en GET la méthode getSnapShotVisionForLast10Minutes
	 */
	@Test
	public void test_getRecentSnapShotVision_errorServer() throws Exception {
		// GIVEN
		when(serviceMock.getSnapShotVisionForLast10Minutes()).thenThrow(new Exception("ERROR TEST"));
		// WHEN
		Response response = client.target("http://localhost:" + getPort()).path("supervision").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get();
		assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
		assertEquals(response.readEntity(String.class), "ERROR TEST");
	}
	
	/**
	 * Vérifie message d'erreur sur getSnapShotsVisionForADay quand format du
	 * param jour est incorrect
	 */
	@Test
	public void test_getSnapShotsForADay_badDateFormat() throws Exception {
		// WHEN
		when(serviceMock.getSnapShotsVisionForADay("20162")).thenThrow(new InvalidParameterException("Format du paramètre jour incorrect. Doit être yyyyMMdd"));
		Response response = client.target("http://localhost:" + getPort()).path("supervision").path("releve").queryParam("jour", "20162").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get();
		assertEquals(response.getStatus(), Status.METHOD_NOT_ALLOWED.getStatusCode());
		assertEquals(response.readEntity(String.class), "Format du paramètre jour incorrect. Doit être yyyyMMdd");
	}
	
	/**
	 * Vérifie message d'erreur sur getSnapShotsVisionForADay lors d'un problème
	 * serveur
	 */
	@Test
	public void test_getSnapShotsForADay_errorServer() throws Exception {
		// WHEN
		when(serviceMock.getSnapShotsVisionForADay("20162")).thenThrow(new Exception("ERROR TEST"));
		Response response = client.target("http://localhost:" + getPort()).path("supervision").path("releve").queryParam("jour", "20162").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get();
		assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
		assertEquals(response.readEntity(String.class), "ERROR TEST");
	}
	
	/**
	 * Vérifie message d'erreur sur getSnapShotAverage quand format du
	 * param jour est incorrect
	 */
	@Test
	public void test_getSnapShotAverage_badDateFormat() throws Exception {
		// WHEN
		when(serviceMock.getSnapShotsAverageForADay("20162")).thenThrow(new InvalidParameterException("Format du paramètre jour incorrect. Doit être yyyyMMdd"));
		Response response = client.target("http://localhost:" + getPort()).path("supervision").path("average").queryParam("jour", "20162").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get();
		assertEquals(response.getStatus(), Status.METHOD_NOT_ALLOWED.getStatusCode());
		assertEquals(response.readEntity(String.class), "Format du paramètre jour incorrect. Doit être yyyyMMdd");
	}
	
	/**
	 * Vérifie message d'erreur sur getSnapShotAverage lors d'un problème
	 * serveur
	 */
	@Test
	public void test_getSnapShotAverage_errorServer() throws Exception {
		// WHEN
		when(serviceMock.getSnapShotsAverageForADay("20162")).thenThrow(new Exception("ERROR TEST"));
		Response response = client.target("http://localhost:" + getPort()).path("supervision").path("average").queryParam("jour", "20162").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get();
		assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
		assertEquals(response.readEntity(String.class), "ERROR TEST");
	}
	
	/**
	 * Vérifie status de getMSStatus quand le service retourne true
	 */
	@Test
	public void test_getMSStatus_ok() throws Exception {
		// WHEN
		when(serviceMock.getMSStatus()).thenReturn(true);
		Response response = client.target("http://localhost:" + getPort()).path("supervision").path("msStatus")
				.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
		assertEquals(response.readEntity(Boolean.class), Boolean.TRUE);
	}
	
	/**
	 * Vérifie status de getMSStatus quand le service retourne true
	 */
	@Test
	public void test_getMSStatus_ko() throws Exception {
		// WHEN
		when(serviceMock.getMSStatus()).thenReturn(false);
		Response response = client.target("http://localhost:" + getPort()).path("supervision").path("msStatus")
				.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
		assertEquals(response.getStatus(), Status.EXPECTATION_FAILED.getStatusCode());
		assertEquals(response.readEntity(Boolean.class), Boolean.FALSE);
	}
	
	/**
	 * Vérifie status de getMSStatus quand le service retourne true
	 */
	@Test
	public void test_getMSStatus_failed() throws Exception {
		// WHEN
		when(serviceMock.getMSStatus()).thenThrow(new Exception("ERROR TEST"));
		Response response = client.target("http://localhost:" + getPort()).path("supervision").path("msStatus")
				.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
		assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
		assertEquals(response.readEntity(Boolean.class), Boolean.FALSE);
	}
}
