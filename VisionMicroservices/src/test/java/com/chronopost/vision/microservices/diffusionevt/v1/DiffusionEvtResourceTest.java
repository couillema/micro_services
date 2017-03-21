package com.chronopost.vision.microservices.diffusionevt.v1;

import static org.testng.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedHashSet;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.rules.LtRules;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DiffusionEvtResourceTest extends JerseyTestNg.ContainerPerClassTest  {

	private DiffusionEvtResource resourceDiffusionEvtTest;
	private IDiffusionEvtService serviceMock;
	private Client client;

	/**
	 * Binding the service to the instantiation of the resource
	 */
	@Override
	protected Application configure(){
		resourceDiffusionEvtTest = new DiffusionEvtResource();		
		serviceMock = Mockito.mock(IDiffusionEvtService.class);
		resourceDiffusionEvtTest.setService(serviceMock);
		
		forceSet(TestProperties.CONTAINER_PORT, "0");
		
		ResourceConfig config = new ResourceConfig();
		config.register(resourceDiffusionEvtTest);
		return config;
	}
	
	@BeforeClass
	public void setUp() throws Exception {
		super.setUp();
		client = ClientBuilder.newClient();
	}
	
	@SuppressWarnings("unchecked")
	@Test
    public void testDiffusionEvtOk() throws Exception {
		Mockito.reset(serviceMock);
		Mockito.when(serviceMock.diffusionEvt(Mockito.anyList())).thenReturn(Boolean.TRUE);
		LinkedHashSet<String> evts = new LinkedHashSet<String>();
		evts.add("000117|2015-03-18 15:03:00|NA146848396FR|||DC|||||APRO|2015-03-18T17:07:04|0|715725386||2|Envoi prêt chez l''expéditeur||NA516|||0||EVT_CHR|EDI||Préparation expéditeur|{'119':'899','94':'1/2','77':'20090','76':'FR','231':'GEO/NA146848396248S','118':'%0020090NA146848396248899250','115':'01'}");
		  
		Lt lt =  new Lt().setNoLt("EE000000001FR")
		  		.setAdresse1Destinataire("adresse_1_destinataire")
		  		.setAdresse1Expediteur("adresse_1_expediteur")
		  		.setAdresse2Destinataire("adresse_2_destinataire")
		  		.setAdresse2Expediteur("adresse_2_expediteur")
		  		.setArticle1("article_1")
		  		.setCodeEtatDestinataire("code_etat_destinataire")
		  		.setCodeEtatExpediteur("code_etat_expediteur")
		  		.setCodeEvt("code_evt")
		  		.setCodeEvtExt("code_evt_ext")
		  		.setCodePaysDestinataire("code_pays_destinataire")
		  		.setCodePaysExpediteur("code_pays_expediteur")
		  		.setCodePaysNumDestinataire("code_pays_num_destinataire")
		  		.setCodePaysNumExpediteur("code_pays_num_expediteur")
		  		.setCodePointRelais("code_point_relais")
		  		.setCodePostalDestinataire("code_postal_destinataire")
		  		.setCodePostalEvt("code_postal_evt")
		  		.setCodePostalExpediteur("code_postal_expediteur")
		  		.setCodeProduit("code_produit")
		  		.setCodeRaisonEvt("code_raison_evt")
		  		.setCodeService("code_service")
		  		.setCrbtRep("crbt_rep")
		  		.setCreateurEvt("createur_evt")
		  		.setDateCreationEvt("date_creation_evt")
		  		.setDateDepotLt(new Timestamp(0))
		  		.setDateDepotLtIntern(new Timestamp(0))
		  		.setDateEntreeSi(new Timestamp(0))
		  		.setDateHeureSaisie("date_heure_saisie")
		  		.setDateLivraisonContractuelle(new Timestamp(0))
		  		.setDateLivraisonPrevue(new Timestamp(0))
		  		.setDateModification(new Timestamp(0))
		  		.setDepotExpediteur("depot_expediteur")
		  		.setDescription("description")
		  		.setDestinationIdFedex("destination_id_fedex")
		  		.setDeviseAssurance("devise_assurance")
		  		.setDeviseRep("devise_rep")
		  		.setDeviseValDeclaree("devise_val_declaree")
		  		.setDocMarch("doc_march")
		  		.setEmail1Destinataire("email_1_destinataire")
		  		.setEmail1Expediteur("email_1_expediteur")
		  		.setEmail2Destinataire("email_2_destinataire")
		  		.setEmail2Expediteur("email_2_expediteur")
		  		.setEsd("esd")
		  		.setHauteur(0)
		  		.setIdAbonnement("id_abonnement")
		  		.setIdAccesClient(0)
		  		.setIdAppli("id_appli")
		  		.setIdColisClient("id_colis_client")
		  		.setIdExtractionEvt("id_extraction_evt")
		  		.setIdLigne("id_ligne")
		  		.setIdSsCodeEvt(0)
		  		.setLargeur(0)
		  		.setNoContrat("no_contrat")
		  		.setNoSsCompte("no_ss_compte")
		  		.setNom1Destinataire("nom_1_destinataire")
		  		.setNom1Expediteur("nom_1_expediteur")
		  		.setNom2Destinataire("nom_2_destinataire")
		  		.setNom2Expediteur("nom_2_expediteur")
		  		.setOrigineSaisie("origine_saisie")
		  		.setPoids(0)
		  		.setRefAbonnement("ref_abonnement")
		  		.setRefDestinataire("ref_destinataire")
		  		.setRefExpediteur("ref_expediteur")
		  		.setRefExtraction("ref_extraction")
		  		.setRefIdAbonnement("ref_id_abonnement")
		  		.setStatusEnvoi("status_envoi")
		  		.setTelephoneDestinataire("telephone_destinataire")
		  		.setTelephoneExpediteur("telephone_expediteur")
		  		.setValDeclaree("val_declaree")
		  		.setValeurAssuree("valeur_assuree")
		  		.setValeurRep("valeur_rep")
		  		.setVilleDestinataire("ville_destinataire")
		  		.setVilleExpediteur("ville_expediteur") 
		  		.setEta("18:00")
		  		.setPositionC11("position_c11")
		  		.setCreneauChargeurEtCreneauTournee("08:00-18:00", "08:00-18:00")
		  		.setLatitudePrevue("latitude_prevue")
		  		.setLongitudePrevue("longitude_prevue")
		  		.setLatitudeDistri("latitude_distri")
		  		.setLongitudeDistri("longitude_distri")
		  		.setEvts(evts)
		  		.setSynonymeMaitre("EE000000001FR");
		
		lt = LtRules.applyRulesToLt(lt);
        
        int status = client.target("http://localhost:" + getPort()).path("/DiffusionEvt/v1/").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(new ObjectMapper().writeValueAsString(Arrays.asList(lt)),
						MediaType.APPLICATION_JSON)).getStatus();
        
        assertEquals(status, 200);
	}
	
	@SuppressWarnings("unchecked")
	@Test
    public void testDiffusionEvtKo() throws Exception {
		Mockito.reset(serviceMock);
		Mockito.when(serviceMock.diffusionEvt(Mockito.anyList())).thenThrow(new MSTechnicalException("grosse cata..."));
		LinkedHashSet<String> evts = new LinkedHashSet<String>();
		
		evts.add("000117|2015-03-18 15:03:00|NA146848396FR|||DC|||||APRO|2015-03-18T17:07:04|0|715725386||2|Envoi prêt chez l''expéditeur||NA516|||0||EVT_CHR|EDI||Préparation expéditeur|{'119':'899','94':'1/2','77':'20090','76':'FR','231':'GEO/NA146848396248S','118':'%0020090NA146848396248899250','115':'01'}");
		  
		Lt lt =  new Lt().setNoLt("EE000000001FR")
		  		.setAdresse1Destinataire("adresse_1_destinataire")
		  		.setAdresse1Expediteur("adresse_1_expediteur")
		  		.setAdresse2Destinataire("adresse_2_destinataire")
		  		.setAdresse2Expediteur("adresse_2_expediteur")
		  		.setArticle1("article_1")
		  		.setCodeEtatDestinataire("code_etat_destinataire")
		  		.setCodeEtatExpediteur("code_etat_expediteur")
		  		.setCodeEvt("code_evt")
		  		.setCodeEvtExt("code_evt_ext")
		  		.setCodePaysDestinataire("code_pays_destinataire")
		  		.setCodePaysExpediteur("code_pays_expediteur")
		  		.setCodePaysNumDestinataire("code_pays_num_destinataire")
		  		.setCodePaysNumExpediteur("code_pays_num_expediteur")
		  		.setCodePointRelais("code_point_relais")
		  		.setCodePostalDestinataire("code_postal_destinataire")
		  		.setCodePostalEvt("code_postal_evt")
		  		.setCodePostalExpediteur("code_postal_expediteur")
		  		.setCodeProduit("code_produit")
		  		.setCodeRaisonEvt("code_raison_evt")
		  		.setCodeService("code_service")
		  		.setCrbtRep("crbt_rep")
		  		.setCreateurEvt("createur_evt")
		  		.setDateCreationEvt("date_creation_evt")
		  		.setDateDepotLt(new Timestamp(0))
		  		.setDateDepotLtIntern(new Timestamp(0))
		  		.setDateEntreeSi(new Timestamp(0))
		  		.setDateHeureSaisie("date_heure_saisie")
		  		.setDateLivraisonContractuelle(new Timestamp(0))
		  		.setDateLivraisonPrevue(new Timestamp(0))
		  		.setDateModification(new Timestamp(0))
		  		.setDepotExpediteur("depot_expediteur")
		  		.setDescription("description")
		  		.setDestinationIdFedex("destination_id_fedex")
		  		.setDeviseAssurance("devise_assurance")
		  		.setDeviseRep("devise_rep")
		  		.setDeviseValDeclaree("devise_val_declaree")
		  		.setDocMarch("doc_march")
		  		.setEmail1Destinataire("email_1_destinataire")
		  		.setEmail1Expediteur("email_1_expediteur")
		  		.setEmail2Destinataire("email_2_destinataire")
		  		.setEmail2Expediteur("email_2_expediteur")
		  		.setEsd("esd")
		  		.setHauteur(0)
		  		.setIdAbonnement("id_abonnement")
		  		.setIdAccesClient(0)
		  		.setIdAppli("id_appli")
		  		.setIdColisClient("id_colis_client")
		  		.setIdExtractionEvt("id_extraction_evt")
		  		.setIdLigne("id_ligne")
		  		.setIdSsCodeEvt(0)
		  		.setLargeur(0)
		  		.setNoContrat("no_contrat")
		  		.setNoSsCompte("no_ss_compte")
		  		.setNom1Destinataire("nom_1_destinataire")
		  		.setNom1Expediteur("nom_1_expediteur")
		  		.setNom2Destinataire("nom_2_destinataire")
		  		.setNom2Expediteur("nom_2_expediteur")
		  		.setOrigineSaisie("origine_saisie")
		  		.setPoids(0)
		  		.setRefAbonnement("ref_abonnement")
		  		.setRefDestinataire("ref_destinataire")
		  		.setRefExpediteur("ref_expediteur")
		  		.setRefExtraction("ref_extraction")
		  		.setRefIdAbonnement("ref_id_abonnement")
		  		.setStatusEnvoi("status_envoi")
		  		.setTelephoneDestinataire("telephone_destinataire")
		  		.setTelephoneExpediteur("telephone_expediteur")
		  		.setValDeclaree("val_declaree")
		  		.setValeurAssuree("valeur_assuree")
		  		.setValeurRep("valeur_rep")
		  		.setVilleDestinataire("ville_destinataire")
		  		.setVilleExpediteur("ville_expediteur") 
		  		.setEta("18:00")
		  		.setPositionC11("position_c11")
		  		.setCreneauChargeurEtCreneauTournee("08:00-18:00", "08:00-18:00")
		  		.setLatitudePrevue("latitude_prevue")
		  		.setLongitudePrevue("longitude_prevue")
		  		.setLatitudeDistri("latitude_distri")
		  		.setLongitudeDistri("longitude_distri")
		  		.setEvts(evts)
		  		.setSynonymeMaitre("EE000000001FR");
		
		lt = LtRules.applyRulesToLt(lt);
        
        int status = client.target("http://localhost:" + getPort()).path("/DiffusionEvt/v1/").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(new ObjectMapper().writeValueAsString(Arrays.asList(lt)),
						MediaType.APPLICATION_JSON)).getStatus();
        
        assertEquals(status, 500);
	}
}
