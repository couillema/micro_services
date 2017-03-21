package com.chronopost.vision.microservices.getsyntheseagence.v1;

import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.ACQUITTEMENT_LIVRAISON;
import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.CONCENTRATION;
import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.DISPERSION;
import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.EXCLUSION;
import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.INCIDENT;
import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.LIVRAISON;
import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.PERDU;
import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.PREPA_DISTRI;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.cassandra.request.builder.TTL;
import com.chronopost.cassandra.table.ETableColisAgence;
import com.chronopost.cassandra.table.ETableColisSpecifications;
import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.getsyntheseagence.v1.ECodeIndicateur;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseColis;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseColisEtListeValeurs;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseListeValeurs;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseDispersionQuantite;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseDispersionQuantitePassee;
import com.chronopost.vision.model.updatespecificationscolis.v1.ESpecificiteColis;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

public class GetSyntheseAgenceAcceptanceTest extends JerseyTestNg.ContainerPerClassTest {
	
	private static final Logger log = LoggerFactory.getLogger(GetSyntheseAgenceAcceptanceTest.class);
	private static final String AGENCE = "99999";
	private static final String HEURE_APPEL = "2016-11-29T01:00:00-05:00";
	private static final DateTime MAINTENANT = DateTime.now();
	private static final DateTime HIER = MAINTENANT.minusDays(1);

	private static final String JOUR = MAINTENANT.toString("yyyyMMdd");
	private static final String HEURE = MAINTENANT.toString("HH");
	private static final String HEURE_ALERTE = MAINTENANT.plusMinutes(10).toString("HHmm");
	private static final Set<String> ONE_ALERTE_JOUR =  getOneAlerte(HEURE_ALERTE,JOUR);
	private static final Set<String> ONE_ALERTE_PASSEE =  getOneAlerte(HEURE_ALERTE, MAINTENANT.minusDays(10).toString("yyyyMMdd"));
	private static final String MINUTE = MAINTENANT.toString("mm").substring(0, 1); // Ne conserver que les dizaines
	private static final int QUATORZEJOURS = 14;
	final static DateTime TODAY = DateTime.now().withTimeAtStartOfDay();
	final static String fromDate = new DateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0)
			.withMillisOfSecond(0).toString();
	final static String toDate = MAINTENANT.toString();
	private static final String TYPE_RPTSDTA = "RPTSDTA";
	
	/* Port utilisé par notre resource */
	private String idColis;
	private boolean suiteLaunch = true;

	/** PreparedStatement pour insérer un colis saisi dans l'agence */
	private PreparedStatement psInsertColisSaisi;
	/** PreparedStatement pour insérer un colis a saisir dans l'agence */
	private PreparedStatement psInsertColisASaisir;
	/** PreparedStatement pour insérer un specif colis / MAJ le champs etapes*/
	private PreparedStatement psUpdateColisSpecifEtapes;
	/** PreparedStatement pour insérer un specif colis / MAJ le champs info_supp */
	private PreparedStatement psUpdateColisSpecifInfoSupp;
	/** PreparedStatement pour insérer un specif colis / MAJ le champs specifs_services */
	private PreparedStatement psUpdateColisSpecifService;
	/** PreparedStatement pour insérer un specif evt / MAJ le champs specifs_evt */
	private PreparedStatement psUpdateColisSpecifEvt;
	/** PreparedStatement pour insérer un specif colis / MAJ le champs service */
	private PreparedStatement psUpdateColisService;
	/** PreparedStatement pour insérer un specif colis */
	private PreparedStatement psInsertColisSpecifAll;
	/** PreparedStatement pour vider les colis saisi et a saisir sur l'agence */
	private PreparedStatement psTruncateColisAgence;
	/** PreparedStatement pour vider les colis spécifications */
	private PreparedStatement psTruncateColisSpec;
	/** PreparedStatement pour MAJ du colis spécification/ MAJ le champs alertes */
	private PreparedStatement psAttachAlerteToSpecifColis;
	/** PreparedStatement pour insérer les colis restants tg2*/
	private PreparedStatement psUpdateColisRestantTg2;
	
    /** @return VisionMicroserviceApplication.cassandraSession (a com.datastax.driver.core ) */
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }
    
    /**
     * Binding the service to the instantiation of the resource
     */
    @Override
	protected Application configure() {
		/*
		 * Si le cluster n'existe pas déjà, alors il faut le créer et considérer que le test est isolé (lancé seul)
		 */
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
		SyntheseAgenceResource resourceSyntheseAgenceResourceTest = new SyntheseAgenceResource();
		resourceSyntheseAgenceResourceTest.setService(SyntheseAgenceServiceImpl.INSTANCE);

		forceSet(TestProperties.CONTAINER_PORT, "0");

		ResourceConfig config = new ResourceConfig();
		config.register(resourceSyntheseAgenceResourceTest);
		return config;
	}

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();

        SyntheseAgenceServiceImpl.INSTANCE.setDao(SyntheseAgenceDaoImpl.INSTANCE);

        psInsertColisSaisi = getSession().prepare(
                "update colis_agence set colis_saisis = colis_saisis + ? "
                        + " WHERE " + ETableColisAgence.POSTE_COMPTABLE.getNomColonne() + " = ? "
                        + "AND    " + ETableColisAgence.JOUR.getNomColonne() + " = ?"
                        + "AND    " + ETableColisAgence.HEURE.getNomColonne() + " = ?"
                        + "AND    " + ETableColisAgence.MINUTE.getNomColonne() + " = ?"
                        );

        psInsertColisASaisir = getSession().prepare(
                "update colis_agence set colis_a_saisir = colis_a_saisir + ? "
                        + " WHERE " + ETableColisAgence.POSTE_COMPTABLE.getNomColonne() + " = ? "
                        + "AND    " + ETableColisAgence.JOUR.getNomColonne() + " = ?"
                        + "AND    " + ETableColisAgence.HEURE.getNomColonne() + " = ?"
                        + "AND    " + ETableColisAgence.MINUTE.getNomColonne() + " = ?"
                        );

        psUpdateColisSpecifEtapes  = getSession().prepare(
                "update "+ ETableColisSpecifications.TABLE_NAME
                + " set " + ETableColisSpecifications.ETAPES
                + "[?] = ?  WHERE " 
                + ETableColisSpecifications.NO_LT.getNomColonne() + " = ?"
                        );
        
        psUpdateColisSpecifInfoSupp  = getSession().prepare(
                "update " + ETableColisSpecifications.TABLE_NAME
                + " set " + ETableColisSpecifications.INFO_SUPP 
                + "[?] = ?  WHERE " 
                + ETableColisSpecifications.NO_LT.getNomColonne() + " = ?"
                        );
        
        psUpdateColisSpecifService = getSession().prepare(
                "update "+ ETableColisSpecifications.TABLE_NAME
                + " set " + ETableColisSpecifications.SPECIFS_SERVICE
                + "[?] = ?  WHERE " 
                + ETableColisSpecifications.NO_LT.getNomColonne() + " = ?"
                        );
        
        psUpdateColisSpecifEvt = getSession().prepare(
                "update "+ ETableColisSpecifications.TABLE_NAME
                + " set " + ETableColisSpecifications.SPECIFS_EVT
                + "[?] = ?  WHERE " 
                + ETableColisSpecifications.NO_LT.getNomColonne() + " = ?"
                        );
        
        psUpdateColisService = getSession().prepare(
                "update "+ ETableColisSpecifications.TABLE_NAME
                + " set " + ETableColisSpecifications.SERVICE
                + "[?] = ?  WHERE " 
                + ETableColisSpecifications.NO_LT.getNomColonne() + " = ?"
                        );
        
        psInsertColisSpecifAll  = getSession().prepare(
                "insert into " + ETableColisSpecifications.TABLE_NAME + "("
                + ETableColisSpecifications.NO_LT.getNomColonne()  + ","
                + ETableColisSpecifications.CONTRAT.getNomColonne()  + ","
                + ETableColisSpecifications.CODE_POSTAL.getNomColonne()  + ")"
                + " VALUES(?, ?, ?); " );
        
        psAttachAlerteToSpecifColis =  getSession().prepare("update " + ETableColisSpecifications.TABLE_NAME
		+ " USING TTL " + TTL.COLISAGENCE.getTimelapse()
		+ " set "+ ETableColisSpecifications.ALERTES.getNomColonne() + " = " + ETableColisSpecifications.ALERTES.getNomColonne() +" +  ? " 
		+ " WHERE "+ ETableColisSpecifications.NO_LT.getNomColonne() + " = ? "
		);
        
        psUpdateColisRestantTg2 = getSession().prepare("update colis_agence set colis_restant_tg2 = colis_restant_tg2 + ? "
                + " WHERE " + ETableColisAgence.POSTE_COMPTABLE.getNomColonne() + " = ? "
                + "AND    " + ETableColisAgence.JOUR.getNomColonne() + " = ?"
                + "AND    " + ETableColisAgence.HEURE.getNomColonne() + " = ?"
                + "AND    " + ETableColisAgence.MINUTE.getNomColonne() + " = ?"
                );
        
        psTruncateColisAgence = getSession().prepare("truncate colis_agence");
        psTruncateColisSpec = getSession().prepare("truncate colis_specifications");
	}
    
    @AfterMethod
    public void cleanAfterTest() {
		cleanColisSpecifications();
    	cleanAgence();
    }
    
	@AfterClass
	public void cleanAfterClass() throws Exception {
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
	}

    /*=================================*/
	/*  Test 1 : NbColisDisperses      */
    /*=================================*/
    @Test
    /**  Entrée : 1 colis avec une étape DISPERSION
     * 
     *  Attendu : Nbre de colis disperse = 1
     * */
	public void test1_1_NbColisDisperses() throws Exception {
		/* init */
		idColis = "colis1_1";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|||||99999", MAINTENANT);

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(1, result.getNbColisDisperses());
	}

    @Test
    /**  Entrée : 1 colis avec une étape CONCENTRATION
     * 
     *  Attendu : Nbre de colis disperse = 0
     * */
	public void test1_2_NbColisDisperses() throws Exception {
		/* init */
		idColis = "colis1_2";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, CONCENTRATION.getCode() + "|||||99999", MAINTENANT);

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisDisperses());
	}

    @Test
    /**  Entrée : 1 colis avec une étape DISPERSION mais datée d'hier
     * 
     *  Attendu : Nbre de colis disperse = 0
     * */
	public void test1_3_NbColisDisperses() throws Exception {
		/* init */
		idColis = "colis1_3";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|||||99999", HIER);

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisDisperses());
	}

    @Test
    /**  Entrée : Entrée : comme test1_1 mais sur mauvaise agence
     * 
     *  Attendu : Nbre de colis disperse = 0
     * */
	public void test1_4_NbColisDisperses_notFromAgence() throws Exception {
		/* init */
		idColis = "colis1_4";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|||||XRE", MAINTENANT);

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisDisperses());
	}

    @Test
    /**  Entrée : Entrée : comme test1_1 mais sans agence
     * 
     *  Attendu : Nbre de colis disperse = 0
     * */
	public void test1_4_NbColisDisperses_noAgence() throws Exception {
		/* init */
		idColis = "colis1_4";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|||||", MAINTENANT);

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisDisperses());
	}

    /*=================================*/
	/*  Test 2 : NbColisDispersePoste  */
    /*=================================*/
    @Test
    /**  Entrée : 1 colis avec une étape DISPERSION à la POSTE
     * 
     *  Attendu : Nbre de colis dispersePoste = 1
     * */
	public void test2_1_NbColisDispersesPoste() throws Exception {
		/* init */
		idColis = "colis2_1";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|RBP|||99999", MAINTENANT);

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(1, result.getNbColisDispersesPoste());
	}

    @Test
    /**  Entrée : 1 colis avec une étape DISPERSION à la non POSTE
     * 
     *  Attendu : Nbre de colis dispersePoste = 0
     * */
	public void test2_2_NbColisDispersesPoste() throws Exception {
		/* init */
		idColis = "colis2_2";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD||||99999", MAINTENANT);

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisDispersesPoste());
	}

    /*=================================*/
    /*  Test 3 : NbColisSDSeche        */
    /*=================================*/
    @Test
    /**  Entrée : 1 colis avec une étape DISPERSION seule
     * 
     *  Attendu : Nbre de colis NbColisSDSeche = 1
     * */
    public void test3_1_NbColisSDSeche() throws Exception {
    	/* init */
    	idColis = "colis3_1";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|00DIF|||99999", MAINTENANT);
    	
    	/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
    	assertIntEquals(1, result.getNbColisSDSeche());
    }
    
    @Test
    /**  Entrée : 1 colis avec une étape DISPERSION + PREPA_DISTRI
     * 
     *  Attendu : Nbre de colis NbColisSDSeche = 0 car déjà en distri
     * */
	public void test3_2_NbColisSDSeche() throws Exception {
		/* init */
		idColis = "colis3_2";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|00DIF|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis, PREPA_DISTRI.getCode() + "|TA|00DIF|||99999", MAINTENANT.plusMinutes(1));

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisSDSeche());
	}

    @Test
    /**  Entrée : 1 colis avec une étape DISPERSION seule mais à la poste
     * 
     *  Attendu : Nbre de colis NbColisSDSeche = 0
     * */
	public void test3_3_NbColisSDSeche() throws Exception {
		/* init */
		idColis = "colis3_3";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|RBP|||99999", MAINTENANT);

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisSDSeche());
		assertIntEquals(1, result.getNbColisSDSechePoste());
	}

    @Test
    /**  Entrée : 1 colis avec une étape DISPERSION seule mais avec exclu du jour
     * 
     * 
     *  Attendu : Nbre de colis NbColisSDSeche = 0 car exclu
     * */
	public void test3_4_NbColisSDSeche() throws Exception {
		/* init */
		idColis = "colis3_4";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|02DIF|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis, EXCLUSION.getCode() + "|SD|02|||99999", MAINTENANT.plusMillis(1));

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisSDSeche());
	}

    @Test
    /**  Entrée : 1 colis avec une étape DISPERSION seule mais avec exclu à J (le meme jour donc pas exclu du jour)
     * 
     * Ce test devra être modifié lorsque l'on considérera les exclu a J (avant 14h)
     * 
     *  Attendu : Nbre de colis NbColisSDSeche = 0 car exclu
     * */
	public void test3_5_NbColisSDSecheAvant14H() throws Exception {
		/* init */
		idColis = "colis3_5_avant_14h";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|00DIF|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis, EXCLUSION.getCode() + "|SD|00|||99999", MAINTENANT.plusMillis(1));

		String dateAppelAvant14H = DateTime.now(DateTimeZone.forID("America/Guadeloupe")).withHourOfDay(10).toString();

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite(dateAppelAvant14H);

		/* Check */
		assertIntEquals(0, result.getNbColisSDSeche());
	}

    @Test
    /**  Entrée : 1 colis avec une étape DISPERSION seule mais avec exclu à J (le meme jour donc pas exclu du jour)
     * 
     *  Attendu : Nbre de colis NbColisSDSeche = 1
     * */
	public void test3_5_NbColisSDSecheApres14H() throws Exception {
		/* init */
		idColis = "colis3_5_apres_14h";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|00DIF|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis, EXCLUSION.getCode() + "|SD|00|||99999", MAINTENANT.plusMillis(1));

		String dateAppelApres14H = DateTime.now().withHourOfDay(15).toString();

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite(dateAppelApres14H);

		/* Check */
		assertIntEquals(1, result.getNbColisSDSeche());
	}

	@Test
    /**  Entrée : 1 colis a saisir (mais non saisi aujourd'hui) 
     *            avec une étape DISPERSION d'hier avec exclu à J+1 
     * 
     *  Attendu : Nbre de colis NbColisSDSeche = 0 car Dispersion n'est pas derniére étape
     * */
	public void test3_6_NbColisSDSeche() throws Exception {
		/* init */
		idColis = "colis3_6";
		newColisASaisirAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|01DIF|||99999", HIER);
		updateColisSpecifEtapes(idColis, EXCLUSION.getCode() + "|SD|01|||99999", HIER.plusMillis(1));

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisSDSeche());
	}

	@Test
    /**  Entrée : 1 colis avec une étape DISPERSION + LIVRAISON
     * 
     *  Attendu : Nbre de colis NbColisSDSeche = 0
     * */
	public void test3_7_NbColisSDSeche() throws Exception {
		/* init */
		idColis = "colis3_7";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|00DIF|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis, LIVRAISON.getCode() + "|D|00DIF|||99999", MAINTENANT.plusMinutes(1));

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisSDSeche());
	}

    @Test
	/**  Entrée : 1 colis avec une étape DISPERSION et PREPA_DISTRI et présent dans Colis a remettre en distri
     * Ce test est différent de 3_7 car si 3_7 est OK, la présence du colis dans les colis à remettre en distri
     * ajoutait le colis quand meme dans les sd seches
     * 
     *  Attendu : Nbre de colis NbColisSDSeche = 0
     * */
	public void test3_8_NbColisSDSeche() {
		/* init */
    	idColis = "colis3_8";
    	newColisAgence(idColis);
    	newColisASaisirAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|00DIF|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis, PREPA_DISTRI.getCode() + "|TA|23T12|||99999", MAINTENANT.plusMinutes(1));

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisSDSeche());
	}
    /*=================================*/   
    /*  Test 4 : NbColisSDSechePoste  */
    /*=================================*/
    @Test
    /**  Entrée : 1 colis avec une étape DISPERSION Poste seule
     * 
     *  Attendu : Nbre de colis NbColisSDSechePoste = 1
     * */
	public void test4_1_NbColisSDSechePoste() throws Exception {
		/* init */
		idColis = "colis4_1";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|RBP|||99999", MAINTENANT);

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(1, result.getNbColisSDSechePoste());
	}

	@Test
    /**  Entrée : 1 colis avec une étape DISPERSION Poste et une etape préparé POSTE
     * 
     *  Attendu : Nbre de colis NbColisSDSechePoste = 1
     * */
	public void test4_2_NbColisSDSechePoste() throws Exception {
		/* init */
		idColis = "colis4_2";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|RBP|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis, PREPA_DISTRI.getCode() + "|TA|POSTE|||99999", MAINTENANT.plusMinutes(1));

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisSDSechePoste());
	}

	@Test
    /**  Entrée : 1 colis avec une étape DISPERSION Poste et une etape LIVRAISON
     * 
     *  Attendu : Nbre de colis NbColisSDSechePoste = 1
     * */
	public void test4_3_NbColisSDSechePoste() throws Exception {
		/* init */
		idColis = "colis4_3";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|RBP|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis, LIVRAISON.getCode() + "|D|92T18|||99999", MAINTENANT.plusMinutes(1));

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisSDSechePoste());
	}

	@Test
    /**  Entrée : 1 colis avec une étape DISPERSION Poste et une etape d'exclusion J+1
     * 
     *  Attendu : Nbre de colis NbColisSDSechePoste = 1
     * */
	public void test4_4_NbColisSDSechePoste() throws Exception {
		/* init */
		idColis = "colis4_4";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|RBP|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis, EXCLUSION.getCode() + "|SD|1|||99999", MAINTENANT.plusMillis(1));

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisSDSechePoste());
	}

	@Test
    /**  Entrée : 1 colis avec une étape DISPERSION Poste puis une etape DISPERSION non POSTE
     * 
     *  Attendu : Nbre de colis NbColisSDSechePoste = 1
     * */
	public void test4_5_NbColisSDSechePoste() throws Exception {
		/* init */
		idColis = "colis4_5";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|RBP|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|45M|||99999", MAINTENANT.plusMinutes(1));

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisSDSechePoste());
	}
    
    /*=================================*/   
    /*  Test 5 : NbColisPrepares       */
    /*=================================*/   
	@Test
    /**  Entrée : 1 colis avec une étape PREPARE simple
     * 
     *  Attendu : Nbre de colis NbColisPrepares = 1
     * */
	public void test5_1_NbColisPrepares() throws Exception {
		/* init */
		idColis = "colis5_1";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, PREPA_DISTRI.getCode() + "|TA|R12|||99999", MAINTENANT);

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(1, result.getNbColisPrepares());
	}

	@Test
    /**  Entrée : 1 colis avec une étape PREPARE POSTE
     * 
     *  Attendu : Nbre de colis NbColisPrepares = 0
     * */
	public void test5_2_NbColisPrepares() throws Exception {
		/* init */
		idColis = "colis5_2";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, PREPA_DISTRI.getCode() + "|TA|LAPOSTE|||99999", MAINTENANT);

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisPrepares());
	}  
	
	@Test
    /**  Entrée : comme test5_1 mais sur mauvaise agence
     * 
     *  Attendu : Nbre de colis NbColisPrepares = 0
     * */
	public void test5_3_NbColisPrepares_notFromAgence() throws Exception {
		/* init */
		idColis = "colis5_3";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, PREPA_DISTRI.getCode() + "|TA|R12|||XRE", MAINTENANT);

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisPrepares());
	}
	
	/*=================================*/
    /*  Test 6 : NbColisExclus         */
    /*=================================*/
	@Test
    /**  Entrée : 1 colis avec derniere etape = EXCLUSION
     * 
     *  Attendu : Nbre de colis NbExclu = 1
     * */
	public void test6_1_NbColisExclu() throws Exception {
		/* init */
		idColis = "colis6_1";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|02DIF|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis, EXCLUSION.getCode() + "|SD|02|||99999", MAINTENANT.plusMillis(1));

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(1, result.getNbColisExclusLivraisonXJours());
	}

	@Test
    /**  Entrée : 1 colis avec une etape = EXCLUSION mais derniere etape = DISPERSION
     * 
     *  Attendu : Nbre de colis NbExclu = 0
     * */
	public void test6_2_NbColisExclu() throws Exception {
		/* init */
		idColis = "colis6_2";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|02DIF|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis, EXCLUSION.getCode() + "|SD|02|||99999", MAINTENANT.plusMillis(1));
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|12R24|||99999", MAINTENANT.plusMinutes(1));

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisExclusLivraisonXJours());
	}
	
	@Test
    /**  Entrée : 1 colis avec une etape = EXCLUSION 1 jour et une etape = DISPERSION mais exclusion est la derniere
     * 
     *  Attendu : Nbre de colis NbExclu = 0
     * */
	public void test6_3_NbColisExclu() throws Exception {
		/* init */
		idColis = "colis6_3";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|12R24|||99999", MAINTENANT.plusMinutes(-1));
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|01DIF|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis, EXCLUSION.getCode() + "|SD|01|||99999", MAINTENANT.plusMillis(1));

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisExclusLivraisonXJours());
		assertIntEquals(1, result.getNbColisExclusLivraisonUnJour());
	}
	
	@Test
    /**  Entrée : Même colis que plus haut mais à saisir pour le lendemain 00:00
     * 
     *  Attendu : Le colis ne doit pas apparaître (correction de bug QC57)
     * */
	public void test6_4_colisNonRemisEnDistribution() throws Exception {
		/* init */
		idColis = "colis6_4";
		newColisASaisirAgence(idColis, TODAY.plusDays(1));
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|12R24", MAINTENANT.minusDays(1).plusMinutes(-1));
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|01DIF", MAINTENANT.minusDays(1));
		updateColisSpecifEtapes(idColis, EXCLUSION.getCode() + "|SD|01", MAINTENANT.minusDays(1).plusMillis(1));

		/* Execute */
		SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(ECodeIndicateur.NON_REMIS_EN_DISTRIBUTION.getCode(), 50, 0);

		/* Check */
		assertIntEquals(0, result.getTotalColis());
	}
	
	@Test
    /**  Entrée : comme test_6_1 amsi sur mauvaise agence
     * 
     *  Attendu : Nbre de colis NbExclu = 0
     * */
	public void test6_1_NbColisExclu_notFromAgence() throws Exception {
		/* init */
		idColis = "colis6_1";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|02DIF|||XRE", MAINTENANT);
		updateColisSpecifEtapes(idColis, EXCLUSION.getCode() + "|SD|02|||XRE", MAINTENANT.plusMillis(1));

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertIntEquals(0, result.getNbColisExclusLivraisonXJours());
	}
	
	/*=================================*/
    /*  Test 7 : nbColisDispersesPoste */
    /*=================================*/
	@Test
    /**  Entrée : 4 colis, 2 à prendre en charge, 2 que la Poste doit prendre en charge 
     * 
     *  Attendu : 4 colis dispersés dont 2 pour la poste. 2 pris en charge par la Poste, 2 par nous
     * */
	public void test7_1_nbColisDispersesPourLaPoste() throws Exception {
		/* init */
		// insert deux colis que doit gérer la poste
		String idColis1 = "colis7";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, DISPERSION.getCode() + "|SD|62RBP||AGMAM3|99999", MAINTENANT.plusMinutes(-1));
		updateColisSpecifEtapes(idColis1, ACQUITTEMENT_LIVRAISON.getCode() + "|TA|LAPOSTE||IFVSIO|99999", MAINTENANT);
		String idColis2 = "colis8";
		newColisAgence(idColis2);
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|SD|63RBP||AGMAM4|99999", MAINTENANT.plusMinutes(-1));
		updateColisSpecifEtapes(idColis2, ACQUITTEMENT_LIVRAISON.getCode() + "|TA|LAPOSTE||IFVSIO|99999", MAINTENANT);
		// insert deux colis que nous devons gérer
		String idColis3 = "colis9";
		newColisAgence(idColis3);
		updateColisSpecifEtapes(idColis3, DISPERSION.getCode() + "|SD|||AGMAM3|99999", MAINTENANT.plusMinutes(-1));
		updateColisSpecifEtapes(idColis3, ACQUITTEMENT_LIVRAISON.getCode() + "|TA|||IFVSIO|99999", MAINTENANT);
		idColis = "colis10";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|||AGMAM3|99999", MAINTENANT.plusMinutes(-1));
		updateColisSpecifEtapes(idColis, ACQUITTEMENT_LIVRAISON.getCode() + "|TA|||IFVSIO|99999", MAINTENANT);

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		assertEquals(4, result.getNbColisDisperses().intValue());
		assertEquals(2, result.getNbColisDispersesPoste().intValue());
		assertEquals(2, result.getNbColisAPreparer().intValue());
		assertEquals(2, result.getNbColisPrisEnChargePoste().intValue());
	}
	
	/*=================================================*/
    /*  Test 8 : getDispersionActivite avec indicateur */
    /*=================================================*/
    @Test
    /**  Entrée : 3 colis dont 2 dispersions et 1 livraion pour la même agence
     * 
     *  Attendu : 2 colis dispersés
     * */
	public void test8_1_getDispersionActivite() throws Exception {
		/* init */
    	String idColis1 = "colis8_1_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, DISPERSION.getCode() + "|SD|00DIF|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis1, PREPA_DISTRI.getCode() + "|TA|00DIF|||99999", MAINTENANT.plusMinutes(1));
    	String idColis2 = "colis8_1_2";
		newColisAgence(idColis2);
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|SD|00DIF|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis2, PREPA_DISTRI.getCode() + "|TA|00DIF|||99999", MAINTENANT.plusMinutes(1));
    	idColis = "colis8_1_3";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, ACQUITTEMENT_LIVRAISON.getCode() + "|TA|00DIF|||99999", MAINTENANT);

		/* Execute */
		List<SyntheseColis>  result = appelMSActiviteEtListeValeurAvecIndicateur(ECodeIndicateur.DISPERSES.getCode(),null, 0).getColis();

		/* Check */
		assertIntEquals(2, result.size());
	}

    @Test
    /**  Entrée : 3 colis dont 2 préparés et 1 livraion pour la même agence
     * 
     *  Attendu : 2 colis préparés
     * */
	public void test8_2_getDispersionActivite() throws Exception {
		/* init */
    	String idColis1 = "colis8_2_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, PREPA_DISTRI.getCode() + "|SD|00DIF|||99999", MAINTENANT);
    	String idColis2 = "colis8_2_2";
		newColisAgence(idColis2);
		updateColisSpecifEtapes(idColis2, PREPA_DISTRI.getCode() + "|SD|00DIF|||99999", MAINTENANT);
    	idColis = "colis8_2_3";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, ACQUITTEMENT_LIVRAISON.getCode() + "|TA|00DIF|||99999", MAINTENANT);

		/* Execute */
		List<SyntheseColis>  result = appelMSActiviteEtListeValeurAvecIndicateur(ECodeIndicateur.PREPARES.getCode(), null, 0).getColis();

		/* Check */
		assertIntEquals(2, result.size());
	}

    @Test
    /**  Entrée : 3 colis dont 2 dispersions poste et 1 livraion pour la même agence
     * 
     *  Attendu : 2 colis dispersés poste
     * */
	public void test8_3_getDispersionActivite() throws Exception {
		/* init */
    	String idColis1 = "colis8_3_1";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	String idColis2 = "colis8_3_2";
		newColisAgence(idColis2);
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	idColis = "colis8_3_3";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, ACQUITTEMENT_LIVRAISON.getCode() + "|TA|00DIF|||99999", MAINTENANT);

		/* Execute */
		List<SyntheseColis>  result = appelMSActiviteEtListeValeurAvecIndicateur(ECodeIndicateur.DISPERSES_POSTE.getCode(),null,0).getColis();

		/* Check */
		assertIntEquals(2, result.size());
	}

    @Test
    /**  Entrée : 8 colis dispersions poste
     * 
     *  Attendu : 4 colis (limit = 4) triès par noLt
     * */
	public void test8_4_getDispersionActiviteWithLimit() throws Exception {
		/* init */
    	String idColis1 = "CCC";
		newColisAgence(idColis1);
		updateColisSpecifEtapes(idColis1, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	String idColis2 = "GGG";
		newColisAgence(idColis2);
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	String idColis3 = "DDD";
		newColisAgence(idColis3);
		updateColisSpecifEtapes(idColis3, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	String idColis4 = "HHH";
		newColisAgence(idColis4);
		updateColisSpecifEtapes(idColis4, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	String idColis5 = "AAA";
		newColisAgence(idColis5);
		updateColisSpecifEtapes(idColis5, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	String idColis6 = "FFF";
		newColisAgence(idColis6);
		updateColisSpecifEtapes(idColis6, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	String idColis7 = "BBB";
		newColisAgence(idColis7);
		updateColisSpecifEtapes(idColis7, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	idColis = "EEE";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);

		/* Execute */
		List<SyntheseColis> result = appelMSActiviteEtListeValeurAvecIndicateur(ECodeIndicateur.DISPERSES_POSTE.getCode(), 4, 0).getColis();

		/* Check */
		assertIntEquals(4, result.size());
		assertEquals("AAA", result.get(0).getNoLt());
		assertEquals("BBB", result.get(1).getNoLt());
		assertEquals("CCC", result.get(2).getNoLt());
		assertEquals("DDD", result.get(3).getNoLt());
	}
    
	/*=============================================*/
    /* Test 11 : getNbColisSDSechePoste on one day */
    /*=============================================*/
    @Test
    /**  Entrée : 6 colis dispersés poste, dont 1 prepa chrono, 1 distrib chrono, 1 exlu, 2 acqPoste
     * 
     *  Attendu : 1 colis seche poste
     * */
	public void test11_1_getDispersionActiviteForDispersesPoste() {
		/* init */
    	// premier colis dispersé poste et acquitté poste TA
    	idColis = "colis11_1_1";
    	newColisAgence(idColis);
    	updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	updateColisSpecifEtapes(idColis, ACQUITTEMENT_LIVRAISON.getCode() + "|TA|LAPOSTE||IFVSIO|99999", MAINTENANT.plusMinutes(1));
    	// deuxiéme colis dispersé poste mais distribué chrono
    	String idColis2 = "colis11_1_2";
    	newColisAgence(idColis2);
    	updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	updateColisSpecifEtapes(idColis2, LIVRAISON.getCode() + "||00DIF|||99999", MAINTENANT.plusMinutes(1));
    	// troisiéme colis dispersé poste mais exclu du jour
    	String idColis3 = "colis11_1_3";
    	newColisAgence(idColis3);
    	updateColisSpecifEtapes(idColis3, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis3, EXCLUSION.getCode() + "|SD|01|||99999", MAINTENANT.plusMinutes(1));
    	// quatrième colis dispersé poste et prepares chrono
    	String idColis4 = "colis11_1_4";
    	newColisAgence(idColis4);
    	updateColisSpecifEtapes(idColis4, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
		updateColisSpecifEtapes(idColis4, PREPA_DISTRI.getCode() + "|SD|01|||99999", MAINTENANT.plusMinutes(1));
    	// cinquiéme colis dispersé poste
    	String idColis5 = "colis11_1_5";
    	newColisAgence(idColis5);
    	updateColisSpecifEtapes(idColis5, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	// sixiéme colis dispersé poste et acquitté poste H
    	String idColis6 = "colis11_1_6";
    	newColisAgence(idColis6);
    	updateColisSpecifEtapes(idColis6, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	updateColisSpecifEtapes(idColis6, ACQUITTEMENT_LIVRAISON.getCode() + "|H|LAPOSTE||IFVSIO|99999", MAINTENANT.plusMinutes(1));

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		// le premier colis et sixième colis
		assertEquals(2, result.getNbColisPrisEnChargePoste().intValue());
		// le premier colis et cinquième colis
		assertEquals(3, result.getNbColisDispersesPoste().intValue());
		// le deuxiéme colis et quatrième colis
		assertEquals(2, result.getNbColisAPreparer().intValue());
		// le troisième colis
		assertEquals(1, result.getNbColisExclusLivraisonUnJour().intValue());
		// le quatrième colis
		assertEquals(1, result.getNbColisPrepares().intValue());
		// le cinquième colis
		assertEquals(1, result.getNbColisSDSechePoste().intValue());
	}
    
    /*===============================================================*/
    /* Test 12 : getNbColisEnDispersion on one day                   */
    /* Entrée  : 4 colis en dispersion                               */
    /* Attendu : Colis en distri ne doit pas être dans SDSeche       */
    /*           Colis en distri ne doit pas être dans SDSeche       */
    /*===============================================================*/
    @Test
    public void test12_1_getDispersionActiviteForColisEnDispersion() {

    	/* init */
    	// premier colis dispersé poste
    	idColis = "colis12_1_1";
    	newColisAgence(idColis);
    	updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	
    	// deuxiéme colis dispersé et distribué poste 
    	String idColis2 = "colis12_1_2";
    	newColisAgence(idColis2);
    	updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	updateColisSpecifEtapes(idColis2, LIVRAISON.getCode() + "|TA|LAPOSTE|||99999", MAINTENANT.plusMinutes(1));
    	
    	// troisiéme colis dispersé chrono
    	String idColis3 = "colis12_1_3";
    	newColisAgence(idColis3);
    	updateColisSpecifEtapes(idColis3, DISPERSION.getCode() + "|SD|01|||99999", MAINTENANT);
    	
    	// quatrième colis dispersé chrono puis remis en distri
    	String idColis4 = "colis12_1_4";
    	newColisASaisirAgence(idColis4, DateTime.now());
    	updateColisSpecifEtapes(idColis4, DISPERSION.getCode() + "|SD|01|||99999", MAINTENANT.minusDays(1));
    	updateColisSpecifEtapes(idColis4, EXCLUSION.getCode() + "|SD|01|||99999", MAINTENANT.minusDays(1).plusMinutes(2));

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
		// le premier et deuxième colis
		assertEquals(2, result.getNbColisDispersesPoste().intValue());
		// le premier colis
		assertEquals(1, result.getNbColisSDSechePoste().intValue());
		// le quatrième colis
		assertEquals(1, result.getNbColisRemisEnDistri().intValue());
		// les trois premiers colis
		assertEquals(3, result.getNbColisDisperses().intValue());
		// le troisème et quatrième colis
		assertEquals(2, result.getNbColisAPreparer().intValue());
		// le troisième
		assertEquals(1, result.getNbColisSDSeche().intValue());
		// le quatrième
		assertEquals(1, result.getNbColisNonRemisEnDistribution().intValue());
    }
    
	/*=============================================*/

    /* Test 13 : getDispersionActiviteEtListeValeurs*/
    /*=============================================*/
    @Test

    /**  Entrée : 3 colis dispersions poste
     * 
     *  Attendu : Taille de la liste des valeurs des codes dispersion égale à 1 
     *  		et contient le code tournée suivant  "62RBP"
     * */
	public void test13_1_getDispersionActiviteEtListeValeurs() throws Exception {
		/* init */
    	
    	idColis = "test13_1_1";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	
		String idColis2 = "test13_1_2";
		newColisAgence(idColis2);
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
		
		String idColis3 = "test13_1_3";
		newColisAgence(idColis3);
		updateColisSpecifEtapes(idColis3, DISPERSION.getCode() + "|SD||||99999", MAINTENANT);
		
		/* Execute */
		SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(ECodeIndicateur.DISPERSES_POSTE.getCode(),null, 0);

		/* Check */
		assertEquals(result.getListeValeurs().getCodeDispersion().size(), 1);
		assertEquals(result.getListeValeurs().getCodeDispersion().contains("62RBP"), true);
	}

    @Test
    /**  Entrée : 4 colis  
     * 		2 colis aves des numéros de contrat égaux et un à null
     * 		2 colis avec des codes postaux égaux
     * 
     *  Attendu : 
     * 			  Taille de la liste des valeurs des numéros de contrats égale à 2
     *  		  Taille de la liste des valeurs des codes postaux égale 3 
     * */
	public void test13_2_getDispersionActiviteEtListeValeurs() throws Exception {
		
    	/* init */
    	idColis = "test13_2_1";
		newColisAgence(idColis);
		newColisSpecif(idColis, "5445", "15424");
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	
		String idColis2 = "test13_2_2";
		newColisAgence(idColis2);
		newColisSpecif(idColis2,"5445","15424");
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
		
		String idColis3 = "test13_2_3";
		newColisAgence(idColis3);
		newColisSpecif(idColis3,"544T","45244");
		updateColisSpecifEtapes(idColis3, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
		
		String idColis4 = "test13_2_4";
		newColisAgence(idColis4);
		newColisSpecif(idColis4,"544F","");
		updateColisSpecifEtapes(idColis4, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
    	
		/* Execute */
		SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(ECodeIndicateur.DISPERSES_POSTE.getCode(),null, 0);

		assertEquals(result.getListeValeurs().getCodePostal().size(),2);
		assertEquals(result.getListeValeurs().getNumeroContrat().size(),3);
	}
    
    @Test

    /**  Entrée : 3 colis avec les info supp suivants DEPOT_RELAIS, DEPOT_RELAIS,REMISE_REGATE
     * 			  1 colis sans info supp
     *  Attendu : 
     *  		 Taille de la liste des valeurs des responsabilités égale à 3
     *  		(CHRONOPOSTE, LAPOSTE, PICKUP)
     * */
	public void test13_3_getDispersionActiviteEtListeValeurs() throws Exception {
		
    	/* init */
    	idColis = "test13_3_1";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
		updateColisSpecifInfoSupp(idColis,"DEPOT_RELAIS","AAA");
    	
		String idColis2 = "test13_3_2";
		newColisAgence(idColis2);
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
		updateColisSpecifInfoSupp(idColis2,"DEPOT_RELAIS","AAA");
		
		String idColis3 = "test13_3_3";
		newColisAgence(idColis3);
		updateColisSpecifEtapes(idColis3, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
		
		String idColis4 = "test13_3_4";
		newColisAgence(idColis4);
		updateColisSpecifEtapes(idColis4, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT);
		updateColisSpecifInfoSupp(idColis4,"REMISE_REGATE","AAA");
		
		/* Execute */
		SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(ECodeIndicateur.DISPERSES_POSTE.getCode(),null, 0);
		
		/* Check */
		assertEquals(result.getListeValeurs().getResponsabilite().size(), 3);
		assertEquals(result.getListeValeurs().getResponsabilite().contains("PICKUP"), true);
		assertEquals(result.getListeValeurs().getResponsabilite().contains("CHRONOPOST"), true);
		assertEquals(result.getListeValeurs().getResponsabilite().contains("LAPOSTE"), true);
	}

    @Test
    /**  Entrée : 4 colis avec des codes tournées distincts 
     * 		1 avec un code dispersion à null
     * 		1 avec codeDispersion.length()=3
     * 		2 avec codeDispersion.substring(2,4) retourne la même chaine de caractère
     * 
     *  Attendu : Taille de la liste des valeurs des codes tournées égale à 3 
     *  		    et contient les codes tournées suivants "xxx", "59T51" et "20T51"
     *  		  
     *  		Taille de la liste des valeurs des codes grappes égale à 1
     * 			    et contient le code Grappe suivant : "T5"
     * */
	public void test13_4_getDispersionActiviteEtListeValeurs() throws Exception {
		
    	/* init */
    	idColis = "test13_4_1";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|59T51|||99999", MAINTENANT);
    	
		String idColis2 = "test13_4_2";
		newColisAgence(idColis2);
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|SD|20T51|||99999", MAINTENANT);
		
		String idColis3 = "test13_4_3";
		newColisAgence(idColis3);
		updateColisSpecifEtapes(idColis3, DISPERSION.getCode() + "|SD|xxx|||99999", MAINTENANT);
		
		String idColis4 = "test13_4_4";
		newColisAgence(idColis4);
		updateColisSpecifEtapes(idColis4, DISPERSION.getCode() + "|SD||||99999", MAINTENANT);
    	
		/* Execute */
		SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(ECodeIndicateur.DISPERSES.getCode(),null, 0);

		/* Check */
		assertEquals(result.getListeValeurs().getCodeDispersion().size(),3);
		assertEquals(result.getListeValeurs().getCodeDispersion().contains("xxx"), true);
		assertEquals(result.getListeValeurs().getCodeDispersion().contains("59T51"), true);
		assertEquals(result.getListeValeurs().getCodeDispersion().contains("20T51"), true);
		assertEquals(result.getListeValeurs().getCodeGrappe().size(), 1);
		assertEquals(result.getListeValeurs().getCodeGrappe().contains("T5"), true);

	}
    
	/*=============================================*/

    /* Test 14 : getDispersionActiviteAveccIndicateurEtFiltre*/
    /*=============================================*/
    
    @Test
    /**  Entrée : 1 colis avec le noLt ="test14_1_1"
     * 					Code tournee : null
     * 					Numero contrat : "5445"
     * 					Code postal : "15424"
     * 					Responsabilite : null
     * 					Precocite : null
     * 					PosteSaisie : null
     * 					CodeGrappe : null
     * 					Code dispersion = "62RB4"
     * 					Caracteristique = null
     * 					isPropage = null
     * 
     *            1 colis avec avec le noLt ="test14_1_2"
     * 		      		Code tournee : null
     * 					Numero contrat : "5443"
     * 					Code postal : "15423"
     * 					Responsabilite : "LAPOSTE"
     * 					Precocite : "18H"
     * 					PosteSaisie : "PSMD578"
     * 					CodeGrappe : "T5"
     * 					Code dispersion = 62RB1 
     * 					Caracteristique = null
     * 					isPropage = null
     * 
     * 			  Dans les critères, on choisit les valeurs qui correspond au deuxième 
     * 			  
     *  Attendu : 
     *  		un seul colis avec le NoLt égale "test14_1_2"
     * */
	public void test14_1_getDispersionActiviteAveccIndicateurEtFiltre() throws Exception {
		
    	/* init */
    	idColis = "test14_1_1";
		newColisAgence(idColis);
		newColisSpecif(idColis, "5445", "15424");
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|62RB4|||99999", MAINTENANT);
		
		String idColis2 = "test14_1_2";
		newColisAgence(idColis2);
		newColisSpecif(idColis2, "5443", "15423");
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|P|62RB1||PSMD578|99999", MAINTENANT);
		updateColisSpecifInfoSupp(idColis2,"REMISE_REGATE","AAA");
		updateColisSpecifService(idColis2, MAINTENANT, Sets.newHashSet(ESpecificiteColis.DIX_HUIT_HEURE.getCode()));
		updateColisSpecifEvt(idColis2, MAINTENANT, ESpecificiteColis.FROID.getCode());
		updateColisSpecifEvt(idColis2, MAINTENANT.minusMinutes(2), ESpecificiteColis.FRESH.getCode());
		
		SyntheseListeValeurs criteres = new SyntheseListeValeurs();
		criteres.addToNumeroContrat("5443");
		criteres.addToCodePostal("15423");
		criteres.addToCodeDispersion("62RB1");
		criteres.addToResponsabilite("LAPOSTE");
		criteres.addToPrecocite(ESpecificiteColis.DIX_HUIT_HEURE.getCode());
		criteres.addToPosteSaisie("PSMD578");
		criteres.addToCodeGrappe("RB");
		criteres.addToPropagation("Oui");
		criteres.addToQualificatif(ESpecificiteColis.FROID.getCode());
		
		/* Execute */
		List<SyntheseColis> result = appelMSDispersionActiviteAvecIndicateurEtFiltre(criteres, ECodeIndicateur.DISPERSES.getCode() ,null).getColis();
		
		/* Check */
		assertEquals(result.size(), 1);
		assertEquals(result.get(0).getNoLt(), "test14_1_2");
	}

    @Test
    /**  Entrée : 1 colis avec le noLt ="test14_2_1"
     * 					Code tournee : "20T60"
     * 					Numero contrat : null
     * 					Code postal : null
     * 					Responsabilite : null
     * 					Precocite : null
     * 					PosteSaisie : null
     * 					CodeGrappe : null
     * 					Code dispersion = null 
     * 					Caracteristique = null
     * 					isPropage = null
     * 
     *            1 colis avec avec le noLt ="test14_2_2"
     * 		      		Code tournee : "20T51"
     * 					Numero contrat : "5443"
     * 					Code postal : "15423"
     * 					Responsabilite : "LAPOSTE"
     * 					Precocite : "18H"
     * 					Qualificatif : "18H"
     * 					PosteSaisie : "PSMD578"
     * 					CodeGrappe : "T5"
     * 					Code dispersion = null 
     * 					Caracteristique = null
     * 					isPropage = "Oui"
     * 
     * 			  Dans les critères, on choisit les valeurs qui correspond au deuxième 
     * 			  
     *  Attendu : 
     *  		un seul colis avec le NoLt égale "test14_2_2"
     * */
	public void test14_2_getDispersionActiviteAveccIndicateurEtFiltre() throws Exception {
    	/* init */
    	idColis = "test14_2_1";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, PREPA_DISTRI.getCode() + "|TA|20T60|||99999", MAINTENANT);
		
		String idColis2 = "test14_2_2";
		newColisAgence(idColis2);
		newColisSpecif(idColis2, "5443", "15423");
		updateColisSpecifEtapes(idColis2, PREPA_DISTRI.getCode() + "|P|20T51||PSMD578|99999", MAINTENANT);
		updateColisSpecifInfoSupp(idColis2,"REMISE_REGATE","AAA");
		updateColisSpecifService(idColis2,MAINTENANT, Sets.newHashSet( ESpecificiteColis.DIX_HUIT_HEURE.getCode()));
	
		
		SyntheseListeValeurs criteres = new SyntheseListeValeurs();
		criteres.addToNumeroContrat("5443");
		criteres.addToCodePostal("15423");
		criteres.addToCodeTournee("20T51");
		criteres.addToResponsabilite("LAPOSTE");
		criteres.addToPrecocite(ESpecificiteColis.DIX_HUIT_HEURE.getCode());
		criteres.addToQualificatif(ESpecificiteColis.DIX_HUIT_HEURE.getCode());
		criteres.addToPosteSaisie("PSMD578");
		criteres.addToPropagation("Oui");
		
		/* Execute */
		List<SyntheseColis> result = appelMSDispersionActiviteAvecIndicateurEtFiltre(criteres, ECodeIndicateur.PREPARES.getCode() ,null).getColis();
		
		/* Check */
		assertEquals(result.size(), 1);
		assertEquals(result.get(0).getNoLt(), "test14_2_2");
	}
    
    @Test
    /**  Entrée : 1 colis avec le noLt ="test14_3_1"
     * 					Code tournee : "20T51"
     * 					Numero contrat : "5451"
     * 					Code postal : "15400"
     * 					Responsabilite : "LAPOSTE"
     * 					Precocite : "13H"
     * 					PosteSaisie : "PSMD514"
     * 					CodeGrappe : "T6"
     * 					Code dispersion = null 
     * 					Caracteristique = null
     * 					isPropage = "Oui"
     * 
     *            1 colis avec avec le noLt ="test14_3_2"
     * 		      		Code tournee : "20T51"
     * 					Numero contrat : "5443"
     * 					Code postal : "15423"
     * 					Responsabilite : "LAPOSTE"
     * 					Precocite : "18H"
     * 					PosteSaisie : "PSMD578"
     * 					CodeGrappe : "T5"
     * 					Code dispersion = null 
     * 					Caracteristique = null
     * 					isPropage = "Oui"
     * 
     * 		
     * 			  
     *  Attendu : 
     *  		les deux colis
     * */
	public void test14_3_getDispersionActiviteAveccIndicateurEtFiltre() throws Exception {
    	/* init */
    	idColis = "test14_3_1";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, PREPA_DISTRI.getCode() + "|TA|20T60|||99999", MAINTENANT);
		
		String idColis2 = "test14_3_2";
		newColisAgence(idColis2);
		newColisSpecif(idColis2, "5443", "15423");
		updateColisSpecifEtapes(idColis2, PREPA_DISTRI.getCode() + "|P|20T51||PSMD578|99999", MAINTENANT);
		updateColisSpecifInfoSupp(idColis2,"REMISE_REGATE","AAA");
		updateColisSpecifService(idColis2,MAINTENANT, Sets.newHashSet( ESpecificiteColis.DIX_HUIT_HEURE.getCode()));
		
		SyntheseListeValeurs criteres = new SyntheseListeValeurs();
		criteres.addToNumeroContrat("Tous");
		criteres.addToCodePostal("Tous");
		criteres.addToCodeTournee("Tous");
		criteres.addToResponsabilite("Tous");
		criteres.addToPrecocite("Tous");
		criteres.addToPosteSaisie("Tous");
		criteres.addToPropagation("Tous");
		criteres.addToQualificatif("Tous");
		
		/* Execute */
		List<SyntheseColis> result = appelMSDispersionActiviteAvecIndicateurEtFiltre(criteres, ECodeIndicateur.PREPARES.getCode() ,null).getColis();
		
		/* Check */
		assertEquals(result.size(), 2);
	}
    
    @Test
    /**
     * Entrée : 4 colis à considérer en Incident de dispersion
     * 
     * Attendu : les 4 colis se retrouvent dans l'indicateur nbColisIncidentTG2Jour
     */
    public void test15_incidentTG2Jour() {
    	/* init */
    	idColis = "test15_1";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|ZT|99S99||PSM001|99999", MAINTENANT.minusMinutes(15));
		updateColisSpecifEtapes(idColis, INCIDENT.getCode() + "|ZT|99S99||PSM001|99999", MAINTENANT);
		String idColis2 = "test15_2";
		newColisAgence(idColis2);
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|ZA|99S99||PSM001|99999", MAINTENANT.minusMinutes(15));
		updateColisSpecifEtapes(idColis2, INCIDENT.getCode() + "|ZA|99S99||PSM001|99999", MAINTENANT);
		String idColis3 = "test15_3";
		newColisAgence(idColis3);
		updateColisSpecifEtapes(idColis3, DISPERSION.getCode() + "|ZC|99S99||PSM001|99999", MAINTENANT.minusMinutes(15));
		updateColisSpecifEtapes(idColis3, INCIDENT.getCode() + "|ZC|99S99||PSM001|99999", MAINTENANT);
		String idColis4 = "test15_4";
		newColisAgence(idColis4);
		updateColisSpecifEtapes(idColis4, DISPERSION.getCode() + "|SD|36ERT||PSM001|99999", MAINTENANT.minusMinutes(15));
		updateColisSpecifEtapes(idColis4, INCIDENT.getCode() + "|SD|36ERT||PSM001|99999", MAINTENANT);
		
		/* Execute */
		List<SyntheseColis> resultIncident = appelMSActiviteEtListeValeurAvecIndicateur(ECodeIndicateur.INCIDENT_TG2_JOUR.getCode(), 50, 0).getColis();
		SyntheseDispersionQuantite resultQuantite = appelMSDispersionQuantite();
		
		/* Check */
		
		/* On doit avoir 4 colis en incidents */
		assertEquals(resultIncident.size(), 4);

		/* Récupération des 4 noLt des colis (car l'ordre n'est pas fixe) */
		Set<String> colisEnIncident = new HashSet<>();
		for (SyntheseColis incident: resultIncident){
			colisEnIncident.add(incident.getNoLt());
		}
		
		/* Et test que les 4 noLt sont bien ceux attendus */
		assert(colisEnIncident.contains("test15_2"));
		assert(colisEnIncident.contains("test15_1"));
		assert(colisEnIncident.contains("test15_4"));
		assert(colisEnIncident.contains("test15_3"));
		assertEquals(resultQuantite.getNbColisIncidentTG2Jour().intValue(), 4);
    }
    
    @Test
    /**
     * Entrée : 1 colis RA (IncidentTG2Jour anciennement ExclusJ+1)
     * 
     * Attendu : le colis doit être dans disperé et incident
     */
    public void test16_EvtRA() {
    	/* init */
    	idColis = "test16";
    	newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|RA|||AGMAN1|99999", MAINTENANT.minusHours(1));
		updateColisSpecifEtapes(idColis, INCIDENT.getCode() + "|RA|||AGMAN1|99999", MAINTENANT);
		
		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();
		
		/* Check */
		assertEquals(result.getNbColisIncidentTG2Jour().intValue(), 1);
		// Evt dispersion avec code RA -> pas une étape de Dispersion
		assertEquals(result.getNbColisDisperses().intValue(), 0);
		assertEquals(result.getNbColisAPreparer().intValue(), 0);
    }
    
    @Test
    /**
     * Entrée : 1 colis exclu hier, aucun evt aujourd'hui
     * 
     * Attendu : le colis, pour aujourd'hui, doit être dans A Remettre en distri et Non Remis en distri
     */
    public void test17_1_ColisNonRemisEnDistriJPlusN() {
    	/* init */
    	idColis = "test17_1";
    	newColisAgenceNDayBefore(idColis, 1);
    	newColisASaisirAgence(idColis, new DateTime());
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|RA|||AGMAN1|99999", MAINTENANT.minusDays(1));
		updateColisSpecifEtapes(idColis, EXCLUSION.getCode() + "|IS|01||AGMAMC|99999", MAINTENANT.minusDays(1));
		
		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();
		
		/* Check */
		assertEquals(result.getNbColisRemisEnDistri().intValue(), 1);
		assertEquals(result.getNbColisNonRemisEnDistribution().intValue(), 1);
    }
    
    @Test
    /**
     * Entrée : 1 colis exclu hier, 1 evt aujourd'hui
     * 
     * Attendu : le colis, pour aujourd'hui, doit être dans A Remettre en distri
     */
    public void test17_2_ColisNonRemisEnDistriJPlusN() {
    	/* init */
    	idColis = "test17_2";
    	newColisAgenceNDayBefore(idColis, 1);
    	newColisASaisirAgence(idColis, new DateTime());
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|RA|||AGMAN1", MAINTENANT.minusDays(1));
		updateColisSpecifEtapes(idColis, EXCLUSION.getCode() + "|IS|01||AGMAMC", MAINTENANT.minusDays(1));
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|IS|||AGMAMC", MAINTENANT);
		
		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();
		
		/* Check */
		assertEquals(result.getNbColisRemisEnDistri().intValue(), 1);
		assertEquals(result.getNbColisNonRemisEnDistribution().intValue(), 0);
    }
    
    @Test
    /**  
     * Tester la fonction de groupBy code dispersion 
     * 
     * Entrée : 1 colis avec le noLt ="test18_1_1"
     * 					Code tournee : null
     * 					Numero contrat : "5445"
     * 					Code postal : "15424"
     * 					Responsabilite : null
     * 					Precocite : "8H"
     * 					PosteSaisie : null
     * 					CodeGrappe : null
     * 					Code dispersion = "62RB4"
     * 					Caracteristique = null
     * 					isPropage = null
     * 
     *            1 colis avec avec le noLt ="test18_1_2"
     * 		      		Code tournee : null
     * 					Numero contrat : "5443"
     * 					Code postal : "15423"
     * 					Responsabilite : "LAPOSTE"
     * 					Precocite : "8H"
     * 					PosteSaisie : "PSMD578"
     * 					CodeGrappe : "T5"
     * 					Code dispersion = 62RB4 
     * 					Caracteristique = null
     * 					isPropage = null
     * 
     * 			  
     *  Attendu : 
     *  		Nombre de colis pour la précocité "8H" et code dispersion "62RB4" est égale à 2
     *  		Taille de la map est égale à 1 avec comme clé le code dispersion "62RB4"
     * */
	public void test18_1_getDispersionActiviteGroupByCodeDispersion() throws Exception {
		
    	/* init */
    	idColis = "test18_1_1";
		newColisAgence(idColis);
		newColisSpecif(idColis, "5445", "15424");
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|62RB4|||99999", MAINTENANT);
		updateColisSpecifService(idColis,MAINTENANT, Sets.newHashSet( ESpecificiteColis.HUIT_HEURE.getCode()));
		
		String idColis2 = "test18_1_2";
		newColisAgence(idColis2);
		newColisSpecif(idColis2, "5443", "15423");
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|P|62RB4||PSMD578|99999", MAINTENANT);
		updateColisSpecifInfoSupp(idColis2,"REMISE_REGATE","AAA");
		updateColisSpecifService(idColis2,MAINTENANT, Sets.newHashSet( ESpecificiteColis.HUIT_HEURE.getCode()));
		
		/* Execute */
		final Map<String, Map<String, Integer>> result = appelMSgetDispersionActiviteGroupByCodeDispersion(ECodeIndicateur.DISPERSES.getCode());
		
		/* Check */
		int nombreColis =  result.get("62RB4").get( ESpecificiteColis.HUIT_HEURE.getCode());
		assertEquals(result.size(),1);
		assertTrue(result.containsKey("62RB4"));
		assertEquals(nombreColis, 2);
	}
    
	@Test
	/**
	 * L’étape DISPERSION n’est pas considérée comme telle pour les événements
	 * IA, IS, RA et RT 6 colis avec etape dispersion, 4 colis avec avec codeEvt
	 * à ne pas considérer en Dispersion 2 colis à considérer en Dispersion :
	 * test19_5 et test19_6
	 */
	public void test19_etapeDispersion() throws Exception {
		/* init */
		idColis = "test19_1";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|IA|62RB4|||99999", MAINTENANT);
	
		String idColis2 = "test19_2";
		newColisAgence(idColis2);
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|IS|62RB4|||99999", MAINTENANT);
	
		String idColis3 = "test19_3";
		newColisAgence(idColis3);
		updateColisSpecifEtapes(idColis3, DISPERSION.getCode() + "|RA|62RB4|||99999", MAINTENANT);
	
		String idColis4 = "test19_4";
		newColisAgence(idColis4);
		updateColisSpecifEtapes(idColis4, DISPERSION.getCode() + "|RT|62RB4|||99999", MAINTENANT);
	
		String idColis5 = "test19_5";
		newColisAgence(idColis5);
		updateColisSpecifEtapes(idColis5, DISPERSION.getCode() + "|ED|62RB4|||99999", MAINTENANT);
	
		String idColis6 = "test19_6";
		newColisAgence(idColis6);
		updateColisSpecifEtapes(idColis6, DISPERSION.getCode() + "|ZS|62RB4|||99999", MAINTENANT);
	
		/* Execute */
		SyntheseDispersionQuantite quantite = appelMSDispersionQuantite();
		SyntheseColisEtListeValeurs resultDisperses = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.DISPERSES.getCode(), null,0);
		SyntheseColisEtListeValeurs resultEnDispersion = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.EN_DISPERSION.getCode(), null,0);
	
		/* Check */
		assertEquals(quantite.getNbColisDisperses().intValue(), 2);
		assertEquals(resultDisperses.getTotalColis(), 2);
		assertEquals(resultDisperses.getListeValeurs().getCodeDispersion().contains("62RB4"), true);
		List<String> noLtList = Arrays.asList(resultDisperses.getColis().get(0).getNoLt(),
				resultDisperses.getColis().get(1).getNoLt());
		assertTrue(noLtList.contains(idColis5));
		assertTrue(noLtList.contains(idColis6));
		assertEquals(resultEnDispersion.getTotalColis(), 2);
		assertEquals(resultEnDispersion.getListeValeurs().getCodeDispersion().contains("62RB4"), true);
		noLtList = Arrays.asList(resultEnDispersion.getColis().get(0).getNoLt(),
				resultEnDispersion.getColis().get(1).getNoLt());
		assertTrue(noLtList.contains(idColis5));
		assertTrue(noLtList.contains(idColis6));
	}

	/*=================================*/
  	/*  Synthèse indicateurs jours précédents */
      /*=================================*/
      @Test
      /**  Entrée : 
       * 			Cas passants :
       * 				1er colis dernière étape DISPERSION  hier ( dans la période et sur l'agence 99999 ) 
       * 				2ème colis délocalisé dernière étape DISPERSION  il y a cinq jours (dans la période et sur l'agence 99999 )
       * 			Cas non passants :
       * 				3ème colis ayant une étape DISPERSION ( dans la période et sur l'agence 99999 )
       * 					et  dern étape LIVRAISON maintenant(après la période et sur l'agence 99999)
       * 				4ème colis dernière étape DISPERSION  hier ( dans la période et sur l'agence 88888 )
       * 				5ème colis dernière étape DISPERSION  maintenant ( après la période et sur l'agence 99999 )
       * 				6ème colis ayant une étape DISPERSION (dans la période et sur l'agence 99999)
       * 					et  dern étape LIVRAISON maintenant(après la période et sur l'agence 88888 )
       * 				7ème colis dernière étape DISPERSION  il y a cinq jours( dans la période et sur l'agence 99999)
       * 					et une étape DISPERSION POSTE (dans la période sur l'agence 99999 )
       * 
       * ==> Calcul de la synthèse sur les 14 jours pour l'agence 99999
       * 
       *  Attendu : NbColisSDSechePassee = 1 (1 er colis)
       *  		    NbColisSDSecheDelocPassee = 1 ( 2 ème colis)
       * */
  	public void test20_1_NbColisSDSechePassee_Quantite() throws Exception {
  		
    	  /* init */
    	// 1 er colis
    	String idColis1 = "colis20_1_1";
    	newColisRestantTg2(idColis1,MAINTENANT.minusDays(4));
  		updateColisSpecifEtapes(idColis1, DISPERSION.getCode()+"|SD|XXXXX|||99999", HIER);
  		// 2 ème colis
  		String idColis2 = "colis20_1_2";
  		newColisRestantTg2(idColis2,MAINTENANT.minusDays(10));
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode()+"|SD|XXUXX|||99999", MAINTENANT.minusDays(5));
		// 3 ème colis
    	String idColis3 = "colis20_1_3";
    	newColisRestantTg2(idColis3,MAINTENANT.minusDays(4));
    	updateColisSpecifEtapes(idColis3, DISPERSION.getCode()+ "|SD||||99999", HIER);
  		updateColisSpecifEtapes(idColis3, LIVRAISON.getCode() +"|D|RBP|||99999", MAINTENANT);
  		// 4 ème colis
    	String idColis4 = "colis20_1_4";
    	newColisRestantTg2(idColis4,MAINTENANT.minusDays(4));
    	updateColisSpecifEtapes(idColis4, DISPERSION.getCode() + "|SD||||88888", HIER);
  		// 5 ème colis
    	String idColis5 = "colis20_1_5";
    	newColisRestantTg2(idColis5,MAINTENANT.minusDays(4));
    	updateColisSpecifEtapes(idColis5, DISPERSION.getCode() +"|SD||||99999", MAINTENANT);
  		// 6 ème colis
    	String idColis6 = "colis20_1_6";
    	newColisRestantTg2(idColis6,MAINTENANT.minusDays(4));
    	updateColisSpecifEtapes(idColis6, DISPERSION.getCode() +"|SD||||99999", HIER);
    	updateColisSpecifEtapes(idColis6, LIVRAISON.getCode() + "|SD||||88888", MAINTENANT);
  		// 7 ème colis
  		String idColis7 = "colis20_1_7";
  		newColisRestantTg2(idColis7,MAINTENANT.minusDays(10));
		updateColisSpecifEtapes(idColis7, DISPERSION.getCode()+"|SD||||99999", MAINTENANT.minusDays(5));
		updateColisSpecifEtapes(idColis7, DISPERSION.getCode() + "|D|RBP|||99999", HIER);
    	
  		/* Execute */
  		SyntheseDispersionQuantitePassee result = appelMSDispersionQuantitePassee(QUATORZEJOURS);

  		/* Check */
		if (!FeatureFlips.INSTANCE.getBoolean("SADelocActif", Boolean.FALSE)) {
			assertIntEquals(2, result.getNbColisSDSechePassee());
		} else {
			assertIntEquals(1, result.getNbColisSDSechePassee());
			assertIntEquals(1, result.getNbColisSDSecheDelocPassee());
		}
  	}
     
      @Test
      /**  Entrée : 
       * 			Cas passants :
       * 				1er colis dernière étape DISPERSION POSTE  hier ( dans la période et sur l'agence 99999 )
       * 				2ème colis dernière étape DISPERSION POSTE il y a cinq jours (dans la période et sur l'agence 99999 )
       * 			Cas non passants :
       * 				3ème colis ayant une étape DISPERSION POSTE ( dans la période et sur l'agence 99999 )
       * 					et  dern étape LIVRAISON maintenant(après la période et sur l'agence 99999)
       * 				4ème colis dernière étape DISPERSION POSTE hier ( dans la période et sur l'agence 88888 )
       * 				5ème colis dernière étape DISPERSION POSTE maintenant ( après la période et sur l'agence 99999 )
       * 				6ème colis ayant une étape DISPERSION POSTE (dans la période et sur l'agence 99999)
       * 					et  dern étape LIVRAISON maintenant(après la période et sur l'agence 88888 )
       * 
       * ==> Calcul de la synthèse sur les 14 jours pour l'agence 99999
       * 
       *  Attendu : NbColisSDSechePostePassee = 2 (1 er colis ,2 ème colis)
       * */
  	public void test20_2_NbColisSDSechePostePassee_Quantite() throws Exception {
  		
    	 /* init */
  		
  		// 1 er colis
    	String idColis1 = "colis20_2_1";
    	newColisRestantTg2(idColis1,MAINTENANT.minusDays(4));
  		updateColisSpecifEtapes(idColis1, DISPERSION.getCode()+"|SD|RBP|||99999", HIER);
  		// 2 ème colis
  		String idColis2 = "colis20_2_2";
  		newColisRestantTg2(idColis2,MAINTENANT.minusDays(10));
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode()+"|SD|RBP|||99999", MAINTENANT.minusDays(5));
		// 3 ème colis
    	String idColis3 = "colis20_2_3";
    	newColisRestantTg2(idColis3,MAINTENANT.minusDays(4));
    	updateColisSpecifEtapes(idColis3, DISPERSION.getCode()+ "|SD|RBP|||99999", HIER);
  		updateColisSpecifEtapes(idColis3, LIVRAISON.getCode()+"|D|RBP|||99999" ,MAINTENANT);
  		// 4 ème colis
    	String idColis4 = "colis20_2_4";
    	newColisRestantTg2(idColis4,MAINTENANT.minusDays(4));
    	updateColisSpecifEtapes(idColis4, DISPERSION.getCode() + "|SD|RBP|||88888", HIER);
  		// 5 ème colis
    	String idColis5 = "colis20_2_5";
    	newColisRestantTg2(idColis5,MAINTENANT.minusDays(4));
    	updateColisSpecifEtapes(idColis5, DISPERSION.getCode() +"|SD|RBP|||99999", MAINTENANT);
  		// 6 ème colis
    	String idColis6 = "colis20_2_6";
    	newColisRestantTg2(idColis6,MAINTENANT.minusDays(4));
    	updateColisSpecifEtapes(idColis6, DISPERSION.getCode() +"|SD|RBP|||99999", HIER);
    	updateColisSpecifEtapes(idColis6, LIVRAISON.getCode() + "|D|RBP|||88888", MAINTENANT);
  		
  		/* Execute */
  		SyntheseDispersionQuantitePassee result = appelMSDispersionQuantitePassee(QUATORZEJOURS);

  		/* Check */
  		assertIntEquals(2, result.getNbColisSDSechePostePassee());
  	}
      
      @Test
      /**  Entrée : 
       * 			Cas passants :
       * 				1er colis dernière étape PERDU  hier ( dans la période et sur l'agence 99999 )
       * 				2ème colis dernière étape PERDU il y a cinq jours (dans la période et sur l'agence 99999 )
       * 			Cas non passants :
       * 				3ème colis ayant une étape PERDU ( dans la période et sur l'agence 99999 )
       * 					et  dern étape LIVRAISON maintenant(après la période et sur l'agence 99999)
       * 				4ème colis dernière étape PERDU hier ( dans la période et sur l'agence 88888 )
       * 				5ème colis dernière étape PERDU maintenant ( après la période et sur l'agence 99999 )
       * 				6ème colis ayant une étape PERDU (dans la période et sur l'agence 99999)
       * 					et  dern étape LIVRAISON maintenant(après la période et sur l'agence 88888 )
       * 
       * ==> Calcul de la synthèse sur les 14 jours pour l'agence 99999
       * 
       *  Attendu : NbColisPerdusPassee = 2 (1 er colis ,2 ème colis)
       * */
  	public void test20_3_NbColisPerdusPassee_Quantite() throws Exception {
  		/* init */
  	
		// 1 er colis
    	String idColis1 = "colis20_3_1";
    	newColisRestantTg2(idColis1,MAINTENANT.minusDays(4));
  		updateColisSpecifEtapes(idColis1, PERDU.getCode()+"|PT||||99999", HIER);
  		// 2 ème colis
  		String idColis2 = "colis20_3_2";
  		newColisRestantTg2(idColis2,MAINTENANT.minusDays(10));
		updateColisSpecifEtapes(idColis2, PERDU.getCode()+"|PT||||99999", MAINTENANT.minusDays(5));
		// 3 ème colis
    	String idColis3 = "colis20_3_3";
    	newColisRestantTg2(idColis3,MAINTENANT.minusDays(4));
    	updateColisSpecifEtapes(idColis3, PERDU.getCode()+ "|PT||||99999", HIER);
  		updateColisSpecifEtapes(idColis3, LIVRAISON.getCode()+"|D||||99999", MAINTENANT);
  		// 4 ème colis
    	String idColis4 = "colis20_3_4";
    	newColisRestantTg2(idColis4,MAINTENANT.minusDays(4));
    	updateColisSpecifEtapes(idColis4, PERDU.getCode() + "|PT||||88888", HIER);
  		// 5 ème colis
    	String idColis5 = "colis20_3_5";
    	newColisRestantTg2(idColis5,MAINTENANT.minusDays(4));
    	updateColisSpecifEtapes(idColis5, PERDU.getCode() +"|PT||||99999", MAINTENANT);
  		// 6 ème colis
    	String idColis6 = "colis20_3_6";
    	newColisRestantTg2(idColis6,MAINTENANT.minusDays(4));
    	updateColisSpecifEtapes(idColis6, PERDU.getCode() +"|PT||||99999", HIER);
    	updateColisSpecifEtapes(idColis6, LIVRAISON.getCode() + "|D||||88888", MAINTENANT);
		
		
  		/* Execute */
  		SyntheseDispersionQuantitePassee result = appelMSDispersionQuantitePassee(QUATORZEJOURS);

  		/* Check */
  		assertIntEquals(2, result.getNbColisPerdusPassee());
  	}
      
      @Test
      /**  Entrée : 
       * 			Cas passants :
       * 				1er colis ayant une dern étape EXCLUSION il y a cinq jours et nombre de jours d'éxclusion égale à 1 ( dans la période et sur l'agence 99999 )
       * 				2ème colis ayant une dern étape EXCLUSION il y a cinq jours et nombre de jours d'éxclusion égale à 2 (dans la période et sur l'agence 88888 )
       * 				3ème colis ayant une dern étape EXCLUSION maintenant (- jours )et nombre de jours d'éxclusion égale à 1( dans la période et sur l'agence 99999 )
       * 			Cas non passants :
       * 				4ème colis dernière étape EXCLUSION hier et nombre de jours d'éxclusion égale à 2 ( dans la période et sur l'agence 88888 )
       * 				5ème colis ayant une étape EXCLUSION ( dans la période et sur l'agence 99999 )
       * 					et  dern étape LIVRAISON maintenant ( après la période et sur l'agence 99999 )
       * 				6ème colis dernière étape EXCLUSION HIER ( dans la période et sur l'agence 99999 )
       * 					et  dern étape LIVRAISON maintenant(après la période et sur l'agence 88888 )
       * 				7ème colis ayant une étape EXCLUSION  et nombre de jours d'éxclusion égale à 2 (dans la période et sur l'agence 99999)
       * 				8ème colis ayant une dern étape EXCLUSION  (MAINTENANT.minusDays(1).plusMinutes(1))
       * 					et nombre de jours d'éxclusion égale à 1( dans la période et sur l'agence 99999 )
       * 
       * ==> Calcul de la synthèse sur les 14 jours pour l'agence 99999
       * 
       *  Attendu : NbColisNonRemisEnDistributionPassee = 3 
       * */
	public void test20_4_NonRemisEnDistributionPassee_Quantite() throws Exception {

		/* init */
    	  
    	// 1 er colis
		String idColis1 = "colis20_4_1";
		newColisRestantTg2(idColis1,MAINTENANT.minusDays(10));
		updateColisSpecifEtapes(idColis1, EXCLUSION.getCode() + "|SD|01|||99999", MAINTENANT.minusDays(5));
		
  		// 2 ème colis
    	String idColis2 = "colis20_4_2";
    	newColisRestantTg2(idColis2,MAINTENANT.minusDays(10));
    	updateColisSpecifEtapes(idColis2, EXCLUSION.getCode() + "|SD|02|||88888", MAINTENANT.minusDays(5));
    	
  		// 3 ème colis
    	String idColis3 = "colis20_4_3";
    	newColisRestantTg2(idColis3,MAINTENANT.minusDays(4));
    	updateColisSpecifEtapes(idColis3, EXCLUSION.getCode() +"|SD|01|||99999", MAINTENANT.minusDays(1));
		
  		// 4 ème colis
    	String idColis4 = "colis20_4_4";
    	newColisRestantTg2(idColis4,MAINTENANT.minusDays(4));
    	updateColisSpecifEtapes(idColis4, EXCLUSION.getCode() + "|SD|02|||88888", HIER);

		// 5 ème colis
    	String idColis5 = "colis20_4_5";
    	newColisRestantTg2(idColis5,MAINTENANT.minusDays(4));
    	updateColisSpecifEtapes(idColis5, EXCLUSION.getCode()+ "|SD||||99999", HIER);
  		updateColisSpecifEtapes(idColis5, LIVRAISON.getCode()+"|D||||99999", MAINTENANT);
    	
  		// 6 ème colis
    	String idColis6 = "colis20_4_6";
    	newColisRestantTg2(idColis6,MAINTENANT.minusDays(4));
    	updateColisSpecifEtapes(idColis6, EXCLUSION.getCode() +"|SD||||99999", HIER);
    	updateColisSpecifEtapes(idColis6, LIVRAISON.getCode() + "|D||||88888", MAINTENANT);
		
    	// 7 ème colis
		String idColis7 = "colis20_4_7";
		newColisRestantTg2(idColis7, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis7, EXCLUSION.getCode() + "|SD|02|||99999", HIER);
    	
  		// 8 ème colis
    	String idColis8 = "colis20_4_8";
    	newColisRestantTg2(idColis8,MAINTENANT.minusDays(4));
    	updateColisSpecifEtapes(idColis8, EXCLUSION.getCode() +"|SD|01|||99999", MAINTENANT.minusDays(1).plusMinutes(1));
    	
		/* Execute */
		SyntheseDispersionQuantitePassee result = appelMSDispersionQuantitePassee(QUATORZEJOURS);

		/* Check */
		assertIntEquals(3, result.getNbColisNonRemisEnDistributionPassee());
	}

  	/*============================================*/
    	/*  Détails indicateurs jours précédents */
    /*============================================*/
    
  @Test
  /**  Entrée : 
   * 			Cas passants :
   * 				1er colis dernière étape DISPERSION  hier ( dans la période et sur l'agence 99999 )
   * 				2ème colis dernière étape DISPERSION  il y a cinq jours (dans la période et sur l'agence 99999 )
   * 			Cas non passants :
   * 				3ème colis ayant une étape DISPERSION ( dans la période et sur l'agence 99999 )
   * 					et  dern étape LIVRAISON maintenant(après la période et sur l'agence 99999)
   * 				4ème colis dernière étape DISPERSION  hier ( dans la période et sur l'agence 88888 )
   * 				5ème colis dernière étape DISPERSION  maintenant ( après la période et sur l'agence 99999 )
   * 				6ème colis ayant une étape DISPERSION (dans la période et sur l'agence 99999)
   * 					et  dern étape LIVRAISON maintenant(après la période et sur l'agence 88888 )
   * 				7ème colis dernière étape DISPERSION  il y a cinq jours( dans la période et sur l'agence 99999)
   * 					et une étape DISPERSION POSTE (dans la période sur l'agence 99999 )
   * 
   * ==> Calcul du détail sur les 14 jours pour l'agence 99999
   * 
   *  Attendu : NbColisSDSechePassee = 2 (1 er colis ,2 ème colis)
   * */
	public void test21_1_SDSechePassee_Detail() throws Exception {
		/* init */
		
    	  
		// 1 er colis
		String idColis1 = "colis21_1_1";
		newColisRestantTg2(idColis1, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis1, DISPERSION.getCode() + "|SD||||99999", HIER);
		// 2 ème colis
		String idColis2 = "colis21_1_2";
		newColisRestantTg2(idColis2, MAINTENANT.minusDays(10));
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|SD||||99999", MAINTENANT.minusDays(5));
		// 3 ème colis
		String idColis3 = "colis21_1_3";
		newColisRestantTg2(idColis3, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis3, DISPERSION.getCode() + "|SD||||99999", HIER);
		updateColisSpecifEtapes(idColis3, LIVRAISON.getCode() + "|D|RBP|||99999", MAINTENANT);
		// 4 ème colis
		String idColis4 = "colis21_1_4";
		newColisRestantTg2(idColis4, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis4, DISPERSION.getCode() + "|SD||||88888", HIER);
		// 5 ème colis
		String idColis5 = "colis21_1_5";
		newColisRestantTg2(idColis5, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis5, DISPERSION.getCode() + "|SD||||99999", MAINTENANT);
		// 6 ème colis
		String idColis6 = "colis21_1_6";
		newColisRestantTg2(idColis6, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis6, DISPERSION.getCode() + "|SD||||99999", HIER);
		updateColisSpecifEtapes(idColis6, LIVRAISON.getCode() + "|SD||||88888", MAINTENANT);
		// 7 ème colis
		String idColis7 = "colis21_1_7";
		newColisRestantTg2(idColis7, MAINTENANT.minusDays(10));
		updateColisSpecifEtapes(idColis7, DISPERSION.getCode() + "|SD||||99999", MAINTENANT.minusDays(5));
		updateColisSpecifEtapes(idColis7, DISPERSION.getCode() + "|D|RBP|||99999", HIER);
		
		
		/* Execute */
		SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.SD_SECHE_PASSEE.getCode(), null, QUATORZEJOURS);

		/* Check */
		assertIntEquals(2, result.getColis().size());
		assertIntEquals(2, result.getTotalColis());
		
		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt(), result.getColis().get(1).getNoLt());

		assertTrue(noLtList.contains(idColis1));
		assertTrue(noLtList.contains(idColis2));
	}
  
  @Test
  /**  Entrée : 
   * 			Cas passants :
   * 				1er colis délocalisé dernière étape DISPERSION  hier ( dans la période et sur l'agence 99999 )
   * 				2ème colis délocalisé dernière étape DISPERSION  il y a cinq jours (dans la période et sur l'agence 99999 )
   * 			Cas non passants :
   * 				3ème colis ayant une étape DISPERSION ( dans la période et sur l'agence 99999 )
   * 					et  dern étape LIVRAISON maintenant(après la période et sur l'agence 99999)
   * 				4ème colis dernière étape DISPERSION  hier ( dans la période et sur l'agence 88888 )
   * 				5ème colis dernière étape DISPERSION  maintenant ( après la période et sur l'agence 99999 )
   * 				6ème colis ayant une étape DISPERSION (dans la période et sur l'agence 99999)
   * 					et  dern étape LIVRAISON maintenant(après la période et sur l'agence 88888 )
   * 				7ème colis dernière étape DISPERSION  il y a cinq jours( dans la période et sur l'agence 99999)
   * 					et une étape DISPERSION POSTE (dans la période sur l'agence 99999 )
   * 
   * ==> Calcul du détail sur les 14 jours pour l'agence 99999
   * 
   *  Attendu : NbColisSDSecheDelocPassee = 2 (1 er colis ,2 ème colis)
   * */
	public void test21_1_SDSecheDelocPassee_Detail() throws Exception {
		/* init */
		
    	  
		// 1 er colis
		String idColis1 = "colis21_5_1";
		newColisRestantTg2(idColis1, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis1, DISPERSION.getCode() + "|SD|YYUYY|||99999", HIER);
		// 2 ème colis
		String idColis2 = "colis21_5_2";
		newColisRestantTg2(idColis2, MAINTENANT.minusDays(10));
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|SD|XXUXX|||99999", MAINTENANT.minusDays(5));
		// 3 ème colis
		String idColis3 = "colis21_5_3";
		newColisRestantTg2(idColis3, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis3, DISPERSION.getCode() + "|SD||||99999", HIER);
		updateColisSpecifEtapes(idColis3, LIVRAISON.getCode() + "|D|RBP|||99999", MAINTENANT);
		// 4 ème colis
		String idColis4 = "colis21_5_4";
		newColisRestantTg2(idColis4, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis4, DISPERSION.getCode() + "|SD||||88888", HIER);
		// 5 ème colis
		String idColis5 = "colis21_5_5";
		newColisRestantTg2(idColis5, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis5, DISPERSION.getCode() + "|SD||||99999", MAINTENANT);
		// 6 ème colis
		String idColis6 = "colis21_5_6";
		newColisRestantTg2(idColis6, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis6, DISPERSION.getCode() + "|SD||||99999", HIER);
		updateColisSpecifEtapes(idColis6, LIVRAISON.getCode() + "|SD||||88888", MAINTENANT);
		// 7 ème colis
		String idColis7 = "colis21_5_7";
		newColisRestantTg2(idColis7, MAINTENANT.minusDays(10));
		updateColisSpecifEtapes(idColis7, DISPERSION.getCode() + "|SD||||99999", MAINTENANT.minusDays(5));
		updateColisSpecifEtapes(idColis7, DISPERSION.getCode() + "|D|RBP|||99999", HIER);
		
		/* Execute */
		SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.SD_SECHE_DELOC_PASSEE.getCode(), null, QUATORZEJOURS);

		/* Check */
		assertIntEquals(2, result.getColis().size());
		assertIntEquals(2, result.getTotalColis());
		
		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt(), result.getColis().get(1).getNoLt());

		assertTrue(noLtList.contains(idColis1));
		assertTrue(noLtList.contains(idColis2));
	}


  @Test
  /**  Entrée : 
   * 			Cas passants :
   * 				1er colis dernière étape DISPERSION POSTE  hier ( dans la période et sur l'agence 99999 )
   * 				2ème colis dernière étape DISPERSION POSTE il y a cinq jours (dans la période et sur l'agence 99999 )
   * 			Cas non passants :
   * 				3ème colis ayant une étape DISPERSION POSTE ( dans la période et sur l'agence 99999 )
   * 					et  dern étape LIVRAISON maintenant(après la période et sur l'agence 99999)
   * 				4ème colis dernière étape DISPERSION POSTE hier ( dans la période et sur l'agence 88888 )
   * 				5ème colis dernière étape DISPERSION POSTE maintenant ( après la période et sur l'agence 99999 )
   * 				6ème colis ayant une étape DISPERSION POSTE (dans la période et sur l'agence 99999)
   * 					et  dern étape LIVRAISON maintenant(après la période et sur l'agence 88888 )
   * 
   * ==> Calcul de la synthèse sur les 14 jours pour l'agence 99999
   * 
   *  Attendu : NbColisSDSechePostePassee = 2 (1 er colis ,2 ème colis)
   * */
	public void test21_2_SDSechePostePassee_Detail() throws Exception {
		/* init */

		// 1 er colis
		String idColis1 = "colis21_2_1";
		newColisRestantTg2(idColis1, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis1, DISPERSION.getCode() + "|SD|RBP|||99999", HIER);
		// 2 ème colis
		String idColis2 = "colis21_2_2";
		newColisRestantTg2(idColis2, MAINTENANT.minusDays(10));
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|SD|RBP|||99999", MAINTENANT.minusDays(5));
		// 3 ème colis
		String idColis3 = "colis21_2_3";
		newColisRestantTg2(idColis3, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis3, DISPERSION.getCode() + "|SD|RBP|||99999", HIER);
		updateColisSpecifEtapes(idColis3, LIVRAISON.getCode() + "|D|RBP|||99999", MAINTENANT);
		// 4 ème colis
		String idColis4 = "colis21_2_4";
		newColisRestantTg2(idColis4, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis4, DISPERSION.getCode() + "|SD|RBP|||88888", HIER);
		// 5 ème colis
		String idColis5 = "colis21_2_5";
		newColisRestantTg2(idColis5, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis5, DISPERSION.getCode() + "|SD|RBP|||99999", MAINTENANT);
		// 6 ème colis
		String idColis6 = "colis21_2_6";
		newColisRestantTg2(idColis6, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis6, DISPERSION.getCode() + "|SD|RBP|||99999", HIER);
		updateColisSpecifEtapes(idColis6, LIVRAISON.getCode() + "|D|RBP|||88888", MAINTENANT);
		
		/* Execute */
		SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.SD_SECHE_POSTE_PASSEE.getCode(), null, QUATORZEJOURS);

		/* Check */
		assertIntEquals(2, result.getColis().size());
		assertIntEquals(2, result.getTotalColis());
		
		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt(), result.getColis().get(1).getNoLt());

		assertTrue(noLtList.contains(idColis1));
		assertTrue(noLtList.contains(idColis2));
	}

  
  @Test
  /**  Entrée : 
   * 			Cas passants :
   * 				1er colis dernière étape PERDU  hier ( dans la période et sur l'agence 99999 )
   * 				2ème colis dernière étape PERDU il y a cinq jours (dans la période et sur l'agence 99999 )
   * 			Cas non passants :
   * 				3ème colis ayant une étape PERDU ( dans la période et sur l'agence 99999 )
   * 					et  dern étape LIVRAISON maintenant(après la période et sur l'agence 99999)
   * 				4ème colis dernière étape PERDU hier ( dans la période et sur l'agence 88888 )
   * 				5ème colis dernière étape PERDU maintenant ( après la période et sur l'agence 99999 )
   * 				6ème colis ayant une étape PERDU (dans la période et sur l'agence 99999)
   * 					et  dern étape LIVRAISON maintenant(après la période et sur l'agence 88888 )
   * 
   * ==> Calcul de la synthèse sur les 14 jours pour l'agence 99999
   * 
   *  Attendu : NbColisPerdusPassee = 2 (1 er colis ,2 ème colis)
   * */
  	public void test21_3_PerduPassee_Detail() throws Exception {
		/* init */

		// 1 er colis
		String idColis1 = "colis21_3_1";
		newColisRestantTg2(idColis1, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis1, PERDU.getCode() + "|PT||||99999", HIER);
		// 2 ème colis
		String idColis2 = "colis21_3_2";
		newColisRestantTg2(idColis2, MAINTENANT.minusDays(10));
		updateColisSpecifEtapes(idColis2, PERDU.getCode() + "|PT||||99999", MAINTENANT.minusDays(5));
		// 3 ème colis
		String idColis3 = "colis21_3_3";
		newColisRestantTg2(idColis3, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis3, PERDU.getCode() + "|PT||||99999", HIER);
		updateColisSpecifEtapes(idColis3, LIVRAISON.getCode() + "|D||||99999", MAINTENANT);
		// 4 ème colis
		String idColis4 = "colis21_3_4";
		newColisRestantTg2(idColis4, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis4, PERDU.getCode() + "|PT||||88888", HIER);
		// 5 ème colis
		String idColis5 = "colis21_3_5";
		newColisRestantTg2(idColis5, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis5, PERDU.getCode() + "|PT||||99999", MAINTENANT);
		// 6 ème colis
		String idColis6 = "colis21_3_6";
		newColisRestantTg2(idColis6, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis6, PERDU.getCode() + "|PT||||99999", HIER);
		updateColisSpecifEtapes(idColis6, LIVRAISON.getCode() + "|D||||88888", MAINTENANT);
		
		
		/* Execute */
		SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.PERDUS_PASSEE.getCode(), null, QUATORZEJOURS);

		/* Check */
		assertIntEquals(2, result.getColis().size());
		assertIntEquals(2, result.getTotalColis());

		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt(), result.getColis().get(1).getNoLt());

		assertTrue(noLtList.contains(idColis1));
		assertTrue(noLtList.contains(idColis2));
    	}
      

  @Test
  /**  Entrée : 
   * 			Cas passants :
   * 				1er colis ayant une dern étape EXCLUSION il y a cinq jours et nombre de jours d'éxclusion égale à 1 ( dans la période et sur l'agence 99999 )
   * 				2ème colis ayant une dern étape EXCLUSION il y a cinq jours et nombre de jours d'éxclusion égale à 2 (dans la période et sur l'agence 88888 )
   * 				3ème colis ayant une dern étape EXCLUSION maintenant (- jours )et nombre de jours d'éxclusion égale à 1( dans la période et sur l'agence 99999 )
   * 			Cas non passants :
   * 				4ème colis dernière étape EXCLUSION hier et nombre de jours d'éxclusion égale à 2 ( dans la période et sur l'agence 88888 )
   * 				5ème colis ayant une étape EXCLUSION ( dans la période et sur l'agence 99999 )
   * 					et  dern étape LIVRAISON maintenant ( après la période et sur l'agence 99999 )
   * 				6ème colis dernière étape EXCLUSION HIER ( dans la période et sur l'agence 99999 )
   * 					et  dern étape LIVRAISON maintenant(après la période et sur l'agence 88888 )
   * 				7ème colis ayant une étape EXCLUSION  et nombre de jours d'éxclusion égale à 2 (dans la période et sur l'agence 99999)
   * 				8ème colis ayant une dern étape EXCLUSION  (MAINTENANT.minusDays(1).plusMinutes(1))
   * 					et nombre de jours d'éxclusion égale à 1( dans la période et sur l'agence 99999 )
   * 
   * ==> Calcul de la synthèse sur les 14 jours pour l'agence 99999
   * 
   *  Attendu : NbColisNonRemisEnDistributionPassee = 3 
   * */
	public void test21_4_NonRemisEnDistributionPassee_Detail() throws Exception {
		/* init */

		// 1 er colis
		String idColis1 = "colis21_4_1";
		newColisRestantTg2(idColis1, MAINTENANT.minusDays(10));
		updateColisSpecifEtapes(idColis1, EXCLUSION.getCode() + "|SD|01|||99999", MAINTENANT.minusDays(5));

		// 2 ème colis
		String idColis2 = "colis21_4_2";
		newColisRestantTg2(idColis2, MAINTENANT.minusDays(10));
		updateColisSpecifEtapes(idColis2, EXCLUSION.getCode() + "|SD|02|||88888", MAINTENANT.minusDays(5));

		// 3 ème colis
		String idColis3 = "colis21_4_3";
		newColisRestantTg2(idColis3, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis3, EXCLUSION.getCode() + "|SD|01|||99999", MAINTENANT.minusDays(1));

		// 4 ème colis
		String idColis4 = "colis21_4_4";
		newColisRestantTg2(idColis4, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis4, EXCLUSION.getCode() + "|SD|02|||88888", HIER);

		// 5 ème colis
		String idColis5 = "colis21_4_5";
		newColisRestantTg2(idColis5, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis5, EXCLUSION.getCode() + "|SD||||99999", HIER);
		updateColisSpecifEtapes(idColis5, LIVRAISON.getCode() + "|D||||99999", MAINTENANT);

		// 6 ème colis
		String idColis6 = "colis21_4_6";
		newColisRestantTg2(idColis6, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis6, EXCLUSION.getCode() + "|SD||||99999", HIER);
		updateColisSpecifEtapes(idColis6, LIVRAISON.getCode() + "|D||||88888", MAINTENANT);

		// 7 ème colis
		String idColis7 = "colis21_4_7";
		newColisRestantTg2(idColis7, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis7, EXCLUSION.getCode() + "|SD|02|||99999", HIER);

		// 8 ème colis
		String idColis8 = "colis21_4_8";
		newColisRestantTg2(idColis8, MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis8, EXCLUSION.getCode() + "|SD|01|||99999",
				MAINTENANT.minusDays(1).plusMinutes(1));

		/* Execute */
		SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.NON_REMIS_EN_DISTRIBUTION_PASSEE.getCode(), null, QUATORZEJOURS);

		/* Check */
		assertIntEquals(3, result.getColis().size());
		assertIntEquals(3, result.getTotalColis());
		
		List<String> noLtList = Arrays.asList(result.getColis().get(0).getNoLt(), result.getColis().get(1).getNoLt(),
				result.getColis().get(2).getNoLt());
		
		assertTrue(noLtList.contains(idColis1));
		assertTrue(noLtList.contains(idColis2));
		assertTrue(noLtList.contains(idColis3));
	}
     
	@Test
	/** Entrée : 1 Specif colis avec le code service "226"
	 *  Attendu : Taille de la liste des codes services égale à 1 et contient le code service "226"
	 *  		  Taille de la liste des colis égale à 1 et son code service égale "226"
	 */
	public void test22_codeService() throws Exception {

		/* init */
		idColis = "colis22_1_1";
		newColisAgence(idColis);
		newColisSpecif(idColis, "5443", "15423");
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|62RB4|||99999", MAINTENANT);
		updateColisService(idColis, MAINTENANT, "226");

		/* Execute */
		SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.DISPERSES.getCode(), null, 0);

		/* Check */
		assertEquals(result.getListeValeurs().getCodeService().size(), 1);
		assertEquals(result.getListeValeurs().getCodeService().contains("226"), true);
		assertEquals(result.getColis().size(), 1);
		assertEquals(result.getColis().get(0).getCodeService(), "226");
	}
    
	@Test
	/** Entrée : 3 colis avec code tournée/dispersion déloc.
	 *  Un prep et dispersé, un pre disp et exclu, un prép 
	 *  Attendu : 2 disp déloc (le 1er et 2éme)
	 *  		  1 sd seche déloc (le 1er)
	 *  		  3 preparé déloc
	 *  		  1 TAsansSD déloc (le 3éme)
	 */
	public void test24_deloc() throws Exception {
		/* init */
		idColis = "colis24_1";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, PREPA_DISTRI.getCode() + "|SD|00U11|||"+AGENCE, MAINTENANT.minusHours(2));
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|00U11|||"+AGENCE, MAINTENANT);

		String idColis2 = "colis24_2";
		newColisAgence(idColis2);
		updateColisSpecifEtapes(idColis2, PREPA_DISTRI.getCode() + "|SD|00U11|||"+AGENCE, MAINTENANT.minusHours(4));
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|SD|00U11|||"+AGENCE, MAINTENANT.minusHours(2));
		updateColisSpecifEtapes(idColis2, EXCLUSION.getCode() + "|SD|01|||"+AGENCE, MAINTENANT);

		String idColis3 = "colis24_3";
		newColisAgence(idColis3);
		updateColisSpecifEtapes(idColis3, PREPA_DISTRI.getCode() + "|SD|00U11|||"+AGENCE, MAINTENANT);

		/* Execute */
		SyntheseColisEtListeValeurs dispersesDeloc = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.DISPERSES_DELOC.getCode(), 5, 0);
		SyntheseColisEtListeValeurs sdSecheDeloc = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.SD_SECHE_DELOC.getCode(), 5, 0);
		SyntheseColisEtListeValeurs preparesDeloc = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.PREPARES_DELOC.getCode(), 5, 0);
		SyntheseColisEtListeValeurs taSansSDDeloc = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.TA_SANS_SD_DELOC.getCode(), 5, 0);

		/* Check */
		assertEquals(dispersesDeloc.getColis().size(), 2);
		List<String> colisDispDeloc = Arrays.asList(dispersesDeloc.getColis().get(0).getNoLt(), dispersesDeloc.getColis().get(1).getNoLt());
		assertTrue(colisDispDeloc.contains(idColis));
		assertTrue(colisDispDeloc.contains(idColis2));
		
		assertEquals(sdSecheDeloc.getColis().size(), 1);
		assertEquals(sdSecheDeloc.getColis().get(0).getNoLt(), idColis);
		
		assertEquals(preparesDeloc.getColis().size(), 3);
		List<String> colisPrepDeloc = Arrays.asList(preparesDeloc.getColis().get(0).getNoLt(),
				preparesDeloc.getColis().get(1).getNoLt(), preparesDeloc.getColis().get(2).getNoLt());
		assertTrue(colisPrepDeloc.contains(idColis));
		assertTrue(colisPrepDeloc.contains(idColis2));
		assertTrue(colisPrepDeloc.contains(idColis3));
		
		assertEquals(taSansSDDeloc.getColis().size(), 1);
		assertEquals(taSansSDDeloc.getColis().get(0).getNoLt(), idColis3);
	}

    
    @Test
	/**
	 * Entrée : 1 colis A Saisir avec une étape EXCLUSION avec la date
	 * d'aujourd'hui et nombre de jours d'éxclusion égale à 0 1 colis avec une
	 * étape EXCLUSION il y a cinq jours et nombre de jours d'éxclusion égale à
	 * 1
	 * 
	 * Attendu : Nombre de colis NonRemisEnDistributionPassee égale 1 Liste des
	 * colis contient le noLt "colis23_1"
	 */
	public void test23_colisPerdu() throws Exception {
		/* init */
		idColis = "colis23_1";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, PERDU.getCode() + "|PT|03|||TLS", MAINTENANT);

		String idColis2 = "colis23_2";
		newColisAgenceNDayBefore(idColis2,10);
		updateColisSpecifEtapes(idColis2, PERDU.getCode() + "|PT|01|||TLS", MAINTENANT.minusHours(2));
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|SD|62RB4|||TLS", MAINTENANT);

		/* Execute */
		SyntheseDispersionQuantite resultQte = appelMSDispersionQuantite();
		SyntheseColisEtListeValeurs resultListe = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.PERDUS.getCode(), 50, 0);

		/* Check */
		assertIntEquals(1, resultQte.getNbColisPerdus());
		assertEquals(resultListe.getColis().size(), 1);
		assertEquals(resultListe.getColis().get(0).getNoLt(), idColis);
	}
      

	
	
    /*=================================*/
    /*  Test 24 : NJoursPrecedent        */
    /*=================================*/
	
	@Test
	/**
	 * Entrée : Un colis avec une étape de dispersion à (Maintenant + 1 minute)
	 * 
	 * Objectif :  Validation du calcul borné sur  l'intervalle  
	 * [Date de début, Date de fin] avec : 
	 *   date de début = Ajourd'hui à minuit 
	 *   date de fin = maintenant
	 *   
	 *  Attendu : NbColisSDSeche = 0
	 *   
	 */
	public void test24_1_JoursPrecedent() throws Exception {
		
		/* init */
		idColis = "colis23_1";
		newColisAgence(idColis);
		updateColisSpecifEtapes(idColis, DISPERSION.getCode() + "|SD|00DIF|||99999", MAINTENANT.plusMinutes(1));

		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantiteJoursPrecedents(fromDate, toDate);

		/* Check */
		assertIntEquals(0, result.getNbColisSDSeche());
	}
	
	@Test
	/**
	 * Entrée : Un colis SD sèche Poste à (Maintenant + 1 minute)
	 * 
	 * Objectif :  Validation du calcul borné sur  l'intervalle  
	 * [Date de début, Date de fin] avec : 
	 *   date de début = Ajourd'hui à minuit 
	 *   date de fin = maintenant
	 *   
	 *  Attendu : NbColisSDSechePoste = 0
	 *   
	 */
	public void test24_2_JoursPrecedent() throws Exception {
		
		/* init */
		String idColis_2 = "colis23_2";
		newColisAgence(idColis_2);
		updateColisSpecifEtapes(idColis_2, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT.plusMinutes(1));
		
		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantiteJoursPrecedents(fromDate, toDate);

		/* Check */
		assertIntEquals(0, result.getNbColisSDSechePoste());
	}
	
	@Test
	/**
	 * Entrée : Un colis avec une étape de PREPARE à (Maintenant + 1 minute)
	 * 
	 * Objectif :  Validation du calcul borné sur  l'intervalle  
	 * [Date de début, Date de fin] avec : 
	 *   date de début = Ajourd'hui à minuit 
	 *   date de fin = maintenant
	 *   
	 *  Attendu : NbColisPrepares = 0
	 *   
	 */
	public void test24_3_JoursPrecedent() throws Exception {
		
		/* init */
		String idColis_3 = "colis23_3";
		newColisAgence(idColis_3);
		updateColisSpecifEtapes(idColis_3, PREPA_DISTRI.getCode() + "|TA|R12|||99999", MAINTENANT.plusMinutes(1));		
		
		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantiteJoursPrecedents(fromDate, toDate);

		/* Check */
		assertIntEquals(0, result.getNbColisPrepares());
	}
	
	@Test
	/**
	 * Entrée : Un colis avec une étape de EXCLUSION à (Maintenant + 1 minute)
	 * 
	 * Objectif :  Validation du calcul borné sur  l'intervalle  
	 * [Date de début, Date de fin] avec : 
	 *   date de début = Ajourd'hui à minuit 
	 *   date de fin = maintenant
	 *   
	 *  Attendu : NbColisExclusLivraisonXJours = 0
	 *   
	 */
	public void test24_4_JoursPrecedent() throws Exception {
		
		/* init */
		String idColis_4 = "colis23_4";
		newColisAgence(idColis_4);
		updateColisSpecifEtapes(idColis_4, DISPERSION.getCode() + "|SD|02DIF|||99999", MAINTENANT.plusMinutes(1));
		updateColisSpecifEtapes(idColis_4, EXCLUSION.getCode() + "|SD|02|||99999", MAINTENANT.plusMinutes(1).plusMillis(1));
		
		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantiteJoursPrecedents(fromDate, toDate);

		/* Check */
		assertIntEquals(0, result.getNbColisExclusLivraisonXJours());
	}
	
	@Test
	/**
	 * Entrée : Un colis avec une étape de A PREPARER à (Maintenant + 1 minute)
	 * 
	 * Objectif :  Validation du calcul borné sur  l'intervalle  
	 * [Date de début, Date de fin] avec : 
	 *   date de début = Ajourd'hui à minuit 
	 *   date de fin = maintenant
	 *   
	 *  Attendu : NbColisAPreparer = 0
	 *   
	 */
	public void test24_5_JoursPrecedent() throws Exception {
		
		/* init */
    	String idColis_5 = "colis23_5";
    	newColisAgence(idColis_5);
    	updateColisSpecifEtapes(idColis_5, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT.plusMinutes(1));
		updateColisSpecifEtapes(idColis_5, PREPA_DISTRI.getCode() + "|SD|01|||99999", MAINTENANT.plusMinutes(1).plusMinutes(1));
		
		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantiteJoursPrecedents(fromDate, toDate);

		/* Check */
		assertIntEquals(0, result.getNbColisAPreparer());
	}
	
	@Test
	/**
	 * Entrée : Un colis dispersé poste et acquitté poste H à (Maintenant + 1 minute)
	 * 
	 * Objectif :  Validation du calcul borné sur  l'intervalle  
	 * [Date de début, Date de fin] avec : 
	 *   date de début = Ajourd'hui à minuit 
	 *   date de fin = maintenant
	 *   
	 *  Attendu : NbColisPrisEnChargePoste = 0
	 *   
	 */
	public void test24_6_JoursPrecedent() throws Exception {
		
		/* init */
    	String idColis_6 = "colis23_6";
    	newColisAgence(idColis_6);
    	updateColisSpecifEtapes(idColis_6, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT.plusMinutes(1));
    	updateColisSpecifEtapes(idColis_6, ACQUITTEMENT_LIVRAISON.getCode() + "|H|LAPOSTE||IFVSIO|99999", MAINTENANT.plusMinutes(1).plusMinutes(1));
    	
		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantiteJoursPrecedents(fromDate, toDate);

		/* Check */
		assertIntEquals(0, result.getNbColisPrisEnChargePoste());
	}
	
	@Test
	/**
	 * Entrée : Un colis dispersé poste et acquitté poste TA à (Maintenant + 1 minute)
	 * 
	 * Objectif :  Validation du calcul borné sur  l'intervalle  
	 * [Date de début, Date de fin] avec : 
	 *   date de début = Ajourd'hui à minuit 
	 *   date de fin = maintenant
	 *   
	 *  Attendu : NbColisDispersesPoste = 0
	 *   
	 */
	public void test24_7_JoursPrecedent() throws Exception {
		
		/* init */
     	String idColis_7 = "colis23_7";
    	newColisAgence(idColis_7);
    	updateColisSpecifEtapes(idColis_7, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT.plusMinutes(1));
    	updateColisSpecifEtapes(idColis_7, ACQUITTEMENT_LIVRAISON.getCode() + "|TA|LAPOSTE||IFVSIO|99999", MAINTENANT.plusMinutes(1).plusMinutes(1));
    	
		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantiteJoursPrecedents(fromDate, toDate);

		/* Check */
		assertIntEquals(0, result.getNbColisDispersesPoste());
	}
	
	@Test
	/**
	 * Entrée : Un colis dispersé poste mais exclu du jour à (Maintenant + 1 minute)
	 * 
	 * Objectif :  Validation du calcul borné sur  l'intervalle  
	 * [Date de début, Date de fin] avec : 
	 *   date de début = Ajourd'hui à minuit 
	 *   date de fin = maintenant
	 *   
	 *  Attendu : NbColisExclusLivraisonUnJour = 0
	 *   
	 */
	public void test24_8_JoursPrecedent() throws Exception {
		
		/* init */
    	String idColis_8 = "colis23_8";
    	newColisAgence(idColis_8);
    	updateColisSpecifEtapes(idColis_8, DISPERSION.getCode() + "|SD|62RBP|||99999", MAINTENANT.plusMinutes(1));
		updateColisSpecifEtapes(idColis_8, EXCLUSION.getCode() + "|SD|01|||99999", MAINTENANT.plusMinutes(1).plusMinutes(1));    	
		
		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantiteJoursPrecedents(fromDate, toDate);

		/* Check */
		assertIntEquals(0, result.getNbColisExclusLivraisonUnJour());
	}
	
	@Test
	/**
	 * Entrée : Un colis incident à (Maintenant + 1 minute)
	 * 
	 * Objectif :  Validation du calcul borné sur  l'intervalle  
	 * [Date de début, Date de fin] avec : 
	 *   date de début = Ajourd'hui à minuit 
	 *   date de fin = maintenant
	 *   
	 *  Attendu : NbColisIncidentTG2Jour = 0
	 *   
	 */
	public void test24_9_JoursPrecedent() throws Exception {
		
		/* init */
		String idColis_9 = "colis23_9";
		newColisAgence(idColis_9);
		updateColisSpecifEtapes(idColis_9, DISPERSION.getCode() + "|ZA|99S99||PSM001|99999", MAINTENANT.minusMinutes(15));
		updateColisSpecifEtapes(idColis_9, INCIDENT.getCode() + "|ZA|99S99||PSM001|99999", MAINTENANT.plusMinutes(1));
		
		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantiteJoursPrecedents(fromDate, toDate);

		/* Check */
	   	assertIntEquals(0,result.getNbColisIncidentTG2Jour());
	}
	
	@Test
	/**
	 * Entrée : Un colis en exclusion à et à une dispersion(Maintenant + 1 minute)
	 * 
	 * Objectif :  Validation du calcul borné sur  l'intervalle  
	 * [Date de début, Date de fin] avec : 
	 *   date de début = Ajourd'hui à minuit 
	 *   date de fin = maintenant
	 *   
	 *  Attendu : NbColisRemisEnDistri = 1
	 *  		  NbColisNonRemisEnDistribution =1
	 *   
	 */
	public void test24_10_JoursPrecedent() throws Exception {
		
		/* init */
		String idColis_10 = "colis23_10";
    	newColisAgenceNDayBefore(idColis_10, 1);
    	newColisASaisirAgence(idColis_10, new DateTime());
		updateColisSpecifEtapes(idColis_10, DISPERSION.getCode() + "|RA|||AGMAN1|99999", MAINTENANT.minusDays(1));
		updateColisSpecifEtapes(idColis_10, EXCLUSION.getCode() + "|IS|01||AGMAMC|99999", MAINTENANT.minusDays(1));
		updateColisSpecifEtapes(idColis_10, DISPERSION.getCode() + "|RA|||AGMAN1|99999", MAINTENANT.plusMinutes(1));
		
		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantiteJoursPrecedents(fromDate, toDate);

		/* Check */
	   	assertIntEquals(1,result.getNbColisRemisEnDistri());
	   	assertIntEquals(1,result.getNbColisNonRemisEnDistribution());
	}
	
	 @Test
     /**  Entrée : 1 colis dernière étape DISPERSION hier
      * 			1 colis avec une étape DISPERSION il y a cinq jours
      * 			1 colis avec une étape DISPERSION il y a quatre jours
      * 	
      * - Le calcul est lancé sur les quatre derniers jours 
      *  
      *  Attendu : 1 seul colis celui qui a une étape de DISPERSION il y a quatre jours
      * */
	public void test25_DateDebutFin() throws Exception {
		/* init */
		String idColis1 = "colis24_1";
		newColisRestantTg2(idColis1,MAINTENANT.minusDays(6));
		updateColisSpecifEtapes(idColis1, DISPERSION.getCode() + "|SD||||99999", MAINTENANT.minusDays(6));

		String idColis2 = "colis24_2";
		newColisRestantTg2(idColis2,MAINTENANT.minusDays(5));
		updateColisSpecifEtapes(idColis2, DISPERSION.getCode() + "|SD||||99999", MAINTENANT.minusDays(5));
		
		String idColis3 = "colis24_3";
		newColisRestantTg2(idColis3,MAINTENANT.minusDays(4));
		updateColisSpecifEtapes(idColis3, DISPERSION.getCode() + "|SD||||99999", MAINTENANT.minusDays(4));

		/* Execute */
		SyntheseColisEtListeValeurs result = appelMSActiviteEtListeValeurAvecIndicateur(
				ECodeIndicateur.SD_SECHE_PASSEE.getCode(), null, 4);

		/* Check */
		assertIntEquals(1, result.getColis().size());
		assertIntEquals(1, result.getTotalColis());
		assertEquals(result.getColis().get(0).getNoLt(), idColis3);
	}
	 
	@Test
	/** 
	 * - Quantité Alertes actives pour les jours précédents
	 * 
	  * Entrée : 	- 1 colis en dispersion avec code dispersion sous forme "xxUxx" 
	  *				et une alerte avec une date postérieure à la date de l'étape de dispersion
      * 			- 1 colis en dispersion avec code dispersion pas sous la forme "xxUxx" 
	  *				et une alerte avec une date postérieure à la date de l'étape de dispersion
	  * 			- 1 colis en dispersion avec code dispersion 
	  *				et une alerte avec une date antérieur à la date de l'étape de dispersion
      *  
      *  Attendu : NbEnAlerteActiveDeloc = 1 (premier colis)
      *  		   NbEnAlerteActive = 1 (deuxième colis)
	 */
	public void test26_1_AlertesActiveQuatite() throws Exception {
		
		/* init */
		String idColis_1 = "colis26_1_1";
		newColisAgence(idColis_1);
		updateColisSpecifEtapes(idColis_1, DISPERSION.getCode() + "|SD|AAUAA|||99999", MAINTENANT);
		attachAlertesToSpecifColis(ONE_ALERTE_JOUR, idColis_1);
		
		String idColis_2 = "colis26_1_2";
		newColisAgence(idColis_2);
		updateColisSpecifEtapes(idColis_2, DISPERSION.getCode() + "|SD|AAAAA|||99999", MAINTENANT);
		attachAlertesToSpecifColis(ONE_ALERTE_JOUR, idColis_2);
    	
		String idColis_3 = "colis26_1_3";
		newColisAgence(idColis_3);
		updateColisSpecifEtapes(idColis_3, DISPERSION.getCode() + "|SD|xxUxx|||99999", MAINTENANT.plusMinutes(15));
		attachAlertesToSpecifColis(ONE_ALERTE_JOUR, idColis_3);
		
		/* Execute */
		SyntheseDispersionQuantite result = appelMSDispersionQuantite();

		/* Check */
    	assertIntEquals(1, result.getNbColisEnAlerteActive());
      	assertIntEquals(1, result.getNbColisEnAlerteActiveDeloc());
	}
	
	@Test
	 /** - Détail indicateur AlertesActive -
	 * 
	  * Entrée : 			
      * 			- 1 colis en dispersion avec code dispersion pas sous la forme "xxUxx" 
	  *				et une alerte avec une date postérieure à la date de l'étape de dispersion
	  * 			- 1 colis en dispersion avec code dispersion 
	  *				et une alerte avec une date antérieur à la date de l'étape de dispersion
      *  
      *  Attendu : Liste des colis contient le premier colis et sa taille égale à 1
	 */
	public void test26_2_AlertesActiveDetail() throws Exception {
		
		/* init */
		String idColis_1 = "colis26_2_1";
		newColisAgence(idColis_1);
		updateColisSpecifEtapes(idColis_1, DISPERSION.getCode() + "|SD|AAAAA|||99999", MAINTENANT);
		attachAlertesToSpecifColis(ONE_ALERTE_JOUR, idColis_1);
    	
		String idColis_2 = "colis26_2_2";
		newColisAgence(idColis_2);
		updateColisSpecifEtapes(idColis_2, DISPERSION.getCode() + "|SD|AAAAA|||99999", MAINTENANT.plusMinutes(15));
		attachAlertesToSpecifColis(ONE_ALERTE_JOUR, idColis_2);
		
		/* Execute */
		List<SyntheseColis> result = appelMSActiviteEtListeValeurAvecIndicateur(ECodeIndicateur.EN_ALERTE_ACTIVE.getCode(), 50, 0).getColis();

		/* Check */
		assertIntEquals(1, result.size());
    	assertEquals(idColis_1, result.get(0).getNoLt());
    	assertEquals(MAINTENANT.withSecondOfMinute(0).withMillisOfSecond(0).plusMinutes(10).toDate(), result.get(0).getDateAlerte());
    	assertEquals(TYPE_RPTSDTA, result.get(0).getTypeAlerte());
	}
	
	@Test
	/**
	 * 	 * - Détail indicateur AlertesActiveDeloc -
	 * Entrée : 		
      * 			- 1 colis en dispersion avec code dispersion sous la forme "xxUxx" 
	  *				et une alerte avec une date postérieure à la date de l'étape de dispersion
	  * 			- 1 colis en dispersion avec code dispersion 
	  *				et une alerte avec une date antérieur à la date de l'étape de dispersion
      *  
      *  Attendu : Liste des colis contient le premier colis et sa taille égale à 1
	 */
	public void test26_3_AlertesActiveDelocDetail() throws Exception {
		
		/* init */
		String idColis_1 = "colis26_3_1";
		newColisAgence(idColis_1);
		updateColisSpecifEtapes(idColis_1, DISPERSION.getCode() + "|SD|xxUxx|||99999", MAINTENANT);
		attachAlertesToSpecifColis(ONE_ALERTE_JOUR, idColis_1);
    	
		String idColis_2 = "colis26_3_2";
		newColisAgence(idColis_2);
		updateColisSpecifEtapes(idColis_2, DISPERSION.getCode() + "|SD|AAAAA|||99999", MAINTENANT.plusMinutes(15));
		attachAlertesToSpecifColis(ONE_ALERTE_JOUR, idColis_2);
		
		/* Execute */
		List<SyntheseColis> result = appelMSActiviteEtListeValeurAvecIndicateur(ECodeIndicateur.EN_ALERTE_ACTIVE_DELOC.getCode(), 50, 0).getColis();

		/* Check */
		assertIntEquals(1, result.size());
    	assertEquals(idColis_1, result.get(0).getNoLt());
    	assertEquals(MAINTENANT.withSecondOfMinute(0).withMillisOfSecond(0).plusMinutes(10).toDate(), result.get(0).getDateAlerte());
    	assertEquals(TYPE_RPTSDTA, result.get(0).getTypeAlerte());
	}
	
	@Test
	/** 
	 * - Quantité Alertes actives pour les jours précédents
	 * 
	  * Entrée : 		- 1 colis en dispersion avec code dispersion sous forme "xxUxx" 
	  *				et une alerte avec une date postérieure à la date de l'étape de dispersion
      * 			- 1 colis en dispersion avec code dispersion pas sous la forme "xxUxx" 
	  *				et une alerte avec une date postérieure à la date de l'étape de dispersion
	  * 			- 1 colis en dispersion avec code dispersion 
	  *				et une alerte avec une date antérieur à la date de l'étape de dispersion
      *  
      *  Attendu : NbEnAlerteActiveDelocPassee = 1 (premier colis)
      *  		   NbEnAlerteActivePassee = 1 (deuxième colis)
	 */
	public void test26_4_AlertesActiveQuatitePassee() throws Exception {
		
		/* init */
		String idColis_1 = "colis26_4_1";
		newColisRestantTg2(idColis_1,MAINTENANT.minusDays(12));
		updateColisSpecifEtapes(idColis_1, DISPERSION.getCode() + "|SD|AAUAA|||99999", MAINTENANT.minusDays(11));
		attachAlertesToSpecifColis(ONE_ALERTE_PASSEE, idColis_1);
		
		String idColis_2 = "colis26_4_2";
		newColisRestantTg2(idColis_2,MAINTENANT.minusDays(12));
		updateColisSpecifEtapes(idColis_2, DISPERSION.getCode() + "|SD|AAAAA|||99999", MAINTENANT.minusDays(11));
		attachAlertesToSpecifColis(ONE_ALERTE_PASSEE, idColis_2);
    	
		String idColis_3 = "colis26_4_3";
		newColisRestantTg2(idColis_3,MAINTENANT.minusDays(10));
		updateColisSpecifEtapes(idColis_3, DISPERSION.getCode() + "|SD|xxUxx|||99999", MAINTENANT.minusDays(9));
		attachAlertesToSpecifColis(ONE_ALERTE_PASSEE, idColis_3);
		
		/* Execute */
		SyntheseDispersionQuantitePassee result = appelMSDispersionQuantitePassee(QUATORZEJOURS);

		/* Check */
    	assertIntEquals(1, result.getNbColisEnAlerteActivePassee());
      	assertIntEquals(1, result.getNbColisEnAlerteActiveDelocPassee());
      	
	}
	
	@Test
	/**
	 * - Détail indicateur AlertesActivePassee -
	 * 
	  * Entrée : 		
      * 			- 1 colis en dispersion avec code dispersion pas sous la forme "xxUxx" 
	  *				et une alerte avec une date postérieure à la date de l'étape de dispersion
	  * 			- 1 colis en dispersion avec code dispersion 
	  *				et une alerte avec une date antérieur à la date de l'étape de dispersion
      *  
      *  Attendu : Liste des colis contient le premier colis et sa taille égale à 1
	 */
	public void test26_5_AlertesActivePasseeDetail() throws Exception {
		
		/* init */
		String idColis_1 = "colis26_5_1";
		newColisRestantTg2(idColis_1,MAINTENANT.minusDays(12));
		updateColisSpecifEtapes(idColis_1, DISPERSION.getCode() + "|SD|AAAAA|||99999", MAINTENANT.minusDays(12));
		attachAlertesToSpecifColis(ONE_ALERTE_PASSEE, idColis_1);
    	
		String idColis_2 = "colis26_5_2";
		newColisRestantTg2(idColis_2,MAINTENANT.minusDays(12));
		updateColisSpecifEtapes(idColis_2, DISPERSION.getCode() + "|SD|AAAAA|||99999", MAINTENANT.minusDays(9));
		attachAlertesToSpecifColis(ONE_ALERTE_PASSEE, idColis_2);
		
		/* Execute */
		List<SyntheseColis> result = appelMSActiviteEtListeValeurAvecIndicateur(ECodeIndicateur.EN_ALERTE_ACTIVE_PASSEE.getCode(), 50, QUATORZEJOURS).getColis();

		/* Check */
		assertIntEquals(1, result.size());
    	assertEquals(idColis_1, result.get(0).getNoLt());
    	assertEquals(MAINTENANT.minusDays(10).plusMinutes(10).withSecondOfMinute(0).withMillisOfSecond(0).toDate(), result.get(0).getDateAlerte());
    	assertEquals(TYPE_RPTSDTA, result.get(0).getTypeAlerte());
	}
	
	@Test
	/**
	 * - Détail indicateur AlertesActiveDelocPassee -
	 * 
	  *	Entrée : 		
      * 			- 1 colis en dispersion avec code dispersion sous la forme "xxUxx" 
	  *				et une alerte avec une date postérieure à la date de l'étape de dispersion
	  * 			- 1 colis en dispersion avec code dispersion 
	  *				et une alerte avec une date antérieur à la date de l'étape de dispersion
      *  
      *  Attendu : Liste des colis contient le premier colis et sa taille égale à 1
	 */
	public void test26_6_AlertesActiveDelocPasseeDetail() throws Exception {
		
		/* init */
		String idColis_1 = "colis26_6_1";
		newColisRestantTg2(idColis_1,MAINTENANT.minusDays(12));
		updateColisSpecifEtapes(idColis_1, DISPERSION.getCode() + "|SD|xxUxx|||99999", MAINTENANT.minusDays(11));
		attachAlertesToSpecifColis(ONE_ALERTE_PASSEE, idColis_1);
    	
		String idColis_2 = "colis26_6_2";
		newColisRestantTg2(idColis_2,MAINTENANT.minusDays(12));
		updateColisSpecifEtapes(idColis_2, DISPERSION.getCode() + "|SD|AAAAA|||99999", MAINTENANT.minusDays(11));
		attachAlertesToSpecifColis(ONE_ALERTE_PASSEE, idColis_2);
		
		/* Execute */
		List<SyntheseColis> result = appelMSActiviteEtListeValeurAvecIndicateur(ECodeIndicateur.EN_ALERTE_ACTIVE_DELOC_PASSEE.getCode(), 50, QUATORZEJOURS).getColis();

		/* Check */
		assertIntEquals(1, result.size());
    	assertEquals(idColis_1, result.get(0).getNoLt());
    	assertEquals(MAINTENANT.minusDays(10).plusMinutes(10).withSecondOfMinute(0).withMillisOfSecond(0).toDate().toString(), result.get(0).getDateAlerte().toString());
    	assertEquals(TYPE_RPTSDTA, result.get(0).getTypeAlerte());
	}
	
	@Test
	/**
	 * Indicateur : nbColisEnCours
	 * 
	 * Entrée : 4 colis ( enDispersion + enDispersionDeloc + nonRemisEnDistribution + perdus)
	 * Attendu : liste des colis en cours contient les quatre colis 
	 *			 (enDispersion + enDispersionDeloc +  nonRemisEnDistribution + perdus)et sa taille égale à 4
	 */
	public void test27_ColisEnCours_Detail() throws Exception{
		
		// Perdus
		String idColis_1 = "colis27_1";
		newColisAgence(idColis_1);
		updateColisSpecifEtapes(idColis_1, PERDU.getCode() + "|PT|03|||TLS", MAINTENANT);
		// EnDispersion
		String idColis_2 = "colis27_2";
		newColisAgence(idColis_2);
		updateColisSpecifEtapes(idColis_2, DISPERSION.getCode() +"|SD|62RBP|||99999", MAINTENANT);
		
		// NonRemisEnDistribution
		String idColis_3 = "colis27_3";
    	newColisASaisirAgence(idColis_3, MAINTENANT);
    	updateColisSpecifEtapes(idColis_3, DISPERSION.getCode() + "|SD|01|||99999", MAINTENANT.minusDays(1));
    	updateColisSpecifEtapes(idColis_3, EXCLUSION.getCode() + "|SD|01|||99999", MAINTENANT.minusDays(1).plusMinutes(2));
		
    	// EnDispersionDeloc
		String idColis_4 = "colis27_4";
		newColisAgence(idColis_4);
		updateColisSpecifEtapes(idColis_4, DISPERSION.getCode() +"|SD|xxUxx|||99999", MAINTENANT);

    	
		/* Execute */
		List<SyntheseColis> result = appelMSActiviteEtListeValeurAvecIndicateur(ECodeIndicateur.EN_COURS.getCode(), 50, 0).getColis();

		/* Check */
		assertIntEquals(4, result.size());
		List<String> colisEnCours= Arrays.asList(result.get(0).getNoLt(),
				result.get(1).getNoLt(), result.get(2).getNoLt(),result.get(3).getNoLt());
    	assertTrue(colisEnCours.contains(idColis_1));
    	assertTrue(colisEnCours.contains(idColis_2));
    	assertTrue(colisEnCours.contains(idColis_3));
    	assertTrue(colisEnCours.contains(idColis_4));
	}
	
	@Test
	/**
	 * Indicateur : nbColisEnCours
	 * 
	 * Entrée : 4 colis ( enDispersion + enDispersionDeloc + nonRemisEnDistribution + perdus)
	 * Attendu : nombre de colis en cours est égale à 4
	 */
	public void test27_ColisEnCours_Quantite() throws Exception{
		
		// Perdus
		String idColis_1 = "colis27_1";
		newColisAgence(idColis_1);
		updateColisSpecifEtapes(idColis_1, PERDU.getCode() + "|PT|03|||TLS", MAINTENANT);
		// EnDispersion
		String idColis_2 = "colis27_2";
		newColisAgence(idColis_2);
		updateColisSpecifEtapes(idColis_2, DISPERSION.getCode() +"|SD|62RBP|||99999", MAINTENANT);
		
		// NonRemisEnDistribution
		String idColis_3 = "colis27_3";
    	newColisASaisirAgence(idColis_3, MAINTENANT);
    	updateColisSpecifEtapes(idColis_3, DISPERSION.getCode() + "|SD|01|||99999", MAINTENANT.minusDays(1));
    	updateColisSpecifEtapes(idColis_3, EXCLUSION.getCode() + "|SD|01|||99999", MAINTENANT.minusDays(1).plusMinutes(2));
		
    	// EnDispersionDeloc
		String idColis_4 = "colis27_4";
		newColisAgence(idColis_4);
		updateColisSpecifEtapes(idColis_4, DISPERSION.getCode() +"|SD|xxUxx|||99999", MAINTENANT);

    	/* Execute */
    	SyntheseDispersionQuantite result = appelMSDispersionQuantite();
		/* Check */
		assertIntEquals(4, result.getNbColisEnCours());
	}
	
	
    /**
     * Ajout du colis dans la liste des colis vu dans l'agence ce jour
     * 
     * @param noLt : identifiant du colis
     *
     * @author LGY
     */
    private void newColisAgence(final String noLt) {
    	final Set<String> setNoLt = Sets.newHashSet(noLt);
		getSession().execute(psInsertColisSaisi.bind(setNoLt, AGENCE, JOUR, HEURE, MINUTE));
    }

    /**
     * Ajout du colis dans la liste des colis vu dans l'agence un jour précédent 
     * 
     * @param noLt : identifiant du colis
     * @param nbJour : nombre de jour avant le jour en cours (1 = hier, 2 avant hier, etc...)
     *
     * @author LGY
     */
    private void newColisAgenceNDayBefore(final String noLt, final int nbJour){
    	final Set<String> setNoLt = Sets.newHashSet(noLt);
		getSession().execute(psInsertColisSaisi.bind(setNoLt, AGENCE, MAINTENANT.minusDays(nbJour).toString("yyyyMMdd"), MAINTENANT.minusDays(nbJour).toString("HH"),
				MAINTENANT.minusDays(nbJour).toString("mm").substring(0, 1)));
    }
    
    /**
     * Ajout du colis dans la liste des colis a saisir dans l'agence ce jour
     * 
     * @param noLt : identifiant du colis
     *
     * @author LGY
     */
    private void newColisASaisirAgence(final String noLt){
    	final Set<String> setNoLt = Sets.newHashSet(noLt);
		getSession().execute(psInsertColisASaisir.bind(setNoLt, AGENCE, JOUR, "00", "0"));
	}
    /**
     * A du colis dans la liste des colis restant tg2
     * @param noLt
     * @param dateColis
     */
    private void newColisRestantTg2(final String noLt,  DateTime dateColis){
       	final Set<String> setNoLt = Sets.newHashSet(noLt);
       getSession().execute(psUpdateColisRestantTg2.bind(setNoLt, AGENCE, dateColis.toString("yyyyMMdd"), "23", "5"));
    	
    }
    
    private void newColisASaisirAgence(final String noLt, DateTime dateColis){
    	final Set<String> setNoLt = Sets.newHashSet(noLt);
		getSession().execute(psInsertColisASaisir.bind(setNoLt, AGENCE, dateColis.toString("yyyyMMdd"), "00", "0"));
	}

    private static Set<String> getOneAlerte(final String heure, final String jour){
    	return Sets.newHashSet((new Gson()).toJson(Arrays.asList(AGENCE,jour,heure,TYPE_RPTSDTA)));
    }
    
	/**
	 *  Ajout de la specif colis 
	 * @param noLt : identifiant colis
	 * @param etape : etape a indiquer
	 * @param dateEtape : date de l'étape
	 * @author LGY
	 */
	private void updateColisSpecifEtapes(String noLt, String etape, DateTime dateEtape) {
		getSession().execute(psUpdateColisSpecifEtapes.bind(dateEtape.toDate(), etape, noLt));
	}

	
	/**
	 * Ajout ou MAJ le champs info_supp du specif colis
	 * @param noLt
	 * @param infoSuppLabel
	 * @param infoSuppValue
	 */
	private void updateColisSpecifInfoSupp(String noLt, String infoSuppLabel, String infoSuppValue) {
		getSession().execute(psUpdateColisSpecifInfoSupp.bind(infoSuppLabel, infoSuppValue, noLt));
	}
	
	/**
	 * Ajout ou MAJ le champs specifs_service du specif colis
	 * @param noLt
	 * @param datePrecocite
	 * @param precocite
	 */
	private void updateColisSpecifService(String noLt, DateTime datePrecocite,Set<String> precocite) {
		getSession().execute(psUpdateColisSpecifService.bind(datePrecocite.toDate(),precocite, noLt));
	}
	
	/**
	 * Ajout ou MAJ le champs specifs_evt du specif colis
	 * @param noLt
	 * @param datePrecocite
	 * @param precocite
	 */
	private void updateColisSpecifEvt(String noLt, DateTime dateSpecifEvt,String SpecifEvt) {
		getSession().execute(psUpdateColisSpecifEvt.bind(dateSpecifEvt.toDate(),SpecifEvt, noLt));
	}
	
	
	/**
	 * Ajout ou MAJ le champs service du specif_colis
	 * @param noLt
	 * @param dateCodeService
	 * @param codeService
	 */
	private void updateColisService(String noLt, DateTime dateCodeService, String codeService) {
		getSession().execute(psUpdateColisService.bind(dateCodeService.toDate(),codeService, noLt));
	}
	
	/**
	 *  Ajout de la specif colis 
	 * @param noLt : identifiant colis
	 * @param no_contrat : numéro  contrant
	 * @param codePostal : code Postal
	 * @author bjbari
	 */
	private void newColisSpecif(String noLt, String no_contrat, String codePostal) {
		getSession().execute(psInsertColisSpecifAll.bind( noLt, no_contrat, codePostal));
	}
    /**
     * Ajouter un Set d'alertes dans la la table colis_specification pour un colis
     * @param alertes
     * @param noLt
     * @author bjbari
     */
	private void attachAlertesToSpecifColis(final Set<String> alertes, final String noLt){
		getSession().executeAsync(psAttachAlerteToSpecifColis.bind( alertes, noLt));
		
	}
	private void cleanAgence() {
		getSession().execute(psTruncateColisAgence.getQueryString());
	}

	private void cleanColisSpecifications() {
		getSession().execute(psTruncateColisSpec.getQueryString());
	}
    
	/**
     * Effectue un appel au microService SyntheseAgence, et retourne la
     * réponse de celui-ci
     * 
     * @return : un objet correspondant à la réponse du MS.
     */
	private SyntheseDispersionQuantite appelMSDispersionQuantite() {

		return appelMSDispersionQuantite(HEURE_APPEL);
    }
	
	/**
	 * Appel MS Synthèse indicateurs du jours
	 * @param dateAppel
	 * @return
	 */
	private SyntheseDispersionQuantite appelMSDispersionQuantite(String dateAppel) {
		final Response response = ClientBuilder.newClient()
				.target("http://localhost:" + getPort()).path("/getSyntheseAgence/Dispersion/Quantite/" + AGENCE + "/dateAppel/" + dateAppel)
				.request().accept(MediaType.APPLICATION_JSON_TYPE).get();

		final SyntheseDispersionQuantite entity = response.readEntity(SyntheseDispersionQuantite.class);
		log.debug("Entity : {}", entity.toString());

		return entity;
    }

	/**
     * Appel MS Synthèse indicateurs des jours précédents	 * @param datedebut
	 * @param dateFin
	 * @return
	 */
	private SyntheseDispersionQuantite appelMSDispersionQuantiteJoursPrecedents(String datedebut, String dateFin) {
		final Response response = ClientBuilder.newClient()
				.target("http://localhost:" + getPort()).path("/getSyntheseAgence/Dispersion/Quantite/JoursPrecedents/" + AGENCE + "/" +datedebut+"/"+dateFin )
				.request().accept(MediaType.APPLICATION_JSON_TYPE).get();

		final SyntheseDispersionQuantite entity = response.readEntity(SyntheseDispersionQuantite.class);
		log.debug("Entity : {}", entity.toString());

		return entity;
    }
	
	/**
	 * Appel MS Synthèse indicateurs jours précédents
	 * @param nbJours
	 * @return synthèse dispersion jours précédents
	 */
	private SyntheseDispersionQuantitePassee appelMSDispersionQuantitePassee(final int nbJours) {
		final Response response = ClientBuilder.newClient()
				.target("http://localhost:" + getPort()).path("/getSyntheseAgence/Dispersion/Quantite/" + AGENCE + "/" + nbJours)
				.request().accept(MediaType.APPLICATION_JSON_TYPE).get();

		final SyntheseDispersionQuantitePassee entity = response.readEntity(SyntheseDispersionQuantitePassee.class);
		log.debug("Entity : {}", entity.toString());

		return entity;
    }
	
	/**
	 * 
	 * @param indicateur
	 * @return
	 */
	private SyntheseColisEtListeValeurs appelMSActiviteEtListeValeurAvecIndicateur(String indicateur, Integer limit, Integer nbJours) {
		WebTarget path = ClientBuilder.newClient().target("http://localhost:" + getPort())
				.path("/getSyntheseAgence/ActiviteEtListeValeur/" + AGENCE + "/" + indicateur +"/dateAppel/"+ HEURE_APPEL+"/"+nbJours);

		if (null != limit) {
			path = path.queryParam("limit", limit);
		}
		
		Response response = path.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get();

		final SyntheseColisEtListeValeurs entity = response.readEntity(new GenericType<SyntheseColisEtListeValeurs>() {
		});
		log.debug("Entity : {}", entity.toString());

		return entity;
	}
	
	/**
	 * Appel du MS DispersionActiviteAvecIndicateurFiltre
	 * @param indicateur
	 * @param limit
	 * @return entity Liste raffinée des colis 
	 */
	private SyntheseColisEtListeValeurs appelMSDispersionActiviteAvecIndicateurEtFiltre(SyntheseListeValeurs criteres, String indicateur, Integer limit) {
		WebTarget path = ClientBuilder.newClient().target("http://localhost:" + getPort())
				.path("/getSyntheseAgence/Activite/" + AGENCE + "/" + indicateur +"/Filtre/dateAppel/" + HEURE_APPEL+"/0");
		if (null != limit) {
			path = path.queryParam("limit", limit);
		}
		
		final Builder request = path.request(MediaType.APPLICATION_JSON_TYPE);
		final Response post = request.post(Entity.entity(criteres, MediaType.APPLICATION_JSON_TYPE));
		
		final SyntheseColisEtListeValeurs entity = post.readEntity(new GenericType<SyntheseColisEtListeValeurs>() {
		});
		log.debug("Entity : {}", entity.toString());

		return entity;
	}
	
	/**
	 * Appel du MS getSyntheseDispersionGroupByCodeDispersion
	 * Renvoie une map qui regroupe les colis par code dispersion et selon les précocité
	 * 
	 * @param posteComptable : le poste comptable de l’agence
	 * @param codeIndicateur : le code de l'indicateur
	 * @param dateAppel : date local d'appel du MS
	 * @return
	 * 
	 * @author bjbari
	 */
	private Map<String, Map<String, Integer>> appelMSgetDispersionActiviteGroupByCodeDispersion(
			final String codeIndicateur) {
		WebTarget path = ClientBuilder.newClient().target("http://localhost:" + getPort())
				.path("/getSyntheseAgence/Dispersion/Activite/" + AGENCE + "/" + codeIndicateur
						+ "/GroupByCodeDispersion/dateAppel/" + HEURE_APPEL+"/0");

		Response response = path.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get();

		final Map<String, Map<String, Integer>> entity = response
				.readEntity(new GenericType<Map<String, Map<String, Integer>>>() {
				});
		log.debug("Entity : {}", entity.toString());

		return entity;
	}

	/**
	 * Surcharge de assertEquals pour éviter l'ambiguité entre assertEquals(Object, Object) et assertEquals(int, int)
	 * @param expected
	 * @param actual
	 */
	private static void assertIntEquals(final int expected, final Integer actual) {
		assertEquals(actual.intValue(), expected);
	}
}
