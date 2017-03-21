package com.chronopost.vision.microservices.insertC11;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class InsertC11AcceptanceTest extends JerseyTestNg.ContainerPerClassTest {

	private static final Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
	}

	private PreparedStatement getInsertedTourneeC11;
	private PreparedStatement getInsertedIdxTourneeAgence;
	private PreparedStatement getInsertedPoints;

	private Client client;
	private boolean suiteLaunch = true;

	@Override
	protected Application configure() {
		/*
		 * Si le cluster n'existe pas déjà, alors il faut le créer et considérer
		 * que le test est isolé (lancé seul)
		 */
		if (!BuildCluster.clusterHasBuilt) {
			try {
				BuildCluster.setUpBeforeSuite();
				suiteLaunch = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		CCMBridge.ipOfNode(1);

		/* Création de la resource et initialisation avec le service */
		InsertC11Resource resource = new InsertC11Resource();
		resource.setService(InsertC11ServiceImpl.INSTANCE);

		forceSet(TestProperties.CONTAINER_PORT, "0");

		ResourceConfig config = new ResourceConfig();
		config.register(resource);

		return config;
	}

	@BeforeClass
	public void setUp() throws Exception {
		super.setUp();

		client = ClientBuilder.newClient();

		InsertC11ServiceImpl.INSTANCE.setDao(InsertC11DaoImpl.INSTANCE);

		getInsertedTourneeC11 = getSession().prepare("SELECT * FROM tournee WHERE id_tournee = 'XRE0123456789012345678'");
		getInsertedIdxTourneeAgence = getSession()
				.prepare("SELECT * FROM idx_tournee_agence_jour WHERE agence = 'ID_SITE' AND jour = '20162809'");
		getInsertedPoints = getSession()
				.prepare("SELECT * FROM tournee_point WHERE id_point IN ('idPoint1','idPoint2')");

		cleanBaseTest();
	}

	@AfterClass
	public void tearDownAfterClass() throws Exception {
		cleanBaseTest();
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
	}

	/**
	 * Poste une entité de l'objet TourneeC11. Vérifie l'insert en base dans les
	 * bonnes tables et colonnes
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	@Test
	public void test_posteTourneeC11() throws IOException, ParseException {
		// GIVEN
		// read file to retrieve json and send it in body request
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
		URL url = Thread.currentThread().getContextClassLoader().getResource("TourneeC11.json");
		File file = new File(url.getPath());
		byte[] encoded = Files.readAllBytes(file.toPath());
		String tourneeC11Entity = new String(encoded, StandardCharsets.UTF_8);
		// WHEN
		Response response = client.target("http://localhost:" + getPort()).path("C11").path("v1").request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(tourneeC11Entity, MediaType.APPLICATION_JSON));
		// THEN
		String result = response.readEntity(String.class);
		assertEquals(result, "true");
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
		// Verifie la tournée créée
		Row tournee = getTournee();
		assertEquals(tournee.getString("id_tournee"), "XRE0123456789012345678");
		assertEquals(tournee.getString("type_prise_en_charge"), "PEC");
		assertEquals(tournee.getString("debut_prevu"), "10:30");
		assertEquals(tournee.getString("duree_prevue"), "99999");
		assertEquals(tournee.getString("distance_prevue"), "11111");
		assertEquals(tournee.getString("duree_pause"), "22222");
		assertEquals(tournee.getString("idpoistt"), "ID_POIST");
		// Verifie l'index tournée agence créé
		Row idxTourneeAgence = getIdxTourneeAgence();
		assertEquals(idxTourneeAgence.getString("id_tournee"), "XRE0123456789012345678");
		assertEquals(idxTourneeAgence.getTimestamp("date_tournee"), DATE_FORMAT.parse("28/09/2016"));
		assertEquals(DATE_FORMAT.format(idxTourneeAgence.getTimestamp("date_tournee")), "28/09/2016");
		assertEquals(idxTourneeAgence.getString("code_tournee"), "CODE_TOURNEE");
		assertEquals(idxTourneeAgence.getString("jour"), "20162809");
		assertEquals(idxTourneeAgence.getString("agence"), "ID_SITE");
		// Vérifie les points tournée créés
		List<Row> pointsTournee = getPointsTournee();
		assertEquals(pointsTournee.get(0).getString("id_point"), "idPoint1");
		assertEquals(pointsTournee.get(0).getString("heure_contractuelle"), "10:45");
		assertEquals(pointsTournee.get(0).getString("debrdv"), "10:55");
		assertEquals(pointsTournee.get(0).getString("finrdv"), "10:59");
		assertEquals(pointsTournee.get(0).getString("type_destination"), "destType1");
		assertEquals(pointsTournee.get(0).getString("gamme_produit"), "GP1");
		assertEquals(pointsTournee.get(0).getString("nom_destination"), "destNom1");
		assertEquals(pointsTournee.get(0).getString("raison_sociale_destination"), "destRS1");
		assertEquals(pointsTournee.get(0).getString("produit_principal"), "lib1");
		assertEquals(pointsTournee.get(0).getString("duree_stop"), "444");
		assertEquals(pointsTournee.get(0).getString("id_adresse_destination"), "idAd1");
		assertEquals(pointsTournee.get(0).getString("id_destination"), "idDest1");
		assertEquals(pointsTournee.get(0).getString("is_spoi"), "1");
		assertEquals(pointsTournee.get(0).getString("nb_colis_prevus"), "333");

		assertEquals(pointsTournee.get(1).getString("id_point"), "idPoint2");
		assertEquals(pointsTournee.get(1).getString("heure_contractuelle"), "10:30");
		assertEquals(pointsTournee.get(1).getString("debrdv"), "10:35");
		assertEquals(pointsTournee.get(1).getString("finrdv"), "10:39");
		assertEquals(pointsTournee.get(1).getString("type_destination"), "destType2");
		assertEquals(pointsTournee.get(1).getString("gamme_produit"), "GP2");
		assertEquals(pointsTournee.get(1).getString("nom_destination"), "destNom2");
		assertEquals(pointsTournee.get(1).getString("raison_sociale_destination"), "destRS2");
		assertEquals(pointsTournee.get(1).getString("produit_principal"), "lib2");
		assertEquals(pointsTournee.get(1).getString("duree_stop"), "888");
		assertEquals(pointsTournee.get(1).getString("id_adresse_destination"), "idAd2");
		assertEquals(pointsTournee.get(1).getString("id_destination"), "idDest2");
		assertEquals(pointsTournee.get(1).getString("is_spoi"), "0");
		assertEquals(pointsTournee.get(1).getString("nb_colis_prevus"), "777");
	}

	private void cleanBaseTest() {
		getSession().execute("DELETE FROM tournee WHERE id_tournee = 'XRE0123456789012345678'");
		getSession().execute("DELETE FROM tournee WHERE id_tournee = '0123456789012345678'");
		getSession().execute("DELETE FROM idx_tournee_agence_jour WHERE agence = 'ID_SITE' AND jour = '20162809'");
		getSession().execute("DELETE FROM tournee_point WHERE id_point in ('idPoint1','idPoint2')");
	}

	private Row getTournee() {
		return getSession().execute(getInsertedTourneeC11.bind()).one();
	}

	private Row getIdxTourneeAgence() {
		return getSession().execute(getInsertedIdxTourneeAgence.bind()).one();
	}

	private List<Row> getPointsTournee() {
		return getSession().execute(getInsertedPoints.bind()).all();
	}
}
