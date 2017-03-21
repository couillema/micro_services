package com.chronopost.vision.microservices.lt.insert;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.ConnectionDetails;
// import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Lt;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/** @author unknown : JJC getSession +  LOGGER import min.**/
public class InsertLtServiceImplTest {
    private boolean suiteLaunch=true;
    private IInsertLtService insertLtService;
	private PreparedStatement psCleanLt;

	/** @return  VisionMicroserviceApplication.cassandraSession  (a com.datastax.driver.core )  */
    private static final Session getSession() { return VisionMicroserviceApplication.getCassandraSession() ; }

    @BeforeClass
    public void setUpBeforeClass() throws Exception {

        if (BuildCluster.HOST.equals(System.getProperty("host", ConnectionDetails.getHost()))) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }

        psCleanLt = getSession().prepare("delete from lt where no_lt in ('EE000000001FR','EE000000002FR','EE000000003FR','EE000000004FR')");

        insertLtService = new InsertLtServiceImpl();
        insertLtService.setDao(InsertLtDAO.getInstance());
    }

    /**
     * Test d'insertion de 4 LT avec vérification de leur présence par requêtes.
     * Test avec tag slow a ne pas exécuter dans infinitest
     * 
     * @throws FunctionalException
     * @throws MSTechnicalException
     */
    @Test(groups = { "database-needed", "slow" })
    public void insertLtsInDatabase() throws MSTechnicalException, FunctionalException {
        String dateDuJour = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        // String dateDuJourFormatFr = new
        // SimpleDateFormat("dd-MM-yyyy").format(new Date());
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
        synonymes.add("EE000000002FR");
        Lt lt = new Lt().setNoLt("EE000000001FR").setAdresse1Destinataire("adresse_1_destinataire")
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
                .setNoContrat("no_contrat").setNoSsCompte("no_ss_compte")
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

        Lt lt2 = new Lt().setNoLt("EE000000002FR").setAdresse1Destinataire("adresse_1_destinataire")
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
                .setNoContrat("no_contrat").setNoSsCompte("no_ss_compte")
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
                .setLongitudeDistri("longitude_distri").setEvts(evts).setSynonymes(synonymes);

        LinkedHashSet<String> evtsLt3 = new LinkedHashSet<>();

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

        Lt lt3 = new Lt().setNoLt("EE000000003FR").setAdresse1Destinataire("adresse_1_destinataire")
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
                .setNoContrat("no_contrat").setNoSsCompte("no_ss_compte")
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
                .setLatitudeDistri("latitude_distri").setLongitudeDistri("longitude_distri").setEvts(evtsLt3)
                .setSynonymes(synonymes);

        LinkedHashSet<String> evtsLt4 = new LinkedHashSet<>();

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

        Lt lt4 = new Lt().setNoLt("EE000000004FR").setAdresse1Destinataire("adresse_1_destinataire")
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
                .setNoContrat("no_contrat").setNoSsCompte("no_ss_compte")
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
                .setSynonymes(synonymes);

        List<Lt> ltsToInsert = new ArrayList<>();
        ltsToInsert.add(lt);
        ltsToInsert.add(lt2);
        ltsToInsert.add(lt3);
        ltsToInsert.add(lt4);

        assertTrue(insertLtService.insertLtsInDatabase(ltsToInsert));

        getSession().execute("update vision.lt set no_contrat='no_contrat' where no_lt='" + lt.getNoLt() + "';");
        
        // Vérification de la présence en base des 4 LT
        ResultSet resultSetLt1 = getSession().execute("select * from lt where no_lt = '" + lt.getNoLt() + "'");
        Row rowLt1 = resultSetLt1.one();

        assertEquals(lt.getNoLt(), rowLt1.getString("no_lt"));
        assertEquals(lt.getAdresse1Destinataire(), rowLt1.getString("adresse_1_destinataire"));
        assertEquals(lt.getAdresse1Expediteur(), rowLt1.getString("adresse_1_expediteur"));
        assertEquals(lt.getAdresse2Destinataire(), rowLt1.getString("adresse_2_destinataire"));
        assertEquals(lt.getAdresse2Expediteur(), rowLt1.getString("adresse_2_expediteur"));
        assertEquals(lt.getArticle1(), rowLt1.getString("article_1"));
        assertEquals(lt.getCodeEtatDestinataire(), rowLt1.getString("code_etat_destinataire"));
        assertEquals(lt.getCodeEtatExpediteur(), rowLt1.getString("code_etat_expediteur"));
        assertEquals(lt.getCodeEvt(), rowLt1.getString("code_evt"));
        assertEquals(lt.getCodeEvtExt(), rowLt1.getString("code_evt_ext"));
        assertEquals(lt.getCodePaysDestinataire(), rowLt1.getString("code_pays_destinataire"));
        assertEquals(lt.getCodePaysExpediteur(), rowLt1.getString("code_pays_expediteur"));
        assertEquals(lt.getCodePaysNumDestinataire(), rowLt1.getString("code_pays_num_destinataire"));
        assertEquals(lt.getCodePaysNumExpediteur(), rowLt1.getString("code_pays_num_expediteur"));
        assertEquals(lt.getCodePointRelais(), rowLt1.getString("code_point_relais"));
        assertEquals(lt.getCodePostalDestinataire(), rowLt1.getString("code_postal_destinataire"));
        assertEquals(lt.getCodePostalEvt(), rowLt1.getString("code_postal_evt"));
        assertEquals(lt.getCodePostalExpediteur(), rowLt1.getString("code_postal_expediteur"));
        assertEquals(lt.getCodeProduit(), rowLt1.getString("code_produit"));
        assertEquals(lt.getCodeRaisonEvt(), rowLt1.getString("code_raison_evt"));
        assertEquals(lt.getCodeService(), rowLt1.getString("code_service"));
        assertEquals(lt.getCodesEvt().size(), rowLt1.getSet("codes_evt", String.class).size());
        assertEquals(lt.getCrbtRep(), rowLt1.getString("crbt_rep"));
        assertEquals(lt.getCreateurEvt(), rowLt1.getString("createur_evt"));
        assertEquals(lt.getDateCreationEvt(), rowLt1.getString("date_creation_evt"));
        // assertEquals(lt.getDate_depot_lt(), rowLt1.getString(new
        // Timestamp(0)));
        // assertEquals(lt.getDate_depot_lt_intern(), rowLt1.getString(new
        // Timestamp(0)));
        // assertEquals(lt.getDate_entree_si(), rowLt1.getString(new
        // Timestamp(0)));
        assertEquals(lt.getDateHeureSaisie(), rowLt1.getString("date_heure_saisie"));
        // assertEquals(lt.getDate_livraison_contractuelle(),
        // rowLt1.getString(new Timestamp(0)));
        // assertEquals(lt.getDate_livraison_prevue(), rowLt1.getString(new
        // Timestamp(0)));
        // assertEquals(lt.getDate_modification(), rowLt1.getString(new
        // Timestamp(0)));
        assertEquals(lt.getDepotExpediteur(), rowLt1.getString("depot_expediteur"));
        assertEquals(lt.getDescription(), rowLt1.getString("description"));
        assertEquals(lt.getDestinationIdFedex(), rowLt1.getString("destination_id_fedex"));
        assertEquals(lt.getDeviseAssurance(), rowLt1.getString("devise_assurance"));
        assertEquals(lt.getDeviseRep(), rowLt1.getString("devise_rep"));
        assertEquals(lt.getDeviseValDeclaree(), rowLt1.getString("devise_val_declaree"));
        assertEquals(lt.getDocMarch(), rowLt1.getString("doc_march"));
        assertEquals(lt.getEmail1Destinataire(), rowLt1.getString("email_1_destinataire"));
        assertEquals(lt.getEmail1Expediteur(), rowLt1.getString("email_1_expediteur"));
        assertEquals(lt.getEmail2Destinataire(), rowLt1.getString("email_2_destinataire"));
        assertEquals(lt.getEmail2Expediteur(), rowLt1.getString("email_2_expediteur"));
        assertEquals(lt.getEsd(), rowLt1.getString("esd"));
        assertTrue(lt.getHauteur() == rowLt1.getInt("hauteur"));
        assertEquals(lt.getIdAbonnement(), rowLt1.getString("id_abonnement"));
        assertTrue(lt.getIdAccesClient() == rowLt1.getInt("id_acces_client"));
        assertEquals(lt.getIdAppli(), rowLt1.getString("id_appli"));
        assertEquals(lt.getIdColisClient(), rowLt1.getString("id_colis_client"));
        assertEquals(lt.getIdExtractionEvt(), rowLt1.getString("id_extraction_evt"));
        assertEquals(lt.getIdLigne(), rowLt1.getString("id_ligne"));
        assertTrue(lt.getIdSsCodeEvt() == rowLt1.getInt("id_ss_code_evt"));
        assertTrue(lt.getLargeur() == rowLt1.getInt("largeur"));
        assertEquals(lt.getNoContrat(), rowLt1.getString("no_contrat"));
        assertEquals(lt.getNoSsCompte(), rowLt1.getString("no_ss_compte"));
        assertEquals(lt.getNom1Destinataire(), rowLt1.getString("nom_1_destinataire"));
        assertEquals(lt.getNom1Expediteur(), rowLt1.getString("nom_1_expediteur"));
        assertEquals(lt.getNom2Destinataire(), rowLt1.getString("nom_2_destinataire"));
        assertEquals(lt.getNom2Expediteur(), rowLt1.getString("nom_2_expediteur"));
        assertEquals(lt.getOrigineSaisie(), rowLt1.getString("origine_saisie"));
        assertTrue(lt.getPoids() == rowLt1.getInt("poids"));
        assertEquals(lt.getRefAbonnement(), rowLt1.getString("ref_abonnement"));
        assertEquals(lt.getRefDestinataire(), rowLt1.getString("ref_destinataire"));
        assertEquals(lt.getRefExpediteur(), rowLt1.getString("ref_expediteur"));
        assertEquals(lt.getRefExtraction(), rowLt1.getString("ref_extraction"));
        assertEquals(lt.getRefIdAbonnement(), rowLt1.getString("ref_id_abonnement"));
        assertEquals(lt.getStatusEnvoi(), rowLt1.getString("status_envoi"));
        assertEquals(lt.getTelephoneDestinataire(), rowLt1.getString("telephone_destinataire"));
        assertEquals(lt.getTelephoneExpediteur(), rowLt1.getString("telephone_expediteur"));
        assertEquals(lt.getValDeclaree(), rowLt1.getString("val_declaree"));
        assertEquals(lt.getValeurAssuree(), rowLt1.getString("valeur_assuree"));
        assertEquals(lt.getValeurRep(), rowLt1.getString("valeur_rep"));
        assertEquals(lt.getVilleDestinataire(), rowLt1.getString("ville_destinataire"));
        assertEquals(lt.getVilleExpediteur(), rowLt1.getString("ville_expediteur"));
        assertEquals(lt.getEta(), rowLt1.getString("eta"));
        assertEquals(lt.getPositionC11(), rowLt1.getString("position_c11"));
        assertEquals(lt.getCreneauChargeur(), rowLt1.getString("creneau"));
        assertEquals(lt.getCreneauTournee(), rowLt1.getString("creneau_notif"));
        assertEquals(lt.getLatitudePrevue(), rowLt1.getString("latitude_prevue"));
        assertEquals(lt.getLongitudePrevue(), rowLt1.getString("longitude_prevue"));
        assertEquals(lt.getLatitudeDistri(), rowLt1.getString("latitude_distri"));
        assertEquals(lt.getLongitudeDistri(), rowLt1.getString("longitude_distri"));
        assertEquals(lt.getEvts().size(), rowLt1.getSet("evts", String.class).size());
        assertEquals(lt.getSynonymes().size(), rowLt1.getSet("synonymes", String.class).size());
        assertEquals(lt.getSynonymeMaitre(), rowLt1.getString("synonyme_maitre"));

        ResultSet resultSetLt2 = getSession().execute("select * from lt where no_lt = '" + lt2.getNoLt() + "'");
        Row rowLt2 = resultSetLt2.one();

        assertEquals(lt2.getNoLt(), rowLt2.getString("no_lt"));
        assertEquals(lt2.getAdresse1Destinataire(), rowLt2.getString("adresse_1_destinataire"));
        assertEquals(lt2.getAdresse1Expediteur(), rowLt2.getString("adresse_1_expediteur"));
        assertEquals(lt2.getAdresse2Destinataire(), rowLt2.getString("adresse_2_destinataire"));
        assertEquals(lt2.getAdresse2Expediteur(), rowLt2.getString("adresse_2_expediteur"));
        assertEquals(lt2.getArticle1(), rowLt2.getString("article_1"));
        assertEquals(lt2.getCodeEtatDestinataire(), rowLt2.getString("code_etat_destinataire"));
        assertEquals(lt2.getCodeEtatExpediteur(), rowLt2.getString("code_etat_expediteur"));
        assertEquals(lt2.getCodeEvt(), rowLt2.getString("code_evt"));
        assertEquals(lt2.getCodeEvtExt(), rowLt2.getString("code_evt_ext"));
        assertEquals(lt2.getCodePaysDestinataire(), rowLt2.getString("code_pays_destinataire"));
        assertEquals(lt2.getCodePaysExpediteur(), rowLt2.getString("code_pays_expediteur"));
        assertEquals(lt2.getCodePaysNumDestinataire(), rowLt2.getString("code_pays_num_destinataire"));
        assertEquals(lt2.getCodePaysNumExpediteur(), rowLt2.getString("code_pays_num_expediteur"));
        assertEquals(lt2.getCodePointRelais(), rowLt2.getString("code_point_relais"));
        assertEquals(lt2.getCodePostalDestinataire(), rowLt2.getString("code_postal_destinataire"));
        assertEquals(lt2.getCodePostalEvt(), rowLt2.getString("code_postal_evt"));
        assertEquals(lt2.getCodePostalExpediteur(), rowLt2.getString("code_postal_expediteur"));
        assertEquals(lt2.getCodeProduit(), rowLt2.getString("code_produit"));
        assertEquals(lt2.getCodeRaisonEvt(), rowLt2.getString("code_raison_evt"));
        assertEquals(lt2.getCodeService(), rowLt2.getString("code_service"));
        assertEquals(lt2.getCodesEvt().size(), rowLt2.getSet("codes_evt", String.class).size());
        assertEquals(lt2.getCrbtRep(), rowLt2.getString("crbt_rep"));
        assertEquals(lt2.getCreateurEvt(), rowLt2.getString("createur_evt"));
        assertEquals(lt2.getDateCreationEvt(), rowLt2.getString("date_creation_evt"));
        // assertEquals(lt2.getDate_depot_lt(), rowLt2.getString(new
        // Timestamp(0)));
        // assertEquals(lt2.getDate_depot_lt_intern(), rowLt2.getString(new
        // Timestamp(0)));
        // assertEquals(lt2.getDate_entree_si(), rowLt2.getString(new
        // Timestamp(0)));
        assertEquals(lt2.getDateHeureSaisie(), rowLt2.getString("date_heure_saisie"));
        // assertEquals(lt2.getDate_livraison_contractuelle(),
        // rowLt2.getString(new Timestamp(0)));
        // assertEquals(lt2.getDate_livraison_prevue(), rowLt2.getString(new
        // Timestamp(0)));
        // assertEquals(lt2.getDate_modification(), rowLt2.getString(new
        // Timestamp(0)));
        assertEquals(lt2.getDepotExpediteur(), rowLt2.getString("depot_expediteur"));
        assertEquals(lt2.getDescription(), rowLt2.getString("description"));
        assertEquals(lt2.getDestinationIdFedex(), rowLt2.getString("destination_id_fedex"));
        assertEquals(lt2.getDeviseAssurance(), rowLt2.getString("devise_assurance"));
        assertEquals(lt2.getDeviseRep(), rowLt2.getString("devise_rep"));
        assertEquals(lt2.getDeviseValDeclaree(), rowLt2.getString("devise_val_declaree"));
        assertEquals(lt2.getDocMarch(), rowLt2.getString("doc_march"));
        assertEquals(lt2.getEmail1Destinataire(), rowLt2.getString("email_1_destinataire"));
        assertEquals(lt2.getEmail1Expediteur(), rowLt2.getString("email_1_expediteur"));
        assertEquals(lt2.getEmail2Destinataire(), rowLt2.getString("email_2_destinataire"));
        assertEquals(lt2.getEmail2Expediteur(), rowLt2.getString("email_2_expediteur"));
        assertEquals(lt2.getEsd(), rowLt2.getString("esd"));
        assertTrue(lt2.getHauteur() == rowLt2.getInt("hauteur"));
        assertEquals(lt2.getIdAbonnement(), rowLt2.getString("id_abonnement"));
        assertTrue(lt2.getIdAccesClient() == rowLt2.getInt("id_acces_client"));
        assertEquals(lt2.getIdAppli(), rowLt2.getString("id_appli"));
        assertEquals(lt2.getIdColisClient(), rowLt2.getString("id_colis_client"));
        assertEquals(lt2.getIdExtractionEvt(), rowLt2.getString("id_extraction_evt"));
        assertEquals(lt2.getIdLigne(), rowLt2.getString("id_ligne"));
        assertTrue(lt2.getIdSsCodeEvt() == rowLt2.getInt("id_ss_code_evt"));
        assertTrue(lt2.getLargeur() == rowLt2.getInt("largeur"));
        assertEquals(lt2.getNoContrat(), rowLt2.getString("no_contrat"));
        assertEquals(lt2.getNoSsCompte(), rowLt2.getString("no_ss_compte"));
        assertEquals(lt2.getNom1Destinataire(), rowLt2.getString("nom_1_destinataire"));
        assertEquals(lt2.getNom1Expediteur(), rowLt2.getString("nom_1_expediteur"));
        assertEquals(lt2.getNom2Destinataire(), rowLt2.getString("nom_2_destinataire"));
        assertEquals(lt2.getNom2Expediteur(), rowLt2.getString("nom_2_expediteur"));
        assertEquals(lt2.getOrigineSaisie(), rowLt2.getString("origine_saisie"));
        assertTrue(lt2.getPoids() == rowLt2.getInt("poids"));
        assertEquals(lt2.getRefAbonnement(), rowLt2.getString("ref_abonnement"));
        assertEquals(lt2.getRefDestinataire(), rowLt2.getString("ref_destinataire"));
        assertEquals(lt2.getRefExpediteur(), rowLt2.getString("ref_expediteur"));
        assertEquals(lt2.getRefExtraction(), rowLt2.getString("ref_extraction"));
        assertEquals(lt2.getRefIdAbonnement(), rowLt2.getString("ref_id_abonnement"));
        assertEquals(lt2.getStatusEnvoi(), rowLt2.getString("status_envoi"));
        assertEquals(lt2.getTelephoneDestinataire(), rowLt2.getString("telephone_destinataire"));
        assertEquals(lt2.getTelephoneExpediteur(), rowLt2.getString("telephone_expediteur"));
        assertEquals(lt2.getValDeclaree(), rowLt2.getString("val_declaree"));
        assertEquals(lt2.getValeurAssuree(), rowLt2.getString("valeur_assuree"));
        assertEquals(lt2.getValeurRep(), rowLt2.getString("valeur_rep"));
        assertEquals(lt2.getVilleDestinataire(), rowLt2.getString("ville_destinataire"));
        assertEquals(lt2.getVilleExpediteur(), rowLt2.getString("ville_expediteur"));
        assertEquals(lt2.getEta(), rowLt2.getString("eta"));
        assertEquals(lt2.getPositionC11(), rowLt2.getString("position_c11"));
        assertEquals(lt2.getCreneauChargeur(), rowLt2.getString("creneau"));
        assertEquals(lt2.getCreneauTournee(), rowLt2.getString("creneau_notif"));
        assertEquals(lt2.getLatitudePrevue(), rowLt2.getString("latitude_prevue"));
        assertEquals(lt2.getLongitudePrevue(), rowLt2.getString("longitude_prevue"));
        assertEquals(lt2.getLatitudeDistri(), rowLt2.getString("latitude_distri"));
        assertEquals(lt2.getLongitudeDistri(), rowLt2.getString("longitude_distri"));
        assertEquals(lt2.getEvts().size(), rowLt2.getSet("evts", String.class).size());
        assertEquals(lt2.getSynonymes().size(), rowLt2.getSet("synonymes", String.class).size());
        assertEquals(lt2.getSynonymeMaitre(), rowLt2.getString("synonyme_maitre"));

        ResultSet resultSetLt3 = getSession().execute("select * from lt where no_lt = '" + lt3.getNoLt() + "'");
        Row rowLt3 = resultSetLt3.one();

        assertEquals(lt3.getNoLt(), rowLt3.getString("no_lt"));
        assertEquals(lt3.getAdresse1Destinataire(), rowLt3.getString("adresse_1_destinataire"));
        assertEquals(lt3.getAdresse1Expediteur(), rowLt3.getString("adresse_1_expediteur"));
        assertEquals(lt3.getAdresse2Destinataire(), rowLt3.getString("adresse_2_destinataire"));
        assertEquals(lt3.getAdresse2Expediteur(), rowLt3.getString("adresse_2_expediteur"));
        assertEquals(lt3.getArticle1(), rowLt3.getString("article_1"));
        assertEquals(lt3.getCodeEtatDestinataire(), rowLt3.getString("code_etat_destinataire"));
        assertEquals(lt3.getCodeEtatExpediteur(), rowLt3.getString("code_etat_expediteur"));
        assertEquals(lt3.getCodeEvt(), rowLt3.getString("code_evt"));
        assertEquals(lt3.getCodeEvtExt(), rowLt3.getString("code_evt_ext"));
        assertEquals(lt3.getCodePaysDestinataire(), rowLt3.getString("code_pays_destinataire"));
        assertEquals(lt3.getCodePaysExpediteur(), rowLt3.getString("code_pays_expediteur"));
        assertEquals(lt3.getCodePaysNumDestinataire(), rowLt3.getString("code_pays_num_destinataire"));
        assertEquals(lt3.getCodePaysNumExpediteur(), rowLt3.getString("code_pays_num_expediteur"));
        assertEquals(lt3.getCodePointRelais(), rowLt3.getString("code_point_relais"));
        assertEquals(lt3.getCodePostalDestinataire(), rowLt3.getString("code_postal_destinataire"));
        assertEquals(lt3.getCodePostalEvt(), rowLt3.getString("code_postal_evt"));
        assertEquals(lt3.getCodePostalExpediteur(), rowLt3.getString("code_postal_expediteur"));
        assertEquals(lt3.getCodeProduit(), rowLt3.getString("code_produit"));
        assertEquals(lt3.getCodeRaisonEvt(), rowLt3.getString("code_raison_evt"));
        assertEquals(lt3.getCodeService(), rowLt3.getString("code_service"));
        assertEquals(lt3.getCodesEvt().size(), rowLt3.getSet("codes_evt", String.class).size());
        assertEquals(lt3.getCrbtRep(), rowLt3.getString("crbt_rep"));
        assertEquals(lt3.getCreateurEvt(), rowLt3.getString("createur_evt"));
        assertEquals(lt3.getDateCreationEvt(), rowLt3.getString("date_creation_evt"));
        // assertEquals(lt3.getDate_depot_lt(), rowLt3.getString(new
        // Timestamp(0)));
        // assertEquals(lt3.getDate_depot_lt_intern(), rowLt3.getString(new
        // Timestamp(0)));
        // assertEquals(lt3.getDate_entree_si(), rowLt3.getString(new
        // Timestamp(0)));
        assertEquals(lt3.getDateHeureSaisie(), rowLt3.getString("date_heure_saisie"));
        // assertEquals(lt3.getDate_livraison_contractuelle(),
        // rowLt3.getString(new Timestamp(0)));
        // assertEquals(lt3.getDate_livraison_prevue(), rowLt3.getString(new
        // Timestamp(0)));
        // assertEquals(lt3.getDate_modification(), rowLt3.getString(new
        // Timestamp(0)));
        assertEquals(lt3.getDepotExpediteur(), rowLt3.getString("depot_expediteur"));
        assertEquals(lt3.getDescription(), rowLt3.getString("description"));
        assertEquals(lt3.getDestinationIdFedex(), rowLt3.getString("destination_id_fedex"));
        assertEquals(lt3.getDeviseAssurance(), rowLt3.getString("devise_assurance"));
        assertEquals(lt3.getDeviseRep(), rowLt3.getString("devise_rep"));
        assertEquals(lt3.getDeviseValDeclaree(), rowLt3.getString("devise_val_declaree"));
        assertEquals(lt3.getDocMarch(), rowLt3.getString("doc_march"));
        assertEquals(lt3.getEmail1Destinataire(), rowLt3.getString("email_1_destinataire"));
        assertEquals(lt3.getEmail1Expediteur(), rowLt3.getString("email_1_expediteur"));
        assertEquals(lt3.getEmail2Destinataire(), rowLt3.getString("email_2_destinataire"));
        assertEquals(lt3.getEmail2Expediteur(), rowLt3.getString("email_2_expediteur"));
        assertEquals(lt3.getEsd(), rowLt3.getString("esd"));
        assertTrue(lt3.getHauteur() == rowLt3.getInt("hauteur"));
        assertEquals(lt3.getIdAbonnement(), rowLt3.getString("id_abonnement"));
        assertTrue(lt3.getIdAccesClient() == rowLt3.getInt("id_acces_client"));
        assertEquals(lt3.getIdAppli(), rowLt3.getString("id_appli"));
        assertEquals(lt3.getIdColisClient(), rowLt3.getString("id_colis_client"));
        assertEquals(lt3.getIdExtractionEvt(), rowLt3.getString("id_extraction_evt"));
        assertEquals(lt3.getIdLigne(), rowLt3.getString("id_ligne"));
        assertTrue(lt3.getIdSsCodeEvt() == rowLt3.getInt("id_ss_code_evt"));
        assertTrue(lt3.getLargeur() == rowLt3.getInt("largeur"));
        assertEquals(lt3.getNoContrat(), rowLt3.getString("no_contrat"));
        assertEquals(lt3.getNoSsCompte(), rowLt3.getString("no_ss_compte"));
        assertEquals(lt3.getNom1Destinataire(), rowLt3.getString("nom_1_destinataire"));
        assertEquals(lt3.getNom1Expediteur(), rowLt3.getString("nom_1_expediteur"));
        assertEquals(lt3.getNom2Destinataire(), rowLt3.getString("nom_2_destinataire"));
        assertEquals(lt3.getNom2Expediteur(), rowLt3.getString("nom_2_expediteur"));
        assertEquals(lt3.getOrigineSaisie(), rowLt3.getString("origine_saisie"));
        assertTrue(lt3.getPoids() == rowLt3.getInt("poids"));
        assertEquals(lt3.getRefAbonnement(), rowLt3.getString("ref_abonnement"));
        assertEquals(lt3.getRefDestinataire(), rowLt3.getString("ref_destinataire"));
        assertEquals(lt3.getRefExpediteur(), rowLt3.getString("ref_expediteur"));
        assertEquals(lt3.getRefExtraction(), rowLt3.getString("ref_extraction"));
        assertEquals(lt3.getRefIdAbonnement(), rowLt3.getString("ref_id_abonnement"));
        assertEquals(lt3.getStatusEnvoi(), rowLt3.getString("status_envoi"));
        assertEquals(lt3.getTelephoneDestinataire(), rowLt3.getString("telephone_destinataire"));
        assertEquals(lt3.getTelephoneExpediteur(), rowLt3.getString("telephone_expediteur"));
        assertEquals(lt3.getValDeclaree(), rowLt3.getString("val_declaree"));
        assertEquals(lt3.getValeurAssuree(), rowLt3.getString("valeur_assuree"));
        assertEquals(lt3.getValeurRep(), rowLt3.getString("valeur_rep"));
        assertEquals(lt3.getVilleDestinataire(), rowLt3.getString("ville_destinataire"));
        assertEquals(lt3.getVilleExpediteur(), rowLt3.getString("ville_expediteur"));
        assertEquals(lt3.getEta(), rowLt3.getString("eta"));
        assertEquals(lt3.getPositionC11(), rowLt3.getString("position_c11"));
        assertEquals(lt3.getCreneauChargeur(), rowLt3.getString("creneau"));
        assertEquals(lt3.getCreneauTournee(), rowLt3.getString("creneau_notif"));
        assertEquals(lt3.getLatitudePrevue(), rowLt3.getString("latitude_prevue"));
        assertEquals(lt3.getLongitudePrevue(), rowLt3.getString("longitude_prevue"));
        assertEquals(lt3.getLatitudeDistri(), rowLt3.getString("latitude_distri"));
        assertEquals(lt3.getLongitudeDistri(), rowLt3.getString("longitude_distri"));
        assertEquals(lt3.getEvts().size(), rowLt3.getSet("evts", String.class).size());
        assertEquals(lt3.getSynonymes().size(), rowLt3.getSet("synonymes", String.class).size());
        assertEquals(lt3.getSynonymeMaitre(), rowLt3.getString("synonyme_maitre"));

        ResultSet resultSetLt4 = getSession().execute("select * from lt where no_lt = '" + lt4.getNoLt() + "'");
        Row rowLt4 = resultSetLt4.one();

        assertEquals(lt4.getNoLt(), rowLt4.getString("no_lt"));
        assertEquals(lt4.getAdresse1Destinataire(), rowLt4.getString("adresse_1_destinataire"));
        assertEquals(lt4.getAdresse1Expediteur(), rowLt4.getString("adresse_1_expediteur"));
        assertEquals(lt4.getAdresse2Destinataire(), rowLt4.getString("adresse_2_destinataire"));
        assertEquals(lt4.getAdresse2Expediteur(), rowLt4.getString("adresse_2_expediteur"));
        assertEquals(lt4.getArticle1(), rowLt4.getString("article_1"));
        assertEquals(lt4.getCodeEtatDestinataire(), rowLt4.getString("code_etat_destinataire"));
        assertEquals(lt4.getCodeEtatExpediteur(), rowLt4.getString("code_etat_expediteur"));
        assertEquals(lt4.getCodeEvt(), rowLt4.getString("code_evt"));
        assertEquals(lt4.getCodeEvtExt(), rowLt4.getString("code_evt_ext"));
        assertEquals(lt4.getCodePaysDestinataire(), rowLt4.getString("code_pays_destinataire"));
        assertEquals(lt4.getCodePaysExpediteur(), rowLt4.getString("code_pays_expediteur"));
        assertEquals(lt4.getCodePaysNumDestinataire(), rowLt4.getString("code_pays_num_destinataire"));
        assertEquals(lt4.getCodePaysNumExpediteur(), rowLt4.getString("code_pays_num_expediteur"));
        assertEquals(lt4.getCodePointRelais(), rowLt4.getString("code_point_relais"));
        assertEquals(lt4.getCodePostalDestinataire(), rowLt4.getString("code_postal_destinataire"));
        assertEquals(lt4.getCodePostalEvt(), rowLt4.getString("code_postal_evt"));
        assertEquals(lt4.getCodePostalExpediteur(), rowLt4.getString("code_postal_expediteur"));
        assertEquals(lt4.getCodeProduit(), rowLt4.getString("code_produit"));
        assertEquals(lt4.getCodeRaisonEvt(), rowLt4.getString("code_raison_evt"));
        assertEquals(lt4.getCodeService(), rowLt4.getString("code_service"));
        assertEquals(lt4.getCodesEvt().size(), rowLt4.getSet("codes_evt", String.class).size());
        assertEquals(lt4.getCrbtRep(), rowLt4.getString("crbt_rep"));
        assertEquals(lt4.getCreateurEvt(), rowLt4.getString("createur_evt"));
        assertEquals(lt4.getDateCreationEvt(), rowLt4.getString("date_creation_evt"));
        // assertEquals(lt4.getDate_depot_lt(), rowLt4.getString(new
        // Timestamp(0)));
        // assertEquals(lt4.getDate_depot_lt_intern(), rowLt4.getString(new
        // Timestamp(0)));
        // assertEquals(lt4.getDate_entree_si(), rowLt4.getString(new
        // Timestamp(0)));
        assertEquals(lt4.getDateHeureSaisie(), rowLt4.getString("date_heure_saisie"));
        // assertEquals(lt4.getDate_livraison_contractuelle(),
        // rowLt4.getString(new Timestamp(0)));
        // assertEquals(lt4.getDate_livraison_prevue(), rowLt4.getString(new
        // Timestamp(0)));
        // assertEquals(lt4.getDate_modification(), rowLt4.getString(new
        // Timestamp(0)));
        assertEquals(lt4.getDepotExpediteur(), rowLt4.getString("depot_expediteur"));
        assertEquals(lt4.getDescription(), rowLt4.getString("description"));
        assertEquals(lt4.getDestinationIdFedex(), rowLt4.getString("destination_id_fedex"));
        assertEquals(lt4.getDeviseAssurance(), rowLt4.getString("devise_assurance"));
        assertEquals(lt4.getDeviseRep(), rowLt4.getString("devise_rep"));
        assertEquals(lt4.getDeviseValDeclaree(), rowLt4.getString("devise_val_declaree"));
        assertEquals(lt4.getDocMarch(), rowLt4.getString("doc_march"));
        assertEquals(lt4.getEmail1Destinataire(), rowLt4.getString("email_1_destinataire"));
        assertEquals(lt4.getEmail1Expediteur(), rowLt4.getString("email_1_expediteur"));
        assertEquals(lt4.getEmail2Destinataire(), rowLt4.getString("email_2_destinataire"));
        assertEquals(lt4.getEmail2Expediteur(), rowLt4.getString("email_2_expediteur"));
        assertEquals(lt4.getEsd(), rowLt4.getString("esd"));
        assertTrue(lt4.getHauteur() == rowLt4.getInt("hauteur"));
        assertEquals(lt4.getIdAbonnement(), rowLt4.getString("id_abonnement"));
        assertTrue(lt4.getIdAccesClient() == rowLt4.getInt("id_acces_client"));
        assertEquals(lt4.getIdAppli(), rowLt4.getString("id_appli"));
        assertEquals(lt4.getIdColisClient(), rowLt4.getString("id_colis_client"));
        assertEquals(lt4.getIdExtractionEvt(), rowLt4.getString("id_extraction_evt"));
        assertEquals(lt4.getIdLigne(), rowLt4.getString("id_ligne"));
        assertTrue(lt4.getIdSsCodeEvt() == rowLt4.getInt("id_ss_code_evt"));
        assertTrue(lt4.getLargeur() == rowLt4.getInt("largeur"));
        assertEquals(lt4.getNoContrat(), rowLt4.getString("no_contrat"));
        assertEquals(lt4.getNoSsCompte(), rowLt4.getString("no_ss_compte"));
        assertEquals(lt4.getNom1Destinataire(), rowLt4.getString("nom_1_destinataire"));
        assertEquals(lt4.getNom1Expediteur(), rowLt4.getString("nom_1_expediteur"));
        assertEquals(lt4.getNom2Destinataire(), rowLt4.getString("nom_2_destinataire"));
        assertEquals(lt4.getNom2Expediteur(), rowLt4.getString("nom_2_expediteur"));
        assertEquals(lt4.getOrigineSaisie(), rowLt4.getString("origine_saisie"));
        assertTrue(lt4.getPoids() == rowLt4.getInt("poids"));
        assertEquals(lt4.getRefAbonnement(), rowLt4.getString("ref_abonnement"));
        assertEquals(lt4.getRefDestinataire(), rowLt4.getString("ref_destinataire"));
        assertEquals(lt4.getRefExpediteur(), rowLt4.getString("ref_expediteur"));
        assertEquals(lt4.getRefExtraction(), rowLt4.getString("ref_extraction"));
        assertEquals(lt4.getRefIdAbonnement(), rowLt4.getString("ref_id_abonnement"));
        assertEquals(lt4.getStatusEnvoi(), rowLt4.getString("status_envoi"));
        assertEquals(lt4.getTelephoneDestinataire(), rowLt4.getString("telephone_destinataire"));
        assertEquals(lt4.getTelephoneExpediteur(), rowLt4.getString("telephone_expediteur"));
        assertEquals(lt4.getValDeclaree(), rowLt4.getString("val_declaree"));
        assertEquals(lt4.getValeurAssuree(), rowLt4.getString("valeur_assuree"));
        assertEquals(lt4.getValeurRep(), rowLt4.getString("valeur_rep"));
        assertEquals(lt4.getVilleDestinataire(), rowLt4.getString("ville_destinataire"));
        assertEquals(lt4.getVilleExpediteur(), rowLt4.getString("ville_expediteur"));
        assertEquals(lt4.getEta(), rowLt4.getString("eta"));
        assertEquals(lt4.getPositionC11(), rowLt4.getString("position_c11"));
        assertEquals(lt4.getCreneauChargeur(), rowLt4.getString("creneau"));
        assertEquals(lt4.getCreneauTournee(), rowLt4.getString("creneau_notif"));
        assertEquals(lt4.getLatitudePrevue(), rowLt4.getString("latitude_prevue"));
        assertEquals(lt4.getLongitudePrevue(), rowLt4.getString("longitude_prevue"));
        assertEquals(lt4.getLatitudeDistri(), rowLt4.getString("latitude_distri"));
        assertEquals(lt4.getLongitudeDistri(), rowLt4.getString("longitude_distri"));
        assertEquals(lt4.getEvts().size(), rowLt4.getSet("evts", String.class).size());
        assertEquals(lt4.getSynonymes().size(), rowLt4.getSet("synonymes", String.class).size());
        assertEquals(lt4.getSynonymeMaitre(), rowLt4.getString("synonyme_maitre"));
    }
    
    @AfterClass
    public void tearDownAfterClass() throws Exception {
    	getSession().execute(psCleanLt.getQueryString());
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }
}

