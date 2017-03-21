package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.getsynthesetournees.v1.InfoTournee;
import com.chronopost.vision.model.getsynthesetournees.v1.PointTournee;
import com.chronopost.vision.model.getsynthesetournees.v1.SyntheseTourneeQuantite;

/**
 * Test de la ressource SyntheseTournee
 * 
 * @author jcbontemps
 */
public class SyntheseTourneeResourceTest extends JerseyTestNg.ContainerPerClassTest {

    private static SyntheseTourneeResource resource = null;
    private static SyntheseTourneeService serviceMock;
    private Client client;

    @SuppressWarnings("unchecked")
	private void nouveauMockService() throws InterruptedException, ExecutionException {
        serviceMock = mock(SyntheseTourneeService.class);
        resource.setService(serviceMock);
        when(serviceMock.getSyntheseTourneeActivite(Mockito.anyString())).thenReturn(new ArrayList<PointTournee>());
        when(serviceMock.getSyntheseTourneeQuantite(Mockito.anyList())).thenReturn(new HashMap<String, SyntheseTourneeQuantite>());
        when(serviceMock.getSyntheseTourneeActiviteEtQuantite(Mockito.anyString())).thenReturn(new InfoTournee());
    }

    @Override
    protected Application configure() {
        /* Création de la resource et initialisation avec le service mocké */
        resource = new SyntheseTourneeResource();
        resource.setService(serviceMock);

        forceSet(TestProperties.CONTAINER_PORT, "0");

        ResourceConfig config = new ResourceConfig();
        config.register(resource);
        return config;
    }

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();
        client = ClientBuilder.newClient();
        nouveauMockService();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getSyntheseTourneeQuantite() throws InterruptedException, ExecutionException {
        /* initialisation des variables à fournir au service */
        List<String> idTournee = new ArrayList<String>();
        /* Test de l'appel */
        Entity<List<String>> inputEntity = Entity.entity(idTournee, MediaType.APPLICATION_JSON);

        Response e = client.target("http://localhost:" + getPort()).path("/getSyntheseTournee/Quantite").request().accept(MediaType.APPLICATION_JSON_TYPE)
                .post(inputEntity);
        int status = e.getStatus();
        assertEquals(status, 200);

        verify(serviceMock, times(1)).getSyntheseTourneeQuantite(Mockito.anyList());
    }

    @Test
    public void getSyntheseTourneeActivite() throws InterruptedException, ExecutionException {
        /* initialisation des variables à fournir au service */
        List<String> idTournee = new ArrayList<String>();
        /* Test de l'appel */
        Entity<List<String>> inputEntity = Entity.entity(idTournee, MediaType.APPLICATION_JSON);

        Response e = client.target("http://localhost:" + getPort()).path("/getSyntheseTournee/Activite").request().accept(MediaType.APPLICATION_JSON_TYPE)
                .post(inputEntity);

        int status = e.getStatus();
        assertEquals(status, 200);
        verify(serviceMock, times(1)).getSyntheseTourneeActivite(Mockito.anyString());
    }

    @Test
    public void getSyntheseTourneeActiviteEtQuantite() throws InterruptedException, ExecutionException {
    	// WHEN
        final Response resp = client.target("http://localhost:" + getPort()).path("/getSyntheseTournee/ActiviteEtQuantite").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity("abc", MediaType.TEXT_PLAIN));
        // THEN
        assertEquals(resp.getStatus(), 200);
        verify(serviceMock, times(1)).getSyntheseTourneeActiviteEtQuantite("abc");
    }

    @Test
    public void getSyntheseTourneeActivitesEtQuantites() {
    	final List<String> idList = Arrays.asList("456");
    	// WHEN
        when(serviceMock.getSyntheseTourneeActivitesEtQuantites(idList)).thenReturn(new HashMap<String, InfoTournee>());
    	final Response resp = client.target("http://localhost:" + getPort()).path("/getSyntheseTournee/ActivitesEtQuantites").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(idList, MediaType.APPLICATION_JSON));
        // THEN
        assertEquals(resp.getStatus(), 200);
        verify(serviceMock, times(1)).getSyntheseTourneeActivitesEtQuantites(Mockito.anyListOf(String.class));
    }

    @Test
    public void getSyntheseTourneeActivitesEtQuantites_withTechnicalException() throws InterruptedException, ExecutionException {
    	final List<String> idList = Arrays.asList("123");
		// WHEN
        when(serviceMock.getSyntheseTourneeActivitesEtQuantites(idList)).thenThrow(new MSTechnicalException("Oh la grosse exception !!"));
        final Response resp = client.target("http://localhost:" + getPort()).path("/getSyntheseTournee/ActivitesEtQuantites").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(idList, MediaType.APPLICATION_JSON));
        
        // THEN
        assertEquals(resp.getStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        final MSTechnicalException entity = resp.readEntity(MSTechnicalException.class);
        assertEquals(entity.getMessage(), "Oh la grosse exception !!");
    }

    @Test
    public void getSyntheseTourneeActivitesEtQuantites_Exception() throws InterruptedException, ExecutionException {
    	final List<String> idList = Arrays.asList("456");
    	// WHEN
        when(serviceMock.getSyntheseTourneeActivitesEtQuantites(idList)).thenThrow(new IllegalArgumentException("Oh la grosse exception !!"));
        final Response resp = client.target("http://localhost:" + getPort()).path("/getSyntheseTournee/ActivitesEtQuantites").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(idList, MediaType.APPLICATION_JSON));
        
        // THEN
        assertEquals(resp.getStatus(), HttpStatus.SC_NOT_FOUND);
        final Exception entity = resp.readEntity(Exception.class);
        assertEquals(entity.getMessage(), "Oh la grosse exception !!");
    }
}
