package com.chronopost.vision.microservices.traitementRetard;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.cassandra.table.ETableDateLivraisonEstimeeLt;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.TraitementRetardInput;
import com.chronopost.vision.model.TraitementRetardWork;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import fr.chronopost.soap.calculretard.cxf.CalculDateDeLivraisonEstimee;
import fr.chronopost.soap.calculretard.cxf.ResultCalculerRetardPourNumeroLt;

/** @author unknown : JJC getSession +  LOGGER import min.**/
public class TraitementRetardDaoTest {

	/** @return  VisionMicroserviceApplication.cassandraSession  (a com.datastax.driver.core )  */
    private static final Session getSession() { return VisionMicroserviceApplication.getCassandraSession() ; }

	/** Format retournée par le Calcul Retard : dd/MM/yyyy HH:mm */
	private static SimpleDateFormat CALCULRETARD_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	private static SimpleDateFormat ENBASE_FMT = new SimpleDateFormat("yyyyMMdd");

	private PreparedStatement psCleanDateLivEst;
  
    private ITraitementRetardDao dao = null;

    /** Indicateur */
    private boolean suiteLaunch = true;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
    	if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }
        CCMBridge.ipOfNode(1);
        
        psCleanDateLivEst = getSession().prepare("DELETE FROM date_livraison_estimee_lt WHERE no_lt = ?");

    	dao = TraitementRetardDaoImpl.getInstance();
    	
    	getSession().execute(psCleanDateLivEst.bind("XX123457X"));
    	getSession().execute(psCleanDateLivEst.bind("XX123456X"));
    	getSession().execute(psCleanDateLivEst.bind("XX123458X"));
    	getSession().execute(psCleanDateLivEst.bind("XX123459X"));
    }
    
    /** Demande d'ajout de 2 nouvelles DLE.
     * On doit retrouver les 2 DLE dans la table dateLivraisonEstimeeLt
     */ @Test
    public void InsertDLETest1() throws FunctionalException {
    	List<TraitementRetardWork> retards = new ArrayList<>();
    	
    	TraitementRetardWork retard;

        /* Ajout d'une LT XX123456X qui sera livrée dans 2 jours */
        Lt lt = new Lt();
        lt.setNoLt("XX123456X");
        ResultCalculerRetardPourNumeroLt resultDLE = new ResultCalculerRetardPourNumeroLt();
        resultDLE.setCalculDateDeLivraisonEstimee(new CalculDateDeLivraisonEstimee());
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DATE, 2);
        resultDLE.getCalculDateDeLivraisonEstimee().setDateDeLivraisonEstimee(CALCULRETARD_FMT.format(new Long(cal1.getTimeInMillis()))); // DLE
        
        retard = new TraitementRetardWork();
        retard.setLt(lt);
        retard.setResultCR(resultDLE);
		retards.add(retard);
    	
        /* Ajout d'une LT XX123457X qui sera livrée dans 1 jours */
        lt = new Lt();
        lt.setNoLt("XX123457X");
        resultDLE = new ResultCalculerRetardPourNumeroLt();
        resultDLE.setCalculDateDeLivraisonEstimee(new CalculDateDeLivraisonEstimee());
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.DATE, 1);
        resultDLE.getCalculDateDeLivraisonEstimee().setDateDeLivraisonEstimee(CALCULRETARD_FMT.format(new Long(cal1.getTimeInMillis()))); // DLE
        retard = new TraitementRetardWork();
        retard.setLt(lt);
        retard.setResultCR(resultDLE);
		retards.add(retard);
    	
		/* Methode à tester */
		dao.insertDLE(retards);
		
		/* Vérification en base */
		ResultSet evtResult = getSession()
                .execute("SELECT * FROM date_livraison_estimee_lt WHERE no_lt in ('XX123456X','XX123457X')");
		
		int nbrow = 0;
		for (Row row : evtResult.all()) {
			nbrow++;
			if (nbrow == 1)
				assertEquals(row.getString(ETableDateLivraisonEstimeeLt.NO_LT.getNomColonne()), "XX123456X");
		}
 		assertEquals(nbrow, 2);
    }
    
    /** Demande d'ajout de 1 nouvelle DLE mais le format de la date est incorrect.
     *  On ne doit pas retrouver la DLE dans la table, et on doit avoir recu une exception en retour.
     */ @Test
    public void InsertDLETest2() throws FunctionalException {
    	List<TraitementRetardWork> retards = new ArrayList<>();
    	Boolean exceptionTrapped = false;
    	
    	TraitementRetardWork retard;

        /* Ajout d'une LT XX123459X qui sera livrée dans 2 jours */
        Lt lt = new Lt();
        lt.setNoLt("XX123459X");
        ResultCalculerRetardPourNumeroLt resultDLE = new ResultCalculerRetardPourNumeroLt();
        resultDLE.setCalculDateDeLivraisonEstimee(new CalculDateDeLivraisonEstimee());
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DATE, 2);
       	resultDLE.getCalculDateDeLivraisonEstimee().setDateDeLivraisonEstimee(ENBASE_FMT.format(new Long(cal1.getTimeInMillis()))); // DLE
        
        retard = new TraitementRetardWork();
        retard.setLt(lt);
        retard.setResultCR(resultDLE);
		retards.add(retard);
    	
		/* Methode à tester */
		try {
			dao.insertDLE(retards);
		} catch (FunctionalException e) {
			exceptionTrapped = true;
		}
		
		/* Vérification en base */
		ResultSet evtResult = getSession()
                .execute("SELECT * FROM date_livraison_estimee_lt WHERE no_lt in ('XX123459X')");
		
		int nbrow = evtResult.all().size();	 // for (Row row: evtResult.all()) {	nbrow++;		}
		assertEquals(nbrow, 0);
 		assertTrue(exceptionTrapped);
    }
    
    /** Ajout de 3 DLE de la même Lt dans la table.
     * Puis demande du Max des DLE
     * On doit obtenir la DLE la plus futuriste pour les 3 calculs.
     * @throws ParseException 
     */ @Test
    public void SelectMaxDLETest1() throws FunctionalException, ParseException {
    	List<TraitementRetardInput> retardsInput = new ArrayList<>();
    	List<TraitementRetardWork> retardsWork = new ArrayList<>();
        TraitementRetardInput retard;

        /* Ajout d'une LT XX123456X qui sera livrée dans 2 jours */
        Lt lt = new Lt();
        lt.setNoLt("XX123458X");
        ResultCalculerRetardPourNumeroLt resultDLE = new ResultCalculerRetardPourNumeroLt();
        resultDLE.setCalculDateDeLivraisonEstimee(new CalculDateDeLivraisonEstimee());
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DATE, 2);
        resultDLE.getCalculDateDeLivraisonEstimee().setDateDeLivraisonEstimee(CALCULRETARD_FMT.format(new Long(cal1.getTimeInMillis()))); // DLE
        
        retard = new TraitementRetardInput();
        retard.setLt(lt);
        retard.setResultCR(resultDLE);
		retardsInput.add(retard);
		retardsWork.add(new TraitementRetardWork(new Date(),retard));
    	
        /* Ajout d'une LT XX123457X qui sera livrée dans 1 jours */
        lt = new Lt();
        lt.setNoLt("XX123458X");
        resultDLE = new ResultCalculerRetardPourNumeroLt();
        resultDLE.setCalculDateDeLivraisonEstimee(new CalculDateDeLivraisonEstimee());
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.DATE, 1);
        resultDLE.getCalculDateDeLivraisonEstimee().setDateDeLivraisonEstimee(CALCULRETARD_FMT.format(new Long(cal1.getTimeInMillis()))); // DLE
        retard = new TraitementRetardInput();
        retard.setLt(lt);
        retard.setResultCR(resultDLE);
		retardsInput.add(retard);
		retardsWork.add(new TraitementRetardWork(new Date(),retard));
		
		/* Ajout d'une LT XX123457X qui sera livrée dans 1 jours */
        lt = new Lt();
        lt.setNoLt("XX123458X");
        resultDLE = new ResultCalculerRetardPourNumeroLt();
        resultDLE.setCalculDateDeLivraisonEstimee(new CalculDateDeLivraisonEstimee());
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.DATE, 3);
        resultDLE.getCalculDateDeLivraisonEstimee().setDateDeLivraisonEstimee(CALCULRETARD_FMT.format(new Long(cal1.getTimeInMillis()))); // DLE
        retard = new TraitementRetardInput();
        retard.setLt(lt);
        retard.setResultCR(resultDLE);
		retardsInput.add(retard);
		retardsWork.add(new TraitementRetardWork(new Date(),retard));
    	
		/* Methode à tester */
		dao.insertDLE(retardsWork);
		retardsWork = dao.selectMaxDLE(retardsInput);
		
		/* Vérification */
		for (TraitementRetardWork late : retardsWork) {
			assertEquals(ENBASE_FMT.parse(ENBASE_FMT.format(new Long(cal1.getTimeInMillis()))), late.getMaxDLE());
		}
		assertEquals(retardsInput.size(), 3);
    }

     @AfterClass
     public void tearDownAfterClass() throws Exception {
     	getSession().execute(psCleanDateLivEst.bind("XX123457X"));
     	getSession().execute(psCleanDateLivEst.bind("XX123456X"));
     	getSession().execute(psCleanDateLivEst.bind("XX123458X"));
     	getSession().execute(psCleanDateLivEst.bind("XX123459X"));
         if (!suiteLaunch) {
             BuildCluster.tearDownAfterSuite();
         }
     }
 }
