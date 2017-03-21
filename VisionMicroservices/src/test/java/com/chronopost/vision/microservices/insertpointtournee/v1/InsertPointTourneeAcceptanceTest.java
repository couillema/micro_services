package com.chronopost.vision.microservices.insertpointtournee.v1;

import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildSelect;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.cassandra.table.ETableIdxTourneeJour;
import com.chronopost.cassandra.table.ETableTournee;
import com.chronopost.cassandra.table.ETableTourneePoint;
import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.CacheManagerService;
import com.chronopost.vision.cachemanager.codeservice.CodeServiceDaoImpl;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.Agence;
import com.chronopost.vision.model.CodeService;
import com.chronopost.vision.model.EInfoComp;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.model.rules.EvtRules;
import com.chronopost.vision.model.updatespecificationscolis.v1.EInfoSupplementaire;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.chronopost.vision.transco.transcoder.Transcoder;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.UDTValue;

public class InsertPointTourneeAcceptanceTest extends JerseyTestNg.ContainerPerClassTest {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat FORMAT_YYYYMMDDHHMISS = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * @return VisionMicroserviceApplication.cassandraSession (a
     *         com.datastax.driver.core )
     */
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

    private Client client;
    private boolean suiteLaunch = true;
    private ITranscoderDao mockTranscoderDao = Mockito.mock(ITranscoderDao.class);

    /** PreparedStatement pour récupérer un point */
    private PreparedStatement getOnePoint = null;

    /** PreparedStatement pour récupérer un point */
    private PreparedStatement getOneTournee;

    /** PreparedStatement pour récupérer une tournee depuis l'index */
    private PreparedStatement getOneIdxTournee;

    /** PreparedStatement pour insérer un vrai evt dans la table evt */    
    private PreparedStatement insertEvtIntoEvt;
    /* Les mock des caches */
    private CacheManager<Agence> cacheAgenceMock;
    private CacheManager<Parametre> cacheParametreMock;

    
    /* La map qui simule le cacheAgence */
    private static HashMap<String,Agence> mapRefAgence = new HashMap<>();
    static
    {
    	mapRefAgence.put("75199",new Agence("75199","WKY", "", ""));
    }
    
    /* La map qui simule le cacheParametre */
    private static HashMap<String,Parametre> mapRefParametre = new HashMap<>();
    static
    {
    	mapRefParametre.put("DatePassageIdC11Plus",new Parametre("DatePassageIdC11Plus","20161210"));
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
                e.printStackTrace();
            }
            suiteLaunch = false;
        }
        CCMBridge.ipOfNode(1);

        /* Création de la resource et initialisation avec le service mocké */
        InsertPointTourneeResource resourceInsertPointTourneeResourceTest = new InsertPointTourneeResource();
        resourceInsertPointTourneeResourceTest.setService(InsertPointTourneeServiceImpl.INSTANCE);

        forceSet(TestProperties.CONTAINER_PORT, "0");

        ResourceConfig config = new ResourceConfig();
        config.register(resourceInsertPointTourneeResourceTest);

        return config;
    }

    @SuppressWarnings("unchecked")
	@BeforeClass
    public void setUp() throws Exception {
        super.setUp();

        client = ClientBuilder.newClient();

        /* == Mock de la transcodification == */

        initTransco("|TA|D|P|TE|NA|");

        /* Initialisation du Referentiel des codes service */
        if (CacheManagerService.INSTANCE.getCacheManager("service", CodeService.class)== null){
            CodeServiceDaoImpl.INSTANCE.setSession(VisionMicroserviceApplication.getCassandraSession());
        	CacheManagerService.INSTANCE.addProjet("service", new CacheManager<CodeService>().setDao(CodeServiceDaoImpl.INSTANCE));
        	CacheManagerService.INSTANCE.startUpdater();
    	}
		
        /* Initialisation du Referentiel des agences */
        /* On mock le cacheAgence */
        cacheAgenceMock = Mockito.mock(CacheManager.class) ;
        Mockito.when(cacheAgenceMock.getCache()).thenReturn(mapRefAgence);
        for(String pcAgence: mapRefAgence.keySet())
        	Mockito.when(cacheAgenceMock.getValue(pcAgence)).thenReturn(mapRefAgence.get(pcAgence));
        
        /* On mock le cacheParametre */
        cacheParametreMock = Mockito.mock(CacheManager.class) ;
        Mockito.when(cacheParametreMock.getCache()).thenReturn(mapRefParametre);
        for(String codeParametre: mapRefParametre.keySet())
        	Mockito.when(cacheParametreMock.getValue(codeParametre)).thenReturn(mapRefParametre.get(codeParametre));


        InsertPointTourneeServiceImpl.INSTANCE.setDao(InsertPointTourneeDaoImpl.INSTANCE);
        InsertPointTourneeServiceImpl.INSTANCE.setRefentielAgence(cacheAgenceMock);
        InsertPointTourneeDaoImpl.INSTANCE.setRefentielParametre(cacheParametreMock);
        InsertPointTourneeDaoImpl.INSTANCE.setRefentielCodeService(CacheManagerService.INSTANCE.getCacheManager("service", CodeService.class));
		
        cleanDB();
    }

	private void cleanDB() {
		getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M1200101011971102010'");
        getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M1200201011971102010'");
        getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M1200301011971102010'");
        getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M1200102011971103010'");
        getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M1200202011971103010'");
        getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M1200102011972103010'");
        getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M1200202011972103010'");
        getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M1200502011972103010'");
        getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M1200602011972103010'");
        getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M1200702011972103010'");
        getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M1200709122016103010'");
        getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M1200713122016103010'");
        getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M1200714122016103010'");
        
        getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M1200101012017083010'");
        getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M1200101012017094010'");
        getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M1200102012017083010'");
        getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M1200102012017094010'");
        getSession().execute(
                "DELETE FROM tournee_point WHERE " + ETableTourneePoint.ID_POINT.getNomColonne()
                        + " = 'CGY12M12Sa002012017083010'");

        getSession().execute(
                "DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1201011971102010'");
        getSession().execute(
                "DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1202011971103010'");
        getSession().execute(
                "DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1202011972103010'");
        getSession().execute(
                "DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1209122016103010'");
        getSession().execute(
                "DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = 'CGY12M1209122016103010'");
        getSession().execute(
                "DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = 'CGY12M1213122016103010'");
        getSession().execute(
                "DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = '12M1214122016103010'");
        getSession().execute(
                "DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = 'CGY12M1201012017083010'");
        getSession().execute(
                "DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = 'CGY12M1201012017094010'");
        getSession().execute(
                "DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = 'CGY12M1202012017094010'");
        getSession().execute(
                "DELETE FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = 'CGY12M1202012017083010'");
        
        
        
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne()
                        + " = '00000' AND " + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '19710101' ");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne()
                        + " = '00000' AND " + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '19710102' ");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne()
                        + " = '00000' AND " + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '19720102' ");
        
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne()
                        + " = '75199' AND " + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '19710101' ");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne()
                        + " = '75199' AND " + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '19710102' ");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne()
                        + " = '75199' AND " + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '19720102' ");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne()
                        + " = '75199' AND " + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '20161209' ");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne()
                        + " = '75199' AND " + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '20161213' ");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne()
                        + " = '75199' AND " + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '20161214' ");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne()
                        + " = '75199' AND " + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '20170101' ");
        getSession().execute(
                "DELETE FROM idx_tournee_agence_jour WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne()
                        + " = '75199' AND " + ETableIdxTourneeJour.JOUR.getNomColonne() + " = '20170102' ");

        
        getSession().execute("DELETE FROM evt WHERE no_lt='no_lt0017'");
        

		getOneIdxTournee = getSession().prepare("SELECT " + ETableIdxTourneeJour.ID_TOURNEE.getNomColonne() + " FROM "
				+ ETableIdxTourneeJour.TABLE_NAME + " WHERE " + ETableIdxTourneeJour.AGENCE.getNomColonne()
				+ " = ? AND " + ETableIdxTourneeJour.JOUR.getNomColonne() + " = ?");
		
		insertEvtIntoEvt = getSession().prepare("insert into evt(no_lt,priorite_evt,date_evt,code_evt,date_creation_evt,infoscomp,lieu_evt,ss_code_evt) values(?,?,?,?,?,?,?,?)");

	}

    /**
     * Envoie de 6 evt sur 3 LT differents, mais seul 4 evts sont des evt de
     * livraison
     * 
     * Resultat : On doit avoir un retour positif de la resource, et la présence
     * de deux evt sur deux points en base.
     * 
     */

    @Test(groups = { "slow", "acceptance" })
    public void Test1_evt_non_livraison_non_consideres() throws Exception {
        /* Construction des data du test */
        List<Evt> evts = new ArrayList<>();

        evts.add(createEvt("no_lt1", "TA", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200101011971102010", null),null));
        evts.add(createEvt("no_lt1", "D", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200101011971102010", null),null));

        evts.add(createEvt("no_lt2", "PC", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200201011971102010", null),null));
        evts.add(createEvt("no_lt2", "DC", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200201011971102010", null),null));

        evts.add(createEvt("no_lt3", "TA", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200301011971102010", null),null));
        evts.add(createEvt("no_lt3", "D", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200301011971102010", null),null));

        /* Invocation du MicroService */
        int status = appelMS(evts).getStatus();

        /* Vérifications */
        assertEquals(status, 200);

        /* point 1 */
        Row row = getPointEnBase("CGY12M1200101011971102010");
        assertNotNull(row);

        Set<UDTValue> setEvtTA = getEvtPointByType(row, "TA");
        Set<UDTValue> setEvtD = getEvtPointByType(row, "D");
        Set<UDTValue> setEvtAll = getEvtPoint(row);

        assertEquals(setEvtTA.size(),  1);
        assertEquals (setEvtD.size(),  1);
        assertEquals (setEvtAll.size(),  2);

        /* point 2 */
        row = getPointEnBase("CGY12M1200301011971102010");
        assertNotNull(row);
        setEvtTA = getEvtPointByType(row, "TA");
        setEvtD = getEvtPointByType(row, "D");
        setEvtAll = getEvtPoint(row);
        assertEquals(setEvtTA.size(), 1);
        assertEquals(setEvtD.size(), 1);
        assertEquals(setEvtAll.size(), 2);

        /* il ne doit pas y avoir de 3eme point */
        row = getPointEnBase("CGY12M1200201011971102010");
        assertNull(row);

        /*
         * vérification de la présence de la tournée dans l'index tournee par
         * jour
         */
        String id = getFirstIdTourneesByJour("75199", "19710101");
        assertEquals(id, "12M1201011971102010");
    }

    /**
     * Envoie de 6 evt sur 3 LT differents, mais seul 4 evts sont des evts de
     * livraison, les 2 autres sont des evenements de début et fin de tournée
     * 
     * Resultat : On doit avoir un retour positif de la resource, la présence de
     * deux evts sur deux points en base, et une date de début et de fin de
     * tournée, ainsi que la présence de la tournée dans l'index tournee par
     * jour
     */
    @Test(groups = { "slow", "acceptance" })
    public void Test2_prise_en_compte_evt_debut_fin_tournee() throws Exception {
        /* Construction des data du test */
        List<Evt> evts = new ArrayList<>();

        evts.add(createEvt("no_lt1", "TA", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200102011971103010", null),null));
        evts.add(createEvt("no_lt1", "D", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200102011971103010", null),null));
        evts.add(createEvt("no_lt2", "TA", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200202011971103010", null),null));
        evts.add(createEvt("no_lt2", "D", addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200202011971103010", null),null));
        evts.add(createEvt(EvtRules.COLIS_FICTIF_DEBUT_TOURNEE, "D",
                addInfoComp(EInfoComp.ID_C11, "12M1202011971103010", null),null, 5));
        evts.add(createEvt(EvtRules.COLIS_FICTIF_FIN_TOURNEE, "D",
                addInfoComp(EInfoComp.ID_C11, "12M1202011971103010", null),null, 3));

        Response rep = appelMS(evts);
        int status = rep.getStatus();

        /* Vérifications */
        assertEquals(status, Status.OK.getStatusCode());

        Row row = getPointEnBase("CGY12M1200102011971103010");
        assertNotNull(row);

        Set<UDTValue> setEvtTA = getEvtPointByType(row, "TA");
        Set<UDTValue> setEvtD = getEvtPointByType(row, "D");
        Set<UDTValue> setEvtAll = getEvtPoint(row);

		assertEquals(setEvtTA.size(), 1);
		assertEquals(setEvtD.size(),  1);
		assertEquals(setEvtAll.size(),  2);

		row = getPointEnBase("CGY12M1200202011971103010");
		assertNotNull(row);
		setEvtTA = getEvtPointByType(row, "TA");
		setEvtD = getEvtPointByType(row, "D");
		setEvtAll = getEvtPoint(row);
		assertEquals(setEvtTA.size(),  1);
		assertEquals(setEvtD.size(),  1);
		assertEquals(setEvtAll.size(),  2);

		row = getTourneeEnBase("12M1202011971103010");
		assertNotNull(row);
		Date debut = getDateDebutTournee(row);
		Date fin = getDateFinTournee(row);
		assertEquals(debut.before(fin), true);

        /*
         * vérification de la présence de la tournée dans l'index tournee par
         * jour
         */
        String id = getFirstIdTourneesByJour("75199", "19710102");
        assertEquals(id, "12M1202011971103010");
    }
    
    /**
     * Envoie de 2 evt sur 2 LT differents, mais 1 seule tournee
     * 1 evt P et 1 evt TE avec des infos comp associées
     * 
     * Resultat : On doit avoir deux événements sur lesquels on retourve les
     * info supplémentaires correspondant aux infocomps.
     * 
     * 
     * jour
     */
    @Test(groups = { "slow", "acceptance" })
    public void Test3_traitement_infosupp() throws Exception {
        /* Construction des data du test */
        List<Evt> evts = new ArrayList<>();

        evts.add(createEvt("no_lt4", "P", 
        		addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200102011972103010", 
        		addInfoComp(EInfoComp.AVIS_PASSAGE, "2323", null)
        ),null));
        
        evts.add(createEvt("no_lt5", "TE", 
        		addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200102011972103010", 
           		addInfoComp(EInfoComp.DUREE_APPEL, "123", 
           		addInfoComp(EInfoComp.NUMERO_APPEL, "0611223344",
           		addInfoComp(EInfoComp.RESULTAT_APPEL, "OK",null)
        ))),null));
        
        appelMS(evts);

        /* Vérification des événement */
        Row row = getPointEnBase("CGY12M1200102011972103010");
        assert (row != null);

        Set<UDTValue> setEvtP = getEvtPoint(row);
        assert (setEvtP.size() == 2);
        
        String infoSupp = null;
        int ok = 0;
		for (UDTValue evtPoint : setEvtP) {
			Map<String, String> infosSupp = evtPoint.getMap("info_supp", String.class, String.class);
			assertEquals(evtPoint.getString("date_tournee"), sdf.format(new Date()));
			assertEquals(evtPoint.getString("code_tournee"), "SsCodeEvt");
			infoSupp = infosSupp.get(EInfoSupplementaire.AVIS_PASSAGE.getCode());
			if (infoSupp != null) {
				assertEquals(infoSupp, "2323");
				ok++;
			} else {
				infoSupp = infosSupp.get(EInfoSupplementaire.NUMERO_APPEL.getCode());
				assertEquals(infoSupp, "0611223344");
				infoSupp = infosSupp.get(EInfoSupplementaire.DUREE_APPEL.getCode());
				assertEquals(infoSupp, "123");
				infoSupp = infosSupp.get(EInfoSupplementaire.RESULTAT_APPEL.getCode());
				assertEquals(infoSupp, "OK");
				ok++;
			}
		}
		assertEquals(ok, 2);
    }
    
    /**
     * 
     * RG-MSInsPoint-016 & RG-MSInsPoint-017
     * Envoie de 1 evt TA avec des date de créneau
     * 
     * Resultat : On doit avoir deux infoSupp avec seulement les heures extraites de ces date de créneau
     */
    @Test(groups = { "slow", "acceptance" })
    public void Test4_traitement_dates_creneau() throws Exception {

        /* Construction des data du test */
        List<Evt> evts = new ArrayList<>();

        evts.add(createEvt("no_lt6", "TA", 
        		addInfoComp(EInfoComp.ID_POINT_C11, "CGY12M1200502011972103010", 
        		addInfoComp(EInfoComp.IDBCO_CRENEAU_BORNE_MIN, "02/01/1972 10:30:10", 
        		addInfoComp(EInfoComp.IDBCO_CRENEAU_BORNE_MAX, "02/01/1972 12:30:10",
        				null)
        				
        )),null));
        
        appelMS(evts);

        /* Vérification des événement */
        Row row = getPointEnBase("CGY12M1200502011972103010");
        assert (row != null);

        Set<UDTValue> setEvtP = getEvtPoint(row);
        assert (setEvtP.size() == 1);
        
        String infoSupp = null;
        for(UDTValue evtPoint: setEvtP) {
        	Map<String,String> infosSupp = evtPoint.getMap("info_supp", String.class, String.class);
        	infoSupp = infosSupp.get(EInfoSupplementaire.CRENEAU_DEBUT.getCode());
        	assert("10:30".equals(infoSupp));
        	infoSupp = infosSupp.get(EInfoSupplementaire.CRENEAU_FIN.getCode());
        	assert("12:30".equals(infoSupp));
        }
    }
    
    /**
     * Vérification de l'interprétation des événement du point de vue Tournée.
     * 
     * Envoie de 1 evt NA avec infocomp 15=RF
     * 
     * Resultat : On doit avoir 1 point avec l'evt N1
     */
    @Test(groups = { "acceptance" })
	public void Test5_traduction_evt_NA_en_N1() throws Exception {

		/* Construction des data du test */
		List<Evt> evts = new ArrayList<>();
		String idPoint = "CGY12M1200602011972103010";

		evts.add(createEvt("no_lt0006", "NA",
				addInfoComp(EInfoComp.ID_POINT_C11, idPoint, addInfoComp(EInfoComp.CODE_RAISON, "RF", null)), "PSM01"));

		Response rep = appelMS(evts);
		int status = rep.getStatus();
		// int status = appelMS(evts).getStatus();

		/* Vérifications */
		assertEquals(status, Status.OK.getStatusCode());

		/* Vérification des événement */
		Row row = getPointEnBase(idPoint);
		assertNotNull(row);

		Set<UDTValue> setEvtP = getEvtPoint(row);
		assertEquals(1, setEvtP.size());

		for (UDTValue evtPoint : setEvtP) {
			String codeEvt = evtPoint.getString("type_evt");
			assertEquals(codeEvt, "N1", "Le code événement tournée doit être N1");
		}
	}
    
    /**
     * Vérification de l'interprétation des événement du point de vue Tournée.
     * 
     * Envoie de 1 evt D mais avec un poste comptable non chrono
     * 
     * Resultat : L'evt ne doit pas générer de point
     */
    @Test(groups = { "acceptance" })
    public void Test6_non_consideration_evt_agence_non_CHRONO() throws Exception {
        /* Construction des data du test */
        List<Evt> evts = new ArrayList<>();
        String idPoint = "CGY12M1200702011972103010";

        evts.add(createEvt("no_lt0007", "D", 
        		addInfoComp(EInfoComp.ID_POINT_C11, idPoint,null),"PSM01"));
        /* On modifie le lieu en un code autre qu'une agence chrono */
        evts.get(0).setLieuEvt("00D00");
        
        Response rep = appelMS(evts);
        int status = rep.getStatus();
        // int status = appelMS(evts).getStatus();

        /* Vérifications */
        assertEquals(status, Status.OK.getStatusCode());

        /* Vérification des événement */
        Row row = getPointEnBase(idPoint);
        assertNull(row);
    }
    
    
    /**
     * Entree: Un evt D du 9/12/2016 (avant date passage idC11+)
     * 
     * Sortie: La tournée doit être enregistrée avec l'idC11 et non idC11+
     * 
     * 
     * 
     * @throws Exception
     */
    @Test(groups = { "acceptance" })
    public void Test7_idC11Plus_Avant_Date() throws Exception {
    	/* Construction des data du test */
        List<Evt> evts = new ArrayList<>();
        String idPoint = "CGY12M1200709122016103010";

        /* Activation du FF idC11+ */
        setFeatureFlip("idC11Plus", "true");
        setFeatureFlip("idC11Plus", "true");

        evts.add(createEvt("no_lt0008", "D", 
        		addInfoComp(EInfoComp.ID_POINT_C11, idPoint,null),"PSM01"));
        
        
        /* On modifie le lieu en un code autre qu'une agence chrono */
        
        Response rep = appelMS(evts);
        int status = rep.getStatus();
        // int status = appelMS(evts).getStatus();

        /* Vérifications */
        assertEquals(status, Status.OK.getStatusCode());

        /* Vérification des événement */
        Row row = getTourneeEnBase("CGY12M1209122016103010");
        assertNull(row);
        row = getTourneeEnBase("12M1209122016103010");
        assertNotNull(row);
        
        setFeatureFlip("idC11Plus", "false");
    }
    
    
    /**
     * Entree: Un evt D du 13/12/2016 (apres date passage idC11+) et FF activé
     * 
     * Sortie: La tournée doit être enregistrée avec l'idC11+ et non idC11
     * 
     * 
     * 
     * @throws Exception
     */
    @Test(groups = { "acceptance" })
    public void Test8_idC11Plus_Apres_Date() throws Exception {
    	/* Construction des data du test */
        List<Evt> evts = new ArrayList<>();
        String idPoint = "CGY12M1200713122016103010";

        /* Activation du FF idC11+ */
        setFeatureFlip("idC11Plus", "true");

        
        evts.add(createEvt("no_lt0009", "D", 
        		addInfoComp(EInfoComp.ID_POINT_C11, idPoint,null),"PSM01"));
        
        
        /* On modifie le lieu en un code autre qu'une agence chrono */
        
        Response rep = appelMS(evts);
        int status = rep.getStatus();
        // int status = appelMS(evts).getStatus();

        /* Vérifications */
        assertEquals(status, Status.OK.getStatusCode());

        /* Vérification des événement */
        Row row = getTourneeEnBase("CGY12M1213122016103010");
        assertNotNull(row);
        row = getTourneeEnBase("12M1213122016103010");
        assertNull(row);
        
        setFeatureFlip("idC11Plus", "false");
    }

    
    /**
     * Entree: Un evt D du 13/12/2016 (apres date passage idC11+) et FF désactivé
     * 
     * Sortie: La tournée doit être enregistrée avec l'idC11 et non idC11+
     * 
     * 
     * 
     * @throws Exception
     */
    @Test(groups = { "acceptance" })
    public void Test9_idC11Plus_Apres_Date() throws Exception {
    	/* Construction des data du test */
        List<Evt> evts = new ArrayList<>();
        String idPoint = "CGY12M1200714122016103010";

        /* Activation du FF idC11+ */
        setFeatureFlip("idC11Plus", "false");

        
        evts.add(createEvt("no_lt0010", "D", 
        		addInfoComp(EInfoComp.ID_POINT_C11, idPoint,null),"PSM01"));
        
        
        /* On modifie le lieu en un code autre qu'une agence chrono */
        
        Response rep = appelMS(evts);
        int status = rep.getStatus();
        // int status = appelMS(evts).getStatus();

        /* Vérifications */
        assertEquals(status, Status.OK.getStatusCode());

        /* Vérification des événement */
        Row row = getTourneeEnBase("CGY12M1214122016103010");
        assertNull(row);
        row = getTourneeEnBase("12M1214122016103010");
        assertNotNull(row);
        
        setFeatureFlip("idC11Plus", "false");
    }
    
    
    /**
     * Entree: Un evt D du 01/01/2017 8h50 sans idPointC11. 
     * Il existe déjà 2 tournées une débutant avant l'evt D (8h30), l'autre apres (9h40).
     * 
     * Sortie: L'evt a été rattaché à la tournée du 01/01/2017 8h30 la plus récente et antérieure à l'evt.
     * 
     * 
     * 
     * @throws Exception
     */
    @Test(groups = { "acceptance" })
    public void Test10_computeIdC11() throws Exception {
    	/* Construction des data du test */
        List<Evt> evts = new ArrayList<>();
        String idPoint1 = "CGY12M1200101012017083010";
        String idPoint2 = "CGY12M1200101012017094010";
        /* Activation du FF idC11+ */
        setFeatureFlip("idC11Plus", "true");
        /* on créé 2 tournées sur la même journée : une à  8h30 l'autre à 9h40 (meme code tournee)*/
        evts.add(createEvt("no_lt0011", "TA", 
        		addInfoComp(EInfoComp.ID_POINT_C11, idPoint1,null),"PSM01"));
        evts.add(createEvt("no_lt0012", "TA", 
        		addInfoComp(EInfoComp.ID_POINT_C11, idPoint2,null),"PSM01"));
        /* On enfourne */
        Response rep = appelMS(evts);
        int status = rep.getStatus();

        /* Maintenant on envoi un evt D sans idPointC11 a la date du 1/1/2017 a 8h50 */
        
        evts.clear();
        evts.add(createEvt("no_lt0013", "D",null,"PSM01").setDateEvt(FORMAT_YYYYMMDDHHMISS.parse("20170101085000")));
        
        /* On enfourne */
        rep = appelMS(evts);
        status = rep.getStatus();
        
        
        /* Vérifications */
        assertEquals(status, Status.OK.getStatusCode());

        /* Vérification des événement */
        Row row = getTourneeEnBase("CGY12M1201012017083010");
 
        assert(row.getSet("colis",String.class).contains("no_lt0013"));
        
        setFeatureFlip("idC11Plus", "false");
    }
    
    
    /**
     * Entree: Un evt D du 01/01/2017 a 10h00 sans idPointC11. 
     * Il existe déjà 2 tournées toutes les deux débutant avant l'evt D (8h30 et 9h40).
     * 
     * Sortie: L'evt a été rattaché à la tournée du 01/01/2017 9h40 la plus récente et antérieure à l'evt.
     * 
     * 
     * 
     * @throws Exception
     */
    @Test(groups = { "acceptance" })
    public void Test11_computeIdC11() throws Exception {
    	/* Construction des data du test */
        List<Evt> evts = new ArrayList<>();
        String idPoint1 = "CGY12M1200101012017083010";
        String idPoint2 = "CGY12M1200101012017094010";
        /* Activation du FF idC11+ */
        setFeatureFlip("idC11Plus", "true");
        /* on créé 2 tournées sur la même journée : une à  8h30 l'autre à 9h40 (meme code tournee)*/
        evts.add(createEvt("no_lt0014", "TA", 
        		addInfoComp(EInfoComp.ID_POINT_C11, idPoint1,null),"PSM01"));
        evts.add(createEvt("no_lt0015", "TA", 
        		addInfoComp(EInfoComp.ID_POINT_C11, idPoint2,null),"PSM01"));
        /* On enfourne */
        Response rep = appelMS(evts);
        int status = rep.getStatus();

        /* Maintenant on envoi un evt D sans idPointC11 a la date du 1/1/2017 a 10h00 */
        
        evts.clear();
        evts.add(createEvt("no_lt0016", "D",null,"PSM01").setDateEvt(FORMAT_YYYYMMDDHHMISS.parse("20170101100000")));
        
        /* On enfourne */
        rep = appelMS(evts);
        status = rep.getStatus();
        
        
        /* Vérifications */
        assertEquals(status, Status.OK.getStatusCode());

        /* Vérification des événement */
        Row row = getTourneeEnBase("CGY12M1201012017094010");
 
        assert(row.getSet("colis",String.class).contains("no_lt0016"));
        
        setFeatureFlip("idC11Plus", "false");
    }
    
    
    /**
     * Entree: Un evt D du 02/01/2017 a 10h00 sans idPointC11. 
     * Il existe déjà 2 tournées toutes les deux débutant avant l'evt D (8h30 et 9h40).
     * Et le colis a déjà 2 TA : une sur chaque tournée. Les deux TA ont la meme date d'evt.
     * Mais le dernier TA inséré est celui de la tournée de 8h30
     * 
     * Sortie: L'evt doit être rattaché à la tournée de 8h30 (qui a été insérée plus tard)
     * 
     * @throws Exception
     */
    @Test(groups = { "acceptance" })
    public void Test12_computeIdC11() throws Exception {
    	/* Construction des data du test */
        List<Evt> evts = new ArrayList<>();
        String idPoint1 = "CGY12M1200102012017083010";
        String idPoint2 = "CGY12M1200102012017094010";
        /* Activation du FF idC11+ */
        setFeatureFlip("idC11Plus", "true");
        /* on créé 2 tournées sur la même journée : une à  8h30 l'autre à 9h40 (meme code tournee)*/
        evts.add(createEvt("no_lt0017", "TA", 
        		addInfoComp(EInfoComp.ID_POINT_C11, idPoint1,null),"PSM01"));
        evts.add(createEvt("no_lt0017", "TA", 
        		addInfoComp(EInfoComp.ID_POINT_C11, idPoint2,null),"PSM01"));
        /* On enfourne  dans insertPointTournee*/
        Response rep = appelMS(evts);
        int status = rep.getStatus();
        /* On ajoute les deux evts dans la table evt (le TA de 8h30 en dernier */
        evts.get(0).setDateCreationEvt("2017-01-02T09:50:00");
        evts.get(0).setDateEvt(FORMAT_YYYYMMDDHHMISS.parse("20170102083200"));
        evts.get(0).setPrioriteEvt(1000);
        insertEvtDansTableEvt(evts.get(0));
        evts.get(1).setDateCreationEvt("2017-01-02T09:41:00");
        evts.get(1).setDateEvt(DateRules.addMillisecondes(FORMAT_YYYYMMDDHHMISS.parse("20170102083200"), 5));
        evts.get(1).setPrioriteEvt(1000);
        insertEvtDansTableEvt(evts.get(1));

        /* Maintenant on envoi un evt D sans idPointC11 a la date du 1/1/2017 a 10h00 */
        
        evts.clear();
        evts.add(createEvt("no_lt0017", "D",null,"PSM01").setDateEvt(FORMAT_YYYYMMDDHHMISS.parse("20170102100000")));
        
        /* On enfourne */
        rep = appelMS(evts);
        status = rep.getStatus();
        
        
        /* Vérifications */
        assertEquals(status, Status.OK.getStatusCode());

        /* Vérification que l'evt D est bien dans la tournee de 8h30 
         * Par contre, il doit être sur un autre point que celui de la TA. */
        
        Row row = getTourneeEnBase("CGY12M1202012017083010");
        
        assert(row.getSet("points",String.class).contains("CGY12M12Sa002012017083010"));
        
        setFeatureFlip("idC11Plus", "false");
    }
    
    
    

    /**
     * Effectue un appel au microService TraitementRetard, et retourne la
     * réponse de celui-ci
     * 
     * @param listeRetards
     *            : la liste des retards à fournir au MS.
     * @return : Une réponse du MS.
     */
	private Response appelMS(List<Evt> evts) {
		WebTarget a = client.target("http://localhost:" + getPort());
		WebTarget b = a.path("/InsertPointTournee/v1");
		Builder c = b.request();
		Builder d = c.accept(MediaType.APPLICATION_JSON_TYPE);
		Entity<List<Evt>> f = Entity.entity(evts, MediaType.APPLICATION_JSON);
		Response e = d.post(f);
		return e;
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

    private void initTransco(String evt_point_tournee) throws Exception {
    	ConcurrentMap<String, Transcoder> transcoders = new ConcurrentHashMap<>();
    	Map<String, Map<String, String>> map = new  HashMap<>();

    	Map<String, String> mapParams = new HashMap<>();
    	mapParams.put("evt_point_tournee", evt_point_tournee);
        map.put("parametre_microservices", mapParams);
        
        mapParams = new HashMap<>();
        mapParams.put("idC11Plus", "false" );
        map.put("feature_flips", mapParams);

        Transcoder transcosDiffVision = new Transcoder();
        transcosDiffVision.setTranscodifications(map);
        transcoders.put("DiffusionVision", transcosDiffVision);
        
        Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("DiffusionVision")).thenReturn(map);
        TranscoderService.INSTANCE.setTranscoders(transcoders);
        TranscoderService.INSTANCE.setDao(mockTranscoderDao);
        TranscoderService.INSTANCE.addProjet("DiffusionVision");
        FeatureFlips.INSTANCE.setFlipProjectName("DiffusionVision");
    }

    /**
     * Instanciation d'un evenement (objet evt) avec le code indiqué et le
     * numero lt indiqué
     * 
     * @param noLt
     * @param codeEvt
     * @param infosComp
     * @param createurEvt
     * @return
     *
     * @author LGY
     */
    private Evt createEvt(@NotNull String noLt, @NotNull String codeEvt, Map<String, String> infosComp, String createurEvt) {
        Evt evt = new Evt();
        evt.setNoLt(noLt);
        evt.setDateEvt(new Date());
        evt.setDateTournee(sdf.format(new Date()));
        evt.setCodeEvt(codeEvt);
        evt.setLieuEvt("75199");
        evt.setCreateurEvt(createurEvt);
        evt.setInfoscomp(infosComp);
        evt.setSsCodeEvt("SsCodeEvt");
        return evt;
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
     * @param minutes
     *            : le nombre de minutes à retirer à la date actuelle pour le
     *            champ date_evt
     * @return
     */
    private Evt createEvt(@NotNull String noLt, @NotNull String codeEvt, Map<String, String> infosComp, String createurEvt, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -minutes);

        Evt evt = createEvt(noLt, codeEvt, infosComp, createurEvt);
        evt.setDateEvt(cal.getTime());
        return evt;
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
        if (pInfoComp != null) infosComps.putAll(pInfoComp);

        infosComps.put(key.getCode(), value);
        return infosComps;
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
            if (evt.getString("type_evt").equals(type_evt)) setResult.add(evt);
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
    	return (Set<UDTValue>) row.getSet(ETableTourneePoint.EVENEMENTS.getNomColonne(), UDTValue.class);
    }

    /**
     * Récupère un Point en base
     * 
     * @param idPoint
     *            : identifiant point
     * @return
     */
    private Row getPointEnBase(String idPoint) {
        if (getOnePoint == null)
        /* PrepareStatement pour les test */
        getOnePoint = getSession().prepare(buildSelect(ETableTourneePoint.TABLE_NAME, ETableTourneePoint.ID_POINT).getQuery());

        ResultSet evtResult = getSession().execute(getOnePoint.bind(idPoint));
        if (evtResult != null) return evtResult.one();
        else return null;
    }

    /**
     * Récupère une Tournée en base
     * 
     * @param idTournee
     *            : identifiant de la tournee
     * @return
     */
    private Row getTourneeEnBase(String idTournee) {

        if (getOneTournee == null)
        /* PrepareStatement pour les test */
			getOneTournee = getSession()
					.prepare("SELECT * FROM tournee WHERE " + ETableTournee.ID_TOURNEE.getNomColonne() + " = ?");

        ResultSet evtResult = getSession().execute(getOneTournee.bind(idTournee));
        if (evtResult != null) return evtResult.one();
        else return null;
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
        ResultSet idxTourneeResult = getSession().execute(getOneIdxTournee.bind(agence, jour));
        return idxTourneeResult;
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
        } else return null;
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
        if (res != null) return res.getString(ETableIdxTourneeJour.ID_TOURNEE.getNomColonne());
        else return null;
    }
    
    /**
     * Positionne la valeur désirée sur un FF mocké 
     * @param FeatureFlipName
     * @param value
     */
    private void setFeatureFlip(String FeatureFlipName, String value){
        TranscoderService.INSTANCE
        	.getTranscoder("DiffusionVision")
        	.getTranscodifications().get("feature_flips")
        	.put(FeatureFlipName, value);

    }
    
    private void insertEvtDansTableEvt(Evt evt){
        getSession().execute(
        		insertEvtIntoEvt.bind(
        				evt.getNoLt(),
        				evt.getPrioriteEvt(),
        				evt.getDateEvt(),
        				evt.getCodeEvt(),
        				evt.getDateCreationEvt(),
        				evt.getInfoscomp(),
        				evt.getLieuEvt(),
        				evt.getSsCodeEvt()));
    }
}
