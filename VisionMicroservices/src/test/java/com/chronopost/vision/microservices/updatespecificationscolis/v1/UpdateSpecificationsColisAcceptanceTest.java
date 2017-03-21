package com.chronopost.vision.microservices.updatespecificationscolis.v1;

import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_BA;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_CL;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_CT;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_DC;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_I;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_RB;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_SC;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_SM;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_ST;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.PROD_NO_LT_BOX;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_ANNUL;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_DATE_CONTRACTUELLE;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_EVT_CF_I_RPR;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_NIMPORTE_QUOI;
import static com.chronopost.vision.model.EInfoComp.CODE_REGATE_EMMETEUR;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.joda.time.DateTime;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.cassandra.table.ETableColisSpecifications;
import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.CacheManagerService;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.CodeService;
import com.chronopost.vision.model.EInfoComp;
import com.chronopost.vision.model.Evenement;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis;
import com.chronopost.vision.model.updatespecificationscolis.v1.EInfoSupplementaire;
import com.chronopost.vision.model.updatespecificationscolis.v1.ESpecificiteColis;
import com.chronopost.vision.model.updatespecificationscolis.v1.EvtEtModifs;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.collect.Sets;

public class UpdateSpecificationsColisAcceptanceTest  extends JerseyTestNg.ContainerPerClassTest {

    private Client client;

    /** Indicateur */
    private boolean suiteLaunch = true;

    /* La map qui simule le cacheEvt*/
    private static HashMap<String,Evenement> mapRefEvenements = new HashMap<>();
    static
    {
    	mapRefEvenements.put("D", new Evenement("D", "88", EEtapesColis.LIVRAISON, 200, "Colis livre a l expediteur", ""));
    	mapRefEvenements.put("I", new Evenement("I", "16", EEtapesColis.RETOUR_AGENCE, 1000, "Envoi differe", ""));
    	mapRefEvenements.put("SM", new Evenement("SM", "120", EEtapesColis.TRANSVERSE, 5000, "Destinataire informe par SMS", ""));
    	mapRefEvenements.put("SD", new Evenement("SD", "7", EEtapesColis.DISPERSION, 1000, "Dispersion", ""));
    	mapRefEvenements.put("TA", new Evenement("TA", "35", EEtapesColis.PREPA_DISTRI, 1000, "Envoi en cours de livraison", ""));
    	mapRefEvenements.put("ZT", new Evenement("ZT", "41", EEtapesColis.DISPERSION, 1000, "Livraison reportee de 24h", ""));
    	mapRefEvenements.put("ZC", new Evenement("ZC", "43", EEtapesColis.DISPERSION, 1000, "Livraison reportee de 24h", ""));
    	mapRefEvenements.put("ZA", new Evenement("ZA", "42", EEtapesColis.DISPERSION, 1000, "Livraison reportee de 24h", ""));
    	mapRefEvenements.put("I2", new Evenement("I2", "126", EEtapesColis.INCIDENT, 1000, "Tri effectue dans l agence de distribution", ""));
    	mapRefEvenements.put("IS", new Evenement("IS", "55", EEtapesColis.DISPERSION, 1000, "Livraison prevue lundi prochain", ""));
    	mapRefEvenements.put("RA", new Evenement("RA", "24", EEtapesColis.DISPERSION, 1000, "Envoi en cours de livraison au point de retrait", ""));
    	mapRefEvenements.put("KC", new Evenement("KC", "21", EEtapesColis.INCIDENT, 1000, "Envoi endommage", ""));
    	mapRefEvenements.put("CT", new Evenement("CT", "91", EEtapesColis.RETOUR_AGENCE, 1000, "Instruction de livraison prise en compte", ""));
    	mapRefEvenements.put("R", new Evenement("R", "20", EEtapesColis.RETOUR_AGENCE, 1000, "Envoi retourne a l expediteur", ""));
    	mapRefEvenements.put("LR", new Evenement("LR", "48", EEtapesColis.INCIDENT, 1000, "Envoi en cours de livraison", ""));
    }
    
    /* La map qui simule le cacheCodeService*/
    private static HashMap<String,CodeService> mapRefCodesServices = new HashMap<>();
    static
    {
		mapRefCodesServices.put("240", new CodeService("240", "", null, Sets.newHashSet("31H", "REP"), ""));
    }
    
    /* Les mock des caches */
    CacheManager<Evenement> cacheEvtMock;
    CacheManager<CodeService> cacheCodeServiceMock;
    
    @Override
    protected Application configure() {
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
        UpdateSpecificationsColisResource resource = new UpdateSpecificationsColisResource().setService(UpdateSpecificationsColisServiceImpl.getInstance());

        forceSet(TestProperties.CONTAINER_PORT, "0");

        ResourceConfig config = new ResourceConfig();
        config.register(resource);

        return config;
    }

    @SuppressWarnings("unchecked")
	@BeforeClass
    public void setUp() throws Exception {
        super.setUp();

        client = ClientBuilder.newClient();

        /* On mock le cacheEvenement */
		cacheEvtMock = Mockito.mock(CacheManager.class);
		Mockito.when(cacheEvtMock.getCache()).thenReturn(mapRefEvenements);
		for (String codeEvt : mapRefEvenements.keySet())
			Mockito.when(cacheEvtMock.getValue(codeEvt)).thenReturn(mapRefEvenements.get(codeEvt));
        
        /* On mock le cacheCodeService */
		cacheCodeServiceMock = Mockito.mock(CacheManager.class);
		Mockito.when(cacheCodeServiceMock.getCache()).thenReturn(mapRefCodesServices);
		for (String soCode : mapRefCodesServices.keySet())
			Mockito.when(cacheCodeServiceMock.getValue(soCode)).thenReturn(mapRefCodesServices.get(soCode));

		UpdateSpecificationsColisServiceImpl.getInstance().setDao(UpdateSpecificationsColisDaoImpl.getInstance())
				.setTranscoderConsignes(UpdateSpecificationsColisTranscoderConsignesImpl.INSTANCE)
				.setRefentielCodeService(cacheCodeServiceMock).setRefentielEvenement(cacheEvtMock);

		nettoyageTests();
    }
    
    @Test
    /**
     * Une étape transverse n'est pas marqué dans la liste des étape du colis
     * 
     * Entree : un evt SM et I sur (dont l'étape est TRANSVERSE) 
     * 
     * Attendu : L'étape dans le specifColis ne doit pas apparaitre
     *
     * @author LGY
     */
    public void test12_etapeTransverse(){
    	/* Initialisation des données en entrée */
    	Date maintenant = new Date();
    	List<EvtEtModifs> evts = new ArrayList<>() ;
    	evts.add(newEvtEtModifs("test12", EVT_SM, maintenant));
    	evts.add(newEvtEtModifs("test12", EVT_I, maintenant));
        
    	/* Appel du MS */
    	callMS(evts);
    	
    	/* Récupération du résultat */
        SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis("test12") ;
        
        /* Vérification de la valeur attendue */
        assertEquals(1, colis.getEtapes().size());
        
    }
    
    @Test
    /**
     * Les créneau de livraison sont mémorisés dans les infoSupp
     * 
     * Entree : un evt TA avec les infoComp creneau
     * 
     * Attendu : Les infoSupp du colis contiennent les 2 infoSupp de créneau
     *
     * @author LGY
     */
    public void test13_infoSupp_Creneau(){
    	/* Initialisation des données en entrée */
    	String noLt = "test13";
    	Date maintenant = new Date();
    	List<EvtEtModifs> evts = new ArrayList<>() ;
    	Map<String, String> infosComp = addInfoComp(EInfoComp.IDBCO_CRENEAU_BORNE_MIN.getCode(), "13H00", null);
    	infosComp = addInfoComp(EInfoComp.IDBCO_CRENEAU_BORNE_MAX.getCode(), "14H00", infosComp);
    	evts.add(newEvtEtModifs(noLt, "DC", maintenant,infosComp));

    	/* Appel du MS */
    	callMS(evts);
    	
    	/* Récupération du résultat */
        SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt) ;
        
        /* Vérification de la valeur attendue */
        assertTrue(colis.getInfoSupp().containsKey(EInfoSupplementaire.CRENEAU_DEBUT_CONTRACTUEL.getCode()));
        assertTrue(colis.getInfoSupp().containsKey(EInfoSupplementaire.CRENEAU_FIN_CONTRACTUEL.getCode()));
    }
    
    @Test
    /**
     * Un événement portant une infocomp 90/CODE REGATE doit avoir son étape créée avec comme
     * code transport LAPOSTE ( etape|xx|LAPOSTE)
     * 
     * Entree : un evt D avec l'infocomp 90 positionné à SITE_REGATE
     * 
     * Attendu : L'étape doit avoir en position 3 le code LAPOSTE
     *
     * @author LGY
     */
	public void test14_etape_LAPOSTE_1() {
		/* Initialisation des données en entrée */
		String noLt = "test14_1";
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		Map<String, String> infosComp = addInfoComp(EInfoComp.CODE_REGATE_EMMETEUR.getCode(), "SITE_REGATE", null);
		evts.add(newEvtEtModifs(noLt, "D", maintenant, infosComp));

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);

		/* Vérification de la valeur attendue */
		String etapeS = colis.getEtapes().get(maintenant);
		String codeTransport = etapeS.split("\\|")[2];
		assertTrue(codeTransport.equals("LAPOSTE"));
	}
    
    @Test
    /**
     * Entree : Un événement dont createurEvt = 'PSFIO'
     * 
     * Attendu : L'étape doit avoir en position 3 le code LAPOSTE
     *
     * @author LGY
     */
	public void test14_etape_LAPOSTE_2() {
		/* Initialisation des données en entrée */
		String noLt = "test14_2";
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		Map<String, String> infosComp = new HashMap<>();
		EvtEtModifs evt = newEvtEtModifs(noLt, "D", maintenant, infosComp);
		evt.getEvt().setCreateurEvt("PSFIO");
		evts.add(evt);

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);

		/* Vérification de la valeur attendue */
		String etapeS = colis.getEtapes().get(maintenant);
		String codeTransport = etapeS.split("\\|")[2];
		assertTrue(codeTransport.equals("LAPOSTE"));
	}
    
	@Test
	/**
	 * Un événement ne portant pas d'infocomp 90 et de createurEvt LAPOSTE, aura
	 * en code transport PICKUP, si son lieuEvt est composé de 4 chiffres et une
	 * lettre pour finir
	 * 
	 * Entree : un evt D avec lieuEvt composé de 4 chiffres et une
	 * lettre pour finir
	 * Attendu : L'étape doit avoir PICKUP comme code transport
	 *
	 * @author XRE
	 */
	public void test14_etape_PICKUP() {
		/* Initialisation des données en entrée */
		String noLt = "test14_3";
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		Map<String, String> infosComp = new HashMap<>();
		EvtEtModifs evt = newEvtEtModifs(noLt, "D", maintenant, infosComp);
		evt.getEvt().setLieuEvt("1234A");
		evts.add(evt);

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);

		/* Vérification de la valeur attendue */
		String etapeS = colis.getEtapes().get(maintenant);
		String codeTransport = etapeS.split("\\|")[2];
		assertEquals("PICKUP", codeTransport);
	}
    
	@Test
	/**
	 * Un événement ne portant pas d'infocomp 90 et de createurEvt LAPOSTE, avec
	 * un lieuEvt incorrect
	 * 
	 * Entree : un evt D avec lieuEvt incorrect
	 * Attendu : L'étape doit avoir 99S99 comme code transport
	 *
	 * @author XRE
	 */
	public void test14_etape_PICKUP_lieuEvtIncorrect() {
		/* Initialisation des données en entrée */
		String noLt = "test14_4";
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		Map<String, String> infosComp = new HashMap<>();
		EvtEtModifs evt = newEvtEtModifs(noLt, "D", maintenant, infosComp);
		evt.getEvt().setLieuEvt("12341");
		evts.add(evt);

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);

		/* Vérification de la valeur attendue */
		String etapeS = colis.getEtapes().get(maintenant);
		String codeTransport = etapeS.split("\\|")[2];
		assertEquals("99S99", codeTransport);
	}
    
    @Test
    /**
     * Un événement ne portant une infocomp 90/CODE REGATE doit avoir son étape créée avec comme
     * code transport la valeur qui est dans ssCodeEvt (ou codeTransport lorsque le champ existera)
     * 
     * Entree : un evt D avec l'infocomp 90 non positionné et ssCodeEvt = 99S99
     * 
     * Attendu : L'étape doit avoir en position 3 le code 99S99 et non LAPOSTE
     *
     * @author LGY
     */
	public void test15_etape_LAPOSTE() {
		/* Initialisation des données en entrée */
		String noLt = "test15_1";
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		evts.add(newEvtEtModifs(noLt, "D", maintenant, null));

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);

		/* Vérification de la valeur attendue */
		String etapeS = colis.getEtapes().get(maintenant);
		String codeTransport = etapeS.split("\\|")[2];
		assertFalse(codeTransport.equals("LAPOSTE"));
		assertTrue(codeTransport.equals("99S99"));
	}
    
    @Test
	public void test16_etape_EXCLUSION() {
		/* Initialisation des données en entrée */
		String noLt = "test16";
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		evts.add(newEvtEtModifs(noLt, "SD", maintenant, null));
		Evt evt = evts.get(0).getEvt();
		evt.setSsCodeEvt("05DIF");
		evts.add(newEvtEtModifs("test16_2", "IS", maintenant, null));

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);
		SpecifsColis colis2 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("test16_2");

		/* Vérification de la valeur attendue */
		assertTrue(colis.getEtapes().size() == 2);
		assertTrue("L'étape EXCLUSION doit apparaitre dans les étapes du colis",
				EEtapesColis.EXCLUSION.isIncludeIn(colis.getEtapes().values()));
		assertTrue("L'étape DISPERSION doit apparaitre dans les étapes du colis",
				EEtapesColis.DISPERSION.isIncludeIn(colis.getEtapes().values()));
		assertTrue(colis2.getEtapes().size() == 2);
		assertTrue("L'étape EXCLUSION doit apparaitre dans les étapes du colis",
				EEtapesColis.EXCLUSION.isIncludeIn(colis2.getEtapes().values()));
		assertTrue("L'étape DISPERSION doit apparaitre dans les étapes du colis",
				EEtapesColis.DISPERSION.isIncludeIn(colis2.getEtapes().values()));
	}
    
    @Test
    /**
     * Un événement TA portant une infocomp 90/CODE REGATE doit avoir une étape ACKLIV|TA|LAPOSTE
     * 
     * Entree : un evt TA avec l'infocomp 90 positionnée 
     * 
     * Attendu : Une étape ACKLIV|TA|LAPOSTE doit être présente dans les étape du colis
     *
     * @author LGY
     */
    public void test17_etape_ACKLIV(){
    	/* Initialisation des données en entrée */
    	String noLt = "test17";
    	Date maintenant = new Date();
    	List<EvtEtModifs> evts = new ArrayList<>() ;
    	evts.add(newEvtEtModifs(noLt, "TA", maintenant,addInfoComp(EInfoComp.CODE_REGATE_EMMETEUR.getCode(), "SITE_REGATE", null)));

    	/* Appel du MS */
    	callMS(evts);
    	
    	/* Récupération du résultat */
        SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);
        
        /* Vérification de la valeur attendue */
        assertTrue(colis.getEtapes().size() >= 1);
        assertTrue("L'étape ACKLIV doit apparaitre dans les étapes du colis",EEtapesColis.ACQUITTEMENT_LIVRAISON.isIncludeIn(colis.getEtapes().values()));
    }
    
    @Test
    /**
     * Un événement TA portant une infocomp 104/PROPAGE doit avoir une étape contenant le flag |P|
     * 
     * Entree : un evt TA avec l'infocomp 104 positionnée 
     * 
     * Attendu : Une étape PREPA_DISTRI|TA|xxx|P|xxx
     *
     * @author LGY
     */
    public void test18_etape_Propage(){
    	/* Initialisation des données en entrée */
    	String noLt = "test18";
    	Date maintenant = new Date();
    	List<EvtEtModifs> evts = new ArrayList<>() ;
    	evts.add(newEvtEtModifs(noLt, "TA", maintenant,addInfoComp(EInfoComp.ID_CONTENANT_PROPAG.getCode(), "CONTENANT", null)));

    	/* Appel du MS */
    	callMS(evts);
    	
    	/* Récupération du résultat */
        SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);
        
        /* Vérification de la valeur attendue */
        for(String etape: colis.getEtapes().values())
        	System.out.println(etape);
        assertTrue(colis.getEtapes().size() == 1);
        assertTrue(colis.getEtapes().values().iterator().next().contains("|P|"));
    }
    
    @Test
    /**
     * Si le createur_evt débute par PROPAG_ il s'agit d'un préfixe que l'on ne doit pas
     * retrouvé dans le champ 5 de l'étape 
     * 
     * Entree : un evt TA avec creat_evt = PROPA_PSM01 
     * 
     * Attendu : Une étape PREPA_DISTRI|TA|xxx|P|PSM01
     *
     * @author LGY
     */
    public void test19_etape_Propage(){
    	/* Initialisation des données en entrée */
    	String noLt = "test19";
    	Date maintenant = new Date();
    	List<EvtEtModifs> evts = new ArrayList<>() ;
    	evts.add(newEvtEtModifs(noLt, "TA", maintenant,addInfoComp(EInfoComp.ID_CONTENANT_PROPAG.getCode(), "CONTENANT", null)));
    	evts.get(0).getEvt().setCreateurEvt("PROPA_PSM01");

    	/* Appel du MS */
    	callMS(evts);
    	
    	/* Récupération du résultat */
        SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);
        
        /* Vérification de la valeur attendue */
        for(String etape: colis.getEtapes().values())
        	System.out.println(etape);
        assertTrue(colis.getEtapes().size() > 0);
        assertTrue(colis.getEtapes().values().iterator().next().contains("|PSM01"));
    }
    
    @Test
    /**
     * Entree : un evt SC avec infocomp code sac non vide 
     * 
     * Attendu : le colis doit avoir la specificite <sensible>
     *
     * @author LGY
     */
    public void test1_sensible(){
    	/* Initialisation des données en entrée */
    	Date maintenant = new Date();
    	List<EvtEtModifs> evts = new ArrayList<>() ;
    	evts.add(newEvtEtModifs( "test1", EVT_SC, maintenant
    			               , addInfoComp(EInfoComp.CODE_SAC.getCode(), VALUE_NIMPORTE_QUOI,null)));
        
    	/* Appel du MS */
    	callMS(evts);
    	
    	/* Récupération du résultat */
        SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis("test1") ;
        
        /* Vérification de la valeur attendue */
        assertEquals(ESpecificiteColis.SENSIBLE.getCode(), colis.getSpecifsEvt().get(maintenant)) ;
    }
    
    @Test
    /**
     * Entree : Evénement CL avec un code infoscomp 175 ANNUL et infoscomp 176 une positionnée
     * 
     * Attendu : Une consigne annulée renseignée avec le contenu
     *
     * @author LGY
     */
    public void test2_consigneAnnulee(){
    	/* Initialisation des données en entrée */
    	Date maintenant = new Date();
    	List<EvtEtModifs> evts = new ArrayList<>() ;
    	evts.add( newEvtEtModifs( "test2", EVT_CL, maintenant
    			, addInfoComp(EInfoComp.CONSIGNE_EVT_CL.getCode(), VALUE_ANNUL
				, addInfoComp(EInfoComp.ID_CONSIGNE.getCode(), VALUE_NIMPORTE_QUOI,null))));
        
    	/* Appel du MS */
    	callMS(evts);
    	
    	/* Récupération du résultat */
        SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis("test2") ;
        
        /* Vérification de la valeur attendue */
        assertEquals(VALUE_NIMPORTE_QUOI, colis.getConsignesAnnulees().get(maintenant)) ;
    }
    
    @Test
    /**
     * Entree : Evénement I avec un code infocomp 56 renseigné avec "EVT_CF_I_RPR"
     * 
     * Attendu : Spécificité Evenement CONSIGNE
     *           consigne Traitée 0|valeurConsigne
     *           étape Retour Agence
     *
     * @author LGY
     */
    public void test3_consigneAnnulee(){
    	/* Initialisation des données en entrée */
    	Date maintenant = new Date();
    	List<EvtEtModifs> evts = new ArrayList<>() ;
    	EvtEtModifs eEM = newEvtEtModifs( "test3", EVT_I, maintenant
    			, addInfoComp(EInfoComp.CONSIGNE_EVT_I.getCode(), VALUE_EVT_CF_I_RPR,null));
    	eEM.getEvt().setSsCodeEvt("RPR");
    	eEM.getEvt().setLieuEvt("99999");
    	evts.add(eEM);
        
    	/* Appel du MS */
    	callMS(evts);
    	
    	/* Récupération du résultat */
        SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis("test3") ;
        
        /* Vérification de la valeur attendue */
        assertEquals("0|RemisePointRelais", colis.getConsignesTraitees().get(maintenant)) ;
        assertEquals(ESpecificiteColis.CONSIGNE.getCode(), colis.getSpecifsEvt().get(maintenant)) ;        
        assertEquals(EEtapesColis.RETOUR_AGENCE.getCode() + "|" + EVT_I + "|RPR||PSM001|99999", colis.getEtapes().get(maintenant)) ;
    }
    
    @Test
    public void test11() {
        /* initialisation des variables à fournir au service */
        List<EvtEtModifs> evts = new ArrayList<>() ;
        // Cas 4
        // Evénement CT avec code Service 240 et infocomp 185 valorisée à MDBP_HZCA 
        // code service 240 => spécificité Service REP
        // CT => Specificité événement CONSIGNE
        // CT et infocomp 85 à MDBP_HZCA => Spécificité consigne traitée RemiseBureau
        Evt evt4 = new Evt() ;
        evt4.setNoLt("test4") ;
        evt4.setDateEvt(new DateTime(2015,9,25,17,50,0).toDate()) ;
        evt4.setCodeEvt(EVT_CT) ;
        evt4.setCodeService("240") ;
        evt4.setInfoscomp(new HashMap<String, String>()) ;
        evt4.getInfoscomp().put(EInfoComp.ACTION_CONTENANT.getCode(),"MDBP_HZCA") ;
        EvtEtModifs evtmodif4 = new EvtEtModifs() ;
        evtmodif4.setEvt(evt4);
        evts.add(evtmodif4) ;

        // Cas 5
        // Evénement ST avec infocomp CLIENT_EN_COMPTE renseignée
        // => cas forcément sans incidence sur les spécificités CONSIGNE, Evénements et service
        // Une Date contractuelle dans l'objet modification
        // => C'est donc la mise à jour de la colonne date_contractuelle qui est testée ici
        Evt evt5 = new Evt() ;
        evt5.setNoLt("test5") ;
        evt5.setDateEvt(new DateTime(2015,9,25,17,59,0).toDate()) ;
        evt5.setCodeEvt(EVT_ST) ;
        evt5.setCodeService(VALUE_NIMPORTE_QUOI) ;
        evt5.setInfoscomp(new HashMap<String, String>()) ;
        evt5.getInfoscomp().put(EInfoComp.TAXE_NO_CONTRAT.getCode(),VALUE_NIMPORTE_QUOI) ;
        EvtEtModifs evtmodif5 = new EvtEtModifs() ;
        evtmodif5.setEvt(evt5);
        evtmodif5.setModifications(new HashMap<String, String>());
        evtmodif5.getModifications().put(VALUE_DATE_CONTRACTUELLE, "15/08/2016 23:00");
        evts.add(evtmodif5) ;

        // Cas 6
        // Evénement ST avec infocomp CLIENT_EN_COMPTE renseignée
        // Pas de date contractuelle
        // => cas forcément sans incidence sur les spécificités CONSIGNE, Evénements et service
        // => événement sans spécification colis donc non trouvable dans la table  
        Evt evt6 = new Evt() ;
        evt6.setNoLt("test6") ;
        evt6.setDateEvt(new DateTime(2015,9,25,18,00,0).toDate()) ;
        evt6.setCodeEvt(EVT_ST) ;
        evt6.setCodeService(VALUE_NIMPORTE_QUOI) ;
        evt6.setInfoscomp(new HashMap<String, String>()) ;
        evt6.getInfoscomp().put(EInfoComp.TAXE_NO_CONTRAT.getCode(),VALUE_NIMPORTE_QUOI) ;
        EvtEtModifs evtmodif6 = new EvtEtModifs() ;
        evtmodif6.setEvt(evt6);
        evts.add(evtmodif6) ;

        // Cas 7
        // Evénement DC avec infocomp NO_LT_RETOUR renseignée
        // Un commentaire associé
        // => cas forcément sans incidence sur les spécificités CONSIGNE, Evénements et service
        // => une entrée en colonne info_supp de la table  
        Evt evt7 = new Evt() ;
        evt7.setNoLt("test7") ;
        evt7.setDateEvt(new DateTime(2015,9,25,18,10,0).toDate()) ;
        evt7.setCodeEvt(EVT_DC) ;
        evt7.setCodeService(VALUE_NIMPORTE_QUOI) ;
        evt7.setInfoscomp(new HashMap<String, String>()) ;
        evt7.getInfoscomp().put(EInfoComp.SWAP_NO_LT_RETOUR.getCode(),VALUE_NIMPORTE_QUOI) ;
        EvtEtModifs evtmodif7 = new EvtEtModifs() ;
        evtmodif7.setEvt(evt7);
        evts.add(evtmodif7) ;
        
        // Cas 8
        // RG-MSUpdSpecColis-06
        // Entrée: Evénement BA avec infocomp 105 non vide
        // Sortie: le colis a la  spécificité SAC avec la date 20000101
        Evt evt8 = new Evt() ;
        evt8.setNoLt("test8") ;
        evt8.setDateEvt(new DateTime(1900,9,25,0,0,0).toDate()) ;
        evt8.setCodeEvt(EVT_BA);
        evt8.setCodeService(VALUE_NIMPORTE_QUOI) ;
        evt8.setInfoscomp(new HashMap<String, String>()) ;
        evt8.getInfoscomp().put(EInfoComp.ID_CONTENU.getCode(),VALUE_NIMPORTE_QUOI) ;
        EvtEtModifs evtmodif8 = new EvtEtModifs() ;
        evtmodif8.setEvt(evt8);
        evts.add(evtmodif8);
        
        // Cas 9
        // RG-MSUpdSpecColis-07
        // Entrée: Evénement avec le produceur prod_no_lt = 20
        // Sortie: le colis a la  spécificité BOX avec la date 20000101
        Evt evt9 = new Evt() ;
        evt9.setNoLt("test9") ;
        evt9.setProdNoLt(PROD_NO_LT_BOX);
        evt9.setCodeEvt(EVT_BA);
        evt9.setDateEvt(new DateTime(1900,9,21,0,0,0).toDate()) ;
        evt9.setCodeService(VALUE_NIMPORTE_QUOI) ;
        EvtEtModifs evtmodif9 = new EvtEtModifs() ;
        evtmodif9.setEvt(evt9);
        evts.add(evtmodif9);
        
        // Cas 10
        // RG-MSUpdSpecColis-63
        // Entrée: Evénement RB avec infocomp 10 non vide
        // Sortie: infoSupp(“DEPOT_RELAIS”) = infocomp 10
        Evt evt10 = new Evt() ;
        evt10.setNoLt("test10") ;
        evt10.setCodeEvt(EVT_RB);
        evt10.setDateEvt(new DateTime(1900,9,21,0,0,0).toDate()) ;
        evt10.setCodeService(VALUE_NIMPORTE_QUOI) ;
        evt10.setInfoscomp(new HashMap<String, String>()) ;
        evt10.getInfoscomp().put(EInfoComp.IDENTIFIANT_POINT_RELAIS.getCode(),VALUE_NIMPORTE_QUOI) ;
        EvtEtModifs evtmodif10 = new EvtEtModifs() ;
        evtmodif10.setEvt(evt10);
        evts.add(evtmodif10);
        
        // Cas 11
        // RG-MSUpdSpecColis-64
        // Entrée: Evénement avec infocomp 90 non vide
        // Sortie: infoSupp(“REMISE_REGATE”) = infocomp 90
        Evt evt11 = new Evt() ;
        evt11.setNoLt("test11") ;
        evt11.setCodeEvt(EVT_RB);
        evt11.setDateEvt(new DateTime(1900,9,21,0,0,0).toDate()) ;
        evt11.setCodeService(VALUE_NIMPORTE_QUOI) ;
        evt11.setInfoscomp(new HashMap<String, String>()) ;
        evt11.getInfoscomp().put(EInfoComp.CODE_REGATE_EMMETEUR.getCode(),VALUE_NIMPORTE_QUOI) ;
        EvtEtModifs evtmodif11 = new EvtEtModifs() ;
        evtmodif11.setEvt(evt11);
        evts.add(evtmodif11);
        
        /* Execution de l'appel */
        Entity<List<EvtEtModifs>> inputEntity = Entity.entity(evts,MediaType.APPLICATION_JSON);
        client.target("http://localhost:" + getPort()).path("/UpdateSpecificationsColis/v1").request().accept(MediaType.APPLICATION_JSON_TYPE).post(inputEntity);
        SpecifsColis colis4 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("test4") ;
        SpecifsColis colis5 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("test5") ;
        SpecifsColis colis7 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("test7") ;
        SpecifsColis colis8 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("test8") ;
        SpecifsColis colis9 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("test9") ;
        SpecifsColis colis10 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("test10") ;
        SpecifsColis colis11 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("test11") ;
        /* Test de l'appel */
        // Cas 4
        // Evénement CT avec code Service 240 et infocomp 185 valorisée à MDBP_HZCA 
        // code service 240 => spécificité Service REP
        // CT => Specificité événement CONSIGNE
        // CT et infocomp 85 à MDBP_HZCA => Spécificité consigne traitée RemiseBureau
        assertTrue(colis4.getConsignesAnnulees().isEmpty()) ;
        assertTrue(colis4.getConsignesRecues().isEmpty()) ;
        assertEquals("null|RemiseBureau", colis4.getConsignesTraitees().get(new DateTime(2015,9,25,17,50,0).toDate())) ;
        assertEquals(ESpecificiteColis.CONSIGNE.getCode(), colis4.getSpecifsEvt().get(new DateTime(2015,9,25,17,50,0).toDate())) ;
        assertTrue(colis4.getSpecifsService().get(new DateTime(2015,9,25,17,50,0).toDate()).contains(ESpecificiteColis.REP.getCode())) ;
        assertTrue(colis4.getEtapes().size() > 0);
        assertTrue(colis4.getEtapes().values().iterator().next().contains(EEtapesColis.RETOUR_AGENCE.getCode()));
        assertTrue(colis4.getDatesContractuelles().isEmpty()) ;

        // Cas 5
        // Evénement ST avec infocomp CLIENT_EN_COMPTE renseignée
        // => cas forcément sans incidence sur les spécificités CONSIGNE, Evénements et service
        // Une Date contractuelle dans l'objet modification
        // => C'est donc la mise à jour de la colonne date_contractuelle qui est testée ici
        assertTrue(colis5.getConsignesAnnulees().isEmpty()) ;
        assertTrue(colis5.getConsignesRecues().isEmpty()) ;
        assertTrue(colis5.getConsignesTraitees().isEmpty()) ;
        assertTrue(colis5.getSpecifsEvt().isEmpty()) ;
        assertTrue(colis5.getSpecifsService().isEmpty()) ;
        assertTrue(colis5.getEtapes().isEmpty()) ;
        assertEquals( new DateTime(2016,8,15,23,0,0).toDate(),colis5.getDatesContractuelles().get(new DateTime(2015,9,25,17,59,0).toDate())) ;

        // Cas 6
        // Evénement ST avec infocomp CLIENT_EN_COMPTE renseignée
        // Pas de date contractuelle
        // => cas forcément sans incidence sur les spécificités CONSIGNE, Evénements et service
        // => événement sans spécification colis donc non trouvable dans la table  
        assertNull(UpdateSpecificationsColisDaoUtils.getRowSpecificationColis("test6")) ;

        // Cas 7
        // Evénement DC avec infocomp NO_LT_RETOUR renseignée
        // Un commentaire associé
        // => cas forcément sans incidence sur les spécificités CONSIGNE, Evénements et service
        // => une entrée en colonne info_supp de la table  
        assertTrue(colis7.getConsignesAnnulees().isEmpty()) ;
        assertTrue(colis7.getConsignesRecues().isEmpty()) ;
        assertTrue(colis7.getConsignesTraitees().isEmpty()) ;
        assertTrue(colis7.getSpecifsEvt().isEmpty()) ;
        assertTrue(colis7.getSpecifsService().isEmpty()) ;
        assertTrue(colis7.getEtapes().isEmpty()) ;
        assertTrue(colis7.getDatesContractuelles().isEmpty()) ;
        assertEquals(VALUE_NIMPORTE_QUOI,colis7.getInfoSupp().get(EInfoSupplementaire.NO_LT_RETOUR.getCode())) ;

        // Cas 8
        // RG-MSUpdSpecColis-06
        // Entrée: Evénement BA avec infocomp 105 non vide
        // Sortie: le colis a la  spécificité SAC avec la date 20000101
        assertTrue(!colis8.getSpecifsEvt().isEmpty());
        assertEquals(colis8.getSpecifsEvt().get((new DateTime(2000, 1, 1, 0, 0, 0)).toDate()),ESpecificiteColis.SAC.getCode());
        
        // Cas 9
        // RG-MSUpdSpecColis-07
        // Entrée: Evénement avec le produceur prod_no_lt = 20
        // Sortie: le colis a la  spécificité BOX avec la date 20000101
        assertTrue(!colis9.getSpecifsEvt().isEmpty());
        assertEquals(colis9.getSpecifsEvt().get((new DateTime(2000, 1, 1, 0, 0, 0)).toDate()),ESpecificiteColis.BOX.getCode());
        
        
        // Cas 10
        // RG-MSUpdSpecColis-63
        // Entrée: Evénement RB avec infocomp 10 non vide
        // Sortie: infoSupp(“DEPOT_RELAIS”) = infocomp 10
        assertEquals(VALUE_NIMPORTE_QUOI,colis10.getInfoSupp().get(EInfoSupplementaire.DEPOT_RELAIS.getCode())) ;
        
        
        // Cas 11
        // RG-MSUpdSpecColis-64
        // Entrée: Evénement avec infocomp 90 non vide
        // Sortie: infoSupp(“REMISE_REGATE”) = infocomp 90
        assertEquals(VALUE_NIMPORTE_QUOI,colis11.getInfoSupp().get(EInfoSupplementaire.REMISE_REGATE.getCode())) ;
    }
    
    /**
     * Un evt avec étape d'acquittement la poste avec exclusion 5 jours
     * Vérifie que l'étape est considérée comme dispersée à la poste, et non exclue de notre côté 
     */
	@Test
	public void test20_etape_EXCLUSION_la_poste() {
		/* Initialisation des données en entrée */
		String noLt = "test20";
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		evts.add(newEvtEtModifs(noLt, "SD", maintenant, null));
		Evt evt = evts.get(0).getEvt();
		evt.setSsCodeEvt("05DIF"); // code transport
		evt.setInfoscomp(addInfoComp(CODE_REGATE_EMMETEUR.getCode(), "SITE_REGATE", null));

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);

		/* Vérification de la valeur attendue */
		assertTrue(colis.getEtapes().size() == 2);
		// l'étape d'exclusion n'apparait pas dans les étapes du colis car poste
		assertTrue("L'étape EXCLUSION ne doit pas apparaitre dans les étapes du colis", !EEtapesColis.EXCLUSION.isIncludeIn(colis.getEtapes().values()));
		assertTrue("L'étape ACQUITTEMENT doit apparaitre dans les étapes du colis", EEtapesColis.ACQUITTEMENT_LIVRAISON.isIncludeIn(colis.getEtapes().values()));
		assertTrue("L'étape DISPERSION doit apparaitre dans les étapes du colis", EEtapesColis.DISPERSION.isIncludeIn(colis.getEtapes().values()));
	}
	
	@Test
	/**
	 * Entree: 1 evt SD avec code tournee
	 * 
	 * Sortie: 1 etape DISPERSION dans les étapes du colis
	 * @author LGY
	 */
	public void test21_etape_DISPERSION() {
		/* Initialisation des données en entrée */
		String noLt = "test21";
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		evts.add(newEvtEtModifs(noLt, "SD", maintenant, null));
		Evt evt = evts.get(0).getEvt();
		evt.setSsCodeEvt("12M23"); // code transport

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);

		/* Vérification de la valeur attendue */
		assertTrue("L'étape dispersion doit être la seule étape du colis",colis.getEtapes().size() == 1);
		// l'étape d'exclusion n'apparait pas dans les étapes du colis car poste
		assertTrue("L'étape DISPERSION doit apparaitre dans les étapes du colis", EEtapesColis.DISPERSION.isIncludeIn(colis.getEtapes().values()));
	}
	
	@Test
	/**
	 * Entree: 1 evt SD avec code dispersion incident
	 * 
	 * Sortie: 1 etape DISPERSION + 1 etape INCIDENT
	 * @author XRE
	 */
	public void test22_1_etape_INCIDENT_DISPERSION() {
		/* Initialisation des données en entrée */
		String noLt = "test22_1";
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		evts.add(newEvtEtModifs(noLt, "SD", maintenant, null));
		Evt evt = evts.get(0).getEvt();
		evt.setSsCodeEvt("12ERT"); // code transport

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);

		/* Vérification des valeurs attendues */
		assertTrue("Les étapes DISPERSION et INCIDENT doivent être les étapes",colis.getEtapes().size() == 2);
		assertTrue("L'étape DISPERSION doit apparaitre dans les étapes du colis", EEtapesColis.DISPERSION.isIncludeIn(colis.getEtapes().values()));
		assertTrue("L'étape INCIDENT doit apparaitre dans les étapes du colis", EEtapesColis.INCIDENT.isIncludeIn(colis.getEtapes().values()));
	}
	
	@Test
	/**
	 * Entree: 3 evt ZT, ZA, ZC
	 * 
	 * Sortie: 3 colis contenant chacun 2 étapes (Disp et Incident)
	 * @author XRE
	 */
	public void test22_2_etape_INCIDENT_DISPERSION() {
		/* Initialisation des données en entrée */
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		evts.add(newEvtEtModifs("test22_2_1", "ZT", maintenant, null));
		evts.add(newEvtEtModifs("test22_2_2", "ZA", maintenant, null));
		evts.add(newEvtEtModifs("test22_2_3", "ZC", maintenant, null));

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis1 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("test22_2_1");
		SpecifsColis colis2 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("test22_2_2");
		SpecifsColis colis3 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("test22_2_3");

		/* Vérification des valeurs attendues */
		assertTrue("Les étapes DISPERSION et INCIDENT doivent être les étapes",colis1.getEtapes().size() == 2);
		assertTrue("L'étape DISPERSION doit apparaitre dans les étapes du colis", EEtapesColis.DISPERSION.isIncludeIn(colis1.getEtapes().values()));
		assertTrue("L'étape INCIDENT doit apparaitre dans les étapes du colis", EEtapesColis.INCIDENT.isIncludeIn(colis1.getEtapes().values()));
		assertTrue("Les étapes DISPERSION et INCIDENT doivent être les étapes",colis2.getEtapes().size() == 2);
		assertTrue("L'étape DISPERSION doit apparaitre dans les étapes du colis", EEtapesColis.DISPERSION.isIncludeIn(colis2.getEtapes().values()));
		assertTrue("L'étape INCIDENT doit apparaitre dans les étapes du colis", EEtapesColis.INCIDENT.isIncludeIn(colis2.getEtapes().values()));
		assertTrue("Les étapes DISPERSION et INCIDENT doivent être les étapes",colis3.getEtapes().size() == 2);
		assertTrue("L'étape DISPERSION doit apparaitre dans les étapes du colis", EEtapesColis.DISPERSION.isIncludeIn(colis3.getEtapes().values()));
		assertTrue("L'étape INCIDENT doit apparaitre dans les étapes du colis", EEtapesColis.INCIDENT.isIncludeIn(colis3.getEtapes().values()));
	}
	
	@Test
	/**
	 * Entree: 1 evt I2
	 * 
	 * Sortie: 1 etape DISPERSION + 1 etape INCIDENT
	 * @author XRE
	 */
	public void test22_3_etape_INCIDENT_EXCLUSION() {
		/* Initialisation des données en entrée */
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		evts.add(newEvtEtModifs("test22_3", "I2", maintenant, null));

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis1 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("test22_3");

		/* Vérification des valeurs attendues */
		assertTrue("Les étapes EXCLUSION et INCIDENT doivent être les étapes",colis1.getEtapes().size() == 2);
		assertTrue("L'étape EXCLUSION doit apparaitre dans les étapes du colis", EEtapesColis.EXCLUSION.isIncludeIn(colis1.getEtapes().values()));
		assertTrue("L'étape INCIDENT doit apparaitre dans les étapes du colis", EEtapesColis.INCIDENT.isIncludeIn(colis1.getEtapes().values()));
	}
	
	@Test
	/**
	 * Entree: 2 evt SD et SK
	 * 
	 * Sortie: 1 etape DISPERSION dans les étapes du colis
	 * @author XRE
	 */
	public void test23_evt_SK() {
		/* Initialisation des données en entrée */
		String noLt = "test23";
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		evts.add(newEvtEtModifs(noLt, "SD", new DateTime(maintenant).minusMinutes(15).toDate(), null));
		Evt evt = evts.get(0).getEvt();
		evt.setSsCodeEvt("12M23"); // code transport
		evts.add(newEvtEtModifs(noLt, "SK", maintenant, null));

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);

		/* Vérification de la valeur attendue */
		assertTrue("L'étape dispersion doit être la seule étape du colis",colis.getEtapes().size() == 1);
		// l'étape d'exclusion n'apparait pas dans les étapes du colis car poste
		assertTrue("L'étape DISPERSION doit apparaitre dans les étapes du colis", EEtapesColis.DISPERSION.isIncludeIn(colis.getEtapes().values()));
	}
	
	@Test
	/**
	 * Entree: 2 evt SD et SK
	 * 
	 * Sortie: 1 etape DISPERSION dans les étapes du colis
	 * @author XRE
	 */
	public void test24_evt_IV() {
		/* Initialisation des données en entrée */
		String noLt = "test24";
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		evts.add(newEvtEtModifs(noLt, "SD", new DateTime(maintenant).minusMinutes(15).toDate(), null));
		Evt evt = evts.get(0).getEvt();
		evt.setSsCodeEvt("12M23"); // code transport
		Map<String, String> infosComp = new HashMap<>();
		infosComp.put(EInfoComp.COMMENTAIRE56.getCode(), "I8");
		evts.add(newEvtEtModifs(noLt, "IV", maintenant, infosComp ));

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);

		/* Vérification de la valeur attendue */
		assertTrue("L'étape dispersion doit être la seule étape du colis",colis.getEtapes().size() == 1);
		// l'étape d'exclusion n'apparait pas dans les étapes du colis car poste
		assertTrue("L'étape DISPERSION doit apparaitre dans les étapes du colis", EEtapesColis.DISPERSION.isIncludeIn(colis.getEtapes().values()));
	}
	
	@Test
	/**
	 * Entree: 1 evt RA (IncidentTG2Jour anciennement ExclusJ+1)
	 * 
	 * Sortie: 1 etape DISPERSION et INCIDENT dans les étapes du colis
	 * @author XRE
	 */
	public void test25_evt_RA() {
		/* Initialisation des données en entrée */
		String noLt = "test25";
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		evts.add(newEvtEtModifs(noLt, "RA", new DateTime(maintenant).minusMinutes(15).toDate(), null));

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);

		/* Vérification de la valeur attendue */
		assertTrue("Les étapes DISPERSION et INCIDENT doivent être les étapes",colis.getEtapes().size() == 2);
		// l'étape DISPERSION n'apparait pas dans les étapes du colis car poste
		assertTrue("L'étape DISPERSION doit apparaitre dans les étapes du colis", EEtapesColis.DISPERSION.isIncludeIn(colis.getEtapes().values()));
		// l'étape INCIDENT n'apparait pas dans les étapes du colis car poste
		assertTrue("L'étape INCIDENT doit apparaitre dans les étapes du colis", EEtapesColis.INCIDENT.isIncludeIn(colis.getEtapes().values()));
	}
	
	@Test
	/**
	 * Entree: 1 colis en SD avec evt KC (colis cassé) avec info comp 56 = EVT_CF_K2
	 * 
	 * Sortie: Une étape incident
	 * @author XRE
	 */
	public void test26_evt_KC_incident() {
		/* Initialisation des données en entrée */
		String noLt = "test26_1";
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		evts.add(newEvtEtModifs(noLt, "SD", new DateTime(maintenant).minusMinutes(15).toDate()));
		evts.add(newEvtEtModifs(noLt, "KC", new DateTime(maintenant).toDate(),
				addInfoComp(EInfoComp.CONSIGNE_EVT_I.getCode(), "EVT_CF_K2", null)));

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);

		/* Vérification de la valeur attendue */
		assertTrue("Les étapes DISPERSION et INCIDENT doivent être les étapes",colis.getEtapes().size() == 2);
		// l'étape DISPERSION n'apparait pas dans les étapes du colis car poste
		assertTrue("L'étape DISPERSION doit apparaitre dans les étapes du colis", EEtapesColis.DISPERSION.isIncludeIn(colis.getEtapes().values()));
		// l'étape INCIDENT n'apparait pas dans les étapes du colis car poste
		assertTrue("L'étape INCIDENT doit apparaitre dans les étapes du colis", EEtapesColis.INCIDENT.isIncludeIn(colis.getEtapes().values()));
	}
	
	@Test
	/**
	 * Entree: 1 colis en SD avec evt KC (colis cassé) avec info comp 56 = EVT_CF_K0
	 * 
	 * Sortie: Pas d'étape incident
	 * @author XRE
	 */
	public void test26_evt_KC_notIncident() {
		/* Initialisation des données en entrée */
		String noLt = "test26_2";
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		evts.add(newEvtEtModifs(noLt, "SD", new DateTime(maintenant).minusMinutes(15).toDate()));
		evts.add(newEvtEtModifs(noLt, "KC", new DateTime(maintenant).toDate(),
				addInfoComp(EInfoComp.CONSIGNE_EVT_I.getCode(), "EVT_CF_K0", null)));

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);
		/* Vérification de la valeur attendue */
		assertTrue("L'étape INCIDENT doit être les étapes",colis.getEtapes().size() == 1);
		// l'étape INCIDENT n'apparait pas dans les étapes du colis car poste
		assertTrue("L'étape INCIDENT doit apparaitre dans les étapes du colis", EEtapesColis.DISPERSION.isIncludeIn(colis.getEtapes().values()));
	}
	
	@Test
	/**
	 * Entree: 1 colis en SD et CT
	 * 
	 * Sortie: Une étape dispersion et une étape retour_agence
	 * @author XRE
	 */
	public void test27_evt_CT() {
		/* Initialisation des données en entrée */
		String noLt = "test27_1";
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		evts.add(newEvtEtModifs(noLt, "SD", new DateTime(maintenant).minusMinutes(15).toDate()));
		evts.add(newEvtEtModifs(noLt, "CT", new DateTime(maintenant).toDate()));

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);

		/* Vérification de la valeur attendue */
		assertTrue("Les étapes DISPERSION et RETOUR_AGENCE doivent être les étapes",colis.getEtapes().size() == 2);
		// l'étape DISPERSION n'apparait pas dans les étapes du colis car poste
		assertTrue("L'étape DISPERSION doit apparaitre dans les étapes du colis", EEtapesColis.DISPERSION.isIncludeIn(colis.getEtapes().values()));
		// l'étape INCIDENT n'apparait pas dans les étapes du colis car poste
		assertTrue("L'étape INCIDENT doit apparaitre dans les étapes du colis", EEtapesColis.RETOUR_AGENCE.isIncludeIn(colis.getEtapes().values()));
	}
	
	@Test
	/**
	 * Entree: 1 colis en SD et R
	 * 
	 * Sortie: Une étape dispersion et une étape retour_agence
	 * @author XRE
	 */
	public void test27_2_evt_R() {
		/* Initialisation des données en entrée */
		String noLt = "test27_2";
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		evts.add(newEvtEtModifs(noLt, "SD", new DateTime(maintenant).minusMinutes(15).toDate()));
		evts.add(newEvtEtModifs(noLt, "R", new DateTime(maintenant).toDate()));

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis = UpdateSpecificationsColisDaoUtils.getSpecificationColis(noLt);

		/* Vérification de la valeur attendue */
		assertTrue("Les étapes DISPERSION et RETOUR_AGENCE doivent être les étapes",colis.getEtapes().size() == 2);
		// l'étape DISPERSION n'apparait pas dans les étapes du colis car poste
		assertTrue("L'étape DISPERSION doit apparaitre dans les étapes du colis", EEtapesColis.DISPERSION.isIncludeIn(colis.getEtapes().values()));
		// l'étape INCIDENT n'apparait pas dans les étapes du colis car poste
		assertTrue("L'étape INCIDENT doit apparaitre dans les étapes du colis", EEtapesColis.RETOUR_AGENCE.isIncludeIn(colis.getEtapes().values()));
	}
    
    @Test
	public void test28_etape_EXCLUSION_LR() {
    	/* Initialisation des données en entrée */
		Date maintenant = new Date();
		List<EvtEtModifs> evts = new ArrayList<>();
		evts.add(newEvtEtModifs("test28", "LR", maintenant, null));

		/* Appel du MS */
		callMS(evts);

		/* Récupération du résultat */
		SpecifsColis colis1 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("test28");

		/* Vérification des valeurs attendues */
		assertTrue("Les étapes EXCLUSION et INCIDENT doivent être les étapes",colis1.getEtapes().size() == 2);
		assertTrue("L'étape EXCLUSION doit apparaitre dans les étapes du colis", EEtapesColis.EXCLUSION.isIncludeIn(colis1.getEtapes().values()));
		assertTrue("L'étape INCIDENT doit apparaitre dans les étapes du colis", EEtapesColis.INCIDENT.isIncludeIn(colis1.getEtapes().values()));
	}
	
    /**@param noLt
     * @param codeEvt
     * @param dateEvt
     * @param modifications
     * @return un EvtEtModif avec les parametres evenement et la map modification fournie
     *
     * @author LGY
     */
	private EvtEtModifs newEvtEtModifs(final String noLt, final String codeEvt, final Date dateEvt,
			final Map<String, String> infosComp, Map<String, String> modifications) {
		EvtEtModifs evtEtModif = new EvtEtModifs();
		evtEtModif.setEvt(newEvt(noLt, codeEvt, dateEvt, infosComp));
		evtEtModif.setModifications(modifications);
		return evtEtModif;
	}
    
    /**@param noLt
     * @param codeEvt
     * @param dateEvt
     * @return un EvtEtModif avec les parametres evenement et une map modification nulle
     *
     * @author LGY
     */
	private EvtEtModifs newEvtEtModifs(final String noLt, final String codeEvt, final Date dateEvt) {
		return newEvtEtModifs(noLt, codeEvt, dateEvt, null, null);
	}

	private EvtEtModifs newEvtEtModifs(final String noLt, final String codeEvt, final Date dateEvt,
			final Map<String, String> infosComp) {
		return newEvtEtModifs(noLt, codeEvt, dateEvt, infosComp, null);
	}

	/**
	 * Création d'un événement
	 * 
	 * @param noLt
	 * @param codeEvt
	 * @param dateEvt
	 * @return
	 *
	 * @author LGY
	 * @param infosComp
	 */
	private Evt newEvt(final String noLt, final String codeEvt, final Date dateEvt, Map<String, String> infosComp) {
		Evt evt = new Evt();
		evt.setNoLt(noLt);
		evt.setDateEvt(dateEvt);
		evt.setCodeEvt(codeEvt);
		evt.setInfoscomp(infosComp);
		evt.setSsCodeEvt("99S99");
		evt.setCreateurEvt("PSM001");
		return evt;
	}

    /** Appel du MS
     * 
     * @param evts : liste des evt et modif à transmettre
     *
     * @author LGY
     */
    private void callMS(List<EvtEtModifs> evts){
    	/* Execution de l'appel */
    	Entity<List<EvtEtModifs>> inputEntity = Entity.entity(evts,MediaType.APPLICATION_JSON);
    	client.target("http://localhost:" + getPort()).path("/UpdateSpecificationsColis/v1").request().accept(MediaType.APPLICATION_JSON_TYPE).post(inputEntity);
    }

    /**
     * Suppression des données généré par le jeux de tests
     * 
     *
     * @author LGY
     */
	private void nettoyageTests() {
		/* nettoyage de la base */
		VisionMicroserviceApplication.getCassandraSession()
				.execute(QueryBuilder.truncate(ETableColisSpecifications.TABLE_NAME));
	}

    /**
     * Ajoute une infocomp dans la map des infoscomp 
     * Si la map fournie est null, une map est créé, sinon
     * celle fournie est utilisée.
     * 
     * @param key : le code infocomp
     * @param value : la valeur de l'infocomp
     * @param infoComp : la map d'infocomp existante, ou null si vide
     * @return
     *
     * @author LGY
     */
	private Map<String, String> addInfoComp(final String key, final String value, final Map<String, String> infoComp) {
		final Map<String, String> result = infoComp != null ? infoComp : new HashMap<String, String>();
		result.put(key, value);
		return result;
	}
    
    @AfterClass
	public void tearDownAfterClass() throws Exception {
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
		CacheManagerService.INSTANCE.stopUpdater();
		CacheManagerService.INSTANCE.delProjet("evenement");
		CacheManagerService.INSTANCE.delProjet("service");
		nettoyageTests();
	}
}
