package com.chronopost.vision.microservices;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.configuration.MapConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.sdk.utils.ServiceMockResponses;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.netflix.config.ConfigurationManager;

/** @author JJC : call setSession a revoir ?? **/
public class BuildCluster {

	private final static Logger logger = LoggerFactory.getLogger(BuildCluster.class);

	public static String HOST = System.getProperty("host", ConnectionDetails.getHost());
	private static CCMBridge ccmBridge = null;
	private static Cluster cluster = null;
	private static Session session = null;
	private static Session session2 = null;
	public static Session sessionFluks = null;
	public static boolean clusterHasBuilt = false;
	private static boolean dynamicCluster = false;

	static{
		HashMap<String, Object> configHystrix = new HashMap<String, Object>();
		configHystrix.put("hystrix.command.ColioutaiPointCommand.execution.isolation.thread.timeoutInMilliseconds", 10000);
		configHystrix.put("hystrix.threadpool.ColioutaiPointCommand.queueSizeRejectionThreshold", 10000);
		configHystrix.put("hystrix.threadpool.ColioutaiPointCommand.maxQueueSize", 10000);
		configHystrix.put("hystrix.threadpool.ColioutaiPointCommand.coreSize", 30);
		
		configHystrix.put("hystrix.command.ColioutaiLtCommand.execution.isolation.thread.timeoutInMilliseconds", 10000);
		configHystrix.put("hystrix.threadpool.ColioutaiLtCommand.queueSizeRejectionThreshold", 10000);
		configHystrix.put("hystrix.threadpool.ColioutaiLtCommand.maxQueueSize", 10000);
		configHystrix.put("hystrix.threadpool.ColioutaiLtCommand.coreSize", 30);
		
		ConfigurationManager.install(new MapConfiguration(configHystrix));
	}
	
	@BeforeSuite(groups = { "init" })
	public static void setUpBeforeSuite() throws Exception {
		logger.info("OPENING CASSANDRA CONNECTION");
		System.setProperty("cassandra.version", "2.1.8");
		System.setProperty("ipprefix", "127.0.0.");
		if (!isClusterActive()) {
			ccmBridge = CCMBridge.create("vision_cluster" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()),
					1, 1);
			ccmBridge.waitForUp(1);
			// ccmBridge.waitForUp(2);
			HOST = CCMBridge.ipOfNode(1);
			dynamicCluster = true;
		} else {
			HOST = "127.0.0.1";
		}

		clusterHasBuilt = true;

		Builder builder = Cluster.builder().withQueryOptions(new QueryOptions()
				.setConsistencyLevel(ConsistencyLevel.QUORUM).setSerialConsistencyLevel(ConsistencyLevel.LOCAL_SERIAL));
		cluster = builder.addContactPoint(HOST).build();
		session = cluster.connect();

		initKeyspace();

		session2 = cluster.connect("vision");
		VisionMicroserviceApplication.setCassandraSession(session2);
		// Thread.sleep(3000);
		initSchema("vision");

		sessionFluks = cluster.connect("fluks");

		sessionFluks.execute(
				"CREATE TABLE IF NOT EXISTS transco (flux text,nom text,entree text,date_maj timestamp,sortie text, PRIMARY KEY (flux, nom, entree))");
		sessionFluks.execute("truncate transco");
	}
	
	private static void initKeyspace() {
		logger.info("Creation des keyspaces vision et fluks");
		session.execute(
				" CREATE KEYSPACE IF NOT EXISTS vision WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1}");
		session.execute(
				" CREATE KEYSPACE IF NOT EXISTS fluks WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1}");
	}
	
	private static void initSchema(String keyspace) throws FileNotFoundException, InterruptedException {
		String cqlScript = ServiceMockResponses.readResponse(keyspace + ".cql");
		for (String query : cqlScript.split(";")) {
			if (query.trim().length() > 1) {
				try {
					logger.info(query);
					VisionMicroserviceApplication.getCassandraSession().execute(query);
				} catch (Exception e) {
					if (!query.toUpperCase().trim().startsWith("ALTER")) {
						logger.error("KO : " + query, e);
						throw new MSTechnicalException(e);
					}
				}
			}
		}
		// ------ Vidage des tables
		final String[] tableNamesToEmpty = new String[]  
				{"actionbox" 
				,"actionsonde" 
				,"activitebox" 
				,"agence" 
				,"agence_tournee" 
				,"alaskacurrentdc" 
				,"alertesonde" 
				,"box" 
				,"boxagence" 
				,"categoriebox" 
				,"colis_agence" 
				,"colis_specifications"
				,"colis_tournee_agence" 
				,"compteursinjecteur" 
				,"compteurssonde" 
				,"date_livraison_estimee_lt" 
				,"depassement_proactif_par_jour" 
				,"evt" 
				,"idx_alertesonde_id" 
				,"info_tournee" 
				,"logalaska" 
				,"lt" 
				,"lt_rupturefroid" 
				,"parametrealaska" 
				,"seuilfroid" 
				,"sonde" 
				,"synonyme" 
				,"temperature" 
				,"tlt_version" 
				,"tournee_c11" 
				,"tournee_commentaire" 
				,"tournees" 
				,"traces_date_proactif" 
				,"typefroid" 
				,"tournees_par_code_service" 
				,"word_index" 
				,"lt_avec_creneau_par_agence" 
				,"traces_date_proactif" 
				,"depassement_proactif_par_jour" 
		} ; 
		for (String tablename : tableNamesToEmpty) { // NO TEST OF ERROR ???
			VisionMicroserviceApplication.getCassandraSession().execute("truncate " + tablename);
		}
	}
	
	@AfterSuite(groups={"init"})
	public static void tearDownAfterSuite() throws Exception {
		logger.info("CLOSING CASSANDRA CONNECTION");
		session.close();
		session2.close();
		sessionFluks.close();
		cluster.close();
		if (dynamicCluster) {
			logger.info("Stopping nodes");
			clusterHasBuilt = false;
			try {
				ccmBridge.forceStop();
				logger.info("Discarding cluster");
				ccmBridge.remove();
				HOST = System.getProperty("host", ConnectionDetails.getHost());
			} catch (Exception e) {
				logger.info("Silent error discarding cluster");
			}
		}
	}
	
	
	public static boolean isClusterActive() {
		try {
			Builder builder = Cluster.builder()
					.withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.QUORUM)
							.setSerialConsistencyLevel(ConsistencyLevel.LOCAL_SERIAL));
			cluster = builder.addContactPoint("127.0.0.1").build();
			session = cluster.connect();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
