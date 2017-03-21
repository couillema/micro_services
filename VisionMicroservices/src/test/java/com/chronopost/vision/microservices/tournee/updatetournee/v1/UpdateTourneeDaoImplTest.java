package com.chronopost.vision.microservices.tournee.updatetournee.v1;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

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

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.utils.TypeBorneCreneau;
import com.chronopost.vision.model.EInfoComp;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.model.rules.EvtRules;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.chronopost.vision.transco.transcoder.Transcoder;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/** @author unknown : JJC getSession +  LOGGER import min.**/
public class UpdateTourneeDaoImplTest {
	
    private boolean suiteLaunch = true;
    private Date dateEvt;
    private HashMap<String, String> infoscomp;
	private PreparedStatement psCleanColisTourneeAgence;
	private PreparedStatement psCleanAgenceTournee;
	private PreparedStatement psCleanLtCreneauAgence;
	private PreparedStatement psCleanInfoTournee;
	private PreparedStatement psCleanTourneeParCodeService;
	private PreparedStatement psCleanTourneeC11;
	private PreparedStatement psCleanTournees;

	/**
	 *  @return  VisionMicroserviceApplication.cassandraSession (a com.datastax.driver.core)
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
        
        psCleanColisTourneeAgence = getSession().prepare("DELETE FROM colis_tournee_agence where numero_lt = 'EECOLTOUAGEFR'");
        psCleanAgenceTournee = getSession().prepare("DELETE FROM agence_tournee where date_jour = ?");
        psCleanLtCreneauAgence = getSession().prepare("DELETE FROM lt_avec_creneau_par_agence where date_jour = ? AND code_agence = ?");
        psCleanInfoTournee  = getSession().prepare("DELETE FROM info_tournee where code_tournee = ?");
        psCleanTourneeParCodeService = getSession().prepare("DELETE FROM tournees_par_code_service where date_jour = ? and code_service = ?");
        psCleanTourneeC11 = getSession().prepare("DELETE FROM tournee_c11 where code_tournee_c11 = ?");
        psCleanTournees = getSession().prepare("DELETE FROM tournees where date_jour = ?");
        
        dateEvt = new Date();
        infoscomp = new HashMap<>();
        infoscomp.put(EInfoComp.LONGITUDE.getCode(), "0.717483333333333");
        infoscomp.put(EInfoComp.LATITUDE.getCode(), "49.5332866666667");
        infoscomp.put(EInfoComp.ID_POINT_C11.getCode(), "AJA20A0100208092015065959");
        infoscomp.put(EInfoComp.IDBCO_CRENEAU_BORNE_MIN.getCode(), "10/12/2015 08:00");
        infoscomp.put(EInfoComp.IDBCO_CRENEAU_BORNE_MAX.getCode(), "10/12/2015 10:00");
        infoscomp.put(EInfoComp.CHAUFFEUR.getCode(), "Jason Statham");
        
        ConcurrentMap<String, Transcoder> transcoders = new ConcurrentHashMap<>();
    	Map<String, Map<String, String>> map = new  HashMap<>();
    	Map<String, String> mapAgence = new HashMap<>();
    	mapAgence.put("31999", "TLS");
    	mapAgence.put("20999", "AJA");
        map.put("code_agence_trigramme", mapAgence);
        Transcoder transcoderAgence = new Transcoder();
        transcoderAgence.setTranscodifications(map);
        transcoders.put("Aladin", transcoderAgence);

        ITranscoderDao mockTranscoderDao = Mockito.mock(ITranscoderDao.class);
        Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("Aladin")).thenReturn(map);

		TranscoderService.INSTANCE.setTranscoders(transcoders);
        TranscoderService.INSTANCE.setDao(mockTranscoderDao);
        TranscoderService.INSTANCE.addProjet("Aladin");
    }

    @Test(groups = "slow")
    public void transcoderTest() {
        assertEquals(TranscoderService.INSTANCE.getTranscoder("Aladin").transcode("code_agence_trigramme", "20999"), "AJA");
        assertEquals(TranscoderService.INSTANCE.getTranscoder("Aladin").transcode("code_agence_trigramme", "31999"), "TLS");
    }

    @Test(groups = "slow")
    public void insertAgenceTournee() throws ParseException {
        // insertion d'un événement porteur d'un id_c11 et vérification de la
        // création dans agence_tournee d'un enregistrement sur cette
        // agence/tournee pour le jour de la date de l'evt
        Evt evtTa = new Evt().setNoLt("EEINSAGETOUFR").setDateEvt(dateEvt).setPrioriteEvt(1).setCodeEvt("TA").setInfoscomp(infoscomp);
        assertTrue(UpdateTourneeDaoImpl.INSTANCE.insertAgenceTournee(Arrays.asList(evtTa)));

		ResultSet result = getSession().execute("select * from agence_tournee where code_agence = 'AJA' and code_tournee = '20A01' and date_jour = '" + DateRules.toDateSortable(dateEvt) + "'");
        Row agenceTourneeRow = result.one();

        assertEquals(agenceTourneeRow.getString("code_agence"), "AJA");
        assertEquals(agenceTourneeRow.getString("code_tournee"), "20A01");
        assertEquals(agenceTourneeRow.getString("date_jour"), new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }

    @Test(groups = "slow")
    public void insertColisTourneeAgence() throws ParseException {
        Evt evtTa = new Evt().setNoLt("EECOLTOUAGEFR").setDateEvt(dateEvt).setPrioriteEvt(1).setCodeEvt("TA").setInfoscomp(infoscomp);
        assertTrue(UpdateTourneeDaoImpl.INSTANCE.insertColisTourneeAgence(Arrays.asList(evtTa)));

        ResultSet result = getSession().execute("select * from colis_tournee_agence where numero_lt = 'EECOLTOUAGEFR' limit 1");
        Row row = result.one();

        assertEquals(row.getString("id_tournee"), "AJA20A01");
        assertEquals(row.getString("id_c11"), "AJA20A0100208092015065959");
        assertEquals(row.getTimestamp("date_maj"), dateEvt);
    }

    @Test(groups = "slow")
    public void insertInfoTournee() throws ParseException {
        Evt evtD = new Evt().setNoLt("EE00INFTOURFR").setDateEvt(dateEvt).setPrioriteEvt(1).setCodeEvt("D").setInfoscomp(infoscomp);
        assertTrue(UpdateTourneeDaoImpl.INSTANCE.insertInfoTournee(Arrays.asList(evtD)));

		ResultSet result = getSession().execute("select * from info_tournee where code_tournee = '" + EvtRules.getCodeAgence(evtD) + EvtRules.getCodeTournee(evtD) + "' limit 1");
        Row row = result.one();

        assertEquals(row.getString("code_tournee"), "AJA20A01");
        assertEquals(row.getString("type_information"), "evt");
        assertEquals(row.getString("id_information"), "EE00INFTOURFR");
        Map<String, String> informations = row.getMap("informations", String.class, String.class);
        assertEquals(informations.get("code_evt"), "D");
        assertEquals(informations.get("id_c11"), "AJA20A0100208092015065959");
        assertEquals(informations.get("latitude"), "49.5332866666667");
        assertEquals(informations.get("longitude"), "0.717483333333333");
    }

    @Test(groups = "slow")
    public void insertTourneeC11() throws ParseException {
        Evt evtTa = new Evt().setNoLt("EE000TOUC11FR").setDateEvt(dateEvt).setPrioriteEvt(1).setCodeEvt("TA").setInfoscomp(infoscomp);
        assertTrue(UpdateTourneeDaoImpl.INSTANCE.insertTourneeC11(Arrays.asList(evtTa)));

		ResultSet result = getSession().execute("select * from tournee_c11 where code_tournee_c11 = '" + EvtRules.getIdC11SansCodeAgence(evtTa) + "' limit 1");
        Row agenceTourneeRow = result.one();

        assertEquals(agenceTourneeRow.getString("code_tournee_agence"), "AJA20A01");
        assertEquals(agenceTourneeRow.getString("code_tournee_c11"), EvtRules.getIdC11SansCodeAgence(evtTa));
        assertEquals(agenceTourneeRow.getTimestamp("date_maj"), dateEvt);
    }

    @Test(groups = "slow")
    public void updateTournee() throws ParseException {
        Evt evtTa = new Evt().setNoLt("EEUPDTOURTAFR").setDateEvt(dateEvt).setPrioriteEvt(1).setCodeEvt("TA").setInfoscomp(infoscomp);
        assertTrue(UpdateTourneeDaoImpl.INSTANCE.updateTournee(Arrays.asList(evtTa)));

		ResultSet result = getSession().execute("select ta from tournees where code_tournee = 'AJA20A01' and date_jour = '" + DateRules.toDateSortable(dateEvt) + "'");
        Row tourneeTa = result.one();

        assertEquals(1, tourneeTa.getSet("ta", String.class).size());
        for (String noLt : tourneeTa.getSet("ta", String.class)) {
            assertEquals(noLt, "EEUPDTOURTAFR");
        }

		// Vérification de l'indexation des lt ayant un créneau de livraison
		result = getSession().execute("select no_lt from lt_avec_creneau_par_agence where date_jour = '" + DateRules.toDateSortable(dateEvt) + "' and code_agence = 'AJA' and type_borne_livraison = '" + TypeBorneCreneau.BORNE_SUP.getTypeBorne() + "' and borne_livraison >= '" + DateRules.toDateSortable(dateEvt) + " 09:30' and borne_livraison <= '" + DateRules.toDateSortable(dateEvt) + " 10:00'");
		Row creneauMaxTa = result.one();
		
		assertEquals(creneauMaxTa.getString("no_lt"),"EEUPDTOURTAFR");		
		
		result = getSession().execute("select no_lt from lt_avec_creneau_par_agence where date_jour = '" + DateRules.toDateSortable(dateEvt) + "' and code_agence = 'AJA' and type_borne_livraison = '" + TypeBorneCreneau.BORNE_INF.getTypeBorne() + "' and borne_livraison >= '" + DateRules.toDateSortable(dateEvt) + " 08:00' and borne_livraison <= '" + DateRules.toDateSortable(dateEvt) + " 08:30'");
        Row creneauMinTa = result.one();

        assertEquals(creneauMinTa.getString("no_lt"), "EEUPDTOURTAFR");

        Evt evtD = new Evt().setNoLt("EEUPDTOURD0FR").setDateEvt(dateEvt).setPrioriteEvt(1).setCodeEvt("D").setInfoscomp(infoscomp);
        assertTrue(UpdateTourneeDaoImpl.INSTANCE.updateTournee(Arrays.asList(evtD)));

		result = getSession().execute("select distri from tournees where code_tournee = 'AJA20A01' and date_jour = '" + DateRules.toDateSortable(dateEvt) + "'");
        Row tourneeD = result.one();

        assertEquals(tourneeD.getSet("distri", String.class).size(), 1);
        for (String noLt : tourneeD.getSet("distri", String.class)) {
            assertEquals(noLt, "EEUPDTOURD0FR");
        }

        Evt evtCollecte = new Evt().setNoLt("EEUPDTOURCOFR").setDateEvt(dateEvt).setPrioriteEvt(1).setCodeEvt("PC").setInfoscomp(infoscomp);
        assertTrue(UpdateTourneeDaoImpl.INSTANCE.updateTournee(Arrays.asList(evtCollecte)));

		result = getSession().execute("select collecte from tournees where code_tournee = 'AJA20A01' and date_jour = '" + DateRules.toDateSortable(dateEvt) + "'");
        Row tourneeCollecte = result.one();

        assertEquals(tourneeCollecte.getSet("collecte", String.class).size(), 1);
        for (String noLt : tourneeCollecte.getSet("collecte", String.class)) {
            assertEquals(noLt, "EEUPDTOURCOFR");
        }
    }
	
	/**
     * Test sur la détection et le marquage des débuts de tournée.
     * 
     * @throws ParseException
     */
    @Test
    public void debutTourneeTest() throws ParseException {
        assertEquals(TranscoderService.INSTANCE.getTranscoder("Aladin").transcode("code_agence_trigramme", "20999"), "AJA");
        assertEquals(TranscoderService.INSTANCE.getTranscoder("Aladin").transcode("code_agence_trigramme", "31999"), "TLS");
		Evt evtDebTournee = new Evt().setNoLt(EvtRules.COLIS_FICTIF_DEBUT_TOURNEE).setDateEvt(dateEvt).setLieuEvt("20999").setSsCodeEvt("20A01").setPrioriteEvt(1).setCodeEvt("TA").setInfoscomp(infoscomp);
		assertTrue(UpdateTourneeDaoImpl.INSTANCE.updateTournee(Arrays.asList(evtDebTournee)));

		ResultSet result = getSession().execute("select ta, informations from tournees where code_tournee = 'AJA20A01' and date_jour = '" + DateRules.toDateSortable(dateEvt) + "'");
        Row tourneeDebut = result.one();

        if (tourneeDebut.getSet("ta", String.class) != null) {
            for (String noLt : tourneeDebut.getSet("ta", String.class)) {
                assertNotEquals(noLt, EvtRules.COLIS_FICTIF_DEBUT_TOURNEE);
            }
        }

        Map<String, String> informations = tourneeDebut.getMap("informations", String.class, String.class);
        assertNotNull(informations.get("debut"));
        assertEquals(informations.get("debut"), DateRules.toDateAndTimeSortable(evtDebTournee.getDateEvt()));
    }

    /**
     * Test sur la détection et le marquage du nom du chauffer au début d'une tournée.
     * 
     * @throws ParseException
     */
    @Test
    public void chauffeurTourneeTest() throws ParseException{
        assertEquals(TranscoderService.INSTANCE.getTranscoder("Aladin").transcode("code_agence_trigramme", "20999"),"AJA");
        assertEquals(TranscoderService.INSTANCE.getTranscoder("Aladin").transcode("code_agence_trigramme", "31999"),"TLS");
        Evt evtDebTournee = new Evt().setNoLt(EvtRules.COLIS_FICTIF_DEBUT_TOURNEE).setDateEvt(dateEvt).setLieuEvt("20999").setSsCodeEvt("20A01").setPrioriteEvt(1).setCodeEvt("TA").setInfoscomp(infoscomp);
        Evt evtFinTournee = new Evt().setNoLt(EvtRules.COLIS_FICTIF_FIN_TOURNEE).setDateEvt(dateEvt).setLieuEvt("20999").setSsCodeEvt("20A01").setPrioriteEvt(1).setCodeEvt("D").setInfoscomp(infoscomp);
        assertTrue(UpdateTourneeDaoImpl.INSTANCE.updateTournee(Arrays.asList(evtDebTournee)));
        assertTrue(UpdateTourneeDaoImpl.INSTANCE.updateTournee(Arrays.asList(evtFinTournee)));
        
        ResultSet result = getSession().execute("select ta, informations from tournees where code_tournee = 'AJA20A01' and date_jour = '" + DateRules.toDateSortable(dateEvt) + "'");

		for (Row tournee : result.all()) {
			if (tournee.getSet("ta", String.class) != null) {
				for (String noLt : tournee.getSet("ta", String.class)) {
					assertNotEquals(noLt, EvtRules.COLIS_FICTIF_DEBUT_TOURNEE);
				}
				Map<String, String> informations = tournee.getMap("informations", String.class, String.class);
				assertNotNull(informations.get("chauffeur"));
				assertEquals(informations.get("chauffeur"), "Jason Statham");
			} else {
				Map<String, String> informations = tournee.getMap("informations", String.class, String.class);
				assertNull(informations.get("chauffeur"));
			}
		}
    }
	
	/**
	 * Test sur la détection et le marquage des fins de tournée.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void finTourneeTest() throws ParseException{
		Evt evtFinTournee = new Evt().setNoLt(EvtRules.COLIS_FICTIF_FIN_TOURNEE).setDateEvt(dateEvt).setLieuEvt("20999").setSsCodeEvt("20A01").setPrioriteEvt(1).setCodeEvt("D").setInfoscomp(infoscomp);
		assertTrue(UpdateTourneeDaoImpl.INSTANCE.updateTournee(Arrays.asList(evtFinTournee)));
		
		ResultSet result = getSession().execute("select distri, informations from tournees where code_tournee = 'AJA20A01' and date_jour = '" + DateRules.toDateSortable(dateEvt) + "'");
		Row tourneeFin = result.one();
		if(tourneeFin.getSet("distri", String.class)!=null){
			for(String noLt:tourneeFin.getSet("distri", String.class)){
				assertNotEquals(noLt, EvtRules.COLIS_FICTIF_FIN_TOURNEE);
			}
		}
		
		Map<String, String> informations = tourneeFin.getMap("informations", String.class, String.class);
		assertNotNull(informations.get("fin"));
		assertEquals(informations.get("fin"), DateRules.toDateAndTimeSortable(evtFinTournee.getDateEvt()));
	}
	
	@Test(groups = "slow")
	public void tourneeParCodeServiceTest() throws ParseException {
		Evt evtTa = new Evt().setNoLt("EE000TOUSRVFR").setDateEvt(dateEvt).setPrioriteEvt(1).setCodeService("931").setCodeEvt("TA").setInfoscomp(infoscomp);
		assertTrue(UpdateTourneeDaoImpl.INSTANCE.updateTourneeCodeService(Arrays.asList(evtTa)));
		
		ResultSet result = getSession().execute("select * from tournees_par_code_service where date_jour = '" + DateRules.toDateSortable(dateEvt) + "' and code_service = '931' limit 1");
		
		Row tourneeParCodeServiceRow = result.one();
		
		assertEquals(tourneeParCodeServiceRow.getString("code_tournee"),"AJA20A01");
	}
	
	@AfterClass
	public void tearDownAfterClass() throws Exception {
		getSession().execute(psCleanColisTourneeAgence.getQueryString());
		getSession().execute(psCleanAgenceTournee.bind(DateRules.toDateSortable(dateEvt)));
		getSession().execute(psCleanLtCreneauAgence.bind("2016-12-02", "AJA"));
		getSession().execute(psCleanInfoTournee.bind("AJA20A01"));
		getSession().execute(psCleanTourneeParCodeService.bind(DateRules.toDateSortable(dateEvt), "931"));
		getSession().execute(psCleanTourneeC11.bind("20A0108092015065959"));
		getSession().execute(psCleanTournees.bind(DateRules.toDateSortable(dateEvt)));
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
	}
}
