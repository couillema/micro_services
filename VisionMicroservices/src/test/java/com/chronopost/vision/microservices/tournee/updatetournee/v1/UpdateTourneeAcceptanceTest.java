package com.chronopost.vision.microservices.tournee.updatetournee.v1;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.EInfoComp;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.rules.EvtRules;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.chronopost.vision.transco.transcoder.Transcoder;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/** @author unknown JJC add getSession() remove unsued random function **/
public class UpdateTourneeAcceptanceTest {
	
    private boolean suiteLaunch = true;
	private PreparedStatement psCleanColisTourneeAgence;
	private PreparedStatement psCleanAgenceTournee;
	private PreparedStatement psCleanInfoTournee;
	private PreparedStatement psCleanLtCreneauAgence;
	private PreparedStatement psCleanTourneeParCodeService;
	private PreparedStatement psCleanTournees;
	private PreparedStatement psCleanTourneeC11;
	
	private final ITranscoderDao mockTranscoderDao = Mockito.mock(ITranscoderDao.class);
	
    /**
     * @return VisionMicroserviceApplication.cassandraSession (a
     *         com.datastax.driver.core )
     */
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

    @BeforeClass(groups = { "init" })
    public void setUp() throws Exception {
        if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }
        
		psCleanColisTourneeAgence = getSession().prepare(
				"DELETE FROM colis_tournee_agence where numero_lt in ('EEUPDTOUR01FR','EEUPDTOUR02FR','EEUPDTOUR03FR')");
        psCleanAgenceTournee = getSession().prepare("DELETE FROM agence_tournee where date_jour = '2015-10-01'");
        psCleanInfoTournee  = getSession().prepare("DELETE FROM info_tournee where code_tournee = ?");
        psCleanLtCreneauAgence = getSession().prepare("DELETE FROM lt_avec_creneau_par_agence where date_jour = ? AND code_agence = ?");
        psCleanTourneeParCodeService = getSession().prepare("DELETE FROM tournees_par_code_service where date_jour = ? and code_service = ?");
        psCleanTournees = getSession().prepare("DELETE FROM tournees where date_jour = ? and code_tournee = ?");
        psCleanTourneeC11 = getSession().prepare("DELETE FROM tournee_c11 where code_tournee_c11 = ?");

        Map<String, String> infoscomp = new HashMap<>();
        infoscomp.put(EInfoComp.LATITUDE.getCode(), "4.55555");
        infoscomp.put(EInfoComp.LONGITUDE.getCode(), "5.66666");
        infoscomp.put(EInfoComp.ETA.getCode(), "12:00");
        infoscomp.put(EInfoComp.IDBCO_CRENEAU_BORNE_MIN.getCode(), "10/12/2015 08:00");
        infoscomp.put(EInfoComp.IDBCO_CRENEAU_BORNE_MAX.getCode(), "10/12/2015 10:00");
        infoscomp.put(EInfoComp.ID_POINT_C11.getCode(), "AJA00M0100101102015101010");
        infoscomp.put(EInfoComp.CHAUFFEUR.getCode(), "LePilote");
        
        
        // Préparation du mock des transcodifications
        // Transcos pour codes Evt
        ConcurrentMap<String, Transcoder> transcoders = new ConcurrentHashMap<>();
    	Map<String, Map<String, String>> map = new  HashMap<>();
    	Map<String, String> mapAgence = new HashMap<>();
    	mapAgence.put("31999", "TLS");
    	mapAgence.put("20999", "AJA");
        map.put("code_agence_trigramme", mapAgence);
        Transcoder transcoderVision = new Transcoder();
        transcoderVision.setTranscodifications(map);
        transcoders.put("Aladin", transcoderVision);
		TranscoderService.INSTANCE.setTranscoders(transcoders);
    	
        Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("Aladin")).thenReturn(map);

        TranscoderService.INSTANCE.setDao(mockTranscoderDao);
        TranscoderService.INSTANCE.addProjet("Aladin");

        Evt evtTa = new Evt().setPrioriteEvt(146).setDateEvt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-10-01 07:00:00"))
                .setNoLt("EEUPDTOUR01FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TA").setCreateurEvt("TRI1")
                .setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
                .setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("988").setIdSsCodeEvt(1).setLibelleLieuEvt("libelle_lieu_evt")
                .setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

        Evt evtDebut = new Evt().setPrioriteEvt(146).setDateEvt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-10-01 07:00:00"))
                .setNoLt(EvtRules.COLIS_FICTIF_DEBUT_TOURNEE).setCabRecu("%0020090NA146848396248899250").setCodeEvt("TA").setCreateurEvt("TRI1")
                .setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
                .setLibelleEvt("Envoi en transit").setLieuEvt("31999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("31U13")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("31999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("988").setIdSsCodeEvt(1).setLibelleLieuEvt("libelle_lieu_evt")
                .setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

        Evt evtD = new Evt().setPrioriteEvt(146).setDateEvt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-10-01 09:00:00"))
                .setNoLt("EEUPDTOUR02FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("D").setCreateurEvt("TRI1")
                .setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
                .setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction")
                .setStatusEnvoi("status_envoi");

        Evt evtPc = new Evt().setPrioriteEvt(146).setDateEvt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-10-01 09:10:00"))
                .setNoLt("EEUPDTOUR03FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("PC").setCreateurEvt("TRI1")
                .setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
                .setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction")
                .setStatusEnvoi("status_envoi");

        Evt evtFin = new Evt().setPrioriteEvt(146).setDateEvt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-10-01 15:00:00"))
                .setNoLt(EvtRules.COLIS_FICTIF_FIN_TOURNEE).setCabRecu("%0020090NA146848396248899250").setCodeEvt("TA").setCreateurEvt("TRI1")
                .setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
                .setLibelleEvt("Envoi en transit").setLieuEvt("31999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("31U13")
                .setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
                .setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction")
                .setStatusEnvoi("status_envoi");

        UpdateTourneeServiceImpl.INSTANCE.setDao(UpdateTourneeDaoImpl.INSTANCE);
        UpdateTourneeServiceImpl.INSTANCE.updateTournee(Arrays.asList(evtTa, evtD, evtPc, evtDebut, evtFin));
    }

    @Test(groups = { "slow", "acceptance" })
    public void cas1Test1() throws IOException, InterruptedException, ExecutionException, TimeoutException, ParseException {
        // vérification des enregistrements de la table tournee
        ResultSet resultSet = getSession().execute("select * from tournees where code_tournee='AJA00M01' and date_jour = '2015-10-01'");
        Row row = resultSet.one();

        assertEquals(row.getSet("ta", String.class).size(), 1);
        assertEquals(row.getSet("distri", String.class).size(), 1);
        assertEquals(row.getSet("collecte", String.class).size(), 1);
        Set<String> distriSet = row.getSet("distri", String.class);
        Iterator<String> distriIterator = distriSet.iterator();
        while (distriIterator.hasNext()) {
            String distriLt = distriIterator.next();
            assertTrue(distriLt.equals("EEUPDTOUR02FR"));
        }

        Set<String> taSet = row.getSet("ta", String.class);
        Iterator<String> taIterator = taSet.iterator();
        while (taIterator.hasNext()) {
            String taLt = taIterator.next();
            assertTrue(taLt.equals("EEUPDTOUR01FR"));
        }

        Set<String> collecteSet = row.getSet("collecte", String.class);
        Iterator<String> collecteIterator = collecteSet.iterator();
        while (collecteIterator.hasNext()) {
            String collecteLt = collecteIterator.next();
            assertTrue(collecteLt.equals("EEUPDTOUR03FR"));
        }

    }

    @Test(groups = { "slow", "acceptance" })
    public void cas1Test2() throws IOException, InterruptedException, ExecutionException, TimeoutException, ParseException {
        // vérification des enregistrements de la table agence_tournee
        ResultSet resultSet2 = getSession().execute("select * from agence_tournee where code_agence='AJA' and date_jour = '2015-10-01'");
        Row row2 = resultSet2.one();

        assertEquals(row2.getString("code_tournee"), "00M01");

    }

    @Test(groups = { "slow", "acceptance" })
    public void cas1Test3() throws IOException, InterruptedException, ExecutionException, TimeoutException, ParseException {

        // vérification des enregistrements de la table colis_tournee_agence
        ResultSet resultSet3 = getSession().execute("select * from colis_tournee_agence where numero_lt = 'EEUPDTOUR01FR'");
        Row row3 = resultSet3.one();

        assertEquals(row3.getString("id_tournee"), "AJA00M01");

        ResultSet resultSet4 = getSession().execute("select * from colis_tournee_agence where numero_lt = 'EEUPDTOUR02FR'");
        Row row4 = resultSet4.one();

        assertEquals(row4.getString("id_tournee"), "AJA00M01");

        ResultSet resultSet5 = getSession().execute("select * from colis_tournee_agence where numero_lt = 'EEUPDTOUR03FR'");
        Row row5 = resultSet5.one();

        assertEquals(row5.getString("id_tournee"), "AJA00M01");
    }

    @Test(groups = { "slow", "acceptance" })
    public void cas1Test4() throws IOException, InterruptedException, ExecutionException, TimeoutException, ParseException {
        // vérification des enregistrements de la table info_tournee
        ResultSet resultSet = getSession()
                .execute(
                        "select * from info_tournee where code_tournee = 'AJA00M01' and type_information = 'evt' and date_heure_transmission <= '2015-10-01 10:00:00'");

        int i = 0;
        for (Row row : resultSet) {
            if (i == 0) {
                assertEquals(row.getString("id_information"), "EEUPDTOUR03FR");
            } else if (i == 1) {
                assertEquals(row.getString("id_information"), "EEUPDTOUR02FR");
            } else if (i == 2) {
                assertEquals(row.getString("id_information"), "EEUPDTOUR01FR");
            }
            ++i;
        }

        assertEquals(i, 3);
    }

    /**
     * Vérification dans le champ information de la date de debut et de fin de
     * tournee et du chauffeur.
     */
    @Test(groups = { "slow", "acceptance" })
    public void cas1Test5() {
        ResultSet resultSet = getSession().execute("select * from tournees where code_tournee='TLS31U13' and date_jour = '2015-10-01'");
        Row row = resultSet.one();

        assertEquals(row.getMap("informations", String.class, String.class).get("debut"), "2015-10-01 07:00:00");
        assertEquals(row.getMap("informations", String.class, String.class).get("fin"), "2015-10-01 15:00:00");
        assertEquals(row.getMap("informations", String.class, String.class).get("chauffeur"), "LePilote");
    }

    /**
     * Vérification de la table d'index des tournées par codeService.
     */
    @Test(groups = { "slow", "acceptance" })
    public void cas1Test6() {
        ResultSet result = getSession().execute("select * from tournees_par_code_service where date_jour = '2015-10-01' and code_service = '988' limit 1");

        Row tourneeParCodeServiceRow = result.one();

        assertEquals(tourneeParCodeServiceRow.getString("code_tournee"), "AJA00M01");
    }

    /**
     * Vérification de l'indexation des colis à risque (avec infocomp CRENEAU
     * MIN et CRENEAU MAX).
     */
    @Test(groups = { "slow", "acceptance" })
    public void cas2Test1() {
        ResultSet result = getSession()
                .execute(
                        "select * from lt_avec_creneau_par_agence where date_jour = '2015-10-01' and code_agence = 'AJA' and type_borne_livraison = 'max' and borne_livraison >= '2015-10-01 09:30' and borne_livraison <= '2015-10-01 10:00'");

        Row ltAvecCreneauMax = result.one();

        assertEquals(ltAvecCreneauMax.getString("no_lt"), "EEUPDTOUR01FR");

        result = getSession()
                .execute(
                        "select * from lt_avec_creneau_par_agence where date_jour = '2015-10-01' and code_agence = 'AJA' and type_borne_livraison = 'min' and borne_livraison >= '2015-10-01 07:50' and borne_livraison <= '2015-10-01 08:10'");

        Row ltAvecCreneauMin = result.one();

        assertEquals(ltAvecCreneauMin.getString("no_lt"), "EEUPDTOUR01FR");
    }
    
    @AfterClass
    public void tearDownAfterClass() throws Exception {
		getSession().execute(psCleanColisTourneeAgence.getQueryString());
		getSession().execute(psCleanAgenceTournee.getQueryString());
		getSession().execute(psCleanInfoTournee.bind("AJA00M01"));
		getSession().execute(psCleanLtCreneauAgence.bind("2015-10-01", "AJA"));
		getSession().execute(psCleanTourneeParCodeService.bind("2015-10-01", "931"));
		getSession().execute(psCleanTourneeParCodeService.bind("2015-10-01", "988"));
		getSession().execute(psCleanTournees.bind("2015-10-01", "TLS31U13"));
		getSession().execute(psCleanTournees.bind("2015-10-01", "AJA00M01"));
		getSession().execute(psCleanTourneeC11.bind("00M0101102015101010"));
		
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }
}
