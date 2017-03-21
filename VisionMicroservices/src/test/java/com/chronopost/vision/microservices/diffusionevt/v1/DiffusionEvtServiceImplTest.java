package com.chronopost.vision.microservices.diffusionevt.v1;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.concurrent.ExecutionException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.NamingException;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.chronopost.vision.jms.ITibcoEmsSender;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.rules.LtRules;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DiffusionEvtServiceImplTest {

	@SuppressWarnings("unchecked")
	@Test
	public void DiffusionEvtServiceImpl() throws NumberFormatException, JsonParseException, JsonMappingException, ParseException, IOException, JMSException, NamingException, InterruptedException, ExecutionException {
		ObjectMapper mapper = new ObjectMapper();
		ITibcoEmsSender mockEms = Mockito.mock(ITibcoEmsSender.class);		
		
		DiffusionEvtServiceImpl.getInstance().setEmsSender(mockEms);
		DiffusionEvtServiceImpl.getInstance().setTopicDestination("sample");
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
		
		DiffusionEvtServiceImpl.getInstance().diffusionEvt(Arrays.asList(lt));
		
		
		// Vérification de l'appel des méthodes du mock
		Mockito.verify(mockEms, Mockito.times(1)).sendMessage(Mockito.contains(mapper.setSerializationInclusion(Include.NON_NULL).writeValueAsString(lt)), Mockito.anyMap(), Mockito.any(Destination.class));
		Mockito.verify(mockEms, Mockito.times(1)).getTopicDestination(Mockito.matches("sample"));		
	}
}
