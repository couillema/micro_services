package com.chronopost.vision.microservices.insertagencecolis.v1;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.cassandra.table.ETableColisAgence;
import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.CacheManagerService;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.Agence;
import com.chronopost.vision.model.EInfoComp;
import com.chronopost.vision.model.Evenement;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.collect.Sets;

public class InsertAgenceColisAcceptanceTest extends JerseyTestNg.ContainerPerClassTest {

    static final Logger logger = LoggerFactory.getLogger(InsertAgenceColisAcceptanceTest.class);

    static final SimpleDateFormat FMT_JOUR = new SimpleDateFormat("yyyyMMdd");
    static final SimpleDateFormat FMT_HEURE = new SimpleDateFormat("HH");
    static final SimpleDateFormat FMT_MINUTE = new SimpleDateFormat("mm");
    /** La date d'aujourd'hui fixée afin de pouvoir faire les test et le nettoyage de ceux-ci */
    static final Date NOW = new Date();
    /** La date du priochain jour ouvrable à minuit */
    static final Date NEXT_WORKING_DAY = DateRules.getJourOuvrableAPartirDe(NOW,1);
    /** La date de samedi prochain à minuit */
    static final Date NEXT_SATURDAY_DAY = calculProchainSamedi(NOW);
    /** La date du jour ouvrable suivant le prochain samedi */
    static final Date NEXT_WORKING_DAY_AFTER_SATURDAY = DateRules.getJourOuvrableAPartirDe(NEXT_SATURDAY_DAY,1);
    
    /* La map qui simule le cacheEvt*/
    private static HashMap<String,Evenement> mapRefEvenements = new HashMap<>();
    static
    {
		mapRefEvenements.put("I", new Evenement("I", "16", EEtapesColis.RETOUR_AGENCE, 1000, "Envoi differe", ""));
		mapRefEvenements.put("SD", new Evenement("SD", "7", EEtapesColis.DISPERSION, 1000, "Colis livre a l expediteur", ""));
		mapRefEvenements.put("ZA", new Evenement("ZA", "42", EEtapesColis.DISPERSION, 1000, "Livraison reportee de 24h", ""));
		mapRefEvenements.put("TA", new Evenement("TA", "42", EEtapesColis.PREPA_DISTRI, 1000, "Envoi en cours de livraison", ""));
    }
    
    /* La map qui simule le cacheAgence */
    private static HashMap<String,Agence> mapRefAgence = new HashMap<>();
    static
    {
    	mapRefAgence.put("99999",new Agence("99999","WKY", "", ""));
    }
    
    /* Les mock des caches */
    CacheManager<Evenement> cacheEvtMock;
    CacheManager<Agence> cacheAgenceMock;
    
	
    /** @return VisionMicroserviceApplication.cassandraSession (a com.datastax.driver.core ) */
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

    static private class AgenceColisRecord {
    	Set<String> colisSaisis;
    	Set<String> colisASaisir;
    	Set<String> colisRestantTg2;
		public AgenceColisRecord(Row row) {
			this.colisSaisis = row.getSet("colis_saisis",String.class);
			this.colisASaisir = row.getSet("colis_a_saisir",String.class);
			this.colisRestantTg2 = row.getSet("colis_restant_tg2",String.class);
		}

		public Set<String> getColisSaisis() {
			return colisSaisis;
		}

		public Set<String> getColisASaisir() {
			return colisASaisir;
		}
		
		public Set<String> getColisRestantTg2() {
			return colisRestantTg2;
		}
    }

    
    /** PreparedStatement pour récupérer une tournee depuis l'index */
    private PreparedStatement getOneRecordOfAgenceColis;
    private Client client;
    private boolean suiteLaunch = true;

    /**
     * Binding the service to the instantiation of the resource
     */
    @Override
    protected Application configure() {

        /* Si le cluster n'existe pas déjà, alors il faut le créer et considérer
         * que le test est isolé (lancé seul) */
        if (!BuildCluster.clusterHasBuilt) {
            try {
                BuildCluster.setUpBeforeSuite();
            } catch (Exception e) {
                e.printStackTrace();
            }
            suiteLaunch = false;
        }
        CCMBridge.ipOfNode(1);

        /* Création de la resource et initialisation avec le service mocké */
        InsertAgenceColisResource resourceInsertAgenceColisResourceTest = new InsertAgenceColisResource();
        resourceInsertAgenceColisResourceTest.setService(InsertAgenceColisServiceImpl.INSTANCE);

        forceSet(TestProperties.CONTAINER_PORT, "0");

        ResourceConfig config = new ResourceConfig();
        config.register(resourceInsertAgenceColisResourceTest);

        return config;
    }

    @SuppressWarnings("unchecked")
	@BeforeClass
    public void setUp() throws Exception {
        super.setUp();
        client = ClientBuilder.newClient();

        /* On mock le cacheEvenement */
        cacheEvtMock = Mockito.mock(CacheManager.class) ;
        Mockito.when(cacheEvtMock.getCache()).thenReturn(mapRefEvenements);
        for(String codeEvt: mapRefEvenements.keySet())
        	Mockito.when(cacheEvtMock.getValue(codeEvt)).thenReturn(mapRefEvenements.get(codeEvt));
        
        /* On mock le cacheAgence */
        cacheAgenceMock = Mockito.mock(CacheManager.class) ;
        Mockito.when(cacheAgenceMock.getCache()).thenReturn(mapRefAgence);
        for(String pcAgence: mapRefAgence.keySet())
        	Mockito.when(cacheAgenceMock.getValue(pcAgence)).thenReturn(mapRefAgence.get(pcAgence));

        InsertAgenceColisServiceImpl.INSTANCE.setDao(InsertAgenceColisDaoImpl.INSTANCE);
        InsertAgenceColisServiceImpl.INSTANCE.setRefentielEvenement(cacheEvtMock);
        InsertAgenceColisServiceImpl.INSTANCE.setRefentielAgence(cacheAgenceMock);
        
        getOneRecordOfAgenceColis = getSession().prepare(
                "SELECT * FROM " + ETableColisAgence.TABLE_NAME
                        + " WHERE " + ETableColisAgence.POSTE_COMPTABLE.getNomColonne() + " = ? "
                        + "AND    " + ETableColisAgence.JOUR.getNomColonne() + " = ?"
                        + "AND    " + ETableColisAgence.HEURE.getNomColonne() + " = ?"
                        + "AND    " + ETableColisAgence.MINUTE.getNomColonne() + " = ?"
                        );
        
        cleanBaseTest();
    }

    /**
     * Supprime les traces des tests dans la base
     */
    private void cleanBaseTest() {
        /* Suppression de tous les enregistrement de test du moment (au cas où ils existeraient) */
        getSession().execute("DELETE FROM colis_agence "
        		+ "WHERE " + ETableColisAgence.POSTE_COMPTABLE.getNomColonne() + " = '99999'"
        		+ "AND   " + ETableColisAgence.JOUR.getNomColonne() + " = '" + FMT_JOUR.format(NOW) + "'"
        		+ "AND   " + ETableColisAgence.HEURE.getNomColonne() + " = '" + FMT_HEURE.format(NOW) + "'"
        		+ "AND   " + ETableColisAgence.MINUTE.getNomColonne() + " = '" + FMT_MINUTE.format(NOW).substring(0,1) + "'"
        		);
        
        getSession().execute("DELETE FROM colis_agence "
        		+ "WHERE " + ETableColisAgence.POSTE_COMPTABLE.getNomColonne() + " = '99999'"
        		+ "AND   " + ETableColisAgence.JOUR.getNomColonne() + " = '" + FMT_JOUR.format(NEXT_WORKING_DAY) + "'"
        		+ "AND   " + ETableColisAgence.HEURE.getNomColonne() + " = '00'"
        		+ "AND   " + ETableColisAgence.MINUTE.getNomColonne() + " = '0'"
        		);	

        getSession().execute("DELETE FROM colis_agence "
        		+ "WHERE " + ETableColisAgence.POSTE_COMPTABLE.getNomColonne() + " = '99999'"
        		+ "AND   " + ETableColisAgence.JOUR.getNomColonne() + " = '" + FMT_JOUR.format(NEXT_SATURDAY_DAY) + "'"
        		+ "AND   " + ETableColisAgence.HEURE.getNomColonne() + " = '" + FMT_HEURE.format(NEXT_SATURDAY_DAY) + "'"
        		+ "AND   " + ETableColisAgence.MINUTE.getNomColonne() + " = '" + FMT_MINUTE.format(NEXT_SATURDAY_DAY).substring(0,1) + "'"
        		);	

        getSession().execute("DELETE FROM colis_agence "
        		+ "WHERE " + ETableColisAgence.POSTE_COMPTABLE.getNomColonne() + " = '99999'"
        		+ "AND   " + ETableColisAgence.JOUR.getNomColonne() + " = '" + FMT_JOUR.format(NEXT_WORKING_DAY_AFTER_SATURDAY) + "'"
        		+ "AND   " + ETableColisAgence.HEURE.getNomColonne() + " = '00'"
        		+ "AND   " + ETableColisAgence.MINUTE.getNomColonne() + " = '0'"
        		);	

        getSession().execute("DELETE FROM colis_agence "
        		+ "WHERE " + ETableColisAgence.POSTE_COMPTABLE.getNomColonne() + " = '99999'"
        		+ "AND   " + ETableColisAgence.JOUR.getNomColonne() + " = '20160610'"
        		+ "AND   " + ETableColisAgence.HEURE.getNomColonne() + " = '13'"
        		+ "AND   " + ETableColisAgence.MINUTE.getNomColonne() + " = '4'"
        		);

        getSession().execute("DELETE FROM colis_agence "
        		+ "WHERE " + ETableColisAgence.POSTE_COMPTABLE.getNomColonne() + " = '99999'"
        		+ "AND   " + ETableColisAgence.JOUR.getNomColonne() + " = '20160625'"
        		+ "AND   " + ETableColisAgence.HEURE.getNomColonne() + " = '00'"
        		+ "AND   " + ETableColisAgence.MINUTE.getNomColonne() + " = '0'"
        		);
        getSession().execute("DELETE FROM colis_agence "
        		+ "WHERE " + ETableColisAgence.POSTE_COMPTABLE.getNomColonne() + " = '99999'"
        		+ "AND   " + ETableColisAgence.JOUR.getNomColonne() + " = '20160625'"
        		+ "AND   " + ETableColisAgence.HEURE.getNomColonne() + " = '13'"
        		+ "AND   " + ETableColisAgence.MINUTE.getNomColonne() + " = '4'"
        		);
        getSession().execute("DELETE FROM colis_agence "
        		+ "WHERE " + ETableColisAgence.POSTE_COMPTABLE.getNomColonne() + " = '99999'"
        		+ "AND   " + ETableColisAgence.JOUR.getNomColonne() + " = '20160725'"
        		+ "AND   " + ETableColisAgence.HEURE.getNomColonne() + " = '00'"
        		+ "AND   " + ETableColisAgence.MINUTE.getNomColonne() + " = '0'"
        		);
    }

	/** @param now : la date recherchée
     * @return l'enregistrement de la table colis_agence de l'agence 99999 pour la date indiquée
     */
    private AgenceColisRecord getAgenceColis(@NotNull Date now,@NotNull String codeAgence) {
    	String jour = FMT_JOUR.format(now);
    	String heure = FMT_HEURE.format(now);
    	String minute = FMT_MINUTE.format(now).substring(0,1);
    	Row row = getSession().execute(getOneRecordOfAgenceColis.bind(codeAgence,jour,heure,minute)).one();
    	
    	if (row != null)
    		return new AgenceColisRecord(row);
    	else 
    		return null;
	}
    
    private AgenceColisRecord getAgenceColisRestant(final String codeAgence, final String jour){
	
    	Row row = getSession().execute(getOneRecordOfAgenceColis.bind(codeAgence,jour,"23","5")).one();
    	
    	if (row != null)
    		return new AgenceColisRecord(row);
    	else 
    		return null;
	}
    	
    
    private AgenceColisRecord getAgenceColis(@NotNull Date now) {
    	return getAgenceColis(now, "99999");
    }

	/**
     * Effectue un appel au microService InsertAgenceColis, et retourne la
     * réponse de celui-ci
     * 
     * @param listeEvts : la liste des evenements a considérer
     * @return : Une réponse du MS.
     */
    private Response appelMS(List<Evt> evts) {
        WebTarget a = client.target("http://localhost:" + getPort());
        WebTarget b = a.path("/InsertAgenceColis/v1");
        Builder c = b.request();  
        Builder d = c.accept(MediaType.APPLICATION_JSON_TYPE);
        Entity<List<Evt>> f = Entity.entity(evts, MediaType.APPLICATION_JSON);
        Response e = d.post(f);
        return e;
    }

	/**
	 * Effectue un appel au microService /InsertAgenceColis/restanttg2/v1, et retourne la
	 * réponse de celui-ci
	 * 
	 * @param agence
	 * @param jour
	 * @param noLts
	 * @return
	 */
	private Response appelMSColisRestantsTG2(final String agence, final String jour, final Set<String> noLts) {
		WebTarget a = client.target("http://localhost:" + getPort());
		WebTarget b = a.path("/InsertAgenceColis/restanttg2");
		if (null != jour) {
			b = b.queryParam("jour", jour);
		}
		if (null != agence) {
			b = b.queryParam("agence", agence);
		}
		Builder c = b.request();
		Builder d = c.accept(MediaType.APPLICATION_JSON_TYPE);
		Entity<Set<String>> f = Entity.entity(noLts, MediaType.APPLICATION_JSON);
		Response e = d.post(f);
		return e;
	}
    
    @AfterMethod
    public void tearDownAfterClass() throws Exception {
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
        CacheManagerService.INSTANCE.stopUpdater();
        CacheManagerService.INSTANCE.delProjet("evenement");
        
        /* Suppression de tous les enregistrement de test du moment (au cas où ils existeraient) */
        cleanBaseTest();
    }


    /**Instanciation d'un evenement (objet evt) avec le code indiqué et le
     * numero lt indiqué
     * 
     * @param noLt
     * @param codeEvt
     * @param infosComp
     * @param createurEvt
     * @return
     */
	private Evt createEvt(@NotNull final String noLt, @NotNull final String codeEvt, final String codeTransport, final Date now) {
        Evt evt = new Evt();
        evt.setNoLt(noLt);
        evt.setDateEvt(now);
        evt.setCodeEvt(codeEvt);
        evt.setLieuEvt("99999");
        evt.setSsCodeEvt(codeTransport);
        return evt;
    }
    
    
    /**
     * Calcul la date du prochain samedi 
     * @param now
     * @return la date du prochain samedi
     */
    static private Date calculProchainSamedi(Date now){
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(now);
    	cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
    	return cal.getTime();
    }

    
    
    /**
     * En Entrée : Un evt TA sur le colis COLIS1 sur l'agence 
     * 
     * Attendu   : présence du colis dans la table colis_agence pour la date indiquée et dans le champ colis_saisi  
     *             et absence du colis dans la colonne colis_a_saisir
     */
    @Test(groups = { "slow", "acceptance" })
    public void TestPassantColisSaisi() throws Exception {

        /* Construction des data du test */
        List<Evt> evts = new ArrayList<>();

        evts.add(createEvt("COLIS0001", "TA", "rien",NOW));

        /* Invocation du MicroService */
        int status = appelMS(evts).getStatus();

        /* Vérifications */
        assertEquals(status, 200);

        /* point 1 */
        AgenceColisRecord result = getAgenceColis(NOW);
        assert (result != null);
        assert(result.getColisSaisis().contains("COLIS0001"));

        return;
    }


    /**
     * En Entrée : Un evt TA sur le colis COLIS2 sur l'agence vide
     * 
     * Attendu   : Absence du colis dans la table colis_agence pour la date indiquée et dans le champ colis_saisi  
     */
    @Test(groups = { "slow", "acceptance" })
    public void TestColisSaisiMaisAgenceVide() throws Exception {

        /* Construction des data du test */
        List<Evt> evts = new ArrayList<>();

        evts.add(createEvt("COLIS2", "TA", "rien",NOW).setLieuEvt(""));

        /* Invocation du MicroService */
        int status = appelMS(evts).getStatus();

        /* Vérifications */
        assertEquals(status, 200);

        /* point 1 */
        AgenceColisRecord result = getAgenceColis(NOW,"");
        assert (result == null || result.getColisSaisis() == null || !result.getColisSaisis().contains("COLIS2"));


        return;
    }
    
    /**
     * En Entrée : Un evt "" sur le colis COLIS3 sans code evt
     * 
     * Attendu   : absence du colis dans la table colis_agence pour la date indiquée et dans le champ colis_saisi  
     */   
    @Test(groups = { "slow", "acceptance" })
    public void TestColisSaisiMaisCodeEvtVide() throws Exception {

        /* Construction des data du test */
        List<Evt> evts = new ArrayList<>();

        evts.add(createEvt("COLIS3", "", "rien",NOW));

        /* Invocation du MicroService */
        int status = appelMS(evts).getStatus();

        /* Vérifications */
        assertEquals(status, 200);

        /* point 1 */
        AgenceColisRecord result = getAgenceColis(NOW);
        assert (result == null || result.getColisSaisis() == null || !result.getColisSaisis().contains("COLIS3"));


        return;
    }
    
    /**
     * En Entrée : Un evt "TA" sur le colis COLIS4 mais le code agence lieu_evt fait moins
     * de 5 caractères
     * 
     * Attendu   : absence du colis dans la table colis_agence pour la date indiquée et dans le champ colis_saisi  
     */   
    @Test(groups = { "slow", "acceptance" })
    public void TestColisSaisiMaisLieuTropCourt() throws Exception {

        /* Construction des data du test */
        List<Evt> evts = new ArrayList<>();

        evts.add(createEvt("COLIS4", "", "rien",NOW).setLieuEvt("7712"));

        /* Invocation du MicroService */
        int status = appelMS(evts).getStatus();

        /* Vérifications */
        assertEquals(status, 200);

        /* point 1 */
        AgenceColisRecord result = getAgenceColis(NOW,"");
        assert (result == null || result.getColisSaisis() == null || !result.getColisSaisis().contains("COLIS4"));


        return;
    }
    
    
    /**
     * En Entrée : Un evt "TE" sur le colis COLIS5 
     * de 5 caractères
     * 
     * Attendu   : absence du colis dans la table colis_agence pour la date indiquée et dans le champ colis_saisi  
     */   
    @Test(groups = { "slow", "acceptance" })
    public void TestColisSaisiMaisEvtSansEtape() throws Exception {

        /* Construction des data du test */
        List<Evt> evts = new ArrayList<>();

        evts.add(createEvt("COLIS5", "", "rien",NOW));

        /* Invocation du MicroService */
        int status = appelMS(evts).getStatus();

        /* Vérifications */
        assertEquals(status, 200);

        /* point 1 */
        AgenceColisRecord result = getAgenceColis(NOW);
        assert (result == null || result.getColisSaisis() == null || !result.getColisSaisis().contains("COLIS5"));

        return;
    }

    /**
     * En Entrée : Un evt ZA sur le colis COLIS6 sur l'agence 
     * 
     * Attendu   : (1) le colis ne doit plus apparaître dans les colis le lendemain
     */
    @Test(groups = { "slow", "acceptance" })
    public void TestPassantColisASaisir_ZA() throws Exception {

        /* Construction des data du test */
        List<Evt> evts = new ArrayList<>();

        evts.add(createEvt("COLIS0006", "ZA", "rien",NOW));

        /* Invocation du MicroService */
        int status = appelMS(evts).getStatus();

        /* Vérifications */
        assertEquals(status, 200);

        /* Verifications */
        AgenceColisRecord result = getAgenceColis(FMT_JOUR.parse(FMT_JOUR.format(NEXT_WORKING_DAY)));
        assert (result == null);
    }
    
    /**
     * En Entrée : Un evt ZA sur le colis COLIS10 de la poste
     * 
     * Attendu   : (1) absence totale du colis dans les colis du lendemain
     * 			   (2) présence du colis dans les colis saisis/vus du jour
     */
    @Test(groups = { "slow", "acceptance" })
    public void TestPassantColisASaisir_ZA_LaPoste() throws Exception {
		/* Construction des data du test */
		List<Evt> evts = new ArrayList<>();

		Evt evt = createEvt("COLIS0010", "ZA", "", NOW);
		Map<String, String> infosComp = new HashMap<>();
		infosComp.put(EInfoComp.CODE_REGATE_EMMETEUR.getCode(), "SITE_REGATE");
		evt.setInfoscomp(infosComp);
		evts.add(evt);

		/* Invocation du MicroService */
		int status = appelMS(evts).getStatus();

		/* Vérifications */
		assertEquals(status, 200);

		/* Verifications */
		AgenceColisRecord result = getAgenceColis(FMT_JOUR.parse(FMT_JOUR.format(NEXT_WORKING_DAY)));
		assert (result == null);
        result = getAgenceColis(NOW);
        assert (result != null);
        assert(result.getColisSaisis().contains("COLIS0010"));
    }
    
    /**
     * En Entrée : Un evt SD RDL sur le colis COLIS7 sur l'agence 
     * 
     * Attendu   : (1) présence du colis dans la table colis_agence pour la date indiquée + 1 et dans le champ colis_a_saisir  
     *             et (2) absence du colis dans la colonne colis_saisi du même jour.
     *             Vérification également que (3) le colis est indiqué saisi aujourd'hui et (4) non a_saisir aujourd'hui
     */
    @Test(groups = { "slow", "acceptance" })
    public void TestPassantColisASaisir_SD_RDL() throws Exception {

        /* Construction des data du test */
        List<Evt> evts = new ArrayList<>();

        evts.add(createEvt("COLIS0007", "SD", "RDL",NOW));

        /* Invocation du MicroService */
        int status = appelMS(evts).getStatus();

        /* Vérifications */
        assertEquals(status, 200);

        /* Verifications */
        
        AgenceColisRecord result = getAgenceColis(FMT_JOUR.parse(FMT_JOUR.format(NEXT_WORKING_DAY)));
        assert (result != null);
        assert(result.getColisASaisir().contains("COLIS0007"));  // (1)
        assert(!result.getColisSaisis().contains("COLIS0007"));  // (2)
        result = getAgenceColis(NOW);
        assert (result != null);
        assert(result.getColisSaisis().contains("COLIS0007"));   // (3)
        assert(!result.getColisASaisir().contains("COLIS0007")); // (4)
		
        return;
    }
    
    /**
     * En Entrée : Un evt SD RDL sur le colis COLIS8 mais daté de samedi 
     * 
     * Attendu   : (1) présence du colis dans la table colis_agence pour le lundi suivant (s'il est ouvrable) 
     * 			   et dans le champ colis_a_saisir  
     *             et (2) absence du colis dans la colonne colis_saisi du même jour.
     *             Vérification également que (3) le colis est indiqué saisi samedi et (4) non a_saisir samedi
     */
    @Test(groups = { "slow", "acceptance" })
    public void TestPassantColisASaisir_SD_RDL_Samedi() throws Exception {

        /* Construction des data du test */
        List<Evt> evts = new ArrayList<>();

        evts.add(createEvt("COLIS0008", "SD", "RDL",NOW).setDateEvt(NEXT_SATURDAY_DAY));

        /* Invocation du MicroService */
        int status = appelMS(evts).getStatus();

        /* Vérifications */
        assertEquals(status, 200);

        /* Verifications */
        
        AgenceColisRecord result = getAgenceColis(NEXT_WORKING_DAY_AFTER_SATURDAY);
        assert (result != null);
        assert(result.getColisASaisir().contains("COLIS0008"));  // (1)
        assert(!result.getColisSaisis().contains("COLIS0008"));  // (2)
        result = getAgenceColis(NEXT_SATURDAY_DAY);
        assert (result != null);
        assert(result.getColisSaisis().contains("COLIS0008"));   // (3)
        assert(!result.getColisASaisir().contains("COLIS0008")); // (4)
		
        return;
    }
    
    
    /**
     * En Entrée : Un evt SD 25DIF sur le colis COLIS9 daté du 10 Juin 2016 
     * 
     * Attendu   : (1) présence du colis dans la table colis_agence pour le samedi 25 juin (il est ouvrable) 
     * 			   et dans le champ colis_a_saisir  
     *             et (2) absence du colis dans la colonne colis_saisi du même jour.
     *             Vérification également que (3) le colis est indiqué saisi vendredi 10 et (4) non a_saisir 
     */
    @Test(groups = { "slow", "acceptance" })
    public void TestPassantColisASaisir_SD_25DIF() throws Exception {

        /* Construction des data du test */
        List<Evt> evts = new ArrayList<>();

        evts.add(createEvt("COLIS0009", "SD", "25DIF",new SimpleDateFormat("yyyyMMddHHmmss").parse("20160610134050")));

        /* Invocation du MicroService */
        int status = appelMS(evts).getStatus();

        /* Vérifications */
        assertEquals(status, 200);

        /* Verifications */
        
        AgenceColisRecord result = getAgenceColis(new SimpleDateFormat("yyyyMMddHHmmss").parse("20160625000000"));
        assert (result != null);
        assert(result.getColisASaisir().contains("COLIS0009"));  // (1)
        assert(!result.getColisSaisis().contains("COLIS0009"));  // (2)
        result = getAgenceColis(new SimpleDateFormat("yyyyMMddHHmmss").parse("20160610134050"));
        assert (result != null);
        assert(result.getColisSaisis().contains("COLIS0009"));   // (3)
        assert(!result.getColisASaisir().contains("COLIS0009")); // (4)
		
        return;
    }
    
    /**
     * En Entrée : Un evt TA sur le colis YYY123456789 sur l'agence. Mais il s'agit d'un colis ficitf (YYY)
     * 
     * Attendu   : absence du colis dans la table colis_agence pour la date indiquée et dans le champ colis_saisi  
     *             et absence du colis dans la colonne colis_a_saisir
     */
    @Test(groups = { "slow", "acceptance" })
    public void TestColisFicitf() throws Exception {

        /* Construction des data du test */
        List<Evt> evts = new ArrayList<>();
		evts.add(createEvt("YYY123456789", "TA", "rien",NOW));

		/* Invocation du MicroService */
		int status = appelMS(evts).getStatus();

		/* Vérifications */
		assertEquals(status, 200);

		/* point 1 */
		AgenceColisRecord result = getAgenceColis(NOW);
		assert (result == null ||  !result.getColisSaisis().contains("COLIS0001"));

		return;
    }
    
    /**
     * En Entrée : Un evt SD 24DIF sur le colis COLIS10 daté du 25 Juin 2016 
     * 
     * Attendu   : (1) présence du colis dans la table colis_agence pour le lundi 25 juillet (il est ouvrable) 
     * 			   et dans le champ colis_a_saisir  
     *             et (2) absence du colis dans la colonne colis_saisi du même jour.
     *             Vérification également que (3) le colis est indiqué saisi samedi 25 juin et (4) non a_saisir 
     */
    @Test(groups = { "slow", "acceptance" })
    public void TestPassantColisASaisir_SD_25DIF_Le25() throws Exception {

        /* Construction des data du test */
        List<Evt> evts = new ArrayList<>();

        evts.add(createEvt("COLIS0010", "SD", "24DIF",new SimpleDateFormat("yyyyMMddHHmmss").parse("20160625134050")));

        /* Invocation du MicroService */
        int status = appelMS(evts).getStatus();

        /* Vérifications */
        assertEquals(status, 200);

        /* Verifications */
        
        AgenceColisRecord result = getAgenceColis(new SimpleDateFormat("yyyyMMddHHmmss").parse("20160725000000"));
        assert (result != null);
        assert(result.getColisASaisir().contains("COLIS0010"));  // (1)
        assert(!result.getColisSaisis().contains("COLIS0010"));  // (2)
        result = getAgenceColis(new SimpleDateFormat("yyyyMMddHHmmss").parse("20160625134050"));
        assert (result != null);
        assert(result.getColisSaisis().contains("COLIS0010"));   // (3)
        assert(!result.getColisASaisir().contains("COLIS0010")); // (4)
    }
    
	@Test
	public void testColisrestantTg2() {
        
		/*init*/
		String nolt = "EE000000000FR";
		String agence = "99999";
		String jour = "20160130";
		/* Invocation du MicroService */
		int status = appelMSColisRestantsTG2(agence, jour, Sets.newHashSet(nolt)).getStatus();

		/* Vérifications */
		assertEquals(status, 200);
		AgenceColisRecord result = getAgenceColisRestant(agence, jour);
		assertTrue(result != null);
		assertTrue(result.getColisRestantTg2().contains(nolt));
		
	}

}
