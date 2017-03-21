package com.chronopost.vision.microservices.insertpointtournee.v1;

import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildSelect;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.cassandra.table.ETableEvt;
import com.chronopost.cassandra.table.ETableIdxTourneeJour;
import com.chronopost.cassandra.table.ETableTournee;
import com.chronopost.cassandra.table.ETableTourneePoint;
import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.CacheManagerService;
import com.chronopost.vision.cachemanager.codeservice.CodeServiceDaoImpl;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.insertevt.v1.InsertEvtDaoImpl;
import com.chronopost.vision.microservices.tournee.getdetailtournee.v1.Tournee;
import com.chronopost.vision.model.CodeService;
import com.chronopost.vision.model.EInfoComp;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.rules.EvtRules;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.UDTValue;
import com.google.common.collect.Lists;

/** @author unknown JJC add getSession() and reformat a few lines. **/
public class InsertPointTourneeDaoImplTest {
    private boolean suiteLaunch = true;

    /** PreparedStatement pour récupérer un point */
    private PreparedStatement getOnePoint;

    /** PreparedStatement pour récupérer un point */
    private PreparedStatement getOneTournee;

    /** PreparedStatement pour récupérer une tournee depuis l'index */
    private PreparedStatement getOneIdxTournee;

    /**
     * @return VisionMicroserviceApplication.cassandraSession (a
     *         com.datastax.driver.core )
     */
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

        if (!CacheManagerService.INSTANCE.containsProjet("service")) {
            CodeServiceDaoImpl.INSTANCE.setSession(VisionMicroserviceApplication.getCassandraSession());
            CacheManagerService.INSTANCE.addProjet("service", new CacheManager<CodeService>().setDao(CodeServiceDaoImpl.INSTANCE));
            CacheManagerService.INSTANCE.startUpdater();
        }

        InsertPointTourneeDaoImpl.INSTANCE.setRefentielCodeService((CacheManager<CodeService>) CacheManagerService.INSTANCE.getCacheManager("service", CodeService.class));

        cleanDB();
        
		/* PrepareStatement pour les test */
        getOnePoint = getSession().prepare(buildSelect(ETableTourneePoint.TABLE_NAME, ETableTourneePoint.ID_POINT).getQuery());
		
		getOneTournee = getSession().prepare("SELECT * FROM tournee WHERE "
				+ ETableTournee.ID_TOURNEE.getNomColonne() + " = ?");
		
		getOneIdxTournee = getSession().prepare("SELECT " + ETableIdxTourneeJour.ID_TOURNEE.getNomColonne() + " FROM "
				+ ETableIdxTourneeJour.TABLE_NAME + " WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne()
				+ " = ? AND " + ETableIdxTourneeJour.JOUR.getNomColonne() + " = ?");
    }

	private void cleanDB() {
		getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200101011970101010'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200102011970101210'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200103011970101410'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200104011970101610'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200204011970101610'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200105011970101810'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200106011970102010'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200206011970102010'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200206011970112010'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200103011970101411'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200103011970101433'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'RNS35T5204213062016075200'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200602011972103010'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200502011972103010'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200301011971102010'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200202011971103010'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200102011972103010'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200102011971103010'");
        getSession().execute("DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = 'CGY12M1200101011971102010'");

        getSession().execute("DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1201011970101010'");
        getSession().execute("DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1202011970101210'");
        getSession().execute("DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1203011970101410'");
        getSession().execute("DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1204011970101610'");
        getSession().execute("DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1205011970101810'");
        getSession().execute("DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1206011970102010'");
        getSession().execute("DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1207011970102210'");
        getSession().execute("DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1206011970112010'");
        getSession().execute("DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1203011970101433'");
        getSession().execute("DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '35T5213062016075200'");
        getSession().execute("DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1203011970101411'");
        getSession().execute("DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1202011971103010'");
        getSession().execute("DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1202011972103010'");
        getSession().execute("DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1201011971102010'");

        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne() + " = '00000' AND "
                        + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '19700101'");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne() + " = '00000' AND "
                        + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '19700102'");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne() + " = '00000' AND "
                        + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '19700103'");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne() + " = '00000' AND "
                        + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '19700104'");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne() + " = '00000' AND "
                        + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '19700105'");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne() + " = '00000' AND "
                        + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '19700106'");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne() + " = '00000' AND "
                        + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '19700107'");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne() + " = '00000' AND "
                        + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '19700107'");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne() + " = 'PCO' AND "
                        + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '20160518' AND " + ETableIdxTourneeJour.CODE_TOURNEE.getNomColonne() + "='75P14'");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne() + " = '00000' AND "
                        + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '20160613' AND " + ETableIdxTourneeJour.CODE_TOURNEE.getNomColonne() + "='35T52'");

        getSession().execute(
                "DELETE FROM " + ETableEvt.TABLE_NAME + " WHERE " + ETableEvt.NO_LT.getNomColonne() + "='MM912357956FR' AND "
                        + ETableEvt.PRIORITE_EVT + "=1000 AND " + ETableEvt.DATE_EVT + "='2016-05-18 06:41:00'");
        getSession().execute(
                "DELETE FROM " + ETableEvt.TABLE_NAME + " WHERE " + ETableEvt.NO_LT.getNomColonne() + "='MM912357957FR' AND "
                        + ETableEvt.PRIORITE_EVT + "=1000 AND " + ETableEvt.DATE_EVT + "='2016-05-18 06:41:00'");
        getSession().execute(
                "DELETE FROM " + ETableEvt.TABLE_NAME + " WHERE " + ETableEvt.NO_LT.getNomColonne() + "='MM912357958FR' AND "
                        + ETableEvt.PRIORITE_EVT + "=1000 AND " + ETableEvt.DATE_EVT + "='2016-05-18 06:41:00'");
        getSession().execute(
                "DELETE FROM " + ETableEvt.TABLE_NAME + " WHERE " + ETableEvt.NO_LT.getNomColonne() + "='MM912357958FR' AND "
                        + ETableEvt.PRIORITE_EVT + "=1000 AND " + ETableEvt.DATE_EVT + "='2016-05-18 06:42:00'");
	}

    @Test
    /**
     * Test simple : un evt TA sur un point C11
     * 
     * Attendu : un enr. dans la table pour ce point, avec l'evt TA
     */
    public void AddEvtTest1() {

        /* Traitement d'un evt TA avec un numéro d'id point */
        List<Evt> evts = new ArrayList<>();
        evts.add(newEvt("no_lt", "TA", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200101011970101010", addInfoComp(EInfoComp.ETA, "9:09", null)), null));

        /* Execution */
        InsertPointTourneeDaoImpl.INSTANCE.addEvtDansPoint(evts);

        /* Vérification en base */

        /* récupération du point */
        Row row = getPointEnBase("CGY12M1200101011970101010");
        assertNotNull(row);
        /* vérif qu'il contient bien 1 evt de type TA */
        Set<UDTValue> setEvt = getEvtPointByType(row, "TA");
        assertEquals(setEvt.size(), 1);
    }

    @Test
    /**
     * Test double evt : un evt TA et un evt D sur un point C11 pour le même no_lt
     * 
     * Attendu : un enr. dans la table pour ce point, avec les evt TA et D présents
     */
    public void AddEvtTest2() {

        /* Traitement de 2 evts TA et D avec le même numéro d'id point */
        List<Evt> evts = new ArrayList<>();
        evts.add(newEvt("no_lt", "TA", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200102011970101210", null), null));
        evts.add(newEvt("no_lt", "D", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200102011970101210", null), null));
        InsertPointTourneeDaoImpl.INSTANCE.addEvtDansPoint(evts);

        /* Vérification en base */
        Row row = getPointEnBase("CGY12M1200102011970101210");
        assertNotNull(row);

        Set<UDTValue> setEvtTA = getEvtPointByType(row, "TA");
        Set<UDTValue> setEvtD = getEvtPointByType(row, "D");
        Set<UDTValue> setEvtAll = getEvtPoint(row);

        assertEquals(setEvtTA.size(), 1);
        assertEquals(setEvtD.size(), 1);
        assertEquals(setEvtAll.size(), 2);
    }

    @Test
    /**
     * Test simple evt : un evt D sur un point C11 avec type et nom de destinataire indiqués
     * 
     * Attendu : un enr. dans la table pour ce point, avec l'evt D présents et les valeurs type et nom destinataires remplis
     */
	public void AddEvtTest3() {
		/*
		 * Traitement d'un evt D avec un numéro d'id point et un type et nom
		 * destinataire
		 */
		List<Evt> evts = new ArrayList<>();
		evts.add(newEvt("no_lt", "D",
				addInfoComp(EInfoComp.TYPE_DESTINATAIRE, "P",
						addInfoComp(EInfoComp.NOM_DESTINATAIRE, "Tartanpion",
								addInfoComp(EInfoComp.IDENTIFIANT_POINT_RELAIS, "21T34",
										addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200103011970101410", null)))),
				null));
		InsertPointTourneeDaoImpl.INSTANCE.addEvtDansPoint(evts);

		/* Vérification en base */
		Row row = getPointEnBase("CGY12M1200103011970101410");
		assertNotNull(row);

		Set<UDTValue> setEvtD = getEvtPointByType(row, "D");
		assertEquals(setEvtD.size(), 1);
	}

    /**
     * Test trois evt : un evt TA et un evt D sur 2 points C11 pour trois no_lt
     * differents
     * 
     * Attendu : un enr. dans la table pour ce point, avec les evt TA et D
     * présents et un enr. dans la table tournee avec les 2 lt et 1 point.
     */
    @Test
    public void AddEvtTest4() {

        /* Traitement de 2 evts TA et D avec le même numéro d'id point */
        List<Evt> evts = new ArrayList<>();
        evts.add(newEvt("no_lt1", "TA", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200104011970101610", null), null));
        evts.add(newEvt("no_lt2", "D", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200104011970101610", null), null));
        evts.add(newEvt("no_lt3", "D", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200204011970101610", null), null));
        InsertPointTourneeDaoImpl.INSTANCE.addEvtDansPoint(evts);

        /* Vérification en base */
        Row row = getPointEnBase("CGY12M1200104011970101610");
        assertNotNull(row);

        Set<UDTValue> setEvtTA = getEvtPointByType(row, "TA");
        Set<UDTValue> setEvtD = getEvtPointByType(row, "D");
        Set<UDTValue> setEvtAll = getEvtPoint(row);

        row = getTourneeEnBase("12M1204011970101610");
        Set<String> setPoints = getPoints(row);
        Set<String> setColis = getColis(row);

        assertEquals(setEvtTA.size(), 1);
        assertEquals(setEvtD.size(), 1);
        assertEquals(setEvtAll.size(), 2);
        assertEquals(setPoints.size(), 2);
        assertEquals(setColis.size(), 3);
    }

    @Test
    /**
     * Test simple : un evt D sur un point C11 avec un diff_eta et un diff_gps
     * 
     * Attendu : un enr. dans la table pour ce point, avec l'evt D et le diff_eta et diff_gps
     */
    public void AddEvtTest5() {

        /* Traitement d'un evt TA avec un numéro d'id point */
        List<Evt> evts = new ArrayList<>();
        evts.add(newEvt(
                "no_lt",
                "D",
                addInfoComp(EInfoComp.DIFF_GPS, "30",
                        addInfoComp(EInfoComp.DIFF_ETA, "12", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200105011970101810", null))), null));
        InsertPointTourneeDaoImpl.INSTANCE.addEvtDansPoint(evts);

        /* Vérification en base */
        Row row = getPointEnBase("CGY12M1200105011970101810");
        assertNotNull(row);

        Set<UDTValue> setEvt = getEvtPointByType(row, "D");
        assertEquals(setEvt.size(), 1);

        UDTValue evt = setEvt.iterator().next();
        assertEquals (evt.getString("diff_eta"), "12");
        assertEquals (evt.getString("diff_gps"), "30");
    }

    @Test
    /**
     * Test simple : un evt D sur un point C11 avec un diff_eta et un diff_gps mais qui ne sont pas des entiers
     * 
     * Attendu : un enr. dans la table pour ce point, avec l'evt D mais les diff_eta et diff_gps non positionné (=null)
     */
    public void AddEvtTest6() {

        /* Traitement d'un evt TA avec un numéro d'id point */
        List<Evt> evts = new ArrayList<>();
        evts.add(newEvt(
                "no_lt",
                "D",
                addInfoComp(EInfoComp.DIFF_GPS, "30m",
                        addInfoComp(EInfoComp.DIFF_ETA, "12min", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200106011970102010", null))), null));
        InsertPointTourneeDaoImpl.INSTANCE.addEvtDansPoint(evts);

        /* Vérification en base */
        Row row = getPointEnBase("CGY12M1200106011970102010");
        assertNotNull(row);

        Set<UDTValue> setEvt = getEvtPointByType(row, "D");
        assertEquals(setEvt.size(), 1);

        UDTValue evt = setEvt.iterator().next();
        assertNull(evt.getString("diff_eta"));
        assertNull(evt.getString("diff_gps"));
    }

    @Test
    /**
     * Test 2 evt : un evt D fictif de début de tournée, et un evt D fictif de fin de tournée
     * 
     * Attendu : un enr. dans la table tournée, avec debut et fin tournée positionnés, mais aucun
     * point ni colis dans les champ points et colis
     */
    public void AddEvtTest7() {

        /* Traitement de 2 evts D fictif avec le même de tournée. */
        List<Evt> evts = new ArrayList<>();
        evts.add(newEvt(EvtRules.COLIS_FICTIF_DEBUT_TOURNEE, "D", addInfoComp(EInfoComp.ID_C11, "12M1207011970102210", null), null, 5));
        evts.add(newEvt(EvtRules.COLIS_FICTIF_FIN_TOURNEE, "D", addInfoComp(EInfoComp.ID_C11, "12M1207011970102210", null), null, 3));
        InsertPointTourneeDaoImpl.INSTANCE.miseAJourTournee(evts);

        /* Vérifications */

        /* récuperation du point */
        Row row = getTourneeEnBase("12M1207011970102210");
        Set<String> setPoints = getPoints(row);
        Set<String> setColis = getColis(row);
        Date debutTournee = getDateDebutTournee(row);
        Date finTournee = getDateFinTournee(row);

        assertEquals(setPoints.size(), 0);
        assertEquals(setColis.size(), 0);
        assertTrue(debutTournee.before(finTournee));

        /*
         * vérification de la présence de la tournée dans l'index tournee par
         * jour
         */
        String id = getFirstIdTourneesByJour("00000", "19700107");
        assertEquals(id, "12M1207011970102210");
    }

    @Test
    /**
     * Test simple : un evt TA sur un point C11 avec une eta mal formée
     * 
     * Attendu : un enr. dans la table pour ce point, avec l'evt TA mais l'eta non positionné (=null)
     */
    public void AddEvtTest8() {

        /* Traitement d'un evt TA avec un numéro d'id point */
        List<Evt> evts = new ArrayList<>();
        evts.add(newEvt(
                "no_lt",
                "TA",
                addInfoComp(EInfoComp.DIFF_GPS, "30",
                        addInfoComp(EInfoComp.ETA, "1230", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200206011970102010", null))), null));

        /* Traitement */
        InsertPointTourneeDaoImpl.INSTANCE.addEvtDansPoint(evts);

        /* Vérification en base */
        Row row = getPointEnBase("CGY12M1200206011970102010");
        assertNotNull(row);

        Set<UDTValue> setEvt = getEvtPointByType(row, "TA");
        assertEquals(setEvt.size(), 1);

        UDTValue evt = setEvt.iterator().next();
        assertNotNull(evt.getString("diff_gps"));
        assertNull(evt.getString("eta"));
    }

    @Test
    /**
     * Test simple : un evt TE avec 4 infoscomp destinée a devenir des infossupp
     * 
     * Attendu : un enr. dans la table pour ce point, avec l'evt TE et ses 4 infosupp
     */
    public void AddEvtTest9() {

        /* Traitement d'un evt TA avec un numéro d'id point */
        List<Evt> evts = new ArrayList<>();
        evts.add(newEvt(
                "no_lt",
                "TE",
                addInfoComp(
                        EInfoComp.DUREE_APPEL,
                        "123",
                        addInfoComp(
                                EInfoComp.NUMERO_APPEL,
                                "0611223344",
                                addInfoComp(EInfoComp.RESULTAT_APPEL, "OKOK",
                                        addInfoComp(EInfoComp.AVIS_PASSAGE, "4556", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200206011970112010", null))))),
                null));

        /* Traitement */
        InsertPointTourneeDaoImpl.INSTANCE.addEvtDansPoint(evts);

        /* Vérification en base */
        Row row = getPointEnBase("CGY12M1200206011970112010");
        assertNotNull(row);

        Set<UDTValue> setEvt = getEvtPointByType(row, "TE");
        assertEquals(setEvt.size(), 1);

        UDTValue evt = setEvt.iterator().next();
        Map<String, String> infosSupp = evt.getMap("info_supp", String.class, String.class);
        assertNotNull(infosSupp);
        assertEquals(infosSupp.size(), 4);
    }

    @Test
    /**
     * Test simple : Ajout d'un nouveau psm createurEvt="SACAP01" pour la tournée avec ID_C11="12M1207011970102210"
     * 
     * Attendu : mise à jour de la table tounée et l'ajout du nouveau psm "SACAP01" 
     */
    public void AddEvtTest10() {

        /* Traitement d'un evt TA avec un numéro d'id point */
        List<Evt> evts = new ArrayList<>();
        evts.add(newEvt("no_lt", "TA", addInfoComp(EInfoComp.ID_C11, "12M1207011970102210", null), "SACAP01"));

        /* Execution */
        InsertPointTourneeDaoImpl.INSTANCE.miseAJourTournee(evts);

        /* Vérification en base */
        /* récupération de la tournée */
        Row row = getTourneeEnBase("12M1207011970102210");
        assertNotNull(row);
        /* vérif qu'il contient bien le psm "SACAP01" */
        Set<String> setEvt = getPsmByType(row, "SACAP01");
        assertEquals(setEvt.size(), 1);
    }

    @Test
    /**
     * Test simple : un evt TA sur un point avec la date du point, heure du début et heure de fin du point
     * 
     * Attendu : un enr. dans la table pour ce point, avec l'evt TA présents et les valeurs Date debut/fin vides
     */
    public void AddEvtTest11() {

        /*
         * Traitement d'un evt TA avec un numéro d'id point et les dates
         * début/fin d'un point de tournée
         */
        List<Evt> evts = new ArrayList<>();
        Evt evt = newEvt("no_lt", "TA", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200103011970101411", null), "");
        evt.setDatePoint("20160607");
        evt.setHeureDebutPoint("0000");
        evt.setHeureFinPoint("1200");
        evts.add(evt);

        InsertPointTourneeDaoImpl.INSTANCE.addEvtDansPoint(evts);

        /* Vérification en base */
        Row row = getPointEnBase("CGY12M1200103011970101411");
        assertNotNull(row);
        assertEquals(row.getTimestamp(ETableTourneePoint.DATE_DEBUT_POINT.getNomColonne()), null);
        assertEquals(row.getTimestamp(ETableTourneePoint.DATE_FIN_POINT.getNomColonne()), null);
    }

    @Test
    /**
     * Test simple : un evt D sur un point avec la date du point, heure du début et heure de fin du point
     * 
     * Attendu : un enr. dans la table pour ce point, avec l'evt D présents et les valeurs Date debut/fin vides
     */
    public void AddEvtTest11_2() {
        /*
         * Traitement d'un evt TA avec un numéro d'id point et les dates
         * début/fin d'un point de tournée
         */
        List<Evt> evts = new ArrayList<>();
        Evt evt = newEvt("no_lt", "D", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200103011970101433", null), "");
        evt.setDatePoint("20160607");
        evt.setHeureDebutPoint("0000");
        evt.setHeureFinPoint("1200");
        evts.add(evt);

        InsertPointTourneeDaoImpl.INSTANCE.addEvtDansPoint(evts);

        /* Vérification en base */
        Row row = getPointEnBase("CGY12M1200103011970101433");
        assertNotNull(row);
    }

    @Test
    /** 
     * Entree: Un evt D a 13h41 sans infocomp d'idC11 ou idPointC11. Et avec une TA présente en base de 6h41 avec un idPointC11
     * Sortie: l'evt D a vu une infocomp idpointC11 lui être ajoutée avec la valeur idPointC11 de la TA précédente
     */
    public void trouverDernierEvtTACas1() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");

        Evt evtTA = newEvt("MM912357956FR",
                "TA",
                formatter.parseDateTime("18/05/2016 06:41:00").toDate(),
                "PCO",
                "75P14",
                1000);

        addInfoComp(evtTA, EInfoComp.ID_POINT_C11, "PCO75P1400118052016061411");
        
        try {
            InsertEvtDaoImpl.getInstance().insertEvts(Collections.singletonList(evtTA));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Evt evtD = newEvt("MM912357956FR", "D", formatter.parseDateTime("18/05/2016 13:41:00").toDate(), null, null, null);

        Evt evtTrouve = InsertPointTourneeDaoImpl.INSTANCE.trouverDernierEvtTA(evtD);
        
        assert(evtTrouve!=null);
        assert(evtTrouve.getInfoscomp()!=null);
        assert("PCO75P1400118052016061411".equals(evtTrouve.getInfoComp(EInfoComp.ID_POINT_C11)));
    }

    @Test
    /** 
     * Entree: Un evt D a 13h41 sans infocomp d'idC11 ou idPointC11. Et avec une 2 TA présentent en base de 6h41 avec un idPointC11 
     *         différent toutes les deux. 
     * Sortie: l'evt D a vu une infocomp idpointC11 lui être ajoutée avec la valeur idPointC11 de la TA précédente la plus récemment insérée.
     * 
     */
    public void trouverDernierEvtTACas2() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");

        /* 1er evt TA a 6h41 */
        Evt evtTA1 = newEvt("MM912357957FR",
                "TA",
                formatter.parseDateTime("18/05/2016 06:41:00").toDate(),
                "PCO",
                "75P14",
                1000);

        addInfoComp(evtTA1, EInfoComp.ID_POINT_C11, "PCO75P1400118052016061411");
        
        try {
            InsertEvtDaoImpl.getInstance().insertEvts(Collections.singletonList(evtTA1));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        /* 2sd evt TA a 6h41  aussi */
        Evt evtTA2 = newEvt("MM912357957FR",
                "TA",
                formatter.parseDateTime("18/05/2016 06:41:00").toDate(),
                "PCO",
                "75P14",
                1000);

        addInfoComp(evtTA2, EInfoComp.ID_POINT_C11, "PCO75P1400118052016061515");
        
        try {
            InsertEvtDaoImpl.getInstance().insertEvts(Collections.singletonList(evtTA2));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Evt evtD = newEvt("MM912357957FR", "D", formatter.parseDateTime("18/05/2016 13:41:00").toDate(), null, null, null);

        Evt evtTrouve = InsertPointTourneeDaoImpl.INSTANCE.trouverDernierEvtTA(evtD);
        
        /* on doit récupérer l'idPointC11 de l'evt inséré en dernier */
        assert(evtTrouve!=null);
        assert(evtTrouve.getInfoscomp()!=null);
        assert("PCO75P1400118052016061515".equals(evtTrouve.getInfoComp(EInfoComp.ID_POINT_C11)));
    }
    
    
    @Test
    /** 
     * Entree: Un evt D a 13h41 sans infocomp d'idC11 ou idPointC11. Et avec une 2 TA présentent en base de 6h41  et 6h42 avec un idPointC11 
     *         différent toutes les deux. La TA de 6h41 est insérée aprés celle des 6h42
     * Sortie: l'evt D a vu une infocomp idpointC11 lui être ajoutée avec la valeur idPointC11 de la TA précédente la plus anciennement insérée car avec la date evt la plus récente.
     * 
     */
    public void trouverDernierEvtTACas3() {
    	/* 1er evt TA a 6h42 */
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");

        Evt evtTA1 = newEvt("MM912357958FR",
                "TA",
                formatter.parseDateTime("18/05/2016 06:42:00").toDate(),
                "PCO",
                "75P14",
                1000);

        addInfoComp(evtTA1, EInfoComp.ID_POINT_C11, "PCO75P1400118052016061411");
        
        try {
            InsertEvtDaoImpl.getInstance().insertEvts(Collections.singletonList(evtTA1));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        /* 2sd evt TA a 6h41 */
        Evt evtTA2 = newEvt("MM912357958FR",
                "TA",
                formatter.parseDateTime("18/05/2016 06:41:00").toDate(),
                "PCO",
                "75P14",
                1000);

        addInfoComp(evtTA2, EInfoComp.ID_POINT_C11, "PCO75P1400118052016061515");
        
        try {
            InsertEvtDaoImpl.getInstance().insertEvts(Collections.singletonList(evtTA2));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Evt evtD = newEvt("MM912357958FR", "D", formatter.parseDateTime("18/05/2016 13:41:00").toDate(), null, null, null);

        Evt evtTrouve = InsertPointTourneeDaoImpl.INSTANCE.trouverDernierEvtTA(evtD);
        
        /* On doit récupérer l'idPoint de l'evt de 6h42 */
        assert(evtTrouve!=null);
        assert(evtTrouve.getInfoscomp()!=null);
        assert("PCO75P1400118052016061411".equals(evtTrouve.getInfoComp(EInfoComp.ID_POINT_C11)));
    }


    @Test
    /**
     /**
     * Test simple : un evt D sur un point C11 avec un outil de saisie "PSM123"
     * Attendu : un enr. dans la table pour ce point, avec l'outil de saisie "PSM123"
     */
    public void AddEvtTest12() {
    	 /* Traitement d'un evt D avec un numéro d'id point */
        List<Evt> evts = new ArrayList<>();
        evts.add(newEvt("no_lt", "D", addInfoComp(EInfoComp.ID_POINT_C11, "RNS35T5204213062016075200", null), "PSM123"));
        InsertPointTourneeDaoImpl.INSTANCE.addEvtDansPoint(evts);

        /* Vérification en base */
        Row row = getPointEnBase("RNS35T5204213062016075200");
        assertNotNull(row);

        Set<UDTValue> setEvt = getEvtPointByType(row, "D");

        UDTValue evt = setEvt.iterator().next();
        assertEquals (evt.getString("no_lt"), "no_lt");
        assertEquals (evt.getString("type_evt"), "D");
        assertEquals (evt.getString("outil_saisie"), "PSM123");
    }

    @Test
    public void trouverDerniereTourneeCas1() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");

        Evt evtTA = newEvt("MM912357959FR",
                "TA",
                formatter.parseDateTime("18/05/2016 06:41:00").toDate(),
                "PCO",
                "75P14",
                1000);

        addInfoComp(evtTA, EInfoComp.ID_POINT_C11, "PCO75P1400118052016061411");
        addInfoComp(evtTA, EInfoComp.ID_C11, "75P1418052016061411");

        InsertPointTourneeDaoImpl.INSTANCE.miseAJourTournee(Lists.newArrayList(evtTA));

        Evt evtD = newEvt("MM912357959FR", "D", formatter.parseDateTime("18/05/2016 13:41:00").toDate(), "PCO", "75P14");

        Tournee tournee = InsertPointTourneeDaoImpl.INSTANCE.trouverDerniereTournee(evtD);

        assertEquals(tournee.getCodeTournee(), "75P14");
        assertEquals(tournee.getDateTournee(), formatter.parseDateTime("18/05/2016 06:14:11").toDate());
        assertEquals(tournee.getIdC11(), "75P1418052016061411");
    }

    /**
     * Ajout d'une info comp dans une liste d'infocomp passée en entrée. Si la
     * liste d'infocomp est null, est n'est pas reprise (rien a reprendre). Si
     * elle existe, une autre map est retournée, contenant les infocomps
     * contenues dans la liste fournie, plus l'infocomp a ajouter (clé,valeur
     * fournis)
     *
     * @param key
     *            : la clé de la nouvelle infocomp
     * @param value
     *            : la valeur de la nouvelle infocomp
     * @param pInfoComp
     *            : la liste d'infocomp
     * @return une liste d'infocomp
     */
    private Map<String, String> addInfoComp(EInfoComp key, String value, Map<String, String> pInfoComp) {
        Map<String, String> infosComps = new HashMap<>();
        if (pInfoComp != null)
            infosComps.putAll(pInfoComp);

        infosComps.put(key.getCode(), value);
        return infosComps;
    }

    private void addInfoComp(Evt evt, EInfoComp key, String value) {
        Map<String, String> infosComps = null;

        if (evt.getInfoscomp().equals(Evt.NO_INFOCOMP)) {
            infosComps = new HashMap<>();
        } else {
            infosComps = evt.getInfoscomp();
        }

        infosComps.put(key.getCode(), value);
        evt.setInfoscomp(infosComps);
    }

    /**
     * Retourne un nouvel événement à partir des valeurs fournies
     * 
     * @param noLt
     *            : identifiant du colis (Not null)
     * @param codeevt
     *            : Le code evenement
     * @param infosComp
     *            : une liste d'infoComp (peut être null)
     * @return
     */
    private Evt newEvt(@NotNull String noLt, @NotNull String codeEvt, Map<String, String> infosComp, String createurEvt) {
        return newEvt(noLt, codeEvt, new Date(), null, null)
                .setLieuEvt("00000")
                .setCreateurEvt(createurEvt)
                .setInfoscomp(infosComp);
    }

    private Evt newEvt(@NotNull String noLt, @NotNull String codeEvt, @NotNull Date dateEvt, String codeAgence, String codeTournee) {
        return new Evt()
            .setNoLt(noLt)
            .setCodeEvt(codeEvt)
            .setDateEvt(dateEvt)
            .setLieuEvt(codeAgence)
            .setSsCodeEvt(codeTournee);
    }

    /**
     *  Retourne un nouvel événement à partir des valeurs fournies
     *  
     * @param noLt
     * @param codeEvt
     * @param dateEvt
     * @param codeAgence
     * @param codeTournee
     * @param priorite
     * @return
     */
    private Evt newEvt(@NotNull String noLt, @NotNull String codeEvt, @NotNull Date dateEvt, String codeAgence, String codeTournee, Integer priorite) {
		return newEvt(noLt, codeEvt, dateEvt, codeAgence, codeTournee).setPrioriteEvt(priorite);
	}

    /**
     * Retourne un nouvel événement à partir des valeurs fournies
     * 
     * @param noLt : identifiant du colis (Not null)
     * @param codeevt : Le code evenement
     * @param infosComp : une liste d'infoComp (peut être null)
     * @param minutes : le nombre de minutes à retirer à la date actuelle pour le champ date_evt
     * @return
     */
	private Evt newEvt(@NotNull String noLt, @NotNull String codeEvt, Map<String, String> infosComp, String createurEvt, int minutes) {
        Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -minutes);

		return newEvt(noLt, codeEvt, cal.getTime(), "00000", null).setInfoscomp(infosComp).setCreateurEvt(createurEvt);
	}

    /**
     * Récupère un Point en base
     * 
     * @param idPoint
     *            : identifiant point
     * @return
     */
	private Row getPointEnBase(String idPoint) {

        ResultSet evtResult = getSession().execute(getOnePoint.bind(idPoint));
		if (evtResult != null)
			return evtResult.one();
		else
			return null;
	}

    /**
     * Renvoi un set des evenements (format evtPoint) contenu dans le point et
     * de type indiqués
     * 
     * @param row
     * @param type_evt
     * @return
     */
	private Set<UDTValue> getEvtPointByType(Row row, String type_evt) {

        Set<UDTValue> setResult = new HashSet<>();

		for (UDTValue evt : getEvtPoint(row)) {
			if (evt.getString("type_evt").equals(type_evt))
				setResult.add(evt);
		}
		return setResult;
	}

    /**
     * Renvoi un set des psm d'une tournée
     * 
     * @param row
     * @param psm
     * @return
     */
	private Set<String> getPsmByType(Row row, String psm) {

        Set<String> setResult = new HashSet<>();

		for (String psmLoc : getPsm(row)) {
			if (psmLoc.equals(psm))
				setResult.add(psmLoc);
		}
		return setResult;
	}

    /**
     * Retourne l'ensemble des evenement (format evtPoint) d'un point (d'un row)
     * 
     * @param row
     * @return
     */
    private Set<UDTValue> getEvtPoint(Row row) {
        return row.getSet(ETableTourneePoint.EVENEMENTS.getNomColonne(), UDTValue.class);
    }

    /**
     * Retourne l'ensemble des PSM d'une tournée (d'un row)
     * 
     * @param row
     * @return
     */
    private Set<String> getPsm(Row row) {
        return row.getSet(ETableTournee.PSM.getNomColonne(), String.class);
    }

    /**
     * Récupère une Tournée en base
     * 
     * @param idTournee
     *            : identifiant de la tournee
     * @return
     */
	private Row getTourneeEnBase(String idTournee) {

        ResultSet evtResult = getSession().execute(getOneTournee.bind(idTournee));
		if (evtResult != null)
			return evtResult.one();
		else
			return null;
	}

    /**
     * Retourne le champ Point d'une row de Tournee
     * 
     * @param row
     * @return
     */
    private Set<String> getPoints(Row row) {
        return row.getSet(ETableTournee.POINTS.getNomColonne(), String.class);
    }

    /**
     * Retourne le champ Colis d'une row de Tournee
     * 
     * @param row
     * @return
     */
    private Set<String> getColis(Row row) {
        return row.getSet(ETableTournee.COLIS.getNomColonne(), String.class);
    }

    /**
     * Retourne le champ debut_tournee d'une row de Tournee
     * 
     * @param row
     * @return
     */
    private Date getDateDebutTournee(Row row) {
        return row.getTimestamp(ETableTournee.DEBUT_TOURNEE.getNomColonne());
    }

    /**
     * Retourne le champ fin_tournee d'une row de Tournee
     * 
     * @param row
     * @return
     */
    private Date getDateFinTournee(Row row) {
        return row.getTimestamp(ETableTournee.FIN_TOURNEE.getNomColonne());
    }

    /**
     * Retourne un resultat (ResultSet) des lignes de l'index
     * idx_tournee_agence_jour
     * 
     * @param agence
     *            : le poste comptable de l'agence
     * @param jour
     *            : le jour format YYYYMMDD
     * @return
     */
    private ResultSet getTourneesByJour(@NotNull String agence, @NotNull String jour) {
        return getSession().execute(getOneIdxTournee.bind(agence, jour));
    }

    /**
     * Retourne la premiere des lignes de l'index idx_tournee_agence_jour
     * 
     * @param agence
     *            : le poste comptable de l'agence
     * @param jour
     *            : le jour format YYYYMMDD
     * @return Un enr. de l'index.
     */
    private Row getFirstTourneesByJour(@NotNull String agence, @NotNull String jour) {
        ResultSet res = getTourneesByJour(agence, jour);
        if (res != null) {
            return res.one();
        } else
            return null;
    }

    /**
     * Retourne l'identifiant tournee de la premiere des lignes de l'index
     * idx_tournee_agence_jour
     * 
     * @param agence
     *            : le poste comptable de l'agence
     * @param jour
     *            : le jour format YYYYMMDD
     * @return Un identifiant tournee (idC11)
     */
    private String getFirstIdTourneesByJour(@NotNull String agence, @NotNull String jour) {
    	Row res = getFirstTourneesByJour(agence, jour);
        if (res != null)
            return res.getString(ETableIdxTourneeJour.ID_TOURNEE.getNomColonne());
        else
            return null;
    }

    @AfterClass
    public void tearDownAfterClass() throws Exception {
    	cleanDB();
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
        CacheManagerService.INSTANCE.stopUpdater();
        CacheManagerService.INSTANCE.delProjet("service");
    }
}
