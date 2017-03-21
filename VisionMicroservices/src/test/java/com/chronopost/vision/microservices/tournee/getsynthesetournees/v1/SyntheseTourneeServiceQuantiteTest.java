package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.model.getsynthesetournees.v1.ColisPoint;
import com.chronopost.vision.model.getsynthesetournees.v1.EAnomalie;
import com.chronopost.vision.model.getsynthesetournees.v1.PointTournee;
import com.chronopost.vision.model.getsynthesetournees.v1.SyntheseTourneeQuantite;
import com.chronopost.vision.model.updatespecificationscolis.v1.EConsigne;
import com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis;
import com.chronopost.vision.model.updatespecificationscolis.v1.EInfoSupplementaire;
import com.chronopost.vision.model.updatespecificationscolis.v1.ESpecificiteColis;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Test des méthodes de SyntheseTourneeServiceImpl retournant les statistiques (quantité) d'une tournée
 * 
 * @author jcbontemps
 */
public class SyntheseTourneeServiceQuantiteTest extends SyntheseTourneeTestUtils {

    private final SyntheseTourneeServiceImpl service = SyntheseTourneeServiceImpl.INSTANCE;
	private SyntheseTourneeDao mock = mock(SyntheseTourneeDao.class);

	public static final String TOURNEE_ID = "tournee1";
	private static final String EVT_TA = "TA";
	private static final String EVT_RB = "RB";
	private static final String EVT_IP = "IP";
	private static final String EVT_TE = "TE";
	private static final String EVT_D = "D";
	private static final String EVT_P = "P";

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        setTranscos();
		service.setDao(mock);
    }
    
    /**
     * test de la méthode calculSyntheseTournee
     */
    @Test
    public void calculSyntheseTournee() {

        // Explications du test
        // 3 tournées avec chacune 2 colis. Une avec 2 colis TA, une avec 2 sans
        // TA, et une avec 2 dont un TA
        // on va donc vérifier qu'à la fin nous avons 3 colis TA, 3 colis non
        // TA, et 2 tournées avec au moins une TA et 2 tournées avec au moins un
        // colis sans TA

        CollectionTournee collection = new CollectionTournee();
        collection.addToColisPrepares("colis11");
        collection.addToColisPrepares("colis12");
        collection.addToColisPrepares("colis21");

        collection.addToColisTraites("colis22");
        collection.addToColisTraites("colis31");
        collection.addToColisTraites("colis32");

        collection.addToPointsPrepares("pt1");
        collection.addToPointsPrepares("pt2");

        collection.addToPointsTraites("pt2");
        collection.addToPointsTraites("pt3");

        // execution
        SyntheseTourneeQuantite qtte = service.calculSyntheseTournee(collection);

        // vérification
        assertEquals(2, (int) qtte.getNbPtTA());
        assertEquals(2, (int) qtte.getNbPtVisites());
        assertEquals(3, (int) qtte.getNbColisTA());
        assertEquals(3, (int) qtte.getNbColisTraites());
        assertEquals(1, (int) qtte.getNbPtTANonVisites());
    }

    /**
     * test de la méthode genereCollectionTournee ;
     */
    @Test
    public void genereCollectionTourneeCas1() {
        // Explications du test
        // 3 tournées avec chacune 2 colis. Une avec 2 colis TA, une avec 2
        // sans TA, et une avec 2 dont un TA on va donc vérifier qu'à la fin
        // nous avons 3 colis TA, 3 colis non TA, et 2 tournées avec au
        // moins une TA et 2 tournées avec au moins un colis sans TA

        // initialisation
        // 3 points de tournée (1, 2 et 3) dont on initialise chacun le non
        // ainsi que le nombre de colis présents et prévus
        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.setColisPresents(new HashSet<ColisPoint>());
        pt1.setColisPrevus(new HashSet<ColisPoint>());
        pt1.setDatePassage(new Date());

        PointTournee pt2 = new PointTournee();
        pt2.setIdentifiantPoint("pt2");
        pt2.setColisPresents(new HashSet<ColisPoint>());
        pt2.setColisPrevus(new HashSet<ColisPoint>());

        PointTournee pt3 = new PointTournee();
        pt3.setIdentifiantPoint("pt3");
        pt3.setColisPresents(new HashSet<ColisPoint>());
        pt3.setColisPrevus(new HashSet<ColisPoint>());
        pt3.setDatePassage(new Date());

        // 6 colis de nommés colisXY où X est la tournée et Y un ordinal
        // on met 2 colis TA dans la tournée 1, 1 TA 1 D dans la tournée 2
        // et 2 D dans la tournée 3
        ColisPoint colis11 = new ColisPoint();
        colis11.setNo_lt("colis11");
        colis11.setCodeEvenement("TA");
        ColisPoint colis12 = new ColisPoint();
        colis12.setNo_lt("colis12");
        colis12.setCodeEvenement("TA");
        ColisPoint colis21 = new ColisPoint();
        colis21.setNo_lt("colis21");
        colis21.setCodeEvenement("TA");
        ColisPoint colis22 = new ColisPoint();
        colis22.setNo_lt("colis22");
        colis22.setCodeEvenement("D");
        ColisPoint colis31 = new ColisPoint();
        colis31.setNo_lt("colis31");
        colis31.setCodeEvenement("D");
        ColisPoint colis32 = new ColisPoint();
        colis32.setNo_lt("colis32");
        colis32.setCodeEvenement("D");

        pt1.getColisPrevus().add(colis11);
        pt1.getColisPrevus().add(colis12);
        pt2.getColisPrevus().add(colis21);
        pt2.getColisPresents().add(colis22);
        pt3.getColisPresents().add(colis31);
        pt3.getColisPresents().add(colis32);

        List<PointTournee> points = new ArrayList<PointTournee>();
        points.add(pt1);
        points.add(pt2);
        points.add(pt3);

        Tournee tournee = new Tournee();
        tournee.setPoints(points);

        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);

        // verification de l'ordonnancement
        assertEquals("pt1", points.get(0).getIdentifiantPoint());
        assertEquals("pt2", points.get(1).getIdentifiantPoint());
        assertEquals("pt3", points.get(2).getIdentifiantPoint());

        assertEquals(1, (int) points.get(0).getNumPointDistri());
        // Pas de numPtDistri si pas de date de passage
        assertEquals(null, points.get(1).getNumPointDistri());
        assertEquals(2, (int) points.get(2).getNumPointDistri());

        // verification du contenu
        Set<String> colisPrepares = collection.getColisPrepares();
        Set<String> colisTraites = collection.getColisTraites();
        Set<String> pointsPrepares = collection.getPointsPrepares();
        Set<String> pointsTraites = collection.getPointsTraites();

        assertEquals(3, colisPrepares.size());

        assertTrue(colisPrepares.contains("colis11"));
        assertTrue(colisPrepares.contains("colis12"));
        assertTrue(colisPrepares.contains("colis21"));
        assertFalse(colisPrepares.contains("colis22"));
        assertFalse(colisPrepares.contains("colis31"));
        assertFalse(colisPrepares.contains("colis32"));

        assertEquals(3, colisTraites.size());

        assertFalse(colisTraites.contains("colis11"));
        assertFalse(colisTraites.contains("colis12"));
        assertFalse(colisTraites.contains("colis21"));
        assertTrue(colisTraites.contains("colis22"));
        assertTrue(colisTraites.contains("colis31"));
        assertTrue(colisTraites.contains("colis32"));

        assertEquals(2, pointsPrepares.size());
        assertTrue(pointsPrepares.contains("pt1"));
        assertTrue(pointsPrepares.contains("pt2"));
        assertFalse(pointsPrepares.contains("pt3"));

        assertEquals(2, pointsTraites.size());
        assertFalse(pointsTraites.contains("pt1"));
        assertTrue(pointsTraites.contains("pt2"));
        assertTrue(pointsTraites.contains("pt3"));
    }

    /**
     * Test ???
     */
    @Test
    public void genereCollectionTourneeCas2() {
        // initialisation
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        // Colis 1
        ColisPoint evt11 = new ColisPoint();
        evt11.setNo_lt("colis1");
        evt11.setIdentifiantPoint("pt");
        evt11.setCodeEvenement("TA");
        evt11.setDateEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate());
        tournee.putToColisEvenements("colis1", evt11);

        ColisPoint evt12 = new ColisPoint();
        evt12.setNo_lt("colis1");
        evt12.setIdentifiantPoint("pt2");
        evt12.setCodeEvenement("RC");
        evt12.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
        tournee.putToColisEvenements("colis1", evt12);

        ColisPoint evt13 = new ColisPoint();
        evt13.setNo_lt("colis1");
        evt13.setIdentifiantPoint("pt3");
        evt13.setCodeEvenement("RB");
        evt13.setDateEvt(formatter.parseDateTime("07/04/2016 17:05:00").toDate());
        tournee.putToColisEvenements("colis1", evt13);

        // Colis 2
        ColisPoint evt21 = new ColisPoint();
        evt21.setNo_lt("colis2");
        evt21.setIdentifiantPoint("pt");
        evt21.setCodeEvenement("TA");
        evt21.setDateEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate());
        tournee.putToColisEvenements("colis2", evt21);

        ColisPoint evt22 = new ColisPoint();
        evt22.setNo_lt("colis2");
        evt22.setIdentifiantPoint("pt2");
        evt22.setCodeEvenement("D");
        evt22.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
        tournee.putToColisEvenements("colis2", evt22);

        // Points
        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        tournee.addToPoints(pt1);

        PointTournee pt2 = new PointTournee();
        pt2.setIdentifiantPoint("pt2");
        tournee.addToPoints(pt2);

        PointTournee pt3 = new PointTournee();
        pt3.setIdentifiantPoint("pt3");
        tournee.addToPoints(pt3);

        // Specifs colis
        SpecifsColis specifsColis1 = new SpecifsColis();
        Map<Date, String> specifsEvtColis1 = new HashMap<>();
        specifsEvtColis1.put(formatter.parseDateTime("07/04/2016 06:15:00").toDate(), ESpecificiteColis.SENSIBLE.getCode());
        specifsEvtColis1.put(formatter.parseDateTime("07/04/2016 06:15:01").toDate(), ESpecificiteColis.CONSIGNE.getCode());
        specifsColis1.setSpecifsEvt(specifsEvtColis1);
        tournee.putToColisSpecifs("colis1", specifsColis1);

        SpecifsColis specifsColis2 = new SpecifsColis();
        Map<Date, String> specifsEvtColis2 = new HashMap<>();
        Map<Date, Set<String>> specifsServiceColis2 = new HashMap<>();
        Set<String> specsService = newHashSet();
        specsService.add(ESpecificiteColis.SWAP.getCode());
        specsService.add(ESpecificiteColis.REP.getCode());
        specifsServiceColis2.put(formatter.parseDateTime("07/04/2016 06:15:00").toDate(), specsService);
        specifsEvtColis2.put(formatter.parseDateTime("07/04/2016 06:15:02").toDate(), ESpecificiteColis.TAXE.getCode());
        specifsColis2.setSpecifsEvt(specifsEvtColis2);
        specifsColis2.setSpecifsService(specifsServiceColis2);
        tournee.putToColisSpecifs("colis2", specifsColis2);

        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);

        // verification
        assertTrue(collection.getColisAvecConsigne().size() == 1 && collection.getColisAvecConsigne().contains("colis1"));
        assertTrue(collection.getColisSensibles().size() == 1 && collection.getColisSensibles().contains("colis1"));
        assertTrue(collection.getColisAnoSpecifSWAP().size() == 1 && collection.getColisAnoSpecifSWAP().contains("colis2"));
        assertTrue(collection.getColisAnoSpecifREP().size() == 1 && collection.getColisAnoSpecifREP().contains("colis2"));
        assertTrue(collection.getColisAnoSpecifTAXE().size() == 1 && collection.getColisAnoSpecifTAXE().contains("colis2"));
    }

    /**
     * Test ???
     */
    @Test
    public void genereCollectionTourneeCas3() {
        // RG-MSGetSyntTournee-007 & RG-MSGetSyntTournee-016
        // initialisation
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        // Points
        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        tournee.addToPoints(pt1);

        PointTournee pt2 = new PointTournee();
        pt2.setIdentifiantPoint("pt2");
        tournee.addToPoints(pt2);

        // Colis 1
        ColisPoint evt11 = new ColisPoint();
        evt11.setNo_lt("colis1");
        evt11.setIdentifiantPoint("pt1");
        evt11.setCodeEvenement("TA");
        evt11.setDateEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate());
        tournee.putToColisEvenements("colis1", evt11);

        ColisPoint evt12 = new ColisPoint();
        evt12.setNo_lt("colis1");
        evt12.setIdentifiantPoint("pt2");
        evt12.setCodeEvenement("P");
        evt12.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
        tournee.putToColisEvenements("colis1", evt12);

        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);

        // verification
        Multimap<String, String> presentationsInfructueuses = HashMultimap.create();
        presentationsInfructueuses.put("P", "colis1");

        assertEquals(newHashSet("colis1"), collection.getColisEchecPresentation());
        assertEquals(newHashSet("pt2"), collection.getPointsEchecPresentation());
        assertEquals(presentationsInfructueuses.asMap(), collection.getPresentationsInfructueuses());
    }

    /**
     * Test ???
     */
    @Test
    public void genereCollectionTourneeCas4() {
        // RG-MSGetSyntTournee-012/023/024
        // initialisation
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        // Points
        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        tournee.addToPoints(pt1);

        PointTournee pt2 = new PointTournee();
        pt2.setIdentifiantPoint("pt2");
        tournee.addToPoints(pt2);

        PointTournee pt3 = new PointTournee();
        pt3.setIdentifiantPoint("pt3");
        tournee.addToPoints(pt3);

        PointTournee pt4 = new PointTournee();
        pt4.setIdentifiantPoint("pt4");
        tournee.addToPoints(pt4);

        // Colis 1 (cas - pas de colis retour trouvé)
        ColisPoint evt11 = new ColisPoint();
        evt11.setNo_lt("colis1");
        evt11.setIdentifiantPoint("pt1");
        evt11.setCodeEvenement("TA");
        evt11.setDateEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate());
        tournee.putToColisEvenements("colis1", evt11);

        ColisPoint evt12 = new ColisPoint();
        evt12.setNo_lt("colis1");
        evt12.setIdentifiantPoint("pt2");
        evt12.setCodeEvenement("D");
        evt12.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
        tournee.putToColisEvenements("colis1", evt12);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.getSpecifsEvt().put(formatter.parseDateTime("07/04/2016 06:15:00").toDate(), ESpecificiteColis.SWAP.getCode());
        specifsColis1.addInfoSupp(EInfoSupplementaire.NO_LT_RETOUR.getCode(), "colisRetour");
        tournee.putToColisSpecifs("colis1", specifsColis1);

        // Colis 2 (cas - colis aller avec evt PC)
        ColisPoint evt21 = new ColisPoint();
        evt21.setNo_lt("colis2");
        evt21.setIdentifiantPoint("pt1");
        evt21.setCodeEvenement("TA");
        evt21.setDateEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate());
        tournee.putToColisEvenements("colis2", evt21);

        ColisPoint evt22 = new ColisPoint();
        evt22.setNo_lt("colis2");
        evt22.setIdentifiantPoint("pt3");
        evt22.setCodeEvenement("D");
        evt22.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
        tournee.putToColisEvenements("colis2", evt22);

        ColisPoint evt23 = new ColisPoint();
        evt23.setNo_lt("colis2");
        evt23.setIdentifiantPoint("pt3");
        evt23.setCodeEvenement("PC");
        evt23.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:10").toDate());
        tournee.putToColisEvenements("colis2", evt23);

        SpecifsColis specifsColis2 = new SpecifsColis();
        specifsColis2.getSpecifsEvt().put(formatter.parseDateTime("07/04/2016 06:15:00").toDate(), ESpecificiteColis.SWAP.getCode());
        specifsColis2.getInfoSupp().put(EInfoSupplementaire.NO_LT_RETOUR.getCode(), "colisRetour");
        tournee.putToColisSpecifs("colis2", specifsColis2);

        // Colis 3 (cas - colis retour avec evt PC)
        ColisPoint evt31 = new ColisPoint();
        evt31.setNo_lt("colis3");
        evt31.setIdentifiantPoint("pt1");
        evt31.setCodeEvenement("TA");
        evt31.setDateEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate());
        tournee.putToColisEvenements("colis3", evt31);

        ColisPoint evt32 = new ColisPoint();
        evt32.setNo_lt("colis3");
        evt32.setIdentifiantPoint("pt4");
        evt32.setCodeEvenement("D");
        evt32.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
        tournee.putToColisEvenements("colis3", evt32);

        SpecifsColis specifsColis3 = new SpecifsColis();
        specifsColis3.getSpecifsEvt().put(formatter.parseDateTime("07/04/2016 06:15:00").toDate(), ESpecificiteColis.SWAP.getCode());
        specifsColis3.getInfoSupp().put(EInfoSupplementaire.NO_LT_RETOUR.getCode(), "colis4");
        tournee.putToColisSpecifs("colis3", specifsColis3);

        ColisPoint evt41 = new ColisPoint();
        evt41.setNo_lt("colis4");
        evt41.setIdentifiantPoint("pt4");
        evt41.setCodeEvenement("PC");
        evt41.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:15").toDate());
        tournee.putToColisEvenements("colis4", evt41);

        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);

        // verification
        assertEquals(newHashSet("colis1", "colis2", "colis3"), collection.getColisAnoSpecifSWAP());
        assertEquals(newHashSet("pt2"), collection.getPointsAnoSWAPDSansP());
        assertEquals(newHashSet("pt3"), collection.getPointsAnoSWAPDEtP());
    }

    /**
     * Test RG-MSGetSyntTournee-013/014
     */
    @Test
    public void genereCollectionTourneeCas5() {
        // initialisation
        // Initialisation
        String noLt1 = "colis1";
        String noLt2 = "colis2";
        String idPoint1 = "pt1";
        String idPoint2 = "pt2";
        String idPoint3 = "pt3";

        // Colis 1 (consigne RemiseBureau mais colis dépose chez le destinataire)
        ColisPoint evt11 = newColisPoint(noLt1, idPoint1, "TA", "07/04/2016 06:15:00");
        ColisPoint evt12 = newColisPoint(noLt1, idPoint2, "D", "07/04/2016 14:31:00");

        // Colis 2 (consigne MiseADispoAgenre mais colis dépose chez le destinataire)
        ColisPoint evt21 = newColisPoint(noLt2, idPoint1, "TA", "07/04/2016 06:15:00");
        ColisPoint evt22 = newColisPoint(noLt2, idPoint3, "D", "07/04/2016 14:31:00");

        Tournee tournee = newTournee(newHashSet(evt11, evt12, evt21, evt22));

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.getSpecifsEvt().put(parseDateTime("07/04/2016 06:15:00"), ESpecificiteColis.REP.getCode());
        tournee.putToColisSpecifs(noLt1, specifsColis1);

        // Colis 2
        SpecifsColis specifsColis2 = new SpecifsColis();
        specifsColis2.getSpecifsEvt().put(parseDateTime("07/04/2016 06:15:00"), ESpecificiteColis.TAXE.getCode());
        tournee.putToColisSpecifs(noLt2, specifsColis2);

        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);

        // verification
        assertEquals(newHashSet(noLt1), collection.getColisAnoSpecifREP());
        assertEquals(newHashSet(noLt2), collection.getColisAnoSpecifTAXE());
    }

    /**
     * Test RG-MSGetSyntTournee-017 && RG-MSGetSyntTournee-018
     */
    @Test
    public void genereCollectionTourneeCas6() {
        // Initialisation
        String noLt1 = "colis1";
        String noLt2 = "colis2";
        String idPoint1 = "pt1";
        String idPoint2 = "pt2";
        String idPoint3 = "pt3";

        // Colis 1 (consigne RemiseBureau mais colis dépose chez le destinataire)
        ColisPoint evt11 = newColisPoint(noLt1, idPoint1, "TA", "07/04/2016 06:15:00");
        ColisPoint evt12 = newColisPoint(noLt1, idPoint2, "D", "07/04/2016 14:31:00");

        // Colis 2 (consigne MiseADispoAgenre mais colis dépose chez le destinataire)
        ColisPoint evt21 = newColisPoint(noLt2, idPoint1, "TA", "07/04/2016 06:15:00");
        ColisPoint evt22 = newColisPoint(noLt2, idPoint3, "D", "07/04/2016 14:31:00");

        Tournee tournee = newTournee(newHashSet(evt11, evt12, evt21, evt22));

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.getConsignesTraitees().put(parseDateTime("07/04/2016 06:15:00"), "|" + EConsigne.REMISE_BUREAU.getCode());
        tournee.putToColisSpecifs(noLt1, specifsColis1);

        SpecifsColis specifsColis2 = new SpecifsColis();
        specifsColis2.getConsignesTraitees().put(parseDateTime("07/04/2016 06:15:00"), "|" + EConsigne.MISE_A_DISPO_AGENCE.getCode());
        tournee.putToColisSpecifs(noLt2, specifsColis2);

        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);

        // verification
        assertEquals(newHashSet(noLt1, noLt2), collection.getColisAnoConsigneNonRespectee());
        assertEquals(newHashSet(idPoint2, idPoint3), collection.getPointsAnoConsigneNonRespectee());
    }

    /**
     * Test RG-MSGetSyntTournee-019 && RG-MSGetSyntTournee-022
     */
    @Test
    public void genereCollectionTourneeCas7() {
        // initialisation
        String noLt = "colis1";
        String idPoint1 = "pt1";
        String idPoint2 = "pt2";
        String idPoint3 = "pt3";
        ColisPoint evt11 = newColisPoint(noLt, idPoint1, "TA", "07/04/2016 06:15:00");
        ColisPoint evt12 = newColisPoint(noLt, idPoint2, "CO", "07/04/2016 14:31:00");
        ColisPoint evt13 = newColisPoint(noLt, idPoint3, "RB", "07/04/2016 16:15:00");

        Tournee tournee = newTournee(newHashSet(evt11, evt12, evt13));

        SpecifsColis specifsColis = new SpecifsColis();
        specifsColis.getConsignesTraitees().put(parseDateTime("07/04/2016 06:15:00"), "|" + EConsigne.REMISE_TIERS.getCode());
        tournee.putToColisSpecifs(noLt, specifsColis);

        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);

        // verification
        assertEquals(newHashSet("colis1"), collection.getColisAnoConsigneNonRespectee());
        assertEquals(newHashSet("pt3"), collection.getPointsAnoConsigneNonRespectee());
        assertEquals(newHashSet("pt3"), collection.getPointsAnoMadNonPermise());
    }

    /**
     * Test RG-MSGetSyntTournee-020
     */
    @Test
    public void genereCollectionTourneeCas8() {
        // initialisation
        String noLt = "colis1";
        String idPoint1 = "pt1";
        String idPoint2 = "pt2";
        ColisPoint evt11 = newColisPoint(noLt, idPoint1, "TA", "07/04/2016 06:15:00");
        ColisPoint evt12 = newColisPoint(noLt, idPoint2, "CO", "07/04/2016 14:31:00");
        evt12.addToAnomalies(EAnomalie.EVT_NON_PERMIS);

        Tournee tournee = newTournee(newHashSet(evt11, evt12));

        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);

        // verification
        assertEquals(newHashSet(noLt), collection.getColisAnoEvtNonPermis());
        assertEquals(newHashSet(idPoint2), collection.getPointsAnoEvtNonPermis());
    }

    /**
     * Test PSM utilisé pour chaque points avec date d'utilisation
     */
    @Test
    public void genereCollectionTourneeCas9() {
        // initialisation
        String noLt = "colis1";
        String idPoint1 = "pt3";
        // Colis 1 (consigne RemiseBureau mais colis dépose chez le destinataire)
        ColisPoint evt11 = newColisPoint(noLt, idPoint1, "TA", "07/04/2016 06:15:00");
        ColisPoint evt12 = newColisPoint(noLt, idPoint1, "TE", "07/04/2016 14:31:00");
        ColisPoint evt13 = newColisPoint(noLt, idPoint1, "D", "07/04/2016 14:35:00");

        Tournee tournee = newTournee(newHashSet(evt11, evt12, evt13));

        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);

        // verification
        Map<String, Date> expectedMap = new HashMap<>();
        expectedMap.put("PSM-pt3", parseDateTime("07/04/2016 14:35:00"));
        assertEquals(expectedMap, collection.getPsm());
    }

    @Test
    /**
     * RG-MSGetSyntTournee-025 & RG-MSGetSyntTournee-026
     * Entrée: 1 point avec un evt TA, 1 point avec un evt P
     * Sortie: la collection pointAvecColisRetour = 2
     */
    public void genereCollectionRG25_26() {
        Tournee tournee = new Tournee();
        // Points 1 avec 1 evt TA
        PointTournee pt1 = newPointTournee("pt1");
        addColisPointToPointTournee(pt1, newColisPoint("colis1", "pt1", "TA", "07/04/2016 06:15:00"));
        addPointTourneeToTournee(tournee, pt1);
        // Points 2 avec 1 evt P
        PointTournee pt2 = newPointTournee("pt2");
        addColisPointToPointTournee(pt2, newColisPoint("colis2", "pt2", "P", "07/04/2016 14:31:00"));
        addPointTourneeToTournee(tournee, pt2);

        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);

        // verification
        assertEquals(2, collection.getPointAvecColisRetour().size());
    }

    @Test
    /**
     * RG-MSGetSyntTournee-027 
     * Entrée: 1 point avec un evt P et une étape RETOUR_AGENCE
     * Sortie: 1 colis retour, et  1 colis vu en retour et 1 NbColisRetourTraite
     */
    public void genereCollectionRG27_1() {
        Tournee tournee = new Tournee();
        tournee.setIdentifiantTournee("tournee1");
        // Points 1 avec 1 evt SK
        PointTournee pt1 = newPointTournee("pt1");
        addColisPointToPointTournee(pt1, newColisPoint("colis1", "pt1", "P", "07/04/2016 06:15:00"));
        addPointTourneeToTournee(tournee, pt1);

        SpecifsColis specifsColis1 = newSpecifsColis("07/04/2016 06:20:00", EEtapesColis.RETOUR_AGENCE.getCode());
        tournee.putToColisSpecifs("colis1", specifsColis1);

        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);
        SyntheseTourneeQuantite synthese = service.calculSyntheseTournee(collection);

        // verification
        assertEquals(1, collection.getColisVusEnRetour().size());
        assertEquals(new Integer(1), synthese.getNbColisRetourTraite());
        assertEquals(new Integer(1), synthese.getNbColisRetour());
    }

    @Test
    /**
     * RG-MSGetSyntTournee-027 
     * Entrée: 1 point avec un evt P mais sans étape RETOUR_AGENCE
     * Sortie: 1 colis retour, mais 0 colis vu en retour et 0 NbColisRetourTraite
     */
    public void genereCollectionRG27_2() {
        Tournee tournee = new Tournee();
        tournee.setIdentifiantTournee("tournee1");
        // Points 1 avec 1 evt SK
        PointTournee pt1 = newPointTournee("pt1");
        addColisPointToPointTournee(pt1, newColisPoint("colis1", "pt1", "P", "07/04/2016 06:15:00"));
        addPointTourneeToTournee(tournee, pt1);

        SpecifsColis specifsColis1 = newSpecifsColis("07/04/2016 06:20:00", EEtapesColis.LIVRAISON.getCode());
        tournee.putToColisSpecifs("colis1", specifsColis1);

        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);
        SyntheseTourneeQuantite synthese = service.calculSyntheseTournee(collection);

        // verification
        assertEquals(0, collection.getColisVusEnRetour().size());
        assertEquals(new Integer(0), synthese.getNbColisRetourTraite());
        assertEquals(new Integer(1), synthese.getNbColisRetour());
    }

    @Test
    /**
     * RG-MSGetSyntTournee-027 
     * Entrée: 1 point avec un evt D mais avec étape RETOUR_AGENCE
     * Sortie: 0 colis retour, mais 1 colis vu en retour mais 0 dans NbColisRetourTraite
     */
    public void genereCollectionRG27_3() {
        Tournee tournee = new Tournee();
        tournee.setIdentifiantTournee("tournee1");
        // Points 1 avec 1 evt SK
        PointTournee pt1 = newPointTournee("pt1");
        addColisPointToPointTournee(pt1, newColisPoint("colis1", "pt1", "D", "07/04/2016 06:15:00"));
        addPointTourneeToTournee(tournee, pt1);

        SpecifsColis specifsColis1 = newSpecifsColis("07/04/2016 06:20:00", EEtapesColis.RETOUR_AGENCE.getCode());
        tournee.putToColisSpecifs("colis1", specifsColis1);

        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);
        SyntheseTourneeQuantite synthese = service.calculSyntheseTournee(collection);

        // verification
        assertEquals(1, collection.getColisVusEnRetour().size());
        assertEquals(new Integer(0), synthese.getNbColisRetourTraite());
        assertEquals(new Integer(0), synthese.getNbColisRetour());
    }
    
    @Test
    /**
     * RG-MSGetSyntTournee-029
     * Entrée: 1 point avec un evt RB et un specifevt SENSIBLE en métropole
     * Sortie: 1 colis dans ColisAnoEvtNonPermis
     */
    public void genereCollectionRG29_1() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();
        // Points 1 avec 1 evt SK
        PointTournee pt1 = newPointTournee("pt1");
        addColisPointToPointTournee(pt1, newColisPoint("colis1", "pt1", "RB", "07/04/2016 06:15:00"));
        addPointTourneeToTournee(tournee, pt1);

        SpecifsColis specifsColis1 = newSpecifsColis("07/04/2016 06:20:00", EEtapesColis.RETOUR_AGENCE.getCode());
        specifsColis1.addSpecifEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate(), ESpecificiteColis.SENSIBLE.getCode());
        tournee.putToColisSpecifs("colis1", specifsColis1);
        tournee.setIdentifiantTournee("75M1220160101132435");

        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);

        // verification
        assertEquals(1, collection.getColisAnoEvtNonPermis().size());
    }
    
    @Test
    /**
     * RG-MSGetSyntTournee-029
     * Entrée: 1 point avec un evt D et un specifevt SENSIBLE en métropole
     * Sortie: 0 colis dans ColisAnoEvtNonPermis
     */
    public void genereCollectionRG29_2() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();
        // Points 1 avec 1 evt SK
        PointTournee pt1 = newPointTournee("pt1");
        addColisPointToPointTournee(pt1, newColisPoint("colis1", "pt1", "D", "07/04/2016 06:15:00"));
        addPointTourneeToTournee(tournee, pt1);

        SpecifsColis specifsColis1 = newSpecifsColis("07/04/2016 06:20:00", EEtapesColis.RETOUR_AGENCE.getCode());
        specifsColis1.addSpecifEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate(), ESpecificiteColis.SENSIBLE.getCode());
        tournee.putToColisSpecifs("colis1", specifsColis1);
        tournee.setIdentifiantTournee("75M1220160101132435");

        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);

        // verification
        assertEquals(0, collection.getColisAnoEvtNonPermis().size());
    }
    
    @Test
    /**
     * RG-MSGetSyntTournee-029
     * Entrée: 1 point avec un evt RG et un specifevt TAXE en DOM
     * Sortie: 1 colis dans ColisAnoEvtNonPermis
     */
    public void genereCollectionRG29_3() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();
        // Points 1 avec 1 evt SK
        PointTournee pt1 = newPointTournee("pt1");
        addColisPointToPointTournee(pt1, newColisPoint("colis1", "pt1", "RG", "07/04/2016 06:15:00"));
        addPointTourneeToTournee(tournee, pt1);

        SpecifsColis specifsColis1 = newSpecifsColis("07/04/2016 06:20:00", EEtapesColis.RETOUR_AGENCE.getCode());
        specifsColis1.addSpecifEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate(), ESpecificiteColis.TAXE.getCode());
        tournee.putToColisSpecifs("colis1", specifsColis1);
        tournee.setIdentifiantTournee("97M1220160101132435");

        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);

        // verification
        assertEquals(1, collection.getColisAnoEvtNonPermis().size());
    }
    
    @Test
    /**
     * RG-MSGetSyntTournee-029
     * Entrée: 1 point avec un evt RG et un specifevt SENSIBLE en DOM
     * Sortie: 0 colis dans ColisAnoEvtNonPermis
     */
    public void genereCollectionRG29_4() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();
        // Points 1 avec 1 evt SK
        PointTournee pt1 = newPointTournee("pt1");
        addColisPointToPointTournee(pt1, newColisPoint("colis1", "pt1", "RG", "07/04/2016 06:15:00"));
        addPointTourneeToTournee(tournee, pt1);

        SpecifsColis specifsColis1 = newSpecifsColis("07/04/2016 06:20:00", EEtapesColis.RETOUR_AGENCE.getCode());
        specifsColis1.addSpecifEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate(), ESpecificiteColis.SENSIBLE.getCode());
        tournee.putToColisSpecifs("colis1", specifsColis1);
        tournee.setIdentifiantTournee("97M1220160101132435");

        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);

        // verification
        assertEquals(0, collection.getColisAnoEvtNonPermis().size());
    }
    
    @Test
    /**
     * RG-MSGetSyntTournee-010
     * Entrée: 1 point avec un evt TA precocite RDV et creneau et un evt D dans le creneau 
     * Sortie: 1 colis dans ColisHorsDateContractuelle
     */
    public void genereCollectionRG10_1() {
    	final String noLt = "colis10_1";
    	final String idPoint = "point10_1";
    	DateTimeFormatter jourFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");
    	final String ceJour = jourFormatter.print(new DateTime());
    	ColisPoint cpt;
    	
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();
        // Points 1 avec 1 evt SK
        PointTournee pt1 = newPointTournee(idPoint);
        addColisPointToPointTournee(pt1, (cpt=newColisPoint(noLt, idPoint, "TA", ceJour+" 06:15:00")));
        addColisPointToPointTournee(pt1, newColisPoint(noLt, idPoint, "D", ceJour+" 11:15:00"));
        cpt.addInfoSupplementaire(EInfoSupplementaire.CRENEAU_DEBUT,"10:30");
        cpt.addInfoSupplementaire(EInfoSupplementaire.CRENEAU_FIN,"12:30");
        cpt.setPrecocite("RDV");
        addPointTourneeToTournee(tournee, pt1);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.getDatesContractuelles().put(formatter.parseDateTime(ceJour+" 01:15:00").toDate(), formatter.parseDateTime(ceJour+" 18:00:00").toDate());
        tournee.putToColisSpecifs(noLt, specifsColis1);
        
        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);

        // verification
        assertEquals(0, collection.getColisHorsDateContractuelle().size());
        assertEquals(0, collection.getPointsHorsDateContractuelle().size());
    }
    
    @Test
    /**
     * RG-MSGetSyntTournee-010
     * Entrée: 1 point avec un evt TA precocite RDV et creneau et un evt D hors creneau 
     * Sortie: 1 colis dans ColisHorsDateContractuelle
     */
    public void genereCollectionRG10_2() {
    	final String noLt = "colis10_1";
    	final String idPoint = "point10_1";
    	DateTimeFormatter jourFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");
    	final String ceJour = jourFormatter.print(new DateTime());
    	ColisPoint cpt;
    	
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();
        // Points 1 avec 1 evt SK
        PointTournee pt1 = newPointTournee(idPoint);
        addColisPointToPointTournee(pt1, (cpt=newColisPoint(noLt, idPoint, "TA", ceJour+" 06:15:00")));
        addColisPointToPointTournee(pt1, newColisPoint(noLt, idPoint, "D", ceJour+" 13:15:00"));
        cpt.addInfoSupplementaire(EInfoSupplementaire.CRENEAU_DEBUT,"10:30");
        cpt.addInfoSupplementaire(EInfoSupplementaire.CRENEAU_FIN,"12:30");
        cpt.setPrecocite("RDV");
        addPointTourneeToTournee(tournee, pt1);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.getDatesContractuelles().put(formatter.parseDateTime(ceJour+" 01:15:00").toDate(), formatter.parseDateTime(ceJour+" 18:00:00").toDate());
        tournee.putToColisSpecifs(noLt, specifsColis1);
        
        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);

        // verification
        assertEquals(1, collection.getColisHorsDateContractuelle().size());
        assertEquals(1, collection.getPointsHorsDateContractuelle().size());
    }
    
    @Test
    /**
     * RG-MSGetSyntTournee-010
     * Entrée: 1 point avec un evt TA precocite 18H et creneau et un evt D hors creneau mais avant 18H 
     * Sortie: 0 colis dans ColisHorsDateContractuelle
     */
    public void genereCollectionRG10_3() {
    	final String noLt = "colis10_3";
    	final String idPoint = "point10_3";
    	DateTimeFormatter jourFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");
    	final String ceJour = jourFormatter.print(new DateTime());
    	ColisPoint cpt;
    	
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();
        // Points 1 avec 1 evt SK
        PointTournee pt1 = newPointTournee(idPoint);
        addColisPointToPointTournee(pt1, (cpt=newColisPoint(noLt, idPoint, "TA", ceJour+" 06:15:00")));
        addColisPointToPointTournee(pt1, newColisPoint(noLt, idPoint, "D", ceJour+" 13:15:00"));
        cpt.addInfoSupplementaire(EInfoSupplementaire.CRENEAU_DEBUT,"10:30");
        cpt.addInfoSupplementaire(EInfoSupplementaire.CRENEAU_FIN,"12:30");
        cpt.setPrecocite("18H");
        addPointTourneeToTournee(tournee, pt1);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.getDatesContractuelles().put(formatter.parseDateTime(ceJour+" 18:00:00").toDate(), formatter.parseDateTime(ceJour+" 18:00:00").toDate());
        tournee.putToColisSpecifs(noLt, specifsColis1);
        
        // execution
        CollectionTournee collection = service.genereCollectionTournee(tournee);

        // verification
        assertEquals(1, collection.getColisHorsDateContractuelle().size());
        assertEquals(1, collection.getPointsHorsDateContractuelle().size());
    }
    
    /**
     * vérification de la méthode getSyntheseTourneeQuantite Noter la difficulté de compréhension demandée pour l'action
     * de toutes les méthodes intermédiaire entre l'entrée et le résultat Noter que ce test passe par la commande
     * Hystrix
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    @Test
	public void getSyntheseTourneeQuantite() throws InterruptedException, ExecutionException {
		// init
		List<String> points = new ArrayList<String>();
		points.add("test1");

		// execution
		Mockito.when(mock.getPointsTournee(TOURNEE_ID)).thenReturn(getPointsTournee());
		SyntheseTourneeQuantite qtt1 = service.getOneSyntheseQuantiteTournee(TOURNEE_ID);

		// vérification
		assertEquals(Integer.valueOf(2), qtt1.getNbPtTA());
		assertEquals(Integer.valueOf(4), qtt1.getNbColisTA());
		assertEquals(Integer.valueOf(2), qtt1.getNbPtVisites());
		assertEquals(Integer.valueOf(1), qtt1.getNbPtTANonVisites());
		assertTrue(qtt1.getNbColisHorsDateContractuelle() == 1 || qtt1.getNbColisHorsDateContractuelle() == 3);
		assertTrue(newHashSet("colis22", "colis31", "colis32").containsAll(qtt1.getColisHorsDateContractuelle())
				|| newHashSet("colis22").containsAll(qtt1.getColisHorsDateContractuelle()));
		assertEquals(Integer.valueOf(1), qtt1.getNbColisAvecConsigne());
		assertEquals(Integer.valueOf(3), qtt1.getNbColisRetour());
		assertEquals(Integer.valueOf(1), qtt1.getNbColisSecurisesRetour());
		assertEquals(Integer.valueOf(2), qtt1.getNbPointsAvecColisRetour());
		assertEquals(Integer.valueOf(0), qtt1.getNbColisSpecifiques());
		assertEquals(Integer.valueOf(3), qtt1.getNbPtAnomalie());
		assertEquals(Integer.valueOf(4), qtt1.getNbColisTraites());
		assertEquals(Integer.valueOf(0), qtt1.getNbPointsAnomalieTracabilite());
		assertTrue(qtt1.getNbPtMisADispoBureau() == 2 || qtt1.getNbPtMisADispoBureau() == 3);
		assertTrue(qtt1.getNbHorsDelai() == 2 || qtt1.getNbHorsDelai() == 3);
		assertTrue(qtt1.getPresentationsInfructueuses().isEmpty());
		assertEquals(Integer.valueOf(4), qtt1.getNbColisAvecETA());
		assertEquals(Integer.valueOf(2), qtt1.getNbColisHorsETA());
		assertEquals(Integer.valueOf(0), qtt1.getNbColisAvecConsigneNonRespectee());
	}

    /**
     * Créé une nouvelle Tournee à partir d'une liste d'événements
     * 
     * @param pointsTournee
     * @param colisPoints
     * @return
     */
    private Tournee newTournee(Collection<ColisPoint> colisPoints) {
        Tournee tournee = new Tournee();

        SortedMap<String, Date> idPoints = new TreeMap<>();
        for (ColisPoint colisPoint : colisPoints) {
            idPoints.put(colisPoint.getIdentifiantPoint(), colisPoint.getDateEvt());
            tournee.putToColisEvenements(colisPoint.getNo_lt(), colisPoint);
        }

        for (Map.Entry<String, Date> entry : idPoints.entrySet()) {
            PointTournee pt = new PointTournee();
            pt.setIdentifiantPoint(entry.getKey());
            pt.setDatePassage(entry.getValue());
            tournee.addToPoints(pt);
        }
        return tournee;
    }

    /**
     * Créé un nouveau ColisPoint
     * 
     * @param noLt
     * @param idPoint
     * @param codeEvt
     * @param dateEvt
     * @return
     */
    private ColisPoint newColisPoint(String noLt, String idPoint, String codeEvt, String dateEvt) {
        ColisPoint colisPoint = new ColisPoint();
        colisPoint.setNo_lt(noLt);
        colisPoint.setIdentifiantPoint(idPoint);
        colisPoint.setCodeEvenement(codeEvt);
        colisPoint.setDateEvt(parseDateTime(dateEvt));
        colisPoint.setOutilSaisie("PSM-"+idPoint);
        return colisPoint;
    }

    /**
     * Parse une String en date
     * 
     * @param date
     *            String au format "dd/MM/yyyy HH:mm:ss"
     * @return une date
     */
    private Date parseDateTime(String date) {
        return DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss").parseDateTime(date).toDate();
    }

    /**
     * @param dateEvt
     *            : date de de l'étape au format dd/MM/yyyy hh:mm:ss
     * @param etapeColis
     *            : étape
     * @return SpecifsColis
     */
    public SpecifsColis newSpecifsColis(final String dateEvt, final String etapeColis) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        SpecifsColis specifsColis = new SpecifsColis();
        specifsColis.addEtape(formatter.parseDateTime(dateEvt).toDate(), etapeColis);
        return specifsColis;
    }

    /**
     * Ajoute un evenement à un point
     * 
     * @param pt
     *            : le point tournee
     * @param cp
     *            : le colis point
     * @return : le point tournee
     */
    private void addColisPointToPointTournee(final PointTournee pt, final ColisPoint cp) {
        if (cp.getCodeEvenement().equals("TA"))
            pt.getColisPrevus().add(cp);
        else
            pt.getColisPresents().add(cp);

        cp.setIdentifiantPoint(pt.getIdentifiantPoint());
    }

    /**
     * Ajout un point à une tournee (le point doit déjà contenir tous ses colispoint (evenement)
     * 
     * @param t
     *            : la tournee
     * @param pt
     *            : le point tournee à ajouter
     */
    private void addPointTourneeToTournee(final Tournee t, final PointTournee pt) {
        t.addToPoints(pt);
        for (ColisPoint cp : pt.getColisPresents())
            t.putToColisEvenements(cp.getNo_lt(), cp);
        for (ColisPoint cp : pt.getColisPrevus())
            t.putToColisEvenements(cp.getNo_lt(), cp);
    }

    /**
     * @param identifiant
     *            : identifiant du point a creer
     * @return un nouveau PointTournee
     */
    private PointTournee newPointTournee(final String identifiant) {
        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint(identifiant);
        return pt1;
    }
    
    private Tournee getPointsTournee() {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
		Date maintenant = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(maintenant);
		cal.add(Calendar.HOUR, -1);
		Date ilYAUneHeure = cal.getTime();
		cal.set(Calendar.HOUR_OF_DAY, 16);
		Date ceJour16h = cal.getTime();

		String idPt1 = "pt1";
		String idPt2 = "pt2";
		String idPt3 = "pt3";

		String idColis11 = "colis11";
		String idColis12 = "colis12";
		String idColis21 = "colis21";
		String idColis22 = "colis22";
		String idColis31 = "colis31";
		String idColis32 = "colis32";
		String idColis4 = "colis4";

		// création des points de tournée (1, 2, et 3)
		PointTournee pt1 = new PointTournee();
		pt1.setIdentifiantPoint(idPt1);
		pt1.setColisPresents(new HashSet<ColisPoint>());
		pt1.setColisPrevus(new HashSet<ColisPoint>());
		pt1.setDatePassage(formatter.parseDateTime("07/04/2016 06:55:00").toDate());

		PointTournee pt2 = new PointTournee();
		pt2.setIdentifiantPoint(idPt2);
		pt2.setColisPresents(new HashSet<ColisPoint>());
		pt2.setColisPrevus(new HashSet<ColisPoint>());
		pt2.setDatePassage(formatter.parseDateTime("07/04/2016 09:57:00").toDate());

		PointTournee pt3 = new PointTournee();
		pt3.setIdentifiantPoint(idPt3);
		pt3.setColisPresents(new HashSet<ColisPoint>());
		pt3.setColisPrevus(new HashSet<ColisPoint>());
		pt3.setDatePassage(formatter.parseDateTime("07/04/2016 14:30:00").toDate());

		// création de colis
		// chaque colis a pour nom "colisXY" où X est la tournée et Y un numéro
		// (pour la clarté des tests)
		ColisPoint colis11 = new ColisPoint();
		colis11.setNo_lt(idColis11);
		colis11.setCodeEvenement(EVT_TA);
		colis11.setDateEvt(maintenant);
		colis11.setIdentifiantPoint(idPt1);

		ColisPoint colis12 = new ColisPoint();
		colis12.setNo_lt(idColis12);
		colis12.setCodeEvenement(EVT_TA);
		colis12.setDateEvt(maintenant);
		colis12.setIdentifiantPoint(idPt1);

		ColisPoint colis21 = new ColisPoint();
		colis21.setNo_lt(idColis21);
		colis21.setCodeEvenement(EVT_TA);
		colis21.setDateEvt(maintenant);
		colis21.setIdentifiantPoint(idPt2);

		// le colis aura une anomalie HORD_DELAI car livraison 16H > precocité
		// 8H
		ColisPoint colis22 = new ColisPoint();
		colis22.setNo_lt(idColis22);
		colis22.setCodeEvenement(EVT_D);
		colis22.setDateEvt(ceJour16h);
		colis22.setIdentifiantPoint(idPt2);
		colis22.setDiffETA(15);
		colis22.setPrecocite("8H");

		ColisPoint colis31 = new ColisPoint();
		colis31.setNo_lt(idColis31);
		colis31.setCodeEvenement(EVT_RB);
		colis31.setDateEvt(maintenant);
		colis31.setIdentifiantPoint(idPt3);
		colis31.setDiffETA(40);
		// livraision 14h31 - ok car < heure precocité
		colis31.setPrecocite(ESpecificiteColis.DIX_HUIT_HEURE.getCode());

		ColisPoint colis32 = new ColisPoint();
		colis32.setNo_lt(idColis32);
		colis32.setCodeEvenement(EVT_IP);
		colis32.setDateEvt(maintenant);
		colis32.setIdentifiantPoint(idPt3);
		colis32.setDiffETA(40);
		// livraision 14h31 - ok car < heure precocité
		colis32.setPrecocite(ESpecificiteColis.DIX_HUIT_HEURE.getCode());

		ColisPoint colis41 = new ColisPoint();
		colis41.setNo_lt(idColis4);
		colis41.setCodeEvenement(EVT_TA);
		colis41.setDateEvt(formatter.parseDateTime("07/04/2016 06:57:00").toDate());
		colis41.setIdentifiantPoint(idPt1);

		ColisPoint colis42 = new ColisPoint();
		colis42.setNo_lt(idColis4);
		colis42.setCodeEvenement(EVT_P);
		colis42.setDateEvt(formatter.parseDateTime("07/04/2016 09:57:00").toDate());
		colis42.setIdentifiantPoint(idPt2);
		colis42.putToInfosSupplementaires("ID_AVIS_PASSAGE", "XYZ123456789AA");
		colis42.setDiffETA(15);

		ColisPoint colis43 = new ColisPoint();
		colis43.setNo_lt(idColis4);
		colis43.setCodeEvenement(EVT_TE);
		colis43.setDateEvt(formatter.parseDateTime("07/04/2016 09:59:00").toDate());
		colis43.setIdentifiantPoint(idPt2);

		ColisPoint colis44 = new ColisPoint();
		colis44.setNo_lt(idColis4);
		colis44.setCodeEvenement(EVT_RB);
		colis44.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
		colis44.setIdentifiantPoint(idPt3);
		// livraision 14h31 - ok car < heure precocité
		colis44.setPrecocite(ESpecificiteColis.DIX_HUIT_HEURE.getCode());

		// affectation des colis aux point de tournée
		pt1.getColisPrevus().add(colis11);
		pt1.getColisPrevus().add(colis12);
		pt1.getColisPrevus().add(colis41);
		pt2.getColisPrevus().add(colis21);
		pt2.getColisPresents().add(colis22);
		pt3.getColisPresents().add(colis31);
		pt3.getColisPresents().add(colis32);
		pt2.getColisPresents().add(colis42);
		pt2.getColisPresents().add(colis43);
		pt3.getColisPresents().add(colis44);

		// définition des diffETA des points de tournée
		pt1.setDiffETA(null); // ne génère pas d'anomalie
		pt2.setDiffETA(-20); // ne génère pas d'anomalie
		pt3.setDiffETA(40); // génère une anomalie

		Tournee tournee = new Tournee();
		tournee.setIdentifiantTournee(TOURNEE_ID);

		tournee.addToPoints(pt1);
		tournee.addToPoints(pt2);
		tournee.addToPoints(pt3);

		tournee.putToColisEvenements(idColis11, colis11);
		tournee.putToColisEvenements(idColis12, colis12);
		tournee.putToColisEvenements(idColis21, colis21);
		tournee.putToColisEvenements(idColis22, colis22);
		tournee.putToColisEvenements(idColis31, colis31);
		tournee.putToColisEvenements(idColis32, colis32);
		tournee.putToColisEvenements(idColis4, colis41);
		tournee.putToColisEvenements(idColis4, colis42);
		tournee.putToColisEvenements(idColis4, colis43);
		tournee.putToColisEvenements(idColis4, colis44);

		SpecifsColis specifsColis11 = new SpecifsColis();
		specifsColis11.setNoLt(idColis11);
		specifsColis11.setSpecifsEvt(new TreeMap<Date, String>());
		specifsColis11.getSpecifsEvt().put(ilYAUneHeure, ESpecificiteColis.SENSIBLE.getCode());

		SpecifsColis specifsColis22 = new SpecifsColis();
		specifsColis22.setNoLt(idColis22);

		SpecifsColis specifsColis31 = new SpecifsColis();
		specifsColis31.setNoLt(idColis31);
		specifsColis31.setSpecifsService(new TreeMap<Date, Set<String>>());
		specifsColis31.setSpecifsEvt(new TreeMap<Date, String>());
		specifsColis31.getSpecifsEvt().put(formatter.parseDateTime("07/04/2016 14:31:00").toDate(),
				ESpecificiteColis.CONSIGNE.getCode());
		specifsColis31.setConsignesTraitees(new TreeMap<Date, String>());

		SpecifsColis specifsColis32 = new SpecifsColis();
		specifsColis32.setNoLt(idColis32);
		specifsColis32.setSpecifsService(new TreeMap<Date, Set<String>>());
		specifsColis32.setSpecifsEvt(new TreeMap<Date, String>());
		specifsColis32.setConsignesTraitees(new TreeMap<Date, String>());
		specifsColis32.getConsignesTraitees().put(new Date(new Date().getTime() - 2000),
				"|" + EConsigne.REMISE_BUREAU.getCode());

		SpecifsColis specifsColis4 = new SpecifsColis();
		specifsColis4.setNoLt(idColis4);
		specifsColis4.setSpecifsService(new TreeMap<Date, Set<String>>());
		specifsColis4.setSpecifsEvt(new TreeMap<Date, String>());
		specifsColis4.setConsignesTraitees(new TreeMap<Date, String>());

		tournee.putToColisSpecifs(idColis11, specifsColis11);
		tournee.putToColisSpecifs(idColis22, specifsColis22);
		tournee.putToColisSpecifs(idColis31, specifsColis31);
		tournee.putToColisSpecifs(idColis32, specifsColis32);
		tournee.putToColisSpecifs(idColis4, specifsColis4);

		return tournee;
	}
}
