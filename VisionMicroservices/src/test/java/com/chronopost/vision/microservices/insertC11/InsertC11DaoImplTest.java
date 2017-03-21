package com.chronopost.vision.microservices.insertC11;

import static com.chronopost.cassandra.request.builder.CassandraClauseBuilder.buildInClause;
import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildDelete;
import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildSelect;
import static org.testng.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.cassandra.table.ETableIdxTourneeJour;
import com.chronopost.cassandra.table.ETableTournee;
import com.chronopost.cassandra.table.ETableTourneePoint;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.insertC11.PointC11;
import com.chronopost.vision.model.insertC11.PointC11Liste;
import com.chronopost.vision.model.insertC11.TourneeC11;
import com.chronopost.vision.model.insertC11.TourneeVision;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class InsertC11DaoImplTest {

	private IInsertC11Dao dao;
	private boolean suiteLaunch = true;
	
	private PreparedStatement getTourneeFromId;
	private PreparedStatement getIdxTourneeFromId;
	private PreparedStatement getPoints;
	private PreparedStatement deleteTourneeC11;
	private PreparedStatement deleteIdxTourneeAgence;
	private PreparedStatement deletePoints;

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
		
		getTourneeFromId = getSession().prepare(buildSelect(ETableTournee.TABLE_NAME).getQuery());
		getIdxTourneeFromId = getSession().prepare(buildSelect(ETableIdxTourneeJour.TABLE_NAME, ETableIdxTourneeJour.AGENCE, ETableIdxTourneeJour.JOUR)
				.getQuery());
		getPoints = getSession().prepare(buildSelect(ETableTourneePoint.TABLE_NAME,
				buildInClause(ETableTourneePoint.ID_POINT)).getQuery());
		
		deleteTourneeC11 = getSession().prepare(buildDelete(ETableTournee.TABLE_NAME));
		deleteIdxTourneeAgence = getSession().prepare(buildDelete(ETableIdxTourneeJour.TABLE_NAME,
				Arrays.asList(ETableIdxTourneeJour.AGENCE, ETableIdxTourneeJour.JOUR)));
		deletePoints = getSession().prepare(buildDelete(ETableTourneePoint.TABLE_NAME, buildInClause(ETableTourneePoint.ID_POINT)));
		
		dao = InsertC11DaoImpl.INSTANCE;
	}

	/**
	 * Test miseAJourTournee DAO method to update tournee table with TourneeC11
	 * object
	 * IdC11 on 22 char
	 */
	@Test
	public void test_miseAJourTournee_idC11_22char() {
		// GIVEN
		final TourneeC11 tourneeC11 = new TourneeC11();
		TourneeVision tourneeVision = new TourneeVision();
		tourneeVision.setIdC11("Id_22char_1_1234567890");
		tourneeVision.setPriseEnCharge("PEC");
		tourneeVision.setDebutPrevu("DEB_P");
		tourneeVision.setDureePrevue("DUR_PREVUE");
		tourneeVision.setDistancePrevue("DIS_P");
		tourneeVision.setDureePause("DUR_PAUSE");
		tourneeVision.setIdPoiSousTraitant("ID_POIST");
		tourneeC11.setTourneeVision(tourneeVision);
		// WEN
		dao.miseAJourTournee(tourneeC11);
		// THEN
		ResultSet resultSet = getSession().execute(getTourneeFromId.bind("Id_22char_1_1234567890"));
		Row row = resultSet.one();
		assertEquals(row.getString("id_tournee"), "Id_22char_1_1234567890");
		assertEquals(row.getString("type_prise_en_charge"), "PEC");
		assertEquals(row.getString("debut_prevu"), "DEB_P");
		assertEquals(row.getString("duree_prevue"), "DUR_PREVUE");
		assertEquals(row.getString("distance_prevue"), "DIS_P");
		assertEquals(row.getString("duree_pause"), "DUR_PAUSE");
		assertEquals(row.getString("idpoistt"), "ID_POIST");
	}

	/**
	 * Test miseAJourTournee DAO method to update tournee table with TourneeC11
	 * object
	 * IdC11 on 19 char
	 */
	@Test
	public void test_miseAJourTournee_idC11_19char() {
		// GIVEN
		TourneeC11 tourneeC11 = new TourneeC11();
		TourneeVision tourneeVision = new TourneeVision();
		tourneeVision.setIdC11("XREId_19char_1_1234567");
		tourneeVision.setPriseEnCharge("PEC");
		tourneeVision.setDebutPrevu("DEB_P");
		tourneeVision.setDureePrevue("DUR_PREVUE");
		tourneeVision.setDistancePrevue("DIS_P");
		tourneeVision.setDureePause("DUR_PAUSE");
		tourneeVision.setIdPoiSousTraitant("ID_POIST");
		tourneeVision.setTrigramme("XRE");
		tourneeC11.setTourneeVision(tourneeVision);
		// WEN
		dao.miseAJourTournee(tourneeC11);
		// THEN
		ResultSet resultSet = getSession().execute(getTourneeFromId.bind("XREId_19char_1_1234567"));
		Row row = resultSet.one();
		assertEquals(row.getString("id_tournee"), "XREId_19char_1_1234567");
		assertEquals(row.getString("type_prise_en_charge"), "PEC");
		assertEquals(row.getString("debut_prevu"), "DEB_P");
		assertEquals(row.getString("duree_prevue"), "DUR_PREVUE");
		assertEquals(row.getString("distance_prevue"), "DIS_P");
		assertEquals(row.getString("duree_pause"), "DUR_PAUSE");
		assertEquals(row.getString("idpoistt"), "ID_POIST");
	}

	@Test
	public void test_miseAJourIdxTourneeJour_idC11_22char() throws ParseException {
		// GIVEN
		TourneeC11 tourneeC11 = new TourneeC11();
		TourneeVision tourneeVision = new TourneeVision();
		tourneeVision.setIdC11("Id_22char_2_1234567890");
		tourneeVision.setIdSite("ID_SITE");
		tourneeVision.setDateTournee("24/10/2016");
		tourneeVision.setCodeTourne("CODE_TOURNEE");
		tourneeC11.setTourneeVision(tourneeVision);
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
		// WEN
		dao.miseAJourIdxTourneeJour(tourneeC11);
		// THEN
		ResultSet resultSet = getSession().execute(getIdxTourneeFromId.bind("ID_SITE", "20162410"));
		Row row = resultSet.one();
		assertEquals(row.getString("id_tournee"), "Id_22char_2_1234567890");
		assertEquals(DATE_FORMAT.format(row.getTimestamp("date_tournee")), "24/10/2016");
		assertEquals(row.getTimestamp("date_tournee"), DATE_FORMAT.parse("24/10/2016"));
		assertEquals(row.getString("code_tournee"), "CODE_TOURNEE");
		assertEquals(row.getString("jour"), "20162410");
		assertEquals(row.getString("agence"), "ID_SITE");
	}

	@Test
	public void test_miseAJourIdxTourneeJour_idC11_19char() throws ParseException {
		// GIVEN
		TourneeC11 tourneeC11 = new TourneeC11();
		TourneeVision tourneeVision = new TourneeVision();
		tourneeVision.setIdC11("XREId_19char_2_1234567");
		tourneeVision.setIdSite("ID_SITE");
		tourneeVision.setDateTournee("24/10/2016");
		tourneeVision.setCodeTourne("CODE_TOURNEE");
		tourneeVision.setTrigramme("XRE");
		tourneeC11.setTourneeVision(tourneeVision);
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
		// WEN
		dao.miseAJourIdxTourneeJour(tourneeC11);
		// THEN
		ResultSet resultSet = getSession().execute(getIdxTourneeFromId.bind("ID_SITE", "20162410"));
		Row row = resultSet.one();
		assertEquals(row.getString("id_tournee"), "XREId_19char_2_1234567");
		assertEquals(DATE_FORMAT.format(row.getTimestamp("date_tournee")), "24/10/2016");
		assertEquals(row.getTimestamp("date_tournee"), DATE_FORMAT.parse("24/10/2016"));
		assertEquals(row.getString("code_tournee"), "CODE_TOURNEE");
		assertEquals(row.getString("jour"), "20162410");
		assertEquals(row.getString("agence"), "ID_SITE");
	}

	@Test
	public void test_miseAJourPoints() {
		// GIVEN
		TourneeC11 tourneeC11 = new TourneeC11();
		TourneeVision tourneeVision = new TourneeVision();
		PointC11Liste pointC11Liste = new PointC11Liste();
		PointC11 pointC11_1 = new PointC11();
		tourneeVision.setCodeTourne("CODE_TOURNEE");
		tourneeVision.setIdC11("IDC11_TOURNEEEEEEEEEEE");
		tourneeVision.setDateTournee("31/01/2017");
		pointC11_1.setNumeroPoint("NUM_POINT_1");
		pointC11_1.setContrainteHoraire("CH_1");
		pointC11_1.setHeureDebutRDV("HDRDV_1");
		pointC11_1.setHeureFinRDV("HFRDV_1");
		pointC11_1.setDestType("DTYPE_1");
		pointC11_1.setDestNom1("DNOM_1");
		pointC11_1.setDestRaisonSociale1("DRAISOC_1");
		pointC11_1.setLibelleProduitPoint("LIB_PD_1");
		pointC11_1.setTempsStopService("TSS_1");
		pointC11_1.setIdAdresse("ID_AD_1");
		pointC11_1.setIdDest("ID_DEST_1");
		pointC11_1.setIsSPOI("true");
		pointC11_1.setNbOjets("4");
		pointC11_1.setIdPtC11("ID_POINT_1");
		PointC11 pointC11_2 = new PointC11();
		pointC11_2.setNumeroPoint("NUM_POINT_2");
		pointC11_2.setContrainteHoraire("CH_2");
		pointC11_2.setHeureDebutRDV("HDRDV_2");
		pointC11_2.setHeureFinRDV("HFRDV_2");
		pointC11_2.setDestType("DTYPE_2");
		pointC11_2.setDestNom1("DNOM_2");
		pointC11_2.setDestRaisonSociale1("DRAISOC_2");
		pointC11_2.setLibelleProduitPoint("LIB_PD_2");
		pointC11_2.setTempsStopService("TSS_2");
		pointC11_2.setIdAdresse("ID_AD_2");
		pointC11_2.setIdDest("ID_DEST_2");
		pointC11_2.setIsSPOI("false");
		pointC11_2.setNbOjets("7");
		pointC11_2.setIdPtC11("ID_POINT_2");
		pointC11Liste.addPointC11(pointC11_1);
		pointC11Liste.addPointC11(pointC11_2);
		tourneeVision.setPointC11Liste(pointC11Liste);
		tourneeC11.setTourneeVision(tourneeVision);
		// WEN
		dao.miseAJourPoints(tourneeC11);
		// THEN
		ResultSet resultSet = getSession().execute(getPoints.bind(Arrays.asList("ID_POINT_1", "ID_POINT_2")));
		Row row = resultSet.one();
		assertEquals(row.getString("id_point"), "ID_POINT_1");
		assertEquals(row.getString("heure_contractuelle"), "CH_1");
		assertEquals(row.getString("debrdv"), "HDRDV_1");
		assertEquals(row.getString("finrdv"), "HFRDV_1");
		assertEquals(row.getString("type_destination"), "DTYPE_1");
		assertEquals(row.getString("nom_destination"), "DNOM_1");
		assertEquals(row.getString("raison_sociale_destination"), "DRAISOC_1");
		assertEquals(row.getString("produit_principal"), "LIB_PD_1");
		assertEquals(row.getString("duree_stop"), "TSS_1");
		assertEquals(row.getString("id_adresse_destination"), "ID_AD_1");
		assertEquals(row.getString("id_destination"), "ID_DEST_1");
		assertEquals(row.getString("is_spoi"), "true");
		assertEquals(row.getString("nb_colis_prevus"), "4");
		row = resultSet.one();
		assertEquals(row.getString("id_point"), "ID_POINT_2");
		assertEquals(row.getString("heure_contractuelle"), "CH_2");
		assertEquals(row.getString("debrdv"), "HDRDV_2");
		assertEquals(row.getString("finrdv"), "HFRDV_2");
		assertEquals(row.getString("type_destination"), "DTYPE_2");
		assertEquals(row.getString("nom_destination"), "DNOM_2");
		assertEquals(row.getString("raison_sociale_destination"), "DRAISOC_2");
		assertEquals(row.getString("produit_principal"), "LIB_PD_2");
		assertEquals(row.getString("duree_stop"), "TSS_2");
		assertEquals(row.getString("id_adresse_destination"), "ID_AD_2");
		assertEquals(row.getString("id_destination"), "ID_DEST_2");
		assertEquals(row.getString("is_spoi"), "false");
		assertEquals(row.getString("nb_colis_prevus"), "7");
	}

	@AfterClass
	public void tearDownAfterClass() throws Exception {
		getSession().execute(deleteTourneeC11.bind("Id_22char_1_1234567890"));
		getSession().execute(deleteTourneeC11.bind("Id_22char_2_1234567890"));
		getSession().execute(deleteTourneeC11.bind("XREId_19char_1_1234567"));
		getSession().execute(deleteTourneeC11.bind("XREId_19char_2_1234567"));
		getSession().execute(deleteIdxTourneeAgence.bind("ID_SITE", "20162410"));
		getSession().execute(deletePoints.bind(Arrays.asList("ID_POINT_1", "ID_POINT_2")));
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
	}
}
