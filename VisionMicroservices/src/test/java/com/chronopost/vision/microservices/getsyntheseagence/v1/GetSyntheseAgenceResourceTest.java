package com.chronopost.vision.microservices.getsyntheseagence.v1;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.chronopost.vision.model.getsyntheseagence.v1.ECodeIndicateur;

public class GetSyntheseAgenceResourceTest extends JerseyTestNg.ContainerPerClassTest {
	private static final String GET_SYNTHESE_AGENCE_DISPERSION_ACTIVITE = "/getSyntheseAgence/Activite";
	private static final String GET_SYNTHESE_AGENCE_DISPERSION_ACTIVITE_ET_LISTEVALEUR = "/getSyntheseAgence/ActiviteEtListeValeur";
	private static final String GET_SYNTHESE_AGENCE_DISPERSION_QUANTITE = "/getSyntheseAgence/Dispersion/Quantite";
	private static final String POSTE_COMPTABLE = "94999";
	private static final String HEURE_APPEL = "2016-11-29T01:00:00-05:00";
	/**
	 * Mocking the service
	 * 
	 */
	private SyntheseAgenceResource synthseAgenceResourceTest;
	private ISyntheseAgenceService serviceMock;
	private Client client;

	/**
	 * Binding the service to the instantiation of the resource
	 */
	@Override
	protected Application configure() {
		/* Création de la resource et initialisation avec le service mocké */
		synthseAgenceResourceTest = new SyntheseAgenceResource();

		forceSet(TestProperties.CONTAINER_PORT, "0");

		ResourceConfig config = new ResourceConfig();
		config.register(synthseAgenceResourceTest);

		return config;
	}

	@BeforeClass
	public void setUp() throws Exception {
		super.setUp();
		client = ClientBuilder.newClient();
	}

	@BeforeMethod
	public void initService() {
		serviceMock = Mockito.mock(ISyntheseAgenceService.class);
		synthseAgenceResourceTest.setService(serviceMock);
	}

	@Test
	public void getDispersionUnvalidPath() {
		synthseAgenceResourceTest.setService(serviceMock);
		/* Test de l'appel */
		Response response = get(GET_SYNTHESE_AGENCE_DISPERSION_QUANTITE);
		int status = response.getStatus();

		assertEquals(status, NOT_FOUND.getStatusCode());
	}

	@Test
	public void getDispersionQuantitePosteComptableForDateWithException() {
		/* Réglage du Mock pour qu'il retourne la réponse désirée */
		when(serviceMock.getSyntheseDispersionQuantite(eq(POSTE_COMPTABLE), eq("1"))).thenThrow(new RuntimeException());

		/* Test de l'appel */
		Response response = get(GET_SYNTHESE_AGENCE_DISPERSION_QUANTITE, POSTE_COMPTABLE, "dateAppel", "1");
		int status = response.getStatus();

		assertEquals(status, INTERNAL_SERVER_ERROR.getStatusCode());
	}

	@Test
	public void getDispersionQuantitePosteComptableForNDaysWithException() {
		/* Réglage du Mock pour qu'il retourne la réponse désirée */
		when(serviceMock.getSyntheseDispersionQuantitePassee(eq(POSTE_COMPTABLE), eq(2))).thenThrow(new RuntimeException());

		/* Test de l'appel */
		Response response = get(GET_SYNTHESE_AGENCE_DISPERSION_QUANTITE, POSTE_COMPTABLE, "2");
		int status = response.getStatus();

		assertEquals(status, INTERNAL_SERVER_ERROR.getStatusCode());
	}

	@Test
	public void getDispersionQuantitePosteComptableIndicateurWithException() {
		/* Réglage du Mock pour qu'il retourne la réponse désirée */
		when(serviceMock.getSyntheseDetailIndicateur(POSTE_COMPTABLE, ECodeIndicateur.EN_DISPERSION.getCode(), 1500, HEURE_APPEL, 0))
				.thenThrow(new RuntimeException());
		
		/* Test de l'appel */
		Response response = getDispersionActiviteEtListeValeurAvecIndicateur(ECodeIndicateur.EN_DISPERSION.getCode(),
				1500, 0);
		int status = response.getStatus();

		assertEquals(status, INTERNAL_SERVER_ERROR.getStatusCode());
	}

	
	@Test
	public void getDispersionActivitePosteComptableIndicateurWithException() {
		/* Réglage du Mock pour qu'il retourne la réponse désirée */
		when(serviceMock.getSyntheseDetailIndicateur(POSTE_COMPTABLE, ECodeIndicateur.EN_DISPERSION.getCode(), null, HEURE_APPEL, null))
				.thenThrow(new RuntimeException());
		
		/* Test de l'appel */
		Response response = get(GET_SYNTHESE_AGENCE_DISPERSION_ACTIVITE, POSTE_COMPTABLE, ECodeIndicateur.EN_DISPERSION.getCode(),"dateAppel", HEURE_APPEL);
		int status = response.getStatus();

		assertEquals(status, INTERNAL_SERVER_ERROR.getStatusCode());
	}

	/**
	 * Envoie une requete POST avec un poste comptable en paramètre et retourne
	 * le résultat.
	 * 
	 * @param posteComptable
	 * @return
	 */
	private Response get(String path, String... pathParams) {
		WebTarget target = client.target("http://localhost:" + getPort()).path(path);
		for (String param : pathParams) {
			target = target.path(param);
		}
		Builder requestBuilder = target.request().accept(MediaType.APPLICATION_JSON_TYPE);

		return requestBuilder.get();
	}
	
	/**
	 * 
	 * @param indicateur
	 * @param limit
	 * @param nbJours
	 * @return
	 */
	private Response getDispersionActiviteEtListeValeurAvecIndicateur(String indicateur, Integer limit, Integer nbJours) {
		WebTarget path = ClientBuilder.newClient().target("http://localhost:" + getPort())
				.path(GET_SYNTHESE_AGENCE_DISPERSION_ACTIVITE_ET_LISTEVALEUR +"/"+ POSTE_COMPTABLE + "/" + indicateur +"/dateAppel/"+ HEURE_APPEL+"/"+nbJours);

		if (null != limit) {
			path = path.queryParam("limit", limit);
		}
		
		Response response = path.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get();


		return response;
	}
}
