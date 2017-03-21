package com.chronopost.vision.microservices.traitementRetard;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.cassandra.table.ETableDateLivraisonEstimeeLt;
import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.sdk.GenereEvtV1;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.TraitementRetardInput;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.chronopost.vision.transco.transcoder.Transcoder;
import com.chronopost.vision.ut.RandomUts;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import fr.chronopost.soap.calculretard.cxf.Analyse;
import fr.chronopost.soap.calculretard.cxf.CalculDateDeLivraisonEstimee;
import fr.chronopost.soap.calculretard.cxf.ResultCalculerRetardPourNumeroLt;

/** @author unknown : JJC getSession + LOGGER import min. **/
public class TraitementRetardAcceptanceTest extends JerseyTestNg.ContainerPerClassTest {

	/**
	 * @return VisionMicroserviceApplication.cassandraSession (a
	 *         com.datastax.driver.core )
	 */
	private static final Session getSession() {
		return VisionMicroserviceApplication.getCassandraSession();
	}

	private PreparedStatement psCleanDateLivEst;
	private PreparedStatement psSelectDateLiv;
	private PreparedStatement psInsertDateLiv;

	/**
	 * Mocking the service
	 */
	private static ITraitementRetardService serviceMock = Mockito.mock(ITraitementRetardService.class);

	private Client client;

	/* Port utilisé avec wiremock */
	private int wireHttpPort;

	private boolean suiteLaunch = true;

	private final ITranscoderDao mockTranscoderDao = Mockito.mock(ITranscoderDao.class);

	private WireMockServer wireMockServer;
	private WireMock wireMock;

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
				suiteLaunch = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		CCMBridge.ipOfNode(1);

		/* Création de la resource et initialisation avec le service mocké */
		// TraitementRetardResource resourceTraitementRetardResourceTest = new
		// TraitementRetardResource(serviceMock);
		TraitementRetardResource resourceTraitementRetardResourceTest = new TraitementRetardResource();

		resourceTraitementRetardResourceTest.setService(TraitementRetardServiceImpl.getInstance());

		wireHttpPort = RandomUts.getRandomHttpPort();
		forceSet(TestProperties.CONTAINER_PORT, "0");

		ResourceConfig config = new ResourceConfig();
		config.register(resourceTraitementRetardResourceTest);

		return config;
	}

	@BeforeClass
	public void setUp() throws Exception {
		super.setUp();
		client = ClientBuilder.newClient();
		initFlip("true");

		/* Mock du MS genereEvt */
		wireMockServer = new WireMockServer(wireHttpPort);
		wireMockServer.start();
		WireMock.configureFor("127.0.0.2", wireHttpPort);
		wireMock = new WireMock("127.0.0.2", wireHttpPort);

		initWiremock();

		psCleanDateLivEst = getSession().prepare("DELETE FROM date_livraison_estimee_lt WHERE no_lt = ?");
		psSelectDateLiv = getSession().prepare("SELECT " + ETableDateLivraisonEstimeeLt.DATE_ESTIMEE.getNomColonne()
				+ " FROM date_livraison_estimee_lt WHERE no_lt = ? LIMIT 1");
		psInsertDateLiv = getSession().prepare("INSERT INTO " + ETableDateLivraisonEstimeeLt.TABLE_NAME + "("
				+ ETableDateLivraisonEstimeeLt.NO_LT.getNomColonne() + ","
				+ ETableDateLivraisonEstimeeLt.DATE_ESTIMEE.getNomColonne() + ","
				+ ETableDateLivraisonEstimeeLt.DATE_INSERTION.getNomColonne() + ") " + "VALUES(?,?,?)");

		for (String cur : new String[] { "XX123460X", "XX123461X", "XX123462X", "XX123463X", "XX123464X", "XX123465X",
				"XX123466X", "XX123467X" }) {
			getSession().execute(psCleanDateLivEst.bind(cur));
		}

		/* On initialise le service sdk d'appel à genereEvt */
		GenereEvtV1.getInstance().setEndpoint("http://127.0.0.2:" + wireHttpPort);
		TraitementRetardServiceImpl.getInstance().setDao(TraitementRetardDaoImpl.getInstance());
	}

	/**
	 * Initialisation du mock des appels http
	 * 
	 * @throws FileNotFoundException
	 */
	public void initWiremock() throws FileNotFoundException {
		wireMock.register(
				post(urlEqualTo("/genereEvt/true")).withHeader("Content-Type", equalTo("application/json")).willReturn(
						aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"status\":\"true\"}")));
	}

	/**
	 * Appel avec 2 retours de CalculRetard mais 1 seul a le flag RD a générer.
	 * Pour les 2 LT la nouvelle DLE > Max(DLE précédentes) et DLE > DCL Seul 1
	 * des retours de CalculRetard a le champ generationRD à 'O' Le parametre
	 * genereEvt_RD_actif est sur ON
	 * 
	 * Resultat : Seul 1 evt RD doit être créé. Mais les 2 DLE sont ajoutées
	 * dans la table dateLivraisonEstimeeLt
	 * 
	 * PS: DLE = Date livraison estimée DCL = Date contractuelle de livraison
	 * 
	 */
	@Test
	public void cas1Test1() throws Exception {
		Mockito.reset(serviceMock);
		wireMock.resetRequests();

		/* Construction des data du test */
		/* initialisation des retards à fournir au MS */
		List<TraitementRetardInput> retards = new ArrayList<>();
		Date maintenant = new Date();

		/* 1 retard de 2 jours avec proposition de génération d'un RD */
		retards.add(newTraitementRetardInput(newLt("XX123460X"),
				newResultCalculerRetardPourNumeroLt(1, maintenant, 2, "O")));

		/* 1 retard de 3 jours sans proposition de génération d'un RD */
		retards.add(newTraitementRetardInput(newLt("XX123461X"),
				newResultCalculerRetardPourNumeroLt(1, maintenant, 3, "N")));

		/* Appel */
		int status = appelMS(retards).getStatus();

		/* Tests de l'appel */
		assertEquals(status, 200);
		assertNotNull(getDernierDleEnBase("XX123460X"));
		assertNotNull(getDernierDleEnBase("XX123461X"));
		verify(1, postRequestedFor(urlMatching("/genereEvt.*")));
	}

	/**
	 * Appel avec 2 retours de CalculRetard mais date inferieures. Pour 1 LT la
	 * nouvelle DLE < Max(DLE précédentes) et DLE > DCL Pour 1 LT la nouvelle
	 * DLE > Max(DLE précédentes) et DLE < DCL Pour les 2 retours de
	 * CalculRetard a le champ generationRD à 'O' Le parametre
	 * genereEvt_RD_actif est sur ON
	 * 
	 * Resultat : Aucun evt RD n'est créé. Mais les 2 DLE sont ajoutées dans la
	 * table dateLivraisonEstimeeLt
	 * 
	 * PS: DLE = Date livraison estimée DCL = Date contractuelle de livraison
	 * 
	 * @throws ParseException
	 * 
	 */
	@Test(groups = { "slow", "acceptance" })
	public void cas1Test2() throws ParseException {
		Mockito.reset(serviceMock);
		wireMock.resetRequests();

		/* Construction des data du test */
		/* initialisation des retards à fournir au MS */
		List<TraitementRetardInput> retards = new ArrayList<>();
		Date maintenant = new Date();

		/*
		 * 1 retard de 2 jours avec proposition de génération d'un RD mais
		 * DLE<Max(DLE)
		 */
		retards.add(newTraitementRetardInput(newLt("XX123462X"),
				newResultCalculerRetardPourNumeroLt(1, maintenant, 2, "O")));
		insertDleEnBase("XX123462X", maintenant, 3);

		/*
		 * 1 retard de 3 jours avec proposition de génération d'un RD mais pas
		 * de retard détecté (c'est incohérent mais on test le flag DLE>DCL)
		 */
		retards.add(newTraitementRetardInput(newLt("XX123463X"),
				newResultCalculerRetardPourNumeroLt(0, maintenant, 3, "O")));

		/* Appel */
		int status = appelMS(retards).getStatus();

		/* Test de l'appel */
		assertEquals(status, 200);
		assertNotNull(getDernierDleEnBase("XX123462X"));
		assertNotNull(getDernierDleEnBase("XX123463X"));
		verify(0, postRequestedFor(urlMatching("/genereEvt.*")));
	}

	/**
	 * Appel avec 2 retours de CalculRetard mais date égales. Pour 1 LT la
	 * nouvelle DLE = Max(DLE précédentes) et DLE > DCL Pour 1 LT la nouvelle
	 * DLE > Max(DLE précédentes) et DLE = DCL Pour les 2 retours de
	 * CalculRetard a le champ generationRD à 'O' Le parametre
	 * genereEvt_RD_actif est sur ON
	 * 
	 * Resultat : Aucun evt RD n'est créé. Mais les 2 DLE sont ajoutées dans la
	 * table dateLivraisonEstimeeLt
	 * 
	 * PS: DLE = Date livraison estimée DCL = Date contractuelle de livraison
	 * 
	 * @throws ParseException
	 * 
	 */
	@Test(groups = { "slow", "acceptance" })
	public void cas1Test3() throws ParseException {
		Mockito.reset(serviceMock);
		wireMock.resetRequests();

		/* Construction des data du test */
		/* initialisation des retards à fournir au MS */
		List<TraitementRetardInput> retards = new ArrayList<>();
		Date maintenant = new Date();

		/*
		 * 1 retard de 2 jours avec proposition de génération d'un RD mais
		 * DLE<Max(DLE)
		 */
		retards.add(newTraitementRetardInput(newLt("XX123464X"),
				newResultCalculerRetardPourNumeroLt(1, maintenant, 2, "O")));
		insertDleEnBase("XX123464X", maintenant, 2);

		/*
		 * 1 retard de 3 jours avec proposition de génération d'un RD mais pas
		 * de retard détecté (c'est incohérent mais on test le flag DLE>DCL)
		 */
		retards.add(newTraitementRetardInput(newLt("XX123465X"),
				newResultCalculerRetardPourNumeroLt(0, maintenant, 3, "O")));

		/* Appel */
		int status = appelMS(retards).getStatus();

		/* Test de l'appel */
		assertEquals(status, 200);
		assertNotNull(getDernierDleEnBase("XX123464X"));
		assertNotNull(getDernierDleEnBase("XX123465X"));
		verify(0, postRequestedFor(urlMatching("/genereEvt.*")));
	}

	/**
	 * Appel avec 2 retours de CalculRetard mais 1 seul a le flag RD a générer.
	 * Pour les 2 LT la nouvelle DLE > Max(DLE précédentes) et DLE > DCL Seul 1
	 * des retour de CalculRetard a le champ generationRD à 'O' Le parametre
	 * genereEvt_RD_actif est sur OFF
	 * 
	 * Resultat : Aucun evt RD n'est créé. Mais les 2 DLE sont ajoutées dans la
	 * table dateLivraisonEstimeeLt
	 * 
	 * PS: DLE = Date livraison estimée DCL = Date contractuelle de livraison
	 * 
	 * @throws Exception
	 * 
	 */
	@Test(groups = { "slow", "acceptance" })
	public void cas1Test4() throws Exception {
		Mockito.reset(serviceMock);
		wireMock.resetRequests();

		/* Construction des data du test */
		/* initialisation des retards à fournir au MS */
		List<TraitementRetardInput> retards = new ArrayList<>();
		Date maintenant = new Date();

		/* 1 retard de 2 jours avec proposition de génération d'un RD */
		retards.add(newTraitementRetardInput(newLt("XX123466X"),
				newResultCalculerRetardPourNumeroLt(1, maintenant, 2, "O")));

		/* 1 retard de 3 jours sans proposition de génération d'un RD */
		retards.add(newTraitementRetardInput(newLt("XX123467X"),
				newResultCalculerRetardPourNumeroLt(1, maintenant, 3, "N")));

		/* la parametre d'activation du générateur d'événements RD est à OFF */
		initFlip("false");

		/* Appel */
		Thread.sleep(5000);
		int status = appelMS(retards).getStatus();

		/* Tests de l'appel */
		assertEquals(status, 200);
		assertNotNull(getDernierDleEnBase("XX123466X"));
		assertNotNull(getDernierDleEnBase("XX123467X"));
		verify(0, postRequestedFor(urlMatching("/genereEvt.*")));
	}

	/**
	 * Effectue un appel au microService TraitementRetard, et retourne la
	 * réponse de celui-ci
	 * 
	 * @param listeRetards
	 *            : la liste des retards à fournir au MS.
	 * @return : Une réponse du MS.
	 */
	private Response appelMS(List<TraitementRetardInput> listeRetards) {
		WebTarget a = client.target("http://localhost:" + getPort());
		WebTarget b = a.path("/TraitementRetard");
		Builder c = b.request();
		Builder d = c.accept(MediaType.APPLICATION_JSON_TYPE);
		Entity<List<TraitementRetardInput>> f = Entity.entity(listeRetards, MediaType.APPLICATION_JSON);
		return d.post(f);
	}

	/**
	 * @param lt
	 *            : un objet Lt (avec au moins le numéro lt positionné)
	 * @param resultCalculRetard
	 *            : Un objet résultant d'un appel au WebService CalculRetard
	 * @return un objet (TraitementRetardInput) retard à fournir au MS
	 *         TraitementRetard intialiser avec les paramètres fournis
	 */
	private TraitementRetardInput newTraitementRetardInput(Lt lt, ResultCalculerRetardPourNumeroLt resultCalculRetard) {
		TraitementRetardInput retard = new TraitementRetardInput();
		retard.setLt(lt);
		retard.setResultCR(resultCalculRetard);
		return retard;
	}

	/**
	 * 
	 * 
	 * @param retardDetecte
	 *            : indicateur de retard detecté (1 pour détecté, 0 pour non
	 *            détecté)
	 * @param dateActuelle
	 *            : Date à partir de laquelle calculer la date de livraison
	 *            prévue
	 * @param nbJourFuturDLE
	 *            : nombre de jours a ajouter à la date actuelle pour former la
	 *            DLE
	 * @param generationRDConseillee
	 *            : indicateur de generation d'événement RD proposée.
	 * @return : un objet ResultCalculerRetardPourNumeroLt tel qu'il serait
	 *         retourné par le WebService CalculRetard avec les valeur indiquée
	 *         en paramètre.
	 * @throws ParseException
	 */
	private ResultCalculerRetardPourNumeroLt newResultCalculerRetardPourNumeroLt(int retardDetecte, Date dateActuelle,
			int nbJourFuturDLE, String generationRDConseillee) throws ParseException {
		ResultCalculerRetardPourNumeroLt resultDLE = new ResultCalculerRetardPourNumeroLt();
		resultDLE.setAnalyse(new Analyse());
		resultDLE.getAnalyse().setEnRetardDateEstimeeSupDateContractuelle(retardDetecte); // Retard
																							// détecté
		resultDLE.setCalculDateDeLivraisonEstimee(new CalculDateDeLivraisonEstimee());
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(dateActuelle);
		cal2.add(Calendar.DATE, nbJourFuturDLE);
		resultDLE.getCalculDateDeLivraisonEstimee()
				.setDateDeLivraisonEstimee(DateRules.formatDateCalculRetard(cal2.getTimeInMillis())); // DLE
		resultDLE.getCalculDateDeLivraisonEstimee().setGenerationRD(generationRDConseillee);
		return resultDLE;
	}

	/**
	 * @param noLt
	 *            : l'identifiant colis recherché.
	 * @return La derniere date de livraison estimé en base (table
	 *         dateLivraisonEstimeeLt) pour la lt indiquée
	 */
	private Date getDernierDleEnBase(String noLt) {
		/* Vérification en base */
		final ResultSet evtResult = getSession().execute(psSelectDateLiv.bind(noLt));
		try {
			return evtResult.one().getTimestamp(0);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Insert en base une DLE pour une LT.
	 * 
	 * @param noLt
	 *            : identfiant du colis
	 * @param dateActuelle
	 *            : date de référence
	 * @param nbJourFuturDLE
	 *            : nombre de jour a ajouter a la date référence pour generer la
	 *            DLE à enregistrer
	 */
	private void insertDleEnBase(String noLt, Date dateActuelle, int nbJourFuturDLE) {
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(dateActuelle);
		cal2.add(Calendar.DATE, nbJourFuturDLE);
		getSession().execute(psInsertDateLiv.bind(noLt, cal2.getTime(), new Date()));
	}

	/**
	 * @param noLt
	 *            : identifiant du colis
	 * @return un objet Lt initialisé avec les valeurs fournies en parametre
	 */
	private Lt newLt(String noLt) {
		Lt lt = new Lt();
		lt.setNoLt(noLt);
		return lt;
	}

	private void initFlip(String value) throws Exception {
		// Préparation du mock des transcodifications
		ConcurrentMap<String, Transcoder> transcoders = new ConcurrentHashMap<>();
		Map<String, Map<String, String>> map = new HashMap<>();

		Map<String, String> mapPays = new HashMap<>();
		mapPays.put("250", "FR|FRANCE");
		map.put("code_pays", mapPays);

		Map<String, String> mapParam = new HashMap<>();
		mapParam.put("evt_calcul_retard", "|PC|EC|SD|TA|");
		mapParam.put("poste_comptable_evt_RD", "99999");
		map.put("parametre_microservices", mapParam);
		Transcoder transcoderParam = new Transcoder();
		transcoderParam.setTranscodifications(map);
		transcoders.put("DiffusionVision", transcoderParam);

		Map<String, String> mapFlip = new HashMap<>();
		mapFlip.put("genere_evt_RD_actif", value);
		map.put("feature_flips", mapFlip);
		Transcoder transcoderFlip = new Transcoder();
		transcoderFlip.setTranscodifications(map);
		transcoders.put("Vision", transcoderFlip);

		TranscoderService.INSTANCE.setTranscoders(transcoders);

		Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("DiffusionVision")).thenReturn(map);
		Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("Vision")).thenReturn(map);

		TranscoderService.INSTANCE.setDao(mockTranscoderDao);
		TranscoderService.INSTANCE.addProjet("Vision");
		FeatureFlips.INSTANCE.setFlipProjectName("Vision");
	}

	@AfterClass
	public void tearDownAfterClass() throws Exception {
		for (String cur : new String[] { "XX123460X", "XX123461X", "XX123462X", "XX123463X", "XX123464X", "XX123465X",
				"XX123466X", "XX123467X" }) {
			getSession().execute(psCleanDateLivEst.bind(cur));
		}
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
		wireMockServer.shutdownServer();
	}
}
