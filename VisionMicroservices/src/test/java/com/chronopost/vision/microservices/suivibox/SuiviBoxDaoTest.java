package com.chronopost.vision.microservices.suivibox;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.joda.time.DateTime;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.cassandra.request.builder.TTL;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

/** @author unknown : JJC getSession */
public class SuiviBoxDaoTest {

    private static final SimpleDateFormat formatDateCassandra = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private boolean suiteLaunch = true;

	private PreparedStatement psCleanBoxAgence;

    private SuiviBoxDaoImpl dao;

    private final String idBox1 = "EE000000001FR";
    private final String idBox2 = "EE000000002FR";

    private final String date1 = "2015-04-13 22:05:34";
    private final String date2 = "1970-01-01 00:00:00";

    private final String agence1 = "75012";
    private final String agence2 = "94700";

    /**
     * @return VisionMicroserviceApplication.cassandraSession (a
     *         com.datastax.driver.core )
     */
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }
    
    @BeforeClass
    public void setUp() throws Exception {
        if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }
        CCMBridge.ipOfNode(1);

        psCleanBoxAgence = getSession().prepare("delete from boxagence where id_box = ?");

        dao = SuiviBoxDaoImpl.getInstance();

    }

    /**
     * Que test-on ici ?
     */
    @Test(groups = { "database-needed", "slow" })
    public void updateAgenceBox() {

        // initialisations
        Statement statement = null;
        ResultSet execute = null;
        Row one = null;

        Date dateDernierEvt1 = (new DateTime(2015, 4, 13, 22, 5, 34)).toDate();
        Date dateDernierEvt2 = (new DateTime(1970, 1, 1, 0, 0, 0)).toDate();

        getSession().execute("INSERT INTO boxagence (id_box, date_dernier_evt, pc_agence) values ('" + idBox1 + "', '"
                + date1 + "', '" + agence1 + "');");
        getSession().execute("INSERT INTO boxagence (id_box, date_dernier_evt, pc_agence) values ('" + idBox2 + "', '"
                + date2 + "', '" + agence2 + "');");

        statement = new SimpleStatement("select pc_agence from boxagence where id_box='" + idBox1
                + "' and date_dernier_evt='" + formatDateCassandra.format(dateDernierEvt1) + "';");
        //statement.setConsistencyLevel(ConsistencyLevel.ONE);
        execute = getSession().execute(statement);
        one = execute.one();

        assertNotNull(execute);
        assertEquals(one.getString(0), agence1);

        statement = new SimpleStatement("select pc_agence from boxagence where id_box='" + idBox2
                + "' and date_dernier_evt='" + formatDateCassandra.format(dateDernierEvt2) + "';");
        //statement.setConsistencyLevel(ConsistencyLevel.ONE);
        execute = getSession().execute(statement);
        one = execute.one();

        assertNotNull(execute);
        assertEquals(one.getString(0), agence2);

        assertEquals(
                dao.updateAgenceBox(
                		Arrays.asList
                				( new SuiviBoxAgence()
                				. setIdBox(idBox1)
                				. setDateDernierEvt(dateDernierEvt1)
                				. setPcAgence(agence2)
                				. setAction("Action1")
                				. setEtape("Etape1")
                				. setCodeTournee("TourneeT1")
                				. setCodeLR("LR1")
                				)), true);

        statement = new SimpleStatement("select pc_agence,action,etape,code_tournee,code_lr from vision.boxagence where id_box='" + idBox1
                + "' and date_dernier_evt='" + formatDateCassandra.format(dateDernierEvt1) + "';");
        statement.setConsistencyLevel(ConsistencyLevel.ONE);
        execute = getSession().execute(statement);
        one = execute.one();

        assertNotNull(execute);
        assertEquals(one.getString("pc_agence"), agence2);
        assertEquals(one.getString("action"), "Action1");
        assertEquals(one.getString("etape"), "Etape1");
        assertEquals(one.getString("code_tournee"), "TourneeT1");
        assertEquals(one.getString("code_lr"), "LR1");

        /* Test que le TTL est bien positionné et à la bonne valeur */
        statement = new SimpleStatement("select TTL(pc_agence) from vision.boxagence where id_box='" + idBox1
                + "' and date_dernier_evt='" + formatDateCassandra.format(dateDernierEvt1) + "';");
        statement.setConsistencyLevel(ConsistencyLevel.ONE);
        execute = getSession().execute(statement);
        one = execute.one();

        assertNotNull(execute);
        assertTrue(one.getInt(0) > TTL.SUIVIBOX.getTimelapse() - 10);

        statement = new SimpleStatement("select pc_agence from vision.boxagence where id_box='" + idBox2
                + "' and date_dernier_evt='" + formatDateCassandra.format(dateDernierEvt2) + "';");
        statement.setConsistencyLevel(ConsistencyLevel.ONE);
        execute = getSession().execute(statement);
        one = execute.one();

        assertNotNull(execute);
        assertEquals(one.getString(0), agence2);

    }

    /**
     * Lorsque l'objet SuiviBoxAgence est vide, on doit avoir un false en retour.
     */
    @Test(groups = { "database-needed", "slow" }, expectedExceptions = MSTechnicalException.class)
    public void updateAgenceBox2() {
        assertEquals(dao.updateAgenceBox(Arrays.asList(new SuiviBoxAgence())), false);
    }

    @AfterClass
    public void tearDownAfterClass() throws Exception {
    	getSession().execute(psCleanBoxAgence.bind(idBox1));
    	getSession().execute(psCleanBoxAgence.bind(idBox2));
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }
}
