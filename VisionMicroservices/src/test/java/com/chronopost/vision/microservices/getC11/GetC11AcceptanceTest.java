package com.chronopost.vision.microservices.getC11;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.insertC11.PointC11;
import com.chronopost.vision.model.insertC11.TourneeC11;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

public class GetC11AcceptanceTest extends JerseyTestNg.ContainerPerClassTest {

	private static final Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
	}

	private PreparedStatement psInsertIdxAgenceJour;
	private PreparedStatement psInsertTournee;
	private PreparedStatement psInsertTourneePoint;
	private PreparedStatement psDeleteIdxAgenceJour;
	private PreparedStatement psDeleteTournee;
	private PreparedStatement psDeleteTourneePoint;
	
	private final static String AGENCE = "99999";
	private final static String JOUR = "20161222";

	private final static String CODE_TOURNEE_1 = "44M01";
	private final static String CODE_TOURNEE_2 = "44M02";
	private final static String DATE_TOURNEE_1 = "2016-12-22 12:00:00";
	private final static String DATE_TOURNEE_2 = "2016-12-22 13:00:00";
	private final static String ID_TOURNEE_1 = "44M0003122016000000";
	private final static String ID_TOURNEE_2 = "44M0103122016102852";
	private final static String TOURNEE_POINT_ID_1 = "point1";
	private final static String TOURNEE_POINT_ID_2 = "point2";
	private final static String TOURNEE_POINT_ID_3 = "point3";
	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
		final GetC11Resource resource = new GetC11Resource();
		resource.setService(GetC11ServiceImpl.INSTANCE);

		forceSet(TestProperties.CONTAINER_PORT, "0");

		final ResourceConfig config = new ResourceConfig();
		config.register(resource);
		return config;
	}

	@BeforeClass
	public void setUp() throws Exception {
		super.setUp();

		client = ClientBuilder.newClient();

		GetC11ServiceImpl.INSTANCE.setDao(GetC11DaoImpl.INSTANCE);

		// Prepare insert dans idx_tournee_agence_jour
		psInsertIdxAgenceJour = GetC11TestRequests.getInsertIdxAgenceJour(getSession());

		// Prepare insert dans tournee
		psInsertTournee = GetC11TestRequests.getInsertTournee(getSession());

		// Prepare insert dans tournee_point
		psInsertTourneePoint = GetC11TestRequests.getInsertTourneePoint(getSession());

		// Prepare delete dans idx_tournee_agence_jour
		psDeleteIdxAgenceJour = GetC11TestRequests.getDeleteIdxAgenceJour(getSession());

		// Prepare delete dans tournee
		psDeleteTournee = GetC11TestRequests.getDeleteTournee(getSession());
		
		// Prepare delete dans tournee_point
		psDeleteTourneePoint = GetC11TestRequests.getDeleteTourneePoint(getSession());
	}

	/**
	 * Poste une entité de l'objet TourneeC11. Vérifie l'insert en base dans les
	 * bonnes tables et colonnes
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	@Test
	public void test_getTourneeC11() throws IOException, ParseException {
		// GIVEN
		// Insére deux idx
		getSession().execute(psInsertIdxAgenceJour.bind(ID_TOURNEE_1, AGENCE, JOUR, CODE_TOURNEE_1, sdf.parse(DATE_TOURNEE_1)));
		getSession().execute(psInsertIdxAgenceJour.bind(ID_TOURNEE_2, AGENCE, JOUR, CODE_TOURNEE_2, sdf.parse(DATE_TOURNEE_2)));
		// Insére deux tournées
		final Set<String> idsPoints1 = new HashSet<String>(Arrays.asList(TOURNEE_POINT_ID_1, TOURNEE_POINT_ID_2));
		getSession().execute(psInsertTournee.bind("TPEC1", "DurPre1", "DistPre1", "DurPau1", "IdPoiStt1", idsPoints1, "debut_prevu_1", ID_TOURNEE_1));
		final Set<String> idsPoints2 = new HashSet<String>(Arrays.asList(TOURNEE_POINT_ID_3));
		getSession().execute(psInsertTournee.bind("TPEC2", "DurPre2", "DistPre2", "DurPau2", "IdPoiStt2", idsPoints2, "debut_prevu_2", ID_TOURNEE_2));
		// Insére trois points tournées
		getSession().execute(psInsertTourneePoint.bind("num1", "11:00", "11:30", "11:45", "type1", "nom1", "raison1", "produit1", "duree1",
				"idAd1", "idDest1", "spoi1", "10", "GP1", TOURNEE_POINT_ID_1));
		getSession().execute(psInsertTourneePoint.bind("num2", "12:00", "12:30", "12:45", "type2", "nom2", "raison2", "produit2", "duree2",
				"idAd2", "idDest2", "spoi2", "20", "GP2", TOURNEE_POINT_ID_2));
		getSession().execute(psInsertTourneePoint.bind("num3", "13:00", "13:30", "13:45", "type3", "nom3", "raison3", "produit3", "duree3",
				"idAd3", "idDest3", "spoi3", "30", "GP3", TOURNEE_POINT_ID_3));
		
		// WHEN
		final Response response = client.target("http://localhost:" + getPort()).path("getC11").path(AGENCE).path(JOUR).request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get();
		// THEN
		final List<TourneeC11> result = response.readEntity(new GenericType<List<TourneeC11>>() {
		});
		assertEquals(response.getStatus(), 200);
		assertEquals(result.size(), 2);
		TourneeC11 tournee1, tournee2;
		if (result.get(0).geTourneeVision().getIdC11().equals(ID_TOURNEE_1)) {
			tournee1 = result.get(0);
			tournee2 = result.get(1);
		} else {
			tournee1 = result.get(1);
			tournee2 = result.get(0);
		}
		assertEquals(tournee1.geTourneeVision().getIdC11(), ID_TOURNEE_1);
		assertEquals(tournee1.geTourneeVision().getPriseEnCharge(), "TPEC1");
		assertEquals(tournee1.geTourneeVision().getDureePrevue(), "DurPre1");
		assertEquals(tournee1.geTourneeVision().getDistancePrevue(), "DistPre1");
		assertEquals(tournee1.geTourneeVision().getDureePause(), "DurPau1");
		assertEquals(tournee1.geTourneeVision().getIdPoiSousTraitant(), "IdPoiStt1");
		assertEquals(tournee1.geTourneeVision().getDebutPrevu(), "debut_prevu_1");
		final List<PointC11> pointsC11T1 = tournee1.geTourneeVision().getPointC11Liste().getPointC11s();
		assertEquals(pointsC11T1.size(), 2);
		PointC11 pointC11_1, pointC11_2;
		if (pointsC11T1.get(0).getNumeroPoint().equals("num1")) {
			pointC11_1 = pointsC11T1.get(0);
			pointC11_2 = pointsC11T1.get(1);
		} else {
			pointC11_1 = pointsC11T1.get(1);
			pointC11_2 = pointsC11T1.get(0);
		}
		assertEquals(pointC11_1.getNumeroPoint(), "num1");
		assertEquals(pointC11_1.getContrainteHoraire(), "11:00");
		assertEquals(pointC11_1.getHeureDebutRDV(), "11:30");
		assertEquals(pointC11_1.getHeureFinRDV(), "11:45");
		assertEquals(pointC11_1.getDestType(), "type1");
		assertEquals(pointC11_1.getDestNom1(), "nom1");
		assertEquals(pointC11_1.getDestRaisonSociale1(), "raison1");
		assertEquals(pointC11_1.getLibelleProduitPoint(), "produit1");
		assertEquals(pointC11_1.getTempsStopService(), "duree1");
		assertEquals(pointC11_1.getIdAdresse(), "idAd1");
		assertEquals(pointC11_1.getIdDest(), "idDest1");
		assertEquals(pointC11_1.getIsSPOI(), "spoi1");
		assertEquals(pointC11_1.getNbOjets(), "10");
		assertEquals(pointC11_1.getGammeProduit(), "GP1");
		assertEquals(pointC11_2.getNumeroPoint(), "num2");
		assertEquals(pointC11_2.getContrainteHoraire(), "12:00");
		assertEquals(pointC11_2.getHeureDebutRDV(), "12:30");
		assertEquals(pointC11_2.getHeureFinRDV(), "12:45");
		assertEquals(pointC11_2.getDestType(), "type2");
		assertEquals(pointC11_2.getDestNom1(), "nom2");
		assertEquals(pointC11_2.getDestRaisonSociale1(), "raison2");
		assertEquals(pointC11_2.getLibelleProduitPoint(), "produit2");
		assertEquals(pointC11_2.getTempsStopService(), "duree2");
		assertEquals(pointC11_2.getIdAdresse(), "idAd2");
		assertEquals(pointC11_2.getIdDest(), "idDest2");
		assertEquals(pointC11_2.getIsSPOI(), "spoi2");
		assertEquals(pointC11_2.getNbOjets(), "20");
		assertEquals(pointC11_2.getGammeProduit(), "GP2");

		assertEquals(tournee2.geTourneeVision().getIdC11(), ID_TOURNEE_2);
		assertEquals(tournee2.geTourneeVision().getPriseEnCharge(), "TPEC2");
		assertEquals(tournee2.geTourneeVision().getDureePrevue(), "DurPre2");
		assertEquals(tournee2.geTourneeVision().getDistancePrevue(), "DistPre2");
		assertEquals(tournee2.geTourneeVision().getDureePause(), "DurPau2");
		assertEquals(tournee2.geTourneeVision().getIdPoiSousTraitant(), "IdPoiStt2");
		assertEquals(tournee2.geTourneeVision().getDebutPrevu(), "debut_prevu_2");
		final List<PointC11> pointsC11T2 = tournee2.geTourneeVision().getPointC11Liste().getPointC11s();
		assertEquals(pointsC11T2.size(), 1);
		final PointC11 pointC11_3 = pointsC11T2.get(0);
		assertEquals(pointC11_3.getNumeroPoint(), "num3");
		assertEquals(pointC11_3.getContrainteHoraire(), "13:00");
		assertEquals(pointC11_3.getHeureDebutRDV(), "13:30");
		assertEquals(pointC11_3.getHeureFinRDV(), "13:45");
		assertEquals(pointC11_3.getDestType(), "type3");
		assertEquals(pointC11_3.getDestNom1(), "nom3");
		assertEquals(pointC11_3.getDestRaisonSociale1(), "raison3");
		assertEquals(pointC11_3.getLibelleProduitPoint(), "produit3");
		assertEquals(pointC11_3.getTempsStopService(), "duree3");
		assertEquals(pointC11_3.getIdAdresse(), "idAd3");
		assertEquals(pointC11_3.getIdDest(), "idDest3");
		assertEquals(pointC11_3.getIsSPOI(), "spoi3");
		assertEquals(pointC11_3.getNbOjets(), "30");
		assertEquals(pointC11_3.getGammeProduit(), "GP3");
	}

	@AfterClass
	public void tearDownAfterClass() throws Exception {
		getSession().execute(psDeleteIdxAgenceJour.bind(AGENCE, JOUR, CODE_TOURNEE_1, sdf.parse(DATE_TOURNEE_1)));
		getSession().execute(psDeleteIdxAgenceJour.bind(AGENCE, JOUR, CODE_TOURNEE_2, sdf.parse(DATE_TOURNEE_2)));
		getSession().execute(psDeleteTournee.bind(ID_TOURNEE_1));
		getSession().execute(psDeleteTournee.bind(ID_TOURNEE_2));
		getSession().execute(psDeleteTourneePoint.bind(TOURNEE_POINT_ID_1));
		getSession().execute(psDeleteTourneePoint.bind(TOURNEE_POINT_ID_2));
		getSession().execute(psDeleteTourneePoint.bind(TOURNEE_POINT_ID_3));
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
	}
}
