package com.chronopost.vision.microservices.getC11;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.chronopost.vision.model.insertC11.PointC11;
import com.chronopost.vision.model.insertC11.PointC11Liste;
import com.chronopost.vision.model.insertC11.TourneeC11;
import com.chronopost.vision.model.insertC11.TourneeVision;

public class GetC11ResourceTest extends JerseyTestNg.ContainerPerClassTest {

	private GetC11Resource getC11Resource;
	private Client client;
	private IGetC11Service serviceMock;

	private final static String ID_TOURNEE_1 = "44M0003122016000000";
	private final static String TOURNEE_POINT_ID_1 = "point1";
	private final static String TOURNEE_POINT_ID_2 = "point2";

	@Override
	protected Application configure() {
		getC11Resource = new GetC11Resource();

		forceSet(TestProperties.CONTAINER_PORT, "0");

		final ResourceConfig config = new ResourceConfig();
		config.register(getC11Resource);
		client = ClientBuilder.newClient();

		return config;
	}

	/**
	 * Initialise le service pour pouvoir donner un comportement différent à ses
	 * méthodes dans chaque test
	 */
	@BeforeMethod
	public void prepareTest() {
		serviceMock = mock(IGetC11Service.class);
		getC11Resource.setService(serviceMock);
	}

	/**
	 * Vérifie le retour de la request GetC11 quand le process est ok
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_getTourneeC11() throws Exception {
		// GIVEN
		final List<TourneeC11> tourneeC11Liste = new ArrayList<>();
		final TourneeC11 tourneeC11 = new TourneeC11();
		final TourneeVision tourneeVision = new TourneeVision();
		tourneeVision.setIdC11(ID_TOURNEE_1);
		final PointC11Liste pointC11Liste = new PointC11Liste();
		final PointC11 pointC11_1 = new PointC11();
		pointC11_1.setIdPtC11(TOURNEE_POINT_ID_1);
		pointC11Liste.getPointC11s().add(pointC11_1);
		final PointC11 pointC11_2 = new PointC11();
		pointC11_2.setIdPtC11(TOURNEE_POINT_ID_2);
		pointC11Liste.getPointC11s().add(pointC11_2);
		tourneeVision.setPointC11Liste(pointC11Liste);
		tourneeC11.setTourneeVision(tourneeVision);
		tourneeC11Liste.add(tourneeC11);

		when(serviceMock.getTournees(eq("pC"), eq("jour"))).thenReturn(tourneeC11Liste);
		// WHEN
		final Response response = client.target("http://localhost:" + getPort()).path("getC11").path("pC").path("jour")
				.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
		// THEN
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
		final List<TourneeC11> result = response.readEntity(new GenericType<List<TourneeC11>>() {
		});
		assertEquals(result.size(), 1);
		assertEquals(result.get(0).geTourneeVision().getIdC11(), ID_TOURNEE_1);
		assertEquals(result.get(0).geTourneeVision().getPointC11Liste().getPointC11s().size(), 2);
		assertEquals(result.get(0).geTourneeVision().getPointC11Liste().getPointC11s().get(0).getIdPtC11(),
				TOURNEE_POINT_ID_1);
		assertEquals(result.get(0).geTourneeVision().getPointC11Liste().getPointC11s().get(1).getIdPtC11(),
				TOURNEE_POINT_ID_2);
	}

	/**
	 * Vérifie le retour de la request GetC11 quand le service retourne une
	 * exception HTTP_STATUS = 500
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_getTourneeC11_withException() throws Exception {
		// GIVEN
		when(serviceMock.getTournees(any(String.class), any(String.class))).thenThrow(new Exception("ERROR TEST"));
		// WHEN
		final Response response = client.target("http://localhost:" + getPort()).path("getC11").path("pC").path("jour")
				.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
		// THEN
		assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
		assertEquals(response.readEntity(String.class), "ERROR TEST");
	}

	/**
	 * Vérifie le retour de la request GetC11 quand le service retourne une
	 * liste vide
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_getTourneeC11_withNoContent() throws Exception {
		// GIVEN
		when(serviceMock.getTournees(eq("pC"), eq("jour"))).thenReturn(new ArrayList<TourneeC11>());
		// WHEN
		final Response response = client.target("http://localhost:" + getPort()).path("getC11").path("pC").path("jour")
				.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
		// THEN
		assertEquals(response.getStatus(), Status.NO_CONTENT.getStatusCode());
	}
}
