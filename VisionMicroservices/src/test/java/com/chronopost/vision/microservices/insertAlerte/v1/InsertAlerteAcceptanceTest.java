package com.chronopost.vision.microservices.insertAlerte.v1;

import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildSelect;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.cassandra.table.ETableAlerte;
import com.chronopost.cassandra.table.ETableColisSpecifications;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.insertAlerte.v1.Alerte;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

/**
 * 
 * @author bjbari
 *
 */
public class InsertAlerteAcceptanceTest extends JerseyTestNg.ContainerPerClassTest {

	static final Logger logger = LoggerFactory.getLogger(InsertAlerteAcceptanceTest.class);

	private boolean suiteLaunch = true;
	private Client client;

	/** requête pour récupérer une alerte de la table alerte */
	private PreparedStatement psSelectRecordAlerte;

	/**
	 * requête pour récupérer la colonne alerte de la table colis_specification
	 */
	private PreparedStatement psSelectRecordSpecifColis;

	private static final String AGENCE = "99999";
	private static final DateTime MAINTENANT = DateTime.now();
	private static final String JOUR = MAINTENANT.toString("yyyyMMdd");
	private static final String HEURE = MAINTENANT.toString("HHmm");
	private static final String TYPE_RPTSDTA = "RPTSDTA";

	/**
	 * @return VisionMicroserviceApplication.cassandraSession (a
	 *         com.datastax.driver.core )
	 */
	private static final Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
	}

	/**
	 * Binding the service to the instantiation of the resource
	 */
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
				logger.debug(e.toString());
			}
			suiteLaunch = false;
		}
		CCMBridge.ipOfNode(1);

		/* Création de la resource et initialisation avec le service mocké */
		InsertAlerteResource insertAlerteResource = new InsertAlerteResource();
		insertAlerteResource.setService(InsertAlerteServiceImpl.INSTANCE);

		forceSet(TestProperties.CONTAINER_PORT, "0");

		ResourceConfig config = new ResourceConfig();
		config.register(insertAlerteResource);

		return config;
	}

	@BeforeClass
	public void setUp() throws Exception {
		logger.debug("before class Valeur du port={}", getPort());

		super.setUp();

		client = ClientBuilder.newClient();

		InsertAlerteServiceImpl.INSTANCE.setDao(InsertAlerteDaoImpl.INSTANCE);

		buildSelect(ETableAlerte.TABLE_NAME);

		psSelectRecordAlerte = getSession().prepare(buildSelect(ETableAlerte.TABLE_NAME).getQuery());

		psSelectRecordSpecifColis = getSession().prepare(
				buildSelect(ETableColisSpecifications.TABLE_NAME, Arrays.asList(ETableColisSpecifications.ALERTES))
						.getQuery());
		cleanBaseTest();
	}

	@AfterClass
	public void cleanAfterClass() throws Exception {
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
	}

	@Test
	/**
	 * En Entrée : une alerte de type "RPTSDTA"
	 * 
	 * Attendu : présence de l'alerte dans la table alerte avec l'agence, jour,
	 * heure, type et le noLt indiqué et MAJ de la colonne alerte de la table
	 * colis_specification sous la forme {AGENCE,JOUR,HEURE,TYPE_RPTSDTA}
	 */
	public void insertAlerte() throws Exception {
		/* init - Construction des data du test */
		String noLt = "EE00000000XFR";
		List<Alerte> alertes = new ArrayList<>();
		alertes.add(createAlerte(AGENCE, JOUR, HEURE, TYPE_RPTSDTA, noLt));
		Gson gson = new Gson();
		String oneAlerte = gson.toJson(Arrays.asList(AGENCE, JOUR, HEURE, TYPE_RPTSDTA));
		Set<String> oneAlerteJson = Sets.newHashSet(oneAlerte);
		/* Invocation du MicroService */
		int status = appelMSInserteAlertes(alertes).getStatus();

		/* Vérifications */
		assertEquals(status, 200);

		Row row_1 = getAlerte(noLt);
		assertTrue(row_1 != null);
		assertEquals(row_1.get("agence", String.class), AGENCE);
		assertEquals(row_1.get("jour", String.class), JOUR);
		assertEquals(row_1.get("heure", String.class), HEURE);
		assertEquals(row_1.get("type", String.class), TYPE_RPTSDTA);
		assertEquals(row_1.get("no_lt", String.class), noLt);

		Row row_2 = getSpecifColisAlerte(noLt);
		assertTrue(row_2 != null);
		assertEquals(row_2.getSet("alertes", String.class), oneAlerteJson);

		/* Clean */
		cleanBaseTest();
	}

	/**
	 * /** @param noLt
	 * 
	 * @return un enregistrement de la table alerte
	 */
	private Row getAlerte(@NotNull String noLt) {
		return getSession().execute(psSelectRecordAlerte.bind(AGENCE, JOUR, HEURE, TYPE_RPTSDTA, noLt)).one();
	}

	/**
	 * /** @param noLt
	 * 
	 * @return la colonne alerte de la table colis_specification
	 */
	private Row getSpecifColisAlerte(@NotNull String noLt) {
		return getSession().execute(psSelectRecordSpecifColis.bind(noLt)).one();
	}

	/**
	 * Appel MS InsertAlertes.v1
	 * 
	 * @param alertes
	 * @return
	 */
	private Response appelMSInserteAlertes(List<Alerte> alertes) {
		WebTarget a = client.target("http://localhost:" + getPort());
		WebTarget b = a.path("/InsertAlertes");
		Builder c = b.request();
		Builder d = c.accept(MediaType.APPLICATION_JSON_TYPE);
		Entity<List<Alerte>> f = Entity.entity(alertes, MediaType.APPLICATION_JSON);
		Response e = d.post(f);
		return e;
	}

	private Alerte createAlerte(final String agence, final String jour, final String heure, final String type,
			final String noLt) {
		Alerte alerte = new Alerte();
		alerte.setAgence(agence);
		alerte.setJour(jour);
		alerte.setHeure(heure);
		alerte.setType(type);
		alerte.setNoLt(noLt);
		return alerte;
	}

	/**
	 * Clean des données
	 * 
	 * @param noLt
	 */
	private void cleanBaseTest() {
		getSession().execute(QueryBuilder.truncate(ETableColisSpecifications.TABLE_NAME));
		getSession().execute(QueryBuilder.truncate(ETableAlerte.TABLE_NAME));
	}
}
