package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.cassandra.request.builder.CassandraRequestBuilder;
import com.chronopost.cassandra.table.ETableColisSpecifications;
import com.chronopost.cassandra.table.ETableTournee;
import com.chronopost.cassandra.table.ETableTourneePoint;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.getsynthesetournees.v1.ColisPoint;
import com.chronopost.vision.model.getsynthesetournees.v1.PointTournee;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.model.updatespecificationscolis.v1.ESpecificiteColis;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.collect.Sets;

/**
 * Classe de test du Mapper
 * 
 * @author jcbontemps
 *
 */
public class MapperTest {
	
	private static final DateTime MAINTENANT = DateTime.now();

    private boolean suiteLaunch = true;
    private PreparedStatement psDelTournee;
    private PreparedStatement psDelPoint;
    private PreparedStatement psGetSpecColis;
    private PreparedStatement psInsertSpecColis;
    private ITranscoderDao mockTranscoderDao = Mockito.mock(ITranscoderDao.class);

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }

        CCMBridge.ipOfNode(1);

        psDelTournee = getSession().prepare(
                "delete from " + ETableTournee.TABLE_NAME + " WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = ? ");

        psDelPoint = getSession().prepare(
                "delete from " + ETableTourneePoint.TABLE_NAME + " WHERE " + ETableTourneePoint.ID_POINT.getNomColonne() + " = ? ");
        
        psGetSpecColis = getSession().prepare(ETableColisSpecifications.getSpecifColisQuery().getQuery());
        
        psInsertSpecColis = getSession().prepare(CassandraRequestBuilder.buildInsert(ETableColisSpecifications.TABLE_NAME,
				Arrays.asList(ETableColisSpecifications.NO_LT, ETableColisSpecifications.SPECIFS_EVT,
						ETableColisSpecifications.SPECIFS_SERVICE, ETableColisSpecifications.ETAPES,
						ETableColisSpecifications.CONSIGNES_TRAITEES, ETableColisSpecifications.INFO_SUPP,
						ETableColisSpecifications.SERVICE)));
        
        Map<String, Map<String, String>> transcos = new HashMap<String, Map<String, String>>();
        Map<String, String> transcosFlips = new HashMap<String, String>();
        transcosFlips.put("evt_Dplus", "|P|D|B|RG|RC|PR|RB|CO|PA|NA|N1|N2|P1|P2|D1|D2|D3|IP|");
        transcos.put("parametre_microservices", transcosFlips);
        Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("DiffusionVision")).thenReturn(transcos);
        TranscoderService.INSTANCE.setDao(mockTranscoderDao);
        TranscoderService.INSTANCE.addProjet("DiffusionVision");
        TranscoderService.INSTANCE.startUpdater();
        int nbRetries = 0;
        while (!TranscoderService.INSTANCE.getTranscoder("DiffusionVision").transcode("parametre_microservices", "evt_Dplus")
                .equals("|P|D|B|RG|RC|PR|RB|CO|PA|NA|N1|N2|P1|P2|D1|D2|D3|IP|")
                && nbRetries < 100) {
            Thread.sleep(500);
            ++nbRetries;
        }
    }

    /**
     * Test du mapping
     * @throws InterruptedException 
     */
    @Test(groups = { "database-needed", "slow" })
    public void map() throws InterruptedException {
        String idPointC11 = new IdPointC11("FTV", "666", "BIDON01010101010101").toString();
        delPointTournee(idPointC11);

        // 24/02/16 13:47
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "STARK INDUSTRIES", new Date(1456321620125L), "TA", null, null, idPointC11, null, null);
        // 24/02/16 10:02
        SyntheseTourneeDaoUtils.insertPoint("JP204948885JB", "P", "NED STARK", new Date(1456308120006L), "D", "-81", "12", idPointC11, null, null);
        // 24/02/16 10:08
        SyntheseTourneeDaoUtils.insertPoint("JP204948877JB", "P", "JON SNOW",  new Date(1456308521935L), "D", "31", "13", idPointC11, null, null);
        // 24/02/16 11:45
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "MARTELL",   new Date(1456314300982L), "D", null, null, idPointC11, null, null);

        PointTournee point = new PointTournee();
        Mapper.makeMapDeColisPoint(SyntheseTourneeDaoUtils.getPointEnBase(idPointC11), point, null, null, null);

        assertEquals(point.getAnomalies().size(), 0);
        assertEquals(point.getDatePassage(), new Date(1456314300982L));
        assertEquals((int) point.getDiffETA(), -81);
        assertEquals((int) point.getDiffGPS(), 12);
        assertEquals((int) point.getNumPointTA(), 666);
        assertEquals(point.getIdentifiantPoint(), idPointC11);
        assertEquals(point.getNomDestinataire(), "NED STARK");
        assertEquals(point.getTypeDestinataire(), "P");

        Set<ColisPoint> colisPrevus = point.getColisPrevus();
        Set<ColisPoint> colisPresents = point.getColisPresents();

        assertEquals(colisPrevus.size(), 1);
        assertEquals(colisPresents.size(), 3);

        for (ColisPoint colis : colisPrevus) {
            assertEquals("TA", colis.getCodeEvenement());
            assertEquals(new Date(1456321620125L), colis.getDateEvt());
            assertEquals("GR960704576FR", colis.getNo_lt());
        }

        for (ColisPoint colis : colisPresents) {
            assertNotSame(colis.getCodeEvenement(), "TA");
            if ("JP204948885JB".equals(colis.getNo_lt()))
                assertEquals(new Date(1456308120006L), colis.getDateEvt());
            if ("JP204948877JB".equals(colis.getNo_lt()))
                assertEquals(new Date(1456308521935L), colis.getDateEvt());
            if ("GR960704576FR".equals(colis.getNo_lt()))
                assertEquals(new Date(1456314300982L), colis.getDateEvt());
        }

        SyntheseTourneeDaoUtils.delPointTournee(idPointC11);
    }
    
    /**
     * Test du mapping avec caractéristiques colis existante
     */
    @Test(groups = { "database-needed", "slow" })
    public void mapAvecSpecifColis() {
        String idPointC11 = new IdPointC11("FTV", "666", "BIDON01010101010101").toString();
        delPointTournee(idPointC11);

        // 24/02/16 13:47
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "STARK INDUSTRIES", new Date(1456321620125L), "TA", null, null, idPointC11, null, null);
        // 24/02/16 10:02 : Premier evt DPlus
        SyntheseTourneeDaoUtils.insertPoint("JP204948885JB", "P", "NED STARK", new Date(1456308120006L), "D", "-81", "12", idPointC11, null, null);
        // 24/02/16 10:08
        SyntheseTourneeDaoUtils.insertPoint("JP204948877JB", "P", "JON SNOW",  new Date(1456308521935L), "D", "31", "13", idPointC11, null, null);
        // 24/02/16 11:45
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "MARTELL",   new Date(1456314300982L), "D", null, null, idPointC11, null, null);

        /* On se fait des specif colis */
        PointTournee point = new PointTournee();
        Map<String, SpecifsColis> specifsColis = new HashMap<>();
        HashMap<Date, String> mapSpecCol = new HashMap<Date,String>();
        mapSpecCol.put(new Date(1456314200982L), "SENSIBLE");
        mapSpecCol.put(new Date(1456314400982L), "VERDATRE");
        SpecifsColis specCol = new SpecifsColis();
        specCol.setSpecifsEvt(mapSpecCol);
        specifsColis.put("GR960704576FR",specCol);
		Mapper.makeMapDeColisPoint(SyntheseTourneeDaoUtils.getPointEnBase(idPointC11), point, specifsColis, null, null);

        assertEquals(point.getAnomalies().size(), 0);
        assertEquals(point.getDatePassage(), new Date(1456314300982L));
        assertEquals((int) point.getDiffETA(), -81);
        assertEquals((int) point.getDiffGPS(), 12);
        assertEquals((int) point.getNumPointTA(), 666);
        assertEquals(point.getIdentifiantPoint(), idPointC11);
        assertEquals(point.getNomDestinataire(), "NED STARK");
        assertEquals(point.getTypeDestinataire(), "P");

        Set<ColisPoint> colisPrevus = point.getColisPrevus();
        Set<ColisPoint> colisPresents = point.getColisPresents();

        assertEquals(colisPrevus.size(), 1);
        assertEquals(colisPresents.size(), 3);

        for (ColisPoint colis : colisPrevus) {
            assertEquals("TA", colis.getCodeEvenement());
            assertEquals(new Date(1456321620125L), colis.getDateEvt());
            assertEquals("GR960704576FR", colis.getNo_lt());
        }

        for (ColisPoint colis : colisPresents) {
            assertNotSame(colis.getCodeEvenement(), "TA");
            if ("JP204948885JB".equals(colis.getNo_lt()))
                assertEquals(new Date(1456308120006L), colis.getDateEvt());
            if ("JP204948877JB".equals(colis.getNo_lt()))
                assertEquals(new Date(1456308521935L), colis.getDateEvt());
            if ("GR960704576FR".equals(colis.getNo_lt())) {
                assertEquals(new Date(1456314300982L), colis.getDateEvt());
                assert(colis.getCaracteristiques() != null);
                assertEquals(1, colis.getCaracteristiques().size());
                assert(colis.getCaracteristiques().contains("SENSIBLE"));
            }
        }
        SyntheseTourneeDaoUtils.delPointTournee(idPointC11);
    }
    
    /**
     * Test du mapping avec infos tournee code_tournee et date_tournee
     */
    @Test(groups = { "database-needed", "slow" })
    public void testEvtsAvecEtSansInfosTournee_1() {
        String idPointC11 = new IdPointC11("FTV", "666", "BIDON01010101010101").toString();
        delPointTournee(idPointC11);

        // 24/02/16 13:47
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "STARK INDUSTRIES", new Date(1456321620125L), "TA", null, null, idPointC11, "codeT", "dateT", null, null, null, null);
        // 24/02/16 10:02 : Premier evt DPlus
        SyntheseTourneeDaoUtils.insertPoint("JP204948885JB", "P", "NED STARK", new Date(1456308120006L), "D", "-81", "12", idPointC11, "codeT", "dateT", null, null, null, null);
        // 24/02/16 10:08
        SyntheseTourneeDaoUtils.insertPoint("JP204948877JB", "P", "JON SNOW",  new Date(1456308521935L), "D", "31", "13", idPointC11, null, "dateT", null, null, null, null);
        // 24/02/16 11:45
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "MARTELL",   null, "D", null, null, idPointC11, "codeT", null, null, null, null, null);

        /* On se fait des specif colis */
        PointTournee point = new PointTournee();
        Map<String, SpecifsColis> specifsColis = new HashMap<>();
        HashMap<Date, String> mapSpecCol = new HashMap<Date,String>();
        mapSpecCol.put(new Date(1456314200982L), "SENSIBLE");
        mapSpecCol.put(new Date(1456314400982L), "VERDATRE");
        SpecifsColis specCol = new SpecifsColis();
        specCol.setSpecifsEvt(mapSpecCol);
        specifsColis.put("GR960704576FR",specCol);
		Mapper.makeMapDeColisPoint(SyntheseTourneeDaoUtils.getPointEnBase(idPointC11), point, specifsColis, "codeT",DateRules.formatDateYYYYMMDD(new Date(1456308120006L)));

        assertEquals(point.getAnomalies().size(), 0);
        assertEquals(point.getDatePassage(), new Date(1456308120006L));
        assertEquals((int) point.getDiffETA(), -81);
        assertEquals((int) point.getDiffGPS(), 12);
        assertEquals((int) point.getNumPointTA(), 666);
        assertEquals(point.getIdentifiantPoint(), idPointC11);
        assertEquals(point.getNomDestinataire(), "NED STARK");
        assertEquals(point.getTypeDestinataire(), "P");

        Set<ColisPoint> colisPrevus = point.getColisPrevus();
        Set<ColisPoint> colisPresents = point.getColisPresents();

        assertEquals(colisPrevus.size(), 1);
        assertEquals(colisPresents.size(), 1);

        for (ColisPoint colis : colisPrevus) {
            assertEquals("TA", colis.getCodeEvenement());
            assertEquals(new Date(1456321620125L), colis.getDateEvt());
            assertEquals("GR960704576FR", colis.getNo_lt());
        }

        for (ColisPoint colis : colisPresents) {
            assertNotSame(colis.getCodeEvenement(), "TA");
            Iterator<ColisPoint> iterator = colisPresents.iterator();
            assertTrue(iterator.next().getNo_lt().equals("JP204948885JB"));
            assertFalse(colisPresents.contains("JP204948877JB"));
            assertFalse(colisPresents.contains("JP204948877JB-P"));
            assertFalse(colisPresents.contains("GR960704576FR"));
            assertFalse(colisPresents.contains("GR960704576FR-E"));
        }
        SyntheseTourneeDaoUtils.delPointTournee(idPointC11);
    }
    
    /**
     * Test du mapping avec infos tournee code_tournee et sans date_tournee
     */
    @Test(groups = { "database-needed", "slow" })
    public void testEvtsAvecEtSansInfosTournee_2() {
        String idPointC11 = new IdPointC11("FTV", "666", "BIDON01010101010101").toString();
        delPointTournee(idPointC11);

        // 24/02/16 13:47
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "STARK INDUSTRIES", new Date(1456321620125L), "TA", null, null, idPointC11, "codeT", "dateT", null, null, null, null);
        // 24/02/16 10:08 : Premier evt DPlus de la tournée
        SyntheseTourneeDaoUtils.insertPoint("JP204948885JB", "P", "NED STARK", new Date(1456308521935L), "D", "-81", "12", idPointC11, "codeT", "dateT", null, null, null, null);
        // 24/02/16 10:02 : Premier evt DPlus mais code tournée différent de la tournée
        SyntheseTourneeDaoUtils.insertPoint("JP204948877JB", "P", "JON SNOW",  new Date(1456308120006L), "D", "31", "13", idPointC11, null, "dateT", null, null, null, null);
        // 24/02/16 11:45
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "MARTELL",   new Date(1456314300982L), "D", null, null, idPointC11, "codeT", null, null, null, null, null);

        /* On se fait des specif colis */
        PointTournee point = new PointTournee();
        Map<String, SpecifsColis> specifsColis = new HashMap<>();
        HashMap<Date, String> mapSpecCol = new HashMap<Date,String>();
        mapSpecCol.put(new Date(1456314200982L), "SENSIBLE");
        mapSpecCol.put(new Date(1456314400982L), "VERDATRE");
        SpecifsColis specCol = new SpecifsColis();
        specCol.setSpecifsEvt(mapSpecCol);
        specifsColis.put("GR960704576FR",specCol);
		Mapper.makeMapDeColisPoint(SyntheseTourneeDaoUtils.getPointEnBase(idPointC11), point, specifsColis, "codeT", null);

        assertEquals(point.getAnomalies().size(), 0);
        assertEquals(point.getDatePassage(), new Date(1456314300982L));
        assertEquals((int) point.getDiffETA(), -81);
        assertEquals((int) point.getDiffGPS(), 12);
        assertEquals((int) point.getNumPointTA(), 666);
        assertEquals(point.getIdentifiantPoint(), idPointC11);
        assertEquals(point.getNomDestinataire(), "NED STARK");
        assertEquals(point.getTypeDestinataire(), "P");

        Set<ColisPoint> colisPrevus = point.getColisPrevus();
        Set<ColisPoint> colisPresents = point.getColisPresents();

        assertEquals(colisPrevus.size(), 1);
        assertEquals(colisPresents.size(), 2);

        for (ColisPoint colis : colisPrevus) {
            assertEquals("TA", colis.getCodeEvenement());
            assertEquals(new Date(1456321620125L), colis.getDateEvt());
            assertEquals("GR960704576FR", colis.getNo_lt());
        }

        for (ColisPoint colis : colisPresents) {
            assertNotSame(colis.getCodeEvenement(), "TA");
            Iterator<ColisPoint> iterator = colisPresents.iterator();
            Set<String> noLtSet = new HashSet<>();
            while (iterator.hasNext()) {
            	noLtSet.add(iterator.next().getNo_lt());
			}
            assertTrue(noLtSet.contains("GR960704576FR"));
            assertTrue(noLtSet.contains("JP204948885JB"));
        }
        SyntheseTourneeDaoUtils.delPointTournee(idPointC11);
    }
    
    /**
     * Test du mapping avec infos tournee date_tournee et sans code_tournee
     */
    @Test(groups = { "database-needed", "slow" })
    public void testEvtsAvecEtSansInfosTournee_3() {
        String idPointC11 = new IdPointC11("FTV", "666", "BIDON01010101010101").toString();
        delPointTournee(idPointC11);

        // 24/02/16 13:47
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "STARK INDUSTRIES", new Date(1456321620125L), "TA", null, null, idPointC11, "codeT", "dateT", null, null, null, null);
        // 24/02/16 11:45
        SyntheseTourneeDaoUtils.insertPoint("JP204948885JB", "P", "NED STARK", new Date(1456314300982L), "D", "-81", "12", idPointC11, "codeT", "dateT", null, null, null, null);
        // 24/02/16 10:08 : Premier evt DPlus de la tournée
        SyntheseTourneeDaoUtils.insertPoint("JP204948877JB", "P", "JON SNOW",  new Date(1456308521935L), "D", "31", "13", idPointC11, null, "dateT", null, null, null, null);
        // 24/02/16 10:02 : Premier evt DPlus mais date tournée différent de la tournée
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "MARTELL", null, "D", null, null, idPointC11, "codeT", null, null, null, null, null);

        /* On se fait des specif colis */
        PointTournee point = new PointTournee();
        Map<String, SpecifsColis> specifsColis = new HashMap<>();
        HashMap<Date, String> mapSpecCol = new HashMap<Date,String>();
        mapSpecCol.put(new Date(1456314200982L), "SENSIBLE");
        mapSpecCol.put(new Date(1456314400982L), "VERDATRE");
        SpecifsColis specCol = new SpecifsColis();
        specCol.setSpecifsEvt(mapSpecCol);
        specifsColis.put("GR960704576FR",specCol);
		Mapper.makeMapDeColisPoint(SyntheseTourneeDaoUtils.getPointEnBase(idPointC11), point, specifsColis, null,  DateRules.formatDateYYYYMMDD(new Date(1456308120006L)));

        assertEquals(point.getAnomalies().size(), 0);
        assertEquals(point.getDatePassage(), new Date(1456314300982L));
        assertEquals((int) point.getDiffETA(), 31);
        assertEquals((int) point.getDiffGPS(), 13);
        assertEquals((int) point.getNumPointTA(), 666);
        assertEquals(point.getIdentifiantPoint(), idPointC11);
        assertEquals(point.getNomDestinataire(), "JON SNOW");
        assertEquals(point.getTypeDestinataire(), "P");

        Set<ColisPoint> colisPrevus = point.getColisPrevus();
        Set<ColisPoint> colisPresents = point.getColisPresents();

        assertEquals(colisPrevus.size(), 1);
        assertEquals(colisPresents.size(), 2);

        for (ColisPoint colis : colisPrevus) {
            assertEquals("TA", colis.getCodeEvenement());
            assertEquals(new Date(1456321620125L), colis.getDateEvt());
            assertEquals("GR960704576FR", colis.getNo_lt());
        }

        for (ColisPoint colis : colisPresents) {
            assertNotSame(colis.getCodeEvenement(), "TA");
            Iterator<ColisPoint> iterator = colisPresents.iterator();
            Set<String> noLtSet = new HashSet<>();
            while (iterator.hasNext()) {
            	noLtSet.add(iterator.next().getNo_lt());
			}
            assertTrue(noLtSet.contains("JP204948885JB"));
            assertTrue(noLtSet.contains("JP204948877JB"));
        }
        SyntheseTourneeDaoUtils.delPointTournee(idPointC11);
    }
    
    /**
     * Test du mapping avec infos tournee date_tournee et sans code_tournee
     */
    @Test(groups = { "database-needed", "slow" })
    public void testEvts_withInfosFromEvtDPlus() {
        String idPointC11 = new IdPointC11("FTV", "666", "BIDON01010101010101").toString();
        delPointTournee(idPointC11);

        // 24/02/16 13:47
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "STARK INDUSTRIES", new Date(1456321620125L), "TA", null, null, idPointC11, "codeT", "dateT", null, null, "ko", null);
        // 24/02/16 11:45
        SyntheseTourneeDaoUtils.insertPoint("JP204948885JB", "P", "NED STARK", new Date(1456314300982L), "D", "-81", "12", idPointC11, "codeT", "dateT", null, null, "ko", null);
        // 24/02/16 10:08 : Premier evt DPlus de la tournée
        SyntheseTourneeDaoUtils.insertPoint("JP204948877JB", "P", "JON SNOW",  new Date(1456308521935L), "D", "31", "13", idPointC11, null, "dateT", "1045", "1115", "eta", null);
        // 24/02/16 10:02 : Premier evt DPlus mais date tournée différent de la tournée
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "MARTELL",   null , "D", null, null, idPointC11, "codeT", null, null, null, "ko", null);

        /* On se fait des specif colis */
        PointTournee point = new PointTournee();
        Map<String, SpecifsColis> specifsColis = new HashMap<>();
        HashMap<Date, String> mapSpecCol = new HashMap<Date,String>();
        mapSpecCol.put(new Date(1456314200982L), "SENSIBLE");
        mapSpecCol.put(new Date(1456314400982L), "VERDATRE");
        SpecifsColis specCol = new SpecifsColis();
        specCol.setSpecifsEvt(mapSpecCol);
        specifsColis.put("GR960704576FR",specCol);
		Mapper.makeMapDeColisPoint(SyntheseTourneeDaoUtils.getPointEnBase(idPointC11), point, specifsColis, null,  DateRules.formatDateYYYYMMDD(new Date(1456314300982L)));

        assertEquals(point.getAnomalies().size(), 0);
        assertEquals(point.getDatePassage(), new Date(1456314300982L));
        assertEquals((int) point.getDiffETA(), 31);
        assertEquals(point.getEta(), "eta");
        assertEquals((int) point.getDiffGPS(), 13);
        assertEquals((int) point.getNumPointTA(), 666);
        assertEquals(point.getIdentifiantPoint(), idPointC11);
        assertEquals(point.getNomDestinataire(), "JON SNOW");
        assertEquals(point.getTypeDestinataire(), "P");

        Set<ColisPoint> colisPrevus = point.getColisPrevus();
        Set<ColisPoint> colisPresents = point.getColisPresents();

        assertEquals(colisPrevus.size(), 1);
        assertEquals(colisPresents.size(), 2);

        for (ColisPoint colis : colisPrevus) {
            assertEquals("TA", colis.getCodeEvenement());
            assertEquals(new Date(1456321620125L), colis.getDateEvt());
            assertEquals("GR960704576FR", colis.getNo_lt());
        }

        for (ColisPoint colis : colisPresents) {
            assertNotSame(colis.getCodeEvenement(), "TA");
            Iterator<ColisPoint> iterator = colisPresents.iterator();
            Set<String> noLtSet = new HashSet<>();
            while (iterator.hasNext()) {
            	noLtSet.add(iterator.next().getNo_lt());
			}
            assertTrue(noLtSet.contains("JP204948885JB"));
            assertTrue(noLtSet.contains("JP204948877JB"));
        }
        SyntheseTourneeDaoUtils.delPointTournee(idPointC11);
    }
    
    /**
     * Test du mapping avec precocite
     */
    @Test(groups = { "database-needed", "slow" })
    public void mapAvecPrecocite() {
        String idPointC11 = new IdPointC11("FTV", "666", "BIDON01010101010101").toString();
        delPointTournee(idPointC11);

        SyntheseTourneeDaoUtils.insertPoint("GR960704576MA", "E", "TEST",   new Date(1456314300982L), "D", null, null, idPointC11, null, null);
        
        /* On se fait des specif colis */
        PointTournee point = new PointTournee();
        Map<String, SpecifsColis> specifsColis = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        SpecifsColis specCol1= new SpecifsColis();
        specCol1.setSpecifsService(new HashMap<Date, Set<String>>());
        specCol1.getSpecifsService().put(formatter.parseDateTime("07/01/2016 06:15:00").toDate(), Sets.newHashSet(ESpecificiteColis.DIX_HUIT_HEURE.getCode()));
        specifsColis.put("GR960704576MA",specCol1);
		Mapper.makeMapDeColisPoint(SyntheseTourneeDaoUtils.getPointEnBase(idPointC11), point, specifsColis, null, null);

        assertEquals(point.getAnomalies().size(), 0);
        assertEquals(point.getDatePassage(), new Date(1456314300982L));
        assertEquals((int) point.getNumPointTA(), 666);
        assertEquals(point.getIdentifiantPoint(), idPointC11);
        assertEquals(point.getNomDestinataire(), "TEST");
        assertEquals(point.getTypeDestinataire(), "E");

        Set<ColisPoint> colisPresents = point.getColisPresents();

        assertEquals(colisPresents.size(), 1);

        for (ColisPoint colis : colisPresents) {
            
            if("GR960704576MA".equals(colis.getNo_lt())) {
            	assertEquals(colis.getPrecocite(), ESpecificiteColis.DIX_HUIT_HEURE.getCode());
            	assertFalse(colis.getCaracteristiques().contains(ESpecificiteColis.DIX_HUIT_HEURE.getCode()));
            }
        }
        SyntheseTourneeDaoUtils.delPointTournee(idPointC11);
    }
    
    /**
     * test du mapping avec des valeurs à null. On verifié bien que le mapping
     * conserve l'état "non renseigné" d'une donnée
     */
    @Test(groups = { "database-needed", "slow" })
    public void testNull() {
        String idPointC11 = new IdPointC11("FTV", "666", "BIDON01010101010101").toString();
        SyntheseTourneeDaoUtils.delPointTournee(idPointC11);

        {
            SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "STARK INDUSTRIES", null, "TA", null, null, idPointC11, null, null);
            SyntheseTourneeDaoUtils.insertPoint("JP204948885JB", "P", "NED STARK", null, "D", null, null, idPointC11, null, null);

			PointTournee point = new PointTournee();
			Mapper.makeMapDeColisPoint(SyntheseTourneeDaoUtils.getPointEnBase(idPointC11), point, null, null, null);

            assertNull(point.getDiffETA());
            assertNull(point.getDiffGPS());
            assertNull(point.getDatePassage());  

            SyntheseTourneeDaoUtils.delPointTournee(idPointC11);
        }

        {
            SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "STARK INDUSTRIES", null, "TA", null, null, idPointC11, null, null);
            SyntheseTourneeDaoUtils.insertPoint("JP204948885JB", "P", "NED STARK", new Date(1456308120006L), "D", "-81", "12", idPointC11, null, null);
            PointTournee point = new PointTournee();
            Mapper.makeMapDeColisPoint(SyntheseTourneeDaoUtils.getPointEnBase(idPointC11), point, null, null, null);

            assertEquals(-81, (int) point.getDiffETA());
            assertEquals(12, (int) point.getDiffGPS());
            assertEquals(new Date(1456308120006L), point.getDatePassage());

            SyntheseTourneeDaoUtils.delPointTournee(idPointC11);
        }

        {
            SyntheseTourneeDaoUtils.insertPoint("JP204948877JB", "P", "JON SNOW", new Date(1456308120935L), "D", "31", "13", idPointC11, null, null);
            SyntheseTourneeDaoUtils.insertPoint("JP204948885JB", "P", "NED STARK", new Date(1456308120006L), "D", "-81", "12", idPointC11, null, null);
            PointTournee point = new PointTournee();
            Mapper.makeMapDeColisPoint(SyntheseTourneeDaoUtils.getPointEnBase(idPointC11), point, null, null, null);

            assertEquals(-81, (int) point.getDiffETA());
            assertEquals(12, (int) point.getDiffGPS());
            assertEquals(new Date(1456308120935L), point.getDatePassage());

            SyntheseTourneeDaoUtils.delPointTournee(idPointC11);
        }
    }
    
    /**
     * Les numéros de point peuvent contenir une valeur non numérique dans le numéro d'ordre (caracteres 8, 9 et 10)
     * 
     * Entrée: un point en base avec un id_point contenant un numéro de point TA non numérique
     * 
     * Attendu : l'attribut NumeroPointTA vaut NULL
     */
    @Test(groups = { "database-needed", "slow" })
    public void testPointNonPrevu() {
		String idPointC11 = new IdPointC11("FTV", "Sb3", "12M3421062016231245").toString();
		SyntheseTourneeDaoUtils.delPointTournee(idPointC11);
		SyntheseTourneeDaoUtils.insertPoint("COLIS_TEST", "P", "NED STARK", new Date(), "D", null, null, idPointC11, null, null);

		PointTournee point = new PointTournee();
		Mapper.makeMapDeColisPoint(SyntheseTourneeDaoUtils.getPointEnBase(idPointC11), point, null, null, null);

		assertNull(point.getNumPointTA());

		SyntheseTourneeDaoUtils.delPointTournee(idPointC11);
    }
   
    /**
     * Evolution cf. US GT040 : c'est vrai aussi dans les autres cas
     * 
     * Vérifier que la date utilisée est bien celle de l'evt et pas celle de la tournée 
     * lorsque la date tournée est null ou vide
     * 
     *  Entrée : La date tournée est vide et l'outil de saisie ne commence pas pas "PSM"
     * 
     *  Attendu : le point n'est pas vide
     * */
    @Test
    public void testDateTournee_1() {
    	
    	/* init */
    	String idPointC11 = new IdPointC11("FTV", "Sb3", "12M3421062016231245").toString();
		SyntheseTourneeDaoUtils.delPointTournee(idPointC11);
		SyntheseTourneeDaoUtils.insertPoint("COLIS_TEST", "P", "NED STARK", MAINTENANT.toDate(), "D", null, null, idPointC11, null, null);
		PointTournee point = new PointTournee();
		
		/* Execute */
		Mapper.makeMapDeColisPoint(SyntheseTourneeDaoUtils.getPointEnBase(idPointC11), point, null, null,  MAINTENANT.toString("yyyyMMdd"));
		
		/* Check */
	    assertEquals(point.getDatePassage(), MAINTENANT.toDate());
		assertTrue(!point.getColisPresents().isEmpty());
		/* Clean */
		SyntheseTourneeDaoUtils.delPointTournee(idPointC11);
    }
    
    
    /**
     *  Evolution cf. US GT040 : c'est vrai aussi dans les autres cas
     *   
     * Vérifier que la date utilisée est bien celle de l'evt et pas celle de la tournée 
     * lorsque l'outil_saisie débute par "PSF" 
     * 
     *  Entrée : La date tournée n'est pas vide et l'outil de saisie commence pas pas "PSM"
     * 
     *  Attendu : le point n'est pas vide
     * */
    @Test
    public void testDateTournee_2() {
    	
    	/* init */
    	String idPointC11 = new IdPointC11("FTV", "Sb3", "12M3421062016231245").toString();
		SyntheseTourneeDaoUtils.delPointTournee(idPointC11);
		SyntheseTourneeDaoUtils.insertPoint("COLIS_TEST", "P", "NED STARK", MAINTENANT.toDate(), "D", null, null, idPointC11, "PSFxxx", MAINTENANT.toString("yyyyMMdd"));
		PointTournee point = new PointTournee();
		
		/* Execute */
		Mapper.makeMapDeColisPoint(SyntheseTourneeDaoUtils.getPointEnBase(idPointC11), point, null, null,  MAINTENANT.toString("yyyyMMdd"));
		
		/* Check */
	    assertEquals(point.getDatePassage(), MAINTENANT.toDate());
		assertTrue(!point.getColisPresents().isEmpty());
		/* Clean */
		SyntheseTourneeDaoUtils.delPointTournee(idPointC11);
    }
    
    
    
    @Test
    public void test_makeSpecifColis() {
    	getSession().execute(QueryBuilder.truncate(ETableColisSpecifications.TABLE_NAME));
    	// prepare insert
    	final Date date1 = new DateTime().minusHours(1).toDate();
    	final Date date2 = new DateTime().minusHours(2).toDate();
    	final Date date3 = new DateTime().minusHours(3).toDate();
    	final Date date4 = new DateTime().minusHours(4).toDate();
    	final Date date5 = new DateTime().minusHours(5).toDate();
    	final String noLt = "noLt";
    	final Map<Date, String> specifEvts = new HashMap<>();
    	specifEvts.put(date1, "specifEvts");
    	final Map<Date, Set<String>> specifService = new HashMap<>();
    	specifService.put(date2, new HashSet<>(Arrays.asList("specifService")));
    	final Map<Date, String> etapes = new HashMap<>();
    	etapes.put(date3, "etapes");
    	final Map<Date, String> consignesTraitees = new HashMap<>();
    	consignesTraitees.put(date4, "consignesTraitees");
    	final Map<String, String> infoSupp = new HashMap<>();
    	infoSupp.put("info1", "value1");
    	final Map<Date, String> services = new HashMap<>();
    	services.put(date5, "services");
    	// insert colis spec
		getSession().execute(
				psInsertSpecColis.bind(noLt, specifEvts, specifService, etapes, consignesTraitees, infoSupp, services));
		// get colis spec dans une Row
		final ResultSet resultSet = getSession().execute(psGetSpecColis.bind(noLt));
		final SpecifsColis specifColis = Mapper.makeSpecifColis(resultSet.one());
		assertEquals(specifColis.getNoLt(), noLt);
		assertEquals(specifColis.getSpecifsEvt(), specifEvts);
		assertEquals(specifColis.getSpecifsService(), specifService);
		assertEquals(specifColis.getEtapes(), etapes);
		assertEquals(specifColis.getConsignesTraitees(), consignesTraitees);
		assertEquals(specifColis.getInfoSupp(), infoSupp);
		assertEquals(specifColis.getService(), services);
    }

    @AfterClass
    public void tearDownAfterClass() throws Exception {
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }

    /**
     * Efface un point en base
     * 
     * @param idPointC11
     *            id du PointTournee à effacer en base
     */
    private void delPointTournee(String idPointC11) {
        String idTournee = idC11FromIdPointC11(idPointC11);
        getSession().execute(psDelTournee.bind(idTournee));
        getSession().execute(psDelPoint.bind(idPointC11));
    }

    /**
     * Renvoi un idC11 (identifiant tournee) à partir d'un idPointC11
     * (identifiant de point)
     * 
     * @param idPointC11
     * @return
     */
    private static String idC11FromIdPointC11(@NotNull final String idPointC11) {
        return new IdPointC11(idPointC11).getIdC11();
    }
    
    private Session getSession() {
    	return VisionMicroserviceApplication.getCassandraSession();
    }
}
