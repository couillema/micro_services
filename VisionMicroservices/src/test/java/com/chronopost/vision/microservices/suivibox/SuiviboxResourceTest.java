package com.chronopost.vision.microservices.suivibox;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.sdk.SuiviBoxV1;
import com.chronopost.vision.microservices.sdk.exception.NotFoundException;
import com.chronopost.vision.microservices.sdk.exception.ServiceUnavailableException;
import com.chronopost.vision.microservices.sdk.exception.TechnicalException;
import com.chronopost.vision.model.Evt;

public class SuiviboxResourceTest extends JerseyTestNg.ContainerPerClassTest {

    /**
     * Mocking the service
     */
    private static SuiviboxService serviceMock;

    /**
     * Binding the service to the instantiation of the resource
     */
    @Override
    protected Application configure() {
        SuiviboxResource resourceSuivibox = new SuiviboxResource();
        serviceMock = new InsertSuiviboxServiceMock();
        resourceSuivibox.setService(serviceMock);

        forceSet(TestProperties.CONTAINER_PORT, "0");

        ResourceConfig config = new ResourceConfig();
        config.register(resourceSuivibox);

        return config;
    }

    @Test
    public void testInsertion() throws TechnicalException, ServiceUnavailableException, NotFoundException {
        Evt evt1 = new Evt();
        evt1.setNoLt("test1");
        evt1.setLieuEvt("94700");
        evt1.setCodeEvt("GC");
        evt1.setProdNoLt(20);

        SuiviBoxV1.getInstance().setEndpoint("http://localhost:" + getPort());

        assertTrue(SuiviBoxV1.getInstance().insertGC(Arrays.asList(evt1)).booleanValue());

        evt1.setCodeEvt("PS");
        evt1.setProdNoLt(20);

        /*
         * Response response2 = client.target("http://localhost:"+
         * getPort()).path("/SuiviBox/").request()
         * .accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(evts,
         * MediaType.APPLICATION_JSON));
         */

        assertFalse(SuiviBoxV1.getInstance().insertGC(Arrays.asList(evt1)).booleanValue());
        // assertEquals(response2.getStatus() ,
        // Status.BAD_REQUEST.getStatusCode()) ;
    }

    private class InsertSuiviboxServiceMock implements SuiviboxService {
        @Override
        public boolean insertEvtGCInDatabase(List<Evt> evts) {
            for (Evt evt : evts)
                if (!Utils.isEvenementGC(evt)) return false;
            return true;
        }
    }
}
