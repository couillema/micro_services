package com.chronopost.vision.microservices.reference;

import static org.glassfish.jersey.test.TestProperties.CONTAINER_PORT;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.CacheManagerService;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.model.Agence;
import com.chronopost.vision.model.CodeService;
import com.chronopost.vision.model.Evenement;
import com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis;

public class ReferenceAcceptanceTest extends JerseyTestNg.ContainerPerClassTest {

	private Client client;
	private boolean suiteLaunch = true;

	private CacheManager<CodeService> cacheCodeServiceMock;
	private CacheManager<Evenement> cacheEvtMock;
	private CacheManager<Agence> cacheAgenceMock;
	private CacheManager<Parametre> cacheParametreMock;

	private static HashMap<String, CodeService> mapRefCodeService = new HashMap<>();
	private static HashMap<String, Evenement> mapRefEvenements = new HashMap<>();
	private static HashMap<String, Agence> mapRefAgence = new HashMap<>();

	@SuppressWarnings("unchecked")
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

		/* On mock le cacheCodeService */
		cacheCodeServiceMock = Mockito.mock(CacheManager.class);
		Mockito.when(cacheCodeServiceMock.getCache()).thenReturn(mapRefCodeService);

		/* On mock le cacheEvenement */
		cacheEvtMock = Mockito.mock(CacheManager.class);
		Mockito.when(cacheEvtMock.getCache()).thenReturn(mapRefEvenements);

		/* On mock le cacheAgence */
		cacheAgenceMock = Mockito.mock(CacheManager.class);
		Mockito.when(cacheAgenceMock.getCache()).thenReturn(mapRefAgence);

		/* On mock le cacheParametre */
		cacheParametreMock = Mockito.mock(CacheManager.class);
		Mockito.when(cacheParametreMock.getValue("key1")).thenReturn(new Parametre("key1", "value1"));

		ReferenceServiceImpl.INSTANCE.setRefentielCodeService(cacheCodeServiceMock);
		ReferenceServiceImpl.INSTANCE.setRefentielEvenement(cacheEvtMock);
		ReferenceServiceImpl.INSTANCE.setRefentielAgence(cacheAgenceMock);
		ReferenceServiceImpl.INSTANCE.setRefentielParametre(cacheParametreMock);
		final ReferenceResource resource = new ReferenceResource();
		resource.setService(ReferenceServiceImpl.INSTANCE);

		forceSet(CONTAINER_PORT, "0");

		final ResourceConfig config = new ResourceConfig();
		config.register(resource);
		return config;
	}

	@BeforeClass
	public void setUp() throws Exception {
		super.setUp();
		client = ClientBuilder.newClient();
		
		mapRefCodeService.put("key1", new CodeService("soCode1", "", null, null, ""));
		mapRefCodeService.put("key2", new CodeService("", "asCode2", null, null, ""));
		mapRefCodeService.put("key3", new CodeService("", "", null, null, "lib3"));
		
		mapRefEvenements.put("key1", new Evenement("codeEvt1", "", EEtapesColis.ACQUITTEMENT_LIVRAISON, 1, "", ""));
		mapRefEvenements.put("key2", new Evenement("", "codeBaseColis2", EEtapesColis.LIVRAISON, 2, "", ""));
		mapRefEvenements.put("key3", new Evenement("", "", EEtapesColis.COLLECTE, 3, "libelle3", ""));
		mapRefEvenements.put("key4", new Evenement("", "", null, 4, "", "libellePdv4"));
		
		mapRefAgence.put("key1", new Agence("posteComptable1", "", "", "", "TZ", "R"));
		mapRefAgence.put("key2", new Agence("", "trigramme2", "", "", "TZ", "R"));
		mapRefAgence.put("key3", new Agence("", "", "depot3", "", "TZ", "R"));
		mapRefAgence.put("key4", new Agence("", "", "", "libelle4", "TZ", "R"));
	}

	@Test
	public void test_getCodesService() throws Exception {
		final Response response = client.target("http://localhost:" + getPort()).path("reference").path("service").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get();
		final Set<CodeService> codesService = response.readEntity(new GenericType<Set<CodeService>>() {
		});
		assertEquals(codesService.size(), 3);
		final StringBuilder soCodes = new StringBuilder();
		final StringBuilder asCodes = new StringBuilder();
		final StringBuilder libelles = new StringBuilder();
		final Iterator<CodeService> iterator = codesService.iterator();
		while (iterator.hasNext()) {
			final CodeService codeService = iterator.next();
			soCodes.append(codeService.getSocode());
			asCodes.append(codeService.getAscode());
			libelles.append(codeService.getLibelle());
		}
		assertEquals(soCodes.toString(), "soCode1");
		assertEquals(asCodes.toString(), "asCode2");
		assertEquals(libelles.toString(), "lib3");
	}

	@Test
	public void test_getEvenements() throws Exception {
		final Response response = client.target("http://localhost:" + getPort()).path("reference").path("evenement").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get();
		final Set<Evenement> evenements = response.readEntity(new GenericType<Set<Evenement>>() {
		});
		assertEquals(evenements.size(), 4);
		final StringBuilder codesEvt = new StringBuilder();
		final StringBuilder codesBaseColis = new StringBuilder();
		final StringBuilder libelles = new StringBuilder();
		final StringBuilder libellesPdv = new StringBuilder();
		final Iterator<Evenement> iterator = evenements.iterator();
		while (iterator.hasNext()) {
			final Evenement evenement = iterator.next();
			codesEvt.append(evenement.getCodeEvt());
			codesBaseColis.append(evenement.getCodeBaseColis());
			libelles.append(evenement.getLibelle());
			libellesPdv.append(evenement.getLibellePdv1());
		}
		assertEquals(codesEvt.toString(), "codeEvt1");
		assertEquals(codesBaseColis.toString(), "codeBaseColis2");
		assertEquals(libelles.toString(), "libelle3");
		assertEquals(libellesPdv.toString(), "libellePdv4");
	}

	@Test
	public void test_getAgences() throws Exception {
		final Response response = client.target("http://localhost:" + getPort()).path("reference").path("agence").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get();
		final Set<Agence> agences = response.readEntity(new GenericType<Set<Agence>>() {
		});
		assertEquals(agences.size(), 4);
		final StringBuilder postesComptable = new StringBuilder();
		final StringBuilder trigrammes = new StringBuilder();
		final StringBuilder depots = new StringBuilder();
		final StringBuilder libelles = new StringBuilder();
		final StringBuilder timezones = new StringBuilder();
		final StringBuilder reseaux = new StringBuilder();
		final Iterator<Agence> iterator = agences.iterator();
		while (iterator.hasNext()) {
			final Agence agence = iterator.next();
			postesComptable.append(agence.getPosteComptable());
			trigrammes.append(agence.getTrigramme());
			depots.append(agence.getDepot());
			libelles.append(agence.getLibelle());
			timezones.append(agence.getTimezone());
			reseaux.append(agence.getReseau());
		}
		assertEquals(postesComptable.toString(), "posteComptable1");
		assertEquals(trigrammes.toString(), "trigramme2");
		assertEquals(depots.toString(), "depot3");
		assertEquals(libelles.toString(), "libelle4");
		assertEquals(timezones.toString(), "TZTZTZTZ");
		assertEquals(reseaux.toString(), "RRRR");
	}

	@Test
	public void test_getParametre() throws Exception {
		final Response response = client.target("http://localhost:" + getPort()).path("reference").path("parametre").path("key1").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get();
		final String paramValue = response.readEntity(String.class);
		assertEquals(paramValue, "value1");
		Mockito.verify(cacheParametreMock, Mockito.times(1)).getValue("key1");
	}

    @AfterMethod
    public void tearDownAfterClass() throws Exception {
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
        CacheManagerService.INSTANCE.stopUpdater();
    }
}
