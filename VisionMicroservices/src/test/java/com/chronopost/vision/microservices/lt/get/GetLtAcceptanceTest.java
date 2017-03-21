package com.chronopost.vision.microservices.lt.get;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.enums.ETraitementSynonymes;
import com.chronopost.vision.model.Lt;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

/** @author unknown : JJC getSession . **/
public class GetLtAcceptanceTest {

    /** format de date limité au jour */
    private static final SimpleDateFormat FORMAT_JOUR = new SimpleDateFormat("dd/MM/yyyy");

    private boolean suiteLaunch = true;

	private PreparedStatement psCleanLt;
	private PreparedStatement psCleanWordIndex;

    /**
     * @return VisionMicroserviceApplication.cassandraSession (a
     *         com.datastax.driver.core )
     */
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

    @BeforeClass(groups = { "init" })
    public void setUp() throws Exception {

        if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }
        CCMBridge.ipOfNode(1);

        getSession()
                .execute(
                        "INSERT INTO lt (no_lt,ref_expediteur,synonyme_maitre, code_service) values('XX000000001FR','REFEXPED_XX000000001FR','XX000000001FR', '899')");
        getSession()
                .execute(
                        "INSERT INTO lt (no_lt,ref_expediteur,synonyme_maitre, code_service) values('XX000000002FR','REFEXPED_XX000000002FR','XX000000001FR', '899')");
        getSession()
                .execute(
                        "INSERT INTO lt (no_lt,ref_expediteur,synonyme_maitre, code_service, date_livraison_prevue) values('XX000000006FR','REFEXPED_XX000000002FR','XX000000001FR', '899',DATEOF(NOW()))");

        // recherche par email desti
        getSession()
                .execute(
                        "INSERT INTO lt (no_lt,ref_expediteur,synonyme_maitre, code_service, email_1_destinataire, email_2_destinataire) values('XX000000003FR','REFEXPED_XX000000003FR','XX000000003FR', '899','adejanovski','pmail.com')");
        getSession()
                .execute(
                        "INSERT INTO lt (no_lt,ref_expediteur,synonyme_maitre, code_service, email_1_destinataire, email_2_destinataire) values('XX000000004FR','REFEXPED_XX000000004FR','XX000000004FR', '899','adejanovski','tmail.com')");
        getSession()
                .execute(
                        "INSERT INTO lt (no_lt,ref_expediteur,synonyme_maitre, code_service, email_1_destinataire, email_2_destinataire) values('XX000000005FR','REFEXPED_XX000000005FR','XX000000005FR', '899','adejanovski','pmail.com')");

        getSession()
                .execute(
                        "INSERT INTO word_index(table_ressource,champ,word,date_ressource,id_ressource) VALUES ('lt','email_1_destinataire','adejanovski','2015-10-24 10:00','XX000000003FR')");
        getSession()
                .execute(
                        "INSERT INTO word_index(table_ressource,champ,word,date_ressource,id_ressource) VALUES ('lt','email_1_destinataire','adejanovski','2015-10-24 10:00','XX000000004FR')");
        getSession()
                .execute(
                        "INSERT INTO word_index(table_ressource,champ,word,date_ressource,id_ressource) VALUES ('lt','email_1_destinataire','adejanovski','2015-10-28 10:00','XX000000005FR')");

        GetLtServiceImpl.getInstance().setDao(GetLtDaoImpl.getInstance().setLtBuilder(new GetLtBuilder()));

        psCleanLt = getSession().prepare("delete from lt where no_lt in ('XX000000001FR', 'XX000000002FR', 'XX000000003FR', 'XX000000004FR', 'XX000000005FR', 'XX000000006FR');");
        psCleanWordIndex = getSession().prepare("delete from word_index where table_ressource = 'lt' and champ = 'email_1_destinataire' and word = 'adejanovski';");
    }

    @Test(groups = { "slow", "acceptance" })
    public void cas1Test1() throws IOException, InterruptedException, ExecutionException, TimeoutException {

        IGetLtService service = GetLtServiceImpl.getInstance();

        Map<String, Lt> lts = service.getLtsFromDatabase(Arrays.asList("XX000000001FR"),
                ETraitementSynonymes.PAS_DE_RESOLUTION_DES_SYNONYMES);
        assertTrue(lts.containsKey("XX000000001FR"));
        assertEquals(lts.get("XX000000001FR").getRefExpediteur(), "REFEXPED_XX000000001FR");

    }

    @Test(groups = { "slow", "acceptance" })
    public void cas1Test2() throws IOException, InterruptedException, ExecutionException, TimeoutException {

        IGetLtService service = GetLtServiceImpl.getInstance();

        Map<String, Lt> lts = service.getLtsFromDatabase(Arrays.asList("XX000000002FR"),
                ETraitementSynonymes.PAS_DE_RESOLUTION_DES_SYNONYMES);
        assertTrue(lts.containsKey("XX000000002FR"));
        assertEquals(lts.get("XX000000002FR").getRefExpediteur(), "REFEXPED_XX000000002FR");

    }

    @Test(groups = { "slow", "acceptance" })
    public void cas1Test3() throws IOException, InterruptedException, ExecutionException, TimeoutException {

        IGetLtService service = GetLtServiceImpl.getInstance();

        Map<String, Lt> lts = service.getLtsFromDatabase(Arrays.asList("XX000000001FR"),
                ETraitementSynonymes.RESOLUTION_DES_SYNONYMES);
        assertTrue(lts.containsKey("XX000000001FR"));
        assertEquals(lts.get("XX000000001FR").getRefExpediteur(), "REFEXPED_XX000000001FR");

    }

    @Test(groups = { "slow", "acceptance" })
    public void cas1Test4() throws IOException, InterruptedException, ExecutionException, TimeoutException {

        IGetLtService service = GetLtServiceImpl.getInstance();

        Map<String, Lt> lts = service.getLtsFromDatabase(Arrays.asList("XX000000002FR"),
                ETraitementSynonymes.RESOLUTION_DES_SYNONYMES);
        assertTrue(lts.containsKey("XX000000002FR"));
        assertEquals(lts.get("XX000000002FR").getRefExpediteur(), "REFEXPED_XX000000001FR");
        assertEquals(lts.get("XX000000002FR").getNoLt(), "XX000000001FR");

    }

    /**
     * Vérification de la remontée des codes service dans le getLt
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @Test(groups = { "slow", "acceptance" })
    public void cas1Test5() throws IOException, InterruptedException, ExecutionException, TimeoutException {

        IGetLtService service = GetLtServiceImpl.getInstance();

        Map<String, Lt> lts = service.getLtsFromDatabase(Arrays.asList("XX000000002FR"),
                ETraitementSynonymes.RESOLUTION_DES_SYNONYMES);
        assertEquals(lts.get("XX000000002FR").getCodeService(), "899");

    }

    /**
     * Vérification de la remontée de la date de livraison prévue dans Ltsmall
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @Test(groups = { "slow", "acceptance" })
    public void cas1Test6() throws IOException, InterruptedException, ExecutionException, TimeoutException {

        IGetLtService service = GetLtServiceImpl.getInstance();

        Map<String, Lt> lts = service.getLtsFromDatabase(Arrays.asList("XX000000006FR"),
                ETraitementSynonymes.PAS_DE_RESOLUTION_DES_SYNONYMES, Boolean.TRUE);
        assertEquals(lts.get("XX000000006FR").getCodeService(), "899");
        assertEquals(FORMAT_JOUR.format(lts.get("XX000000006FR").getDateLivraisonPrevue()),
                FORMAT_JOUR.format(new Date()));

    }

    /**
     * Recherche sur email destinataire, avec restriction de date.
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws ParseException
     */
    @Test(groups = { "slow", "acceptance" })
    public void cas2Test1() throws IOException, InterruptedException, ExecutionException, TimeoutException,
            ParseException {

        IGetLtService service = GetLtServiceImpl.getInstance();

        Map<String, Lt> lts = service.getLtsParEmailDestinataire("adejanovski@pmail.com", new SimpleDateFormat(
                "yyyy-MM-dd HH:mm").parse("2015-10-24 00:00"), new SimpleDateFormat("yyyy-MM-dd HH:mm")
                .parse("2015-10-25 00:00"));
        assertEquals(lts.keySet().size(), 1);
        assertEquals(lts.get("XX000000003FR").getNoLt(), "XX000000003FR");

    }

    /**
     * Recherche sur email destinataire, avec restriction de date plus large.
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws ParseException
     */
    @Test(groups = { "slow", "acceptance" })
    public void cas2Test2() throws IOException, InterruptedException, ExecutionException, TimeoutException,
            ParseException {

        IGetLtService service = GetLtServiceImpl.getInstance();

        Map<String, Lt> lts = service.getLtsParEmailDestinataire("adejanovski@pmail.com", new SimpleDateFormat(
                "yyyy-MM-dd HH:mm").parse("2015-10-24 00:00"), new SimpleDateFormat("yyyy-MM-dd HH:mm")
                .parse("2015-10-30 00:00"));
        assertEquals(lts.keySet().size(), 2);
        assertEquals(lts.get("XX000000003FR").getNoLt(), "XX000000003FR");
        assertEquals(lts.get("XX000000005FR").getNoLt(), "XX000000005FR");

    }

    @AfterClass
    public void tearDownAfterClass() throws Exception {
    	getSession().execute(psCleanLt.getQueryString());
    	getSession().execute(psCleanWordIndex.getQueryString());
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }

}
