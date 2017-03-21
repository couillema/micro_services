package com.chronopost.vision.microservices.enums;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.chronopost.vision.enums.IEnumForField;
import com.chronopost.vision.microservices.utils.AttrDesc;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.stringuts.StrUtils;

/** 
 * Enumération des différents champs que l'on peut trouver dans le model et la base LT 
 * 
 * @author JJC :  add interface and getColName() no getNom !!     
 *  BELOW : made by  EnumsTest 
 *  ENUM : EInsertLT(nb=102) com.chronopost.vision.model.Lt has 110 fields 
 *	DIFF: LT - EInsertLT : nb= 8 vals: [heureEvt, etaMax, dateEvtReadable, heureMaxLivraison, retardEta, creneauTourneeRecopie, noLt, evenements]
 *	Sgbd request list clause =adresse1Destinataire,adresse1Expediteur,adresse2Destinataire,adresse2Expediteur,article1,cabEvtSaisi,cabRecu,codeEtatDestinataire,codeEtatExpediteur,codeEvt,codeEvtExt,codePaysDestinataire,codePaysExpediteur,codePaysNumDestinataire,codePaysNumExpediteur,codePointRelais,codePostalDestinataire,codePostalEvt,codePostalExpediteur,codeProduit,codeRaisonEvt,codeService,codesEvt,crbtRep,createurEvt,creneau,creneauNotif,dateCreationEvt,dateDepotLt,dateDepotLtIntern,dateEntreeSi,dateEvt,dateHeureSaisie,dateLivraisonContractuelle,dateLivraisonPrevue,dateModification,depotExpediteur,description,destinationIdFedex,deviseAssurance,deviseRep,deviseValDeclaree,docMarch,email1Destinataire,email1Expediteur,email2Destinataire,email2Expediteur,esd,eta,evts,hauteur,idAbonnement,idAccesClient,idAppli,idColisClient,idExtractionEvt,idLigne,idSsCodeEvt,idbcoEvt,idxDepassement,infoscomp,jourLivraison,largeur,latitudeDistri,latitudePrevue,libelleEvt,libelleLieuEvt,lieuEvt,longitudeDistri,longitudePrevue,longueur,noContrat,noSsCompte,nom1Destinataire,nom1Expediteur,nom2Destinataire,nom2Expediteur,origineSaisie,poids,position,positionC11,positionEvt,prioriteEvt,prodCabEvtSaisi,prodNoLt,refAbonnement,refDestinataire,refExpediteur,refExtraction,refIdAbonnement,ssCodeEvt,statusEnvoi,statusEvt,synonymeMaitre,synonymes,telephoneDestinataire,telephoneExpediteur,valDeclaree,valeurAssuree,valeurRep,villeDestinataire,villeExpediteur
 * EInsertLT position =id=79 N='position' FLDName='private int com.chronopost.vision.model.Lt.positionTournee' typ=String
 * 
 **/
public enum EInsertLT implements IEnumForField {
	/** fake start of 102 fields line 21 --> line 122 */
	ADRESSE_1_DESTINATAIRE()		
	, ADRESSE_1_EXPEDITEUR()		
	, ADRESSE_2_DESTINATAIRE()		
	, ADRESSE_2_EXPEDITEUR()		
	, ARTICLE_1()					
	, CAB_EVT_SAISI()				 // NOT IN ORIG VERSION of laurent ?? 
	, CAB_RECU()					
	, CODE_ETAT_DESTINATAIRE()		
	, CODE_ETAT_EXPEDITEUR()		
	, CODE_EVT()					
	, CODE_EVT_EXT()				
	, CODE_PAYS_DESTINATAIRE()		
	, CODE_PAYS_EXPEDITEUR()		
	, CODE_PAYS_NUM_DESTINATAIRE()	
	, CODE_PAYS_NUM_EXPEDITEUR()	
	, CODE_POINT_RELAIS()			
	, CODE_POSTAL_DESTINATAIRE()	
	, CODE_POSTAL_EVT()				
	, CODE_POSTAL_EXPEDITEUR()		
	, CODE_PRODUIT()				
	, CODE_RAISON_EVT()				
	, CODE_SERVICE()				
	, CODES_EVT				("Set")	
	, CRBT_REP()					
	, CREATEUR_EVT()				
	, CRENEAU			("creneau" 		 , "creneauChargeur", StrUtils.STRING)	/** imposed by LT.class */
	, CRENEAU_NOTIF		("creneau_notif" , "creneauTournee"	, StrUtils.STRING)	/** imposed by creneauTournee in Lt.class */
	, DATE_CREATION_EVT()						
	, DATE_DEPOT_LT					("Date")	
	, DATE_DEPOT_LT_INTERN			("Date")	
	, DATE_ENTREE_SI				("Date")	
	, DATE_EVT						("Date")	
	, DATE_HEURE_SAISIE()						
	, DATE_LIVRAISON_CONTRACTUELLE	("Date")	
	, DATE_LIVRAISON_PREVUE			("Date")	
	, DATE_MODIFICATION				("Date")	 	// , DEPASSEMENT_PROACTIF("depassement_proactif","Int")
	, DEPOT_EXPEDITEUR()			
	, DESCRIPTION()					
	, DESTINATION_ID_FEDEX()		
	, DEVISE_ASSURANCE()			
	, DEVISE_REP()					
	, DEVISE_VAL_DECLAREE()			
	, DOC_MARCH()					
	, EMAIL_1_DESTINATAIRE()		
	, EMAIL_1_EXPEDITEUR()			
	, EMAIL_2_DESTINATAIRE()		
	, EMAIL_2_EXPEDITEUR()			
	, ESD()							
	, ETA()							
	, EVTS					("Set")	
	, HAUTEUR				("Int")	
	, ID_ABONNEMENT()				
	, ID_ACCES_CLIENT		("Int")	
	, ID_APPLI()					
	, ID_COLIS_CLIENT()				
	, ID_EXTRACTION_EVT()			
	, ID_LIGNE()					
	, ID_SS_CODE_EVT		("Int")	
	, IDBCO_EVT				("Int")	
	, IDX_DEPASSEMENT()				
	, INFOSCOMP				("Map")	
	, JOUR_LIVRAISON()				
	, LARGEUR				("Int")	
	, LATITUDE_DISTRI("latitude_distri", "latitudeDistri", "String") 	/** REM_SVP */ // "latitude_distri", "latitudeDistri", "String") // ??
	, LATITUDE_PREVUE("latitude_prevue", "latitudePrevue", "String")	/** REM_SVP */ //  "latitude_prevue", "latitudePrevue", "String")// ??
	, LIBELLE_EVT()					 // "libelle_evt" ??
	, LIBELLE_LIEU_EVT()			
	, LIEU_EVT()					
	, LONGITUDE_DISTRI("longitude_distri", "longitudeDistri", "String")		/** REM_SVP */ //"longitude_distri", "longitudeDistri", "String")// ??	
	, LONGITUDE_PREVUE("longitude_prevue", "longitudePrevue","String" )		/** REM_SVP */ //"longitude_prevue", "longitudePrevue","String") // ??	
	, LONGUEUR				("Int")	
	, NO_CONTRAT()					
	, NO_SS_COMPTE()				
	, NOM_1_DESTINATAIRE()			
	, NOM_1_EXPEDITEUR()			
	, NOM_2_DESTINATAIRE()			
	, NOM_2_EXPEDITEUR()			
	, ORIGINE_SAISIE()				
	, POIDS					("Int")	
	, POSITION		("position" , "positionTournee", StrUtils.STRING)	/**  CALLED at 282/283 of InsertLtMapperTest !! in 2 ways */
	, POSITION_C11()				
	, POSITION_EVT			("Int")	
	, PRIORITE_EVT			("Int")	
	, PROD_CAB_EVT_SAISI	("Int")	
	, PROD_NO_LT			("Int")	
	, REF_ABONNEMENT()				
	, REF_DESTINATAIRE()			
	, REF_EXPEDITEUR()				
	, REF_EXTRACTION()				
	, REF_ID_ABONNEMENT()			
	, SS_CODE_EVT()					
	, STATUS_ENVOI()				
	, STATUS_EVT()					
	, SYNONYME_MAITRE()				
	, SYNONYMES					("Set")	
	, TELEPHONE_DESTINATAIRE()		
	, TELEPHONE_EXPEDITEUR()		
	, VAL_DECLAREE()				
	, VALEUR_ASSUREE()				
	, VALEUR_REP()					
	, VILLE_DESTINATAIRE()			
	, VILLE_EXPEDITEUR()			
	, ID_ADRESSE_DESTINATAIRE()		
	, ID_POI_DESTINATAIRE()			
	; // eof 102 fields line 21 to 122 


	/* [heureEvt , etaMax , positionTournee,dateEvtReadable ,heureMaxLivraison,retardEta , creneauTourneeRecopie ,evenements] 
	 * SAME same as comment except "noLt" + "creneauTournee" missing
     * private String heureEvt; private String dateEvtReadable; private
     * String etaMax; private int positionTournee; private long retardEta;
     * private ArrayList<Evt> evenements; private String heureMaxLivraison;
     * private boolean creneauTourneeRecopie = false;
     */
    
	/*** ordinal() :  fixed here : for speed. */
	private final int idCol;
	/** Column name in SGBD (property name ??)  */
	private final String nomCol;
	/** like String , Int,Date ,Map ,Set   etc..     */
	private final String type;
	/** Field and set Method of Lt */ 
	private AttrDesc ltDesc ;  
	
    /** CST: nomCol and fieldName will be both name().toLowerCase() , type is "String"     */
    private EInsertLT() {    this(StrUtils.NULL_STR ,StrUtils.NULL_STR ,StrUtils.STRING) ; }

    /** CST: nomCol and fieldName will be both name().toLowerCase().    
     *  @param ptype type of field : usually Date, Set , List , Int */
    private EInsertLT(final String pType) {    this(StrUtils.NULL_STR,StrUtils.NULL_STR,pType) ; }

    /** @param pNomCol null means name() in lowerCase .
     * @param pAttrField null means name() in lower case 
     * @param pType never null stored as is.    */
    private EInsertLT(final String pNomCol, final String pAttrField  ,final String pType) {
        this.nomCol  = (pNomCol  == StrUtils.NULL_STR) ? name().toLowerCase(): pNomCol;
        final String attrField = ( pAttrField == StrUtils.NULL_STR ) ? nomCol : pAttrField ;
        this.ltDesc = new AttrDesc(Lt.class,attrField,StrUtils.FOUND_TYPE.FIELD_IS_REQUIRED); // Stroong requirement !! 
        this.type = pType;
        this.idCol = this.ordinal();
    }

    /** @see com.chronopost.vision.enums.IEnumForField#getField() */
	public final Field getField()  { return ltDesc.getField() ; }  
    /** @return @NonNull description of the Set method associate to this attribute in Lt.class   */
	public final Method getSetMethod()  { return ltDesc.getSetMethod() ; }  

    /** @return name of column in DataBase     */
    public final String getColName() {        return nomCol;     }
    
	/** @return like String , Int,Date ,Map ,Set   etc..     */
    public String getType() {        return type;    }

	/** @return the column name in DataBase.   ATTENTION NEEDED for ALL_COL_NAMES */
	public final String toString() {      return nomCol;    }

    /** @return a one line description.  */
    public String getDesc() { return "id=" + idCol + " N='" + nomCol + "' FLDName='" + getField() +  "' typ=" + type ;  }
    
} //EOC Enum EInsertLT 181 lines

