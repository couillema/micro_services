package com.chronopost.vision.microservices.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testng.annotations.Test;

import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.Lt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/** @author jjean-charles : for changes and test of bug :  hidden dependency of field order when json-serializing.
   ADD some diff utilities and testJSON_LTS() to reset JSON_LT1 and JSON_LT2 if Lt.class is changed. 
   complete missing control of JSON3 and refine tool using CARET '^' for very long string comparison !! **/
public class LtDataMakerTest {

//    /** @see FROM VisionInsertEvtAcceptanceTest.java for test cas1Test1() */
//    public static final String JSON_LT1 = "[{\"no_lt\":\"EEINSEVT001FR\",\"adresse_1_destinataire\":null,\"adresse_1_expediteur\":null,\"adresse_2_destinataire\":null,\"adresse_2_expediteur\":null,\"article_1\":null,\"cab_evt_saisi\":null,\"cab_recu\":null,\"code_etat_destinataire\":null,\"code_etat_expediteur\":null,\"code_evt\":null,\"code_evt_ext\":null,\"code_pays_destinataire\":\"FR\",\"code_pays_expediteur\":null,\"code_pays_num_destinataire\":null,\"code_pays_num_expediteur\":null,\"code_point_relais\":null,\"code_postal_destinataire\":null,\"code_postal_evt\":null,\"code_postal_expediteur\":null,\"code_produit\":null,\"code_raison_evt\":null,\"code_service\":\"899\",\"codes_evt\":[\"TA\"],\"crbt_rep\":null,\"createur_evt\":null,\"date_creation_evt\":null,\"date_depot_lt\":null,\"date_depot_lt_intern\":null,\"date_entree_si\":null,\"date_evt\":null,\"heure_evt\":null,\"date_heure_saisie\":null,\"date_livraison_contractuelle\":\"2015-03-22T10:00:00.000+0100\",\"date_livraison_prevue\":\"2015-03-23T16:06:00.000+0100\",\"date_modification\":null,\"depot_expediteur\":null,\"description\":null,\"destination_id_fedex\":null,\"devise_assurance\":null,\"devise_rep\":null,\"devise_val_declaree\":null,\"doc_march\":null,\"email_1_destinataire\":null,\"email_1_expediteur\":null,\"email_2_destinataire\":null,\"email_2_expediteur\":null,\"esd\":null,\"hauteur\":null,\"id_abonnement\":null,\"id_acces_client\":null,\"id_appli\":null,\"id_colis_client\":null,\"id_extraction_evt\":null,\"id_ligne\":null,\"id_ss_code_evt\":null,\"idbco_evt\":null,\"infoscomp\":null,\"jour_livraison\":null,\"largeur\":null,\"libelle_evt\":null,\"libelle_lieu_evt\":null,\"lieu_evt\":null,\"longueur\":null,\"no_contrat\":null,\"no_ss_compte\":null,\"nom_1_destinataire\":null,\"nom_1_expediteur\":null,\"nom_2_destinataire\":null,\"nom_2_expediteur\":null,\"origine_saisie\":null,\"poids\":null,\"position_evt\":null,\"priorite_evt\":null,\"prod_cab_evt_saisi\":null,\"prod_no_lt\":null,\"ref_abonnement\":null,\"ref_destinataire\":null,\"ref_expediteur\":null,\"ref_extraction\":null,\"ref_id_abonnement\":null,\"ss_code_evt\":null,\"status_envoi\":null,\"status_evt\":null,\"telephone_destinataire\":null,\"telephone_expediteur\":null,\"val_declaree\":null,\"valeur_assuree\":null,\"valeur_rep\":null,\"ville_destinataire\":null,\"ville_expediteur\":null,\"date_evt_readable\":null,\"eta\":\"12:00\",\"etaMax\":null,\"position_c11\":\"002\",\"positionTournee\":0,\"creneauChargeur\":null,\"creneauTournee\":null,\"latitudePrevue\":\"4.55555\",\"longitudePrevue\":\"5.66666\",\"latitudeDistri\":null,\"longitudeDistri\":null,\"evts\":[\"000201|2015-03-18 22:26:00|EEINSEVT001FR|cab_evt_saisi|%0020090NA146848396248899250|TA|toto|13999|code_raison_evt|code_service|TRI1|2015-09-01T22:34:56|0|717493191|1|88|Envoi en transit|libelle_lieu_evt|93999|0|1|1|ref_extraction|EVT_CHR|AJA0|status_envoi|Acheminement en cours|{'190':'4.55555','191':'5.66666','193':'AJA20A0100208092015065959','240':'12:00'}\"],\"retardEta\":0,\"evenements\":null,\"synonymes\":null,\"heureMaxLivraison\":null,\"creneauTourneeRecopie\":false,\"synonyme_maitre\":null,\"idx_depassement\":\"2015-03-22__1\"}]";
//    
//    /** @see FROM VisionInsertEvtAcceptanceTest.java for test cas1Test2() */
//    public static final String JSON_LT2 = "[{\"no_lt\":\"EEINSEVT003FR\",\"adresse_1_destinataire\":null,\"adresse_1_expediteur\":null,\"adresse_2_destinataire\":null,\"adresse_2_expediteur\":null,\"article_1\":null,\"cab_evt_saisi\":null,\"cab_recu\":null,\"code_etat_destinataire\":null,\"code_etat_expediteur\":null,\"code_evt\":null,\"code_evt_ext\":null,\"code_pays_destinataire\":\"FR\",\"code_pays_expediteur\":null,\"code_pays_num_destinataire\":null,\"code_pays_num_expediteur\":null,\"code_point_relais\":null,\"code_postal_destinataire\":null,\"code_postal_evt\":null,\"code_postal_expediteur\":null,\"code_produit\":null,\"code_raison_evt\":null,\"code_service\":\"899\",\"codes_evt\":[\"D\"],\"crbt_rep\":null,\"createur_evt\":null,\"date_creation_evt\":null,\"date_depot_lt\":null,\"date_depot_lt_intern\":null,\"date_entree_si\":null,\"date_evt\":null,\"heure_evt\":null,\"date_heure_saisie\":null,\"date_livraison_contractuelle\":null,\"date_livraison_prevue\":null,\"date_modification\":null,\"depot_expediteur\":null,\"description\":null,\"destination_id_fedex\":null,\"devise_assurance\":null,\"devise_rep\":null,\"devise_val_declaree\":null,\"doc_march\":null,\"email_1_destinataire\":null,\"email_1_expediteur\":null,\"email_2_destinataire\":null,\"email_2_expediteur\":null,\"esd\":null,\"hauteur\":null,\"id_abonnement\":null,\"id_acces_client\":null,\"id_appli\":null,\"id_colis_client\":null,\"id_extraction_evt\":null,\"id_ligne\":null,\"id_ss_code_evt\":null,\"idbco_evt\":null,\"infoscomp\":null,\"jour_livraison\":null,\"largeur\":null,\"libelle_evt\":null,\"libelle_lieu_evt\":null,\"lieu_evt\":null,\"longueur\":null,\"no_contrat\":null,\"no_ss_compte\":null,\"nom_1_destinataire\":null,\"nom_1_expediteur\":null,\"nom_2_destinataire\":null,\"nom_2_expediteur\":null,\"origine_saisie\":null,\"poids\":null,\"position_evt\":null,\"priorite_evt\":null,\"prod_cab_evt_saisi\":null,\"prod_no_lt\":null,\"ref_abonnement\":null,\"ref_destinataire\":null,\"ref_expediteur\":null,\"ref_extraction\":null,\"ref_id_abonnement\":null,\"ss_code_evt\":null,\"status_envoi\":null,\"status_evt\":null,\"telephone_destinataire\":null,\"telephone_expediteur\":null,\"val_declaree\":null,\"valeur_assuree\":null,\"valeur_rep\":null,\"ville_destinataire\":null,\"ville_expediteur\":null,\"date_evt_readable\":null,\"eta\":null,\"etaMax\":null,\"position_c11\":null,\"positionTournee\":0,\"creneauChargeur\":null,\"creneauTournee\":null,\"latitudePrevue\":null,\"longitudePrevue\":null,\"latitudeDistri\":\"4.55555\",\"longitudeDistri\":\"5.66666\",\"evts\":[\"000201|2015-03-18 22:26:00|EEINSEVT003FR|cab_evt_saisi|%0020090NA146848396248899250|D|toto|13999|code_raison_evt|226|TRI1|2015-09-01T22:34:56|0|717493191|1|88|Envoi en transit|libelle_lieu_evt|93999|0|1|1|ref_extraction|EVT_CHR|AJA0|status_envoi|Acheminement en cours|{'190':'4.55555','191':'5.66666','193':'AJA20A0100208092015065959','240':'12:00'}\"],\"retardEta\":0,\"evenements\":null,\"synonymes\":null,\"heureMaxLivraison\":null,\"creneauTourneeRecopie\":false,\"synonyme_maitre\":null,\"idx_depassement\":null}]";

    /** taken from VisionInsertEvtAcceptanceTest */ 
	private static String PATCH_EVT1 = "000201|2015-03-18 22:26:00|EEINSEVT001FR|cab_evt_saisi|%0020090NA146848396248899250|TA|toto|13999|code_raison_evt|code_service|TRI1|2015-09-01T22:34:56|0|717493191|1|88|Envoi en transit|libelle_lieu_evt|93999|0|1|1|ref_extraction|EVT_CHR|AJA0|status_envoi|Acheminement en cours|{'190':'4.55555','191':'5.66666','193':'AJA20A0100208092015065959','240':'12:00'}" ;
    /** taken from VisionInsertEvtAcceptanceTest : 2 differences with PATCH_EVT1 above: 
     *  EEINSEVT001FR ==> EEINSEVT003FR ; TA==>D ;  codeService ==> 226 */ 
	private static String PATCH_EVT2 = "000201|2015-03-18 22:26:00|EEINSEVT003FR|cab_evt_saisi|%0020090NA146848396248899250|D|toto|13999|code_raison_evt|226|TRI1|2015-09-01T22:34:56|0|717493191|1|88|Envoi en transit|libelle_lieu_evt|93999|0|1|1|ref_extraction|EVT_CHR|AJA0|status_envoi|Acheminement en cours|{'190':'4.55555','191':'5.66666','193':'AJA20A0100208092015065959','240':'12:00'}" ;
    /** ADDED FOR JSON_LT3 */
	private static String PATCH_EVT3 = "000189|1970-01-01 01:00:00|EEINSEVT262FR|cab_evt_saisi|%0020090NA146848396248899250|TA|toto|13999|code_raison_evt||TRI1|2015-09-01T22:34:56|0|717493191|1|88|Envoi en transit|libelle_lieu_evt|93999|0|1|1|ref_extraction|EVT_CHR|AJA0|status_envoi|Acheminement en cours|{'190':'4.55555','191':'5.66666','193':'AJA20A0100208092015065959','240':'12:00'}";

	/** Made using testJSON_LTS()  : for VisionInsertEvtAcceptanceTest.cas1Test1() */
	private static final String JSON_LT1 = "[{\"no_lt\":\"EEINSEVT001FR\",\"article_1\":null,\"cab_recu\":null,\"code_point_relais\":null,\"code_produit\":null,\"code_service\":\"899\",\"crbt_rep\":null,\"date_depot_lt\":null,\"date_depot_lt_intern\":null,\"date_entree_si\":null,\"date_heure_saisie\":null,\"date_livraison_contractuelle\":\"2015-03-22T10:00:00.000+0100\",\"date_livraison_prevue\":\"2015-03-23T16:06:00.000+0100\",\"description\":null,\"destination_id_fedex\":null,\"devise_assurance\":null,\"devise_rep\":null,\"devise_val_declaree\":null,\"doc_march\":null,\"esd\":null,\"hauteur\":null,\"id_abonnement\":null,\"id_acces_client\":null,\"id_appli\":null,\"id_colis_client\":null,\"id_ligne\":null,\"infoscomp\":null,\"jour_livraison\":null,\"largeur\":null,\"longueur\":null,\"no_contrat\":null,\"no_ss_compte\":null,\"origine_saisie\":null,\"poids\":null,\"prod_no_lt\":null,\"ref_abonnement\":null,\"ref_extraction\":null,\"ref_id_abonnement\":null,\"status_envoi\":null,\"val_declaree\":null,\"valeur_assuree\":null,\"valeur_rep\":null,\"eta\":\"12:00\",\"etaMax\":null,\"position_c11\":\"002\",\"positionTournee\":0,\"creneauChargeur\":null,\"creneauTournee\":null,\"latitudePrevue\":\"4.55555\",\"longitudePrevue\":\"5.66666\",\"latitudeDistri\":null,\"longitudeDistri\":null,\"evts\":[\"000201|2015-03-18 22:26:00|EEINSEVT001FR|cab_evt_saisi|%0020090NA146848396248899250|TA|toto|13999|code_raison_evt|code_service|TRI1|2015-09-01T22:34:56|0|717493191|1|88|Envoi en transit|libelle_lieu_evt|93999|0|1|1|ref_extraction|EVT_CHR|AJA0|status_envoi|Acheminement en cours|{'190':'4.55555','191':'5.66666','193':'AJA20A0100208092015065959','240':'12:00'}\"],\"retardEta\":0,\"evenements\":null,\"synonymes\":null,\"heureMaxLivraison\":null,\"creneauTourneeRecopie\":false,\"synonyme_maitre\":null,\"idx_depassement\":\"2015-03-22__1\",\"adresse_1_expediteur\":null,\"adresse_2_expediteur\":null,\"code_etat_expediteur\":null,\"code_pays_expediteur\":null,\"code_pays_num_expediteur\":null,\"code_postal_expediteur\":null,\"depot_expediteur\":null,\"email_1_expediteur\":null,\"email_2_expediteur\":null,\"nom_1_expediteur\":null,\"nom_2_expediteur\":null,\"ref_expediteur\":null,\"telephone_expediteur\":null,\"ville_expediteur\":null,\"adresse_1_destinataire\":null,\"adresse_2_destinataire\":null,\"code_etat_destinataire\":null,\"code_pays_destinataire\":\"FR\",\"code_pays_num_destinataire\":null,\"code_postal_destinataire\":null,\"email_1_destinataire\":null,\"email_2_destinataire\":null,\"nom_1_destinataire\":null,\"nom_2_destinataire\":null,\"ref_destinataire\":null,\"telephone_destinataire\":null,\"ville_destinataire\":null,\"cab_evt_saisi\":null,\"code_evt\":null,\"code_evt_ext\":null,\"code_postal_evt\":null,\"code_raison_evt\":null,\"codes_evt\":[\"TA\"],\"createur_evt\":null,\"date_creation_evt\":null,\"date_evt\":null,\"heure_evt\":null,\"id_extraction_evt\":null,\"id_ss_code_evt\":null,\"idbco_evt\":null,\"libelle_evt\":null,\"libelle_lieu_evt\":null,\"lieu_evt\":null,\"position_evt\":null,\"priorite_evt\":null,\"prod_cab_evt_saisi\":null,\"ss_code_evt\":null,\"status_evt\":null,\"date_evt_readable\":null}]" ;
	/** Made using testJSON_LTS()  :  for VisionInsertEvtAcceptanceTest.cas1Test2() */
	private static final String JSON_LT2 = "[{\"no_lt\":\"EEINSEVT003FR\",\"article_1\":null,\"cab_recu\":null,\"code_point_relais\":null,\"code_produit\":null,\"code_service\":\"899\",\"crbt_rep\":null,\"date_depot_lt\":null,\"date_depot_lt_intern\":null,\"date_entree_si\":null,\"date_heure_saisie\":null,\"date_livraison_contractuelle\":null,\"date_livraison_prevue\":null,\"description\":null,\"destination_id_fedex\":null,\"devise_assurance\":null,\"devise_rep\":null,\"devise_val_declaree\":null,\"doc_march\":null,\"esd\":null,\"hauteur\":null,\"id_abonnement\":null,\"id_acces_client\":null,\"id_appli\":null,\"id_colis_client\":null,\"id_ligne\":null,\"infoscomp\":null,\"jour_livraison\":null,\"largeur\":null,\"longueur\":null,\"no_contrat\":null,\"no_ss_compte\":null,\"origine_saisie\":null,\"poids\":null,\"prod_no_lt\":null,\"ref_abonnement\":null,\"ref_extraction\":null,\"ref_id_abonnement\":null,\"status_envoi\":null,\"val_declaree\":null,\"valeur_assuree\":null,\"valeur_rep\":null,\"eta\":null,\"etaMax\":null,\"position_c11\":null,\"positionTournee\":0,\"creneauChargeur\":null,\"creneauTournee\":null,\"latitudePrevue\":null,\"longitudePrevue\":null,\"latitudeDistri\":\"4.55555\",\"longitudeDistri\":\"5.66666\",\"evts\":[\"000201|2015-03-18 22:26:00|EEINSEVT003FR|cab_evt_saisi|%0020090NA146848396248899250|D|toto|13999|code_raison_evt|226|TRI1|2015-09-01T22:34:56|0|717493191|1|88|Envoi en transit|libelle_lieu_evt|93999|0|1|1|ref_extraction|EVT_CHR|AJA0|status_envoi|Acheminement en cours|{'190':'4.55555','191':'5.66666','193':'AJA20A0100208092015065959','240':'12:00'}\"],\"retardEta\":0,\"evenements\":null,\"synonymes\":null,\"heureMaxLivraison\":null,\"creneauTourneeRecopie\":false,\"synonyme_maitre\":null,\"idx_depassement\":null,\"adresse_1_expediteur\":null,\"adresse_2_expediteur\":null,\"code_etat_expediteur\":null,\"code_pays_expediteur\":null,\"code_pays_num_expediteur\":null,\"code_postal_expediteur\":null,\"depot_expediteur\":null,\"email_1_expediteur\":null,\"email_2_expediteur\":null,\"nom_1_expediteur\":null,\"nom_2_expediteur\":null,\"ref_expediteur\":null,\"telephone_expediteur\":null,\"ville_expediteur\":null,\"adresse_1_destinataire\":null,\"adresse_2_destinataire\":null,\"code_etat_destinataire\":null,\"code_pays_destinataire\":\"FR\",\"code_pays_num_destinataire\":null,\"code_postal_destinataire\":null,\"email_1_destinataire\":null,\"email_2_destinataire\":null,\"nom_1_destinataire\":null,\"nom_2_destinataire\":null,\"ref_destinataire\":null,\"telephone_destinataire\":null,\"ville_destinataire\":null,\"cab_evt_saisi\":null,\"code_evt\":null,\"code_evt_ext\":null,\"code_postal_evt\":null,\"code_raison_evt\":null,\"codes_evt\":[\"D\"],\"createur_evt\":null,\"date_creation_evt\":null,\"date_evt\":null,\"heure_evt\":null,\"id_extraction_evt\":null,\"id_ss_code_evt\":null,\"idbco_evt\":null,\"libelle_evt\":null,\"libelle_lieu_evt\":null,\"lieu_evt\":null,\"position_evt\":null,\"priorite_evt\":null,\"prod_cab_evt_saisi\":null,\"ss_code_evt\":null,\"status_evt\":null,\"date_evt_readable\":null}]" ;
	/** VisionInsertEvtAcceptanceTest.coli262Test() */
	private static final String JSON_LT3 = "[{\"no_lt\":\"EEINSEVT262FR\",\"article_1\":null,\"cab_recu\":null,\"code_point_relais\":null,\"code_produit\":null,\"code_service\":\"899\",\"crbt_rep\":null,\"date_depot_lt\":null,\"date_depot_lt_intern\":null,\"date_entree_si\":null,\"date_heure_saisie\":null,\"date_livraison_contractuelle\":\"2015-03-22T10:00:00.000+0100\",\"date_livraison_prevue\":null,\"description\":null,\"destination_id_fedex\":null,\"devise_assurance\":null,\"devise_rep\":null,\"devise_val_declaree\":null,\"doc_march\":null,\"esd\":null,\"hauteur\":null,\"id_abonnement\":null,\"id_acces_client\":null,\"id_appli\":null,\"id_colis_client\":null,\"id_ligne\":null,\"infoscomp\":null,\"jour_livraison\":null,\"largeur\":null,\"longueur\":null,\"no_contrat\":null,\"no_ss_compte\":null,\"origine_saisie\":null,\"poids\":null,\"prod_no_lt\":null,\"ref_abonnement\":null,\"ref_extraction\":null,\"ref_id_abonnement\":null,\"status_envoi\":null,\"val_declaree\":null,\"valeur_assuree\":null,\"valeur_rep\":null,\"eta\":\"12:00\",\"etaMax\":null,\"position_c11\":\"002\",\"positionTournee\":0,\"creneauChargeur\":null,\"creneauTournee\":null,\"latitudePrevue\":\"4.55555\",\"longitudePrevue\":\"5.66666\",\"latitudeDistri\":null,\"longitudeDistri\":null,\"evts\":[\"000189|1970-01-01 01:00:00|EEINSEVT262FR|cab_evt_saisi|%0020090NA146848396248899250|TA|toto|13999|code_raison_evt||TRI1|2015-09-01T22:34:56|0|717493191|1|88|Envoi en transit|libelle_lieu_evt|93999|0|1|1|ref_extraction|EVT_CHR|AJA0|status_envoi|Acheminement en cours|{'190':'4.55555','191':'5.66666','193':'AJA20A0100208092015065959','240':'12:00'}\"],\"retardEta\":0,\"evenements\":null,\"synonymes\":null,\"heureMaxLivraison\":null,\"creneauTourneeRecopie\":false,\"synonyme_maitre\":null,\"idx_depassement\":\"2015-03-22__1\",\"adresse_1_expediteur\":null,\"adresse_2_expediteur\":null,\"code_etat_expediteur\":null,\"code_pays_expediteur\":null,\"code_pays_num_expediteur\":null,\"code_postal_expediteur\":null,\"depot_expediteur\":null,\"email_1_expediteur\":null,\"email_2_expediteur\":null,\"nom_1_expediteur\":null,\"nom_2_expediteur\":null,\"ref_expediteur\":null,\"telephone_expediteur\":null,\"ville_expediteur\":null,\"adresse_1_destinataire\":null,\"adresse_2_destinataire\":null,\"code_etat_destinataire\":null,\"code_pays_destinataire\":\"FR\",\"code_pays_num_destinataire\":null,\"code_postal_destinataire\":null,\"email_1_destinataire\":null,\"email_2_destinataire\":null,\"nom_1_destinataire\":null,\"nom_2_destinataire\":null,\"ref_destinataire\":null,\"telephone_destinataire\":null,\"ville_destinataire\":null,\"cab_evt_saisi\":null,\"code_evt\":null,\"code_evt_ext\":null,\"code_postal_evt\":null,\"code_raison_evt\":null,\"codes_evt\":[\"TA\"],\"createur_evt\":null,\"date_creation_evt\":null,\"date_evt\":null,\"heure_evt\":null,\"id_extraction_evt\":null,\"id_ss_code_evt\":null,\"idbco_evt\":null,\"libelle_evt\":null,\"libelle_lieu_evt\":null,\"lieu_evt\":null,\"position_evt\":null,\"priorite_evt\":null,\"prod_cab_evt_saisi\":null,\"ss_code_evt\":null,\"status_evt\":null,\"date_evt_readable\":null}]" ;

    /** Avoids instantiation. V3 */
	public LtDataMakerTest() {} 
  
	/** @author jjean-charles	compare JSON LT generated **/
	private final class JsonCmp { 
		/** Functional caller .*/
		private final String caller;
		/** Input stored as is.*/
		private final Lt lt ;
		/** One of JSON_LTxx. */
		private final String orig ;
		private final  String[] toRemove ; 
		/** made by JSON ObjectMapper().writeValueAsString(lt) **/
		private final String made ; 
		
		/** @param aCaller LIKE "1" "2" "3" 
		 *  @param aLt not null
		 *  @param aToRemove empty eventually
		 *  @param aOrig like	JSON_LT1 , JSON_LT2 , JSON_LT3 */
		public JsonCmp(final String aCaller , final Lt aLt , final String[] aToRemove , final String aOrig ) { 
			this.caller = aCaller ; 
			this.lt = aLt; 
			this.orig = aOrig; 
			this.toRemove = aToRemove;
			String ret ; 
			try {		
				 ret =  "["  + new ObjectMapper().writeValueAsString(lt) +  "]" ;
				 for ( String cur : toRemove ) ret = ret.replaceFirst(cur,"") ;
				 //ret = ret.replaceFirst("positionTournee\":null" , "positionTournee\":0" ) ; // PATCH !!
			 } catch (JsonProcessingException e) { 		e.printStackTrace(); ret= null; 	}
			this.made = ret;
		}
		
		/** @return if made is same as compare if false print the necessary code to use.  */
		public boolean checkCMP() { 
			if ( made.compareTo(orig) == 0 ) return true ; 
			showDiffBigStr(caller,made,orig); 
			// Now we show the expected code like public static final String JSON_LT1 .... 
			final String msgErrAdd = "********* PLEASE ADD THE LINE OF CODE BELOW --> : \n" ;
			System.out.println(" JSON"+ caller +": " + msgErrAdd + " public static final String JSON_LT" + caller + "= \"" + escapeQuote(made)+ "\" ;" ); 
			return false ; 
		}
		/** @param in not null 
		 * @return 	in with each double quotes '"' replaced by '\"'  */
		final String escapeQuote(final String in ) { return in.replace("\"","\\\"") ; }  

		/** @throws JSONException on call of JSONAssert.assertEquals(orig, made, JSONCompareMode.LENIENT ); 	 */
		public void checkAssertLenient() throws JSONException { 	JSONAssert.assertEquals(orig, made, JSONCompareMode.LENIENT );	}
		
	} // EOC JsonCmp 

	/** @param caller functional 
	 *  @param made as it says 
	 *  @param orig compare with made : if different will show lines in parallel whith a third line having a CARET for first and last differences. 	 */
	public static final void showDiffBigStr(final String caller , final String made,final String orig ) {
		final int lgMin =  Math.min(made.length(), orig.length() ) ;
		String caret ="" ;
		String diffStr = "NO_DIFF" ;
		int idxFirstDiff = -1 ; 
		for ( int i = 0 ; i < lgMin ; i++)  {
			if (made.charAt(i) ==orig.charAt(i) ) { caret +=" "; continue; }
			diffStr = " DIFF"+caller+" at char " + i ;
			caret +="^" + diffStr + " CARET ZZZZZZZ"; 
			idxFirstDiff = i ; 
			break; 
		}
		if ( idxFirstDiff != -1 ) { // add a final message for euqals end !!  
			int posMade = lgMin ; 
			for ( int i = 1 ; i < lgMin ; i++)  {
				posMade = made.length()-i;
				if (made.charAt(posMade) !=orig.charAt(orig.length()-i) ) break; 
			}
			String spaceAdded = "." ;   
			for ( int i = 0 ; i < posMade - caret.length() ; i++ ) spaceAdded += " ";  
			caret += spaceAdded + " ^--> OK ALL after char " + posMade ;
		}
		System.out.println(" ORIG"+caller+ " lg=" + orig.length() + " NEW lg=" + made.length() + " lgMin=" + lgMin + " DF at " + diffStr); 
		System.out.println(" ORIG"+caller+ " =" + orig ) ;   
		System.out.println("  NEW"+caller+ " =" + made ) ;
		System.out.println(" CAR" +caller+ "=" + caret ) ;
	} // eof showDiffBigStr

	/** Will test the constant string used in JSON_LT* and print the string for cut and paste here. 
	 * @throws JSONException */ @Test 
	public void testJSON_LTS() throws JSONException {
		final String[] toRemove = new String[] { ",\"date_modification\":null"} ; 
		final JsonCmp[] tests = new JsonCmp[]	{ new JsonCmp("1",myjackLt1(),toRemove	,JSON_LT1)
												, new JsonCmp("2",myjackLt2(),toRemove	,JSON_LT2)
												, new JsonCmp("3",myjackLt3(),toRemove	,JSON_LT3)
												} ;
		boolean isOk = true ; 
		for ( JsonCmp cur  : tests  )  isOk &= cur.checkCMP() ;
		if ( isOk == false ) System.out.println(" JSON SOMME DIFF !!"); 

		for ( JsonCmp cur  : tests  )  cur.checkAssertLenient(); 
	}

	/** @return make as in com.chronopost.vision.microservices.insertevt.v1.VisionInsertEvtAcceptanceTest orig was JSON_LT1 */
	private static Lt  myjackLt1() {   		
		Lt  lt1 = new Lt(); 
		lt1.setNoLt("EEINSEVT001FR");
		lt1.setCodePaysDestinataire("FR");
		lt1.setCodeService("899") ; 		
		lt1.setEvts(Sets.newHashSet(PATCH_EVT1)); 
		
		lt1.setLatitudePrevue("4.55555");
		lt1.setLongitudePrevue("5.66666");
		lt1.setDateLivraisonContractuelle	(makeTimeStamp("2015-03-22T10:00:00.000+0100")); 
		lt1.setDateLivraisonPrevue		(makeTimeStamp("2015-03-23T16:06:00.000+0100")); 
		lt1.setEta("12:00");
		lt1.setPositionC11("002") ;
		lt1.setIdxDepassement("2015-03-22__1");
		lt1.setCodesEvt(Sets.newHashSet("TA")); 
		return lt1;
	}

	/** @return make as in com.chronopost.vision.microservices.insertevt.v1.VisionInsertEvtAcceptanceTest orig was JSON_LT2 */
	private static Lt  myjackLt2() {   		
		Lt  lt1 = new Lt(); 
		lt1.setNoLt("EEINSEVT003FR");
		lt1.setCodePaysDestinataire("FR");
		lt1.setCodeService("899") ; 		
		lt1.setEvts(Sets.newHashSet(PATCH_EVT2)); 
		
		lt1.setLatitudeDistri("4.55555");
		lt1.setLongitudeDistri("5.66666");
		lt1.setCodesEvt(Sets.newHashSet("D"));
		return lt1;
	}
	
	/** @return same as myjackLt1() EXCEPT :  numeroLT , PATH_EVT3 and DateLivraisonPrevue at null for JSON_LT3 */
	private static Lt  myjackLt3() {
		Lt  lt1 =  myjackLt1(); 
		lt1.setNoLt("EEINSEVT262FR");
		lt1.setEvts(Sets.newHashSet(PATCH_EVT3)); 
		lt1.setDateLivraisonPrevue	(null); 
		return lt1;
	}
		
	///////////////////////////////     UTILITIES //////////////////////////////
	
	/** @param date format is "yyyy-MM-dd'T'HH:mm:ss" EXAMPLE :  "2015-03-22T10:00:00.000+0100"
	 * @return parsed timeStamp */
	public static Timestamp makeTimeStamp(final String date) { 
		Date dateLiv= null ;
		try { 
			dateLiv = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(date); // see WS_DATE_FMT of DateRules
		} catch (ParseException e) {e.printStackTrace();
		}
		final long time = ( dateLiv== null ) ? 0 : dateLiv.getTime() ;
		return new Timestamp(time); 
	}


	/**@param one to compare
	 * @param two to compare
	 * @return "" if same else description of all differences found	 */
	public static final String showDiffInfosComp(final Map<String,String> one ,final Map<String,String> two ) {
		final MapDifference<String,String> diff = Maps.difference(one, two); 
		return ( diff.areEqual() ) ? "" : diff.entriesDiffering().toString() ;
	}

	/**@param one to compare
	 * @param two to compare
	 * @return "" if same else description of all differences found	 */
	public static final String showDiffSet(final Set<String> one ,final Set<String> two ) {
		final SetView<String> diff = Sets.difference(one, two); 
		return ( diff.isEmpty() ) ? ""  : diff.toString() ;
	}

	/**@param e1 to compare
	 * @param e2 to compare
	 * @return "" if same else description of all differences found	 */
	public static final String showDiff(final Evt e1 ,final Evt e2 ) {
		String ret = "" ;
		if ( ( e1 == null ) || ( e2 == null ) ) {
			if ( e1 == e2 ) return "" ; // ok both null 
			return ( e1 ==  null)  ?  " e1 is null not e2 "  : " e2 is null not e1 " ; // one is null not the other 
		}
		
		ret += addDiff("cab_evt_saisi"		, e1.getCabEvtSaisi()		,e2.getCabEvtSaisi()); 
		ret += addDiff("cab_recu"			, e1.getCabRecu()			,e1.getCabRecu() );
		ret += addDiff("code_evt"			, e1.getCodeEvt()			,e2.getCodeEvt() );

		ret += addDiff("code_evt_ext"		, e1.getCodeEvtExt()		,e2.getCodeEvtExt() );
		ret += addDiff("Code_postal_evt"	, e1.getCodePostalEvt()	,e2.getCodePostalEvt() );
		ret += addDiff("Code_raison_evt"	, e1.getCodeRaisonEvt()	,e2.getCodeRaisonEvt() );
		ret += addDiff("Code_service"		, e1.getCodeService()		,e2.getCodeService() );

		ret += addDiff("Createur_evt"		, e1.getCreateurEvt()		,e2.getCreateurEvt() );
		ret += addDiff("Date_creation_evt"	, e1.getDateCreationEvt()	,e2.getDateCreationEvt() );
		
		ret += addDiff("Id_acces_client"	, e1.getIdAccesClient()	,e2.getIdAccesClient() );
		ret += addDiff("Id_extraction_evt"	, e1.getIdExtractionEvt()	,e2.getIdExtractionEvt() );
		ret += addDiff("Id_ss_code_evt"		, e1.getIdSsCodeEvt()	,e2.getIdSsCodeEvt() );
		ret += addDiff("Idbco_evt"			, e1.getIdbcoEvt()			,e2.getIdbcoEvt() );
		
		ret += addDiff("Libelle_evt"		, e1.getLibelleEvt()		,e2.getLibelleEvt() );
		ret += addDiff("Libelle_lieu_evt"	, e1.getLibelleLieuEvt()	,e2.getLibelleLieuEvt() );
		ret += addDiff("Lieu_evt"			, e1.getLieuEvt()			,e2.getLieuEvt() );

		ret += addDiff("No_lt"				, e1.getNoLt()				,e2.getNoLt() );
		ret += addDiff("Position_evt"		, e1.getPositionEvt()		,e2.getPositionEvt() );
		ret += addDiff("Priorite_evt"		, e1.getPrioriteEvt()		,e2.getPrioriteEvt() );

		ret += addDiff("Prod_cab_evt_saisi"	, e1.getProdCabEvtSaisi(),e2.getProdCabEvtSaisi() );
		ret += addDiff("Prod_no_lt"			, e1.getProdNoLt()		,e2.getProdNoLt() );
		ret += addDiff("Ref_extraction"		, e1.getRefExtraction()	,e2.getRefExtraction() );
		ret += addDiff("Ref_id_abonnement"	, e1.getRefIdAbonnement()	,e2.getRefIdAbonnement() );
		
		ret += addDiff("Ss_code_evt"		, e1.getSsCodeEvt()		,e2.getSsCodeEvt() );
		ret += addDiff("Status_envoi"		, e1.getStatusEnvoi()		,e2.getStatusEnvoi() );
		ret += addDiff("Status_evt"			, e1.getStatusEvt()		,e2.getStatusEvt() );

		ret += addDiff("Date_evt"			, e1.getDateEvt().toString()	,e2.getDateEvt().toString() );
		
		ret += showDiffInfosComp( e1.getInfoscomp()	,e2.getInfoscomp() );	//	addDiff("Infoscomp"		, e1.getInfoscomp().toString()			,e2.getInfoscomp().toString() );
		return ret; 
	}

	/** Indicates that we have not id  */
	public static final Integer NO_INT = new Integer("-1");

    // public static final   Evt BASIC = makeBaseEvt(new Date(0)) ;  

	/** @param name functional name of values
	 *  @param v1 to compare
	 *  @param v2 to compare
	 *  @return "" if same else name  and value pair to show diff  */
	public static String addDiff(final String name,final Integer v1, final Integer v2) {
		if ( ( v1.equals(v2) ) || ( v1.compareTo(v2) == 0 ) ) return "" ; 
		return addDiff(name,v1.toString() ,v2.toString()) ;
	}

	/** @param name functional name of values
	 *  @param v1 to compare
	 *  @param v2 to compare
	 *  @return "" if same else name  and value pair to show diff  */
	public static String addDiff(final String name,final String v1, final String v2) {
		if ( ( v1.equals(v2) ) || ( v1.compareTo(v2) == 0 ) ) return "" ; 
		return " N:" + name + " '" + v1 + "' ==> '" + v2 + "'" ;
	}
	
} // EOC LtDataMaker 292 lines 


//	/** List of test 1 assert only for NO ERROR */ @Test 
//	public void testLtDataMaker() {
//		printDiff( LtDataMaker.EVT_160 ,LtDataMaker.EVT_246);
//		printDiff( LtDataMaker.EVT_343 ,LtDataMaker.EVT_353);
//		
//		LtDataMaker.showDiffInfosComp(LtDataMaker.MY_INFO_COMP1,LtDataMaker.MY_INFO_COMP2);
//		
////		String s1 = LtDataMaker.myjackLt1().replace("\"","\\\"") ;
////		String s2 = LtDataMaker.myjackLt2().replace("\"","\\\"") ;
////		System.out.println(" LtDataMaker.myjackLt1() =" + s1 ); 
////		System.out.println(" LtDataMaker.myjackLt2() =" + s2 );
//		new LtDataMaker().testJSON_LTS();
//	}


//    private static Evt mkVERY_BASIC_EVT() { 
//    	return new Evt()
//            .setCab_recu("cab_recu").setCode_evt("code_evt").setCab_evt_saisi("cab_evt_saisi")
//            .setCode_evt_ext("code_evt_ext").setCode_postal_evt("code_postal_evt").setCode_raison_evt("code_raison_evt").setCode_service("code_service")
//            .setCreateur_evt("createur_evt").setDate_creation_evt("date_creation_evt")
//            .setId_acces_client(NO_INT).setId_extraction_evt("Id_extraction_evt").setId_ss_code_evt(NO_INT).setIdbco_evt(NO_INT)
//            .setLibelle_evt("Libelle_evt").setLibelle_lieu_evt("libelle_lieu_evt").setLieu_evt("Lieu_evt")
//            .setNo_lt("No_lt").setPosition_evt(NO_INT).setPriorite_evt(NO_INT)
//            .setProd_cab_evt_saisi(1).setProd_no_lt(NO_INT).setRef_extraction("ref_extraction").setRef_id_abonnement("ref_id_abonnement") 
//            .setSs_code_evt("ss_code_evt").setStatus_envoi("status_envoi").setStatus_evt("status_evt")
////            .setDate_evt(null)
////            .setInfoscomp(null)                        
//            ;
//    }
//    
//    public static Evt makeBaseEvt(final Date dateEvt ,Map<String, String> infosComp) {    	// final String strDateEvt = dateEvt.toString();
//    	return  mkVERY_BASIC_EVT()    	
//        .setNo_lt("EEINSEVT001FR").setCab_recu("%0020090NA146848396248899250")
//        .setCreateur_evt("TRI1").setDate_creation_evt("2015-09-01T22:34:56").setId_acces_client(0)
//        .setId_extraction_evt("717493191").setIdbco_evt(88).setLibelle_evt("Envoi en transit")
//        .setLieu_evt("93999").setPosition_evt(0).setRef_id_abonnement("EVT_CHR").setSs_code_evt("AJA0")
//        .setStatus_evt("Acheminement en cours").setCode_postal_evt("13999")        
//        .setCode_evt_ext("toto")
//        .setCode_service("code_service").setId_ss_code_evt(1)
//        .setProd_cab_evt_saisi(1).setProd_no_lt(1)
//        .setDate_evt(dateEvt)
//        .setInfoscomp(infosComp)
//        ;
//    }

//	/** 4 informations: ("190", "4.55555") , ("191", "5.66666") , ("240", "12:00") , ("193", "AJA20A0100208092015065959") */
//	public static final Map<String, String> MY_INFO_COMP1 = ImmutableMap.<String, String>builder()
//		    .put("190", "4.55555")
//		    .put("191", "5.66666")
//		    .put("240", "12:00")
//		    .put("193", "AJA20A0100208092015065959")
//		    .build();
//
//	/** 4 informations: ("190", "4.55555") , ("191", "5.66666") , ("240", "12:00") , ("193", "AJA20A0100208092015065959") */
//	public static final Map<String, String> MY_INFO_COMP2 = ImmutableMap.<String, String>builder()
//	        .put("190", "4.55555")
//	        .put("191", "5.66666")
//	        .put("240", "12:00")
//	        .put("193", "AJA20A0100208092015065959")
//		    .build();
//
//
//	/** From line 160 of VisionInsertEvtAcceptanceTest */
//	public static final   Evt EVT_160 = new Evt().setPriorite_evt(146)
//            .setDate_evt(mkStrDate("yyyy-MM-dd HH:mm:ss","2015-03-18 22:26:00") ) 
//            .setNo_lt("EEINSEVT001FR").setCab_recu("%0020090NA146848396248899250").setCode_evt("TA")
//            .setCreateur_evt("TRI1").setDate_creation_evt("2015-09-01T22:34:56").setId_acces_client(0)
//            .setId_extraction_evt("717493191").setIdbco_evt(88).setLibelle_evt("Envoi en transit")
//            .setLieu_evt("93999").setPosition_evt(0).setRef_id_abonnement("EVT_CHR").setSs_code_evt("AJA0")
//            .setStatus_evt("Acheminement en cours").setInfoscomp(MY_INFO_COMP1).setCode_postal_evt("13999")
//            .setCab_evt_saisi("cab_evt_saisi").setCode_evt_ext("toto").setCode_raison_evt("code_raison_evt")
//            .setCode_service("code_service").setId_ss_code_evt(1).setLibelle_lieu_evt("libelle_lieu_evt")
//            .setProd_cab_evt_saisi(1).setProd_no_lt(1).setRef_extraction("ref_extraction")
//            .setStatus_envoi("status_envoi");
//
//	public static final   Evt EVT_246 = new Evt().setPriorite_evt(146)
//            .setDate_evt(mkStrDate("yyyy-MM-dd HH:mm:ss","2015-03-18 22:26:00") )
//            .setNo_lt("EEINSEVT002FR").setCab_recu("%0020090NA146848396248899250").setCode_evt("D")
//            .setCreateur_evt("TRI1").setDate_creation_evt("2015-09-01T22:34:56").setId_acces_client(0)
//            .setId_extraction_evt("717493191").setIdbco_evt(88).setLibelle_evt("Envoi en transit")
//            .setLieu_evt("93999").setPosition_evt(0).setRef_id_abonnement("EVT_CHR").setSs_code_evt("AJA0")
//            .setStatus_evt("Acheminement en cours").setInfoscomp(MY_INFO_COMP1).setCode_postal_evt("13999")
//            .setCab_evt_saisi("cab_evt_saisi").setCode_evt_ext("toto").setCode_raison_evt("code_raison_evt")
//            .setCode_service("code_service").setId_ss_code_evt(1).setLibelle_lieu_evt("libelle_lieu_evt")
//            .setProd_cab_evt_saisi(1).setProd_no_lt(1).setRef_extraction("ref_extraction")
//            .setStatus_envoi("status_envoi");
//
//    
//    public static final Date DATEEVT = new Date();
//
//	
//    public static final   Evt EVT_343 = new Evt().setPriorite_evt(200).setDate_evt(DATEEVT).setNo_lt("EEINSEVT0C3FR")
//            .setCab_recu("%0020090NA146848396248899250").setCode_evt("D").setCreateur_evt("TRI1")
//            .setDate_creation_evt("2015-09-01T22:34:56").setId_acces_client(0).setId_extraction_evt("717493191")
//            .setIdbco_evt(88).setLibelle_evt("Envoi en transit").setLieu_evt("93999").setPosition_evt(0)
//            .setRef_id_abonnement("EVT_CHR").setSs_code_evt("AJA0").setStatus_evt("Acheminement en cours")
//            .setInfoscomp(MY_INFO_COMP2).setCode_postal_evt("13999").setCab_evt_saisi("cab_evt_saisi")
//            .setCode_evt_ext("toto").setCode_raison_evt("code_raison_evt").setCode_service("code_service")
//            .setId_ss_code_evt(1).setLibelle_lieu_evt("libelle_lieu_evt").setProd_cab_evt_saisi(1).setProd_no_lt(1)
//            .setRef_extraction("ref_extraction").setStatus_envoi("status_envoi");
//
//    public static final   Evt EVT_353 = new Evt().setPriorite_evt(1000).setDate_evt(DATEEVT).setNo_lt("EEINSEVT0C3FR")
//            .setCab_recu("%0020090NA146848396248899250").setCode_evt("TA").setCreateur_evt("TRI1")
//            .setDate_creation_evt("2015-09-01T22:34:56").setId_acces_client(0).setId_extraction_evt("717493191")
//            .setIdbco_evt(80).setLibelle_evt("Envoi en transit").setLieu_evt("93999").setPosition_evt(0)
//            .setRef_id_abonnement("EVT_CHR").setSs_code_evt("AJA0").setStatus_evt("Acheminement en cours")
//            .setInfoscomp(MY_INFO_COMP2).setCode_postal_evt("13999").setCab_evt_saisi("cab_evt_saisi")
//            .setCode_evt_ext("toto").setCode_raison_evt("code_raison_evt").setCode_service("code_service")
//            .setId_ss_code_evt(1).setLibelle_lieu_evt("libelle_lieu_evt").setProd_cab_evt_saisi(1).setProd_no_lt(1)
//            .setRef_extraction("ref_extraction").setStatus_envoi("status_envoi");
//
//	/** @param format of date below.
//	 * @param date for SimpleDateFormat should follow format.
//	 * @return SimpleDateFormat(format).parse(date) null if any problem ( squeeze exception */
//	public static final Date mkStrDate(final String format,final String date) { 
//		try 					{ return new SimpleDateFormat(format).parse(date);} 
//		catch (ParseException e){ return null ; 	}
//	}
//
