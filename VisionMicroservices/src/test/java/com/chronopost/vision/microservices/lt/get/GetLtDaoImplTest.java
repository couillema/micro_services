package com.chronopost.vision.microservices.lt.get;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.lt.get.GetLtDaoImpl;
import com.chronopost.vision.microservices.lt.get.GetLtBuilder;
import com.chronopost.vision.model.Lt;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

/** @author unknown : JJC getSession +  LOGGER import min.**/
public class GetLtDaoImplTest {

	/** format de date limité au jour */
	private static final SimpleDateFormat FORMAT_JOUR = new SimpleDateFormat("dd/MM/yyyy");
	
    private boolean suiteLaunch = true;
    private IGetLtDao ltDao;

	private PreparedStatement psCleanLt;
	private PreparedStatement psCleanWordIndex;

	/** @return  VisionMicroserviceApplication.cassandraSession  (a com.datastax.driver.core )  */
    private static final Session getSession() { return VisionMicroserviceApplication.getCassandraSession() ; }

    @BeforeClass
    public void setUp() throws Exception {

        if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }
        
        psCleanLt = getSession().prepare("delete from lt where no_lt in ('XY642265463EE', 'XY642265464EE', '1234');");
        psCleanWordIndex = getSession().prepare("delete from word_index where table_ressource = 'lt' and champ = 'email_1_destinataire' and word = 'franck.bleuzen';");
    
        CCMBridge.ipOfNode(1);
    }

    @Test(groups = { "database-needed", "slow" })
    public void getLtFromDatabase() {
        ltDao = GetLtDaoImpl.getInstance().setLtBuilder(new GetLtBuilder());

        getSession().execute("insert into lt (no_lt) values ('1234')");

        Map<String, Lt> mapLt = ltDao.getLtsFromDatabase(Arrays.asList("1234"));

        assertNotNull(mapLt);
        assertNotNull(mapLt.get("1234"));
        assertEquals(mapLt.get("1234").getNoLt(), "1234");
    }
    
    @Test(groups = { "database-needed", "slow" })
    /**
     * Les colonnes noLt, dateLivraisonPrevue doivent être présentes dans la LtSmall.
     * 
     */
    public void getLtSmallFromDatabase() {
        ltDao = GetLtDaoImpl.getInstance().setLtBuilder(new GetLtBuilder());

        getSession().execute("insert into lt (no_lt,date_livraison_prevue) values ('1234',DATEOF(NOW()))");

        Map<String, Lt> mapLt = ltDao.getLtsFromDatabase(Arrays.asList("1234"),Boolean.TRUE);

        assertNotNull(mapLt);
        assertNotNull(mapLt.get("1234"));
        assertEquals(mapLt.get("1234").getNoLt(), "1234");
        assertEquals(FORMAT_JOUR.format(mapLt.get("1234").getDateLivraisonPrevue()), FORMAT_JOUR.format(new Date()));
    }
    
    @Test(groups = { "database-needed", "slow" })
    public void getLtParAdresseEmail() {
        // LEAK Session session = getSession();
        ltDao = GetLtDaoImpl.getInstance().setLtBuilder(new GetLtBuilder());

        getSession().execute("INSERT INTO word_index(table_ressource,champ,word,date_ressource,id_ressource) VALUES ('lt','email_1_destinataire','franck.bleuzen','2015-10-24 10:00','XY642265464EE')");
        getSession().execute("INSERT INTO word_index(table_ressource,champ,word,date_ressource,id_ressource) VALUES ('lt','email_1_destinataire','franck.bleuzen','2015-10-24 10:00','XY642265463EE')");
        getSession().execute("INSERT INTO lt(no_lt,email_1_destinataire,email_2_destinataire) VALUES ('XY642265464EE','franck.bleuzen','vinci-construction.fr');");
        getSession().execute("INSERT INTO lt(no_lt,email_1_destinataire,email_2_destinataire) VALUES ('XY642265463EE','franck.bleuzen','gmail.com');");
        
        List<String> lts = ltDao.rechercheLt("email_1_destinataire", "franck.bleuzen", new DateTime().withYear(2015).withMonthOfYear(10).withDayOfMonth(23).withHourOfDay(0).toDate(), new DateTime().withYear(2015).withMonthOfYear(10).withDayOfMonth(24).withHourOfDay(23).toDate());

        assertNotNull(lts);
        assertEquals(lts.size(),2);
        assertTrue(lts.contains("XY642265464EE"));
        assertTrue(lts.contains("XY642265463EE"));
    }
    
    @AfterClass
    public void tearDownAfterClass() throws Exception {
    	getSession().execute(psCleanLt.getQueryString());
    	getSession().execute(psCleanWordIndex.getQueryString());
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }
}
