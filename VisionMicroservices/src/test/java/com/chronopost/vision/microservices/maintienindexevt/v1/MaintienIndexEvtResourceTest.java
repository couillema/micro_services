package com.chronopost.vision.microservices.maintienindexevt.v1;

import static org.testng.Assert.assertEquals;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.model.maintienIndexEvt.v1.MaintienIndexEvtInput;
import com.chronopost.vision.model.maintienIndexEvt.v1.MaintienIndexEvtOutput;

public class MaintienIndexEvtResourceTest extends JerseyTestNg.ContainerPerClassTest {
    /**
     * Mocking the service
     */
	private static IMaintienIndexEvtService serviceMock = Mockito.mock(IMaintienIndexEvtService.class);
    private Client client;

    /**
     * Binding the service to the instantiation of the resource
     */
    protected Application configure() {

        MaintienIndexEvtResource resourceMaintienIndexEvtResource = new MaintienIndexEvtResource();
        resourceMaintienIndexEvtResource.setService(serviceMock);

        forceSet(TestProperties.CONTAINER_PORT, "0");

        ResourceConfig config = new ResourceConfig();
        config.register(resourceMaintienIndexEvtResource);

        return config;
    }

    @BeforeClass
    public void setUp() throws Exception {

        super.setUp();

        client = ClientBuilder.newClient();
    }

    /**
     * Appel correct de la ressource. Vérification du retour 200 et que le
     * service a été appelé une fois.
     * 
     * @throws Exception
     */
    @Test
    public void testResource() throws Exception {

        Mockito.reset(serviceMock);

        Mockito.when(serviceMock.maintienIndexEvt(Mockito.any(MaintienIndexEvtInput.class))).thenReturn(
                new MaintienIndexEvtOutput().setSuccess(true));
        MaintienIndexEvtInput input = new MaintienIndexEvtInput();
        int status = client.target("http://localhost:" + getPort()).path("/MaintienIndexEvt/v1").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(input, MediaType.APPLICATION_JSON_TYPE))
                .getStatus();

        assertEquals(status, 200);

        // Vérification de l'appel à la méthode updateTournee du service, avec
        // les 2 événements
        Mockito.verify(serviceMock).maintienIndexEvt(Mockito.any(MaintienIndexEvtInput.class));

    }

    /**
     * Appel de la ressource avec un objet du mauvais type. Vérification du
     * retour 500 et que le service n'a pas été appelé.
     * 
     * @throws Exception
     */
/*
 * Le test n'a plus lieu d'être à cause de l'attribut @JsonIgnoreProperties(ignoreUnknown=true)
 *  
    @Test
    public void testResourceFail() throws Exception {

        Mockito.reset(serviceMock);

        Mockito.when(serviceMock.maintienIndexEvt(Mockito.any(MaintienIndexEvtInput.class))).thenReturn(
                new MaintienIndexEvtOutput().setSuccess(true));
        //MaintienIndexEvtInput input = new MaintienIndexEvtInput();
        
        int status = client.target("http://localhost:" + getPort()).path("/MaintienIndexEvt/v1").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(new MaintienIndexEvtOutput().setSuccess(true), MediaType.APPLICATION_JSON_TYPE))
                .getStatus();

        assertEquals(status, 500);

        // Vérification de l'appel à la méthode updateTournee du service, avec
        // les 2 événements
        Mockito.verify(serviceMock, Mockito.never()).maintienIndexEvt(Mockito.any(MaintienIndexEvtInput.class));

    }
*/
}
