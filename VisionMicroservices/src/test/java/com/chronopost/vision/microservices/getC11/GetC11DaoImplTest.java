package com.chronopost.vision.microservices.getC11;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class GetC11DaoImplTest {

	private IGetC11Dao dao;
	private boolean suiteLaunch = true;

	private PreparedStatement psInsertIdxAgenceJour;
	private PreparedStatement psInsertTournee;
	private PreparedStatement psInsertTourneePoint;
	private PreparedStatement psDeleteIdxAgenceJour;
	private PreparedStatement psDeleteTournee;
	private PreparedStatement psDeleteTourneePoint;
	
	private final static String AGENCE = "99999";
	private final static String AGENCE_INCORRECTE = "77777";
	private final static String JOUR = "20161222";
	private final static String JOUR_INCORRECT = "20161221";

	private final static String CODE_TOURNEE_1 = "44M01";
	private final static String CODE_TOURNEE_2 = "44M02";
	private final static String CODE_TOURNEE_3 = "44M03";
	private final static String CODE_TOURNEE_4 = "44M04";
	private final static String CODE_TOURNEE_5 = "44M05";
	private final static String DATE_TOURNEE_1 = "2016-12-22 12:00:00";
	private final static String DATE_TOURNEE_2 = "2016-12-22 13:00:00";
	private final static String DATE_TOURNEE_3 = "2016-12-22 14:00:00";
	private final static String DATE_TOURNEE_4 = "2016-12-21 14:00:00";
	private final static String DATE_TOURNEE_5 = "2016-12-22 14:00:00";
	private final static String ID_TOURNEE_1 = "44M0003122016000000";
	private final static String ID_TOURNEE_2 = "44M0103122016102852";
	private final static String ID_TOURNEE_3 = "44M0103122016082910";
	private final static String ID_TOURNEE_4 = "44M0803122016075322";
	private final static String ID_TOURNEE_5 = "44M6203122016070530";
	private final static String TOURNEE_POINT_ID = "point1";
	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
	}

	@BeforeClass
	public void setUpBeforeClass() throws Exception {
		if (!BuildCluster.clusterHasBuilt) {
			BuildCluster.setUpBeforeSuite();
			suiteLaunch = false;
		}
		CCMBridge.ipOfNode(1);

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

		dao = GetC11DaoImpl.INSTANCE;
	}

	/**
	 * Teste la méthode getIdTourneesByAgenceAndJour.
	 * Insére 5 idx tournées. 3 sur l'agence et le jour recherché.
	 * 1 sur un mauvais jour. 1 sur une mauvaise agence.
	 * Vérifie qu'on retourne bien les ids des tournées sur le bon jour et la bonnne agence
	 * @throws Exception 
	 */
	@Test
	public void test_getIdTournees() throws Exception {
		// Insére des idx
		getSession().execute(psInsertIdxAgenceJour.bind(ID_TOURNEE_1, AGENCE, JOUR, CODE_TOURNEE_1, sdf.parse(DATE_TOURNEE_1)));
		getSession().execute(psInsertIdxAgenceJour.bind(ID_TOURNEE_2, AGENCE, JOUR, CODE_TOURNEE_2, sdf.parse(DATE_TOURNEE_2)));
		getSession().execute(psInsertIdxAgenceJour.bind(ID_TOURNEE_3, AGENCE, JOUR, CODE_TOURNEE_3, sdf.parse(DATE_TOURNEE_3)));
		getSession().execute(psInsertIdxAgenceJour.bind(ID_TOURNEE_4, AGENCE, JOUR_INCORRECT, CODE_TOURNEE_4, sdf.parse(DATE_TOURNEE_4)));
		getSession().execute(psInsertIdxAgenceJour.bind(ID_TOURNEE_5, AGENCE_INCORRECTE, JOUR, CODE_TOURNEE_5, sdf.parse(DATE_TOURNEE_5)));
		
		// Get tournées idx sur l'agence et le jour
		final List<String> idTournees = dao.getIdxTourneesByAgenceAndJour(AGENCE, JOUR);
		
		// Vérifie les ids retournés
		assertTrue(idTournees.size() == 3);
		assertEquals(idTournees.get(0), ID_TOURNEE_1);
		assertEquals(idTournees.get(1), ID_TOURNEE_2);
		assertEquals(idTournees.get(2), ID_TOURNEE_3);
	}

	/**
	 * Teste la méthode getTourneeById.
	 * Insére 1 tournée. Vérifie que l'objet TourneeC11 retourné est bien rempli
	 * @throws Exception 
	 */
	@Test
	public void test_getTourneeById() throws Exception {
		// Insére une tournée
		final Set<String> idsPoints = new HashSet<String>(Arrays.asList("point1", "point2"));
		getSession().execute(psInsertTournee.bind("TPEC", "DurPre", "DistPre", "DurPau", "IdPoiStt", idsPoints, "debut_prevu", ID_TOURNEE_1));
		
		// Get tournee
		final List<TourneeC11> tourneesC11 = dao.getTourneesById(Arrays.asList(ID_TOURNEE_1));
		final TourneeC11 tourneeC11 = tourneesC11.get(0);
		
		// Vérifie la tournée retournée
		assertEquals(tourneeC11.geTourneeVision().getPriseEnCharge(), "TPEC");
		assertEquals(tourneeC11.geTourneeVision().getDureePrevue(), "DurPre");
		assertEquals(tourneeC11.geTourneeVision().getDistancePrevue(), "DistPre");
		assertEquals(tourneeC11.geTourneeVision().getDureePause(), "DurPau");
		assertEquals(tourneeC11.geTourneeVision().getIdPoiSousTraitant(), "IdPoiStt");
		assertEquals(tourneeC11.geTourneeVision().getIdsPoint(), idsPoints);
		assertEquals(tourneeC11.geTourneeVision().getIdC11(), ID_TOURNEE_1);
		assertEquals(tourneeC11.geTourneeVision().getDebutPrevu(), "debut_prevu");
	}

	/**
	 * Teste la méthode getPointC11ById.
	 * Insére 1 pointC11. Vérifie que l'objet PointC11 retourné est bien rempli
	 * @throws Exception 
	 */
	@Test
	public void test_getPointC11ById() throws Exception {
		getSession().execute(psInsertTourneePoint.bind("num1", "12:00", "10:30", "11:30", "type1", "nom1", "raison1", "produit1", "duree1",
				"idAd1", "idDest1", "spoi1", "10", "GP1", TOURNEE_POINT_ID));
		
		// Get point tournées
		final List<PointC11> pointsC11 = dao.getPointsForTourneeId(Arrays.asList(TOURNEE_POINT_ID));
		final PointC11 pointC11 = pointsC11.get(0);
		
		// Vérifie le point tournée
		assertEquals(pointC11.getNumeroPoint(), "num1");
		assertEquals(pointC11.getContrainteHoraire(), "12:00");
		assertEquals(pointC11.getHeureDebutRDV(), "10:30");
		assertEquals(pointC11.getHeureFinRDV(), "11:30");
		assertEquals(pointC11.getDestType(), "type1");
		assertEquals(pointC11.getDestNom1(), "nom1");
		assertEquals(pointC11.getDestRaisonSociale1(), "raison1");
		assertEquals(pointC11.getLibelleProduitPoint(), "produit1");
		assertEquals(pointC11.getTempsStopService(), "duree1");
		assertEquals(pointC11.getIdAdresse(), "idAd1");
		assertEquals(pointC11.getIdDest(), "idDest1");
		assertEquals(pointC11.getIsSPOI(), "spoi1");
		assertEquals(pointC11.getNbOjets(), "10");
		assertEquals(pointC11.getGammeProduit(), "GP1");
	}

	@AfterClass
	public void tearDownAfterClass() throws Exception {
		getSession().execute(psDeleteIdxAgenceJour.bind(AGENCE, JOUR, CODE_TOURNEE_1, sdf.parse(DATE_TOURNEE_1)));
		getSession().execute(psDeleteIdxAgenceJour.bind(AGENCE, JOUR, CODE_TOURNEE_2, sdf.parse(DATE_TOURNEE_2)));
		getSession().execute(psDeleteIdxAgenceJour.bind(AGENCE, JOUR, CODE_TOURNEE_3, sdf.parse(DATE_TOURNEE_3)));
		getSession().execute(psDeleteIdxAgenceJour.bind(AGENCE, JOUR_INCORRECT, CODE_TOURNEE_4, sdf.parse(DATE_TOURNEE_4)));
		getSession().execute(psDeleteIdxAgenceJour.bind(AGENCE_INCORRECTE, JOUR, CODE_TOURNEE_5, sdf.parse(DATE_TOURNEE_5)));
		getSession().execute(psDeleteTournee.bind(ID_TOURNEE_1));
		getSession().execute(psDeleteTourneePoint.bind(TOURNEE_POINT_ID));
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
	}
}
