package com.chronopost.vision.microservices.maintienindexevt.v1;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.maintienIndexEvt.v1.MaintienIndexEvtInput;
import com.chronopost.vision.model.rules.DateRules;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.core.JsonProcessingException;

import fr.chronopost.soap.calculretard.cxf.Analyse;
import fr.chronopost.soap.calculretard.cxf.CalculDateDeLivraisonEstimee;
import fr.chronopost.soap.calculretard.cxf.ResultCalculerRetardPourNumeroLt;
import fr.chronopost.soap.calculretard.cxf.ResultRetard;

/** @author unknown : JJC getSession + LOGGER import min. **/
public class MaintienIndexEvtAcceptanceTest extends JerseyTestNg.ContainerPerClassTest {

	private Client client;
    private boolean suiteLaunch = true;

	private PreparedStatement psCleanTracesDateProactif;

    /**
     * @return VisionMicroserviceApplication.cassandraSession (a
     *         com.datastax.driver.core )
     */
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

    /**
     * Binding the service to the instantiation of the resource
     */
    protected Application configure() {
        final MaintienIndexEvtResource resourceMaintienIndexEvtResource = new MaintienIndexEvtResource();
        resourceMaintienIndexEvtResource.setService(MaintienIndexEvtServiceImpl.getInstance());

        forceSet(TestProperties.CONTAINER_PORT, "0");

        final ResourceConfig config = new ResourceConfig();
        config.register(resourceMaintienIndexEvtResource);

        return config;
    }

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();

        client = ClientBuilder.newClient();

        if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }
        CCMBridge.ipOfNode(1);

		psCleanTracesDateProactif = getSession().prepare(
				"delete from traces_date_proactif where no_lt in ('XXMIE000001FR','XXMIE000002FR','XXMIE000003FR','XXMIE000004FR',"
				+ "'XXMIE000006FR','XXMIE000007FR','XXMIE000008FR')");

        MaintienIndexEvtServiceImpl.getInstance().setDao(MaintienIndexEvtDaoImpl.getInstance());
    }

    /**
     * Test de traitement d'un colis qui n'est pas en retard dans la base mais
     * le devient sur appel au CalculRetard.
     * 
     * @throws Exception
     * @throws JsonProcessingException
     * 
     */
    @Test
    public void testAppelServiceSurNouveauRetardProactif() throws JsonProcessingException, Exception {

    	final MaintienIndexEvtInput maintienIndexEvtData = new MaintienIndexEvtInput();

    	final Lt lt = new Lt().setNoLt("XXMIE000001FR").setNoContrat("19999700");

    	final Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("XXMIE000001FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TO")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

    	final ResultCalculerRetardPourNumeroLt resultCalculRetard = new ResultCalculerRetardPourNumeroLt();
        // Date de livraison estimee == Date de livraison prévue (recalculée)
    	final CalculDateDeLivraisonEstimee calculDLE = new CalculDateDeLivraisonEstimee();
        calculDLE.setDateDeLivraisonEstimee("02/12/2015");
        calculDLE.setHeureMaxDeLivraisonEstimee("13:00");
        calculDLE.setDateDeLivraisonEstimeeCalculee(true);
        resultCalculRetard.setCalculDateDeLivraisonEstimee(calculDLE);

        // Date de livraison prévue == Date de livraison contractuelle (ne
        // change que sur demande du client via CL)
        final ResultRetard resultRetard = new ResultRetard();
        resultRetard.setDateDeLivraisonPrevueCalculee(true);
        resultRetard.setDateDeLivraisonPrevue("01/12/2015 18:00");

        resultCalculRetard.setResultRetard(resultRetard);

        final Analyse analyse = new Analyse();
        analyse.setEnRetardDateEstimeeSupDateContractuelle(1); // Le colis est en retard
        resultCalculRetard.setAnalyse(analyse);

        maintienIndexEvtData.setEvts(Arrays.asList(evt1)).setLt(lt).setResultatCalculRetard(resultCalculRetard);

        final int status = client.target("http://localhost:" + getPort()).path("/MaintienIndexEvt/v1").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(maintienIndexEvtData, MediaType.APPLICATION_JSON_TYPE)).getStatus();

        assertEquals(status, 200);

        // Vérification présence enregistrement dans traces_date_proactif
        final Row row = getSession().execute("SELECT * FROM traces_date_proactif WHERE no_lt = 'XXMIE000001FR'").one();
        assertNotNull(row);
        assertEquals(row.getTimestamp("date_livraison_contractuelle").getTime(),
                DateRules.toTimestampDateWsCalculRetard("01/12/2015 18:00").getTime());
        assertEquals(row.getTimestamp("date_livraison_prevue").getTime(),
                DateRules.toTimestampDateWsCalculRetard("02/12/2015 13:00").getTime());
        assertEquals(row.getString("code_evt"), "TO");

        // Vérification présence enregistrement dans
        // depassement_proactif_par_jour
        /*
         * dateLivraisonContractuelle text, noLt text, infosLt MAP<text, text>,
         * noContrat text,
         */
        final Row row2 = getSession()
                .execute(
                        "SELECT * FROM depassement_proactif_par_jour WHERE date_livraison_contractuelle = '2015-12-01' and no_lt = 'XXMIE000001FR'").one();
        assertNotNull(row2);
        assertEquals(row2.getString("no_contrat"), "19999700");
    }

    /**
     * Test de traitement d'un colis qui est pas en retard dans la base mais ne
     * l'est plus suite à l'appel au CalculRetard.
     * 
     * @throws Exception
     * @throws JsonProcessingException
     * 
     */
    @Test
    public void testAppelServiceSurFinRetardProactif() throws JsonProcessingException, Exception {
        // etat initial : le colis était déjà en retard
        getSession()
                .execute(
                        "INSERT INTO depassement_proactif_par_jour (date_livraison_contractuelle, no_lt, no_contrat) values('2015-12-01', 'XXMIE000002FR', '19999700')");

        final MaintienIndexEvtInput maintienIndexEvtData = new MaintienIndexEvtInput();
        final Lt lt = new Lt().setNoLt("XXMIE000002FR").setIdxDepassement("2015-12-01__1").setNoContrat("19999700");
        final Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("XXMIE000002FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TO")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

        final ResultCalculerRetardPourNumeroLt resultCalculRetard = new ResultCalculerRetardPourNumeroLt();
        // Date de livraison estimee == Date de livraison prévue (recalculée)
        final CalculDateDeLivraisonEstimee calculDLE = new CalculDateDeLivraisonEstimee();
        calculDLE.setDateDeLivraisonEstimee("01/12/2015");
        calculDLE.setHeureMaxDeLivraisonEstimee("13:00");
        calculDLE.setDateDeLivraisonEstimeeCalculee(true);
        resultCalculRetard.setCalculDateDeLivraisonEstimee(calculDLE);

        // Date de livraison prévue == Date de livraison contractuelle (ne
        // change que sur demande du client via CL)
        final ResultRetard resultRetard = new ResultRetard();
        resultRetard.setDateDeLivraisonPrevueCalculee(true);
        resultRetard.setDateDeLivraisonPrevue("01/12/2015 18:00");
        resultCalculRetard.setResultRetard(resultRetard);

        final Analyse analyse = new Analyse();
        analyse.setEnRetardDateEstimeeSupDateContractuelle(0); // Le colis est en retard
        resultCalculRetard.setAnalyse(analyse);
        maintienIndexEvtData.setEvts(Arrays.asList(evt1)).setLt(lt).setResultatCalculRetard(resultCalculRetard);

        final int status = client.target("http://localhost:" + getPort()).path("/MaintienIndexEvt/v1").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(maintienIndexEvtData, MediaType.APPLICATION_JSON_TYPE)).getStatus();
        assertEquals(status, 200);

        // Vérification présence enregistrement dans traces_date_proactif
        final Row row = getSession().execute("SELECT * FROM traces_date_proactif WHERE no_lt = 'XXMIE000002FR'").one();
		assertNotNull(row);
		assertEquals(row.getTimestamp("date_livraison_contractuelle").getTime(),
				DateRules.toTimestampDateWsCalculRetard("01/12/2015 18:00").getTime());
		assertEquals(row.getTimestamp("date_livraison_prevue").getTime(),
				DateRules.toTimestampDateWsCalculRetard("01/12/2015 13:00").getTime());
		assertEquals(row.getString("code_evt"), "TO");

        // Vérification présence enregistrement dans
        // depassement_proactif_par_jour
        /*
         * dateLivraisonContractuelle text, noLt text, infosLt MAP<text, text>,
         * noContrat text,
         */
		final Row row2 = getSession()
				.execute(
						"SELECT * FROM depassement_proactif_par_jour WHERE date_livraison_contractuelle = '2015-12-01' and no_lt = 'XXMIE000002FR'").one();
		assertNotNull(row2);
		assertEquals(row2.getString("no_contrat"), "19999700");
		// Vérification de la suppression
		assertEquals(row2.getString("deleted"), "deleted");
    }

    /**
     * Test de traitement d'un colis qui est en retard dans la base et l'est de
     * nouveau mais sur une autre date contractuelle. On attend 2 appels à
     * updateDepassementProactifParJour (la suppression et l'insertion)
     * 
     * @throws Exception
     * @throws JsonProcessingException
     * 
     */
    @Test
    public void testAppelServiceSurRetardProactifAvecChangementDeDateContractuelle() throws JsonProcessingException,
            Exception {
        // etat initial : le colis était déjà en retard
        getSession()
                .execute(
                        "INSERT INTO depassement_proactif_par_jour (date_livraison_contractuelle, no_lt, no_contrat) values('2015-12-02', 'XXMIE000003FR', '19999700')");

        final MaintienIndexEvtInput maintienIndexEvtData = new MaintienIndexEvtInput();

        final Lt lt = new Lt().setNoLt("XXMIE000003FR").setIdxDepassement("2015-12-02__1").setNoContrat("19999700");

        final Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("XXMIE000003FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TO")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

        final ResultCalculerRetardPourNumeroLt resultCalculRetard = new ResultCalculerRetardPourNumeroLt();
        // Date de livraison estimee == Date de livraison prévue (recalculée)
        final CalculDateDeLivraisonEstimee calculDLE = new CalculDateDeLivraisonEstimee();
        calculDLE.setDateDeLivraisonEstimee("04/12/2015");
        calculDLE.setHeureMaxDeLivraisonEstimee("13:00");
        calculDLE.setDateDeLivraisonEstimeeCalculee(true);
        resultCalculRetard.setCalculDateDeLivraisonEstimee(calculDLE);

        // Date de livraison prévue == Date de livraison contractuelle (ne
        // change que sur demande du client via CL)
        final ResultRetard resultRetard = new ResultRetard();
        resultRetard.setDateDeLivraisonPrevueCalculee(true);
        resultRetard.setDateDeLivraisonPrevue("03/12/2015 18:00");
        resultCalculRetard.setResultRetard(resultRetard);

        final Analyse analyse = new Analyse();
        analyse.setEnRetardDateEstimeeSupDateContractuelle(1); // Le colis est en retard
        resultCalculRetard.setAnalyse(analyse);
        maintienIndexEvtData.setEvts(Arrays.asList(evt1)).setLt(lt).setResultatCalculRetard(resultCalculRetard);

        final int status = client.target("http://localhost:" + getPort()).path("/MaintienIndexEvt/v1").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(maintienIndexEvtData, MediaType.APPLICATION_JSON_TYPE)).getStatus();
        assertEquals(status, 200);

        // Vérification présence enregistrement dans traces_date_proactif
        final Row row = getSession().execute("SELECT * FROM traces_date_proactif WHERE no_lt = 'XXMIE000003FR'").one();
        assertNotNull(row);
        assertEquals(row.getTimestamp("date_livraison_contractuelle").getTime(),
                DateRules.toTimestampDateWsCalculRetard("03/12/2015 18:00").getTime());
        assertEquals(row.getTimestamp("date_livraison_prevue").getTime(),
                DateRules.toTimestampDateWsCalculRetard("04/12/2015 13:00").getTime());
        assertEquals(row.getString("code_evt"), "TO");

        // Vérification présence enregistrement dans
        // depassement_proactif_par_jour
        final Row row2 = getSession()
                .execute(
                        "SELECT * FROM depassement_proactif_par_jour WHERE date_livraison_contractuelle = '2015-12-02' and no_lt = 'XXMIE000003FR'").one();
        assertNotNull(row2);
        assertEquals(row2.getString("no_contrat"), "19999700");
        // Vérification de la suppression
        assertEquals(row2.getString("deleted"), "deleted");

        // Vérification du nouvel enregistrement
        final Row row3 = getSession()
                .execute(
                        "SELECT * FROM depassement_proactif_par_jour WHERE date_livraison_contractuelle = '2015-12-03' and no_lt = 'XXMIE000003FR'").one();
        assertNotNull(row3);
        assertEquals(row3.getString("no_contrat"), "19999700");
        // Vérification de la validité de l'enregistrement
        assertNotEquals(row3.getString("deleted"), "deleted");
    }

    /**
     * Test de traitement d'un colis qui est en retard dans la base mais est
     * livré. On attend 1 appel à updateDepassementProactifParJour (la
     * suppression)
     * 
     * @throws Exception
     * @throws JsonProcessingException
     * 
     */
    @Test
    public void testAppelServiceSurRetardProactifAvecEvtLivraison() throws JsonProcessingException, Exception {
        getSession().execute("truncate depassement_proactif_par_jour");
        // etat initial : le colis était déjà en retard
        getSession()
                .execute(
                        "INSERT INTO depassement_proactif_par_jour (date_livraison_contractuelle, no_lt, no_contrat) values('2015-12-02', 'XXMIE000004FR', '19999700')");

        final MaintienIndexEvtInput maintienIndexEvtData = new MaintienIndexEvtInput();
        final Lt lt = new Lt().setNoLt("XXMIE000004FR").setIdxDepassement("2015-12-02__1");
        final Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("XXMIE000004FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("D")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

        final ResultCalculerRetardPourNumeroLt resultCalculRetard = new ResultCalculerRetardPourNumeroLt();
        // Date de livraison estimee == Date de livraison prévue (recalculée)
        final CalculDateDeLivraisonEstimee calculDLE = new CalculDateDeLivraisonEstimee();
        calculDLE.setDateDeLivraisonEstimeeCalculee(false);
        resultCalculRetard.setCalculDateDeLivraisonEstimee(calculDLE);

        // Date de livraison prévue == Date de livraison contractuelle (ne
        // change que sur demande du client via CL)
        final ResultRetard resultRetard = new ResultRetard();
        resultRetard.setDateDeLivraisonPrevueCalculee(false);
        resultCalculRetard.setResultRetard(resultRetard);

        final Analyse analyse = new Analyse();
        analyse.setEnRetardDateEstimeeSupDateContractuelle(2); // Le colis est en retard
        resultCalculRetard.setAnalyse(analyse);
        maintienIndexEvtData.setEvts(Arrays.asList(evt1)).setLt(lt).setResultatCalculRetard(null);

        final int status = client.target("http://localhost:" + getPort()).path("/MaintienIndexEvt/v1").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(maintienIndexEvtData, MediaType.APPLICATION_JSON_TYPE)).getStatus();
        assertEquals(status, 200);

        // Vérification présence enregistrement dans traces_date_proactif
        final Row row = getSession().execute("SELECT * FROM traces_date_proactif WHERE no_lt = 'XXMIE000004FR'").one();
        assertNotNull(row);

        // Vérification présence enregistrement unique dans
        // depassement_proactif_par_jour
        final Row rowCount = getSession().execute("SELECT count(*) as nb FROM depassement_proactif_par_jour").one();
        assertEquals(rowCount.getLong("nb"), 1l);

        // Vérification présence enregistrement dans
        // depassement_proactif_par_jour
        final Row row2 = getSession()
                .execute(
                        "SELECT * FROM depassement_proactif_par_jour WHERE date_livraison_contractuelle = '2015-12-02' and no_lt = 'XXMIE000004FR'").one();
        assertNotNull(row2);
        // Vérification de la suppression
        assertEquals(row2.getString("deleted"), "deleted");
    }

    /**
     * Test de traitement d'un colis qui est en retard dans la base mais est
     * livré. On attend 1 appel à updateDepassementProactifParJour (la
     * suppression)
     * 
     * @throws Exception
     * @throws JsonProcessingException
     * 
     */
    @Test
    public void testAppelServiceSurRetardProactifSansModif() throws JsonProcessingException, Exception {
    	final MaintienIndexEvtInput maintienIndexEvtData = new MaintienIndexEvtInput();
    	final Lt lt = new Lt().setNoLt("XXMIE000005FR").setIdxDepassement("2015-12-02__1").setNoContrat("19999700");
    	final Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("XXMIE000005FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TA")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

    	final ResultCalculerRetardPourNumeroLt resultCalculRetard = new ResultCalculerRetardPourNumeroLt();
        // Date de livraison estimee == Date de livraison prévue (recalculée)
    	final CalculDateDeLivraisonEstimee calculDLE = new CalculDateDeLivraisonEstimee();
        calculDLE.setDateDeLivraisonEstimeeCalculee(false);
        resultCalculRetard.setCalculDateDeLivraisonEstimee(calculDLE);

        // Date de livraison prévue == Date de livraison contractuelle (ne
        // change que sur demande du client via CL)
        final ResultRetard resultRetard = new ResultRetard();
        resultRetard.setDateDeLivraisonPrevueCalculee(false);
        resultCalculRetard.setResultRetard(resultRetard);

        final Analyse analyse = new Analyse();
        analyse.setEnRetardDateEstimeeSupDateContractuelle(2); // Le colis est en retard
        resultCalculRetard.setAnalyse(analyse);
        maintienIndexEvtData.setEvts(Arrays.asList(evt1)).setLt(lt).setResultatCalculRetard(resultCalculRetard);
    }

    /**
     * Test de traitement d'un colis qui n'est pas en retard dans la base mais
     * le devient sur appel au CalculRetard.
     * 
     * @throws Exception
     * @throws JsonProcessingException
     * 
     */
    @Test
    public void testResultRetardNul() throws JsonProcessingException, Exception {
    	final MaintienIndexEvtInput maintienIndexEvtData = new MaintienIndexEvtInput();
    	final Lt lt = new Lt().setNoLt("XXMIE000006FR").setNoContrat("19999700");
    	final Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("XXMIE000006FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TO")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

    	final ResultCalculerRetardPourNumeroLt resultCalculRetard = new ResultCalculerRetardPourNumeroLt();
        // Date de livraison estimee == Date de livraison prévue (recalculée)
    	final CalculDateDeLivraisonEstimee calculDLE = new CalculDateDeLivraisonEstimee();
        calculDLE.setDateDeLivraisonEstimee("02/12/2015");
        calculDLE.setHeureMaxDeLivraisonEstimee("13:00");
        calculDLE.setDateDeLivraisonEstimeeCalculee(true);
        resultCalculRetard.setCalculDateDeLivraisonEstimee(calculDLE);
        resultCalculRetard.setResultRetard(null);

        final Analyse analyse = new Analyse();
        analyse.setEnRetardDateEstimeeSupDateContractuelle(1); // Le colis est en retard
        resultCalculRetard.setAnalyse(analyse);
        maintienIndexEvtData.setEvts(Arrays.asList(evt1)).setLt(lt).setResultatCalculRetard(resultCalculRetard);

        final int status = client.target("http://localhost:" + getPort()).path("/MaintienIndexEvt/v1").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(maintienIndexEvtData, MediaType.APPLICATION_JSON_TYPE)).getStatus();

        assertEquals(status, 200);

        // Vérification présence enregistrement dans traces_date_proactif
        final Row row = getSession().execute("SELECT * FROM traces_date_proactif WHERE no_lt = 'XXMIE000006FR'").one();
        assertNotNull(row);
        // assertEquals(row.getTimestamp("date_livraison_contractuelle").getTime(),
        // DateRules.toTimestampDateWsCalculRetard("01/12/2015 18:00").getTime());
        assertEquals(row.getTimestamp("date_livraison_prevue").getTime(),
                DateRules.toTimestampDateWsCalculRetard("02/12/2015 13:00").getTime());
        assertEquals(row.getString("code_evt"), "TO");

        // Vérification présence enregistrement dans
        // depassement_proactif_par_jour
        /*
         * dateLivraisonContractuelle text, noLt text, infosLt MAP<text, text>,
         * noContrat text,
         */
        final Row row2 = getSession()
                .execute(
                        "SELECT * FROM depassement_proactif_par_jour WHERE date_livraison_contractuelle = '2015-12-01' and no_lt = 'XXMIE000006FR'").one();
        assertNull(row2);
    }

    /**
     * Test de traitement d'un colis qui n'est pas en retard dans la base mais
     * le devient sur appel au CalculRetard.
     * 
     * @throws Exception
     * @throws JsonProcessingException
     * 
     */
    @Test
    public void testAnalyseRetardNul() throws JsonProcessingException, Exception {
    	final MaintienIndexEvtInput maintienIndexEvtData = new MaintienIndexEvtInput();
    	final Lt lt = new Lt().setNoLt("XXMIE000007FR").setNoContrat("19999700");
    	final Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("XXMIE000007FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TO")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

    	final ResultCalculerRetardPourNumeroLt resultCalculRetard = new ResultCalculerRetardPourNumeroLt();
        // Date de livraison estimee == Date de livraison prévue (recalculée)
    	final CalculDateDeLivraisonEstimee calculDLE = new CalculDateDeLivraisonEstimee();
        calculDLE.setDateDeLivraisonEstimee("02/12/2015");
        calculDLE.setHeureMaxDeLivraisonEstimee("13:00");
        calculDLE.setDateDeLivraisonEstimeeCalculee(true);
        resultCalculRetard.setCalculDateDeLivraisonEstimee(calculDLE);

        // Date de livraison prévue == Date de livraison contractuelle (ne
        // change que sur demande du client via CL)
        final ResultRetard resultRetard = new ResultRetard();
        resultRetard.setDateDeLivraisonPrevueCalculee(true);
        resultRetard.setDateDeLivraisonPrevue("01/12/2015 18:00");
        resultCalculRetard.setResultRetard(resultRetard);
        resultCalculRetard.setAnalyse(null);
        maintienIndexEvtData.setEvts(Arrays.asList(evt1)).setLt(lt).setResultatCalculRetard(resultCalculRetard);

        final int status = client.target("http://localhost:" + getPort()).path("/MaintienIndexEvt/v1").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(maintienIndexEvtData, MediaType.APPLICATION_JSON_TYPE)).getStatus();

        assertEquals(status, 200);

        // Vérification présence enregistrement dans traces_date_proactif
        final Row row = getSession().execute("SELECT * FROM traces_date_proactif WHERE no_lt = 'XXMIE000007FR'").one();
        assertNotNull(row);
        assertEquals(row.getTimestamp("date_livraison_contractuelle").getTime(),
                DateRules.toTimestampDateWsCalculRetard("01/12/2015 18:00").getTime());
        assertEquals(row.getTimestamp("date_livraison_prevue").getTime(),
                DateRules.toTimestampDateWsCalculRetard("02/12/2015 13:00").getTime());
        assertEquals(row.getString("code_evt"), "TO");

        // Vérification présence enregistrement dans
        // depassement_proactif_par_jour
        /*
         * dateLivraisonContractuelle text, noLt text, infosLt MAP<text, text>,
         * noContrat text,
         */
        final Row row2 = getSession()
                .execute(
                        "SELECT * FROM depassement_proactif_par_jour WHERE date_livraison_contractuelle = '2015-12-01' and no_lt = 'XXMIE000007FR'").one();
        assertNull(row2);
    }

    /**
     * Test de traitement d'un colis qui n'est pas en retard dans la base mais
     * le devient sur appel au CalculRetard.
     * 
     * @throws Exception
     * @throws JsonProcessingException
     * 
     */
    @Test
    public void testCalculDateDeLivraisonEstimeeNul() throws JsonProcessingException, Exception {
    	final MaintienIndexEvtInput maintienIndexEvtData = new MaintienIndexEvtInput();
    	final Lt lt = new Lt().setNoLt("XXMIE000008FR").setNoContrat("19999700");
    	final Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("XXMIE000008FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TO")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit").setLieuEvt("93999")
                .setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

    	final ResultCalculerRetardPourNumeroLt resultCalculRetard = new ResultCalculerRetardPourNumeroLt();
        // Date de livraison estimee == Date de livraison prévue (recalculée)
    	final CalculDateDeLivraisonEstimee calculDLE = new CalculDateDeLivraisonEstimee();
        calculDLE.setDateDeLivraisonEstimee("02/12/2015");
        calculDLE.setHeureMaxDeLivraisonEstimee("13:00");
        calculDLE.setDateDeLivraisonEstimeeCalculee(true);
        resultCalculRetard.setCalculDateDeLivraisonEstimee(null);

        // Date de livraison prévue == Date de livraison contractuelle (ne
        // change que sur demande du client via CL)
        final ResultRetard resultRetard = new ResultRetard();
        resultRetard.setDateDeLivraisonPrevueCalculee(true);
        resultRetard.setDateDeLivraisonPrevue("01/12/2015 18:00");
        resultCalculRetard.setResultRetard(resultRetard);
        resultCalculRetard.setAnalyse(null);
        maintienIndexEvtData.setEvts(Arrays.asList(evt1)).setLt(lt).setResultatCalculRetard(resultCalculRetard);

        final int status = client.target("http://localhost:" + getPort()).path("/MaintienIndexEvt/v1").request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(maintienIndexEvtData, MediaType.APPLICATION_JSON_TYPE)).getStatus();

        assertEquals(status, 200);

        // Vérification présence enregistrement dans traces_date_proactif
        final Row row = getSession().execute("SELECT * FROM traces_date_proactif WHERE no_lt = 'XXMIE000008FR'").one();

        assertNotNull(row);
        assertEquals(row.getTimestamp("date_livraison_contractuelle").getTime(),
                DateRules.toTimestampDateWsCalculRetard("01/12/2015 18:00").getTime());
        // assertEquals(row.getTimestamp("date_livraison_prevue").getTime(),
        // DateRules.toTimestampDateWsCalculRetard("02/12/2015 13:00").getTime());
        assertEquals(row.getString("code_evt"), "TO");

        // Vérification présence enregistrement dans
        // depassement_proactif_par_jour
        /*
         * dateLivraisonContractuelle text, noLt text, infosLt MAP<text, text>,
         * noContrat text,
         */
        final Row row2 = getSession()
                .execute(
                        "SELECT * FROM depassement_proactif_par_jour WHERE date_livraison_contractuelle = '2015-12-01' and no_lt = 'XXMIE000008FR'").one();
        assertNull(row2);
    }

    @AfterClass
    public void tearDownAfterClass() throws Exception {
    	getSession().execute(psCleanTracesDateProactif.getQueryString());
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }
}
