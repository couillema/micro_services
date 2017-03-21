package com.chronopost.vision.microservices.suivibox;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.ConnectionDetails;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.Evt;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class SuiviboxServiceTest {

    private static final SimpleDateFormat formatDateCassandra = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private boolean suiteLaunch=true;
    private static Session session;
    
    private SuiviboxServiceImpl service;

	private PreparedStatement psCleanBoxAgence;

    private static final String CODE_EVT_GC = "GC";
    private static final int CODE_PROD_NO_LT = 20;

	private String idBox1;

	private String idBox3;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        System.setProperty("cassandra.version", "2.1.8");

        if (BuildCluster.HOST.equals(System.getProperty("host", ConnectionDetails.getHost()))) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }
        CCMBridge.ipOfNode(1);

        psCleanBoxAgence = getSession().prepare("delete from boxagence where id_box = ?");

        session = VisionMicroserviceApplication.getCassandraSession() ;        
        service = new SuiviboxServiceImpl(SuiviBoxDaoImpl.getInstance()) ;
    }

    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }
   
    /**
     * On insere une ligne GC et on vérifie que l'insertion s'est bien passée 
     */
    @Test(groups = { "database-needed", "slow" })
    public void insertEvtGCInDatabase1() {
        idBox1 = "majSuiviBox1";
        Date dateDernierEvt = new DateTime(2015,5,6,11,23,45).toDate() ; 
        String agence = "94700" ;
        
        Evt evt = new Evt() ;
        evt.setNoLt(idBox1) ;
        evt.setDateEvt(dateDernierEvt) ;
        evt.setLieuEvt(agence) ;
        evt.setCodeEvt(CODE_EVT_GC) ;
        evt.setProdNoLt(CODE_PROD_NO_LT) ;

        List<Evt> liste = new ArrayList<Evt>() ;
        liste.add(evt) ;
        
        assertTrue(service.insertEvtGCInDatabase(liste)) ;
        
        // initialisations
        Statement statement = null ;
        ResultSet execute = null; 
        Row one = null;

        statement = new SimpleStatement("select * from boxagence where id_box='" + idBox1 + "' and date_dernier_evt='" + formatDateCassandra.format(dateDernierEvt) + "';");
        statement.setConsistencyLevel(ConsistencyLevel.ONE);
        execute = session.execute(statement);
        one = execute.one();

        assertNotNull(execute);
        assertEquals(one.getString("pc_agence"), agence);
    }

    /**
     * On vérifie qu'on a bien évité l'insertion d'un événement NON GC 
     */
    @Test(groups = { "database-needed", "slow" })
    public void insertEvtGCInDatabase2() {

        String idBox = "majSuiviBox2" ;
        Date dateDernierEvt = new DateTime(2015,5,6,11,23,45).toDate() ; 
        String agence = "94700" ;
        
        Evt evt1 = new Evt() ;
        evt1.setNoLt(idBox) ;
        evt1.setDateEvt(dateDernierEvt) ;
        evt1.setLieuEvt(agence) ;
        evt1.setCodeEvt("PS") ;
        evt1.setProdNoLt(CODE_PROD_NO_LT) ;

        List<Evt> liste1 = new ArrayList<Evt>() ;
        liste1.add(evt1) ;
        
        assertTrue(service.insertEvtGCInDatabase(liste1)) ;
        
        // initialisations
        Statement statement = null ;
        ResultSet execute = null; 
        Row one = null;

        statement = new SimpleStatement("select * from boxagence where id_box='" + idBox + "' and date_dernier_evt='" + formatDateCassandra.format(dateDernierEvt) + "';");
        statement.setConsistencyLevel(ConsistencyLevel.ONE);
        execute = session.execute(statement);
        one = execute.one();

        assertNotNull(execute);
        assertNull(one) ;
    }

    /**
     * mise à jour d'une événement non GC. On vérifie que les données n'ont pas été modifiées
     */
    @Test(groups = { "database-needed", "slow" })
    public void insertEvtGCInDatabase3() {
        // initialisations
        Statement statement = null ;
        ResultSet execute = null; 
        Row one = null;

        idBox3 = "majSuiviBox3";
        Date dateDernierEvt = new DateTime(2015,5,6,11,23,45).toDate() ; 
        String agence1 = "94700" ;
        String agence2 = "75012" ;

        session.execute("INSERT INTO boxagence (id_box, date_dernier_evt, pc_agence) values ('" + idBox3 + "', '" + formatDateCassandra.format(dateDernierEvt) + "', '" + agence1 + "');");

        Evt evt3 = new Evt() ;
        evt3.setNoLt(idBox3) ;
        evt3.setDateEvt(dateDernierEvt) ;
        evt3.setLieuEvt(agence2) ;
        evt3.setCodeEvt("PS") ;
        evt3.setProdNoLt(CODE_PROD_NO_LT) ;

        List<Evt> liste3 = new ArrayList<Evt>() ;
        liste3.add(evt3) ;
        assertTrue(service.insertEvtGCInDatabase(liste3)) ;

        statement = new SimpleStatement("select * from boxagence where id_box='" + idBox3 + "' and date_dernier_evt='" + formatDateCassandra.format(dateDernierEvt) + "';");
        statement.setConsistencyLevel(ConsistencyLevel.ONE);
        execute = session.execute(statement);
        one = execute.one();

        assertNotNull(execute);
        assertNotNull(one) ;
        assertEquals(one.getString("pc_agence"), agence1);
    }
    
    @AfterClass
    public void tearDownAfterClass() throws Exception {
    	getSession().execute(psCleanBoxAgence.bind(idBox1));
    	getSession().execute(psCleanBoxAgence.bind(idBox3));
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
            BuildCluster.clusterHasBuilt = false;
        }
    }
}
