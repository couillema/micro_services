package com.chronopost.vision.microservices.suivibox;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.EInfoComp;
import com.chronopost.vision.model.Evt;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/** @author unknown JJC change one call getCassandraSession() **/
public class SuiviBoxAcceptanceTest {

    private boolean suiteLaunch = true;

    private SuiviboxService service;

	private PreparedStatement psCleanBoxAgence;

    @BeforeClass
    public void setUp() throws Exception {

        if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }
        CCMBridge.ipOfNode(1);

        service = new SuiviboxServiceImpl(SuiviBoxDaoImpl.getInstance());

        psCleanBoxAgence = getSession().prepare("delete from boxagence where id_box = ?");
    }
    
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

    /**
     * On insère une LT en base via le service et on vérifie les valeurs lues
     * dans la base.
     * 
     * @throws FunctionalException
     * @throws MSTechnicalException
     * 
     */
    @Test(groups = { "database-needed", "slow", "acceptance" })
    public void cas1Test1() throws MSTechnicalException, FunctionalException {

        List<Evt> evts = new ArrayList<Evt>();

        Evt evt1 = new Evt();
        evt1.setNoLt("TEST_NOGC_NOPROD");
        evt1.setDateEvt(new DateTime(2015, 1, 1, 0, 0, 0).toDate());
        evt1.setLieuEvt("75998");
        evt1.setCodeEvt("PS");
        evt1.setProdNoLt(0);
        evts.add(evt1);

        Evt evt2 = new Evt();
        evt2.setNoLt("TEST_GC_NOPROD");
        evt2.setDateEvt(new DateTime(2015, 1, 2, 0, 0, 0).toDate());
        evt2.setLieuEvt("75998");
        evt2.setCodeEvt("GC");
        evt2.setProdNoLt(0);
        evts.add(evt2);

        Evt evt3 = new Evt();
        evt3.setNoLt("TEST_NOGC_PROD");
        evt3.setDateEvt(new DateTime(2015, 2, 1, 0, 0, 0).toDate());
        evt3.setLieuEvt("75998");
        evt3.setCodeEvt("PS");
        evt3.setProdNoLt(20);
        evts.add(evt3);

        HashMap<String, String> infoscomp = new HashMap<>();
        infoscomp.put(EInfoComp.ACTION_CONTENANT.getCode(), "Action2");
        infoscomp.put(EInfoComp.ETAPE_CONTENANT.getCode(), "Etape2");
        infoscomp.put(EInfoComp.CODE_TOURNEE.getCode(), "Tournee2");
        infoscomp.put(EInfoComp.CODE_LIGNE_ROUTIERE.getCode(), "LR2");
        Evt evt4 = new Evt();
        evt4.setNoLt("TEST_GC_PROD");
        evt4.setDateEvt(new DateTime(2015, 2, 2, 0, 0, 0).toDate());
        evt4.setLieuEvt("75998");
        evt4.setCodeEvt("GC");
        evt4.setProdNoLt(20);
        evt4.setInfoscomp(infoscomp);
        evts.add(evt4);

        boolean result = service.insertEvtGCInDatabase(evts);
        assertTrue(result);

        ResultSet ltResult = VisionMicroserviceApplication
                .getCassandraSession()
                .execute(
                        "SELECT id_box, date_dernier_evt, pc_agence,etape FROM boxagence WHERE id_box IN ('TEST_NOGC_NOPROD', 'TEST_GC_NOPROD', 'TEST_NOGC_PROD', 'TEST_GC_PROD')");

        List<Row> all = ltResult.all();
        assertEquals(1, all.size());
        Row row = all.get(0);

        assertEquals(row.getString("id_box"), "TEST_GC_PROD");
        assertEquals(row.getTimestamp("date_dernier_evt"), new DateTime(2015, 2, 2, 0, 0, 0).toDate());
        assertEquals(row.getString("pc_agence"), "75998");
        assertEquals(row.getString("etape"), "Etape2");

    }

    @AfterClass
    public void tearDownAfterClass() throws Exception {
    	getSession().execute(psCleanBoxAgence.bind("TEST_GC_PROD"));
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }

}
