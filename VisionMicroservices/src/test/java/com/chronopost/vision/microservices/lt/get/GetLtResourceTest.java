package com.chronopost.vision.microservices.lt.get;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpStatus;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.enums.ETraitementSynonymes;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.sdk.GetLtParEmailDestiV1;
import com.chronopost.vision.microservices.sdk.exception.NotFoundException;
import com.chronopost.vision.microservices.sdk.exception.ServiceUnavailableException;
import com.chronopost.vision.microservices.sdk.exception.TechnicalException;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.getLt.v1.RechercheLtParEmailDestiInput;

/** @author unknown : JJC port */
public class GetLtResourceTest extends JerseyTestNg.ContainerPerClassTest {

    /**
     * Mocking the service
     */
	private static IGetLtService serviceMock = Mockito.mock(IGetLtService.class);

    /**
     * Binding the service to the instantiation of the resource
     */
    @Override
	protected Application configure() {
		GetLtResource resourceGetLts = new GetLtResource();
		resourceGetLts.setService(serviceMock);

		forceSet(TestProperties.CONTAINER_PORT, "0");

		ResourceConfig config = new ResourceConfig();
		config.register(resourceGetLts);

		return config;
	}

    @Test
    public void testGetLTFoundNotFound() {
        Client client = ClientBuilder.newClient();

        Mockito.reset(serviceMock);

        Map<String, Lt> mapLt5678 = new HashMap<>();
        Lt lt5678 = new Lt();
        lt5678.setNoLt("5678");
        mapLt5678.put("5678", lt5678);

        Mockito.when(
                serviceMock.getLtsFromDatabase(Mockito.eq(Arrays.asList("5678")),
                        Mockito.eq(ETraitementSynonymes.PAS_DE_RESOLUTION_DES_SYNONYMES))).thenReturn(mapLt5678);

        // shouldn't be found
        int status = client.target("http://localhost:" + getPort()).path("/GetLT/1234").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get().getStatus();
        assertEquals(status, 404);

        // should be 400
        status = client.target("http://localhost:" + getPort()).path("/GetLT/ ").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get().getStatus();
        assertEquals(status, 400);

        // should be found
        Response responseFound = client.target("http://localhost:" + getPort()).path("/GetLT/5678/false").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get();

        status = responseFound.getStatus();
        assertEquals(status, 200);

        Map<String, Lt> map = responseFound.readEntity(new GenericType<Map<String, Lt>>() {
        });
        assertEquals(map.get("5678").getNoLt(), "5678");
    }

    @Test
    public void testRecherchePlusieursLt() {
        Client client = ClientBuilder.newClient();

        Mockito.reset(serviceMock);

        Map<String, Lt> mapLTABC123 = new HashMap<>();
        Lt ltABC = new Lt();
        ltABC.setNoLt("ABC");
        mapLTABC123.put("ABC", ltABC);
        Lt lt123 = new Lt();
        lt123.setNoLt("123");
        mapLTABC123.put("123", lt123);

        Mockito.when(
                serviceMock.getLtsFromDatabase(Mockito.eq(Arrays.asList("ABC", "123")),
                        Mockito.eq(ETraitementSynonymes.PAS_DE_RESOLUTION_DES_SYNONYMES))).thenReturn(mapLTABC123);

        // shouldn't be found
        Response responseFound = client.target("http://localhost:" + getPort()).path("/GetLTs").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(Arrays.asList("2345", "EIF"), MediaType.APPLICATION_JSON_TYPE));

        int status = responseFound.getStatus();
        assertEquals(status, 200);
        Map<String, Lt> map = responseFound.readEntity(new GenericType<Map<String, Lt>>() {
        });
        assertNotNull(map);
        assertEquals(map.size(), 0);

        // should be found
        responseFound = client.target("http://localhost:" + getPort()).path("/GetLTs/false").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(Arrays.asList("ABC", "123"), MediaType.APPLICATION_JSON_TYPE));

        status = responseFound.getStatus();
        assertEquals(status, 200);
        map = responseFound.readEntity(new GenericType<Map<String, Lt>>() {
        });
        assertNotNull(map);
        assertEquals(map.size(), 2);

        ltABC = map.get("ABC");
        assertEquals(ltABC.getNoLt(), "ABC");

        lt123 = map.get("123");
        assertEquals(lt123.getNoLt(), "123");

        // bad request pas de LT passÃ©
        responseFound = client.target("http://localhost:" + getPort()).path("/GetLTs").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(Arrays.asList(), MediaType.APPLICATION_JSON_TYPE));

        status = responseFound.getStatus();
        assertEquals(status, 400);

        // bad request pas de LT passÃ©
        responseFound = client.target("http://localhost:" + getPort()).path("/GetLTs").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(null, MediaType.APPLICATION_JSON_TYPE));

        status = responseFound.getStatus();
        assertEquals(status, 400);
    }

    @Test
    public void testSynonymieMonoLT() {
        Client client = ClientBuilder.newClient();

        Mockito.reset(serviceMock);

        Map<String, Lt> mapLtSansSyn = new HashMap<>();
        Lt ltEsclave = new Lt();
        ltEsclave.setNoLt("SYNESCLAVE");
        mapLtSansSyn.put("SYNESCLAVE", ltEsclave);

        Mockito.when(
                serviceMock.getLtsFromDatabase(Mockito.eq(Arrays.asList("SYNESCLAVE")),
                        Mockito.eq(ETraitementSynonymes.PAS_DE_RESOLUTION_DES_SYNONYMES))).thenReturn(mapLtSansSyn);

        Map<String, Lt> mapLtAvecSyn = new HashMap<>();
        Lt ltMaitre = new Lt();
        ltMaitre.setNoLt("SYNMAITRE");
        mapLtAvecSyn.put("SYNESCLAVE", ltEsclave);
        mapLtAvecSyn.put("SYNMAITRE", ltMaitre);

        Mockito.when(
                serviceMock.getLtsFromDatabase(Mockito.eq(Arrays.asList("SYNESCLAVE")),
                        Mockito.eq(ETraitementSynonymes.RESOLUTION_DES_SYNONYMES))).thenReturn(mapLtAvecSyn);

        // shouldn't be found
        int status = client.target("http://localhost:" + getPort()).path("/GetLT/1234/true").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get().getStatus();
        assertEquals(status, 404);

        // should be 400
        status = client.target("http://localhost:" + getPort()).path("/GetLT/ / ").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get().getStatus();
        assertEquals(status, 400);

        // should be found with maitre
        Response responseFound = client.target("http://localhost:" + getPort()).path("/GetLT/SYNESCLAVE/true").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get();

        status = responseFound.getStatus();
        assertEquals(status, 200);

        Map<String, Lt> map = responseFound.readEntity(new GenericType<Map<String, Lt>>() {
        });
        assertNotNull(map);
        assertEquals(map.size(), 2);
        ltEsclave = map.get("SYNESCLAVE");
        assertNotNull(ltEsclave);
        assertEquals(ltEsclave.getNoLt(), "SYNESCLAVE");
        ltMaitre = map.get("SYNMAITRE");
        assertNotNull(ltMaitre);
        assertEquals(ltMaitre.getNoLt(), "SYNMAITRE");

        // should be found without maitre
        responseFound = client.target("http://localhost:" + getPort()).path("/GetLT/SYNESCLAVE/ ").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get();

        status = responseFound.getStatus();
        assertEquals(status, 200);

        map = responseFound.readEntity(new GenericType<Map<String, Lt>>() {
        });
        assertNotNull(map);
        assertEquals(map.size(), 1);
        ltEsclave = map.get("SYNESCLAVE");
        assertNotNull(ltEsclave);
        assertEquals(ltEsclave.getNoLt(), "SYNESCLAVE");

        // without maitre as well
        responseFound = client.target("http://localhost:" + getPort()).path("/GetLT/SYNESCLAVE/false").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get();

        status = responseFound.getStatus();
        assertEquals(status, 200);

        map = responseFound.readEntity(new GenericType<Map<String, Lt>>() {
        });
        assertNotNull(map);
        assertEquals(map.size(), 1);
        ltEsclave = map.get("SYNESCLAVE");
        assertNotNull(ltEsclave);
        assertEquals(ltEsclave.getNoLt(), "SYNESCLAVE");
    }

    @Test
    public void testSynonymieMultiLT() {
        // throw new RuntimeException("Test not implemented");
    }

    @Test
    public void testRechercheLtParAdresseEmailDesti() {
        Client client = ClientBuilder.newClient();

        Mockito.reset(serviceMock);

        Map<String, Lt> mapLtResultat = new HashMap<>();
        Lt lt1 = new Lt().setNoLt("EE000000001FR").setEmail1Destinataire("adejanovski")
                .setEmail2Destinataire("pmail.com");
        Lt lt2 = new Lt().setNoLt("EE000000002FR").setEmail1Destinataire("adejanovski")
                .setEmail2Destinataire("pmail.com");
        mapLtResultat.put("EE000000001FR", lt1);
        mapLtResultat.put("EE000000002FR", lt2);

        Date dateDeb = new Date();
        Date dateFin = new Date();

        Mockito.when(
                serviceMock.getLtsParEmailDestinataire(Mockito.eq("adejanovski@pmail.com"), Mockito.any(Date.class),
                        Mockito.any(Date.class))).thenReturn(mapLtResultat);

        RechercheLtParEmailDestiInput paramRecherche = new RechercheLtParEmailDestiInput("adejanovski@pmail.com",
                dateDeb, dateFin);

        Response responseFound = client.target("http://localhost:" + getPort()).path("/RechercheLtParEmailDesti")
                .request().accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(paramRecherche, MediaType.APPLICATION_JSON_TYPE));

        int status = responseFound.getStatus();
        assertEquals(status, HttpStatus.SC_OK);

        Mockito.verify(serviceMock, Mockito.times(1)).getLtsParEmailDestinataire(Mockito.eq("adejanovski@pmail.com"),
                Mockito.any(Date.class), Mockito.any(Date.class));
    }

    /**
     * Test passant avec utilisation du SDK.
     * 
     * @throws TimeoutException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     * @throws NotFoundException 
     * @throws ServiceUnavailableException 
     * @throws TechnicalException 
     */
    @Test
    public void testRechercheLtParAdresseEmailDestiSdk() throws IOException, InterruptedException, ExecutionException,
            TimeoutException, TechnicalException, ServiceUnavailableException, NotFoundException {

        GetLtParEmailDestiV1.getInstance().setEndpoint("http://localhost:" + getPort()).setTimeout(1000);

        Mockito.reset(serviceMock);

        Map<String, Lt> mapLtResultat = new HashMap<>();
        Lt lt1 = new Lt().setNoLt("EE000000001FR").setEmail1Destinataire("adejanovski")
                .setEmail2Destinataire("pmail.com");
        Lt lt2 = new Lt().setNoLt("EE000000002FR").setEmail1Destinataire("adejanovski")
                .setEmail2Destinataire("pmail.com");
        mapLtResultat.put("EE000000001FR", lt1);
        mapLtResultat.put("EE000000002FR", lt2);

        Date dateDeb = new Date();
        Date dateFin = new Date();

        Mockito.when(
                serviceMock.getLtsParEmailDestinataire(Mockito.eq("adejanovski@pmail.com"), Mockito.any(Date.class),
                        Mockito.any(Date.class))).thenReturn(mapLtResultat);

        RechercheLtParEmailDestiInput paramRecherche = new RechercheLtParEmailDestiInput("adejanovski@pmail.com",
                dateDeb, dateFin);

        Map<String, Lt> result = GetLtParEmailDestiV1.getInstance().getLtsParEmailDestiService(paramRecherche);
        assertNotNull(result);
        assertEquals(result.keySet().size(), 2);

        Mockito.verify(serviceMock, Mockito.times(1)).getLtsParEmailDestinataire(Mockito.eq("adejanovski@pmail.com"),
                Mockito.any(Date.class), Mockito.any(Date.class));
    }

    /**
     * Verification du comportement de la ressource quand le service lève une
     * exception.
     */
    @Test
    public void testRechercheLtParAdresseEmailDestiException() {
        Client client = ClientBuilder.newClient();

        Mockito.reset(serviceMock);

        Map<String, Lt> mapLtResultat = new HashMap<>();
        Lt lt1 = new Lt().setNoLt("EE000000001FR").setEmail1Destinataire("adejanovski")
                .setEmail2Destinataire("pmail.com");
        Lt lt2 = new Lt().setNoLt("EE000000002FR").setEmail1Destinataire("adejanovski")
                .setEmail2Destinataire("pmail.com");
        mapLtResultat.put("EE000000001FR", lt1);
        mapLtResultat.put("EE000000002FR", lt2);

        Date dateDeb = new Date();
        Date dateFin = new Date();

        Mockito.when(
                serviceMock.getLtsParEmailDestinataire(Mockito.eq("adejanovski@pmail.com"), Mockito.any(Date.class),
                        Mockito.any(Date.class))).thenThrow(new MSTechnicalException("Erreur très très grave"));

        RechercheLtParEmailDestiInput paramRecherche = new RechercheLtParEmailDestiInput("adejanovski@pmail.com",
                dateDeb, dateFin);

        Response responseFound = client.target("http://localhost:" + getPort()).path("/RechercheLtParEmailDesti")
                .request().accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(paramRecherche, MediaType.APPLICATION_JSON_TYPE));

        int status = responseFound.getStatus();
        // On vérifie qu'on a bien une INTERNAL_SERVER_ERROR
        assertEquals(status, HttpStatus.SC_INTERNAL_SERVER_ERROR);

        Mockito.verify(serviceMock, Mockito.times(1)).getLtsParEmailDestinataire(Mockito.eq("adejanovski@pmail.com"),
                Mockito.any(Date.class), Mockito.any(Date.class));
    }

    /**
     * Test sur les cas non passants. Quand l'un des paramètres est null, le
     * service doit retourner un status 400 (BAD_REQUEST)
     */
    @Test
    public void testRechercheLtParAdresseEmailDestiFailure() {
        Client client = ClientBuilder.newClient();

        Mockito.reset(serviceMock);

        Map<String, Lt> mapLtResultat = new HashMap<>();
        Lt lt1 = new Lt().setNoLt("EE000000001FR").setEmail1Destinataire("adejanovski")
                .setEmail2Destinataire("pmail.com");
        Lt lt2 = new Lt().setNoLt("EE000000002FR").setEmail1Destinataire("adejanovski")
                .setEmail2Destinataire("pmail.com");
        mapLtResultat.put("EE000000001FR", lt1);
        mapLtResultat.put("EE000000002FR", lt2);

        Date dateDeb = new Date();
        Date dateFin = new Date();

        Mockito.when(
                serviceMock.getLtsParEmailDestinataire(Mockito.eq("adejanovski@pmail.com"), Mockito.any(Date.class),
                        Mockito.any(Date.class))).thenReturn(mapLtResultat);

        // Test erreur avec date debut null
        RechercheLtParEmailDestiInput paramRecherche = new RechercheLtParEmailDestiInput("adejanovski@pmail.com", null,
                dateFin);

        Response responseFound = client.target("http://localhost:" + getPort()).path("/RechercheLtParEmailDesti")
                .request().accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(paramRecherche, MediaType.APPLICATION_JSON_TYPE));

        int status = responseFound.getStatus();
        assertEquals(status, HttpStatus.SC_BAD_REQUEST);

        // Test erreur avec date fin null
        paramRecherche = new RechercheLtParEmailDestiInput("adejanovski@pmail.com", dateDeb, null);

        responseFound = client.target("http://localhost:" + getPort()).path("/RechercheLtParEmailDesti").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(paramRecherche, MediaType.APPLICATION_JSON_TYPE));

        status = responseFound.getStatus();
        assertEquals(status, HttpStatus.SC_BAD_REQUEST);

        // Test erreur avec adresse email null
        paramRecherche = new RechercheLtParEmailDestiInput(null, dateDeb, dateFin);

        responseFound = client.target("http://localhost:" + getPort()).path("/RechercheLtParEmailDesti").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(paramRecherche, MediaType.APPLICATION_JSON_TYPE));

        status = responseFound.getStatus();
        assertEquals(status, HttpStatus.SC_BAD_REQUEST);

        // Vérification que la ressource a bien détecté les problème et n'a pas
        // fait d'appel au service.
        Mockito.verify(serviceMock, Mockito.times(0)).getLtsParEmailDestinataire(Mockito.eq("adejanovski@pmail.com"),
                Mockito.any(Date.class), Mockito.any(Date.class));
    }
}
