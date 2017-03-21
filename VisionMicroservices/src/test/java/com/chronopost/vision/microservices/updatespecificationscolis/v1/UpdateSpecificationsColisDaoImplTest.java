package com.chronopost.vision.microservices.updatespecificationscolis.v1;

import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_ANNUL;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_NIMPORTE_QUOI;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis;
import com.chronopost.vision.model.updatespecificationscolis.v1.EInfoSupplementaire;
import com.chronopost.vision.model.updatespecificationscolis.v1.ESpecificiteColis;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;
import com.datastax.driver.core.PreparedStatement;

public class UpdateSpecificationsColisDaoImplTest {

    private UpdateSpecificationsColisDaoImpl dao = null;

    private List<SpecifsColis> specifsColis;

    /** Indicateur */
    private boolean suiteLaunch = true;
	/** PreparedStatement pour vider les colis saisi et a saisir sur l'agence */
	private PreparedStatement psTruncateColisSpec;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }
        CCMBridge.ipOfNode(1);
        
        psTruncateColisSpec = VisionMicroserviceApplication.getCassandraSession().prepare("truncate colis_specifications");

        dao = UpdateSpecificationsColisDaoImpl.getInstance();
    }

    @BeforeMethod
    public void beforeMethod() {
        specifsColis = new ArrayList<>();
    }

    @Test
    public void updateSpecifsServices() {

        UpdateSpecificationsColisDaoUtils.delSpecificationColis("20160101000000");
        UpdateSpecificationsColisDaoUtils.delSpecificationColis("20160102000000");

        // initialisation
        {
            SpecifsColis colis = new SpecifsColis();
            colis.setNoLt("20160101000000");
            Date dateId = new DateTime(2016, 1, 1, 0, 0, 0).toDate();
            colis.setSpecifsService(new HashMap<Date, Set<String>>());
            colis.getSpecifsService().put(dateId, new HashSet<String>());
            colis.getSpecifsService().get(dateId).add("SWAP");
            colis.getService().put(dateId, "666");
            specifsColis.add(colis);
        }
        {
            SpecifsColis colis = new SpecifsColis();
            colis.setNoLt("20160102000000");
            Date dateId = new DateTime(2016, 1, 2, 0, 0, 0).toDate();
            colis.setSpecifsService(new HashMap<Date, Set<String>>());
            colis.getSpecifsService().put(dateId, new HashSet<String>());
            colis.getSpecifsService().get(dateId).add("REP");
            colis.getService().put(dateId, "667");
            specifsColis.add(colis);
        }

        // execution
        dao.updateSpecifsServices(specifsColis);

        // verification
        SpecifsColis colis1 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("20160101000000");
        SpecifsColis colis2 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("20160102000000");
        assertTrue(colis1.getSpecifsService().get(new DateTime(2016, 1, 1, 0, 0, 0).toDate()).contains("SWAP")) ;
        assertTrue(colis2.getSpecifsService().get(new DateTime(2016, 1, 2, 0, 0, 0).toDate()).contains("REP")) ;
        assertEquals("666", colis1.getService().get(new DateTime(2016, 1, 1, 0, 0, 0).toDate())) ;
        assertEquals("667", colis2.getService().get(new DateTime(2016, 1, 2, 0, 0, 0).toDate())) ;
    }

    @Test
    public void updateConsignes() {
        UpdateSpecificationsColisDaoUtils.delSpecificationColis("20160101000000");
        UpdateSpecificationsColisDaoUtils.delSpecificationColis("20160102000000");

        // initialisation
        {
            SpecifsColis colis = new SpecifsColis();
            colis.setNoLt("20160101000000");
            Date dateId = new DateTime(2016, 1, 1, 0, 0, 0).toDate();
            colis.setConsignesRecues(new HashMap<Date, String>());
            colis.getConsignesRecues().put(dateId, VALUE_NIMPORTE_QUOI);
            specifsColis.add(colis);
        }
        {
            SpecifsColis colis = new SpecifsColis();
            colis.setNoLt("20160102000000");
            Date dateId = new DateTime(2016, 1, 2, 0, 0, 0).toDate();
            colis.setConsignesRecues(new HashMap<Date, String>());
            colis.getConsignesRecues().put(dateId, VALUE_NIMPORTE_QUOI);
            colis.setConsignesAnnulees(new HashMap<Date, String>());
            colis.getConsignesAnnulees().put(dateId, VALUE_ANNUL);
            specifsColis.add(colis);
        }

        // execution
        dao.updateConsignes(specifsColis);
        
        SpecifsColis colis1 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("20160101000000");
        SpecifsColis colis2 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("20160102000000");

        // verification
        assertTrue(colis1.getConsignesAnnulees().isEmpty()) ;
        assertEquals(VALUE_NIMPORTE_QUOI, colis1.getConsignesRecues().get(new DateTime(2016, 1, 1, 0, 0, 0).toDate())) ;
        assertTrue(colis1.getConsignesTraitees().isEmpty()) ;

        assertEquals(VALUE_ANNUL, colis2.getConsignesAnnulees().get(new DateTime(2016, 1, 2, 0, 0, 0).toDate())) ;
        assertEquals(VALUE_NIMPORTE_QUOI, colis2.getConsignesRecues().get(new DateTime(2016, 1, 2, 0, 0, 0).toDate())) ;
        assertTrue(colis2.getConsignesTraitees().isEmpty()) ;
    }

    @Test
    public void updateSpecifsEvenements() {

        UpdateSpecificationsColisDaoUtils.delSpecificationColis("20160101000000");
        UpdateSpecificationsColisDaoUtils.delSpecificationColis("20160102000000");
        UpdateSpecificationsColisDaoUtils.delSpecificationColis("20160201000000");
        UpdateSpecificationsColisDaoUtils.delSpecificationColis("20160301000000");
        UpdateSpecificationsColisDaoUtils.delSpecificationColis("20160401000000");
        
        // initialisation
        {
            SpecifsColis colis = new SpecifsColis();
            colis.setNoLt("20160101000000");
            Date dateId = new DateTime(2016, 1, 1, 0, 0, 0).toDate();
            colis.setSpecifsEvt(new HashMap<Date, String>());
            colis.getSpecifsEvt().put(dateId, ESpecificiteColis.ATTRACTIF.getCode());
            specifsColis.add(colis);
        }
        {
            SpecifsColis colis = new SpecifsColis();
            colis.setNoLt("20160102000000");
            colis.setSpecifsEvt(new HashMap<Date, String>());
            colis.getSpecifsEvt().put(new DateTime(2016, 1, 2, 0, 0, 0).toDate(), ESpecificiteColis.SENSIBLE.getCode());
            colis.getSpecifsEvt().put(new DateTime(2016, 1, 2, 12, 0, 0).toDate(), ESpecificiteColis.CONSIGNE.getCode());
            specifsColis.add(colis);
        }
        {
            SpecifsColis colis = new SpecifsColis();
            colis.setNoLt("20160201000000");
            Date dateId = new DateTime(2016, 2, 1, 0, 0, 0).toDate();
            colis.getEtapes().put(dateId, EEtapesColis.RETOUR_AGENCE.getCode());
            specifsColis.add(colis);
        }
        {
            SpecifsColis colis = new SpecifsColis();
            colis.setNoLt("20160301000000");
            Date dateId = new DateTime(2016, 3, 1, 0, 0, 0).toDate();
            colis.setDatesContractuelles(new HashMap<Date, Date>());
            colis.getDatesContractuelles().put(dateId, new DateTime(2016, 2, 29, 0, 0, 0).toDate());
            specifsColis.add(colis);
        }
        {
            SpecifsColis colis = new SpecifsColis();
            colis.setNoLt("20160401000000");
            //colis.setInfoSupp(new HashMap<String, String>());
            colis.addInfoSupp(EInfoSupplementaire.TAXE_VALEUR.getCode(), VALUE_NIMPORTE_QUOI);
            colis.addInfoSupp(EInfoSupplementaire.NO_LT_RETOUR.getCode(), "noLtRetour001");
            specifsColis.add(colis);
        }

        dao.updateSpecifsEvenements(specifsColis);

        SpecifsColis colis1 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("20160101000000");
        SpecifsColis colis2 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("20160102000000");
        SpecifsColis colis3 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("20160201000000");
        SpecifsColis colis4 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("20160301000000");
        SpecifsColis colis5 = UpdateSpecificationsColisDaoUtils.getSpecificationColis("20160401000000");

        assertEquals(ESpecificiteColis.ATTRACTIF.getCode(), colis1.getSpecifsEvt().get(new DateTime(2016, 1, 1, 0, 0, 0).toDate()));
        assertEquals(ESpecificiteColis.SENSIBLE.getCode(), colis2.getSpecifsEvt().get(new DateTime(2016, 1, 2, 0, 0, 0).toDate()));
        assertEquals(ESpecificiteColis.CONSIGNE.getCode(), colis2.getSpecifsEvt().get(new DateTime(2016, 1, 2, 12, 0, 0).toDate()));
        assertEquals(EEtapesColis.RETOUR_AGENCE.getCode(),colis3.getEtapes().get(new DateTime(2016, 2, 1, 0, 0, 0).toDate())) ;
        assertEquals(new DateTime(2016, 2, 29, 0, 0, 0).toDate(),colis4.getDatesContractuelles().get(new DateTime(2016, 3, 1, 0, 0, 0).toDate())) ;
        assertEquals("noLtRetour001",colis5.getInfoSupp().get(EInfoSupplementaire.NO_LT_RETOUR.getCode())) ;
    }

    @AfterClass
    public void tearDownAfterClass() throws Exception {
    	VisionMicroserviceApplication.getCassandraSession().execute(psTruncateColisSpec.getQueryString());
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }
}
