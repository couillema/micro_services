package com.chronopost.vision.microservices.updatereferentiel;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.updatereferentiel.DefinitionEvt;
import com.chronopost.vision.model.updatereferentiel.UpdateReferentielEvtInput;
import com.chronopost.vision.model.updatereferentiel.UpdateReferentielInfocompInput;
import com.chronopost.vision.transco.dao.TranscoderDao;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class UpdateReferentielAcceptanceTest extends JerseyTestNg.ContainerPerClassTest {

	private boolean suiteLaunch = true;
	private Client client;
	private Session sessionFlusks;
	private Session sessionVision;

	@SuppressWarnings("unchecked")
	@Override
	protected Application configure() {
		/*
		 * Si le cluster n'existe pas déjà, alors il faut le créer et considérer
		 * que le test est isolé (lancé seul)
		 */
		if (!BuildCluster.clusterHasBuilt) {
			try {
				BuildCluster.setUpBeforeSuite();
			} catch (Exception e) {
				e.printStackTrace();
			}
			suiteLaunch = false;
		}
		CCMBridge.ipOfNode(1);

		sessionFlusks = BuildCluster.sessionFluks;
		sessionVision = VisionMicroserviceApplication.getCassandraSession();
		TranscoderDao.INSTANCE.setCassandraSession(sessionFlusks);
		ReferentielVisionDaoImpl.INSTANCE.setCassandraSession(sessionVision);
		CacheManager<Parametre> mockParametre = Mockito.mock(CacheManager.class);
		Parametre param = new Parametre("TTL_REF_CONTRAT", "10");
		Mockito.when(mockParametre.getValue(Mockito.eq("TTL_REF_CONTRAT"))).thenReturn(param);
		ReferentielVisionDaoImpl.INSTANCE.setRefentielParametre(mockParametre);
		UpdateReferentielService service = UpdateReferentielServiceImpl.getInstance().setDao(TranscoderDao.INSTANCE);
		service.setDaoVision(ReferentielVisionDaoImpl.INSTANCE);

		/* Création de la resource et initialisation avec le service mocké */
		UpdateReferentielResource resource = new UpdateReferentielResource().setService(service);

		forceSet(TestProperties.CONTAINER_PORT, "0");

		ResourceConfig config = new ResourceConfig();
		config.register(resource);
		return config;
	}

	@BeforeClass
	public void setUp() throws Exception {
		super.setUp();
		client = ClientBuilder.newClient();
		sessionVision.execute("truncate parametre");
		sessionVision.execute("truncate ref_contrat");
	}

	@AfterClass
	public void tearDownAfterClass() throws Exception {
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
	}

	// @Test
	public void updateInfoscomp() {

		/* initialisation des variables à fournir au service */
		UpdateReferentielInfocompInput input = new UpdateReferentielInfocompInput();
		Map<String, String> infoscomp = new HashMap<>();
		infoscomp.put("test1", "1");
		infoscomp.put("test2", "2");
		input.setInfoscomp(infoscomp);

		/* Test de l'appel */
		Entity<UpdateReferentielInfocompInput> inputEntity = Entity.entity(input, MediaType.APPLICATION_JSON);

		Response e = client.target("http://localhost:" + getPort()).path("/updatereferentiel/infoscomp").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).post(inputEntity);

		// tests pré-éxécution
		Map<String, String> id_infoscomp = TranscoderDao.INSTANCE.getTranscodificationsFromDatabase("DiffusionVision")
				.get("id_infocomp");
		assertEquals(infoscomp.get("test1"), id_infoscomp.get("test1"));
		assertEquals(infoscomp.get("test2"), id_infoscomp.get("test2"));

		int status = e.getStatus();
		assertEquals(status, 200);
	}

	// @Test
	public void updateEvt() {
		/* initialisation des variables à fournir au service */
		UpdateReferentielEvtInput input = new UpdateReferentielEvtInput();
		// initialisations
		List<DefinitionEvt> evts = new ArrayList<>();

		DefinitionEvt def1 = new DefinitionEvt();
		def1.setIdEvenement("idEvenement1");
		def1.setCodeProducerInput("codeProducerInput1");
		def1.setCodeEvtInput("codeEvtInput1");
		def1.setPriorite("priorite1");
		def1.setLibVueCalculRetard("libVueCalculRetard1");
		def1.setLivVueChronotrace("livVueChronotrace1");
		def1.setLibEvt("libEvt1");

		DefinitionEvt def2 = new DefinitionEvt();
		def2.setIdEvenement("idEvenement2");
		def2.setCodeProducerInput("codeProducerInput2");
		def2.setCodeEvtInput("codeEvtInput2");
		def2.setPriorite("priorite2");
		def2.setLibVueCalculRetard("libVueCalculRetard2");
		def2.setLivVueChronotrace("livVueChronotrace2");
		def2.setLibEvt("libEvt2");

		evts.add(def1);
		evts.add(def2);

		input.setEvts(evts);

		/* Test de l'appel */
		Entity<UpdateReferentielEvtInput> inputEntity = Entity.entity(input, MediaType.APPLICATION_JSON);

		Response e = client.target("http://localhost:" + getPort()).path("/updatereferentiel/evt").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).post(inputEntity);

		int status = e.getStatus();
		assertEquals(status, 200);

		Map<String, String> code_id_evt = TranscoderDao.INSTANCE.getTranscodificationsFromDatabase("DiffusionVision")
				.get("code_id_evt");
		Map<String, String> evenements = TranscoderDao.INSTANCE.getTranscodificationsFromDatabase("DiffusionVision")
				.get("evenements");

		assertEquals("idEvenement1", code_id_evt.get("codeProducerInput1|codeEvtInput1"));
		assertEquals("idEvenement2", code_id_evt.get("codeProducerInput2|codeEvtInput2"));
		assertEquals("codeEvtInput1|priorite1|libVueCalculRetard1|livVueChronotrace1|libEvt1",
				evenements.get("idEvenement1"));
		assertEquals("codeEvtInput2|priorite2|libVueCalculRetard2|livVueChronotrace2|libEvt2",
				evenements.get("idEvenement2"));
	}

	/**
	 * Appelle insertRefContrat avec deux JSON. 5 éléments total, 5 données dans
	 * les JSON. Vérifie que le status du dernier appel est 201
	 * 
	 * @throws IOException
	 */
	@Test
	public void test_insertRefContratOk() throws IOException {
		// GIVEN
		final URL url1 = Thread.currentThread().getContextClassLoader().getResource("ReferenceContrat1.json");
		final File file1 = new File(url1.getPath());
		final String refContrat1 = new String(Files.readAllBytes(file1.toPath()), StandardCharsets.UTF_8);
		final URL url2 = Thread.currentThread().getContextClassLoader().getResource("ReferenceContrat2.json");
		final File file2 = new File(url2.getPath());
		final String refContrat2 = new String(Files.readAllBytes(file2.toPath()), StandardCharsets.UTF_8);

		// WHEN
		final Response response1 = client.target("http://localhost:" + getPort()).path("/updatereferentiel/contrat")
				.request().accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(refContrat1, MediaType.APPLICATION_JSON));
		final Response response2 = client.target("http://localhost:" + getPort()).path("/updatereferentiel/contrat")
				.request().accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(refContrat2, MediaType.APPLICATION_JSON));

		// THEN
		assertEquals(response1.getStatus(), 200);
		assertEquals(response2.getStatus(), 201);
		final ResultSet resultSelectRef = sessionVision
				.execute("select numero_version, numero_contrat, caracteristiques from ref_contrat");
		final List<Row> refList = resultSelectRef.all();
		assertEquals(refList.get(0).getString(0), "20170214102345");
		assertEquals(refList.get(0).getString(1), "no1");
		assertEquals(refList.get(0).getSet(2, String.class), new HashSet<>(Arrays.asList("ATTRACTIF_0")));
		assertEquals(refList.get(1).getString(0), "20170214102345");
		assertEquals(refList.get(1).getString(1), "no2");
		assertEquals(refList.get(1).getSet(2, String.class), new HashSet<>(Arrays.asList("ATTRACTIF_1")));
		assertEquals(refList.get(2).getString(0), "20170214102345");
		assertEquals(refList.get(2).getString(1), "no3");
		assertEquals(refList.get(2).getSet(2, String.class), new HashSet<>(Arrays.asList("ATTRACTIF_2")));
		assertEquals(refList.get(3).getString(0), "20170214102345");
		assertEquals(refList.get(3).getString(1), "no4");
		assertEquals(refList.get(3).getSet(2, String.class), new HashSet<>(Arrays.asList("ATTRACTIF_0")));
		assertEquals(refList.get(4).getString(0), "20170214102345");
		assertEquals(refList.get(4).getString(1), "no5");
		assertEquals(refList.get(4).getSet(2, String.class), new HashSet<>(Arrays.asList("SENSIBLE")));

		// vérifie que le param 'VERSION_REF_CONTRAT' a été mis à jour avec la
		// version donnée
		final ResultSet resultSelectParam = sessionVision
				.execute("select valeur from parametre where code = 'VERSION_REF_CONTRAT'");
		assertEquals(resultSelectParam.one().getString(0), "20170214102345");
	}

	/**
	 * Appelle insertRefContrat avec deux JSON. 5 éléments total, 3 données dans
	 * les JSON. Vérifie que le status du dernier appel est 417
	 * 
	 * @throws IOException
	 */
	@Test
	public void test_insertRefContratKo() throws IOException {
		// GIVEN
		final URL url1 = Thread.currentThread().getContextClassLoader().getResource("ReferenceContrat1.json");
		final File file1 = new File(url1.getPath());
		final String refContrat1 = new String(Files.readAllBytes(file1.toPath()), StandardCharsets.UTF_8);
		final URL url2 = Thread.currentThread().getContextClassLoader().getResource("ReferenceContrat3.json");
		final File file2 = new File(url2.getPath());
		final String refContrat2 = new String(Files.readAllBytes(file2.toPath()), StandardCharsets.UTF_8);

		// WHEN
		final Response response1 = client.target("http://localhost:" + getPort()).path("/updatereferentiel/contrat")
				.request().accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(refContrat1, MediaType.APPLICATION_JSON));
		final Response response2 = client.target("http://localhost:" + getPort()).path("/updatereferentiel/contrat")
				.request().accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(refContrat2, MediaType.APPLICATION_JSON));

		// THEN
		assertEquals(response1.getStatus(), 200);
		assertEquals(response2.getStatus(), 417);
	}
	
	/**
	 * Appelle insertRefContrat avec deux JSON. 5 éléments total, 3 données dans
	 * les JSON. Vérifie que le status du dernier appel est 417
	 * 
	 * @throws IOException
	 */
	@Test
	public void test_insertRefContratCorrompu() throws IOException {
		// GIVEN
		final URL url1 = Thread.currentThread().getContextClassLoader().getResource("ReferenceContrat4.json");
		final File file1 = new File(url1.getPath());
		final String refContrat1 = new String(Files.readAllBytes(file1.toPath()), StandardCharsets.UTF_8);

		// WHEN
		final Response response1 = client.target("http://localhost:" + getPort()).path("/updatereferentiel/contrat")
				.request().accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(refContrat1, MediaType.APPLICATION_JSON));

		// THEN
		assertEquals(response1.getStatus(), 500);
	}

	@Test
	public void test_insertRefContratFULL() throws IOException {
		// GIVEN
		final URL url = Thread.currentThread().getContextClassLoader().getResource("RefContratFull.json");
		final File file = new File(url.getPath());
		final String refContrat = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

		// WHEN
		final Response response1 = client.target("http://localhost:" + getPort()).path("/updatereferentiel/contrat")
				.request().accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(refContrat, MediaType.APPLICATION_JSON));

		// THEN
		assertEquals(response1.getStatus(), 201);

		// vérifie que le param 'VERSION_REF_CONTRAT' a été mis à jour avec la
		// version donnée
		final ResultSet resultSelectParam = sessionVision
				.execute("select valeur from parametre where code = 'VERSION_REF_CONTRAT'");
		assertEquals(resultSelectParam.one().getString(0), "1488204278609");
	}
}
