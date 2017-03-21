package com.chronopost.vision.microservices.tournee.getalertestournees.v1;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.joda.time.DateTime;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.utils.TypeBorneCreneau;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

/**
 * @author adejanovski JJC getSession.
 *
 */
public class AlertesTourneesDaoImplTest {

    private boolean suiteLaunch = true;
    private IGetAlertesTourneesDao dao;
	private PreparedStatement psCleanLtCreneauAgence;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }
        CCMBridge.ipOfNode(1);

        dao = GetAlertesTourneesDaoImpl.getInstance();
        getSession().execute("INSERT INTO lt_avec_creneau_par_agence (date_jour, code_agence, type_borne_livraison, borne_livraison, no_lt, code_tournee)"
                        + " values('2015-12-10', 'TST', '"
                        + TypeBorneCreneau.BORNE_SUP.getTypeBorne()
                        + "', '2015-12-10 10:00:00', 'EERISQUE001FR','TST00A99')");
        psCleanLtCreneauAgence = getSession().prepare("DELETE FROM lt_avec_creneau_par_agence where date_jour = ? AND code_agence = ?");
    }
    
    private static final Session getSession() {
        return VisionMicroserviceApplication.getCassandraSession();
    }

    /**
     * Vérification d'un retour correct d'une LT sur recherche.
     */
    @Test
    public void GetAlertesTournees() {
        List<String> lts = dao.getNoLtAvecCreneauPourAgence("TST", new DateTime(2015, 12, 10, 9, 35, 0).toDate(),
                new DateTime(2015, 12, 10, 10, 30, 0).toDate(), TypeBorneCreneau.BORNE_SUP);

        assertEquals(lts.size(), 1);
        assertEquals(lts.get(0), "EERISQUE001FR");

    }

    /**
     * Vérification de l'absence de retour en cas de recherche sans
     * correspondance.
     */
    @Test
    public void GetAlertesTourneesNoResults() {
        List<String> lts = dao.getNoLtAvecCreneauPourAgence("TST", new DateTime(2015, 12, 10, 10, 35, 0).toDate(),
                new DateTime(2015, 12, 10, 10, 50, 0).toDate(), TypeBorneCreneau.BORNE_SUP);
        assertEquals(lts.size(), 0);
    }
    
    @AfterClass
    public void tearDownAfterClass() throws Exception {
		getSession().execute(psCleanLtCreneauAgence.bind("2015-12-10", "TST"));
		
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }
}
