package com.chronopost.vision.microservices.insertC11;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.chronopost.vision.model.insertC11.PointC11;
import com.chronopost.vision.model.insertC11.TourneeC11;

public class InsertC11ResourceTest extends JerseyTestNg.ContainerPerClassTest {

	private InsertC11Resource insertC11Resource;
	private Client client;
	private IInsertC11Service serviceMock;

	@Override
	protected Application configure() {
		insertC11Resource = new InsertC11Resource();

		forceSet(TestProperties.CONTAINER_PORT, "0");

		ResourceConfig config = new ResourceConfig();
		config.register(insertC11Resource);
		client = ClientBuilder.newClient();

		return config;
	}

	/**
	 * Initialise le service pour pouvoir donner un comportement différent à ses
	 * méthodes dans chaque test
	 */
	@BeforeMethod
	public void prepareTest() {
		serviceMock = Mockito.mock(IInsertC11Service.class);
		insertC11Resource.setService(serviceMock);
	}

	/**
	 * Extrait le JSON du fichier TourneeC11.json et le POST à la ressource C11
	 * Verifie que l'object TourneeC11 est bien construit et que la méthode
	 * traitementC11 du service est bien appelée
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_postTourneeC11() throws Exception {
		// GIVEN
		ArgumentCaptor<TourneeC11> tourneeC11Captor = ArgumentCaptor.forClass(TourneeC11.class);
		Mockito.when(serviceMock.traitementC11(Mockito.any(TourneeC11.class))).thenReturn(true);
		// read file to retrieve json and send it in body request
		URL url = Thread.currentThread().getContextClassLoader().getResource("TourneeC11.json");
		File file = new File(url.getPath());
		byte[] encoded = Files.readAllBytes(file.toPath());
		String tourneeC11Entity = new String(encoded, StandardCharsets.UTF_8);
		// WHEN
		Response response = client.target("http://localhost:" + getPort()).path("C11").path("v1").request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(tourneeC11Entity, MediaType.APPLICATION_JSON));
		// THEN
		// check request response
		String result = response.readEntity(String.class);
		assertEquals(result, "true");
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
		// check TourneeC11 and PointC11 list entities are well created
		Mockito.verify(serviceMock, Mockito.times(1)).traitementC11(tourneeC11Captor.capture());
		TourneeC11 tourneeC11 = tourneeC11Captor.getAllValues().get(0);
		assertEquals(tourneeC11.geTourneeVision().getIdC11(), "XRE0123456789012345678");
		assertEquals(tourneeC11.geTourneeVision().getDateTournee(), "28/09/2016");
		assertEquals(tourneeC11.geTourneeVision().getCodeTourne(), "CODE_TOURNEE");
		assertEquals(tourneeC11.geTourneeVision().getIdSite(), "ID_SITE");
		assertEquals(tourneeC11.geTourneeVision().getTrigramme(), "XRE");
		assertEquals(tourneeC11.geTourneeVision().getPriseEnCharge(), "PEC");
		assertEquals(tourneeC11.geTourneeVision().getDebutPrevu(), "10:30");
		assertEquals(tourneeC11.geTourneeVision().getDureePrevue(), "99999");
		assertEquals(tourneeC11.geTourneeVision().getDistancePrevue(), "11111");
		assertEquals(tourneeC11.geTourneeVision().getDureePause(), "22222");
		assertEquals(tourneeC11.geTourneeVision().getIdPoiSousTraitant(), "ID_POIST");
		List<PointC11> pointC11s = tourneeC11.geTourneeVision().getPointC11Liste().getPointC11s();
		assertEquals(pointC11s.size(), 2);
		assertEquals(pointC11s.get(0).getNumeroPoint(), "147");
		assertEquals(pointC11s.get(0).getIdPtC11(), "idPoint1");
		assertEquals(pointC11s.get(0).getContrainteHoraire(), "10:45");
		assertEquals(pointC11s.get(0).getHeureDebutRDV(), "10:55");
		assertEquals(pointC11s.get(0).getHeureFinRDV(), "10:59");
		assertEquals(pointC11s.get(0).getGammeProduit(), "GP1");
		assertEquals(pointC11s.get(0).getDestType(), "destType1");
		assertEquals(pointC11s.get(0).getDestNom1(), "destNom1");
		assertEquals(pointC11s.get(0).getDestRaisonSociale1(), "destRS1");
		assertEquals(pointC11s.get(0).getLibelleProduitPoint(), "lib1");
		assertEquals(pointC11s.get(0).getTempsStopService(), "444");
		assertEquals(pointC11s.get(0).getIdAdresse(), "idAd1");
		assertEquals(pointC11s.get(0).getIdDest(), "idDest1");
		assertEquals(pointC11s.get(0).getIsSPOI(), "1");
		assertEquals(pointC11s.get(0).getNbOjets(), "333");
		assertEquals(pointC11s.get(1).getNumeroPoint(), "258");
		assertEquals(pointC11s.get(1).getIdPtC11(), "idPoint2");
		assertEquals(pointC11s.get(1).getContrainteHoraire(), "10:30");
		assertEquals(pointC11s.get(1).getHeureDebutRDV(), "10:35");
		assertEquals(pointC11s.get(1).getHeureFinRDV(), "10:39");
		assertEquals(pointC11s.get(1).getGammeProduit(), "GP2");
		assertEquals(pointC11s.get(1).getDestType(), "destType2");
		assertEquals(pointC11s.get(1).getDestNom1(), "destNom2");
		assertEquals(pointC11s.get(1).getDestRaisonSociale1(), "destRS2");
		assertEquals(pointC11s.get(1).getLibelleProduitPoint(), "lib2");
		assertEquals(pointC11s.get(1).getTempsStopService(), "888");
		assertEquals(pointC11s.get(1).getIdAdresse(), "idAd2");
		assertEquals(pointC11s.get(1).getIdDest(), "idDest2");
		assertEquals(pointC11s.get(1).getIsSPOI(), "0");
		assertEquals(pointC11s.get(1).getNbOjets(), "777");
	}

	/**
	 * Vérifie le retour de la request POST quand le servie retourne une
	 * exception HTTP_STATUS = 500
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_postTourneeC11_withException() throws Exception {
		// GIVEN
		Mockito.when(serviceMock.traitementC11(Mockito.any(TourneeC11.class))).thenThrow(new Exception("ERROR TEST"));
		// read file to retrieve json and send it in body request
		// WHEN
		Response response = client.target("http://localhost:" + getPort()).path("C11").path("v1").request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity("{ \"tournee-vision\": { \"id-c11\": \"idC11\" } }", MediaType.APPLICATION_JSON));
		// THEN
		// check request response
		assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
		assertEquals(response.readEntity(String.class), "ERROR TEST");
	}
}
