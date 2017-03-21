package com.chronopost.vision.microservices.lt.get;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import jersey.repackaged.com.google.common.collect.Sets;

import org.testng.annotations.Test;

import com.chronopost.vision.microservices.enums.ESelectLT;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.stringuts.StrUtils;

/** @author jjean-charles add complete tests of difference between LT and Enum. Made robust to underscore changes using toUpperUnderscoreStrs twice.
 * fake modif for second push via sourcetree **/
public class ESelectLTTest {

	 // LT1 =  RES SORT=[adresse_1_destinataire, adresse_1_expediteur, adresse_2_destinataire, adresse_2_expediteur, article_1, cab_evt_saisi, cab_recu, code_etat_destinataire, code_etat_expediteur, code_evt, code_evt_ext, code_pays_destinataire, code_pays_expediteur, code_pays_num_destinataire, code_pays_num_expediteur, code_point_relais, code_postal_destinataire, code_postal_evt, code_postal_expediteur, code_produit, code_raison_evt, code_service, codes_evt, crbt_rep, createur_evt, creneauChargeur, creneauTournee, creneauTourneeRecopie, date_creation_evt, date_depot_lt, date_depot_lt_intern, date_entree_si, date_evt, date_evt_readable, date_heure_saisie, date_livraison_contractuelle, date_livraison_prevue, date_modification, depot_expediteur, description, destination_id_fedex, devise_assurance, devise_rep, devise_val_declaree, doc_march, email_1_destinataire, email_1_expediteur, email_2_destinataire, email_2_expediteur, esd, eta, etaMax, evenements, evts, hauteur, heureMaxLivraison, heure_evt, id_abonnement, id_acces_client, id_appli, id_colis_client, id_extraction_evt, id_ligne, id_ss_code_evt, idbco_evt, idx_depassement, infoscomp, jour_livraison, largeur, latitudeDistri, latitudePrevue, libelle_evt, libelle_lieu_evt, lieu_evt, longitudeDistri, longitudePrevue, longueur, no_contrat, no_lt, no_ss_compte, nom_1_destinataire, nom_1_expediteur, nom_2_destinataire, nom_2_expediteur, origine_saisie, poids, positionTournee, position_c11, position_evt, priorite_evt, prod_cab_evt_saisi, prod_no_lt, ref_abonnement, ref_destinataire, ref_expediteur, ref_extraction, ref_id_abonnement, retardEta, ss_code_evt, status_envoi, status_evt, synonyme_maitre, synonymes, telephone_destinataire, telephone_expediteur, val_declaree, valeur_assuree, valeur_rep, ville_destinataire, ville_expediteur]
	//     RES SORTORIG=[adresse_1_destinataire, adresse_1_expediteur, adresse_2_destinataire, adresse_2_expediteur, article_1, cab_evt_saisi, cab_recu, code_etat_destinataire, code_etat_expediteur, code_evt, code_evt_ext, code_pays_destinataire, code_pays_expediteur, code_pays_num_destinataire, code_pays_num_expediteur, code_point_relais, code_postal_destinataire, code_postal_evt, code_postal_expediteur, code_produit, code_raison_evt, code_service, codes_evt, crbt_rep, createur_evt, creneauChargeur, creneauTournee, creneauTourneeRecopie, date_creation_evt, date_depot_lt, date_depot_lt_intern, date_entree_si, date_evt, date_evt_readable, date_heure_saisie, date_livraison_contractuelle, date_livraison_prevue, date_modification, depot_expediteur, description, destination_id_fedex, devise_assurance, devise_rep, devise_val_declaree, doc_march, email_1_destinataire, email_1_expediteur, email_2_destinataire, email_2_expediteur, esd, eta, etaMax, evenements, evts, hauteur, heureMaxLivraison, heure_evt, id_abonnement, id_acces_client, id_appli, id_colis_client, id_extraction_evt, id_ligne, id_ss_code_evt, idbco_evt, idx_depassement, infoscomp, jour_livraison, largeur, latitudeDistri, latitudePrevue, libelle_evt, libelle_lieu_evt, lieu_evt, longitudeDistri, longitudePrevue, longueur, no_contrat, no_lt, no_ss_compte, nom_1_destinataire, nom_1_expediteur, nom_2_destinataire, nom_2_expediteur, origine_saisie, poids, positionTournee, position_c11, position_evt, priorite_evt, prod_cab_evt_saisi, prod_no_lt, ref_abonnement, ref_destinataire, ref_expediteur, ref_extraction, ref_id_abonnement, retardEta, ss_code_evt, status_envoi, status_evt, synonyme_maitre, synonymes, telephone_destinataire, telephone_expediteur, val_declaree, valeur_assuree, valeur_rep, ville_destinataire, ville_expediteur]
	 // V2 RES SORTORIG=[adresse_1_destinataire, adresse_1_expediteur, adresse_2_destinataire, adresse_2_expediteur, article_1, cab_evt_saisi, cab_recu, code_etat_destinataire, code_etat_expediteur, code_evt, code_evt_ext, code_pays_destinataire, code_pays_expediteur, code_pays_num_destinataire, code_pays_num_expediteur, code_point_relais, code_postal_destinataire, code_postal_evt, code_postal_expediteur, code_produit, code_raison_evt, code_service, codes_evt, crbt_rep, createur_evt, creneauChargeur, creneauTournee, creneauTourneeRecopie, date_creation_evt, date_depot_lt, date_depot_lt_intern, date_entree_si, date_evt, date_evt_readable, date_heure_saisie, date_livraison_contractuelle, date_livraison_prevue, date_modification, depot_expediteur, description, destination_id_fedex, devise_assurance, devise_rep, devise_val_declaree, doc_march, email_1_destinataire, email_1_expediteur, email_2_destinataire, email_2_expediteur, esd, eta, etaMax, evenements, evts, hauteur, heureMaxLivraison, heure_evt, id_abonnement, id_acces_client, id_appli, id_colis_client, id_extraction_evt, id_ligne, id_ss_code_evt, idbco_evt, idx_depassement, infoscomp, jour_livraison, largeur, latitudeDistri, latitudePrevue, libelle_evt, libelle_lieu_evt, lieu_evt, longitudeDistri, longitudePrevue, longueur, no_contrat, no_lt, no_ss_compte, nom_1_destinataire, nom_1_expediteur, nom_2_destinataire, nom_2_expediteur, origine_saisie, poids, positionTournee, position_c11, position_evt, priorite_evt, prod_cab_evt_saisi, prod_no_lt, ref_abonnement, ref_destinataire, ref_expediteur, ref_extraction, ref_id_abonnement, retardEta, ss_code_evt, status_envoi, status_evt, synonyme_maitre, synonymes, telephone_destinataire, telephone_expediteur, val_declaree, valeur_assuree, valeur_rep, ville_destinataire, ville_expediteur]

//	/** KEEP SVP  to check by code evolution. one shot n but may be reused  */
//	@Test
//	public void compareFields()  {
//		List<String> lt1 = Lists.newArrayList();	lt1.addAll(makeFlds(Lt.class)) ; 
//		List<String> ltrem = Lists.newArrayList();	ltrem.addAll(makeFlds(LtREMADE.class)) ; 
//		// System.out.println(" RES ORIG=" + lt1 ) ; 
//
//		Collections.sort(ltrem);  
//		Collections.sort(lt1); 
//		System.out.println(" RES SORTORIG=" + lt1 ) ; 
//		System.out.println(" RES SORTREM=" + ltrem ) ; 
//		
//		System.out.println(" NB FIELDS = " + lt1.size() ) ;
//		System.out.println(" NB FIELDS = " + ltrem.size() ) ;
//	    final Class<?> clz = LtREMADE.class ; // new code remade ( version with no underscore
//	    final Field[] flds = clz.getDeclaredFields();
//	}
	
	/** patchy : yet corrected  */
	@Test
	public void getSmallSelectClause() {
		final String select = GetLtDaoImpl.SMALL_SELECT ; // ESelectLT.makeSmallSelectClause(ESelectLT.FIELDS_FOR_SMALL);
		assertEquals(select,"no_lt,synonyme_maitre,idx_depassement,code_service,date_livraison_prevue,date_livraison_contractuelle,no_contrat,email_1_destinataire,email_2_destinataire,email_1_expediteur,email_2_expediteur,telephone_destinataire,code_pays_destinataire");
	}
	
	/** The Attributes of Lt.class not found in Enum.  */
	private static final Set<String> LT_MINUS_ESelectLT  = StrUtils.mkSet(true,"NO_INFOCOMPS","creneauTournee" ,"etaMax" ,"positionTournee" ,"heure_evt" ,"date_evt_readable" ,"heureMaxLivraison" ,"retardEta" ,"creneauTourneeRecopie" ,"creneauChargeur" ,"evenements");

	/** Will test that all getFieldName() are in LT except  precisely for LT_MINUS_ESelectLT. */
	@Test
	public void testLtFieldCoverage() {
		final Set<String> made = makeFlds(Lt.class);

		for (ESelectLT cur : ESelectLT.values())
			made.remove(cur.getFieldName());

		made.removeAll(LT_MINUS_ESelectLT);

		assertEquals(0, made.size(), "ERREUR LT - Eselect : " + made);
	}

	/** Will test that all all field are found except those from StrUtils.LT_COLNAMES_WITH_NOFIELD. that is ["position" ,"creneau_notif" ,"depassement_proactif" ,"creneau" ]*/
	@Test
	public void testESelectLTNullLtFields() {
		final Set<String> ltFlds = makeFlds(Lt.class);
		final Set<String> bad = new HashSet<>();
		final Set<String> acceptedNullFields = Sets.newHashSet();
		// make each normalised name
		for (String cur : StrUtils.LT_COLNAMES_WITH_NOFIELD)
			acceptedNullFields.add(StrUtils.toUpperUnderscoreStr(cur));

		for (ESelectLT cur : ESelectLT.values()) {
			final String name = cur.getFieldName();
			if (acceptedNullFields.contains(name)) {
				if (cur.getField() != null)
					bad.add("NULL:" + name);
				continue;
			}
			if (ltFlds.contains(name) == false)
				bad.add("NOT_IN_LT:" + name);
		}
		assertEquals(0, bad.size(),
				"acceptedNullFields are : " + acceptedNullFields + " ERREUR NULLS Eselect : " + bad);
	}

	/** Will test the select clause : in fact we are redoing the job of StrUtils.mkAllNamesCommaSeparated. this test SHOULD BE ELSEWHERE now. */
	@Test
	public void testESelectLTSelectClause() {
		String sep = "";
		final ESelectLT[] fullSelectFields = GetLtDaoImpl.FULL_SELECT_FIELDS;
		StringBuffer res = new StringBuffer();
		for (ESelectLT cur : fullSelectFields) {
			res.append(sep + cur.getColName());
			sep = ",";
		}
		final String made = res.toString();
		final String orig = StrUtils.mkAllNamesCommaSeparated(fullSelectFields);
		assertEquals(0, orig.compareTo(made), "ERROR ESelectLT : Selorig= " + orig + " Selmade= " + made);
	}

	/** @param clz whose field names we retrieve
	 *  @return clz.getDeclaredFields()  MINUS (toExclude1 + toExclude2)	 */
	private static final Set<String> makeFlds(final Class<?> clz ) { 
	    final Set<String> ret = new HashSet<>() ;
		for ( Field cur : clz.getDeclaredFields() )  ret.add(cur.getName()) ;
		return ret; 
	}
}
