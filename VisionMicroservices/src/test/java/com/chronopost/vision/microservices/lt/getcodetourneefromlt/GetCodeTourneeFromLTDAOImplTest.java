package com.chronopost.vision.microservices.lt.getcodetourneefromlt;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.sql.Timestamp;
import java.util.Date;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.GetCodeTourneeFromLTResponse;
import com.datastax.driver.core.Session;

/** @author unknown : JJC getSession . **/
public class GetCodeTourneeFromLTDAOImplTest {

    private boolean suiteLaunch = true;
    private GetCodeTourneeFromLTDAOImpl dao;

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
        CCMBridge.ipOfNode(1);

        dao = (GetCodeTourneeFromLTDAOImpl) GetCodeTourneeFromLTDAOImpl.getInstance();

        // LEAK Session session =
        // VisionMicroserviceApplication.cassandraSession;
        getSession().execute(
                "insert into colis_tournee_agence (numero_lt, date_maj, id_c11, id_tournee) values (?,?,?,?)", "1234",
                new Timestamp(50000L), "1", "AAA11111");
        getSession().execute(
                "insert into colis_tournee_agence (numero_lt, date_maj, id_c11, id_tournee) values (?,?,?,?)", "1234",
                new Timestamp(60000L), "2", "AAA22222");
        getSession().execute(
                "insert into colis_tournee_agence (numero_lt, date_maj, id_c11, id_tournee) values (?,?,?,?)", "1234",
                new Timestamp(70000L), "3", "BBB33333");
        getSession().execute(
                "insert into colis_tournee_agence (numero_lt, date_maj, id_c11, id_tournee) values (?,?,?,?)", "5678",
                new Timestamp(50000L), "2", "CCC44444");

    }

    @Test(groups = { "database-needed", "slow" })
    public void testNoTournee() {

        assertNull(dao.findTourneeBy("1234", new Timestamp(40000L)));

    }

    @Test(groups = { "database-needed", "slow" })
    public void testTourneeAfterDate() {

        GetCodeTourneeFromLTResponse model = dao.findTourneeBy("1234", new Date(90000L));
        assertEquals(model.getCodeAgence(), "BBB");
        assertEquals(model.getCodeTournee(), "33333");
    }

    @Test(groups = { "database-needed", "slow" })
    public void testTourneeIntermediaire() {

        GetCodeTourneeFromLTResponse model = dao.findTourneeBy("1234", new Date(65000L));
        assertEquals(model.getCodeAgence(), "AAA");
        assertEquals(model.getCodeTournee(), "22222");
    }

    @AfterClass(groups = { "init" })
    public void tearDownAfterClass() throws Exception {
        getSession().execute("truncate colis_tournee_agence");
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }
}
