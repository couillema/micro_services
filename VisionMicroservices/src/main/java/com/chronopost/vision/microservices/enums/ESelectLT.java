package com.chronopost.vision.microservices.enums;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.chronopost.vision.enums.IEnumForField;
import com.chronopost.vision.microservices.utils.AttrDesc;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.stringuts.StrUtils;

/** @author jcbontemps     Enumération des champs d'une LT pour les clauses Select sur CASSANDRA + refomarte and add CleanName() 
*

ENUM : ESelectLT(nb=104) com.chronopost.vision.model.Lt has 110 fields 
	 DIFF: LT - ESelectLT : nb= 10 vals: [creneauTournee, heureEvt, etaMax, positionTournee, dateEvtReadable, heureMaxLivraison, retardEta, creneauTourneeRecopie, creneauChargeur, evenements]
ATTENTION ENUM - com.chronopost.vision.model.Lt != 0 : 4 DIFF: ESelectLT - LT  vals: [position, depassementProactif, creneauNotif, creneau]
	 Sgbd request list clause ="no_lt,adresse_1_destinataire,adresse_1_expediteur,adresse_2_destinataire,adresse_2_expediteur,article_1,cab_evt_saisi,cab_recu,code_etat_destinataire,code_etat_expediteur,code_evt,code_evt_ext,code_pays_destinataire,code_pays_expediteur,code_pays_num_destinataire,code_pays_num_expediteur,code_point_relais,code_postal_destinataire,code_postal_evt,code_postal_expediteur,code_produit,code_raison_evt,code_service,codes_evt,crbt_rep,createur_evt,creneau,creneau_notif,date_creation_evt,date_depot_lt,date_depot_lt_intern,date_entree_si,date_evt,date_heure_saisie,date_livraison_contractuelle,date_livraison_prevue,date_modification,depassement_proactif,depot_expediteur,description,destination_id_fedex,devise_assurance,devise_rep,devise_val_declaree,doc_march,email_1_destinataire,email_1_expediteur,email_2_destinataire,email_2_expediteur,esd,eta,evts,hauteur,id_abonnement,id_acces_client,id_appli,id_colis_client,id_extraction_evt,id_ligne,id_ss_code_evt,idbco_evt,idx_depassement,infoscomp,jour_livraison,largeur,latitude_distri,latitude_prevue,libelle_evt,libelle_lieu_evt,lieu_evt,longitude_distri,longitude_prevue,longueur,no_contrat,no_ss_compte,nom_1_destinataire,nom_1_expediteur,nom_2_destinataire,nom_2_expediteur,origine_saisie,poids,position,position_c11,position_evt,priorite_evt,prod_cab_evt_saisi,prod_no_lt,ref_abonnement,ref_destinataire,ref_expediteur,ref_extraction,ref_id_abonnement,ss_code_evt,status_envoi,status_evt,synonyme_maitre,synonymes,telephone_destinataire,telephone_expediteur,val_declaree,valeur_assuree,valeur_rep,ville_destinataire,ville_expediteur"
 ESelectLT positon =id=81 N='position' fldName='position' typ=String

NOTE on 4 fields :    
   - "depassementProactif" : should be removed ?? 
   - "creneauNotif", "creneau" : may be using setCreneauChargeurEtCreneauTournee(final String aCreneauChargeur, final String aCreneauTournee) ; ???
   - "position" : goes by method setPositionTournee(string in ) ;   

//	*** NULLS : WE have 4 null fields in ESelectLT FLDS=[position, creneau_notif, depassement_proactif, creneau]


 */

public enum ESelectLT implements IEnumForField 
	{ /** 104 FIELDS : FIRST FIELD : First line 31 , LAST line 134 ==> 104 fields ! */
     NO_LT  							(	  	) /** ord=0			String */ 
	,ADRESSE_1_DESTINATAIRE   			(  		) /** ord=1         String	*/
	,ADRESSE_1_EXPEDITEUR 				(  		) /** ord=2         String	*/
	,ADRESSE_2_DESTINATAIRE   			(  		) /** ord=3         String	*/
	,ADRESSE_2_EXPEDITEUR 				(  		) /** ord=4         String	*/
	,ARTICLE_1  						(		) /** ord=5         String	*/
	,CAB_EVT_SAISI  					(		) /** ord=6         String	*/
	,CAB_RECU 							(  		) /** ord=7         String	*/
	,CODE_ETAT_DESTINATAIRE   			(  		) /** ord=8         String	*/ 
	,CODE_ETAT_EXPEDITEUR 				(		) /** ord=9         String	*/ 
	,CODE_EVT 							(		) /** ord=10        String	*/ 
	,CODE_EVT_EXT 						(		) /** ord=11        String	*/ 
	,CODE_PAYS_DESTINATAIRE   			(		) /** ord=12        String	*/ 
	,CODE_PAYS_EXPEDITEUR 				(		) /** ord=13        String	*/	
	,CODE_PAYS_NUM_DESTINATAIRE   		(		) /** ord=14        String	*/ 
	,CODE_PAYS_NUM_EXPEDITEUR 			(		) /** ord=15        String	*/ 
	,CODE_POINT_RELAIS  				(  		) /** ord=16        String	*/ 
	,CODE_POSTAL_DESTINATAIRE 			(		) /** ord=17        String	*/ 
	,CODE_POSTAL_EVT					(  		) /** ord=18        String	*/ 
	,CODE_POSTAL_EXPEDITEUR   			(		) /** ord=19        String	*/ 
	,CODE_PRODUIT 						(		) /** ord=20        String	*/ 
	,CODE_RAISON_EVT					(  		) /** ord=21        String	*/	 
	,CODE_SERVICE 						(		) /** ord=22        String	*/ 
	,CODES_EVT  						("Set"	) /** ord=23				*/
	,CRBT_REP 							(		) /** ord=24        String	*/ 
	,CREATEUR_EVT 						(		) /** ord=25        String	*/ 
	,CRENEAU							(		) /** ord=26        String	*/ 
	,CRENEAU_NOTIF  					(		) /** ord=27        String	*/ 
	,DATE_CREATION_EVT  				(		) /** ord=28		String	*/ 
	,DATE_DEPOT_LT  					("Date"	) /** ord=29				*/
	,DATE_DEPOT_LT_INTERN 				("Date"	) /** ord=30				*/
	,DATE_ENTREE_SI   					("Date"	) /** ord=31				*/
	,DATE_EVT 							("Date"	) /** ord=32				*/
	,DATE_HEURE_SAISIE  				(  		) /** ord=33        String	*/ 
	,DATE_LIVRAISON_CONTRACTUELLE 		("Date"	) /** ord=34				*/
	,DATE_LIVRAISON_PREVUE  			("Timestamp"	) /** ord=35				*/
	,DATE_MODIFICATION  				("Date"	) /** ord=36				*/
	,DEPASSEMENT_PROACTIF 				("Int"	) /** ord=37				*/
	,DEPOT_EXPEDITEUR 					(		) /** ord=38        String	*/ 
	,DESCRIPTION						(  		) /** ord=39        String	*/ 
	,DESTINATION_ID_FEDEX 				(		) /** ord=40        String	*/ 
	,DEVISE_ASSURANCE 					(		) /** ord=41        String	*/ 
	,DEVISE_REP   						(		) /** ord=42        String	*/ 
	,DEVISE_VAL_DECLAREE				(  		) /** ord=43        String	*/ 
	,DOC_MARCH  						(  		) /** ord=44        String	*/ 
	,EMAIL_1_DESTINATAIRE 				(		) /** ord=45        String	*/ 
	,EMAIL_1_EXPEDITEUR   				(		) /** ord=46        String	*/ 
	,EMAIL_2_DESTINATAIRE 				(		) /** ord=47        String	*/ 
	,EMAIL_2_EXPEDITEUR   				(		) /** ord=48        String	*/ 
	,ESD								(		) /** ord=49        String	*/ 
	,ETA								(  		) /** ord=50        String	*/ 
	,EVTS 								("Set"	) /** ord=51				*/
	,HAUTEUR							("Int"	) /** ord=52				*/
	,ID_ABONNEMENT  					(		) /** ord=53        String	*/ 
	,ID_ACCES_CLIENT					("Int"	) /** ord=54				*/
	,ID_ADRESSE_DESTINATAIRE			(		) /** ord=55        String	*/ 
	,ID_APPLI 							(		) /** ord=56        String	*/ 
	,ID_COLIS_CLIENT					(		) /** ord=57        String	*/ 
	,ID_EXTRACTION_EVT  				(		) /** ord=58        String	*/ 
	,ID_LIGNE 							(		) /** ord=59        String	*/ 
	,ID_POI_DESTINATAIRE 	   			(		) /** ord=60        String	*/ 
	,ID_SS_CODE_EVT   					("Int"	) /** ord=61				*/
	,IDBCO_EVT  						("Int"	) /** ord=62				*/
	,IDX_DEPASSEMENT					(		) /** ord=63        String	*/ 
	,INFOSCOMP  						("Map"	) /** ord=64				*/
	,JOUR_LIVRAISON   					(		) /** ord=65        String	*/ 
	,LARGEUR							("Int"	) /** ord=66				*/
	,LATITUDE_DISTRI					(		) /** ord=67        String	*/ 
	,LATITUDE_PREVUE					(		) /** ord=68        String	*/ 
	,LIBELLE_EVT						(		) /** ord=69        String	*/ 
	,LIBELLE_LIEU_EVT 					(		) /** ord=70        String	*/ 
	,LIEU_EVT 							(		) /** ord=71        String	*/ 
	,LONGITUDE_DISTRI 					(		) /** ord=72        String	*/ 
	,LONGITUDE_PREVUE 					(		) /** ord=73        String	*/ 
	,LONGUEUR 							("Int"	) /** ord=74				*/
	,NO_CONTRAT   						(		) /** ord=75        String	*/ 
	,NO_SS_COMPTE 						(		) /** ord=76        String	*/ 
	,NOM_1_DESTINATAIRE   				(		) /** ord=77        String	*/ 
	,NOM_1_EXPEDITEUR 					(		) /** ord=78        String	*/ 
	,NOM_2_DESTINATAIRE   				(		) /** ord=79        String	*/ 
	,NOM_2_EXPEDITEUR 					(		) /** ord=80        String	*/ 
	,ORIGINE_SAISIE   					(		) /** ord=81        String	*/ 
	,POIDS  							("Int"	) /** ord=82				*/
	,POSITION 							(		) /** ord=83        String	*/ 
	,POSITION_C11 						(		) /** ord=84        String	*/ 
	,POSITION_EVT 						("Int"	) /** ord=85				*/
	,PRIORITE_EVT 						("Int"	) /** ord=86				*/
	,PROD_CAB_EVT_SAISI   				("Int"	) /** ord=87				*/
	,PROD_NO_LT   						("Int"	) /** ord=88				*/
	,REF_ABONNEMENT   					(		) /** ord=89        String	*/ 
	,REF_DESTINATAIRE 					(		) /** ord=90        String	*/ 
	,REF_EXPEDITEUR   					(		) /** ord=91        String	*/ 
	,REF_EXTRACTION   					(		) /** ord=92        String	*/ 
	,REF_ID_ABONNEMENT  				(		) /** ord=93        String	*/ 
	,SS_CODE_EVT						(		) /** ord=94        String	*/ 
	,STATUS_ENVOI 						(		) /** ord=95        String	*/ 
	,STATUS_EVT   						(		) /** ord=96        String	*/ 
	,SYNONYME_MAITRE					(		) /** ord=97        String	*/ 
	,SYNONYMES  						("Set"	) /** ord=98				*/
	,TELEPHONE_DESTINATAIRE   			(		) /** ord=99        String	*/ 
	,TELEPHONE_EXPEDITEUR 				(		) /** ord=100        String	*/ 
	,VAL_DECLAREE 						(		) /** ord=101        String	*/ 
	,VALEUR_ASSUREE   					(		) /** ord=102       String	*/ 
	,VALEUR_REP   						(		) /** ord=103       String	*/ 
	,VILLE_DESTINATAIRE   				(		) /** ord=104       String	*/ 
	,VILLE_EXPEDITEUR 					(		) /** ord=105       String	*/ 
	;  // ordinal 103 ==> 104 fields ( as ordinal starts at 0 ! 

	/*** ordinal() :  fixed here : speed purpose */
	private final int idCol;
	/** column name in SGBD  */
	private final String nomCol;
	/** like String , Int,Date ,Map ,Set   etc..     */
	private final String type;
	/** Field and set Method of Lt */ 
	private final AttrDesc ltDesc ;  
	
	/** CST helper with type "String" implicit  */
	private ESelectLT() {	this("String"); } 

	/** @param pType stored as is.     */
	private ESelectLT(final String pType  ) {
		this.nomCol = name().toLowerCase() ; 
		this.type = pType;
		this.idCol = this.ordinal();
		final StrUtils.FOUND_TYPE requirement = StrUtils.LT_COLNAMES_WITH_NOFIELD.contains(nomCol) ? StrUtils.FOUND_TYPE.FIELD_IS_MISSING : StrUtils.FOUND_TYPE.FIELD_IS_REQUIRED ;  
        this.ltDesc = new AttrDesc(Lt.class,StrUtils.ltColNameToFieldName(nomCol),requirement);  // System.out.println( " MADE CST ENUM" + getDesc() + " getSetMethod()=" + getSetMethod() ) ;
	}

	/** @see com.chronopost.vision.enums.IEnumForField#getField() */
	public final Field getField()  { return ltDesc.getField() ; }  
	
    /** @return @NonNull description of the Set method associate to this attribute in Lt.class   */
	public final Method getSetMethod()  { return ltDesc.getSetMethod() ; }  

	/** @return the column name in DataBase.    */
	public final String getColName() {        return nomCol;    }
	
	/** @return the column name in DataBase.   ATTENTION NEEDED for ALL_COL_NAMES */
	public final String toString() {      return nomCol;    }

	/** @return fieldName of class LT  ( = normalized column name )   */
	public final String getFieldName() {  return ltDesc.getFieldName();    }

	/** @return like String , Int,Date ,Map ,Set   etc..     */
	public String getType() {       return type;    }
  
    /** @return a one line description.  */
    public String getDesc() { return "id=" + idCol + " N='" + nomCol + "' fldName='" + getFieldName() +  "' typ=" + type ;  }
  
} // EOC ESelectLT 186 lines 
