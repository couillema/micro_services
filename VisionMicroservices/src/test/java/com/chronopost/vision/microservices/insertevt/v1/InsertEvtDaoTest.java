package com.chronopost.vision.microservices.insertevt.v1;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.cassandra.table.ETableEvt;
import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.chronopost.vision.transco.transcoder.Transcoder;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;

public class InsertEvtDaoTest {

    private boolean suiteLaunch = true;
    private IInsertEvtDao evtDao;
	private PreparedStatement psCleanEvt;
	private PreparedStatement psCleanEvtCounters;
	private PreparedStatement psSelectEvt;
	private String jour = null;
	private String heure = null;
	private String minute = null;

    /**
     * @return VisionMicroserviceApplication.cassandraSession (a
     *         com.datastax.driver.core )
     */
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

    @BeforeClass
    public void setUp() throws Exception {
        if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }
        CCMBridge.ipOfNode(1);
        
        psCleanEvt = getSession().prepare(QueryBuilder.truncate(ETableEvt.TABLE_NAME));
        psCleanEvtCounters = getSession().prepare(QueryBuilder.truncate("evt_counters"));
		psSelectEvt = getSession().prepare("SELECT * FROM evt WHERE no_lt = ?");
		
		// clean des données avant tests
    	getSession().execute(psCleanEvt.getQueryString());
    	getSession().execute(psCleanEvtCounters.getQueryString());

        evtDao = InsertEvtDaoImpl.getInstance();
        
        // set CacheManager<Parametre> avec données pour 'filtreColisFictifs'
        @SuppressWarnings("unchecked")
		final CacheManager<Parametre> cacheParametre = Mockito.mock(CacheManager.class);
        final StringBuilder buildRegExp = new StringBuilder();
        buildRegExp.append("[\"\\\\b00000000000000\",");
        buildRegExp.append("\"\\\\bNO_READ\",");
        buildRegExp.append("\"CM91000000001\",");
        buildRegExp.append("\"\\\\bDD(.{5})0000(.{2})\",");
        buildRegExp.append("\"\\\\bFD(.{5})0000(.{2})\",");
        buildRegExp.append("\"\\\\bDC(.{5})0000(.{4})\",");
        buildRegExp.append("\"\\\\bFC(.{5})0000(.{4})\"]");
        final Parametre parametre = new Parametre("filtreColisFictifs", buildRegExp.toString());
		when(cacheParametre.getValue("filtreColisFictifs")).thenReturn(parametre);
		evtDao.setRefentielParametre(cacheParametre);
    }
    
    @Test(groups = { "database-needed", "slow" })
    public void insertEvts() throws ParseException {
    	mockTranscos(false);
        Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("CM91000000001").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TO")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

        Evt evt2 = new Evt().setCabEvtSaisi("cab_evt_saisi").setCabRecu("cab_recu").setCodeEvt("code_evt")
                .setCodeEvtExt("code_evt_ext").setCodePostalEvt("code_postal_evt").setCodeRaisonEvt("code_raison_evt")
                .setCodeService("code_service").setCreateurEvt("createur_evt").setDateCreationEvt("date_creation_evt")
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setIdAccesClient(1).setIdExtractionEvt("id_extraction_evt").setIdSsCodeEvt(1).setIdbcoEvt(1)
                .setInfoscomp(new HashMap<String, String>()).setLibelleEvt("libelle_evt")
                .setLibelleLieuEvt("libelle_lieu_evt").setLieuEvt("lieu_evt").setNoLt("DDaBcDe0000a1")
                .setPositionEvt(1).setPrioriteEvt(1).setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setRefIdAbonnement("ref_id_abonnement").setSsCodeEvt("ss_code_evt")
                .setStatusEnvoi("status_envoi").setStatusEvt("status_evt");
        
        // colis fictif
        Evt evt3 = new Evt().setCabEvtSaisi("cab_evt_saisi").setCabRecu("cab_recu").setCodeEvt("code_evt")
                .setCodeEvtExt("code_evt_ext").setCodePostalEvt("code_postal_evt").setCodeRaisonEvt("code_raison_evt")
                .setCodeService("code_service").setCreateurEvt("createur_evt").setDateCreationEvt("date_creation_evt")
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setIdAccesClient(1).setIdExtractionEvt("id_extraction_evt").setIdSsCodeEvt(1).setIdbcoEvt(1)
                .setInfoscomp(new HashMap<String, String>()).setLibelleEvt("libelle_evt")
                .setLibelleLieuEvt("libelle_lieu_evt").setLieuEvt("lieu_evt").setNoLt("YYY0000EVT2FR")
                .setPositionEvt(1).setPrioriteEvt(1).setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setRefIdAbonnement("ref_id_abonnement").setSsCodeEvt("ss_code_evt")
                .setStatusEnvoi("status_envoi").setStatusEvt("status_evt");
        
        // colis fictif
        Evt evt4 = new Evt().setCabEvtSaisi("cab_evt_saisi").setCabRecu("cab_recu").setCodeEvt("code_evt")
                .setCodeEvtExt("code_evt_ext").setCodePostalEvt("code_postal_evt").setCodeRaisonEvt("code_raison_evt")
                .setCodeService("code_service").setCreateurEvt("createur_evt").setDateCreationEvt("date_creation_evt")
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setIdAccesClient(1).setIdExtractionEvt("id_extraction_evt").setIdSsCodeEvt(1).setIdbcoEvt(1)
                .setInfoscomp(new HashMap<String, String>()).setLibelleEvt("libelle_evt")
                .setLibelleLieuEvt("libelle_lieu_evt").setLieuEvt("lieu_evt").setNoLt("1234567")
                .setPositionEvt(1).setPrioriteEvt(1).setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setRefIdAbonnement("ref_id_abonnement").setSsCodeEvt("ss_code_evt")
                .setStatusEnvoi("status_envoi").setStatusEvt("status_evt");

        evtDao.insertEvts(Arrays.asList(evt1, evt2, evt3, evt4));

        ResultSet evtResult = getSession().execute(psSelectEvt.bind("CM91000000001"));

        Row row = evtResult.one();
        assertNotNull(row);

        assertEquals(row.getTimestamp("date_evt"), new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"));
        assertEquals(row.getString("no_lt"), "CM91000000001");
        assertEquals(row.getString("cab_recu"), "%0020090NA146848396248899250");
        assertEquals(row.getString("code_evt"), "TO");
        assertEquals(row.getString("createur_evt"), "TRI1");
        assertEquals(row.getInt("id_acces_client"), 0);
        assertEquals(row.getString("id_extraction_evt"), "717493191");
        assertEquals(row.getInt("idbco_evt"), 88);
        assertEquals(row.getString("libelle_evt"), "Envoi en transit");
        assertEquals(row.getString("lieu_evt"), "93999");
        assertEquals(row.getInt("position_evt"), 0);
        assertEquals(row.getString("ref_id_abonnement"), "EVT_CHR");
        assertEquals(row.getString("ss_code_evt"), "AJA0");
        assertEquals(row.getString("status_evt"), "Acheminement en cours");
        assertEquals(row.getMap("infoscomp", String.class, String.class), new HashMap<String, String>());
        assertEquals(row.getString("code_postal_evt"), "13999");
        assertEquals(row.getString("cab_evt_saisi"), "cab_evt_saisi");
        assertEquals(row.getString("code_evt_ext"), "toto");
        assertEquals(row.getString("code_raison_evt"), "code_raison_evt");
        assertEquals(row.getString("code_service"), "code_service");
        assertEquals(row.getInt("id_ss_code_evt"), 1);
        assertEquals(row.getString("libelle_lieu_evt"), "libelle_lieu_evt");
        assertEquals(row.getInt("prod_cab_evt_saisi"), 1);
        assertEquals(row.getInt("prod_no_lt"), 1);
        assertEquals(row.getString("ref_extraction"), "ref_extraction");
        assertEquals(row.getString("status_envoi"), "status_envoi");

        // récupération du 2nd événement
        evtResult = getSession().execute(psSelectEvt.bind("DDaBcDe0000a1"));

        row = evtResult.one();
        assertNotNull(row);

        assertEquals(row.getString("cab_evt_saisi"), "cab_evt_saisi");
        assertEquals(row.getString("cab_recu"), "cab_recu");
        assertEquals(row.getString("code_evt"), "code_evt");
        assertEquals(row.getString("code_evt_ext"), "code_evt_ext");
        assertEquals(row.getString("code_postal_evt"), "code_postal_evt");
        assertEquals(row.getString("code_raison_evt"), "code_raison_evt");
        assertEquals(row.getString("code_service"), "code_service");
        assertEquals(row.getString("createur_evt"), "createur_evt");
        assertEquals(row.getTimestamp("date_evt"), new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"));
        assertEquals(row.getInt("id_acces_client"), 1);
        assertEquals(row.getString("id_extraction_evt"), "id_extraction_evt");
        assertEquals(row.getInt("id_ss_code_evt"), 1);
        assertEquals(row.getInt("idbco_evt"), 1);
        assertEquals(row.getMap("infoscomp", String.class, String.class), new HashMap<String, String>());
        assertEquals(row.getString("libelle_evt"), "libelle_evt");
        assertEquals(row.getString("libelle_lieu_evt"), "libelle_lieu_evt");
        assertEquals(row.getString("lieu_evt"), "lieu_evt");
        assertEquals(row.getString("no_lt"), "DDaBcDe0000a1");
        assertEquals(row.getInt("position_evt"), 1);
        assertEquals(row.getInt("priorite_evt"), 1);
        assertEquals(row.getInt("prod_cab_evt_saisi"), 1);
        assertEquals(row.getInt("prod_no_lt"), 1);
        assertEquals(row.getString("ref_extraction"), "ref_extraction");
        assertEquals(row.getString("ref_id_abonnement"), "ref_id_abonnement");
        assertEquals(row.getString("ss_code_evt"), "ss_code_evt");
        assertEquals(row.getString("status_envoi"), "status_envoi");
        assertEquals(row.getString("status_evt"), "status_evt");
        
        // s'assure que les colis fictifs ne sont pas en base
        evtResult = getSession().execute(psSelectEvt.bind("YYY0000EVT2FR"));
        row = evtResult.one();
        assertNull(row);
        
        // s'assure que les colis fictifs ne sont pas en base
        evtResult = getSession().execute(psSelectEvt.bind("1234567"));
        row = evtResult.one();
        assertNull(row);
    }
    
    @Test(groups = { "database-needed", "slow" })
    public void insertEvts_avecColisFictifs() throws ParseException {
    	mockTranscos(true);
    	// num colis ok
        Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("EE00000EVT2FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TO")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

    	// num colis fictif
        Evt evt2 = new Evt().setCabEvtSaisi("cab_evt_saisi").setCabRecu("cab_recu").setCodeEvt("code_evt")
                .setCodeEvtExt("code_evt_ext").setCodePostalEvt("code_postal_evt").setCodeRaisonEvt("code_raison_evt")
                .setCodeService("code_service").setCreateurEvt("createur_evt").setDateCreationEvt("date_creation_evt")
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setIdAccesClient(1).setIdExtractionEvt("id_extraction_evt").setIdSsCodeEvt(1).setIdbcoEvt(1)
                .setInfoscomp(new HashMap<String, String>()).setLibelleEvt("libelle_evt")
                .setLibelleLieuEvt("libelle_lieu_evt").setLieuEvt("lieu_evt").setNoLt("00000000000000U")
                .setPositionEvt(1).setPrioriteEvt(1).setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setRefIdAbonnement("ref_id_abonnement").setSsCodeEvt("ss_code_evt")
                .setStatusEnvoi("status_envoi").setStatusEvt("status_evt");

    	// num colis fictif
        Evt evt3 = new Evt().setCabEvtSaisi("cab_evt_saisi").setCabRecu("cab_recu").setCodeEvt("code_evt")
                .setCodeEvtExt("code_evt_ext").setCodePostalEvt("code_postal_evt").setCodeRaisonEvt("code_raison_evt")
                .setCodeService("code_service").setCreateurEvt("createur_evt").setDateCreationEvt("date_creation_evt")
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setIdAccesClient(1).setIdExtractionEvt("id_extraction_evt").setIdSsCodeEvt(1).setIdbcoEvt(1)
                .setInfoscomp(new HashMap<String, String>()).setLibelleEvt("libelle_evt")
                .setLibelleLieuEvt("libelle_lieu_evt").setLieuEvt("lieu_evt").setNoLt("NO_READ")
                .setPositionEvt(1).setPrioriteEvt(1).setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setRefIdAbonnement("ref_id_abonnement").setSsCodeEvt("ss_code_evt")
                .setStatusEnvoi("status_envoi").setStatusEvt("status_evt");

    	// num colis fictif
        Evt evt4 = new Evt().setCabEvtSaisi("cab_evt_saisi").setCabRecu("cab_recu").setCodeEvt("code_evt")
                .setCodeEvtExt("code_evt_ext").setCodePostalEvt("code_postal_evt").setCodeRaisonEvt("code_raison_evt")
                .setCodeService("code_service").setCreateurEvt("createur_evt").setDateCreationEvt("date_creation_evt")
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setIdAccesClient(1).setIdExtractionEvt("id_extraction_evt").setIdSsCodeEvt(1).setIdbcoEvt(1)
                .setInfoscomp(new HashMap<String, String>()).setLibelleEvt("libelle_evt")
                .setLibelleLieuEvt("libelle_lieu_evt").setLieuEvt("lieu_evt").setNoLt("CM91000000002")
                .setPositionEvt(1).setPrioriteEvt(1).setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setRefIdAbonnement("ref_id_abonnement").setSsCodeEvt("ss_code_evt")
                .setStatusEnvoi("status_envoi").setStatusEvt("status_evt");

        evtDao.insertEvts(Arrays.asList(evt1, evt2, evt3, evt4));

        ResultSet evtResult = getSession().execute(psSelectEvt.bind("EE00000EVT2FR"));

        Row row = evtResult.one();
        assertNotNull(row);

        assertEquals(row.getTimestamp("date_evt"), new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"));
        assertEquals(row.getString("no_lt"), "EE00000EVT2FR");
        assertEquals(row.getString("cab_recu"), "%0020090NA146848396248899250");
        assertEquals(row.getString("code_evt"), "TO");
        assertEquals(row.getString("createur_evt"), "TRI1");
        assertEquals(row.getInt("id_acces_client"), 0);
        assertEquals(row.getString("id_extraction_evt"), "717493191");
        assertEquals(row.getInt("idbco_evt"), 88);
        assertEquals(row.getString("libelle_evt"), "Envoi en transit");
        assertEquals(row.getString("lieu_evt"), "93999");
        assertEquals(row.getInt("position_evt"), 0);
        assertEquals(row.getString("ref_id_abonnement"), "EVT_CHR");
        assertEquals(row.getString("ss_code_evt"), "AJA0");
        assertEquals(row.getString("status_evt"), "Acheminement en cours");
        assertEquals(row.getMap("infoscomp", String.class, String.class), new HashMap<String, String>());
        assertEquals(row.getString("code_postal_evt"), "13999");
        assertEquals(row.getString("cab_evt_saisi"), "cab_evt_saisi");
        assertEquals(row.getString("code_evt_ext"), "toto");
        assertEquals(row.getString("code_raison_evt"), "code_raison_evt");
        assertEquals(row.getString("code_service"), "code_service");
        assertEquals(row.getInt("id_ss_code_evt"), 1);
        assertEquals(row.getString("libelle_lieu_evt"), "libelle_lieu_evt");
        assertEquals(row.getInt("prod_cab_evt_saisi"), 1);
        assertEquals(row.getInt("prod_no_lt"), 1);
        assertEquals(row.getString("ref_extraction"), "ref_extraction");
        assertEquals(row.getString("status_envoi"), "status_envoi");

        // s'assure que les colis fictifs ne sont pas en base
        evtResult = getSession().execute(psSelectEvt.bind("00000000000000U"));
        row = evtResult.one();
        assertNull(row);
        
        evtResult = getSession().execute(psSelectEvt.bind("NO_READ"));
        row = evtResult.one();
        assertNull(row);
        
        evtResult = getSession().execute(psSelectEvt.bind("CM91000000002"));
        row = evtResult.one();
        assertNotNull(row);
    }

    @Test(groups = { "database-needed", "slow" })
    public void getPrioriteEvtTest() throws Exception {
    	mockTranscos(false);
        Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("EE00000EVT1FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TO")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

        Integer priorite = evtDao.getPrioriteEvt(evt1);

        assertEquals((Integer) priorite, (Integer) 2000);

        // transco evt inexistante, on garde la priorité de base
        Evt evt2 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("EE00000EVT1FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TO")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(90).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

        priorite = evtDao.getPrioriteEvt(evt2);

        assertEquals((Integer) priorite, (Integer) 146);

    }

    @Test(groups = { "database-needed", "slow" })
    public void test_insertDiffEvtCounter() {
    	mockTranscos(false);
    	// GIVEN
    	final SimpleDateFormat FORMAT_JOUR_HEURE_MINUTE = new SimpleDateFormat("yyyMMddHHmm");
    	final String date = FORMAT_JOUR_HEURE_MINUTE.format(new Date());
    	jour = date.substring(0, 8);
    	heure = date.substring(8, 10);
    	minute = date.substring(10, 11);
    	
    	// WHEN
    	evtDao.insertDiffEvtCounter(15);
    	
    	// THEN
    	final String requestGet = new StringBuilder().append("SELECT evt_diffuses, hit_evt_diffuses FROM evt_counters WHERE jour = '").append(jour)
    			.append("' and heure = '").append(heure).append("' and minute = '").append(minute).append("'").toString();
    	final Row row = getSession().execute(requestGet).one();
		assertNotNull(row);
		assertEquals(row.getLong("evt_diffuses"), 15L);
		assertEquals(row.getLong("hit_evt_diffuses"), 1L);
    	
    	// WHEN
    	evtDao.insertDiffEvtCounter(10);
    	
    	// THEN
    	final Row row2 = getSession().execute(requestGet).one();
		assertNotNull(row2);
		assertEquals(row2.getLong("evt_diffuses"), 25L);
		assertEquals(row2.getLong("hit_evt_diffuses"), 2L);
    }
    
	/**
	 * A appeler au début de chaque méthode
	 * Prepare le mock des transco Le paramétre permet d'activer ou non le
	 * feature flip 'FiltreColisFictifs' pour chaque test
	 * 
	 * @param enableFiltreColisFictifs
	 */
    private void mockTranscos(final Boolean enableFiltreColisFictifs) {
        // Préparation du mock des transcodifications
        ConcurrentMap<String, Transcoder> transcoders = new ConcurrentHashMap<>();
    	Map<String, Map<String, String>> map = new  HashMap<>();
    	Map<String, String> mapEvt = new HashMap<>();
        mapEvt.put("88", "TO|2000");
        mapEvt.put("89", "TA|1000");
        map.put("evenements", mapEvt);
        Transcoder transcoderDiffVision = new Transcoder();
        transcoderDiffVision.setTranscodifications(map);
        transcoders.put("DiffusionVision", transcoderDiffVision);

    	Map<String, String> mapFlips = new HashMap<>();
        mapFlips.put("FiltreColisFictifs", enableFiltreColisFictifs.toString());
        map.put("feature_flips", mapFlips);
        Transcoder transcoderVision = new Transcoder();
        transcoderVision.setTranscodifications(map);
        transcoders.put("Vision", transcoderVision);
		TranscoderService.INSTANCE.setTranscoders(transcoders);

	    final ITranscoderDao mockTranscoderDao = Mockito.mock(ITranscoderDao.class);
        Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("DiffusionVision")).thenReturn(map);
		Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("Vision")).thenReturn(map);

        TranscoderService.INSTANCE.setDao(mockTranscoderDao);
        TranscoderService.INSTANCE.addProjet("DiffusionVision");
        FeatureFlips.INSTANCE.setFlipProjectName("Vision");
    }

    @AfterClass
    public void tearDownAfterClass() throws Exception {
    	getSession().execute(psCleanEvt.getQueryString());
    	getSession().execute(psCleanEvtCounters.getQueryString());
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }
}
