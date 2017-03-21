package com.chronopost.vision.microservices.tournee.updatetournee.v1;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Evt;

public class UpdateTourneeResourceTest extends JerseyTestNg.ContainerPerClassTest {

    /**
     * Mocking the service
     */
    private static IUpdateTourneeService serviceMock = Mockito.mock(IUpdateTourneeService.class);
    private Client client;

    /**
     * Binding the service to the instantiation of the resource
     */
    @Override
    protected Application configure() {
        UpdateTourneeResource resourceUpdateTournee = new UpdateTourneeResource();
        resourceUpdateTournee.setService(serviceMock);

        forceSet(TestProperties.CONTAINER_PORT, "0");

        ResourceConfig config = new ResourceConfig();
        config.register(resourceUpdateTournee);

        return config;
    }

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();
        client = ClientBuilder.newClient();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testResource() throws Exception {

        Mockito.reset(serviceMock);
        Mockito.when(serviceMock.updateTournee(Mockito.anyList())).thenReturn(true);

        Evt evt1 = new Evt().setNoLt("lt1").setDateEvt(new Date()).setCodeEvt("D");
        Evt evt2 = new Evt().setNoLt("lt2").setDateEvt(new Date()).setCodeEvt("D");
        int status = client.target("http://localhost:" + getPort()).path("/UpdateTournee/v1/").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(Arrays.asList(evt1, evt2), MediaType.APPLICATION_JSON_TYPE)).getStatus();

        assertEquals(status, 200);

        // Vérification de l'appel à la méthode updateTournee du service, avec
        // les 2 événements
        Mockito.verify(serviceMock).updateTournee(Mockito.argThat(new EvtListMatcher()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testResourceKo() throws Exception {

        Mockito.reset(serviceMock);
        Mockito.when(serviceMock.updateTournee(Mockito.anyList())).thenThrow(new MSTechnicalException("erreur"));

        Evt evt1 = new Evt().setNoLt("lt1").setDateEvt(new Date()).setCodeEvt("D");
        Evt evt2 = new Evt().setNoLt("lt2").setDateEvt(new Date()).setCodeEvt("D");
        int status = client.target("http://localhost:" + getPort()).path("/UpdateTournee/v1/").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(Arrays.asList(evt1, evt2), MediaType.APPLICATION_JSON_TYPE)).getStatus();

        assertEquals(status, 500);
    }

    // Matcher pour vérifier que l'appel à service.updateTournee s'est fait avec
    // les bons arguments
    private class EvtListMatcher extends ArgumentMatcher<ArrayList<Evt>> {

        @Override
        public boolean matches(Object argument) {
            @SuppressWarnings("unchecked")
            ArrayList<Evt> evtList = ((ArrayList<Evt>) argument);
            return evtList.size() == 2 && evtList.get(0).getNoLt().equals("lt1")
                    && evtList.get(1).getNoLt().equals("lt2");
        }
    }
}
