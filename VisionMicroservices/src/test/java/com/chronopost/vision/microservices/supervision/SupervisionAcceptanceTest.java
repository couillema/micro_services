package com.chronopost.vision.microservices.supervision;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.METHOD_NOT_ALLOWED;
import static org.glassfish.jersey.test.TestProperties.CONTAINER_PORT;
import static org.testng.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.cassandra.table.ETableEvtCounters;
import com.chronopost.cassandra.table.ETableLtCounters;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.supervision.SnapShotVision;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;

public class SupervisionAcceptanceTest extends JerseyTestNg.ContainerPerClassTest {

	private Client client;
	private boolean suiteLaunch = true;
	
	private final static SimpleDateFormat jour_SDF = new SimpleDateFormat("yyyyMMdd");

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
		final SupervisionResource resource = new SupervisionResource();
		resource.setService(SupervisionServiceImpl.INSTANCE);

		forceSet(CONTAINER_PORT, "0");

		final ResourceConfig config = new ResourceConfig();
		config.register(resource);

		return config;
	}

	private static final Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
	}

	@BeforeClass
	public void setUp() throws Exception {
		super.setUp();

		client = ClientBuilder.newClient();

		SupervisionServiceImpl.INSTANCE.setDao(SupervisionDaoImpl.INSTANCE);

		cleanBaseTest();

		// Insére pour le même jour quatre lignes pour trois horaires différents
		getSession().execute("update lt_counters set lt_in_insertlt = lt_in_insertlt+1100, lt_out_insertlt = lt_out_insertlt+1200,"
				+ " hit_insertlt = hit_insertlt+1300 where jour = '20161027' and heure = '16' and minute = '1'");
		getSession().execute("update lt_counters set lt_in_insertlt = lt_in_insertlt+1400, lt_out_insertlt = lt_out_insertlt+1500,"
				+ " hit_insertlt = hit_insertlt+1600 where jour = '20161027' and heure = '19' and minute = '3'");
		getSession().execute("update evt_counters set evt_diffuses = evt_diffuses+900, hit_evt_diffuses = hit_evt_diffuses+500,"
				+ " evt_in_insertevt = evt_in_insertevt+600, evt_out_insertevt = evt_out_insertevt+700, hit_insertevt = hit_insertevt+800,"
				+ " retards_cumules_hit = retards_cumules_hit+900, evt_suivi_box = evt_suivi_box+1000 where jour = '20161027' and heure = '16' and minute = '1'");
		getSession().execute("update evt_counters set evt_diffuses = evt_diffuses+1900, hit_evt_diffuses = hit_evt_diffuses+1500,"
				+ " evt_in_insertevt = evt_in_insertevt+1600, evt_out_insertevt = evt_out_insertevt+1700, hit_insertevt = hit_insertevt+1800,"
				+ " retards_cumules_hit = retards_cumules_hit+1900, evt_suivi_box = evt_suivi_box+110 where jour = '20161027' and heure = '18' and minute = '2'");
		
		// Insére pour le 15/10/2017 uniquement des LT
		getSession().execute("update lt_counters set lt_in_insertlt = lt_in_insertlt+31, lt_out_insertlt = lt_out_insertlt+32,"
				+ " hit_insertlt = hit_insertlt+33 where jour = '20161015' and heure = '16' and minute = '3'");
		// Insére pour le 16/10/2017 uniquement des EVT
		getSession().execute("update evt_counters set evt_diffuses = evt_diffuses+20, hit_evt_diffuses = hit_evt_diffuses+21,"
				+ " evt_in_insertevt = evt_in_insertevt+22, evt_out_insertevt = evt_out_insertevt+23, hit_insertevt = hit_insertevt+24,"
				+ " retards_cumules_hit = retards_cumules_hit+25, evt_suivi_box = evt_suivi_box+26 where jour = '20161016' and heure = '16' and minute = '1'");
	}

	@AfterClass
	public void tearDownAfterClass() throws Exception {
		cleanBaseTest();
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
	}

	@Test
	/**
	 * Entree: les données de counters sur les 10 minutes précédente (et sur les 10 minutes
	 * en cours également pour les cas ou l'on change de dixaine en cours de route.).
	 * 
	 * Sortie: Les données insérées ont été récupérées comme derniere valeurs d'en cours.
	 * 
	 */
	public void test_getRecentSnapShot() {
		// GIVEN
		DateTime dateTime = new DateTime();
		String jour = jour_SDF.format(dateTime.toDate());
		String heure = String.format("%02d",dateTime.get(DateTimeFieldType.hourOfDay()));
		String minute = String.valueOf(dateTime.get(DateTimeFieldType.minuteOfHour())).length() == 1 ? "0"
				: String.valueOf(dateTime.get(DateTimeFieldType.minuteOfHour())).substring(0, 1);

		insertCounters(jour,heure,minute);
		
		dateTime = dateTime.minusMinutes(10);
		jour = jour_SDF.format(dateTime.toDate());
		heure = String.format("%02d",dateTime.get(DateTimeFieldType.hourOfDay()));
		minute = String.valueOf(dateTime.get(DateTimeFieldType.minuteOfHour())).length() == 1 ? "0"
				: String.valueOf(dateTime.get(DateTimeFieldType.minuteOfHour())).substring(0, 1);
		
		insertCounters(jour,heure,minute);
		
		// WHEN
		final Response response = client.target("http://localhost:" + getPort()).path("supervision").request()
				.accept(APPLICATION_JSON_TYPE).get();
		final SnapShotVision result = response.readEntity(SnapShotVision.class);
		
		assertEquals(result.getAskEvt().longValue(), 600);
		assertEquals(result.getInsertEvt().longValue(), 700);
		assertEquals(result.getHitEvt().longValue(), 800);
		assertEquals(result.getDiffEvt().longValue(), 400);
		assertEquals(result.getHitDiffEvt().longValue(), 500);
		assertEquals(result.getAskLt().longValue(), 100);
		assertEquals(result.getInsertLt().longValue(), 200);
		assertEquals(result.getHitLt().longValue(), 300);
		assertEquals(result.getJour(), jour);
		assertEquals(result.getHeure(), heure);
		assertEquals(result.getMinute(), minute);
	}
	
	/** 
	 * Insertion de valeurs de compteur à la date indiquée
	 * 
	 * @param jour2
	 * @param heure2
	 * @param minute2
	 */
	private void insertCounters(String jour, String heure, String minute) {
		// Insére des données pour la dernière dizaine de minutes écoulées ex : si il est 19h45, la dizaine est 19h30 à 19h30 donc hour : 19 minute : 3
		getSession().execute(
				"update lt_counters set lt_in_insertlt = lt_in_insertlt+100, lt_out_insertlt = lt_out_insertlt+200,"
						+ " hit_insertlt = hit_insertlt+300 where jour = '" + jour + "' and heure = '" + heure
						+ "' and minute = '" + minute + "'");
		getSession().execute(
				"update evt_counters set evt_diffuses = evt_diffuses+400, hit_evt_diffuses = hit_evt_diffuses+500,"
						+ " evt_in_insertevt = evt_in_insertevt+600, evt_out_insertevt = evt_out_insertevt+700, hit_insertevt = hit_insertevt+800,"
						+ " retards_cumules_hit = retards_cumules_hit+900, evt_suivi_box = evt_suivi_box+1000 where jour = '"
						+ jour + "' and heure = '" + heure + "' and minute = '" + minute + "'");
	}

	/**
	 * Récupére la liste de SnapShotVision pour un jour
	 */
	@Test
	public void test_getSnapShotForADay() {
		// WHEN
		final Response response = client.target("http://localhost:" + getPort()).path("supervision").path("releve").queryParam("jour", "20161027").request()
				.accept(APPLICATION_JSON_TYPE).get();
		// THEN
		final ArrayList<SnapShotVision> snapShotVisions = response.readEntity(new GenericType<ArrayList<SnapShotVision>>() { });
		assertEquals(snapShotVisions.size(), 3);
		assertEquals(snapShotVisions.get(0).getAskLt().longValue(), 1100);
		assertEquals(snapShotVisions.get(0).getInsertLt().longValue(), 1200);
		assertEquals(snapShotVisions.get(0).getHitLt().longValue(), 1300);
		assertEquals(snapShotVisions.get(0).getAskEvt().longValue(), 600);
		assertEquals(snapShotVisions.get(0).getInsertEvt().longValue(), 700);
		assertEquals(snapShotVisions.get(0).getHitEvt().longValue(), 800);
		assertEquals(snapShotVisions.get(0).getDiffEvt().longValue(), 900);
		assertEquals(snapShotVisions.get(0).getHitDiffEvt().longValue(), 500);
		assertEquals(snapShotVisions.get(0).getJour(), "20161027");
		assertEquals(snapShotVisions.get(0).getHeure(), "16");
		assertEquals(snapShotVisions.get(0).getMinute(), "1");
		
		assertEquals(snapShotVisions.get(1).getAskLt().longValue(), 0);
		assertEquals(snapShotVisions.get(1).getInsertLt().longValue(), 0);
		assertEquals(snapShotVisions.get(1).getHitLt().longValue(), 0);
		assertEquals(snapShotVisions.get(1).getAskEvt().longValue(), 1600);
		assertEquals(snapShotVisions.get(1).getInsertEvt().longValue(), 1700);
		assertEquals(snapShotVisions.get(1).getHitEvt().longValue(), 1800);
		assertEquals(snapShotVisions.get(1).getDiffEvt().longValue(), 1900);
		assertEquals(snapShotVisions.get(1).getHitDiffEvt().longValue(), 1500);
		assertEquals(snapShotVisions.get(1).getJour(), "20161027");
		assertEquals(snapShotVisions.get(1).getHeure(), "18");
		assertEquals(snapShotVisions.get(1).getMinute(), "2");
		
		assertEquals(snapShotVisions.get(2).getAskLt().longValue(), 1400);
		assertEquals(snapShotVisions.get(2).getInsertLt().longValue(), 1500);
		assertEquals(snapShotVisions.get(2).getHitLt().longValue(), 1600);
		assertEquals(snapShotVisions.get(2).getAskEvt().longValue(), 0);
		assertEquals(snapShotVisions.get(2).getInsertEvt().longValue(), 0);
		assertEquals(snapShotVisions.get(2).getHitEvt().longValue(), 0);
		assertEquals(snapShotVisions.get(2).getDiffEvt().longValue(), 0);
		assertEquals(snapShotVisions.get(2).getHitDiffEvt().longValue(), 0);
		assertEquals(snapShotVisions.get(2).getJour(), "20161027");
		assertEquals(snapShotVisions.get(2).getHeure(), "19");
		assertEquals(snapShotVisions.get(2).getMinute(), "3");
	}
	
	/**
	 * Récupére la liste de SnapShotVision pour un jour sans LT
	 */
	@Test
	public void test_getSnapShotForADay_noLt() {
		// WHEN
		final Response response = client.target("http://localhost:" + getPort()).path("supervision").path("releve").queryParam("jour", "20161016").request()
				.accept(APPLICATION_JSON_TYPE).get();
		// THEN
		final ArrayList<SnapShotVision> snapShotVisions = response.readEntity(new GenericType<ArrayList<SnapShotVision>>() { });
		assertEquals(snapShotVisions.size(), 1);
		assertEquals(snapShotVisions.get(0).getAskLt().longValue(), 0);
		assertEquals(snapShotVisions.get(0).getInsertLt().longValue(), 0);
		assertEquals(snapShotVisions.get(0).getHitLt().longValue(), 0);
		assertEquals(snapShotVisions.get(0).getAskEvt().longValue(), 22);
		assertEquals(snapShotVisions.get(0).getInsertEvt().longValue(), 23);
		assertEquals(snapShotVisions.get(0).getHitEvt().longValue(), 24);
		assertEquals(snapShotVisions.get(0).getDiffEvt().longValue(), 20);
		assertEquals(snapShotVisions.get(0).getHitDiffEvt().longValue(), 21);
		assertEquals(snapShotVisions.get(0).getJour(), "20161016");
		assertEquals(snapShotVisions.get(0).getHeure(), "16");
		assertEquals(snapShotVisions.get(0).getMinute(), "1");
	}
	
	/**
	 * Récupére la liste de SnapShotVision pour un jour sans EVT
	 */
	@Test
	public void test_getSnapShotForADay_noEvt() {
		// WHEN
		final Response response = client.target("http://localhost:" + getPort()).path("supervision").path("releve").queryParam("jour", "20161015").request()
				.accept(APPLICATION_JSON_TYPE).get();
		// THEN
		final ArrayList<SnapShotVision> snapShotVisions = response.readEntity(new GenericType<ArrayList<SnapShotVision>>() { });
		assertEquals(snapShotVisions.size(), 1);
		assertEquals(snapShotVisions.get(0).getAskLt().longValue(), 31);
		assertEquals(snapShotVisions.get(0).getInsertLt().longValue(), 32);
		assertEquals(snapShotVisions.get(0).getHitLt().longValue(), 33);
		assertEquals(snapShotVisions.get(0).getAskEvt().longValue(), 0);
		assertEquals(snapShotVisions.get(0).getInsertEvt().longValue(), 0);
		assertEquals(snapShotVisions.get(0).getHitEvt().longValue(), 0);
		assertEquals(snapShotVisions.get(0).getDiffEvt().longValue(), 0);
		assertEquals(snapShotVisions.get(0).getHitDiffEvt().longValue(), 0);
		assertEquals(snapShotVisions.get(0).getJour(), "20161015");
		assertEquals(snapShotVisions.get(0).getHeure(), "16");
		assertEquals(snapShotVisions.get(0).getMinute(), "3");
	}
	
	/**
	 * Récupére l'objet SnapShotVision contenant les counters moyens pour un jour
	 */
	@Test
	public void test_getSnapShotAverage() {
		// WHEN
		final Response response = client.target("http://localhost:" + getPort()).path("supervision").path("average").queryParam("jour", "20161027").request()
				.accept(APPLICATION_JSON_TYPE).get();
		// THEN
		final SnapShotVision snapShotsAverage = response.readEntity(SnapShotVision.class);
		assertEquals(snapShotsAverage.getAskEvt(), new Long(2200/144));
		assertEquals(snapShotsAverage.getAskEvt().longValue(), 15L);
		assertEquals(snapShotsAverage.getDiffEvt(), new Long(2800/144));
		assertEquals(snapShotsAverage.getDiffEvt().longValue(), 19L);
		assertEquals(snapShotsAverage.getHitDiffEvt(), new Long(2000/144));
		assertEquals(snapShotsAverage.getHitDiffEvt().longValue(), 13L);
		assertEquals(snapShotsAverage.getHitEvt(), new Long(2600/144));
		assertEquals(snapShotsAverage.getHitEvt().longValue(), 18L);
		assertEquals(snapShotsAverage.getInsertEvt(), new Long(2400/144));
		assertEquals(snapShotsAverage.getInsertEvt().longValue(), 16L);
		assertEquals(snapShotsAverage.getAskLt(), new Long(2500/144));
		assertEquals(snapShotsAverage.getAskLt().longValue(), 17L);
		assertEquals(snapShotsAverage.getHitLt(), new Long(2900/144));
		assertEquals(snapShotsAverage.getHitLt().longValue(), 20L);
		assertEquals(snapShotsAverage.getInsertLt(), new Long(2700/144));
		assertEquals(snapShotsAverage.getInsertLt().longValue(), 18L);
		assertEquals(snapShotsAverage.getJour(), "20161027");
	}
	
	/**
	 * Teste la response du MS quand le param jour est dans le mauvais format
	 */
	@Test
	public void test_getSnapShotAverage_withBadFormatParam() {
		// WHEN
		final Response response = client.target("http://localhost:" + getPort()).path("supervision").path("average").queryParam("jour", "2016").request()
				.accept(APPLICATION_JSON_TYPE).get();
		// THEN
		assertEquals(response.getStatus(), METHOD_NOT_ALLOWED.getStatusCode());
		assertEquals(response.readEntity(String.class), "Format du paramètre jour incorrect. Doit être yyyyMMdd");
	}
	
	/**
	 * Teste la response du MS quand le param jour est dans le futur
	 */
	@Test
	public void test_getSnapShotAverage_withJourInTheFuture() {
		// GIVEN
		final DateTime dateTime = new DateTime().plusDays(3);
		// WHEN
		final Response response = client.target("http://localhost:" + getPort()).path("supervision").path("average")
				.queryParam("jour", jour_SDF.format(dateTime.toDate())).request().accept(APPLICATION_JSON_TYPE).get();
		// THEN
		assertEquals(response.getStatus(), METHOD_NOT_ALLOWED.getStatusCode());
		assertEquals(response.readEntity(String.class), "Paramètre jour doit être dans le passé ou aujourd'hui");
	}
	
	/**
	 * Teste l'url cehckMsStatus
	 */
	@Test
	public void test_checkSessionActive() {
		// WHEN
		final Response response = client.target("http://localhost:" + getPort()).path("supervision").path("msStatus")
				.request().accept(APPLICATION_JSON_TYPE).get();
		// THEN
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
		assertEquals(response.readEntity(String.class), "true");
	}

	private void cleanBaseTest() {
		getSession().execute(QueryBuilder.truncate(ETableLtCounters.TABLE_NAME));
		getSession().execute(QueryBuilder.truncate(ETableEvtCounters.TABLE_NAME));
	}
}
