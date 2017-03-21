package com.chronopost.vision.microservices.lt.getcodetourneefromlt;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.testng.Assert.assertEquals;

import java.sql.Timestamp;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.lt.getcodetourneefromlt.GetCodeTourneeFromLTDAOImpl;
import com.chronopost.vision.microservices.lt.getcodetourneefromlt.GetCodeTourneeFromLTException;
import com.chronopost.vision.microservices.lt.getcodetourneefromlt.GetCodeTourneeFromLTServiceImpl;
import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.model.GetCodeTourneeFromLTResponse;
import com.chronopost.vision.ut.RandomUts;
import com.datastax.driver.core.Session;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

/** @author unknown : JJC getSession + randomPort **/
public class GetCodeTourneeFromLTAcceptanceTest {

	private boolean suiteLaunch = true;

	private WireMockServer wireMockServer;
    private WireMock wireMock;
    
    private int httpPort = RandomUts.getRandomHttpPort(); 
	
    GetCodeTourneeFromLTServiceImpl service;
		
	/** @return  VisionMicroserviceApplication.cassandraSession  (a com.datastax.driver.core )  */
    private static final Session getSession() { return VisionMicroserviceApplication.getCassandraSession() ; }
		
	
	@BeforeClass(groups={"init"})
	public void setUp() throws Exception {

		//getLtV1Service = GetLtV1.getInstance();
		//getLtV1Service.setEndpoint("http://127.0.0.1:" + httpPort);
		wireMockServer = new WireMockServer(httpPort);
	    wireMockServer.start();
	    WireMock.configureFor("127.0.0.1", httpPort);
	    wireMock = new WireMock("127.0.0.1", httpPort);
	    
		if (!BuildCluster.clusterHasBuilt) {
			BuildCluster.setUpBeforeSuite();
			suiteLaunch = false;
		}
		CCMBridge.ipOfNode(1);

		GetLtV1.getInstance().setEndpoint("http://127.0.0.1:"+ httpPort);
		service = GetCodeTourneeFromLTServiceImpl.getInstance().setDao(GetCodeTourneeFromLTDAOImpl.getInstance()).setGetLtV1(GetLtV1.getInstance()) ;
  

		// LEAK POSSIBLE !! Session session = getSession() ;
		getSession().execute("insert into colis_tournee_agence (numero_lt, date_maj, id_c11, id_tournee) values (?,?,?,?)",
				"EE000000001FR",
				new Timestamp(50000L),
				"1",
				"AAA11111");
		getSession().execute("insert into colis_tournee_agence (numero_lt, date_maj, id_c11, id_tournee) values (?,?,?,?)",
				"EE000000001FR",
				new Timestamp(60000L),
				"2",
				"AAA22222");		
		
	}

	/**
	 * On cherche une tournee ayant pris en charge un colis alors qu'une autre prise en charge a eu lieu dans le futur.
	 * On doit bien récupérer la 1ere tournée.
	 * 
	 * @throws GetCodeTourneeFromLTException
	 */
	@Test(groups={"slow","acceptance"})
	public void cas1Test1() throws GetCodeTourneeFromLTException {
		
		wireMock.register(post(urlEqualTo("/GetLTs/true"))
	            .withHeader("Accept", equalTo("application/json"))
	            .willReturn(aResponse()
	                .withStatus(200)
	                .withHeader("Content-Type", "application/json")
	                .withBody(WiremockResponse.GET_LT_RESPONSE_CAS1_TEST1)));
		
		GetCodeTourneeFromLTResponse tournee = service.findTourneeBy("EE000000001FR", new Timestamp(50000L));
		
		assertEquals(tournee.getCodeAgence(),"AAA");
		assertEquals(tournee.getCodeTournee(),"11111");
		
				
	}
	
	/**
	 * On cherche une tournee ayant pris en charge un colis alors qu'une autre prise en charge a eu lieu dans le passé.
	 * On doit bien récupérer la dernière tournée.
	 * 
	 * @throws GetCodeTourneeFromLTException
	 */
	@Test(groups={"slow","acceptance"})
	public void cas1Test2() throws GetCodeTourneeFromLTException {
		
		wireMock.register(post(urlEqualTo("/GetLTs/true"))
	            .withHeader("Accept", equalTo("application/json"))
	            .willReturn(aResponse()
	                .withStatus(200)
	                .withHeader("Content-Type", "application/json")
	                .withBody(WiremockResponse.GET_LT_RESPONSE_CAS1_TEST1)));
		
		
		GetCodeTourneeFromLTResponse tournee2 = service.findTourneeBy("EE000000001FR", new Timestamp(650000L));
		
		assertEquals(tournee2.getCodeAgence(),"AAA");
		assertEquals(tournee2.getCodeTournee(),"22222");
		
		
		
		
	}
	
	/**
	 * On recherche une tournee pour une date sur laquelle on n'a pas de correspondance.
	 * Le test doit lever une exception.
	 * 
	 * @throws GetCodeTourneeFromLTException
	 */
	@Test(expectedExceptions=GetCodeTourneeFromLTException.class, groups = { "database-needed", "slow" ,"acceptance"})
	public void cas1ExceptionTest() throws GetCodeTourneeFromLTException{
		
		wireMock.register(post(urlEqualTo("/GetLTs/true"))
	            .withHeader("Accept", equalTo("application/json"))
	            .willReturn(aResponse()
	                .withStatus(200)
	                .withHeader("Content-Type", "application/json")
	                .withBody(WiremockResponse.GET_LT_RESPONSE_CAS1_TEST1)));
		
		
		service.findTourneeBy("EE000000001FR", new Timestamp(40000L));
	}


	@AfterClass
	public void tearDownAfterClass() throws Exception {
		
		getSession().execute("truncate colis_tournee_agence");
		
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
	}
	
    
    
    private static class WiremockResponse{
    	private final static String GET_LT_RESPONSE_CAS1_TEST1 = new StringBuilder().append("{ ")                 																																																																																					
    			.append("     \"EE000000001FR\": {                                                                                                                                                                                                                                                                                                                                                  " )
    			.append("         \"no_lt\": \"EE000000001FR\",                                                                                                                                                                                                                                                                                                                                     " )
    			.append("         \"adresse_1_destinataire\": \"CC MERIEM RTE DE PAU\",                                                                                                                                                                                                                                                                                                             " )
    			.append("         \"adresse_1_expediteur\": \"QUARTIER TROMPETTE\",                                                                                                                                                                                                                                                                                                                 " )
    			.append("         \"adresse_2_destinataire\": \"\",                                                                                                                                                                                                                                                                                                                                 " )
    			.append("         \"adresse_2_expediteur\": \"\",                                                                                                                                                                                                                                                                                                                                   " )
    			.append("         \"article_1\": \"\",                                                                                                                                                                                                                                                                                                                                              " )
    			.append("         \"cab_evt_saisi\": null,                                                                                                                                                                                                                                                                                                                                          " )
    			.append("         \"cab_recu\": null,                                                                                                                                                                                                                                                                                                                                               " )
    			.append("         \"code_etat_destinataire\": \"\",                                                                                                                                                                                                                                                                                                                                 " )
    			.append("         \"code_etat_expediteur\": \"\",                                                                                                                                                                                                                                                                                                                                   " )
    			.append("         \"code_evt\": \"TA\",                                                                                                                                                                                                                                                                                                                                               " )
    			.append("         \"code_evt_ext\": null,                                                                                                                                                                                                                                                                                                                                           " )
    			.append("         \"code_pays_destinataire\": \"FR\",                                                                                                                                                                                                                                                                                                                               " )
    			.append("         \"code_pays_expediteur\": \"FR\",                                                                                                                                                                                                                                                                                                                                 " )
    			.append("         \"code_pays_num_destinataire\": \"250\",                                                                                                                                                                                                                                                                                                                          " )
    			.append("         \"code_pays_num_expediteur\": \"\",                                                                                                                                                                                                                                                                                                                               " )
    			.append("         \"code_point_relais\": \"\",                                                                                                                                                                                                                                                                                                                                      " )
    			.append("         \"code_postal_destinataire\": \"65420\",                                                                                                                                                                                                                                                                                                                          " )
    			.append("         \"code_postal_evt\": null,                                                                                                                                                                                                                                                                                                                                        " )
    			.append("         \"code_postal_expediteur\": \"26740\",                                                                                                                                                                                                                                                                                                                            " )
    			.append("         \"code_produit\": \"01\",                                                                                                                                                                                                                                                                                                                                         " )
    			.append("         \"code_raison_evt\": null,                                                                                                                                                                                                                                                                                                                                        " )
    			.append("         \"code_service\": \"226\",                                                                                                                                                                                                                                                                                                                                        " )
    			.append("         \"codes_evt\": [                                                                                                                                                                                                                                                                                                                                                  " )
    			.append("             \"CP\",                                                                                                                                                                                                                                                                                                                                                       " )
    			.append("             \"D\",                                                                                                                                                                                                                                                                                                                                                        " )
    			.append("             \"DC\",                                                                                                                                                                                                                                                                                                                                                       " )
    			.append("             \"I9\",                                                                                                                                                                                                                                                                                                                                                       " )
    			.append("             \"K\",                                                                                                                                                                                                                                                                                                                                                        " )
    			.append("             \"SC\",                                                                                                                                                                                                                                                                                                                                                       " )
    			.append("             \"SD\",                                                                                                                                                                                                                                                                                                                                                       " )
    			.append("             \"TA\",                                                                                                                                                                                                                                                                                                                                                       " )
    			.append("             \"TS\"                                                                                                                                                                                                                                                                                                                                                        " )
    			.append("         ],                                                                                                                                                                                                                                                                                                                                                                " )
    			.append("         \"crbt_rep\": \"\",                                                                                                                                                                                                                                                                                                                                               " )
    			.append("         \"createur_evt\": null,                                                                                                                                                                                                                                                                                                                                           " )
    			.append("         \"date_creation_evt\": null,                                                                                                                                                                                                                                                                                                                                      " )
    			.append("         \"date_depot_lt\": 1430311200000,                                                                                                                                                                                                                                                                                                                                 " )
    			.append("         \"date_depot_lt_intern\": null,                                                                                                                                                                                                                                                                                                                                   " )
    			.append("         \"date_entree_si\": 315525600000,                                                                                                                                                                                                                                                                                                                                 " )
    			.append("         \"date_evt\": null,                                                                                                                                                                                                                                                                                                                                               " )
    			.append("         \"heure_evt\": null,                                                                                                                                                                                                                                                                                                                                              " )
    			.append("         \"date_heure_saisie\": null,                                                                                                                                                                                                                                                                                                                                      " )
    			.append("         \"date_livraison_contractuelle\": null,                                                                                                                                                                                                                                                                                                                           " )
    			.append("         \"date_livraison_prevue\": null,                                                                                                                                                                                                                                                                                                                                  " )
    			.append("         \"date_modification\": null,                                                                                                                                                                                                                                                                                                                                      " )
    			.append("         \"depot_expediteur\": \"\",                                                                                                                                                                                                                                                                                                                                       " )
    			.append("         \"description\": \"\",                                                                                                                                                                                                                                                                                                                                            " )
    			.append("         \"destination_id_fedex\": \"\",                                                                                                                                                                                                                                                                                                                                   " )
    			.append("         \"devise_assurance\": \"EUR\",                                                                                                                                                                                                                                                                                                                                    " )
    			.append("         \"devise_rep\": \"EUR\",                                                                                                                                                                                                                                                                                                                                          " )
    			.append("         \"devise_val_declaree\": \"000000000000000\",                                                                                                                                                                                                                                                                                                                     " )
    			.append("         \"doc_march\": \"M\",                                                                                                                                                                                                                                                                                                                                             " )
    			.append("         \"email_1_destinataire\": \"\",                                                                                                                                                                                                                                                                                                                                   " )
    			.append("         \"email_1_expediteur\": \"\",                                                                                                                                                                                                                                                                                                                                     " )
    			.append("         \"email_2_destinataire\": \"\",                                                                                                                                                                                                                                                                                                                                   " )
    			.append("         \"email_2_expediteur\": \"\",                                                                                                                                                                                                                                                                                                                                     " )
    			.append("         \"esd\": \" \",                                                                                                                                                                                                                                                                                                                                                   " )
    			.append("         \"hauteur\": 0,                                                                                                                                                                                                                                                                                                                                                   " )
    			.append("         \"id_abonnement\": \"\",                                                                                                                                                                                                                                                                                                                                          " )
    			.append("         \"id_acces_client\": 0,                                                                                                                                                                                                                                                                                                                                           " )
    			.append("         \"id_appli\": \"AFAC\",                                                                                                                                                                                                                                                                                                                                           " )
    			.append("         \"id_colis_client\": \" \",                                                                                                                                                                                                                                                                                                                                       " )
    			.append("         \"id_extraction_evt\": null,                                                                                                                                                                                                                                                                                                                                      " )
    			.append("         \"id_ligne\": \"860984716\",                                                                                                                                                                                                                                                                                                                                      " )
    			.append("         \"id_ss_code_evt\": 0,                                                                                                                                                                                                                                                                                                                                            " )
    			.append("         \"idbco_evt\": 0,                                                                                                                                                                                                                                                                                                                                                 " )
    			.append("         \"infoscomp\": {},                                                                                                                                                                                                                                                                                                                                                " )
    			.append("         \"jour_livraison\": \"N\",                                                                                                                                                                                                                                                                                                                                        " )
    			.append("         \"largeur\": 0,                                                                                                                                                                                                                                                                                                                                                   " )
    			.append("         \"libelle_evt\": null,                                                                                                                                                                                                                                                                                                                                            " )
    			.append("         \"libelle_lieu_evt\": null,                                                                                                                                                                                                                                                                                                                                       " )
    			.append("         \"lieu_evt\": null,                                                                                                                                                                                                                                                                                                                                               " )
    			.append("         \"longueur\": 0,                                                                                                                                                                                                                                                                                                                                                  " )
    			.append("         \"no_contrat\": \"54057001\",                                                                                                                                                                                                                                                                                                                                     " )
    			.append("         \"no_ss_compte\": \"000\",                                                                                                                                                                                                                                                                                                                                        " )
    			.append("         \"nom_1_destinataire\": \"LECLERC  IBOS\",                                                                                                                                                                                                                                                                                                                        " )
    			.append("         \"nom_1_expediteur\": \"RMA\",                                                                                                                                                                                                                                                                                                                                    " )
    			.append("         \"nom_2_destinataire\": \"\",                                                                                                                                                                                                                                                                                                                                     " )
    			.append("         \"nom_2_expediteur\": \"RMA\",                                                                                                                                                                                                                                                                                                                                    " )
    			.append("         \"origine_saisie\": \"\",                                                                                                                                                                                                                                                                                                                                         " )
    			.append("         \"poids\": 1600,                                                                                                                                                                                                                                                                                                                                                  " )
    			.append("         \"position_evt\": 0,                                                                                                                                                                                                                                                                                                                                              " )
    			.append("         \"priorite_evt\": 0,                                                                                                                                                                                                                                                                                                                                              " )
    			.append("         \"prod_cab_evt_saisi\": 0,                                                                                                                                                                                                                                                                                                                                        " )
    			.append("         \"prod_no_lt\": 0,                                                                                                                                                                                                                                                                                                                                                " )
    			.append("         \"ref_abonnement\": \"\",                                                                                                                                                                                                                                                                                                                                         " )
    			.append("         \"ref_destinataire\": \"LECLERC654\",                                                                                                                                                                                                                                                                                                                             " )
    			.append("         \"ref_expediteur\": \"LASCAD\",                                                                                                                                                                                                                                                                                                                                   " )
    			.append("         \"ref_extraction\": null,                                                                                                                                                                                                                                                                                                                                         " )
    			.append("         \"ref_id_abonnement\": null,                                                                                                                                                                                                                                                                                                                                      " )
    			.append("         \"ss_code_evt\": null,                                                                                                                                                                                                                                                                                                                                            " )
    			.append("         \"status_envoi\": null,                                                                                                                                                                                                                                                                                                                                           " )
    			.append("         \"status_evt\": null,                                                                                                                                                                                                                                                                                                                                             " )
    			.append("         \"telephone_destinataire\": \"00.00.00.00.00\",                                                                                                                                                                                                                                                                                                                   " )
    			.append("         \"telephone_expediteur\": \"04 75 46 46 53\",                                                                                                                                                                                                                                                                                                                     " )
    			.append("         \"val_declaree\": \"000000000000000\",                                                                                                                                                                                                                                                                                                                            " )
    			.append("         \"valeur_assuree\": \"000000000000000\",                                                                                                                                                                                                                                                                                                                          " )
    			.append("         \"valeur_rep\": \"000000000000000\",                                                                                                                                                                                                                                                                                                                              " )
    			.append("         \"ville_destinataire\": \"IBOS\",                                                                                                                                                                                                                                                                                                                                 " )
    			.append("         \"ville_expediteur\": \"SAUZET\",                                                                                                                                                                                                                                                                                                                                 " )
    			.append("         \"date_evt_readable\": null,                                                                                                                                                                                                                                                                                                                                      " )
    			.append("         \"eta\": \"09:00\",                                                                                                                                                                                                                                                                                                                                               " )
    			.append("         \"etaMax\": null,                                                                                                                                                                                                                                                                                                                                                 " )
    			.append("         \"position_c11\": \"001\",                                                                                                                                                                                                                                                                                                                                        " )
    			.append("         \"positionTournee\": 0,                                                                                                                                                                                                                                                                                                                                           " )
    			.append("         \"creneauChargeur\": null,                                                                                                                                                                                                                                                                                                                                        " )
    			.append("         \"creneauTournee\": null,                                                                                                                                                                                                                                                                                                                                         " )
    			.append("         \"latitudePrevue\": null,                                                                                                                                                                                                                                                                                                                                         " )
    			.append("         \"longitudePrevue\": null,                                                                                                                                                                                                                                                                                                                                        " )
    			.append("         \"latitudeDistri\": null,                                                                                                                                                                                                                                                                                                                                         " )
    			.append("         \"longitudeDistri\": null,                                                                                                                                                                                                                                                                                                                                        " )
    			.append("         \"evts\": [                                                                                                                                                                                                                                                                                                                                                       " )
    			.append("             \"000145|2015-04-29 18:37:00|XY992227688FR||XY992227688FR|SC|||||SACAP07|2015-04-29T19:24:00|0|862725080||6|Tri effectué dans l''agence de départ||26999|||0||EVT_CHR|26PUF||Acheminement en cours|{'':''}\",                                                                                                                                               " )
    			.append("             \"000145|2015-04-29 21:51:00|XY992227688FR||XY992227688FR|TS|||||SACA53|2015-04-29T22:01:47|0|863328806||87|Envoi en transit||69999|||0||EVT_CHR|69PUF||Acheminement en cours|{'':''}\",                                                                                                                                                                    " )
    			.append("             \"000145|2015-04-30 06:19:00|XY992227688FR||%0065420XY992227688248226250|SD|||||DISA31|2015-04-30T06:28:48|0|864525864||7|Tri effectué dans l''agence de distribution||65999|||0||EVT_CHR|65S40||En préparation pour livraison|{'':''}\",                                                                                                                   " )
    			.append("             \"000145|2015-04-30 08:33:00|XY992227688FR||%0065420XY992227688248226250|TA|||||PSMD527|2015-04-30T09:17:28|0|865583734||35|Envoi en cours de livraison||65999|||0||EVT_CHR|65S42||En cours de livraison|{'64':'20150430-0900','45':'4','193':'PUF65S4200130042015085618','240':'09:00','197':'0','254':'9','203':'65S4230042015085618'}\"                  " )
    			.append("         ],                                                                                                                                                                                                                                                                                                                                                                " )
    			.append("         \"retardEta\": 0,                                                                                                                                                                                                                                                                                                                                                 " )
    			.append("         \"evenements\": null,                                                                                                                                                                                                                                                                                                                                             " )
    			.append("         \"synonymes\": [],                                                                                                                                                                                                                                                                                                                                                " )
    			.append("         \"heureMaxLivraison\": null,                                                                                                                                                                                                                                                                                                                                      " )
    			.append("         \"creneauTourneeRecopie\": false,                                                                                                                                                                                                                                                                                                                                 " )
    			.append("         \"synonyme_maitre\": null                                                                                                                                                                                                                                                                                                                                         " )
    			.append("     }                                                                                                                                                                                                                                                                                                                                                                     " )    			
    			.append(" }").toString();
    }

}
