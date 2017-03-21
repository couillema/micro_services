package com.chronopost.vision.microservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;

import org.apache.commons.configuration.MapConfiguration;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.cassandra.request.builder.TTL;
import com.chronopost.cassandra.table.ETableMicroServiceCounters;
import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.CacheManagerService;
import com.chronopost.vision.cachemanager.agence.AgenceDaoImpl;
import com.chronopost.vision.cachemanager.codeservice.CodeServiceDaoImpl;
import com.chronopost.vision.cachemanager.evenement.EvenementDaoImpl;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.cachemanager.parametre.ParametreDaoImpl;
import com.chronopost.vision.cachemanager.refcontrat.RefContrat;
import com.chronopost.vision.cachemanager.refcontrat.RefContratDaoImpl;
import com.chronopost.vision.microservices.colioutai.get.ColioutaiResource;
import com.chronopost.vision.microservices.colioutai.get.services.ColioutaiServiceImpl;
import com.chronopost.vision.microservices.colioutai.get.services.GoogleGeocoderHelper;
import com.chronopost.vision.microservices.colioutai.log.dao.ColioutaiLogDaoImpl;
import com.chronopost.vision.microservices.diffusionevt.v1.DiffusionEvtResource;
import com.chronopost.vision.microservices.diffusionevt.v1.DiffusionEvtServiceImpl;
import com.chronopost.vision.microservices.featureflips.FeatureFlipsResource;
import com.chronopost.vision.microservices.genereevt.GenereEvtResource;
import com.chronopost.vision.microservices.genereevt.GenereEvtServiceImpl;
import com.chronopost.vision.microservices.getC11.GetC11DaoImpl;
import com.chronopost.vision.microservices.getC11.GetC11Resource;
import com.chronopost.vision.microservices.getC11.GetC11ServiceImpl;
import com.chronopost.vision.microservices.getEvts.GetEvtsDaoImpl;
import com.chronopost.vision.microservices.getEvts.GetEvtsResource;
import com.chronopost.vision.microservices.getEvts.GetEvtsServiceImpl;
import com.chronopost.vision.microservices.getsyntheseagence.v1.SyntheseAgenceDaoImpl;
import com.chronopost.vision.microservices.getsyntheseagence.v1.SyntheseAgenceResource;
import com.chronopost.vision.microservices.getsyntheseagence.v1.SyntheseAgenceServiceImpl;
import com.chronopost.vision.microservices.healthcheck.CalculRetardServiceHealthCheck;
import com.chronopost.vision.microservices.healthcheck.CassandraHealthCheck;
import com.chronopost.vision.microservices.healthcheck.ColioutaiInfoV1HealthCheck;
import com.chronopost.vision.microservices.healthcheck.ConsigneServiceHealthCheck;
import com.chronopost.vision.microservices.healthcheck.DiffusionEvtV1HealthCheck;
import com.chronopost.vision.microservices.healthcheck.GenereEvtV1HealthCheck;
import com.chronopost.vision.microservices.healthcheck.GetCodeTourneeFromLtV1HealthCheck;
import com.chronopost.vision.microservices.healthcheck.GetDetailTourneeV1HealthCheck;
import com.chronopost.vision.microservices.healthcheck.GetLtV1HealthCheck;
import com.chronopost.vision.microservices.healthcheck.GoogleServiceHealthCheck;
import com.chronopost.vision.microservices.healthcheck.InsertAgenceColisV1HealthCheck;
import com.chronopost.vision.microservices.healthcheck.InsertEvtV1HealthCheck;
import com.chronopost.vision.microservices.healthcheck.InsertPointTourneeV1HealthCheck;
import com.chronopost.vision.microservices.healthcheck.PoiServiceHealthCheck;
import com.chronopost.vision.microservices.healthcheck.PtvServiceHealthCheck;
import com.chronopost.vision.microservices.healthcheck.SuiviBoxV1HealthCheck;
import com.chronopost.vision.microservices.healthcheck.SyntheseAgenceHealthCheck;
import com.chronopost.vision.microservices.healthcheck.SyntheseTourneeV1HealthCheck;
import com.chronopost.vision.microservices.healthcheck.TraitementRetardV1HealthCheck;
import com.chronopost.vision.microservices.healthcheck.UpdateTourneeV1HealthCheck;
import com.chronopost.vision.microservices.healthcheck.view.HealthCheckResource;
import com.chronopost.vision.microservices.insertAlerte.v1.InsertAlerteDaoImpl;
import com.chronopost.vision.microservices.insertAlerte.v1.InsertAlerteResource;
import com.chronopost.vision.microservices.insertAlerte.v1.InsertAlerteServiceImpl;
import com.chronopost.vision.microservices.insertC11.InsertC11DaoImpl;
import com.chronopost.vision.microservices.insertC11.InsertC11Resource;
import com.chronopost.vision.microservices.insertC11.InsertC11ServiceImpl;
import com.chronopost.vision.microservices.insertagencecolis.v1.InsertAgenceColisDaoImpl;
import com.chronopost.vision.microservices.insertagencecolis.v1.InsertAgenceColisResource;
import com.chronopost.vision.microservices.insertagencecolis.v1.InsertAgenceColisServiceImpl;
import com.chronopost.vision.microservices.insertevt.v1.InsertEvtDaoImpl;
import com.chronopost.vision.microservices.insertevt.v1.InsertEvtResource;
import com.chronopost.vision.microservices.insertevt.v1.InsertEvtServiceImpl;
import com.chronopost.vision.microservices.insertpointtournee.v1.InsertPointTourneeDaoImpl;
import com.chronopost.vision.microservices.insertpointtournee.v1.InsertPointTourneeResource;
import com.chronopost.vision.microservices.insertpointtournee.v1.InsertPointTourneeServiceImpl;
import com.chronopost.vision.microservices.lt.get.GetLtBuilder;
import com.chronopost.vision.microservices.lt.get.GetLtDaoImpl;
import com.chronopost.vision.microservices.lt.get.GetLtResource;
import com.chronopost.vision.microservices.lt.get.GetLtServiceImpl;
import com.chronopost.vision.microservices.lt.getcodetourneefromlt.GetCodeTourneeFromLTDAOImpl;
import com.chronopost.vision.microservices.lt.getcodetourneefromlt.GetCodeTourneeFromLTResource;
import com.chronopost.vision.microservices.lt.getcodetourneefromlt.GetCodeTourneeFromLTServiceImpl;
import com.chronopost.vision.microservices.lt.insert.InsertLtDAO;
import com.chronopost.vision.microservices.lt.insert.InsertLtResource;
import com.chronopost.vision.microservices.lt.insert.InsertLtServiceImpl;
import com.chronopost.vision.microservices.maintienindexevt.v1.MaintienIndexEvtDaoImpl;
import com.chronopost.vision.microservices.maintienindexevt.v1.MaintienIndexEvtResource;
import com.chronopost.vision.microservices.maintienindexevt.v1.MaintienIndexEvtServiceImpl;
import com.chronopost.vision.microservices.reference.ReferenceResource;
import com.chronopost.vision.microservices.reference.ReferenceServiceImpl;
import com.chronopost.vision.microservices.sdk.GetCodeTourneeFromLtV1;
import com.chronopost.vision.microservices.sdk.GetDetailTourneeV1;
import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.microservices.suivibox.SuiviBoxDaoImpl;
import com.chronopost.vision.microservices.suivibox.SuiviboxResource;
import com.chronopost.vision.microservices.suivibox.SuiviboxServiceImpl;
import com.chronopost.vision.microservices.supervision.SupervisionDaoImpl;
import com.chronopost.vision.microservices.supervision.SupervisionResource;
import com.chronopost.vision.microservices.supervision.SupervisionServiceImpl;
import com.chronopost.vision.microservices.tournee.getalertestournees.v1.GetAlertesTourneesDaoImpl;
import com.chronopost.vision.microservices.tournee.getalertestournees.v1.GetAlertesTourneesResource;
import com.chronopost.vision.microservices.tournee.getalertestournees.v1.GetAlertesTourneesServiceImpl;
import com.chronopost.vision.microservices.tournee.getdetailtournee.v1.GetDetailTourneeDaoImpl;
import com.chronopost.vision.microservices.tournee.getdetailtournee.v1.GetDetailTourneeResource;
import com.chronopost.vision.microservices.tournee.getdetailtournee.v1.GetDetailTourneeServiceImpl;
import com.chronopost.vision.microservices.tournee.getsynthesetournees.v1.SyntheseTourneeDaoImpl;
import com.chronopost.vision.microservices.tournee.getsynthesetournees.v1.SyntheseTourneeResource;
import com.chronopost.vision.microservices.tournee.getsynthesetournees.v1.SyntheseTourneeServiceImpl;
import com.chronopost.vision.microservices.tournee.updatetournee.v1.UpdateTourneeDaoImpl;
import com.chronopost.vision.microservices.tournee.updatetournee.v1.UpdateTourneeResource;
import com.chronopost.vision.microservices.tournee.updatetournee.v1.UpdateTourneeServiceImpl;
import com.chronopost.vision.microservices.traitementRetard.TraitementRetardDaoImpl;
import com.chronopost.vision.microservices.traitementRetard.TraitementRetardResource;
import com.chronopost.vision.microservices.traitementRetard.TraitementRetardServiceImpl;
import com.chronopost.vision.microservices.transcos.TranscoResource;
import com.chronopost.vision.microservices.updatereferentiel.ReferentielVisionDaoImpl;
import com.chronopost.vision.microservices.updatereferentiel.UpdateReferentielResource;
import com.chronopost.vision.microservices.updatereferentiel.UpdateReferentielServiceImpl;
import com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisDaoImpl;
import com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisResource;
import com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisServiceImpl;
import com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisTranscoderConsignesImpl;
import com.chronopost.vision.microservices.utils.ErrorMessageBodyWriter;
import com.chronopost.vision.microservices.utils.ServiceInitializer;
import com.chronopost.vision.model.Agence;
import com.chronopost.vision.model.CodeService;
import com.chronopost.vision.model.Evenement;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.dao.TranscoderDao;
import com.codahale.metrics.JmxReporter;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.contrib.codahalemetricspublisher.HystrixCodaHaleMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;

import de.thomaskrille.dropwizard_template_config.TemplateConfigBundle;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class VisionMicroserviceApplication extends Application<VisionMicroserviceConfiguration> {

    /**
     * ATTENTION use getCassandraSession() Should be private : use
     * getCassandraSession()
     **/
    private static Session cassandraSession;
    private static VisionMicroserviceConfiguration config;
	private static final Logger logger = LoggerFactory.getLogger(VisionMicroserviceApplication.class);
    private static final String TOKEN_CODE = "cql_token";
    private static final String TOKEN_VALUE = "isRunning";
    
    /* Resources */
    private GetLtResource resourceGetLts;
    private GetDetailTourneeResource resourceGetDetailTournee;
    private InsertLtResource resourceInsertLts;
    private InsertEvtResource resourceInsertEvts;
    private UpdateTourneeResource resourceUpdateTournee;
    private GenereEvtResource resourceGenereEvt;
    private GetCodeTourneeFromLTResource resourceCodeTourneeResource;
    private ColioutaiResource resourceColioutai;
    private com.chronopost.vision.microservices.colioutai.get.v2.ColioutaiResource resourceColioutaiv2;
    private SuiviboxResource resourceSuiviBox;
    private FeatureFlipsResource resourceFeatureFlips;
    private GetAlertesTourneesResource resourceGetAlertesTournees;
    private MaintienIndexEvtResource resourceMaintienIndexEvt;
    private TraitementRetardResource resourceTraitementRetard;
    private UpdateReferentielResource updateReferentiel;
    private SyntheseTourneeResource syntheseTournee; 
    private InsertAgenceColisResource resourceInsertAgenceColis;
    private InsertPointTourneeResource resourceInsertPointTournee;
    private UpdateSpecificationsColisResource resourceUpdateSpecificationsColis;
    private TranscoResource resourceTransco;
    private DiffusionEvtResource diffusionEvtResource;
    private SyntheseAgenceResource syntheseAgenceResouce;
    private HealthCheckResource resourceHealthCheck;
    private InsertC11Resource insertC11Resource;
    private GetC11Resource getC11Resource;
    private SupervisionResource supervisionResource;
    private ReferenceResource referenceResource;
    private GetEvtsResource getEvtsResource;
    private InsertAlerteResource insertAlerteResource;
    
    private MSResponseFilter responseFilter;
    
    public static void main(String[] args) throws Exception {
        new VisionMicroserviceApplication().run(args);
    }

    /** @return the static VisionMicroserviceApplication.cassandraSession. */ 
    public static Session getCassandraSession() {
        return cassandraSession;
    }

    /**
     * @param in
     *            will replace the static
     *            VisionMicroserviceApplication.cassandraSession
     * @return the set session ( in in fact
     */
    public static Session setCassandraSession(final Session in) {
        cassandraSession = in;
        return in;
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<VisionMicroserviceConfiguration> bootstrap) {
        bootstrap.addBundle(new TemplateConfigBundle());
        bootstrap.addBundle(new AssetsBundle("/assets", "/static", "index.html"));
        bootstrap.addBundle(new ViewBundle<VisionMicroserviceConfiguration>());

        bootstrap.addBundle(new SwaggerBundle<VisionMicroserviceConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
                    VisionMicroserviceConfiguration configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });
    }

    @Override
    /**
     * Méthode de démarrage du microservice
     */
    public void run(VisionMicroserviceConfiguration configuration, Environment environment) throws Exception {
    	/* Configuration des servlet jetty */
        setConfig(configuration);
        configureCors(environment);

        /* init Trancodification (fluks) et session Vision */
        initTranscodification(configuration,environment);
        initSessionCassandraVision(configuration,environment);

        /* init des metrics et de l'application */
        initMetrics(environment);
        initDropWizard(configuration);
        
        /* init du schema de BDD */
        initModeleBDD(configuration); 

        // Init des paramètres Hystrix
        ConfigurationManager.install(new MapConfiguration(configuration.getDefaultHystrixConfig()));

        /* Injection des endpoint et des timeout dans les singletons des microServices et du SDK */
        ServiceInitializer.setEndpointsAndTimeouts(configuration);

        /* creation des resources, des cachesManager et injection du tout */
        initResources();
        initCacheManager();
        injections(configuration);
        injectionColioutai(configuration);
        
        /* creation et enregistrement des healthchecks */
        initHealthCheck(environment);
        
        /* enregistrement des resources */
        registerResources(environment);
        
    }

	private void registerResources(Environment environment) {
		// Enregistrement de tous les services exposés
        environment.jersey().register(new MSRequestFilter());
        environment.jersey().register(responseFilter);
        environment.jersey().register(resourceColioutai);
        environment.jersey().register(resourceColioutaiv2);
        environment.jersey().register(resourceGetLts);
        environment.jersey().register(resourceInsertEvts);
        environment.jersey().register(resourceInsertLts);
        environment.jersey().register(resourceUpdateTournee);
        environment.jersey().register(resourceCodeTourneeResource);
        environment.jersey().register(resourceGetDetailTournee);
        environment.jersey().register(resourceSuiviBox);
        environment.jersey().register(resourceFeatureFlips);
        environment.jersey().register(resourceHealthCheck);
        environment.jersey().register(resourceGenereEvt);
        environment.jersey().register(resourceGetAlertesTournees);
        environment.jersey().register(resourceMaintienIndexEvt);
        environment.jersey().register(resourceTraitementRetard);
        environment.jersey().register(resourceTransco);
        environment.jersey().register(updateReferentiel);
        environment.jersey().register(syntheseTournee);
        environment.jersey().register(resourceInsertPointTournee);
        environment.jersey().register(resourceInsertAgenceColis);
        environment.jersey().register(diffusionEvtResource);
        environment.jersey().register(resourceUpdateSpecificationsColis);
        environment.jersey().register(syntheseAgenceResouce);
        environment.jersey().register(insertC11Resource);
        environment.jersey().register(getC11Resource);
        environment.jersey().register(supervisionResource);
        environment.jersey().register(referenceResource);
        environment.jersey().register(getEvtsResource);
        environment.jersey().register(insertAlerteResource);
        
    }

	/**
     * Configuration des filtres jetty
     * @param environment
     *
     * @author LGY
     */
	private void configureCors(Environment environment) {
        Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
        filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
        filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        filter.setInitParameter("allowedHeaders",
                "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        filter.setInitParameter("allowCredentials", "true");
    }

    /**
     * Insert un jeton dans la table parametre pour empêcher de lancer plusieurs
     * fois l'exécution du cql lors du lancement de multiple instance des microservices
     * 
     * @return true si l'insertion a réussi ou si la requête lève une exception
     *         InvalidQueryException (la table parametre n'existe probablement
     *         pas encore) sinon false
     */
    private boolean canExecuteCQL() {
        boolean canExecuteCQL = false;

        try {
            PreparedStatement insertToken = getCassandraSession().prepare(
                    "INSERT INTO parametre (code, valeur) VALUES (?, ?) IF NOT EXISTS USING TTL " + TTL.CQL_TOKEN.getTimelapse());
            canExecuteCQL = getCassandraSession().execute(insertToken.bind(TOKEN_CODE, TOKEN_VALUE)).wasApplied();
        } catch (InvalidQueryException e) {
            logger.info("Une erreur est survenue lors de la tentative d'insertion du jeton pour empêcher de lancer le cql plusieurs fois : {}",
                    e.getMessage());
            canExecuteCQL = true;
        }

        return canExecuteCQL;
    }

    /**
     * Supprime le jeton d'execution du script cql.
     * Seul le process ayant eu le jeton doit le supprimer.
     * Les autres process attende cette suppression pour terminer leur initialisation.
     */
    private void releaseTokenCQL() {
        try {
            PreparedStatement insertToken = getCassandraSession().prepare(
                    "DELETE from parametre WHERE code=?");
            getCassandraSession().execute(insertToken.bind(TOKEN_CODE));
            logger.info("Token CQL relaché");
        } catch (InvalidQueryException e) {
            logger.info("Une erreur est survenue lors de la tentative de suppression du jeton pour empêcher de lancer le cql plusieurs fois : {}",
                    e.getMessage());
        }

    }

    /**
     * Methode d'attente (sans limite) de suppression du jeton CQL (table parametre) 
     */
    private void waitExecuteCQL() {
    	boolean isStillRunning = true;
    	long sleepTime = 0;
        try {
            PreparedStatement stillPresent = getCassandraSession().prepare("select valeur from parametre where code=?");
            while ( isStillRunning ){
            	Row row = getCassandraSession().execute(stillPresent.bind(TOKEN_CODE)).one();
                logger.info("Attente de relachement du token cql...");
            	Thread.sleep(sleepTime);
            	sleepTime = 1000;
            	isStillRunning = (row!=null && TOKEN_VALUE.equals(row.getString("valeur")));
            }
        } catch (InvalidQueryException | InterruptedException e) {
            logger.info("Une erreur est survenue lors de la tentative de lecture du jeton pour empêcher de lancer le cql plusieurs fois : {}",
                    e.getMessage());
        }
        logger.info("Reprise du démarrage du MS");
    }
    
    /**
     * Initialise le système de transcodification. 
     * @param configuration : la configuration
     * @param environment
     *
     * @author LGY
     * @throws Exception 
     */
    private void initTranscodification(VisionMicroserviceConfiguration configuration,Environment environment) throws Exception{
    	Cluster cassandraFluks = configuration.getCassandraFluksFactory().build(environment);
        // Initialisation du service de transco
        TranscoderDao.INSTANCE.setCassandraSession(cassandraFluks.connect(configuration.getCassandraFluksFactory().getKeyspace()));
        TranscoderService.INSTANCE.setDao(TranscoderDao.INSTANCE);
        
        TranscoderService.INSTANCE.addProjet("DiffusionVision");
        TranscoderService.INSTANCE.addProjet("Aladin");
        TranscoderService.INSTANCE.addProjet("Vision");
        FeatureFlips.INSTANCE.setFlipProjectName("Vision");
        TranscoderService.INSTANCE.startUpdater();

    }
    
    /**
     * Initialise la session du cluster cassadra vision 
     * @param configuration
     * @param environment
     *
     * @author LGY
     */
	private void initSessionCassandraVision(final VisionMicroserviceConfiguration configuration,
			final Environment environment) {
		final Cluster cassandra = configuration.getCassandraFactory().build(environment);
		if (cassandraSession == null) {
			logger.info("Connecting to KS {}", configuration.getCassandraFactory().getKeyspace());
			cassandraSession = cassandra.connect(configuration.getCassandraFactory().getKeyspace());
		}
		ETableMicroServiceCounters.prepareUpdateTRT(cassandraSession);
		ETableMicroServiceCounters.prepareIncrementHit(cassandraSession);
		ETableMicroServiceCounters.prepareIncrementFail(cassandraSession);
	}
    
    /**
     * Initialise les différentes metrics du projet
     * 
     *
     * @author LGY
     */
    private void initMetrics(Environment environment) {	
    	// Ajout des métriques Hystrix à celle de dropwizard
        HystrixPlugins.getInstance().registerMetricsPublisher(
                new HystrixCodaHaleMetricsPublisher(environment.metrics()));        

        // Reporting des métriques via JMX <= FIXME : Utile ? Apparement ce n'est pas conseillé en prod...
        final JmxReporter reporter = JmxReporter.forRegistry(environment.metrics()).build();
        reporter.start();
        
        // Ajout des métriques de la failover load balancing policy de Cassandra
        environment.metrics().registerAll(org.adejanovski.cassandra.policies.dropwizard.DCAwareFailoverRoundRobinPolicyFactory.metrics());
	}
    
    /**
     * Configuration des port d'accès à l'application et de la resource du healthcheck
     * @param configuration
     *
     * @author LGY
     */
    private void initDropWizard(VisionMicroserviceConfiguration configuration) {
    	int adminPort = 0;
    	DefaultServerFactory serverFactory = (DefaultServerFactory) configuration.getServerFactory();
    	for (ConnectorFactory connector : serverFactory.getAdminConnectors()) {
    		if (connector.getClass().isAssignableFrom(HttpConnectorFactory.class)) {
    			adminPort = ((HttpConnectorFactory) connector).getPort();
    			break;
    		}
    	}
    	resourceHealthCheck = new HealthCheckResource().setPort(adminPort);
    }
    
    /**
     * Tentative de lancement du script cql de création des objets de la base.
     * Le lancement du script est effectué par une seule des instances du MS.
     * Si une autre instance execute le script cql, alors le MS attendra que cette
     * execution soit terminée avant de retourner.
     * 
     * @author LGY
     * @throws IOException 
     * @throws URISyntaxException 
     * @throws InterruptedException 
     */
    private void initModeleBDD(VisionMicroserviceConfiguration configuration) throws IOException, InterruptedException, URISyntaxException {
    	if (canExecuteCQL()) {
    		// Mise à jour du schéma de la base
    		logger.info("reading : " + configuration.getCassandraFactory().getKeyspace() + ".cql");
    		BufferedReader cqlScriptReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(
    				"/" + configuration.getCassandraFactory().getKeyspace() + ".cql")));
    		StringBuilder cqlScript = new StringBuilder();
    		String line = null;
    		while ((line = cqlScriptReader.readLine()) != null) {
    			cqlScript.append(line + " ");
    		}

    		ServiceInitializer.initSchema(cqlScript.toString());
    		releaseTokenCQL();
    	}
    	else
    		waitExecuteCQL();
    }
    
    /**
     * Création des caches manager et lancement du CacheManagerService qui
     * gère le rafraichissement automatique
     * @throws InterruptedException
     *
     * @author LGY
     */
    private void initCacheManager() throws InterruptedException {
    	/* Initialisation de la factory des cachemanager : rechargement toutes les 15 minutes */
    	CacheManagerService.INSTANCE.setDelaiRafraichissement(900000); 
    	CacheManagerService.INSTANCE.setMaxRetries(60);
    	
        /* Initialisation du cache des codes services (table services) */
        CodeServiceDaoImpl.INSTANCE.setSession(getCassandraSession());
        CacheManagerService.INSTANCE.addProjet("service", new CacheManager<CodeService>().setDao(CodeServiceDaoImpl.INSTANCE));

        /* Initialisation du cache des agences (table agence) */
        AgenceDaoImpl.INSTANCE.setSession(getCassandraSession());
        CacheManagerService.INSTANCE.addProjet("agence", new CacheManager<Agence>().setDao(AgenceDaoImpl.INSTANCE));

        /* Initialisation du cache des evenements (table ref_evenement) */
        EvenementDaoImpl.INSTANCE.setSession(getCassandraSession());
        CacheManagerService.INSTANCE.addProjet("evenement", new CacheManager<Evenement>().setDao(EvenementDaoImpl.INSTANCE));

        /* Initialisation du cache des parametre (table parametre) */
        ParametreDaoImpl.INSTANCE.setSession(getCassandraSession());
        CacheManagerService.INSTANCE.addProjet("parametre", new CacheManager<Parametre>().setDao(ParametreDaoImpl.INSTANCE));

        /* Initialisation du cache des parametre (table parametre) */
        RefContratDaoImpl.INSTANCE.setSession(getCassandraSession());
        CacheManagerService.INSTANCE.addProjet(CacheManager.REF_CONTRAT, new CacheManager<RefContrat>().setDao(RefContratDaoImpl.INSTANCE));

        CacheManagerService.INSTANCE.startUpdater();
    }

    /**
     * Initialisations des resources (les microservices)
	
     * @author LGY
     */
    private void initResources(){
    	resourceGetLts = new GetLtResource();
    	resourceGetDetailTournee = new GetDetailTourneeResource();
    	resourceInsertLts = new InsertLtResource();
    	resourceInsertEvts = new InsertEvtResource();
    	resourceUpdateTournee = new UpdateTourneeResource();
    	resourceGenereEvt = new GenereEvtResource();
    	resourceCodeTourneeResource = new GetCodeTourneeFromLTResource();
    	resourceColioutai = new ColioutaiResource();
    	resourceSuiviBox = new SuiviboxResource();
    	resourceFeatureFlips = new FeatureFlipsResource();
    	resourceGetAlertesTournees = new GetAlertesTourneesResource();
    	resourceMaintienIndexEvt = new MaintienIndexEvtResource();
    	resourceTraitementRetard = new TraitementRetardResource();
    	updateReferentiel = new UpdateReferentielResource();
    	syntheseTournee = new SyntheseTourneeResource();
    	resourceInsertAgenceColis = new InsertAgenceColisResource();
    	resourceInsertPointTournee = new InsertPointTourneeResource();
    	resourceUpdateSpecificationsColis = new UpdateSpecificationsColisResource();
        resourceTransco = new TranscoResource();
        diffusionEvtResource = new DiffusionEvtResource();
        syntheseAgenceResouce = new SyntheseAgenceResource();
        insertC11Resource = new InsertC11Resource();
        getC11Resource = new GetC11Resource();
        supervisionResource = new SupervisionResource();
        referenceResource = new ReferenceResource();
        getEvtsResource = new GetEvtsResource();
        insertAlerteResource = new InsertAlerteResource();
    }

    /**
     * Injections des dao dans les services
     * Injections des services dans les resources
     * Injections des caches dans les services et dao demandeurs
     * @author LGY
     */
    private void injections(VisionMicroserviceConfiguration configuration) {
    	
        /* diffusionEvt */
        diffusionEvtResource.setService(DiffusionEvtServiceImpl.getInstance());

        /* featureFlip  */
        resourceFeatureFlips.setBaseUrl(configuration.getEndpoints().get("featureFlips"));

        /* getLt */
    	GetLtDaoImpl.getInstance().setLtBuilder(new GetLtBuilder());
    	GetLtServiceImpl.getInstance().setDao(GetLtDaoImpl.getInstance());
        resourceGetLts.setService(GetLtServiceImpl.getInstance());

        /* getCodeTourneeFromLt  */
        GetCodeTourneeFromLTServiceImpl.getInstance().setDao(GetCodeTourneeFromLTDAOImpl.getInstance());
        GetCodeTourneeFromLTServiceImpl.getInstance().setGetLtV1(GetLtV1.getInstance());
        resourceCodeTourneeResource.setService(GetCodeTourneeFromLTServiceImpl.getInstance());

        /* genereEvt  */
        resourceGenereEvt.setService(GenereEvtServiceImpl.getInstance());

    	/* getAlerteTournees  */
    	GetAlertesTourneesServiceImpl.getInstance().setDao(GetAlertesTourneesDaoImpl.getInstance());
        resourceGetAlertesTournees.setService(GetAlertesTourneesServiceImpl.getInstance());

    	/* getDetailTournee */
    	GetDetailTourneeServiceImpl.getInstance().setDao(GetDetailTourneeDaoImpl.getInstance());
    	resourceGetDetailTournee.setGetLt(GetLtV1.getInstance());
        resourceGetDetailTournee.setService(GetDetailTourneeServiceImpl.getInstance());

        /* insertAgenceColis */
        InsertAgenceColisServiceImpl.INSTANCE.setDao(InsertAgenceColisDaoImpl.INSTANCE);
        InsertAgenceColisServiceImpl.INSTANCE.setRefentielAgence(CacheManagerService.INSTANCE.getCacheManager("agence", Agence.class));
        InsertAgenceColisServiceImpl.INSTANCE.setRefentielEvenement(CacheManagerService.INSTANCE.getCacheManager("evenement", Evenement.class));
        resourceInsertAgenceColis.setService(InsertAgenceColisServiceImpl.INSTANCE);

        /* insertEvt  */
        InsertEvtDaoImpl.getInstance().setRefentielParametre(CacheManagerService.INSTANCE.getCacheManager("parametre", Parametre.class));
    	InsertEvtServiceImpl.getInstance().setDao(InsertEvtDaoImpl.getInstance());
    	resourceInsertEvts.setService(InsertEvtServiceImpl.getInstance());
        
        /* insertLt  */
        InsertLtServiceImpl insertLtService = new InsertLtServiceImpl();
        insertLtService.setDao(InsertLtDAO.getInstance());
        InsertLtDAO.getInstance().setReferentielRefContrat(CacheManagerService.INSTANCE.getCacheManager(CacheManager.REF_CONTRAT, RefContrat.class));
        resourceInsertLts.setService(insertLtService);

        /* insertPointTournee */
        InsertPointTourneeDaoImpl.INSTANCE.setRefentielCodeService(CacheManagerService.INSTANCE.getCacheManager("service", CodeService.class));
        InsertPointTourneeDaoImpl.INSTANCE.setRefentielAgence(CacheManagerService.INSTANCE.getCacheManager("agence", Agence.class));
        InsertPointTourneeDaoImpl.INSTANCE.setRefentielParametre(CacheManagerService.INSTANCE.getCacheManager("parametre", Parametre.class));
        InsertPointTourneeServiceImpl.INSTANCE.setDao(InsertPointTourneeDaoImpl.INSTANCE);
        InsertPointTourneeServiceImpl.INSTANCE.setRefentielAgence(CacheManagerService.INSTANCE.getCacheManager("agence", Agence.class));
        resourceInsertPointTournee.setService(InsertPointTourneeServiceImpl.INSTANCE);

        /* maintienIndexEvt */
        MaintienIndexEvtServiceImpl.getInstance().setDao(MaintienIndexEvtDaoImpl.getInstance());
        resourceMaintienIndexEvt.setService(MaintienIndexEvtServiceImpl.getInstance());

        /* updateTournee  */
        UpdateTourneeServiceImpl.INSTANCE.setDao(UpdateTourneeDaoImpl.INSTANCE);
        resourceUpdateTournee.setService(UpdateTourneeServiceImpl.INSTANCE);

        /* suiviBox  */
        resourceSuiviBox.setService(new SuiviboxServiceImpl(SuiviBoxDaoImpl.getInstance()));

        /* syntheseTournee */
        SyntheseTourneeServiceImpl.INSTANCE.setDao(SyntheseTourneeDaoImpl.getInstance());
        syntheseTournee.setService(((SyntheseTourneeServiceImpl) SyntheseTourneeServiceImpl.INSTANCE));


        /* traitementRetard */
        TraitementRetardServiceImpl.getInstance().setDao(TraitementRetardDaoImpl.getInstance());
        resourceTraitementRetard.setService(TraitementRetardServiceImpl.getInstance());
        
        /* updateReferentiel */
        UpdateReferentielServiceImpl.getInstance().setDao(TranscoderDao.INSTANCE);
        UpdateReferentielServiceImpl.getInstance().setDaoVision(ReferentielVisionDaoImpl.INSTANCE);
        ReferentielVisionDaoImpl.INSTANCE.setCassandraSession(cassandraSession);
        ReferentielVisionDaoImpl.INSTANCE.setRefentielParametre(CacheManagerService.INSTANCE.getCacheManager("parametre", Parametre.class));
        updateReferentiel.setService(((UpdateReferentielServiceImpl) UpdateReferentielServiceImpl.getInstance()));
        
        /* updateSpecificationsColis */
        UpdateSpecificationsColisServiceImpl.getInstance().setRefentielCodeService(CacheManagerService.INSTANCE.getCacheManager("service", CodeService.class));
        UpdateSpecificationsColisServiceImpl.getInstance().setRefentielEvenement(CacheManagerService.INSTANCE.getCacheManager("evenement", Evenement.class));
        UpdateSpecificationsColisServiceImpl.getInstance().setTranscoderConsignes(UpdateSpecificationsColisTranscoderConsignesImpl.INSTANCE);
        UpdateSpecificationsColisServiceImpl.getInstance().setDao(UpdateSpecificationsColisDaoImpl.getInstance());
        resourceUpdateSpecificationsColis.setService(UpdateSpecificationsColisServiceImpl.getInstance());
        
        /* getSyntheseAgence */
        SyntheseAgenceServiceImpl.INSTANCE.setDao(SyntheseAgenceDaoImpl.INSTANCE);
        SyntheseAgenceServiceImpl.INSTANCE.setRefentielParametre(CacheManagerService.INSTANCE.getCacheManager("parametre", Parametre.class));
        syntheseAgenceResouce.setService(SyntheseAgenceServiceImpl.INSTANCE);
        
        InsertC11DaoImpl.INSTANCE.setRefentielParametre(CacheManagerService.INSTANCE.getCacheManager("parametre", Parametre.class));
        InsertC11ServiceImpl.INSTANCE.setDao(InsertC11DaoImpl.INSTANCE);
        insertC11Resource.setService(InsertC11ServiceImpl.INSTANCE);
        
        GetC11ServiceImpl.INSTANCE.setDao(GetC11DaoImpl.INSTANCE);
        getC11Resource.setService(GetC11ServiceImpl.INSTANCE);
        
        SupervisionServiceImpl.INSTANCE.setDao(SupervisionDaoImpl.INSTANCE);
        supervisionResource.setService(SupervisionServiceImpl.INSTANCE);
        
        ReferenceServiceImpl.INSTANCE.setRefentielCodeService(CacheManagerService.INSTANCE.getCacheManager("service", CodeService.class));
        ReferenceServiceImpl.INSTANCE.setRefentielEvenement(CacheManagerService.INSTANCE.getCacheManager("evenement", Evenement.class));
        ReferenceServiceImpl.INSTANCE.setRefentielAgence(CacheManagerService.INSTANCE.getCacheManager("agence", Agence.class));
        ReferenceServiceImpl.INSTANCE.setRefentielParametre(CacheManagerService.INSTANCE.getCacheManager("parametre", Parametre.class));
        referenceResource.setService(ReferenceServiceImpl.INSTANCE);
        
        GetEvtsServiceImpl.INSTANCE.setDao(GetEvtsDaoImpl.INSTANCE);
        getEvtsResource.setService(GetEvtsServiceImpl.INSTANCE);
        
        /*Insert Alerte*/
        InsertAlerteServiceImpl.INSTANCE.setDao(InsertAlerteDaoImpl.INSTANCE);
        insertAlerteResource.setService(InsertAlerteServiceImpl.INSTANCE);

        responseFilter = new MSResponseFilter();
        responseFilter.setDao(SupervisionDaoImpl.INSTANCE);
    }

    /**
     * Injection et instanciation des MS Colioutai 
     * @param configuration
     *
     * @author LGY
     */
    private void injectionColioutai(VisionMicroserviceConfiguration configuration){
    	/* colioutai  */
        resourceColioutai.setService(new ColioutaiServiceImpl(GoogleGeocoderHelper.getInstance(
                configuration.getProxyURL(), configuration.getProxyPort(), configuration.getProxyTimeout()),
                configuration.getEndpoints().get("poi"), GetLtV1.getInstance(), GetDetailTourneeV1.getInstance(),
                GetCodeTourneeFromLtV1.getInstance(), configuration.getEndpoints().get("consigne"), configuration
                        .getEndpoints().get("ptv")).setDao(ColioutaiLogDaoImpl.getInstance()));

        /* colioutai v2 */
        resourceColioutaiv2 = new com.chronopost.vision.microservices.colioutai.get.v2.ColioutaiResource();
        resourceColioutaiv2
                .setService(new com.chronopost.vision.microservices.colioutai.get.v2.services.ColioutaiServiceImpl(
                        com.chronopost.vision.microservices.colioutai.get.v2.services.GoogleGeocoderHelper.getInstance(
                                configuration.getProxyURL(), configuration.getProxyPort(),
                                configuration.getProxyTimeout()), configuration.getEndpoints().get("poi"), GetLtV1
                                .getInstance(), GetDetailTourneeV1.getInstance(), GetCodeTourneeFromLtV1.getInstance(),
                        configuration.getEndpoints().get("consigne"), configuration.getEndpoints().get("ptv"))
                        .setDao(com.chronopost.vision.microservices.colioutai.log.v2.dao.ColioutaiLogDaoImpl
                                .getInstance()).setCalculRetardEndpoint(configuration.getEndpoints().get("calculRetard")));

    }

    /**
     * Creation des healthcheck et enregistrement des resources de ces healthcheck
     *
     * @author LGY
     */
	private void initHealthCheck(Environment environment) {
		// Health check for production
        final CassandraHealthCheck cassandraHealthCheck = new CassandraHealthCheck(cassandraSession);
        final GetLtV1HealthCheck getLtHealthCheck = new GetLtV1HealthCheck();
        final GetCodeTourneeFromLtV1HealthCheck getCodeTourneeFromLtHealthCheck = new GetCodeTourneeFromLtV1HealthCheck();
        final GetDetailTourneeV1HealthCheck getDetailTourneeHealthCheck = new GetDetailTourneeV1HealthCheck();
        final ColioutaiInfoV1HealthCheck colioutaiInfoV1HealthCheck = new ColioutaiInfoV1HealthCheck();
        final SuiviBoxV1HealthCheck suiviboxV1HealthCheck = new SuiviBoxV1HealthCheck();
        final TraitementRetardV1HealthCheck traitementRetardV1HealthCheck = new TraitementRetardV1HealthCheck();
        final InsertEvtV1HealthCheck insertEvtV1HealthCheck = new InsertEvtV1HealthCheck();
        final UpdateTourneeV1HealthCheck updateTourneeV1HealthCheck = new UpdateTourneeV1HealthCheck();
        final InsertPointTourneeV1HealthCheck insertPointTourneeV1HealthCheck = new InsertPointTourneeV1HealthCheck();
        final InsertAgenceColisV1HealthCheck insertAgenceColisV1HealthCheck = new InsertAgenceColisV1HealthCheck();

        final CalculRetardServiceHealthCheck calculretardServiceHealthCheck = new CalculRetardServiceHealthCheck();
        final ConsigneServiceHealthCheck consigneServiceHealthCheck = new ConsigneServiceHealthCheck();
        final GoogleServiceHealthCheck googleServiceHealthCheck = new GoogleServiceHealthCheck();
        final PoiServiceHealthCheck poiServiceHealthCheck = new PoiServiceHealthCheck();
        final PtvServiceHealthCheck ptvServiceHealthCheck = new PtvServiceHealthCheck();
        final GenereEvtV1HealthCheck genereEvtV1HealthCheck = new GenereEvtV1HealthCheck();
        final SyntheseTourneeV1HealthCheck syntheseTourneeV1HealthCheck = new SyntheseTourneeV1HealthCheck();
        
        final DiffusionEvtV1HealthCheck diffusionEvtV1HealthCheck = new DiffusionEvtV1HealthCheck();

        environment.jersey().register(new ErrorMessageBodyWriter());
        // Enregistrement des health check pour vérifier la disponibilité des ressources en production
        environment.healthChecks().register("Cassandra", cassandraHealthCheck);
        environment.healthChecks().register("GetLt Service", getLtHealthCheck);
        environment.healthChecks().register("GetCodeTourneeFromLt Service", getCodeTourneeFromLtHealthCheck);
        environment.healthChecks().register("GetDetailTournee Service", getDetailTourneeHealthCheck);
        environment.healthChecks().register("ColioutaiInfoV1 Service", colioutaiInfoV1HealthCheck);
        environment.healthChecks().register("SuiviBoxV1 Service", suiviboxV1HealthCheck);
        environment.healthChecks().register("TraitementretardV1 Service", traitementRetardV1HealthCheck);
        environment.healthChecks().register("InsertEvtV1 Service", insertEvtV1HealthCheck);
        environment.healthChecks().register("UpdateTourneeV1 Service", updateTourneeV1HealthCheck);
        environment.healthChecks().register("InsertPointTourneeV1 Service", insertPointTourneeV1HealthCheck);
        environment.healthChecks().register("InsertAgenceColisV1 Service", insertAgenceColisV1HealthCheck);

        environment.healthChecks().register("CalculRetard Service", calculretardServiceHealthCheck);
        environment.healthChecks().register("Consigne Service", consigneServiceHealthCheck);
        environment.healthChecks().register("Google Service", googleServiceHealthCheck);
        environment.healthChecks().register("Poi Service", poiServiceHealthCheck);
        environment.healthChecks().register("Ptv Service", ptvServiceHealthCheck);
        environment.healthChecks().register("Genere Evt Service", genereEvtV1HealthCheck);
        environment.healthChecks().register("Synthese Tournee", syntheseTourneeV1HealthCheck);
        environment.healthChecks().register("Diffusion Evt", diffusionEvtV1HealthCheck);
        environment.healthChecks().register("Synthese Agence", new SyntheseAgenceHealthCheck());

        // Retrait des healthchecks de dropwizard-cassandra en attendant que le passage à la 3.0.0 soit correctement pris en compte
        environment.healthChecks().unregister("cassandra.Fluks Cluster");
        environment.healthChecks().unregister("cassandra.Vision DEV Cluster");
	}
	
    public static void setConfig(VisionMicroserviceConfiguration config) {
		VisionMicroserviceApplication.config = config;
	}
    
    public static VisionMicroserviceConfiguration getConfig() {
		return config;
	}
}
