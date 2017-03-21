package com.chronopost.vision.microservices.lt.insert;

import static com.chronopost.cassandra.request.builder.CassandraRequestBuilder.buildSelect;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.cassandra.table.ETableColisSpecifications;
import com.chronopost.cassandra.table.ETableLt;
import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.CacheManagerService;
import com.chronopost.vision.cachemanager.refcontrat.RefContrat;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.ConnectionDetails;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.enums.EInsertLT;
import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.chronopost.vision.transco.transcoder.Transcoder;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

/** @author unknown : JJC getSession +  LOGGER import min.**/
public class InsertLtDaoTest {

	private boolean suiteLaunch = true;
	private InsertLtDAO dao;
	private PreparedStatement psCleanLt;
	private PreparedStatement psCleanRefContrat;
	private PreparedStatement psCleanColisSpec;
	private PreparedStatement psCleanParametre;
	private PreparedStatement psCleanLtCounters;
	private PreparedStatement psSelectLtCounters;
	private PreparedStatement psGetSpecifEvt;
	private PreparedStatement psGetAdrDestColisSpec;
	private PreparedStatement psGetAdrDestLt;

	private Lt lt;
	private Lt lt2;
	private Lt lt3;
	private String dateDuJour;

	private final static String NO_LT1 = "insertLt1";
	private final static String NO_LT2 = "insertLt2";
	private final static String NO_LT3 = "insertLt3";
	private final static String NUM_CONTRAT1 = "contrat1";
	private final static String NUM_CONTRAT2 = "contrat2";
	private final static String NUM_CONTRAT3 = "contrat3";

	private final static String ID_ADR_DEST_1 = "idAdrDest1";
	private final static String ID_ADR_DEST_2 = "idAdrDest2";
	private final static String ID_ADR_DEST_3 = "idAdrDest3";
	private final static String ID_ADR_DEST_4 = "idAdrDest4";
	private final static String ID_POI_DEST_1 = "idPoiDest1";
	private final static String ID_POI_DEST_2 = "idPoiDest2";
	private final static String ID_POI_DEST_3 = "idPoiDest3";
	private final static String ID_POI_DEST_4 = "idPoiDest4";

    @SuppressWarnings("unchecked")
	@BeforeClass
	public void setUp() throws Exception {
		if (BuildCluster.HOST.equals(System.getProperty("host", ConnectionDetails.getHost()))) {
			BuildCluster.setUpBeforeSuite();
			suiteLaunch = false;
		}
		CCMBridge.ipOfNode(1);

		dao = InsertLtDAO.getInstance();
		CacheManager<RefContrat> cacheRefContrat = Mockito.mock(CacheManager.class);
		final Map<String, RefContrat> contrats = new HashMap<>();
		contrats.put(NUM_CONTRAT1, new RefContrat(NUM_CONTRAT1, new HashSet<>(Arrays.asList("CARAC_1"))));
		contrats.put(NUM_CONTRAT2, new RefContrat(NUM_CONTRAT2, new HashSet<>(Arrays.asList("CARAC_2", "CARAC_3"))));
		Mockito.when(cacheRefContrat.getCache()).thenReturn(contrats);
		dao.setReferentielRefContrat(cacheRefContrat);
		
		FeatureFlips.INSTANCE.setFlipProjectName("Vision");
    	Map<String, Map<String, String>> map = new  HashMap<>();
    	
    	Map<String, String> idC11Flip = new HashMap<>();
    	idC11Flip.put("QualificatifContrat", "true");
    	map.put("feature_flips", idC11Flip);
    	
		ConcurrentMap<String, Transcoder> transcoders = new ConcurrentHashMap<>();
		Transcoder transcoderIdC11Plus = new Transcoder();
		transcoderIdC11Plus.setTranscodifications(map);
		transcoders.put("Vision", transcoderIdC11Plus);
		TranscoderService.INSTANCE.setTranscoders(transcoders);
		final ITranscoderDao mockTranscoderDao = Mockito.mock(ITranscoderDao.class);
		Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("Vision")).thenReturn(map);
		TranscoderService.INSTANCE.setDao(mockTranscoderDao);

		psCleanLt = getSession().prepare("delete from lt where no_lt in ('" + NO_LT1 + "','" + NO_LT2 + "','" + NO_LT3 + "')");
		psCleanLtCounters = getSession().prepare("truncate lt_counters");
		psCleanRefContrat = getSession().prepare("truncate ref_contrat");
		psCleanParametre = getSession().prepare("truncate parametre");
		psCleanColisSpec = getSession().prepare("truncate colis_specifications");
		psSelectLtCounters = getSession().prepare(
				"SELECT jour, heure, minute, hit_insertlt, lt_in_insertlt, lt_out_insertlt FROM lt_counters WHERE jour = ? and heure = ? and minute = ?");
		psGetSpecifEvt = getSession().prepare(
				buildSelect(ETableColisSpecifications.TABLE_NAME, Arrays.asList(ETableColisSpecifications.SPECIFS_EVT))
						.getQuery());
		psGetAdrDestColisSpec = getSession().prepare(buildSelect(ETableColisSpecifications.TABLE_NAME, Arrays
				.asList(ETableColisSpecifications.ID_ADRESSE_DESTINATAIRE, ETableColisSpecifications.ID_POI_DESTINATAIRE))
						.getQuery());
		psGetAdrDestLt = getSession().prepare(buildSelect(ETableLt.TABLE_NAME,
				Arrays.asList(ETableLt.ID_ADRESSE_DESTINATAIRE, ETableLt.ID_POI_DESTINATAIRE)).getQuery());
        CacheManagerService.INSTANCE.startUpdater();
		init();
	}
    
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

    private void init() {
    	// clean tables before tests
    	getSession().execute(psCleanLt.getQueryString());
    	getSession().execute(psCleanLtCounters.getQueryString());
    	getSession().execute(psCleanRefContrat.getQueryString());
    	getSession().execute(psCleanParametre.getQueryString());
    	getSession().execute(psCleanColisSpec.getQueryString());
    	
    	// insére ref_contrats
    	
        dateDuJour = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        LinkedHashSet<String> codesEvt = new LinkedHashSet<>();
        codesEvt.add("DC");
        codesEvt.add("TA");
        codesEvt.add("D");
        LinkedHashSet<String> evts = new LinkedHashSet<>();

        evts.add("000117|2015-03-18 15:03:00|NA146848396FR|||DC|||||APRO|2015-03-18T17:07:04|0|715725386||2|Envoi prêt chez l''expéditeur||NA516|||0||EVT_CHR|EDI||Préparation expéditeur|{'119':'899','94':'1/2','77':'20090','76':'FR','231':'GEO/NA146848396248S','118':'%0020090NA146848396248899250','115':'01'}");
        evts.add("000117|2015-03-18 15:04:00|NA146848396FR|||EA|||||TEST|2015-03-18T17:07:04|0|715725388||129|Envoi faisant partie d''une expédition groupée||NA516|||0||EVT_CHR|EDI||Préparation expéditeur|{'103':'NA146848405FR','94':'2/2','115':'01'}");
        evts.add("000117|2015-03-18 17:12:00|NA146848396FR|||K|||||PSFPUS|2015-03-18T17:21:57|0|717691165||131|Echange informatique de données||PUS|||0||EVT_CHR|||DIVERS|{'7':'20150318-1712','10':'200360','167':'5O','168':'BP','169':'1111100','170':'87'}");
        evts.add("000132|2015-03-18 16:06:00|NA146848396FR||%0020090NA146848396248899250|PC|||||PSMC511|2015-03-18T16:27:49|0|716642940||32|Envoi pris en charge chez l''expéditeur||14999|||0||EVT_CHR|14T20||Acheminement en cours|{'64':'20150318-1627','253':'00068581215077','190':'49.228855','191':'-0.263886666666667','192':'8','45':'0'}");
        evts.add("000145|2015-03-18 18:30:00|NA146848396FR||%0020090NA146848396248899250|SC|||||SACAP01|2015-03-18T18:34:47|0|716445967||6|Tri effectué dans l''agence de départ||14999|||0||EVT_CHR|14AJA||Acheminement en cours|{'':''}");
        evts.add("000145|2015-03-18 22:30:00|NA146848396FR||%0020090NA146848396248899250|TS|||||TRI1|2015-03-18T22:30:01|0|717430515||87|Envoi en transit||93999|||0||EVT_CHR|AJA0||Acheminement en cours|{'166':'2','225':'005R','235':'AJA0','239':'TRIEURR','251':'OK','12':'37','13':'32','11':'36','234':'RELIABLE/RELIABLE/RELIABLE','2':'760','233':'RELIABLE'}");
        evts.add("000145|2015-03-19 08:15:00|NA146848396FR||%0020090NA146848396248899250|TA|||||TRI1|2015-03-18T22:34:56|0|717493191||88|Envoi en transit||93999|||0||EVT_CHR|AJA0||Acheminement en cours|{'225':'111A','235':'AJA0','239':'TRIEURA','251':'OK'}");
        evts.add("000146|2015-03-19 08:30:00|NA146848396FR||%0020090NA146848396248899250|P|||||TRI1|2015-03-18T22:34:56|0|717493191||88|Envoi en transit||93999|||0||EVT_CHR|AJA0||Acheminement en cours|{'225':'111A','235':'AJA0','239':'TRIEURA','251':'OK'}");
        evts.add("000145|"
                + dateDuJour
                + " 07:12:00|NA146848396FR||%0020090NA146848396248899250|TA|||||TRI1|"
                + dateDuJour
                + "T07:12:56|0|717493191||88|Envoi en transit||93999|||0||EVT_CHR|AJA0||Acheminement en cours|{'225':'111A','235':'AJA0','239':'TRIEURA','251':'OK'}");
        evts.add("000146|"
                + dateDuJour
                + " 08:15:00|NA146848396FR||%0020090NA146848396248899250|P|||||TRI1|"
                + dateDuJour
                + "T08:15:56|0|717493191||88|Envoi en transit||93999|||0||EVT_CHR|AJA0||Acheminement en cours|{'225':'111A','235':'AJA0','239':'TRIEURA','251':'OK'}");
        evts.add("000146|"
                + dateDuJour
                + " 09:15:00|NA146848396FR||%0020090NA146848396248899250|RB|||||TRI1|"
                + dateDuJour
                + "T09:15:56|0|717493191||88|Envoi en transit||93999|||0||EVT_CHR|AJA0||Acheminement en cours|{'225':'111A','235':'AJA0','239':'TRIEURA','251':'OK'}");

        LinkedHashSet<String> synonymes = new LinkedHashSet<>();
        synonymes.add(NO_LT2);
        lt = new Lt().setNoLt(NO_LT1).setAdresse1Destinataire("adresse_1_destinataire")
                .setAdresse1Expediteur("adresse_1_expediteur").setAdresse2Destinataire("adresse_2_destinataire")
                .setAdresse2Expediteur("adresse_2_expediteur").setArticle1("article_1")
                .setCodeEtatDestinataire("code_etat_destinataire").setCodeEtatExpediteur("code_etat_expediteur")
                .setCodeEvt("code_evt").setCodeEvtExt("code_evt_ext")
                .setCodePaysDestinataire("code_pays_destinataire").setCodePaysExpediteur("code_pays_expediteur")
                .setCodePaysNumDestinataire("code_pays_num_destinataire")
                .setCodePaysNumExpediteur("code_pays_num_expediteur").setCodePointRelais("code_point_relais")
                .setCodePostalDestinataire("code_postal_destinataire").setCodePostalEvt("code_postal_evt")
                .setCodePostalExpediteur("code_postal_expediteur").setCodeProduit("code_produit")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setCodesEvt(codesEvt)
                .setCrbtRep("crbt_rep").setCreateurEvt("createur_evt").setDateCreationEvt("date_creation_evt")
                .setDateDepotLt(new Timestamp(0)).setDateDepotLtIntern(new Timestamp(0))
                .setDateEntreeSi(new Timestamp(0)).setDateHeureSaisie("date_heure_saisie")
                .setDateLivraisonContractuelle(new Timestamp(0)).setDateLivraisonPrevue(new Timestamp(0))
                .setDateModification(new Timestamp(0)).setDepotExpediteur("depot_expediteur")
                .setDescription("description").setDestinationIdFedex("destination_id_fedex")
                .setDeviseAssurance("devise_assurance").setDeviseRep("devise_rep")
                .setDeviseValDeclaree("devise_val_declaree").setDocMarch("doc_march")
                .setEmail1Destinataire("email_1_destinataire").setEmail1Expediteur("email_1_expediteur")
                .setEmail2Destinataire("email_2_destinataire").setEmail2Expediteur("email_2_expediteur")
                .setEsd("esd").setHauteur(0).setIdAbonnement("id_abonnement").setIdAccesClient(0)
                .setIdAppli("id_appli").setIdColisClient("id_colis_client")
                .setIdExtractionEvt("id_extraction_evt").setIdLigne("id_ligne").setIdSsCodeEvt(0).setLargeur(0)
                .setNoContrat(NUM_CONTRAT1).setNoSsCompte("no_ss_compte")
                .setNom1Destinataire("nom_1_destinataire").setNom1Expediteur("nom_1_expediteur")
                .setNom2Destinataire("nom_2_destinataire").setNom2Expediteur("nom_2_expediteur")
                .setOrigineSaisie("origine_saisie").setPoids(0).setRefAbonnement("ref_abonnement")
                .setRefDestinataire("ref_destinataire").setRefExpediteur("ref_expediteur")
                .setRefExtraction("ref_extraction").setRefIdAbonnement("ref_id_abonnement")
                .setStatusEnvoi("status_envoi").setTelephoneDestinataire("telephone_destinataire")
                .setTelephoneExpediteur("telephone_expediteur").setValDeclaree("val_declaree")
                .setValeurAssuree("valeur_assuree").setValeurRep("valeur_rep")
                .setVilleDestinataire("ville_destinataire").setVilleExpediteur("ville_expediteur").setEta("18:00")
                .setPositionC11("position_c11").setCreneauChargeurEtCreneauTournee("08:00-18:00", "08:00-18:00")
                .setLatitudePrevue("latitude_prevue").setLongitudePrevue("longitude_prevue")
                .setLatitudeDistri("latitude_distri").setLongitudeDistri("longitude_distri").setEvts(evts)
                .setSynonymes(synonymes).setSynonymeMaitre(NO_LT1).setIdxDepassement("2015-01-01__1")
                .setIdAdresseDestinataire(ID_ADR_DEST_1).setIdPoiDestinataire(ID_POI_DEST_1);

        lt2 = new Lt().setNoLt(NO_LT2).setAdresse1Destinataire("adresse_1_destinataire")
                .setAdresse1Expediteur("adresse_1_expediteur").setAdresse2Destinataire("adresse_2_destinataire")
                .setAdresse2Expediteur("adresse_2_expediteur").setArticle1("article_1")
                .setCodeEtatDestinataire("code_etat_destinataire").setCodeEtatExpediteur("code_etat_expediteur")
                .setCodeEvt("code_evt").setCodeEvtExt("code_evt_ext")
                .setCodePaysDestinataire("code_pays_destinataire").setCodePaysExpediteur("code_pays_expediteur")
                .setCodePaysNumDestinataire("code_pays_num_destinataire")
                .setCodePaysNumExpediteur("code_pays_num_expediteur").setCodePointRelais("code_point_relais")
                .setCodePostalDestinataire("code_postal_destinataire").setCodePostalEvt("code_postal_evt")
                .setCodePostalExpediteur("code_postal_expediteur").setCodeProduit("code_produit")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setCodesEvt(codesEvt)
                .setCrbtRep("crbt_rep").setCreateurEvt("createur_evt").setDateCreationEvt("date_creation_evt")
                .setDateDepotLt(new Timestamp(0)).setDateDepotLtIntern(new Timestamp(0))
                .setDateEntreeSi(new Timestamp(0)).setDateHeureSaisie("date_heure_saisie")
                .setDateLivraisonContractuelle(new Timestamp(0)).setDateLivraisonPrevue(new Timestamp(0))
                .setDateModification(new Timestamp(0)).setDepotExpediteur("depot_expediteur")
                .setDescription("description").setDestinationIdFedex("destination_id_fedex")
                .setDeviseAssurance("devise_assurance").setDeviseRep("devise_rep")
                .setDeviseValDeclaree("devise_val_declaree").setDocMarch("doc_march")
                .setEmail1Destinataire("email_1_destinataire").setEmail1Expediteur("email_1_expediteur")
                .setEmail2Destinataire("email_2_destinataire").setEmail2Expediteur("email_2_expediteur")
                .setEsd("esd").setHauteur(0).setIdAbonnement("id_abonnement").setIdAccesClient(0)
                .setIdAppli("id_appli").setIdColisClient("id_colis_client")
                .setIdExtractionEvt("id_extraction_evt").setIdLigne("id_ligne").setIdSsCodeEvt(0).setLargeur(0)
                .setNoContrat(NUM_CONTRAT2).setNoSsCompte("no_ss_compte")
                .setNom1Destinataire("nom_1_destinataire").setNom1Expediteur("nom_1_expediteur")
                .setNom2Destinataire("nom_2_destinataire").setNom2Expediteur("nom_2_expediteur")
                .setOrigineSaisie("origine_saisie").setPoids(0).setRefAbonnement("ref_abonnement")
                .setRefDestinataire("ref_destinataire").setRefExpediteur("ref_expediteur")
                .setRefExtraction("ref_extraction").setRefIdAbonnement("ref_id_abonnement")
                .setStatusEnvoi("status_envoi").setTelephoneDestinataire("telephone_destinataire")
                .setTelephoneExpediteur("telephone_expediteur").setValDeclaree("val_declaree")
                .setValeurAssuree("valeur_assuree").setValeurRep("valeur_rep")
                .setVilleDestinataire("ville_destinataire").setVilleExpediteur("ville_expediteur").setEta("11:00")
                .setPositionC11("position_c11").setLatitudePrevue("latitude_prevue")
                .setLongitudePrevue("longitude_prevue").setLatitudeDistri("latitude_distri")
                .setLongitudeDistri("longitude_distri").setEvts(evts).setSynonymes(synonymes).setIdxDepassement("2015-01-01__1")
                .setIdAdresseDestinataire(ID_ADR_DEST_2).setIdPoiDestinataire(ID_POI_DEST_2);
        
        lt3 = new Lt().setNoLt(NO_LT3).setAdresse1Destinataire("adresse_1_destinataire")
                .setAdresse1Expediteur("adresse_1_expediteur").setAdresse2Destinataire("adresse_2_destinataire")
                .setAdresse2Expediteur("adresse_2_expediteur").setArticle1("article_1")
                .setCodeEtatDestinataire("code_etat_destinataire").setCodeEtatExpediteur("code_etat_expediteur")
                .setCodeEvt("code_evt").setCodeEvtExt("code_evt_ext")
                .setCodePaysDestinataire("code_pays_destinataire").setCodePaysExpediteur("code_pays_expediteur")
                .setCodePaysNumDestinataire("code_pays_num_destinataire")
                .setCodePaysNumExpediteur("code_pays_num_expediteur").setCodePointRelais("code_point_relais")
                .setCodePostalDestinataire("code_postal_destinataire").setCodePostalEvt("code_postal_evt")
                .setCodePostalExpediteur("code_postal_expediteur").setCodeProduit("code_produit")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setCodesEvt(codesEvt)
                .setCrbtRep("crbt_rep").setCreateurEvt("createur_evt").setDateCreationEvt("date_creation_evt")
                .setDateDepotLt(new Timestamp(0)).setDateDepotLtIntern(new Timestamp(0))
                .setDateEntreeSi(new Timestamp(0)).setDateHeureSaisie("date_heure_saisie")
                .setDateLivraisonContractuelle(new Timestamp(0)).setDateLivraisonPrevue(new Timestamp(0))
                .setDateModification(new Timestamp(0)).setDepotExpediteur("depot_expediteur")
                .setDescription("description").setDestinationIdFedex("destination_id_fedex")
                .setDeviseAssurance("devise_assurance").setDeviseRep("devise_rep")
                .setDeviseValDeclaree("devise_val_declaree").setDocMarch("doc_march")
                .setEmail1Destinataire("email_1_destinataire").setEmail1Expediteur("email_1_expediteur")
                .setEmail2Destinataire("email_2_destinataire").setEmail2Expediteur("email_2_expediteur")
                .setEsd("esd").setHauteur(0).setIdAbonnement("id_abonnement").setIdAccesClient(0)
                .setIdAppli("id_appli").setIdColisClient("id_colis_client")
                .setIdExtractionEvt("id_extraction_evt").setIdLigne("id_ligne").setIdSsCodeEvt(0).setLargeur(0)
                .setNoContrat(NUM_CONTRAT3).setNoSsCompte("no_ss_compte")
                .setNom1Destinataire("nom_1_destinataire").setNom1Expediteur("nom_1_expediteur")
                .setNom2Destinataire("nom_2_destinataire").setNom2Expediteur("nom_2_expediteur")
                .setOrigineSaisie("origine_saisie").setPoids(0).setRefAbonnement("ref_abonnement")
                .setRefDestinataire("ref_destinataire").setRefExpediteur("ref_expediteur")
                .setRefExtraction("ref_extraction").setRefIdAbonnement("ref_id_abonnement")
                .setStatusEnvoi("status_envoi").setTelephoneDestinataire("telephone_destinataire")
                .setTelephoneExpediteur("telephone_expediteur").setValDeclaree("val_declaree")
                .setValeurAssuree("valeur_assuree").setValeurRep("valeur_rep")
                .setVilleDestinataire("ville_destinataire").setVilleExpediteur("ville_expediteur").setEta("11:00")
                .setPositionC11("position_c11").setLatitudePrevue("latitude_prevue")
                .setLongitudePrevue("longitude_prevue").setLatitudeDistri("latitude_distri")
                .setLongitudeDistri("longitude_distri").setEvts(evts).setSynonymes(synonymes).setIdxDepassement("2015-01-01__1")
                .setIdAdresseDestinataire(ID_ADR_DEST_3).setIdPoiDestinataire(ID_POI_DEST_3);
    }

    @Test(groups = { "database-needed", "slow" })
    public void testInit() {
        Map<EInsertLT, PreparedStatement> ps = dao.getPs();

        assertEquals(ps.get(EInsertLT.CAB_RECU).getQueryString(), "UPDATE lt set " + EInsertLT.CAB_RECU.getColName()
                + " = ? where no_lt = ?");
    }

    @Test(groups = { "database-needed", "slow" })
	public void insertLts() throws MSTechnicalException, FunctionalException {
		ResultSet execute = null;
		Row one = null;

		List<Lt> listeLTs = new ArrayList<Lt>();
		listeLTs.add(lt);
		listeLTs.add(lt2);
		listeLTs.add(lt3);
		dao.insertLts(listeLTs);

		Statement statement;
		statement = new SimpleStatement("select * from lt where no_lt='" + NO_LT1 + "';");
		statement.setConsistencyLevel(ConsistencyLevel.QUORUM);
		execute = VisionMicroserviceApplication.getCassandraSession().execute(statement);
		one = execute.one();

		assertNotNull(execute);
		assertEquals(one.getString("position_c11"), "position_c11");
		assertEquals(one.getString("status_envoi"), "status_envoi");
		assertEquals(one.getString("idx_depassement"), "2015-01-01__1");

		statement = new SimpleStatement("select position_c11, status_envoi from lt where no_lt='" + NO_LT2 + "'");
		statement.setConsistencyLevel(ConsistencyLevel.QUORUM);
		execute = VisionMicroserviceApplication.getCassandraSession().execute(statement);
		one = execute.one();
		assertNotNull(execute);
		assertEquals(one.getString("position_c11"), "position_c11");
		assertEquals(one.getString("status_envoi"), "status_envoi");

		// check update de lt_counters
		final SimpleDateFormat FORMAT_JOUR_HEURE_MINUTE = new SimpleDateFormat("yyyyMMddHHmm");
		final Date maintenant = new Date();
		final String maintenantString = FORMAT_JOUR_HEURE_MINUTE.format(maintenant);
		final ResultSet resultSet = getSession().execute(psSelectLtCounters.bind(maintenantString.substring(0, 8),
				maintenantString.substring(8, 10), maintenantString.substring(10, 11)));
		final Row row = resultSet.one();
		assertEquals(row.getString(0), maintenantString.substring(0, 8));
		assertEquals(row.getString(1), maintenantString.substring(8, 10));
		assertEquals(row.getString(2), maintenantString.substring(10, 11));
		assertEquals(row.getLong(3), 1L);
		assertEquals(row.getLong(4), 3L);
		assertEquals(row.getLong(5), 3L);
		
		// get specif colis
		final ResultSet res2 = getSession().execute(psGetSpecifEvt.bind(NO_LT1));
		final Map<Date, String> map2 = res2.one().getMap(0, Date.class, String.class);
		assertTrue(map2.values().contains("CARAC_1"));
		final ResultSet res3 = getSession().execute(psGetSpecifEvt.bind(NO_LT2));
		final Map<Date, String> map3 = res3.one().getMap(0, Date.class, String.class);
		assertTrue(map3.values().contains("CARAC_2"));
		assertTrue(map3.values().contains("CARAC_3"));
		final ResultSet res4 = getSession().execute(psGetSpecifEvt.bind(NO_LT3));
		final Map<Date, String> map4 = res4.one().getMap(0, Date.class, String.class);
		assertTrue(map4.size() == 0);
		
		// check remplissage des champs id_adresse_destinataire et id_poi_destinataire du colis_spec
		final ResultSet getAdrSpecC1 = getSession().execute(psGetAdrDestColisSpec.bind(NO_LT1));
		final Row specColis1 = getAdrSpecC1.one();
		final Map<Date, String> idAdLt1 =specColis1.getMap(0, Date.class, String.class);
		assertTrue(idAdLt1.values().contains("L|" + ID_ADR_DEST_1));
		final Map<Date, String> idPoiLt1 = specColis1.getMap(1, Date.class, String.class);
		assertTrue(idPoiLt1.values().contains("L|" + ID_POI_DEST_1));
		final ResultSet getAdrSpecC2 = getSession().execute(psGetAdrDestColisSpec.bind(NO_LT2));
		final Row specColis2 = getAdrSpecC2.one();
		final Map<Date, String> idAdLt2 = specColis2.getMap(0, Date.class, String.class);
		assertTrue(idAdLt2.values().contains("L|" + ID_ADR_DEST_2));
		final Map<Date, String> idPoiLt2 = specColis2.getMap(1, Date.class, String.class);
		assertTrue(idPoiLt2.values().contains("L|" + ID_POI_DEST_2));
		final ResultSet getAdrSpecC3 = getSession().execute(psGetAdrDestColisSpec.bind(NO_LT3));
		final Row specColis3 = getAdrSpecC3.one();
		final Map<Date, String> idAdLt3 = specColis3.getMap(0, Date.class, String.class);
		assertTrue(idAdLt3.values().contains("L|" + ID_ADR_DEST_3));
		final Map<Date, String> idPoiLt3 = specColis3.getMap(1, Date.class, String.class);
		assertTrue(idPoiLt3.values().contains("L|" + ID_POI_DEST_3));

		// check remplissage des champs id_adresse_destinataire et id_poi_destinataire du lt
		final ResultSet getAdrLt1 = getSession().execute(psGetAdrDestLt.bind(NO_LT1));
		final Row rowLt1 = getAdrLt1.one();
		assertEquals(ID_ADR_DEST_1, rowLt1.getString(0));
		assertEquals(ID_POI_DEST_1, rowLt1.getString(1));
		final ResultSet getAdrLt2 = getSession().execute(psGetAdrDestLt.bind(NO_LT2));
		final Row rowLt2 = getAdrLt2.one();
		assertEquals(ID_ADR_DEST_2, rowLt2.getString(0));
		assertEquals(ID_POI_DEST_2, rowLt2.getString(1));
		final ResultSet getAdrLt3 = getSession().execute(psGetAdrDestLt.bind(NO_LT3));
		final Row rowLt3 = getAdrLt3.one();
		assertEquals(ID_ADR_DEST_3, rowLt3.getString(0));
		assertEquals(ID_POI_DEST_3, rowLt3.getString(1));
		
		// refait un insert du lt3 avec id_adr et id_poi différents
		// vérifie que les map contiennent les 2 id
		listeLTs.clear();
		lt3.setIdAdresseDestinataire(ID_ADR_DEST_4);
		lt3.setIdPoiDestinataire(ID_POI_DEST_4);
		listeLTs.add(lt3);
		dao.insertLts(listeLTs);
		final ResultSet getAdrSpecC3_2 = getSession().execute(psGetAdrDestColisSpec.bind(NO_LT3));
		final Row specColis3_2 = getAdrSpecC3_2.one();
		final Map<Date, String> idAdLt3_2 = specColis3_2.getMap(0, Date.class, String.class);
		assertTrue(idAdLt3_2.values().contains("L|" + ID_ADR_DEST_3));
		assertTrue(idAdLt3_2.values().contains("L|" + ID_ADR_DEST_4));
		final Map<Date, String> idPoiLt3_2 = specColis3_2.getMap(1, Date.class, String.class);
		assertTrue(idPoiLt3_2.values().contains("L|" + ID_POI_DEST_3));
		assertTrue(idPoiLt3_2.values().contains("L|" + ID_POI_DEST_4));
	}

    @AfterClass
    public void tearDownAfterClass() throws Exception {
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }
}
