package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.BuildCluster;
import com.chronopost.vision.microservices.CCMBridge;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.transcoder.Transcoder;

/**
 * classe de test du SyntheseTourneeDaoImpl
 * @author jcbontemps
 */
public class SyntheseTourneeDaoTest {

    private boolean suiteLaunch = true;
    private SyntheseTourneeDao dao;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        if (!BuildCluster.clusterHasBuilt) {
            BuildCluster.setUpBeforeSuite();
            suiteLaunch = false;
        }
        CCMBridge.ipOfNode(1);

        dao = SyntheseTourneeDaoImpl.getInstance();
    }

    private void mockTransco(final String active) throws Exception {
        FeatureFlips.INSTANCE.setFlipProjectName("Vision");
    	Map<String, Map<String, String>> map = new  HashMap<>();
    	
    	Map<String, String> idC11Flip = new HashMap<>();
    	idC11Flip.put("idC11Plus", active);
    	map.put("feature_flips", idC11Flip);
    	
    	Map<String, String> evtDPlusTransco = new HashMap<>();
    	evtDPlusTransco.put("evt_Dplus", "|P|D|B|RG|RC|PR|RB|CO|PA|NA|N1|N2|P1|P2|D1|D2|D3|IP|");
    	map.put("parametre_microservices", evtDPlusTransco);
    	
		ConcurrentMap<String, Transcoder> transcoders = new ConcurrentHashMap<>();
		Transcoder transcoderIdC11Plus = new Transcoder();
		transcoderIdC11Plus.setTranscodifications(map);
		transcoders.put("Vision", transcoderIdC11Plus);
		Transcoder transcoderEvtDPlus = new Transcoder();
		transcoderEvtDPlus.setTranscodifications(map);
		transcoders.put("DiffusionVision", transcoderEvtDPlus);
		TranscoderService.INSTANCE.setTranscoders(transcoders);
	}

	@Test(groups = { "database-needed", "slow" })
    public void getPointsTournee_IdC11PlusDisabled() throws Exception {
        mockTransco("FALSE");
        String agence1 = "FTV";
        String agence2 = "NEY";

        String idC11_1_11 = "BIDON01010101010101";
        String idC11_1_12 = "BIDON01234567890123";
        String idC11_2_21 = "BIDON01111111111111";

        String pt1 = "666";
        String pt2 = "667";

        // agence 1, tournéé BIDON01010101010101 , pt1
        String idPointC11_1 = new IdPointC11(agence1, pt1, idC11_1_11).toString();
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "STARK INDUSTRIES", new Date(1456321620125L), "TA",
                null, null, idPointC11_1, null, null);
        // agence 1, tournéé BIDON01010101010101 , pt2
        String idPointC11_2 = new IdPointC11(agence1, pt2, idC11_1_11).toString();
        SyntheseTourneeDaoUtils.insertPoint("JP204948885JB", "P", "NED STARK", new Date(1456308120006L), "D", "-81",
                "12", idPointC11_2, null, null);
        // agence 1, tournéé BIDON01234567890123 , pt1
        String idPointC11_3 = new IdPointC11(agence2, pt1, idC11_1_12).toString();
        SyntheseTourneeDaoUtils.insertPoint("JP204948877JB", "P", "JON SNOW", new Date(1456308120935L), "D", "-81",
                "12", idPointC11_3, null, null);
        // agence 2, tournéé BIDON01111111111111 , pt1
        String idPointC11_4 = new IdPointC11(agence2, pt1, idC11_2_21).toString();
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "MARTELL", new Date(1456314300982L), "D", null, null,
                idPointC11_4, null, null);

        // A ce stade:
        // Tournée BIDON01010101010101 : 1 TA et un D
        // Tournée BIDON01234567890123 : 1 D
        // Tournéé BIDON01111111111111 : 1 D
		// donne des idC11 trop longs, feature désactivée, s'assure que les 19
		// derniers char de l'id formeront l'id à cherche en base
        Tournee tournee1 = dao.getPointsTournee("XRE" + idC11_1_11);
        Tournee tournee2 = dao.getPointsTournee("TEST_XRE" + idC11_1_12);
        Tournee tournee3 = dao.getPointsTournee(idC11_2_21);

        assertEquals(2, tournee1.getPoints().size());
        assertEquals(1, tournee2.getPoints().size());
        assertEquals(1, tournee3.getPoints().size());

        // on verifie que les points ont bien été trié dans l'ordre des dates
        assertEquals(idPointC11_1, tournee1.getPoints().get(1).getIdentifiantPoint());
        assertEquals(idPointC11_2, tournee1.getPoints().get(0).getIdentifiantPoint());

        // on verifie le traitement des TA
        assertEquals(0, tournee2.getPoints().get(0).getColisPrevus().size());
        assertEquals(1, tournee2.getPoints().get(0).getColisPresents().size());

        assertEquals(0, tournee3.getPoints().get(0).getColisPrevus().size());
        assertEquals(1, tournee3.getPoints().get(0).getColisPresents().size());
        assertEquals("E", tournee3.getPoints().get(0).getTypeDestinataire());
        assertEquals("MARTELL", tournee3.getPoints().get(0).getNomDestinataire());
    }

	/**
	 * Test avec flip 'IdC11Plus' activée
	 * Tournée1 : insérée en base avec iDC11 19 char, appelle la méthode avec 22 char,
	 * s'assure qu'une deuxiéme recherche sur l'id tronqué de 3 char se lance
	 * Tournée2 : deux points avec codeTournée. Celui avec le codeTournée dans
	 * l'idC11 doit se retrouver dans la liste retournée idC11 = 27char
	 * Tournée3 : pareil que Tournée2 mais pour dateTournée idC11 = 30char
	 * Tournée4 : pareil mais avec codeTournée et dateTournée idC11 = 35char
	 * @throws Exception 
	 */
    @Test(groups = { "database-needed", "slow" })
    public void getPointsTournee_IdC11PlusEnabled() throws Exception {
        mockTransco("TRUE");
        String agence1 = "FTV";
        String agence2 = "NEY";

        // Pour RG-MSGetSyntTournee-0300 : 1 idC11 sur 19
        String idC11_1_11 = "BIDON01010101010101";
        // 2 idC11 sur 22
        String idC11_1_12 = "BIDON01234567890123456";
        String idC11_2_21 = "BIDON01111111111111111";
        String idC11_2_31 = "BIDON98765432109876543";

        String pt1 = "666";
        String pt2 = "667";

        // agence 1, tournéé BIDON01010101010101 , pt1
        String idPointC11_1 = new IdPointC11(agence1, pt1, idC11_1_11).toString();
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "STARK INDUSTRIES", new Date(1456321620125L), "TA",
                null, null, idPointC11_1, null, null, null, null, null, null);
        // agence 1, tournéé BIDON01010101010101 , pt2
        String idPointC11_2 = new IdPointC11(agence1, pt2, idC11_1_11).toString();
        SyntheseTourneeDaoUtils.insertPoint("JP204948885JB", "P", "NED STARK", new Date(1456308120006L), "D", "-81",
                "12", idPointC11_2, null, null, null, null, null, null);
        // agence 1, tournéé BIDON01234567890123 , pt1, code_tournée = celui de la tournée cherchée
        String idPointC11_3 = new IdPointC11(agence2, pt1, idC11_1_12).toString();
        SyntheseTourneeDaoUtils.insertPoint("JP204948877JB", "P", "JON SNOW", new Date(1456308120935L), "D", "-81",
                "12", idPointC11_3, "codeT", null, null, null, null, null);
        // agence 1, tournéé BIDON01234567890123 , pt1, code_tournée != celui de la tournée cherchée
        String idPointC11_4 = new IdPointC11(agence2, pt1, idC11_1_12).toString();
        SyntheseTourneeDaoUtils.insertPoint("JP204948877JB", "P", "JON SNOW", new Date(1456308120935L), "D", "-81",
                "12", idPointC11_4, "badCode", null, null, null, null, null);
        // agence 2, tournéé BIDON01111111111111 , pt1, date_tournée = celle de la tournée cherchée
        String idPointC11_5 = new IdPointC11(agence2, pt1, idC11_2_21).toString();
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "MARTELL", new Date(1456314300982L), "D", null, null,
                idPointC11_5, null, "dateTour", null, null, null, null);
        // agence 2, tournéé BIDON01111111111111 , pt1, date_tournée != celle de la tournée cherchée
        String idPointC11_6 = new IdPointC11(agence2, pt1, idC11_2_21).toString();
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "MARTELL", new Date(1456314300982L), "D", null, null,
                idPointC11_6, null, "badDate", null, null, null, null);
        
        // Tournée 4
        // agence 2, tournéé BIDON01111111111111 , pt1, date_tournée = celle de la tournée cherchée
        String idPointC11_7 = new IdPointC11(agence2, pt1, idC11_2_31).toString();
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "MARTELL", new Date(1456314300982L), "D", null, null,
        		idPointC11_7, "codeT", "dateTour", null, null, null, null);
        // agence 2, tournéé BIDON01111111111111 , pt1, date_tournée != celle de la tournée cherchée
        String idPointC11_8 = new IdPointC11(agence2, pt1, idC11_2_31).toString();
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "MARTELL", new Date(1456314300982L), "D", null, null,
        		idPointC11_8, "codeT", "badDate", null, null, null, null);
        // agence 2, tournéé BIDON01111111111111 , pt1, date_tournée = celle de la tournée cherchée
        String idPointC11_9 = new IdPointC11(agence2, pt1, idC11_2_31).toString();
        SyntheseTourneeDaoUtils.insertPoint("GR960704576FR", "E", "MARTELL", new Date(1456314300982L), "D", null, null,
        		idPointC11_9, "badCode", "dateTour", null, null, null, null);
        
        // A ce stade:
        // Tournée BIDON01010101010101 : 1 TA et un D
        // Tournée BIDON01234567890123 : 1 D
        // Tournéé BIDON01111111111111 : 1 D
        // feature idC11Plus activée, les id trop longs seront tronqués
        
        // ajoute FAK pour passer sur 22 char
        Tournee tournee1 = dao.getPointsTournee("FAK" + idC11_1_11);
        // ajoute 'codeT' pour code tournée, 
        Tournee tournee2 = dao.getPointsTournee("codeT" + idC11_1_12);
        // ajoute 'dateTour' pour date tournée, 
        Tournee tournee3 = dao.getPointsTournee(DateRules.formatDateYYYYMMDD(new Date(1456314300982L)) + idC11_2_21);
        // ajoute 'dateTour' pour date tournée, 
        Tournee tournee4 = dao.getPointsTournee(DateRules.formatDateYYYYMMDD(new Date(1456314300982L)) + "codeT" + idC11_2_31);

        assertEquals(2, tournee1.getPoints().size());
        // point 3 avec le bon code tournée
        assertEquals(1, tournee2.getPoints().size());
        // point 5 avec la bonne date tournée
        assertEquals(1, tournee3.getPoints().size());
        // point 5 avec la bonne date tournée
        assertEquals(1, tournee4.getPoints().size());

        // on verifie que les points ont bien été trié dans l'ordre des dates
        assertEquals(idPointC11_1, tournee1.getPoints().get(1).getIdentifiantPoint());
        assertEquals(idPointC11_2, tournee1.getPoints().get(0).getIdentifiantPoint());

        // on verifie le traitement des TA
        assertEquals(0, tournee2.getPoints().get(0).getColisPrevus().size());
        assertEquals(1, tournee2.getPoints().get(0).getColisPresents().size());

        assertEquals(0, tournee3.getPoints().get(0).getColisPrevus().size());
        assertEquals(2, tournee3.getPoints().get(0).getColisPresents().size());
        assertEquals("E", tournee3.getPoints().get(0).getTypeDestinataire());
        assertEquals("MARTELL", tournee3.getPoints().get(0).getNomDestinataire());
    }

    @AfterClass
    public void tearDownAfterClass() throws Exception {
        // nettoyage
        SyntheseTourneeDaoUtils.delPointTournee("NEYBIDON66698765432109876543");
        SyntheseTourneeDaoUtils.delPointTournee("NEYBIDON66601234567890123");
        SyntheseTourneeDaoUtils.delPointTournee("NEYBIDON66601111111111111111");
        SyntheseTourneeDaoUtils.delPointTournee("FTVBIDON66701010101010101");
        SyntheseTourneeDaoUtils.delPointTournee("FTVBIDON66601010101010101");
        SyntheseTourneeDaoUtils.delPointTournee("NEYBIDON66601234567890123456");
        SyntheseTourneeDaoUtils.delPointTournee("NEYBIDON66601111111111111");
        if (!suiteLaunch) {
            BuildCluster.tearDownAfterSuite();
        }
    }
}
