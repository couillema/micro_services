package com.chronopost.vision.microservices.colioutai.log.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.text.ParseException;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.IndiceConfiance;
import com.chronopost.vision.model.colioutai.ColioutaiInfoLT;
import com.chronopost.vision.model.colioutai.ColioutaiLog;
import com.chronopost.vision.model.rules.DateRules;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

/** @author unknown : JJC getSession **/
public class ColioutaiLogDaoTest {
	
	private boolean suiteLaunch = true;
	private ColioutaiLogDao dao;
	private ColioutaiInfoLT colioutaiInfoLT;

	/** @return  VisionMicroserviceApplication.cassandraSession  (a com.datastax.driver.core )  */
    private static final Session getSession() { return VisionMicroserviceApplication.getCassandraSession() ; }

	private PreparedStatement psCleanColisOuTaiLog;

	@BeforeClass
	public void setUpBeforeClass() throws Exception {
		if (!BuildCluster.clusterHasBuilt) {
			BuildCluster.setUpBeforeSuite();
			suiteLaunch = false;
		}
        
		psCleanColisOuTaiLog = getSession().prepare("delete from colioutai_log where type_log = 'TC'");
		
		dao = ColioutaiLogDaoImpl.getInstance();
		init();
		dao.insertLog(colioutaiInfoLT);
	}
	
	private void init() {
		colioutaiInfoLT = new ColioutaiInfoLT();
		colioutaiInfoLT.setTypeLog("TC");
		colioutaiInfoLT.setCodeAgence("codeAgence");
		colioutaiInfoLT.setCodeTournee("codeTournee");
		colioutaiInfoLT.setCreneau("creneau");
		colioutaiInfoLT.setEtaInitial("etaInitial");
		colioutaiInfoLT.setEtaMaj("etaMaj");
		colioutaiInfoLT.setIndiceConfiance(IndiceConfiance.A);
		colioutaiInfoLT.setPositionTournee(Integer.valueOf(1));
	}

	@Test(groups = "slow")
	public void insertLog(){
		ResultSet execute = null;
        Row one = null;
        
        Statement statement ;
        statement = new SimpleStatement("select * from colioutai_log where type_log = 'TC';");
        statement.setConsistencyLevel(ConsistencyLevel.QUORUM);
        execute = getSession().execute(statement);
        one = execute.one();
        assertNotNull(execute);
        assertEquals(one.getMap("detail", String.class, String.class).get("codeAgence"),"codeAgence");
	}
	
	@Test(groups = "slow")
	public void getColioutaiLog() throws MSTechnicalException, ParseException{
		getSession().execute("INSERT INTO colioutai_log (type_log, date_heure_log, detail) values ('TC','2016-01-21 10:00:00',{'codeAgence':'TEST','codeTournee':'12345','creneau':'10:22/11:22','etaInitial':'10:52','etaMaj':'10:32','indiceConfiance':'D','ip':'127.0.0.1','no_LT':'XW003325006JB','numDossier':'','positionTournee':'49','typeUser':'TC'});");
		getSession().execute("INSERT INTO colioutai_log (type_log, date_heure_log, detail) values ('TC','2016-01-21 11:00:00',{'codeAgence':'TEST','codeTournee':'12345','creneau':'10:22/11:22','etaInitial':'10:52','etaMaj':'10:32','indiceConfiance':'D','ip':'127.0.0.1','no_LT':'XW003325006JB','numDossier':'','positionTournee':'49','typeUser':'TC'});");
		getSession().execute("INSERT INTO colioutai_log (type_log, date_heure_log, detail) values ('TC','2016-01-22 12:00:00',{'codeAgence':'TEST','codeTournee':'12345','creneau':'10:22/11:22','etaInitial':'10:52','etaMaj':'10:32','indiceConfiance':'D','ip':'127.0.0.1','no_LT':'XW003325006JB','numDossier':'','positionTournee':'49','typeUser':'TC'});");
		getSession().execute("INSERT INTO colioutai_log (type_log, date_heure_log, detail) values ('TC','2016-01-23 00:00:00',{'codeAgence':'TEST','codeTournee':'12345','creneau':'10:22/11:22','etaInitial':'10:52','etaMaj':'10:32','indiceConfiance':'D','ip':'127.0.0.1','no_LT':'XW003325006JB','numDossier':'','positionTournee':'49','typeUser':'TC'});");
		
		List<ColioutaiLog> list = dao.getColioutaiLog("TC",DateRules.fromDateSortable("2016-01-21"), DateRules.fromDateSortable("2016-01-22"));
		assertNotNull(list);
		assertEquals(list.size(), 3);
	}
	
	@AfterClass
    public void tearDownAfterClass() throws Exception {
		getSession().execute(psCleanColisOuTaiLog.getQueryString());
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }
}
