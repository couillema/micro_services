package com.chronopost.vision.microservices.getsyntheseagence.v1;

import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.LIVRAISON;
import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.PREPA_DISTRI;
import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.RETOUR_AGENCE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.joda.time.DateTime;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.cassandra.table.ETableColisAgence;
import com.chronopost.cassandra.table.ETableColisSpecifications;
import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.getsyntheseagence.v1.ECodeIndicateur;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseColisEtListeValeurs;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseDistributionQuantite;
import com.chronopost.vision.ut.RandomUts;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.google.common.collect.Sets;

public class GetSyntheseAgenceDistributionAcceptanceTest extends JerseyTestNg.ContainerPerClassTest {

	private static final Logger log = LoggerFactory.getLogger(GetSyntheseAgenceDistributionAcceptanceTest.class);
	private static final String AGENCE = "99999";
	private static final DateTime MAINTENANT = DateTime.now();
	private static final String JOUR = MAINTENANT.toString("yyyyMMdd");
	private static final String HEURE = MAINTENANT.toString("HH");
	private static final String MINUTE = MAINTENANT.toString("mm").substring(0, 1);
	private static final String HEURE_APPEL = "2017-03-16T01:00:00-05:00";
	/* Port utilisé par notre resource */
	private int httpPort;
	private boolean suiteLaunch = true;

	/** PreparedStatement pour insérer un colis saisi dans l'agence */
	private PreparedStatement psInsertColisSaisi;
	/** PreparedStatement pour insérer un specif colis / MAJ le champs etapes */
	private PreparedStatement psUpdateColisSpecifEtapes;
	/** PreparedStatement pour vider les colis saisi et a saisir sur l'agence */
	private PreparedStatement psTruncateColisAgence;
	/** PreparedStatement pour vider les colis spécifications */
	private PreparedStatement psTruncateColisSpec;

	/**
	 * @return VisionMicroserviceApplication.cassandraSession (a
	 *         com.datastax.driver.core )
	 */
	private static final Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
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
		SyntheseAgenceResource resourceSyntheseAgenceResourceTest = new SyntheseAgenceResource();
		resourceSyntheseAgenceResourceTest.setService(SyntheseAgenceServiceImpl.INSTANCE);

		httpPort = RandomUts.getRandomHttpPort();
		System.out.println("Valeur du port=" + httpPort);
		forceSet(TestProperties.CONTAINER_PORT, httpPort + "");

		ResourceConfig config = new ResourceConfig();
		config.register(resourceSyntheseAgenceResourceTest);

		return config;
	}

	@SuppressWarnings("unchecked")
	@BeforeClass
	public void setUp() throws Exception {
		log.debug("before class Valeur du port={}", httpPort);

		super.setUp();

		SyntheseAgenceServiceImpl.INSTANCE.setDao(SyntheseAgenceDaoImpl.INSTANCE);

		final CacheManager<Parametre> mockParametre = Mockito.mock(CacheManager.class);

		final Parametre distriDomicileParam = new Parametre();
		distriDomicileParam.setValue("[\"RG\", \"B\", \"D\", \"D1\", \"D2\", \"D3\", \"D4\", \"D5\"]");
		Mockito.when(mockParametre.getValue(Mockito.eq("evt_distri_domicile_positif"))).thenReturn(distriDomicileParam);

		final Parametre instanceParam = new Parametre();
		instanceParam.setValue("[\"IP\", \"RB\"]");
		Mockito.when(mockParametre.getValue(Mockito.eq("evt_instance"))).thenReturn(instanceParam);

		final Parametre distriNegatifParam = new Parametre();
		distriNegatifParam.setValue("[\"P\", \"RC\", \"PR\", \"CO\", \"PA\", \"NA\", \"N1\", \"N2\", \"P1\", \"P2\"]");
		Mockito.when(mockParametre.getValue(Mockito.eq("evt_distri_negatif"))).thenReturn(distriNegatifParam);

		SyntheseAgenceServiceImpl.INSTANCE.setRefentielParametre(mockParametre);

		psInsertColisSaisi = getSession().prepare("update colis_agence set colis_saisis = colis_saisis + ? " + " WHERE "
				+ ETableColisAgence.POSTE_COMPTABLE.getNomColonne() + " = ? " + "AND    "
				+ ETableColisAgence.JOUR.getNomColonne() + " = ?" + "AND    " + ETableColisAgence.HEURE.getNomColonne()
				+ " = ?" + "AND    " + ETableColisAgence.MINUTE.getNomColonne() + " = ?");

		psUpdateColisSpecifEtapes = getSession().prepare(
				"update " + ETableColisSpecifications.TABLE_NAME + " set " + ETableColisSpecifications.ETAPES
						+ "[?] = ?  WHERE " + ETableColisSpecifications.NO_LT.getNomColonne() + " = ?");
		psTruncateColisAgence = getSession().prepare("truncate colis_agence");
		psTruncateColisSpec = getSession().prepare("truncate colis_specifications");
	}

	@Test
	public void test_1_DistriQuantite_distribues() {
		newColisAgence("idDistri_1");
		updateColisSpecifEtapes("idDistri_1", LIVRAISON.getCode() + "|RG||||99999", MAINTENANT);

		final SyntheseDistributionQuantite distribQuantite = appelMSDistribQuantite(MAINTENANT.toString());

		assertEquals(distribQuantite.getNbColisDistribues().intValue(), 1);
	}
	
	@Test
    /**  Entrée : 1 colis  dernière étape est une étape de LIVRAISON sur un evt de distri domicile RG
     * 
     *  Attendu : 1 colis distribué
     * */
	public void test_1_DistriDetail_distribues() {
		/* init */
		String idColis1 = "idDistri_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, LIVRAISON.getCode() + "|RG||||99999", MAINTENANT);

		/* Execute */
		final SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.DISTRIBUES.getCode(), 1500, 0);

		/* Check */
		assertEquals(result.getColis().size(), 1);
		assertEquals(result.getTotalColis(),1);
		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt());
		assertTrue(noLtList.contains(idColis1));
	}
	
	@Test
	public void test_2_DistriQuantite_distribues_avecDeloc() {
		newColisAgence("idDistri_1");
		updateColisSpecifEtapes("idDistri_1", LIVRAISON.getCode() + "|RG||||99999", MAINTENANT);
		newColisAgence("idDistri_2");
		updateColisSpecifEtapes("idDistri_2", LIVRAISON.getCode() + "|RG|XXUXX|||99999", MAINTENANT);

		final SyntheseDistributionQuantite distribQuantite = appelMSDistribQuantite(MAINTENANT.toString());

		assertEquals(distribQuantite.getNbColisDistribues().intValue(), 1);
		assertEquals(distribQuantite.getNbColisDistribuesDeloc().intValue(), 1);
	}

	@Test
    /**  Entrée : 1er colis  dernière étape est une étape de LIVRAISON sur un evt de distri domicile RG
     * 			  2ème colis délocalisé dernière étape est une étape de LIVRAISON sur un evt de distri domicile RG
     * 
     *  Attendu : 2 ème colis  (colis distribué déloc)
     * */
	public void test_2_DistriDetail_distribues_avecDeloc() {
		/* init */
		String idColis1 = "idDistri_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, LIVRAISON.getCode() + "|RG||||99999", MAINTENANT);
		String idColis2 = "idDistri_2";
		newColisAgence(idColis2);
		updateColisSpecifEtapes(idColis2, LIVRAISON.getCode() + "|RG|XXUXX|||99999", MAINTENANT);

		/* Execute */
		final SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.DISTRIBUES_DELOC.getCode(), 1500, 0);

		/* Check */
		assertEquals(result.getColis().size(), 1);
		assertEquals(result.getTotalColis(),1);
		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt());
		assertTrue(noLtList.contains(idColis2));
	}
	
	
	@Test
	public void test_3_DistriQuantite_avecEchec() {
		newColisAgence("idDistri_1");
		updateColisSpecifEtapes("idDistri_1", LIVRAISON.getCode() + "|RC||||99999", MAINTENANT);

		final SyntheseDistributionQuantite distribQuantite = appelMSDistribQuantite(MAINTENANT.toString());

		assertEquals(distribQuantite.getNbColisAvecEchec().intValue(), 1);
	}

	@Test
    /**  Entrée : 1 colis  une étape LIVRAISON d'échec de livraison avec un evenement RC
     * 
     *  Attendu : 1 colis avec échec
     * */
	public void test_3_DistriDetail_avecEchec() {
		/* init */
		String idColis1 = "idDistri_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, LIVRAISON.getCode() + "|RC||||99999", MAINTENANT);

		/* Execute */
		final SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.AVEC_ECHEC.getCode(), 1500, 0);

		/* Check */
		assertEquals(result.getColis().size(), 1);
		assertEquals(result.getTotalColis(),1);
		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt());
		assertTrue(noLtList.contains(idColis1));
	}
	
	
	@Test
	public void test_3_DistriQuantite_avecEchec_avecEnInstance_1() {
		newColisAgence("idDistri_1");
		updateColisSpecifEtapes("idDistri_1", LIVRAISON.getCode() + "|RC||||99999", MAINTENANT.minusHours(1));
		updateColisSpecifEtapes("idDistri_1", LIVRAISON.getCode() + "|RB||||99999", MAINTENANT);

		final SyntheseDistributionQuantite distribQuantite = appelMSDistribQuantite(MAINTENANT.toString());

		assertEquals(distribQuantite.getNbColisAvecEchec().intValue(), 0);
	}
	
	@Test
    /**  Entrée : 1 colis  une étape LIVRAISON d'échec de livraison avec un evenement RC à  maintenant - une heure 
     * 						Et une étape LIVRAISON d'échec de livraison avec un evenement RB à maintenant
     * 
     *  Attendu : 0 colis avec échec
     * */
	public void test_3_DistriDetail_avecEchec_avecEnInstance_1() {
		/* init */
		String idColis1 = "idDistri_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, LIVRAISON.getCode() + "|RC||||99999", MAINTENANT.minusHours(1));
		updateColisSpecifEtapes(idColis1, LIVRAISON.getCode() + "|RB||||99999", MAINTENANT);

		/* Execute */
		final SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.AVEC_ECHEC.getCode(), 1500, 0);

		/* Check */
		assertEquals(result.getColis().size(), 0);
		assertEquals(result.getTotalColis(),0);
	}
	

	@Test
	public void test_3_DistriQuantite_avecEchec_avecEnInstance_2() {
		newColisAgence("idDistri_1");
		updateColisSpecifEtapes("idDistri_1", LIVRAISON.getCode() + "|RC||||99999", MAINTENANT);
		updateColisSpecifEtapes("idDistri_1", LIVRAISON.getCode() + "|RB||||99999", MAINTENANT.minusHours(1));

		final SyntheseDistributionQuantite distribQuantite = appelMSDistribQuantite(MAINTENANT.toString());

		assertEquals(distribQuantite.getNbColisAvecEchec().intValue(), 1);
	}

	@Test
    /**  Entrée : 1 colis  une étape LIVRAISON d'échec de livraison avec un evenement RC à maintenant 
     * 						Et une étape LIVRAISON d'échec de livraison avec un evenement RB maintenant - une heure 
     * 
     *  Attendu : 1 colis avec échec
     * */
	public void test_3_DistriDetail_avecEchec_avecEnInstance_2() {
		/* init */
		String idColis1 = "idDistri_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, LIVRAISON.getCode() + "|RC||||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis1, LIVRAISON.getCode() + "|RB||||99999", MAINTENANT.minusHours(1));

		/* Execute */
		final SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.AVEC_ECHEC.getCode(), 1500, 0);

		/* Check */
		assertEquals(result.getColis().size(), 1);
		assertEquals(result.getTotalColis(),1);
		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt());
		assertTrue(noLtList.contains(idColis1));
	}
	
	@Test
	public void test_3_DistriQuantite_avecEchec_avecDeloc() {
		newColisAgence("idDistri_1");
		updateColisSpecifEtapes("idDistri_1", LIVRAISON.getCode() + "|RC||||99999", MAINTENANT);
		newColisAgence("idDistri_2");
		updateColisSpecifEtapes("idDistri_2", LIVRAISON.getCode() + "|RC|XXUXX|||99999", MAINTENANT);

		final SyntheseDistributionQuantite distribQuantite = appelMSDistribQuantite(MAINTENANT.toString());

		assertEquals(distribQuantite.getNbColisAvecEchec().intValue(), 1);
		assertEquals(distribQuantite.getNbColisAvecEchecDeloc().intValue(), 1);
	}

	@Test
    /**  Entrée : 1 colis  une étape LIVRAISON d'échec de livraison avec un evenement RC  
     * 			  1 colis délocalisé une étape LIVRAISON d'échec de livraison avec un evenement RC
     * 
     *  Attendu : 2 ème colis  (colis avec échec deloc)
     * */
	public void test_3_DistriDetail_avecEchec_avecDelo() {
		
		/* init */
		String idColis1 = "idDistri_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, LIVRAISON.getCode() + "|RC||||99999", MAINTENANT);
		String idColis2 = "idDistri_2";
		newColisAgence(idColis2);
		updateColisSpecifEtapes(idColis2, LIVRAISON.getCode() + "|RC|XXUXX|||99999", MAINTENANT);

		/* Execute */
		final SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.AVEC_ECHEC_DELOC.getCode(), 1500, 0);

		/* Check */
		assertEquals(result.getColis().size(), 1);
		assertEquals(result.getTotalColis(),1);
		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt());
		assertTrue(noLtList.contains(idColis2));
	}
	
	@Test
	public void test_4_DistriQuantite_instance() {
		newColisAgence("idDistri_1");
		updateColisSpecifEtapes("idDistri_1", LIVRAISON.getCode() + "|RB||||99999", MAINTENANT);

		final SyntheseDistributionQuantite distribQuantite = appelMSDistribQuantite(MAINTENANT.toString());

		assertEquals(distribQuantite.getNbColisInstance().intValue(), 1);
	}
	
	@Test
    /**  Entrée : 1 colis  la derniere étape est une LIVRAISON de mise en instance avec un evt RB
     * 
     *  Attendu : 1 colis instance
     * */
	public void test_4_DistriDetail_instance() {
		
		/* init */
		String idColis1 = "idDistri_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, LIVRAISON.getCode() + "|RB||||99999", MAINTENANT);

		/* Execute */
		final SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.INSTANCE.getCode(), 1500, 0);

		/* Check */
		assertEquals(result.getColis().size(), 1);
		assertEquals(result.getTotalColis(),1);
		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt());
		assertTrue(noLtList.contains(idColis1));
	}

	@Test
	public void test_4_DistriQuantite_instance_avecDeloc() {
		newColisAgence("idDistri_1");
		updateColisSpecifEtapes("idDistri_1", LIVRAISON.getCode() + "|RB||||99999", MAINTENANT);
		newColisAgence("idDistri_2");
		updateColisSpecifEtapes("idDistri_2", LIVRAISON.getCode() + "|RB|XXUXX|||99999", MAINTENANT);

		final SyntheseDistributionQuantite distribQuantite = appelMSDistribQuantite(MAINTENANT.toString());
		if (!FeatureFlips.INSTANCE.getBoolean("SADelocActif", Boolean.FALSE)) {
			assertEquals(distribQuantite.getNbColisInstance().intValue(), 2);
		} else {
			assertEquals(distribQuantite.getNbColisInstance().intValue(), 1);
			assertEquals(distribQuantite.getNbColisInstanceDeloc().intValue(), 1);
		}

	}
	
	@Test
    /**  Entrée : 1 colis  la derniere étape est une LIVRAISON de mise en instance avec un evt RB
     * 			  1 colis delocalisé  la derniere étape est une LIVRAISON de mise en instance avec un evt RB
     * 
     *  Attendu : 2 ème colis ( colis instance deloc)
     * */
	public void test_4_DistriDetail_instance_avecDeloc() {
		
		/* init */
		String idColis1 = "idDistri_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, LIVRAISON.getCode() + "|RB||||99999", MAINTENANT);
		String idColis2 = "idDistri_2";
		newColisAgence(idColis2);
		updateColisSpecifEtapes(idColis2, LIVRAISON.getCode() + "|RB|XXUXX|||99999", MAINTENANT);

		/* Execute */
		final SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.INSTANCE_DELOC.getCode(), 1500, 0);

		/* Check */
		assertEquals(result.getColis().size(), 1);
		assertEquals(result.getTotalColis(),1);
		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt());
		assertTrue(noLtList.contains(idColis2));
	}

	@Test
	public void test_5_DistriQuantite_TASeche() {
		newColisAgence("idDistri_1");
		updateColisSpecifEtapes("idDistri_1", PREPA_DISTRI.getCode() + "|TA||||99999", MAINTENANT);

		final SyntheseDistributionQuantite distribQuantite = appelMSDistribQuantite(MAINTENANT.toString());

		assertEquals(distribQuantite.getNbColisTASeche().intValue(), 1);
	}
	
	@Test
    /**  Entrée : 1 colis la dernière étape est une étape PREPA_DISTRI sur un evt TA
     * 
     *  Attendu : 1 colis TA Sèche
     * */
	public void test_5_DistriDetail_TASeche() {
		
		/* init */
		String idColis1 = "idDistri_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, PREPA_DISTRI.getCode() + "|TA||||99999", MAINTENANT);

		/* Execute */
		final SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.TA_SECHE.getCode(), 1500, 0);

		/* Check */
		assertEquals(result.getColis().size(), 1);
		assertEquals(result.getTotalColis(),1);
		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt());
		assertTrue(noLtList.contains(idColis1));
	}

	@Test
	public void test_5_DistriQuantite_TASeche_avecDeloc() {
		newColisAgence("idDistri_1");
		updateColisSpecifEtapes("idDistri_1", PREPA_DISTRI.getCode() + "|TA||||99999", MAINTENANT);
		newColisAgence("idDistri_2");
		updateColisSpecifEtapes("idDistri_2", PREPA_DISTRI.getCode() + "|TA|XXUXX|||99999", MAINTENANT);

		final SyntheseDistributionQuantite distribQuantite = appelMSDistribQuantite(MAINTENANT.toString());
		
		if (!FeatureFlips.INSTANCE.getBoolean("SADelocActif", Boolean.FALSE)) {
			assertEquals(distribQuantite.getNbColisTASeche().intValue(), 2);
		} else {
			assertEquals(distribQuantite.getNbColisTASeche().intValue(), 1);
			assertEquals(distribQuantite.getNbColisTASecheDeloc().intValue(), 1);
		}
	}
	
	@Test
    /**  Entrée : 1 colis la dernière étape est une étape PREPA_DISTRI sur un evt TA
     * 			  1 colis délocalisé la dernière étape est une étape PREPA_DISTRI sur un evt TA
     * 
     *  Attendu : 1 colis TA Sèche Déloc
     * */
	public void test_5_DistriDetail_TASeche_avecDeloc() {
		
		/* init */
		String idColis1 = "idDistri_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, PREPA_DISTRI.getCode() + "|TA||||99999", MAINTENANT);
		String idColis2 = "idDistri_2";
		newColisAgence(idColis2);
		updateColisSpecifEtapes(idColis2, PREPA_DISTRI.getCode() + "|TA|XXUXX|||99999", MAINTENANT);
		
		/* Execute */
		final SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.TA_SECHE_DELOC.getCode(), 1500, 0);

		/* Check */
		assertEquals(result.getColis().size(), 1);
		assertEquals(result.getTotalColis(),1);
		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt());
		assertTrue(noLtList.contains(idColis2));
	}

	@Test
	public void test_6_DistriQuantite_instanceNonAcquittes() {
		newColisAgence("idDistri_1");
		updateColisSpecifEtapes("idDistri_1", LIVRAISON.getCode() + "|IP||||99999", MAINTENANT);

		final SyntheseDistributionQuantite distribQuantite = appelMSDistribQuantite(MAINTENANT.toString());

		assertEquals(distribQuantite.getNbColisInstanceNonAcquittes().intValue(), 1);
	}
	
	@Test
    /**  Entrée : 1 colis la derniere étape est une LIVRAISON de mise en instance avec un evt IP
     * 
     *  Attendu : 1 colis instance non acquitté
     * */
	public void test_6_DistriDetail_instanceNonAcquittes() {
		
		/* init */
		String idColis1 = "idDistri_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, LIVRAISON.getCode() + "|IP||||99999", MAINTENANT);

		/* Execute */
		final SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.INSTANCE_NON_ACQUITTES.getCode(), 1500, 0);

		/* Check */
		assertEquals(result.getColis().size(), 1);
		assertEquals(result.getTotalColis(),1);
		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt());
		assertTrue(noLtList.contains(idColis1));
	}


	@Test
	public void test_6_DistriQuantite_instanceNonAcquittes_avecSortieReseau() {
		newColisAgence("idDistri_1");
		updateColisSpecifEtapes("idDistri_1", LIVRAISON.getCode() + "|IP|PICKUP|||99999", MAINTENANT);

		final SyntheseDistributionQuantite distribQuantite = appelMSDistribQuantite(MAINTENANT.toString());

		assertEquals(distribQuantite.getNbColisInstanceNonAcquittes().intValue(), 0);
	}
	
	@Test
    /**  Entrée : 1 colis la derniere étape est une LIVRAISON de mise en instance avec un evt IP
     * 
     *  Attendu : 0 colis instance non acquitté
     * */
	public void test_6_DistriDetail_instanceNonAcquittes_avecSortieReseau() {
		
		/* init */
		String idColis1 = "idDistri_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, LIVRAISON.getCode() + "|IP|PICKUP|||99999", MAINTENANT);

		/* Execute */
		final SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.INSTANCE_NON_ACQUITTES.getCode(), 1500, 0);

		/* Check */
		assertEquals(result.getColis().size(), 0);
		assertEquals(result.getTotalColis(),0);
	}


	@Test
	public void test_6_DistriQuantite_instanceNonAcquittes_avecDeloc() {
		newColisAgence("idDistri_1");
		updateColisSpecifEtapes("idDistri_1", LIVRAISON.getCode() + "|IP||||99999", MAINTENANT);
		newColisAgence("idDistri_2");
		updateColisSpecifEtapes("idDistri_2", LIVRAISON.getCode() + "|IP|XXUXX|||99999", MAINTENANT);

		final SyntheseDistributionQuantite distribQuantite = appelMSDistribQuantite(MAINTENANT.toString());
		if (!FeatureFlips.INSTANCE.getBoolean("SADelocActif", Boolean.FALSE)) {
			assertEquals(distribQuantite.getNbColisInstanceNonAcquittes().intValue(), 2);
		}else {
			assertEquals(distribQuantite.getNbColisInstanceNonAcquittes().intValue(), 1);
			assertEquals(distribQuantite.getNbColisInstanceNonAcquittesDeloc().intValue(), 1);
		}
	}
	
	@Test
    /**  Entrée : 1er colis la derniere étape est une LIVRAISON de mise en instance avec un evt IP
     * 			  2ème colis délocalisé derniere étape est une LIVRAISON de mise en instance avec un evt IP
     * 
     *  Attendu :  2ème colis ( instance non acquitté deloc)
     * */
	public void test_6_DistriDetail_instanceNonAcquittes_avecDeloc() {
		
		/* init */
		String idColis1 = "idDistri_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, LIVRAISON.getCode() + "|IP||||99999", MAINTENANT);
		String idColis2 = "idDistri_2";
		newColisAgence(idColis2);
		updateColisSpecifEtapes(idColis2, LIVRAISON.getCode() + "|IP|XXUXX|||99999", MAINTENANT);
		
		/* Execute */
		final SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.INSTANCE_NON_ACQUITTES_DELOC.getCode(), 1500, 0);

		/* Check */
		assertEquals(result.getColis().size(), 1);
		assertEquals(result.getTotalColis(),1);
		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt());
		assertTrue(noLtList.contains(idColis2));
	}
	
	@Test
	public void test_7_DistriQuantite_enEchec() {
		newColisAgence("idDistri_1");
		updateColisSpecifEtapes("idDistri_1", LIVRAISON.getCode() + "|RC||||99999", MAINTENANT);

		final SyntheseDistributionQuantite distribQuantite = appelMSDistribQuantite(MAINTENANT.toString());

		assertEquals(distribQuantite.getNbColisEnEchec().intValue(), 1);
	}
	
	
	@Test
    /**  Entrée : 1 colis  la derniere étape est une LIVRAISON d'échec de livraison avec un evt RC
     * 
     *  Attendu : 1 colis en echec
     * */
	public void test_7_DistriDetail_enEchec() {
		
		/* init */
		String idColis1 = "idDistri_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, LIVRAISON.getCode() + "|RC||||99999", MAINTENANT);

		/* Execute */
		final SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.EN_ECHEC.getCode(), 1500, 0);

		/* Check */
		assertEquals(result.getColis().size(), 1);
		assertEquals(result.getTotalColis(),1);
		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt());
		assertTrue(noLtList.contains(idColis1));
	}

	
	@Test
	public void test_7_DistriQuantite_enEchec_avecDeloc() {
		newColisAgence("idDistri_1");
		updateColisSpecifEtapes("idDistri_1", LIVRAISON.getCode() + "|RC||||99999", MAINTENANT);
		newColisAgence("idDistri_2");
		updateColisSpecifEtapes("idDistri_2", LIVRAISON.getCode() + "|RC|XXUXX|||99999", MAINTENANT);

		final SyntheseDistributionQuantite distribQuantite = appelMSDistribQuantite(MAINTENANT.toString());
		if (!FeatureFlips.INSTANCE.getBoolean("SADelocActif", Boolean.FALSE)) {
			assertEquals(distribQuantite.getNbColisEnEchec().intValue(), 2);
		} else {
			assertEquals(distribQuantite.getNbColisEnEchec().intValue(), 1);
			assertEquals(distribQuantite.getNbColisEnEchecDeloc().intValue(), 1);
		}
	}
	
	@Test
    /**  Entrée : 1er colis  la derniere étape est une LIVRAISON d'échec de livraison avec un evt RC
     * 			  2ème colis  la derniere étape est une LIVRAISON d'échec de livraison avec un evt RC
     *  
     *  Attendu : 2ème colis en echec deloc
     * */
	public void test_7_DistriDetail_enEchec_avecDeloc() {
		
		/* init */
		String idColis1 = "idDistri_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, LIVRAISON.getCode() + "|RC||||99999", MAINTENANT);
		String idColis2 = "idDistri_2";
		newColisAgence(idColis2);
		updateColisSpecifEtapes(idColis2, LIVRAISON.getCode() + "|RC|XXUXX|||99999", MAINTENANT);

		/* Execute */
		final SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.EN_ECHEC_DELOC.getCode(), 1500, 0);

		/* Check */
		assertEquals(result.getColis().size(), 1);
		assertEquals(result.getTotalColis(),1);
		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt());
		assertTrue(noLtList.contains(idColis2));
	}
	
	@Test
	public void test_7_DistriQuantite_enEchec_avecDeloc_avecRetourAgence() {
		newColisAgence("idDistri_1");
		updateColisSpecifEtapes("idDistri_1", LIVRAISON.getCode() + "|RC||||99999", MAINTENANT);
		updateColisSpecifEtapes("idDistri_1", RETOUR_AGENCE.getCode() + "|RC||||99999", MAINTENANT.plusHours(1));
		newColisAgence("idDistri_2");
		updateColisSpecifEtapes("idDistri_2", LIVRAISON.getCode() + "|RC|XXUXX|||99999", MAINTENANT);
		updateColisSpecifEtapes("idDistri_2", RETOUR_AGENCE.getCode() + "|RC|XXUXX|||99999", MAINTENANT.plusHours(1));

		final SyntheseDistributionQuantite distribQuantite = appelMSDistribQuantite(MAINTENANT.toString());

		assertEquals(distribQuantite.getNbColisEnEchec().intValue(), 0);
		assertEquals(distribQuantite.getNbColisEnEchecDeloc().intValue(), 0);
	}

	@Test
    /**  Entrée : 1 colis déloc étape est une LIVRAISON d'échec de livraison avec un evt RC
     * 			 et la derniere étape est une Retour agence avec un evt RC
     *  
     *  Attendu : O colis en echec deloc
     * */
	public void test_7_DistriDetail_enEchec_avecRetourAgence_avecDeloc() {
		
		/* init */
		String idColis1 = "idDistri_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, LIVRAISON.getCode() + "|RC|XXUXX|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis1, RETOUR_AGENCE.getCode() + "|RC|XXUXX|||99999", MAINTENANT.plusHours(1));

		/* Execute */
		final SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.EN_ECHEC_DELOC.getCode(), 1500, 0);

		/* Check */
		assertEquals(result.getColis().size(), 0);
		assertEquals(result.getTotalColis(),0);
	}
	
	@Test
    /**  Entrée : 1 colis  étape est une LIVRAISON d'échec de livraison avec un evt RC
     * 			 et la derniere étape est une Retour agence avec un evt RC
     *  
     *  Attendu : O colis en echec 
     * */
	public void test_7_DistriDetail_enEchec_avecRetourAgence() {
		
		/* init */
		String idColis1 = "idDistri_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, LIVRAISON.getCode() + "|RC||||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis1, RETOUR_AGENCE.getCode() + "|RC||||99999", MAINTENANT.plusHours(1));

		/* Execute */
		final SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.EN_ECHEC.getCode(), 1500, 0);

		/* Check */
		assertEquals(result.getColis().size(), 0);
		assertEquals(result.getTotalColis(),0);
	}
	
	
	@AfterMethod
	public void cleanAfterTest() {
		cleanColisSpecifications();
		cleanAgence();
	}

	@AfterClass
	public void cleanAfterClass() throws Exception {
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
	}

	private void newColisAgence(final String noLt) {
		final Set<String> setNoLt = Sets.newHashSet(noLt);
		getSession().execute(psInsertColisSaisi.bind(setNoLt, AGENCE, JOUR, HEURE, MINUTE));
	}

	private void updateColisSpecifEtapes(final String noLt, final String etape, final DateTime dateEtape) {
		getSession().execute(psUpdateColisSpecifEtapes.bind(dateEtape.toDate(), etape, noLt));
	}

	private SyntheseDistributionQuantite appelMSDistribQuantite(final String dateAppel) {
		final Response response = ClientBuilder.newClient().target("http://localhost:" + httpPort)
				.path("/getSyntheseAgence/Distribution/Quantite/" + AGENCE + "/dateAppel/" + dateAppel).request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get();
		return response.readEntity(SyntheseDistributionQuantite.class);
	}

	private void cleanAgence() {
		getSession().execute(psTruncateColisAgence.getQueryString());
	}

	private void cleanColisSpecifications() {
		getSession().execute(psTruncateColisSpec.getQueryString());
	}
	
	/**
	 * 
	 * @param indicateur
	 * @return
	 */
	private SyntheseColisEtListeValeurs appelMSActiviteEtListeValeurAvecIndicateur(String indicateur, Integer limit, Integer nbJours) {
		WebTarget path = ClientBuilder.newClient().target("http://localhost:" + getPort())
				.path("/getSyntheseAgence/ActiviteEtListeValeur/" + AGENCE + "/" + indicateur +"/dateAppel/"+ HEURE_APPEL+"/"+nbJours);

		if (null != limit) {
			path = path.queryParam("limit", limit);
		}
		
		Response response = path.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get();

		final SyntheseColisEtListeValeurs entity = response.readEntity(new GenericType<SyntheseColisEtListeValeurs>() {
		});
		log.debug("Entity : {}", entity.toString());

		return entity;
	}
}
