package com.chronopost.vision.microservices.updatereferentiel;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.model.updatereferentiel.DefinitionEvt;
import com.chronopost.vision.model.updatereferentiel.UpdateReferentielEvtInput;
import com.chronopost.vision.model.updatereferentiel.UpdateReferentielInfocompInput;
import com.chronopost.vision.model.updatereferentiel.contrat.ContratVision;
import com.chronopost.vision.model.updatereferentiel.contrat.ReferenceContrat;
import com.datastax.driver.core.exceptions.DriverException;

public class UpdateReferentielResourceTest extends JerseyTestNg.ContainerPerClassTest {

	private static UpdateReferentielService serviceMock = Mockito.mock(UpdateReferentielService.class);
	private Client client;

	@Override
	protected Application configure() {
		/* Création de la resource et initialisation avec le service mocké */
		UpdateReferentielResource resource = new UpdateReferentielResource().setService(serviceMock);

		forceSet(TestProperties.CONTAINER_PORT, "0");

		ResourceConfig config = new ResourceConfig();
		config.register(resource);

		return config;
	}

	@SuppressWarnings("unchecked")
	@BeforeClass
	public void setUp() throws Exception {
		super.setUp();
		client = ClientBuilder.newClient();

		Mockito.reset(serviceMock);

		List<Boolean> resultat = new ArrayList<>();
		resultat.add(true);
		Mockito.when(serviceMock.updateInfoscomp(Mockito.anyMap())).thenReturn(resultat);
		Mockito.when(serviceMock.updateEvt(Mockito.anyList())).thenReturn(resultat);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void updateInfoscomp() {
		Mockito.reset(serviceMock);
		/* Réglage du Mock pour qu'il retourne la réponse désirée */
		/* initialisation des variables à fournir au service */
		UpdateReferentielInfocompInput input = new UpdateReferentielInfocompInput();
		input.setInfoscomp(new HashMap<String, String>());

		/* Test de l'appel */
		Entity<UpdateReferentielInfocompInput> inputEntity = Entity.entity(input, MediaType.APPLICATION_JSON);

		Response e = client.target("http://localhost:" + getPort()).path("/updatereferentiel/infoscomp").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).post(inputEntity);

		assertEquals(e.getStatus(), 200);
		Mockito.verify(serviceMock, Mockito.times(1)).updateInfoscomp(Mockito.anyMap());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void updateEvt() {
		/* initialisation des variables à fournir au service */
		UpdateReferentielEvtInput input = new UpdateReferentielEvtInput();
		DefinitionEvt evt = new DefinitionEvt();
		evt.setCodeEvtAffiche("CC");
		ArrayList<DefinitionEvt> evtList = new ArrayList<DefinitionEvt>();
		evtList.add(evt);
		input.setEvts(evtList);

		/* Test de l'appel */
		Entity<UpdateReferentielEvtInput> inputEntity = Entity.entity(input, MediaType.APPLICATION_JSON);

		Response e = client.target("http://localhost:" + getPort()).path("/updatereferentiel/evt").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).post(inputEntity);

		assertEquals(e.getStatus(), 200);
		Mockito.verify(serviceMock, Mockito.times(1)).updateEvt(Mockito.anyList());
	}

	@Test
	public void insertRefContrat() {
		/* mock une driver exception lors de l'appel au service */
		Mockito.doThrow(new DriverException("Error pas belle")).when(serviceMock)
				.insertRefContrat(Mockito.any(ContratVision.class));

		/* Test de l'appel */
		final ReferenceContrat input = new ReferenceContrat();
		final Entity<ReferenceContrat> inputEntity = Entity.entity(input, MediaType.APPLICATION_JSON);

		final Response e = client.target("http://localhost:" + getPort()).path("/updatereferentiel/contrat").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).post(inputEntity);

		assertEquals(e.getStatus(), 500);
		final Exception entity = e.readEntity(Exception.class);
		assertEquals(entity.getMessage(), "Error pas belle");
	}
}
