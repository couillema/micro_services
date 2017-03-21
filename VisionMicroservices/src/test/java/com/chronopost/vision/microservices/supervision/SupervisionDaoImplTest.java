package com.chronopost.vision.microservices.supervision;

import static org.testng.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

public class SupervisionDaoImplTest {

	private ISupervisionDao dao;
	private boolean suiteLaunch = true;

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
		
		getSession().execute(QueryBuilder.truncate(ETableLtCounters.TABLE_NAME));
		getSession().execute(QueryBuilder.truncate(ETableEvtCounters.TABLE_NAME));

		dao = SupervisionDaoImpl.INSTANCE;
		// Insére pour le même jour quatre lignes pour trois horaires différents
		getSession().execute("update lt_counters set lt_in_insertlt = lt_in_insertlt+1, lt_out_insertlt = lt_out_insertlt+2,"
				+ " hit_insertlt = hit_insertlt+3 where jour = '20161027' and heure = '16' and minute = '1'");
		getSession().execute("update lt_counters set lt_in_insertlt = lt_in_insertlt+4, lt_out_insertlt = lt_out_insertlt+5,"
				+ " hit_insertlt = hit_insertlt+6 where jour = '20161027' and heure = '19' and minute = '3'");
		getSession().execute("update evt_counters set evt_diffuses = evt_diffuses+9, hit_evt_diffuses = hit_evt_diffuses+5,"
				+ " evt_in_insertevt = evt_in_insertevt+6, evt_out_insertevt = evt_out_insertevt+7, hit_insertevt = hit_insertevt+8,"
				+ " retards_cumules_hit = retards_cumules_hit+9, evt_suivi_box = evt_suivi_box+10 where jour = '20161027' and heure = '16' and minute = '1'");
		getSession().execute("update evt_counters set evt_diffuses = evt_diffuses+19, hit_evt_diffuses = hit_evt_diffuses+15,"
				+ " evt_in_insertevt = evt_in_insertevt+16, evt_out_insertevt = evt_out_insertevt+17, hit_insertevt = hit_insertevt+18,"
				+ " retards_cumules_hit = retards_cumules_hit+19, evt_suivi_box = evt_suivi_box+110 where jour = '20161027' and heure = '18' and minute = '2'");
	}

	/**
	 * Récupére la liste de SnapShotVision pour une même journée
	 * Entrée : 4 lignes en base, pour 3 horaires différents, 1 horaire contenant des Evt et Lt
	 * Attendu : Une liste à 3 objets, triée chronologiquement
	 */
	@Test
	public void test_getSnapShotVisionForDay() throws Exception {
		// GIVEN
		SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd HH:mm");
		Date date = SDF.parse("20161027 16:12");
		// WEN
		List<SnapShotVision> snapShotVisions = dao.getSnapShotVisionForDay(date, false);
		// THEN
		// liste à 3 éléments car 3 horaires différents en base pour la même journée
		assertEquals(snapShotVisions.size(), 3);
		assertEquals(snapShotVisions.get(0).getAskLt().longValue(), 1);
		assertEquals(snapShotVisions.get(0).getInsertLt().longValue(), 2);
		assertEquals(snapShotVisions.get(0).getHitLt().longValue(), 3);
		assertEquals(snapShotVisions.get(0).getAskEvt().longValue(), 6);
		assertEquals(snapShotVisions.get(0).getInsertEvt().longValue(), 7);
		assertEquals(snapShotVisions.get(0).getHitEvt().longValue(), 8);
		assertEquals(snapShotVisions.get(0).getDiffEvt().longValue(), 9);
		assertEquals(snapShotVisions.get(0).getHitDiffEvt().longValue(), 5);
		assertEquals(snapShotVisions.get(0).getJour(), "20161027");
		assertEquals(snapShotVisions.get(0).getHeure(), "16");
		assertEquals(snapShotVisions.get(0).getMinute(), "1");
		
		assertEquals(snapShotVisions.get(1).getAskLt().longValue(), 0);
		assertEquals(snapShotVisions.get(1).getInsertLt().longValue(), 0);
		assertEquals(snapShotVisions.get(1).getHitLt().longValue(), 0);
		assertEquals(snapShotVisions.get(1).getAskEvt().longValue(), 16);
		assertEquals(snapShotVisions.get(1).getInsertEvt().longValue(), 17);
		assertEquals(snapShotVisions.get(1).getHitEvt().longValue(), 18);
		assertEquals(snapShotVisions.get(1).getDiffEvt().longValue(), 19);
		assertEquals(snapShotVisions.get(1).getHitDiffEvt().longValue(), 15);
		assertEquals(snapShotVisions.get(1).getJour(), "20161027");
		assertEquals(snapShotVisions.get(1).getHeure(), "18");
		assertEquals(snapShotVisions.get(1).getMinute(), "2");
		
		assertEquals(snapShotVisions.get(2).getAskLt().longValue(), 4);
		assertEquals(snapShotVisions.get(2).getInsertLt().longValue(), 5);
		assertEquals(snapShotVisions.get(2).getHitLt().longValue(), 6);
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
	 * Récupére l'objet SnapShotVision pour la dizaine de minute le 27/10/2016 de 1h10 à 1h19
	 * Entrée : 4 lignes en base, pour 3 horaires différents, 1 horaire contenant des Evt et Lt
	 * Attendu : Récupére l'horaire contenant des Evt et Lt
	 */
	@Test
	public void test_getSnapShotFor10Minutes() throws Exception {
		// WEN
		SnapShotVision snapShotVision = dao.getSnapShotByKey("20161027", "16", "1");
		// THEN
		assertEquals(snapShotVision.getAskLt().longValue(), 1);
		assertEquals(snapShotVision.getInsertLt().longValue(), 2);
		assertEquals(snapShotVision.getHitLt().longValue(), 3);
		assertEquals(snapShotVision.getAskEvt().longValue(), 6);
		assertEquals(snapShotVision.getInsertEvt().longValue(), 7);
		assertEquals(snapShotVision.getHitEvt().longValue(), 8);
		assertEquals(snapShotVision.getDiffEvt().longValue(), 9);
		assertEquals(snapShotVision.getHitDiffEvt().longValue(), 5);
		assertEquals(snapShotVision.getJour(), "20161027");
		assertEquals(snapShotVision.getHeure(), "16");
		assertEquals(snapShotVision.getMinute(), "1");
	}

	@AfterClass
	public void tearDownAfterClass() throws Exception {
		getSession().execute(QueryBuilder.truncate(ETableLtCounters.TABLE_NAME));
		getSession().execute(QueryBuilder.truncate(ETableEvtCounters.TABLE_NAME));
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
	}
}
