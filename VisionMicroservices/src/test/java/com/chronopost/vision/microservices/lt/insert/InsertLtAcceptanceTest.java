package com.chronopost.vision.microservices.lt.insert;

import static org.testng.Assert.assertTrue;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedHashSet;

import org.joda.time.DateTime;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.cassandra.table.ETableLt;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Lt;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;

/** @author ?? : JJC add getSession() **/
public class InsertLtAcceptanceTest {

    private boolean suiteLaunch = true;

    private InsertLtServiceImpl service;

    @BeforeClass(groups = { "init" })
    public void setUp() throws Exception {

        if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }
        CCMBridge.ipOfNode(1);

        service = new InsertLtServiceImpl();
        service.setDao(InsertLtDAO.getInstance());
    }

    /**
     * @return VisionMicroserviceApplication.cassandraSession (a
     *         com.datastax.driver.core )
     */
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

    /**
     * On insère une LT en base via le service et on vérifie les valeurs lues
     * dans la base.
     * 
     * @throws FunctionalException
     * @throws MSTechnicalException
     * 
     */
    @Test(groups = { "database-needed", "slow" })
    public void cas1Test1() throws MSTechnicalException, FunctionalException {
        Lt lt = new Lt().setNoLt("EEINSLT0001FR").setAdresse1Destinataire("adresse_1_destinataire")
                .setAdresse1Expediteur("adresse_1_expediteur").setAdresse2Destinataire("adresse_2_destinataire")
                .setAdresse2Expediteur("adresse_2_expediteur").setArticle1("article_1")
                .setCodeEtatDestinataire("code_etat_destinataire").setCodeEtatExpediteur("code_etat_expediteur")
                .setCodeEvt("code_evt").setCodeEvtExt("code_evt_ext").setCodePaysDestinataire("code_pays_destinataire")
                .setCodePaysExpediteur("code_pays_expediteur").setCodePaysNumDestinataire("code_pays_num_destinataire")
                .setCodePaysNumExpediteur("code_pays_num_expediteur").setCodePointRelais("code_point_relais")
                .setCodePostalDestinataire("code_postal_destinataire").setCodePostalEvt("code_postal_evt")
                .setCodePostalExpediteur("code_postal_expediteur").setCodeProduit("code_produit")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setCrbtRep("crbt_rep")
                .setCreateurEvt("createur_evt").setDateCreationEvt("date_creation_evt")
                .setDateDepotLt(new Timestamp(new DateTime(2015, 01, 01, 10, 00, 00).getMillis()))
                .setDateDepotLtIntern(new Timestamp(new DateTime(2015, 01, 01, 10, 00, 00).getMillis()))
                .setDateEntreeSi(new Timestamp(new DateTime(2015, 01, 01, 10, 00, 00).getMillis()))
                .setDateHeureSaisie("date_heure_saisie")
                .setDateLivraisonContractuelle(new Timestamp(new DateTime(2015, 01, 02, 10, 00, 00).getMillis()))
                .setDateLivraisonPrevue(new Timestamp(new DateTime(2015, 01, 02, 10, 00, 00).getMillis()))
                .setDateModification(new Timestamp(new DateTime(2015, 01, 01, 10, 00, 00).getMillis()))
                .setDepotExpediteur("depot_expediteur").setDescription("description")
                .setDestinationIdFedex("destination_id_fedex").setDeviseAssurance("devise_assurance")
                .setDeviseRep("devise_rep").setDeviseValDeclaree("devise_val_declaree").setDocMarch("doc_march")
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
                .setLatitudeDistri("latitude_distri").setLongitudeDistri("longitude_distri")
                .setSynonymeMaitre("EEINSLT0002FR");

        service.insertLtsInDatabase(Arrays.asList(lt));

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        ResultSet ltResult = getSession().execute("SELECT * FROM lt WHERE no_lt = 'EEINSLT0001FR'");
        Row row = ltResult.one();
        assertTrue(row.getString("no_lt").equals("EEINSLT0001FR"));
        assertTrue(row.getString("adresse_1_destinataire").equals("adresse_1_destinataire"));
        assertTrue(row.getString("adresse_1_expediteur").equals("adresse_1_expediteur"));
        assertTrue(row.getString("adresse_2_destinataire").equals("adresse_2_destinataire"));
        assertTrue(row.getString("adresse_2_expediteur").equals("adresse_2_expediteur"));
        assertTrue(row.getString("article_1").equals("article_1"));
        assertTrue(row.getString("code_etat_destinataire").equals("code_etat_destinataire"));
        assertTrue(row.getString("code_etat_expediteur").equals("code_etat_expediteur"));
        assertTrue(row.getString("code_evt").equals("code_evt"));
        assertTrue(row.getString("code_evt_ext").equals("code_evt_ext"));
        assertTrue(row.getString("code_pays_destinataire").equals("code_pays_destinataire"));
        assertTrue(row.getString("code_pays_expediteur").equals("code_pays_expediteur"));
        assertTrue(row.getString("code_pays_num_destinataire").equals("code_pays_num_destinataire"));
        assertTrue(row.getString("code_pays_num_expediteur").equals("code_pays_num_expediteur"));
        assertTrue(row.getString("code_point_relais").equals("code_point_relais"));
        assertTrue(row.getString("code_postal_destinataire").equals("code_postal_destinataire"));
        assertTrue(row.getString("code_postal_evt").equals("code_postal_evt"));
        assertTrue(row.getString("code_postal_expediteur").equals("code_postal_expediteur"));
        assertTrue(row.getString("code_produit").equals("code_produit"));
        assertTrue(row.getString("code_raison_evt").equals("code_raison_evt"));
        assertTrue(row.getString("code_service").equals("code_service"));
        // assertTrue(row.getString("codes_evt").equals("codes_evt"));
        assertTrue(row.getString("crbt_rep").equals("crbt_rep"));
        assertTrue(row.getString("createur_evt").equals("createur_evt"));
        assertTrue(row.getString("date_creation_evt").equals("date_creation_evt"));
        assertTrue(dateFormatter.format(row.getTimestamp("date_depot_lt")).equals("2015-01-01 10:00:00"));
        assertTrue(dateFormatter.format(row.getTimestamp("date_depot_lt_intern")).equals("2015-01-01 10:00:00"));
        assertTrue(dateFormatter.format(row.getTimestamp("date_entree_si")).equals("2015-01-01 10:00:00"));
        assertTrue(row.getString("date_heure_saisie").equals("date_heure_saisie"));
        assertTrue(dateFormatter.format(row.getTimestamp("date_livraison_contractuelle")).equals("2015-01-02 10:00:00"));
        assertTrue(dateFormatter.format(row.getTimestamp("date_livraison_prevue")).equals("2015-01-02 10:00:00"));
        assertTrue(dateFormatter.format(row.getTimestamp("date_modification")).equals("2015-01-01 10:00:00"));
        assertTrue(row.getString("depot_expediteur").equals("depot_expediteur"));
        assertTrue(row.getString("description").equals("description"));
        assertTrue(row.getString("destination_id_fedex").equals("destination_id_fedex"));
        assertTrue(row.getString("devise_assurance").equals("devise_assurance"));
        assertTrue(row.getString("devise_rep").equals("devise_rep"));
        assertTrue(row.getString("devise_val_declaree").equals("devise_val_declaree"));
        assertTrue(row.getString("doc_march").equals("doc_march"));
        assertTrue(row.getString("email_1_destinataire").equals("email_1_destinataire"));
        assertTrue(row.getString("email_1_expediteur").equals("email_1_expediteur"));
        assertTrue(row.getString("email_2_destinataire").equals("email_2_destinataire"));
        assertTrue(row.getString("email_2_expediteur").equals("email_2_expediteur"));
        assertTrue(row.getString("esd").equals("esd"));
        assertTrue(row.getInt("hauteur") == 0);
        assertTrue(row.getString("id_abonnement").equals("id_abonnement"));
        assertTrue(row.getInt("id_acces_client") == 0);
        assertTrue(row.getString("id_appli").equals("id_appli"));
        assertTrue(row.getString("id_colis_client").equals("id_colis_client"));
        assertTrue(row.getString("id_extraction_evt").equals("id_extraction_evt"));
        assertTrue(row.getString("id_ligne").equals("id_ligne"));
        assertTrue(row.getInt("id_ss_code_evt") == 0);
        assertTrue(row.getInt("largeur") == 0);
        assertTrue(row.getString("no_contrat").equals("no_contrat"));
        assertTrue(row.getString("no_ss_compte").equals("no_ss_compte"));
        assertTrue(row.getString("nom_1_destinataire").equals("nom_1_destinataire"));
        assertTrue(row.getString("nom_1_expediteur").equals("nom_1_expediteur"));
        assertTrue(row.getString("nom_2_destinataire").equals("nom_2_destinataire"));
        assertTrue(row.getString("nom_2_expediteur").equals("nom_2_expediteur"));
        assertTrue(row.getString("origine_saisie").equals("origine_saisie"));
        assertTrue(row.getInt("poids") == 0);
        assertTrue(row.getString("ref_abonnement").equals("ref_abonnement"));
        assertTrue(row.getString("ref_destinataire").equals("ref_destinataire"));
        assertTrue(row.getString("ref_expediteur").equals("ref_expediteur"));
        assertTrue(row.getString("ref_extraction").equals("ref_extraction"));
        assertTrue(row.getString("ref_id_abonnement").equals("ref_id_abonnement"));
        assertTrue(row.getString("status_envoi").equals("status_envoi"));
        assertTrue(row.getString("telephone_destinataire").equals("telephone_destinataire"));
        assertTrue(row.getString("telephone_expediteur").equals("telephone_expediteur"));
        assertTrue(row.getString("val_declaree").equals("val_declaree"));
        assertTrue(row.getString("valeur_assuree").equals("valeur_assuree"));
        assertTrue(row.getString("valeur_rep").equals("valeur_rep"));
        assertTrue(row.getString("ville_destinataire").equals("ville_destinataire"));
        assertTrue(row.getString("ville_expediteur").equals("ville_expediteur"));
        assertTrue(row.getString("eta").equals("18:00"));
        assertTrue(row.getString("position_c11").equals("position_c11"));
        assertTrue(row.getString("creneau").equals("08:00-18:00"));
        assertTrue(row.getString("creneau_notif").equals("08:00-18:00"));
        assertTrue(row.getString("latitude_prevue").equals("latitude_prevue"));
        assertTrue(row.getString("longitude_prevue").equals("longitude_prevue"));
        assertTrue(row.getString("latitude_distri").equals("latitude_distri"));
        assertTrue(row.getString("longitude_distri").equals("longitude_distri"));
        assertTrue(row.getString("synonyme_maitre").equals("EEINSLT0002FR"));
        
        ResultSet scResult = getSession().execute("SELECT * FROM colis_specifications WHERE no_lt = 'EEINSLT0001FR'");
        Row rowSC = scResult.one();
        assertTrue(rowSC.getString("no_contrat").equals("no_contrat"));
        assertTrue(rowSC.getString("code_postal_destinataire").equals("code_postal_destinataire"));
        assertTrue(rowSC.getString("code_produit").equals("code_produit"));
    }

    /**
     * On insère une LT en base directement avec un seul evenement. On met à
     * jour la LT via le service en lui ajoutant 2 evenements. On vérifie enfin
     * que les 3 événements sont bien présents dans l'enregistrement.
     * 
     * @throws FunctionalException
     * @throws MSTechnicalException
     * 
     */
    @Test(groups = { "database-needed", "slow", "acceptance" })
    public void cas1Test2() throws MSTechnicalException, FunctionalException {
        getSession()
                .execute(
                        "INSERT INTO lt (no_lt, evts) VALUES('EEINSLT0003FR',{'000117|2015-03-18 15:03:00|NA146848396FR|||DC|||||APRO|2015-03-18T17:07:04|0|715725386||2|Envoi prêt chez l''expéditeur||NA516|||0||EVT_CHR|EDI||Préparation expéditeur|{''119'':''899'',''94'':''1/2'',''77'':''20090'',''76'':''FR'',''231'':''GEO/NA146848396248S'',''118'':''%0020090NA146848396248899250'',''115'':''01''}'})");
        LinkedHashSet<String> evts = new LinkedHashSet<>();
        evts.add("000117|2015-03-18 15:04:00|NA146848396FR|||EA|||||TEST|2015-03-18T17:07:04|0|715725388||129|Envoi faisant partie d''une expédition groupée||NA516|||0||EVT_CHR|EDI||Préparation expéditeur|{'103':'NA146848405FR','94':'2/2','115':'01'}");
        evts.add("000117|2015-03-18 17:12:00|NA146848396FR|||K|||||PSFPUS|2015-03-18T17:21:57|0|717691165||131|Echange informatique de données||PUS|||0||EVT_CHR|||DIVERS|{'7':'20150318-1712','10':'200360','167':'5O','168':'BP','169':'1111100','170':'87'}");
        Lt lt = new Lt().setNoLt("EEINSLT0003FR").setEvts(evts);

        service.insertLtsInDatabase(Arrays.asList(lt));

        ResultSet ltResult = getSession().execute("SELECT * FROM lt WHERE no_lt = 'EEINSLT0003FR'");
        Row row = ltResult.one();
        assertTrue(row.getString("no_lt").equals("EEINSLT0003FR"));
        assertTrue(row.getSet("evts", String.class).size() == 3);
    }

    @AfterClass(groups = { "init" })
	public void tearDownAfterClass() throws Exception {
		getSession().execute(QueryBuilder.truncate(ETableLt.TABLE_NAME));
		if (!suiteLaunch) {
			BuildCluster.tearDownAfterSuite();
		}
	}
}
