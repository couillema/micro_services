package com.chronopost.vision.microservices.lt.insert;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.enums.EInsertLT;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Lt;

/**
 * @author JCJ : minor : remove redundant specifications + clean test of
 *         getLtValue ( remove extractValue()) + change extractValue(..) ==>
 *         getLtValue(..)
 **/
public class InsertLtMapperTest {

    private Lt lt;
    private Lt lt4;
    private String dateDuJour;

    @BeforeClass
    public void beforeClass() {

        dateDuJour = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Set<String> codesEvt = new LinkedHashSet<>();
        codesEvt.add("DC");
        codesEvt.add("TA");
        codesEvt.add("D");
        Set<String> evts = new LinkedHashSet<>();

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
        synonymes.add("EE000000002FR");
        lt = new Lt().setNoLt("EE000000001FR").setAdresse1Destinataire("adresse_1_destinataire")
                .setAdresse1Expediteur("adresse_1_expediteur").setAdresse2Destinataire("adresse_2_destinataire")
                .setAdresse2Expediteur("adresse_2_expediteur").setArticle1("article_1")
                .setCodeEtatDestinataire("code_etat_destinataire").setCodeEtatExpediteur("code_etat_expediteur")
                .setCodeEvt("code_evt").setCodeEvtExt("code_evt_ext").setCodePaysDestinataire("code_pays_destinataire")
                .setCodePaysExpediteur("code_pays_expediteur").setCodePaysNumDestinataire("code_pays_num_destinataire")
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
                .setEmail2Destinataire("email_2_destinataire").setEmail2Expediteur("email_2_expediteur").setEsd("esd")
                .setHauteur(0).setIdAbonnement("id_abonnement").setIdAccesClient(0).setIdAppli("id_appli")
                .setIdColisClient("id_colis_client").setIdExtractionEvt("id_extraction_evt").setIdLigne("id_ligne")
                .setIdSsCodeEvt(0).setLargeur(0).setNoContrat("no_contrat").setNoSsCompte("no_ss_compte")
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
                .setSynonymes(synonymes).setSynonymeMaitre("EE000000001FR");


        Set<String> evtsLt3 = new LinkedHashSet<>();

        evtsLt3.add("000117|2015-03-18 15:03:00|NA146848396FR|||DC|||||APRO|2015-03-18T17:07:04|0|715725386||2|Envoi prêt chez l''expéditeur||NA516|||0||EVT_CHR|EDI||Préparation expéditeur|{'119':'899','94':'1/2','77':'20090','76':'FR','231':'GEO/NA146848396248S','118':'%0020090NA146848396248899250','115':'01'}");
        evtsLt3.add("000117|2015-03-18 15:04:00|NA146848396FR|||EA|||||TEST|2015-03-18T17:07:04|0|715725388||129|Envoi faisant partie d''une expédition groupée||NA516|||0||EVT_CHR|EDI||Préparation expéditeur|{'103':'NA146848405FR','94':'2/2','115':'01'}");
        evtsLt3.add("000117|2015-03-18 17:12:00|NA146848396FR|||K|||||PSFPUS|2015-03-18T17:21:57|0|717691165||131|Echange informatique de données||PUS|||0||EVT_CHR|||DIVERS|{'7':'20150318-1712','10':'200360','167':'5O','168':'BP','169':'1111100','170':'87'}");
        evtsLt3.add("000145|2015-03-19 08:15:00|NA146848396FR||%0020090NA146848396248899250|TA|||||TRI1|2015-03-18T22:34:56|0|717493191||88|Envoi en transit||93999|||0||EVT_CHR|AJA0||Acheminement en cours|{'225':'111A','235':'AJA0','239':'TRIEURA','251':'OK'}");
        evtsLt3.add("000146|2015-03-19 08:30:00|NA146848396FR||%0020090NA146848396248899250|P|||||TRI1|2015-03-18T22:34:56|0|717493191||88|Envoi en transit||93999|||0||EVT_CHR|AJA0||Acheminement en cours|{'225':'111A','235':'AJA0','239':'TRIEURA','251':'OK'}");
        evtsLt3.add("000145|"
                + dateDuJour
                + " 07:12:00|NA146848396FR||%0020090NA146848396248899250|TA|||||TRI1|"
                + dateDuJour
                + "T07:12:56|0|717493191||88|Envoi en transit||93999|||0||EVT_CHR|AJA0||Acheminement en cours|{'225':'111A','235':'AJA0','239':'TRIEURA','251':'OK'}");
        evtsLt3.add("000146|"
                + dateDuJour
                + " 08:15:00|NA146848396FR||%0020090NA146848396248899250|TE|||||TRI1|"
                + dateDuJour
                + "T08:15:56|0|717493191||88|Envoi en transit||93999|||0||EVT_CHR|AJA0||Acheminement en cours|{'225':'111A','235':'AJA0','239':'TRIEURA','251':'OK'}");

        Set<String> evtsLt4 = new LinkedHashSet<>();

        evtsLt4.add("000117|2015-03-18 15:03:00|NA146848396FR|||DC|||||APRO|2015-03-18T17:07:04|0|715725386||2|Envoi prêt chez l''expéditeur||NA516|||0||EVT_CHR|EDI||Préparation expéditeur|{'119':'899','94':'1/2','77':'20090','76':'FR','231':'GEO/NA146848396248S','118':'%0020090NA146848396248899250','115':'01'}");
        evtsLt4.add("000117|2015-03-18 15:04:00|NA146848396FR|||EA|||||TEST|2015-03-18T17:07:04|0|715725388||129|Envoi faisant partie d''une expédition groupée||NA516|||0||EVT_CHR|EDI||Préparation expéditeur|{'103':'NA146848405FR','94':'2/2','115':'01'}");
        evtsLt4.add("000117|2015-03-18 17:12:00|NA146848396FR|||K|||||PSFPUS|2015-03-18T17:21:57|0|717691165||131|Echange informatique de données||PUS|||0||EVT_CHR|||DIVERS|{'7':'20150318-1712','10':'200360','167':'5O','168':'BP','169':'1111100','170':'87'}");
        evtsLt4.add("000145|2015-03-19 08:15:00|NA146848396FR||%0020090NA146848396248899250|TA|||||TRI1|2015-03-18T22:34:56|0|717493191||88|Envoi en transit||93999|||0||EVT_CHR|AJA0||Acheminement en cours|{'225':'111A','235':'AJA0','239':'TRIEURA','251':'OK'}");
        evtsLt4.add("000146|2015-03-19 08:30:00|NA146848396FR||%0020090NA146848396248899250|P|||||TRI1|2015-03-18T22:34:56|0|717493191||88|Envoi en transit||93999|||0||EVT_CHR|AJA0||Acheminement en cours|{'225':'111A','235':'AJA0','239':'TRIEURA','251':'OK'}");
        evtsLt4.add("000145|"
                + dateDuJour
                + " 08:15:00|NA146848396FR||%0020090NA146848396248899250|TE|||||TRI1|"
                + dateDuJour
                + "T08:15:56|0|717493191||88|Envoi en transit||93999|||0||EVT_CHR|AJA0||Acheminement en cours|{'225':'111A','235':'AJA0','239':'TRIEURA','251':'OK'}");

        lt4 = new Lt().setNoLt("EE000000004FR").setAdresse1Destinataire("adresse_1_destinataire")
                .setAdresse1Expediteur("adresse_1_expediteur").setAdresse2Destinataire("adresse_2_destinataire")
                .setAdresse2Expediteur("adresse_2_expediteur").setArticle1("article_1")
                .setCodeEtatDestinataire("code_etat_destinataire").setCodeEtatExpediteur("code_etat_expediteur")
                .setCodeEvt("code_evt").setCodeEvtExt("code_evt_ext").setCodePaysDestinataire("code_pays_destinataire")
                .setCodePaysExpediteur("code_pays_expediteur").setCodePaysNumDestinataire("code_pays_num_destinataire")
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
                .setEmail2Destinataire("email_2_destinataire").setEmail2Expediteur("email_2_expediteur").setEsd("esd")
                .setHauteur(0).setIdAbonnement("id_abonnement").setIdAccesClient(0).setIdAppli("id_appli")
                .setIdColisClient("id_colis_client").setIdExtractionEvt("id_extraction_evt").setIdLigne("id_ligne")
                .setIdSsCodeEvt(0).setLargeur(0).setNoContrat("no_contrat").setNoSsCompte("no_ss_compte")
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
                .setLatitudeDistri("latitude_distri").setLongitudeDistri("longitude_distri").setEvts(evtsLt4)
                .setSynonymes(synonymes).setIdxDepassement("2015-01-01__1");
    }

    /** test all date of type Integer Except Position. */
    @Test
    public void extractValuesTest() {

        try {
            assertEquals(InsertLtMapper.getLtValue(EInsertLT.ADRESSE_1_DESTINATAIRE, lt), lt.getAdresse1Destinataire());
            assertEquals(InsertLtMapper.getLtValue(EInsertLT.CRENEAU, lt), lt.getCreneauChargeur());
            assertEquals(InsertLtMapper.getLtValue(EInsertLT.CRENEAU_NOTIF, lt), lt.getCreneauTournee());
            assertEquals(InsertLtMapper.getLtValue(EInsertLT.DATE_EVT, lt), lt.getDateEvt());

            assertEquals(InsertLtMapper.getLtValue(EInsertLT.EVTS, lt), lt.getEvts());
            assertTrue(InsertLtMapper.getLtValue(EInsertLT.HAUTEUR, lt).equals(lt.getHauteur()));
            assertEquals(InsertLtMapper.getLtValue(EInsertLT.INFOSCOMP, lt), lt.getInfoscomp());
            assertEquals(InsertLtMapper.getLtValue(EInsertLT.LATITUDE_DISTRI, lt), lt.getLatitudeDistri());
            assertEquals(InsertLtMapper.getLtValue(EInsertLT.LATITUDE_PREVUE, lt), lt.getLatitudePrevue());
            assertEquals(InsertLtMapper.getLtValue(EInsertLT.SYNONYMES, lt), lt.getSynonymes());
            assertEquals(InsertLtMapper.getLtValue(EInsertLT.IDX_DEPASSEMENT, lt4), lt4.getIdxDepassement());

        } catch (MSTechnicalException e) {
            fail("TechnicalException dans getLtValueTest", e);
        }
    }

    /** Compare of getHauteur() versus extract via LtValue. */
    @Test
    public void getValueTestHauteur() {
        try {
            final Object ltValue = InsertLtMapper.getLtValue(EInsertLT.HAUTEUR, lt);
            assertEquals(ltValue.getClass(), Integer.class);
            assertTrue(ltValue.equals(lt.getHauteur()));
        } catch (MSTechnicalException e) {
            fail("TechnicalException in getValueTestHauteur", e);
        }
    }

    /** Test only positionTournee via EInsertLT.POSITION ( on Lt Value) */
    @Test
    public void getValueTestPosition() {

        try {
            final Object ltValue = InsertLtMapper.getLtValue(EInsertLT.POSITION, lt);
            lt.getPositionTournee();

            assertEquals(ltValue.getClass(), String.class);
            assertTrue(InsertLtMapper.getLtValue(EInsertLT.POSITION, lt).equals("" + lt.getPositionTournee()));

        } catch (MSTechnicalException e) {
            fail("TechnicalException in getValueTestPosition", e);
        }
    }

} // EOC InsertMapperTest

