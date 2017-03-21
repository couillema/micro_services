package com.chronopost.vision.microservices.lt.get;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.enums.ESelectLT;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.rules.DateRules;
import com.datastax.driver.core.Row;
import com.google.common.collect.Sets;

/**
 * Classe de test du Builder de LT
 * 
 * @author vdesaintpern  revamped by JJC 2 tests ( one for big lt AND small Lt Case and one )
 * 
 */
public class GetLtBuilderTest {
    /** Used for lt.getCodesEvt(). */
    private static final Set<String> SOME_CODE_EVTS = Sets.newHashSet("x1","x2");
    /** Used for lt.getEvts(). */
    private static final Set<String>  SOME_EVTS = Sets.newHashSet("y1","y2");
    /** Used for lt.getSynonymes(). */
    private static final Set<String> SOME_SYNONYMS = Sets.newHashSet("s1","s2");
    /** Used for lt.getInfoscomp() warning added for serial id ???. */
    @SuppressWarnings("serial")
	private static final Map<String, String> SOME_INFOSCOMP  = new HashMap<String, String>() { 
		{
    		put("a", "1");
    		put("b", "2");
    		put("c", "3");
    	}
    }; 

    /** TEST COMPLET of all set methods * @throws Exception if PB    */
   @Test
    public void testMakeGenLtAllRow() throws Exception {

        // with data
        Row row = Mockito.mock(Row.class);
        addFields(row);
        GetLtBuilder ltBuilder = new GetLtBuilder();
        Lt lt = ltBuilder.makeGenLtAll(row); 
        checkLTFields(lt);

        // with no data - shouldn't throw any NPE or anything
        row = Mockito.mock(Row.class);
        lt = ltBuilder.makeGenLtAll(row);
        checkEmptyLTFields(lt);
    }
    
    /** TEST of 5 fields:  @throws Exception if ANY PB.     */
    @Test  // OK 
    public void testMakeGenLtRow() throws Exception {
        final ESelectLT[] fields = GetLtDaoImpl.SMALL_SELECT_FIELDS ; // SEE also  
    	// -- Make row 
        GetLtBuilder ltBuilder = new GetLtBuilder();

        Row row2 = Mockito.mock(Row.class); addFieldsSmallById(row2,fields);
    	Lt lt2 = ltBuilder.makeGenLt(row2,fields);  // MODJER           // *** AFTER MAKE2: lt1='null' l2='no_lt'

		// System.out.println("*** AFTER MAKE1/2: ***R2=" + row2 ) ;     	
    	System.out.println(" LTDao.SMALL_SELECT_FIELDS tested : TTGEN=" + strLtSmall(lt2)) ; 
//    	final int nbDiff = getNbDiff(fields, lt1 ,lt2,150) ; // nb differences
//    	System.out.println(" DIFFSMALL SMALL =" + nbDiff) ; 
     }



    /** @param in of which we show a part : normally LTDao.SMALL_SELECT_FIELDS
     *  @return a string describing some      */
    private static String strLtSmall(Lt in ) {
    	String ret = "" ;
        ret += "\t nolt='" 		+ in.getNoLt()  +"'";
        ret += "\t synMa='" 	+ in.getSynonymeMaitre() +"'";
        ret += "\t idxDepass='" + in.getIdxDepassement()+"'";
        ret += "\t codServ='" 	+ in.getCodeService() +"'";
        ret += "\t DateLiv='"	+ in.getDateLivraisonPrevue() +"'";
        return ret; 
    }
 
    /** @param row to emulate at each fields : normally LTDao.SMALL_SELECT_FIELDS
     * @param flds   those to change ( else null ??)   */
    private void addFieldsSmallById(Row row ,final ESelectLT[]flds  ) {
    	ESelectLT cur ; 
    	int iCol = 0 ; // ATTENTION ORDER !! 
        cur = ESelectLT.NO_LT 			; Mockito.when(row.getString(Mockito.eq(iCol++))).thenReturn(cur.toString());
        cur = ESelectLT.SYNONYME_MAITRE ; Mockito.when(row.getString(Mockito.eq(iCol++))).thenReturn(cur.toString());
        
        cur = ESelectLT.IDX_DEPASSEMENT ; Mockito.when(row.getString(Mockito.eq(iCol++))).thenReturn(cur.toString()); 
        cur = ESelectLT.CODE_SERVICE ;  		Mockito.when(row.getString(Mockito.eq(iCol++))).thenReturn(cur.toString()); // WAS "CODE_SERVICE"
        cur = ESelectLT.DATE_LIVRAISON_PREVUE ; Mockito.when(row.getTimestamp(Mockito.eq(iCol++))).thenReturn(new Date(7000000));
        //System.out.println(" ---------- SERV_ID3= gotrow=" + row.getString(3) );
    }

//    /** @param row to emulate at each fields normally LTDao.SMALL_SELECT_FIELDS ( FOR INDO : METHOD DOES NOT WORK OK )
//     * @param flds   those to change ( else null ??)   */
//    private void addFieldsSmallByName(Row row ,final ESelectLT[]flds  ) { // getNom et toString ==> SAME !! 
//    	ESelectLT cur ; 
//        cur = ESelectLT.NO_LT 			; Mockito.when(row.getString(Mockito.eq(cur.getNom()))).thenReturn(cur.toString());
//        cur = ESelectLT.SYNONYME_MAITRE ; Mockito.when(row.getString(Mockito.eq(cur.getNom()))).thenReturn(cur.toString());
//        
//        cur = ESelectLT.IDX_DEPASSEMENT ; Mockito.when(row.getString(Mockito.eq(cur.getNom()))).thenReturn(cur.toString()); 
//        cur = ESelectLT.CODE_SERVICE ;  		Mockito.when(row.getString(Mockito.eq(cur.getNom()))).thenReturn(cur.toString()); // WAS "CODE_SERVICE"
//        cur = ESelectLT.DATE_LIVRAISON_PREVUE ; Mockito.when(row.getTimestamp(Mockito.eq(cur.getNom()))).thenReturn(new Date(7000000));
//        
//        String serv1=  "'" + ESelectLT.CODE_SERVICE.toString() + "'" ;
//        System.out.println(" ---------- SERV_NAME1='" + serv1 + "' gotrow='" + row.getString(ESelectLT.CODE_SERVICE.toString()) + "' getnom='" + ESelectLT.CODE_SERVICE.getNom() + "'");
////        String s ; 
////        s = ESelectLT.CODE_SERVICE.getNom() ;       System.out.println(" ---------- s=" + s + " rw=" + row.getString(s));
////        s = ESelectLT.NO_LT.getNom() 	;       System.out.println(" ---------- s=" + s + " rw=" + row.getString(s));
////        s = ESelectLT.SYNONYME_MAITRE.getNom() 	;       System.out.println(" ---------- s=" + s + " rw=" + row.getString(s));
//    }
//
    
    
    /** Checks all fields.    */
    public void checkLTFields(Lt lt) {

        assertEquals(lt.getDateDepotLt(), DateRules.toTimestamp(new Date(2000000)));
        assertEquals(lt.getDateDepotLtIntern(), DateRules.toTimestamp(new Date(3000000)));
        assertEquals(lt.getDateEntreeSi(), DateRules.toTimestamp(new Date(4000000)));
        assertEquals(lt.getDateEvt(), DateRules.toTimestamp(new Date(5000000)));
        assertEquals(lt.getDateLivraisonContractuelle(), DateRules.toTimestamp(new Date(6000000)));
        assertEquals(lt.getDateLivraisonPrevue(), DateRules.toTimestamp(new Date(7000000)));
        assertEquals(lt.getDateModification(), DateRules.toTimestamp(new Date(8000000)));
        assertEquals(lt.getHauteur().intValue(), 1);
        assertEquals(lt.getIdAccesClient().intValue(), 2);
        assertEquals(lt.getIdSsCodeEvt().intValue(), 3);
        assertEquals(lt.getIdbcoEvt().intValue(), 4);
        assertEquals(lt.getLargeur().intValue(), 5);
        assertEquals(lt.getLongueur().intValue(), 6);
        assertEquals(lt.getPoids().intValue(), 7);
        assertEquals(lt.getPositionEvt().intValue(), 8);
        assertEquals(lt.getPrioriteEvt().intValue(), 9);
        assertEquals(lt.getProdCabEvtSaisi().intValue(), 10);
        assertEquals(lt.getProdNoLt().intValue(), 11);
        assertEquals(lt.getPositionTournee(), 12);
        assertEquals(lt.getCreneauChargeur(), "CRENEAU");
        assertEquals(lt.getCreneauTournee(), "CRENEAU_NOTIF");

        assertEquals(lt.getAdresse1Destinataire(), ESelectLT.ADRESSE_1_DESTINATAIRE.toString());
        assertEquals(lt.getAdresse1Expediteur(), ESelectLT.ADRESSE_1_EXPEDITEUR.toString());
        assertEquals(lt.getAdresse2Destinataire(), ESelectLT.ADRESSE_2_DESTINATAIRE.toString());
        assertEquals(lt.getAdresse2Expediteur(), ESelectLT.ADRESSE_2_EXPEDITEUR.toString());
        assertEquals(lt.getArticle1(), ESelectLT.ARTICLE_1.toString());
        assertEquals(lt.getCabEvtSaisi(), ESelectLT.CAB_EVT_SAISI.toString());
        assertEquals(lt.getCabRecu(), ESelectLT.CAB_RECU.toString());
        assertEquals(lt.getCodeEtatDestinataire(), ESelectLT.CODE_ETAT_DESTINATAIRE.toString());
        assertEquals(lt.getCodeEtatExpediteur(), ESelectLT.CODE_ETAT_EXPEDITEUR.toString());
        assertEquals(lt.getCodeEvt(), ESelectLT.CODE_EVT.toString());
        assertEquals(lt.getCodeEvtExt(), ESelectLT.CODE_EVT_EXT.toString());
        assertEquals(lt.getCodePaysDestinataire(), ESelectLT.CODE_PAYS_DESTINATAIRE.toString());
        assertEquals(lt.getCodePaysExpediteur(), ESelectLT.CODE_PAYS_EXPEDITEUR.toString());
        assertEquals(lt.getCodePaysNumDestinataire(), ESelectLT.CODE_PAYS_NUM_DESTINATAIRE.toString());
        assertEquals(lt.getCodePaysNumExpediteur(), ESelectLT.CODE_PAYS_NUM_EXPEDITEUR.toString());
        assertEquals(lt.getCodePointRelais(), ESelectLT.CODE_POINT_RELAIS.toString());
        assertEquals(lt.getCodePostalDestinataire(), ESelectLT.CODE_POSTAL_DESTINATAIRE.toString());
        assertEquals(lt.getCodePostalEvt(), ESelectLT.CODE_POSTAL_EVT.toString());
        assertEquals(lt.getCodePostalExpediteur(), ESelectLT.CODE_POSTAL_EXPEDITEUR.toString());
        assertEquals(lt.getCodeProduit(), ESelectLT.CODE_PRODUIT.toString());
        assertEquals(lt.getCodeRaisonEvt(), ESelectLT.CODE_RAISON_EVT.toString());
// ??????        assertEquals(lt.getCode_service(), null);
        assertEquals(lt.getCodeService(), ESelectLT.CODE_SERVICE.toString()); // MODJER chnage null LINE 
        assertEquals(lt.getCrbtRep(), ESelectLT.CRBT_REP.toString());
        assertEquals(lt.getCreateurEvt(), ESelectLT.CREATEUR_EVT.toString());
        assertEquals(lt.getDepotExpediteur(), ESelectLT.DEPOT_EXPEDITEUR.toString());
        assertEquals(lt.getDescription(), ESelectLT.DESCRIPTION.toString());
        assertEquals(lt.getDestinationIdFedex(), ESelectLT.DESTINATION_ID_FEDEX.toString());
        assertEquals(lt.getDeviseAssurance(), ESelectLT.DEVISE_ASSURANCE.toString());
        assertEquals(lt.getDeviseRep(), ESelectLT.DEVISE_REP.toString());
        assertEquals(lt.getDeviseValDeclaree(), ESelectLT.DEVISE_VAL_DECLAREE.toString());
        assertEquals(lt.getDocMarch(), ESelectLT.DOC_MARCH.toString());
        assertEquals(lt.getEmail1Destinataire(), ESelectLT.EMAIL_1_DESTINATAIRE.toString());
        assertEquals(lt.getEmail1Expediteur(), ESelectLT.EMAIL_1_EXPEDITEUR.toString());
        assertEquals(lt.getEmail2Destinataire(), ESelectLT.EMAIL_2_DESTINATAIRE.toString());
        assertEquals(lt.getEmail2Expediteur(), ESelectLT.EMAIL_2_EXPEDITEUR.toString());
        assertEquals(lt.getEsd(), ESelectLT.ESD.toString());
        assertEquals(lt.getEta(), ESelectLT.ETA.toString());
        assertEquals(lt.getIdAbonnement(), ESelectLT.ID_ABONNEMENT.toString());
        assertEquals(lt.getIdAppli(), ESelectLT.ID_APPLI.toString());
        assertEquals(lt.getIdColisClient(), ESelectLT.ID_COLIS_CLIENT.toString());
        assertEquals(lt.getIdExtractionEvt(), ESelectLT.ID_EXTRACTION_EVT.toString());
        assertEquals(lt.getIdLigne(), ESelectLT.ID_LIGNE.toString());
        assertEquals(lt.getJourLivraison(), ESelectLT.JOUR_LIVRAISON.toString());
        assertEquals(lt.getLatitudeDistri(), ESelectLT.LATITUDE_DISTRI.toString());
        assertEquals(lt.getLatitudePrevue(), ESelectLT.LATITUDE_PREVUE.toString());
        assertEquals(lt.getLibelleEvt(), ESelectLT.LIBELLE_EVT.toString());
        assertEquals(lt.getLibelleLieuEvt(), ESelectLT.LIBELLE_LIEU_EVT.toString());
        assertEquals(lt.getLieuEvt(), ESelectLT.LIEU_EVT.toString());
        assertEquals(lt.getLongitudeDistri(), ESelectLT.LONGITUDE_DISTRI.toString());
        assertEquals(lt.getLongitudePrevue(), ESelectLT.LONGITUDE_PREVUE.toString());
        assertEquals(lt.getNoContrat(), ESelectLT.NO_CONTRAT.toString());
        assertEquals(lt.getNoLt(), ESelectLT.NO_LT.toString());
        assertEquals(lt.getNoSsCompte(), ESelectLT.NO_SS_COMPTE.toString());
        assertEquals(lt.getNom1Destinataire(), ESelectLT.NOM_1_DESTINATAIRE.toString());
        assertEquals(lt.getNom1Expediteur(), ESelectLT.NOM_1_EXPEDITEUR.toString());
        assertEquals(lt.getNom2Destinataire(), ESelectLT.NOM_2_DESTINATAIRE.toString());
        assertEquals(lt.getNom2Expediteur(), ESelectLT.NOM_2_EXPEDITEUR.toString());
        assertEquals(lt.getOrigineSaisie(), ESelectLT.ORIGINE_SAISIE.toString());
        assertEquals(lt.getPositionC11(), ESelectLT.POSITION_C11.toString());
        assertEquals(lt.getRefAbonnement(), ESelectLT.REF_ABONNEMENT.toString());
        assertEquals(lt.getRefDestinataire(), ESelectLT.REF_DESTINATAIRE.toString());
        assertEquals(lt.getRefExpediteur(), ESelectLT.REF_EXPEDITEUR.toString());
        assertEquals(lt.getRefExtraction(), ESelectLT.REF_EXTRACTION.toString());
        assertEquals(lt.getRefIdAbonnement(), ESelectLT.REF_ID_ABONNEMENT.toString());
        assertEquals(lt.getSsCodeEvt(), ESelectLT.SS_CODE_EVT.toString());
        assertEquals(lt.getStatusEnvoi(), ESelectLT.STATUS_ENVOI.toString());
        assertEquals(lt.getStatusEvt(), ESelectLT.STATUS_EVT.toString());
        assertEquals(lt.getSynonymeMaitre(), ESelectLT.SYNONYME_MAITRE.toString());
        assertEquals(lt.getTelephoneDestinataire(), ESelectLT.TELEPHONE_DESTINATAIRE.toString());
        assertEquals(lt.getTelephoneExpediteur(), ESelectLT.TELEPHONE_EXPEDITEUR.toString());
        assertEquals(lt.getValDeclaree(), ESelectLT.VAL_DECLAREE.toString());
        assertEquals(lt.getValeurAssuree(), ESelectLT.VALEUR_ASSUREE.toString());
        assertEquals(lt.getValeurRep(), ESelectLT.VALEUR_REP.toString());
        assertEquals(lt.getVilleDestinataire(), ESelectLT.VILLE_DESTINATAIRE.toString());
        assertEquals(lt.getVilleExpediteur(), ESelectLT.VILLE_EXPEDITEUR.toString());

        // --- check all collections 
        assertEquals(lt.getCodesEvt()	, SOME_CODE_EVTS);
        assertEquals(lt.getEvts()		, SOME_EVTS);
        assertEquals(lt.getSynonymes()	, SOME_SYNONYMS);
        assertEquals(lt.getInfoscomp()	, SOME_INFOSCOMP);

    }

    /** @param row used to add all fields.    */
    public void addFields(Row row) {

        Mockito.when(row.getTimestamp(Mockito.eq(ESelectLT.DATE_DEPOT_LT.ordinal()))).thenReturn(new Date(2000000));
        Mockito.when(row.getTimestamp(Mockito.eq(ESelectLT.DATE_DEPOT_LT_INTERN.ordinal()))).thenReturn(new Date(3000000));
        Mockito.when(row.getTimestamp(Mockito.eq(ESelectLT.DATE_ENTREE_SI.ordinal()))).thenReturn(new Date(4000000));
        Mockito.when(row.getTimestamp(Mockito.eq(ESelectLT.DATE_EVT.ordinal()))).thenReturn(new Date(5000000));
        Mockito.when(row.getTimestamp(Mockito.eq(ESelectLT.DATE_LIVRAISON_CONTRACTUELLE.ordinal()))).thenReturn(
                new Date(6000000));
        Mockito.when(row.getTimestamp(Mockito.eq(ESelectLT.DATE_LIVRAISON_PREVUE.ordinal()))).thenReturn(new Date(7000000));
        Mockito.when(row.getTimestamp(Mockito.eq(ESelectLT.DATE_MODIFICATION.ordinal()))).thenReturn(new Date(8000000));

        Mockito.when(row.getInt(Mockito.eq(ESelectLT.HAUTEUR.ordinal()))).thenReturn(1);
        Mockito.when(row.getInt(Mockito.eq(ESelectLT.ID_ACCES_CLIENT.ordinal()))).thenReturn(2);
        Mockito.when(row.getInt(Mockito.eq(ESelectLT.ID_SS_CODE_EVT.ordinal()))).thenReturn(3);
        Mockito.when(row.getInt(Mockito.eq(ESelectLT.IDBCO_EVT.ordinal()))).thenReturn(4);
        Mockito.when(row.getInt(Mockito.eq(ESelectLT.LARGEUR.ordinal()))).thenReturn(5);
        Mockito.when(row.getInt(Mockito.eq(ESelectLT.LONGUEUR.ordinal()))).thenReturn(6);
        Mockito.when(row.getInt(Mockito.eq(ESelectLT.POIDS.ordinal()))).thenReturn(7);
        Mockito.when(row.getInt(Mockito.eq(ESelectLT.POSITION_EVT.ordinal()))).thenReturn(8);
        Mockito.when(row.getInt(Mockito.eq(ESelectLT.PRIORITE_EVT.ordinal()))).thenReturn(9);
        Mockito.when(row.getInt(Mockito.eq(ESelectLT.PROD_CAB_EVT_SAISI.ordinal()))).thenReturn(10);
        Mockito.when(row.getInt(Mockito.eq(ESelectLT.PROD_NO_LT.ordinal()))).thenReturn(11);
        Mockito.when(row.getString(Mockito.eq(ESelectLT.POSITION.ordinal()))).thenReturn("12");
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CODE_SERVICE.ordinal()))).thenReturn("CODE_SERVICE");
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CRENEAU.ordinal()))).thenReturn("CRENEAU");
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CRENEAU_NOTIF.ordinal()))).thenReturn("CRENEAU_NOTIF");

        Mockito.when(row.getString(Mockito.eq(ESelectLT.ADRESSE_1_DESTINATAIRE.ordinal()))).thenReturn(
                ESelectLT.ADRESSE_1_DESTINATAIRE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.ADRESSE_1_EXPEDITEUR.ordinal()))).thenReturn(
                ESelectLT.ADRESSE_1_EXPEDITEUR.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.ADRESSE_2_DESTINATAIRE.ordinal()))).thenReturn(
                ESelectLT.ADRESSE_2_DESTINATAIRE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.ADRESSE_2_EXPEDITEUR.ordinal()))).thenReturn(
                ESelectLT.ADRESSE_2_EXPEDITEUR.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.ARTICLE_1.ordinal()))).thenReturn(
                ESelectLT.ARTICLE_1.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CAB_EVT_SAISI.ordinal()))).thenReturn(
                ESelectLT.CAB_EVT_SAISI.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CAB_RECU.ordinal()))).thenReturn(ESelectLT.CAB_RECU.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CODE_ETAT_DESTINATAIRE.ordinal()))).thenReturn(
                ESelectLT.CODE_ETAT_DESTINATAIRE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CODE_ETAT_EXPEDITEUR.ordinal()))).thenReturn(
                ESelectLT.CODE_ETAT_EXPEDITEUR.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CODE_EVT.ordinal()))).thenReturn(ESelectLT.CODE_EVT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CODE_EVT_EXT.ordinal()))).thenReturn(
                ESelectLT.CODE_EVT_EXT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CODE_PAYS_DESTINATAIRE.ordinal()))).thenReturn(
                ESelectLT.CODE_PAYS_DESTINATAIRE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CODE_PAYS_EXPEDITEUR.ordinal()))).thenReturn(
                ESelectLT.CODE_PAYS_EXPEDITEUR.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CODE_PAYS_NUM_DESTINATAIRE.ordinal()))).thenReturn(
                ESelectLT.CODE_PAYS_NUM_DESTINATAIRE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CODE_PAYS_NUM_EXPEDITEUR.ordinal()))).thenReturn(
                ESelectLT.CODE_PAYS_NUM_EXPEDITEUR.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CODE_POINT_RELAIS.ordinal()))).thenReturn(
                ESelectLT.CODE_POINT_RELAIS.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CODE_POSTAL_DESTINATAIRE.ordinal()))).thenReturn(
                ESelectLT.CODE_POSTAL_DESTINATAIRE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CODE_POSTAL_EVT.ordinal()))).thenReturn(
                ESelectLT.CODE_POSTAL_EVT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CODE_POSTAL_EXPEDITEUR.ordinal()))).thenReturn(
                ESelectLT.CODE_POSTAL_EXPEDITEUR.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CODE_PRODUIT.ordinal()))).thenReturn(
                ESelectLT.CODE_PRODUIT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CODE_RAISON_EVT.ordinal()))).thenReturn(
                ESelectLT.CODE_RAISON_EVT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CODE_SERVICE.ordinal()))).thenReturn(
                ESelectLT.CODE_SERVICE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CRBT_REP.ordinal()))).thenReturn(ESelectLT.CRBT_REP.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.CREATEUR_EVT.ordinal()))).thenReturn(
                ESelectLT.CREATEUR_EVT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.DEPOT_EXPEDITEUR.ordinal()))).thenReturn(
                ESelectLT.DEPOT_EXPEDITEUR.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.DESCRIPTION.ordinal()))).thenReturn(
                ESelectLT.DESCRIPTION.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.DESTINATION_ID_FEDEX.ordinal()))).thenReturn(
                ESelectLT.DESTINATION_ID_FEDEX.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.DEVISE_ASSURANCE.ordinal()))).thenReturn(
                ESelectLT.DEVISE_ASSURANCE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.DEVISE_REP.ordinal()))).thenReturn(
                ESelectLT.DEVISE_REP.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.DEVISE_VAL_DECLAREE.ordinal()))).thenReturn(
                ESelectLT.DEVISE_VAL_DECLAREE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.DOC_MARCH.ordinal()))).thenReturn(
                ESelectLT.DOC_MARCH.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.EMAIL_1_DESTINATAIRE.ordinal()))).thenReturn(
                ESelectLT.EMAIL_1_DESTINATAIRE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.EMAIL_1_EXPEDITEUR.ordinal()))).thenReturn(
                ESelectLT.EMAIL_1_EXPEDITEUR.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.EMAIL_2_DESTINATAIRE.ordinal()))).thenReturn(
                ESelectLT.EMAIL_2_DESTINATAIRE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.EMAIL_2_EXPEDITEUR.ordinal()))).thenReturn(
                ESelectLT.EMAIL_2_EXPEDITEUR.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.ESD.ordinal()))).thenReturn(ESelectLT.ESD.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.ETA.ordinal()))).thenReturn(ESelectLT.ETA.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.ID_ABONNEMENT.ordinal()))).thenReturn(
                ESelectLT.ID_ABONNEMENT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.ID_APPLI.ordinal()))).thenReturn(ESelectLT.ID_APPLI.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.ID_COLIS_CLIENT.ordinal()))).thenReturn(
                ESelectLT.ID_COLIS_CLIENT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.ID_EXTRACTION_EVT.ordinal()))).thenReturn(
                ESelectLT.ID_EXTRACTION_EVT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.ID_LIGNE.ordinal()))).thenReturn(ESelectLT.ID_LIGNE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.JOUR_LIVRAISON.ordinal()))).thenReturn(
                ESelectLT.JOUR_LIVRAISON.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.LATITUDE_DISTRI.ordinal()))).thenReturn(
                ESelectLT.LATITUDE_DISTRI.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.LATITUDE_PREVUE.ordinal()))).thenReturn(
                ESelectLT.LATITUDE_PREVUE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.LIBELLE_EVT.ordinal()))).thenReturn(
                ESelectLT.LIBELLE_EVT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.LIBELLE_LIEU_EVT.ordinal()))).thenReturn(
                ESelectLT.LIBELLE_LIEU_EVT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.LIEU_EVT.ordinal()))).thenReturn(ESelectLT.LIEU_EVT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.LONGITUDE_DISTRI.ordinal()))).thenReturn(
                ESelectLT.LONGITUDE_DISTRI.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.LONGITUDE_PREVUE.ordinal()))).thenReturn(
                ESelectLT.LONGITUDE_PREVUE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.NO_CONTRAT.ordinal()))).thenReturn(
                ESelectLT.NO_CONTRAT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.NO_LT.ordinal()))).thenReturn(ESelectLT.NO_LT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.NO_SS_COMPTE.ordinal()))).thenReturn(
                ESelectLT.NO_SS_COMPTE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.NOM_1_DESTINATAIRE.ordinal()))).thenReturn(
                ESelectLT.NOM_1_DESTINATAIRE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.NOM_1_EXPEDITEUR.ordinal()))).thenReturn(
                ESelectLT.NOM_1_EXPEDITEUR.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.NOM_2_DESTINATAIRE.ordinal()))).thenReturn(
                ESelectLT.NOM_2_DESTINATAIRE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.NOM_2_EXPEDITEUR.ordinal()))).thenReturn(
                ESelectLT.NOM_2_EXPEDITEUR.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.ORIGINE_SAISIE.ordinal()))).thenReturn(
                ESelectLT.ORIGINE_SAISIE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.POSITION_C11.ordinal()))).thenReturn(
                ESelectLT.POSITION_C11.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.REF_ABONNEMENT.ordinal()))).thenReturn(
                ESelectLT.REF_ABONNEMENT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.REF_DESTINATAIRE.ordinal()))).thenReturn(
                ESelectLT.REF_DESTINATAIRE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.REF_EXPEDITEUR.ordinal()))).thenReturn(
                ESelectLT.REF_EXPEDITEUR.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.REF_EXTRACTION.ordinal()))).thenReturn(
                ESelectLT.REF_EXTRACTION.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.REF_ID_ABONNEMENT.ordinal()))).thenReturn(
                ESelectLT.REF_ID_ABONNEMENT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.SS_CODE_EVT.ordinal()))).thenReturn(
                ESelectLT.SS_CODE_EVT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.STATUS_ENVOI.ordinal()))).thenReturn(
                ESelectLT.STATUS_ENVOI.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.STATUS_EVT.ordinal()))).thenReturn(
                ESelectLT.STATUS_EVT.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.SYNONYME_MAITRE.ordinal()))).thenReturn(
                ESelectLT.SYNONYME_MAITRE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.TELEPHONE_DESTINATAIRE.ordinal()))).thenReturn(
                ESelectLT.TELEPHONE_DESTINATAIRE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.TELEPHONE_EXPEDITEUR.ordinal()))).thenReturn(
                ESelectLT.TELEPHONE_EXPEDITEUR.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.VAL_DECLAREE.ordinal()))).thenReturn(
                ESelectLT.VAL_DECLAREE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.VALEUR_ASSUREE.ordinal()))).thenReturn(
                ESelectLT.VALEUR_ASSUREE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.VALEUR_REP.ordinal()))).thenReturn(
                ESelectLT.VALEUR_REP.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.VILLE_DESTINATAIRE.ordinal()))).thenReturn(
                ESelectLT.VILLE_DESTINATAIRE.toString());
        Mockito.when(row.getString(Mockito.eq(ESelectLT.VILLE_EXPEDITEUR.ordinal()))).thenReturn(
                ESelectLT.VILLE_EXPEDITEUR.toString());

        // ------- update collections :  evts, code_evts , synonyms , infoscomp 
        Mockito.when(row.getSet(Mockito.eq(ESelectLT.CODES_EVT.ordinal())	, Mockito.eq(String.class))).thenReturn(SOME_CODE_EVTS);
        Mockito.when(row.getSet(Mockito.eq(ESelectLT.EVTS.ordinal())		, Mockito.eq(String.class))).thenReturn(SOME_EVTS);
        Mockito.when(row.getSet(Mockito.eq(ESelectLT.SYNONYMES.ordinal())	, Mockito.eq(String.class))).thenReturn(SOME_SYNONYMS);
        Mockito.when(row.getMap(Mockito.eq(ESelectLT.INFOSCOMP.ordinal())	, Mockito.eq(String.class),Mockito.eq(String.class))).thenReturn(SOME_INFOSCOMP);

    }

    private void checkEmptyLTFields(Lt lt) {

        assertNull(lt.getDateDepotLt());
        assertNull(lt.getDateDepotLtIntern());
        assertNull(lt.getDateEntreeSi());
        assertNull(lt.getDateEvt());
        assertNull(lt.getDateLivraisonContractuelle());
        assertNull(lt.getDateLivraisonPrevue());
        assertNull(lt.getDateModification());
        assertEquals(lt.getHauteur().intValue(), 0);
        assertEquals(lt.getIdAccesClient().intValue(), 0);
        assertEquals(lt.getIdSsCodeEvt().intValue(), 0);
        assertEquals(lt.getIdbcoEvt().intValue(), 0);
        assertEquals(lt.getLargeur().intValue(), 0);
        assertEquals(lt.getLongueur().intValue(), 0);
        assertEquals(lt.getPoids().intValue(), 0);
        assertEquals(lt.getPositionEvt().intValue(), 0);
        assertEquals(lt.getPrioriteEvt().intValue(), 0);
        assertEquals(lt.getProdCabEvtSaisi().intValue(), 0);
        assertEquals(lt.getProdNoLt().intValue(), 0);
        assertEquals(lt.getPositionTournee(), 0);
        assertNull(lt.getCreneauChargeur());
        assertNull(lt.getCreneauTournee());

        assertNull(lt.getAdresse1Destinataire());
        assertNull(lt.getAdresse1Expediteur());
        assertNull(lt.getAdresse2Destinataire());
        assertNull(lt.getAdresse2Expediteur());
        assertNull(lt.getArticle1());
        assertNull(lt.getCabEvtSaisi());
        assertNull(lt.getCabRecu());
        assertNull(lt.getCodeEtatDestinataire());
        assertNull(lt.getCodeEtatExpediteur());
        assertNull(lt.getCodeEvt());
        assertNull(lt.getCodeEvtExt());
        assertNull(lt.getCodePaysDestinataire());
        assertNull(lt.getCodePaysExpediteur());
        assertNull(lt.getCodePaysNumDestinataire());
        assertNull(lt.getCodePaysNumExpediteur());
        assertNull(lt.getCodePointRelais());
        assertNull(lt.getCodePostalDestinataire());
        assertNull(lt.getCodePostalEvt());
        assertNull(lt.getCodePostalExpediteur());
        assertNull(lt.getCodeProduit());
        assertNull(lt.getCodeRaisonEvt());
        assertNull(lt.getCodeService());
        assertNull(lt.getCrbtRep());
        assertNull(lt.getCreateurEvt());
        assertNull(lt.getDepotExpediteur());
        assertNull(lt.getDescription());
        assertNull(lt.getDestinationIdFedex());
        assertNull(lt.getDeviseAssurance());
        assertNull(lt.getDeviseRep());
        assertNull(lt.getDeviseValDeclaree());
        assertNull(lt.getDocMarch());
        assertNull(lt.getEmail1Destinataire());
        assertNull(lt.getEmail1Expediteur());
        assertNull(lt.getEmail2Destinataire());
        assertNull(lt.getEmail2Expediteur());
        assertNull(lt.getEsd());
        assertNull(lt.getEta());
        assertNull(lt.getIdAbonnement());
        assertNull(lt.getIdAppli());
        assertNull(lt.getIdColisClient());
        assertNull(lt.getIdExtractionEvt());
        assertNull(lt.getIdLigne());
        assertNull(lt.getJourLivraison());
        assertNull(lt.getLatitudeDistri());
        assertNull(lt.getLatitudePrevue());
        assertNull(lt.getLibelleEvt());
        assertNull(lt.getLibelleLieuEvt());
        assertNull(lt.getLieuEvt());
        assertNull(lt.getLongitudeDistri());
        assertNull(lt.getLongitudePrevue());
        assertNull(lt.getNoContrat());
        assertNull(lt.getNoLt());
        assertNull(lt.getNoSsCompte());
        assertNull(lt.getNom1Destinataire());
        assertNull(lt.getNom1Expediteur());
        assertNull(lt.getNom2Destinataire());
        assertNull(lt.getNom2Expediteur());
        assertNull(lt.getOrigineSaisie());
        assertNull(lt.getPositionC11());
        assertNull(lt.getRefAbonnement());
        assertNull(lt.getRefDestinataire());
        assertNull(lt.getRefExpediteur());
        assertNull(lt.getRefExtraction());
        assertNull(lt.getRefIdAbonnement());
        assertNull(lt.getSsCodeEvt());
        assertNull(lt.getStatusEnvoi());
        assertNull(lt.getStatusEvt());
        assertNull(lt.getSynonymeMaitre());
        assertNull(lt.getTelephoneDestinataire());
        assertNull(lt.getTelephoneExpediteur());
        assertNull(lt.getValDeclaree());
        assertNull(lt.getValeurAssuree());
        assertNull(lt.getValeurRep());
        assertNull(lt.getVilleDestinataire());
        assertNull(lt.getVilleExpediteur());
        assertEquals(lt.getCodesEvt().size(), 0);
        assertEquals(lt.getEvts().size(), 0);
        assertEquals(lt.getSynonymes().size(), 0);
        assertEquals(lt.getInfoscomp().size(), 0);

    }
} // EOC 534 lines 
