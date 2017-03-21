package com.chronopost.vision.microservices.maintienindexevt.v1;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.maintienindexevt.v1.model.UpdateDepassementProactifInput;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.maintienIndexEvt.v1.MaintienIndexEvtInput;
import com.chronopost.vision.model.rules.DateRules;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.core.JsonProcessingException;

import fr.chronopost.soap.calculretard.cxf.Analyse;
import fr.chronopost.soap.calculretard.cxf.CalculDateDeLivraisonEstimee;
import fr.chronopost.soap.calculretard.cxf.ResultCalculerRetardPourNumeroLt;
import fr.chronopost.soap.calculretard.cxf.ResultRetard;

/** @author unknown : JJC getSession +  LOGGER import min.**/
public class MaintienIndexEvtDaoImplTest {
    
	/** @return  VisionMicroserviceApplication.cassandraSession  (a com.datastax.driver.core )  */
    private static final Session getSession() { return VisionMicroserviceApplication.getCassandraSession() ; }
    private boolean suiteLaunch = true;

	private PreparedStatement psCleanDepasProActif;
	private PreparedStatement psCleanTracesDateProactif;

    @BeforeClass
    public void setUp() throws Exception {

        if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }

        psCleanDepasProActif = getSession().prepare("delete from depassement_proactif_par_jour where date_livraison_contractuelle = ?");
		psCleanTracesDateProactif = getSession().prepare(
				"delete from traces_date_proactif where no_lt in ('XXMIE000001FR','XXMIE000002FR')");
        CCMBridge.ipOfNode(1);
    }

    @Test(groups = { "database-needed", "slow" })
    public void insertTracesDateProactif() throws JsonProcessingException, Exception {
    	MaintienIndexEvtInput maintienIndexEvtData = new MaintienIndexEvtInput();
    	Lt lt = new Lt().setNoLt("XXMIE000001FR");
    	
        
    	Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("XXMIE000001FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TO")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit")
                .setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");

    	ResultCalculerRetardPourNumeroLt resultCalculRetard = new ResultCalculerRetardPourNumeroLt();
    	// Date de livraison estimee == Date de livraison prévue (recalculée)
    	CalculDateDeLivraisonEstimee calculDLE = new CalculDateDeLivraisonEstimee();
    	calculDLE.setDateDeLivraisonEstimee("01/12/2015 13:00");
    	calculDLE.setDateDeLivraisonEstimeeCalculee(true);
    	resultCalculRetard.setCalculDateDeLivraisonEstimee(calculDLE);
    	
    	// Date de livraison prévue == Date de livraison contractuelle (ne change que sur demande du client via CL)
    	ResultRetard resultRetard = new ResultRetard();
    	resultRetard.setDateDeLivraisonPrevueCalculee(true);
    	resultRetard.setDateDeLivraisonPrevue("01/12/2015 18:00");
    	
    	resultCalculRetard.setResultRetard(resultRetard);
    	
    	Analyse analyse = new Analyse();
    	analyse.setEnRetardDateEstimeeSupDateContractuelle(0);
    	
    	resultCalculRetard.setAnalyse(analyse);
    	
    	maintienIndexEvtData.setEvts(Arrays.asList(evt1)).setLt(lt).setResultatCalculRetard(resultCalculRetard);
    	
    	MaintienIndexEvtDaoImpl.getInstance().insertTracesDateProactif(maintienIndexEvtData);
    	    	    	
        ResultSet evtResult = getSession()
                .execute("SELECT * FROM traces_date_proactif WHERE no_lt = 'XXMIE000001FR'");

        Row row = evtResult.one();
        assertNotNull(row);

        assertEquals(row.getString("no_lt"), "XXMIE000001FR");
        assertEquals(row.getString("code_evt"), "TO");
        assertEquals(row.getTimestamp("date_livraison_contractuelle"), new SimpleDateFormat("dd/MM/yyyy HH:mm").parse("01/12/2015 18:00"));
        assertEquals(row.getTimestamp("date_livraison_prevue"), new SimpleDateFormat("dd/MM/yyyy HH:mm").parse("01/12/2015 13:00"));
        assertEquals(row.getString("en_retard"), "0");        
    }
    
    @Test(groups = { "database-needed", "slow" })
    public void insertTracesDateProactifResultRetardNull() throws JsonProcessingException, Exception {
    	MaintienIndexEvtInput maintienIndexEvtData = new MaintienIndexEvtInput();
    	Lt lt = new Lt().setNoLt("XXMIE000002FR");
        
    	Evt evt1 = new Evt().setPrioriteEvt(146)
                .setDateEvt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-03-18 22:26:00"))
                .setNoLt("XXMIE000002FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TO")
                .setCreateurEvt("TRI1").setDateCreationEvt("2015-03-18T22:34:56").setIdAccesClient(0)
                .setIdExtractionEvt("717493191").setIdbcoEvt(88).setLibelleEvt("Envoi en transit")
                .setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
                .setStatusEvt("Acheminement en cours").setInfoscomp(new HashMap<String, String>())
                .setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi").setCodeEvtExt("toto")
                .setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
                .setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1)
                .setRefExtraction("ref_extraction").setStatusEnvoi("status_envoi");
    	    	
    	maintienIndexEvtData.setEvts(Arrays.asList(evt1)).setLt(lt);
    	MaintienIndexEvtDaoImpl.getInstance().insertTracesDateProactif(maintienIndexEvtData);
        ResultSet evtResult = getSession().execute("SELECT * FROM traces_date_proactif WHERE no_lt = 'XXMIE000002FR'");

        Row row = evtResult.one();
        assertNotNull(row);
        assertEquals(row.getString("no_lt"), "XXMIE000002FR");
        assertEquals(row.getString("code_evt"), "TO");
        assertNull(row.getTimestamp("date_livraison_contractuelle"));
        assertNull(row.getTimestamp("date_livraison_prevue"));
        assertEquals(row.getString("en_retard"),"");
    }

    @Test(groups = { "database-needed", "slow" })
    public void updateDepassementProactifParJourTest() throws JsonProcessingException, Exception {
    	Date dateLivraisonPrevue = new Date();
    	Date dateLivraisonContractuelle = new Date();
    	
    	UpdateDepassementProactifInput inputData = new UpdateDepassementProactifInput().setDateLivraisonContractuelle(dateLivraisonContractuelle)
    																				   .setDateLivraisonPrevue(dateLivraisonPrevue)
    																				   .setLt(new Lt().setNoLt("EEMAINIDX01FR").setNoContrat("19999700"))
    																				   .setNoLt("EEMAINIDX01FR")
    																				   .setDeleted("deleted")
    																				   ;
    	
    	MaintienIndexEvtDaoImpl.getInstance().updateDepassementProactifParJour(inputData);
    	ResultSet evtResult = getSession().execute("SELECT * FROM depassement_proactif_par_jour WHERE date_livraison_contractuelle = '" + DateRules.toDateSortable(dateLivraisonContractuelle) + "' and no_lt = 'EEMAINIDX01FR'");
    	
        Row row = evtResult.one();
        assertNotNull(row);
        assertEquals(row.getString("date_livraison_contractuelle"),DateRules.toDateSortable(dateLivraisonContractuelle));
        assertEquals(row.getMap("infos_lt", String.class, String.class).get("date_livraison_prevue"), DateRules.toDateAndTimeSortable(dateLivraisonPrevue));
    }
    
    @AfterClass
    public void tearDownAfterClass() throws Exception {
    	getSession().execute(psCleanDepasProActif.bind(DateRules.toDateSortable(new Date())));
    	getSession().execute(psCleanDepasProActif.bind("2015-12-02"));
    	getSession().execute(psCleanTracesDateProactif.getQueryString());
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }
}
